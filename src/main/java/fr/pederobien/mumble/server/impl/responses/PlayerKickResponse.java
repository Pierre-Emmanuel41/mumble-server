package fr.pederobien.mumble.server.impl.responses;

import java.util.Optional;

import fr.pederobien.messenger.interfaces.IMessage;
import fr.pederobien.mumble.common.impl.ErrorCode;
import fr.pederobien.mumble.common.impl.Header;
import fr.pederobien.mumble.common.impl.MumbleMessageFactory;
import fr.pederobien.mumble.server.event.RequestEvent;
import fr.pederobien.mumble.server.impl.InternalServer;
import fr.pederobien.mumble.server.impl.Player;

public class PlayerKickResponse extends AbstractResponse {

	public PlayerKickResponse(InternalServer internalServer) {
		super(internalServer);
	}

	@Override
	public IMessage<Header> apply(RequestEvent event) {
		switch (event.getRequest().getHeader().getOid()) {
		case SET:
			String playerName = (String) event.getRequest().getPayload()[0];
			if (!event.getClient().getPlayer().getName().equals(playerName))
				return MumbleMessageFactory.answer(event.getRequest(), ErrorCode.UNEXPECTED_ERROR);

			String playerKickName = (String) event.getRequest().getPayload()[1];
			final Optional<Player> optPlayerMuteOrUnmute = getInternalServer().getPlayer(playerKickName);
			if (!optPlayerMuteOrUnmute.isPresent())
				return MumbleMessageFactory.answer(event.getRequest(), ErrorCode.PLAYER_NOT_RECOGNIZED);

			try {
				optPlayerMuteOrUnmute.get().getChannel().removePlayer(optPlayerMuteOrUnmute.get());
				return event.getRequest().answer(playerName, playerKickName);
			} catch (NullPointerException e) {
				return MumbleMessageFactory.answer(event.getRequest(), ErrorCode.PLAYER_NOT_REGISTERED);
			}
		default:
			return MumbleMessageFactory.answer(event.getRequest(), ErrorCode.INCOMPATIBLE_IDC_OID);
		}
	}
}
