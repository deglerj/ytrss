package org.ytrss.pages;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ChannelPage {

	private final String			source;

	private static final Pattern	CONTENT_GRID_PATTERN	= Pattern.compile("video-page-content(.+)yt-uix-load-more", Pattern.MULTILINE | Pattern.DOTALL);

	// Value of title attribute is matched in group 1, href in group 2
	private static final Pattern	GRID_ENTRY_PATTERN		= Pattern.compile("<a[^<]*title=\"([^\"]+)\"[^<]*href=\"([^\"]+)\"[^<]*>", Pattern.MULTILINE);

	public ChannelPage(final String source) {
		this.source = source;
	}

	public List<ContentGridEntry> getContentGridEntries() {
		final Matcher gridMatcher = CONTENT_GRID_PATTERN.matcher(source);
		gridMatcher.find();
		final String contentGrid = gridMatcher.group(1);

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
