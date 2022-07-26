package fr.pederobien.mumble.server.persistence;

import fr.pederobien.mumble.server.impl.SimpleMumbleServer;
import fr.pederobien.mumble.server.impl.SoundManager;
import fr.pederobien.mumble.server.persistence.loaders.SimpleMumbleSerializerV10;

public class SimpleMumbleServerPersistence extends AbstractMumblePersistence<SimpleMumbleServer> {

	/**
	 * Creates a persistence responsible to serialize or deserialize a server configuration file. The configuration file should
	 * correspond to a simple server configuration.
	 * 
	 * @param path The folder that contains the server configuration file.
	 */
	public SimpleMumbleServerPersistence(String path) {
		super(path);
		getPersistence().register(getPersistence().adapt(new SimpleMumbleSerializerV10()));
	}

	@Override
	protected void onFailToDeserialize(SimpleMumbleServer element, boolean loadingSucceed) {
		element.getChannels().add("Welcome", SoundManager.DEFAULT_SOUND_MODIFIER_NAME);
		element.setConfigurationPort(28000);
		element.setVocalPort(28100);
	}
}
