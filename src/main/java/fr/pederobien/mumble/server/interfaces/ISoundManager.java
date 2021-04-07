package fr.pederobien.mumble.server.interfaces;

import java.util.Map;
import java.util.Optional;

public interface ISoundManager {

	/**
	 * Appends the given sound modifier to this manager.
	 * 
	 * @param soundModifier The sound modifier to add.
	 */
	void add(ISoundModifier soundModifier);

	/**
	 * Removes the given sound modifier from this manager.
	 * 
	 * @param soundModifier The sound modifier to remove.
	 * 
	 * @return True if the sound modifier was registered, false otherwise.
	 */
	boolean remove(ISoundModifier soundModifier);

	/**
	 * Try to get the sound modifier associated to the given name.
	 * 
	 * @param name The sound modifier name.
	 * 
	 * @return An optional that contains the sound modifier if it is registered, an empty optional otherwise.
	 */
	Optional<ISoundModifier> getByName(String name);

	/**
	 * @return An unmodifiable map that contains all registered sound modifiers.
	 */
	Map<String, ISoundModifier> getSoundModifiers();
}
