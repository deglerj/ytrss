package org.ytrss.transcoders;

import java.io.File;

public interface Transcoder {

	File transcode(File input);

}
