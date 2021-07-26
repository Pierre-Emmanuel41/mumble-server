package fr.pederobien.mumble.server.impl.responses;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import fr.pederobien.messenger.interfaces.IMessage;
import fr.pederobien.mumble.common.impl.ErrorCode;
import fr.pederobien.mumble.common.impl.Header;
import fr.pederobien.mumble.common.impl.MumbleMessageFactory;
import fr.pederobien.mumble.server.event.RequestEvent;
import fr.pederobien.mumble.server.impl.InternalServer;
import fr.pederobien.mumble.server.impl.SoundManager;
import fr.pederobien.mumble.server.interfaces.IChannel;
import fr.pederobien.mumble.server.interfaces.ISoundModifier;

public class SoundModifierResponse extends AbstractResponse {

	public SoundModifierResponse(InternalServer internalServer) {
		super(internalServer);
	}

	@Override
	public IMessage<Header> apply(RequestEvent event) {
		String channelName, modifierName;
		IChannel channel;
		Optional<ISoundModifier> soundModifier;
		switch (event.getRequest().getHeader().getOid()) {
		case GET:
			channelName = (String) event.getRequest().getPayload()[0];
			channel = getInternalServer().getChannels().get(channelName);
			if (channel == null)
				return MumbleMessageFactory.answer(event.getRequest(), ErrorCode.CHANNEL_DOES_NOT_EXISTS);

			return event.getRequest().answer(channelName, channel.getSoundModifier().getName());
		case SET:
			channelName = (String) event.getRequest().getPayload()[0];
			channel = getInternalServer().getChannels().get(channelName);
			if (channel == null)
				return MumbleMessageFactory.answer(event.getRequest(), ErrorCode.CHANNEL_DOES_NOT_EXISTS);

			modifierName = (String) event.getRequest().getPayload()[1];
			soundModifier = SoundManager.getByName(modifierName);
			if (!soundModifier.isPresent())
				return MumbleMessageFactory.answer(event.getRequest(), ErrorCode.SOUND_MODIFIER_DOES_NOT_EXIST);

			channel.setSoundModifier(soundModifier.get());
			return event.getRequest().answer(channelName, soundModifier.get().getName());
		case INFO:
			List<Object> informations = new ArrayList<Object>();
			Map<String, ISoundModifier> modifiers = SoundManager.getSoundModifiers();
			informations.add(getInternalServer().getChannels().size());

			// Modifier informations
			for (ISoundModifier modifier : modifiers.values())
				informations.add(modifier.getName());

			return event.getRequest().answer(informations.toArray());
		default:
			return MumbleMessageFactory.answer(event.getRequest(), ErrorCode.INCOMPATIBLE_IDC_OID);
		}
	}
}
