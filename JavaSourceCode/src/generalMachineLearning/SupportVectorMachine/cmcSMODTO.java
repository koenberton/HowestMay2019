package generalMachineLearning.SupportVectorMachine;

import generalMachineLearning.cmcMachineLearningEnums;
import generalMachineLearning.ARFF.ARFFCategoryCoreDTO;
import generalMachineLearning.LinearAlgebra.cmcMatrix;

public class cmcSMODTO {
	
	private boolean FULL_PLATT_ALGORITHM = true;

	private cmcMachineLearningEnums.KernelType KernelTrickTipe = cmcMachineLearningEnums.KernelType.UNKNOWN;
	private String LongARFFFileName = null;
	private ARFFCategoryCoreDTO[] features=null;
	private double[] normalizerMean=null;
	private double[] normalizerStdDev=null;
	private int MaxCycles=-1;
	private int NbrOfFeatures=-1;
	private int NbrOfRows=-1;
	private int NbrOfSupportVectors=0;
	private double sigma = Double.NaN;
	private double gamma = Double.NaN;

	private double C = Double.NaN;  // SoftMargin
	private double Bias = Double.NaN;
	private double tolerance = Double.NaN;
	private cmcMatrix dataMatrix = null;
	private cmcMatrix resultMatrix = null;
    private cmcMatrix lagrangeMultiplierMatrix = null;   // M,1
    private double[] errorCache = null;
    private cmcMatrix weightMatrix = null;   //  dimensionality 1,N
    
    public cmcSMODTO(String FName , cmcMachineLearningEnums.KernelType itp)
	{
    	KernelTrickTipe = itp;
		LongARFFFileName = FName;
		MaxCycles=-1;
		NbrOfFeatures=-1;
		NbrOfRows = -1;
		C = Double.NaN;
		Bias = Double.NaN;
		tolerance = Double.NaN;
		MaxCycles = -1;
		resultMatrix = null;
		gamma = Double.NaN;
		sigma = Double.NaN;
	}

	public cmcSMODTO(String FName, int inrows , double cin , double itol , int icycl , cmcMachineLearningEnums.KernelType itp)
	{
		KernelTrickTipe = itp;
		LongARFFFileName = FName;
		MaxCycles=-1;
		NbrOfFeatures=-1;
		NbrOfRows = inrows;
		C = cin;
		Bias = 0;
		tolerance = itol;
		MaxCycles = icycl;
		resultMatrix = null;
		gamma = Double.NaN;
		sigma = Double.NaN;
	    initializeMatrices( NbrOfRows ); 
	}
	
	public void initializeMatrices(int inrows)
	{
		// Lagrande multipliers - 1 for each row
    	double[][] alp = new double[ inrows ][ 1 ];
    	for(int i=0;i<inrows;i++) alp[i][0] = 0;
    	lagrangeMultiplierMatrix = new cmcMatrix( alp );
    	// error cache
    	this.errorCache = new double[ inrows ];
    	for(int i=0;i<inrows;i++) this.errorCache[i] = 0;
    	NbrOfSupportVectors=0;
	}
	
	//------------------------------------------------------------
	public String checkDTO()
	//------------------------------------------------------------
	{
		if( this.dataMatrix == null ) return "NULL DataMatrix";
    	if( this.resultMatrix == null ) return "NULL resultMatrix";
    	if( this.dataMatrix.getNbrOfRows() != this.getNbrOfRows() ) return "Row mismatch Data and DTO";
    	if( this.resultMatrix.getNbrOfRows() != this.getNbrOfRows() ) return "Row mismatch Data and Result matrix"; 
    	if( this.resultMatrix.getNbrOfColumns() != 1 ) return "Number of columns is not 1 on resultmatrix";
    	if( this.lagrangeMultiplierMatrix.getNbrOfColumns() != resultMatrix.getNbrOfColumns() )  return "Alpha en result col mismatch";    
    	if( this.lagrangeMultiplierMatrix.getNbrOfRows() != resultMatrix.getNbrOfRows() )  return "Alpha and result row mismatch"; 
    	if( this.lagrangeMultiplierMatrix.getNbrOfColumns() != 1)  return "lagrangemultipliers - nbr of cols must be 1"; 
    	if( this.lagrangeMultiplierMatrix.getNbrOfRows() != dataMatrix.getNbrOfRows() ) return "Alpha and data - rows mismatch"; 
    	//
    	return null;
	}
	
