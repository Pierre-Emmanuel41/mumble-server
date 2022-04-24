package fr.pederobien.mumble.server.impl;

import java.net.InetSocketAddress;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicBoolean;

import fr.pederobien.communication.event.ConnectionLostEvent;
import fr.pederobien.communication.event.UnexpectedDataReceivedEvent;
import fr.pederobien.communication.interfaces.ITcpConnection;
import fr.pederobien.mumble.common.impl.ErrorCode;
import fr.pederobien.mumble.common.impl.Idc;
import fr.pederobien.mumble.common.interfaces.IMumbleMessage;
import fr.pederobien.mumble.server.event.ChannelNameChangePostEvent;
import fr.pederobien.mumble.server.event.ChannelSoundModifierChangePostEvent;
import fr.pederobien.mumble.server.event.ClientDisconnectPostEvent;
import fr.pederobien.mumble.server.event.ParameterMaxValueChangePostEvent;
import fr.pederobien.mumble.server.event.ParameterMinValueChangePostEvent;
import fr.pederobien.mumble.server.event.ParameterValueChangePostEvent;
import fr.pederobien.mumble.server.event.PlayerAdminChangePostEvent;
import fr.pederobien.mumble.server.event.PlayerDeafenChangePostEvent;
import fr.pederobien.mumble.server.event.PlayerListPlayerAddPostEvent;
import fr.pederobien.mumble.server.event.PlayerListPlayerRemovePostEvent;
import fr.pederobien.mumble.server.event.PlayerMuteChangePostEvent;
import fr.pederobien.mumble.server.event.PlayerNameChangePostEvent;
import fr.pederobien.mumble.server.event.PlayerOnlineChangePostEvent;
import fr.pederobien.mumble.server.event.PlayerPositionChangePostEvent;
import fr.pederobien.mumble.server.event.ServerChannelAddPostEvent;
import fr.pederobien.mumble.server.event.ServerChannelRemovePostEvent;
import fr.pederobien.mumble.server.event.ServerClientJoinPostEvent;
import fr.pederobien.mumble.server.event.ServerClientLeavePostEvent;
import fr.pederobien.mumble.server.event.ServerClosePostEvent;
import fr.pederobien.mumble.server.event.ServerPlayerAddPostEvent;
import fr.pederobien.mumble.server.event.ServerPlayerRemovePostEvent;
import fr.pederobien.mumble.server.interfaces.IMumbleServer;
import fr.pederobien.mumble.server.interfaces.IPlayer;
import fr.pederobien.utils.event.EventHandler;
import fr.pederobien.utils.event.EventManager;
import fr.pederobien.utils.event.EventPriority;
import fr.pederobien.utils.event.IEventListener;

public class PlayerMumbleClient extends AbstractMumbleConnection implements IEventListener {
	private IPlayer player;
	private UUID uuid;
	private AtomicBoolean isJoined;

	/**
	 * Creates a client associated to a specific player.
	 * 
	 * @param server The server associated to this client.
	 * @param uuid   The client unique identifier.
	 */
	protected PlayerMumbleClient(IMumbleServer server, UUID uuid) {
		super(server, null);
		this.uuid = uuid;

		isJoined = new AtomicBoolean(false);
	}

	/**
	 * @return The identifier of this client.
	 */
	public UUID getUUID() {
		return uuid;
	}

	/**
	 * @return The player associated to this client. Null if not connected in game.
	 */
	public IPlayer getPlayer() {
		return player;
	}

	/**
	 * Set the player associated to this client.
	 * 
	 * @param player The player of this client.
	 */
	public void setPlayer(Player player) {
		this.player = player;
		if (player != null)
			player.setUUID(getUUID());
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;

		if (!(obj instanceof PlayerMumbleClient))
			return false;

		PlayerMumbleClient other = (PlayerMumbleClient) obj;
		return uuid.equals(other.getUUID());
	}

	@Override
	protected void setTcpConnection(ITcpConnection connection) {
		super.setTcpConnection(connection);

		if (establishCommunicationProtocolVersion())
			EventManager.registerListener(this);
	}

	/**
	 * Check if the game address or the mumble address correspond to the port number. If the mumble client is registered then a
	 * request is sent to know if the the given port is used.
	 * 
	 * @param port The port number used to play at the game.
	 * 
	 * @return True if the game address or the mumble address correspond to the given address and port.
	 */
	public boolean isAssociatedTo(int port) {
		InetSocketAddress gameAddress = getGameAddress();
		InetSocketAddress mumbleAddress = getMumbleAddress();
		return gameAddress != null && gameAddress.getPort() == port || mumbleAddress != null && mumbleAddress.getPort() == port;
	}

	/**
	 * Check if the game address or the mumble address correspond to the given address and port number. If the mumble client is
	 * registered then a request is sent to know if the the given port is used.
	 * 
	 * @param port The port number used to play at the game.
	 * 
	 * @return True if the game address or the mumble address correspond to the given address and port.
	 */
	public boolean isAssociatedTo(String address) {
		String gameAddress = getGameAddress() == null ? null : getGameAddress().getAddress().getHostAddress();
		String mumbleAddress = getMumbleAddress() == null ? null : getMumbleAddress().getAddress().getHostAddress();
		return gameAddress != null && gameAddress.equals(address) || mumbleAddress != null && mumbleAddress.equals(address);
	}

