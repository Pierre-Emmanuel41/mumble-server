package fr.pederobien.mumble.server.impl.responses;

import fr.pederobien.messenger.interfaces.IMessage;
import fr.pederobien.mumble.common.impl.ErrorCode;
import fr.pederobien.mumble.common.impl.Header;
import fr.pederobien.mumble.common.impl.MumbleMessageFactory;
import fr.pederobien.mumble.server.event.RequestEvent;
import fr.pederobien.mumble.server.impl.InternalServer;

public class ServerJoinResponse extends AbstractResponse {

	public ServerJoinResponse(InternalServer internalServer) {
		super(internalServer);
	}

	@Override
	public IMessage<Header> apply(RequestEvent event) {
		try {
			event.getClient().onJoin();
			return event.getRequest().answer(event.getRequest().getPayload());
		} catch (IllegalStateException e) {
			return MumbleMessageFactory.answer(event.getRequest(), ErrorCode.PERMISSION_REFUSED);
		}
	}

}
