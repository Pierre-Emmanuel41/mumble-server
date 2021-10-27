package fr.pederobien.mumble.server.event;

import java.util.StringJoiner;

import fr.pederobien.mumble.server.interfaces.IChannel;
import fr.pederobien.mumble.server.interfaces.ISoundModifier;

public class ChannelSoundModifierChangePostEvent extends ChannelEvent {
	private ISoundModifier oldSoundModifier;

	/**
	 * Creates an event thrown when the sound modifier of a channel has changed.
	 * 
	 * @param channel          The channel whose the sound modifier has changed.
	 * @param oldSoundModifier The old sound modifier of the channel.
	 */
	public ChannelSoundModifierChangePostEvent(IChannel channel, ISoundModifier oldSoundModifier) {
		super(channel);
		this.oldSoundModifier = oldSoundModifier;
	}

	/**
	 * @return The old sound modifier of the channel.
	 */
	public ISoundModifier getOldSoundModifier() {
		return oldSoundModifier;
	}

	@Override
	public String toString() {
		StringJoiner joiner = new StringJoiner(",", "{", "}");
		joiner.add("channel=" + getChannel().getName());
		joiner.add("oldSoundModifier=" + getOldSoundModifier().getName());
		joiner.add("currentSoundModifier=" + getChannel().getSoundModifier().getName());
		return String.format("%s_%s", getName(), joiner);
	}
}
