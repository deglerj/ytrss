package org.ytrss.db;

public class Videos {

	public static String getFileName(final Video video) {
		final String name = video.getName();
		final String cleanName = name.replaceAll("[^\\w\\d]", "");
		return cleanName + "_" + video.getYoutubeID();
	}

	private Videos() {
		// Static utility class, no instances allowed
	}

}
