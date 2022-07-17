package fr.pederobien.mumble.server.event;

import java.util.StringJoiner;

import fr.pederobien.mumble.server.interfaces.IChannel;
import fr.pederobien.mumble.server.interfaces.IMumbleServer;

public class MumbleServerChannelAddPostEvent extends MumbleServerEvent {
	private IChannel channel;

	/**
	 * Creates an event thrown when a channel has been added to a server.
	 * 
	 * @param server  The server to which a channel has been added.
	 * @param channel The added channel.
	 */
	public MumbleServerChannelAddPostEvent(IMumbleServer server, IChannel channel) {
		super(server);
		this.channel = channel;
	}

	/**
	 * @return The added channel.
	 */
	public IChannel getChannel() {
		return channel;
	}

	@Override
	public String toString() {
		StringJoiner joiner = new StringJoiner(",", "{", "}");
		joiner.add("server=" + getServer().getName());
		joiner.add("channel=" + getChannel().getName());
		joiner.add("soundModifier=" + getChannel().getSoundModifier().getName());
		return String.format("%s_%s", getName(), joiner);
	}
}
