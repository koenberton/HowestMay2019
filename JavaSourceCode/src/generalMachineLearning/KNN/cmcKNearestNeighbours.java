package generalMachineLearning.KNN;

// https://archive.ics.uci.edu/ml/index.php

import cbrTekStraktorModel.cmcProcSettings;
import dao.cmcMachineLearningModelDAO;
import featureExtraction.PredictDetail;
import generalMachineLearning.cmcMachineLearningConstants;
import generalMachineLearning.cmcModelMaker;
import generalMachineLearning.ARFF.ARFFCategory;
import generalMachineLearning.ARFF.ARFFCategoryCoreDTO;
import generalMachineLearning.ARFF.cmcARFF;
import generalMachineLearning.ARFF.cmcARFFRoutines;
import generalMachineLearning.ARFF.cmcTextToARFF;
import generalMachineLearning.Evaluation.cmcModelEvaluation;
import generalMachineLearning.LinearAlgebra.cmcMath;
import generalMachineLearning.LinearAlgebra.cmcVector;
import generalMachineLearning.LinearAlgebra.cmcVectorRoutines;
import logger.logLiason;

public class cmcKNearestNeighbours implements cmcModelMaker {

	cmcProcSettings xMSet=null;
	logLiason logger=null;
	cmcVectorRoutines vrout = null;
	cmcARFFRoutines arffrout = null;
	cmcMath cma=null;
	
	private int OptimalK = -1; 
	private double LastPredictedValue = -1;
	cmcModelEvaluation[] evalList = null;
	private long startTime=0L;
	
	private int NBR_OF_REPARTITIONS = cmcMachineLearningConstants.NBR_OF_REPARTITIONS;
	private int NBR_OF_PARTITIONS   = cmcMachineLearningConstants.NBR_OF_PARTITIONS;
	private int MAX_K               = cmcMachineLearningConstants.MAX_K;

	private PredictDetail[] predictlist=null;
	private  cmcModelEvaluation trainResult = null;
	
	class confusion
	{
		int K=-1;
		int[][] mtrx=null;
		confusion( int k , int nclasses )
		{
			K=k;
			mtrx = new int[ nclasses ][ nclasses ];
			for(int i=0;i<nclasses;i++)
			{
				for(int j=0;j<nclasses;j++) mtrx[i][j]=0;
			}
		}
	}
	confusion[] arConfusion = null;
	
	
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
    public cmcKNearestNeighbours(cmcProcSettings is,logLiason ilog)
    //------------------------------------------------------------
    {
    	xMSet = is;
		logger=ilog;
		vrout = new cmcVectorRoutines();
		cma = new cmcMath();
    }
    
    //------------------------------------------------------------
    public boolean trainModel(	ARFFCategory[] categories , int nbins , String[] ClassNameList , String ModelFileName )
    //------------------------------------------------------------
    {
    	return false;
    }
    
    //------------------------------------------------------------
    public boolean trainModel(String OriginalFileName , String LongModelName )
    //------------------------------------------------------------
    {
    	trainResult=null;
    	startTime = System.currentTimeMillis();
    	String DiagramFileName = xMSet.xU.getFileNameWithoutSuffix(OriginalFileName);
    	if( DiagramFileName == null ) { do_error("Cannot strip suffix"); return false; }
        DiagramFileName = DiagramFileName.trim() + ".png";
        //
    	cmcTextToARFF txt = new cmcTextToARFF( xMSet , logger );
    	ARFFCategory[] categories = txt.getCategories( OriginalFileName , DiagramFileName , cmcMachineLearningConstants.NBR_BINS , true); // make train and test set
    	if( categories == null ) return false;
        if( categories.length <= 0 ) return false;
        String[] ClassNames = txt.getClassNames(categories);
        if( ClassNames == null ) return false;
        cmcVector[] dataVectorList = txt.transformIntoVectors(categories);
        if( dataVectorList == null ) return false;
        cmcVector resultVector = txt.getResultVector();
        if( resultVector == null ) return false;
        //
      	boolean ib = doKNN( dataVectorList , resultVector , ClassNames );
    	if( ib == false ) return false;
    	//
    	ib = dumpModel( categories , dataVectorList , resultVector , OriginalFileName , LongModelName );
    	if( ib == false ) return false;
    	
        // run the model on the test set immediately
    	cmcModelEvaluation bres = this.trainResult;
    	ib = executeModel( LongModelName , txt.getTestSetName() , null );
    	if( ib == false ) return false;
    	cmcModelEvaluation tres = this.trainResult;
    	cmcMachineLearningModelDAO dao = new cmcMachineLearningModelDAO(xMSet,logger);
    	ib = dao.insertEvaluation( bres , tres , "Number of trained elements is for all iterations. Test elements for 1 iteration. So TestSetPercentge is off" , LongModelName );
    	dao=null;
        if( ib == false ) return false;
        
    	return true;
    }
    
