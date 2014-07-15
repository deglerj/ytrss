package org.ytrss;

import java.io.File;
import java.sql.Timestamp;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import javax.annotation.PostConstruct;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.ytrss.db.Channel;
import org.ytrss.db.ChannelDAO;
import org.ytrss.db.Video;
import org.ytrss.db.VideoDAO;
import org.ytrss.db.VideoState;
import org.ytrss.pages.ChannelPage;
import org.ytrss.pages.ContentGridEntry;
import org.ytrss.pages.StreamMapEntry;
import org.ytrss.pages.StreamMapEntryScorer;
import org.ytrss.pages.VideoPage;
import org.ytrss.transcoders.Transcoder;

@Component
public class Ripper {

	private static final long			DELAY	= TimeUnit.MINUTES.toMillis(30);

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

	public long getCountdown() {
		if (active || lastExecuted == null) {
			return 0;
		}
		else {
			return (lastExecuted + DELAY) - System.currentTimeMillis();
		}
	}

	@PostConstruct
	public void resumeAfterRestart() {
		// TODO Spawn new thread, doing this synchronously slows down the server startup
		resumeTranscoding();
		resumeDownloads();
	}

	@Scheduled(fixedDelay = 1000)
	public void schedule() {
		if (!active && delayExpired()) {
			start();
		}
	}

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

	private Video createVideo(final Channel channel, final VideoPage videoPage) {
		final Video video = new Video();
		video.setChannelID(channel.getId());
		video.setDiscovered(new Timestamp(System.currentTimeMillis()));
		video.setName(videoPage.getTitle());
		video.setState(VideoState.NEW);
		video.setYoutubeID(videoPage.getVideoID());
		video.setUploaded(videoPage.getUploaded());
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
		System.out.println("REQUESTING DOWNLOAD FOR " + video.getName());

		updateVideoState(video, VideoState.DOWNLOADING_ENQUEUED);

		downloader.download(video, entry, nil -> updateVideoState(video, VideoState.DOWNLOADING), videoFile -> {
			onDownloadComplete(video, videoFile);
		}, t -> {
			onDownloadFailed(video, t, entry);
		});

	}

	private boolean hasBeenRipped(final VideoPage videoPage) {
		return videoDAO.findByYoutubeID(videoPage.getVideoID()) != null;
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
		String url = channel.getUrl();
		if (!url.endsWith("/videos")) {
			url += "/videos";
		}

		return new ChannelPage(URLs.copyToString(url));
	}

	private VideoPage openVideoPage(final ContentGridEntry contentEntry) {
		final String url = "http://youtube.com" + contentEntry.getHref() + "&gl=gb&hl=en"; // Force locale to make date parsing easier
		return new VideoPage(URLs.copyToString(url));
	}

	private VideoPage openVideoPage(final Video video) {
		final String url = "http://youtube.com/watch?v=" + video.getYoutubeID() + "&gl=gb&hl=en"; // Force locale to make date parsing easier
		return new VideoPage(URLs.copyToString(url));
	}

	private void resumeDownloads() {
		for (final Video video : videoDAO.findAll()) {
			if (video.getState() == VideoState.DOWNLOADING || video.getState() == VideoState.DOWNLOADING_ENQUEUED
					|| video.getState() == VideoState.DOWNLOADING_FAILED) {
				System.out.println("RESUME DOWNLOADING FOR " + video.getName());
				final VideoPage videoPage = openVideoPage(video);
				final StreamMapEntry bestEntry = streamMapEntryScorer.findBestEntry(videoPage.getStreamMapEntries());
				download(video, bestEntry);
			}
		}
	}

	private void resumeTranscoding() {
		for (final Video video : videoDAO.findAll()) {
			if (video.getState() == VideoState.TRANSCODING || video.getState() == VideoState.TRANSCODING_ENQUEUED
					|| video.getState() == VideoState.TRANSCODING_FAILED) {
				System.out.println("RESUME TRANSCODING FOR " + video.getName());
				final File videoFile = new File(video.getVideoFile());
				transcode(videoFile, video);
			}
		}
	}

	private void rip(final Channel channel) {
		final ChannelPage channelPage = openChannelPage(channel);
		for (final ContentGridEntry contentEntry : channelPage.getContentGridEntries()) {
			final VideoPage videoPage = openVideoPage(contentEntry);
			if (!hasBeenRipped(videoPage)) {
				final Video video = createVideo(channel, videoPage);

				final StreamMapEntry bestEntry = streamMapEntryScorer.findBestEntry(videoPage.getStreamMapEntries());
				download(video, bestEntry);
			}
		}
	}

	private void transcode(final File videoFile, final Video video) {
		System.out.println("REQUESTING TRANSCODING FOR " + video.getName());

		updateVideoState(video, VideoState.TRANSCODING_ENQUEUED);

		transcoder.transcode(videoFile, video, nil -> updateVideoState(video, VideoState.TRANSCODING), mp3File -> {
			onTranscodeComplete(mp3File, video);
		}, t -> {
			onTranscodeFailed(videoFile, video, t);
		});
	}

	private void updateVideoState(final Video video, final VideoState state) {
		System.out.println("STATE " + state + " FOR " + video.getName());

		video.setState(state);
		video.setErrorMessage(null);
		videoDAO.persist(video);
	}

	private void updateVideoState(final Video video, final VideoState state, final Throwable t) {
		System.out.println("STATE " + state + " FOR " + video.getName());

		video.setState(state);
		video.setErrorMessage(t.getMessage());
		videoDAO.persist(video);
	}
}
