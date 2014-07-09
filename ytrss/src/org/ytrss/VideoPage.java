package org.ytrss;

import java.io.IOException;
import java.net.URLDecoder;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.lang3.StringEscapeUtils;

import com.google.common.base.Splitter;
import com.google.common.base.Throwables;
import com.google.common.collect.Lists;

public class VideoPage {

	private String					source;

	private static final Pattern	STREAM_MAP_PATTERN	= Pattern.compile("\"url_encoded_fmt_stream_map\": \"([^\"]+)");

	private static final Pattern	TITLE_PATTERN		= Pattern.compile("og:title\" content=\"(.+)\">");

	private static final Pattern	DESCRIPTION_PATTERN	= Pattern.compile("og:description\" content=\"(.+)\">");

	public VideoPage(String source) {
		this.source = source;
	}

	public String getTitle() {
		return getFromPattern(TITLE_PATTERN);
	}

	public String getDescription() {
		return getFromPattern(DESCRIPTION_PATTERN);
	}

	private String getFromPattern(Pattern pattern) {
		final Matcher matcher = pattern.matcher(source);
		matcher.find();
		final String match = matcher.group(1);
		if (match == null)
			return null;
		else
			return StringEscapeUtils.unescapeHtml4(match);
	}

	private static final Pattern	KEY_VALUE_PATTERN	= Pattern.compile("^([^=]+)=(.+)$");

	public List<StreamMapEntry> getStreamMapEntries() {
		final Matcher mapMatcher = STREAM_MAP_PATTERN.matcher(source);
		mapMatcher.find();

		final String encodedStreamMap = mapMatcher.group(1);

		try {
			String decodedStreamMap = decode(encodedStreamMap);

			List<StreamMapEntry> entries = Lists.newArrayList();
			String url = null;
			String type = null;
			String quality = null;
			for (String line : Splitter.on(";").trimResults().split(fixSeparators(decodedStreamMap))) {
				final Matcher pairMatcher = KEY_VALUE_PATTERN.matcher(line);
				pairMatcher.find();

				final String key = pairMatcher.group(1);
				final String value = pairMatcher.group(2);

				if ("url".equals(key))
					url = value;
				else if ("type".equals(key))
					type = value;
				else if ("quality".equals(key))
					quality = value;

				if (url != null && type != null && quality != null) {
					StreamMapEntry entry = new StreamMapEntry(url, type, quality);
					entries.add(entry);

					url = null;
					type = null;
					quality = null;
				}
			}
			if (url != null && type != null && quality != null) {
				StreamMapEntry entry = new StreamMapEntry(url, type, quality);
				entries.add(entry);
			}

			return entries;
		}
		catch (IOException e) {
			throw Throwables.propagate(e);
		}
	}

	private String fixSeparators(String in) {
		String fixed = in;

		// Some key-value-pairs are separated by "," instead of ";"
		for (String key : new String[] { "url", "type", "fallback_host", "itag", "quality", "codecs" })
			fixed = fixed.replaceAll("\\,\\s*" + key + "=", ";" + key + "=");

		return fixed;
	}

	private String decode(String encoded) throws IOException {
		String lastDecoded = URLDecoder.decode(encoded, "UTF-8");

		// Decode multiple times if necessary
		while (true) {
			String decoded = URLDecoder.decode(lastDecoded, "UTF-8");
			decoded = decoded.replace("\\u0026", ";");
			if (decoded.equals(lastDecoded))
				return decoded;
			else
				lastDecoded = decoded;
		}
	}
}
