package org.ytrss.youtube;

import static com.google.common.base.Preconditions.checkArgument;

import java.sql.Date;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.lang3.StringEscapeUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.ytrss.HTML;
import org.ytrss.Patterns;
import org.ytrss.controllers.ChannelController;

import com.google.common.base.Strings;
import com.google.common.base.Throwables;

public class VideoPage {

	private static final Pattern	TITLE_PATTERN				= Pattern.compile("og:title\"\\s*content=\"(.+)\">");

	private static final Pattern	DESCRIPTION_PATTERN			= Pattern
			.compile("id=\"eow-description\"[^>]*>\\s*(.*)<\\/p>[\\s|\\n]*<\\/div>", Pattern.MULTILINE);

	private static final Pattern	VIDEO_ID_PATTERN			= Pattern.compile("videoId\"\\s*content=\"(.+)\">");

	private static final Pattern	UPLOADED_PATTERN			= Pattern
			.compile("<strong[^>]*watch-time-text[^>]*>.+on\\s+([^<]+)<\\/strong>");

	private static final Pattern	SCHEDULED_STREAM_PATTERN	= Pattern
			.compile("<strong[^>]*watch-time-text[^>]*>\\s*Scheduled for[^<]+[^<]+<\\/strong>");

	private static Logger			log							= LoggerFactory.getLogger(ChannelController.class);

	private final String			source;

	public VideoPage(final String source) {
		checkArgument(!Strings.isNullOrEmpty(source), "Source must not be emtpy");
		this.source = source;
	}

	public String getDescription() {
		final String description = getFromPattern(VideoPage.DESCRIPTION_PATTERN);
		final String plainDescription = HTML.toFormattedPlainText(description);
		return plainDescription;
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
