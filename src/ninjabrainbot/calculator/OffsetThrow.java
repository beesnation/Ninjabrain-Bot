package ninjabrainbot.calculator;

import ninjabrainbot.util.Pair;

public class OffsetThrow implements IThrow
{
    static final double eyeDist = 12;

    // The two F3+C inputs
    private final IThrow throwPos, measurement;

    // To correctly disambiguate between the two ray-circle intersection points,
    // This would need to be set to true were the user to move in front of the eye
    private final boolean passedEyeTangent;

    // Properties computed in constructor
    private final double alpha_0, alpha, std_factor, var_add;

    public OffsetThrow(IThrow throwPos, IThrow measurement, boolean passedEyeTangent) {
        this.throwPos = throwPos;
        this.measurement = measurement;
        this.passedEyeTangent = passedEyeTangent;

        this.alpha_0 = angleFromEyeVec(computeEyeVec(measurement.alpha_0()));
        Pair<Double, Double> eyeVec = computeEyeVec(measurement.alpha());
        this.alpha = angleFromEyeVec(eyeVec);
        this.std_factor = computeStdFactor(eyeVec);
        this.var_add = computeVarAdd();
    }


    private static boolean isInaccuratePosition(IRay pos)
    {
        return (pos.x() != 0.3 && pos.x() != 0.5 && pos.x() != 0.7)
                || (pos.z() != 0.3 && pos.z() != 0.5 && pos.z() != 0.7);
    }

    public String toString() {
        return "x=" + x() + ", z=" + z() + ", alpha=" + alpha_0 + ", stdFactor=" + std_factor;
    }

    private Pair<Double, Double> computeEyeVec(double measuredAngle) {
        // Returns null if ray does not intersect circle

        boolean outsideCircle = throwPos.distance2(measurement) > eyeDist*eyeDist;
        boolean useFirstIntersection = passedEyeTangent && outsideCircle;

        double phi = Math.toRadians(measuredAngle);

        // Ray direction from second
        double dx = -Math.sin(phi);
        double dz = Math.cos(phi);

        // Vector from second to first
        double ux = throwPos.x() - measurement.x();
        double uz = throwPos.z() - measurement.z();

        // Project it onto ray
        double u_dot_d = dx*ux + dz*uz;
        if (outsideCircle && u_dot_d < 0) return null; // rays don't exist behind you
        double upx = u_dot_d * dx;
        double upz = u_dot_d * dz;

        // compute distance along ray from the closest point to the intersection points
        double udx = ux - upx;
        double udz = uz - upz;
        double d2 = udx*udx + udz*udz;
        double m2 = eyeDist*eyeDist - d2;
        if (m2 < 0) return null; // No intersections
        double m = Math.sqrt(m2);

        // Compute the eye pos
        if (useFirstIntersection) m *= -1;
        double ix = upx + m*dx;
        double iz = upz + m*dz;

        return new Pair<>(ix, iz);
    }

    private double angleFromEyeVec(Pair<Double, Double> ev)
    {
        if (ev == null) return Double.NaN;
        double x = ev.fst + (measurement.x() - throwPos.x());
        double z = ev.snd + (measurement.z() - throwPos.z());
        return Math.toDegrees(-Math.atan2(x, z));
    }

    private double computeStdFactor(Pair<Double, Double> ev) {
        // Steeper vertical angles result in less accurate measurements.
        // Assuming the user-provided std is for eyes measured at around -31.5 deg:
        if (ev == null) return Double.NaN;
        double incline_factor = Math.cos(-0.55) / Math.cos(Math.toRadians(measurement.beta()));

        // The maths used to convert the angle also scales the error.
        // Moving closer to the eye improves the effective error, but measuring from an off-angle worsens it.
        double theta = Math.toRadians(alpha - measurement.alpha());
        double r = Math.sqrt(ev.fst*ev.fst + ev.snd*ev.snd);
        double conversion_factor = Math.abs(r / (eyeDist * Math.cos(theta)));

        return incline_factor * conversion_factor;
    }
    private double computeVarAdd() {
        if (measurement.x() == throwPos.x() && measurement.z() == throwPos.z()) return 0;

        final double varPerPosition = (0.01 * 0.01 / 12) * Math.pow(360 / (eyeDist * 2 * Math.PI), 2);
        double var = 0;
        if (isInaccuratePosition(measurement))
            var += varPerPosition;
        if (isInaccuratePosition(throwPos))
            var += varPerPosition;
        return var;
    }

    @Override
    public double getStd(StdSettings stds) {
        return Math.sqrt(var_add + Math.pow(std_factor * measurement.getStd(stds), 2));
    }

    @Override
    public double x() { return throwPos.x(); }
    @Override
    public double z() { return throwPos.z(); }

    @Override
    public double alpha() { return alpha; }

    @Override
    public double beta() {return measurement.beta();}



    @Override
    public double alpha_0() { return alpha_0; }

    @Override
    public double correction() { return measurement.correction(); }

    @Override
    public boolean lookingBelowHorizon() { return false; }

    @Override
    public boolean altStd() { return measurement.altStd(); }

    @Override
    public boolean manualInput() { return measurement.manualInput();}

    @Override
    public boolean isNether() { return false; }

    @Override
    public OffsetThrow withAddedCorrection(double angle) {
        return new OffsetThrow(throwPos, measurement.withAddedCorrection(angle), passedEyeTangent);
    }

    @Override
    public OffsetThrow withToggledSTD() {
        return new OffsetThrow(throwPos, measurement.withToggledSTD(), passedEyeTangent);
    }

    public boolean isValid()
    {
        return !(Double.isNaN(alpha) || Double.isNaN(alpha_0));
    }
}
