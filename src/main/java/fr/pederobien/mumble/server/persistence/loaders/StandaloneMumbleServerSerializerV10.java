package fr.pederobien.mumble.server.persistence.loaders;

import org.w3c.dom.Element;
import org.w3c.dom.Node;

import fr.pederobien.mumble.server.impl.StandaloneMumbleServer;
import fr.pederobien.mumble.server.impl.modifiers.RangeParameter;
import fr.pederobien.mumble.server.interfaces.IChannel;
import fr.pederobien.mumble.server.interfaces.IParameter;
import fr.pederobien.mumble.server.persistence.EMumbleXmlTag;

public class StandaloneMumbleServerSerializerV10 extends AbstractXmlMumbleSerializer<StandaloneMumbleServer> {

	/**
	 * Creates a serializer with version 1.0 in order to serialize or deserialize server configuration.
	 */
	public StandaloneMumbleServerSerializerV10() {
		super(1.0);
	}

	@Override
	public boolean deserialize(StandaloneMumbleServer element, Element root) {
		checkServerType(root, STANDALONE_MUMBLE_SERVER);

		// Set the mumble port number and the external game server port number.
		Node port = getElementsByTagName(root, EMumbleXmlTag.PORT).item(0);
		element.setConfigurationPort(getIntAttribute((Element) port, EMumbleXmlTag.CONFIGURATION_PORT));
		element.setVocalPort(getIntAttribute((Element) port, EMumbleXmlTag.VOCAL_PORT));
		element.setGamePort(getIntAttribute((Element) port, EMumbleXmlTag.GAME_PORT));

		// Set the channels
		setChannels(element, root);
		return false;
	}

	@Override
	public boolean serialize(StandaloneMumbleServer element, Element root) {
		// Simple mumble server
		Element serverType = createElement(EMumbleXmlTag.TYPE);
		serverType.appendChild(createTextNode(STANDALONE_MUMBLE_SERVER));
		root.appendChild(serverType);

		// Server's port numbers
		Element port = createElement(EMumbleXmlTag.PORT);
		setAttribute(port, EMumbleXmlTag.CONFIGURATION_PORT, element.getConfigurationPort());
		setAttribute(port, EMumbleXmlTag.GAME_PORT, element.getGamePort());
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
