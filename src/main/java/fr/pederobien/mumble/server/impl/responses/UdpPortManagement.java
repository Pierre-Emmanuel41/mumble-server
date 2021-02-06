package fr.pederobien.mumble.server.impl.responses;

import fr.pederobien.messenger.interfaces.IMessage;
import fr.pederobien.mumble.common.impl.ErrorCode;
import fr.pederobien.mumble.common.impl.Header;
import fr.pederobien.mumble.common.impl.MumbleMessageFactory;
import fr.pederobien.mumble.server.event.RequestEvent;
import fr.pederobien.mumble.server.impl.InternalServer;

public class UdpPortManagement extends AbstractManagement {

	public UdpPortManagement(InternalServer internalServer) {
		super(internalServer);
	}

	@Override
	public IMessage<Header> apply(RequestEvent event) {
		switch (event.getRequest().getHeader().getOid()) {
		case GET:
			return event.getRequest().answer(getInternalServer().getUdpPort());
		default:
			return MumbleMessageFactory.answer(event.getRequest(), ErrorCode.INCOMPATIBLE_IDC_OID);
		}
	}
}
