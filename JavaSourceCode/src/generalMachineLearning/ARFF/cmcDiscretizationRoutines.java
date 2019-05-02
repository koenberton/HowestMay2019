package generalMachineLearning.ARFF;

import cbrTekStraktorModel.cmcProcSettings;
import logger.logLiason;

public class cmcDiscretizationRoutines {

	cmcProcSettings xMSet=null;
	logLiason logger=null;
	
    double[] binsegmentbegins = null;
    
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
    public cmcDiscretizationRoutines(cmcProcSettings is,logLiason ilog)
    //------------------------------------------------------------
    {
    	xMSet = is;
		logger=ilog;
    }
    
    //------------------------------------------------------------
    public double[] getBinSegementStarts()
    //------------------------------------------------------------
    {
    	return  binsegmentbegins;
    }
    
    
    // Start Value in INCLUSIVE
    // End value is the same as the start of the next segment and is EXCLUSIVE
    // binSegementBegins holds the begin value of each bing
    // bins is an array of integers which defines the index of the bin the list value belongs to
    //------------------------------------------------------------
    public int[] simpleBinner(double[] list , int nbrBins)
    //------------------------------------------------------------
    {
    	binsegmentbegins=null;
    	try {
    	double min     = list[0];
    	double max     = list[ list.length - 1];
    	double span    = max - min;
    	double binspan = span / (double)nbrBins;
    	double[] start = new double[ nbrBins ];
    	double curr    = min;
    	for(int i=0;i<nbrBins;i++)
    	{
    		start[i] = curr;
    		curr     = curr + binspan;
    	}
    	binsegmentbegins = new double[ nbrBins ];
    	
    	// assign
    	do_log( 1 , "[MIN=" + min + "]   [MAX=" + max + "]  [" + binspan + "]");
    	int[] bins = new int[ list.length ];
    	for(int i=0;i<bins.length; i++) bins[i] = -1;
    	//
    	for(int i=0 ; i<start.length ; i++)
    	{
    		double start_segment = (i==0) ? min - 10 : start[i];
    		double end_segment = (i == (start.length-1)) ? max + 10 : start[i+1];
    		// store
    		binsegmentbegins[i] = start_segment;
    		//
    		for(int j=0 ; j<list.length ; j++)
    		{
    		  if ( list[j] < start_segment ) continue;
    		  if ( list[j] > end_segment ) break;  // sorted
    		  bins[j] = i;
    		}
    	}
    	for(int i=0;i<bins.length;i++)
    	{
    		if( (bins[i] < 0) || (bins[i]>=nbrBins) ) {
    			do_log( 1 , "Could not allocate a bin for [" + list[i] + "] [idx=" + i + "]");
    			do_error( "Could not allocate a bin for [" + list[i] + "] [idx=" + i + "]");
    			return null;
    		}
    	}
    	return bins;
    	}
    	catch(Exception e ) {
    		do_error("Could not run simple binner");
    		e.printStackTrace();
    		return null;
    	}
    }
    
    public int[] entropyBinner()
    {
    	
    	
    	return null;
    }
}
