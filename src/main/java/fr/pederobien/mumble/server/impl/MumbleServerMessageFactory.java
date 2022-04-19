package fr.pederobien.mumble.server.impl;

import fr.pederobien.mumble.common.impl.ErrorCode;
import fr.pederobien.mumble.common.impl.Idc;
import fr.pederobien.mumble.common.impl.MumbleMessageFactory;
import fr.pederobien.mumble.common.impl.Oid;
import fr.pederobien.mumble.common.interfaces.IMumbleMessage;

public class MumbleServerMessageFactory {
	private static final MumbleMessageFactory FACTORY;

	static {
		FACTORY = MumbleMessageFactory.getInstance(10000);
	}

	/**
	 * Creates a message based on the given parameters associated to the latest version of the communication protocol.
	 * 
	 * @param idc     The message idc.
	 * @param payload The message payload.
	 * 
	 * @return The created message.
	 */
	public static IMumbleMessage create(Idc idc, Object... payload) {
		return FACTORY.create(idc, Oid.GET, ErrorCode.NONE, payload);
	}

	/**
	 * Creates a message based on the given parameters associated to a specific version of the communication protocol.
	 * 
	 * @param version The protocol version to use for the returned message.
	 * @param idc     The message idc.
	 * @param payload The message payload.
	 * 
	 * @return The created message.
	 */
	public static IMumbleMessage create(float version, Idc idc, Oid oid, Object... payload) {
		return FACTORY.create(version, idc, oid, ErrorCode.NONE, payload);
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
	 * @param errorCode The error code of the response.
	 * 
	 * @return The message associated to the answer.
	 */
	public static IMumbleMessage answer(IMumbleMessage message, ErrorCode errorCode) {
		return FACTORY.answer(message, message.getHeader().getIdc(), message.getHeader().getOid(), errorCode);
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
	public static IMumbleMessage answer(float version, IMumbleMessage message, ErrorCode errorCode) {
		return FACTORY.answer(version, message, message.getHeader().getIdc(), message.getHeader().getOid(), errorCode);
	}
}
