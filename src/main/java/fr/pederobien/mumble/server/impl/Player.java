package fr.pederobien.mumble.server.impl;

import java.net.InetSocketAddress;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import fr.pederobien.mumble.server.event.PlayerAdminStatusChangeEvent;
import fr.pederobien.mumble.server.event.PlayerDeafenChangeEvent;
import fr.pederobien.mumble.server.event.PlayerMuteByChangeEvent;
import fr.pederobien.mumble.server.event.PlayerMuteChangeEvent;
import fr.pederobien.mumble.server.event.PlayerOnlineChangeEvent;
import fr.pederobien.mumble.server.exceptions.PlayerNotRegisteredInChannelException;
import fr.pederobien.mumble.server.interfaces.IChannel;
import fr.pederobien.mumble.server.interfaces.IPlayer;
import fr.pederobien.mumble.server.interfaces.IPosition;
import fr.pederobien.utils.event.EventManager;

public class Player implements IPlayer {
	private InternalServer internalServer;
	private InetSocketAddress address;
	private String name;
	private IPosition position;
	private IChannel channel;
	private Client client;
	private boolean isAdmin, isOnline, isMute, isDeafen;
	private Map<IPlayer, Boolean> muteBy;
	private Object lockMuteBy;

	protected Player(InternalServer internalServer, InetSocketAddress address, String name, boolean isAdmin) {
		this.internalServer = internalServer;
		this.address = address;
		this.name = name;
		this.isAdmin = isAdmin;

		position = new Position(this);
		isOnline = false;
		muteBy = new HashMap<IPlayer, Boolean>();
		lockMuteBy = new Object();
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
		EventManager.callEvent(new PlayerAdminStatusChangeEvent(this, isAdmin));
		client.sendAdminChanged(isAdmin);
	}

	@Override
	public boolean isOnline() {
		return isOnline;
	}

	@Override
	public UUID getUUID() {
		return client.getUUID();
	}

	@Override
	public IChannel getChannel() {
		return channel;
	}

	public void setChannel(IChannel channel) {
		this.channel = channel;
	}

	@Override
	public boolean isMute() {
		return isMute;
	}

	@Override
	public void setMute(boolean isMute) {
		checkChannel();
		this.isMute = isMute;
		EventManager.callEvent(new PlayerMuteChangeEvent(this, isMute));
		internalServer.onPlayerMuteChanged(getName(), isMute);
	}

	@Override
	public boolean isDeafen() {
		return isDeafen;
	}

	@Override
	public void setDeafen(boolean isDeafen) {
		checkChannel();
		this.isDeafen = isDeafen;
		EventManager.callEvent(new PlayerDeafenChangeEvent(this, isDeafen));
		internalServer.onPlayerDeafenChanged(getName(), isDeafen);
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

	/**
	 * Send the data to the client in order to play it on the client side.
	 * 
	 * @param playerName The speaking player.
	 * @param data       The byte array that correspond to what the player is saying.
	 * @param global     The global volume of the signal.
	 * @param left       The left channel volume of the signal.
	 * @param right      The right channel volume of the signal.
	 */
	public void onOtherPlayerSpeaker(String playerName, byte[] data, double global, double left, double right) {
		client.onOtherPlayerSpeak(playerName, data, global, left, right);
	}

	/**
	 * Set the client of this player. The client represent the client side associated to this player.
	 * 
	 * @param client The client of the player.
	 */
	public void setClient(Client client) {
		this.client = client;
	}

	/**
	 * Set if this player is online are not. If the status changed then data is sent through the client in order to update the client
	 * graphical user interface.
	 * 
	 * @param isOnline True if the player is online, false otherwise.
	 */
	public void setIsOnline(boolean isOnline) {
		if (this.isOnline == isOnline)
			return;

		this.isOnline = isOnline;
		EventManager.callEvent(new PlayerOnlineChangeEvent(this, isOnline));
		client.sendOnlineChanged(isOnline);
	}

	/**
	 * Set if this player is muted by another player. True in order to mute it, false in order to unmute it.
	 * 
	 * @param player The player that mute this player.
	 * @param isMute True if the player is mute, false if the player is unmute.
	 */
	public void setIsMuteBy(IPlayer player, boolean isMute) {
		synchronized (lockMuteBy) {
			muteBy.put(player, isMute);
			EventManager.callEvent(new PlayerMuteByChangeEvent(this, player));
		}
	}

	/**
	 * Get if this player is mute by the given player.
	 * 
	 * @param player The player for which this player is mute or unmute.
	 * @return True if this player is mute for the given player, false if this player is unmute for the given player.
	 */
	public boolean isMuteBy(IPlayer player) {
		Boolean isMute = muteBy.get(player);
		return isMute == null ? false : isMute;
	}

	private void checkChannel() {
		if (channel == null)
			throw new PlayerNotRegisteredInChannelException(this);
	}
}
