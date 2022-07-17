package fr.pederobien.mumble.server.event;

import fr.pederobien.mumble.server.interfaces.IChannel;

public class ChannelEvent extends ProjectMumbleServerEvent {
	private IChannel channel;

	/**
	 * Creates a channel event.
	 * 
	 * @param channel The channel source involved in this event.
	 */
	public ChannelEvent(IChannel channel) {
		this.channel = channel;
	}

	/**
	 * @return The channel involved in this event.
	 */
	public IChannel getChannel() {
		return channel;
	}
}
