package fr.pederobien.mumble.server.exceptions;

public class SoundModifierDoesNotExistException extends RuntimeException {
	private static final long serialVersionUID = 1L;
	private String name;

	public SoundModifierDoesNotExistException(String name) {
		super("There are no sound modifier registered for " + name);
		this.name = name;
	}

	/**
	 * @return The sound modifier name.
	 */
	public String getName() {
		return name;
	}
}
