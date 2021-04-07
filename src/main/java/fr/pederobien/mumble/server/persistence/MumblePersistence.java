package fr.pederobien.mumble.server.persistence;

import java.io.IOException;
import java.nio.file.Path;

import org.w3c.dom.Document;
import org.w3c.dom.Element;

import fr.pederobien.mumble.server.interfaces.IChannel;
import fr.pederobien.mumble.server.interfaces.IMumbleServer;
import fr.pederobien.mumble.server.persistence.loaders.MumbleLoaderV10;
import fr.pederobien.persistence.impl.xml.AbstractXmlPersistence;

public class MumblePersistence extends AbstractXmlPersistence<IMumbleServer> {
	private static final String ROOT_XML_DOCUMENT = "mumble";

	public MumblePersistence(Path path, IMumbleServer mumbleServer) {
		super(path);
		set(mumbleServer);
		register(new MumbleLoaderV10(mumbleServer));
	}

	@Override
	public boolean save() {
		if (get() == null)
			return false;
		Document doc = newDocument();
		doc.setXmlStandalone(true);

		Element root = createElement(doc, ROOT_XML_DOCUMENT);
		doc.appendChild(root);

		Element version = createElement(doc, VERSION);
		version.appendChild(doc.createTextNode(getVersion().toString()));
		root.appendChild(version);

		Element channels = createElement(doc, EMumbleXmlTag.CHANNELS);
		for (IChannel c : get().getChannels().values()) {
			Element channel = createElement(doc, EMumbleXmlTag.CHANNEL);
			setAttribute(channel, EMumbleXmlTag.CHANNEL_NAME, c.getName());
			setAttribute(channel, EMumbleXmlTag.SOUND_MODIFIER_NAME, c.getSoundModifier().getName());
			channels.appendChild(channel);
		}
		root.appendChild(channels);

		saveDocument(doc, get().getName());
		return true;
	}

	@Override
	protected Document createDoc(Object... objects) throws IOException {
		return parseFromFileName((String) objects[0]);
	}
}
