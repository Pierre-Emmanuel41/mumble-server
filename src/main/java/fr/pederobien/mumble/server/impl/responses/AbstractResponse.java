package fr.pederobien.mumble.server.impl.responses;

import java.util.function.Function;

import fr.pederobien.messenger.interfaces.IMessage;
import fr.pederobien.mumble.common.impl.Header;
import fr.pederobien.mumble.server.event.RequestEvent;
import fr.pederobien.mumble.server.impl.InternalServer;

public abstract class AbstractResponse implements Function<RequestEvent, IMessage<Header>> {
	private InternalServer internalServer;

	public AbstractResponse(InternalServer internalServer) {
		this.internalServer = internalServer;
	}

	/**
	 * @return The internal mumble server.
	 */
	protected InternalServer getInternalServer() {
		return internalServer;
	}

}
