package org.ytrss.pages;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.List;

import org.junit.Before;
import org.junit.Test;
import org.ytrss.URLs;
import org.ytrss.youtube.pages.StreamMapEntry;
import org.ytrss.youtube.pages.VideoPage;

public class VideoPageTest {

	private VideoPage	page;

	@Before
	public void setUp() throws MalformedURLException, IOException {
		page = new VideoPage(URLs.copyToString(new URL("https://www.youtube.com/watch?v=ALZZx1xmAzg")));
	}

	@Test
	public void testGetTitle() {
		assertEquals("The IT Crowd - Series 2 - Episode 3: Piracy warning", page.getTitle());
	}

	@Test
	public void testGetDescription() {
		final String foo = page.getDescription();
		assertTrue(foo.startsWith("You wouldn't steal a policeman's helmet"));
	}

	@Test
	public void testGetStreamMapEntries() throws IOException {
		final List<StreamMapEntry> entries = page.getStreamMapEntries();

		assertEquals(entries.size(), 6);

		entries.forEach(e -> {
			assertFalse(e.getUrl().isEmpty());
			assertFalse(e.getType().isEmpty());
			assertFalse(e.getQuality().isEmpty());
		});
	}

}
