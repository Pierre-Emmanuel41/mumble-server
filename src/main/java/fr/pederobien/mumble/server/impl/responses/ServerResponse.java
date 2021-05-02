package fr.pederobien.mumble.server.impl.responses;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import fr.pederobien.messenger.interfaces.IMessage;
import fr.pederobien.mumble.common.impl.ErrorCode;
import fr.pederobien.mumble.common.impl.Header;
import fr.pederobien.mumble.server.event.RequestEvent;
import fr.pederobien.mumble.server.impl.InternalServer;
import fr.pederobien.mumble.server.interfaces.IChannel;
import fr.pederobien.mumble.server.interfaces.IPlayer;

public class ServerResponse extends AbstractResponse {

	public ServerResponse(InternalServer internalServer) {
		super(internalServer);
	}

	@Override
	public IMessage<Header> apply(RequestEvent event) {
		Header header = event.getRequest().getHeader();
		switch (header.getOid()) {
		case GET:
			return getServerConfiguration(event.getRequest());
		case SET:
			return setServerConfiguration(event.getRequest());
		default:
			return event.getRequest().answer(new Header(header.getIdc(), header.getOid(), ErrorCode.INCOMPATIBLE_IDC_OID));
		}
	}

	private IMessage<Header> getServerConfiguration(IMessage<Header> request) {
		List<Object> informations = new ArrayList<Object>();
		Map<String, IChannel> channels = getInternalServer().getChannels();
		informations.add(channels.size());

		for (IChannel channel : channels.values()) {
			informations.add(channel.getName());
			informations.add(channel.getPlayers().size());

			for (IPlayer player : channel.getPlayers())
				informations.add(player.getName());
		}
		return request.answer(informations.toArray());
	}

	private IMessage<Header> setServerConfiguration(IMessage<Header> request) {
		try {
			int numberOfChannel = (int) request.getPayload()[0];
			List<String> channelNames = new ArrayList<String>();
			for (int i = 0; i < numberOfChannel; i++)
				channelNames.add((String) request.getPayload()[2 * i + 1]);

			getInternalServer().getChannels().values().forEach(channel -> channel.clear());
			getInternalServer().clearChannels();

			for (String name : channelNames)
				getInternalServer().addChannel(name);
		} catch (ClassCastException | IndexOutOfBoundsException e) {
			return request.answer(new Header(request.getHeader().getIdc(), request.getHeader().getOid(), ErrorCode.UNEXPECTED_ERROR));
		}
		return getServerConfiguration(request);
	}
}
