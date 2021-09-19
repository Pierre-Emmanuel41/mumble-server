package fr.pederobien.mumble.server.impl.responses;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import fr.pederobien.messenger.interfaces.IMessage;
import fr.pederobien.mumble.common.impl.ErrorCode;
import fr.pederobien.mumble.common.impl.Header;
import fr.pederobien.mumble.common.impl.MumbleMessageFactory;
import fr.pederobien.mumble.server.event.RequestEvent;
import fr.pederobien.mumble.server.impl.InternalServer;
import fr.pederobien.mumble.server.impl.SoundManager;
import fr.pederobien.mumble.server.interfaces.IChannel;
import fr.pederobien.mumble.server.interfaces.IPlayer;
import fr.pederobien.mumble.server.interfaces.ISoundModifier;

public class ServerJoinResponse extends AbstractResponse {

	public ServerJoinResponse(InternalServer internalServer) {
		super(internalServer);
	}

	@Override
	public IMessage<Header> apply(RequestEvent event) {
		switch (event.getRequest().getHeader().getOid()) {
		case SET:
			if (event.getClient() != null)
				event.getClient().onJoin();

			List<Object> informations = new ArrayList<Object>();

			// UDP port number
			informations.add(getInternalServer().getUdpPort());

			// Number of channels
			informations.add(getInternalServer().getChannels().size());
			for (IChannel channel : getInternalServer().getChannels().values()) {
				// Channel name
				informations.add(channel.getName());

				// Channel's sound modifier name
				informations.add(channel.getSoundModifier().getName());

				// Number of players
				informations.add(channel.getPlayers().size());

				for (IPlayer player : channel.getPlayers()) {
					// Player name
					informations.add(player.getName());

					// Player's mute status
					informations.add(player.isMute());

					// Player's deafen status
					informations.add(player.isDeafen());
				}
			}

			// Number of sound modifier
			Map<String, ISoundModifier> modifiers = SoundManager.getSoundModifiers();
			informations.add(modifiers.size());

			// Modifier informations
			for (ISoundModifier modifier : modifiers.values())
				// Modifier's name
				informations.add(modifier.getName());

			// Player identifier
			informations.add(event.getClient().getUUID());

			boolean playerConnected = event.getClient().getPlayer() != null;
			if (playerConnected) {
				// Player online status
				informations.add(true);

				// Player name
				informations.add(event.getClient().getPlayer().getName());

				// Player admin status
				informations.add(event.getClient().getPlayer().isOnline());
			} else
				informations.add(false);
			return event.getRequest().answer(informations.toArray());
		default:
			return MumbleMessageFactory.answer(event.getRequest(), ErrorCode.INCOMPATIBLE_IDC_OID);
		}
	}
}
