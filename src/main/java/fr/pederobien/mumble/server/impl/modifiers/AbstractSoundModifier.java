package fr.pederobien.mumble.server.impl.modifiers;

import fr.pederobien.mumble.server.interfaces.IPlayer;
import fr.pederobien.mumble.server.interfaces.IPosition;
import fr.pederobien.mumble.server.interfaces.ISoundModifier;

public abstract class AbstractSoundModifier implements ISoundModifier {
	public static final ISoundModifier DEFAULT = new DefaultSoundModifier();
	private static final double MATH_2PI = 2 * Math.PI;
	private String name;

	public AbstractSoundModifier(String name) {
		this.name = name;
	}

	@Override
	public String getName() {
		return name;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;

		if (obj == null || !(obj instanceof ISoundModifier))
			return false;

		ISoundModifier other = (ISoundModifier) obj;
		return getName().equals(other.getName());
	}

	/**
	 * Calculate the yaw between the first position "from" and the other position "to".
	 * 
	 * @param from The position used to know its yaw with the other position.
	 * @param to   The reference position.
	 * 
	 * @return The yaw in radian between the two positions.
	 */
	protected double getYaw(IPosition from, IPosition to) {
		double deltaX = getDistance1D(from, to, Axis.X);
		double deltaY = getDistance1D(from, to, Axis.Y);
		double theta = Math.atan(deltaY / deltaX);
		double yawP1 = to.getYaw();
		double yaw = theta - yawP1;
		return yaw < 0 ? yaw + MATH_2PI : yaw > MATH_2PI ? yaw - MATH_2PI : yaw;
	}

	/**
	 * Calculate the left and right colume for a stere signal according to the given positions.
	 * 
	 * @param from The position used to know its yaw with the other position.
	 * @param to   The reference position.
	 * 
	 * @return A double array with size 2. The first value correspond to the left volume, the second to the right volume.
	 */
	protected double[] getDefaultLeftAndRightVolume(IPosition from, IPosition to) {
		double yaw = getYaw(from, to);
		double leftVolume = 1.0, rightVolume = 1.0;
		if (0 <= yaw && yaw < Math.PI)
			leftVolume = Math.abs(Math.cos(yaw));
		else
			rightVolume = Math.abs(Math.cos(yaw));
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
	protected double getDistance1D(IPosition from, IPosition to, Axis axis) {
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
	protected double getDistance2D(IPosition from, IPosition to, Plan plan) {
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
	protected double getDistance3D(IPosition from, IPosition to) {
		return Math.sqrt(Math.pow(getDistance1D(from, to, Axis.X), 2) + Math.pow(getDistance1D(from, to, Axis.Y), 2) + Math.pow(getDistance1D(from, to, Axis.Z), 2));

	}

	private static class DefaultSoundModifier extends AbstractSoundModifier {

		public DefaultSoundModifier() {
			super("default");
		}

		@Override
		public VolumeResult calculate(IPlayer transmitter, IPlayer receiver) {
			return VolumeResult.DEFAULT;
		}
	}
}
