package fr.pederobien.mumble.server.interfaces;

import fr.pederobien.mumble.server.event.MumblePlayerPositionChangePostEvent;

public interface IPosition {

	/**
	 * @return The player associated to this position.
	 */
	IPlayer getPlayer();

	/**
	 * @return The x position of the player to which this position is associated.
	 */
	double getX();

	/**
	 * @return The y position of the player to which this position is associated.
	 */
	double getY();

	/**
	 * @return The z position of the player to which this position is associated.
	 */
	double getZ();

	/**
	 * @return The yaw of the player to which this position is associated.
	 */
	double getYaw();

	/**
	 * @return The pitch of the player to which this position is associated.
	 */
	double getPitch();

	/**
	 * Update the coordinates associated to this position. Rise an {@link MumblePlayerPositionChangePostEvent}.
	 * 
	 * @param x     The new x position.
	 * @param y     The new y position.
	 * @param z     The new z position.
	 * @param yaw   The new yaw value.
	 * @param pitch The new pitch value.
	 */
	void update(double x, double y, double z, double yaw, double pitch);
}
