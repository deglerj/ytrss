package org.ytrss;

import java.io.File;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.regex.Pattern;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.ytrss.db.Channel;
import org.ytrss.db.ChannelDAO;
import org.ytrss.db.ServerStateChangeEvent;
import org.ytrss.db.SettingsService;
import org.ytrss.db.Video;
import org.ytrss.db.VideoDAO;
import org.ytrss.db.VideoState;
import org.ytrss.transcoders.Transcoder;
import org.ytrss.youtube.ChannelPage;
import org.ytrss.youtube.ContentGridEntry;
import org.ytrss.youtube.StreamMapEntry;
import org.ytrss.youtube.StreamMapEntryScorer;
import org.ytrss.youtube.VideoPage;

import com.google.common.base.Strings;
import com.google.common.eventbus.EventBus;

@Component
public class Ripper {

	private static Logger				log	= LoggerFactory.getLogger(Ripper.class);

	private Long						lastExecuted;

	private volatile boolean			active;

	@Autowired
	private ChannelDAO					channelDAO;

	@Autowired
	private VideoDAO					videoDAO;

	@Autowired
	private Transcoder					transcoder;

	@Autowired
	private StreamDownloader			downloader;

	@Autowired
	private ScheduledExecutorService	scheduledExecutor;

	@Autowired
	private StreamMapEntryScorer		streamMapEntryScorer;

	@Autowired
	private ID3Tagger					id3Tagger;

	@Autowired
	private EventBus					eventBus;

	@Autowired
	private SettingsService				settings;

	public void download(final Video video) {
		final VideoPage videoPage = openVideoPage(video);
		final StreamMapEntry bestEntry = streamMapEntryScorer.findBestEntry(videoPage.getStreamMapEntries());
		download(video, bestEntry);
	}

	public long getCountdown() {
		if (active || lastExecuted == null) {
			return 0;
		}
		else {
			return (lastExecuted + getDelay()) - System.currentTimeMillis();
		}
	}

	public boolean isActive() {
		return active;
	}

	@Async
	public synchronized void start() {
		active = true;

		eventBus.post(new ServerStateChangeEvent());

		try {
			for (final Channel channel : channelDAO.findAll()) {
				rip(channel);
			}
		}
		finally {
			active = false;
			lastExecuted = System.currentTimeMillis();

			eventBus.post(new ServerStateChangeEvent());
		}
	}

	public void transcode(final File videoFile, final Video video) {
		log.info("Requesting transcoding for " + video.getName());

		updateVideoState(video, VideoState.TRANSCODING_ENQUEUED);

		transcoder.transcode(videoFile, video, nil -> {
			log.info("Started transcoding of " + video.getName());
			updateVideoState(video, VideoState.TRANSCODING);
		}, mp3File -> {
			log.info("Completed transcoding " + video.getName());
			onTranscodeComplete(mp3File, video);
		}, t -> {
			log.warn("Transcoding failed for " + video.getName(), t);
			onTranscodeFailed(videoFile, video, t);
		});
	}

	private boolean delayExpired() {
		if (lastExecuted == null) {
			return true;
		}
		else {
			return System.currentTimeMillis() - lastExecuted >= getDelay();
		}
	}

	private void download(final Video video, final StreamMapEntry entry) {
		log.info("Requesting download for " + video.getName());

		updateVideoState(video, VideoState.DOWNLOADING_ENQUEUED);

		downloader.download(video, entry, nil -> {
			log.info("Started download of " + video.getName());
			updateVideoState(video, VideoState.DOWNLOADING);
		}, videoFile -> {
			log.info("Completed download of " + video.getName());
			onDownloadComplete(video, videoFile);
		}, t -> {
			log.error("Download failed for " + video.getName(), t);
			onDownloadFailed(video, t, entry);
		});

	}

	private long getDelay() {
		final int delayMinutes = settings.getSetting("delay", Integer.class);
		return TimeUnit.MINUTES.toMillis(delayMinutes);
	}

	private boolean isExcluded(final ContentGridEntry contentEntry, final Channel channel) {
		if (Strings.isNullOrEmpty(channel.getExcludeRegex())) {
			return false;
		}

		return Pattern.compile(channel.getExcludeRegex(), Pattern.CASE_INSENSITIVE).matcher(contentEntry.getTitle()).matches();
	}

	private boolean isIncluded(final ContentGridEntry contentEntry, final Channel channel) {
		if (Strings.isNullOrEmpty(channel.getIncludeRegex())) {
			return true;
		}

		return Pattern.compile(channel.getIncludeRegex(), Pattern.CASE_INSENSITIVE).matcher(contentEntry.getTitle()).matches();
	}

	private void markAsSkipped(final Channel channel, final ContentGridEntry entry, final VideoState state) {
		final VideoPage videoPage = openVideoPage(entry);
		final Video video = videoDAO.create(channel, videoPage);
		video.setState(state);
		videoDAO.persist(video);
	}

