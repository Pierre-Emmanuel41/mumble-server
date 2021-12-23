package fr.pederobien.mumble.server.persistence;

import java.nio.file.Path;

import fr.pederobien.mumble.server.impl.InternalServer;
import fr.pederobien.mumble.server.persistence.loaders.MumbleSerializerV10;
import fr.pederobien.persistence.exceptions.ExtensionException;
import fr.pederobien.persistence.impl.Persistences;
import fr.pederobien.persistence.impl.xml.XmlPersistence;
import fr.pederobien.persistence.interfaces.IPersistence;

public class MumblePersistence {
	private XmlPersistence<InternalServer> persistence;

	/**
	 * Creates a new persistence for the mumble server configuration.
	 */
	public MumblePersistence() {
		persistence = Persistences.xmlPersistence();
		persistence.register(persistence.adapt(new MumbleSerializerV10()));
	}

	/**
	 * Load the file associated to the given path, and update the element properties.
	 * 
	 * @param element The element that contains data registered in the configuration file.
	 * @param path    The path leading to the configuration file. It should contains the file name and the extension.
	 * 
	 * @return True if the element has been successfully updated, false otherwise.
	 * 
	 * @throws ExtensionException If the extension associated to the file to deserialize does not match with the extension of this
	 *                            persistence.
	 */
	public boolean deserialize(InternalServer element, Path path) {
		return persistence.deserialize(element, path.toString());
	}

	/**
	 * Save the element properties in a file associated to the specified path. If the path does not end with the extension associated
	 * to this persistence it is automatically added. If some intermediate directories are missing they are automatically created.
	 * 
	 * @param element the element that contains informations to save.
	 * @param path    The path leading to the configuration file. It should contains the file name.
	 * 
	 * @return True if the save went well.
	 */
	public boolean serialize(InternalServer element, Path path) {
		return persistence.serialize(element, IPersistence.LATEST, path.toString());
	}
}
