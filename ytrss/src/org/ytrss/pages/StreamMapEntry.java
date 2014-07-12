package org.ytrss.pages;

public class StreamMapEntry {

	private final String	url;

	private final String	type;

	private final String	quality;

	public StreamMapEntry(final String url, final String type, final String quality) {
		this.url = url;
		this.type = type;
		this.quality = quality;
	}

	public String getUrl() {
		return url;
	}

	public String getType() {
		return type;
	}

	public String getQuality() {
		return quality;
	}

}
