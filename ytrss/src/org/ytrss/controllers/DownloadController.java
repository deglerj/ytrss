package org.ytrss.controllers;

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkState;

import java.io.Closeable;
import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.io.RandomAccessFile;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.ytrss.db.Video;
import org.ytrss.db.VideoDAO;

//Based on: http://balusc.blogspot.de/2009/02/fileservlet-supporting-resume-and.html
@Controller
public class DownloadController {

	private static class Range {
		private final long	start;

		private final long	end;

		private final long	length;

		private final long	total;

		private Range(final long start, final long end, final long total) {
			this.start = start;
			this.end = end;
			length = end - start + 1;
			this.total = total;
		}

	}

	private static final int	DEFAULT_BUFFER_SIZE	= 10240;					// ..bytes = 10KB.

	private static final long	DEFAULT_EXPIRE_TIME	= 604800000L;				// ..ms = 1 week.

	private static final String	MULTIPART_BOUNDARY	= "MULTIPART_BYTERANGES";

	@Autowired
	private VideoDAO			videoDAO;

	@RequestMapping(value = "/download", method = RequestMethod.GET)
	public void getDownload(@RequestParam("id") final long id, @RequestParam("token") final String token, final HttpServletRequest request,
			final HttpServletResponse response) throws IOException {
		processRequest(id, token, request, response, true);
	}

	@RequestMapping(value = "/download", method = RequestMethod.HEAD)
	public void headDownload(@RequestParam("id") final long id, @RequestParam("token") final String token, final HttpServletRequest request,
			final HttpServletResponse response) throws IOException {
		processRequest(id, token, request, response, false);
	}

	private boolean accepts(final String acceptHeader, final String toAccept) {
		final String[] acceptValues = acceptHeader.split("\\s*(,|;)\\s*");
		Arrays.sort(acceptValues);
		return Arrays.binarySearch(acceptValues, toAccept) > -1 || Arrays.binarySearch(acceptValues, toAccept.replaceAll("/.*$", "/*")) > -1
				|| Arrays.binarySearch(acceptValues, "*/*") > -1;
	}

	private void close(final Closeable resource) {
		if (resource != null) {
			try {
				resource.close();
			}
			catch (final IOException ignore) {
				// Ignore IOException. Only thrown when the client aborted the request.
			}
		}
	}

	private void copy(final RandomAccessFile input, final OutputStream output, final long start, final long length) throws IOException {
		final byte[] buffer = new byte[DEFAULT_BUFFER_SIZE];
		int read;

		if (input.length() == length) {
			// Write full range.
			while ((read = input.read(buffer)) > 0) {
				output.write(buffer, 0, read);
			}
		}
		else {
			// Write partial range.
			input.seek(start);
			long toRead = length;

			while ((read = input.read(buffer)) > 0) {
				if ((toRead -= read) > 0) {
					output.write(buffer, 0, read);
				}
				else {
					output.write(buffer, 0, (int) toRead + read);
					break;
				}
			}
		}
	}

	private boolean matches(final String matchHeader, final String toMatch) {
		final String[] matchValues = matchHeader.split("\\s*,\\s*");
		Arrays.sort(matchValues);
		return Arrays.binarySearch(matchValues, toMatch) > -1 || Arrays.binarySearch(matchValues, "*") > -1;
	}

