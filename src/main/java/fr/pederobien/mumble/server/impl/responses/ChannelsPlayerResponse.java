package fr.pederobien.mumble.server.impl.responses;

import java.util.Optional;

import fr.pederobien.messenger.interfaces.IMessage;
import fr.pederobien.mumble.common.impl.ErrorCode;
import fr.pederobien.mumble.common.impl.Header;
import fr.pederobien.mumble.common.impl.MumbleMessageFactory;
import fr.pederobien.mumble.server.event.RequestEvent;
import fr.pederobien.mumble.server.impl.InternalServer;
import fr.pederobien.mumble.server.impl.Player;
import fr.pederobien.mumble.server.interfaces.IChannel;
import fr.pederobien.mumble.server.interfaces.IPlayer;

public class ChannelsPlayerResponse extends AbstractResponse {

	public ChannelsPlayerResponse(InternalServer internalServer) {
		super(internalServer);
	}

	@Override
	public IMessage<Header> apply(RequestEvent event) {
		String channelName, playerName;
		Optional<IChannel> optChannel;

		switch (event.getRequest().getHeader().getOid()) {
		case ADD:
			// Getting channel associated to the its name.
			channelName = (String) event.getRequest().getPayload()[0];
			optChannel = getInternalServer().getChannels().getChannel(channelName);
			if (!optChannel.isPresent())
				return MumbleMessageFactory.answer(event.getRequest(), ErrorCode.CHANNEL_DOES_NOT_EXISTS);

			// Getting player associated to its name.
			playerName = (String) event.getRequest().getPayload()[1];
			final Optional<Player> optPlayerAdd = getInternalServer().getClients().getPlayer(playerName);
			if (!optPlayerAdd.isPresent())
				return MumbleMessageFactory.answer(event.getRequest(), ErrorCode.PLAYER_NOT_RECOGNIZED);

			// A player cannot be registered in two channels at the same time.
			for (IChannel c : getInternalServer().getChannels())
				for (IPlayer p : c.getPlayers())
					if (p.equals(optPlayerAdd.get()))
						return MumbleMessageFactory.answer(event.getRequest(), ErrorCode.PLAYER_ALREADY_REGISTERED);

			// Doing modification on the server.
			optChannel.get().addPlayer(optPlayerAdd.get());
			return event.getRequest().answer(channelName, playerName);
		case REMOVE:
			// Getting channel associated to the its name.
			channelName = (String) event.getRequest().getPayload()[0];
			optChannel = getInternalServer().getChannels().getChannel(channelName);
			if (!optChannel.isPresent())
				return MumbleMessageFactory.answer(event.getRequest(), ErrorCode.CHANNEL_DOES_NOT_EXISTS);

			// Getting player associated to its name.
			playerName = (String) event.getRequest().getPayload()[1];
			final Optional<Player> optPlayerRemove = getInternalServer().getClients().getPlayer(playerName);
			if (!optPlayerRemove.isPresent())
				return MumbleMessageFactory.answer(event.getRequest(), ErrorCode.PLAYER_NOT_RECOGNIZED);

			// doing modification on the server.
			optChannel.get().removePlayer(optPlayerRemove.get());
			return event.getRequest().answer(channelName, playerName);
		default:
			return MumbleMessageFactory.answer(event.getRequest(), ErrorCode.INCOMPATIBLE_IDC_OID);
		}
	}
}
