package fr.pederobien.mumble.server.impl.modifiers;

import fr.pederobien.mumble.server.interfaces.IChannel;
import fr.pederobien.mumble.server.interfaces.IParameter;
import fr.pederobien.mumble.server.interfaces.IPlayer;
import fr.pederobien.mumble.server.interfaces.ISoundModifier;
import fr.pederobien.vocal.common.impl.VolumeResult;

public class SoundModifier implements ISoundModifier {
	public static final String FEEDBACK_PARAMETER_NAME = "Feedback";
	private String name;
	private ParameterList parameters;
	private IChannel channel;
	private IParameter<Boolean> feedbackParameter;

	/**
	 * Creates a sound modifier associated to a name. Classes that inherits this class should also override the protected constructor
	 * in order to clone. The method {@link #calculate(IPlayer, IPlayer)} is the default implementation that try to evaluate the sound
	 * volume between two players. The method can be override, but it is preferable to override the method
	 * {@link #dispatch(IPlayer, IPlayer)}.
	 * 
	 * @param name The sound volume name.
	 */
	public SoundModifier(String name) {
		this.name = name;
		parameters = new ParameterList();
		parameters.add(feedbackParameter = Parameter.of(this, FEEDBACK_PARAMETER_NAME, false));
	}

	/**
	 * Protected constructor for method clone.
	 * 
	 * @param original The original sound modifier to clone.
	 */
	@SuppressWarnings("unchecked")
	protected SoundModifier(SoundModifier original) {
		this.name = original.getName();
		this.parameters = original.getParameters().clone();
		this.channel = original.getChannel();

		for (IParameter<?> parameter : parameters)
			((Parameter<?>) parameter).setSoundModifier(this);

		feedbackParameter = (IParameter<Boolean>) parameters.get(FEEDBACK_PARAMETER_NAME).get();
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
	public boolean sendFeedback() {
		return feedbackParameter.getValue();
	}

	@Override
	public void setSendFeedback(boolean sendFeedback) {
		feedbackParameter.setValue(sendFeedback);
	}

	@Override
	public VolumeResult calculate(IPlayer transmitter, IPlayer receiver) {
		if (transmitter.equals(receiver))
			return sendFeedback() ? dispatch(transmitter, receiver) : VolumeResult.NONE;
		return dispatch(transmitter, receiver);
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;

		if (obj == null || !(obj instanceof ISoundModifier))
			return false;

		ISoundModifier other = (ISoundModifier) obj;
		return getName().equals(other.getName()) && getChannel().equals(other.getChannel());
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

	/**
	 * Calculate the left audio channel volume, the right audio channel volume and the signal global volume.
	 * 
	 * @param transmitter The player currently speaking.
	 * @param receiver    The player currently hearing.
	 * 
	 * @return The result.
	 */
	protected VolumeResult dispatch(IPlayer transmitter, IPlayer receiver) {
		return VolumeResult.DEFAULT;
	}
}
