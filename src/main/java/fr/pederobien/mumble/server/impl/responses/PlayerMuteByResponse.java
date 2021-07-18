package fr.pederobien.mumble.server.impl.responses;

import java.util.Optional;

import fr.pederobien.messenger.interfaces.IMessage;
import fr.pederobien.mumble.common.impl.ErrorCode;
import fr.pederobien.mumble.common.impl.Header;
import fr.pederobien.mumble.common.impl.MumbleMessageFactory;
import fr.pederobien.mumble.server.event.RequestEvent;
import fr.pederobien.mumble.server.impl.InternalServer;
import fr.pederobien.mumble.server.impl.Player;
import fr.pederobien.mumble.server.interfaces.IPlayer;

public class PlayerMuteByResponse extends AbstractResponse {

	public PlayerMuteByResponse(InternalServer internalServer) {
		super(internalServer);
	}

	@Override
	public IMessage<Header> apply(RequestEvent event) {
		switch (event.getRequest().getHeader().getOid()) {
		case SET:
			IPlayer player = event.getClient().getPlayer();
			if (player == null)
				return MumbleMessageFactory.answer(event.getRequest(), ErrorCode.PLAYER_NOT_RECOGNIZED);

			String playerName = (String) event.getRequest().getPayload()[0];
			if (!player.getName().equals(playerName))
				return MumbleMessageFactory.answer(event.getRequest(), ErrorCode.UNEXPECTED_ERROR);

			String playerMuteOrUnmuteName = (String) event.getRequest().getPayload()[1];
			final Optional<Player> optPlayerMuteOrUnmute = getInternalServer().getPlayer(playerMuteOrUnmuteName);
			if (!optPlayerMuteOrUnmute.isPresent())
				return MumbleMessageFactory.answer(event.getRequest(), ErrorCode.PLAYER_NOT_RECOGNIZED);

			Player playerMuteOrUnmute = optPlayerMuteOrUnmute.get();
			if (!playerMuteOrUnmute.getChannel().equals(player.getChannel()))
				return MumbleMessageFactory.answer(event.getRequest(), ErrorCode.PLAYERS_IN_DIFFERENT_CHANNELS);

			boolean isMute = (boolean) event.getRequest().getPayload()[2];
			optPlayerMuteOrUnmute.get().setIsMuteBy(player, isMute);
			return event.getRequest().answer(playerName, playerMuteOrUnmuteName, isMute);
		default:
			return MumbleMessageFactory.answer(event.getRequest(), ErrorCode.INCOMPATIBLE_IDC_OID);
		}
	}
}
