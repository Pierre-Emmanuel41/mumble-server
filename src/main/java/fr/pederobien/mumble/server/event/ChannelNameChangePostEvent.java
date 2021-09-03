package fr.pederobien.mumble.server.event;

import fr.pederobien.mumble.server.interfaces.IChannel;

public class ChannelNameChangePostEvent extends ChannelEvent {
	private String oldName;

	/**
	 * Creates an event thrown when a channel has been renamed.
	 * 
	 * @param channel The channel that has been renamed.
	 * @param oldName The old channel name.
	 */
	public ChannelNameChangePostEvent(IChannel channel, String oldName) {
		super(channel);
		this.oldName = oldName;
	}

	/**
	 * @return The old channel name.
	 */
	public String getOldName() {
		return oldName;
	}
}
