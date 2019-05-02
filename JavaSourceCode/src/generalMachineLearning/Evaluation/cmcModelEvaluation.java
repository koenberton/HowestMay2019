package generalMachineLearning.Evaluation;


public class cmcModelEvaluation {

	private static String ctEOL =  System.getProperty("line.separator");
			
	String[] ClassNames=null;
	int[][] confusion = null;
	double  Accuracy=0;
	double[] precision = null;
	double[] recall = null;
	int totalEntries=0;
	
	
	//------------------------------------------------------------
	public cmcModelEvaluation(String[] classnames )
	//------------------------------------------------------------
	{
		if( classnames != null ) {
		  int nclasses = classnames.length;
		  ClassNames = new String[ nclasses ];
		  precision = new double[ nclasses ];
		  recall = new double[ nclasses ];
		  confusion = new int[ nclasses ][ nclasses ];
		  for(int i=0 ; i<nclasses ; i++) 
		  { 
			  ClassNames[i] = classnames[i];
			  precision[i] = recall [i] = 0;
			  for( int j=0;j<nclasses;j++) { confusion[i][j]=0; }
		  }
	    }
	}
	//------------------------------------------------------------
	public boolean addToConfusion( String calculated , String anticipated)
	//------------------------------------------------------------
	{
		int predicted = -1; // calculated-predicted
		int actual = -1; // actual
		for(int i=0;i<this.ClassNames.length;i++)
		{
			if( anticipated.compareToIgnoreCase( ClassNames[i] ) == 0 ) actual = i;
			if( calculated.compareToIgnoreCase( ClassNames[i] )  == 0 ) predicted = i;
		}
		if( (actual < 0) || (predicted < 0) ) {
			//do_error("cannot allocate [" + anticipated + "," + calculated + "]");
			return false;
		}
		confusion[ predicted ][ actual ]++;
		return true;
	}
	//------------------------------------------------------------
	public boolean injectMatrix( int[][] inj)
	//------------------------------------------------------------
	{
			confusion = inj;
			return true;
	}
	//------------------------------------------------------------
	public boolean calculate()
	//------------------------------------------------------------
	{
		int nclasses = confusion.length;
		int[] truePositivesPerClass = new int[ nclasses ];
		int[] falsePositivesPerClass = new int[ nclasses ];
		int[] trueNegativesPerClass = new int[ nclasses ];
		int[] falseNegativesPerClass = new int[ nclasses ];
		int[] elementsPerRow = new int[ nclasses ];
	    for(int i=0 ; i < nclasses ; i++) 
    	{
			truePositivesPerClass[i] = elementsPerRow[i] = falsePositivesPerClass[i] = trueNegativesPerClass[i] = falseNegativesPerClass[i] = 0;
    	}
	    // Process per row
		for(int i=0 ; i < nclasses ; i++)  // vertical -> row = predicted
    	{
			for(int j=0;j<nclasses;j++)  // horizontal -> cols = actual
			{
				if( i == j ) truePositivesPerClass[i] += confusion[i][j];  
				        else falsePositivesPerClass[i] += confusion[i][j];
			}
    	}
		// process per column
		for(int j=0;j<nclasses;j++)
		{
			for(int i=0;i<nclasses;i++) 
			{
			   if( i != j) falseNegativesPerClass[ j ] += confusion[i][j];
			}
		}
		//
		totalEntries = 0;
		for(int i=0 ; i < nclasses ; i++)  // row = predicted
    	{
			for(int j=0;j<nclasses;j++)  // cols = actual
			{
				totalEntries += confusion[i][j];
			}
    	}
        //
		int truePos = 0;
		for(int i=0;i<nclasses;i++)
		{
			truePos += truePositivesPerClass[i];
			int ctot = truePositivesPerClass[i] + falsePositivesPerClass[i] + falseNegativesPerClass[i];
			trueNegativesPerClass[i] = totalEntries - ctot;
		}
		
	    this.Accuracy = (double)truePos / (double)totalEntries;
		for(int i=0;i<nclasses;i++)
	    {
		 int noe = truePositivesPerClass[i] + falsePositivesPerClass[i];	
	     precision[i] = (noe == 0) ? Double.NaN : (double) truePositivesPerClass[i] / (double)noe;
		 noe = truePositivesPerClass[i] + falseNegativesPerClass[i];	
	     recall[i] = (noe == 0) ? Double.NaN : (double)truePositivesPerClass[i] / (double)noe;	
	    }
		return true;
	}
	//------------------------------------------------------------
	public String showConfusionMatrix()
	//------------------------------------------------------------
	{
		this.calculate();  // just in case
		//
		String sRet = ctEOL + "               ";
		for(int i=0 ; i < confusion.length ; i++) sRet += String.format("%12s", ClassNames[i] );
		sRet += " ---> Actual" + ctEOL +  "                -----------  ----------  ----------";
	    for(int i=0 ; i < confusion.length ; i++)
    	{
    		String sl= String.format( "%-15s" , ClassNames[i]);
    		for(int j=0;j<confusion[i].length;j++) sl += "[" + String.format( "%10d" , confusion[i][j] ) + "]";
    		sRet += ctEOL + sl;
    	}
	    //    	
		sRet += ctEOL + ctEOL + "                  Precision  Sensitivity    FMeasure";
		sRet += ctEOL +  "                -----------  -----------  ----------";
		for(int i=0;i<ClassNames.length;i++)
		{
	   		String sl= String.format( "%-15s" , ClassNames[i]) + "[";
			sl += String.format("% 10.6f", getPrecisionPerClass(ClassNames[i])) + "][ " + 
			      String.format("% 10.6f", getRecallPerClass(ClassNames[i])) + "][" + 
				  String.format("% 10.6f", getFValuePerClass(ClassNames[i])) + "]";
			sRet += ctEOL + sl;
		}
        //		
		return sRet;
	}
	public double getAccuracy()
	{
 		return this.Accuracy;
	}
	public int getTotalEntries()
	{
 		return this.totalEntries;
	}
	private int getClassIdx( String name )
	{
		for(int i=0;i<ClassNames.length;i++)
		{
			if( ClassNames[i].compareToIgnoreCase(name) == 0 ) return i;
		}
		return -1;
	}
	public double getPrecisionPerClass( String name)
	{
       int idx = getClassIdx( name );
       if( idx < 0 ) return Double.NaN;
       return this.precision[ idx ];
	}
	public double getRecallPerClass( String name)
	{
       int idx = getClassIdx( name );
       if( idx < 0 ) return Double.NaN;
       return this.recall[ idx ];
	}
	public double getFValuePerClass( String name)
	{
	   try {
       int idx = getClassIdx( name );
       if( idx < 0 ) return Double.NaN;
       double p = precision[idx];
       double r = recall[idx];
       if( (Double.isNaN(r)) || (Double.isNaN(p)) ) return Double.NaN;
       double n = p + r;
       if( n == 0 ) return Double.NaN;
       return  2 * p * r / n;
	   }
	   catch(Exception e ) { return Double.NaN; }
	}
	public double getMeanPrecision()
	{
	   try {
		    double sum = 0;
		    for(int i=0;i<ClassNames.length;i++)
			{
			   double p = precision[i];
		       if(  Double.isNaN(p) ) return Double.NaN;
		       sum +=  p;
			}
            return sum / ClassNames.length;     
	   }
	   catch(Exception e ) { return Double.NaN; }
	}
	public double getMeanFValue()
	{
	   try {
		    double sum = 0;
		    for(int i=0;i<ClassNames.length;i++)
			{
			   double p = precision[i];
		       double r = recall[i];
		       if( (Double.isNaN(r)) || (Double.isNaN(p)) ) return Double.NaN;
		       double n = p + r;
		       if( n == 0 ) return Double.NaN;
		       sum +=  2 * p * r / n;
			}
            return sum / ClassNames.length;     
	   }
	   catch(Exception e ) { return Double.NaN; }
	}

}
