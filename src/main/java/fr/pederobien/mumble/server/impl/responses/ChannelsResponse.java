package fr.pederobien.mumble.server.impl.responses;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import fr.pederobien.messenger.interfaces.IMessage;
import fr.pederobien.mumble.common.impl.ErrorCode;
import fr.pederobien.mumble.common.impl.Header;
import fr.pederobien.mumble.common.impl.MumbleMessageFactory;
import fr.pederobien.mumble.common.impl.ParameterType;
import fr.pederobien.mumble.server.event.RequestEvent;
import fr.pederobien.mumble.server.exceptions.ChannelAlreadyRegisteredException;
import fr.pederobien.mumble.server.exceptions.ChannelNotRegisteredException;
import fr.pederobien.mumble.server.impl.InternalServer;
import fr.pederobien.mumble.server.impl.SoundManager;
import fr.pederobien.mumble.server.impl.modifiers.Parameter;
import fr.pederobien.mumble.server.impl.modifiers.ParameterList;
import fr.pederobien.mumble.server.interfaces.IChannel;
import fr.pederobien.mumble.server.interfaces.IParameter;
import fr.pederobien.mumble.server.interfaces.IPlayer;
import fr.pederobien.mumble.server.interfaces.ISoundModifier;

public class ChannelsResponse extends AbstractResponse {

	public ChannelsResponse(InternalServer internalServer) {
		super(internalServer);
	}

	@Override
	public IMessage<Header> apply(RequestEvent event) {
		List<Object> informations = new ArrayList<Object>();
		switch (event.getRequest().getHeader().getOid()) {
		case GET:
			// Number of channels
			informations.add(getInternalServer().getChannels().toList().size());

			for (IChannel channel : getInternalServer().getChannels()) {
				// Channel's name
				informations.add(channel.getName());

				// Modifier's name
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
					// Player's name
					informations.add(player.getName());

					// Player's mute
					informations.add(player.isMute());

					// Player's deafen
					informations.add(player.isDeafen());
				}
			}
			return event.getRequest().answer(informations.toArray());
		case ADD:
			int currentIndex = 0;

			// Channel's name
			String addChannelName = (String) event.getRequest().getPayload()[currentIndex++];

			if (getInternalServer().getChannels().getChannel(addChannelName).isPresent())
				return MumbleMessageFactory.answer(event.getRequest(), ErrorCode.CHANNEL_ALREADY_EXISTS);

			// Modifier's name
			String soundModifierName = (String) event.getRequest().getPayload()[currentIndex++];

			Optional<ISoundModifier> optModifier = SoundManager.getByName(soundModifierName);
			if (!optModifier.isPresent())
				return MumbleMessageFactory.answer(event.getRequest(), ErrorCode.SOUND_MODIFIER_DOES_NOT_EXIST);

			// Number of parameters
			int numberOfParameters = (int) event.getRequest().getPayload()[currentIndex++];
			ParameterList parameterList = new ParameterList();

			for (int i = 0; i < numberOfParameters; i++) {
				// Parameter's name
				String parameterName = (String) event.getRequest().getPayload()[currentIndex++];

				// Parameter's type
				ParameterType<?> type = (ParameterType<?>) event.getRequest().getPayload()[currentIndex++];

				// Parameter's value
				Object value = event.getRequest().getPayload()[currentIndex++];

				parameterList.add(Parameter.fromType(type, parameterName, value, value));
			}

			IChannel addedChannel = getInternalServer().getChannels().add(addChannelName, soundModifierName);
			addedChannel.getSoundModifier().getParameters().update(parameterList);

			return event.getRequest().answer(event.getRequest().getPayload());
		case REMOVE:
			String removeChannelName = (String) event.getRequest().getPayload()[0];
			boolean canRemoveChannel = getInternalServer().getChannels().getChannel(removeChannelName).isPresent();
			if (canRemoveChannel) {
				getInternalServer().getChannels().remove(removeChannelName);
				return event.getRequest().answer(removeChannelName);
			} else
				return MumbleMessageFactory.answer(event.getRequest(), ErrorCode.CHANNEL_DOES_NOT_EXISTS);
		case SET:
			String oldName = (String) event.getRequest().getPayload()[0];
			String newName = (String) event.getRequest().getPayload()[1];
			try {
				getInternalServer().getChannels().renameChannel(oldName, newName);
				return event.getRequest().answer(oldName, newName);
			} catch (ChannelAlreadyRegisteredException e) {
				return MumbleMessageFactory.answer(event.getRequest(), ErrorCode.CHANNEL_ALREADY_EXISTS);
			} catch (ChannelNotRegisteredException e) {
				return MumbleMessageFactory.answer(event.getRequest(), ErrorCode.CHANNEL_DOES_NOT_EXISTS);
			}
		default:
			return MumbleMessageFactory.answer(event.getRequest(), ErrorCode.INCOMPATIBLE_IDC_OID);
		}
	}
}
