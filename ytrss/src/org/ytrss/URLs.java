package org.ytrss;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;

import org.apache.commons.io.IOUtils;

import com.google.common.base.Throwables;

public class URLs {

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
			throw Throwables.propagate(e);
		}
	}

	private URLs() {
		// Static utility class, no instances allowed
	}
}
