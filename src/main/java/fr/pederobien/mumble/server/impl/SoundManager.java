package fr.pederobien.mumble.server.impl;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import fr.pederobien.mumble.server.impl.modifiers.AbstractSoundModifier;
import fr.pederobien.mumble.server.interfaces.IChannel;
import fr.pederobien.mumble.server.interfaces.ISoundModifier;

public class SoundManager {
	private static Map<String, ISoundModifier> sounds;
	private static Map<String, List<IChannel>> pendings;

	static {
		sounds = new HashMap<String, ISoundModifier>();
		sounds.put(AbstractSoundModifier.DEFAULT.getName(), AbstractSoundModifier.DEFAULT);
		pendings = new HashMap<String, List<IChannel>>();
	}

	private SoundManager() {
	}

	/**
	 * Adds the given sound modifier to the collection of modifiers managed by this manager.
	 * 
	 * @param soundModifier The sound modifier to add.
	 */
	public static void add(ISoundModifier soundModifier) {
		sounds.put(soundModifier.getName(), soundModifier);

		List<IChannel> channels = pendings.remove(soundModifier.getName());
		if (channels == null)
			return;

		for (IChannel channel : channels)
			channel.setSoundModifier(soundModifier);
	}

	/**
	 * Removes the given sound modifier from the collection of modifiers managed by this manager.
	 * 
	 * @param soundModifier The sound modifier to remove.
	 * 
	 * @return True if the modifier was in the list, false otherwise.
	 */
	public static boolean remove(ISoundModifier soundModifier) {
		return sounds.remove(soundModifier.getName()) != null;
	}

	/**
	 * Get the sound modifier associated to the given name.
	 * 
	 * @param name The sound modifier name.
	 * 
	 * @return An optional that contains the sound modifier if it exist, an empty optional otherwise.
	 */
	public static Optional<ISoundModifier> getByName(String name) {
		return Optional.ofNullable(sounds.get(name));
	}

	/**
	 * Set the sound modifier for the given channel. If the sound modifier associated to the given name does not exist then the
	 * channel is added to a pending queue in order to be reused when the modifier will be added.
	 * 
	 * @param channel The channel whose sound modifier must be modified.
	 * @param name    The sound modifier name.
	 */
	public static void setSoundModifier(IChannel channel, String name) {
		ISoundModifier modifier = sounds.get(name);
		if (modifier != null)
			channel.setSoundModifier(modifier);
		else {
			List<IChannel> channels = pendings.get(name);
			if (channels == null) {
				channels = new ArrayList<IChannel>();
				pendings.put(name, channels);
			}
			channels.add(channel);
		}
	}

	/**
	 * @return An unmodifiable map that contains all registered sound modifiers.
	 */
	public static Map<String, ISoundModifier> getSoundModifiers() {
		return Collections.unmodifiableMap(sounds);
	}
}
