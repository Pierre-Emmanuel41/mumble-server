package fr.pederobien.mumble.server.impl;

import java.net.InetSocketAddress;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicBoolean;

import fr.pederobien.communication.event.ConnectionLostEvent;
import fr.pederobien.communication.event.UnexpectedDataReceivedEvent;
import fr.pederobien.communication.interfaces.ITcpConnection;
import fr.pederobien.mumble.common.impl.Identifier;
import fr.pederobien.mumble.common.impl.MumbleErrorCode;
import fr.pederobien.mumble.common.interfaces.IMumbleMessage;
import fr.pederobien.mumble.server.event.MumbleChannelNameChangePostEvent;
import fr.pederobien.mumble.server.event.MumbleChannelSoundModifierChangePostEvent;
import fr.pederobien.mumble.server.event.MumbleClientDisconnectPostEvent;
import fr.pederobien.mumble.server.event.MumbleParameterMaxValueChangePostEvent;
import fr.pederobien.mumble.server.event.MumbleParameterMinValueChangePostEvent;
import fr.pederobien.mumble.server.event.MumbleParameterValueChangePostEvent;
import fr.pederobien.mumble.server.event.MumblePlayerAdminChangePostEvent;
import fr.pederobien.mumble.server.event.MumblePlayerGameAddressChangePostEvent;
import fr.pederobien.mumble.server.event.MumblePlayerKickPostEvent;
import fr.pederobien.mumble.server.event.MumblePlayerListPlayerAddPostEvent;
import fr.pederobien.mumble.server.event.MumblePlayerListPlayerRemovePostEvent;
import fr.pederobien.mumble.server.event.MumblePlayerNameChangePostEvent;
import fr.pederobien.mumble.server.event.MumblePlayerOnlineChangePostEvent;
import fr.pederobien.mumble.server.event.MumblePlayerPositionChangePostEvent;
import fr.pederobien.mumble.server.event.MumbleServerChannelAddPostEvent;
import fr.pederobien.mumble.server.event.MumbleServerChannelRemovePostEvent;
import fr.pederobien.mumble.server.event.MumbleServerClientJoinPostEvent;
import fr.pederobien.mumble.server.event.MumbleServerClientLeavePostEvent;
import fr.pederobien.mumble.server.event.MumbleServerClosePostEvent;
import fr.pederobien.mumble.server.interfaces.IMumbleServer;
import fr.pederobien.mumble.server.interfaces.IPlayer;
import fr.pederobien.utils.event.EventHandler;
import fr.pederobien.utils.event.EventManager;
import fr.pederobien.utils.event.EventPriority;
import fr.pederobien.utils.event.IEventListener;

public class PlayerMumbleClient extends AbstractMumbleConnection implements IEventListener {
	private IPlayer player;
	private UUID uuid;
	private AtomicBoolean isJoined, isRegistered;

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
		isRegistered = new AtomicBoolean(false);
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

