package fr.pederobien.mumble.server.event;

import java.util.StringJoiner;

import fr.pederobien.mumble.server.interfaces.IChannel;
import fr.pederobien.mumble.server.interfaces.IPlayer;
import fr.pederobien.utils.ICancellable;

public class MumblePlayerKickPreEvent extends MumblePlayerEvent implements ICancellable {
	private boolean isCancelled;
	private IChannel channel;
	private IPlayer kickingPlayer;

	/**
	 * Creates an event thrown when a player is about to kick another player from a channel.
	 * 
	 * @param kickedPlayer  The player that is about to be kicked.
	 * @param channel       The channel from which the player is about to be kicked.
	 * @param kickingPlayer The player that is about to kick another player.
	 */
	public MumblePlayerKickPreEvent(IPlayer kickedPlayer, IChannel channel, IPlayer kickingPlayer) {
		super(kickedPlayer);
		this.channel = channel;
		this.kickingPlayer = kickingPlayer;
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
	 * @return The player that is about to be kick from a channel.
	 */
	@Override
	public IPlayer getPlayer() {
		return super.getPlayer();
	}

	/**
	 * @return The channel from which the player is about to be kicked.
	 */
	public IChannel getChannel() {
		return channel;
	}

	/**
	 * @return The player that is about to kick another player.
	 */
	public IPlayer getKickingPlayer() {
		return kickingPlayer;
	}

	@Override
	public String toString() {
		StringJoiner joiner = new StringJoiner(", ", "{", "}");
		joiner.add("kicked=" + getPlayer().getName());
		joiner.add("channel=" + getChannel().getName());
		joiner.add("kicking=" + getKickingPlayer().getName());
		return String.format("%s_%s", getName(), joiner);
	}
}
