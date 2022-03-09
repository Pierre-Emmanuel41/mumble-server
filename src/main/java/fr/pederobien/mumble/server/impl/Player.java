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
	private String name;
	private UUID uuid;
	private InetSocketAddress gameAddress;
	private IPosition position;
	private IChannel channel;
	private boolean isAdmin, isOnline, isMute, isDeafen;
	private Map<IPlayer, Boolean> muteBy;
	private Object lockMuteBy;

	/**
	 * Creates a player specified by a name, a vocal address and an administrator status.
	 * 
	 * @param name        The player's name.
	 * @param gameAddress The player's address to play to the game.
	 * @param isAdmin     The player's administrator status.
	 * @param x           The player's x coordinate.
	 * @param x           The player's y coordinate.
	 * @param x           The player's z coordinate.
	 * @param x           The player's yaw angle.
	 * @param x           The player's pitch angle.
	 */
	protected Player(String name, InetSocketAddress gameAddress, boolean isAdmin, double x, double y, double z, double yaw, double pitch) {
		this.name = name;
		this.gameAddress = gameAddress;
		this.isAdmin = isAdmin;

		position = new Position(this, x, y, z, yaw, pitch);
		isOnline = false;
		muteBy = new HashMap<IPlayer, Boolean>();
		lockMuteBy = new Object();
	}

	@Override
	public String getName() {
		return name;
	}

	@Override
	public InetSocketAddress getGameAddress() {
		return gameAddress;
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
	}

	@Override
	public boolean isOnline() {
		return isOnline;
	}

	@Override
	public UUID getUUID() {
		return uuid;
	}

	@Override
	public IChannel getChannel() {
		return channel;
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
	}

	@Override
	public String toString() {
		return String.format("Player={%s}", name);
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
	 * Set the address used by this player in order to play to the game.
	 * 
	 * @param gameAddress The address
	 */
	public void setGameAddress(InetSocketAddress gameAddress) {
		this.gameAddress = gameAddress;
	}

	/**
	 * Set the identifier of this player.
	 * 
	 * @param uuid The player unique identifier.
	 */
	public void setUUID(UUID uuid) {
		this.uuid = uuid;
	}

	/**
	 * Set the channel associated to this player.
	 * 
	 * @param channel The new channel player.
	 */
	public void setChannel(IChannel channel) {
		this.channel = channel;
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
			EventManager.callEvent(new PlayerMuteByChangeEvent(this, player, isMute));
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
