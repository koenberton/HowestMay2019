package generalMachineLearning.Bayes;

import cbrTekStraktorModel.cmcProcSettings;
import dao.cmcARFFDAO;
import dao.cmcMachineLearningModelDAO;
import featureExtraction.PredictDetail;
import generalMachineLearning.cmcModelMaker;
import generalMachineLearning.ARFF.ARFFCategory;
import generalMachineLearning.ARFF.cmcARFF;
import generalMachineLearning.ARFF.cmcARFFRoutines;
import generalMachineLearning.ARFF.cmcARFF.ARFFattribute;
import generalMachineLearning.Evaluation.cmcModelEvaluation;
import logger.logLiason;

public class cmcModelMakerNaiveBayes implements cmcModelMaker {

	private static boolean DEBUG = false;
		
	cmcProcSettings xMSet=null;
	logLiason logger=null;
	
	int NBR_BINS = -1;
	
	cmcARFFRoutines arffrout = null;
	enum MODELING_STATUS   { UNKNOWN , TRAINING , EVALUATING , TESTING }
	MODELING_STATUS modstat = MODELING_STATUS.UNKNOWN;
	
	class ConditionalProbability
	{
		   String attributeName;
		   String className;
		   double inputvalue;
		   double BinClassProbability;
		   double BinProbability;
		   ConditionalProbability(String a, String c , double din)
		   {
			   attributeName       = a;
			   className           = c;
			   inputvalue          = din;
			   BinClassProbability = Double.NaN;
			   BinProbability      = Double.NaN;
		   }
	}
		
	PredictDetail[] predictlist=null;
	
	
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
    public cmcModelMakerNaiveBayes(cmcProcSettings is,logLiason ilog)
    //------------------------------------------------------------
    {
    	xMSet = is;
		logger=ilog;
		arffrout = new cmcARFFRoutines( xMSet , logger);
    }

     // P(A|B) = P(B|A) * P(A) / P(B)
 	 // gathers statistics on the bin contents
     // per bin, per bin-class and per class for each category except of type CLASS
     //------------------------------------------------------------
     private cmcBayesModel gatherBayesStats( ARFFCategory cat , int nbrclasses )
     //------------------------------------------------------------
     {
     	// first populate the histogram for this category
     	if( cat.populateHistogram() == false ) {
     		do_error("Something went wrong populating the histograms");
     		return null;
     	}
     	
     	// POPULATE MODEL
     	cmcBayesModel model = new cmcBayesModel( NBR_BINS , nbrclasses , cat.getCategoryName() , cat.getTipe() );
     	for(int i=0; i< cat.getBinsegmentbegins().length ; i++) model.binsegmentbegins[i] = cat.getBinsegmentbegins()[i];
     	for(int i=0;i<NBR_BINS;i++)
     	{
     		for(int j=0;j<nbrclasses;j++)
     		{
     			model.BinClassCounts[i][j] = cat.getBinClassHistogram()[i][j];
     		}
     	}
         // set the nominal names
     	if( (cat.getTipe() == cmcARFF.ARFF_TYPE.NOMINAL) || (cat.getTipe() == cmcARFF.ARFF_TYPE.CLASS) )
     	{
     	  model.NominalNameList = cat.getNominalValueList();
     	  if( model.NominalNameList == null ) {
     		  do_error("There should be a list of nominal value - got null");
     		  return null;
     	  }
     	  if( model.NominalNameList.length < 1) {
     		  do_error("There are no nominal values on the list for [" + cat.getCategoryName() + "]");
     		  return null;
     	  }
     	}
         // aggregate
     	if( model.performAggregate() == false ) return null;
     	
     	//
     	return model;
     }
    
     //------------------------------------------------------------
     private boolean dumpModels( String BayesModelFileName , cmcBayesModel[] list)
     //------------------------------------------------------------
     {
     	cmcMachineLearningModelDAO dao = new cmcMachineLearningModelDAO( xMSet , logger );
     	boolean ib= dao.writeBayesModel( list , BayesModelFileName );
     	dao=null;
     	if( ib == false ) {
     		do_error( "Could not create [" + BayesModelFileName + "]");
     		return false;
     	}
     	do_log( 1 , "Bayes model have been stored in [" + BayesModelFileName + "]");
     	return ib;
     }
    
