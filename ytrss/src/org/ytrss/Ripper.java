package org.ytrss;

import java.io.File;
import java.sql.Timestamp;
import java.util.concurrent.TimeUnit;

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

import com.google.common.base.Throwables;

@Component
public class Ripper {

	private static final long	DELAY	= TimeUnit.MINUTES.toMillis(30);

	private Long				lastExecuted;

	private volatile boolean	active;

	@Autowired
	private ChannelDAO			channelDAO;

	@Autowired
	private VideoDAO			videoDAO;

	@Autowired
	private Transcoder			transcoder;

	public long getCountdown() {
		if (active || lastExecuted == null) {
			return 0;
		}
		else {
			return (lastExecuted + DELAY) - System.currentTimeMillis();
		}
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

	private File download(final VideoPage videoPage, final Video video, final StreamMapEntry bestEntry) {
		try {
			updateVideoState(video, VideoState.DOWNLOADING);
			return new Downloader().download(bestEntry);
		}
		catch (final Exception e) {
			updateVideoState(video, VideoState.DOWNLOADING_FAILED, e);
			throw Throwables.propagate(e);
		}
	}

	private boolean hasBeenRipped(final VideoPage videoPage) {
		return videoDAO.findByYoutubeID(videoPage.getVideoID()) != null;
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

	private void rip(final Channel channel) {
		final ChannelPage channelPage = openChannelPage(channel);
		for (final ContentGridEntry contentEntry : channelPage.getContentGridEntries()) {
			final VideoPage videoPage = openVideoPage(contentEntry);
			if (!hasBeenRipped(videoPage)) {
				rip(channel, videoPage);
			}
		}
	}

	private void rip(final Channel channel, final VideoPage videoPage) {
		final Video video = createVideo(channel, videoPage);

		final StreamMapEntry bestEntry = new StreamMapEntryScorer().findBestEntry(videoPage.getStreamMapEntries());

		final File videoFile = download(videoPage, video, bestEntry);

		try {
			updateVideoState(video, VideoState.TRANSCODING);
			transcoder.transcode(videoFile);
			updateVideoState(video, VideoState.READY);
		}
		catch (final Exception e) {
			updateVideoState(video, VideoState.TRANSCONDING_FAILED, e);
			throw Throwables.propagate(e);
		}
	}

	private void updateVideoState(final Video video, final VideoState state) {
		video.setState(state);
		video.setErrorMessage(null);
		videoDAO.persist(video);
	}

	private void updateVideoState(final Video video, final VideoState state, final Exception e) {
		video.setState(state);
		video.setErrorMessage(e.getMessage());
		videoDAO.persist(video);
	}
}
