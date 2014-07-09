package org.ytrss;

import java.io.File;

public class Test {

	public static void main(String[] args) {
		VideoPage page = new VideoPage(URLs.copyToString("https://www.youtube.com/watch?v=ALZZx1xmAzg"));
		final StreamMapEntry bestEntry = new StreamMapEntryScorer().findBestEntry(page.getStreamMapEntries());
		final File input = new Downloader().download(bestEntry);
		new JaveTranscoder().transcode(input);
	}

}
