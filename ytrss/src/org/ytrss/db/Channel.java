package org.ytrss.db;

import javax.validation.constraints.Max;
import javax.validation.constraints.Min;
import javax.validation.constraints.NotNull;

import org.hibernate.validator.constraints.NotBlank;
import org.hibernate.validator.constraints.URL;
import org.ytrss.URLs;

public class Channel {

	private Long	id;

	@NotBlank
	private String	name;

	@NotBlank
	@URL
	@ChannelURL
	private String	url;

	private String	securityToken;

	@RegularExpression
	private String	includeRegex;

	@RegularExpression
	private String	excludeRegex;

	@NotNull
	@Min(1)
	@Max(50)
	private Integer	maxVideos;

	private boolean	hidden;

	@Override
	public boolean equals(final Object obj) {
		if (this == obj) {
			return true;
		}
		if (obj == null) {
			return false;
		}
		if (!(obj instanceof Channel)) {
			return false;
		}
		final Channel other = (Channel) obj;
		if (id == null) {
			if (other.id != null) {
				return false;
			}
		}
		else if (!id.equals(other.id)) {
			return false;
		}
		if (name == null) {
			if (other.name != null) {
				return false;
			}
		}
		else if (!name.equals(other.name)) {
			return false;
		}
		return true;
	}

	public String getExcludeRegex() {
		return excludeRegex;
	}

	public Long getId() {
		return id;
	}

	public String getIncludeRegex() {
		return includeRegex;
	}

	public Integer getMaxVideos() {
		return maxVideos;
	}

	public String getName() {
		return name;
	}

	public String getSecurityToken() {
		return securityToken;
	}

	public String getUrl() {
		return url;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((id == null) ? 0 : id.hashCode());
		result = prime * result + ((name == null) ? 0 : name.hashCode());
		return result;
	}

	public boolean isHidden() {
		return hidden;
	}

	public void setExcludeRegex(final String excludeRegex) {
		this.excludeRegex = excludeRegex;
	}

	public void setHidden(final boolean hidden) {
		this.hidden = hidden;
	}

	public void setId(final Long id) {
		this.id = id;
	}

	public void setIncludeRegex(final String includeRegex) {
		this.includeRegex = includeRegex;
	}

	public void setMaxVideos(final Integer maxVideos) {
		this.maxVideos = maxVideos;
	}

	public void setName(final String name) {
		this.name = name;
	}

	public void setSecurityToken(final String securityToken) {
		this.securityToken = securityToken;
	}

	public void setUrl(final String url) {
		this.url = URLs.cleanUpURL(url);
	}

	@Override
	public String toString() {
		return "Channel [id=" + id + ", name=" + name + "]";
	}

}
