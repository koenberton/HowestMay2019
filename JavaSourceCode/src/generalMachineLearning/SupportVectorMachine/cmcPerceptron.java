package generalMachineLearning.SupportVectorMachine;

import java.util.Random;

import cbrTekStraktorModel.cmcProcSettings;
import generalMachineLearning.LinearAlgebra.cmcVector;
import generalMachineLearning.LinearAlgebra.cmcVectorRoutines;
import logger.logLiason;

public class cmcPerceptron {
	
	private static int MAX_ATTEMPTS = 1000;
	
	cmcProcSettings xMSet=null;
	logLiason logger=null;
	cmcVectorRoutines vrout = null;
	
	
	//------------------------------------------------------------
    private void do_log(int logLevel , String sIn)
	//------------------------------------------------------------
    {
       if( logger != null ) logger.write( this.getClass().getName() , logLevel , sIn);
       else 
       if (logLevel == 0 ) System.err.println(sIn);
       else System.out.println(sIn);
    }
	//------------------------------------------------------------
    private void do_error(String sIn)
	//------------------------------------------------------------
    {
    	do_log(0,sIn);
    }

    //------------------------------------------------------------
    public cmcPerceptron(cmcProcSettings is,logLiason ilog)
    //------------------------------------------------------------
    {
    	xMSet = is;
		logger=ilog;
		vrout = new cmcVectorRoutines();
    }
    
    //------------------------------------------------------------
    public boolean testPerceptron()
    //------------------------------------------------------------
    {
    	double[][] nums = { {8,7,1} , {4,10,1} , {9,7,1} , {7,10,1} , {9,6,1} , {4,8,1} , {10,10,1} , 
    			            {2,7,-1} , {8,3,-1}  , {7,5,-1} , {4,4,-1} , {4,6,-1} , {1,3,-1} , {2,5,-1}  };
    	
    	// initialize vectors
    	cmcVector[] vectorset = new cmcVector[ nums.length ];
    	double[]  anticipated = new double[ nums.length ];
    	for(int i=0;i<vectorset.length;i++)
    	{
    		double[] vec = new double[2]; vec[0] = nums[i][0]; vec[1] = nums[i][1];
    		vectorset[i] = new cmcVector( vec );
    		anticipated[i] = nums[i][2];
    		if( (anticipated[i] != -1) && (anticipated[i] != 1) ) {
    			do_error("Anticipated incorrect");
    			return false;
    		}
    	}
     	return doPerceptron( vectorset , anticipated , true );
    }
    
    //------------------------------------------------------------
    public boolean doPerceptron( cmcVector[] vectorlist , double[] anticipatedlist , boolean PLA )
    //------------------------------------------------------------
    {
    	if( vectorlist == null ) {
    		do_error("NULL vectorlist"); return false;
    	}
    	if( anticipatedlist == null ) {
    		do_error("NULL anticipatedlist"); return false;
    	}
    	if( anticipatedlist.length != vectorlist.length ) { do_error("number of vectors does not match number of expected values"); return false;}
    	if( vectorlist.length < 1) {
    		do_error("Empty vectorlist");
    		return false;
    	}
    	//
    	cmcVector[] augmentedlist = new cmcVector[vectorlist.length ];
    	for(int i=0;i<augmentedlist.length;i++)
    	{
    		augmentedlist[i] = vrout.makeAugmentedVector( vectorlist[i] );
    		if( (anticipatedlist[i] != -1) && (anticipatedlist[i] != 1) ) {  // put the validation here
    			do_error("Anticipated incorrect");
    			return false;
    		}
    	}
    	int dimensie=-1;
    	double maxval = Double.NaN;
        for(int i=0;i<augmentedlist.length;i++)
        {
        	if( augmentedlist[i] == null ) {
        		do_error("system error - null augmented");
        		return false;
        	}
        	if( dimensie == -1 ) dimensie = augmentedlist[i].getVectorValues().length;
        	if( dimensie != augmentedlist[i].getVectorValues().length ) {
        		do_error("the dimension of the vectors differs");
        		return false;
        	}
        	for(int j=0;j<dimensie;j++)
        	{
        		if( Double.isNaN(maxval) ) maxval = augmentedlist[i].getVectorValues()[j];
        		if( maxval <  augmentedlist[i].getVectorValues()[j] ) maxval = augmentedlist[i].getVectorValues()[j];
        	}
        }
        if( Double.isNaN(maxval) ) {
        	do_error( "System error - cannot set maxval");
        	return false;
        }
        //do_log( 1 , "Dimension " + dimensie );
    	
        // seed the W
        // CAUTION w is augmented !!! i.e. dimension + 1   first value is the bias
        Random rn = new Random();
        double[] seed = new double[ dimensie ];
        for(int i=0;i<seed.length;i++)
        {
          seed[i] = maxval - (rn.nextInt((int)maxval) * 2);
        }
        cmcVector w = new cmcVector( seed );
    	//do_log( 1 , "Seed [w=" + w.show() + "]" );
    	//
       	boolean[] classifiedCorrectly=null;
    	boolean found=false;
    	int attempts=0;
    	int[] trend = new int[MAX_ATTEMPTS];
    	for(int i=0 ; i<trend.length ; i++ ) trend[i]=-1;
    	for(int i=0;i<MAX_ATTEMPTS;i++)
    	{
    	  attempts++;
    	  classifiedCorrectly = calcHypothesisAndGetMatch( w , augmentedlist , anticipatedlist , false );
    	  if( classifiedCorrectly == null ) { do_error("System error 1"); return false; }
    	  int nerrors=0;
    	  for(int j=0;j<classifiedCorrectly.length;j++)
    	  {
    		  if( classifiedCorrectly[j] == false ) nerrors++;
    	  }
    	  trend[attempts-1] = nerrors;
    	  if( nerrors == 0 ) { found = true;  break;  }
    	  if( PLA ) {
    		 
    	  }
    	  // make a list of all vectors for which the function did not match
    	  int[] pickList = new int[ nerrors ];
    	  nerrors=0;
    	  for(int j=0;j<classifiedCorrectly.length;j++)
    	  {
    		  if( classifiedCorrectly[j] == false ) { pickList[nerrors] = j; nerrors++; }
    	  }
    	  if( nerrors != pickList.length ) { do_error("System error 2"); return false; }
    	  // now just fecth a random vector for which the function did not match
    	  int pdx = rn.nextInt((nerrors+1)*97) % nerrors;
    	  if( (pdx<0) || (pdx>=pickList.length)) { do_error("System error 2"); return false; }
    	  int idx = pickList[pdx];
    	  if( (idx<0) || (idx>=augmentedlist.length)) { do_error("System error 3"); return false; }
    	  // update rule
    	  // get the random vector for which function failed and multiply with its (single) anticipated result (1,-1)
    	  cmcVector zz = vrout.scalarMultiplicationVector( augmentedlist[ idx ] , anticipatedlist[idx] );
    	  if( zz == null ) return false;
    	  // modify all wieght by adding the result of the update rule.
    	  w = vrout.addVectors( w , zz );
    	  
    	  //do_log( 1 , "Iteration [" + i + "] [#Misclassified=" + nerrors + "] [w=" + w.show() + "] [Swap=" + "" + "]");
    	  
    	}
    	if( !found ) {
    		do_error( "Could not find a hyperplane in [" + attempts + "] attempts. Set may not be not linear separable."); 
    		return false;
    	}
    	// calculate geometric margin
    	double geom = calculateGeometricMargin( w , vectorlist , anticipatedlist );
    	if( Double.isNaN(geom) ) {
    		do_error("Cannot determine geometric margin");
    		return false;
    	}
   	
        //
    	cmcVector unaugw = vrout.extractVectorFromAugmentedVector(w);
    	do_log( 1 , "Found hyperplane w = " + unaugw.showRound() + " bias=" + vrout.getBiasFromAugmentedVector(w) +" in [" + attempts + "] attempts [GeometricMargin=" + geom + "] " + w.showRound());
    	if( (w.getDimension() == 3) && ( w.getVectorValues()[0] != 0)) {
    		double a = -1 * w.getVectorValues()[1] /  w.getVectorValues()[2];
    		double b = -1 * w.getVectorValues()[0] /  w.getVectorValues()[2];
    		do_log( 1 , "y = " + a + "x " + (b<0?"":"+ ") + b);
    	}
    	//
    	return true;
    }
    
