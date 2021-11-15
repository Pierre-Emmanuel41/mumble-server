package fr.pederobien.mumble.server.impl.modifiers;

import fr.pederobien.mumble.server.impl.MathHelper;
import fr.pederobien.mumble.server.interfaces.IPlayer;

public class LinearCircularSoundModifier extends AbstractSoundModifier {
	private double slope;

	public LinearCircularSoundModifier(double radius) {
		super(String.format("LinearCircular_%s", radius < 0 ? -radius : radius));
		slope = -1.0 / (radius < 0 ? -radius : radius);
	}

	@Override
	public VolumeResult calculate(IPlayer transmitter, IPlayer receiver) {
		if (transmitter.equals(receiver))
			return VolumeResult.DEFAULT;
		
		double distance = MathHelper.getDistance3D(transmitter.getPosition(), receiver.getPosition());
		double[] volumes = MathHelper.getDefaultLeftAndRightVolume(transmitter.getPosition(), receiver.getPosition());
		return new VolumeResult(slope * distance + 1, volumes[0], volumes[1]);
	}
}
