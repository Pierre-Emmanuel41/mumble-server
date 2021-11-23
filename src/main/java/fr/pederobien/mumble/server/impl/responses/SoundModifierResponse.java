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
import fr.pederobien.mumble.server.impl.InternalServer;
import fr.pederobien.mumble.server.impl.SoundManager;
import fr.pederobien.mumble.server.impl.modifiers.Parameter;
import fr.pederobien.mumble.server.impl.modifiers.ParameterList;
import fr.pederobien.mumble.server.impl.modifiers.RangeParameter;
import fr.pederobien.mumble.server.interfaces.IChannel;
import fr.pederobien.mumble.server.interfaces.IParameter;
import fr.pederobien.mumble.server.interfaces.ISoundModifier;

public class SoundModifierResponse extends AbstractResponse {

	public SoundModifierResponse(InternalServer internalServer) {
		super(internalServer);
	}

	@Override
	public IMessage<Header> apply(RequestEvent event) {
		IChannel channel;
		Optional<ISoundModifier> soundModifier;
		List<Object> informations = new ArrayList<Object>();

		switch (event.getRequest().getHeader().getOid()) {
		case GET:
			// channel's name
			channel = getInternalServer().getChannels().get((String) event.getRequest().getPayload()[0]);
			if (channel == null)
				return MumbleMessageFactory.answer(event.getRequest(), ErrorCode.CHANNEL_DOES_NOT_EXISTS);

			// channel's name
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
			return event.getRequest().answer(informations.toArray());
		case SET:
			int currentIndex = 0;

			// Channel's name
			channel = getInternalServer().getChannels().get((String) event.getRequest().getPayload()[currentIndex++]);
			if (channel == null)
				return MumbleMessageFactory.answer(event.getRequest(), ErrorCode.CHANNEL_DOES_NOT_EXISTS);

			// Modifier's name
			soundModifier = SoundManager.getByName((String) event.getRequest().getPayload()[currentIndex++]);
			if (!soundModifier.isPresent())
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

			if (channel.getSoundModifier().equals(soundModifier.get()))
				channel.getSoundModifier().getParameters().update(parameterList);
			else {
				soundModifier.get().getParameters().update(parameterList);
				channel.setSoundModifier(soundModifier.get());
			}

			return event.getRequest().answer(event.getRequest().getPayload());
		case INFO:
			// Number of modifiers
			Map<String, ISoundModifier> modifiers = SoundManager.getSoundModifiers();
			informations.add(modifiers.size());

			// Modifier informations
			for (ISoundModifier modifier : modifiers.values()) {
				// Modifier's name
				informations.add(modifier.getName());

				// Number of parameter
				informations.add(modifier.getParameters().size());

				// Modifier's parameter
				for (Map.Entry<String, IParameter<?>> parameterEntry : modifier.getParameters()) {
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

			return event.getRequest().answer(informations.toArray());
		default:
			return MumbleMessageFactory.answer(event.getRequest(), ErrorCode.INCOMPATIBLE_IDC_OID);
		}
	}
}
