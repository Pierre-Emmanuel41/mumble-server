package fr.pederobien.mumble.server.impl.request;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

import fr.pederobien.mumble.common.impl.ErrorCode;
import fr.pederobien.mumble.common.impl.Idc;
import fr.pederobien.mumble.common.impl.Oid;
import fr.pederobien.mumble.common.impl.messages.v10.PlayerGetMessageV10;
import fr.pederobien.mumble.common.impl.messages.v10.PlayerSetMessageV10;
import fr.pederobien.mumble.common.impl.messages.v10.SoundModifierGetMessageV10;
import fr.pederobien.mumble.common.impl.messages.v10.SoundModifierInfoMessageV10;
import fr.pederobien.mumble.common.interfaces.IMumbleMessage;
import fr.pederobien.mumble.server.impl.MumbleServerMessageFactory;
import fr.pederobien.mumble.server.impl.SoundManager;
import fr.pederobien.mumble.server.interfaces.IMumbleServer;
import fr.pederobien.mumble.server.interfaces.IRequestManager;

public abstract class RequestManager implements IRequestManager {
	private float version;
	private IMumbleServer server;
	private Map<Idc, Map<Oid, Function<IMumbleMessage, IMumbleMessage>>> requests;

	/**
	 * Creates a request management in order to modify the given server and answer to remote requests.
	 * 
	 * @param server  The server to update.
	 * @param version The version of the communication protocol associated to this requests manager.
	 */
	public RequestManager(IMumbleServer server, float version) {
		this.server = server;
		this.version = version;
		requests = new HashMap<Idc, Map<Oid, Function<IMumbleMessage, IMumbleMessage>>>();
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
		Map<Oid, Function<IMumbleMessage, IMumbleMessage>> map = requests.get(request.getHeader().getIdc());
		if (map == null)
			return MumbleServerMessageFactory.answer(request, ErrorCode.IDC_UNKNOWN);

		Function<IMumbleMessage, IMumbleMessage> answer = map.get(request.getHeader().getOid());
		if (answer == null)
			return MumbleServerMessageFactory.answer(request, ErrorCode.INCOMPATIBLE_IDC_OID);

		return answer.apply(request);
	}

	/**
	 * @return The map that stores requests.
	 */
	public Map<Idc, Map<Oid, Function<IMumbleMessage, IMumbleMessage>>> getRequests() {
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
	 * @param idc     The message idc.
	 * @param oid     The message oid.
	 * @param payload The message payload.
	 */
	protected IMumbleMessage create(float version, Idc idc, Oid oid, Object... payload) {
		return MumbleServerMessageFactory.create(version, idc, oid, payload);
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

	/**
	 * Get information about a specific player.
	 * 
	 * @param request The request sent by the remote.
	 * 
	 * @return The server response.
	 */
	protected abstract IMumbleMessage playerInfoGet(PlayerGetMessageV10 request);

	/**
	 * Update the statuses of a specific player.
	 * 
	 * @param request The request sent by the remote.
	 * 
	 * @return The server response.
	 */
	protected abstract IMumbleMessage playerInfoSet(PlayerSetMessageV10 request);

	/**
	 * Get or update the sound modifier of a channel.
	 * 
	 * @param request The request sent by the remote.
	 * 
	 * @return The server response.
	 */
	protected abstract IMumbleMessage soundModifierGet(SoundModifierGetMessageV10 request);

	/**
	 * Get a description of each sound modifier registered in the {@link SoundManager}.
	 * 
	 * @param request The request sent by the remote.
	 * 
	 * @return The server response.
	 */
	protected abstract IMumbleMessage soundModifierInfo(SoundModifierInfoMessageV10 request);
}
