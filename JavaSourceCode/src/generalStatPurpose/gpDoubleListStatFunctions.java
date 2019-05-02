package generalStatPurpose;

import logger.logLiason;

public class gpDoubleListStatFunctions {
    
	logLiason logger=null;
	
	private double[] set=null;
	private int aantal=-1;
	private double mean=-1;
	private double maximum=-1;
	private double minimum=-1;
	private double median=-1;
	private double meanVariance=-1;
	private double standardDeviation=-1;
	private double quartile1=-1;
	private double quartile3=-1;
	private double iqr=-1;

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
	
	
	
	public int getAantal() {
		return aantal;
	}
	public void setAantal(int aantal) {
		this.aantal = aantal;
	}
	public double getMean() {
		return mean;
	}
	public void setMean(double mean) {
		this.mean = mean;
	}
	public double getMaximum() {
		return maximum;
	}
	public void setMaximum(double maximum) {
		this.maximum = maximum;
	}
	public double getMinimum() {
		return minimum;
	}
	public void setMinimum(double minimum) {
		this.minimum = minimum;
	}
	public double getMedian() {
		return median;
	}
	public void setMedian(double median) {
		this.median = median;
	}
	public double getMeanVariance() {
		return meanVariance;
	}
	public void setMeanVariance(double meanVariance) {
		this.meanVariance = meanVariance;
	}
	public double getStandardDeviation() {
		return standardDeviation;
	}
	public void setStandardDeviation(double standardDeviation) {
		this.standardDeviation = standardDeviation;
	}
	public double getQuartile1() {
		return quartile1;
	}
	public void setQuartile1(double quartile1) {
		this.quartile1 = quartile1;
	}
	public double getQuartile3() {
		return quartile3;
	}
	public void setQuartile3(double quartile3) {
		this.quartile3 = quartile3;
	}
	public double getIqr() {
		return iqr;
	}
	public void setIqr(double iqr) {
		this.iqr = iqr;
	}

	//------------------------------------------------------------
	private void reset()
	//------------------------------------------------------------
	{
			   // init
			   mean = Double.NaN;
			   median = Double.NaN;
			   meanVariance=Double.NaN;
			   maximum=Double.NaN;
			   minimum=Double.NaN;
			   quartile1=Double.NaN;
			   quartile3=Double.NaN;
			   standardDeviation=Double.NaN;
			   iqr=Double.NaN;
	}
	
	//------------------------------------------------------------
	public gpDoubleListStatFunctions(double[] is , logLiason ilog)
	//------------------------------------------------------------
	{
		logger=ilog;
	    reset();
	    if( is != null ) calculate(is);
	}
	
	//------------------------------------------------------------
	public boolean calculate(double[] is )
	//------------------------------------------------------------
	{
	    reset();
	    if( is == null ) return false; 
	    try {
	       //
	 	   // kopieren
	 	   aantal = is.length;	
	 	   if( aantal <= 0 ) return false;
	 	   set = new double[aantal];
	        for(int i=0;i<aantal;i++) {
	 		   set[i] = is[i];
	 		   if( i == 0 ) {
	 			   minimum = set[i];
	 			   maximum = set[i];
	 		   }
	 		   else {
	 			   if( minimum > set[i] ) minimum = set[i];
	 			   if( maximum < set[i] ) maximum = set[i];
	 		   }
	 	   }
	 	   // sorteren
	 	   boolean swap=false;
	 	   for(int i=0;i<aantal;i++)
	 	   {
	 		   swap=false;
	 		   for(int j=0;j<(aantal-1);j++)
	 		   {
	 			   if( set[j] > set[j+1] ) {
	 				   double k = set[j];
	 				   set[j] = set[j+1];
	 				   set[j+1] = k;
	 				   swap=true;
	 			   }
	 		   }
	 		   if( swap == false ) break;
	 	   }
	 	   //
		   // Rekenkundig gemiddelde
		   double sum=0;
		   for(int i=0;i<aantal;i++)
		   {
			   sum += (double)set[i];
		   }
		   mean = sum / (double)aantal;
		   //
		   // median
		   if( (!isEven( aantal)) || (aantal < 2) ) {  // Oneven neem gewoon de middelste
				int midi = aantal / 2;  // + 1 doch we tellen van zero
				median  = set[midi];
			}
			else {  // even, neem de waarde van DIV 2 en tal daarbij  de helft van her verschil van de waarden van DIV2 en 1+DIV2 
				int midi = aantal / 2;
			    double wdelta = (double)(set[ midi] - set[ midi - 1 ]) / 2;
				median = set[ midi -1 ] + wdelta;
			}   
		    //
		    // gemiddelde afwijking
		    double bmean =  sum / (double)aantal;
			double sq = 0;
			double dd=0;
			for(int i=0;i<aantal;i++)
			{
				dd = (double)set[i] - bmean;
				sq += (dd * dd);
			}
			if( aantal > 1 ) meanVariance = sq / ((double)aantal - 1); else meanVariance = sq;
			standardDeviation = Math.sqrt( meanVariance );
	 	   
	    }
	    catch(Exception e) {
		 reset();
		 do_error("oops " + e.getMessage() );
		 return false;	      
	    }
	    //
	    // quartilen
		// Q1 =  ( N + 1 ) / 4 
		// Q3 = (3N + 3 -) / 4
		int quart1idx = ((int)Math.round(((double)aantal + 1)/4) + 1 );
		int quart3idx = ((int)Math.round( ((3*(double)aantal + 3))/4) + 1 );
		// boundaries
		if( quart1idx >= aantal ) quart1idx = aantal - 1 ;
		if( quart1idx < 0 ) quart1idx = 0;
		if( quart3idx >= aantal ) quart3idx = aantal - 1 ;
		if( quart3idx < 0 ) quart3idx=0;
		//
		try {
				 quartile1 = set[quart1idx];
				 quartile3 = set[quart3idx];
				 iqr = quartile3 - quartile1;
		}
		catch(Exception e) {
			        reset();
					do_error("oops " + e.getMessage() );
					do_error("Q1IDX=" + quart1idx + " Q3IDX=" + quart3idx + "   Aantal=" + aantal + " " + aantal);
					return false;
		}
	    return true;
	}
	//
	//------------------------------------------------------------
	private boolean isEven(int i)
	//------------------------------------------------------------
	{
			double rest = i % 2;
			boolean even = (rest == 0 ? true : false);
			return even;
	}

}
