package fr.pederobien.mumble.server.persistence;

import fr.pederobien.mumble.server.impl.SoundManager;
import fr.pederobien.mumble.server.impl.StandaloneMumbleServer;
import fr.pederobien.mumble.server.persistence.loaders.StandaloneMumbleServerSerializerV10;

public class StandaloneMumbleServerPersistence extends AbstractMumblePersistence<StandaloneMumbleServer> {

	/**
	 * Creates a persistence responsible to serialize or deserialize a server configuration file. The configuration file should
	 * correspond to a standalone server configuration.
	 * 
	 * @param path The folder that contains the server configuration file.
	 */
	public StandaloneMumbleServerPersistence(String path) {
		super(path);
		getPersistence().register(getPersistence().adapt(new StandaloneMumbleServerSerializerV10()));
	}

	@Override
	protected void onFailToDeserialize(StandaloneMumbleServer element, boolean loadingSucceed) {
		element.getChannels().add("Welcome", SoundManager.DEFAULT_SOUND_MODIFIER_NAME);
		element.setConfigurationPort(28000);
		element.setVocalPort(28100);
		element.setGamePort(28200);
	}
}