	private void onDownloadComplete(final Video video, final File videoFile) {
		video.setVideoFile(videoFile.getAbsolutePath());

		transcode(videoFile, video);
	}

	private void onDownloadFailed(final Video video, final Throwable error, final StreamMapEntry entry) {
		// Mark as failed
		updateVideoState(video, VideoState.DOWNLOADING_FAILED, error);

		// Retry in 5 minutes
		scheduledExecutor.schedule(() -> {
			download(video, entry);
		}, 5, TimeUnit.MINUTES);
	}

	private void onTranscodeComplete(final File mp3File, final Video video) {
		id3Tagger.tag(mp3File, video);

		video.setMp3File(mp3File.getAbsolutePath());
		updateVideoState(video, VideoState.READY);

		// Delete video file
		final File videoFile = new File(video.getVideoFile());
		videoFile.delete();
	}

	private void onTranscodeFailed(final File videoFile, final Video video, final Throwable errors) {
		log.error("Transconding of file \"" + videoFile.getAbsolutePath() + "\" belonging to video \"" + video + "\" failed", errors);

		// Mark as failed
		updateVideoState(video, VideoState.TRANSCODING_FAILED, errors);

		// Retry in 5 minutes
		scheduledExecutor.schedule(() -> {
			transcode(videoFile, video);
		}, 5, TimeUnit.MINUTES);
	}

	private ChannelPage openChannelPage(final Channel channel) {
		final String url = URLs.cleanUpURL(channel.getUrl()) + "/videos";
		return new ChannelPage(URLs.getSource(url, false));
	}

	private VideoPage openVideoPage(final ContentGridEntry contentEntry) {
		final String url = "http://youtube.com" + contentEntry.getHref() + "&gl=gb&hl=en"; // Force locale to make date parsing easier
		return new VideoPage(URLs.getSource(url, false));
	}

	private VideoPage openVideoPage(final Video video) {
		final String url = "http://youtube.com/watch?v=" + video.getYoutubeID() + "&gl=gb&hl=en"; // Force locale to make date parsing easier
		return new VideoPage(URLs.getSource(url, false));
	}

	private void rip(final Channel channel) {
		final ChannelPage channelPage = openChannelPage(channel);
		for (final ContentGridEntry entry : channelPage.getContentGridEntries(channel.getMaxVideos())) {
			// Get existing video for this entry (or null if it's a new entry)
			final Video video = videoDAO.findByYoutubeID(entry.getVideoID());

			// New entry? -> Start ripping
			if (video == null) {
				ripNew(channel, entry);
				continue;
			}

			// Previously processed entry? -> Decide depending on video state
			switch (video.getState()) {
				case DELETED:
				case READY:
				case DOWNLOADING:
				case DOWNLOADING_ENQUEUED:
				case TRANSCODING:
				case TRANSCODING_ENQUEUED:
				case EXCLUDED:
				case NOT_INCLUDED:
					log.debug("Skipping YouTube entry \"{}\" with video state {} (no action necessary)", entry.getTitle(), video.getState());
					// Nothing to do
					break;

				case NEW:
				case DOWNLOADING_FAILED:
				case TRANSCODING_FAILED:
					log.info("Starting ripping of previously processed YouTube entry \"{}\" with video state {}", entry.getTitle(), video.getState());
					ripExisting(entry, video);
					break;
			}
		}
	}

	private void ripExisting(final ContentGridEntry entry, final Video video) {
		final VideoPage videoPage = openVideoPage(entry);
		final StreamMapEntry bestEntry = streamMapEntryScorer.findBestEntry(videoPage.getStreamMapEntries());
		download(video, bestEntry);
	}

	private void ripNew(final Channel channel, final ContentGridEntry entry) {
		if (isExcluded(entry, channel)) {
			log.info("Skipping new YouTube entry \"{}\": matches the channel's exclude regex", entry.getTitle());
			markAsSkipped(channel, entry, VideoState.EXCLUDED);
			return;
		}

		if (!isIncluded(entry, channel)) {
			log.info("Skipping new YouTube entry \"{}\": does not match the channel's include regex", entry.getTitle());
			markAsSkipped(channel, entry, VideoState.NOT_INCLUDED);
			return;
		}

		log.info("Starting ripping of new YouTube entry \"{}\"", entry.getTitle());

		final VideoPage videoPage = openVideoPage(entry);
		final Video video = videoDAO.create(channel, videoPage);

		ripExisting(entry, video);
	}

	@Scheduled(fixedDelay = 1000)
	private void schedule() {
		if (!active && delayExpired()) {
			start();
		}
	}

	private void updateVideoState(final Video video, final VideoState state) {
		video.setState(state);
		video.setErrorMessage(null);
		videoDAO.persist(video);
	}

	private void updateVideoState(final Video video, final VideoState state, final Throwable t) {
		video.setState(state);
		video.setErrorMessage(t.getMessage());
		videoDAO.persist(video);
	}
}
