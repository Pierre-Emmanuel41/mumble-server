package fr.pederobien.mumble.server.impl.responses;

import java.util.ArrayList;
import java.util.List;

import fr.pederobien.messenger.interfaces.IMessage;
import fr.pederobien.mumble.common.impl.ErrorCode;
import fr.pederobien.mumble.common.impl.Header;
import fr.pederobien.mumble.common.impl.MumbleMessageFactory;
import fr.pederobien.mumble.server.event.RequestEvent;
import fr.pederobien.mumble.server.exceptions.ChannelAlreadyExistException;
import fr.pederobien.mumble.server.exceptions.ChannelNotRegisteredException;
import fr.pederobien.mumble.server.impl.InternalServer;
import fr.pederobien.mumble.server.interfaces.IChannel;
import fr.pederobien.mumble.server.interfaces.IPlayer;

public class ChannelsResponse extends AbstractResponse {

	public ChannelsResponse(InternalServer internalServer) {
		super(internalServer);
	}

	@Override
	public IMessage<Header> apply(RequestEvent event) {
		switch (event.getRequest().getHeader().getOid()) {
		case GET:
			List<Object> informations = new ArrayList<Object>();
			informations.add(getInternalServer().getChannels().size());

			for (IChannel channel : getInternalServer().getChannels().values()) {
				informations.add(channel.getName());
				informations.add(channel.getPlayers().size());

				for (IPlayer player : channel.getPlayers())
					informations.add(player.getName());
			}
			return event.getRequest().answer(informations.toArray());
		case ADD:
			String addChannelName = (String) event.getRequest().getPayload()[0];
			boolean canAddChannel = getInternalServer().getChannels().get(addChannelName) == null;
			if (canAddChannel) {
				getInternalServer().addChannel(addChannelName);
				return event.getRequest().answer(addChannelName);
			} else
				return MumbleMessageFactory.answer(event.getRequest(), ErrorCode.CHANNEL_ALREADY_EXISTS);
		case REMOVE:
			String removeChannelName = (String) event.getRequest().getPayload()[0];
			boolean canRemoveChannel = getInternalServer().getChannels().get(removeChannelName) != null;
			if (canRemoveChannel) {
				getInternalServer().removeChannel(removeChannelName);
				return event.getRequest().answer(removeChannelName);
			} else
				return MumbleMessageFactory.answer(event.getRequest(), ErrorCode.CHANNEL_DOES_NOT_EXISTS);
		case SET:
			String oldName = (String) event.getRequest().getPayload()[0];
			String newName = (String) event.getRequest().getPayload()[1];
			try {
				getInternalServer().renameChannel(oldName, newName);
				return event.getRequest().answer(oldName, newName);
			} catch (ChannelAlreadyExistException | ChannelNotRegisteredException e) {
				return MumbleMessageFactory.answer(event.getRequest(), ErrorCode.UNEXPECTED_ERROR);
			}
		default:
			return MumbleMessageFactory.answer(event.getRequest(), ErrorCode.INCOMPATIBLE_IDC_OID);
		}
	}
}