   //------------------------------------------------------------
     public cmcBayesModel[] readBayesModels(String BayesModelFileName )
     //------------------------------------------------------------
     {
     	cmcMachineLearningModelDAO dao = new cmcMachineLearningModelDAO( xMSet , logger );
     	cmcBayesModel[] list = dao.readBayesModel(BayesModelFileName );
     	dao=null;
     	return list;
     }
     
    //------------------------------------------------------------
    public boolean trainModel(	String OriginalFileName , String ModelFileName )
    //------------------------------------------------------------
    {
 		do_error( "SYTEM ERROR");
     	return false;
    }
   
    //------------------------------------------------------------
    public boolean trainModel(	ARFFCategory[] categories , int nbins , String[] ClassNameList , String ModelFileName )
    //------------------------------------------------------------
    {
    	NBR_BINS = nbins;
    	modstat = MODELING_STATUS.TRAINING;
    	
    	 // P(A|B) = P(B|A) * P(A) / P(B)
        int nums=-1;
        for(int i=0 ; i<categories.length; i++)
        {
        	if( categories[i].getTipe() == cmcARFF.ARFF_TYPE.NUMERIC ) nums++;
        	if( categories[i].getTipe() == cmcARFF.ARFF_TYPE.NOMINAL ) nums++;
        }	
        if( nums < 0 ) {
        	do_error("There are no numeric nor nominal classes");
        	return false;
        }
        cmcBayesModel[] models = new cmcBayesModel[ categories.length ];
        for(int i=0 ; i<categories.length; i++)
        {
        	if( (categories[i].getTipe() == cmcARFF.ARFF_TYPE.NUMERIC) ||
        		(categories[i].getTipe() == cmcARFF.ARFF_TYPE.CLASS) ||
        		(categories[i].getTipe() == cmcARFF.ARFF_TYPE.NOMINAL) ) 
        	{
        	 do_log( 1 , "Calculating the Bayes distributions per bin for [" +  categories[i].getCategoryName() + "]");
        	 cmcBayesModel model = gatherBayesStats( categories[i] , ClassNameList.length  );
        	 if ( model == null ) return false;
        	 for(int k=0;k<ClassNameList.length;k++) model.ClassNameList[k] = ClassNameList[k];
           	 models[ i ] = model;
        	}
        }
        //
        dumpModels( ModelFileName , models );
        //              
        return true;
    }
    
    //------------------------------------------------------------
    public boolean testModel( String ModelFileName , String FileNameTestSet , cmcARFF xa)
    //------------------------------------------------------------
    {
    	 cmcBayesModel[] models = null;
         models = readBayesModels(ModelFileName);
         if( models == null ) return false;
         
         // check the internal quality of the model
     	 modstat = MODELING_STATUS.EVALUATING;
         cmcModelEvaluation bres = makeConfusionMatrix( xa , models );
         if( bres == null ) return false;
         bres.calculate();
        
         // run the bayes model against the test set
     	 modstat = MODELING_STATUS.TESTING;
         cmcARFFDAO dao = new cmcARFFDAO( xMSet , logger );
     	 if( dao.performFirstPass(FileNameTestSet) == false )  return false;
     	 do_log(1,"ARRF[" + FileNameTestSet + "] [Attribs=" + dao.getNumberOfAttributes() +"][Rows=" + dao.getNumberOfDataRows() + "]");
     	
     	 // second pass - read the data from the training set
     	 xa = dao.performSecondPass(FileNameTestSet);
     	 if( xa == null ) return false;
         
     	 // check the quality of the model against test set
     	 cmcModelEvaluation tres = makeConfusionMatrix( xa , models );
         if( tres == null ) return false;
         tres.calculate();
     	
         // write back the results of the Evaluation
         if( insertEvaluation ( ModelFileName , bres, tres ) == false ) return false;
         
         //
     	 dao = null;
     	 return true;
    }
    
