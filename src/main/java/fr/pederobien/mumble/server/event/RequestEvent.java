package fr.pederobien.mumble.server.event;

import fr.pederobien.messenger.interfaces.IMessage;
import fr.pederobien.mumble.common.impl.Header;
import fr.pederobien.mumble.server.impl.Client;

public class RequestEvent {
	private Client client;
	private IMessage<Header> request;

	public RequestEvent(Client client, IMessage<Header> request) {
		this.client = client;
		this.request = request;
	}

	/**
	 * @return The client who received a request.
	 */
	public Client getClient() {
		return client;
	}

	/**
	 * @return The request received by the client.
	 */
	public IMessage<Header> getRequest() {
		return request;
	}
}