    //------------------------------------------------------------
    private boolean doKNN( cmcVector[] absoluteVList, cmcVector resultVector , String[] ClassNames )
    //------------------------------------------------------------
    {
    	// Check
    	int nrows = absoluteVList.length;
    	int ncols = -1;
    	for(int i=0;i<nrows;i++)
    	{
    		ncols = absoluteVList[i].getDimension();
    		for(int j=0;j<ncols;j++) {
    			if( Double.isNaN(absoluteVList[i].getVectorValues()[j]) )  { do_error("Vector - Found a NaN " + i + " " + j); return false; }
    		}
    	}
    	if( (ncols<=0) || (nrows<=0) ) { do_error("Nothing to process [Rows=" + nrows + "] [Cols=" + ncols + "]"); return false; }
    	if( nrows != resultVector.getDimension() ) { do_error("ResultVector - dimension differs from " + nrows ); return false; }
    	for(int j=0;j<nrows;j++) 
    	{
			if( Double.isNaN(resultVector.getVectorValues()[j]) )  { do_error("ResultVector - Found a NaN " + j ); return false; }
		}
    	
    	// Normalize
    	cmcVector[] dataVectorList = vrout.normalizeVectorList( absoluteVList );
    	if( dataVectorList == null ) { do_error("Could not normalize - null"); return false; }
    	if( dataVectorList.length != nrows ) { do_error("Could not normalize - rows"); return false; }
    	if( dataVectorList[0].getDimension() != ncols ) { do_error("Could not normalize - features"); return false; }
    	
    	// Accelerator : make a matrix with the euclidian distance between each vector
    	double[][] distanceArray = new double[ nrows ][ nrows ];
    	for(int i=0;i<nrows;i++)
    	{
    		for(int j=0;j<nrows;j++) distanceArray[i][j]= Double.NaN;
    	}
    	double MAX_DISTANCE = 0;
    	for(int i=0;i<nrows;i++)
    	{
    		for(int j=0;j<nrows;j++)
    		{
    			if( Double.isNaN( distanceArray[i][j] ) == false ) continue;
    			// euclidische afstand i en j
    			double dist = vrout.euclidianDistance( dataVectorList[i] , dataVectorList[j] );
    			if( Double.isNaN( dist  ) ) { do_error( "System error euclidian"); return false; }
    			distanceArray[i][j] = dist;
    			distanceArray[j][i] = dist;
    			if( dist > MAX_DISTANCE ) MAX_DISTANCE = dist;
    		}
    	}
    	// check
    	for(int i=0;i<nrows;i++)
    	{
    		for(int j=0;j<nrows;j++)  
    		{
    			if( Double.isNaN(distanceArray[i][j]) ) { do_error("Distance - Found a NaN " + i + " " + j); return false; }
    			if( (i==j) && (distanceArray[i][j] !=0) ) { do_error("Distance should be 0 for [" + i + "]"); return false; }
    		}
    	}
    	//do_log( 1 , "MAX DIST=" + MAX_DISTANCE );
    	
    	// Number of classes
    	// determine max class
    	int minclass=0;
    	int maxclass=0;
    	double[] target = resultVector.getVectorValues();
    	for(int i=0;i<target.length; i++ )
    	{
    		if( i==0 ) { maxclass = (int)target[i]; minclass = (int)target[i]; }
    		if( target[i] > maxclass ) maxclass = (int)target[i];
    		if( target[i] < minclass ) minclass = (int)target[i];
    	}
    	if( minclass<0 ) { do_error( "MIN CLASS less than zero MIN=" + minclass + " MAX=" + maxclass); return false; }
        if( minclass>=maxclass ) { do_error( "Min class larger than max class"); return false; }
        if( maxclass > 5 ) { do_error( "Probably an error - there are too many classe - increase?? " + maxclass); return false; }
        int nclasses = maxclass + 1;
        // check read class
        if( ClassNames.length != nclasses ) { do_error( "NClasses differs from number of elements on ClassNames"); return false; }
     	
        // initialize confusion
        arConfusion = new confusion[ MAX_K ];
        for(int i=0;i<MAX_K;i++) arConfusion[i] = new confusion( i , nclasses );
        
        
    	// Number of repartitions
    	for(int repar=0;repar<NBR_OF_REPARTITIONS;repar++)
    	{
    		// shuffle is list of unique integers ranging from 0 .. nrows in random order - use this to split into partitions
        	int[] shuffle = cma.shuffleList( nrows );
        	if( shuffle == null ) { do_error("Could not shuffle"); return false; }
        	//
        	int[] partitieSize = new int[ NBR_OF_PARTITIONS ];
        	int sz =  nrows / NBR_OF_PARTITIONS;
        	for(int i=0;i<NBR_OF_PARTITIONS;i++) partitieSize[i] = sz;
        	int rema = nrows % NBR_OF_PARTITIONS;
        	for(int i=0;i<rema;i++) partitieSize[i]++;  // distribute remainder over the partitions
        	
        	// loop through shuffle list and assign block comprising partitionsize elements
        	// every block is used as a test set once
        	for(int z=0;z<NBR_OF_PARTITIONS;z++)
        	{
        		int startIndex = 0;
        		for(int k=0;k<z;k++) startIndex += partitieSize[k];
        		int endIndex = startIndex + partitieSize[z] - 1;
        		//
        		//do_log( 1 , "REPAR=" + repar + " PART=" + z + " "+ startIndex + " " + endIndex );
        		int[] exclude = new int[ endIndex - startIndex + 1 ];
        		for(int k=0 ; k<exclude.length ; k++) exclude[ k ] = shuffle[ startIndex + k ];
                //        		
        		boolean ib = performKNN( distanceArray , exclude , resultVector.getVectorValues() , nclasses );
        		if( ib == false ) return false;
               	do_log( 1 , "R=" + repar + " P=" + z );
        		
        	}
    	}
    	OptimalK = evaluateRun(ClassNames,true);
        if( OptimalK < 0 ) return false;
    	// 
    	return true;
    }
    
