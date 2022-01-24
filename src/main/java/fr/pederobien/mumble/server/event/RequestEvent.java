package fr.pederobien.mumble.server.event;

import fr.pederobien.messenger.interfaces.IMessage;
import fr.pederobien.mumble.common.impl.Header;
import fr.pederobien.mumble.server.impl.MumblePlayerClient;

public class RequestEvent {
	private MumblePlayerClient mumblePlayerClient;
	private IMessage<Header> request;

	public RequestEvent(MumblePlayerClient mumblePlayerClient, IMessage<Header> request) {
		this.mumblePlayerClient = mumblePlayerClient;
		this.request = request;
	}

	/**
	 * @return The client who received a request.
	 */
	public MumblePlayerClient getClient() {
		return mumblePlayerClient;
	}

	/**
	 * @return The request received by the client.
	 */
	public IMessage<Header> getRequest() {
		return request;
	}
}
