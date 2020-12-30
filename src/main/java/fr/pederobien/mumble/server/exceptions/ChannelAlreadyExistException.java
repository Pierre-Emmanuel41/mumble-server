package fr.pederobien.mumble.server.exceptions;

public class ChannelAlreadyExistException extends RuntimeException {
	private static final long serialVersionUID = 1L;
	private String name;

	public ChannelAlreadyExistException(String name) {
		super("The channel " + name + " already exists");
		this.name = name;
	}

	/**
	 * @return The channel's name.
	 */
	public String getName() {
		return name;
	}
}
