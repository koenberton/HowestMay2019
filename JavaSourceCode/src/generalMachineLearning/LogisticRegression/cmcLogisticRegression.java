package generalMachineLearning.LogisticRegression;

import cbrTekStraktorModel.cmcProcSettings;
import dao.cmcMachineLearningModelDAO;
import generalMachineLearning.cmcMachineLearningConstants;
import generalMachineLearning.ARFF.ARFFCategory;
import generalMachineLearning.ARFF.ARFFCategoryCoreDTO;
import generalMachineLearning.ARFF.cmcARFF;
import generalMachineLearning.ARFF.cmcARFFRoutines;
import generalMachineLearning.ARFF.cmcTextToARFF;
import generalMachineLearning.Evaluation.cmcModelEvaluation;
import generalMachineLearning.LinearAlgebra.cmcMatrix;
import generalMachineLearning.LinearAlgebra.cmcVector;
import generalMachineLearning.LinearAlgebra.cmcVectorRoutines;
import logger.logLiason;

public class cmcLogisticRegression {
	
	cmcProcSettings xMSet=null;
	logLiason logger=null;
	cmcVectorRoutines vrout = null;
	cmcARFFRoutines arffrout = null;
	
	private String LastErrorMsg = null;
	
	private int OptimalNbrOfCycles = -1;
	
	private cmcLogisticRegressionDTO lgdto = null;
	private cmcModelEvaluation eval = null;
	private cmcMatrix finalweight = null;
	
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
    public cmcLogisticRegression(cmcProcSettings is,logLiason ilog)
    //------------------------------------------------------------
    {
    	xMSet = is;
		logger=ilog;
		vrout = new cmcVectorRoutines();
    }
   
    //------------------------------------------------------------
    public int getOptimalNumberOfCycles()
    //------------------------------------------------------------
    {
    	return OptimalNbrOfCycles;
    }
    
    //------------------------------------------------------------
    private boolean checkLRCategories( ARFFCategory[] categories )
    //------------------------------------------------------------
    {
    	if( categories == null ) { do_error("Null category list"); return false; }
        if( categories.length <= 0 ) { do_error("Empty category list"); return false; }
    	// There must be exactly 2 classes for Logistic regression
        for(int i=0 ; i<categories.length ; i++)
        {
        	ARFFCategory cat = categories[i];
        	if( cat == null ) continue;
        	if( cat.getTipe() == cmcARFF.ARFF_TYPE.CLASS ) { // class
        		if( i != (categories.length-1) ) {
        			do_error( "CLASS is not on last column");
        			return false;
        		}
        		String[] ClassNames = cat.getNominalValueList();
        		if( ClassNames == null ) { do_error("System Error - NULL ClassNames"); return false; }
        		if( ClassNames.length != 2 ) { do_error("There must be exactly 2 target classes for Logistic Regression algoritm"); return false; }
        	}	
        }
        return true;
    }
    
