package fr.pederobien.mumble.server.impl;

import java.text.DecimalFormat;

import fr.pederobien.mumble.server.interfaces.IPosition;

public class Position implements IPosition {
	private static final DecimalFormat FORMAT = new DecimalFormat("#.####");
	private double x, y, z, yaw, pitch;

	public Position(double x, double y, double z, double yaw, double pitch) {
		this.x = x;
		this.y = y;
		this.z = z;
		this.yaw = yaw;
		this.pitch = pitch;
	}

	public Position() {
		this(0, 0, 0, 0, 0);
	}

	@Override
	public double getX() {
		return x;
	}

	@Override
	public void setX(double x) {
		this.x = x;
	}

	@Override
	public double getY() {
		return y;
	}

	@Override
	public void setY(double y) {
		this.y = y;
	}

	@Override
	public double getZ() {
		return z;
	}

	@Override
	public void setZ(double z) {
		this.z = z;
	}

	@Override
	public double getYaw() {
		return yaw;
	}

	@Override
	public void setYaw(double yaw) {
		this.yaw = yaw;
	}

	@Override
	public double getPitch() {
		return pitch;
	}

	@Override
	public void setPitch(double pitch) {
		this.pitch = pitch;
	}

	@Override
	public String toString() {
		return "Position={" + format(x) + "," + format(y) + "," + format(z) + "," + format(yaw) + "," + format(pitch) + "}";
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;

		if (!(obj instanceof IPosition))
			return false;

		IPosition other = (IPosition) obj;
		return toString().equals(other.toString());
	}

	private String format(double number) {
		return FORMAT.format(number);
	}
}
