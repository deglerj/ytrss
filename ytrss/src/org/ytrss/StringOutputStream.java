package org.ytrss;

import org.apache.commons.exec.LogOutputStream;

public class StringOutputStream extends LogOutputStream {

	private final StringBuffer lines = new StringBuffer();

	@Override
	public String toString() {
		return lines.toString();
	}

	@Override
	protected void processLine(final String line, final int level) {
		if (lines.length() != 0) {
			lines.append("\n");
		}
		lines.append(line);
	}
}