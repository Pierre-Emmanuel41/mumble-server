package fr.pederobien.mumble.server.persistence.loaders;

import org.w3c.dom.Element;

import fr.pederobien.mumble.server.interfaces.IMumbleServer;
import fr.pederobien.persistence.interfaces.xml.IXmlPersistenceLoader;

public class MumbleLoaderV10 extends AbstractMumbleLoader {

	public MumbleLoaderV10(IMumbleServer mumbleServer) {
		super(1.0, mumbleServer);
	}

	@Override
	public IXmlPersistenceLoader<IMumbleServer> load(Element root) {
		createNewElement();

		// Guetting channels
		setChannels(root);
		return this;
	}

}
