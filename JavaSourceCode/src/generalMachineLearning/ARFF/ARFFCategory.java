package generalMachineLearning.ARFF;

import cbrTekStraktorModel.cmcProcSettings;
import logger.logLiason;

public class ARFFCategory {

	    private int NBR_BINS = -1; 
	
		cmcProcSettings xMSet=null;
		logLiason logger=null;
		
		private String categoryName;
		private cmcARFF.ARFF_TYPE tipe = cmcARFF.ARFF_TYPE.UNKNOWN;
		int[] arfflineno;     // the rownumber in ARFF file
		double[] values;      // all feature value for this category
		double[] results;     // all results (class inex) for this category
		int[] binIndexes;     // list of the index of the bin to which a value belongs
		private double[] binsegmentbegins=null;  // the start of each bin - end is excluded !
		private int[] ClassCounts=null;
		private int[][] BinClassHistogram=null;
		private String[] NominalValueList=null;
		
		
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

	    public String getCategoryName()
	    {
	    	return getName();
	    }
	    public String getName()
	    {
	    	return this.categoryName;
	    }
	    public void setTipe(cmcARFF.ARFF_TYPE ti  )
	    {
	    	tipe = ti;
	    }
	    public cmcARFF.ARFF_TYPE getTipe()
	    {
	    	return this.tipe;
	    }
	    public void setNominalValueList( String[] nominallist  )
	    {
	    	NominalValueList = nominallist;
	    }
	    public String[] getNominalValueList()
	    {
	    	return this.NominalValueList;
	    }
	    public double[] getBinsegmentbegins()
	    {
	    	return binsegmentbegins;
	    }
	    public int[][] getBinClassHistogram()
	    {
	    	return BinClassHistogram;
	    }
	    public double[] getValues()
	    {
	    	return values;
	    }
	    public int[] getArfflineno()
	    {
	    	return arfflineno;
	    }
	    public double[] getResults()
	    {
	    	return results;
	    }
	    //------------------------------------------------------------
	    public ARFFCategory(cmcProcSettings is,logLiason ilog,String s , int nrows , cmcARFF.ARFF_TYPE ti , int nclasses , String[] nominallist , int nbrbins)
	    //------------------------------------------------------------
	    {
	    	xMSet = is;
			logger=ilog;
			NBR_BINS = nbrbins;
			//
			categoryName = s;
			tipe = ti;
			binsegmentbegins  = new double[NBR_BINS]; 
			values            = new double[nrows]; 
			results           = new double[nrows]; 
			binIndexes        = new int[nrows];
			arfflineno        = new int[nrows];
			ClassCounts       = new int[nclasses];
			BinClassHistogram = new int[ NBR_BINS ][ nclasses ];
			NominalValueList  = ((ti == cmcARFF.ARFF_TYPE.NOMINAL)||(ti == cmcARFF.ARFF_TYPE.CLASS)) ? nominallist : null;
			//
			for(int i=0;i<nrows;i++)
			{
				values[i] = results[i] = Double.MIN_VALUE;
				binIndexes[i] = -1;
				arfflineno[i] = -1;
			}
			for(int i=0;i<nclasses;i++)
			{
				ClassCounts[i] = 0;
				for(int j=0;j<NBR_BINS;j++) BinClassHistogram[j][i]=0;
			}
	    }
	    
	    //------------------------------------------------------------
	    public boolean performSort()
	    //------------------------------------------------------------
	    {
	        if( this.tipe != cmcARFF.ARFF_TYPE.NUMERIC ) return true;
	    	for(int z=0 ; z<this.values.length ; z++)
	    	{
	    		boolean swap = false;
	    		for(int k=0 ; k<(this.values.length - 1) ; k++)
	    		{
	    			if( this.values[k] <= this.values[k+1] ) continue;
	    			double d1            = this.values[k];
	    			double d2            = this.results[k];
	    			int i1               = this.binIndexes[k];
	    			int i2               = this.arfflineno[k];
	    			//
	    			this.values[k]       = this.values[k+1];
	    			this.results[k]      = this.results[k+1];
	    			this.binIndexes[k]   = this.binIndexes[k+1];
	    			this.arfflineno[k]   = this.arfflineno[k+1];
	    			//
	    			this.values[k+1]     = d1;
	    			this.results[k+1]    = d2;
	    			this.binIndexes[k+1] = i1;
	    			this.arfflineno[k+1] = i2;
	    			swap=true;
	    		}
	    		if( swap == false ) return true;
	    	}
	    	return true;
	    }
	    
