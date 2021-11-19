package fr.pederobien.mumble.server.impl;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import fr.pederobien.mumble.server.event.SoundModifierRegisterPostEvent;
import fr.pederobien.mumble.server.event.SoundModifierRegisterPreEvent;
import fr.pederobien.mumble.server.event.SoundModifierUnregisterPostEvent;
import fr.pederobien.mumble.server.event.SoundModifierUnregisterPreEvent;
import fr.pederobien.mumble.server.impl.modifiers.AbstractSoundModifier;
import fr.pederobien.mumble.server.impl.modifiers.Parameter;
import fr.pederobien.mumble.server.impl.modifiers.RangeParameter;
import fr.pederobien.mumble.server.interfaces.IPlayer;
import fr.pederobien.mumble.server.interfaces.ISoundModifier;
import fr.pederobien.utils.event.EventManager;

public class SoundManager {
	public static final String DEFAULT_SOUND_MODIFIER_NAME = "default";
	private static Map<String, ISoundModifier> sounds;

	static {
		sounds = new HashMap<String, ISoundModifier>();
		sounds.put(DEFAULT_SOUND_MODIFIER_NAME, new DefaultSoundModifier());
	}

	private SoundManager() {
	}

	/**
	 * Adds the given sound modifier to the collection of modifiers managed by this manager.
	 * 
	 * @param soundModifier The sound modifier constructor to add.
	 */
	public static void add(ISoundModifier soundModifier) {
		if (soundModifier.getName().equals(DEFAULT_SOUND_MODIFIER_NAME))
			return;

		Runnable add = () -> sounds.put(soundModifier.getName(), soundModifier);
		EventManager.callEvent(new SoundModifierRegisterPreEvent(soundModifier), add, new SoundModifierRegisterPostEvent(soundModifier));
	}

	/**
	 * Removes the given sound modifier from the collection of modifiers managed by this manager.
	 * 
	 * @param soundModifier The sound modifier to remove.
	 * 
	 * @return True if the modifier was in the list, false otherwise.
	 */
	public static boolean remove(ISoundModifier soundModifier) {
		if (soundModifier.getName().equals(DEFAULT_SOUND_MODIFIER_NAME))
			return false;

		SoundModifierUnregisterPreEvent preEvent = new SoundModifierUnregisterPreEvent(soundModifier);
		EventManager.callEvent(preEvent);
		if (preEvent.isCancelled())
			return false;

		boolean registered = sounds.remove(soundModifier.getName()) != null;
		if (registered)
			EventManager.callEvent(new SoundModifierUnregisterPostEvent(soundModifier));
		return registered;
	}

	/**
	 * Get the sound modifier associated to the given name.
	 * 
	 * @param name The sound modifier name.
	 * 
	 * @return An optional that contains the sound modifier if it exist, an empty optional otherwise.
	 */
	public static Optional<ISoundModifier> getByName(String name) {
		ISoundModifier soundModifier = sounds.get(name);
		return Optional.ofNullable(soundModifier == null ? null : soundModifier.clone());
	}

	/**
	 * @return The sound modifier associated to the name {@link #DEFAULT_SOUND_MODIFIER_NAME}.
	 */
	public static ISoundModifier getDefaultSoundModifier() {
		return getByName(DEFAULT_SOUND_MODIFIER_NAME).get();
	}

	/**
	 * @return A map that contains all registered sound modifiers.
	 */
	public static Map<String, ISoundModifier> getSoundModifiers() {
		return sounds;
	}

	private static class DefaultSoundModifier extends AbstractSoundModifier {

		public DefaultSoundModifier() {
			super(DEFAULT_SOUND_MODIFIER_NAME);
			getParametersList().add(Parameter.of(this, "Enabled", true));
			getParametersList().add(RangeParameter.of(this, "Radius", 50, 0, 100));
			getParametersList().add(Parameter.of(this, "Slope", 1.0 / 50.0));
		}

		@Override
		public VolumeResult calculate(IPlayer transmitter, IPlayer receiver) {
			return VolumeResult.DEFAULT;
		}
	}
}
