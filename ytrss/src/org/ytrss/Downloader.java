package org.ytrss;

import java.io.File;
import java.io.IOException;
import java.net.URL;

import org.apache.commons.io.FileUtils;
import org.ytrss.pages.StreamMapEntries;
import org.ytrss.pages.StreamMapEntry;

import com.google.common.base.Throwables;

public class Downloader {

	public File download(final StreamMapEntry entry) {
		final File file = new File("C:\\Users\\Johannes\\Desktop\\ytrss\\" + System.currentTimeMillis() + "." + StreamMapEntries.getExtension(entry));
		try {
			FileUtils.copyURLToFile(new URL(entry.getUrl()), file);
		}
		catch (final IOException e) {
			throw Throwables.propagate(e);
		}
		return file;
	}

}
