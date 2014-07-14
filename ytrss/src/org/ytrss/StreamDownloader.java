package org.ytrss;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.function.Consumer;

import org.apache.commons.io.FileUtils;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.ytrss.db.Video;
import org.ytrss.pages.StreamMapEntries;
import org.ytrss.pages.StreamMapEntry;

@Component
public class StreamDownloader {

	@Async("streamDownloader")
	public void download(final Video video, final StreamMapEntry entry, final Consumer<File> downloaded, final Consumer<Throwable> failed) {
		System.out.println("DOWNLOADING " + video.getName());

		final File file = new File("C:\\Users\\Johannes\\Desktop\\ytrss\\" + System.currentTimeMillis() + "." + StreamMapEntries.getExtension(entry));
		try {
			FileUtils.copyURLToFile(new URL(entry.getUrl()), file);
		}
		catch (final IOException e) {
			failed.accept(e);
		}

		downloaded.accept(file);
	}

}
