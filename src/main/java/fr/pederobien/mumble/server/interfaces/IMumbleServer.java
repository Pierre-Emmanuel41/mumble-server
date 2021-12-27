package fr.pederobien.mumble.server.interfaces;

public interface IMumbleServer {

	/**
	 * @return The server name.
	 */
	String getName();

	/**
	 * Open this server.
	 */
	void open();

	/**
	 * Close this server.
	 */
	void close();

	/**
	 * @return If the server has been opened or the method {@link #close()} has not been called.
	 */
	boolean isOpened();

	/**
	 * Get a list that contains all players connected to the game. It does not means their mumble client is also connected.
	 * 
	 * @return A list that contains all registered players. This list is unmodifiable.
	 */
	IPlayerList getPlayers();

	/**
	 * @return The list of channels associated to this server.
	 */
	IChannelList getChannels();
}
