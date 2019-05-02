package generalMachineLearning.SupportVectorMachine;

import cbrTekStraktorModel.cmcProcSettings;
import dao.cmcMachineLearningModelDAO;
import generalMachineLearning.cmcMachineLearningConstants;
import generalMachineLearning.cmcMachineLearningEnums;
import generalMachineLearning.ARFF.ARFFCategory;
import generalMachineLearning.ARFF.ARFFCategoryCoreDTO;
import generalMachineLearning.ARFF.cmcARFF;
import generalMachineLearning.ARFF.cmcARFFRoutines;
import generalMachineLearning.ARFF.cmcTextToARFF;
import generalMachineLearning.Evaluation.cmcModelEvaluation;
import generalMachineLearning.LinearAlgebra.cmcMath;
import generalMachineLearning.LinearAlgebra.cmcMatrix;
import generalMachineLearning.LinearAlgebra.cmcVectorRoutines;
import generalMachineLearning.LogisticRegression.cmcLogisticRegressionDTO;
import logger.logLiason;

public class cmcSMO {
	
	cmcProcSettings xMSet=null;
	logLiason logger=null;
	cmcVectorRoutines vrout = null;
	cmcARFFRoutines arffrout = null;
	cmcMath mrout = null;
	
	private String LastErrorMsg = null;
	private cmcModelEvaluation eval = null;
    private double probedAccuracy = -1;
    
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
	//------------------------------------------------------------
    public String getLastErrorMsg()
	//------------------------------------------------------------
    {
    	return LastErrorMsg;
    }
	//------------------------------------------------------------
    public cmcSMO( cmcProcSettings is,logLiason ilog )
	//------------------------------------------------------------
    {
    	xMSet = is;
		logger=ilog;
		vrout = new cmcVectorRoutines();
		mrout = new cmcMath();
    }
    
