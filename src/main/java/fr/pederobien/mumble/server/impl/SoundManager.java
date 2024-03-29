package fr.pederobien.mumble.server.impl;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Stream;

import fr.pederobien.mumble.server.event.MumbleSoundModifierRegisterPostEvent;
import fr.pederobien.mumble.server.event.MumbleSoundModifierRegisterPreEvent;
import fr.pederobien.mumble.server.event.MumbleSoundModifierUnregisterPostEvent;
import fr.pederobien.mumble.server.event.MumbleSoundModifierUnregisterPreEvent;
import fr.pederobien.mumble.server.impl.modifiers.SoundModifier;
import fr.pederobien.mumble.server.interfaces.ISoundModifier;
import fr.pederobien.utils.event.EventManager;

public class SoundManager {
	public static final String DEFAULT_SOUND_MODIFIER_NAME = "default";
	private static Map<String, ISoundModifier> sounds;

	static {
		sounds = new LinkedHashMap<String, ISoundModifier>();
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
		EventManager.callEvent(new MumbleSoundModifierRegisterPreEvent(soundModifier), add, new MumbleSoundModifierRegisterPostEvent(soundModifier));
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

		MumbleSoundModifierUnregisterPreEvent preEvent = new MumbleSoundModifierUnregisterPreEvent(soundModifier);
		EventManager.callEvent(preEvent);
		if (preEvent.isCancelled())
			return false;

		boolean registered = sounds.remove(soundModifier.getName()) != null;
		if (registered)
			EventManager.callEvent(new MumbleSoundModifierUnregisterPostEvent(soundModifier));
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
	 * @return A copy of the underlying list.
	 */
	public static List<ISoundModifier> toList() {
		return new ArrayList<ISoundModifier>(sounds.values());
	}

	/**
	 * @return a sequential {@code Stream} over the elements in this collection.
	 */
	public static Stream<ISoundModifier> toStream() {
		return toList().stream();
	}

	private static class DefaultSoundModifier extends SoundModifier {

		public DefaultSoundModifier() {
			super(DEFAULT_SOUND_MODIFIER_NAME);
		}

		@Override
		public ISoundModifier clone() {
			return new DefaultSoundModifier();
		}
	}
}
