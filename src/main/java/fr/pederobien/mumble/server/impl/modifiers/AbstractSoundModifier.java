package fr.pederobien.mumble.server.impl.modifiers;

import fr.pederobien.mumble.server.interfaces.IChannel;
import fr.pederobien.mumble.server.interfaces.IParameter;
import fr.pederobien.mumble.server.interfaces.IParameterList;
import fr.pederobien.mumble.server.interfaces.IPlayer;
import fr.pederobien.mumble.server.interfaces.ISoundModifier;

public abstract class AbstractSoundModifier implements ISoundModifier {
	private String name;
	private ParameterList parameters;
	private IChannel channel;

	public AbstractSoundModifier(String name) {
		this.name = name;
		parameters = new ParameterList();
	}

	@Override
	public String getName() {
		return name;
	}

	@Override
	public IParameterList getParameters() {
		return parameters;
	}

	@Override
	public IChannel getChannel() {
		return channel;
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

	@Override
	public ISoundModifier clone() {
		AbstractSoundModifier modifier = new AbstractSoundModifier(getName()) {
			@Override
			public VolumeResult calculate(IPlayer transmitter, IPlayer receiver) {
				return AbstractSoundModifier.this.calculate(transmitter, receiver);
			}
		};
		modifier.parameters = parameters.clone();
		for (IParameter<?> parameter : modifier.parameters)
			((Parameter<?>) parameter).setSoundModifier(modifier);
		return modifier;
	}

	/**
	 * Set the channel associated to this sound modifier.
	 * 
	 * @param channel the new channel.
	 */
	public void setChannel(IChannel channel) {
		this.channel = channel;
	}

	/**
	 * @return The list of registered parameters;
	 */
	protected ParameterList getParametersList() {
		return parameters;
	}
}
