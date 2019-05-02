package generalMachineLearning.LogisticRegression;

import cbrTekStraktorModel.cmcProcSettings;
import generalImagePurpose.cmcImageRoutines;
import generalMachineLearning.cmcMachineLearningConstants;
import generalMachineLearning.ARFF.ARFFCategory;
import generalMachineLearning.ARFF.cmcARFF;
import generalMachineLearning.ARFF.cmcTextToARFF;
import generalMachineLearning.LinearAlgebra.cmcMatrix;
import generalStatPurpose.gpDiagram;
import generalStatPurpose.gpDiagramDTO;
import logger.logLiason;

public class cmcLinearRegression {
	
	cmcProcSettings xMSet=null;
	logLiason logger=null;
	
	private String LastErrorMsg = null;
	private double[] weights = null;
	private String RegressionImageFileName = null;
	private int canvas_width = -1;
	private int canvas_heigth = -1;
	private double MeanSquareError=-1;
	
	
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
    	LastErrorMsg=sIn;
    	do_log(0,sIn);
    }
    public String getLastErrorMsg()
    {
    	return LastErrorMsg;
    }
    
    //------------------------------------------------------------
    public cmcLinearRegression(cmcProcSettings is,logLiason ilog)
    //------------------------------------------------------------
    {
    	xMSet = is;
		logger=ilog;
    }
    
    //------------------------------------------------------------
    public int getCanvasWidth()
    //------------------------------------------------------------
    {
    	return canvas_width;
    }
    //------------------------------------------------------------
    public int getCanvasHeigth()
    //------------------------------------------------------------
    {
    	return canvas_heigth;
    }
    //------------------------------------------------------------
    public String getRegressionFileName()
    //------------------------------------------------------------
    {
    	return RegressionImageFileName;
    }
    //------------------------------------------------------------
    public double[] getWeights()
    //------------------------------------------------------------
    {
    	return weights;
    }
    //------------------------------------------------------------
    public double getMeanSquareError()
    //------------------------------------------------------------
    {
    	return MeanSquareError;
    }
    //------------------------------------------------------------
    private boolean checkLinearCategories( ARFFCategory[] categories, String[] featuresToCorrelate )
    //------------------------------------------------------------
    {
    	if( categories == null ) { do_error("Null category list"); return false; }
        if( categories.length <= 0 ) { do_error("Empty category list"); return false; }
    	
        // look for the features to correlate and invalidate the other ones
        boolean foundclass=false;
        boolean[] repo = new boolean[2];  repo[0] = repo[1] = false;
        for(int i=0 ; i<categories.length ; i++)
        {
        	ARFFCategory cat = categories[i];
        	if( cat == null ) continue;
        	if( cat.getTipe() == cmcARFF.ARFF_TYPE.CLASS ) { // class
        		foundclass=true;
        		if( i != (categories.length-1) ) {
        			do_error( "CLASS is not on last column");
        			return false;
        		}
                continue;		
        	}	
        	if( cat.getTipe() == cmcARFF.ARFF_TYPE.NUMERIC ) {
        	    if( cat.getName().compareToIgnoreCase( featuresToCorrelate[0] ) == 0 ) { repo[0] = true; continue; } // OK
        	    if( cat.getName().compareToIgnoreCase( featuresToCorrelate[1] ) == 0 ) { repo[1] = true; continue; } // OK
        	}
        	categories[i] = null;   // invalidate
        }
        if( foundclass == false ) {
        	do_error( "There is no target class on the ARFF");
        	return false;
        }
        // there must be exactly 3 categories left
        int cntr=0;
        for(int i=0 ; i<categories.length ; i++)
        {
        	if(  categories[i] != null ) cntr++;
        }
        if( cntr != 3 ) {
        	do_error( "Could not find the features to be correlated - or the features are not numeric [" + cntr + "]" + "\n" +
        	   "Feature [" + featuresToCorrelate[0] + "] found=" + repo[0] + "\n" +
        	   "Feature [" + featuresToCorrelate[1] + "] found=" + repo[1] );
        	return false;
        }
        return true;
    }

    //------------------------------------------------------------
    private boolean swapLinearCategories( ARFFCategory[] categories, String[] featuresToCorrelate )
    //------------------------------------------------------------
    {
    	 boolean[] repo = new boolean[2];  repo[0] = repo[1] = false;
         for(int i=0 ; i<categories.length ; i++)
         {
         	ARFFCategory cat = categories[i];
         	if( cat == null ) continue;
         	if( cat.getName().compareToIgnoreCase( featuresToCorrelate[0] ) == 0 ) return false;
         	return true;
         }	
         return false;
    }
    
    //------------------------------------------------------------
    public boolean testLinearRegression(String OriginalFileName , String[] featuresToCorrelate , int degree , int ncycles , double stepsize)
    //------------------------------------------------------------
    {
    	RegressionImageFileName = null;
    	canvas_width = -1;
    	canvas_heigth = -1;
    	MeanSquareError = Double.NaN;
    	if( featuresToCorrelate ==  null ) return false;
    	if( featuresToCorrelate.length != 2) return false; //  defines the 2 features to correlate
    	if( featuresToCorrelate[0].compareToIgnoreCase( featuresToCorrelate[1] ) == 0 ) {
    		do_error( "Regression on same features is not (yet) supported");
    		return false;
    	}
    	if( (stepsize<=0) || (stepsize>1) ) {
    		do_error( "Stepsize should be between 0 and 1");
    		return false;
    	}
    	//
       	String DiagramFileName = xMSet.xU.getFileNameWithoutSuffix(OriginalFileName);
    	if( DiagramFileName == null ) { do_error("Cannot strip suffix"); return false; }
        DiagramFileName = DiagramFileName.trim() + ".png";
        //
    	cmcTextToARFF txt = new cmcTextToARFF( xMSet , logger );
    	ARFFCategory[] categories = txt.getCategories( OriginalFileName , DiagramFileName , cmcMachineLearningConstants.NBR_BINS , false ); // no training set needed
    	//
        if( checkLinearCategories(categories , featuresToCorrelate ) == false ) return false;
     	//
    	String[] ClassNameList = txt.getClassNames(categories);
    	if( ClassNameList == null ) { do_error("Null class name list"); return false; }
    	if( ClassNameList.length <= 0) { do_error("Empty class name list"); return false; }
        //
        cmcMatrix[] DataTargetTuple = txt.transformIntoMatrix( categories ); // 0 = data  1 = Target
        if( DataTargetTuple == null ) return false;
        if(  DataTargetTuple[0].getNbrOfColumns() != 2 ) {
        	do_error("There must be exactly 2 columns on the data matrix");
        	return false;
        }
        //  swap first and second column of data in order to match the order of the categories on the feaure2correlate list
        if(  swapLinearCategories(categories , featuresToCorrelate ) ) {
        	for(int i=0;i<DataTargetTuple[0].getNbrOfRows();i++)
        	{
        		double dd = DataTargetTuple[0].getValues()[i][0];
        		DataTargetTuple[0].setValue( i , 0 , DataTargetTuple[0].getValues()[i][1] );
        		DataTargetTuple[0].setValue( i , 1 , dd );
        	}
        	do_log( 1 , "Swapped " + featuresToCorrelate[1] + " <->" + featuresToCorrelate[0] );
        }
        do_log( 1 , "Attempting Lineair regression for [" + DataTargetTuple[0].getNbrOfRows()  + "] pairs (" + featuresToCorrelate[0] + "," + featuresToCorrelate[1] + ")");
        //
        boolean ib = doLineairRegression( DataTargetTuple[0] , ncycles , degree , stepsize );
        if( ib == false ) return false;
        
        // diagram
        // make DTO
        double[] arx = new double[ DataTargetTuple[0].getNbrOfRows() ];
        double[] ary = new double[ arx.length ];
        double[] arr = new double[ arx.length ];
        for(int i=0;i<DataTargetTuple[0].getNbrOfRows();i++)
        {
        	arx[i] = DataTargetTuple[0].getValues()[i][0];
        	ary[i] = DataTargetTuple[0].getValues()[i][1];
        	arr[i] = DataTargetTuple[1].getValues()[i][0];
        }
        gpDiagramDTO dto = new gpDiagramDTO( arx , ary , arr);
        dto.setxLabel( featuresToCorrelate[0] );
        dto.setyLabel( featuresToCorrelate[1] );
       
        //
        gpDiagram dia = new gpDiagram(xMSet,logger);
        
        // calculate curve
        double minx = Double.NaN;
        double maxx = Double.NaN;
        for(int i=0;i<arx.length;i++)
        {
        	if( i == 0 ) minx=maxx=arx[i];
        	if( minx > arx[i] ) minx=arx[i];
        	if( maxx < arx[i] ) maxx=arx[i];
        }
        if( Double.isNaN( minx ) ) {
        	do_error("Cannot determine minimum");
        	return false;
        }
        if( Double.isNaN( maxx ) ) {
        	do_error("Cannot determine maximum");
        	return false;
        }
        if( minx >= maxx ) {
        	do_error("Minimum and maximum mismatch " + minx + " " + maxx );
        	return false;
        }
        int span = dia.getRectangleSide();  // will always be more then necessary, so OK
        if( span <= 0 ) {
        	do_error( "invalid span " + span );
        	return false;
        }
        double dstep = (maxx - minx) / (double)span;
        double dx = minx - dstep;
        double[][] curve = new double[ span ][ 2 ];
        do_log( 1 , "Min" + minx + "  Max" + maxx + " Span" + span);
        for(int i=0;i<span;i++)
        {
        	dx += dstep;
        	curve[i][0] = dx;
        	double ycalc = 0;
        	for(int j=0 ; j<weights.length;j++)
        	{
        		ycalc += Math.pow(  dx , (double)j ) * weights[j];
        	}
        	curve[i][1] = ycalc;
        }
        dto.setDoubleArray( curve );
        
        // draw diagram
        int[] canvas = dia.drawScatterPlot(dto);
        if( canvas == null ) return false;
        canvas_width = dia.getRectangleSide();
        canvas_heigth = canvas.length/canvas_width;
        
        // dump diagram
        int len = 7;
        String one = featuresToCorrelate[0].length()<=len ? featuresToCorrelate[0] : featuresToCorrelate[0].substring(0,len);
        String two = featuresToCorrelate[1].length()<=len ? featuresToCorrelate[1] : featuresToCorrelate[1].substring(0,len);
        one = (one + "-" + two).toLowerCase();
        DiagramFileName = xMSet.xU.getFileNameWithoutSuffix(OriginalFileName) + "-LinReg-" + one + ".png";
        if( xMSet.xU.IsBestand( DiagramFileName ) ) xMSet.xU.VerwijderBestand( DiagramFileName );
        cmcImageRoutines irout = new cmcImageRoutines(logger);
        irout.writePixelsToFile( canvas , canvas_width , canvas_heigth ,  DiagramFileName , cmcImageRoutines.ImageType.RGB );
        if( xMSet.xU.IsBestand( DiagramFileName ) == false ) return false;
        RegressionImageFileName = DiagramFileName;
        do_log( 1, "dumped to [" + getRegressionFileName() + "]");
        //
        canvas=null;
        irout=null;
        dia=null;
        dto=null;
        //
        return true;
    }
    
    private boolean doLineairRegression( cmcMatrix data , int NbrOfCycles , int degree , double stepsize)
    {
    	// Initialize weights
    	weights = new double[ degree + 1 ];
    	for(int i=0;i<weights.length;i++) weights[i] = 0;
    	// precalculate the x^n values - you will need those continuously
    	double[][] xn = new double[ data.getNbrOfRows() ][ weights.length ];
    	for(int i=0;i<data.getNbrOfRows();i++)
		{
		    double x = data.getValues()[i][0];
		    for(int j=0;j<weights.length;j++)
			{
				xn[i][j] = Math.pow(  x , (double)j );
		    }
		}	
    	// ALGO
    	for(int cycle=0;cycle<NbrOfCycles;cycle++)
    	{
    		// Gradient
    		double[] gradient = new double[ weights.length ];
    		for( int i=0;i<gradient.length;i++) gradient[i]=0;
    		for(int i=0;i<data.getNbrOfRows();i++)
    		{
    			double y = data.getValues()[i][1];    // algo only works op sets of 2 features
    		    //  CURVE => y = w2.(x2^2)  + w1.(x1^1) + w0.(x0^0)  etc
    			double yi = 0; // the calculated value of y using the current weights
    			for(int j=0;j<weights.length;j++)
    			{
    				yi += xn[i][j] * weights[j];
    		    }
    			// calculate the gradient    (y - calculatedY).x power 
    			for(int j=0;j<weights.length;j++)
    			{
    				gradient[j] += (y - yi) * xn[i][j];		// 1/N  - but postpone until end
    			}
    			
    		}
    		for( int i=0;i<gradient.length;i++) gradient[i]=gradient[i] / data.getNbrOfRows();
    		// update weights
    		for( int i=0;i<gradient.length;i++)
    		{
    			weights[i] = weights[i] + (stepsize * gradient[i] );  //  + or - does not really matter
    		}
    		// calculate error with new weights
    		double lms = 0;
    		for(int i=0;i<data.getNbrOfRows();i++)
    		{
    			double y = data.getValues()[i][1];
    		    //  CURVE => y = w2.(x2^2)  + w1.(x1^1) + w0.(x0^0)  etc
    			double yi = 0; // the calculated value of y using the current weights
    			for(int j=0;j<weights.length;j++)
    			{
    				yi += xn[i][j] * weights[j];
    		    }
    			lms += Math.pow( (y - yi), (double)2);
    		}	
    		lms = lms / ( 2 * data.getNbrOfRows() );
    		MeanSquareError = lms;
    	}
  
    	// display
    	for(int i=0;i<data.getNbrOfRows();i++)
		{
			double x = data.getValues()[i][0];
			double y = data.getValues()[i][1];
		    //  CURVE => y = w2.(x2^2)  + w1.(x1^1) + w0.(x0^0)  etc
			double yi = 0; // the calculated value of y using the current weights
			for(int j=0;j<weights.length;j++)
			{
				yi += xn[i][j] * weights[j];
		    }
			//do_log( 1 , "(" + x + "," + y + ") -> " + yi + "       e=" + (y -yi) );
		}	
      	String sl="";
    	for( int i=0;i<weights.length;i++) sl += "w" + i + "=" + weights[i]+ " ";
    	do_log( 1 , sl +  " LMS=" + MeanSquareError );
  
    	return true;
    }
    
    	
    
    
}
