package fr.pederobien.mumble.server.persistence;

import java.io.FileNotFoundException;
import java.nio.file.FileSystems;

import fr.pederobien.mumble.server.impl.AbstractMumbleServer;
import fr.pederobien.persistence.exceptions.ExtensionException;
import fr.pederobien.persistence.impl.Persistences;
import fr.pederobien.persistence.impl.xml.XmlPersistence;
import fr.pederobien.persistence.interfaces.IPersistence;

public abstract class AbstractMumblePersistence<T extends AbstractMumbleServer> {
	private String path;
	private XmlPersistence<T> persistence;
	private boolean loadingSucceed;

	/**
	 * Creates a new persistence for the mumble server configuration.
	 * 
	 * @param path The folder that contains the server configuration file.
	 */
	protected AbstractMumblePersistence(String path) {
		this.path = path;
		persistence = Persistences.xmlPersistence();
	}

	/**
	 * Load the file associated to the given path, and update the element properties.
	 * 
	 * @param element The element that contains data registered in the configuration file.
	 * 
	 * @throws ExtensionException If the extension associated to the file to deserialize does not match with the extension of this
	 *                            persistence.
	 */
	public void deserialize(T element) {
		try {
			String filePath = path.concat(String.format("%s%s%s", FileSystems.getDefault().getSeparator(), element.getName(), persistence.getExtension()));
			persistence.deserialize(element, filePath);
			loadingSucceed = true;
		} catch (Exception e) {
			loadingSucceed = e instanceof FileNotFoundException;
			if (!loadingSucceed)
				e.printStackTrace();
			onFailToDeserialize(element, loadingSucceed);
		}
	}

	/**
	 * Save the element properties in a file associated to the specified path. If the path does not end with the extension associated
	 * to this persistence it is automatically added. If some intermediate directories are missing they are automatically created.
	 * 
	 * @param element the element that contains informations to save.
	 */
	public void serialize(T element) {
		if (!loadingSucceed)
			return;

		try {
			String filePath = path.concat(String.format("%s%s%s", FileSystems.getDefault().getSeparator(), element.getName(), persistence.getExtension()));
			persistence.serialize(element, IPersistence.LATEST, filePath);
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

	/**
	 * @return The underlying persistence that store the server configuration in XML format.
	 */
	public XmlPersistence<T> getPersistence() {
		return persistence;
	}

	/**
	 * Method called when the deserialization fails.
	 * 
	 * @param loadingSucceed True if the exception thrown during deserialization is a {@link FileNotFoundException}, false otherwise.
	 */
	protected abstract void onFailToDeserialize(T element, boolean loadingSucceed);
}