		doIfPlayerJoined(() -> send(getServer().getRequestManager().onPlayerInfoChanged(getVersion(), getPlayer())));
	}

	/**
	 * @return Return true if the mumble client has joined the server.
	 */
	public boolean isJoined() {
		return isJoined.get();
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

		if (establishCommunicationProtocolVersion() && isRegistered.compareAndSet(false, true))
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

	/**
	 * Creates a message to send to the remote in order to check if the specified port is used.
	 * 
	 * @param gamePort The game port to check.
	 * 
	 * @return The message to send to the remote.
	 */
	public IMumbleMessage createCheckGamePortMessage(int gamePort) {
		return getServer().getRequestManager().onGamePortCheck(1.0f, gamePort);
	}

	@EventHandler(priority = EventPriority.HIGHEST)
	private void onChannelAdded(MumbleServerChannelAddPostEvent event) {
		if (!event.getServer().equals(getServer()))
			return;

		doIfPlayerJoined(() -> send(getServer().getRequestManager().onChannelAdd(getVersion(), event.getChannel())));
	}

	@EventHandler(priority = EventPriority.HIGHEST)
	private void onChannelRemove(MumbleServerChannelRemovePostEvent event) {
		if (!event.getServer().equals(getServer()))
			return;

		doIfPlayerJoined(() -> send(getServer().getRequestManager().onChannelRemove(getVersion(), event.getChannel())));
	}

	@EventHandler(priority = EventPriority.HIGHEST)
	private void onChannelNameChange(MumbleChannelNameChangePostEvent event) {
		if (!event.getChannel().getServer().equals(getServer()))
			return;

		doIfPlayerJoined(() -> send(getServer().getRequestManager().onChannelNameChange(getVersion(), event.getChannel(), event.getOldName())));
	}

	@EventHandler(priority = EventPriority.HIGHEST)
	private void onPlayerNameChange(MumblePlayerNameChangePostEvent event) {
		if (!event.getPlayer().getServer().equals(getServer()))
			return;

		doIfPlayerJoined(() -> send(getServer().getRequestManager().onPlayerNameChange(getVersion(), event.getOldName(), event.getPlayer().getName())));
	}

	@EventHandler(priority = EventPriority.HIGHEST)
	private void onPlayerOnlineChange(MumblePlayerOnlineChangePostEvent event) {
		if (!event.getPlayer().getServer().equals(getServer()) || !event.getPlayer().equals(player))
			return;

		doIfPlayerJoined(() -> send(getServer().getRequestManager().onPlayerOnlineChange(getVersion(), event.getPlayer())));
	}

	@EventHandler(priority = EventPriority.HIGHEST)
	private void onPlayerGameAddressChange(MumblePlayerGameAddressChangePostEvent event) {
		if (!event.getPlayer().getServer().equals(getServer()) || !event.getPlayer().equals(player))
			return;

		send(getServer().getRequestManager().onPlayerGameAddressChange(getVersion(), event.getPlayer()));
	}

	@EventHandler(priority = EventPriority.HIGHEST)
	private void onPlayerAdminChange(MumblePlayerAdminChangePostEvent event) {
		if (!event.getPlayer().getServer().equals(getServer()) || !event.getPlayer().equals(player))
			return;

		doIfPlayerJoined(() -> send(getServer().getRequestManager().onPlayerAdminChange(getVersion(), event.getPlayer())));
	}

	@EventHandler(priority = EventPriority.HIGHEST)
	private void onPlayerKick(MumblePlayerKickPostEvent event) {
		if (!event.getPlayer().getServer().equals(getServer()))
			return;

		doIfPlayerJoined(() -> send(getServer().getRequestManager().onPlayerKick(getVersion(), event.getPlayer(), event.getKickingPlayer())));
	}

	@EventHandler(priority = EventPriority.HIGHEST)
	private void onPlayerPositionChange(MumblePlayerPositionChangePostEvent event) {
		if (!event.getPlayer().getServer().equals(getServer()) || !event.getPlayer().equals(player))
			return;

		doIfPlayerJoined(() -> send(getServer().getRequestManager().onPlayerPositionChange(getVersion(), event.getPlayer())));
	}

	@EventHandler(priority = EventPriority.HIGHEST)
	private void onChannelPlayerAdd(MumblePlayerListPlayerAddPostEvent event) {
		if (!event.getPlayer().getServer().equals(getServer()))
			return;

		boolean isMute = event.getPlayer().isMuteBy(player);
		doIfPlayerJoined(() -> send(getServer().getRequestManager().onChannelPlayerAdd(getVersion(), event.getList().getChannel(), event.getPlayer(), isMute)));
	}

	@EventHandler(priority = EventPriority.HIGHEST)
	private void onChannelPlayerRemove(MumblePlayerListPlayerRemovePostEvent event) {
		if (!event.getPlayer().getServer().equals(getServer()))
			return;

		doIfPlayerJoined(() -> send(getServer().getRequestManager().onChannelPlayerRemove(getVersion(), event.getList().getChannel(), event.getPlayer())));
	}

	@EventHandler(priority = EventPriority.HIGHEST)
	private void onParameterValueChange(MumbleParameterValueChangePostEvent event) {
		if (!getServer().getChannels().toList().contains(event.getParameter().getSoundModifier().getChannel()))
			return;

		doIfPlayerJoined(() -> send(getServer().getRequestManager().onParameterValueChange(getVersion(), event.getParameter())));
	}

	@EventHandler(priority = EventPriority.HIGHEST)
	private void onParameterMinValueChange(MumbleParameterMinValueChangePostEvent event) {
		if (!getServer().getChannels().toList().contains(event.getParameter().getSoundModifier().getChannel()))
			return;

		doIfPlayerJoined(() -> send(getServer().getRequestManager().onParameterMinValueChange(getVersion(), event.getParameter())));
	}

	@EventHandler(priority = EventPriority.HIGHEST)
	private void onParameterMaxValueChange(MumbleParameterMaxValueChangePostEvent event) {
		if (!getServer().getChannels().toList().contains(event.getParameter().getSoundModifier().getChannel()))
			return;

		doIfPlayerJoined(() -> send(getServer().getRequestManager().onParameterMaxValueChange(getVersion(), event.getParameter())));
	}

	@EventHandler(priority = EventPriority.HIGHEST)
	private void onChannelSoundModifierChanged(MumbleChannelSoundModifierChangePostEvent event) {
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
		if (request.getHeader().getIdentifier() == Identifier.SET_SERVER_JOIN) {
			if (!isJoined.compareAndSet(false, true))
				send(MumbleServerMessageFactory.answer(request, MumbleErrorCode.SERVER_ALREADY_JOINED));
			else {
				EventManager.callEvent(new MumbleServerClientJoinPostEvent(getServer(), this));
				send(MumbleServerMessageFactory.answer(request));
			}
			return;
		}

		// Always allow this request whatever the client state.
		if (request.getHeader().getIdentifier() == Identifier.SET_SERVER_LEAVE) {
			if (player != null && player.getChannel() != null)
				player.getChannel().getPlayers().remove(player);

			isJoined.set(false);
			EventManager.callEvent(new MumbleServerClientLeavePostEvent(getServer(), this));
			send(MumbleServerMessageFactory.answer(request));
			return;
		}

		if (checkPermission(request))
			send(getServer().getRequestManager().answer(new RequestReceivedHolder(request, this)));
		else
			send(MumbleServerMessageFactory.answer(request, MumbleErrorCode.PERMISSION_REFUSED));
	}

	@EventHandler
	private void OnConnectionLostEvent(ConnectionLostEvent event) {
		if (!event.getConnection().equals(getTcpConnection()))
			return;

		if (player != null && player.getChannel() != null)
			player.getChannel().getPlayers().remove(player);

		isJoined.set(false);
		getTcpConnection().dispose();
		EventManager.callEvent(new MumbleClientDisconnectPostEvent(this));
	}

	@EventHandler
	private void onServerClosing(MumbleServerClosePostEvent event) {
		getTcpConnection().dispose();
		EventManager.unregisterListener(this);
	}

	private boolean checkPermission(IMumbleMessage request) {
		// No need to check the permission for this identifier.
		if (request.getHeader().getIdentifier() == Identifier.SET_GAME_PORT_USED)
			return true;

		if (!isJoined.get())
			return false;

		switch (request.getHeader().getIdentifier()) {
		case GET_FULL_SERVER_CONFIGURATION:
			return false;
		case GET_SERVER_CONFIGURATION:
			return true;
		case REGISTER_PLAYER_ON_SERVER:
		case UNREGISTER_PLAYER_FROM_SERVER:
			return false;
		case GET_PLAYER_ONLINE_STATUS:
			return true;
		case SET_PLAYER_NAME:
			return false;
		case GET_PLAYER_ADMINISTRATOR:
			return true;
		case SET_PLAYER_ADMINISTRATOR:
			return player != null && player.isOnline() && player.isAdmin();
		case GET_PLAYER_MUTE:
		case SET_PLAYER_MUTE:
			return true;
		case SET_PLAYER_MUTE_BY:
			return true;
		case GET_PLAYER_DEAFEN:
		case SET_PLAYER_DEAFEN:
			return true;
		case KICK_PLAYER_FROM_CHANNEL:
			return player != null && player.isOnline() && player.isAdmin();
		case GET_PLAYER_POSITION:
		case SET_PLAYER_POSITION:
			return true;
		case GET_CHANNELS_INFO:
		case GET_CHANNEL_INFO:
			return true;
		case REGISTER_CHANNEL_ON_THE_SERVER:
		case UNREGISTER_CHANNEL_FROM_SERVER:
		case SET_CHANNEL_NAME:
			return player != null && player.isOnline() && player.isAdmin();
		case ADD_PLAYER_TO_CHANNEL:
		case REMOVE_PLAYER_FROM_CHANNEL:
			return player != null && player.isOnline();
		case GET_PARAMETER_VALUE:
			return true;
		case SET_PARAMETER_VALUE:
			return player != null && player.isOnline() && player.isAdmin();
		case GET_PARAMETER_MIN_VALUE:
			return true;
		case SET_PARAMETER_MIN_VALUE:
			return player != null && player.isOnline() && player.isAdmin();
		case GET_PARAMETER_MAX_VALUE:
			return true;
		case SET_PARAMETER_MAX_VALUE:
			return player != null && player.isOnline() && player.isAdmin();
		case GET_SOUND_MODIFIERS_INFO:
		case GET_CHANNEL_SOUND_MODIFIER_INFO:
			return true;
		case IS_GAME_PORT_USED:
			return false;
		case SET_GAME_PORT_USED:
			return true;
		default:
			return player != null && player.isOnline() && player.isAdmin();
		}
	}

	private void doIfPlayerJoined(Runnable runnable) {
		if (isJoined.get())
			runnable.run();
	}
}
