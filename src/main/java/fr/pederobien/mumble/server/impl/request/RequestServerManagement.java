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

public abstract class RequestServerManagement {
	private IMumbleServer server;
	private Map<Idc, Map<Oid, Function<IMumbleMessage, IMumbleMessage>>> requests;

	/**
	 * Creates a request management in order to modify the given server and answer to remote requests.
	 * 
	 * @param server The server to update.
	 */
	public RequestServerManagement(IMumbleServer server) {
		this.server = server;
		requests = new HashMap<Idc, Map<Oid, Function<IMumbleMessage, IMumbleMessage>>>();
	}

	/**
	 * run a specific treatment associated to the given request.
	 * 
	 * @param request The request sent by the remote.
	 * 
	 * @return The server response.
	 */
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
