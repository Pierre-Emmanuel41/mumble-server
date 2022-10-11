package fr.pederobien.mumble.server.impl;

import fr.pederobien.mumble.server.impl.modifiers.Axis;
import fr.pederobien.mumble.server.impl.modifiers.Plan;
import fr.pederobien.mumble.server.interfaces.IPosition;

public class MathHelper {
	private static final int PRECISION = 8;
	private static final double[] COS_VALUES = new double[90 * PRECISION + 1];

	static {
		// Precomputing cosinus value for calculation optimization.
		int index = 0;
		for (double i = 0; i <= 90; i += 1.0 / (double) PRECISION) {
			COS_VALUES[index++] = Math.cos(Math.toRadians(i));
			if (i == 90)
				COS_VALUES[COS_VALUES.length - 1] = 0;
		}
	}

	/**
	 * Calculate the yaw between the first position "from" and the other position "to".
	 * 
	 * @param from The position used to know its yaw with the other position.
	 * @param to   The reference position.
	 * 
	 * @return The yaw in radian between the two positions.
	 */
	public static double getYaw(IPosition from, IPosition to) {
		return getYaw(from, to, from.getYaw());
	}

	/**
	 * Calculate the yaw between the second position "to" and the first position "from".
	 * 
	 * @param from The reference position.
	 * @param to   The position used to know its yaw with the other position.
	 * 
	 * @return The yaw in radian between the two positions.
	 */
	public static double getInverseYaw(IPosition from, IPosition to) {
		return getYaw(from, to, to.getYaw());
	}

	/**
	 * Calculate the left and right volume for a stereo signal according to the given positions.
	 * 
	 * @param from The position used to know its yaw with the other position.
	 * @param to   The reference position.
	 * 
	 * @return A double array with size 2. The first value correspond to the left volume, the second to the right volume.
	 */
	public static double[] getDefaultLeftAndRightVolume(IPosition from, IPosition to) {
		double yaw = getInverseYaw(from, to);
		// First getting the yaw between 0 and PI/2
		// Second getting the angle in degrees in order to get the precomputed cosinus value.
		int index = (int) Math.toDegrees(Math.abs(inRange(yaw, -Math.PI / 2, Math.PI / 2))) * PRECISION;

		double leftVolume = 1.0, rightVolume = 1.0;
		if (0 <= yaw && yaw < Math.PI) {
			leftVolume = Math.abs(COS_VALUES[index]);
			rightVolume += 1 - leftVolume;
		} else {
			rightVolume = Math.abs(COS_VALUES[index]);
			leftVolume += 1 - rightVolume;
		}
		return new double[] { leftVolume, rightVolume };
	}

	/**
	 * Calculate the distance in one dimension between the two positions.
	 * 
	 * @param from The position used to now it distance with the other position.
	 * @param to   The reference position.
	 * @param axis The axis on which the calcul is executed.
	 * 
	 * @return The distance between the two positions.
	 */
	public static double getDistance1D(IPosition from, IPosition to, Axis axis) {
		switch (axis) {
		case X:
			return to.getX() - from.getX();
		case Y:
			return to.getY() - from.getY();
		default:
			return to.getZ() - from.getZ();
		}
	}

	/**
	 * Calculate the distance in two dimensions between the two positions.
	 * 
	 * @param from The position used to now it distance with the other position.
	 * @param to   The reference position.
	 * @param plan The plan on which the calcul is executed.
	 * 
	 * @return The distance between the two positions.
	 */
	public static double getDistance2D(IPosition from, IPosition to, Plan plan) {
		switch (plan) {
		case XY:
			return Math.sqrt(Math.pow(getDistance1D(from, to, Axis.X), 2) + Math.pow(getDistance1D(from, to, Axis.Y), 2));
		case XZ:
			return Math.sqrt(Math.pow(getDistance1D(from, to, Axis.X), 2) + Math.pow(getDistance1D(from, to, Axis.Z), 2));
		default:
			return Math.sqrt(Math.pow(getDistance1D(from, to, Axis.Y), 2) + Math.pow(getDistance1D(from, to, Axis.Z), 2));
		}
	}

	/**
	 * Calculate the distance in three dimensions between the two positions.
	 * 
	 * @param from The position used to now it distance with the other position.
	 * @param to   The reference position.
	 * 
	 * @return The distance between the two positions.
	 */
	public static double getDistance3D(IPosition from, IPosition to) {
		return Math.sqrt(Math.pow(getDistance1D(from, to, Axis.X), 2) + Math.pow(getDistance1D(from, to, Axis.Y), 2) + Math.pow(getDistance1D(from, to, Axis.Z), 2));
	}

	/**
	 * Check the value of the given angle in order to put it in range -<i>pi</i> and <i>pi</i>.
	 * 
	 * @param angle the angle in radians to check.
	 * 
	 * @return The same angle but in the right range.
	 */
	public static double inRange(double angle) {
		return inRange(angle, -Math.PI, Math.PI);
	}

	/**
	 * Check the value of the given angle in order to put it in given range. The range must be centered in 0.
	 * 
	 * @param angle the angle in radians to check.
	 * 
	 * @return The same angle but in the right range.
	 */
	private static double inRange(double angle, double infRange, double supRange) {
		return angle > supRange ? angle - (supRange - infRange) : angle < infRange ? angle + (supRange - infRange) : angle;
	}

	private static double getYaw(IPosition from, IPosition to, double yawPlayer) {
		return inRange(Math.atan2(getDistance1D(from, to, Axis.Y), getDistance1D(from, to, Axis.X)) - yawPlayer);
	}
}
