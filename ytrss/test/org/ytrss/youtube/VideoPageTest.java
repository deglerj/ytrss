package org.ytrss.youtube;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.sql.Date;
import java.text.SimpleDateFormat;
import java.util.List;
import java.util.Locale;

import org.junit.Before;
import org.junit.Test;
import org.ytrss.URLs;
import org.ytrss.youtube.StreamMapEntry;
import org.ytrss.youtube.VideoPage;

public class VideoPageTest {

	private VideoPage	page;

	@Before
	public void setUp() throws MalformedURLException, IOException {
		page = new VideoPage(URLs.copyToString(new URL("https://www.youtube.com/watch?v=ALZZx1xmAzg&gl=gb&hl=en")));
	}

	@Test
	public void testGetDescription() {
		final String description = page.getDescription();
		assertTrue(description.startsWith("You wouldn't steal a policeman's helmet"));
	}

	@Test
	public void testGetStreamMapEntries() throws IOException {
		final List<StreamMapEntry> entries = page.getStreamMapEntries();

		entries.forEach(e -> {
			assertFalse(e.getUrl().isEmpty());
			assertFalse(e.getType().isEmpty());
			assertFalse(e.getQuality().isEmpty());
		});

		assertEquals(entries.size(), 6);
	}

	@Test
	public void testGetTitle() {
		assertEquals("The IT Crowd - Series 2 - Episode 3: Piracy warning", page.getTitle());
	}

	@Test
	public void testGetUploaded() {
		final SimpleDateFormat format = new SimpleDateFormat("MMM dd, yyyy", Locale.UK);

		final Date uploaded = page.getUploaded();
		assertEquals("Mar 18, 2009", format.format(uploaded));
	}
}
