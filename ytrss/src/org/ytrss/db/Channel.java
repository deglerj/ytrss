package org.ytrss.db;

import org.hibernate.validator.constraints.NotBlank;
import org.hibernate.validator.constraints.URL;

public class Channel {

	private Long	id;

	@NotBlank
	private String	name;

	@NotBlank
	@URL
	private String	url;

	private String	includeRegex;

	private String	excludeRegex;

	public String getExcludeRegex() {
		return excludeRegex;
	}

	public Long getId() {
		return id;
	}

	public String getIncludeRegex() {
		return includeRegex;
	}

	public String getName() {
		return name;
	}

	public String getUrl() {
		return url;
	}

	public void setExcludeRegex(final String excludeRegex) {
		this.excludeRegex = excludeRegex;
	}

	public void setId(final Long id) {
		this.id = id;
	}

	public void setIncludeRegex(final String includeRegex) {
		this.includeRegex = includeRegex;
	}

	public void setName(final String name) {
		this.name = name;
	}

	public void setUrl(final String url) {
		this.url = url;
	}

}