    //------------------------------------------------------------
    private boolean insertEvaluation( String BayesModelFileName , cmcModelEvaluation bres , cmcModelEvaluation tres )
    //------------------------------------------------------------
    {
    	do_log( 1 , "Model accuracy [" + bres.getAccuracy() + "]");
    	do_log( 1 , bres.showConfusionMatrix() );
    	do_log( 1 , "Train accuracy [" + tres.getAccuracy() + "]");
    	do_log( 1 , tres.showConfusionMatrix() );
        //	
    	cmcMachineLearningModelDAO dao = new cmcMachineLearningModelDAO( xMSet ,logger );
        boolean ib = dao.insertEvaluation( bres , tres , (arffrout.getRandomize() ? null : "Caution the samples are not randomized - do you really want to run  in debug mode") , BayesModelFileName );
    	dao = null;
    	return ib;
    }
    
    //------------------------------------------------------------
    private double[] getClassProbabilities(String[] ClassNameList , cmcBayesModel[] models)
    //------------------------------------------------------------
    {
    	double[] classprobabilities = null;
    	for(int i=0;i<models.length;i++)
    	{
    		if( models[i] == null ) continue;  // STRING/CLASS models are empty
    		classprobabilities = models[i].ClassProbs;
    	    for(int j=0;j<classprobabilities.length;j++) {
                //do_log( 1 , "" + models[i].categoryName + " " + classprobabilities[j]);
    	    	if( Double.isNaN( classprobabilities[j])) {
    	    		classprobabilities = null;
    	    		continue;
    	    	}
    	    }
    		if( classprobabilities != null ) break;
    	}
    	if( classprobabilities == null ) {
    		do_error("Cannot extract class probabilty");
    		return null;
    	}
    	if( ClassNameList.length != classprobabilities.length ) {
    		do_error( "Number of classes does not match number  of class probabilities ");
    		return null;
    	}
    	
    	String sl="Class probabilities ";
    	for(int i=0;i<ClassNameList.length;i++)
    	{
    		if( classprobabilities[i] <= 0 ) {
    			do_log( 1 , "There is a ZERO probabilty [" + classprobabilities[i] + "][" + ClassNameList[i] + "]");
    			//return null;  // IS OK -> if there is no classprobability predict will be 0
    		}
    		sl += "[" + ClassNameList[i] + "=" + classprobabilities[i] +"]";
    	}
    	do_log( 1 , sl );
    	//
    	return classprobabilities;
    }
    
    //------------------------------------------------------------
    private String[] getClassNameList(cmcBayesModel[] models)
    //------------------------------------------------------------
    {
    	String[] ClassNameList = null;
    	for(int i=0;i<models.length;i++)
    	{
    		if( models[i] == null ) continue;  // STRING/CLASS models are empty
    		ClassNameList = models[i].ClassNameList;
    		if( ClassNameList != null ) break;
    	}
    	if( ClassNameList == null ) {
    		do_error("Cannot extract classes");
    		return null;
    	}
    	if( ClassNameList.length < 1 ) {
    		do_error( "There are too few classes in the model [" + ClassNameList.length + "]");
    		return null;
    	}
    	return ClassNameList;
    }
    
    //------------------------------------------------------------
    private int getIndexFirstNumericCategoryAndVerify(cmcARFF xa  , cmcBayesModel[] models)
    //------------------------------------------------------------
    {
    	if( arffrout == null ) {
    		arffrout = new cmcARFFRoutines( xMSet , logger);
    	}
    	int idx = arffrout.getIndexFirstNumericCategory( xa );
    	if( idx < 0 ) return -1;
    	
    	// verify whether the sequence of the models is the same as the sequence of the attributes
    	if( models.length != xa.getAttributeList().length ) {
    		do_error( "Model and ATTR have not the same number of entries [" + models.length + "] [" +  xa.getAttributeList().length + "]");
			return -1;
    	}
    	for(int i=0;i<xa.getAttributeList().length;i++)
    	{
    		ARFFattribute attr = xa.getAttributeList()[i];
    		if( attr.getTipe() != cmcARFF.ARFF_TYPE.NUMERIC ) continue;
    		if( attr.getAttrName().compareToIgnoreCase( models[i].categoryName ) != 0 ) {
    				do_error( "Model and ATTR not  in line " +  attr.getAttrName() + " " +  models[i].categoryName + " " + i );
    				return -1;
    		}
    	}
        return idx;
    }
    
