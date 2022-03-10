package fr.pederobien.mumble.server.external;

import java.util.ArrayList;
import java.util.List;

import fr.pederobien.communication.event.ConnectionLostEvent;
import fr.pederobien.communication.event.UnexpectedDataReceivedEvent;
import fr.pederobien.communication.interfaces.ITcpConnection;
import fr.pederobien.mumble.common.impl.ErrorCode;
import fr.pederobien.mumble.common.impl.Idc;
import fr.pederobien.mumble.common.impl.MumbleCallbackMessage;
import fr.pederobien.mumble.common.impl.Oid;
import fr.pederobien.mumble.common.interfaces.IMumbleMessage;
import fr.pederobien.mumble.server.event.ChannelNameChangePostEvent;
import fr.pederobien.mumble.server.event.ChannelSoundModifierChangePostEvent;
import fr.pederobien.mumble.server.event.PlayerListPlayerAddPostEvent;
import fr.pederobien.mumble.server.event.PlayerListPlayerRemovePostEvent;
import fr.pederobien.mumble.server.event.PlayerNameChangePostEvent;
import fr.pederobien.mumble.server.event.PlayerOnlineChangePostEvent;
import fr.pederobien.mumble.server.event.ServerChannelAddPostEvent;
import fr.pederobien.mumble.server.event.ServerChannelRemovePostEvent;
import fr.pederobien.mumble.server.event.ServerPlayerAddPostEvent;
import fr.pederobien.mumble.server.event.ServerPlayerRemovePostEvent;
import fr.pederobien.mumble.server.impl.InternalServer;
import fr.pederobien.mumble.server.impl.MumbleServerMessageFactory;
import fr.pederobien.mumble.server.interfaces.IParameter;
import fr.pederobien.utils.event.EventHandler;
import fr.pederobien.utils.event.EventManager;
import fr.pederobien.utils.event.IEventListener;

public class MumbleGameServerClient implements IEventListener {
	private ITcpConnection tcpConnection;
	private InternalServer internalServer;

	public MumbleGameServerClient(InternalServer internalServer, ITcpConnection tcpConnection) {
		this.internalServer = internalServer;
		this.tcpConnection = tcpConnection;

		EventManager.registerListener(this);
	}

	@EventHandler
	private void onChannelAdded(ServerChannelAddPostEvent event) {
		if (!event.getServer().equals(internalServer))
			return;

		List<Object> properties = new ArrayList<Object>();

		// Channel's name
		properties.add(event.getChannel().getName());

		// Sound modifier's name
		properties.add(event.getChannel().getSoundModifier().getName());

		// Number of parameters
		properties.add(event.getChannel().getSoundModifier().getParameters().toList().size());

		for (IParameter<?> parameter : event.getChannel().getSoundModifier().getParameters()) {
			// Parameter's name
			properties.add(parameter.getName());

			// Parameter's type
			properties.add(parameter.getType());

			// Parameter's value
			properties.add(parameter.getValue());
		}

		send(MumbleServerMessageFactory.create(Idc.CHANNELS, Oid.ADD, properties.toArray()));
	}

	@EventHandler
	private void onChannelRemoved(ServerChannelRemovePostEvent event) {
		if (!event.getServer().equals(internalServer))
			return;

		send(MumbleServerMessageFactory.create(Idc.CHANNELS, Oid.REMOVE, event.getChannel().getName()));
	}

	@EventHandler
	private void onChannelRenamed(ChannelNameChangePostEvent event) {
		send(MumbleServerMessageFactory.create(Idc.CHANNELS, Oid.SET, event.getOldName(), event.getChannel().getName()));
	}

