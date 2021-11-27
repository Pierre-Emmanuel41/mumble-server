package fr.pederobien.mumble.server.impl.modifiers;

import fr.pederobien.mumble.server.interfaces.IPlayer;

public class LinearEllipseModifier extends SoundModifier {
	// Major axis
	private double a;

	// Minor axis
	private double b;

	public LinearEllipseModifier(double front, double behind) {
		super(String.format("LinearEllipse_%s_%s", front, behind));
		a = (front + behind) / 2;
		b = Math.sqrt(front * behind);
	}

	@Override
	protected VolumeResult dispatch(IPlayer transmitter, IPlayer receiver) {
		return VolumeResult.DEFAULT;
	}
}
