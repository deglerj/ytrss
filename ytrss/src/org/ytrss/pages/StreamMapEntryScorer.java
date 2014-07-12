package org.ytrss.pages;

import java.util.Comparator;
import java.util.List;

public class StreamMapEntryScorer {

	public StreamMapEntry findBestEntry(final List<StreamMapEntry> entries) {
		entries.sort(Comparator.comparingInt(value -> getScore((StreamMapEntry) value)).reversed());

		return entries.get(0);
	}

	private int getScore(final StreamMapEntry entry) {
		return getTypeScore(entry.getType()) + getQualityScore(entry.getQuality());
	}

	private int getQualityScore(final String quality) {
		switch (quality) {
			case "medium":
				return 10;
			case "hd720":
				return 6;
			case "hd1080":
				return 2;
			case "small":
				return 1;
			default:
				System.out.println("Unknown quality type \"" + quality + "\"");
				return 0;
		}
	}

	private int getTypeScore(final String type) {
		switch (type) {
			case "video/mp4":
				return 10;
			case "video/webm":
				return 8;
			case "video/x-flv":
				return 3;
			case "video/3gpp":
				return 2;
			default:
				System.out.println("Unknown video type \"" + type + "\"");
				return 0;
		}
	}
}
