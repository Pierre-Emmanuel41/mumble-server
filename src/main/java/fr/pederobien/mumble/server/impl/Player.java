package fr.pederobien.mumble.server.impl;

import java.net.InetSocketAddress;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import fr.pederobien.mumble.server.event.PlayerAdminChangePostEvent;
import fr.pederobien.mumble.server.event.PlayerAdminChangePreEvent;
import fr.pederobien.mumble.server.event.PlayerDeafenChangePostEvent;
import fr.pederobien.mumble.server.event.PlayerDeafenChangePreEvent;
import fr.pederobien.mumble.server.event.PlayerGameAddressChangePostEvent;
import fr.pederobien.mumble.server.event.PlayerGameAddressChangePreEvent;
import fr.pederobien.mumble.server.event.PlayerMuteByChangeEvent;
import fr.pederobien.mumble.server.event.PlayerMuteByChangePostEvent;
import fr.pederobien.mumble.server.event.PlayerMuteByChangePreEvent;
import fr.pederobien.mumble.server.event.PlayerMuteChangePostEvent;
import fr.pederobien.mumble.server.event.PlayerMuteChangePreEvent;
import fr.pederobien.mumble.server.event.PlayerNameChangePostEvent;
import fr.pederobien.mumble.server.event.PlayerNameChangePreEvent;
import fr.pederobien.mumble.server.event.PlayerOnlineChangePostEvent;
import fr.pederobien.mumble.server.event.PlayerOnlineChangePreEvent;
import fr.pederobien.mumble.server.exceptions.PlayerNotRegisteredInChannelException;
import fr.pederobien.mumble.server.interfaces.IChannel;
import fr.pederobien.mumble.server.interfaces.IMumbleServer;
import fr.pederobien.mumble.server.interfaces.IPlayer;
import fr.pederobien.mumble.server.interfaces.IPosition;
import fr.pederobien.utils.event.EventManager;

public class Player implements IPlayer {
	private IMumbleServer server;
	private String name;
	private UUID uuid;
	private InetSocketAddress gameAddress;
	private IPosition position;
	private IChannel channel;
	private boolean isAdmin, isOnline, isMute, isDeafen;
	private Map<IPlayer, Boolean> muteBy;
	private Lock lock;

	/**
	 * Creates a player specified by a name, a vocal address and an administrator status.
	 * 
	 * @param server      The server on which this player is registered.
	 * @param name        The player's name.
	 * @param gameAddress The player's address to play to the game.
	 * @param isAdmin     The player's administrator status.
	 * @param x           The player's x coordinate.
	 * @param x           The player's y coordinate.
	 * @param x           The player's z coordinate.
	 * @param x           The player's yaw angle.
	 * @param x           The player's pitch angle.
	 */
	protected Player(IMumbleServer server, String name, InetSocketAddress gameAddress, boolean isAdmin, double x, double y, double z, double yaw, double pitch) {
		this.server = server;
		this.name = name;
		this.gameAddress = gameAddress;
		this.isAdmin = isAdmin;

		isOnline = true;
		position = new Position(this, x, y, z, yaw, pitch);
		muteBy = new HashMap<IPlayer, Boolean>();
		lock = new ReentrantLock(true);
	}

	@Override
	public IMumbleServer getServer() {
		return server;
	}

	@Override
	public String getName() {
		return name;
	}

	@Override
	public void setName(String name) {
		if (this.name.equals(name))
			return;

		String oldName = this.name;
		EventManager.callEvent(new PlayerNameChangePreEvent(this, name), () -> this.name = name, new PlayerNameChangePostEvent(this, oldName));
	}

	@Override
	public InetSocketAddress getGameAddress() {
		return gameAddress;
	}

	@Override
	public void setGameAddress(InetSocketAddress gameAddress) {
		if (this.gameAddress.equals(gameAddress))
			return;

		InetSocketAddress oldGameAddress = this.gameAddress;
		Runnable update = () -> this.gameAddress = gameAddress;
		EventManager.callEvent(new PlayerGameAddressChangePreEvent(this, gameAddress), update, new PlayerGameAddressChangePostEvent(this, oldGameAddress));
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

		boolean oldAdmin = this.isAdmin;
		Runnable update = () -> this.isAdmin = isAdmin;
		EventManager.callEvent(new PlayerAdminChangePreEvent(this, isAdmin), update, new PlayerAdminChangePostEvent(this, oldAdmin));
	}

	@Override
	public boolean isOnline() {
		return isOnline;
	}

	@Override
	public void setOnline(boolean isOnline) {
		if (this.isOnline == isOnline)
			return;

		boolean oldOnline = this.isOnline;
		Runnable update = () -> this.isOnline = isOnline;
		EventManager.callEvent(new PlayerOnlineChangePreEvent(this, isOnline), update, new PlayerOnlineChangePostEvent(this, oldOnline));
	}

	@Override
	public UUID getIdentifier() {
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
		if (this.isMute == isMute)
			return;

		checkChannel();
		boolean oldMute = this.isMute;
		Runnable update = () -> this.isMute = isMute;
		EventManager.callEvent(new PlayerMuteChangePreEvent(this, isMute), update, new PlayerMuteChangePostEvent(this, oldMute));
	}

	@Override
	public boolean isMuteBy(IPlayer player) {
		Boolean isMute = muteBy.get(player);
		return isMute == null ? false : isMute;
	}

	@Override
	public void setMuteBy(IPlayer player, boolean isMute) {
		if (!getServer().getPlayers().toList().contains(player))
			throw new IllegalArgumentException("The player must be registered on the server");

		Boolean status = muteBy.get(player);
		boolean oldMute = status == null ? false : status;
		if (oldMute == isMute)
			return;

		Runnable update = () -> muteBy.put(player, isMute);
		EventManager.callEvent(new PlayerMuteByChangePreEvent(this, player, isMute), update, new PlayerMuteByChangePostEvent(this, player, oldMute));
	}

	@Override
	public boolean isDeafen() {
		return isDeafen;
	}

	@Override
	public void setDeafen(boolean isDeafen) {
		if (this.isDeafen == isDeafen)
			return;

		checkChannel();
		boolean oldDeafen = this.isDeafen;
		Runnable update = () -> this.isDeafen = isDeafen;
		EventManager.callEvent(new PlayerDeafenChangePreEvent(this, isDeafen), update, new PlayerDeafenChangePostEvent(this, oldDeafen));
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
		return getIdentifier().equals(other.getIdentifier());
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
	 * Set if this player is muted by another player. True in order to mute it, false in order to unmute it.
	 * 
	 * @param player The player that mute this player.
	 * @param isMute True if the player is mute, false if the player is unmute.
	 */
	public void setIsMuteBy(IPlayer player, boolean isMute) {
		lock.lock();
		try {
			muteBy.put(player, isMute);
		} finally {
			lock.unlock();
		}

		EventManager.callEvent(new PlayerMuteByChangeEvent(this, player, isMute));
	}

	private void checkChannel() {
		if (channel == null)
			throw new PlayerNotRegisteredInChannelException(this);
	}
}
