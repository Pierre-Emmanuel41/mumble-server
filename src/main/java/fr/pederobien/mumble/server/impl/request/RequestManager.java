package fr.pederobien.mumble.server.impl.request;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

import fr.pederobien.mumble.common.impl.MumbleErrorCode;
import fr.pederobien.mumble.common.impl.Identifier;
import fr.pederobien.mumble.common.interfaces.IMumbleMessage;
import fr.pederobien.mumble.server.impl.AbstractMumbleConnection;
import fr.pederobien.mumble.server.impl.MumbleServerMessageFactory;
import fr.pederobien.mumble.server.impl.RequestReceivedHolder;
import fr.pederobien.mumble.server.interfaces.IMumbleServer;
import fr.pederobien.mumble.server.interfaces.IRequestManager;

public abstract class RequestManager implements IRequestManager {
	private float version;
	private IMumbleServer server;
	private Map<Identifier, Function<RequestReceivedHolder, IMumbleMessage>> requests;

	/**
	 * Creates a request management in order to modify the given server and answer to remote requests.
	 * 
	 * @param server  The server to update.
	 * @param version The version of the communication protocol associated to this requests manager.
	 */
	public RequestManager(IMumbleServer server, float version) {
		this.server = server;
		this.version = version;
		requests = new HashMap<Identifier, Function<RequestReceivedHolder, IMumbleMessage>>();
	}

	@Override
	public float getVersion() {
		return version;
	}

	@Override
	public IMumbleMessage answer(RequestReceivedHolder holder) {
		Function<RequestReceivedHolder, IMumbleMessage> answer = requests.get(holder.getRequest().getHeader().getIdentifier());
		if (answer == null)
			return MumbleServerMessageFactory.answer(holder.getRequest(), MumbleErrorCode.IDENTIFIER_UNKNOWN);

		return answer.apply(holder);
	}

	/**
	 * @return The map that contains the code to run according to the identifier of the request sent by the remote.
	 */
	public Map<Identifier, Function<RequestReceivedHolder, IMumbleMessage>> getRequests() {
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
	 * @param mumbleErrorCode The error code of the response.
	 * 
	 * @return The message associated to the answer.
	 */
	protected IMumbleMessage answer(float version, IMumbleMessage message, MumbleErrorCode mumbleErrorCode) {
		return MumbleServerMessageFactory.answer(version, message, mumbleErrorCode);
	}

	/**
	 * Try to execute code according to the instance type of the connection.
	 * 
	 * @param holder   The holder that contains the connection that received the request and the request itself.
	 * @param clazz    The expected connection type.
	 * @param function The code to execute if the connection is an instance of the given class.
	 * 
	 * @return False if the connection is not an instance of the class, the function's result otherwise.
	 */
	protected <T extends AbstractMumbleConnection> RunResult runIfInstanceof(RequestReceivedHolder holder, Class<T> clazz, Function<T, Boolean> function) {
		try {
			return new RunResult(true, function.apply(clazz.cast(holder.getConnection())));
		} catch (ClassCastException e) {
			return new RunResult(false, false);
		}
	}

	protected class RunResult {
		private boolean hasRun, result;

		private RunResult(boolean hasRun, boolean result) {
			this.hasRun = hasRun;
			this.result = result;
		}

		/**
		 * @return True if the function has been ran.
		 */
		public boolean getHasRun() {
			return hasRun;
		}

		/**
		 * @return The result of the function.
		 */
		public boolean getResult() {
			return result;
		}
	}
}
