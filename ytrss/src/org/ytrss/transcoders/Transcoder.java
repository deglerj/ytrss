package org.ytrss.transcoders;

import java.io.File;
import java.util.function.Consumer;

import org.ytrss.db.Video;

public interface Transcoder {

	void transcode(File videoFile, Video video, Consumer<File> transcoded, Consumer<Throwable> failed);

}
