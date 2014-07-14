package org.ytrss.transcoders;

import it.sauronsoftware.jave.AudioAttributes;
import it.sauronsoftware.jave.Encoder;
import it.sauronsoftware.jave.EncoderException;
import it.sauronsoftware.jave.EncodingAttributes;

import java.io.File;
import java.util.function.Consumer;

import org.springframework.scheduling.annotation.Async;

public class JaveTranscoder implements Transcoder {

	@Override
	@Async("transcoder")
	public void transcode(final File videoFile, final Consumer<File> transcoded, final Consumer<Throwable> failed) {
		System.out.println("TRANSCODING " + videoFile.getName());

		final File mp3File = new File("C:\\Users\\Johannes\\Desktop\\ytrss\\ " + System.currentTimeMillis() + ".mp3");

		final AudioAttributes audio = new AudioAttributes();
		audio.setCodec("libmp3lame");
		audio.setBitRate(new Integer(128000));
		audio.setChannels(new Integer(2));
		audio.setSamplingRate(new Integer(44100));

		final EncodingAttributes attrs = new EncodingAttributes();
		attrs.setFormat("mp3");
		attrs.setAudioAttributes(audio);

		try {
			new Encoder().encode(videoFile, mp3File, attrs);
		}
		catch (IllegalArgumentException | EncoderException e) {
			failed.accept(e);
		}

		transcoded.accept(mp3File);
	}

}
