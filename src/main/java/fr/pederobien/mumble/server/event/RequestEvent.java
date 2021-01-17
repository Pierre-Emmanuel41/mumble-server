package fr.pederobien.mumble.server.event;

import fr.pederobien.messenger.interfaces.IMessage;
import fr.pederobien.mumble.common.impl.Header;
import fr.pederobien.mumble.server.impl.TcpClient;

public class RequestEvent {
	private TcpClient client;
	private IMessage<Header> request;

	public RequestEvent(TcpClient client, IMessage<Header> request) {
		this.client = client;
		this.request = request;
	}

	/**
	 * @return The client who received a request.
	 */
	public TcpClient getClient() {
		return client;
	}

	/**
	 * @return The request received by the client.
	 */
	public IMessage<Header> getRequest() {
		return request;
	}
}
