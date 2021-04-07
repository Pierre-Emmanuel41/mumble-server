package fr.pederobien.mumble.server.persistence;

public enum EMumbleXmlTag {
	CHANNELS("channels"), CHANNEL("channel"), CHANNEL_NAME("channelName"), SOUND_MODIFIER_NAME("soundModifierName");

	private String name;

	private EMumbleXmlTag(String name) {
		this.name = name;
	}

	@Override
	public String toString() {
		return name;
	}
}
