package fr.pederobien.mumble.server.interfaces;

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
	 * Set the x position of the player to which this position is associated.
	 * 
	 * @param x the new x position.
	 */
	void setX(double x);

	/**
	 * @return The y position of the player to which this position is associated.
	 */
	double getY();

	/**
	 * Set the y position of the player to which this position is associated.
	 * 
	 * @param y the new y position.
	 */
	void setY(double y);

	/**
	 * @return The z position of the player to which this position is associated.
	 */
	double getZ();

	/**
	 * Set the z position of the player to which this position is associated.
	 * 
	 * @param z the new z position.
	 */
	void setZ(double z);

	/**
	 * @return The yaw of the player to which this position is associated.
	 */
	double getYaw();

	/**
	 * Set the yaw of the player to which this position is associated.
	 * 
	 * @param yaw the new yaw position.
	 */
	void setYaw(double yaw);

	/**
	 * @return The pitch of the player to which this position is associated.
	 */
	double getPitch();

	/**
	 * Set the pitch of the player to which this position is associated.
	 * 
	 * @param pitch the new pitch position.
	 */
	void setPitch(double pitch);
}
