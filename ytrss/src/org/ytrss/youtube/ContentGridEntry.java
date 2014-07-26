package org.ytrss.youtube;

import java.util.regex.Pattern;

import org.ytrss.Patterns;

public class ContentGridEntry {

	private final String			title;

	private final String			href;

	private static final Pattern	VIDEO_ID_PATTERN	= Pattern.compile("v=([^&]+)");

	public ContentGridEntry(final String title, final String href) {
		this.title = title;
		this.href = href;
	}

	public String getHref() {
		return href;
	}

	public String getTitle() {
		return title;
	}

	public String getVideoID() {
		return Patterns.getMatchGroup(VIDEO_ID_PATTERN, 1, href);
	}

}
