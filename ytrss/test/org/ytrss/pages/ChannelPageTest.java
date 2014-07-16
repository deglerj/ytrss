package org.ytrss.pages;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;

import org.junit.Before;
import org.junit.Test;
import org.ytrss.URLs;

public class ChannelPageTest {

	private ChannelPage	normalPage;

	private ChannelPage	oneEntryPage;

	@Before
	public void setUp() throws MalformedURLException, IOException {
		normalPage = new ChannelPage(URLs.copyToString(new URL("https://www.youtube.com/user/ROCKETBEANSTV/videos")));
		oneEntryPage = new ChannelPage(URLs.copyToString(new URL("https://www.youtube.com/user/freekthecat/videos")));
	}

	@Test
	public void testNormalPage() {
		// Should have 30 entries (maximum for a page)
		assertEquals(30, normalPage.getContentGridEntries().size());

		assertNotNull(normalPage.getProfileImage());

		// All content grid entries should point to a valid video page
		for (final ContentGridEntry contentEntry : normalPage.getContentGridEntries()) {
			final String url = "http://youtube.com" + contentEntry.getHref() + "&gl=gb&hl=en"; // Force locale to make date parsing easier
			final VideoPage videoPage = new VideoPage(URLs.copyToString(url));
			assertNotNull(videoPage.getTitle());
		}
	}

	@Test
	public void testOneEntryPage() {
		// Should have 30 entries (maximum for a page)
		assertEquals(1, oneEntryPage.getContentGridEntries().size());

		assertNotNull(oneEntryPage.getProfileImage());

		// Content grid entry should point to a valid video page
		final ContentGridEntry contentEntry = oneEntryPage.getContentGridEntries().get(0);
		final String url = "http://youtube.com" + contentEntry.getHref() + "&gl=gb&hl=en"; // Force locale to make date parsing easier
		final VideoPage videoPage = new VideoPage(URLs.copyToString(url));
		assertNotNull(videoPage.getTitle());

	}
}
