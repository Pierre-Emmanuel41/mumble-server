package fr.pederobien.mumble.server.persistence.loaders;

import org.w3c.dom.Element;
import org.w3c.dom.Node;

import fr.pederobien.mumble.server.impl.SimpleMumbleServer;
import fr.pederobien.mumble.server.impl.modifiers.RangeParameter;
import fr.pederobien.mumble.server.interfaces.IChannel;
import fr.pederobien.mumble.server.interfaces.IParameter;
import fr.pederobien.mumble.server.persistence.EMumbleXmlTag;

public class SimpleMumbleSerializerV10 extends AbstractXmlMumbleSerializer<SimpleMumbleServer> {

	public SimpleMumbleSerializerV10() {
		super(1.0);
	}

	@Override
	public boolean deserialize(SimpleMumbleServer element, Element root) {
		checkServerType(root, SIMPLE_MUMBLE_SERVER);

		Node port = getElementsByTagName(root, EMumbleXmlTag.PORT).item(0);
		element.setConfigurationPort(getIntAttribute((Element) port, EMumbleXmlTag.CONFIGURATION_PORT));
		element.setVocalPort(getIntAttribute((Element) port, EMumbleXmlTag.VOCAL_PORT));

		// Setting server's channels
		setChannels(element, root);
		return true;
	}

	@Override
	public boolean serialize(SimpleMumbleServer element, Element root) {
		// Simple mumble server
		Element serverType = createElement(EMumbleXmlTag.TYPE);
		serverType.appendChild(createTextNode(SIMPLE_MUMBLE_SERVER));
		root.appendChild(serverType);

		// Mumble port number
		Element port = createElement(EMumbleXmlTag.PORT);
		setAttribute(port, EMumbleXmlTag.CONFIGURATION_PORT, element.getConfigurationPort());
		setAttribute(port, EMumbleXmlTag.VOCAL_PORT, element.getVocalPort());
		root.appendChild(port);

		Element channels = createElement(EMumbleXmlTag.CHANNELS);
		for (IChannel c : element.getChannels()) {
			// Channel's name
			Element channel = createElement(EMumbleXmlTag.CHANNEL);
			setAttribute(channel, EMumbleXmlTag.NAME, c.getName());

			// Sound modifier's name
			Element soundModifier = createElement(EMumbleXmlTag.SOUND_MODIFIER);
			setAttribute(soundModifier, EMumbleXmlTag.NAME, c.getSoundModifier().getName());
			channel.appendChild(soundModifier);

			Element parameters = createElement(EMumbleXmlTag.PARAMETERS);
			for (IParameter<?> param : c.getSoundModifier().getParameters()) {
				// Parameter's name
				Element parameter = createElement(EMumbleXmlTag.PARAMETER);
				setAttribute(parameter, EMumbleXmlTag.NAME, param.getName());

				// Parameter's type
				Element type = createElement(EMumbleXmlTag.TYPE);
				setAttribute(type, EMumbleXmlTag.NAME, param.getType().toString());
				type.appendChild(createTextNode("" + param.getType().getCode()));
				parameter.appendChild(type);

				// Parameter's default value
				Element defaultValue = createElement(EMumbleXmlTag.DEFAULT_VALUE);
				defaultValue.appendChild(createTextNode("" + param.getDefaultValue()));
				parameter.appendChild(defaultValue);

				// Parameter's value
				Element value = createElement(EMumbleXmlTag.VALUE);
				value.appendChild(createTextNode("" + param.getValue()));
				parameter.appendChild(value);

				// Parameter's range
				if (param instanceof RangeParameter) {
					RangeParameter<?> rangeParameter = (RangeParameter<?>) param;
					Element range = createElement(EMumbleXmlTag.RANGE);
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
		return true;
	}
}
