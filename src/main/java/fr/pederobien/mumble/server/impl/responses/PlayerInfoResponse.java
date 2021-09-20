package fr.pederobien.mumble.server.impl.responses;

import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.UnknownHostException;

import fr.pederobien.messenger.interfaces.IMessage;
import fr.pederobien.mumble.common.impl.ErrorCode;
import fr.pederobien.mumble.common.impl.Header;
import fr.pederobien.mumble.common.impl.MumbleMessageFactory;
import fr.pederobien.mumble.server.event.RequestEvent;
import fr.pederobien.mumble.server.impl.InternalServer;
import fr.pederobien.mumble.server.impl.Player;

public class PlayerInfoResponse extends AbstractResponse {

	public PlayerInfoResponse(InternalServer internalServer) {
		super(internalServer);
	}

	@Override
	public IMessage<Header> apply(RequestEvent event) {
		switch (event.getRequest().getHeader().getOid()) {
		case GET:
			Player player = event.getClient().getPlayer();
			if (player != null)
				return event.getRequest().answer(player.isOnline(), player.getName(), player.isAdmin());
			return event.getRequest().answer(false);
		case SET:
			// Request sent by a mumble client but should be sent by the game server.
			if (event.getClient() != null)
				return MumbleMessageFactory.answer(event.getRequest(), ErrorCode.PERMISSION_REFUSED);

			String playerName = (String) event.getRequest().getPayload()[0];
			boolean connected = (boolean) event.getRequest().getPayload()[1];
			if (connected) {
				String address = (String) event.getRequest().getPayload()[2];
				int port = (int) event.getRequest().getPayload()[3];
				boolean isAdmin = (boolean) event.getRequest().getPayload()[4];
				try {
					getInternalServer().addPlayer(new InetSocketAddress(InetAddress.getByName(address), port), playerName, isAdmin);
				} catch (UnknownHostException e) {
					return MumbleMessageFactory.answer(event.getRequest(), ErrorCode.UNEXPECTED_ERROR);
				}
			} else
				getInternalServer().removePlayer(playerName);
			return event.getRequest().answer(event.getRequest().getPayload());
		default:
			return MumbleMessageFactory.answer(event.getRequest(), ErrorCode.INCOMPATIBLE_IDC_OID);
		}
	}
}
