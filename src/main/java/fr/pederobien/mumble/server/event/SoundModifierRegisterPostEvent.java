package fr.pederobien.mumble.server.event;

import fr.pederobien.mumble.server.impl.SoundManager;
import fr.pederobien.mumble.server.interfaces.ISoundModifier;

public class SoundModifierRegisterPostEvent extends SoundModifierEvent {

	/**
	 * Creates an event thrown when a sound modifier has been registered in the {@link SoundManager}.
	 * 
	 * @param soundModifier The sound modifier that has been registered.
	 */
	public SoundModifierRegisterPostEvent(ISoundModifier soundModifier) {
		super(soundModifier);
	}
}
