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
		private double left, right, global;

		public VolumeResult(double left, double right, double global) {
			this.left = left;
			this.right = right;
			this.global = global;
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

		/**
		 * @return The global volume for the left and right channel.
		 */
		public double getGlobal() {
			return global;
		}
	}
}
