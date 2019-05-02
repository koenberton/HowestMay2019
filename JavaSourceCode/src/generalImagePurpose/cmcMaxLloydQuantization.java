package generalImagePurpose;

import cbrTekStraktorModel.cmcProcSettings;
import generalStatPurpose.gpFrequencyDistributionStatFunctions;
import logger.logLiason;

public class cmcMaxLloydQuantization {
	
	private static boolean DEBUG = false;
	
	static int MAX_RUNS = 20;   // should be enough
	
	cmcProcSettings xMSet=null;
	logLiason logger=null;
	gpFrequencyDistributionStatFunctions statter = null;
	
	double[] bfr_centers = null;
	
	class cycle
	{
		int run;
	    int totalBins;
	    int reducedBins;
		double[] ar_center;
		double[] ar_left;   // inclusive
		double[] ar_right;  // exclusive
		double[] ar_mean;
		double[] ar_mse;
		double totalMSE;
		double meanMSE;
		boolean valid=false;
		//
		cycle(int idx , int total , int redu )
		{
			run         = idx;
			totalBins   = total;
			reducedBins = redu;
			valid       = false;
			ar_center   = new double[redu];
			ar_left     = new double[redu];
			ar_right    = new double[redu];
			ar_mean     = new double[redu];
			ar_mse      = new double[redu];
			totalMSE    = -1;
			meanMSE     = -1;
			for(int i=0;i<redu;i++) ar_center[i]=ar_left[i]=ar_right[i]=ar_mean[i]=ar_mse[i]=-1;
		}
		void setBoundaries()
		{
			ar_left[0] = 0;
			ar_right[ this.reducedBins - 1 ] = this.totalBins;
			for(int i=0;i<this.reducedBins-1;i++)
			{
			   double right =  (ar_center[i] + ar_center[i+1]) / 2;
			   ar_right[i] = right;
			   ar_left[i+1] = right;
			}
		}
		void dump()
		{
			do_log( 5 , "[" + this.run + "] [Redu=" + reducedBins + "]");
			String slijn="";
			for(int i=0;i<ar_left.length;i++) slijn += "(" +(int)ar_left[i] + "-" + (int)ar_center[i] + "-" + (int)ar_right[i] + ") ";
			do_log( 5 , slijn );
			slijn="";
			for(int i=0;i<ar_mean.length;i++) slijn += "[" + ar_mean[i] + "]";
			do_log( 5 , slijn );
			slijn="";
			for(int i=0;i<ar_mean.length;i++) slijn += "[" + ar_mse[i] + "]";
			do_log( 5 , slijn );
			slijn="";
			do_log( 5 , " ==> [MSE=" + this.totalMSE + " meanMSE=" + this.meanMSE + "]");
			
		}
		void msedump()
		{
			do_log( 5 , "[" + run + "] [totMSE=" + this.totalMSE + " meanMSE=" + this.meanMSE + "]");
		}
	}
	
	cycle[] ar_run = null;
	
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
	public cmcMaxLloydQuantization(cmcProcSettings is,logLiason ilog)
	//------------------------------------------------------------
	{
		xMSet = is;
		logger=ilog;
		statter = new gpFrequencyDistributionStatFunctions();
	}

	//------------------------------------------------------------
	private void initialize(int totalbins , int reducedbins )
	//------------------------------------------------------------
	{
		ar_run = new cycle[ MAX_RUNS ];
		for(int i=0;i<ar_run.length;i++) 
		{
			ar_run[i] = new cycle( i , totalbins , reducedbins);
		}
		bfr_centers = new double[ reducedbins ];
	}

	//------------------------------------------------------------
	public double[] performMaxLloydQuantizationDOUBLE( int[] histo , int targetlevel )
	//------------------------------------------------------------
	{
			 return performMaxLloydQuantization( histo , targetlevel );
	}
	
	//------------------------------------------------------------
	public int[] performMaxLloydQuantizationINT( int[] histo , int targetlevel )
	//------------------------------------------------------------
	{
		 double[] dret = performMaxLloydQuantization( histo , targetlevel );
		 if( dret == null )  return null;
		 int[] ret = new int[dret.length];
		 for(int i=0;i<dret.length;i++) ret[i] = (int)Math.round( dret[i] );
		 return ret;
	}
	
	//------------------------------------------------------------
	public int[] getCentersINT()
	//------------------------------------------------------------
	{
	    int[] ret = new int[ bfr_centers.length ];
	    for(int i=0;i<bfr_centers.length;i++) ret[i] = (int)Math.round( bfr_centers[i] );
	    return ret;
	}
	
