package fr.pederobien.mumble.server.impl.responses;

import java.util.Optional;

import fr.pederobien.messenger.interfaces.IMessage;
import fr.pederobien.mumble.common.impl.ErrorCode;
import fr.pederobien.mumble.common.impl.Header;
import fr.pederobien.mumble.common.impl.MumbleMessageFactory;
import fr.pederobien.mumble.server.event.RequestEvent;
import fr.pederobien.mumble.server.exceptions.PlayerNotRegisteredInChannelException;
import fr.pederobien.mumble.server.impl.InternalServer;
import fr.pederobien.mumble.server.impl.Player;

public class PlayerMuteResponse extends AbstractResponse {

	public PlayerMuteResponse(InternalServer internalServer) {
		super(internalServer);
	}

	@Override
	public IMessage<Header> apply(RequestEvent event) {
		String playerName = (String) event.getRequest().getPayload()[0];
		boolean isMute = (boolean) event.getRequest().getPayload()[1];
		switch (event.getRequest().getHeader().getOid()) {
		case SET:
			try {
				Optional<Player> optPlayer = getInternalServer().getPlayer(playerName);
				if (!optPlayer.isPresent())
					return MumbleMessageFactory.answer(event.getRequest(), ErrorCode.PLAYER_NOT_RECOGNIZED);

				optPlayer.get().setMute(isMute);
				return event.getRequest().answer(event.getRequest().getPayload());
			} catch (PlayerNotRegisteredInChannelException e) {
				return MumbleMessageFactory.answer(event.getRequest(), ErrorCode.PLAYER_NOT_REGISTERED);
			}
		default:
			return MumbleMessageFactory.answer(event.getRequest(), ErrorCode.INCOMPATIBLE_IDC_OID);
		}
	}
}
