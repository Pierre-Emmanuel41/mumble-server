package fr.pederobien.mumble.server.impl;

import java.net.InetSocketAddress;
import java.util.UUID;
import java.util.function.Predicate;
import java.util.stream.Stream;

import fr.pederobien.mumble.server.event.MumblePlayerAdminChangePostEvent;
import fr.pederobien.mumble.server.event.MumblePlayerAdminChangePreEvent;
import fr.pederobien.mumble.server.event.MumblePlayerGameAddressChangePostEvent;
import fr.pederobien.mumble.server.event.MumblePlayerGameAddressChangePreEvent;
import fr.pederobien.mumble.server.event.MumblePlayerKickPostEvent;
import fr.pederobien.mumble.server.event.MumblePlayerKickPreEvent;
import fr.pederobien.mumble.server.event.MumblePlayerListPlayerAddPostEvent;
import fr.pederobien.mumble.server.event.MumblePlayerListPlayerRemovePostEvent;
import fr.pederobien.mumble.server.event.MumblePlayerNameChangePostEvent;
import fr.pederobien.mumble.server.event.MumblePlayerNameChangePreEvent;
import fr.pederobien.mumble.server.event.MumblePlayerOnlineChangePostEvent;
import fr.pederobien.mumble.server.event.MumblePlayerOnlineChangePreEvent;
import fr.pederobien.mumble.server.event.MumbleServerPlayerRemovePostEvent;
import fr.pederobien.mumble.server.exceptions.PlayerNotAdministratorException;
import fr.pederobien.mumble.server.exceptions.PlayerNotRegisteredInChannelException;
import fr.pederobien.mumble.server.interfaces.IChannel;
import fr.pederobien.mumble.server.interfaces.IMumbleServer;
import fr.pederobien.mumble.server.interfaces.IPlayer;
import fr.pederobien.mumble.server.interfaces.IPosition;
import fr.pederobien.utils.event.EventHandler;
import fr.pederobien.utils.event.EventManager;
import fr.pederobien.utils.event.EventPriority;
import fr.pederobien.utils.event.IEventListener;
import fr.pederobien.vocal.server.event.VocalServerPlayerAddPostEvent;
import fr.pederobien.vocal.server.event.VocalServerPlayerRemovePostEvent;
import fr.pederobien.vocal.server.interfaces.IVocalPlayer;

public class Player implements IPlayer, IEventListener {
	private IMumbleServer server;
	private String name;
	private UUID uuid;
	private InetSocketAddress gameAddress;
	private IPosition position;
	private IChannel channel;
	private boolean isAdmin, isOnline;
	private IVocalPlayer vocalPlayer;

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

		EventManager.registerListener(this);
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
		Runnable update = () -> {
			this.name = name;
			if (vocalPlayer != null)
				vocalPlayer.setName(name);
		};
		EventManager.callEvent(new MumblePlayerNameChangePreEvent(this, name), update, new MumblePlayerNameChangePostEvent(this, oldName));
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
		EventManager.callEvent(new MumblePlayerGameAddressChangePreEvent(this, gameAddress), update, new MumblePlayerGameAddressChangePostEvent(this, oldGameAddress));
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
		EventManager.callEvent(new MumblePlayerAdminChangePreEvent(this, isAdmin), update, new MumblePlayerAdminChangePostEvent(this, oldAdmin));
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
		EventManager.callEvent(new MumblePlayerOnlineChangePreEvent(this, isOnline), update, new MumblePlayerOnlineChangePostEvent(this, oldOnline));
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
		return vocalPlayer == null ? false : vocalPlayer.isMute();
	}

	@Override
	public void setMute(boolean isMute) {
		if (vocalPlayer == null)
			return;

		vocalPlayer.setMute(isMute);
	}

	@Override
	public boolean isMuteBy(IPlayer player) {
		if (!(player instanceof Player))
			return true;

		return vocalPlayer == null ? false : vocalPlayer.isMuteBy(((Player) player).getVocalPlayer());
	}

	@Override
	public void setMuteBy(IPlayer player, boolean isMute) {
		if (!getServer().getPlayers().toList().contains(player))
			throw new IllegalArgumentException("The player must be registered on the server");

		if (vocalPlayer == null)
			return;

		vocalPlayer.setMuteBy(((Player) player).getVocalPlayer(), isMute);
	}

	@Override
	public Stream<IPlayer> getMuteByPlayers() {
		if (vocalPlayer == null)
			return Stream.empty();

		Predicate<IVocalPlayer> filterVocalPlayer = player -> getServer().getPlayers().get(player.getName()).isPresent();
		return vocalPlayer.getMuteByPlayers().filter(filterVocalPlayer).map(player -> getServer().getPlayers().get(player.getName()).get());
	}

	@Override
	public boolean isDeafen() {
		return vocalPlayer == null ? false : vocalPlayer.isDeafen();
	}

	@Override
	public void setDeafen(boolean isDeafen) {
		if (vocalPlayer == null)
			return;

		vocalPlayer.setDeafen(isDeafen);
	}

	@Override
	public void kick(IPlayer kickingPlayer) {
		if (!kickingPlayer.isAdmin())
			throw new PlayerNotAdministratorException(kickingPlayer);

		if (channel == null)
			throw new PlayerNotRegisteredInChannelException(this);

		Runnable update = () -> {
			IChannel oldChannel = channel;
			EventManager.callEvent(new MumblePlayerKickPostEvent(this, oldChannel, kickingPlayer));
		};
		EventManager.callEvent(new MumblePlayerKickPreEvent(this, channel, kickingPlayer), update);
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
	 * @return The vocal player associated to this mumble player.
	 */
	public IVocalPlayer getVocalPlayer() {
		return vocalPlayer;
	}

	@EventHandler(priority = EventPriority.LOWEST)
	private void onChannelsPlayerAdd(MumblePlayerListPlayerAddPostEvent event) {
		if (!event.getPlayer().equals(this))
			return;

		channel = event.getList().getChannel();
	}

	@EventHandler(priority = EventPriority.LOWEST)
	private void onChannelsPlayerRemove(MumblePlayerListPlayerRemovePostEvent event) {
		if (!event.getPlayer().equals(this))
			return;

		channel = null;
	}

	@EventHandler(priority = EventPriority.LOWEST)
	private void onPlayerKick(MumblePlayerKickPostEvent event) {
		if (!event.getPlayer().equals(this))
			return;

		channel = null;
	}

	@EventHandler(priority = EventPriority.LOWEST)
	private void onServerPlayerRemove(MumbleServerPlayerRemovePostEvent event) {
		if (!event.getPlayer().equals(this))
			return;

		if (isOnline) {
			this.isOnline = false;
			EventManager.callEvent(new MumblePlayerOnlineChangePostEvent(this, true));
		}

		EventManager.unregisterListener(this);
	}

	@EventHandler
	private void onServerVocalPlayerAdd(VocalServerPlayerAddPostEvent event) {
		if (!event.getPlayer().getName().equals(getName()))
			return;

		vocalPlayer = event.getPlayer();
	}

	@EventHandler
	private void onServerVocalPlayerRemove(VocalServerPlayerRemovePostEvent event) {
		if (!event.getPlayer().getName().equals(getName()))
			return;

		vocalPlayer = null;
	}
}
