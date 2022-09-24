package fr.pederobien.mumble.server.persistence.loaders;

import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

import fr.pederobien.mumble.common.impl.ParameterType;
import fr.pederobien.mumble.server.exceptions.MumbleServerTypeDismatchException;
import fr.pederobien.mumble.server.impl.AbstractMumbleServer;
import fr.pederobien.mumble.server.impl.SoundManager;
import fr.pederobien.mumble.server.impl.modifiers.Parameter;
import fr.pederobien.mumble.server.impl.modifiers.ParameterList;
import fr.pederobien.mumble.server.impl.modifiers.RangeParameter;
import fr.pederobien.mumble.server.interfaces.IChannel;
import fr.pederobien.mumble.server.persistence.EMumbleXmlTag;
import fr.pederobien.persistence.impl.xml.AbstractXmlSerializer;

public abstract class AbstractXmlMumbleSerializer<T extends AbstractMumbleServer> extends AbstractXmlSerializer<T> {
	protected static final String SIMPLE_MUMBLE_SERVER = "simple";
	protected static final String STANDALONE_MUMBLE_SERVER = "standalone";

	protected AbstractXmlMumbleSerializer(Double version) {
		super(version);
	}

	/**
	 * Update the list of channels of the mumble server.
	 * 
	 * @param element The element to update.
	 * @param root    The xml root that contains all server's channel.
	 */
	protected void setChannels(T element, Element root) {
		PendingChannelManager pendingChannelManager = new PendingChannelManager();

		NodeList channels = getElementsByTagName(root, EMumbleXmlTag.CHANNEL);
		for (int i = 0; i < channels.getLength(); i++) {
			Element channel = (Element) channels.item(i);
			String channelName = getStringAttribute(channel, EMumbleXmlTag.NAME);

			Element soundModifier = (Element) getElementsByTagName(channel, EMumbleXmlTag.SOUND_MODIFIER).item(0);
			String soundModifierName = getStringAttribute(soundModifier, EMumbleXmlTag.NAME);

			NodeList parameters = getElementsByTagName(channel, EMumbleXmlTag.PARAMETER);
			ParameterList parameterList = new ParameterList();
			for (int j = 0; j < parameters.getLength(); j++) {
				Element parameter = (Element) parameters.item(j);
				String parameterName = getStringAttribute(parameter, EMumbleXmlTag.NAME);

				Element type = (Element) getElementsByTagName(parameter, EMumbleXmlTag.TYPE).item(0);
				int code = getIntNodeValue(type.getChildNodes().item(0));

				Element defaultValue = (Element) getElementsByTagName(parameter, EMumbleXmlTag.DEFAULT_VALUE).item(0);
				String parameterDefaultValue = defaultValue.getChildNodes().item(0).getNodeValue();

				Element value = (Element) getElementsByTagName(parameter, EMumbleXmlTag.VALUE).item(0);
				String parameterValue = value.getChildNodes().item(0).getNodeValue();

				Element range = (Element) getElementsByTagName(parameter, EMumbleXmlTag.RANGE).item(0);
				if (range != null) {
					String min = getStringAttribute(range, EMumbleXmlTag.RANGE_MIN);
					String max = getStringAttribute(range, EMumbleXmlTag.RANGE_MAX);
					parameterList.add(RangeParameter.fromType(ParameterType.fromCode(code), parameterName, parameterDefaultValue, parameterValue, min, max));
				} else {
					parameterList.add(Parameter.fromType(ParameterType.fromCode(code), parameterName, parameterDefaultValue, parameterValue));
				}
			}

			IChannel ch = element.getChannels().add(channelName, SoundManager.DEFAULT_SOUND_MODIFIER_NAME);
			pendingChannelManager.register(ch, soundModifierName, parameterList);
		}
	}

	/**
	 * Check if the type found in the server configuration file correspond to the type of the running server.
	 * 
	 * @param root The xml root that contains the server configuration.
	 * @param type The type of the running server.
	 */
	protected void checkServerType(Element root, String type) {
		Element typeElement = (Element) getElementsByTagName(root, EMumbleXmlTag.TYPE).item(0);
		String serverType = typeElement.getChildNodes().item(0).getNodeValue();
		if (!serverType.equalsIgnoreCase(type))
			throw new MumbleServerTypeDismatchException(serverType, type);
	}
}
