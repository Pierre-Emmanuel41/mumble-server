package fr.pederobien.mumble.server.impl.responses;

import fr.pederobien.messenger.interfaces.IMessage;
import fr.pederobien.mumble.common.impl.ErrorCode;
import fr.pederobien.mumble.common.impl.Header;
import fr.pederobien.mumble.common.impl.MumbleMessageFactory;
import fr.pederobien.mumble.server.event.RequestEvent;
import fr.pederobien.mumble.server.exceptions.PlayerNotRegisteredInChannelException;
import fr.pederobien.mumble.server.impl.InternalServer;

public class PlayerMuteResponse extends AbstractResponse {

	public PlayerMuteResponse(InternalServer internalServer) {
		super(internalServer);
	}

	@Override
	public IMessage<Header> apply(RequestEvent event) {
		boolean isMute = (boolean) event.getRequest().getPayload()[0];
		try {
			event.getClient().getPlayer().setMute(isMute);
			return event.getRequest().answer(event.getRequest().getPayload());
		} catch (PlayerNotRegisteredInChannelException e) {
			return MumbleMessageFactory.answer(event.getRequest(), ErrorCode.UNEXPECTED_ERROR);
		}
	}
}
