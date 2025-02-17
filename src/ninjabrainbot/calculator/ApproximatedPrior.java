package ninjabrainbot.calculator;

import ninjabrainbot.util.Coords;

/**
 * A prior that approximates biome snapping as opposed to calculating it
 * exactly, which is costly.
 */
public class ApproximatedPrior extends Prior {

	public ApproximatedPrior() {
		super();
	}

	public ApproximatedPrior(int centerX, int centerZ, int radius, DivineContext divineContext) {
		super(centerX, centerZ, radius, divineContext);
	}
	
	/**
	 * Test the accuracy of of the approximated prior.
	 */
	public void evaluateError() {
		System.out.println("Evaluating approximated prior.");
		System.out.println("Constructing true prior...");
		Prior prior = new Prior();
		System.out.println("Comparing approximation to true prior...");
		double largestRelError = 0;
		double sump = 0;
		double sum = 0;
		int falseNegativeCount = 0;
		// ArrayList<Pair<Double, String>> errors = new ArrayList<>();
		double totalSquaredError = 0;
		int numNonZeroChunks = 0;
		for (int i = 0; i < chunks.length; i++) {
			if (prior.chunks[i].weight != 0) {
				if (chunks[i].weight == 0) {
					System.out.println("x: " + (i % size1d - radius) + ", z: " + (i / size1d - radius));
					System.out.println(prior.chunks[i].weight);
					falseNegativeCount++;
				}
				double relError = chunks[i].weight / prior.chunks[i].weight;
				if (relError < 1f)
					relError = 1f / relError;
				relError -= 1f;
				if (relError > largestRelError)
					largestRelError = relError;
				// errors.add(new Pair<Double, String>(relError, "x: " + (i % size1d - radius) + ", z: " + (i / size1d - radius)));
				numNonZeroChunks++;
				double error = prior.chunks[i].weight - chunks[i].weight;
				totalSquaredError += error * error;
			}
			sump += prior.chunks[i].weight;
			sum += chunks[i].weight;
		}
//		errors.sort((Pair<Double, String> p1, Pair<Double, String> p2) -> Double.compare(p1.fst, p2.fst));
//		for (Pair<Double, String> p : errors) {
//			System.out.println(p.fst + p.snd);
//		}
		System.out.println("Average non-zero weight: " + sum/numNonZeroChunks);
		System.out.println("Root-mean-square error (on non-zero weights): " + Math.sqrt(totalSquaredError/numNonZeroChunks));
		System.out.println("Largest relative error: " + largestRelError);
		System.out.println("False negative count: " + falseNegativeCount);
		System.out.println("Prior sum: " + sump);
		System.out.println("Approx prior sum: " + sum);
		RingIterator ringIterator = new RingIterator();
		Ring ring = ringIterator.next();
		System.out.println("Density at 1600: Approx: " + strongholdDensity(100, 0, ring) + ", True (pre snapping): " + super.strongholdDensity(100, 0, ring));
		ring = ringIterator.next();
		ring = ringIterator.next();
		System.out.println("Density at 8000: Approx: " + strongholdDensity(500, 0, ring) + ", True (pre snapping): " + super.strongholdDensity(500, 0, ring));
	}

	@Override
	protected double strongholdDensity(double cx, double cz, Ring ring) {
		double d2 = cx * cx + cz * cz;
		double relativeWeight = 1.0;
		if (ring.ring == 0 && divineContext != null) {
			double phi = Coords.getPhi(cx, cz);
			relativeWeight = -divineContext.angleOffsetFromSector(phi) / (StrongholdConstants.snappingRadius * 1.5 / Math.sqrt(d2)); // 1.5 ~ sqrt(2) + a small margin
			relativeWeight = (1.0 + relativeWeight) * 0.5;
			// clamp
			if (relativeWeight > 1)
				relativeWeight = 1;
			if (relativeWeight < 0)
				relativeWeight = 0;
			relativeWeight *= divineContext.relativeDensity();
		}
		// Post snapping circle radiuses (dont have to be exact, tighter margins only affect performance, not the result)
		double c0_ps = ring.innerRadius - 2 * StrongholdConstants.snappingRadius;
		double c1_ps = ring.outerRadius + 2 * StrongholdConstants.snappingRadius;
		if (d2 < c0_ps * c0_ps || d2 > c1_ps * c1_ps)
			return 0;
		return relativeWeight * ApproximatedDensity.density(cx, cz);
	}
	
	@Override
	protected int discretisationPointsPerChunkSide() {
		return 2;
	}
	
	@Override
	protected int margin() {
		return StrongholdConstants.snappingRadius;
	}

	@Override
	protected void setInitialSize(int centerX, int centerZ, int radius) {
		setSize(centerX, centerZ, radius);
	}

	@Override
	protected void setInitialWeights() {
		super.setInitialWeights();
	}

	@Override
	protected void smoothWeights() {
		// Skip biome snapping (already accounted for by approximation)
	}

}
