package fr.pederobien.mumble.server.impl;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import fr.pederobien.mumble.server.interfaces.ISoundManager;
import fr.pederobien.mumble.server.interfaces.ISoundModifier;

public class SoundManager implements ISoundManager {
	private Map<String, ISoundModifier> sounds;

	public SoundManager() {
		sounds = new HashMap<String, ISoundModifier>();
	}

	@Override
	public void add(ISoundModifier soundModifier) {
		sounds.put(soundModifier.getName(), soundModifier);
	}

	@Override
	public boolean remove(ISoundModifier soundModifier) {
		return sounds.remove(soundModifier.getName()) != null;
	}

	@Override
	public Optional<ISoundModifier> getByName(String name) {
		return Optional.ofNullable(sounds.get(name));
	}

	@Override
	public Map<String, ISoundModifier> getSoundModifiers() {
		return Collections.unmodifiableMap(sounds);
	}
}
