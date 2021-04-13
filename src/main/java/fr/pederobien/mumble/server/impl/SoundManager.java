package fr.pederobien.mumble.server.impl;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import fr.pederobien.mumble.server.interfaces.ISoundModifier;

public class SoundManager {
	private static Map<String, ISoundModifier> sounds;

	static {
		sounds = new HashMap<String, ISoundModifier>();
		sounds.put(AbstractSoundModifier.DEFAULT.getName(), AbstractSoundModifier.DEFAULT);
	}

	private SoundManager() {
	}

	public static void add(ISoundModifier soundModifier) {
		sounds.put(soundModifier.getName(), soundModifier);
	}

	public static boolean remove(ISoundModifier soundModifier) {
		return sounds.remove(soundModifier.getName()) != null;
	}

	public static Optional<ISoundModifier> getByName(String name) {
		return Optional.ofNullable(sounds.get(name));
	}

	public static Map<String, ISoundModifier> getSoundModifiers() {
		return Collections.unmodifiableMap(sounds);
	}
}
