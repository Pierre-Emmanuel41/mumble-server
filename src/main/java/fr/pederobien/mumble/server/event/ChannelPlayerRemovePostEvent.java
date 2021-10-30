package fr.pederobien.mumble.server.event;

import java.util.StringJoiner;

import fr.pederobien.mumble.server.interfaces.IChannel;
import fr.pederobien.mumble.server.interfaces.IPlayer;

public class ChannelPlayerRemovePostEvent extends ChannelEvent {
	private IPlayer player;

	/**
	 * Creates an event thrown when a player has been removed from a channel.
	 * 
	 * @param channel The channel from which a player has been removed.
	 * @param player  The removed player.
	 */
	public ChannelPlayerRemovePostEvent(IChannel channel, IPlayer player) {
		super(channel);
		this.player = player;
	}

	/**
	 * @return The removed player.
	 */
	public IPlayer getPlayer() {
		return player;
	}

	@Override
	public String toString() {
		StringJoiner joiner = new StringJoiner(",", "{", "}");
		joiner.add("channel=" + getChannel().getName());
		joiner.add("player=" + getPlayer().getName());
		return String.format("%s_%s", getName(), joiner);
	}
}
