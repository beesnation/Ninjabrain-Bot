package ninjabrainbot.calculator;

public interface IThrow extends IRay {

    public double getStd(StdSettings stds);

    public double beta();

    public double alpha_0();

    public double correction();

    public boolean lookingBelowHorizon();

    public boolean altStd();
    public boolean manualInput();
    public boolean isNether();

    public IThrow withAddedCorrection(double angle);
    public IThrow withToggledSTD();

}