    //------------------------------------------------------------
    private String predictBayesResult(cmcARFF xa  , cmcBayesModel[] models ,  double[] ClassProbabilities , double[] attributevalues , String anticipated)
    //------------------------------------------------------------
    {
    	boolean isOK=true;
        int nattributes = xa.getAttributeList().length;
        String[] ClassNameList = getClassNameList( models );
        if( ClassNameList == null ) return null;
        int nclasses = ClassNameList.length;
        //
    	ConditionalProbability[][] pset = new ConditionalProbability[nattributes][nclasses];
		for(int i=0;i<pset.length;i++)
		{
			for(int j=0;j<pset[i].length;j++) pset[i][j]=null;
		}
		//
		int HistogramEntries = -1;
		for(int col=0 ; col < xa.getAttributeList().length ; col++ )
		{
		   ARFFattribute attr = xa.getAttributeList()[col];
		   if( attr.getTipe() == cmcARFF.ARFF_TYPE.STRING ) continue;  
		   if( attr.getTipe() == cmcARFF.ARFF_TYPE.CLASS ) continue;  // do not use the class to calculate class .. 
		   if( HistogramEntries < 0 ) HistogramEntries = models[ col ].getHistogramEntries();
		   for(int classcntr = 0 ; classcntr < nclasses ; classcntr++)
		   {
			 if( Double.isNaN( attributevalues[col] ) ) {
				 do_error("Got a NAN on the attribute value list");
				 return null;
			 }
   			 ConditionalProbability proba = new ConditionalProbability( attr.getAttrName() , ClassNameList[classcntr] , attributevalues[col] );
			 //  FETCH related conditional probability ie. BinClass ( name , class , value , from model )
			 proba.BinClassProbability = models[ col ].getBinClassProbability( proba.className , attributevalues[col] );
			 if( Double.isNaN( proba.BinClassProbability ) ) {
				 do_error("Could not determine Bin Class probability for [" + proba.attributeName + " " + proba.className + " " + proba.inputvalue + "]");
				 return null;
			 }
			 // fetch the BIN probability for divisor (normaliser) from the model
			 proba.BinProbability = models[ col ].getBinProbability( attributevalues[col] );
			 if( Double.isNaN( proba.BinProbability ) ) {
				 do_error("Could not determine Bin probability for [" + proba.attributeName + " " + proba.className + " " + proba.inputvalue + "]");
				 return null;
			 }
			 // debug
			 //if( (int)attributevalues[col] == 1055 )  do_error( models[col].dumper(attributevalues[col]) );
			 //
			 pset[ col ][ classcntr ] = proba;
		   }
		}
	    if( HistogramEntries <= 0) {
	    	do_error("Could not fetch number of elements");
	    	return null;
	    }
		
		// Calculate Bayes predicted values
		double feebleProbability = 1 / (double)(HistogramEntries + 1);
		double[] predicted = new double[ nclasses ];
		for(int i=0;i<nclasses;i++)
		{
			double teller=1;
			double noemer=1;
			for(int j=0;j<nattributes;j++)
			{
				if( pset[j][i] == null ) continue;
				// teller : indien de kans 0 is zet dan een zeer kleine kans
				double telProb = pset[j][i].BinClassProbability;
				if( telProb == 0 ) telProb =  feebleProbability;
				teller = teller * telProb;
				
				// noemer : indien kans is 0 dan is er geen statistiek beschikbaar - negeer de entry dan
				double noeProb = pset[j][i].BinProbability;
				if( noeProb == 0 ) {
					noeProb = 1;
			        if( modstat == MODELING_STATUS.EVALUATING ) {
			         isOK=false;	
			    	 anticipated = "Rogue value [" +  attributevalues[j] + "] model [" + j + "]";
			         do_error( "SYSTEM ERROR ! Could not find a probability for any of the classes in the model for model [" + j + "] for value [" + attributevalues[j] + "] for class [" + i + "]");
			        }
			        else
			        do_log( 1 , "Could not find a probability for any of the classes in the model for model [" + j + "] for value [" + attributevalues[j] + "] for class [" + i + "] - Guessing");
				}
				noemer = noemer * noeProb;
			}
			if( noemer == 0 ) predicted[i] = Double.MIN_VALUE;
			else predicted[i] = teller * ClassProbabilities [i] / noemer;
			// en wat met de P(A)  ??   P(A|B) = P(B|A) P(A) / P(B)  ??P(A) is de classprobability?? vermenigvuldigen
		}
		int idx=-1;
		double maxprob= (double)-1000;
		for(int i=0;i<predicted.length;i++)
		{
		  if( predicted[i] > maxprob ) {
			idx=i;
			maxprob = predicted[i];
		  }
		}
		if( idx < 0 ) {
		    do_error( "Cannot determine maximum probability");
		    return null;
		}
		
	        //  DEBUG
			if( (anticipated != null) && (DEBUG) )
			{
             String sAll = "[Anticipated=" + anticipated + "] [Predicted=" + ClassNameList[idx] + "]";
             for(int i=0;i<nclasses;i++)
			 {
			   double noemer=1;
			   double teller=1;
			   String s0="";
			   String s1="";
			   String s2="";
			   String s3="";
			   String classname = ClassNameList[i];
			   if( classname.compareToIgnoreCase("MIXED") == 0 ) continue;
			   s0 = "P(all|" + classname + ") = ";
			   for(int col=0 ; col < xa.getAttributeList().length ; col++)
			   {
				  ARFFattribute attr = xa.getAttributeList()[col];
				  if( attr.getTipe() != cmcARFF.ARFF_TYPE.NUMERIC ) continue;
				  s0 += "P(" +  attr.getAttrName() + "|" + classname + ")";
				  s1 += "[" + String.format( "% 7.5f" , attributevalues[col] ) + "]";
				  s2 += "[" + String.format( "% 7.5f" , pset[col][i].BinClassProbability) + "]";
				  s3 += "[" + String.format( "% 7.5f" , pset[col][i].BinProbability) + "]";
				  
				  teller *= pset[col][i].BinClassProbability;
				  noemer *= pset[col][i].BinProbability;
			   }
			   s2 += "   [" + String.format( "% 7.5f" , ClassProbabilities[i] ) + "]";
			   s2 += "  =>  [" + String.format( "% 16.15f" , teller ) + "]";
			   s3 += "  =>  [" + String.format( "% 16.15f" , noemer ) + "]";
			   
			   String s4 = "[" + String.format( "% 7.5f" , predicted[i] ) + "]  " + (teller * ClassProbabilities[i] /noemer) ;
			   sAll += xMSet.xU.ctEOL + "Formula " + s0;
			   sAll += xMSet.xU.ctEOL + "Input   " + s1;
			   sAll += xMSet.xU.ctEOL + "Teller  " + s2;
			   sAll += xMSet.xU.ctEOL + "Noemer  " + s3;
			   sAll += xMSet.xU.ctEOL + "Result  " + s4;
	      	 }
			 sAll += xMSet.xU.ctEOL + "==================================================";
			 do_log( 1 , sAll );
			}
		
		//
        return isOK ? ClassNameList[idx] : null;
    }
    
