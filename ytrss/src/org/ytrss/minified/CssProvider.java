package org.ytrss.minified;

import java.util.Arrays;
import java.util.regex.Pattern;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

@Component
public class CssProvider extends BaseMinifyProvider {

	private static Logger	log	= LoggerFactory.getLogger(CssProvider.class);

	@Override
	protected Iterable<String> getFiles() {
		return Arrays.asList("/webapp/WEB-INF/css/bootstrap.css", "/webapp/WEB-INF/css/ytrss.css");
	}

	@Override
	protected String minify(final String content) {
		log.info("Compressing CSS");

		// Remove comments
		String minified = Pattern.compile("(?:\\/\\*(?:[\\s\\S]*?)\\*\\/)|(?:([\\s;])+\\/\\/(?:.*)$)", Pattern.MULTILINE).matcher(content).replaceAll("");

		// Remove line breaks
		minified = minified.replaceAll("\\n", "");

		// Remove multiple whitespaces
		minified = minified.replaceAll("\\s+", " ");

		// Replace " : " with ":" (and similar cases)
		minified = minified.replaceAll("\\s*([:,{};])\\s*", "$1");

		// Replace ";}" (last attribute) with "}"
		minified = minified.replaceAll(";}", "}");

		return minified;
	}
}
