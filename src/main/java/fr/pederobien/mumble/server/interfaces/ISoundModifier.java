package fr.pederobien.mumble.server.interfaces;

public interface ISoundModifier {

	/**
	 * @return The name of this sound modifier.
	 */
	String getName();

	/**
	 * Calculate the left audio channel volume, the right audio channel volume and the signal global volume.
	 * 
	 * @param transmitter The player currently speaking.
	 * @param receiver    The player currently hearing.
	 * 
	 * @return The result.
	 */
	VolumeResult calculate(IPlayer transmitter, IPlayer receiver);

	public class VolumeResult {
		public static final VolumeResult DEFAULT = new VolumeResult(1.0, 1.0, 1.0);
		private double global, left, right;

		public VolumeResult(double global, double left, double right) {
			this.global = global < 0 ? 0 : global > 1 ? 1 : global;
			this.left = left < 0 ? 0 : left > 1 ? 1 : left;
			this.right = right < 0 ? 0 : right > 1 ? 1 : right;
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
