package fr.pederobien.mumble.server.impl;

import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;
import java.util.function.Consumer;

import fr.pederobien.communication.ResponseCallbackArgs;
import fr.pederobien.communication.event.ConnectionLostEvent;
import fr.pederobien.communication.event.UnexpectedDataReceivedEvent;
import fr.pederobien.communication.interfaces.ITcpConnection;
import fr.pederobien.mumble.common.impl.ErrorCode;
import fr.pederobien.mumble.common.impl.Idc;
import fr.pederobien.mumble.common.impl.MumbleCallbackMessage;
import fr.pederobien.mumble.common.impl.messages.v10.CommunicationProtocolGetMessageV10;
import fr.pederobien.mumble.common.impl.messages.v10.CommunicationProtocolSetMessageV10;
import fr.pederobien.mumble.common.interfaces.IMumbleMessage;
import fr.pederobien.mumble.server.event.ChannelNameChangePostEvent;
import fr.pederobien.mumble.server.event.ChannelSoundModifierChangePostEvent;
import fr.pederobien.mumble.server.event.ParameterMaxValueChangePostEvent;
import fr.pederobien.mumble.server.event.ParameterMinValueChangePostEvent;
import fr.pederobien.mumble.server.event.ParameterValueChangePostEvent;
import fr.pederobien.mumble.server.event.PlayerAdminChangePostEvent;
import fr.pederobien.mumble.server.event.PlayerDeafenChangePostEvent;
import fr.pederobien.mumble.server.event.PlayerGameAddressChangePostEvent;
import fr.pederobien.mumble.server.event.PlayerKickPostEvent;
import fr.pederobien.mumble.server.event.PlayerListPlayerAddPostEvent;
import fr.pederobien.mumble.server.event.PlayerListPlayerRemovePostEvent;
import fr.pederobien.mumble.server.event.PlayerMuteByChangePostEvent;
import fr.pederobien.mumble.server.event.PlayerMuteChangePostEvent;
import fr.pederobien.mumble.server.event.PlayerNameChangePostEvent;
import fr.pederobien.mumble.server.event.PlayerOnlineChangePostEvent;
import fr.pederobien.mumble.server.event.PlayerPositionChangePostEvent;
import fr.pederobien.mumble.server.event.ServerChannelAddPostEvent;
import fr.pederobien.mumble.server.event.ServerChannelRemovePostEvent;
import fr.pederobien.mumble.server.event.ServerPlayerAddPostEvent;
import fr.pederobien.mumble.server.event.ServerPlayerRemovePostEvent;
import fr.pederobien.utils.event.EventHandler;
import fr.pederobien.utils.event.EventManager;
import fr.pederobien.utils.event.EventPriority;
import fr.pederobien.utils.event.IEventListener;
import fr.pederobien.utils.event.LogEvent;

public class StandaloneMumbleClient implements IEventListener {
	private float version;
	private ITcpConnection tcpConnection;
	private StandaloneMumbleServer server;

	/**
	 * Creates a client associated to the external game server.
	 * 
	 * @param server        The server associated to this client.
	 * @param tcpConnection The connection with the external game server in order to send/receive data.
	 */
	protected StandaloneMumbleClient(StandaloneMumbleServer internalServer, ITcpConnection tcpConnection) {
		this.server = internalServer;
		this.tcpConnection = tcpConnection;

		establishCommunicationProtocolVersion();
	}

	@EventHandler(priority = EventPriority.HIGHEST)
	private void onChannelAdded(ServerChannelAddPostEvent event) {
		if (!event.getServer().equals(server))
			return;

		send(server.getRequestManager().onChannelAdd(version, event.getChannel()));
	}

	@EventHandler(priority = EventPriority.HIGHEST)
	private void onChannelRemove(ServerChannelRemovePostEvent event) {
		if (!event.getServer().equals(server))
			return;

		send(server.getRequestManager().onChannelRemove(version, event.getChannel()));
	}

	@EventHandler(priority = EventPriority.HIGHEST)
	private void onChannelNameChange(ChannelNameChangePostEvent event) {
		if (!event.getChannel().getServer().equals(server))
			return;

		send(server.getRequestManager().onChannelNameChange(version, event.getChannel(), event.getOldName()));
	}

	@EventHandler(priority = EventPriority.HIGHEST)
	private void onServerPlayerAdd(ServerPlayerAddPostEvent event) {
		if (!event.getList().getServer().equals(server))
			return;

		send(server.getRequestManager().onServerPlayerAdd(version, event.getPlayer()));
	}

