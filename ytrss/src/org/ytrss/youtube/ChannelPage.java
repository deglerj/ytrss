package org.ytrss.youtube;

import static com.google.common.base.Preconditions.checkArgument;

import java.util.List;
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
import com.google.common.collect.Lists;

public class ChannelPage {

	private final String			source;

	private static final Pattern	PROFILE_IMAGE_PATTERN	= Pattern.compile("channel-header-profile-image\"\\s*src=\"([^\"]+)\"", Pattern.MULTILINE);

	private static final Pattern	CHANNEL_ID_PATTERN		= Pattern.compile("meta\\s*itemprop=\"channelId\"\\s*content=\"([^\"]+)\">", Pattern.MULTILINE);

	private static final Logger		log						= LoggerFactory.getLogger(ChannelPage.class);

	private final String			apiKey;

	public ChannelPage(final String source, final String apiKey) {
		checkArgument(!Strings.isNullOrEmpty(source), "Source must not be emtpy");

		this.apiKey = apiKey;
		this.source = source;
	}

	public String fetchNextPage(final String pageToken, final List<ContentGridEntry> entries) {
		final StringBuilder url = buildApiRequestUrl(50, pageToken);

		final String json = URLs.getSource(url.toString(), true);

		try {
			final JsonRootNode root = new JdomParser().parse(json);
			final String nextPageToken = root.isStringValue("nextPageToken") ? root.getStringValue("nextPageToken") : null;
			final List<JsonNode> items = root.getArrayNode("items");

			for (final JsonNode item : items) {
				final JsonNode id = item.getNode("id");
				final String kind = id.getStringValue("kind");

				// Skip playlists, etc.
				if (!"youtube#video".equalsIgnoreCase(kind)) {
					continue;
				}

				final String videoId = id.getStringValue("videoId");

				final JsonNode snippet = item.getNode("snippet");
				final String title = snippet.getStringValue("title");

				entries.add(new ContentGridEntry(title, videoId));
			}

			return nextPageToken;

		}
		catch (final InvalidSyntaxException e) {
			log.error("Error listing channel videos", e);
			throw Throwables.propagate(e);
		}
	}

	public String getChannelId() {
		return Patterns.getMatchGroup(CHANNEL_ID_PATTERN, 1, source);
	}

	public List<ContentGridEntry> getContentGridEntries(final int maxEntries) {
		final List<ContentGridEntry> entries = Lists.newArrayList();

		String nextPageToken = null;
		do {
			nextPageToken = fetchNextPage(nextPageToken, entries);
		}
		while (nextPageToken != null && entries.size() < maxEntries);

		if (entries.size() > maxEntries) {
			return entries.subList(0, maxEntries);
		}
		else {
			return entries;
		}
	}

	public String getProfileImage() {
		return Patterns.getMatchGroup(PROFILE_IMAGE_PATTERN, 1, source);
	}

	private StringBuilder buildApiRequestUrl(final int maxEntries, final String pageToken) {
		final StringBuilder url = new StringBuilder();
		url.append("https://www.googleapis.com/youtube/v3/search?key=");
		url.append(apiKey);
		url.append("&channelId=");
		url.append(getChannelId());
		url.append("&part=snippet,id&fields=nextPageToken,items(id,snippet(title))&order=date&maxResults=");
		url.append(maxEntries);
		if (pageToken != null) {
			url.append("&pageToken=");
			url.append(pageToken);
		}
		return url;
	}

}