    //------------------------------------------------------------
    private boolean dumpModel( ARFFCategory[] categories , cmcVector[] absoluteVList, cmcVector resultVector , String OriginalFileName , String LongModelName )
    //------------------------------------------------------------
    {
    	if( trainResult == null ) return false;
    	//do_log( 1 , trainResult.showConfusionMatrix() );
    	boolean OK=true;
    	int nrows = absoluteVList.length;
    	int nfeatures = categories.length;
    	if( resultVector.getDimension() != nrows ) { do_error("System error AA"); return false;}
    	cmcKNearestNeighboursDTO dto = new cmcKNearestNeighboursDTO( OptimalK , nrows , nfeatures );
    	ARFFCategoryCoreDTO[] features = new ARFFCategoryCoreDTO[ categories.length ];
    	for(int i=0 ; i<categories.length ; i++ )
    	{
    		features[i] = new ARFFCategoryCoreDTO( categories[i].getCategoryName() );
    		features[i].setTipe( categories[i].getTipe() );
    		features[i].setNominalValueList( categories[i].getNominalValueList() );
    	}
    	dto.setFeatures(features);	
    	//
    	for(int row=0;row<nrows;row++)
    	{
    	   int ncols = absoluteVList[row].getDimension();
    	   if( ncols != (nfeatures-1) ) {
    		   do_error("Number of cols does not match nbr of features");
    		   return false;
    	   }
    	   for(int j=0;j<ncols;j++) {
    		   OK = dto.setSamplesValue( row , j , absoluteVList[row].getVectorValues()[j] );
    		   if( OK == false ) { do_error("Cannot set SAMPLE value [" + row + "][" + j + "]"); return false; }
    	   }
    	}
    	// check  aantal kolommen op de data moet (features - 1) zijn; else fout
	    if( dto.getNbrOfColumnsOnSamples() != (nfeatures-1) ) { do_error("System error AB"); return false;}
   	    for(int row=0;row<nrows;row++)
   	    {
   	    	OK = dto.setResultsValue( row , resultVector.getVectorValues()[row] );
   	        if( OK == false ) { do_error("Cannot set RESULT value [" + row + "]"); return false; }
   	    }
   	    // MISC
   	    dto.setLongARFFFileName(OriginalFileName);
   	    dto.setMaximumK( MAX_K );
   	    dto.setNbrOfPartitions(NBR_OF_PARTITIONS);
   	    dto.setNbrOfRePartitions( NBR_OF_REPARTITIONS);
   	    dto.setElapsedTime(System.currentTimeMillis() - startTime);
   	    
   	    
   	    //  DTO initialized
   	    cmcMachineLearningModelDAO dao = new cmcMachineLearningModelDAO( xMSet , logger );
   	    OK = dao.writeKNN( dto , LongModelName , evalList );
   	    //
   	    dto=null;
   	    dao=null;
    	return OK;
    }
   
