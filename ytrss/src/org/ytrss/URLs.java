package org.ytrss;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;

import org.apache.commons.io.IOUtils;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.ytrss.controllers.ChannelController;

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

	public static String getSource(final String url, final boolean nonHtmlMode) {
		// Try multiple times, sometimes opening a page fails due to temporary Youtube problems
		for (int i = 0; i < 20; i++) {
			try {
				return nonHtmlMode ? getNonHtmlSource(url) : getHtmlSource(url);
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

	private static String getHtmlSource(final String url) throws IOException {
		final Document doc = Jsoup.connect(url).userAgent("Mozilla/5.0 (Windows NT 6.1; WOW64; rv:34.0) Gecko/20100101 Firefox/34.0")
				.header("Accept-Language", "en-US;q=0.7,en;q=0.3").get();
		return doc.html();
	}

	private static String getNonHtmlSource(final String url) throws MalformedURLException, IOException {
		return IOUtils.toString(new URL(url), "UTF-8");
	}

	private static Logger	log	= LoggerFactory.getLogger(ChannelController.class);

	private URLs() {
		// Static utility class, no instances allowed
	}
}
