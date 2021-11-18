package fr.pederobien.mumble.server.persistence;

import java.io.IOException;
import java.nio.file.Path;

import org.w3c.dom.Document;
import org.w3c.dom.Element;

import fr.pederobien.mumble.server.impl.modifiers.RangeParameter;
import fr.pederobien.mumble.server.interfaces.IChannel;
import fr.pederobien.mumble.server.interfaces.IMumbleServer;
import fr.pederobien.mumble.server.interfaces.IParameter;
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
			setAttribute(channel, EMumbleXmlTag.NAME, c.getName());

			Element soundModifier = createElement(doc, EMumbleXmlTag.SOUND_MODIFIER);
			setAttribute(soundModifier, EMumbleXmlTag.NAME, c.getSoundModifier().getName());
			channel.appendChild(soundModifier);

			Element parameters = createElement(doc, EMumbleXmlTag.PARAMETERS);
			for (IParameter<?> p : c.getSoundModifier().getParameters()) {
				Element parameter = createElement(doc, EMumbleXmlTag.PARAMETER);
				setAttribute(parameter, EMumbleXmlTag.NAME, p.getName());

				Element type = createElement(doc, EMumbleXmlTag.TYPE);
				setAttribute(type, EMumbleXmlTag.NAME, p.getType().toString());
				type.appendChild(doc.createTextNode("" + p.getType().getCode()));
				parameter.appendChild(type);

				Element defaultValue = createElement(doc, EMumbleXmlTag.DEFAULT_VALUE);
				defaultValue.appendChild(doc.createTextNode("" + p.getDefaultValue()));
				parameter.appendChild(defaultValue);

				Element value = createElement(doc, EMumbleXmlTag.VALUE);
				value.appendChild(doc.createTextNode("" + p.getValue()));
				parameter.appendChild(value);

				if (p instanceof RangeParameter) {
					RangeParameter<?> rangeParameter = (RangeParameter<?>) p;
					Element range = createElement(doc, EMumbleXmlTag.RANGE);
					setAttribute(range, EMumbleXmlTag.RANGE_MIN, rangeParameter.getMin());
					setAttribute(range, EMumbleXmlTag.RANGE_MAX, rangeParameter.getMax());
					parameter.appendChild(range);
				}
				parameters.appendChild(parameter);
			}
			soundModifier.appendChild(parameters);
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
