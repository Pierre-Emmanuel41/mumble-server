package fr.pederobien.mumble.server.persistence.loaders;

import org.w3c.dom.Element;

import fr.pederobien.mumble.server.impl.InternalServer;
import fr.pederobien.mumble.server.impl.modifiers.RangeParameter;
import fr.pederobien.mumble.server.interfaces.IChannel;
import fr.pederobien.mumble.server.interfaces.IParameter;
import fr.pederobien.mumble.server.persistence.EMumbleXmlTag;

public class MumbleSerializerV10 extends AbstractXmlMumbleSerializer {

	public MumbleSerializerV10() {
		super(1.0);
	}

	@Override
	public boolean deserialize(InternalServer element, Element root) {
		// Setting server's communication port.
		setPort(element, root);

		// Setting server's channels
		setChannels(element, root);

		return true;
	}

	@Override
	public boolean serialize(InternalServer element, Element root) {
		Element port = createElement(EMumbleXmlTag.PORT);
		port.appendChild(createTextNode("" + element.getPort()));
		root.appendChild(port);

		Element channels = createElement(EMumbleXmlTag.CHANNELS);
		for (IChannel c : element.getChannels()) {
			Element channel = createElement(EMumbleXmlTag.CHANNEL);
			setAttribute(channel, EMumbleXmlTag.NAME, c.getName());

			Element soundModifier = createElement(EMumbleXmlTag.SOUND_MODIFIER);
			setAttribute(soundModifier, EMumbleXmlTag.NAME, c.getSoundModifier().getName());
			channel.appendChild(soundModifier);

			Element parameters = createElement(EMumbleXmlTag.PARAMETERS);
			for (IParameter<?> param : c.getSoundModifier().getParameters()) {
				Element parameter = createElement(EMumbleXmlTag.PARAMETER);
				setAttribute(parameter, EMumbleXmlTag.NAME, param.getName());

				Element type = createElement(EMumbleXmlTag.TYPE);
				setAttribute(type, EMumbleXmlTag.NAME, param.getType().toString());
				type.appendChild(createTextNode("" + param.getType().getCode()));
				parameter.appendChild(type);

				Element defaultValue = createElement(EMumbleXmlTag.DEFAULT_VALUE);
				defaultValue.appendChild(createTextNode("" + param.getDefaultValue()));
				parameter.appendChild(defaultValue);

				Element value = createElement(EMumbleXmlTag.VALUE);
				value.appendChild(createTextNode("" + param.getValue()));
				parameter.appendChild(value);

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
