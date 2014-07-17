package org.ytrss.youtube;

import static com.google.common.base.Preconditions.checkArgument;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.ytrss.Patterns;

import com.google.common.base.Strings;

public class ChannelPage {

	private final String			source;

	private static final Pattern	CONTENT_GRID_PATTERN	= Pattern.compile("video-page-content(.+)<div id=\"ad_creative_1\"", Pattern.MULTILINE
			| Pattern.DOTALL);

	// Value of title attribute is matched in group 1, href in group 2
	private static final Pattern	GRID_ENTRY_PATTERN		= Pattern.compile("<a[^<]*title=\"([^\"]+)\"[^<]*href=\"([^\"]+)\"[^<]*>", Pattern.MULTILINE);

	private static final Pattern	PROFILE_IMAGE_PATTERN	= Pattern.compile("channel-header-profile-image\"\\s*src=\"([^\"]+)\"", Pattern.MULTILINE);

	public ChannelPage(final String source) {
		checkArgument(!Strings.isNullOrEmpty(source), "Source must not be emtpy");

		this.source = source;
	}

	public List<ContentGridEntry> getContentGridEntries() {
		final String contentGrid = Patterns.getMatchGroup(CONTENT_GRID_PATTERN, 1, source);

		final List<ContentGridEntry> entries = new ArrayList<>();

		final Matcher entriesMatcher = GRID_ENTRY_PATTERN.matcher(contentGrid);
		while (entriesMatcher.find()) {
			final String title = entriesMatcher.group(1);
			final String href = entriesMatcher.group(2);
			entries.add(new ContentGridEntry(title, href));
		}

		return entries;
	}

	public String getProfileImage() {
		return Patterns.getMatchGroup(PROFILE_IMAGE_PATTERN, 1, source);
	}

}