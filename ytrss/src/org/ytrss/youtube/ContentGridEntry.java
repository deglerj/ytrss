package org.ytrss.youtube;

public class ContentGridEntry {

	private final String	title;

	private final String	videoId;

	public ContentGridEntry(final String title, final String videoId) {
		this.title = title;
		this.videoId = videoId;
	}

	public String getTitle() {
		return title;
	}

	public String getVideoID() {
		return videoId;
	}

}
