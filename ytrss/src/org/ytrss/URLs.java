package org.ytrss;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;

import org.apache.commons.io.IOUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.ytrss.controllers.ChannelController;

import com.google.common.base.Function;
import com.google.common.base.Throwables;

public class URLs {

	public static String cleanUpURL(final String url) {
		if (url == null) {
			return null;
		}

		String cleanUrl = url.trim();
		if (cleanUrl.endsWith("/")) {
			cleanUrl = cleanUrl.substring(0, cleanUrl.length() - 1);
		}

		return cleanUrl;
	}

	public static String copyToString(final String url) {
		try {
			return copyToString(new URL(url));
		}
		catch (final MalformedURLException e) {
			throw Throwables.propagate(e);
		}
	}

	public static String copyToString(final URL url) {
		try {
			return IOUtils.toString(url, "UTF-8");
		}
		catch (final IOException e) {
			log.error("Error downloading page", e);
			throw Throwables.propagate(e);
		}
	}

	public static <P> P openPage(final String url, final int maxRetries, final Function<String, P> factory) {
		// Try multiple times, sometimes opening a page fails due to temporary Youtube problems
		for (int i = 0; i < maxRetries; i++) {
			try {
				final String source = URLs.copyToString(url);
				return factory.apply(source);
			}
			catch (final Exception e) {
				if (i < 9) {
					log.warn("Could not open page \"" + url + "\". Reason: \"" + e.getMessage() + "\" - Retrying...");
				}
				else {
					log.warn("Could not open page \"" + url + "\". Reason: \"" + e.getMessage() + "\" - Giving up...");
					throw Throwables.propagate(e);
				}
			}
		}

		// This code should never be reached
		throw new RuntimeException("Unexpected code section reached");
	}

	private static Logger	log	= LoggerFactory.getLogger(ChannelController.class);

	private URLs() {
		// Static utility class, no instances allowed
	}
}