    //------------------------------------------------------------
    private boolean performKNN(  double[][] distanceArray , int[] trainingSet , double[] target  , int nclasses)
    //------------------------------------------------------------
    {
    	if( distanceArray.length != target.length ) { do_error( "Number of rows in distance array doe not match nbr of rwos in target"); return false; }
    	int nrows = target.length;
        if( trainingSet.length <= 0) { do_error("There is nothing to process"); return false; }
       
    	// accelerator : put a flag (isTrainingVector) against each row
    	boolean[] isTrainingVector = new boolean[ nrows ];
    	for(int row=0 ; row<nrows ; row++) isTrainingVector[ row ] = false;
    	for(int i=0;i<trainingSet.length;i++)
    	{
    		int idx = trainingSet[i];
    		if( (idx<0) || (idx>=nrows) ) { do_error( "training index out of bound [" + idx + "] [NROWS=" + nrows +"]" ); return false; }
    		isTrainingVector[ idx ] = true;
    	}
    	
    	// get maximum distance
    	// will probably be remove again to higher level
    	double MAX_DISTANCE = -1;
    	for(int i=0;i<distanceArray.length;i++)
    	{
    		for(int j=0;j<distanceArray.length;j++) { if ( distanceArray[i][j] > MAX_DISTANCE ) MAX_DISTANCE =  distanceArray[i][j]; }
    	}
    	if( MAX_DISTANCE < 0 ) { do_error("Could not determine MAX_DISTANCE"); return false; }
    	//do_log( 1 , "MAX DIST=" + MAX_DISTANCE );
     	//
    	double[] lineDistance = new double[ nrows ];
		for(int row=0 ; row<nrows ; row++ )
    	{
    		if( isTrainingVector[ row ] == false ) continue;
    		// determine K minimale afstanden voor die ene vector
    		for( int i=0;i<nrows;i++)
    		{
    			//referentie[i]=i;
    			if( isTrainingVector[i] ) lineDistance[ i ] = MAX_DISTANCE + 10;  // put an unreachable value
    			                     else lineDistance[ i ] = distanceArray[row][i];
    		}
    	    //
    		boolean ib = performScoring( -1 , lineDistance , nclasses , target , target[row]);
    		if( ib == false ) return false;
    		
    	}  // FOREACH row in trainingset
    	
    	return true;
    }

