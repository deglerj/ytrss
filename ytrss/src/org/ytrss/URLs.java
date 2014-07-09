package org.ytrss;

import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;

import org.apache.commons.io.IOUtils;

import com.google.common.base.Throwables;

public class URLs {

	public static String copyToString(String url) {
		try {
			return copyToString(new URL(url));
		}
		catch (MalformedURLException e) {
			throw Throwables.propagate(e);
		}
	}

	public static String copyToString(URL url) {
		try (InputStream input = url.openStream()) {
			String string = IOUtils.toString(input);
			return string;
		}
		catch (IOException e) {
			throw Throwables.propagate(e);
		}
	}
}
