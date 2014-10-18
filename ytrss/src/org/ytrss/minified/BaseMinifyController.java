package org.ytrss.minified;

import java.io.IOException;

import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.io.IOUtils;

public abstract class BaseMinifyController {

	private static long	STARTUP_DATE	= System.currentTimeMillis();

	protected abstract String getMimeType();

	protected void writeResponse(final String content, final HttpServletResponse response, final HttpServletRequest request) throws IOException {
		// Check if development mode is active (disables caching of CSS and JS resources)
		final boolean development = "true".equalsIgnoreCase(System.getProperty("dev"));

		// Not in development mode and "If-Modified-Header" present? -> Client has downloaded CSS before, send 304 code, forcing client to use its cache
		if (!development && request.getHeader("If-Modified-Since") != null) {
			response.sendError(304); // Not Modified
			return;
		}

		response.setHeader("Cache-Control", "must-revalidate, private");
		response.setDateHeader("Last-Modified", STARTUP_DATE);
		response.setHeader("Expires", "-1");
		response.setHeader("Date", "");
		response.setHeader("Pragma", "");

		response.setContentType(getMimeType());

		final ServletOutputStream out = response.getOutputStream();
		IOUtils.write(content, out);
	}

}