	/**
	 * @return the address of the player used to play at the game. Null if the player is not connected in game.
	 */
	public InetSocketAddress getGameAddress() {
		return player != null && player.isOnline() ? player.getGameAddress() : null;
	}

	/**
	 * @return The address used by the player to speak to the other players. Null if there the player is not connected with mumble.
	 */
	public InetSocketAddress getMumbleAddress() {
		return getTcpConnection() == null ? null : getTcpConnection().getAddress();
	}

	@EventHandler(priority = EventPriority.HIGHEST)
	private void onChannelAdded(ServerChannelAddPostEvent event) {
		if (!event.getServer().equals(getServer()))
			return;

		doIfPlayerJoined(() -> send(getServer().getRequestManager().onChannelAdd(getVersion(), event.getChannel())));
	}

	@EventHandler(priority = EventPriority.HIGHEST)
	private void onChannelRemove(ServerChannelRemovePostEvent event) {
		if (!event.getServer().equals(getServer()))
			return;

		doIfPlayerJoined(() -> send(getServer().getRequestManager().onChannelRemove(getVersion(), event.getChannel())));
	}

	@EventHandler(priority = EventPriority.HIGHEST)
	private void onChannelNameChange(ChannelNameChangePostEvent event) {
		if (!event.getChannel().getServer().equals(getServer()))
			return;

		doIfPlayerJoined(() -> send(getServer().getRequestManager().onChannelNameChange(getVersion(), event.getChannel(), event.getOldName())));
	}

	@EventHandler(priority = EventPriority.HIGHEST)
	private void onServerPlayerAdd(ServerPlayerAddPostEvent event) {
		if (!event.getList().getServer().equals(getServer()))
			return;

		doIfPlayerJoined(() -> send(getServer().getRequestManager().onServerPlayerAdd(getVersion(), event.getPlayer())));
	}

	@EventHandler(priority = EventPriority.HIGHEST)
	private void onServerPlayerRemove(ServerPlayerRemovePostEvent event) {
		if (!event.getList().getServer().equals(getServer()))
			return;

		doIfPlayerJoined(() -> send(getServer().getRequestManager().onServerPlayerRemove(getVersion(), event.getPlayer().getName())));
	}

	@EventHandler(priority = EventPriority.HIGHEST)
	private void onPlayerNameChange(PlayerNameChangePostEvent event) {
		if (!getServer().getPlayers().toList().contains(event.getPlayer()))
			return;

		doIfPlayerJoined(() -> send(getServer().getRequestManager().onPlayerNameChange(getVersion(), event.getOldName(), event.getPlayer().getName())));
	}

	@EventHandler(priority = EventPriority.HIGHEST)
	private void onPlayerOnlineChange(PlayerOnlineChangePostEvent event) {
		if (!getServer().getPlayers().toList().contains(event.getPlayer()))
			return;

		doIfPlayerJoined(() -> send(getServer().getRequestManager().onPlayerOnlineChange(getVersion(), event.getPlayer())));
	}

	@EventHandler(priority = EventPriority.HIGHEST)
	private void onPlayerAdminChange(PlayerAdminChangePostEvent event) {
		if (!getServer().getPlayers().toList().contains(event.getPlayer()))
			return;

		doIfPlayerJoined(() -> send(getServer().getRequestManager().onPlayerAdminChange(getVersion(), event.getPlayer())));
	}

	@EventHandler(priority = EventPriority.HIGHEST)
	private void onPlayerMuteChange(PlayerMuteChangePostEvent event) {
		if (!getServer().getPlayers().toList().contains(event.getPlayer()))
			return;

		doIfPlayerJoined(() -> send(getServer().getRequestManager().onPlayerMuteChange(getVersion(), event.getPlayer())));
	}

	@EventHandler(priority = EventPriority.HIGHEST)
	private void onPlayerDeafenChange(PlayerDeafenChangePostEvent event) {
		if (!getServer().getPlayers().toList().contains(event.getPlayer()))
			return;

		doIfPlayerJoined(() -> send(getServer().getRequestManager().onPlayerDeafenChange(getVersion(), event.getPlayer())));
	}

	@EventHandler(priority = EventPriority.HIGHEST)
	private void onPlayerPositionChange(PlayerPositionChangePostEvent event) {
		if (!getServer().getPlayers().toList().contains(event.getPlayer()) && !event.getPlayer().equals(player))
			return;

		doIfPlayerJoined(() -> send(getServer().getRequestManager().onPlayerPositionChange(getVersion(), event.getPlayer())));
	}

	@EventHandler(priority = EventPriority.HIGHEST)
	private void onChannelPlayerAdd(PlayerListPlayerAddPostEvent event) {
		if (!getServer().getPlayers().toList().contains(event.getPlayer()))
			return;

		doIfPlayerJoined(() -> send(getServer().getRequestManager().onChannelPlayerAdd(getVersion(), event.getList().getChannel(), event.getPlayer())));
	}

