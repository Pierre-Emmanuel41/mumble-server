package fr.pederobien.mumble.server.persistence.loaders;

import org.w3c.dom.Element;

import fr.pederobien.mumble.server.impl.InternalServer;
import fr.pederobien.persistence.interfaces.xml.IXmlPersistenceLoader;

public class MumbleLoaderV10 extends AbstractMumbleLoader {

	public MumbleLoaderV10(InternalServer mumbleServer) {
		super(1.0, mumbleServer);
	}

	@Override
	public IXmlPersistenceLoader<InternalServer> load(Element root) {
		createNewElement();

		// Setting server's communication port.
		setPort(root);

		// Setting server's channels
		setChannels(root);
		return this;
	}

}