	private void processRequest(final long id, final String token, final HttpServletRequest request, final HttpServletResponse response, final boolean content)
			throws IOException {
		// Load video and check security token
		final Video video = videoDAO.findById(id);
		checkArgument(token.equals(video.getSecurityToken()), "Token mismatch");

		// Get mp3 file and check if it exists
		final File file = new File(video.getMp3File());
		checkState(file.exists(), "MP3 file for video #" + video.getId() + " does not exist");

		// Prepare some variables. The ETag is an unique identifier of the file.
		final String fileName = file.getName();
		final long length = file.length();
		final long lastModified = file.lastModified();
		final String eTag = fileName + "_" + length + "_" + lastModified;
		final long expires = System.currentTimeMillis() + DEFAULT_EXPIRE_TIME;

		// Validate request headers for caching

		// If-None-Match header should contain "*" or ETag. If so, then return 304.
		final String ifNoneMatch = request.getHeader("If-None-Match");
		if (ifNoneMatch != null && matches(ifNoneMatch, eTag)) {
			response.setStatus(HttpServletResponse.SC_NOT_MODIFIED);
			response.setHeader("ETag", eTag); // Required in 304.
			response.setDateHeader("Expires", expires); // Postpone cache with 1 week.
			return;
		}

		// If-Modified-Since header should be greater than LastModified. If so, then return 304.
		// This header is ignored if any If-None-Match header is specified.
		final long ifModifiedSince = request.getDateHeader("If-Modified-Since");
		if (ifNoneMatch == null && ifModifiedSince != -1 && ifModifiedSince + 1000 > lastModified) {
			response.setStatus(HttpServletResponse.SC_NOT_MODIFIED);
			response.setHeader("ETag", eTag); // Required in 304.
			response.setDateHeader("Expires", expires); // Postpone cache with 1 week.
			return;
		}

		// Validate request headers for resume

		// If-Match header should contain "*" or ETag. If not, then return 412.
		final String ifMatch = request.getHeader("If-Match");
		if (ifMatch != null && !matches(ifMatch, eTag)) {
			response.sendError(HttpServletResponse.SC_PRECONDITION_FAILED);
			return;
		}

		// If-Unmodified-Since header should be greater than LastModified. If not, then return 412.
		final long ifUnmodifiedSince = request.getDateHeader("If-Unmodified-Since");
		if (ifUnmodifiedSince != -1 && ifUnmodifiedSince + 1000 <= lastModified) {
			response.sendError(HttpServletResponse.SC_PRECONDITION_FAILED);
			return;
		}

		// Validate and process range

		// Prepare some variables. The full Range represents the complete file.
		final Range full = new Range(0, length - 1, length);
		final List<Range> ranges = new ArrayList<>();

		// Validate and process Range and If-Range headers.
		final String range = request.getHeader("Range");
		if (range != null) {

			// Range header should match format "bytes=n-n,n-n,n-n...". If not, then return 416.
			if (!range.matches("^bytes=\\d*-\\d*(,\\d*-\\d*)*$")) {
				response.setHeader("Content-Range", "bytes */" + length); // Required in 416.
				response.sendError(HttpServletResponse.SC_REQUESTED_RANGE_NOT_SATISFIABLE);
				return;
			}

			// If-Range header should either match ETag or be greater then LastModified. If not,
			// then return full file.
			final String ifRange = request.getHeader("If-Range");
			if (ifRange != null && !ifRange.equals(eTag)) {
				try {
					final long ifRangeTime = request.getDateHeader("If-Range"); // Throws IAE if invalid.
					if (ifRangeTime != -1 && ifRangeTime + 1000 < lastModified) {
						ranges.add(full);
					}
				}
				catch (final IllegalArgumentException ignore) {
					ranges.add(full);
				}
			}

			// If any valid If-Range header, then process each part of byte range.
			if (ranges.isEmpty()) {
				for (final String part : range.substring(6).split(",")) {
					// Assuming a file with length of 100, the following examples returns bytes at:
					// 50-80 (50 to 80), 40- (40 to length=100), -20 (length-20=80 to length=100).
					long start = sublong(part, 0, part.indexOf("-"));
					long end = sublong(part, part.indexOf("-") + 1, part.length());

					if (start == -1) {
						start = length - end;
						end = length - 1;
					}
					else if (end == -1 || end > length - 1) {
						end = length - 1;
					}

					// Check if Range is syntactically valid. If not, then return 416.
					if (start > end) {
						response.setHeader("Content-Range", "bytes */" + length); // Required in 416.
						response.sendError(HttpServletResponse.SC_REQUESTED_RANGE_NOT_SATISFIABLE);
						return;
					}

					// Add range.
					ranges.add(new Range(start, end, length));
				}
			}
		}

		// Prepare and initialize response

		final String contentType = "audio/mpeg";
		final String accept = request.getHeader("Accept");
		final String disposition = accept != null && accepts(accept, contentType) ? "inline" : "attachment";

		// Initialize response.
		response.reset();
		response.setBufferSize(DEFAULT_BUFFER_SIZE);
		response.setHeader("Content-Disposition", disposition + ";filename=\"" + fileName + "\"");
		response.setHeader("Accept-Ranges", "bytes");
		response.setHeader("ETag", eTag);
		response.setDateHeader("Last-Modified", lastModified);
		response.setDateHeader("Expires", expires);

		// Send requested file (part(s)) to client

		// Prepare streams.
		RandomAccessFile input = null;
		OutputStream output = null;

		try {
			// Open streams.
			input = new RandomAccessFile(file, "r");
			output = response.getOutputStream();

			if (ranges.isEmpty() || ranges.get(0) == full) {

				// Return full file.
				final Range r = full;
				response.setContentType(contentType);
				response.setHeader("Content-Range", "bytes " + r.start + "-" + r.end + "/" + r.total);

				if (content) {
					response.setHeader("Content-Length", String.valueOf(r.length));

					// Copy full range.
					copy(input, output, r.start, r.length);
				}

			}
			else if (ranges.size() == 1) {

				// Return single part of file.
				final Range r = ranges.get(0);
				response.setContentType(contentType);
				response.setHeader("Content-Range", "bytes " + r.start + "-" + r.end + "/" + r.total);
				response.setHeader("Content-Length", String.valueOf(r.length));
				response.setStatus(HttpServletResponse.SC_PARTIAL_CONTENT); // 206.

				if (content) {
					// Copy single part range.
					copy(input, output, r.start, r.length);
				}

			}
			else {

				// Return multiple parts of file.
				response.setContentType("multipart/byteranges; boundary=" + MULTIPART_BOUNDARY);
				response.setStatus(HttpServletResponse.SC_PARTIAL_CONTENT); // 206.

				if (content) {
					// Cast back to ServletOutputStream to get the easy println methods.
					final ServletOutputStream sos = (ServletOutputStream) output;

					// Copy multi part range.
					for (final Range r : ranges) {
						// Add multipart boundary and header fields for every range.
						sos.println();
						sos.println("--" + MULTIPART_BOUNDARY);
						sos.println("Content-Type: " + contentType);
						sos.println("Content-Range: bytes " + r.start + "-" + r.end + "/" + r.total);

						// Copy single part range of multi part range.
						copy(input, output, r.start, r.length);
					}

					// End with multipart boundary.
					sos.println();
					sos.println("--" + MULTIPART_BOUNDARY + "--");
				}
			}
		}
		finally {
			// Gently close streams.
			close(output);
			close(input);
		}
	}

	private long sublong(final String value, final int beginIndex, final int endIndex) {
		final String substring = value.substring(beginIndex, endIndex);
		return (substring.length() > 0) ? Long.parseLong(substring) : -1;
	}

}
