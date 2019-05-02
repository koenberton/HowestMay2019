package featureExtraction;

public class cmcHaralick {

	enum ORIENTATION { VERTICAL , HORIZONTAL , UNKNOWN }
	enum CHANNEL     { RED , GREEN , BLUE , GRAYSCALE , UNKNOWN }
	
	ORIENTATION orientation  = ORIENTATION.UNKNOWN;
	CHANNEL channel = CHANNEL.UNKNOWN;
	double[][] gc   = null;
	double maximumProbability;
	double contrast;
	double dissimilarity;
	double inverseDifference;
	double homegenity;
	double energy;
	double entropy;
	double autoCorrelation;   
	double correlation;
	double cbrTekCorrelation;
	
	public cmcHaralick(ORIENTATION io , CHANNEL ic , double[][] igc)
	{
		orientation        = io;
		channel            = ic;
		gc                 = igc;
		maximumProbability = 0;
    	contrast           = 0;
    	dissimilarity      = 0;
    	inverseDifference  = 0;
    	homegenity         = 0;
    	energy             = 0;
    	entropy            = 0;
    	autoCorrelation    = 0; 
    	correlation        = 0;
    	cbrTekCorrelation  = 0;
	}
	
	String format()
	{
		return
		"-------- label  : [" + this.orientation + "] [" + this.channel + "] ----------------" +
		"\nmaxProbability : [" + this.maximumProbability + "]" +
		"\n      Contrast : [" + this.contrast + "]" + 
    	"\n Dissimilarity : [" + this.dissimilarity + "]" +
    	"\n    Homogenity : [" + this.homegenity + "]" +
    	"\n   InverseDiff : [" + this.inverseDifference + "]" +
    	"\n        Energy : [" + this.energy + "]" +
    	"\n       Entropy : [" + this.entropy + "]" +
    	"\nAutoCorrelation : [" + this.autoCorrelation + "]" +
    	"\n   Correlation : [" + this.correlation + "]" +
    	"\nCBRCorrelation : [" + this.cbrTekCorrelation + "]";
   }
}