	    // re-order on the linenumber
	    //------------------------------------------------------------
	    public boolean restoreLineNumberOrder()
	    //------------------------------------------------------------
	    {
	        if( this.tipe != cmcARFF.ARFF_TYPE.NUMERIC ) return true;
	    	for(int z=0 ; z<this.values.length ; z++)
	    	{
	    		boolean swap = false;
	    		for(int k=0 ; k<(this.values.length - 1) ; k++)
	    		{
	    			if( this.arfflineno[k] <= this.arfflineno[k+1] ) continue;
	    			double d1            = this.values[k];
	    			double d2            = this.results[k];
	    			int i1               = this.binIndexes[k];
	    			int i2               = this.arfflineno[k];
	    			//
	    			this.values[k]       = this.values[k+1];
	    			this.results[k]      = this.results[k+1];
	    			this.binIndexes[k]   = this.binIndexes[k+1];
	    			this.arfflineno[k]   = this.arfflineno[k+1];
	    			//
	    			this.values[k+1]     = d1;
	    			this.results[k+1]    = d2;
	    			this.binIndexes[k+1] = i1;
	    			this.arfflineno[k+1] = i2;
	    			swap=true;
	    		}
	    		if( swap == false ) return true;
	    	}
	    	return true;
	    }
	    
	    
	    //------------------------------------------------------------
	    public boolean performBinning()
	    //------------------------------------------------------------
	    {
	        switch( this.tipe )
            {
            case NUMERIC : return performBinningNumeric();
            case NOMINAL : return performBinningNominal();
            case STRING  : return true;
            case CLASS   : return performBinningNominal();
            default      : { do_error("Unsupport class for binning [" + this.tipe + "]"); return false; }
            }
	    }
	    //------------------------------------------------------------
	    private boolean performBinningNumeric()
	    //------------------------------------------------------------
	    {
	    	cmcDiscretizationRoutines  disc = new cmcDiscretizationRoutines( xMSet , logger );
	    	int[] res = disc.simpleBinner( this.values , NBR_BINS );
	    	if( res == null )  { disc=null; return false; }
	    	if( res.length != this.values.length) {
	    		disc = null;
	    		do_error("number of entries does not match those on the values and the bins");
	    		return false;
	    	}
	    	this.binIndexes = new int[ res.length ];
	    	for(int i=0;i<res.length;i++) this.binIndexes[i] = res[i];
	    	// get the begin values of each bin
	    	this.binsegmentbegins = disc.getBinSegementStarts();
	    	if( this.binsegmentbegins == null ) {
	    		do_error("Could not fetch the begin values of each bin segement");
	    		disc = null;
	    		return false;
	    	}
	    	disc=null;
	    	return true;
	    }
	    //------------------------------------------------------------
	    private boolean performBinningNominal()
	    //------------------------------------------------------------
	    {
	    	if( this.values == null ) return false;
	    	// values is the index of the nominal or class value
	    	this.binIndexes = new int[ values.length ];
	    	for(int i=0 ; i<this.values.length ; i++)
	    	{
	    		int idx = (int)this.values[i];
	    		if( (idx<0) || (idx >= NBR_BINS) ) {
	    			do_error("Nominal index [" + idx + "] on row [" + i + "] is less than zero or more than " + NBR_BINS + " " + this.getCategoryName());
	    			return false;
	    		}
	    		this.binIndexes[ i ] = idx;
	    	}
	    	return true;
	    }

	    //------------------------------------------------------------
        public boolean populateHistogram()
	    //------------------------------------------------------------
        {
        	int nbrclasses = this.ClassCounts.length;
        	// check whether the results are between 0 .. nbrclasses
        	for(int i=0;i<this.results.length;i++)
        	{
        		if( (this.results[i] < 0) || (this.results[i] >= nbrclasses) ) {
        			do_error("Found a CLASS index not between 0 and " + nbrclasses + " for [" + i + "][" + this.results[i] + "]");
        			return false;
        		}
        	}
        	//
        	for(int i=0;i<NBR_BINS;i++)
        	{
        		for(int j=0;j<nbrclasses ; j++) this.BinClassHistogram[i][j]=0;
        	}
        	for(int i=0;i<NBR_BINS;i++)  // number of bins in the distribution
        	{
        		for(int j=0;j<nbrclasses;j++)  // text -mixed - grap
        		{
        			for(int z=0 ; z<this.binIndexes.length ; z++)
        			{
        				if( this.binIndexes[z] != i ) continue;
        				if( this.results[z] != j ) continue;
        				this.BinClassHistogram[i][j]++;
        				/*
        				if ( (this.arfflineno[z] == 55)  ) {
        					do_error( this.categoryName + " -> [bin=" + i + "][class=" + j + "] "+ this.values[z]  );
        				}
        				*/
        			}
        		}
        	}
            // counts per class
        	for(int i=0;i<this.ClassCounts.length;i++) this.ClassCounts[i] = 0;
        	for(int i=0;i<this.results.length;i++)
        	{
        	   	for(int j=0 ; j<nbrclasses;j++)
        	   	{
        	   		if( this.results[i] == j) this.ClassCounts[j]++;
        	   	}
        	}
        	
        	// check : the sum of all entries in the histogram must match the number of values
        	int total=0;
        	for(int i=0;i<this.BinClassHistogram.length;i++)
        	{
        		for(int j=0;j<this.BinClassHistogram[i].length ; j++) 
        		{
        			total += this.BinClassHistogram[i][j];
        		}
        	}
        	if( total != this.binIndexes.length ) {
        		do_error( "Overall total [" + total + "] does not match with expected [" + this.binIndexes.length );
        		return false;
        	}
        	// check : the sum of class totals must match the number of values
        	int tot=0;
        	for(int i=0;i<this.ClassCounts.length;i++)
        	{
        		tot += this.ClassCounts[i];
        	}
        	if( tot != this.binIndexes.length ) {
        		do_error( "Sum of classtotal [" + tot + "] does not match with expected [" + this.binIndexes.length );
        		return false;
        	}
        	// the sum of counts over all bins for a givenclass must macht the corresponding classtotal
        	for(int i=0;i<nbrclasses;i++)
        	{
        	   int subtot=0;
        	   for(int j=0 ; j<this.BinClassHistogram.length ; j++)
        	   {
        	     subtot += this.BinClassHistogram[j][i];   
        	   }

        	   if( subtot != this.ClassCounts[i] ) {
        		   do_error( "Sum of subclasstotal [" + subtot + "] does not match with expected [" + this.ClassCounts[i] + "] for class [" + i + "]");
           		   return false;
        	   }
        	}
        	//
        	return true;
        }
	    
}
