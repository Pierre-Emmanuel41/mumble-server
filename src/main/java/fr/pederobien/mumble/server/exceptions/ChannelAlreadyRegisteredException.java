package fr.pederobien.mumble.server.exceptions;

import fr.pederobien.mumble.server.interfaces.IChannel;
import fr.pederobien.mumble.server.interfaces.IMumbleServer;

public class ChannelAlreadyRegisteredException extends RuntimeException {
	private static final long serialVersionUID = 1L;
	private IMumbleServer server;
	private IChannel channel;

	public ChannelAlreadyRegisteredException(IMumbleServer server, IChannel channel) {
		super(String.format("The channel %s is already registered in %s", channel.getName(), server.getName()));
		this.server = server;
		this.channel = channel;
	}

	/**
	 * @return The server invloved in this exception.
	 */
	public IMumbleServer getServer() {
		return server;
	}

	/**
	 * @return The registered channel.
	 */
	public IChannel getChannel() {
		return channel;
	}
}
