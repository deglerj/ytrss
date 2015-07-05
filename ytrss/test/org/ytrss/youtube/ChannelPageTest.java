package org.ytrss.youtube;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.io.IOException;
import java.io.InputStream;
import java.util.List;

import org.apache.commons.io.IOUtils;
import org.junit.Before;
import org.junit.Test;
import org.ytrss.URLs;

import com.google.common.collect.Iterables;

public class ChannelPageTest {

	private String	apiKey;

	@Before
	public void readApiKey() throws IOException {
		try (final InputStream is = this.getClass().getResourceAsStream("api.key")) {

			if (is == null) {
				fail("Please create a file \"api.key\" and insert your YouTube Data API Key. Make sure to keep the file excluded from Git.");
				return;
			}

			apiKey = IOUtils.toString(is);
		}
	}

	@Test
	public void testExtraSlashURLPage() {
		final ChannelPage page = openPage("http://www.youtube.com/user/brentalfloss/");
		testPage(page);
	}

	@Test
	public void testNormalPage() {
		final ChannelPage page = openPage("http://www.youtube.com/user/ROCKETBEANSTV");
		testPage(page);
	}

	@Test
	public void testOneEntryPage() {
		final ChannelPage page = openPage("http://www.youtube.com/user/freekthecat");
		testPage(page);
	}

	@Test
	public void testWeirdURLPage() {
		final ChannelPage page = openPage("http://www.youtube.com/channel/UCn0TDqRR4NjwWiAQR9FAd5w");
		testPage(page);
	}

	private ChannelPage openPage(final String url) {
		final String cleanURL = URLs.cleanUpURL(url);
		return new ChannelPage(URLs.getSource(cleanURL, false), apiKey);

	}

	private void testEntry(final ContentGridEntry entry) {
		final String url = "http://youtube.com/watch?v=" + entry.getVideoID() + "&gl=gb&hl=en";
		final VideoPage videoPage = new VideoPage(URLs.getSource(url, false));
		assertNotNull(videoPage.getTitle());
	}

	private void testPage(final ChannelPage page) {
		final List<ContentGridEntry> entries = page.getContentGridEntries(50);

		assertTrue(entries.size() > 0);

		assertNotNull(page.getProfileImage());

		// All content grid entries should point to a valid video page (test 1, second and last entry)
		if (entries.size() > 0) {
			testEntry(entries.get(0));
		}
		if (entries.size() > 1) {
			testEntry(entries.get(1));
		}
		if (entries.size() > 2) {
			testEntry(Iterables.getLast(entries));
		}
	}

}
