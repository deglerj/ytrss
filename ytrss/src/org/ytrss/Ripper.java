package org.ytrss;

import java.io.File;
import java.sql.Timestamp;
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

@Component
public class Ripper {

	private static final long			DELAY	= TimeUnit.MINUTES.toMillis(30);

	private static Logger				log		= LoggerFactory.getLogger(Ripper.class);

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
			return (lastExecuted + DELAY) - System.currentTimeMillis();
		}
	}

	public boolean isActive() {
		return active;
	}

	@Async
	public synchronized void start() {
		active = true;

		try {
			for (final Channel channel : channelDAO.findAll()) {
				rip(channel);
			}
		}
		finally {
			active = false;
			lastExecuted = System.currentTimeMillis();
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

	private Video createVideo(final Channel channel, final VideoPage videoPage) {
		final Video video = new Video();
		video.setChannelID(channel.getId());
		video.setDiscovered(new Timestamp(System.currentTimeMillis()));
		video.setName(videoPage.getTitle());
		video.setState(VideoState.NEW);
		video.setYoutubeID(videoPage.getVideoID());
		video.setUploaded(videoPage.getUploaded());
		video.setDescription(videoPage.getDescription());
		videoDAO.persist(video);
		return video;
	}

	private boolean delayExpired() {
		if (lastExecuted == null) {
			return true;
		}
		else {
			return System.currentTimeMillis() - lastExecuted >= Ripper.DELAY;
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

	private boolean hasBeenRipped(final VideoPage videoPage) {
		return videoDAO.findByYoutubeID(videoPage.getVideoID()) != null;
	}

	private boolean matchesPatterns(final ContentGridEntry contentEntry, final Channel channel) {
		boolean included = true;
		if (!Strings.isNullOrEmpty(channel.getIncludeRegex())) {
			included = Pattern.compile(channel.getIncludeRegex(), Pattern.CASE_INSENSITIVE).matcher(contentEntry.getTitle()).matches();
		}

		boolean excluded = false;
		if (!Strings.isNullOrEmpty(channel.getExcludeRegex())) {
			excluded = Pattern.compile(channel.getExcludeRegex(), Pattern.CASE_INSENSITIVE).matcher(contentEntry.getTitle()).matches();
		}

		return included && !excluded;
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
		// Mark as failed
		updateVideoState(video, VideoState.TRANSCODING_FAILED, errors);

		// Retry in 5 minutes
		scheduledExecutor.schedule(() -> {
			transcode(videoFile, video);
		}, 5, TimeUnit.MINUTES);
	}

	private ChannelPage openChannelPage(final Channel channel) {
		final String url = URLs.cleanUpURL(channel.getUrl()) + "/videos";
		return URLs.openPage(url, 10, s -> new ChannelPage(s));
	}

	private VideoPage openVideoPage(final ContentGridEntry contentEntry) {
		final String url = "http://youtube.com" + contentEntry.getHref() + "&gl=gb&hl=en"; // Force locale to make date parsing easier
		return URLs.openPage(url, 10, s -> new VideoPage(s));
	}

	private VideoPage openVideoPage(final Video video) {
		final String url = "http://youtube.com/watch?v=" + video.getYoutubeID() + "&gl=gb&hl=en"; // Force locale to make date parsing easier
		return URLs.openPage(url, 10, s -> new VideoPage(s));
	}

	private void rip(final Channel channel) {
		final ChannelPage channelPage = openChannelPage(channel);
		for (final ContentGridEntry contentEntry : channelPage.getContentGridEntries()) {
			if (!matchesPatterns(contentEntry, channel)) {
				continue;
			}

			final VideoPage videoPage = openVideoPage(contentEntry);
			if (!hasBeenRipped(videoPage)) {
				final Video video = createVideo(channel, videoPage);

				final StreamMapEntry bestEntry = streamMapEntryScorer.findBestEntry(videoPage.getStreamMapEntries());
				download(video, bestEntry);
			}
		}
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