    //------------------------------------------------------------
    private boolean performScoring(int SingleK , double[] lineDistance , int nclasses , double[] target , double expectedValue)
    //------------------------------------------------------------
    {
    	if( lineDistance == null ) { do_error("Linedistance is null"); return false; }
        if( target == null ) { do_error("target is null"); return false; }
        if( target.length != lineDistance.length ) { do_error("lengths differ"); return false; }
        //
        int nrows = lineDistance.length;	
    	int[] referentie = new int[ nrows ];
		int[] score = new int[ nclasses ];
		for(int i=0;i<nrows;i++) referentie[i] = i;

    	// make a list of distances sorted from low to high
		for(int i=0;i<nrows;i++)
		{
			boolean swap=false;
			for(int j=0;j<(nrows-1);j++)
			{
				if( lineDistance[j] <= lineDistance[j+1]) continue;
				swap=true;
				double dd = lineDistance[j];
				lineDistance[j] = lineDistance[j+1];
				lineDistance[j+1] = dd;
				int kk = referentie[j];
				referentie[j] = referentie[j+1];
				referentie[j+1] = kk;
			}
			if( swap == false ) break;
		}
		try {
		  // predict target for a range of K values
	      for(int K=1;K<MAX_K;K++)
	      {
	    	// if SingleK != -1 then only for that K value
	    	if( (SingleK != -1) && ( SingleK != K) ) continue;
	    	// 10APR really small sets, ie. nhumber of records is less than MAX_K
	    	if( K >= target.length ) break;
	    	
			// get the class of the K number of smallest distances
			int[] smallestDistanceTarget = new int[ K ];
			for(int i=0;i<K;i++) smallestDistanceTarget[i] = (int)target[ referentie[i] ];   
			// determine highest score or vote
			for(int i=0;i<score.length ; i++) score[i]=0;
			for(int i=0;i<K;i++) score[ smallestDistanceTarget[i] ]++;
			int maxScoreIdx=-1;
			int maxScore=-1;
			for(int i=0;i<score.length;i++) {
				if( score[i] > maxScore ) {
					maxScore=score[i];
					maxScoreIdx=i;
				}
			}
			if( maxScoreIdx < 0 ) { do_error("Cannot determine max score index"); return false; }
			// predicted value = maxScoreIdx; 
			boolean ib = addToConfusion( K , maxScoreIdx , (int)expectedValue );
			LastPredictedValue = maxScoreIdx;
			if( ib == false ) return false;
	      }
		  return true;
		}
		catch(Exception e ) {
			do_error( "Oops - scoring error " + e.getMessage() + "K=" + MAX_K + " Target.length=" + target.length );
			do_error( xMSet.xU.LogStackTrace(e) );
			return false;
		}
    }
    
    //------------------------------------------------------------
    private boolean addToConfusion( int K , int predicted , int expected )
    //------------------------------------------------------------
    {
    	if( arConfusion == null ) return false;
    	if( (K<0) || (K>=arConfusion.length) ) { do_error("addToConfusion out of bound"); return false; }
    	confusion co = arConfusion[ K ];
    	if( co == null ) { do_error("addToConfusion system error"); return false; }
    	int ncls = co.mtrx.length;
    	if( (predicted<0) || (predicted>=ncls) ) { do_error("addToConfusion PREDICTED out of bound"); return false; }
    	if( (expected<0) || (expected>=ncls) ) { do_error("addToConfusion EXPECTED out of bound"); return false; }
    	co.mtrx[ predicted ][ expected ]++;
    	return true;
    }
    
    //------------------------------------------------------------
    private int evaluateRun(String[] ClassNames , boolean verbose)
    //------------------------------------------------------------
    {
    	if( arConfusion == null ) return -1;
    	evalList = new cmcModelEvaluation[ arConfusion.length ];
    	double[] fmeasureList = new double[ arConfusion.length ];
    	double[] accuracyList = new double[ arConfusion.length ];
    	for(int k=1;k<arConfusion.length;k++)
    	{
    		evalList[k] = new cmcModelEvaluation( ClassNames );
    		evalList[k].injectMatrix(arConfusion[ k ].mtrx);
    		evalList[k].calculate();
    		accuracyList[k] = evalList[k].getAccuracy();
    		fmeasureList[k] = evalList[k].getMeanFValue();
    	    if( verbose ) do_log( 1 , "[K=" + k +"] [Accu=" + accuracyList[k] +"] [MeanFMeasure=" + fmeasureList[k] +"]");
    	}
    	double maxAccu = -1;
    	for(int k=1;k<arConfusion.length;k++)
    	{
    		if( Double.isNaN(accuracyList[k]) ) continue; 
    		if( accuracyList[k] > maxAccu ) maxAccu = accuracyList[k];
    	}
    	if( maxAccu < 0 ) { do_error("Cannot determine max accuracy"); return -1; }
    	int idx = -1;
    	for(int k=1;k<arConfusion.length;k++)
    	{
    	   if( accuracyList[k] == maxAccu ) idx = k;  // pick LAST one => more votes
    	}		
    	if( idx < 0 ) { do_error("Cannot determine max accuracy index"); return -1; }
        do_log( 1 , "[Optimal-K= " + idx + "] [Accuracy=" + maxAccu +"]");
        trainResult = evalList[ idx ];
    	return idx;
    }
    
