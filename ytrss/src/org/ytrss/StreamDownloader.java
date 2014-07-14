package org.ytrss;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.function.Consumer;

import org.apache.commons.io.FileUtils;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.ytrss.db.Video;
import org.ytrss.db.Videos;
import org.ytrss.pages.StreamMapEntries;
import org.ytrss.pages.StreamMapEntry;

@Component
public class StreamDownloader {

	@Async("streamDownloader")
	public void download(final Video video, final StreamMapEntry entry, final Consumer<File> downloaded, final Consumer<Throwable> failed) {
		final String userHome = System.getProperty("user.home");
		final String fileName = userHome + "/.ytrss/videos/" + Videos.getFileName(video) + "." + StreamMapEntries.getExtension(entry);

		final File file = new File(fileName);
		try {
			FileUtils.copyURLToFile(new URL(entry.getUrl()), file);
		}
		catch (final IOException e) {
			failed.accept(e);
		}

		downloaded.accept(file);
	}

}
