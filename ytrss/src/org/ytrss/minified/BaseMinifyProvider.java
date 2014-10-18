package org.ytrss.minified;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.base.Throwables;
import com.google.common.io.CharStreams;

public abstract class BaseMinifyProvider {

	private final String	minified;

	private final String	id;

	private static Logger	log	= LoggerFactory.getLogger(BaseMinifyProvider.class);

	public BaseMinifyProvider() {
		minified = minify(readFilesCombined());
		id = String.valueOf(System.currentTimeMillis());
	}

	public String getId() {
		return id;
	}

	public String getMinified() {
		// Check if development mode is active (disables caching of CSS and JS resources)
		final boolean development = "true".equalsIgnoreCase(System.getProperty("dev"));
		if (development) {
			return minify(readFilesCombined());
		}

		return minified;
	}

	protected abstract Iterable<String> getFiles();

	protected abstract String minify(String content);

	private String readFile(final String file) {
		try (final InputStream in = BaseMinifyProvider.class.getResourceAsStream(file); final InputStreamReader reader = new InputStreamReader(in);) {
			return CharStreams.toString(reader);
		}
		catch (final IOException e) {
			log.error("Could not read file {}", file, e);
			throw Throwables.propagate(e);
		}
	}

	private String readFilesCombined() {
		final StringBuilder combined = new StringBuilder();
		for (final String file : getFiles()) {
			combined.append(readFile(file));
		}

		return combined.toString();
	}

}
