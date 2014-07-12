package org.ytrss.transcoders;

import it.sauronsoftware.jave.AudioAttributes;
import it.sauronsoftware.jave.Encoder;
import it.sauronsoftware.jave.EncoderException;
import it.sauronsoftware.jave.EncodingAttributes;

import java.io.File;

import com.google.common.base.Throwables;

public class JaveTranscoder implements Transcoder {

	@Override
	public File transcode(final File input) {
		final File target = new File("C:\\Users\\Johannes\\Desktop\\ytrss\\ " + System.currentTimeMillis() + ".mp3");

		final AudioAttributes audio = new AudioAttributes();
		audio.setCodec("libmp3lame");
		audio.setBitRate(new Integer(128000));
		audio.setChannels(new Integer(2));
		audio.setSamplingRate(new Integer(44100));

		final EncodingAttributes attrs = new EncodingAttributes();
		attrs.setFormat("mp3");
		attrs.setAudioAttributes(audio);

		try {
			new Encoder().encode(input, target, attrs);
		}
		catch (IllegalArgumentException | EncoderException e) {
			throw Throwables.propagate(e);
		}

		return target;
	}

}