	@EventHandler(priority = EventPriority.HIGHEST)
	private void onServerPlayerRemove(ServerPlayerRemovePostEvent event) {
		if (!event.getList().getServer().equals(server))
			return;

		send(server.getRequestManager().onServerPlayerRemove(version, event.getPlayer().getName()));
	}

	@EventHandler(priority = EventPriority.HIGHEST)
	private void onPlayerNameChange(PlayerNameChangePostEvent event) {
		if (!server.getPlayers().toList().contains(event.getPlayer()))
			return;

		send(server.getRequestManager().onPlayerNameChange(version, event.getOldName(), event.getPlayer().getName()));
	}

	@EventHandler(priority = EventPriority.HIGHEST)
	private void onPlayerOnlineChange(PlayerOnlineChangePostEvent event) {
		if (!server.getPlayers().toList().contains(event.getPlayer()))
			return;

		send(server.getRequestManager().onPlayerOnlineChange(version, event.getPlayer()));
	}

	@EventHandler(priority = EventPriority.HIGHEST)
	private void onPlayerGameAddressChange(PlayerGameAddressChangePostEvent event) {
		if (!server.getPlayers().toList().contains(event.getPlayer()))
			return;

		send(server.getRequestManager().onPlayerGameAddressChange(version, event.getPlayer()));
	}

	@EventHandler(priority = EventPriority.HIGHEST)
	private void onPlayerAdminChange(PlayerAdminChangePostEvent event) {
		if (!server.getPlayers().toList().contains(event.getPlayer()))
			return;

		send(server.getRequestManager().onPlayerAdminChange(version, event.getPlayer()));
	}

	@EventHandler(priority = EventPriority.HIGHEST)
	private void onPlayerMuteChange(PlayerMuteChangePostEvent event) {
		if (!server.getPlayers().toList().contains(event.getPlayer()))
			return;

		send(server.getRequestManager().onPlayerMuteChange(version, event.getPlayer()));
	}

	@EventHandler(priority = EventPriority.HIGHEST)
	private void onPlayerMuteByChange(PlayerMuteByChangePostEvent event) {
		if (!server.getPlayers().toList().contains(event.getPlayer()))
			return;

		send(server.getRequestManager().onPlayerMuteByChange(version, event.getPlayer(), event.getSource()));
	}

	@EventHandler(priority = EventPriority.HIGHEST)
	private void onPlayerDeafenChange(PlayerDeafenChangePostEvent event) {
		if (!server.getPlayers().toList().contains(event.getPlayer()))
			return;

		send(server.getRequestManager().onPlayerDeafenChange(version, event.getPlayer()));
	}

	@EventHandler(priority = EventPriority.HIGHEST)
	private void onPlayerKick(PlayerKickPostEvent event) {
		if (!server.getPlayers().toList().contains(event.getPlayer()))
			return;

		send(server.getRequestManager().onPlayerKick(version, event.getPlayer(), event.getKickingPlayer()));
	}

	@EventHandler(priority = EventPriority.HIGHEST)
	private void onPlayerPositionChange(PlayerPositionChangePostEvent event) {
		if (!server.getPlayers().toList().contains(event.getPlayer()))
			return;

		send(server.getRequestManager().onPlayerPositionChange(version, event.getPlayer()));
	}

	@EventHandler(priority = EventPriority.HIGHEST)
	private void onChannelPlayerAdd(PlayerListPlayerAddPostEvent event) {
		if (!server.getPlayers().toList().contains(event.getPlayer()))
			return;

		send(server.getRequestManager().onChannelPlayerAdd(version, event.getList().getChannel(), event.getPlayer()));
	}

	@EventHandler(priority = EventPriority.HIGHEST)
	private void onChannelPlayerRemove(PlayerListPlayerRemovePostEvent event) {
		if (!server.getPlayers().toList().contains(event.getPlayer()))
			return;

		send(server.getRequestManager().onChannelPlayerRemove(version, event.getList().getChannel(), event.getPlayer()));
	}

	@EventHandler(priority = EventPriority.HIGHEST)
	private void onParameterValueChange(ParameterValueChangePostEvent event) {
		if (!server.getChannels().toList().contains(event.getParameter().getSoundModifier().getChannel()))
			return;

		send(server.getRequestManager().onParameterValueChange(version, event.getParameter()));
	}

