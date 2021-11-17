package fr.pederobien.mumble.server.event;

import fr.pederobien.mumble.server.impl.SoundManager;
import fr.pederobien.mumble.server.interfaces.ISoundModifier;

public class SoundModifierUnregisterPostEvent extends SoundModifierEvent {

	/**
	 * Creates an event thrown when a sound modifier has been unregistered from the {@link SoundManager}.
	 * 
	 * @param soundModifier The sound modifier that has been unregistered.
	 */
	public SoundModifierUnregisterPostEvent(ISoundModifier soundModifier) {
		super(soundModifier);
	}
}
