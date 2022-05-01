package fr.pederobien.mumble.server.impl.request;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

import fr.pederobien.mumble.common.impl.ErrorCode;
import fr.pederobien.mumble.common.impl.Identifier;
import fr.pederobien.mumble.common.interfaces.IMumbleMessage;
import fr.pederobien.mumble.server.impl.MumbleServerMessageFactory;
import fr.pederobien.mumble.server.interfaces.IMumbleServer;
import fr.pederobien.mumble.server.interfaces.IRequestManager;

public abstract class RequestManager implements IRequestManager {
	private float version;
	private IMumbleServer server;
	private Map<Identifier, Function<IMumbleMessage, IMumbleMessage>> requests;

	/**
	 * Creates a request management in order to modify the given server and answer to remote requests.
	 * 
	 * @param server  The server to update.
	 * @param version The version of the communication protocol associated to this requests manager.
	 */
	public RequestManager(IMumbleServer server, float version) {
		this.server = server;
		this.version = version;
		requests = new HashMap<Identifier, Function<IMumbleMessage, IMumbleMessage>>();
	}

	@Override
	public float getVersion() {
		return version;
	}

	/**
	 * run a specific treatment associated to the given request.
	 * 
	 * @param request The request sent by the remote.
	 * 
	 * @return The server response.
	 */
	@Override
	public IMumbleMessage answer(IMumbleMessage request) {
		Function<IMumbleMessage, IMumbleMessage> answer = requests.get(request.getHeader().getIdentifier());
		if (answer == null)
			return MumbleServerMessageFactory.answer(request, ErrorCode.IDENTIFIER_UNKNOWN);

		return answer.apply(request);
	}

	/**
	 * @return The map that contains the code to run according to the identifier of the request sent by the remote.
	 */
	public Map<Identifier, Function<IMumbleMessage, IMumbleMessage>> getRequests() {
		return requests;
	}

	/**
	 * @return The server to update.
	 */
	protected IMumbleServer getServer() {
		return server;
	}

	/**
	 * Send a message based on the given parameter to the remote.
	 * 
	 * @param identifier The identifier of the request to create.
	 * @param properties The message properties.
	 */
	protected IMumbleMessage create(float version, Identifier identifier, Object... properties) {
		return MumbleServerMessageFactory.create(version, identifier, properties);
	}

	/**
	 * Creates a new message corresponding to the answer of the <code>message</code>. Neither the identifier nor the header are
	 * modified. A specific version of the communication protocol is used to create the returned message.
	 * 
	 * @param version    The protocol version to use for the returned message.
	 * @param message    The message to answer.
	 * @param properties The response properties.
	 * 
	 * @return A new message.
	 */
	protected IMumbleMessage answer(float version, IMumbleMessage message, Object... properties) {
		return MumbleServerMessageFactory.answer(version, message, properties);
	}

	/**
	 * Creates a new message corresponding to the answer of the <code>message</code>. The identifier is not incremented. A specific
	 * version of the communication protocol is used to create the answer.
	 * 
	 * @param version   The protocol version to use for the returned message.
	 * @param request   The request to answer.
	 * @param errorCode The error code of the response.
	 * 
	 * @return The message associated to the answer.
	 */
	protected IMumbleMessage answer(float version, IMumbleMessage message, ErrorCode errorCode) {
		return MumbleServerMessageFactory.answer(version, message, errorCode);
	}
}
