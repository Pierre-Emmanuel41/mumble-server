package fr.pederobien.mumble.server.impl.modifiers;

import fr.pederobien.mumble.server.interfaces.IPlayer;
import fr.pederobien.mumble.server.interfaces.ISoundModifier;

public abstract class AbstractSoundModifier implements ISoundModifier {
	public static final ISoundModifier DEFAULT = new DefaultSoundModifier();
	private String name;

	public AbstractSoundModifier(String name) {
		this.name = name;
	}

	@Override
	public String getName() {
		return name;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;

		if (obj == null || !(obj instanceof ISoundModifier))
			return false;

		ISoundModifier other = (ISoundModifier) obj;
		return getName().equals(other.getName());
	}

	private static class DefaultSoundModifier extends AbstractSoundModifier {

		public DefaultSoundModifier() {
			super("default");
		}

		@Override
		public VolumeResult calculate(IPlayer transmitter, IPlayer receiver) {
			return VolumeResult.DEFAULT;
		}
	}
}
