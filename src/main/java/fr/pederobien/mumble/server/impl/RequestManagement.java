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
import fr.pederobien.mumble.server.impl.responses.ChannelsManagement;
import fr.pederobien.mumble.server.impl.responses.ChannelsPlayerManagement;
import fr.pederobien.mumble.server.impl.responses.PlayerDeafenManagement;
import fr.pederobien.mumble.server.impl.responses.PlayerMuteByManagement;
import fr.pederobien.mumble.server.impl.responses.PlayerMuteManagement;
import fr.pederobien.mumble.server.impl.responses.PlayerStatusManagement;
import fr.pederobien.mumble.server.impl.responses.ServerManagement;
import fr.pederobien.mumble.server.impl.responses.UdpPortManagement;
import fr.pederobien.mumble.server.impl.responses.UniqueIdentifierManagement;

public class RequestManagement {
	private Map<Idc, Function<RequestEvent, IMessage<Header>>> answers;

	public RequestManagement(InternalServer internalServer) {
		answers = new HashMap<Idc, Function<RequestEvent, IMessage<Header>>>();

		answers.put(Idc.UNIQUE_IDENTIFIER, new UniqueIdentifierManagement(internalServer));
		answers.put(Idc.PLAYER_STATUS, new PlayerStatusManagement(internalServer));
		answers.put(Idc.CHANNELS, new ChannelsManagement(internalServer));
		answers.put(Idc.CHANNELS_PLAYER, new ChannelsPlayerManagement(internalServer));
		answers.put(Idc.SERVER_CONFIGURATION, new ServerManagement(internalServer));
		answers.put(Idc.UDP_PORT, new UdpPortManagement(internalServer));
		answers.put(Idc.PLAYER_MUTE, new PlayerMuteManagement(internalServer));
		answers.put(Idc.PLAYER_DEAFEN, new PlayerDeafenManagement(internalServer));
		answers.put(Idc.PLAYER_MUTE_BY, new PlayerMuteByManagement(internalServer));
	}

	public IMessage<Header> answer(RequestEvent event) {
		Function<RequestEvent, IMessage<Header>> answer = answers.get(event.getRequest().getHeader().getIdc());
		return answer != null ? answer.apply(event) : MumbleMessageFactory.answer(event.getRequest(), ErrorCode.IDC_UNKNOWN);
	}
}
