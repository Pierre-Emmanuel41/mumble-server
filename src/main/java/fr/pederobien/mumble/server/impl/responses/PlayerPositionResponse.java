package fr.pederobien.mumble.server.impl.responses;

import java.util.Optional;

import fr.pederobien.messenger.interfaces.IMessage;
import fr.pederobien.mumble.common.impl.ErrorCode;
import fr.pederobien.mumble.common.impl.Header;
import fr.pederobien.mumble.common.impl.MumbleMessageFactory;
import fr.pederobien.mumble.server.event.RequestEvent;
import fr.pederobien.mumble.server.impl.InternalServer;
import fr.pederobien.mumble.server.impl.Player;
import fr.pederobien.mumble.server.interfaces.IPosition;

public class PlayerPositionResponse extends AbstractResponse {

	public PlayerPositionResponse(InternalServer internalServer) {
		super(internalServer);
	}

	@Override
	public IMessage<Header> apply(RequestEvent event) {
		String playerName;
		Optional<Player> optPlayer;

		switch (event.getRequest().getHeader().getOid()) {
		case GET:
			playerName = (String) event.getRequest().getPayload()[0];

			optPlayer = getInternalServer().getPlayer(playerName);
			if (!optPlayer.isPresent())
				return MumbleMessageFactory.answer(event.getRequest(), ErrorCode.PLAYER_NOT_RECOGNIZED);

			IPosition position = optPlayer.get().getPosition();
			return event.getRequest().answer(playerName, position.getX(), position.getY(), position.getZ(), position.getYaw(), position.getPitch());

		case SET:
			playerName = (String) event.getRequest().getPayload()[0];
			double x = (double) event.getRequest().getPayload()[1];
			double y = (double) event.getRequest().getPayload()[2];
			double z = (double) event.getRequest().getPayload()[3];
			double yaw = (double) event.getRequest().getPayload()[4];
			double pitch = (double) event.getRequest().getPayload()[5];

			optPlayer = getInternalServer().getPlayer(playerName);
			if (!optPlayer.isPresent())
				return MumbleMessageFactory.answer(event.getRequest(), ErrorCode.PLAYER_NOT_RECOGNIZED);

			optPlayer.get().getPosition().update(x, y, z, yaw, pitch);
			return event.getRequest().answer(event.getRequest().getPayload());
		default:
			return MumbleMessageFactory.answer(event.getRequest(), ErrorCode.INCOMPATIBLE_IDC_OID);
		}
	}
}