	//------------------------------------------------------------
	private double[] performMaxLloydQuantization( int[] histo , int targetlevel )
	//------------------------------------------------------------
	{
		// DEBUG
		if( DEBUG ) {
		 String sl= "HISTO in ";
		 for(int i=0;i<histo.length;i++) sl += "[" + histo[i] + "]";
		 do_log( 1 , sl );		
		}			
		
		//		
		initialize( histo.length , targetlevel );
		
		// Seed the first runs: set arbitrary or random centers
		// in this case just distribute the centers evenly across all bins
		double dbinwi =  (double)histo.length / (double)targetlevel;
		double dstart = (dbinwi / (double)2);
		for(int i=0;i<targetlevel;i++)
		{
			 ar_run[0].ar_center[i] = dstart;
			 dstart += dbinwi;
		}
		ar_run[0].setBoundaries();
		
		try {
		// iterate
		double prevmse = -1;
		for(int z=0;z<ar_run.length-1;z++)
		{
			cycle cyc = ar_run[z];
			// calculate the mean per segment
			for(int i=0;i<cyc.reducedBins;i++)
			{
				// populate a minihistogram for each segment
				int left  = (int)cyc.ar_left[i];
				int right = (int)cyc.ar_right[i];
				int gap = right - left;
				if( gap <= 1 ) {
					do_error( "System error - Not a valid segment");
					return null;
				}
				int[] ar_minihisto = new int[ gap ];
				for(int j=0;j<ar_minihisto.length;j++) ar_minihisto[j]=-1;
				/*
				for(int j=0;j<histo.length;j++)
				{
				 if( (j>=left) && (j<right) ) {  // right is exclusive
					 ar_minihisto[ j - left ] = histo[j];
				 }
				}
				*/
				for(int j=left;j<right;j++)
				{
					ar_minihisto[ j - left ] = histo[j];
				}
				// check whether histogram is fully populated
				int sumVal=0;
				for(int j=0;j<ar_minihisto.length;j++) {
					if( ar_minihisto[j] < 0 ) {
						do_error("System error - mini histogram is not populated");
						return null;
					}
					sumVal += ar_minihisto[j];
				}
				// check whether the histogram is empty (if so set MSE to large and average to 0)
				if( sumVal <= 0 ) {
					cyc.ar_mean[i] = left;   // mean is zero
					cyc.totalMSE = cyc.meanMSE = Double.MAX_VALUE / ((double)cyc.reducedBins * 4);  // large value
				}
				else {
                    // mean and MSE
				    cyc.ar_mean[i] = left + statter.getMeanDouble( ar_minihisto );
				    cyc.ar_mse[i] = statter.getStdDevDouble( ar_minihisto );
				    //  total mse
				    cyc.totalMSE = cyc.meanMSE = 0;
				    for(int j=0;j<cyc.ar_mse.length;j++) cyc.totalMSE += cyc.ar_mse[j];
				    cyc.meanMSE= cyc.totalMSE / (double)cyc.ar_mse.length;
				}
				
			}   // all M bins have been processed (mean and MSE have been calcualted)
			
			
			// close current cycle
			cyc.valid = true;
			//cyc.dump();
			//cyc.msedump();
		    
			// Has the EXIT condition been met
			if( (z>=2) && ( prevmse < cyc.meanMSE) ) {  // perform at least 1 corrected run
				cyc.valid = false;
				if( DEBUG ) do_log( 1 , "Break -> MSE is increasing after [" + (z+1) + "] runs");
				break;
			}
			prevmse = cyc.meanMSE;
			
			
			// initialize the next run
			cycle next = ar_run[z+1];
			for(int j=0;j<cyc.ar_mean.length;j++)
			{
				next.ar_center[j] = cyc.ar_mean[j];
			}
			next.setBoundaries();
			//
			// perform a check 
			// the starting points of the segments must be in ascending order
			// a segments must at least be 1
			boolean isOK=true;
			for(int j=0;j<next.ar_mean.length-1;j++)
			{
				if( next.ar_center[j] >= next.ar_center[j+1] ) {
					next.valid = false;
					isOK=false;
					if( DEBUG ) do_log( 1 , "Break -> centers not ascending");
					break;
				}
				int left  = (int)next.ar_left[j];
				int right = (int)next.ar_right[j];
				int gap = right - left;
				if( gap <= 1 ) {
					if( DEBUG ) do_log( 1 , "Break -> Empty segment");
					isOK=false;
					break;
				}
			}
			if( isOK == false ) break;
			// debug
			//if( z > 30 ) break;
		}  // Z
		}
		catch(Exception e ) {
			e.printStackTrace();
			return null;
		}
		
		// just get the last valid entry.
		int idx=-1;
		for(int i=0;i<ar_run.length;i++)
		{
			if( ar_run[i].valid == false ) break;
			idx = i;
		}
		if( idx < 0 ) {
			do_error( "Could not determine a valid run");
			return null;
		}
		if( idx == 0 ) {
			do_log(1,"Algoritm already stopped after the first run");
		}
		cycle best = ar_run[idx];
		if( DEBUG ) best.dump();
		//
		double[] ret = new double[ best.ar_left.length];
		for(int i=0;i<ret.length;i++) {
			ret[i] = best.ar_left[i];
			bfr_centers[i] = best.ar_center[i];
		}
		return ret;
	}
	
}