	@EventHandler(priority = EventPriority.HIGHEST)
	private void onChannelPlayerRemove(PlayerListPlayerRemovePostEvent event) {
		if (!getServer().getPlayers().toList().contains(event.getPlayer()))
			return;

		doIfPlayerJoined(() -> send(getServer().getRequestManager().onChannelPlayerRemove(getVersion(), event.getList().getChannel(), event.getPlayer())));
	}

	@EventHandler(priority = EventPriority.HIGHEST)
	private void onParameterValueChange(ParameterValueChangePostEvent event) {
		if (!getServer().getChannels().toList().contains(event.getParameter().getSoundModifier().getChannel()))
			return;

		doIfPlayerJoined(() -> send(getServer().getRequestManager().onParameterValueChange(getVersion(), event.getParameter())));
	}

	@EventHandler(priority = EventPriority.HIGHEST)
	private void onParameterMinValueChange(ParameterMinValueChangePostEvent event) {
		if (!getServer().getChannels().toList().contains(event.getParameter().getSoundModifier().getChannel()))
			return;

		doIfPlayerJoined(() -> send(getServer().getRequestManager().onParameterMinValueChange(getVersion(), event.getParameter())));
	}

	@EventHandler(priority = EventPriority.HIGHEST)
	private void onParameterMaxValueChange(ParameterMaxValueChangePostEvent event) {
		if (!getServer().getChannels().toList().contains(event.getParameter().getSoundModifier().getChannel()))
			return;

		doIfPlayerJoined(() -> send(getServer().getRequestManager().onParameterMaxValueChange(getVersion(), event.getParameter())));
	}

	@EventHandler(priority = EventPriority.HIGHEST)
	private void onChannelSoundModifierChanged(ChannelSoundModifierChangePostEvent event) {
		if (!getServer().getChannels().toList().contains(event.getChannel()))
			return;

		doIfPlayerJoined(() -> send(getServer().getRequestManager().onChannelSoundModifierChange(getVersion(), event.getChannel())));
	}

	@EventHandler
	private void onUnexpectedDataReceived(UnexpectedDataReceivedEvent event) {
		IMumbleMessage request = checkReceivedRequest(event);
		if (request == null)
			return;

		// There is no need to answer to a server join request.
		if (request.getHeader().getIdc() == Idc.SERVER_JOIN) {
			if (!isJoined.compareAndSet(false, true))
				send(MumbleServerMessageFactory.answer(request, ErrorCode.SERVER_ALREADY_JOINED));
			else {
				EventManager.callEvent(new ServerClientJoinPostEvent(getServer(), this));
				send(MumbleServerMessageFactory.answer(request));
			}
			return;
		}

		// Always allow this request whatever the client state.
		if (request.getHeader().getIdc() == Idc.SERVER_LEAVE) {
			isJoined.set(false);
			EventManager.callEvent(new ServerClientLeavePostEvent(getServer(), this));
			send(MumbleServerMessageFactory.answer(request));
			return;
		}

		if (checkPermission(request))
			send(getServer().getRequestManager().answer(request));
		else
			send(MumbleServerMessageFactory.answer(request, ErrorCode.PERMISSION_REFUSED));
	}

	@EventHandler
	private void OnConnectionLostEvent(ConnectionLostEvent event) {
		if (!event.getConnection().equals(getTcpConnection()))
			return;

		if (player != null && player.getChannel() != null)
			player.getChannel().getPlayers().remove(player);

		getTcpConnection().dispose();
		EventManager.callEvent(new ClientDisconnectPostEvent(this));
	}

	@EventHandler
	private void onServerClosing(ServerClosePostEvent event) {
		getTcpConnection().dispose();
		EventManager.unregisterListener(this);
	}

	private boolean checkPermission(IMumbleMessage request) {
		// No need to check the permission for this Idc.
		if (request.getHeader().getIdc() == Idc.GAME_PORT)
			return true;

		if (!isJoined.get())
			return false;

		switch (request.getHeader().getIdc()) {
		case SERVER_INFO:
			switch (request.getHeader().getOid()) {
			case GET:
				return true;
			default:
				return false;
			}
		case PLAYER:
		case PLAYER_MUTE:
		case PLAYER_DEAFEN:
			return true;
		case CHANNELS:
			switch (request.getHeader().getOid()) {
			case GET:
				return true;
			case ADD:
			case REMOVE:
				return player != null && player.isAdmin();
			default:
				return false;
			}
		case CHANNELS_PLAYER:
			switch (request.getHeader().getOid()) {
			case ADD:
			case REMOVE:
				return player != null && player.isOnline();
			default:
				return false;
			}
		case SOUND_MODIFIER:
			switch (request.getHeader().getOid()) {
			case GET:
			case INFO:
				return true;
			case SET:
				return player != null && player.isAdmin();
			default:
				return false;
			}
		default:
			return player != null && player.isAdmin();
		}
	}

	private void doIfPlayerJoined(Runnable runnable) {
		if (isJoined.get())
			runnable.run();
	}
}
