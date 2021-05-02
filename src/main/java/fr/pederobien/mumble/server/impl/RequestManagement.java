package fr.pederobien.mumble.server.impl;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

import fr.pederobien.messenger.interfaces.IMessage;
import fr.pederobien.mumble.common.impl.ErrorCode;
import fr.pederobien.mumble.common.impl.Header;
import fr.pederobien.mumble.common.impl.Idc;
import fr.pederobien.mumble.common.impl.MumbleMessageFactory;
import fr.pederobien.mumble.server.event.RequestEvent;
import fr.pederobien.mumble.server.impl.responses.ChannelsResponse;
import fr.pederobien.mumble.server.impl.responses.ChannelsPlayerResponse;
import fr.pederobien.mumble.server.impl.responses.PlayerDeafenResponse;
import fr.pederobien.mumble.server.impl.responses.PlayerKickResponse;
import fr.pederobien.mumble.server.impl.responses.PlayerMuteByResponse;
import fr.pederobien.mumble.server.impl.responses.PlayerMuteResponse;
import fr.pederobien.mumble.server.impl.responses.PlayerStatusResponse;
import fr.pederobien.mumble.server.impl.responses.ServerResponse;
import fr.pederobien.mumble.server.impl.responses.UdpPortResponse;
import fr.pederobien.mumble.server.impl.responses.UniqueIdentifierResponse;

public class RequestManagement {
	private Map<Idc, Function<RequestEvent, IMessage<Header>>> responses;

	public RequestManagement(InternalServer internalServer) {
		responses = new HashMap<Idc, Function<RequestEvent, IMessage<Header>>>();

		responses.put(Idc.UNIQUE_IDENTIFIER, new UniqueIdentifierResponse(internalServer));
		responses.put(Idc.PLAYER_STATUS, new PlayerStatusResponse(internalServer));
		responses.put(Idc.CHANNELS, new ChannelsResponse(internalServer));
		responses.put(Idc.CHANNELS_PLAYER, new ChannelsPlayerResponse(internalServer));
		responses.put(Idc.SERVER_CONFIGURATION, new ServerResponse(internalServer));
		responses.put(Idc.UDP_PORT, new UdpPortResponse(internalServer));
		responses.put(Idc.PLAYER_MUTE, new PlayerMuteResponse(internalServer));
		responses.put(Idc.PLAYER_DEAFEN, new PlayerDeafenResponse(internalServer));
		responses.put(Idc.PLAYER_MUTE_BY, new PlayerMuteByResponse(internalServer));
		responses.put(Idc.PLAYER_KICK, new PlayerKickResponse(internalServer));
	}

	public IMessage<Header> answer(RequestEvent event) {
		Function<RequestEvent, IMessage<Header>> answer = responses.get(event.getRequest().getHeader().getIdc());
		return answer != null ? answer.apply(event) : MumbleMessageFactory.answer(event.getRequest(), ErrorCode.IDC_UNKNOWN);
	}
}
