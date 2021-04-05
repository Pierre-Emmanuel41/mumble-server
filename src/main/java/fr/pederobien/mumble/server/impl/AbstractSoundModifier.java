package fr.pederobien.mumble.server.impl;

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

	private static class DefaultSoundModifier extends AbstractSoundModifier {

		public DefaultSoundModifier() {
			super("default");
		}

		@Override
		public VolumeResult calculate(IPlayer transmitter, IPlayer receiver) {
			return new VolumeResult(1.0, 1.0, 1.0);
		}
	}
}
