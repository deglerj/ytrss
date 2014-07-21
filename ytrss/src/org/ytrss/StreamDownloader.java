package org.ytrss;

import java.io.File;
import java.net.URL;
import java.util.function.Consumer;

import org.apache.commons.io.FileUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.ytrss.db.Video;
import org.ytrss.db.VideoDAO;
import org.ytrss.db.VideoState;
import org.ytrss.db.Videos;
import org.ytrss.youtube.StreamMapEntries;
import org.ytrss.youtube.StreamMapEntry;

@Component
public class StreamDownloader {

	private static Logger	log	= LoggerFactory.getLogger(StreamDownloader.class);

	@Autowired
	private VideoDAO		videoDAO;

	@Async("streamDownloader")
	public void download(Video video, final StreamMapEntry entry, final Consumer<Void> started, final Consumer<File> downloaded,
			final Consumer<Throwable> failed) {
		// Reload video from DB in case it has changed (e.g. state has been altered)
		video = videoDAO.findById(video.getId());

		// Make sure the video has not been deleted or reset while being enqueued for downloading
		if (isInvalid(video)) {
			log.info("Skipped downloading video \"" + video.getName() + "\" because it was already deleted or is in an invalid state");
			return;
		}

		started.accept(null);

		final String userHome = System.getProperty("user.home");
		final String fileName = userHome + "/.ytrss/videos/" + Videos.getFileName(video) + "." + StreamMapEntries.getExtension(entry);

		final File file = new File(fileName);
		try {
			FileUtils.copyURLToFile(new URL(entry.getUrl()), file);
		}
		catch (final Throwable t) {
			failed.accept(t);
			return;
		}

		downloaded.accept(file);
	}

	private boolean isDeleted(final Video video) {
		try {
			videoDAO.findById(video.getId());
			return false;
		}
		catch (final EmptyResultDataAccessException e) {
			return true;
		}
	}

	private boolean isInvalid(final Video video) {
		return isDeleted(video) || isInvalidState(video.getState());
	}

	private boolean isInvalidState(final VideoState state) {
		return !(state == VideoState.NEW || state == VideoState.DOWNLOADING_ENQUEUED || state == VideoState.DOWNLOADING_FAILED);
	}

}
