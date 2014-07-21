package org.ytrss.transcoders;

import java.io.File;
import java.util.function.Consumer;

import org.apache.commons.exec.CommandLine;
import org.apache.commons.exec.DefaultExecutor;
import org.apache.commons.exec.LogOutputStream;
import org.apache.commons.exec.PumpStreamHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.scheduling.annotation.Async;
import org.ytrss.db.Video;
import org.ytrss.db.VideoDAO;
import org.ytrss.db.VideoState;
import org.ytrss.db.Videos;

public class FFMPEGCommandTranscoder implements Transcoder {

	private static class StringOutputStream extends LogOutputStream {

		private final StringBuffer	lines	= new StringBuffer();

		@Override
		public String toString() {
			return lines.toString();
		}

		@Override
		protected void processLine(final String line, final int level) {
			if (lines.length() != 0) {
				lines.append("\n");
			}
			lines.append(line);
		}
	}

	@Autowired
	private VideoDAO		videoDAO;

	private static Logger	log	= LoggerFactory.getLogger(FFMPEGCommandTranscoder.class);

	@Override
	@Async("transcoder")
	public void transcode(final File videoFile, Video video, final Consumer<Void> started, final Consumer<File> transcoded, final Consumer<Throwable> failed) {
		// Reload video from DB in case it has changed (e.g. state has been altered)
		video = videoDAO.findById(video.getId());

		// Make sure the video has not been deleted or reset while being enqueued for transcoding
		if (isInvalid(video)) {
			log.info("Skipped transcoding video \"" + video.getName() + "\" because it was already deleted or is an invalid state");
			return;
		}

		started.accept(null);

		log.info("Transcoding " + videoFile.getName());

		final String userHome = System.getProperty("user.home");
		final String fileName = userHome + "/.ytrss/mp3s/" + Videos.getFileName(video) + ".mp3";

		final File mp3File = new File(fileName);

		try {
			final String command = "ffmpeg -y -loglevel panic -i \"" + videoFile.getAbsolutePath() + "\" -b:a 128K -vn \"" + mp3File.getAbsolutePath() + "\"";
			final CommandLine cmdLine = CommandLine.parse(command);
			final DefaultExecutor executor = new DefaultExecutor();
			executor.setWorkingDirectory(new File(userHome + "/.ytrss"));
			final StringOutputStream output = new StringOutputStream();
			executor.setStreamHandler(new PumpStreamHandler(output, output));
			final int exitValue = executor.execute(cmdLine);
			if (executor.isFailure(exitValue)) {
				throw new RuntimeException("Transconding failed. ffmpeg output was:\n" + output.toString());
			}

		}
		catch (final Throwable t) {
			failed.accept(t);
			return;
		}

		transcoded.accept(mp3File);
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
		return !(state == VideoState.TRANSCODING_ENQUEUED || state == VideoState.TRANSCODING_FAILED);
	}

}
