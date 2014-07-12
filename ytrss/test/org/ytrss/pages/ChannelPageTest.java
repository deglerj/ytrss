package org.ytrss.pages;

import static org.junit.Assert.assertEquals;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.List;

import org.junit.Before;
import org.junit.Test;
import org.ytrss.URLs;

public class ChannelPageTest {

	private ChannelPage	page;

	@Before
	public void setUp() throws MalformedURLException, IOException {
		page = new ChannelPage(URLs.copyToString(new URL("https://www.youtube.com/user/ROCKETBEANSTV/videos")));
	}

	@Test
	public void testGetConentGridEntries() {
		final List<ContentGridEntry> entries = page.getContentGridEntries();

		assertEquals(30, entries.size());
	}

}