	//------------------------------------------------------------
	public boolean setErrorCacheIdxValue( int idx , double dd )
	//------------------------------------------------------------
	{
		try {
			this.errorCache[ idx ] = dd;
			return true;
		}
		catch(Exception e ) {
			return false;
		}
	}
	
	public boolean calculateNbrOfSupportVectors()
	{
		NbrOfSupportVectors=0;
		if( lagrangeMultiplierMatrix == null ) return false;  // M,1
		for(int i=0;i<lagrangeMultiplierMatrix.getNbrOfRows();i++)
		{
			if( this.lagrangeMultiplierMatrix.getValues()[i][0] != 0 ) NbrOfSupportVectors++;
		}
		return true;
	}
	//------------------------------------------------------------
	//------------------------------------------------------------
	
	public double getSigma() {
		return sigma;
	}

	public double getGamma() {
		return gamma;
	}

	public void setSigma(double sigma) {
		this.sigma = sigma;
	}

	public void setGamma(double gamma) {
		this.gamma = gamma;
	}

	public cmcMachineLearningEnums.KernelType getKernelTrickTipe() {
		return KernelTrickTipe;
	}

	public void setKernelTrickTipe(cmcMachineLearningEnums.KernelType kernelTrickTipe) {
		KernelTrickTipe = kernelTrickTipe;
	}
	
	public int getNbrOfSupportVectors() {
		return NbrOfSupportVectors;
	}

	public void setNbrOfSupportVectors(int i)
	{
		NbrOfSupportVectors = i;
	}
	
	public cmcMatrix getWeightMatrix() {
		return weightMatrix;
	}

	public void setWeightMatrix(cmcMatrix weightMatrix) {
		this.weightMatrix = weightMatrix;
	}
	public boolean isFULL_PLATT_ALGORITHM() {
		return FULL_PLATT_ALGORITHM;
	}
	
	public double[] getErrorCache() {
		return errorCache;
	}

	public void setErrorCache(double[] errorCache) {
		this.errorCache = errorCache;
	}
	
	public cmcMatrix getDataMatrix() {
		return dataMatrix;
	}

	public cmcMatrix getResultMatrix() {
		return resultMatrix;
	}

	public void setDataMatrix(cmcMatrix dataMatrix) {
		this.dataMatrix = dataMatrix;
	}

	public void setResultMatrix(cmcMatrix resultMatrix) {
		this.resultMatrix = resultMatrix;
	}

	public cmcMatrix getAlphaMatrix() {
		return lagrangeMultiplierMatrix;
	}

	public void setAlphaMatrix(cmcMatrix lagrangeMultiplierMatrix) {
		this.lagrangeMultiplierMatrix = lagrangeMultiplierMatrix;
	}
	
	public cmcMatrix getLagrangeMultiplierMatrix() {
		return lagrangeMultiplierMatrix;
	}

	public void setLagrangeMultiplierMatrix(cmcMatrix lagrangeMultiplierMatrix) {
		this.lagrangeMultiplierMatrix = lagrangeMultiplierMatrix;
	}

	public int getNbrOfRows() {
		return NbrOfRows;
	}

	public double getBias() {
		return Bias;
	}

	public void setNbrOfRows(int nbrOfRows) {
		NbrOfRows = nbrOfRows;
	}

	public void setBias(double b) {
		Bias = b;
	}
	
	public double getC() {
		return C;
	}

	public double getTolerance() {
		return tolerance;
	}

	public void setC(double c) {
		C = c;
	}

	public void setTolerance(double tolerance) {
		this.tolerance = tolerance;
	}

	public String getLongARFFFileName() {
		return LongARFFFileName;
	}

	public ARFFCategoryCoreDTO[] getFeatures() {
		return features;
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
	
	public void setLongARFFFileName(String longARFFFileName) {
		LongARFFFileName = longARFFFileName;
	}

	public void setFeatures(ARFFCategoryCoreDTO[] categories) {
		this.features = categories;
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
	
	public int getNbrOfFeatures() {
		return NbrOfFeatures;
	}

	public void setNbrOfFeatures(int nbrOfFeatures) {
		NbrOfFeatures = nbrOfFeatures;
	}
	
}
