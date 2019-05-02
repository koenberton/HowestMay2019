package generalMachineLearning.LogisticRegression;


import generalMachineLearning.ARFF.ARFFCategoryCoreDTO;

public class cmcLogisticRegressionDTO {

	private String LongARFFFileName = null;
	private ARFFCategoryCoreDTO[] features=null;
	private double[] weights=null;
	private double[] normalizerMean=null;
	private double[] normalizerStdDev=null;
	private int MaxCycles=-1;
	private int OptimalCycles=-1;
	private int NbrOfFeatures=-1;
	private double StepSize=-Double.NaN;

	public cmcLogisticRegressionDTO(String FName)
	{
		LongARFFFileName = FName;
		MaxCycles=-1;
		OptimalCycles=-1;
		NbrOfFeatures=-1;
	}
	
	public String getLongARFFFileName() {
		return LongARFFFileName;
	}

	public ARFFCategoryCoreDTO[] getFeatures() {
		return features;
	}

	public double[] getWeights() {
		return weights;
	}

	public double[] getNormalizerMean() {
		return normalizerMean;
	}

	public double[] getNormalizerStdDev() {
		return normalizerStdDev;
	}

	public int getMaxCycles() {
		return MaxCycles;
	}

	public int getOptimalCycles() {
		return OptimalCycles;
	}
	
	public double getStepSize() {
		return StepSize;
	}
	
	public void setLongARFFFileName(String longARFFFileName) {
		LongARFFFileName = longARFFFileName;
	}

	public void setFeatures(ARFFCategoryCoreDTO[] categories ) {
		this.features = categories;
	}

	public void setWeights(double[] weights) {
		this.weights = weights;
	}

	public void setNormalizerMean(double[] normalizerMean) {
		this.normalizerMean = normalizerMean;
	}

	public void setNormalizerStdDev(double[] normalizerStdDev) {
		this.normalizerStdDev = normalizerStdDev;
	}

	public void setMaxCycles(int maxCycles) {
		MaxCycles = maxCycles;
	}

	public void setOptimalCycles(int optimalCycles) {
		OptimalCycles = optimalCycles;
	}
	public int getNbrOfFeatures() {
		return NbrOfFeatures;
	}

	public void setNbrOfFeatures(int nbrOfFeatures) {
		NbrOfFeatures = nbrOfFeatures;
	}
	
	public void setStepSize(double d)
	{
		StepSize=d;
	}
}
