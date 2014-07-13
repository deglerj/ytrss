package org.ytrss.pages;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ChannelPage {

	private final String			contentGrid;

	private static final Pattern	CONTENT_GRID_PATTERN	= Pattern.compile("video-page-content(.+)<div id=\"ad_creative_1\"", Pattern.MULTILINE
																	| Pattern.DOTALL);

	// Value of title attribute is matched in group 1, href in group 2
	private static final Pattern	GRID_ENTRY_PATTERN		= Pattern.compile("<a[^<]*title=\"([^\"]+)\"[^<]*href=\"([^\"]+)\"[^<]*>", Pattern.MULTILINE);

	public ChannelPage(final String source) {
		final Matcher gridMatcher = CONTENT_GRID_PATTERN.matcher(source);
		gridMatcher.find();
		contentGrid = gridMatcher.group(1);
	}

	public List<ContentGridEntry> getContentGridEntries() {
		final List<ContentGridEntry> entries = new ArrayList<>();

		final Matcher entriesMatcher = GRID_ENTRY_PATTERN.matcher(contentGrid);
		while (entriesMatcher.find()) {
			final String title = entriesMatcher.group(1);
			final String href = entriesMatcher.group(2);
			entries.add(new ContentGridEntry(title, href));
		}

		return entries;
	}

}
