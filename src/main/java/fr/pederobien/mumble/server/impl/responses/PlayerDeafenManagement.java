package fr.pederobien.mumble.server.impl.responses;

import fr.pederobien.messenger.interfaces.IMessage;
import fr.pederobien.mumble.common.impl.Header;
import fr.pederobien.mumble.server.event.RequestEvent;
import fr.pederobien.mumble.server.impl.InternalServer;

public class PlayerDeafenManagement extends AbstractManagement {

	public PlayerDeafenManagement(InternalServer internalServer) {
		super(internalServer);
	}

	@Override
	public IMessage<Header> apply(RequestEvent t) {
		return null;
	}

}