    //------------------------------------------------------------
    public boolean testModel( String ModelFileName , String FileNameTestSet  , cmcARFF xa)
    //------------------------------------------------------------
    {
    	return executeModel ( ModelFileName , FileNameTestSet , xa);
    }
    
    //------------------------------------------------------------
    public boolean executeModel( String ModelFileName , String ARFFFileName , cmcARFF xa)  // xa is not used 
    //------------------------------------------------------------
    {
    	cmcMachineLearningModelDAO dao = new cmcMachineLearningModelDAO( xMSet , logger );
    	cmcKNearestNeighboursDTO model = dao.readKNNModel(ModelFileName);
    	if( model == null ) return false;
        
        //
        if( model.getNbrOfRows() < 1 ) { do_error("there is nothing to test/exec"); return false; }
        cmcVector[] modelVectorList = new cmcVector[ model.getNbrOfRows() ];
        for(int i=0;i< model.getNbrOfRows() ; i++)
        {
        	double[] val = new double[ model.getNbrOfColumnsOnSamples() ];
        	for(int j=0;j<model.getNbrOfColumnsOnSamples();j++) val[j] = model.getSamples()[i][j];
        	modelVectorList[i] = new cmcVector( val );
        }
        cmcVector modelResultVector = new cmcVector( model.getResults() );
        
        // read the testfile
        cmcTextToARFF txt = new cmcTextToARFF( xMSet , logger );
    	ARFFCategory[] categories = txt.getCategories( ARFFFileName , null , cmcMachineLearningConstants.NBR_BINS , false); // no diagram and do no create test set
    	if( categories == null ) return false;
        if( categories.length <= 0 ) return false;
        String[] ClassNames = txt.getClassNames(categories);
        if( ClassNames == null ) return false;
        int nclasses = ClassNames.length;
        cmcVector[] testVectorList = txt.transformIntoVectors(categories);
        if( testVectorList == null ) return false;
        if( testVectorList.length < 1 ) { do_error("there is nothing to test"); return false; }
        cmcVector testResultVector = txt.getResultVector();
        if( testResultVector == null ) return false;
        
        // only if a genuine CBRTEKSTRAKTOR ARFF file is used this list will be set
        String[] CBRFileNameList = txt.getValidatedCBRFileNameList(testVectorList.length);
        
        // tests
        if( modelVectorList[0].getDimension() != testVectorList[0].getDimension() ) { do_error("model and test vector list have different dimensions " + modelVectorList[0].getDimension() + " " +  testVectorList[0].getDimension()); return false; }
        if( categories.length != model.getNbrOfFeatures() ) { do_error( "Number of categories does not match number of model features"); return false;       }
        for(int i=0 ; i<categories.length ; i++)
        {
        	ARFFCategory cat = categories[i];
        	ARFFCategoryCoreDTO feat = model.getFeatures()[i];
        	if( cat.getCategoryName().compareToIgnoreCase(feat.getCategoryName()) != 0) { do_error("Categoryname mismatch"); return false; }
        	if( cat.getTipe() != feat.getTipe() ) { do_error("Type mismatch"); return false; }
        	if( (cat.getNominalValueList() == null) && (feat.getNominalValueList() == null) ) continue;
        	if( (cat.getNominalValueList() != null) && (feat.getNominalValueList() == null) ) { do_error("Nominal value length mismatch"); return false; }
        	if( (cat.getNominalValueList() == null) && (feat.getNominalValueList() != null) ) { do_error("Nominal value length mismatch"); return false; }
        	for(int j=0;j<cat.getNominalValueList().length;j++)
        	{
        		if( cat.getNominalValueList()[j].compareToIgnoreCase(feat.getNominalValueList()[j]) != 0) { 
        		 do_error("Nominal value mismatch [" +  cat.getNominalValueList()[j] + "] [" + feat.getNominalValueList()[j] + "]"); return false; 
        		}
        	}
        }
        
        // Normalize everything (determine mean and stdev on the entire set of data : model and test data
        int total = model.getNbrOfRows() + testVectorList.length;
        cmcVector[] combinedVectorList = new  cmcVector[ total ];
        for(int i=0; i<model.getNbrOfRows() ; i++) combinedVectorList[i] = modelVectorList[i];
        for(int i=0; i<testVectorList.length;i++) combinedVectorList[ i + model.getNbrOfRows() ] = testVectorList[i];
        cmcVector[] combinedNormalizedVectorList = vrout.normalizeVectorList( combinedVectorList );
    	if( combinedNormalizedVectorList == null ) { do_error("exec Could not normalize - null"); return false; }
    	if( combinedNormalizedVectorList.length != total ) { do_error("exec Could not normalize - rows"); return false; }
    	if( combinedNormalizedVectorList[0].getDimension() !=  modelVectorList[0].getDimension() ) { do_error("exec Could not normalize - features"); return false; }
        // overwrite vector with normalized values
    	for(int i=0; i<model.getNbrOfRows() ; i++) modelVectorList[i] = combinedNormalizedVectorList[ i ];
    	for(int i=0; i<testVectorList.length;i++)  testVectorList[i]  = combinedNormalizedVectorList[ i + model.getNbrOfRows() ];
    	//
    	 // initialize confusion
        arConfusion = new confusion[ MAX_K ];
        for(int i=0;i<MAX_K;i++) arConfusion[i] = new confusion( i , nclasses );
        
    	// euclidian distance per testrow to all other
    	int hits=0;
    	int ndatarows = testVectorList.length;
    	predictlist=new PredictDetail[ndatarows];
        for(int i=0;i<ndatarows;i++) predictlist[i]=null;
    	for(int testrow=0;testrow<testVectorList.length;testrow++)
    	{
    	  double[] distanceLine	= new double[ model.getNbrOfRows() ];
    	  for(int i=0;i<distanceLine.length;i++) distanceLine[i] = Double.NaN;
    	  for(int row=0;row<model.getNbrOfRows();row++)
    	  {
    		  double dist = vrout.euclidianDistance( modelVectorList[row] , testVectorList[ testrow ]);
    		  if( Double.isNaN( dist ) ) return false;
    		  distanceLine[ row ] = dist;
    	  }
    	  double actual = testResultVector.getVectorValues()[testrow];
    	  boolean ib = performScoring( model.getOptimalK() , distanceLine , nclasses , model.getResults() , actual );
    	  if( ib == false ) return false;
    	  if( LastPredictedValue == actual ) hits++;
    	  //
    	  if( CBRFileNameList != null ) {
    		  try {
    		   String sActual = ClassNames[(int)actual];
    		   String sPredicted = ClassNames[(int)LastPredictedValue];
    		   //do_log( 1 ,  CBRFileNameList[testrow] + " [" + actual + "," + sActual + "] [" + LastPredictedValue + "," + sPredicted + "]");
    		   predictlist[testrow] = new PredictDetail( CBRFileNameList[testrow] , sActual , sPredicted );
    		  }
    		  catch( Exception e) {
    			  do_error("Oops - error while converting back");
    			  return false;
    		  }
    	  }
    	  //do_log( 1 , "Test " + testrow + "   P=" + LastPredictedValue + "   A=" + actual  );
    	}
    	do_log( 1 , "Exec run Hits = " + hits + "/" + testVectorList.length );
    	
    	// 
    	//do_log( 1, trainResult.showConfusionMatrix() );
    	int zz = evaluateRun(ClassNames,false);
    	if( zz != model.getOptimalK() ) { do_log( 1 , "Probably an error Optimal K differs from single K"); }
    	//do_log( 1, trainResult.showConfusionMatrix() );
    	
    	return true;
    }
    
    //------------------------------------------------------------
    public PredictDetail[] getPredictionResults()
    //------------------------------------------------------------
    {
    	return predictlist;
    }
}
