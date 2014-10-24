package org.ytrss.youtube;

import static com.google.common.base.Preconditions.checkArgument;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.ytrss.Patterns;
import org.ytrss.URLs;

import argo.jdom.JdomParser;
import argo.jdom.JsonNode;
import argo.jdom.JsonRootNode;
import argo.saj.InvalidSyntaxException;

import com.google.common.base.Strings;
import com.google.common.base.Throwables;

public class ChannelPage {

	private static class ContentGrid {

		private final JsonRootNode	json;

		public ContentGrid(final JsonRootNode json) {
			this.json = json;
		}

		public List<ContentGridEntry> getEntries() {
			final JsonNode contentNode = json.getNode("content_html");

			final List<ContentGridEntry> entries = new ArrayList<>();

			final Matcher entriesMatcher = GRID_ENTRY_PATTERN.matcher(contentNode.getText());
			while (entriesMatcher.find()) {
				final String title = entriesMatcher.group(1);
				final String href = entriesMatcher.group(2).replace("&amp;", "&");
				entries.add(new ContentGridEntry(title, href));
			}

			return entries;
		}

		public boolean hasMore() {
			if (json.isNode("load_more_widget_html")) {
				final String loadMore = json.getNode("load_more_widget_html").getText();
				return !Strings.isNullOrEmpty(loadMore);
			}
			else {
				return false;
			}
		}
	}

	private final String			source;

	// Value of title attribute is matched in group 1, href in group 2
	private static final Pattern	GRID_ENTRY_PATTERN		= Pattern.compile("<a[^<]*title=\"([^\"]+)\"[^<]*href=\"([^\"]+)\"[^<]*>", Pattern.MULTILINE);

	private static final Pattern	PROFILE_IMAGE_PATTERN	= Pattern.compile("channel-header-profile-image\"\\s*src=\"([^\"]+)\"", Pattern.MULTILINE);

	private static final Pattern	CHANNEL_ID_PATTERN		= Pattern.compile("meta\\s*itemprop=\"channelId\"\\s*content=\"([^\"]+)\">", Pattern.MULTILINE);

	private static final Logger		log						= LoggerFactory.getLogger(ChannelPage.class);

	public ChannelPage(final String source) {
		checkArgument(!Strings.isNullOrEmpty(source), "Source must not be emtpy");

		this.source = source;
	}

	public String getChannelId() {
		return Patterns.getMatchGroup(CHANNEL_ID_PATTERN, 1, source);
	}

	public List<ContentGridEntry> getContentGridEntries(final int maxEntries) {
		int page = 1;
		final List<ContentGridEntry> entries = new ArrayList<>();

		try {
			ContentGrid grid;
			do {
				grid = loadGrid(page);
				entries.addAll(grid.getEntries());

				page++;
			}
			while (entries.size() < maxEntries && grid.hasMore());

			if (entries.size() > maxEntries) {
				return entries.subList(0, maxEntries);
			}
			else {
				return entries;
			}
		}
		catch (final IOException | InvalidSyntaxException e) {
			log.error("Error parsing conten grid entries", e);
			throw Throwables.propagate(e);
		}
	}

	public String getProfileImage() {
		return Patterns.getMatchGroup(PROFILE_IMAGE_PATTERN, 1, source);
	}

	private ContentGrid loadGrid(final int page) throws IOException, InvalidSyntaxException {
		final String url = "https://www.youtube.com/c4_browse_ajax?action_load_more_videos=1&view=0&paging=" + page + "&channel_id=" + getChannelId()
				+ "&sort=dd&flow=grid&fluid=True";

		final String gridSource = URLs.copyToString(url);

		final JsonRootNode json = new JdomParser().parse(gridSource);
		return new ContentGrid(json);

	}

}
