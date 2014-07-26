package org.ytrss.transcoders;

import java.io.File;
import java.io.IOException;
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
import org.ytrss.db.SettingsService;
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

	public static boolean isFfmpegAvailable() {
		try {
			final String command = "ffmpeg -version";
			final CommandLine cmdLine = CommandLine.parse(command);
			final DefaultExecutor executor = new DefaultExecutor();
			final StringOutputStream output = new StringOutputStream();
			executor.setStreamHandler(new PumpStreamHandler(output, output));
			int exitValue;
			exitValue = executor.execute(cmdLine);
			return !executor.isFailure(exitValue);
		}
		catch (final IOException e) {
			log.warn("ffmpeg detection failed", e);
			return false;
		}

	}

	@Autowired
	private VideoDAO		videoDAO;

	@Autowired
	private SettingsService	settingsService;

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

		final String fileName = settingsService.getSetting("files", String.class) + File.separator + "mp3s" + File.separator + Videos.getFileName(video)
				+ ".mp3";

		final File mp3File = new File(fileName);

		try {
			final String command = "ffmpeg -y -i \"" + videoFile.getAbsolutePath() + "\" -vn -qscale:a 6 \"" + mp3File.getAbsolutePath() + "\"";
			final CommandLine cmdLine = CommandLine.parse(command);
			final DefaultExecutor executor = new DefaultExecutor();
			executor.setWorkingDirectory(new File(settingsService.getSetting("files", String.class)));
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
