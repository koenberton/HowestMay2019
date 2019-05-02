package generalMachineLearning;

public class cmcMachineLearningConstants {

	 public static int NBR_BINS = 16;
	 public static int TRAININGSET_PERCENTAGE = 13;
	 public static double TOLERANCE_LIMIT = (double)0.00001;  // tolerance for the calculations using DOUBLE 
	 //
	 // Decision tree
	 public static int MAX_NBR_OF_BRANCHES = 1000;  
     public static int MAX_NBR_OF_LEVELS   = 25;
	 //
     // KNN
	 public static int NBR_OF_REPARTITIONS = 5;
	 public static int NBR_OF_PARTITIONS   = 12;
	 public static int MAX_K               = 30;
	 // Lenair regression
	 public static int MAX_NUMBER_OF_LINEAIR_REGRESSION_CYCLES = 10000;
	 public static double LINEAIR_REGRESSION_STEPSIZE = (double)0.01;
	 // Logistic regression
	 public static int MAX_NUMBER_OF_LOGISTIC_REGRESSION_CYCLES = 2500;
	 public static double LOGISTIC_REGRESSION_STEPSIZE = (double)0.001;
	 // SMO
	 public static double SMO_RBF_GAMMA = (double)1;
	 public static double SMO_GAUSSIAN_SIGMA = (double)5;   // larger is more general classifier  0.25 .. 5
	 
}