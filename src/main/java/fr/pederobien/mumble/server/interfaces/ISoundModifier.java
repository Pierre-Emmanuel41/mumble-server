package fr.pederobien.mumble.server.interfaces;

import fr.pederobien.vocal.common.impl.VolumeResult;

public interface ISoundModifier extends Cloneable {

	/**
	 * @return The name of this sound modifier.
	 */
	String getName();

	/**
	 * @return The list of parameters associated to this sound modifier.
	 */
	IParameterList getParameters();

	/**
	 * @return The channel associated to this sound modifier.
	 */
	IChannel getChannel();

	/**
	 * @return True if this sound modifier should send data to the transmitting player.
	 */
	boolean sendFeedback();

	/**
	 * Set if this sound modifier should send data to the transmitting player.
	 * 
	 * @param sendFeedback True if the transmitting player should receive its own data, false otherwise.
	 */
	void setSendFeedback(boolean sendFeedback);

	/**
	 * Calculate the left audio channel volume, the right audio channel volume and the signal global volume.
	 * 
	 * @param transmitter The player currently speaking.
	 * @param receiver    The player currently hearing.
	 * 
	 * @return The result.
	 */
	VolumeResult calculate(IPlayer transmitter, IPlayer receiver);

	/**
	 * Clone this sound modifier. It creates a new parameter based on the properties of this sound modifier.
	 * 
	 * @return A new sound modifier.
	 */
	ISoundModifier clone();
}
