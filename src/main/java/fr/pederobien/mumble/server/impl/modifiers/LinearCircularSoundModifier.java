package fr.pederobien.mumble.server.impl.modifiers;

import fr.pederobien.mumble.server.impl.MathHelper;
import fr.pederobien.mumble.server.interfaces.IParameter;
import fr.pederobien.mumble.server.interfaces.IPlayer;
import fr.pederobien.vocal.common.impl.VolumeResult;

public class LinearCircularSoundModifier extends SoundModifier {
	public static final String RADIUS_PARAMETER_NAME = "Radius";
	private IParameter<Double> radiusParameter;

	/**
	 * Creates a linear circular sound modifier. The first adjective <code>linear</code> refers how the global sample volume will
	 * evolve according to the distance between two players. The second adjective <code>circular</code> refers how the stereo will
	 * evolve according to the yaw between the two players. For this sound modifier, the stereo depends only on the yaw between the
	 * receiving player and the transmitting player the yaw between the transmitting player and the receiving player has no influence
	 * on the stereo. The default radius is 50 which means that from a distance of 0 to a distance of 50 the global sample volume will
	 * decrease linearly from 1 (maximum volume) to 0. A {@link Parameter} for the radius is created associated to the name
	 * {@value #RADIUS_PARAMETER_NAME}.
	 */
	public LinearCircularSoundModifier() {
		super("LinearCircular");
		getParameters().add(radiusParameter = RangeParameter.of(this, RADIUS_PARAMETER_NAME, 50.0, 1.0, Double.MAX_VALUE));
	}

	/**
	 * Private constructor for method clone.
	 * 
	 * @param original The original sound modifier to clone.
	 */
	private LinearCircularSoundModifier(LinearCircularSoundModifier original) {
		super(original);
		this.radiusParameter = getParameters().getParameter(RADIUS_PARAMETER_NAME);
	}

	@Override
	protected VolumeResult dispatch(IPlayer transmitter, IPlayer receiver) {
		double distance = MathHelper.getDistance3D(transmitter.getPosition(), receiver.getPosition());
		double[] volumes = MathHelper.getDefaultLeftAndRightVolume(transmitter.getPosition(), receiver.getPosition());
		return new VolumeResult((-1.0 / radiusParameter.getValue()) * distance + 1, volumes[0], volumes[1]);
	}

	@Override
	public LinearCircularSoundModifier clone() {
		return new LinearCircularSoundModifier(this);
	}

	/**
	 * @return The radius beyond which two players can no longer talk together.
	 */
	public double getRadius() {
		return radiusParameter.getValue();
	}

	/**
	 * Set the radius beyond which two players can no longer talk together.
	 * 
	 * @param radius The new radius.
	 */
	public void setRadius(double radius) {
		radiusParameter.setValue(radius);
	}
}
