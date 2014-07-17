package org.ytrss.transcoders;

import java.io.File;
import java.util.function.Consumer;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Async;
import org.ytrss.db.Video;
import org.ytrss.db.Videos;

import be.hogent.tarsos.transcoder.DefaultAttributes;

public class TarsosTranscoder implements Transcoder {

	private static Logger	log	= LoggerFactory.getLogger(TarsosTranscoder.class);

	@Override
	@Async("transcoder")
	public void transcode(final File videoFile, final Video video, final Consumer<Void> started, final Consumer<File> transcoded,
			final Consumer<Throwable> failed) {
		started.accept(null);

		log.info("Transcoding " + videoFile.getName());

		final String userHome = System.getProperty("user.home");
		final String fileName = userHome + "/.ytrss/mp3s/" + Videos.getFileName(video) + ".mp3";

		final File mp3File = new File(fileName);

		try {
			be.hogent.tarsos.transcoder.Transcoder.transcode(videoFile, mp3File, DefaultAttributes.MP3_128KBS_STEREO_44KHZ);
		}
		catch (final Throwable t) {
			failed.accept(t);
		}

		transcoded.accept(mp3File);
	}

}
