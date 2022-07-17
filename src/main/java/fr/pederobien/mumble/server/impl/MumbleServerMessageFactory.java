package fr.pederobien.mumble.server.impl;

import fr.pederobien.mumble.common.impl.MumbleErrorCode;
import fr.pederobien.mumble.common.impl.Identifier;
import fr.pederobien.mumble.common.impl.MumbleMessageFactory;
import fr.pederobien.mumble.common.interfaces.IMumbleMessage;

public class MumbleServerMessageFactory {
	private static final MumbleMessageFactory FACTORY;

	static {
		FACTORY = MumbleMessageFactory.getInstance(10000);
	}

	/**
	 * Creates a message based on the given parameters associated to the latest version of the communication protocol.
	 * 
	 * @param identifier The identifier of the request to create.
	 * @param properties The message properties.
	 * 
	 * @return The created message.
	 */
	public static IMumbleMessage create(Identifier identifier, Object... properties) {
		return FACTORY.create(identifier, MumbleErrorCode.NONE, properties);
	}

	/**
	 * Creates a message based on the given parameters associated to a specific version of the communication protocol.
	 * 
	 * @param version    The protocol version to use for the returned message.
	 * @param identifier The identifier of the request to create.
	 * @param properties The message properties.
	 * 
	 * @return The created message.
	 */
	public static IMumbleMessage create(float version, Identifier identifier, Object... properties) {
		return FACTORY.create(version, identifier, MumbleErrorCode.NONE, properties);
	}

	/**
	 * Parse the given buffer in order to create the associated header and the payload.
	 * 
	 * @param buffer The bytes array received from the remote.
	 * 
	 * @return A new message.
	 */
	public static IMumbleMessage parse(byte[] buffer) {
		return FACTORY.parse(buffer);
	}

	/**
	 * Creates a new message corresponding to the answer of the <code>message</code>. Neither the identifier nor the header are
	 * modified. The latest version of the communication protocol is used to create the returned message.
	 * 
	 * @param message    The message to answer.
	 * @param properties The response properties.
	 * 
	 * @return A new message.
	 */
	public static IMumbleMessage answer(IMumbleMessage message, Object... properties) {
		return FACTORY.answer(message, properties);
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
	public static IMumbleMessage answer(float version, IMumbleMessage message, Object... properties) {
		return FACTORY.answer(version, message, properties);
	}

	/**
	 * Creates a new message corresponding to the answer of the <code>message</code>. The identifier is not incremented. The latest
	 * version of the communication protocol is used to create the answer.
	 * 
	 * @param request   The request to answer.
	 * @param mumbleErrorCode The error code of the response.
	 * 
	 * @return The message associated to the answer.
	 */
	public static IMumbleMessage answer(IMumbleMessage message, MumbleErrorCode mumbleErrorCode) {
		return FACTORY.answer(message, message.getHeader().getIdentifier(), mumbleErrorCode);
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
	public static IMumbleMessage answer(float version, IMumbleMessage message, MumbleErrorCode mumbleErrorCode) {
		return FACTORY.answer(version, message, message.getHeader().getIdentifier(), mumbleErrorCode);
	}
}
