package org.ytrss.youtube;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import java.util.List;

import org.junit.Test;
import org.ytrss.URLs;

import com.google.common.collect.Iterables;

public class ChannelPageTest {

	@Test
	public void testExtraSlashURLPage() {
		final ChannelPage page = openPage("https://www.youtube.com/user/brentalfloss/");
		testPage(page, null);
	}

	@Test
	public void testNormalPage() {
		final ChannelPage page = openPage("https://www.youtube.com/user/ROCKETBEANSTV");
		testPage(page, 30);
	}

	@Test
	public void testOneEntryPage() {
		final ChannelPage page = openPage("https://www.youtube.com/user/freekthecat");
		testPage(page, 1);
	}

	@Test
	public void testWeirdURLPage() {
		final ChannelPage page = openPage("http://www.youtube.com/channel/UCn0TDqRR4NjwWiAQR9FAd5w");
		testPage(page, null);
	}

	private ChannelPage openPage(final String url) {
		final String cleanURL = URLs.cleanUpURL(url);
		return new ChannelPage(URLs.copyToString(cleanURL + "/videos"));
	}

	private void testEntry(final ContentGridEntry entry) {
		final String url = "http://youtube.com" + entry.getHref() + "&gl=gb&hl=en"; // Force locale to make date parsing easier
		final VideoPage videoPage = new VideoPage(URLs.copyToString(url));
		assertNotNull(videoPage.getTitle());
	}

	private void testPage(final ChannelPage page, final Integer expectedEntries) {
		final List<ContentGridEntry> entries = page.getContentGridEntries();

		if (expectedEntries != null) {
			assertEquals(expectedEntries.intValue(), entries.size());
		}

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