    //------------------------------------------------------------
    public boolean testLogisticRegression(String OriginalFileName , String LongModelName , int MaxNbrOfCycles , double StepSize )
    //------------------------------------------------------------
    {
    	
       	String DiagramFileName = xMSet.xU.getFileNameWithoutSuffix(OriginalFileName);
    	if( DiagramFileName == null ) { do_error("Cannot strip suffix"); return false; }
        DiagramFileName = DiagramFileName.trim() + ".png";
        //
    	cmcTextToARFF txt = new cmcTextToARFF( xMSet , logger );
    	ARFFCategory[] categories = txt.getCategories( OriginalFileName , DiagramFileName , cmcMachineLearningConstants.NBR_BINS , true);
    	if( checkLRCategories(categories) == false ) return false;
    	String[] ClassNameList = txt.getClassNames(categories);
    	if( ClassNameList == null ) { do_error("Null class name list"); return false; }
    	if( ClassNameList.length <= 0) { do_error("Empty class name list"); return false; }
        //
        cmcMatrix[] DataTargetTuple = txt.transformIntoMatrix( categories ); // 0 = data  1 = Target
        if( DataTargetTuple == null ) return false;
        //
        lgdto = new cmcLogisticRegressionDTO( OriginalFileName );
        
        // normalize the data and store the normalized values
        cmcMatrix datamatrix = vrout.normalizeMatrix(  DataTargetTuple[0] );
        if( datamatrix == null ) return false;
        //do_error( "DATA ROWS=" + datamatrix.getNbrOfRows() + " COLS=" + datamatrix.getNbrOfColumns() );
        //
        lgdto.setNormalizerMean(vrout.getNormalizerMean());
        if( lgdto.getNormalizerMean() == null ) { do_error("Cannot fetch normalizer mean values"); return false; }
        if( lgdto.getNormalizerMean().length != datamatrix.getNbrOfColumns() ) { do_error("Normalizer Mean number of items mismatch "); return false;}
        //
        lgdto.setNormalizerStdDev(vrout.getNormalizerStdDev());
        if( lgdto.getNormalizerStdDev() == null ) { do_error("Cannot fetch normalizer STDDEV values"); return false; }
        if( lgdto.getNormalizerStdDev().length != datamatrix.getNbrOfColumns() ) { do_error("Normalizer STDDEV number of items mismatch "); return false;}
        
        // transform target in 0 and 1 (?? necessary - guess so)
        cmcMatrix resultmatrix = vrout.transform2ZeroAndOne( DataTargetTuple[1] );
        if( resultmatrix == null ) { do_error("Cannot transform to 0/1"); return false; }
        
        // We will first iterate through the entire range of optimal cycles 
        // the global variable optimal will be set to the optimal number of cycles
        boolean ib = simpleLogisticRegressionMatrix( datamatrix , resultmatrix , MaxNbrOfCycles , StepSize );
    	if( ib == false ) return false;
    	OptimalNbrOfCycles++;   // optimal is set via a for-loop, so you need to increase it
    	lgdto.setMaxCycles( MaxNbrOfCycles );
    	lgdto.setOptimalCycles(OptimalNbrOfCycles);
    	
    	// Redo and stop at optimal cycle
    	do_log( 1 , "Optimal number of cycles [" + OptimalNbrOfCycles + "]");
    	if( OptimalNbrOfCycles == MaxNbrOfCycles ) {
    		do_log( 1 , "Optimal number of cycles has not been reached"); // OK just use max then
    	}
    	// Rerun the algoritm but this time only for the optimal number of cycles
    	ib = simpleLogisticRegressionMatrix( datamatrix , resultmatrix , OptimalNbrOfCycles , StepSize );
    	if( ib == false ) return false;
        
        // prepare to write
        ARFFCategoryCoreDTO[] cclist = new ARFFCategoryCoreDTO[ categories.length ];
        for(int i=0 ; i<categories.length ; i++)
        {
        	ARFFCategory cat = categories[i];
        	if( cat == null ) continue;
        	cclist[i] = new ARFFCategoryCoreDTO(cat.getCategoryName());
        	cclist[i].setTipe( cat.getTipe() );
        	cclist[i].setNominalValueList(cat.getNominalValueList());
        }
        lgdto.setNbrOfFeatures( cclist.length );
        lgdto.setFeatures(cclist);
        lgdto.setStepSize(StepSize);
        
        //
        if( finalweight == null ) return false;
        if( finalweight.getNbrOfRows() != 1 ) { do_error( "finalweigt must be [M,1]"); return false; }
        double[] weights = new double[finalweight.getNbrOfColumns()];
        for(int i=0;i<finalweight.getNbrOfColumns();i++)
        {
        	weights[i] = finalweight.getValues()[0][i];
        }
        lgdto.setWeights(weights);
        //
        ib = dumpModel(LongModelName);
        if( ib == false ) return false;
        lgdto = null;
        
        // run model on the training set again to test it intrinsically
        do_log( 1 , "Evaluating");
        if( executeModel( LongModelName ,  txt.getTrainingSetName() ) == false ) return false;
        cmcModelEvaluation bres = eval;
        do_log( 1 , eval.showConfusionMatrix() );
        
        // run model on test set
        do_log( 1 , "Testing");
        if( executeModel( LongModelName ,  txt.getTestSetName() ) == false ) return false;
        do_log( 1 , eval.showConfusionMatrix() );
        
        //
        ib=updateModel( bres , eval , LongModelName );
        
        txt = null;
    	return ib;
    }
    
  
    //------------------------------------------------------------
    private boolean simpleLogisticRegressionMatrix(cmcMatrix mx, cmcMatrix tgtmtrx, int NbrOfCycles , double StepSize)
    //------------------------------------------------------------
    {   //  M= number of rows and  N=number of columns !!  sorry most of the times it is the other way round
    	if( mx == null ) { do_error("Null datamatrix matrix"); return false; }
    	int m = mx.getValues().length;
    	if( m != mx.getNbrOfRows() ) { do_error("Error 100"); return false; }
    	if( m <= 0 ) { do_error("no rows"); return false; }
    	int n = mx.getValues()[0].length;
    	if( n != mx.getNbrOfColumns() ) { do_error("Error 101"); return false; }
    	if( n <= 0 ) { do_error("no columns"); return false; }
    	//
    	if( tgtmtrx == null ) { do_error("Null target matrix"); return false; }
    	if( tgtmtrx.getNbrOfColumns() != 1 ) { do_error("Number of cols on target must be 1"); return false; }
    	if( tgtmtrx.getNbrOfRows() != m ) { do_error("Target en Data matrix - row mismatch " + tgtmtrx.getNbrOfRows() + " " + m); return false; }
    	// add a new first column comprising all 1  - augment
    	cmcMatrix dataMatrix = vrout.makeAugmentedMatrix( mx );
    	if( dataMatrix == null ) { do_error("Null augmented matrix"); return false; }
    	
    	// initialize weight matrix 1 row of N+1 weights  and set all weights to 1
    	double[][] tmp = new double[ 1 ][n + 1];
    	for(int i=0;i<tmp.length;i++) tmp[0][i]=1;
    	cmcMatrix weightMatrix = new cmcMatrix( tmp );
    	cmcMatrix prevWeight = null;
    	
    	// transpose data matrix
    	cmcMatrix dataMatrixTransposed = vrout.transposeMatrix( dataMatrix );
    	 if( dataMatrixTransposed == null ) { do_error("Cannot transpose data matrix"); return false; }
    	 if( m != dataMatrixTransposed.getNbrOfColumns() ) { do_error("Datamatrix transpose error 1"); return false; }
    	 if( (n+1) != dataMatrixTransposed.getNbrOfRows() ) { do_error("Datamatrix transpose error 2"); return false; }
    	
    	// transpose target matrix so that you have 1 row and M columns
    	cmcMatrix targetMatrixTransposed = vrout.transposeMatrix( tgtmtrx );
    	 if( targetMatrixTransposed == null ) { do_error("Null target matrix"); return false; }
    	 if( targetMatrixTransposed.getNbrOfRows() != 1 ) { do_error("Number of cols on transposed target must be 1"); return false; }
    	 if( targetMatrixTransposed.getNbrOfColumns() != m ) { do_error("Teranspoesed Target en Data matrix - row mismatch " + tgtmtrx.getNbrOfRows() + " " + m); return false; }
    	
  		//  FORMULE = W - alfa ( (y - sigmoid( w XT ))X )
    	double minLoss = -1;
    	double prevloss = 0;
    	double  maxAccuracy = Double.NaN;
    	int minLossCycle=-1;
        for(int cycle=0;cycle<NbrOfCycles;cycle++)
        {
        	cmcMatrix h1 = vrout.multiplyMatrix( weightMatrix , dataMatrixTransposed ); // (1,n) * (n,m) =>  (1,m)
        	 if ( h1 == null ) { do_error("System error A10 -Multiplcition"); return false; }
        	 if( h1.getNbrOfRows() != 1) { do_error("System error A10 - wrong number of rows"); return false; }
        	 if( h1.getNbrOfColumns() != m) { do_error("System error A11 - wrong number of cols"); return false; }
        	
        	cmcMatrix h2b = vrout.sigmoidMatrix( h1 ); // (1,m)
        	 if( h2b == null ) { do_error("System error A12 Sigmoid"); return false; }
        	 if( h2b.getNbrOfRows() != 1) { do_error("System error A12 - wrong number of rows"); return false; }
        	 if( h2b.getNbrOfColumns() != m) { do_error("System error A13 - wrong number of cols"); return false; }
        	
        	cmcMatrix errorMatrix = vrout.substractMatrix( targetMatrixTransposed , h2b );  // 1,m - 1,m = 1,m
        	 if( errorMatrix == null ) { do_error("System error A10 - Substract"); return false; }
        	 if( errorMatrix.getNbrOfRows() != 1) { do_error("System error A14 - wrong number of rows"); return false; }
        	 if( errorMatrix.getNbrOfColumns() != m) { do_error("System error A15 - wrong number of cols"); return false; }
                   
        	// (y - sigmoid( w XT )) X     (1,m) (m,n) => 1,n    => number of columns
        	cmcMatrix gradientMatrix = vrout.multiplyMatrix( errorMatrix , dataMatrix );
        	 if( gradientMatrix == null ) { do_error("System error A16 multiplication"); return false; }
        	 if( gradientMatrix.getNbrOfRows() != 1) { do_error("System error A16 - wrong number of rows"); return false; }
        	 if( gradientMatrix.getNbrOfColumns() != (n+1) ) { do_error("System error A17 - wrong number of cols"); return false; }
        
        	//  apply stepsize
        	cmcMatrix t2 = vrout.scalarMultiplyMatrix( StepSize , gradientMatrix);  // alfa * 1,n
        	 if( t2 == null ) { do_error("System error A20 scalar multiplication"); return false; }
         	//do_log( 1 , "alpha*data.T*error " +  vrout.printMatrix(t2) );
        	
        	// 
        	prevWeight = weightMatrix;  // 1,n
        	weightMatrix = vrout.addMatrix( weightMatrix , t2 );   // (n,1)
        	 if( weightMatrix == null )  return false;
        	 if( weightMatrix.getNbrOfColumns() != (n+1) ) { do_error( "System error ZZ [" + weightMatrix.getDimension()[0] + "]"); return false; }
            //do_log( 1 , "[" + cycle + "] w= " +  vrout.printMatrix( vrout.transposeMatrix(weightMatrix)) );
        	
        	 
        	 // Max Accurary is not necessarily the best trained model ..
        	 double dac = determineCurrentAccuracy( dataMatrixTransposed , weightMatrix , targetMatrixTransposed);
        	 if( Double.isNaN( dac ) ) return false;
        	 if( cycle == 0 ) { maxAccuracy = dac; OptimalNbrOfCycles = NbrOfCycles; }
        	 if( dac > maxAccuracy ) { maxAccuracy = dac; OptimalNbrOfCycles = cycle; }
        	
        	 
        	 // alternatief = zoek naar de cycle waar de weights niet meer wijzigen - maxaccu is overfitted
        	 double wdist=0;
        	 for(int k=0;k<weightMatrix.getNbrOfColumns();k++)
        	 {
        		wdist +=  Math.abs(weightMatrix.getValues()[0][k] - prevWeight.getValues()[0][k]);
        	 }
        	 if( cycle == 0 ) { minLoss = wdist; }
        	 if( minLoss > wdist ) { minLoss = wdist; minLossCycle = cycle; }
        	do_log( 1 , "=> Cycle=" +  cycle  + " Accu=" + dac + "  #Optimal=" + OptimalNbrOfCycles + " " + minLoss + " " + minLossCycle );
        }
        //
        finalweight = weightMatrix;
        do_log( 1 , "w= " +  vrout.printMatrix(finalweight) );
         if( finalweight.getNbrOfRows() != 1 ) { do_error( "Finalweight mustbe [N+1,1]"); return false; }
         if( finalweight.getNbrOfColumns() != (n+1) ) { do_error( "Finalweight mustbe [N+1,1]"); return false; }
        
        // evaluate
        double dac = determineCurrentAccuracy( dataMatrixTransposed , finalweight , targetMatrixTransposed);
        do_log( 1 , "Accuracy=" + dac + " #Cycles=" + NbrOfCycles);
     	return true;
    }
    
    
    private double determineCurrentAccuracy(cmcMatrix dataMatrix , cmcMatrix weightMatrix , cmcMatrix targetMatrix)
    {
    	   cmcMatrix selftest = vrout.multiplyMatrix( weightMatrix , dataMatrix );  //  1,n x n,m => 1,m
           if( selftest == null )  { do_error( "Error multiplying selftest"); return Double.NaN; }
           if( selftest.getNbrOfRows() != 1 ) {  do_error( "selftest must be 1 col"); return Double.NaN; }
           if( selftest.getNbrOfColumns() != dataMatrix.getNbrOfColumns() ) {  do_error( "selftest columns"); return Double.NaN; }
           if( selftest.getNbrOfColumns() != targetMatrix.getNbrOfColumns() ) {  do_error( "selftest and target columns differ"); return Double.NaN; }
           if( selftest.getNbrOfRows() != targetMatrix.getNbrOfRows() ) {  do_error( "selftest and target rows differ"); return Double.NaN; }
          //  selftest is 1xM         
          double[][] aval = selftest.getValues();
           if( aval == null ) { do_error("Aval null"); return Double.NaN; }
           if( aval.length != 1 ) { do_error("Aval cols must be 1"); return Double.NaN; }
           if( aval[0].length != dataMatrix.getNbrOfColumns() ) { do_error("Aval cols must be m"); return Double.NaN; }
          //
          int hit=0;
          for(int i=0;i<aval[0].length;i++)
          {
          	int result =  vrout.sigmoid(selftest.getValues()[0][i]) < 0.5 ? 0 : 1;
          	boolean correct = result == (int)targetMatrix.getValues()[0][i] ? true : false;
          	if( correct ) hit++;
      //do_log( 1 , "CALC [" + selftest.getValues()[0][i] + "] [" + targetMatrixTransposed.getValues()[0][i] + "] [" + vrout.sigmoid( selftest.getValues()[0][i]) + "] [" + correct + "]");
          }
          double pp = (double)hit / (double)dataMatrix.getNbrOfColumns();   // data matrix is tranposed
          
          return pp;
    }
    
    
    
    
    /*
    // mx (M,N)  tgt (M,1)
    //------------------------------------------------------------
    private boolean simpleLogisticRegressionMatrixOLDBUTOK(cmcMatrix mx, cmcMatrix tgtmtrx, int NbrOfCycles , double StepSize)
    //------------------------------------------------------------
    {   //  M= number of rows and  N=number of columns !!
    	if( mx == null ) return false;
    	int m = mx.getValues().length;
    	if( m <= 0 ) { do_error("no rows"); return false; }
    	int n = mx.getValues()[0].length;
    	if( n <= 0 ) { do_error("no columns"); return false; }
    	// add a new first column comprising all 1  - augment
    	cmcMatrix dataMatrix = vrout.makeAugmentedMatrix( mx );
    	
    	// initialize weight matrix n+1 , 1  and set all weights to 1
    	
    	double[][] tmp = new double[ n+1 ][1];
    	for(int i=0;i<tmp.length;i++) tmp[i][0]=1;
    	cmcMatrix weightMatrix = new cmcMatrix( tmp );
    	cmcMatrix prevWeight = null;
    	//
    	double minLoss = -1;
        for(int cycle=0;cycle<NbrOfCycles;cycle++)
        {
        	cmcMatrix h1 = vrout.multiplyMatrix( dataMatrix , weightMatrix ); // (m,n) * (n,1) =>  (m,1)
        	if ( h1 == null ) return false;
        	if( h1.getNbrOfColumns() != 1) { do_error("System error A1"); return false; }
        	
        	cmcMatrix h2b = vrout.sigmoidMatrix( h1 ); // m,1
        	if( h2b == null ) return false;
        	
        	cmcMatrix errorMatrix = vrout.substractMatrix( tgtmtrx , h2b );  // m,1
        	if( errorMatrix == null ) return false;
        	//do_log( 1 , "Error " +  vrout.printMatrix(errorMatrix) );
                    	
        	// bereken de squared loss op de error
        	double[] loss = new double[ errorMatrix.getNbrOfRows() ];
        	for(int lo=0;lo<loss.length;lo++) loss[ lo ] = errorMatrix.getValues()[lo][0];
        	cmcVector errorVector = new cmcVector( loss );
        	double SquaredLoss = errorVector.getMagnitude();
        	double dd = Math.round( SquaredLoss * 10000 );
        	if( cycle == 0 ) { minLoss = dd; optimal = NbrOfCycles; }
        	if( dd < minLoss ) { minLoss = dd; optimal = cycle; }
        	//do_log( 1 , "=> " +  cycle + " " + (int)dd + " " + (int)minLoss + " " + optimal );
        	
        	// weight per attribute N
        	cmcMatrix t1 = vrout.multiplyMatrix( vrout.transposeMatrix(dataMatrix) , errorMatrix ); // (n,m) * (m,1) => (n,1)
        	if( t1 == null ) return false;
        	//do_log( 1 , "data.T*Error " +  vrout.printMatrix(t1) );
        	
        	cmcMatrix t2 = vrout.scalarMultiplyMatrix( StepSize , t1);  // (n,1)
        	if( t2 == null ) return false;
         	//do_log( 1 , "alpha*data.T*error " +  vrout.printMatrix(t2) );
        	
        	weightMatrix = vrout.addMatrix( weightMatrix , t2 );   // (n,1)
        	if( weightMatrix == null )  return false;
        	if( weightMatrix.getNbrOfRows() != (n+1) ) { do_error( "System error ZZ [" + weightMatrix.getDimension()[0] + "]"); return false; }
            //do_log( 1 , "[" + cycle + "] w= " +  vrout.printMatrix( vrout.transposeMatrix(weightMatrix)) );
        	
        	
        	prevWeight = weightMatrix;
        }
        //
        finalweight = vrout.transposeMatrix(weightMatrix);
        do_log( 1 , "w= " +  vrout.printMatrix(finalweight) );
        if( finalweight.getNbrOfRows() != 1 ) { do_error( "Finalweight mustbe [N+1,1]"); return false; }
        
        // evaluate
        cmcMatrix selftest = vrout.multiplyMatrix( dataMatrix , weightMatrix );  //  (m,n) * (n,1)
        //do_log( 1 , "selftest= " +  vrout.printMatrix( selftest ) );
        double[][] aval = selftest.getValues();
        int hit=0;
        for(int i=0;i<aval.length;i++)
        {
        	//boolean correct = selftest.getValues()[i][0] * tgtmtrx.getValues()[i][0] >= 0;
        	int result =  selftest.getValues()[i][0] < 0.5 ? 0 : 1;
        	boolean correct = result == (int)tgtmtrx.getValues()[i][0] ? true : false;
        	if( correct ) hit++;
    //do_log( 1 , "CALC [" + selftest.getValues()[i][0] + "] [" + tgtmtrx.getValues()[i][0] +  "] [" + correct + "]");
        }
        double pp = (double)hit / (double)aval.length;
        do_log( 1 , "Hits [" + hit + "] [" +aval.length + "] P=" + pp + "  #Cycles=" + NbrOfCycles);
          
   
    	return true;
    }
    */
    //------------------------------------------------------------
    private boolean dumpModel( String LongModelName )
    //------------------------------------------------------------
    {
    	cmcMachineLearningModelDAO dao = new cmcMachineLearningModelDAO( xMSet , logger );
    	boolean ib = dao.writeLogisticRegressionModel( lgdto , LongModelName );
    	LastErrorMsg = dao.getLasterrorMsg();
    	dao=null;
    	return ib;
    }
    
