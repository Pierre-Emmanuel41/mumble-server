package fr.pederobien.mumble.server.event;

import java.util.StringJoiner;

import fr.pederobien.mumble.server.interfaces.IPlayer;
import fr.pederobien.utils.ICancellable;

public class PlayerMuteByChangePreEvent extends PlayerEvent implements ICancellable {
	private boolean isCancelled, newMute;
	private IPlayer source;

	/**
	 * Creates an event thrown when a target player is about to be mute for a source player.
	 * 
	 * @param target  The target to mute or unmute for the source player.
	 * @param source  The source player for which the target is mute or unmute.
	 * @param newMute The new target player mute status for the source player.
	 */
	public PlayerMuteByChangePreEvent(IPlayer target, IPlayer source, boolean newMute) {
		super(target);
		this.source = source;
		this.newMute = newMute;
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
	 * Get the target player to mute or unmute for the source player.
	 */
	@Override
	public IPlayer getPlayer() {
		return super.getPlayer();
	}

	/**
	 * @return The source player for which the target player is mute or unmute.
	 */
	public IPlayer getSource() {
		return source;
	}

	/**
	 * @return The new mute status of the target player for the source player.
	 */
	public boolean getNewMute() {
		return newMute;
	}

	@Override
	public String toString() {
		StringJoiner joiner = new StringJoiner(", ", "{", "}");
		joiner.add("target=" + getPlayer().getName());
		joiner.add("source=" + getSource().getName());
		joiner.add("currentMute=" + getPlayer().isMuteBy(getSource()));
		joiner.add("newMute=" + getNewMute());
		return String.format("%s_%s", getName(), joiner);
	}
}
