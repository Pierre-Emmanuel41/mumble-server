package fr.pederobien.mumble.server.impl.request;

import java.util.HashMap;
import java.util.Map;

import fr.pederobien.mumble.common.impl.ErrorCode;
import fr.pederobien.mumble.common.interfaces.IMumbleMessage;
import fr.pederobien.mumble.server.impl.MumbleServerMessageFactory;
import fr.pederobien.mumble.server.interfaces.IMumbleServer;

public class ServerRequestManager {
	private Map<Float, RequestManager> requests;

	/**
	 * Creates a request management in order to modify the given server and answer to remote requests.
	 * 
	 * @param server The server to update.
	 */
	public ServerRequestManager(IMumbleServer server) {
		requests = new HashMap<Float, RequestManager>();

		requests.put(1.0f, new RequestManagerV10(server));
	}

	/**
	 * run a specific treatment associated to the given request.
	 * 
	 * @param request The request sent by the remote.
	 * 
	 * @return The server response.
	 */
	public IMumbleMessage answer(IMumbleMessage request) {
		RequestManager management = requests.get(request.getHeader().getVersion());

		if (management == null)
			return MumbleServerMessageFactory.answer(request, ErrorCode.INCOMPATIBLE_VERSION);

		return management.answer(request);
	}
}