	@EventHandler(priority = EventPriority.HIGHEST)
	private void onParameterMinValueChange(ParameterMinValueChangePostEvent event) {
		if (!server.getChannels().toList().contains(event.getParameter().getSoundModifier().getChannel()))
			return;

		send(server.getRequestManager().onParameterMinValueChange(version, event.getParameter()));
	}

	@EventHandler(priority = EventPriority.HIGHEST)
	private void onParameterMaxValueChange(ParameterMaxValueChangePostEvent event) {
		if (!server.getChannels().toList().contains(event.getParameter().getSoundModifier().getChannel()))
			return;

		send(server.getRequestManager().onParameterMaxValueChange(version, event.getParameter()));
	}

	@EventHandler(priority = EventPriority.HIGHEST)
	private void onChannelSoundModifierChanged(ChannelSoundModifierChangePostEvent event) {
		if (!server.getChannels().toList().contains(event.getChannel()))
			return;

		send(server.getRequestManager().onChannelSoundModifierChange(version, event.getChannel()));
	}

	@EventHandler
	private void onUnexpectedDataReceived(UnexpectedDataReceivedEvent event) {
		if (!event.getConnection().equals(tcpConnection))
			return;

		IMumbleMessage request = MumbleServerMessageFactory.parse(event.getAnswer());
		if (version != -1 && version != request.getHeader().getVersion()) {
			String format = "Receiving message with unexpected version of the communication protocol, expected=v%s, actual=v%s";
			EventManager.callEvent(new LogEvent(format, version, request.getHeader().getVersion()));
		} else {
			if (request.getHeader().getIdc() != Idc.UNKNOWN)
				send(server.getRequestManager().answer(request));
			else
				send(MumbleServerMessageFactory.answer(request, ErrorCode.PERMISSION_REFUSED));
		}
	}

	@EventHandler
	private void onConnectionLost(ConnectionLostEvent event) {
		if (!event.getConnection().equals(tcpConnection))
			return;

		tcpConnection.dispose();
		EventManager.unregisterListener(this);
	}

	private void establishCommunicationProtocolVersion() {
		Lock lock = new ReentrantLock(true);
		Condition received = lock.newCondition();

		version = -1;
		getCommunicationProtocolVersion(lock, received);

		lock.lock();
		try {
			if (!received.await(5000, TimeUnit.MILLISECONDS) || version == -1)
				tcpConnection.dispose();
			else
				EventManager.registerListener(this);
		} catch (InterruptedException e) {
			// Do nothing
		} finally {
			lock.unlock();
		}
	}

	private void getCommunicationProtocolVersion(Lock lock, Condition received) {
		// Step 1: Asking the latest version of the communication protocol supported by the remote
		send(server.getRequestManager().getCommunicationProtocolVersion(), args -> {
			if (args.isTimeout())
				// No need to wait more
				exit(lock, received);
			else {
				CommunicationProtocolGetMessageV10 message = (CommunicationProtocolGetMessageV10) MumbleServerMessageFactory.parse(args.getResponse().getBytes());
				setCommunicationProtocolVersion(lock, received, findHighestVersion(message.getVersions()));
			}
		});
	}

	private void setCommunicationProtocolVersion(Lock lock, Condition received, float version) {
		// Step 2: Setting a specific version of the communication protocol to use for the client-server communication.
		send(server.getRequestManager().setCommunicationProtocolVersion(version), args -> {
			if (!args.isTimeout()) {
				CommunicationProtocolSetMessageV10 message = (CommunicationProtocolSetMessageV10) MumbleServerMessageFactory.parse(args.getResponse().getBytes());
				if (message.getVersion() == version)
					this.version = version;
			}

			exit(lock, received);
		});
	}

	private void exit(Lock lock, Condition received) {
		lock.lock();
		try {
			received.signal();
		} finally {
			lock.unlock();
		}
	}

	private float findHighestVersion(float[] versions) {
		float version = -1;
		for (int i = versions.length - 1; 0 < i; i--) {
			if (server.getRequestManager().isSupported(versions[i])) {
				version = versions[i];
				break;
			}
		}

		return version == -1 ? 1.0f : version;
	}

	private void send(IMumbleMessage message) {
		send(message, null);
	}

	private void send(IMumbleMessage message, Consumer<ResponseCallbackArgs> callback) {
		if (tcpConnection.isDisposed())
			return;

		tcpConnection.send(new MumbleCallbackMessage(message, callback));
	}
}