    //------------------------------------------------------------
    private boolean checkSMOCategories( ARFFCategory[] categories )
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
        		if( ClassNames.length != 2 ) { do_error("There must be exactly 2 target classes for SMO algoritm"); return false; }
        	}	
        }
        return true;
    }
    
    //------------------------------------------------------------
    public boolean testSMO(String OriginalFileName , String LongModelName , cmcMachineLearningEnums.KernelType kerneltipe)
    //------------------------------------------------------------
    {
       	String DiagramFileName = xMSet.xU.getFileNameWithoutSuffix(OriginalFileName);
    	if( DiagramFileName == null ) { do_error("Cannot strip suffix"); return false; }
        DiagramFileName = DiagramFileName.trim() + ".png";
        //
    	cmcTextToARFF txt = new cmcTextToARFF( xMSet , logger );
    	ARFFCategory[] categories = txt.getCategories( OriginalFileName , DiagramFileName , cmcMachineLearningConstants.NBR_BINS , true);
    	if( checkSMOCategories(categories) == false ) return false;
    	String[] ClassNameList = txt.getClassNames(categories);
    	if( ClassNameList == null ) { do_error("Null class name list"); return false; }
    	if( ClassNameList.length <= 0) { do_error("Empty class name list"); return false; }
        //
        cmcMatrix[] DataTargetTuple = txt.transformIntoMatrix( categories ); // 0 = data  1 = Target
        if( DataTargetTuple == null ) return false;
        //
        cmcSMODTO smodto = new cmcSMODTO( OriginalFileName , DataTargetTuple[0].getNbrOfRows() , 0.6 , 0.001 , 40 , kerneltipe );
        smodto.setBias(0);
              
        // normalize the data and store the normalized values
        cmcMatrix datamatrix = vrout.normalizeMatrix(  DataTargetTuple[0] );
        if( datamatrix == null ) return false;
        //
        smodto.setNormalizerMean(vrout.getNormalizerMean());
        if( smodto.getNormalizerMean() == null ) { do_error("Cannot fetch normalizer mean values"); return false; }
        if( smodto.getNormalizerMean().length != datamatrix.getNbrOfColumns() ) { do_error("Normalizer Mean number of items mismatch "); return false;}
        //
        smodto.setNormalizerStdDev(vrout.getNormalizerStdDev());
        if( smodto.getNormalizerStdDev() == null ) { do_error("Cannot fetch normalizer STDDEV values"); return false; }
        if( smodto.getNormalizerStdDev().length != datamatrix.getNbrOfColumns() ) { do_error("Normalizer STDDEV number of items mismatch "); return false;}
        
        // transform target in -1 and 1 (?? necessary - guess so)
        cmcMatrix resultmatrix = vrout.transform2ZeroAndMinusOne( DataTargetTuple[1] );
        if( resultmatrix == null ) { do_error("Cannot transform to -1/1"); return false; }
        // prepare to train model
        smodto.setDataMatrix(datamatrix);
        smodto.setResultMatrix(resultmatrix);
        // TRAIN MODEL
        boolean ib = simpleSMO( smodto );
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
        smodto.setNbrOfFeatures( cclist.length );
        smodto.setFeatures(cclist);
        smodto.calculateNbrOfSupportVectors();
        if( writeXML( smodto ,  LongModelName ) == false ) return false;
        smodto=null;
        
        // run model on the training set again to test it intrinsically
        do_log( 1 , "Evaluating");
        if( executeModel( LongModelName ,  txt.getTrainingSetName() ) == false ) return false;
        cmcModelEvaluation bres = eval;
        do_log( 1 , eval.showConfusionMatrix() );
        if( bres.getAccuracy() != probedAccuracy ) { do_error("Probed accuracy [" + probedAccuracy + "] differs from confusion matric accuracy [" + bres.getAccuracy() + "]"); return false; }
        
        // run model on test set
        do_log( 1 , "Testing");
        if( executeModel( LongModelName ,  txt.getTestSetName()  ) == false ) return false;
        do_log( 1 , eval.showConfusionMatrix() );
        
        //
        ib=updateModel( bres , eval , LongModelName );
         

do_error( "Ensure classname list matches sequence -1 and 1");

        return ib;
    }
    
    //  returns a 1 x N matrix, ie. vector
    //------------------------------------------------------------
    private cmcMatrix getWeightVector( cmcSMODTO dto )
    //------------------------------------------------------------
    {
    	cmcMatrix data = dto.getDataMatrix();
   	    cmcMatrix result = dto.getResultMatrix();
   	    cmcMatrix alpha = dto.getLagrangeMultiplierMatrix();
   	    
        //  w = sum alpha result x for each i between 1 and number of data rows
    	double[] alphay = new double[ alpha.getNbrOfRows() ];
    	for(int i=0;i<alphay.length;i++)
    	{
    		alphay[i] = alpha.getValues()[i][0] * result.getValues()[i][0];
    	}
    	double[][] weight = new double[ 1 ][ data.getNbrOfColumns() ];
    	for(int i=0;i<data.getNbrOfColumns();i++)
    	{
    		weight[0][i] = 0;
    		for(int rows=0;rows<data.getNbrOfRows();rows++)
    		{
    			weight[0][i] += alphay[rows] * data.getValues()[rows][i]; 
    		}
    	}
    	return new cmcMatrix( weight );  
    }
    
    //------------------------------------------------------------
    private double getPredictedValueNoKernelTrick( int idx , cmcSMODTO dto )
    //------------------------------------------------------------
    {
    	  // predicted f(x(idx)) =  W.x(idx) + b
		  cmcMatrix dataMatrix = dto.getDataMatrix();
    	  //cmcMatrix resultMatrix = dto.getResultMatrix();
    	  //cmcMatrix alphaMatrix = dto.getLagrangeMultiplierMatrix();
    	  
    	  // Weight vector W == sum ai yi Xi   
		  cmcMatrix weightVector = getWeightVector( dto ); // result is 1,N
		  if( weightVector == null ) return Double.NaN;
    	  cmcMatrix Xi = vrout.getRowsAsMatrix( dataMatrix , idx ); //  1,N
		  if( Xi == null ) { do_error( "(getPredictedValue) Getting row " + idx + " as matrix"); return Double.NaN; }
		  
		  // fXi = (weight * xI) + b
		  cmcMatrix tempXi = vrout.multiplyMatrix( weightVector , vrout.transposeMatrix( Xi ) );
		  if ( tempXi ==null ) return Double.NaN;
		  if( tempXi.getNbrOfRows() != 1 ) { do_error( "(getPredictedValue) fXi - row must be 1"); return Double.NaN; }
		  if( tempXi.getNbrOfColumns() != 1 ) { do_error( "(getPredictedValue) fXi - cols must be 1"); return Double.NaN; }
		  double fXi = tempXi.getValues()[0][0] + dto.getBias();  // predicted
		  weightVector=null;
		  Xi=null;
		  tempXi=null;
		  return fXi;
    }
    
    // expected input is [M,N] [1,N]   so a matrix and a vector
    // the routine will perform the transpose
    // should be   [M,n] [ N, 1]   and M can be 1 ; so the result is an M,1 matrix
    //------------------------------------------------------------
    private cmcMatrix performKernelTrick( cmcMatrix XMatrix , cmcMatrix XVector , String source , cmcSMODTO dto)
    //------------------------------------------------------------
    {
     try {
    	if( XMatrix == null ) { do_error( "(kerneltrick) X matrix null"); return null; }
    	if( XVector == null ) { do_error( "(kerneltrick) X vector null"); return null; }
        if( XMatrix.getNbrOfColumns() != XVector.getNbrOfColumns() ) {
          do_error( "(kerneltrick-" + source + ") columns on X and Xi must match"); 
          do_error( "X[" + XMatrix.getNbrOfRows() + "," + XMatrix.getNbrOfColumns() +  "]  V[" + XVector.getNbrOfRows() + "," + XVector.getNbrOfColumns() + "]" ); 
          return null; }
        cmcMatrix kernelMatrix = null;
        
        // Which kernel
        switch( dto.getKernelTrickTipe() )
        {
        case NONE : { kernelMatrix = vrout.multiplyMatrix( XMatrix , vrout.transposeMatrix( XVector ) ); break; }
        case GAUSSIAN : ;
        case RBF  : {
        	double factor = 0;
        	if( dto.getKernelTrickTipe() == cmcMachineLearningEnums.KernelType.GAUSSIAN ) {
        		factor = 1 / ((double)2 * cmcMachineLearningConstants.SMO_GAUSSIAN_SIGMA * cmcMachineLearningConstants.SMO_GAUSSIAN_SIGMA);
        	}
        	if( dto.getKernelTrickTipe() == cmcMachineLearningEnums.KernelType.RBF ) {
        		factor = cmcMachineLearningConstants.SMO_RBF_GAMMA;
        	}
        	if( factor <= 0 ) { do_error( "(kerneltrick-" + source + ") factor <= 0"); return null; }
        	double[][] rbf = new double[XMatrix.getNbrOfRows()][1];  // m,1
        	for(int i=0;i<XMatrix.getNbrOfRows();i++)
        	{
        		double deltaDistance = 0;
        		for(int j=0;j<XMatrix.getNbrOfColumns();j++)  //  voor iedere kolom van de matrix versus iedere kolom vector
        		{
        			double dd = XMatrix.getValues()[i][j] - XVector.getValues()[0][j];    // euclidian = som vd kwadraten verschil
        			deltaDistance += ( dd * dd );
        		}
        		double term = 0 - (deltaDistance * factor);
        		rbf[i][0] = Math.exp( term );
        	}
        	kernelMatrix = new cmcMatrix( rbf );
        	break;
        }
        default : { do_error( "Unsupported Kernel [" + dto.getKernelTrickTipe() + "]"); break; }
        }
        
        // check
        if( kernelMatrix == null )  { do_error( "(kerneltrick) Kernel function error"); return null; }
        if( kernelMatrix.getNbrOfRows() != XMatrix.getNbrOfRows() )  { do_error( "(kerneltrick) result must have [" + XMatrix.getNbrOfRows() + "] rows got [" + kernelMatrix.getNbrOfRows() + "]"); return null; }
        if( kernelMatrix.getNbrOfColumns() != 1)  { do_error( "(kerneltrick) result must have 1 colum"); return null; }
        XMatrix = null;
        XVector=null;
        return kernelMatrix;
     }
     catch( Exception e) {
    		do_error("performKernelTrick");
    		do_error( xMSet.xU.LogStackTrace(e) );
    		return null;
     }
    }
    
    //------------------------------------------------------------
    private double getPredictedValue( int idx , cmcSMODTO dto )
    //------------------------------------------------------------
    {
       	cmcMatrix data = dto.getDataMatrix();
   	    cmcMatrix result = dto.getResultMatrix();
   	    cmcMatrix alpha = dto.getLagrangeMultiplierMatrix();
       	//   (alpha scalarmultiplicatio label).T  ( X X(idx:).T )
   	    //    1,m                          m,n  (1,n)T
   	    //    1,m                          m,n n,1
   	    //    1,1
       	double[][] alphay = new double[1][ alpha.getNbrOfRows() ];  // 1,m
    	for(int i=0;i<alpha.getNbrOfRows();i++)
    	{
    		alphay[0][i] = alpha.getValues()[i][0] * result.getValues()[i][0];
    	}
    	cmcMatrix alphaLabelMatrix = new cmcMatrix( alphay );
    	//if( alphaLabelMatrix == null ) { do_error( "(getPredicted) alphaLabelMatrix"); return Double.NaN; }
    	cmcMatrix Xi = vrout.getRowsAsMatrix( data , idx ); //  1,N
    	if ( Xi == null ) { do_error( "(getPredicted) Xi vector"); return Double.NaN; }  
    	//  KERNELTRICK
    	// OLD cmcMatrix xxiMatrix = vrout.multiplyMatrix( data , vrout.transposeMatrix( Xi ) );  // m,n n, 1  => m,1
    	cmcMatrix xxiMatrix = this.performKernelTrick( data , Xi , "PREDICTED" , dto);  // m,n 1,n  kernel will bring back m,1
    	if( xxiMatrix == null ) { do_error( "(getPredicted) X xi"); return Double.NaN; }
    	//
    	cmcMatrix predictMatrix = vrout.multiplyMatrix( alphaLabelMatrix ,  xxiMatrix);  // 1,m  m,1  => 1,1
    	if( predictMatrix == null ) { do_error( "(getPredicted) predictMatrix"); return Double.NaN; }
    	if( predictMatrix.getNbrOfColumns() != 1)  { do_error( "(getPredicted) #cols must be 1"); return Double.NaN; }
    	if( predictMatrix.getNbrOfRows() != 1)  { do_error( "(getPredicted) #rows must be 1"); return Double.NaN; }
    	double res = predictMatrix.getValues()[0][0] + dto.getBias();
    	//  DEBUG
    	// double result1 =  getPredictedValueNoKernelTrick( idx , dto );
    	// if( Math.abs(result1 - res) > 0.00001 ) { do_error( "FOUT" + result1 + " " + res); return Double.NaN; }
    	//
    	predictMatrix=null;
    	xxiMatrix=null;
    	alphaLabelMatrix=null;
    	alphay=null;
    	return res;
    }
    
    // You can make this faster, just get the 2 arrays of the double values and multiply/sum
   //------------------------------------------------------------
    private double getKernelTrickedXiXj( cmcMatrix dataMatrix , int i ,  int j , cmcSMODTO dto)
    //------------------------------------------------------------
    {
    	cmcMatrix Xi = vrout.getRowsAsMatrix( dataMatrix , i );   // 1,n
        if( Xi == null ) { do_error( "(getKxixj) get vector Xi"); return Double.NaN; }
        cmcMatrix Xj = vrout.getRowsAsMatrix( dataMatrix , j );
        if( Xj == null ) { do_error( "(getKxixj) get vector Xj"); return Double.NaN; }
        // vervang door kernel trick
        //cmcMatrix xixj = vrout.multiplyMatrix( Xi , vrout.transposeMatrix( Xj ) );
        cmcMatrix xixj = performKernelTrick( Xi , Xj , "XIXJ" , dto);  // 1,n 1,n  kernel will bring back 1,1
        if( xixj ==  null ) { do_error( "(getKxixj) xixj"); return Double.NaN; }
        if( xixj.getNbrOfColumns() != 1 ) { do_error( "(getKxixj) xixj cols"); return Double.NaN; }
        if( xixj.getNbrOfRows() != 1 ) { do_error( "(getKxixj) xixj rows"); return Double.NaN; }
        double dxixj = xixj.getValues()[0][0];
        Xi = null;
        Xj = null;
        xixj = null;
        return dxixj;
    }
    
    //------------------------------------------------------------
    private double clipAlpha(double alpha , double H , double L )
    //------------------------------------------------------------
    {
      if( alpha > H ) return H;
	  if( alpha < L ) return L;
	  return alpha;
    }
    
    //------------------------------------------------------------
    private int getNextIndexForMultiplier( int firstIdx , cmcSMODTO dto)
    //------------------------------------------------------------
    {
    	try  {
    	  if( dto.isFULL_PLATT_ALGORITHM() == false ) return mrout.getRandomNumberExcluding( dto.getNbrOfRows() , firstIdx );
    	  //
    	  if( recalculateAndStoreError( firstIdx , dto) == false )   { do_error( "getNextIndexForMultiplier I"); return -1; }
    	  double Ei = dto.getErrorCache()[firstIdx];
    	  double Ej = 0;
    	  int maxIdx=-1;
          double maxDeltaError = 0;
          for(int j=0; j<dto.getErrorCache().length ; j++)
          {
        	if( j == firstIdx ) continue;
        	Ej = dto.getErrorCache()[j];
        	double delta = Math.abs(Ei - Ej);
        	if( delta > maxDeltaError ) {
        		maxDeltaError = delta;
        		maxIdx=j;
        	}
          }
          if( maxIdx > -1 ) return maxIdx;
          // there are no errors > 0 => random
          return mrout.getRandomNumberExcluding( dto.getNbrOfRows() , firstIdx );
    	}
    	catch(Exception e ) {
    		do_error( "getNextIndexForMultiplier " + e.getMessage() ); 
    		return -1;
    	}
    }
    
    //------------------------------------------------------------
    private double calculateError( int idx , cmcSMODTO dto )
    //------------------------------------------------------------
    {
    	try {
    	  double fXidx =  getPredictedValue(  idx , dto );
		  if( Double.isNaN(fXidx) ) { do_error( "return from getPredictedValue is NAN"); return Double.NaN; }
		  double Eidx = fXidx - dto.getResultMatrix().getValues()[idx][0];  // Mind the correct order  !!!
		  return Eidx;
    	}
    	catch( Exception e ) {
    		do_error( "calculateError");
    		do_error( xMSet.xU.LogStackTrace(e));
    		return Double.NaN;
    	}
    }
    
    //------------------------------------------------------------
    private boolean recalculateAndStoreError( int idx , cmcSMODTO dto)
    //------------------------------------------------------------
    {
    	try {
    	  double Eidx = calculateError( idx , dto );
		  if( Double.isNaN(Eidx) ) { do_error( "Recalculate and store error I"); return false; }
		  if ( dto.setErrorCacheIdxValue( idx , Eidx ) == false ) {
			do_error("Recalculate and store error II");
			return false;
		  }
		  return true;
    	}
    	catch(Exception e ) {
    		do_error("Recalculate and store error III");
    		return false;
    	}
    }
    
    //------------------------------------------------------------
    private int innerLoop(int i , cmcSMODTO dto)
    //------------------------------------------------------------
    {
    	  cmcMatrix dataMatrix = dto.getDataMatrix();
      	  cmcMatrix resultMatrix = dto.getResultMatrix();
      	  cmcMatrix alphaMatrix = dto.getLagrangeMultiplierMatrix();
      	  //
		  double Ei = calculateError( i , dto );
		  if( Double.isNaN(Ei) ) return -1;
		  // see whether Lagrange multipliers can be changed
		  if( (((resultMatrix.getValues()[i][0]*Ei) < (0-dto.getTolerance())) && (alphaMatrix.getValues()[i][0] < dto.getC()))  ||
		      (((resultMatrix.getValues()[i][0]*Ei) > dto.getTolerance()) && (alphaMatrix.getValues()[i][0] > 0)) ) 
		  {
			  //do_log( 1 , "Cycle=" + cycle + " fXi=" + fXi + " Ei=" + Ei);
			  int j = getNextIndexForMultiplier( i , dto );
			  if( j < 0 ) { do_error( "Fetching next alpha index"); return -1; }
			  double Ej = calculateError( j , dto );
			  if( Double.isNaN(Ej) ) return -1;
			  //
			  double alphaIOld = alphaMatrix.getValues()[i][0];
			  double alphaJOld = alphaMatrix.getValues()[j][0];
			  double labelI = resultMatrix.getValues()[i][0];
			  double labelJ = resultMatrix.getValues()[j][0];
			  double L = Double.NaN;
			  double H = Double.NaN;
			  //  alphas must always between 0 and C
			  if( labelI != labelJ ) {
				  L = Math.max(  0 , (alphaJOld - alphaIOld) );
				  H = Math.min(  dto.getC() , dto.getC() + (alphaJOld - alphaIOld) );
			  }
			  else {
				  L = Math.max(  0 , (alphaJOld + alphaIOld) - dto.getC() );
				  H = Math.min(  dto.getC() , (alphaJOld + alphaIOld) );
			  }
			  if( L==H ) { 
				  //do_log( 1 , "H==L"); 
				  //continue;
				  return 0;
			  }
			  // 2nd lagrange multiplier - begin met noemer
			  double dKxixi = getKernelTrickedXiXj( dataMatrix , i , i , dto); if( Double.isNaN(dKxixi)) return -1;
			  double dKxjxj = getKernelTrickedXiXj( dataMatrix , j , j , dto); if( Double.isNaN(dKxjxj)) return -1;
  	      	  double dKxixj = getKernelTrickedXiXj( dataMatrix , i , j , dto); if( Double.isNaN(dKxixj)) return -1;
  	      	  double eta = dKxixi + dKxjxj - ((double)2 * dKxixj);
  	      	  if( Double.isNaN( eta ) ) return -1;
  	      	  if( eta <= 0 ) {  //  Why ?? 
  	      		  //continue;
  	      		  return 0;
  	      	  }  
  	      	  //  2nd Lagrange multiplier - alpha2
  	      	  double alphaJ = alphaJOld + ((labelJ * (Ei - Ej)) / eta);
  	      	  alphaJ = clipAlpha( alphaJ , H , L );
  	      	  dto.getAlphaMatrix().setValue( j , 0 , alphaJ );
  	      	  if( recalculateAndStoreError( j , dto ) == false ) return -1;
  	      	  //
  	      	  if( Math.abs( alphaJ - alphaJOld ) < 0.00001 ) { 
  	      		  //do_log( 1, "alpha2 not moving enough"); 
  	      		  //continue;
  	      		  return 0;
  	      	  }
  	      	  //  1st Lagrange multiplier   alhpa + yiyj(alph2old ) alpha2)
  	      	  double alphaI = alphaIOld + (labelI * labelJ * ( alphaJOld - alphaJ));
  	      	  dto.getAlphaMatrix().setValue( i , 0 , alphaI );
  	          if( recalculateAndStoreError( i , dto ) == false ) return -1;
  	      	  //  B
  	      	  double b1 = dto.getBias() - Ei - (labelI * (alphaI - alphaIOld) * dKxixi) - (labelJ * (alphaJ - alphaJOld) * dKxixj);
  	      	  double b2 = dto.getBias() - Ej - (labelI * (alphaI - alphaIOld) * dKxixj) - (labelJ * (alphaJ - alphaJOld) * dKxjxj);
  	      	  //
  	      	  if( (0 < alphaI) && (dto.getC() > alphaI) ) dto.setBias(b1);
  	      	  else
  	      		  if( (0 < alphaJ) && (dto.getC() > alphaJ) ) dto.setBias(b2);
  	      		  else {
  	      			  dto.setBias( ( b1 + b2) / (double)2 );
  	      	  }
  	      	  return 1;
		  }
		  else { // no change possible
			  return 0;
		  }
    }
    
    //------------------------------------------------------------
    private boolean simpleSMO( cmcSMODTO dto )
    //------------------------------------------------------------
    {
    	cmcMatrix dataMatrix = dto.getDataMatrix();
    	cmcMatrix resultMatrix = dto.getResultMatrix();
    	String err = dto.checkDTO();
    	if( err != null ) {
    		do_error(err);
    		return false;
    	}
        //	
    	cmcMatrix dataMatrixTransposed = vrout.transposeMatrix( dataMatrix );
    	if( dataMatrixTransposed == null ) { do_error("Cannot transpose datamatrix"); return false; }
    	//
    	cmcMatrix resultMatrixTransposed = vrout.transposeMatrix( resultMatrix );
    	if( resultMatrixTransposed == null ) { do_error("Cannot transpose resultmatrix"); return false; }
    	if( dataMatrixTransposed.getNbrOfColumns() != resultMatrixTransposed.getNbrOfColumns()) { do_error("Transpose error data-result"); return false; }
    	
    	// This is the non-optimized version - to be avoided
    	if( dto.isFULL_PLATT_ALGORITHM() == false )
    	{
    		int cycle = 0;
    		while( cycle < dto.getMaxCycles() )
    		{
    			int alphaPairChanges=0;
    			for(int i=0;i<dto.getNbrOfRows();i++)
    			{
    				int change = innerLoop( i , dto );
    				if( change < 0 ) { do_error( "innerloop error"); return false; }
    				alphaPairChanges += change;
    			} // rows
    			do_log( 1 , "Iteration " + cycle + " PairChanges=" + alphaPairChanges );
    			if( alphaPairChanges != 0 ) cycle=0; // herbegin
    								   else cycle++;
    		}
    	}
    	// FULL Platt SMO version
    	else {
    		int alphaPairChanges=0;
        	boolean entireSet=true;
        	int cycle = 0;
        	while ( (cycle <dto.getMaxCycles()) || (alphaPairChanges>0) || (entireSet==true) )
        	{
        		alphaPairChanges=0;
        		if( entireSet ) {
        			for(int i=0;i<dto.getNbrOfRows();i++)
        			{
        				int change = innerLoop( i , dto );
        				if( change < 0 ) { do_error( "innerloop error I"); return false; }
        				alphaPairChanges += change;
        			}
        			do_log( 1 , "[#Iter" + cycle + "] [AlphaPairChanges=" + alphaPairChanges +"]");
        			cycle++;
        		}
        		else {
        		    //
        			boolean[] nonBoundList = new boolean[ dto.getNbrOfRows() ];
        			for(int i=0;i<nonBoundList.length ; i++ ) nonBoundList[i]=false;
        			cmcMatrix alphaMatrix = dto.getLagrangeMultiplierMatrix();
        			for(int i=0;i<alphaMatrix.getNbrOfRows();i++)
        			{
        				if( (alphaMatrix.getValues()[i][0] > 0) && (alphaMatrix.getValues()[i][0] < dto.getC()) ) nonBoundList[i]=true;
        			}
        			//
        			for(int i=0;i<nonBoundList.length;i++)
        			{
        				int change = innerLoop( i , dto );
        				if( change < 0 ) { do_error( "innerloop error II"); return false; }
        				alphaPairChanges += change;
        			}
        			do_log( 1 , "[#Iter" + cycle + "] [AlphaPairChanges=" + alphaPairChanges +"] [#nonBounds=" + nonBoundList.length + "]");
        			cycle++;
        		}
        		if( entireSet == true ) entireSet = false;
        		else 
        		if( alphaPairChanges == 0 ) entireSet = true;
        	}
        }
    	// save the result
    	dto.setWeightMatrix(  getWeightVector( dto ) ); // result is 1,N
    	if( dto.getWeightMatrix() == null ) return false;
    	    	   
		//do_log( 1 , "Weights " + vrout.printMatrix( dto.getWeightMatrix() ) );
		//do_log( 1 , "B value=" + dto.getB());
    	cmcMatrix res = vrout.multiplyMatrix( dto.getWeightMatrix() , vrout.transposeMatrix( dataMatrix ) ); // 1,n n,m
		if( res == null ) { do_error("RES"); return false; }
		if( res.getNbrOfRows() != 1) { do_error("Determine res - cols"); return false; }
		if( res.getNbrOfColumns() != dataMatrix.getNbrOfRows()) { do_error("Determine res - rows"); return false; }
		// add b
		for(int i=0;i<res.getNbrOfColumns();i++)
		{
			res.setValue( 0 , i , res.getValues()[0][i] + dto.getBias() );
		}
		// match
		int hit=0;
		for(int i=0;i<res.getNbrOfColumns();i++)
		{
			res.setValue( 0 , i , res.getValues()[0][i] * resultMatrix.getValues()[i][0] );
			if( res.getValues()[0][i] > 0 ) hit++;
		}
		//do_log( 1 , "RESULT" + vrout.printMatrix( res  ));
		
    	
		probedAccuracy = (double)hit / (double)resultMatrix.getNbrOfRows();
		do_log( 1 , "Hits=" + hit +  " op " + resultMatrix.getNbrOfRows() + " Rat=" +  probedAccuracy );
		return true;
    }
       
    //------------------------------------------------------------
    private boolean writeXML( cmcSMODTO dto , String LongModelName )
    //------------------------------------------------------------
    {
    	cmcMachineLearningModelDAO dao = new cmcMachineLearningModelDAO( xMSet , logger );
    	boolean ib = dao.writeSMO( dto , LongModelName );
    	dao=null;
    	return true;
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
    public boolean executeModel( String ModelFileName , String ARFFFileName )
    //------------------------------------------------------------
    {
      try {	
        cmcMachineLearningModelDAO dao = new cmcMachineLearningModelDAO(xMSet,logger);
    	cmcSMODTO model = dao.readSMOModel(ModelFileName);
        if( model == null ) return false;
        //
    	cmcTextToARFF txt = new cmcTextToARFF( xMSet , logger );
    	ARFFCategory[] categories = txt.getCategories( ARFFFileName , null , cmcMachineLearningConstants.NBR_BINS , false);
        if( this.checkSMOCategories(categories) == false ) return false;
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
        
        // transform target in -1 and 1 (?? necessary - guess so)
        cmcMatrix resultMatrix = vrout.transform2ZeroAndMinusOne( DataTargetTuple[1] );
        if( resultMatrix == null ) { do_error("Cannot transform to -1/1"); return false; }
        if( resultMatrix.getNbrOfRows() <= 0 ) { do_error( "No rows on results"); return false; }
        if( resultMatrix.getNbrOfColumns() != 1) { do_error( "Result must be M,1 " + resultMatrix.getNbrOfRows() + " " + resultMatrix.getNbrOfColumns() ); return false; }
        if( rawMatrix.getNbrOfRows() != resultMatrix.getNbrOfRows() ) { do_error( "Number of rows differs on data and results  D=" + rawMatrix.getNbrOfRows() + " R=" + resultMatrix.getNbrOfRows()); return false; }
        
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
        if( normalizedMatrix == null ) { do_error( "NULL normalized matrix"); return false; }
       
        // 
        cmcMatrix weightMatrix = model.getWeightMatrix();  //  dimensionality 1,N
        if( weightMatrix == null ) return false;
        if( weightMatrix == null ) { do_error( "NULL weight matrix"); return false; }
        if( weightMatrix.getNbrOfColumns() <= 0 ) { do_error( "No columns on w"); return false; }
        if( weightMatrix.getNbrOfRows() <= 0 ) { do_error( "No rows on w"); return false; }
        if( weightMatrix.getNbrOfColumns() != normalizedMatrix.getNbrOfColumns()) { do_error( "Weight matrix must be [1,N] @cols=" + weightMatrix.getNbrOfColumns() ); return false; }
        if( weightMatrix.getNbrOfRows() != 1) { do_error( "Weight matrix must be [1,N] - #rows=" + weightMatrix.getNbrOfRows()); return false; }
        if( (normalizedMatrix.getNbrOfColumns()) != weightMatrix.getNbrOfColumns() ) {
        	do_error( "The number of columns on the data matrix must be number of weights [R=" + normalizedMatrix.getNbrOfColumns() + "] [W=" + weightMatrix.getNbrOfRows() + "]"); // will be augmented
        	return false;
        }
 	    //
		cmcMatrix calculatedMatrix = vrout.multiplyMatrix( model.getWeightMatrix() , vrout.transposeMatrix( normalizedMatrix ) ); // 1,n n,m => 1,M
		if( calculatedMatrix == null ) { do_error("calculatedmatrix is null"); return false; }
		if( calculatedMatrix.getNbrOfColumns() != normalizedMatrix.getNbrOfRows()) { do_error("Calculatematrix - columns " + calculatedMatrix.getNbrOfColumns()); return false; }
		if( calculatedMatrix.getNbrOfRows() != 1) { do_error("calculatedmatrix rows must be 1"); return false; }
		// add b
		for(int i=0;i<calculatedMatrix.getNbrOfColumns();i++)  // 1,M
		{
			calculatedMatrix.setValue( 0 , i , calculatedMatrix.getValues()[0][i] + model.getBias() );
		}
		
        //
        int hit=0;
        if( ClassNameList == null ) { do_error( "NULL classnamelist"); return false; }
        if( ClassNameList.length != 2 ) { do_error( "Classnamelist must have exactly 2 entries"); return false; }
        eval = new cmcModelEvaluation( ClassNameList );
        // only if a genuine CBRTEKSTRAKTOR ARFF file is used this list will be set
        String[] CBRFileNameList = txt.getValidatedCBRFileNameList( calculatedMatrix.getNbrOfRows() );  
        //do_error( "C->" + calculatedMatrix.getNbrOfRows() + " " + calculatedMatrix.getNbrOfColumns() );
        //do_error( "R->" + resultMatrix.getNbrOfRows() + " " + resultMatrix.getNbrOfColumns() );
        for(int i=0;i<calculatedMatrix.getNbrOfColumns();i++)  // 1,M
        {
        	int correct = calculatedMatrix.getValues()[0][i] * resultMatrix.getValues()[i][0] >= 0 ? 1 : -1;   // 1,M  M,1  -1*-1=1 1*-1=-1 1*1=1  
        	if( correct == 1 ) hit++;
        	String calculated = calculatedMatrix.getValues()[0][i] >= 0 ? ClassNameList[0] : ClassNameList[1];
        	String anticipated = resultMatrix.getValues()[i][0] >= 0 ? ClassNameList[0] : ClassNameList[1];
            //do_error( "CALC [" + calculatedMatrix.getValues()[0][i] + "] [" + resultMatrix.getValues()[i][0] +  "] [" + correct + "]");
            //do_error( "CALC [" + calculated + "] [" + anticipated +  "] [" + correct + "]");
       
         	boolean ib = eval.addToConfusion( calculated , anticipated);
            if( ib == false ) { do_error("addconfusion"); return false; };
        }
        double pp = (double)hit / (double)calculatedMatrix.getNbrOfColumns();
        do_log( 1 , "Hits [" + hit + "] [" + calculatedMatrix.getNbrOfColumns() + "] P=" + pp);
    	
    	//
    	rawMatrix = null;
    	resultMatrix = null;
    	calculatedMatrix = null;
    	weightMatrix = null;
    	normalizedMatrix = null;
    	txt = null;
        dao=null;
    	
        return true;
      }
      catch(Exception e ) {
    	  do_error( "Oops - Exec model");
    	  do_error( xMSet.xU.LogStackTrace(e ));
    	  return false;
      }
    }
}
