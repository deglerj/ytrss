package org.ytrss.pages;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class StreamMapEntries {

	public static String getExtension(final StreamMapEntry entry) {
		// Don't forget to add new extensions to the regex in Cleaner#cleanUpOrphanedVideoFiles
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
				log.warn("Unknown video type \"" + entry.getType() + "\"");
				return "avi";
		}
	}

	private static Logger	log	= LoggerFactory.getLogger(StreamMapEntries.class);

	private StreamMapEntries() {
		// Static utility class, no instances allowed
	}
}
