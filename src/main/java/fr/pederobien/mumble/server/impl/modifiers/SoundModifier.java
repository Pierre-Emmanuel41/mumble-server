package fr.pederobien.mumble.server.impl.modifiers;

import java.util.Map;

import fr.pederobien.mumble.server.interfaces.IChannel;
import fr.pederobien.mumble.server.interfaces.IParameter;
import fr.pederobien.mumble.server.interfaces.IPlayer;
import fr.pederobien.mumble.server.interfaces.ISoundModifier;

public abstract class SoundModifier implements ISoundModifier {
	private String name;
	private ParameterList parameters;
	private IChannel channel;

	public SoundModifier(String name) {
		this.name = name;
		parameters = new ParameterList();
	}

	/**
	 * Protected constructor for method clone.
	 * 
	 * @param original The original sound modifier to clone.
	 */
	protected SoundModifier(SoundModifier original) {
		this.name = original.getName();
		this.parameters = original.getParameters().clone();
		this.channel = original.getChannel();

		for (Map.Entry<String, IParameter<?>> entry : parameters)
			((Parameter<?>) entry.getValue()).setSoundModifier(this);
	}

	@Override
	public String getName() {
		return name;
	}

	@Override
	public ParameterList getParameters() {
		return parameters;
	}

	@Override
	public IChannel getChannel() {
		return channel;
	}

	@Override
	public VolumeResult calculate(IPlayer transmitter, IPlayer receiver) {
		return transmitter.equals(receiver) ? VolumeResult.NONE : VolumeResult.DEFAULT;
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
		throw new IllegalStateException("The method clone should be overriden by each sound modifer");
	}

	/**
	 * Set the channel associated to this sound modifier.
	 * 
	 * @param channel the new channel.
	 */
	public void setChannel(IChannel channel) {
		this.channel = channel;
	}
}
