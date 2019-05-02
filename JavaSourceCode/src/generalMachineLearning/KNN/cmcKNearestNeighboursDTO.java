package generalMachineLearning.KNN;

import generalMachineLearning.ARFF.ARFFCategoryCoreDTO;

public class cmcKNearestNeighboursDTO {

	private String LongARFFFileName = null;
	private int OptimalK = 1;
	private int NbrOfRows = -1;
	private int NbrOfFeatures = -1;
	private ARFFCategoryCoreDTO[] features = null;
	private double[][] samples = null;
	private double[] results = null;
	private int NbrOfRePartitions = -1;
	private int NbrOfPartitions = -1;
	private int MaximumK = -1;
	private long elapsedTime=-1L;

	public cmcKNearestNeighboursDTO(int K , int nrows , int nfeatures )
	{
	   	OptimalK = K;
	   	NbrOfRows = nrows;
	   	NbrOfFeatures = nfeatures;
	   	features = new ARFFCategoryCoreDTO[ NbrOfFeatures ];
	   	samples = new double[ NbrOfRows ][ NbrOfFeatures-1 ];
	   	results = new double[ NbrOfRows ];
	   	for(int i=0;i<NbrOfRows;i++)
	   	{
	   		results[i] = Double.NaN;
	   		for(int j=0;j<NbrOfFeatures-1;j++ ) samples[i][j] = Double.NaN;
	   	}
   		for(int j=0;j<NbrOfFeatures;j++ ) features[j] = null;
	}

	public boolean setSamplesValue(int row , int col , double val)
	{
		try {
		 this.samples[ row ][ col ] = val;
		 return true;
		}
		catch(Exception e ) {
			System.err.println(" ERROR setting samples[" + samples.length + "] [" + (NbrOfFeatures-1) + "] [r=" + row + " c=" + col + "] " + e.getMessage());
			//e.printStackTrace();
			return false; 
		}
	}
	
	public boolean setResultsValue(int row ,double val)
	{
		try {
		 this.results[ row ]= val;
		 return true;
		}
		catch(Exception e ) {
			System.err.println("ERROR results[" + results.length + "] [r=" + row + "]");
			//e.printStackTrace();
			return false; 
		}
	}
	
	public int getNbrOfColumnsOnSamples()
	{
		if( this.samples == null )  return -1;
		int ncols=-1;
		for(int i=0;i<samples.length;i++)
		{
			if( ncols == -1 ) ncols = samples[i].length;
			if( ncols != samples[i].length ) return -1;
		}
		return ncols;
	}
	
	public String getLongARFFFileName() {
		return LongARFFFileName;
	}

	public void setLongARFFFileName(String longARFFFileName) {
		LongARFFFileName = longARFFFileName;
	}

	public int getOptimalK() {
		return OptimalK;
	}

	public void setOptimalK(int optimalK) {
		OptimalK = optimalK;
	}

	public int getNbrOfRows() {
		return NbrOfRows;
	}

	public void setNbrOfRows(int nbrOfRows) {
		NbrOfRows = nbrOfRows;
	}

	public int getNbrOfFeatures() {
		return NbrOfFeatures;
	}

	public void setNbrOfFeatures(int nbrOfFeatures) {
		NbrOfFeatures = nbrOfFeatures;
	}

	public ARFFCategoryCoreDTO[] getFeatures() {
		return features;
	}

	public void setFeatures(ARFFCategoryCoreDTO[] features) {
		this.features = features;
	}

	public double[][] getSamples() {
		return samples;
	}

	public void setSamples(double[][] samples) {
		this.samples = samples;
	}

	public double[] getResults() {
		return results;
	}

	public void setResults(double[] results) {
		this.results = results;
	}

	public int getNbrOfRePartitions() {
		return NbrOfRePartitions;
	}

	public void setNbrOfRePartitions(int nbrOfRePartitions) {
		NbrOfRePartitions = nbrOfRePartitions;
	}

	public int getNbrOfPartitions() {
		return NbrOfPartitions;
	}

	public void setNbrOfPartitions(int nbrOfPartitions) {
		NbrOfPartitions = nbrOfPartitions;
	}

	public int getMaximumK() {
		return MaximumK;
	}

	public void setMaximumK(int maximumK) {
		MaximumK = maximumK;
	}

	public long getElapsedTime() {
		return elapsedTime;
	}

	public void setElapsedTime(long elapsedTime) {
		this.elapsedTime = elapsedTime;
	}
}
