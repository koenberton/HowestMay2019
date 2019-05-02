package generalStatPurpose;

import logger.logLiason;

public class gpStatAdditionalFunctions {

	logLiason logger=null;
	gpDoubleListStatFunctions stat = null;
	
	private double covariance =  Double.NaN;
	private double correlation = Double.NaN;
	
	
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
    
    public gpStatAdditionalFunctions()
    {
    	stat = new gpDoubleListStatFunctions(null,null);
    	initialize();
    }
    
    public double getCovariance() {
		return covariance;
	}
	public double getCorrelation() {
		return correlation;
	}
	public void setCovariance(double covariance) {
		this.covariance = covariance;
	}
	public void setCorrelation(double correlation) {
		this.correlation = correlation;
	}
	private void initialize()
    {
    	covariance = Double.NaN;
    	correlation = Double.NaN;
    }
	
	//------------------------------------------------------------
    public boolean calcCovariance( double[] alist , double[] blist )
    //------------------------------------------------------------
    {
    	initialize();
    	if( alist == null ) { do_error("Null alist"); return false; }
    	if( blist == null ) { do_error("Null blist"); return false; }
    	if( alist.length != blist.length ) { do_error("Lists have different number of elements"); return false; }
    	if( alist.length == 0 ){ do_error("Empty alist"); return false; }
    	if( blist.length == 0 ){ do_error("Empty blist"); return false; }
    	
    	//
    	if( stat.calculate( alist ) == false ) { do_error("Error calculating stats on alist"); return false; }
    	double amean = stat.getMean();
    	double astddev = stat.getStandardDeviation();
    	//
    	if( stat.calculate( blist ) == false )  { do_error("Error calculating stats on alist"); return false; }
    	double bmean = stat.getMean();
    	double bstddev = stat.getStandardDeviation();
    	
    	//
    	int nvals = alist.length;
    	double covarsum=0;
	    double corsum = 0;
	    double cordiv =  astddev * bstddev;
	    for( int i=0;i<nvals;i++)
	    {
	         covarsum += (alist[i] - amean) * (blist[i] - bmean);	 
	         if( cordiv != 0 )  corsum +=  ((alist[i] - amean) * (blist[i] - bmean)) / cordiv;
	    }
	    covariance =  covarsum / nvals;   //   somtime N-1?
	    correlation = (cordiv == 0) ? Double.NaN : corsum / nvals;
    	
    	
    	return true;
    }
    
}
