package fr.pederobien.mumble.server.impl.modifiers;

import fr.pederobien.mumble.server.interfaces.IPlayer;

public class LinearCircularSoundModifier extends AbstractSoundModifier {
	private double slope;

	public LinearCircularSoundModifier(double radius) {
		super(String.format("LinearCircular_%s", radius < 0 ? -radius : radius));
		slope = -1.0 / (radius < 0 ? -radius : radius);
	}

	@Override
	public VolumeResult calculate(IPlayer transmitter, IPlayer receiver) {
		double distance = getDistance3D(transmitter.getPosition(), receiver.getPosition());
		double[] volumes = getDefaultLeftAndRightVolume(transmitter.getPosition(), receiver.getPosition());
		return new VolumeResult(slope * distance, volumes[0], volumes[1]);
	}
}
