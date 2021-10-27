package fr.pederobien.mumble.server.event;

import java.util.StringJoiner;

import fr.pederobien.mumble.server.interfaces.IChannel;
import fr.pederobien.mumble.server.interfaces.IMumbleServer;
import fr.pederobien.utils.ICancellable;

public class ServerChannelRemovePreEvent extends ServerEvent implements ICancellable {
	private boolean isCancelled;
	private IChannel channel;

	/**
	 * Creates an event thrown when a channel is about to be removed.
	 * 
	 * @param server  The server from which a channel is about to be removed.
	 * @param channel The removed channel.
	 */
	public ServerChannelRemovePreEvent(IMumbleServer server, IChannel channel) {
		super(server);
		this.channel = channel;
	}

	@Override
	public boolean isCancelled() {
		return isCancelled;
	}

	@Override
	public void setCancelled(boolean isCancelled) {
		this.isCancelled = isCancelled;
	}

	/**
	 * @return The channel that is about to be removed.
	 */
	public IChannel getChannel() {
		return channel;
	}

	@Override
	public String toString() {
		StringJoiner joiner = new StringJoiner(",", "{", "}");
		joiner.add("server=" + getServer().getName());
		joiner.add("channel=" + getChannel());
		return String.format("%s_%s", getName(), joiner);
	}
}
