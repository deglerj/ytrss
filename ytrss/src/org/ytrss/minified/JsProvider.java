package org.ytrss.minified;

import java.util.Arrays;
import java.util.regex.Pattern;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

@Component
public class JsProvider extends BaseMinifyProvider {

	private static Logger	log	= LoggerFactory.getLogger(JsProvider.class);

	@Override
	protected Iterable<String> getFiles() {
		return Arrays.asList("/webapp/WEB-INF/js/jquery-2.1.1.js", "/webapp/WEB-INF/js/bootstrap.js", "/webapp/WEB-INF/js/ytrss.js",
				"/webapp/WEB-INF/js/sockjs-0.3.4.js");
	}

	@Override
	protected String minify(final String content) {
		log.info("Compressing JavaScript");

		// Remove comments
		String minified = Pattern.compile("(?:\\/\\*(?:[\\s\\S]*?)\\*\\/)|(?:([\\s;])+\\/\\/(?:.*)$)", Pattern.MULTILINE).matcher(content).replaceAll("");

		// Remove whitespace at line start
		minified = Pattern.compile("^\\s+", Pattern.MULTILINE).matcher(minified).replaceAll("");

		// Remove line break where safe
		minified = Pattern.compile(";\\s*\\n", Pattern.MULTILINE).matcher(minified).replaceAll(";");

		return minified;
	}
}