    //------------------------------------------------------------
    public cmcModelEvaluation makeConfusionMatrix(  cmcARFF xa  , cmcBayesModel[] models)
    //------------------------------------------------------------
    {
    	// get the class names
    	String[] ClassNameList = getClassNameList( models );
    	if( ClassNameList == null ) return null;
    	
    	// get the probabilities for each class
    	double[] classprobabilities=getClassProbabilities( ClassNameList , models);
    	if( classprobabilities == null ) return null;

    	// run a few checks and get the index of the the first numeric category in the ARFF
    	int fidx =  getIndexFirstNumericCategoryAndVerify( xa  , models);
        if( fidx < 0 ) return null;
    	
    	//
    	int nclasses = ClassNameList.length;
        int ndatarows = xa.getAttributeList()[ fidx ].getSValues().length;
        cmcModelEvaluation eval = new cmcModelEvaluation( ClassNameList );
       
		// loop through all rows
        int hits=0;
        String[][] debugShow = new String[nclasses][3]; // classname - correct - incorrect ** pick 1 row to display
        for(int i=0;i<nclasses;i++) {
        	for(int j=0;j<debugShow[0].length;j++) {
        		debugShow[i][0] = ClassNameList[i];
        		debugShow[i][1] = null;
        		debugShow[i][2] = null;
        	}
        }
        predictlist=new PredictDetail[ndatarows];
        for(int i=0;i<ndatarows;i++) predictlist[i]=null;
        for(int row=0;row<ndatarows;row++)
    	{
    	    boolean gotHit = false;
    		double[] attributevalues = new double[ xa.getAttributeList().length ];
    		String FileName=null;
    		for(int col=0 ; col < xa.getAttributeList().length ; col++ )
    		{
    			attributevalues[col] = Double.NaN;
    			if( (xa.getAttributeList()[col].getTipe() == cmcARFF.ARFF_TYPE.STRING) && (FileName==null) ) {
    				FileName = xa.getAttributeList()[col].getSValue( row );
    			}
       		    // only accept NOMINAL or NUMERIC
       		    if( xa.getAttributeList()[col].getTipe() == cmcARFF.ARFF_TYPE.CLASS ) continue;  
       		    if( xa.getAttributeList()[col].getTipe() == cmcARFF.ARFF_TYPE.STRING ) continue;  
                //    		    
     		    attributevalues[col] = xa.getAttributeList()[col].getValue( row );
    		}
    		String anticipated = xa.getAttributeList()[xa.getAttributeList().length - 1].getSValue( row );
        	String calculated = predictBayesResult(xa , models , classprobabilities , attributevalues , null );
        	if( calculated == null ) {
        		String sl = "";
        		for( int z=0;z<attributevalues.length;z++) sl += "[" + attributevalues[z] + " ]";
        		do_error( "Prediction failed [Row=" + row + "] values" + sl);
        		return null;
        	}
    	    //	
    		if( anticipated.compareToIgnoreCase( calculated ) == 0 ) { hits++; gotHit=true; }
    	    //	
    		if( eval.addToConfusion(calculated, anticipated) == false ) {
    			do_error("Could not add to confusion matrix [" + calculated + "," + anticipated + "]");
    			return null;
    		}
    		//
    		predictlist[row] = new PredictDetail( FileName , anticipated , calculated );
    		//  debug ??
    		boolean redoForDebug=false;
    		for(int k=0;k<nclasses;k++)
    		{
    			if( debugShow[k][0].compareToIgnoreCase( anticipated ) != 0 ) continue;
    			if( gotHit ) {
    			  if( debugShow[k][1] != null ) continue;
    			  debugShow[k][1] = ""+row;
    			  redoForDebug=true;
    			}
    			else {
      			  if( debugShow[k][2] != null ) continue;
      			  debugShow[k][2] = ""+row;
      			  redoForDebug=true;
    			}
    		}
    		if( redoForDebug ) { // just rerun the calculation for the report
    		  predictBayesResult(xa , models , classprobabilities , attributevalues , anticipated + " " + FileName );
    	   }
    	}
    	
    	//double dd = (double)hits / (double)ndatarows;
        //do_log( 1 , "Hits=" + hits + " on " + ndatarows + " " + dd );
    	//
    	return eval;
    }
    
    //------------------------------------------------------------
    public PredictDetail[] getPredictionResults()
    //------------------------------------------------------------
    {
    	return predictlist;
    }
}
