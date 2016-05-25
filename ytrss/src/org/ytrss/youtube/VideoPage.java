package org.ytrss.youtube;

import static com.google.common.base.Preconditions.checkArgument;

import java.io.IOException;
import java.net.URLDecoder;
import java.sql.Date;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.List;
import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.lang3.StringEscapeUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.ytrss.HTML;
import org.ytrss.Patterns;
import org.ytrss.controllers.ChannelController;

import com.google.common.base.Splitter;
import com.google.common.base.Strings;
import com.google.common.base.Throwables;
import com.google.common.collect.Lists;

public class VideoPage {

	private static final Pattern STREAM_MAP_PATTERN = Pattern.compile("\"url_encoded_fmt_stream_map\"\\s*:\\s*\"([^\"]+)");

	private static final Pattern TITLE_PATTERN = Pattern.compile("og:title\"\\s*content=\"(.+)\">");

	private static final Pattern DESCRIPTION_PATTERN = Pattern.compile("id=\"eow-description\"[^>]*>\\s*(.*)<\\/p>[\\s|\\n]*<\\/div>", Pattern.MULTILINE);

	private static final Pattern VIDEO_ID_PATTERN = Pattern.compile("videoId\"\\s*content=\"(.+)\">");

	private static final Pattern KEY_VALUE_PATTERN = Pattern.compile("^([^=]+)=(.+)$");

	private static final Pattern UPLOADED_PATTERN = Pattern.compile("<strong[^>]*watch-time-text[^>]*>.+on\\s+([^<]+)<\\/strong>");

	private static final Pattern SCHEDULED_STREAM_PATTERN = Pattern.compile("<strong[^>]*watch-time-text[^>]*>\\s*Scheduled for[^<]+[^<]+<\\/strong>");

	private static Logger log = LoggerFactory.getLogger(ChannelController.class);

	private final String source;

	public VideoPage(final String source) {
		checkArgument(!Strings.isNullOrEmpty(source), "Source must not be emtpy");
		this.source = source;
	}

	public String getDescription() {
		final String description = getFromPattern(VideoPage.DESCRIPTION_PATTERN);
		final String plainDescription = HTML.toFormattedPlainText(description);
		return plainDescription;
	}

	public List<StreamMapEntry> getStreamMapEntries() {
		final String encodedStreamMap = Patterns.getMatchGroup(STREAM_MAP_PATTERN, 1, source);

		try {
			final String decodedStreamMap = decode(encodedStreamMap);

			final List<StreamMapEntry> entries = Lists.newArrayList();
			String url = null;
			String type = null;
			String quality = null;
			for (final String line : Splitter.on(";").trimResults().split(fixSeparators(decodedStreamMap))) {
				final Matcher pairMatcher = VideoPage.KEY_VALUE_PATTERN.matcher(line);
				pairMatcher.find();

				final String key = pairMatcher.group(1);
				final String value = pairMatcher.group(2);

				if ("url".equals(key)) {
					url = value;
				}
				else if ("type".equals(key)) {
					type = value;
				}
				else if ("quality".equals(key)) {
					quality = value;
				}

				if (url != null && type != null && quality != null) {
					final StreamMapEntry entry = new StreamMapEntry(url, type, quality);
					entries.add(entry);

					url = null;
					type = null;
					quality = null;
				}
			}
			if (url != null && type != null && quality != null) {
				final StreamMapEntry entry = new StreamMapEntry(url, type, quality);
				entries.add(entry);
			}

			return entries;
		}
		catch (final IOException e) {
			log.error("Error parsing stream map entries", e);
			throw Throwables.propagate(e);
		}
	}

	public String getTitle() {
		return getFromPattern(VideoPage.TITLE_PATTERN);
	}

	public Date getUploaded() {
		final String text = Patterns.getMatchGroup(UPLOADED_PATTERN, 1, source);

		final DateFormat format = new SimpleDateFormat("MMM dd, yyyy", Locale.UK);
		try {
			final java.util.Date parsed = format.parse(text);
			return new Date(parsed.getTime());
		}
		catch (final ParseException e) {
			log.error("Error parsing upload date", e);
			throw Throwables.propagate(e);
		}

	}

	public String getVideoID() {
		return getFromPattern(VideoPage.VIDEO_ID_PATTERN);
	}

	public boolean isScheduledStream() {
		final Matcher foo = SCHEDULED_STREAM_PATTERN.matcher(source);
		final boolean bar = foo.find();
		return bar;
	}

	private String decode(final String encoded) throws IOException {
		String lastDecoded = URLDecoder.decode(encoded, "UTF-8");

		// Decode multiple times if necessary
		while (true) {
			String decoded = URLDecoder.decode(lastDecoded, "UTF-8");
			decoded = decoded.replace("\\u0026", ";");
			if (decoded.equals(lastDecoded)) {
				return decoded;
			}
			else {
				lastDecoded = decoded;
			}
		}
	}

	private String fixSeparators(final String in) {
		String fixed = in;

		// Some key-value-pairs are separated by "," instead of ";"
		for (final String key : new String[] { "url", "type", "fallback_host", "itag", "quality", "codecs" }) {
			fixed = fixed.replaceAll("\\,\\s*" + key + "=", ";" + key + "=");
		}

		return fixed;
	}

	private String getFromPattern(final Pattern pattern) {
		final String match = Patterns.getMatchGroup(pattern, 1, source);
		if (match == null) {
			return null;
		}
		else {
			return StringEscapeUtils.unescapeHtml4(match);
		}
	}
}
