package org.ytrss;

import java.io.File;
import java.io.IOException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.ytrss.db.Channel;
import org.ytrss.db.ChannelDAO;
import org.ytrss.db.Video;

import com.google.common.base.Throwables;
import com.google.common.io.Files;
import com.mpatric.mp3agic.ID3v1Tag;
import com.mpatric.mp3agic.ID3v2;
import com.mpatric.mp3agic.ID3v24Tag;
import com.mpatric.mp3agic.InvalidDataException;
import com.mpatric.mp3agic.Mp3File;
import com.mpatric.mp3agic.NotSupportedException;
import com.mpatric.mp3agic.UnsupportedTagException;

@Component
public class ID3Tagger {

	@Autowired
	private ChannelDAO			channelDAO;

	private final DateFormat	yearFormat	= new SimpleDateFormat("yyyy");

	private static Logger		log			= LoggerFactory.getLogger(ID3Tagger.class);

	public void tag(final File file, final Video video) {
		final Channel channel = channelDAO.findById(video.getChannelID());

		try {
			final Mp3File mp3 = new Mp3File(file.getAbsolutePath());

			final ID3v1Tag v1Tag = new ID3v1Tag();
			v1Tag.setArtist(channel.getName());
			v1Tag.setTitle(video.getName());
			v1Tag.setYear(yearFormat.format(video.getUploaded()));
			mp3.setId3v1Tag(v1Tag);

			final ID3v2 v2Tag = new ID3v24Tag();
			v2Tag.setTitle(video.getName());
			v2Tag.setYear(yearFormat.format(video.getUploaded()));
			mp3.setId3v2Tag(v2Tag);

			final File taggedFile = new File(file.getAbsolutePath() + ".tmp");
			mp3.save(taggedFile.getAbsolutePath());

			Files.move(taggedFile, file);
		}
		catch (UnsupportedTagException | InvalidDataException | IOException | NotSupportedException e) {
			log.error("Error creating ID3 tagged mp3 file", e);
			throw Throwables.propagate(e);
		}
	}
}