	@EventHandler
	private void onServerPlayerAdd(ServerPlayerAddPostEvent event) {
		List<Object> properties = new ArrayList<Object>();

		// Player's name
		properties.add(event.getPlayer().getName());

		// Player's game address
		properties.add(event.getPlayer().getGameAddress().getAddress().getHostAddress());

		// Player's game port
		properties.add(event.getPlayer().getGameAddress().getPort());

		// Player's identifier
		properties.add(event.getPlayer().getUUID());

		// Player's administrator status
		properties.add(event.getPlayer().isAdmin());

		// Player's mute status
		properties.add(event.getPlayer().isMute());

		// Player's deafen status
		properties.add(event.getPlayer().isDeafen());

		// Player's x coordinate
		properties.add(event.getPlayer().getPosition().getX());

		// Player's y coordinate
		properties.add(event.getPlayer().getPosition().getY());

		// Player's z coordinate
		properties.add(event.getPlayer().getPosition().getZ());

		// Player's yaw angle
		properties.add(event.getPlayer().getPosition().getYaw());

		// Player's pitch angle
		properties.add(event.getPlayer().getPosition().getPitch());

		send(MumbleServerMessageFactory.create(Idc.PLAYER, Oid.ADD, properties.toArray()));
	}

	@EventHandler
	private void onServerPlayerRemove(ServerPlayerRemovePostEvent event) {
		send(MumbleServerMessageFactory.create(Idc.PLAYER, Oid.REMOVE, event.getPlayer().getName()));
	}

	@EventHandler
	private void onPlayerNameChange(PlayerNameChangePostEvent event) {
		send(MumbleServerMessageFactory.create(Idc.PLAYER_NAME, Oid.SET, event.getOldName(), event.getPlayer().getName()));
	}

	@EventHandler
	private void onPlayerOnlineChange(PlayerOnlineChangePostEvent event) {
		send(MumbleServerMessageFactory.create(Idc.PLAYER_ONLINE, Oid.SET, event.getPlayer().getName(), event.getPlayer().isOnline()));
	}

	@EventHandler
	private void onPlayerAdded(PlayerListPlayerAddPostEvent event) {
		send(MumbleServerMessageFactory.create(Idc.CHANNELS_PLAYER, Oid.ADD, event.getList().getName(), event.getPlayer().getName()));
	}

	@EventHandler
	private void onPlayerRemoved(PlayerListPlayerRemovePostEvent event) {
		send(MumbleServerMessageFactory.create(Idc.CHANNELS_PLAYER, Oid.REMOVE, event.getList().getName(), event.getPlayer().getName()));
	}

	@EventHandler
	private void onSoundModifierChanged(ChannelSoundModifierChangePostEvent event) {
		send(MumbleServerMessageFactory.create(Idc.SOUND_MODIFIER, Oid.SET, event.getChannel().getName(), event.getChannel().getSoundModifier().getName()));
	}

	@EventHandler
	private void onUnexpectedDataReceived(UnexpectedDataReceivedEvent event) {
		if (!event.getConnection().equals(tcpConnection))
			return;

		IMumbleMessage request = MumbleServerMessageFactory.parse(event.getAnswer());

		if (checkPermission(request))
			send(internalServer.getRequestManager().answer(request));
		else
			send(MumbleServerMessageFactory.answer(request, ErrorCode.PERMISSION_REFUSED));
	}

	@EventHandler
	private void onConnectionLost(ConnectionLostEvent event) {
		if (!event.getConnection().equals(tcpConnection))
			return;

		tcpConnection.dispose();
		EventManager.unregisterListener(this);
	}

	private void send(IMumbleMessage message) {
		if (tcpConnection.isDisposed())
			return;

		tcpConnection.send(new MumbleCallbackMessage(message, null));
	}

	private boolean checkPermission(IMumbleMessage request) {
		switch (request.getHeader().getIdc()) {
		case SERVER_INFO:
		case CHANNELS:
		case PLAYER:
		case PLAYER_NAME:
		case PLAYER_ONLINE:
		case PLAYER_MUTE:
		case PLAYER_DEAFEN:
		case SOUND_MODIFIER:
		case PLAYER_KICK:
		case PLAYER_POSITION:
			return true;
		default:
			return false;
		}
	}
}
