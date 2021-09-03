package fr.pederobien.mumble.server.event;

import fr.pederobien.mumble.server.interfaces.IChannel;
import fr.pederobien.mumble.server.interfaces.IMumbleServer;

public class ServerChannelAddPostEvent extends ServerEvent {
	private IChannel channel;

	/**
	 * Creates an event thrown when a channel has been added to a server.
	 * 
	 * @param server  The server to which a channel has been added.
	 * @param channel The added channel.
	 */
	public ServerChannelAddPostEvent(IMumbleServer server, IChannel channel) {
		super(server);
		this.channel = channel;
	}

	/**
	 * @return The added channel.
	 */
	public IChannel getChannel() {
		return channel;
	}
}
