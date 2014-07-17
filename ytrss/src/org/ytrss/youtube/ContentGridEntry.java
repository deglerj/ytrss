package org.ytrss.youtube;

public class ContentGridEntry {

	private final String	title;

	private final String	href;

	public ContentGridEntry(final String title, final String href) {
		this.title = title;
		this.href = href;
	}

	public String getHref() {
		return href;
	}

	public String getTitle() {
		return title;
	}

}
