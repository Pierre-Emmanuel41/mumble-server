package fr.pederobien.mumble.server.persistence.loaders;

import java.util.Optional;

import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

import fr.pederobien.mumble.server.impl.Channel;
import fr.pederobien.mumble.server.interfaces.IChannel;
import fr.pederobien.mumble.server.interfaces.IMumbleServer;
import fr.pederobien.mumble.server.interfaces.ISoundModifier;
import fr.pederobien.mumble.server.persistence.EMumbleXmlTag;
import fr.pederobien.persistence.impl.xml.AbstractXmlPersistenceLoader;

public abstract class AbstractMumbleLoader extends AbstractXmlPersistenceLoader<IMumbleServer> {
	private IMumbleServer mumbleServer;

	protected AbstractMumbleLoader(Double version, IMumbleServer mumbleServer) {
		super(version);
		this.mumbleServer = mumbleServer;
	}

	@Override
	protected IMumbleServer create() {
		return mumbleServer;
	}

	/**
	 * Update the list of channels of the mumble server.
	 * 
	 * @param root The xml root that contains all server's channel.
	 */
	protected void setChannels(Element root) {
		NodeList channels = getElementsByTagName(root, EMumbleXmlTag.CHANNEL);
		for (int i = 0; i < channels.getLength(); i++) {
			Element channel = (Element) channels.item(i);
			IChannel c = new Channel(getStringAttribute(channel, EMumbleXmlTag.CHANNEL_NAME));
			Optional<ISoundModifier> soundModifier = get().getSoundManager().getByName(getStringAttribute(channel, EMumbleXmlTag.SOUND_MODIFIER_NAME));
			if (soundModifier.isPresent())
				c.setSoundModifier(soundModifier.get());
		}
	}
}
