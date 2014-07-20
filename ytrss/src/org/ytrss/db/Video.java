package org.ytrss.db;

import java.sql.Date;
import java.sql.Timestamp;

import javax.validation.constraints.NotNull;

import org.hibernate.validator.constraints.NotBlank;

public class Video {

	private Long		id;

	@NotBlank
	private Long		channelID;

	@NotBlank
	private String		youtubeID;

	@NotNull
	private Date		uploaded;

	@NotBlank
	private String		name;

	@NotNull
	private Timestamp	discovered;

	@NotNull
	private VideoState	state;

	private String		securityToken;

	private String		videoFile;

	private String		mp3File;

	private String		errorMessage;

	@Override
	public boolean equals(final Object obj) {
		if (this == obj) {
			return true;
		}
		if (obj == null) {
			return false;
		}
		if (!(obj instanceof Video)) {
			return false;
		}
		final Video other = (Video) obj;
		if (id == null) {
			if (other.id != null) {
				return false;
			}
		}
		else if (!id.equals(other.id)) {
			return false;
		}
		if (youtubeID == null) {
			if (other.youtubeID != null) {
				return false;
			}
		}
		else if (!youtubeID.equals(other.youtubeID)) {
			return false;
		}
		return true;
	}

	public Long getChannelID() {
		return channelID;
	}

	public Timestamp getDiscovered() {
		return discovered;
	}

	public String getErrorMessage() {
		return errorMessage;
	}

	public Long getId() {
		return id;
	}

	public String getMp3File() {
		return mp3File;
	}

	public String getName() {
		return name;
	}

	public String getSecurityToken() {
		return securityToken;
	}

	public VideoState getState() {
		return state;
	}

	public Date getUploaded() {
		return uploaded;
	}

	public String getVideoFile() {
		return videoFile;
	}

	public String getYoutubeID() {
		return youtubeID;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((id == null) ? 0 : id.hashCode());
		result = prime * result + ((youtubeID == null) ? 0 : youtubeID.hashCode());
		return result;
	}

	public void setChannelID(final Long channelID) {
		this.channelID = channelID;
	}

	public void setDiscovered(final Timestamp discovered) {
		this.discovered = discovered;
	}

	public void setErrorMessage(final String errorMessage) {
		this.errorMessage = errorMessage;
	}

	public void setId(final Long id) {
		this.id = id;
	}

	public void setMp3File(final String mp3File) {
		this.mp3File = mp3File;
	}

	public void setName(final String name) {
		this.name = name;
	}

	public void setSecurityToken(final String securityToken) {
		this.securityToken = securityToken;
	}

	public void setState(final VideoState state) {
		this.state = state;
	}

	public void setUploaded(final Date uploaded) {
		this.uploaded = uploaded;
	}

	public void setVideoFile(final String videoFile) {
		this.videoFile = videoFile;
	}

	public void setYoutubeID(final String youtubeID) {
		this.youtubeID = youtubeID;
	}

}
