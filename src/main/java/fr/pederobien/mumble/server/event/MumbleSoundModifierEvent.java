package fr.pederobien.mumble.server.event;

import fr.pederobien.mumble.server.interfaces.ISoundModifier;

public class MumbleSoundModifierEvent extends ProjectMumbleServerEvent {
	private ISoundModifier soundModifier;

	/**
	 * Creates a sound modifier event.
	 * 
	 * @param soundModifier The sound modifier source involved in this event.
	 */
	public MumbleSoundModifierEvent(ISoundModifier soundModifier) {
		this.soundModifier = soundModifier;
	}

	/**
	 * @return The sound modifier involved in this event.
	 */
	public ISoundModifier getSoundModifier() {
		return soundModifier;
	}
}
