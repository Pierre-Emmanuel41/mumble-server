package fr.pederobien.mumble.server.event;

import java.util.StringJoiner;

import fr.pederobien.mumble.server.interfaces.IChannel;
import fr.pederobien.mumble.server.interfaces.ISoundModifier;
import fr.pederobien.utils.ICancellable;

public class ChannelSoundModifierChangePreEvent extends ChannelEvent implements ICancellable {
	private boolean isCancelled;
	private ISoundModifier newSoundModifier;

	/**
	 * Creates an event thrown when the sound modifier of a channel is about to be changed.
	 * 
	 * @param channel          The channel whose the sound modifier is about to change.
	 * @param newSoundModifier The future new sound modifier.
	 */
	public ChannelSoundModifierChangePreEvent(IChannel channel, ISoundModifier newSoundModifier) {
		super(channel);
		this.newSoundModifier = newSoundModifier;
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
	 * @return The channel sound modifier.
	 */
	public ISoundModifier getNewSoundModifier() {
		return newSoundModifier;
	}

	@Override
	public String toString() {
		StringJoiner joiner = new StringJoiner(",", "{", "}");
		joiner.add("channel=" + getChannel().getName());
		joiner.add("currentSoundModifier=" + getChannel().getSoundModifier().getName());
		joiner.add("newSoundModifier=" + getNewSoundModifier().getName());
		return String.format("%s_%s", getName(), joiner);
	}
}
