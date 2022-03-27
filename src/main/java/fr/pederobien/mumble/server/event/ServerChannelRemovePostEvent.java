package fr.pederobien.mumble.server.event;

import java.util.StringJoiner;

import fr.pederobien.mumble.server.interfaces.IChannel;
import fr.pederobien.mumble.server.interfaces.IMumbleServer;

public class ServerChannelRemovePostEvent extends ServerEvent {
	private IChannel channel;

	/**
	 * Creates an event thrown when a channel has been removed from a server.
	 * 
	 * @param server  The server from which a channel has been removed.
	 * @param channel The removed channel.
	 */
	public ServerChannelRemovePostEvent(IMumbleServer server, IChannel channel) {
		super(server);
		this.channel = channel;
	}

	/**
	 * @return The removed channel.
	 */
	public IChannel getChannel() {
		return channel;
	}

	@Override
	public String toString() {
		StringJoiner joiner = new StringJoiner(",", "{", "}");
		joiner.add("server=" + getServer().getName());
		joiner.add("channel=" + getChannel().getName());
		return String.format("%s_%s", getName(), joiner);
	}
}
