package fr.pederobien.mumble.server.persistence;

public enum EMumbleXmlTag {
	CHANNELS("channels"), CHANNEL("channel"), NAME("name"), SOUND_MODIFIER("soundModifier"), PARAMETERS("parameters"), PARAMETER("parameter"), TYPE("type"),
	DEFAULT_VALUE("defaultValue"), VALUE("value"), RANGE("range"), RANGE_MIN("min"), RANGE_MAX("max");

	private String name;

	private EMumbleXmlTag(String name) {
		this.name = name;
	}

	@Override
	public String toString() {
		return name;
	}
}
