package ninjabrainbot.calculator;

import ninjabrainbot.Main;

/**
 * Represents an eye of ender throw.
 */
public class Throw implements IThrow {
	private final double x, z, alpha_0, beta, correction;
	private final boolean manualInput, altStd;

	private final boolean nether;
	
	public Throw(double x, double z, double alpha, double beta, double correction, boolean nether) {
		this(x, z, alpha, beta, correction, false, nether);
	}
	
	public Throw(double x, double z, double alpha, double beta, double correction, boolean altStd, boolean nether) {
		this(x, z, alpha, beta, correction, altStd, nether, false);
	}

	public Throw(double x, double z, double alpha, double beta, double correction, boolean altStd, boolean nether, boolean manualInput) {
		this.x = x;
		this.z = z;
		this.correction = correction;
		alpha = alpha % 360.0;
		if (alpha < -180.0) {
			alpha += 360.0;
		} else if (alpha > 180.0) {
			alpha -= 360.0;
		}
		this.alpha_0 = alpha;
		this.beta = beta;
		this.altStd = altStd;
		this.nether = nether;
		this.manualInput = manualInput;
	}

	@Override
	public String toString() {
		return "x=" + x + ", z=" + z + ", alpha=" + alpha_0;
	}

	/**
	 * Returns a Throw object if the given string is the result of an F3+C command
	 * in the overworld, null otherwise.
	 */
	public static Throw parseF3C(String string) {
		if (!(string.startsWith("/execute in minecraft:overworld run tp @s") ||
				string.startsWith("/execute in minecraft:the_nether run tp @s"))) {
			return parseF3COneTwelve(string);
		}
		String[] substrings = string.split(" ");
		if (substrings.length != 11)
			return null;
		try {
			boolean nether = substrings[2].equals("minecraft:the_nether");
			double x = Double.parseDouble(substrings[6]);
			double z = Double.parseDouble(substrings[8]);
			double alpha = Double.parseDouble(substrings[9]);
			double beta = Double.parseDouble(substrings[10]);
			alpha += Main.preferences.crosshairCorrection.get();
			return new Throw(x, z, alpha, beta, 0, nether);
		} catch (NullPointerException | NumberFormatException e) {
			return null;
		}
	}
	
	private static Throw parseF3COneTwelve(String string) {
		String[] substrings = string.split(" ");
		if (substrings.length != 3)
			return null;
		try {
			double x = Double.parseDouble(substrings[0]) + 0.5; // Add 0.5 because block coords should be used
			double z = Double.parseDouble(substrings[1]) + 0.5; // Add 0.5 because block coords should be used
			double alpha = Double.parseDouble(substrings[2]);
			alpha += Main.preferences.crosshairCorrection.get();
			return new Throw(x, z, alpha, -31, 0, false, false, true);
		} catch (NullPointerException | NumberFormatException e) {
			return null;
		}
	}

	@Override
	public Throw withAddedCorrection(double delta)
	{
		return new Throw(x, z, alpha_0, beta, correction + delta, altStd, isNether(), manualInput);
	}

	@Override
	public Throw withToggledSTD() {
		return new Throw(x, z, alpha_0, beta, correction, !this.altStd, this.nether, this.manualInput);
	}
	
	public boolean lookingBelowHorizon() {
		return beta > 0;
	}
	
	@Override
	public double x() {
		return x;
	}

	@Override
	public double z() {
		return z;
	}

	@Override
	public boolean altStd() { return altStd; }

	@Override
	public boolean manualInput() { return manualInput; }

	@Override
	public double alpha_0() {
		return alpha_0;
	}

	@Override
	public double alpha() {
		return alpha_0 + correction;
	}

	@Override
	public double beta() { return beta; }
	public double correction() { return correction; }

	@Override
	public double getStd(StdSettings stds) {
		if (manualInput) return stds.sigmaManual;
		return altStd ? stds.sigmaAlt : stds.sigma;
	}


	public boolean isNether() {
		return nether;
	}

	public BlindPosition toBlind() {
		return new BlindPosition(x, z);
	}
}
