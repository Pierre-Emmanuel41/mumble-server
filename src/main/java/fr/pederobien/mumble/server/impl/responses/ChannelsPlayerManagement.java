package fr.pederobien.mumble.server.impl.responses;

import java.util.Optional;

import fr.pederobien.messenger.interfaces.IMessage;
import fr.pederobien.mumble.common.impl.ErrorCode;
import fr.pederobien.mumble.common.impl.Header;
import fr.pederobien.mumble.common.impl.MumbleMessageFactory;
import fr.pederobien.mumble.server.event.RequestEvent;
import fr.pederobien.mumble.server.impl.Channel;
import fr.pederobien.mumble.server.impl.InternalServer;
import fr.pederobien.mumble.server.impl.Player;
import fr.pederobien.mumble.server.interfaces.IChannel;
import fr.pederobien.mumble.server.interfaces.IPlayer;

public class ChannelsPlayerManagement extends AbstractManagement {

	public ChannelsPlayerManagement(InternalServer internalServer) {
		super(internalServer);
	}

	@Override
	public IMessage<Header> apply(RequestEvent event) {
		String channelName, playerName;
		Channel channel;

		switch (event.getRequest().getHeader().getOid()) {
		case ADD:
			// Getting channel associated to the its name.
			channelName = (String) event.getRequest().getPayload()[0];
			channel = (Channel) getInternalServer().getChannels().get(channelName);
			if (channel == null)
				return MumbleMessageFactory.answer(event.getRequest(), ErrorCode.CHANNEL_DOES_NOT_EXISTS);

			// Getting player associated to its name.
			playerName = (String) event.getRequest().getPayload()[1];
			final Optional<Player> optPlayerAdd = getInternalServer().getPlayer(playerName);
			if (!optPlayerAdd.isPresent())
				return MumbleMessageFactory.answer(event.getRequest(), ErrorCode.PLAYER_NOT_RECOGNIZED);

			// A player cannot be registered in two channels at the same time.
			for (IChannel c : getInternalServer().getChannels().values())
				for (IPlayer p : c.getPlayers())
					if (p.equals(optPlayerAdd.get()))
						return MumbleMessageFactory.answer(event.getRequest(), ErrorCode.PLAYER_ALREADY_REGISTERED);

			// Doing modification on the server.
			channel.addPlayer(optPlayerAdd.get());
			return event.getRequest().answer(channelName, playerName);
		case REMOVE:
			// Getting channel associated to the its name.
			channelName = (String) event.getRequest().getPayload()[0];
			channel = (Channel) getInternalServer().getChannels().get(channelName);
			if (channel == null)
				return MumbleMessageFactory.answer(event.getRequest(), ErrorCode.CHANNEL_DOES_NOT_EXISTS);

			// Getting player associated to its name.
			playerName = (String) event.getRequest().getPayload()[1];
			final Optional<IPlayer> optPlayerRemove = getInternalServer().getPlayers().stream().filter(player -> player.getName().equals(playerName)).findFirst();
			if (!optPlayerRemove.isPresent())
				return MumbleMessageFactory.answer(event.getRequest(), ErrorCode.PLAYER_NOT_RECOGNIZED);

			// doing modification on the server.
			channel.removePlayer(optPlayerRemove.get());
			return event.getRequest().answer(channelName, playerName);
		default:
			return MumbleMessageFactory.answer(event.getRequest(), ErrorCode.INCOMPATIBLE_IDC_OID);
		}
	}
}
