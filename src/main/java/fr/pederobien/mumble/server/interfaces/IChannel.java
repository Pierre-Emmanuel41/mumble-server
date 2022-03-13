package fr.pederobien.mumble.server.interfaces;

public interface IChannel {

	/**
	 * @return The server to which this channel is attached.
	 */
	IMumbleServer getServer();

	/**
	 * @return The channel name.
	 */
	String getName();

	/**
	 * Set the name of this channel.
	 * 
	 * @param name The new channel name.
	 */
	void setName(String name);

	/**
	 * @return The list of players.
	 */
	IChannelPlayerList getPlayers();

	/**
	 * @return The sound modifier associated to this channel.
	 */
	ISoundModifier getSoundModifier();

	/**
	 * Set the sound modifier of this channel.
	 * 
	 * @param soundModifier The new volume modifier associated to this channel.
	 */
	void setSoundModifier(ISoundModifier soundModifier);
}
