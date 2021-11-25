package fr.pederobien.mumble.server.interfaces;

public interface ISoundModifier extends Cloneable {

	/**
	 * @return The name of this sound modifier.
	 */
	String getName();

	/**
	 * @return The list of parameters associated to this sound modifier.
	 */
	IParameterList getParameters();

	/**
	 * @return The channel associated to this sound modifier.
	 */
	IChannel getChannel();

	/**
	 * Calculate the left audio channel volume, the right audio channel volume and the signal global volume.
	 * 
	 * @param transmitter The player currently speaking.
	 * @param receiver    The player currently hearing.
	 * 
	 * @return The result.
	 */
	VolumeResult calculate(IPlayer transmitter, IPlayer receiver);

	/**
	 * Clone this sound modifier. It creates a new parameter based on the properties of this sound modifier.
	 * 
	 * @return A new sound modifier.
	 */
	ISoundModifier clone();

	public class VolumeResult {
		public static final VolumeResult NONE = new VolumeResult(0);
		public static final VolumeResult DEFAULT = new VolumeResult(1.0, 1.0, 1.0);
		private double global, left, right;

		public VolumeResult(double global, double left, double right) {
			this.global = global < 0 ? 0 : global;
			this.left = left < 0 ? 0 : left;
			this.right = right < 0 ? 0 : right;
		}

		public VolumeResult(double global) {
			this(global, 1.0, 1.0);
		}

		/**
		 * @return The global volume for the left and right channel.
		 */
		public double getGlobal() {
			return global;
		}

		/**
		 * @return The volume for the left audio channel.
		 */
		public double getLeft() {
			return left;
		}

		/**
		 * @return The volume for the right audio channel.
		 */
		public double getRight() {
			return right;
		}
	}
}
