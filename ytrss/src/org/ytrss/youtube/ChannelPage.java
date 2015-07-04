package org.ytrss.youtube;

import static com.google.common.base.Preconditions.checkArgument;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.google.common.base.Preconditions;
import com.google.common.collect.Lists;
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

	private final String			source;

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
		Preconditions.checkArgument(maxEntries <= 50 && maxEntries > 0, "maxEntries must be between 1 and 50 (inclusive)");

		final StringBuilder url = buildApiRequestUrl(maxEntries);

		String json = URLs.getSource(url.toString(), true);

		try {
			JsonRootNode root = new JdomParser().parse(json);
			List<JsonNode> items = root.getArrayNode("items");

			List<ContentGridEntry> entries = Lists.newArrayList();

			for(JsonNode item : items) {
				JsonNode id = item.getNode("id");
				String kind = id.getStringValue("kind");

				// Skip playlists, etc.
				if(!"youtube#video".equalsIgnoreCase(kind)) {
					continue;
				}

				String videoId = id.getStringValue("videoId");

				JsonNode snippet = item.getNode("snippet");
				String title = snippet.getStringValue("title");

				entries.add(new ContentGridEntry(title, videoId));
			}

			return entries;

		} catch (InvalidSyntaxException e) {
			log.error("Error listing channel videos", e);
			throw Throwables.propagate(e);
		}


	}

	private StringBuilder buildApiRequestUrl(int maxEntries) {
		//FIXME JDE key aus Settings auslesen
		final StringBuilder url = new StringBuilder();
		url.append("https://www.googleapis.com/youtube/v3/search?key=<API-KEY>&channelId=");
		url.append(getChannelId());
		url.append("&part=snippet,id&fields=items(id,snippet(title))&order=date&maxResults=");
		url.append(maxEntries);
		return url;
	}

	public String getProfileImage() {
		return Patterns.getMatchGroup(PROFILE_IMAGE_PATTERN, 1, source);
	}

}