    //------------------------------------------------------------
    private cmcLogisticRegressionDTO readModel( String LongModelName )
    //------------------------------------------------------------
    {
    	cmcLogisticRegressionDTO mdl = null;
    	cmcMachineLearningModelDAO dao = new cmcMachineLearningModelDAO( xMSet , logger );
    	mdl = dao.readLogisticRegressionModel( LongModelName );
    	LastErrorMsg = dao.getLasterrorMsg();
    	dao=null;
    	return mdl;
    }
    //------------------------------------------------------------
    private boolean updateModel( cmcModelEvaluation bres , cmcModelEvaluation tres , String LongModelName )
    //------------------------------------------------------------
    {
    	cmcMachineLearningModelDAO dao = new cmcMachineLearningModelDAO(xMSet,logger);
    	boolean ib = dao.insertEvaluation( bres , tres , null , LongModelName );
    	dao=null;
    	return ib;
    }
    
    //------------------------------------------------------------
    public boolean testModel( String ModelFileName , String FileNameTestSet , cmcARFF xa)  // xa always null
    //------------------------------------------------------------
    {
    	return executeModel( ModelFileName , FileNameTestSet );
    }
    
    //------------------------------------------------------------
    public boolean executeModel( String ModelFileName , String ARFFFileName )
    //------------------------------------------------------------
    {
      try {	
    	cmcLogisticRegressionDTO model = readModel(ModelFileName);
        if( model == null ) return false;
        //
    	cmcTextToARFF txt = new cmcTextToARFF( xMSet , logger );
    	ARFFCategory[] categories = txt.getCategories( ARFFFileName , null , cmcMachineLearningConstants.NBR_BINS , false);
        if( checkLRCategories(categories) == false ) return false;
        String[] ClassNameList = txt.getClassNames(categories);
    	if( ClassNameList == null ) { do_error("Null class name list"); return false; }
    	if( ClassNameList.length != 2 ) { do_error("There mus tbe exactly 2 classes"); return false; }
        //
        cmcMatrix[] DataTargetTuple = txt.transformIntoMatrix( categories ); // 0 = data  1 = Target
        if( DataTargetTuple == null ) return false;
        //
        cmcMatrix rawMatrix = DataTargetTuple[0];
        if( rawMatrix == null ) { do_error( "NULL rawmatrix"); return false; }
        if( rawMatrix.getNbrOfColumns() <= 0 ) { do_error( "No columns on data"); return false; }
        if( rawMatrix.getNbrOfRows() <= 0 ) { do_error( "No rows on data"); return false; }
        
        // transform target in 0 and 1  (?? nececessary??)
        cmcMatrix resultMatrix = vrout.transform2ZeroAndOne( DataTargetTuple[1] );
        if( resultMatrix == null ) { do_error( "NULL resultmatrix"); return false; }
        if( resultMatrix.getNbrOfRows() <= 0 ) { do_error( "No rows on results"); return false; }
        if( resultMatrix.getNbrOfColumns() != 1) { do_error( "Result must be M,1 " + resultMatrix.getNbrOfRows() + " " + resultMatrix.getNbrOfColumns() ); return false; }
        if( rawMatrix.getNbrOfRows() != resultMatrix.getNbrOfRows() ) { do_error( "Number of rows differs on data and results  D=" + rawMatrix.getNbrOfRows() + " R=" + resultMatrix.getNbrOfRows()); return false; }
        
        // make the weight matrix and transpose it immediately
        double[] weights = model.getWeights();
        if( weights == null ) { do_error( "Null weights"); return false ; }
        if( weights.length <= 0 ) { do_error( "Empty weights"); return false ; }
        double[][] wvals = new double[ weights.length ][ 1 ];
        for(int i=0;i<weights.length;i++) wvals[ i ][ 0 ] = weights[i];
        cmcMatrix weightMatrix = new cmcMatrix( wvals );
        if( weightMatrix == null ) { do_error( "NULL weight matrix"); return false; }
        if( weightMatrix.getNbrOfColumns() <= 0 ) { do_error( "No columns on w"); return false; }
        if( weightMatrix.getNbrOfRows() <= 0 ) { do_error( "No rows on w"); return false; }
        if( weightMatrix.getNbrOfColumns() != 1) { do_error( "Weight matrix must be [N,1]" ); return false; }
        //
        if( (rawMatrix.getNbrOfColumns() + 1) != weightMatrix.getNbrOfRows() ) {
        	do_error( "The number of columns on the data matrix must be number of weights - 1 [R=" + rawMatrix.getNbrOfColumns() + "] [W=" + weightMatrix.getNbrOfRows() + "]"); // will be augmented
        	return false;
        }
        
        // Normalize the data values
        double[] means = model.getNormalizerMean();
        if( means == null ) { do_error( "NULL means"); return false; }
        double[] stddevs = model.getNormalizerStdDev();
        if( stddevs == null ) { do_error( "NULL stddevs"); return false; }
        if( means.length != stddevs.length ) { do_error( "Means and Stddev differ in length"); return false; }
        if( rawMatrix.getNbrOfColumns() != means.length ) { do_error( "data matrix columns differ from normalizer mean"); return false; }
        double[][] normvals = new double[ rawMatrix.getNbrOfRows() ][ rawMatrix.getNbrOfColumns() ] ;
        for(int row=0;row<rawMatrix.getNbrOfRows();row++)
        {
        	for(int col=0;col<rawMatrix.getNbrOfColumns();col++)
        	{
                normvals[ row ][ col ] = ( rawMatrix.getValues()[ row ] [ col ] - means[ col ] ) / stddevs[ col ]; 		
        	}
        }
        cmcMatrix normalizedMatrix = new cmcMatrix( normvals );
        
        // Augment the normalized data matrix
    	cmcMatrix dataMatrix = vrout.makeAugmentedMatrix( normalizedMatrix );
    	
    	// multiply the data and weights (transposed)     M,N x  N,1
    	if( dataMatrix.getNbrOfColumns() != weightMatrix.getNbrOfRows() ) {
    		do_error( "Number of data columns must match number of weight rows [D=" + dataMatrix.getNbrOfColumns() + "] [W=" + weightMatrix.getNbrOfRows() + "]");
    		return false;
    	}
    	cmcMatrix calculatedMatrix = vrout.multiplyMatrix( dataMatrix , weightMatrix );  //  (m,n) * (n,1)
        if ( calculatedMatrix == null ) return false;
       
        //
        int hit=0;
        eval = new cmcModelEvaluation( ClassNameList );
        // only if a genuine CBRTEKSTRAKTOR ARFF file is used this list will be set
        String[] CBRFileNameList = txt.getValidatedCBRFileNameList( calculatedMatrix.getNbrOfRows() );
        for(int i=0;i<calculatedMatrix.getNbrOfRows();i++)
        {
        	int result =  vrout.sigmoid(calculatedMatrix.getValues()[i][0]) < 0.5 ? 0 : 1;
        	boolean correct = (result == (int)resultMatrix.getValues()[i][0]) ? true : false;
        	if( correct ) hit++;
        	
        	String calculated = ClassNameList[result];
        	String anticipated = ClassNameList[(int)resultMatrix.getValues()[i][0]];
        	boolean ib = eval.addToConfusion( calculated , anticipated);
        	if( ib == false ) return false;
        	
            //do_log( 1 , "CALC [" + calculatedMatrix.getValues()[i][0] + "] [" + resultMatrix.getValues()[i][0] +  "] [" + correct + "]");
        }
        double pp = (double)hit / (double)calculatedMatrix.getNbrOfRows();
        do_log( 1 , "Hits [" + hit + "] [" + calculatedMatrix.getNbrOfRows() + "] P=" + pp);
    	
    	//
    	rawMatrix = null;
    	resultMatrix = null;
    	dataMatrix = null;
    	weightMatrix = null;
    	normalizedMatrix = null;
    	txt = null;
      }
      catch(Exception e ) {
    	 do_error( "(executeModel) " + xMSet.xU.LogStackTrace(e) );
    	 return false;
      }
      return true;
    }
}
