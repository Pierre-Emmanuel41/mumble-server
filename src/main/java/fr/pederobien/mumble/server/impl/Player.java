package fr.pederobien.mumble.server.impl;

import java.net.InetSocketAddress;
import java.util.UUID;

import fr.pederobien.mumble.server.interfaces.IPlayer;
import fr.pederobien.mumble.server.interfaces.IPosition;

public class Player implements IPlayer {
	private InetSocketAddress address;
	private String name;
	private IPosition position;
	private Client client;
	private boolean isAdmin, isOnline;

	protected Player(InetSocketAddress address, String name, boolean isAdmin) {
		this.address = address;
		this.name = name;
		this.isAdmin = isAdmin;

		position = new Position();
		isOnline = false;
	}

	@Override
	public String getName() {
		return name;
	}

	@Override
	public InetSocketAddress getIp() {
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
	public boolean isOnline() {
		return isOnline;
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

	public void onOtherPlayerSpeaker(String playerName, byte[] data) {
		client.onOtherPlayerSpeak(playerName, data);
	}

	public void setClient(Client client) {
		this.client = client;
	}

	public void setIsOnline(boolean isOnline) {
		if (this.isOnline == isOnline)
			return;
		this.isOnline = isOnline;
		client.sendPlayerStatusChanged(isOnline);
	}
}
