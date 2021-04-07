package fr.pederobien.mumble.server.exceptions;

public class ChannelNotRegisteredException extends RuntimeException {
	private static final long serialVersionUID = 1L;
	private String name;

	public ChannelNotRegisteredException(String name) {
		super("The channel " + name + " is not registered");
		this.name = name;
	}

	/**
	 * @return The channel's name.
	 */
	public String getName() {
		return name;
	}
}
