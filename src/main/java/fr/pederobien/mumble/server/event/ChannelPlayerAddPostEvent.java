package fr.pederobien.mumble.server.event;

import fr.pederobien.mumble.server.interfaces.IChannel;
import fr.pederobien.mumble.server.interfaces.IPlayer;

public class ChannelPlayerAddPostEvent extends ChannelEvent {
	private IPlayer player;

	/**
	 * Creates an event thrown when a player has been added to a channel.
	 * 
	 * @param channel The channel to which a player has been added.
	 * @param player  The added player.
	 */
	public ChannelPlayerAddPostEvent(IChannel channel, IPlayer player) {
		super(channel);
		this.player = player;
	}

	/**
	 * @return The added player.
	 */
	public IPlayer getPlayer() {
		return player;
	}
}
