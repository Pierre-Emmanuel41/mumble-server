package fr.pederobien.mumble.server.impl.responses;

import java.util.Optional;

import fr.pederobien.messenger.interfaces.IMessage;
import fr.pederobien.mumble.common.impl.ErrorCode;
import fr.pederobien.mumble.common.impl.Header;
import fr.pederobien.mumble.common.impl.MumbleMessageFactory;
import fr.pederobien.mumble.server.event.RequestEvent;
import fr.pederobien.mumble.server.impl.InternalServer;
import fr.pederobien.mumble.server.impl.Player;

public class PlayerMuteByManagement extends AbstractManagement {

	public PlayerMuteByManagement(InternalServer internalServer) {
		super(internalServer);
	}

	@Override
	public IMessage<Header> apply(RequestEvent event) {
		switch (event.getRequest().getHeader().getOid()) {
		case SET:
			String playerName = (String) event.getRequest().getPayload()[0];
			if (!event.getClient().getPlayer().getName().equals(playerName))
				return MumbleMessageFactory.answer(event.getRequest(), ErrorCode.BAD_PLAYER_NAME);

			String playerMuteOrUnmuteName = (String) event.getRequest().getPayload()[1];
			final Optional<Player> optPlayerMuteOrUnmute = getInternalServer().getPlayer(playerMuteOrUnmuteName);
			if (!optPlayerMuteOrUnmute.isPresent())
				return MumbleMessageFactory.answer(event.getRequest(), ErrorCode.PLAYER_NOT_RECOGNIZED);

			Player playerMuteOrUnmute = optPlayerMuteOrUnmute.get();
			if (!playerMuteOrUnmute.getChannel().equals(event.getClient().getPlayer().getChannel()))
				return MumbleMessageFactory.answer(event.getRequest(), ErrorCode.PLAYERS_IN_DIFFERENT_CHANNELS);

			boolean isMute = (boolean) event.getRequest().getPayload()[2];
			optPlayerMuteOrUnmute.get().setIsMuteBy(event.getClient().getPlayer(), isMute);
			return event.getRequest().answer(playerName, playerMuteOrUnmuteName, isMute);
		default:
			return MumbleMessageFactory.answer(event.getRequest(), ErrorCode.INCOMPATIBLE_IDC_OID);
		}
	}
}
