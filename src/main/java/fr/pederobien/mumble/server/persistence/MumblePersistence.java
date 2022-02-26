package fr.pederobien.mumble.server.persistence;

import java.io.FileNotFoundException;

import fr.pederobien.mumble.server.impl.InternalServer;
import fr.pederobien.mumble.server.impl.SoundManager;
import fr.pederobien.mumble.server.persistence.loaders.MumbleSerializerV10;
import fr.pederobien.persistence.exceptions.ExtensionException;
import fr.pederobien.persistence.impl.Persistences;
import fr.pederobien.persistence.impl.xml.XmlPersistence;
import fr.pederobien.persistence.interfaces.IPersistence;

public class MumblePersistence {
	private String path;
	private XmlPersistence<InternalServer> persistence;
	private boolean loadingSucceed;

	/**
	 * Creates a new persistence for the mumble server configuration.
	 * 
	 * @param path The folder that contains the server configuration file.
	 */
	public MumblePersistence(String path) {
		this.path = path;
		persistence = Persistences.xmlPersistence();
		persistence.register(persistence.adapt(new MumbleSerializerV10()));
	}

	/**
	 * Load the file associated to the given path, and update the element properties.
	 * 
	 * @param element The element that contains data registered in the configuration file.
	 * 
	 * @throws ExtensionException If the extension associated to the file to deserialize does not match with the extension of this
	 *                            persistence.
	 */
	public void deserialize(InternalServer element) {
		try {
			persistence.deserialize(element, path);
			loadingSucceed = true;
		} catch (Exception e) {
			loadingSucceed = e instanceof FileNotFoundException;
			element.getChannels().add("Welcome", SoundManager.DEFAULT_SOUND_MODIFIER_NAME);
			element.setPort(28000);
		}
	}

	/**
	 * Save the element properties in a file associated to the specified path. If the path does not end with the extension associated
	 * to this persistence it is automatically added. If some intermediate directories are missing they are automatically created.
	 * 
	 * @param element the element that contains informations to save.
	 */
	public void serialize(InternalServer element) {
		if (!loadingSucceed)
			return;

		try {
			persistence.serialize(element, IPersistence.LATEST, path);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	/**
	 * @return The folder that contains the server configuration file.
	 */
	public String getPath() {
		return path;
	}
}
