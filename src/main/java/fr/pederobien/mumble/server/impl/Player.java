package fr.pederobien.mumble.server.impl;

import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.util.UUID;

import fr.pederobien.mumble.server.interfaces.IPlayer;
import fr.pederobien.mumble.server.interfaces.IPosition;

public class Player implements IPlayer {
	private InetAddress address;
	private String name;
	private IPosition position;
	private Client client;
	private boolean isAdmin;

	protected Player(InetSocketAddress address, String name, boolean isAdmin) {
		this.address = address.getAddress();
		this.name = name;
		this.isAdmin = isAdmin;
		position = new Position();
	}

	@Override
	public String getName() {
		return name;
	}

	@Override
	public InetAddress getIp() {
		return address;
	}

	@Override
	public IPosition getPosition() {
		return position;
	}

	@Override
	public boolean isAdmin() {
		return isAdmin;
	}

	@Override
	public void setAdmin(boolean isAdmin) {
		if (this.isAdmin == isAdmin)
			return;

		this.isAdmin = isAdmin;
		client.sendAdminChanged(isAdmin);
	}

	@Override
	public String toString() {
		return "Player={" + address + "," + name + "}";
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;

		if (!(obj instanceof IPlayer))
			return false;

		IPlayer other = (IPlayer) obj;
		return getUUID().equals(other.getUUID());
	}

	@Override
	public UUID getUUID() {
		return client.getUUID();
	}

	public void setClient(Client client) {
		this.client = client;
		client.setPlayer(this);
		client.setAddress(address);
	}
}
