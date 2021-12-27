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
import fr.pederobien.mumble.server.impl.modifiers.RangeParameter;
import fr.pederobien.mumble.server.interfaces.IChannel;
import fr.pederobien.mumble.server.interfaces.IParameter;
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
				event.getClient().getTcpClient().onJoin();

			List<Object> informations = new ArrayList<Object>();

			// Number of sound modifier
			Map<String, ISoundModifier> modifiers = SoundManager.getSoundModifiers();
			informations.add(modifiers.size());

			// Modifier informations
			for (Map.Entry<String, ISoundModifier> modifierEntry : modifiers.entrySet()) {
				// Modifier's name
				informations.add(modifierEntry.getValue().getName());

				// Number of parameter
				informations.add(modifierEntry.getValue().getParameters().size());

				// Modifier's parameter
				for (Map.Entry<String, IParameter<?>> parameterEntry : modifierEntry.getValue().getParameters()) {
					// Parameter's name
					informations.add(parameterEntry.getValue().getName());

					// Parameter's type
					informations.add(parameterEntry.getValue().getType());

					// isRangeParameter
					informations.add(parameterEntry.getValue() instanceof RangeParameter);

					// Parameter's default value
					informations.add(parameterEntry.getValue().getDefaultValue());

					// Parameter's value
					informations.add(parameterEntry.getValue().getValue());

					// Parameter's range value
					if (parameterEntry.getValue() instanceof RangeParameter) {
						RangeParameter<?> rangeParameter = (RangeParameter<?>) parameterEntry.getValue();
						informations.add(rangeParameter.getMin());
						informations.add(rangeParameter.getMax());
					}
				}
			}

			// Number of channels
			informations.add(getInternalServer().getChannels().toList().size());
			for (IChannel channel : getInternalServer().getChannels()) {
				// Channel name
				informations.add(channel.getName());

				// Channel's sound modifier name
				informations.add(channel.getSoundModifier().getName());

				// Number of parameters
				informations.add(channel.getSoundModifier().getParameters().size());

				for (Map.Entry<String, IParameter<?>> parameterEntry : channel.getSoundModifier().getParameters()) {
					// Parameter's name
					informations.add(parameterEntry.getValue().getName());

					// Parameter's type
					informations.add(parameterEntry.getValue().getType());

					// Parameter's value
					informations.add(parameterEntry.getValue().getValue());
				}

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

			// Player identifier
			informations.add(event.getClient().getUUID());

			boolean playerConnected = event.getClient().getPlayer() != null;
			if (playerConnected) {
				// Player online status
				informations.add(true);

				// Player name
				informations.add(event.getClient().getPlayer().getName());

				// Player admin status
				informations.add(event.getClient().getPlayer().isAdmin());
			} else
				informations.add(false);
			return event.getRequest().answer(informations.toArray());
		default:
			return MumbleMessageFactory.answer(event.getRequest(), ErrorCode.INCOMPATIBLE_IDC_OID);
		}
	}
}
