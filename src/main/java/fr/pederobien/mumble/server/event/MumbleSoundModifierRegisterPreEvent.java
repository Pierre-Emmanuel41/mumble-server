package fr.pederobien.mumble.server.event;

import java.util.StringJoiner;

import fr.pederobien.mumble.server.impl.SoundManager;
import fr.pederobien.mumble.server.interfaces.ISoundModifier;
import fr.pederobien.utils.ICancellable;

public class MumbleSoundModifierRegisterPreEvent extends MumbleSoundModifierEvent implements ICancellable {
	private boolean isCancelled;

	/**
	 * Creates an event thrown when a sound modifier is about to be registered in the {@link SoundManager}.
	 * 
	 * @param soundModifier The sound modifier that is about to be registered.
	 */
	public MumbleSoundModifierRegisterPreEvent(ISoundModifier soundModifier) {
		super(soundModifier);
	}

	@Override
	public boolean isCancelled() {
		return isCancelled;
	}

	@Override
	public void setCancelled(boolean isCancelled) {
		this.isCancelled = isCancelled;
	}

	@Override
	public String toString() {
		StringJoiner joiner = new StringJoiner(",", "{", "}");
		joiner.add("soundModifier=" + getSoundModifier().getName());
		return String.format("%s_%s", getName(), joiner);
	}
}
