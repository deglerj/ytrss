package org.ytrss.transcoders;

import java.io.File;
import java.util.function.Consumer;

public interface Transcoder {

	void transcode(File videoFile, Consumer<File> transcoded, Consumer<Throwable> failed);

}