    //------------------------------------------------------------
    private boolean[] calcHypothesisAndGetMatch( cmcVector w , cmcVector[] augmentedlist , double[] anticipatedlist , boolean verbose)
    //------------------------------------------------------------
    {
    	boolean[] classifiedCorrectly = new boolean[ anticipatedlist.length ];
    	for(int i=0 ; i<augmentedlist.length ; i++ )
    	{
    		double dot = vrout.dotproduct( w , augmentedlist[i] );
    		if( Double.isNaN( dot ) ) return null;
    		int mult = ( dot < 0 ) ? -1 : 1;  // zero is 1 ?? een punt op de hyperplane is ok?
    		classifiedCorrectly[ i ] = ((int)anticipatedlist[i] * mult == 1 ) ? true : false;  // -1.-1 en 1.1 OK
    		if (verbose) do_log( 1 , "(" + w.show() + "." + augmentedlist[i].show() + ") = " + dot + " " + anticipatedlist[i] + " " + classifiedCorrectly[i]);  
    	}
    	return classifiedCorrectly;
    }
    
    //------------------------------------------------------------
    private double calculateGeometricMargin( cmcVector waugmented , cmcVector[] vectorlist , double[] expectedlist)
    //------------------------------------------------------------
    {
      try  {
    	// unaugment
    	double bias = vrout.getBiasFromAugmentedVector(waugmented);
    	if( Double.isNaN(bias) ) return Double.NaN;
    	cmcVector w = vrout.extractVectorFromAugmentedVector(waugmented);
    	if( w == null ) return Double.NaN;
    	//do_log( 1 , w.showRound() + " " + bias + "   " + waugmented.show() );
    	double norm = w.getNorm();
    	if( norm == 0 ) return Double.NaN;
    	cmcVector wnorm = vrout.scalarMultiplicationVector( w , 1 / norm );
    	if( wnorm == null ) return Double.NaN;
    	double biasnorm = bias / norm;
    	
    	// look for the minimal value of (w/norm)x + (b/norm) 
    	double dmin = Double.NaN;
    	for(int i=0;i<vectorlist.length;i++)
    	{
    	   double dd = vrout.dotproduct( wnorm , vectorlist[i] );
    	   if( Double.isNaN( dd ) ) return Double.NaN;
    	   dd = (dd + biasnorm) * expectedlist[i];
    	   if( dd < 0 ) continue;  //  y (w.x +b) must be positive if calcualtes matches anticipated
    	   if( Double.isNaN(dmin) ) dmin = dd;
    	   if( dmin > dd ) dmin = dd;
    	}
        return dmin;
      }
   	  catch(Exception e ) {
    	do_error("calculateGeometricMargin" + xMSet.xU.LogStackTrace(e) );
    	return Double.NaN;
      }
    }
}
