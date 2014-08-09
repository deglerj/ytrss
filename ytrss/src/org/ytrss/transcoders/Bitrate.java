package org.ytrss.transcoders;

public enum Bitrate {

	CBR_64("64 Kbit/s"), CBR_96("96 Kbit/s"), CBR_128("128 Kbit/s"), CBR_192("192 Kbit/s"), VBR_LOW("VBR low (45-84 Kbit/s)"), VBR_MEDIUM(
			"VBR medium (140-185 Kbit/s)"), VBR_HIGH("VBR high (190-250 Kbit/s)");

	private String	description;

	private Bitrate(final String description) {
		this.description = description;
	}

	public String getDescription() {
		return description;
	}

	@Override
	public String toString() {
		return getDescription();
	}

}
