package fr.pederobien.mumble.server.external;

import fr.pederobien.communication.event.UnexpectedDataReceivedEvent;
import fr.pederobien.communication.interfaces.ITcpConnection;
import fr.pederobien.messenger.interfaces.IMessage;
import fr.pederobien.mumble.common.impl.ErrorCode;
import fr.pederobien.mumble.common.impl.Header;
import fr.pederobien.mumble.common.impl.Idc;
import fr.pederobien.mumble.common.impl.MumbleCallbackMessage;
import fr.pederobien.mumble.common.impl.MumbleMessageFactory;
import fr.pederobien.mumble.common.impl.Oid;
import fr.pederobien.mumble.server.event.ChannelNameChangePostEvent;
import fr.pederobien.mumble.server.event.ChannelPlayerAddPostEvent;
import fr.pederobien.mumble.server.event.ChannelPlayerRemovePostEvent;
import fr.pederobien.mumble.server.event.ChannelSoundModifierChangePostEvent;
import fr.pederobien.mumble.server.event.RequestEvent;
import fr.pederobien.mumble.server.event.ServerChannelAddPostEvent;
import fr.pederobien.mumble.server.event.ServerChannelRemovePostEvent;
import fr.pederobien.mumble.server.impl.InternalServer;
import fr.pederobien.utils.event.EventHandler;
import fr.pederobien.utils.event.EventManager;
import fr.pederobien.utils.event.EventPriority;
import fr.pederobien.utils.event.IEventListener;

public class MumbleGameServerClient implements IEventListener {
	private ITcpConnection tcpConnection;
	private InternalServer internalServer;

	public MumbleGameServerClient(InternalServer internalServer, ITcpConnection tcpConnection) {
		this.internalServer = internalServer;
		this.tcpConnection = tcpConnection;

		EventManager.registerListener(this);
	}

	@EventHandler(priority = EventPriority.NORMAL)
	private void onChannelAdded(ServerChannelAddPostEvent event) {
		if (!event.getServer().equals(internalServer.getMumbleServer()))
			return;

		send(MumbleMessageFactory.create(Idc.CHANNELS, Oid.ADD, event.getChannel().getName(), event.getChannel().getSoundModifier().getName()));
	}

	@EventHandler(priority = EventPriority.NORMAL)
	private void onChannelRemoved(ServerChannelRemovePostEvent event) {
		if (!event.getServer().equals(internalServer.getMumbleServer()))
			return;

		send(MumbleMessageFactory.create(Idc.CHANNELS, Oid.REMOVE, event.getChannel().getName()));
	}

	@EventHandler(priority = EventPriority.NORMAL)
	private void onChannelRenamed(ChannelNameChangePostEvent event) {
		send(MumbleMessageFactory.create(Idc.CHANNELS, Oid.SET, event.getOldName(), event.getChannel().getName()));
	}

	@EventHandler(priority = EventPriority.NORMAL)
	private void onPlayerAdded(ChannelPlayerAddPostEvent event) {
		send(MumbleMessageFactory.create(Idc.CHANNELS_PLAYER, Oid.ADD, event.getChannel().getName(), event.getPlayer().getName()));
	}

	@EventHandler(priority = EventPriority.NORMAL)
	private void onPlayerRemoved(ChannelPlayerRemovePostEvent event) {
		send(MumbleMessageFactory.create(Idc.CHANNELS_PLAYER, Oid.REMOVE, event.getChannel().getName(), event.getPlayer().getName()));
	}

	@EventHandler(priority = EventPriority.NORMAL)
	private void onSoundModifierChanged(ChannelSoundModifierChangePostEvent event) {
		send(MumbleMessageFactory.create(Idc.SOUND_MODIFIER, Oid.SET, event.getChannel().getName(), event.getChannel().getSoundModifier().getName()));
	}

	@EventHandler(priority = EventPriority.NORMAL)
	private void onUnexpectedDataReceived(UnexpectedDataReceivedEvent event) {
		if (!event.getConnection().equals(tcpConnection))
			return;

		IMessage<Header> request = MumbleMessageFactory.parse(event.getAnswer());
		if (checkPermission(request))
			send(internalServer.answer(new RequestEvent(null, request)));
		else
			send(MumbleMessageFactory.answer(request, ErrorCode.PERMISSION_REFUSED));
	}

	private void send(IMessage<Header> message) {
		if (tcpConnection.isDisposed())
			return;
		tcpConnection.send(new MumbleCallbackMessage(message, null));
	}

	private boolean checkPermission(IMessage<Header> request) {
		switch (request.getHeader().getIdc()) {
		case SERVER_JOIN:
		case PLAYER_MUTE:
		case PLAYER_DEAFEN:
		case CHANNELS:
		case SOUND_MODIFIER:
		case PLAYER_KICK:
			return true;
		default:
			return false;
		}
	}
}
