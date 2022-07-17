package fr.pederobien.mumble.server.event;

import java.util.StringJoiner;

import fr.pederobien.mumble.server.interfaces.IPlayer;

public class MumblePlayerMuteByChangePostEvent extends MumblePlayerEvent {
	private IPlayer source;
	private boolean oldMute;

	/**
	 * Creates an event thrown when a target player has been mute or unmute ofr a source player.
	 * 
	 * @param target  The target player to mute or unmute for a source player.
	 * @param source  The source player for which the target player is mute or unmute.
	 * @param oldMute The old target player mute status for the source player.
	 */
	public MumblePlayerMuteByChangePostEvent(IPlayer target, IPlayer source, boolean oldMute) {
		super(target);
		this.source = source;
		this.oldMute = oldMute;
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
	 * @return The old mute status of the target player for the source player.
	 */
	public boolean getOldMute() {
		return oldMute;
	}

	@Override
	public String toString() {
		StringJoiner joiner = new StringJoiner(", ", "{", "}");
		joiner.add("target=" + getPlayer().getName());
		joiner.add("source=" + getSource().getName());
		joiner.add("currentMute=" + getPlayer().isMuteBy(getSource()));
		joiner.add("oldMute=" + getOldMute());
		return String.format("%s_%s", getName(), joiner);
	}
}