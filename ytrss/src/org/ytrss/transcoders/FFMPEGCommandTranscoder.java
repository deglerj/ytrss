package org.ytrss.transcoders;

import java.io.File;
import java.io.IOException;
import java.util.function.Consumer;

import org.apache.commons.exec.CommandLine;
import org.apache.commons.exec.DefaultExecutor;
import org.apache.commons.exec.PumpStreamHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.scheduling.annotation.Async;
import org.ytrss.StringOutputStream;
import org.ytrss.db.SettingsService;
import org.ytrss.db.Video;
import org.ytrss.db.VideoDAO;
import org.ytrss.db.VideoState;
import org.ytrss.db.Videos;

public class FFMPEGCommandTranscoder implements Transcoder {

	private static Logger log = LoggerFactory.getLogger(FFMPEGCommandTranscoder.class);

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

	@Override
	@Async("transcoder")
	public void transcode(final File videoFile, Video video, final Consumer<Void> started, final Consumer<File> transcoded,
			final Consumer<Throwable> failed) {
		try {

			try {
				// Reload video from DB in case it has changed (e.g. state has been altered)
				video = videoDAO.findById(video.getId());
			}
			// Catch exception thrown if the video has been deleted and stop transcoding
			catch (final EmptyResultDataAccessException e) {
				log.info("Canceling transcoding video \"{}\" because it has been deleted", video);
				return;
			}

			// Make sure the video has not been deleted or reset while being enqueued for transcoding
			if (isInvalid(video)) {
				log.info("Skipped transcoding video \"" + video.getName() + "\" because it was already deleted or is an invalid state");
				return;
			}

			started.accept(null);

			log.info("Transcoding " + videoFile.getName());

			final String fileName = settingsService.getSetting("files", String.class) + File.separator + "mp3s" + File.separator
					+ Videos.getFileName(video) + ".mp3";

			final File mp3File = new File(fileName);

			final String bitrate = getBitrateArgument();
			final String command = "ffmpeg -y -i \"" + videoFile.getAbsolutePath() + "\" -vn " + bitrate + " \"" + mp3File.getAbsolutePath()
					+ "\"";
			final CommandLine cmdLine = CommandLine.parse(command);
			final DefaultExecutor executor = new DefaultExecutor();
			executor.setWorkingDirectory(new File(settingsService.getSetting("files", String.class)));
			final StringOutputStream output = new StringOutputStream();
			executor.setStreamHandler(new PumpStreamHandler(output, output));
			final int exitValue = executor.execute(cmdLine);
			if (executor.isFailure(exitValue)) {
				throw new RuntimeException("Transconding failed. ffmpeg output was:\n" + output.toString());
			}

			transcoded.accept(mp3File);
		}
		catch (final Throwable t) {
			failed.accept(t);
			return;
		}
	}

	private String getBitrateArgument() {
		final Bitrate bitrate = settingsService.getSetting("bitrate", Bitrate.class);
		switch (bitrate) {
			case CBR_192:
				return "-b:a 192k";
			case CBR_128:
				return "-b:a 128k";
			case CBR_96:
				return "-b:a 96k";
			case CBR_64:
				return "-b:a 64k";
			case VBR_HIGH:
				return "-q:a 1";
			case VBR_MEDIUM:
				return "-q:a 4";
			case VBR_LOW:
				return "-q:a 9";
			default:
				throw new IllegalStateException("Unknown or unsupported bitrate \"" + bitrate + "\"");
		}
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
