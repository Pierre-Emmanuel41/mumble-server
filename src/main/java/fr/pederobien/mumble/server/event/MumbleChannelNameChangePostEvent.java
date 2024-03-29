package fr.pederobien.mumble.server.event;

import java.util.StringJoiner;

import fr.pederobien.mumble.server.interfaces.IChannel;

public class MumbleChannelNameChangePostEvent extends MumbleChannelEvent {
	private String oldName;

	/**
	 * Creates an event thrown when a channel has been renamed.
	 * 
	 * @param channel The channel that has been renamed.
	 * @param oldName The old channel name.
	 */
	public MumbleChannelNameChangePostEvent(IChannel channel, String oldName) {
		super(channel);
		this.oldName = oldName;
	}

	/**
	 * @return The old channel name.
	 */
	public String getOldName() {
		return oldName;
	}

	@Override
	public String toString() {
		StringJoiner joiner = new StringJoiner(",", "{", "}");
		joiner.add("channel=" + getChannel().getName());
		joiner.add("oldName=" + getOldName());
		return String.format("%s_%s", getName(), joiner);
	}
}
