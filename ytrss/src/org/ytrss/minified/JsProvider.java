package org.ytrss.minified;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintStream;
import java.io.Reader;
import java.io.StringReader;
import java.io.StringWriter;
import java.io.Writer;
import java.util.Arrays;

import org.apache.commons.io.IOUtils;
import org.mozilla.javascript.tools.ToolErrorReporter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import com.google.common.base.Throwables;
import com.yahoo.platform.yui.compressor.JavaScriptCompressor;

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

		final Reader reader = new StringReader(content);
		final Writer writer = new StringWriter();

		final OutputStream errors = new ByteArrayOutputStream();
		final PrintStream errorsStream = new PrintStream(errors);

		try {
			final JavaScriptCompressor compressor = new JavaScriptCompressor(reader, new ToolErrorReporter(false, errorsStream));
			compressor.compress(writer, -1, false, false, false, false);
		}
		catch (final IOException e) {
			// This should never happen since we're not using any file resources
			throw Throwables.propagate(e);
		}

		IOUtils.closeQuietly(reader);
		IOUtils.closeQuietly(writer);
		IOUtils.closeQuietly(errorsStream);
		IOUtils.closeQuietly(errors);

		log.info("JavaScript compressed");

		return writer.toString();
	}
}
