package fr.pederobien.mumble.server.impl;

import java.text.DecimalFormat;
import java.util.Objects;

import fr.pederobien.mumble.server.event.PlayerPositionChangeEvent;
import fr.pederobien.mumble.server.interfaces.IPlayer;
import fr.pederobien.mumble.server.interfaces.IPosition;
import fr.pederobien.utils.event.EventManager;

public class Position implements IPosition {
	private static final DecimalFormat FORMAT = new DecimalFormat("#.####");
	private IPlayer player;
	private double x, y, z, yaw, pitch;

	public Position(IPlayer player, double x, double y, double z, double yaw, double pitch) {
		this.player = Objects.requireNonNull(player, "The player cannot be null");
		update(x, y, z, yaw, pitch);
	}

	public Position(IPlayer player) {
		this(player, 0, 0, 0, 0, 0);
	}

	@Override
	public IPlayer getPlayer() {
		return player;
	}

	@Override
	public double getX() {
		return x;
	}

	@Override
	public double getY() {
		return y;
	}

	@Override
	public double getZ() {
		return z;
	}

	@Override
	public double getYaw() {
		return yaw;
	}

	@Override
	public double getPitch() {
		return pitch;
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

	@Override
	public void update(double x, double y, double z, double yaw, double pitch) {
		this.x = x;
		this.y = y;
		this.z = z;
		this.yaw = yaw;
		this.pitch = pitch;
		EventManager.callEvent(new PlayerPositionChangeEvent(this));
	}

	private String format(double number) {
		return FORMAT.format(number);
	}
}
