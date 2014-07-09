package org.ytrss;

import java.io.File;
import java.io.IOException;
import java.net.URL;

import org.apache.commons.io.FileUtils;

import com.google.common.base.Throwables;

public class Downloader {

	public File download(StreamMapEntry entry) {
		File file = new File("C:\\Users\\Johannes\\Desktop\\ytrss\\" + System.currentTimeMillis() + "." + StreamMapEntries.getExtension(entry));
		try {
			FileUtils.copyURLToFile(new URL(entry.getUrl()), file);
		}
		catch (IOException e) {
			throw Throwables.propagate(e);
		}
		return file;
	}

}
