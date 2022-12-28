package ninjabrainbot.calculator;

public interface IRay {
	
	public double x();
	public double z();
	public double alpha();

	/**
	 * Returns the squared distance between this throw and the given throw.
	 */
	public default double distance2(IRay other) {
		double dx = x() - other.x();
		double dz = z() - other.z();
		return dx * dx + dz * dz;
	}
}
