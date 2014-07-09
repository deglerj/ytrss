package org.ytrss;

public class StreamMapEntry {

	private String	url;

	private String	type;

	private String	quality;

	public StreamMapEntry(String url, String type, String quality) {
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
