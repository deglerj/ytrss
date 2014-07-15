package org.ytrss.db;

import java.util.Objects;

public class Videos {

	public static String createToken(final Video video) {
		// This should be reasonably secure: A potential attacker does not know which Youtube video belongs to which video entity ID and ID and discovered
		// timestamp are system generated values

		final int hash = Objects.hash(video.getDiscovered(), video.getUploaded(), video.getId());
		return video.getYoutubeID() + Math.abs(hash);
	}

	public static String getFileName(final Video video) {
		final String name = video.getName();
		final String cleanName = name.replaceAll("[^\\w\\d]", "");
		return cleanName + "_" + video.getYoutubeID();
	}

	private Videos() {
		// Static utility class, no instances allowed
	}

}
