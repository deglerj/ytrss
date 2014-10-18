package org.ytrss.minified;

import java.io.IOException;
import java.io.StringReader;
import java.io.StringWriter;
import java.util.Arrays;

import org.apache.commons.io.IOUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import com.google.common.base.Throwables;
import com.yahoo.platform.yui.compressor.CssCompressor;

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

		final StringReader reader = new StringReader(content);
		final StringWriter writer = new StringWriter();
		try {
			final CssCompressor compressor = new CssCompressor(reader);
			compressor.compress(writer, -1);
		}
		catch (final IOException e) {
			// This should never happen since we're not using any file resources
			throw Throwables.propagate(e);
		}

		IOUtils.closeQuietly(reader);
		IOUtils.closeQuietly(writer);

		log.info("CSS compressed");

		return writer.toString();
	}
}
