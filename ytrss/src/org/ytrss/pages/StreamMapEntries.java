package org.ytrss.pages;

public class StreamMapEntries {

	public static String getExtension(final StreamMapEntry entry) {
		switch (entry.getType()) {
			case "video/mp4":
				return "mp4";
			case "video/webm":
				return "webm";
			case "video/x-flv":
				return "flv";
			case "video/3gpp":
				return "3gp";
			default:
				System.out.println("Unknown video type \"" + entry.getType() + "\"");
				return "avi";
		}
	}

	private StreamMapEntries() {
		// Static utility class, no instances allowed
	}
}
