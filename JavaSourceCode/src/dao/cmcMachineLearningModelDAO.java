package dao;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.util.StringTokenizer;

import cbrTekStraktorModel.cmcProcSettings;
import generalMachineLearning.cmcMachineLearningEnums;
import generalMachineLearning.ARFF.ARFFCategoryCoreDTO;
import generalMachineLearning.ARFF.cmcARFF;
import generalMachineLearning.Bayes.cmcBayesModel;
import generalMachineLearning.DecisionTree.DecisionTreeDTO;
import generalMachineLearning.Evaluation.cmcModelEvaluation;
import generalMachineLearning.KNN.cmcKNearestNeighboursDTO;
import generalMachineLearning.LinearAlgebra.cmcMatrix;
import generalMachineLearning.LogisticRegression.cmcLogisticRegressionDTO;
import generalMachineLearning.SupportVectorMachine.cmcSMODTO;
import generalpurpose.gpPrintStream;
import logger.logLiason;

public class cmcMachineLearningModelDAO {

	cmcProcSettings xMSet=null;
	logLiason logger=null;
	private String LastErrorMsg=null;
	
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

    public String getLasterrorMsg()
    {
    	return LastErrorMsg;
    }
    
    //------------------------------------------------------------
    public cmcMachineLearningModelDAO(cmcProcSettings is,logLiason ilog)
    //------------------------------------------------------------
    {
    	xMSet = is;
		logger=ilog;
    }
    
    //------------------------------------------------------------
    private double[] calculateClassProbabilities(cmcBayesModel[] models)
    //------------------------------------------------------------
    {
       if( models == null ) return null;
       if( models.length < 1 ) return null;
       int idx =-1;
       for(int z=0;z<models.length;z++)
       {
    	   cmcBayesModel model = models[ z ];
    	   if( model == null ) continue;
    	   if( model.ClassNameList == null ) continue;
    	   idx = z;
    	   break;
       }
       if( idx < 0 ) {
    	   do_error( "Cannot find a valid model");
    	   return null;
       }
       cmcBayesModel model = models[ idx ];
       int nclasses = model.ClassNameList.length;
       double[] prob = new double[ nclasses ];
       for(int i=0 ; i<prob.length ; i++) prob[i] = 0;
       int total=0;
       for(int j=0 ; j< model.BinClassCounts.length ; j++)   //  BinClassCounts[ NBINS ] [ NCLASSES ]
	   {
   		for(int k=0;k< model.BinClassCounts[j].length;k++)
		{
				prob[k] += model.BinClassCounts[j][k];
				total += model.BinClassCounts[j][k];
		}
	   }
       for(int i=0;i<nclasses;i++)
       {
    	   prob[i]= (double)(prob[i] + 1)/ (double)(total + 1);  // laplace
       }
       return prob;
    }
    
    //------------------------------------------------------------
    public boolean insertEvaluation( cmcModelEvaluation bres , cmcModelEvaluation tres , String comment , String LongFileName )
    //------------------------------------------------------------
    {
         String TempFile = LongFileName + ".tmp.txt";
         gpPrintStream tmp = new gpPrintStream( TempFile , xMSet.getCodePageString() );
         BufferedReader reader=null;
    	 try {
			File inFile  = new File(LongFileName);  // File to read from.
	        reader = new BufferedReader(new InputStreamReader(new FileInputStream(inFile), xMSet.getCodePageString()));
	       	//
	       	String sLijn=null;
	       	while ((sLijn=reader.readLine()) != null) {
	          if( sLijn.indexOf("<Evaluation>EMPTY</Evaluation>") >= 0) {
	        	  if( comment != null ) tmp.println("<!-- Comment : " + comment + " -->");
                  //	        	  
	        	  tmp.println("<TrainedModel>");
	        	  tmp.println("<TrainedModelAcurracy>" + bres.getAccuracy() + "</TrainedModelAcurracy>" );
	        	  tmp.println("<TrainedModelEntries>" + bres.getTotalEntries() + "</TrainedModelEntries>" );
	              tmp.println("<![CDATA[");
	              tmp.println( bres.showConfusionMatrix() );      
	              tmp.println("]]>");
	        	  tmp.println("</TrainedModel>");
                  //
	        	  int ttot = bres.getTotalEntries() + tres.getTotalEntries();
	        	  double trainingspercentage = ( ttot > 0 ) ? (double)tres.getTotalEntries() / (double)(ttot)  : Double.NaN;  
	        	  tmp.println("<TestedModel>");
	        	  tmp.println("<TestedModelAcurracy>" + tres.getAccuracy() + "</TestedModelAcurracy>" );
	        	  tmp.println("<TestedModelEntries>" + tres.getTotalEntries() + "</TestedModelEntries>" );
	        	  tmp.println("<TestSetPercentage>" + (int)(trainingspercentage * 100) + "</TestSetPercentage>" );
	        	  tmp.println("<![CDATA[");
	              tmp.println( tres.showConfusionMatrix() );      
	              tmp.println("]]>");
	        	  tmp.println("</TestedModel>");
                  //
	              continue;
	          }
	          tmp.println(sLijn);
	       	}
	       	reader.close();
	       	tmp.close();
	       	//
	       	xMSet.xU.VerwijderBestand( LongFileName );
	       	xMSet.xU.copyFile( TempFile , LongFileName );
	    	xMSet.xU.VerwijderBestand( TempFile );
	    	
    	 }
    	 catch( Exception e ) {
    		 do_error( "Cannot write to [" + TempFile + "]");
    		 return false;
    	 }
         finally {
        	 try {
                reader.close();
                tmp.close();
        	 }
        	 catch( Exception e ) {
        		 do_error("Cannot close [" + TempFile + "]");
        	 }
         }
    	 return true;
    }
    
    //------------------------------------------------------------
    public boolean writeBayesModel(cmcBayesModel[] models , String LongFileName)
    //------------------------------------------------------------
    {
    	try {
    	//
		gpPrintStream aps = new gpPrintStream( LongFileName , xMSet.getCodePageString());
		aps.println(xMSet.getXMLEncodingHeaderLine());
		aps.println("<!-- Application : " + xMSet.getApplicDesc() + " -->");
		aps.println("<!-- Start : " + 	(xMSet.xU.prntStandardDateTime(System.currentTimeMillis())).toUpperCase() + " -->" );
		aps.println("<BayesModel>");
		// 
		double[] classprobabilities = calculateClassProbabilities(models);
    	//
    	String sline="";
        boolean header=false;
    	for(int i=0;i<models.length ; i++)
    	{
    		cmcBayesModel model = models[ i ];
    		if( model == null )  continue;   // if CLASS of STRING no model is created
    		
    		if( header == false ) {
    			aps.println("<BayesModelGeneral>");
    			aps.println("<ModelApplicationDomain>General</ModelApplicationDomain>");
    			aps.println("<NumberOfEntries>" +  model.getHistogramEntries() +"</NumberOfEntries>");
    	    	aps.println("<NumberOfBins>" +  model.BinClassCounts.length +"</NumberOfBins>");
    			aps.println("<NumberOfModels>" +  models.length +"</NumberOfModels>");
    			aps.println("<NumberOfClasses>" +  model.BinClassCounts[0].length +"</NumberOfClasses>");
        		//
    			sline="";
    			for(int k=0;k<model.ClassNameList.length;k++) sline += "[" + model.ClassNameList[k] + "]";
    			aps.println("<Classes>" +  sline +"</Classes>");
    			//
    			sline = "";
    			if( classprobabilities != null ) {
    				for(int z=0;z<classprobabilities.length;z++) sline += "[" + classprobabilities[z] + "]";
    			}
    			aps.println("<ClassProbabilities>" +  sline +"</ClassProbabilities>");
    			aps.println("</BayesModelGeneral>");
    			//
    			aps.println("<BayesModelEvaluation>");
    			aps.println("<Evaluation>EMPTY</Evaluation>");
    			aps.println("</BayesModelEvaluation>");

    			header=true;
    		}
            //    		
    		aps.println("<Model>");
    		aps.println("<ModelIndex>" + i + "</ModelIndex>");
    		aps.println("<CategoryName>" + model.categoryName + "</CategoryName>");
    		aps.println("<CategoryType>" + model.tipe + "</CategoryType>");
        	if( (model.tipe == cmcARFF.ARFF_TYPE.NOMINAL) || (model.tipe == cmcARFF.ARFF_TYPE.CLASS) ) {
        		if( model.NominalNameList == null ) {
        			do_error("Nominal value list is null for [" + model.categoryName + "]");
        			return false;
        		}
        		if( model.NominalNameList.length < 1 ) {
        			do_error("Nominal value list is empty for [" + model.categoryName + "]");
        			return false;
        		}
        		String sv="";
        		for(int z=0;z<model.NominalNameList.length;z++) sv += "[" + model.NominalNameList[z] + "]";
        		aps.println("<NominalValues>" + sv + "</NominalValues>");
            }
            //
    		sline="";
    		for(int j=0 ; j< model.binsegmentbegins.length ; j++ ) sline += "[" + model.binsegmentbegins[j] + "]";
    		aps.println("<BinSegmentBegins>" + sline + "</BinSegmentBegins>");
    		//
    		sline ="";
    		for(int j=0 ; j< model.ClassCounts.length ; j++) sline += "[" + model.ClassCounts[j] + "]";
    		aps.println("<ClassCounts>" + sline + "</ClassCounts>");
    		//
       		aps.println("<BABinStats>");
       		aps.println("<BACountsPerBin>");
       		for(int j=0;j<model.BinCounts.length;j++)
       		{
       		  aps.println("<BACountPerBin>" + model.BinCounts[j] + "</BACountPerBin>");
            }
       		aps.println("</BACountsPerBin>");
            aps.println("<BAProbabilitiesPerBin>");
    		for(int j=0;j<model.BinCounts.length;j++)
       		{
       		  aps.println("<BAProbabilityPerBin>" + model.BinProbs[j] + "</BAProbabilityPerBin>");
            }
    		aps.println("</BAProbabilitiesPerBin>");
    		aps.println("</BABinStats>");
        	//
       		aps.println("<BABinClassStats>");
            aps.println("<BAClassCountsPerBin>");
    		for(int j=0 ; j< model.BinClassCounts.length ; j++)
    		{
        		sline = "";
    			for(int k=0;k< model.BinClassCounts[j].length;k++)
    			{
    				sline += "[" + model.BinClassCounts[j][k] +"]";
    			}
    			aps.println("<BAClassCountPerBin>" + sline + "</BAClassCountPerBin>" );
    		}
    		aps.println("</BAClassCountsPerBin>");
            //
    		aps.println("<BAClassProbabilitiesPerBin>");
    		for(int j=0 ; j< model.BinClassProbs.length ; j++)
    		{
    			sline = "";
    			for(int k=0;k< model.BinClassProbs[j].length;k++)
    			{
    				sline += "[" + model.BinClassProbs[j][k] +"]";
    			}
    			aps.println("<BAClassProbabilityPerBin>" + sline + "</BAClassProbabilityPerBin>" );
    		}
    		aps.println("</BAClassProbabilitiesPerBin>");
    		aps.println("</BABinClassStats>");
            //    		
    		aps.println("</Model>");
        	
    	}
    	
    	aps.println("</BayesModel>");
    	aps.close();
    	return true;
    	}
    	catch(Exception e ) {
    		do_error( "Could not write [" + LongFileName + "]" + e.getMessage());
    		e.printStackTrace();
    		return false;
    	}
    }
    
    //------------------------------------------------------------
    private String[] getSquaredItems( String sin )
    //------------------------------------------------------------
    {
    	 //do_error( sin );
    	 if( sin == null ) return null;
    	 int nentries = xMSet.xU.TelDelims( sin , '[' );
    	 if( nentries < 0 ) return null;
    	 try {
    	   String[] shold = new String[ nentries ];
    	   StringTokenizer sx = new StringTokenizer(sin,"[");
		   int pdx=-1;
    	   while(sx.hasMoreTokens()) {
    		    String claname = sx.nextToken().trim().toUpperCase();
    		    int zz = claname.indexOf("]");
    		    if( zz < 0 ) continue;
    		    claname = claname.substring(0,zz);
    		    pdx++;
    	    	shold[ pdx ] = claname; 
    	   }
    	   if( (pdx + 1)  != shold.length ) {
    		 do_error("tokenizer did not bring back all entries" + nentries);
    		 return null;
    	   }
    	   return shold;
    	 }
    	 catch( Exception e ) {
    		 do_error("tokenizer did not work [" + sin + "]");
    		 return null;
    	 }
    	 
    }
    
    //------------------------------------------------------------
    public cmcBayesModel[] readBayesModel(String LongFileName)
    //------------------------------------------------------------
    {
         if( xMSet.xU.IsBestand(LongFileName) == false ) {
        	 do_error("Cannot lcoate model file [" + LongFileName + "]");
        	 return null;
         }
         //
         cmcBayesModel[] models = null;
         //
         int NumberOfBins=-1;
         int NumberOfClasses = -1;
         int NumberOfModels = -1;
         String[] classlist = null;
         //double[] TempClassProbs=null;
         int idx=-1;
         int countperclass=-1;
         int probaperclass=-1;
         BufferedReader reader=null;
     	 try {
 			File inFile  = new File(LongFileName);  // File to read from.
 	        reader = new BufferedReader(new InputStreamReader(new FileInputStream(inFile), xMSet.getCodePageString()));
 	       	//
 	       	String sLijn=null;
 	       	while ((sLijn=reader.readLine()) != null) {
 	       		
 	          if( sLijn.indexOf("<NumberOfBins>") >= 0 ) {
				NumberOfBins = xMSet.xU.NaarInt( xMSet.xU.extractXMLValue(sLijn,"NumberOfBins"));
				continue;	 
			  }
 	          if( sLijn.indexOf("<NumberOfClasses>") >= 0 ) {
 				NumberOfClasses = xMSet.xU.NaarInt(xMSet.xU.extractXMLValue(sLijn,"NumberOfClasses"));
 				continue;	 
 			  }
 	          if( sLijn.indexOf("<NumberOfModels>") >= 0 ) {
  				NumberOfModels = xMSet.xU.NaarInt(xMSet.xU.extractXMLValue(sLijn,"NumberOfModels"));
  				continue;	 
  			  }
 	          if( sLijn.indexOf("<Classes>") >= 0 ) {
 	        	 classlist = getSquaredItems(  xMSet.xU.extractXMLValue(sLijn,"Classes") );
 	        	 continue;
 	          }
 	          if( sLijn.indexOf("</BayesModelGeneral>") >= 0 ) {  // make model array
 	              if( (NumberOfBins < 0) || (NumberOfClasses< 0) || (NumberOfModels<0) || (classlist == null) ) {
 	            	 do_error("Cannot initialize modellist ");
 	            	 return null;
 	              }
 	              models = new cmcBayesModel[ NumberOfModels ];
 	              do_log( 5 , "Reading BayesModel comprised of [" + NumberOfBins + "] bins [" + NumberOfClasses + "] Classes and [" + NumberOfModels + "] models" );
 	              continue;
 	          }
 	          //
 	          if( models == null ) continue;  // safety
 	          //
 	          if( sLijn.indexOf("<ModelIndex>") >= 0 ) {
 				idx = xMSet.xU.NaarInt( xMSet.xU.extractXMLValue(sLijn,"ModelIndex"));
 				if( (idx < 0) || (idx >= NumberOfModels) ) {
 					do_error("ModelIndex out of bounds " + idx);
 					continue;
 				}
 				models[idx] = new cmcBayesModel( NumberOfBins , NumberOfClasses , "CATEGORYNAME-UNKNOWN" , cmcARFF.ARFF_TYPE.UNKNOWN );
 				models[idx].ClassNameList = classlist;
 				countperclass=-1;
 	            probaperclass=-1;
 				continue;
 			  }
 	          //
 	          if( sLijn.indexOf("<CategoryName>") >= 0 ) {
 	        	 models[idx].categoryName = xMSet.xU.extractXMLValue(sLijn,"CategoryName");
 	        	 continue;
 	          }
 	          //
 	          if( sLijn.indexOf("<CategoryType>") >= 0 ) {
 	        	 String sTipe = xMSet.xU.extractXMLValue(sLijn,"CategoryType");
 	        	 if( sTipe.compareToIgnoreCase("NUMERIC") == 0 ) models[idx].tipe = cmcARFF.ARFF_TYPE.NUMERIC;
 	        	 else
 	        	 if( sTipe.compareToIgnoreCase("NOMINAL") == 0 ) models[idx].tipe = cmcARFF.ARFF_TYPE.NOMINAL;
 	        	 else
 	        	 if( sTipe.compareToIgnoreCase("CLASS") == 0 ) models[idx].tipe = cmcARFF.ARFF_TYPE.CLASS;
 	        	 else {
 	        		 do_error( "There is an invalid type [" + sTipe + "] on [" + LongFileName + "]");
 	        		 return null;
 	        	 }
 	        	 continue;
  	          }
 	          if( sLijn.indexOf("<NominalValues>") >= 0 ) {
 	        	 String sNomi = xMSet.xU.extractXMLValue(sLijn,"NominalValues");
 	        	 models[idx].NominalNameList = getSquaredItems( sNomi );
 	        	 if( models[idx].NominalNameList == null ) {
 	        		 do_error("Could not extract list from [" + sNomi + "]");
 	        		 return null;
 	        	 }
 	        	 if( models[idx].NominalNameList.length < 1 ) {
 	        		 do_error("Nominal list is empty [" + sNomi + "]");
 	        		 return null;
 	        	 }
   	 //for(int z=0;z<models[idx].NominalNameList.length;z++) do_log( 1 , " -> " + models[idx].NominalNameList[z]);
 	          }
  	          //
 	          if( sLijn.indexOf("<BinSegmentBegins>") >= 0 ) {
 	        	 String[] lst = getSquaredItems( xMSet.xU.extractXMLValue(sLijn,"BinSegmentBegins") );
 	        	 if( lst == null ) continue;
 	        	 if( lst.length != NumberOfBins ) {
 	        		 do_error( "->" + sLijn );
 	        		 do_error( "Number of bin segment begins [" + lst.length + "] differs from [" + NumberOfBins + "] " );
 	        		 return null;
 	        	 }
 	        	 for(int z=0;z<lst.length ; z++ ) {
 	        		 models[idx].binsegmentbegins[z] = xMSet.xU.NaarDoubleNAN( lst[z] );
 	        		 if( Double.isNaN(models[idx].binsegmentbegins[z])) {
 	        			 do_error( "Error in beginbinsegements " + lst[z] );
 	        		 }
 	        		 //do_log(1,""+models[idx].binsegmentbegins[z]);
 	        	 }
 	        	 continue;
 	          }
 	          //
 	          if( sLijn.indexOf("<BAClassCountPerBin>") >= 0 ) {
 	        	 countperclass++;
 	        	 if( countperclass >= NumberOfBins ) {
 	        		 do_error("Too many Class Counts per Bin");
 	        		 return null;
 	        	 }
 	        	 String[] lst = getSquaredItems( xMSet.xU.extractXMLValue(sLijn,"BAClassCountPerBin") );
 	        	 if( lst == null ) continue;
 	        	 if( lst.length != NumberOfClasses ) {
 	        		 do_error( "Number of classcounts [" + lst.length + "] differs from [" + NumberOfClasses + "]" + sLijn);
 	        		 return null;
 	        	 }
 	        	 for(int z=0;z<lst.length ; z++ ) {
	        		 models[idx].BinClassCounts[ countperclass][ z ] = xMSet.xU.NaarInt( lst[z] );
	        		 if( models[idx].BinClassCounts[ countperclass ][ z ]  < 0 ) {
	        			 do_error("could not extract Class Counts pet Bin from [" + lst[z] +"] on [" + sLijn + "]");
	        			 return null;
	        		 }
	        		 //do_log(1,"cccc"+models[idx].BAConditionalCounts[ countperclass][ z ]);
	        	 }
 	          }
 	          // 
 	         
 	       	} // while
 	       	
 	       	
     	 }
     	 catch (Exception e) {
     		do_error("Could not read model file [" + LongFileName + "]" + e.getMessage() );
     		e.printStackTrace();
     		return null;
     	 }
     	 finally {
     		try {
     		reader.close();
     		}
     		catch(Exception e ) {
     			do_error("Could not close ARFF [" + LongFileName + "]");
         		return null;
     		}
     	 }
         
     	 // run a few checks on the model extracted from XML
     	 int nonempty=0;
     	 for(int z=0 ; z<models.length ; z++)
     	 {
     		 if( models[z] == null ) continue;
     		 nonempty++;
     		 cmcBayesModel mo = models[z];
     		 //
     	     if( mo.performAggregate() == false ) {
     	    	 do_error("Could not perform aggregate on model [" + mo.categoryName + "]");
     	    	 return null;
     	     }
     		 //
     		 if( mo.categoryName == null ) { do_error("Categoryname not corectly loaded"); return null; }
     		 if( mo.categoryName.trim().length() == 0 ) { do_error("Categoryname is empty"); return null; }
     		 for(int i=0;i<NumberOfClasses;i++)
 		     {
 			  if( mo.ClassCounts[i] == Integer.MIN_VALUE ) { do_error("Classcounts not corectly loaded"); return null; }
 			  if( Double.isNaN( mo.binsegmentbegins[i] )) { do_error("binsegementsbegins not corectly loaded"); return null; }
 			  if( Double.isNaN(mo.ClassProbs[i]) )  { do_error("ClassProbs not corectly loaded - NaN values found"); return null; }
 			  if( mo.ClassNameList[ i ] == null ) { do_error("Classes not corectly loaded"); return null; }
 			  if( mo.ClassNameList[ i ].trim().length() ==  0) { do_error("Class name is empty"); return null; }
 		   }
 		   for(int i=0;i<NumberOfBins;i++)
 		   {
 			   for(int j=0;j<NumberOfClasses;j++) 
 			   {
 				   if( mo.BinClassCounts[i][j] == Integer.MIN_VALUE ) { do_error("BinClassCounts not corectly loaded"); return null; };
 				   if( Double.isNaN(mo.BinClassProbs[i][j])) { do_error("BinClassProabilities not corectly loaded"); return null; };
 			   }
 		   }
     		 
     	 }
     	 if( nonempty == 0 ) {
     		 do_error( "Could not extract a single model from [" + LongFileName + "]");
     		 return null;
     	 }
     	 
     	 do_log(1,"Bayes models loaded [" + LongFileName + "] [#" + models.length + "]" );
         return models;
    }
    
    //------------------------------------------------------------
    public boolean writeDecisionTreeModel( DecisionTreeDTO[] model , String LongFileName )
    //------------------------------------------------------------
    {
    	if( model == null ) { do_error("Null Decision Tree model"); return false; }
    	if( model.length <= 0 ) { do_error("Empty Decision Tree model"); return false; }

    	//
    	DecisionTreeDTO root = model[0];
    	if( root.treetipe != cmcMachineLearningEnums.DecisionTreeType.ROOT ) {
    		do_error("First branch is not a ROOT");
    		return false;
    	}
    	
    	// fetch general specs
    	int nbranches = 0;
    	int coinflips=0;
    	int maxlevel=-1;
    	for(int i=0;i<model.length;i++)
    	{
    		DecisionTreeDTO bra = model[i];
    		if( bra == null ) continue;
    		nbranches++;
    		if( bra.FlippedACoin ) coinflips++;
    		if( bra.level > maxlevel ) maxlevel = bra.level;
    	}
        if( nbranches <= 0 ) { do_error("Decision Tree model is void"); return false; }
        	
        String[] ClassNameList = null;
        for(int i=0;i<root.categories.length;i++)
        {
        	ARFFCategoryCoreDTO sub = root.categories[i];
        	if( sub == null ) continue;
        	if( sub.getTipe() == cmcARFF.ARFF_TYPE.CLASS ) {
        		ClassNameList = sub.getNominalValueList();
        	}
        }
        if( ClassNameList == null ) {
        	do_error("Could not fetch classname list");
        	return false;
        }
        if( ClassNameList.length <= 0 ) {
        	do_error("Classname list is empty");
        	return false;
        }
        
        gpPrintStream aps = null;
        try {
         String sline="";
		 aps = new gpPrintStream( LongFileName , xMSet.getCodePageString());
		 aps.println(xMSet.getXMLEncodingHeaderLine());
		 aps.println("<!-- Application : " + xMSet.getApplicDesc() + " -->");
		 aps.println("<!-- Start : " + 	(xMSet.xU.prntStandardDateTime(System.currentTimeMillis())).toUpperCase() + " -->" );
		 aps.println("<DecisionTreeModel>");
         //
 		 aps.println("<DecisionTreeModelGeneral>");
 		 aps.println("<ModelApplicationDomain>General</ModelApplicationDomain>");
		 aps.println("<NumberOfNodes>" +  nbranches +"</NumberOfNodes>");
		 aps.println("<NumberOfLevels>" +  maxlevel +"</NumberOfLevels>");
		 aps.println("<NumberOfCategories>" +  root.categories.length +"</NumberOfCategories>");
		 aps.println("<NumberOfClasses>" +  ClassNameList.length +"</NumberOfClasses>");
		 aps.println("<NumberOfCoinFlips>" +  coinflips +"</NumberOfCoinFlips>");
		 if( coinflips > 0) 
	      aps.println("<ModelCaution>" +  "There are coinflips required. Please verify whether there are duplicates in the ARFF" +"</ModelCaution>");
    	 sline="";
		 for(int k=0;k<ClassNameList.length;k++) sline += "[" + ClassNameList[k] + "]";
		 aps.println("<Classes>" +  sline +"</Classes>");
		 //
		 aps.println("</DecisionTreeModelGeneral>");
		 //
		 aps.println("<DecisionTreeModelEvaluation>");
		 aps.println("<Evaluation>EMPTY</Evaluation>");
		 aps.println("</DecisionTreeModelEvaluation>");
		 //
		 aps.println("<DecisionTreeModelDisplay>");
	     aps.println("<![CDATA[");
		 treeWalk(model , aps );
		 aps.println("]]>");
		 aps.println("</DecisionTreeModelDisplay>");
         //
		 if( dumpARFFCategoryCoreDTOList( root.categories , aps ) == false ) return false;
		 //
		 aps.println("<Model>");
    	 for(int i=0;i<model.length;i++)
    	 {
    		DecisionTreeDTO bra = model[i];
    		if( bra == null ) continue;
 		    aps.println("<Branch>");
 		    aps.println("<BranchUID>" + bra.UID + "</BranchUID>");
 		    aps.println("<ParentBranchUID>" + bra.parentUID + "</ParentBranchUID>");
 		    aps.println("<BranchLevel>" + bra.level + "</BranchLevel>");
 		    aps.println("<BranchOrientation>" + bra.orientation + "</BranchOrientation>");
 		    aps.println("<BranchType>" + bra.treetipe + "</BranchType>");
 		    if( (bra.treetipe == cmcMachineLearningEnums.DecisionTreeType.ROOT) || (bra.treetipe == cmcMachineLearningEnums.DecisionTreeType.BRANCH) ) {
 		    	aps.println("<SplitOnCategory>" + bra.splitOnCategoryName + "</SplitOnCategory>");
 		   	    aps.println("<SplitOnType>" + bra.splitOnARFFTipe + "</SplitOnType>");
  		 		aps.println("<SplitOnValue>" + bra.splitOnValue + "</SplitOnValue>");
  		 		if( bra.splitOnARFFTipe == cmcARFF.ARFF_TYPE.NOMINAL ) {
  		 			if( bra.splitOnNominalName == null ) {
  		 				do_error("SplitonNominal is null");
  		 				return false;
  		 			}
  		 			aps.println("<SplitOnNominalValue>" + bra.splitOnNominalName + "</SplitOnNominalValue>");
  		 		}
  		 		if(  bra.FlippedACoin ) aps.println("<FlipACoin>" + bra.FlippedACoin + "</FlipACoin>");
  		    }
 		    else
 		    if( bra.treetipe == cmcMachineLearningEnums.DecisionTreeType.LEAF ) {
 		    	if( bra.LeafClassName == null ) {
 		    		do_error("leaf class name is empty");
 		    		return false;
 		    	}
 		    	aps.println("<LeafClassName>" + bra.LeafClassName + "</LeafClassName>");
 		 		 
		    }
 		    else {
 		    	do_error("Unsupported type " + bra.treetipe );
 		    	return false;
 		    }
 		    aps.println("<BranchNbrOfRows>" + bra.NumberOfRows + "</BranchNbrOfRows>");
 		    aps.println("<BranchEntropy>" + bra.TotalEntropy + "</BranchEntropy>");
		    aps.println("</Branch>");
 	    }
		 aps.println("</Model>");
		 //
    	 aps.println("</DecisionTreeModel>");
         aps.close();
         do_log( 1 , "Decision Tree Model written to [" + LongFileName + "]");
         return true;
        }
        catch(Exception e ) {
        	do_error("Writing [" + LongFileName + "]" + xMSet.xU.LogStackTrace(e));
        	return false;
        }
        finally {
        	if( aps != null ) aps.close();
        }
    }
    
    //------------------------------------------------------------
    public boolean treeWalk(DecisionTreeDTO[] model , gpPrintStream aps)
    //------------------------------------------------------------
    {
    	if( model == null ) { do_error("Null decision tree"); return false; }
    	if( model.length <= 0 ) { do_error("Empty decision tree"); return false; }
    	nodeWalk( model , model[0].UID , aps );
    	return true;
    }
    //------------------------------------------------------------
    private boolean nodeWalk( DecisionTreeDTO[] model , long uid ,  gpPrintStream aps)
    //------------------------------------------------------------
    {
    	int idx = getNodeIndexViaUID( model , uid );
    	if( idx < 0 ) return true;
    	DecisionTreeDTO curnode = model[ idx ];
    	if( curnode == null ) return true;
        String sl = "UID=" + curnode.UID;	
    	if( curnode.treetipe == cmcMachineLearningEnums.DecisionTreeType.LEAF ) {
    		sl = "LEAF=" + curnode.LeafClassName + " " + sl;
    		sl += " PUID=" + curnode.parentUID + " #=" + curnode.NumberOfRows; //.getNumberOfRows();
    		sl += " " + curnode.orientation;
    		int pdx =  getNodeIndexViaUID( model , curnode.parentUID );
    		if( (pdx >= 0) && (pdx <model.length) ) {
    			if( model[ pdx ].FlippedACoin ) sl += " *** achieved by flipping a coin ***";
    		}
    		nodeShow( curnode.level , sl , aps);
    		return true;
    	}	
        if( curnode.splitOnARFFTipe == cmcARFF.ARFF_TYPE.NOMINAL ) sl += " Val=" + curnode.splitOnNominalName;
                                                              else sl += " Val=" + curnode.splitOnValue;
        sl += " PUID=" + curnode.parentUID + " #=" + curnode.NumberOfRows; //getNumberOfRows();
        sl += " " + curnode.categories[curnode.splitCategoryIndex].getName();
        sl += " " + curnode.orientation;
        nodeShow( curnode.level , sl , aps );
    	long[] children = getChildren( model , curnode.UID );
    	nodeWalk( model , children[0] , aps );
    	nodeWalk( model , children[1] , aps );
    	return true;
    }
    //------------------------------------------------------------
    private void nodeShow( int level , String s ,  gpPrintStream aps)
    //------------------------------------------------------------
    {
       String sl = "";
       for(int i=0;i<(level-1);i++ ) sl += "    ";
       if( level > 0 ) sl += "+--";
       sl += "[" + s + "]";
       sl = "." + sl;
       if( aps == null ) { do_log( 1 , sl ); return; }
       aps.println(sl);
    }
    //------------------------------------------------------------
    private int getNodeIndexViaUID( DecisionTreeDTO[] model , long uid )
    //------------------------------------------------------------
    {
    	for(int i=0;i<model.length;i++)
    	{
    		DecisionTreeDTO node = model[i];
        	if( node == null ) continue;
        	if( node.UID == uid ) return i;
    	}
    	return -1;
    }
    //------------------------------------------------------------
    private long[] getChildren(DecisionTreeDTO[] model , long puid)
    //------------------------------------------------------------
    {
    	long[] nchild = new long[2];
    	nchild[0] = -1;
    	nchild[1] = -1;
    	for(int i=0;i<model.length;i++)
    	{
    		DecisionTreeDTO node = model[i];
        	if( node == null ) continue;
        	if( node.parentUID != puid ) continue;
        	if( nchild[0] < 0 ) { nchild[0] = node.UID; continue; }
            nchild[1] = node.UID;
            break;
    	}
    	return nchild;   	
    }

    //------------------------------------------------------------
    private cmcARFF.ARFF_TYPE fetchARFFType(String stip)
    //------------------------------------------------------------
    {
     	  if( stip == null ) return null; 
      	  if ( stip.trim().compareToIgnoreCase("NUMERIC") == 0 ) return cmcARFF.ARFF_TYPE.NUMERIC;
          if ( stip.trim().compareToIgnoreCase("NOMINAL") == 0 ) return cmcARFF.ARFF_TYPE.NOMINAL;
          if ( stip.trim().compareToIgnoreCase("STRING") == 0 ) return cmcARFF.ARFF_TYPE.STRING;
          if ( stip.trim().compareToIgnoreCase("CLASS") == 0 ) return cmcARFF.ARFF_TYPE.CLASS;
      	  return null;
    }
    
    //------------------------------------------------------------
    public DecisionTreeDTO[] readDecisionTreeModel(String LongFileName)
    //------------------------------------------------------------
    {
    	  if( xMSet.xU.IsBestand(LongFileName) == false ) {
         	 do_error("Cannot lcoate model file [" + LongFileName + "]");
         	 return null;
          }
          //
          DecisionTreeDTO[] model = null;
          //
          int NumberOfNodes=-1;
          int NumberOfClasses=-1;
          int NumberOfCategories=-1;
          int NumberOfLevels=-1;
          int ncat=-2;
          int nnode=-2;
          ARFFCategoryCoreDTO[] categories = null;
          String[] ClassNameList=null;
          boolean inCategories=false;
          //
          BufferedReader reader=null;
      	  try {
  			File inFile  = new File(LongFileName);  // File to read from.
  	        reader = new BufferedReader(new InputStreamReader(new FileInputStream(inFile), xMSet.getCodePageString()));
  	       	//
  	       	String sLijn=null;
  	       	while ((sLijn=reader.readLine()) != null) {
  	       		
  	       	  if( sLijn.indexOf("<NumberOfNodes>") >= 0 ) {
  				NumberOfNodes = xMSet.xU.NaarInt( xMSet.xU.extractXMLValue(sLijn,"NumberOfNodes"));
  				if( NumberOfNodes <= 0 ) { do_error("Wrong number of nodes " + sLijn ); return null; }
  				continue;	 
  			  }
  	       	  if( sLijn.indexOf("<NumberOfNodes>") >= 0 ) {
  				NumberOfNodes = xMSet.xU.NaarInt( xMSet.xU.extractXMLValue(sLijn,"NumberOfNodes"));
  				if( NumberOfNodes <= 0 ) { do_error("Wrong number of nodes " + sLijn ); return null; }
  				continue;	 
  			  }

  	          if( sLijn.indexOf("<NumberOfCategories>") >= 0 ) {
  				NumberOfCategories = xMSet.xU.NaarInt( xMSet.xU.extractXMLValue(sLijn,"NumberOfCategories"));
  				if( NumberOfCategories <= 0 ) { do_error("Wrong number of categories " + sLijn ); return null; }
  				continue;	 
  			  }
  	          if( sLijn.indexOf("<NumberOfClasses>") >= 0 ) {
  				NumberOfClasses = xMSet.xU.NaarInt( xMSet.xU.extractXMLValue(sLijn,"NumberOfClasses"));
  				if( NumberOfClasses <= 0 ) { do_error("Wrong number of classes " + sLijn ); return null; }
  				continue;	 
  			  }
  	          if( sLijn.indexOf("<NumberOfLevels>") >= 0 ) {
  				NumberOfLevels = xMSet.xU.NaarInt( xMSet.xU.extractXMLValue(sLijn,"NumberOfLevels"));
  				if( NumberOfLevels <= 0 ) { do_error("Wrong number of levels " + sLijn ); return null; }
  				continue;	 
  			  }
  	          if( sLijn.indexOf("<Classes>") >= 0 ) {
  	       	    ClassNameList = getSquaredItems(  xMSet.xU.extractXMLValue(sLijn,"Classes") );
  	       	    if( ClassNameList.length != NumberOfClasses ) {
  	       	    	do_error("ClassNameList does not comprise [" + NumberOfClasses + "] items");
  	       	    	return null;
  	       	    }
  	       	    continue;
  	          }
  	          //
  	          if( sLijn.indexOf("<Categories>") >= 0 ) {
  				categories = new ARFFCategoryCoreDTO[ NumberOfCategories ];
  				ncat=-1;
  				inCategories=true;
  				continue;	 
  			  }
  	          if( inCategories ) {
 	       		if( parseCategory(  sLijn , categories ) == false ) return null;
 	       	  }
 	          if( sLijn.indexOf("</Categories>") >= 0 ) {
 	        	inCategories=false;
 	        	for(int i=0;i<categories.length;i++) 
 	        	{
 	        		if( categories[i] == null ) { do_error( "found uninitialized category"); return null; }
 	        	}
 	        	//model.setFeatures( categories );
 	          }
  	          /*
  	       	  //
  	          if( sLijn.indexOf("<CategoryName>") >= 0 ) {
  				String scat = xMSet.xU.extractXMLValue(sLijn,"CategoryName");
  				ncat++;
  				if( categories == null ) { do_error("Categories not initialized"); return null; }
  				if( ncat >= categories.length ) { do_error("Categories overrun"); return null; }
  				categories[ncat] = new ARFFCategoryCoreDTO( scat );
  				continue;	 
  			  }
  	          if( sLijn.indexOf("<CategoryType>") >= 0 ) {
  	        	cmcARFF.ARFF_TYPE tipe = fetchARFFType( xMSet.xU.extractXMLValue(sLijn,"CategoryType"));
  	        	if( tipe == null ) { do_error("cannot determine tipe" + sLijn); return null; }
  	        	if( ncat < 0 ) { do_error("Category underrun"); return null; }
  	        	categories[ncat].setTipe( tipe );
  	        	continue;	 
  			  }
  	          if( sLijn.indexOf("<NominalValueList>") >= 0 ) {
  				String[] slist = getSquaredItems(  xMSet.xU.extractXMLValue(sLijn,"NominalValueList") );
  				if( slist.length < 1 ) {
  	       	    	do_error("There is nothing on the nominal value list [" + NumberOfClasses + "] items");
  	       	    	return null;
  	       	    }
  				if( ncat < 0 ) { do_error("Category underrun"); return null; }
  				categories[ncat].setNominalValueList( slist );
  				continue;	 
  			  }
  			  */
  	          //
  	          if( sLijn.indexOf("<Model>") >= 0 ) {
  	        	if( checkCategories( categories , NumberOfCategories) == false ) return null;
  				model = new DecisionTreeDTO[ NumberOfNodes ];
  				nnode=-1;
  				continue;	 
  			  }
  	          if( sLijn.indexOf("<BranchUID>") >= 0 ) {
  				long uid= xMSet.xU.NaarLong( xMSet.xU.extractXMLValue(sLijn,"BranchUID") );
  				if( uid < 0L ) { do_error("Incorrect UID on " + sLijn ); return null; }
  				nnode++;
  				if( (nnode<0) || (nnode > model.length)) { do_error("Node overrun"); return  null; }
  				model[ nnode ] = new DecisionTreeDTO();
  				model[ nnode ].UID = uid;
  				model[ nnode ].ClassNameList = ClassNameList;
  				model[ nnode ].categories = categories;
  	          }
  	          if( sLijn.indexOf("<ParentBranchUID>") >= 0 ) {
  				long puid= xMSet.xU.NaarLong( xMSet.xU.extractXMLValue(sLijn,"ParentBranchUID") );
  				//if( puid < 0L ) { do_error("Incorrect UID on " + sLijn ); return null; }
  				if( (nnode<0) || (nnode > model.length)) { do_error("Node overrun"); return  null; }
  				model[ nnode ].parentUID = puid;
  	          } 
  	          if( sLijn.indexOf("<BranchLevel>") >= 0 ) {
  				int levl= xMSet.xU.NaarInt( xMSet.xU.extractXMLValue(sLijn,"BranchLevel") );
  				if( levl < 0 ) { do_error("Incorrect level on " + sLijn ); return null; }
  				if( (nnode<0) || (nnode > model.length)) { do_error("Node overrun"); return  null; }
  				model[ nnode ].level = levl;
  	          }
  	          if( sLijn.indexOf("<BranchType>") >= 0 ) {
  	        	String stip = xMSet.xU.extractXMLValue(sLijn,"BranchType");
  	        	if( stip == null ) { do_error("branchtype is null"); return null; }
  				if( (nnode<0) || (nnode > model.length)) { do_error("Node overrun"); return  null; }
  				if( stip.compareToIgnoreCase("ROOT") == 0 ) model[ nnode ].treetipe = cmcMachineLearningEnums.DecisionTreeType.ROOT;
  				else
  				if( stip.compareToIgnoreCase("BRANCH") == 0 ) model[ nnode ].treetipe = cmcMachineLearningEnums.DecisionTreeType.BRANCH;
  				else
  				if( stip.compareToIgnoreCase("LEAF") == 0 ) model[ nnode ].treetipe = cmcMachineLearningEnums.DecisionTreeType.LEAF;
  				else {
  					do_error("Unsupported type " + sLijn );
  					return null;
  				}
  		      }
  	          if( sLijn.indexOf("<BranchOrientation>") >= 0 ) {
  	        	String stip = xMSet.xU.extractXMLValue(sLijn,"BranchOrientation");
  	        	if( stip == null ) { do_error("branchorientation is null"); return null; }
  				if( (nnode<0) || (nnode > model.length)) { do_error("Node overrun"); return  null; }
  				if( stip.compareToIgnoreCase("LEFT") == 0 ) model[ nnode ].orientation = cmcMachineLearningEnums.BranchOrientation.LEFT;
  				else
  				if( stip.compareToIgnoreCase("RIGHT") == 0 ) model[ nnode ].orientation = cmcMachineLearningEnums.BranchOrientation.RIGHT;
  				else
  				if( stip.compareToIgnoreCase("UNKNOWN") == 0 ) model[ nnode ].orientation = cmcMachineLearningEnums.BranchOrientation.UNKNOWN;
  				else {
  					do_error("Unsupported orientation " + sLijn );
  					return null;
  				}
  	          }
  	          if( sLijn.indexOf("<SplitOnCategory>") >= 0 ) {
  	        	String stip = xMSet.xU.extractXMLValue(sLijn,"SplitOnCategory");
  	        	if( stip == null ) { do_error("split category is null"); return null; }
  				if( (nnode<0) || (nnode > model.length)) { do_error("Node overrun"); return  null; }
  				model[ nnode ].splitOnCategoryName = stip.trim();
  		      }
  	          if( sLijn.indexOf("<SplitOnType>") >= 0 ) {
  	        	String stip = xMSet.xU.extractXMLValue(sLijn,"SplitOnType");
  	        	if( stip == null ) { do_error("splitontype is null"); return null; }
  				if( (nnode<0) || (nnode > model.length)) { do_error("Node overrun"); return  null; }
  				if( stip.compareToIgnoreCase("NUMERIC") == 0 ) model[ nnode ].splitOnARFFTipe = cmcARFF.ARFF_TYPE.NUMERIC;
  				else
  				if( stip.compareToIgnoreCase("NOMINAL") == 0 ) model[ nnode ].splitOnARFFTipe = cmcARFF.ARFF_TYPE.NOMINAL;
  	  			else {
  					do_error("Unsupported split on type " + sLijn );
  					return null;
  				}
  	          }
  	          if( sLijn.indexOf("<SplitOnValue>") >= 0 ) {
  				double dd = xMSet.xU.NaarDoubleNAN( xMSet.xU.extractXMLValue(sLijn,"SplitOnValue") );
  				if( Double.isNaN(dd) ) { do_error("Incorrect splitonvalue " + sLijn ); return null; }
  				if( (nnode<0) || (nnode > model.length)) { do_error("Node overrun"); return  null; }
  				model[ nnode ].splitOnValue = dd;
  	          }
  	          if( sLijn.indexOf("<SplitOnNominalValue>") >= 0 ) {
  	        	String stip = xMSet.xU.extractXMLValue(sLijn,"SplitOnNominalValue");
  	        	if( stip == null ) { do_error("split on nominal is null"); return null; }
  				if( (nnode<0) || (nnode > model.length)) { do_error("Node overrun"); return  null; }
  				model[ nnode ].splitOnNominalName = stip.trim();
  	          }
  	          if( sLijn.indexOf("<FlipACoin>") >= 0 ) {
  	        	String stip = xMSet.xU.extractXMLValue(sLijn,"FlipACoin");
  	        	if( stip == null ) { do_error("flip a coin"); return null; }
  				if( (nnode<0) || (nnode > model.length)) { do_error("Node overrun"); return  null; }
  				if( stip.compareTo("TRUE") == 0 ) model[ nnode ].FlippedACoin = true;
  				                             else model[ nnode ].FlippedACoin = false;
  			  }
  	          if( sLijn.indexOf("<LeafClassName>") >= 0 ) {
  	        	String stip = xMSet.xU.extractXMLValue(sLijn,"LeafClassName");
  	        	if( stip == null ) { do_error("split on nominal is null"); return null; }
  				if( (nnode<0) || (nnode > model.length)) { do_error("Node overrun"); return  null; }
  				model[ nnode ].LeafClassName = stip.trim();
  	          }
  	          if( sLijn.indexOf("<BranchNbrOfRows>") >= 0 ) {
  				int levl= xMSet.xU.NaarInt( xMSet.xU.extractXMLValue(sLijn,"BranchNbrOfRows") );
  				if( levl < 0 ) { do_error("Incorrect level on " + sLijn ); return null; }
  				if( (nnode<0) || (nnode > model.length)) { do_error("Node overrun"); return  null; }
  				model[ nnode ].NumberOfRows = levl;
  	          }
  	          if( sLijn.indexOf("<BranchEntropy>") >= 0 ) {
  				double dd = xMSet.xU.NaarDoubleNAN( xMSet.xU.extractXMLValue(sLijn,"BranchEntropy") );
  				if( Double.isNaN(dd) ) { do_error("Incorrect splitonvalue " + sLijn ); return null; }
  				if( (nnode<0) || (nnode > model.length)) { do_error("Node overrun"); return  null; }
  				model[ nnode ].TotalEntropy = dd;
  	          }
  	          
  	       	} // while
      	  }
      	  catch(Exception e ) {
      		  do_error( "Reading [" + LongFileName + "] " + xMSet.xU.LogStackTrace(e));
      		  return null;
      	  }
      	  finally {
      		  try {
      			  reader.close();
      		  }
      		  catch(Exception e ) {
      			 do_error( "closing [" + LongFileName + "] " + xMSet.xU.LogStackTrace(e));
         		  return null;
      		  }
      	  }
      	  // a few checks
      	  int maxlevel=-10000;
      	  for(int i=0;i<model.length;i++)
      	  {
      		  if( model[i].level > maxlevel ) maxlevel = model[i].level;
      	  }
      	  if( NumberOfLevels != maxlevel ) { do_error("Max level not correct"); return null; }
      	  if( NumberOfNodes != (nnode+1) ) { do_error("Number of nodes not correct [" + NumberOfNodes + "] [" + nnode + "]"); return null; }
      	  //
      	  if( checkDecisionTreeModel( model ) == false ) return null;
      	  //
      	  do_log( 1 , "Decision Tree model correctly loaded [" + LongFileName + "] [#Nodes=" + model.length + "]");
      	  return model;
    }
    
    //------------------------------------------------------------
    private boolean checkCategories( ARFFCategoryCoreDTO[] categories , int ncat)
    //------------------------------------------------------------
    {
    	if( categories == null ) { do_error("NULL categories"); return false; }
    	if( categories.length <= 0 ) { do_error("EMPTY categories"); return false; }
    	if( categories.length != ncat ) { do_error("Number of categories does not match"); return false; }
        //	
    	for(int i=0;i<categories.length;i++)
    	{
    		ARFFCategoryCoreDTO cat = categories[i];
    		if( cat == null ) { do_error("There is an empty category"); return false; }
    		if( (cat.getTipe() == cmcARFF.ARFF_TYPE.CLASS) || (cat.getTipe() == cmcARFF.ARFF_TYPE.NOMINAL) ) {
    			String[] list = cat.getNominalValueList();
    			if( list == null ) { do_error("NominalValue list is NULL"); return false; }
    			if( list.length < 1) { do_error("NominalValue list not populated"); return false; }
    		}
    		if( (cat.getTipe() == cmcARFF.ARFF_TYPE.NUMERIC) || (cat.getTipe() == cmcARFF.ARFF_TYPE.STRING) ) {
    			String[] list = cat.getNominalValueList();
    			if( list != null ) { do_error("NominalValue list must be null"); return false; }
    		}
    	}
        //do_log( 1 , "Categories OK " + ncat);
        return true;	
    }
    
    //------------------------------------------------------------
    private boolean checkDecisionTreeModel(DecisionTreeDTO[] model)
    //------------------------------------------------------------
    {
    	if( model == null ) { do_error("NULL model"); return false; }
    	if( model.length <= 0 ) { do_error("EMPTY model"); return false; }
    	//
    	DecisionTreeDTO root = model[0];
    	if( root == null ) { do_error("NULL root"); return false; }
    	if( root.treetipe != cmcMachineLearningEnums.DecisionTreeType.ROOT ) { do_error("First branch is not a root"); return false; }
    	if( root.ClassNameList == null ) { do_error("No classname list on root"); return false; }
    	if( root.ClassNameList.length < 1 ) { do_error("Empty classname list on root"); return false; }
    	if( root.categories == null ) { do_error("Null categories on root"); return false; }
    	if( root.categories.length < 1) { do_error("Empty categories on root"); return false; }
    	//
    	boolean ok=true;
    	for(int i=0;i<model.length;i++)
    	{
    		DecisionTreeDTO dto = model[i];
    		if( dto.treetipe == cmcMachineLearningEnums.DecisionTreeType.UNKNOWN ) { do_error("Unknown type"); ok=false; }
    		if( dto.treetipe == cmcMachineLearningEnums.DecisionTreeType.LEAF ) { 
    			if( dto.LeafClassName == null ) {
    			do_error("Leafclassname missing"); ok=false; } 
    		}
    		if( dto.orientation == cmcMachineLearningEnums.BranchOrientation.UNKNOWN ) { 
    			if( dto.treetipe != cmcMachineLearningEnums.DecisionTreeType.ROOT ) {
    			do_error("Unknown orientation"); ok=false; }
    		}
    		if( dto.splitOnARFFTipe == cmcARFF.ARFF_TYPE.NUMERIC ) {
    			if( dto.splitOnNominalName != null ) {
    				do_error("Numetic and yet a nominal list is defined"); ok=false;
    			}
    		}
    		if( dto.splitOnARFFTipe == cmcARFF.ARFF_TYPE.NOMINAL ) {
    			if( dto.splitOnNominalName == null ) {
    				do_error("Nominal spliton valu eis missing"); ok=false;
    			}
    		}
    		if( (dto.splitOnCategoryName == null) && (dto.treetipe != cmcMachineLearningEnums.DecisionTreeType.LEAF) ) { 
    			do_error("Split on category is null"); ok=false; 
    			int pdx=-1;
    			for(int z=0;z<root.categories.length;i++)
    			{
    				ARFFCategoryCoreDTO cat = root.categories[z];
    				if( cat.getName().compareToIgnoreCase( dto.splitOnNominalName ) == 0 ) {
    					pdx=z;
    					break;
    				}
    			}
    			if( pdx < 0 ) {
    				do_error("Could not detemrine category"); ok=false;
    			}
    			else {
    				dto.splitCategoryIndex = pdx;
    			}
    		}
    		// LEAF
    		if( dto.treetipe == cmcMachineLearningEnums.DecisionTreeType.LEAF ) { 
    			if( Double.isNaN(dto.TotalEntropy) ) {
    				do_error("Entropy is NAN"); ok = false;
    			}
    			if( dto.LeafClassName == null ) {
    				do_error("Leafclassname is null"); ok=false;  
      			}
    			else {
    			  // Is this a correct class
    			  boolean found = false;
    			  for(int z=0;z<root.ClassNameList.length;z++)
    			  {
    			   if( root.ClassNameList[z].compareToIgnoreCase( dto.LeafClassName ) == 0 ) { found = true; break; }
    			  }
    			  if( found == false ) {
    				do_error("Could not locate class [" + dto.LeafClassName + "]");
    				ok=false;
    			  }
    			}
    		}
    		else {
    			if( dto.LeafClassName != null ) {
    				do_error("Leafclassname is NOT null on a NON leaf"); ok=false;  
      			}
    		}
    		// PUID
    		if( dto.treetipe != cmcMachineLearningEnums.DecisionTreeType.ROOT ) {
    			if( dto.parentUID < 0 )  {
    				do_error("Parent UID is not defined");
    				ok=false;
    			}
    			else {  // PUID must be valid
    				boolean found = false;
    				for(int z=0;z<model.length;z++)
    				{
    					if( model[z].UID == dto.parentUID ) { found = true; break; }
    				}
    				if( !found ) { do_error("Parent UID does not exist"); ok=false; }
    			}
    		} 
    		if( dto.NumberOfRows < 0 ) {do_error("number of rows invalid"); ok=false; } 
    		// the sum of thr rows on the childeren must match
    		if( dto.treetipe != cmcMachineLearningEnums.DecisionTreeType.LEAF ) { 
    		  long[] children = this.getChildren(model, dto.UID );
    		  if( (children[0] < 0L) || (children[1] < 0L) ) {
    			  do_error("There are not exactly 2 childeren");
    			  ok=false;
    		  }
    		  else {
    			int leftidx = getNodeIndexViaUID( model , children[0] );
    			int rightidx = getNodeIndexViaUID( model , children[1] );
    			try {
    				int leftnbr = model[leftidx].NumberOfRows;
    				int rightnbr = model[rightidx].NumberOfRows;
    				if( dto.NumberOfRows != (leftnbr + rightnbr) ) {
    					do_error("Number of rows on left and right child does not sum up correctly " + leftnbr + " " + rightnbr +" " + dto.NumberOfRows);
    					ok=false;
    				}
                    //do_log( 1 , "->" + leftnbr + " " + rightnbr +" " + dto.NumberOfRows);
        		}
    			catch(Exception e) {
    				do_error("Cannot aggregate childeren");
    				ok=false;
    			}
    		  }
    		}
    		
    	}
    	return ok;
    }
    
    //------------------------------------------------------------
    private boolean dumpARFFCategoryCoreDTOList(ARFFCategoryCoreDTO[] flist , gpPrintStream aps)
    //------------------------------------------------------------
    {
    	aps.println("<Categories>");
		for(int i=0;i<flist.length;i++)
		{
			ARFFCategoryCoreDTO cat = flist[i];
			if( cat == null ) continue;
			aps.println("<Category>");
			aps.println("<CategoryName>" + cat.getCategoryName() + "</CategoryName>");
			aps.println("<CategoryType>" + cat.getTipe() + "</CategoryType>");
			if( (cat.getTipe() == cmcARFF.ARFF_TYPE.CLASS) || (cat.getTipe() == cmcARFF.ARFF_TYPE.NOMINAL) ) {
			  String[] nomlist = cat.getNominalValueList();
			  if( nomlist == null ) { do_error("Nominal vlaue list NULL"); return false; }
			  if( nomlist.length < 1 )  { do_error("Nominal value list EMPTY"); return false; }
			  String sn = "";
			  for(int j=0; j<nomlist.length ; j++)
			  {
				  sn += (j==0) ? nomlist[j] : "][" + nomlist[j];
			  }
			  aps.println("<NominalValueList>[" + sn + "]</NominalValueList>");
			}
			aps.println("</Category>");
		}
		aps.println("</Categories>");
		return true;
    }
    
    //------------------------------------------------------------
    public boolean writeKNN( cmcKNearestNeighboursDTO dto , String LongModelName , cmcModelEvaluation[] evalList )
    //------------------------------------------------------------
    {
     try {
    	gpPrintStream aps = new gpPrintStream( LongModelName , xMSet.getCodePageString());
		aps.println(xMSet.getXMLEncodingHeaderLine());
		aps.println("<!-- Application : " + xMSet.getApplicDesc() + " -->");
		aps.println("<!-- Start : " + 	(xMSet.xU.prntStandardDateTime(System.currentTimeMillis())).toUpperCase() + " -->" );
		aps.println("<KNNModel>");
    	
		aps.println("<KNNModelGeneral>");
		aps.println("<ModelApplicationDomain>General</ModelApplicationDomain>");
		aps.println("<ARFFFileName>" + dto.getLongARFFFileName() + "</ARFFFileName>");
		aps.println("<OptimalKValue>" + dto.getOptimalK() + "</OptimalKValue>");
		aps.println("<NbrOfSampleRows>" + dto.getNbrOfRows() + "</NbrOfSampleRows>");
		aps.println("<NbrOfFeatures>" + dto.getNbrOfFeatures() + "</NbrOfFeatures>");
		aps.println("<NbrOfSampleColumns>" + dto.getNbrOfColumnsOnSamples() + "</NbrOfSampleColumns>");
		aps.println("</KNNModelGeneral>");
		aps.println("<KNNModelEvaluation>");
		aps.println("<Evaluation>EMPTY</Evaluation>");
		aps.println("</KNNModelEvaluation>");
	    //
		if( dumpARFFCategoryCoreDTOList( dto.getFeatures() , aps ) == false ) return false;
		//
		aps.println("<OptimizationResults>");
		aps.println("<NbrOfRepartitions>" + dto.getNbrOfRePartitions() + "</NbrOfRepartitions>");
		aps.println("<NbrOfPartitions>" + dto.getNbrOfPartitions() + "</NbrOfPartitions>");
		aps.println("<MaximumK>" + dto.getMaximumK() + "</MaximumK>");
		aps.println("<ElapsedTimeMSec>" + dto.getElapsedTime() + "</ElapsedTimeMSec>");
		if( evalList == null ) { do_error("NULL eval list"); return false; }
		for(int i=1;i<evalList.length;i++)
		{
			aps.println("<Result>");
			aps.println("<KValue>" + i + "</KValue>");
			aps.println("<Accuracy>" + evalList[i].getAccuracy() + "</Accuracy>");
			aps.println("<MeanPrecision>" + evalList[i].getMeanPrecision() + "</MeanPrecision>");
			aps.println("<MeanFMeasure>" + evalList[i].getMeanFValue() + "</MeanFMeasure>");
			aps.println("</Result>");
		}
		aps.println("</OptimizationResults>");
        //
		aps.println("<KNNModelData>");
		int nrows = dto.getNbrOfRows();
		int ncols = dto.getNbrOfColumnsOnSamples();
		int nfeat = dto.getNbrOfFeatures();
		if( ncols != (nfeat -1) ) {
			do_error( "Number of columns on samples differs from number of features-1");
			return false;
		}
		aps.println("<![CDATA[");
		for(int i=0 ; i< nrows ; i++)
		{
	       String sdat="";
	       for(int j=0;j<ncols;j++)
	       {
	    	   sdat +=  (j==0) ? ""+dto.getSamples()[i][j]  : ","+dto.getSamples()[i][j];
	       }
	       sdat += "," + dto.getResults()[i];
	       aps.println( sdat );
		}
		aps.println("]]>");
		aps.println("</KNNModelData>");
        //
		aps.println("</KNNModel>");
	 	return true;
     }
     catch(Exception e ) {
    		do_error( "Writing [" + LongModelName + "]" );
    		return false;
     }
    }
    
    //------------------------------------------------------------
    private double[] getDoubleItems( String sin )
    //------------------------------------------------------------
    {
    	 if( sin == null ) return null;
    	 int nentries = xMSet.xU.TelDelims( sin , ',' ) + 1;
    	 if( nentries < 1 ) return null;
    	 double[] list = new double[ nentries ];
    	 for(int i=0;i<nentries;i++) list[i] = Double.NaN;
    	 try {
    	   StringTokenizer sx = new StringTokenizer(sin,",");
		   int pdx=-1;
    	   while(sx.hasMoreTokens()) {
    		   pdx++;
    		   list[ pdx ]  = xMSet.xU.NaarDoubleNAN(  sx.nextToken().trim() );
    	   }
    	   if( (pdx + 1)  != nentries ) {
    		 do_error("tokenizer did not bring back all entries" + nentries);
    		 return null;
    	   }
    	   for(int i=0;i<nentries;i++) {
    		   if( Double.isNaN( list[i] ))  return null;
    	   }
    	   return list;
    	 }
    	 catch( Exception e ) {
    		 do_error("tokenizer did not work [" + sin + "]");
    		 return null;
    	 }
    	 
    }
    
    //------------------------------------------------------------
    private boolean parseCategory(  String sLijn , ARFFCategoryCoreDTO[] categories )
    //------------------------------------------------------------
    {
    	 if( categories == null ) return true;
   	 	 boolean gotit=false;
    	 if( sLijn.indexOf("<CategoryName>") >= 0 ) gotit=true;
    	 if( sLijn.indexOf("<CategoryType>") >= 0 ) gotit=true;
    	 if( sLijn.indexOf("<NominalValueList>") >= 0 ) gotit=true;
    	 if( gotit == false  ) return true;
    	 int NbrOfFeatures = categories.length;
    	 if( NbrOfFeatures <= 0 ) { do_error( "empty categories"); return false; }
    	 int lastcat = -1;
    	 for(int i=0 ; i< categories.length ; i++)
    	 {
    		 if( categories[i] != null ) { lastcat = i; }
    	 }
    	 //
       	 if( sLijn.indexOf("<CategoryName>") >= 0 ) {
       		String name = xMSet.xU.extractXMLValue(sLijn,"CategoryName");
       		lastcat++;
       		if( lastcat >= NbrOfFeatures ) { do_error("Categories overrun"); return false; }
       	    categories[lastcat] = new ARFFCategoryCoreDTO(name);
       	    return true;
       	 }
       	 //
       	 if( sLijn.indexOf("<CategoryType>") >= 0 ) {
       	    cmcARFF.ARFF_TYPE tipe = fetchARFFType( xMSet.xU.extractXMLValue(sLijn,"CategoryType") );
       	    if( tipe == null ) { do_error("cannot determine tipe" + sLijn); return false; }
       	    if( lastcat < 0 ) { do_error("Category underrun"); return false; }
       	    categories[ lastcat ].setTipe( tipe );
       	    return true;	 
       	 }
       	 if( sLijn.indexOf("<NominalValueList>") >= 0 ) {
				String[] slist = getSquaredItems(  xMSet.xU.extractXMLValue(sLijn,"NominalValueList") );
				if( slist.length < 1 ) {
	       	    	do_error("There is nothing on the nominal value list");
	       	    	return false;
	       	    }
				if( lastcat < 0 ) { do_error("Category underrun"); return false; }
				categories[lastcat].setNominalValueList( slist );
				return true;
		 }
       	 return true;
    }
    
    //------------------------------------------------------------
    public cmcKNearestNeighboursDTO readKNNModel( String ModelFileName )
    //------------------------------------------------------------
    {
    	if( xMSet.xU.IsBestand(ModelFileName) == false ) {
        	 do_error("Cannot lcoate model file [" + ModelFileName + "]");
        	 return null;
         }
         //
    	 cmcKNearestNeighboursDTO model = null;
         //
    	 String ARFFFileName=null;
    	 int Optimal=-1;
    	 int NbrOfSampleRows=-1;
    	 int NbrOfFeatures=-1;
    	 int NbrOfSampleColumns=-1;
    	 int ncat = -1;
    	 int inData = 0;
    	 int nrow=-1;
         int NumberOfClasses=-1;
         int NumberOfCategories=-1;
         ARFFCategoryCoreDTO cat=null;
         ARFFCategoryCoreDTO[] categories = null;
         String[] ClassNameList=null;
         boolean inCategories=false;
         //
         BufferedReader reader=null;
     	 try {
 			File inFile  = new File(ModelFileName);  // File to read from.
 	        reader = new BufferedReader(new InputStreamReader(new FileInputStream(inFile), xMSet.getCodePageString()));
 	       	//
 	       	String sLijn=null;
 	       	while ((sLijn=reader.readLine()) != null) {
 	       	 
 	       	  if( sLijn.trim().length() < 1 ) continue;
 	       	  // 
 	       	 if( sLijn.indexOf("<ARFFFileName>") >= 0 ) {
	  				ARFFFileName = xMSet.xU.extractXMLValue(sLijn,"ARFFFileName");
	  				continue;	 
	  		  }
 	       	  if( sLijn.indexOf("<OptimalKValue>") >= 0 ) {
 	  				Optimal = xMSet.xU.NaarInt( xMSet.xU.extractXMLValue(sLijn,"OptimalKValue"));
 	  				if( Optimal <= 0 ) { do_error("Wrong optimal value " + sLijn ); return null; }
 	  				continue;	 
 	  		  }
 	       	  if( sLijn.indexOf("<NbrOfSampleRows>") >= 0 ) {
	  				NbrOfSampleRows = xMSet.xU.NaarInt( xMSet.xU.extractXMLValue(sLijn,"NbrOfSampleRows"));
	  				if( NbrOfSampleRows <= 0 ) { do_error("Wrong Nbr of samples value " + sLijn ); return null; }
	  				continue;	 
	  		  }
 	       	  if( sLijn.indexOf("<NbrOfFeatures>") >= 0 ) {
	  				NbrOfFeatures = xMSet.xU.NaarInt( xMSet.xU.extractXMLValue(sLijn,"NbrOfFeatures"));
	  				if( NbrOfFeatures <= 0 ) { do_error("Wrong NbrOfFeatures value " + sLijn ); return null; }
	  				continue;	 
	  		  }
 	       	  if( sLijn.indexOf("<NbrOfSampleColumns>") >= 0 ) {
	  				NbrOfSampleColumns = xMSet.xU.NaarInt( xMSet.xU.extractXMLValue(sLijn,"NbrOfSampleColumns"));
	  				if( NbrOfSampleColumns <= 0 ) { do_error("Wrong NbrOfSampleColumns value " + sLijn ); return null; }
	  				continue;	 
	  		  }
 	       	  if( sLijn.indexOf("</KNNModelGeneral>") >= 0 ) { // trigger
 	       		  if( ARFFFileName == null )  { do_error("Filename missing"); return null;}
 	       		  if(  Optimal < 0)  { do_error("Optimal K not found"); return null;}
 	       		  if(  NbrOfSampleRows < 0)  { do_error("Nbr of sample rows not found"); return null;}
 	       	      if(  NbrOfFeatures < 0)  { do_error("Nbr nof features not found"); return null;}
 	       	      if(  NbrOfSampleColumns < 0)  { do_error("Nbr of columns"); return null;}
 	       	      if(  NbrOfSampleColumns != (NbrOfFeatures-1))  { do_error("Nbr of comlums does not match Nbr of features"); return null;}
 	       	      model = new cmcKNearestNeighboursDTO( Optimal , NbrOfSampleRows , NbrOfFeatures );
 	       	      //categories = new ARFFCategoryCoreDTO[ NbrOfFeatures ];
 	       	      ncat=-1; 
 	       	      continue;	 
  		     }
 	       	 if( sLijn.indexOf("<Categories>") >= 0 ) {
 	       		 inCategories = true;
  	             categories = new ARFFCategoryCoreDTO[ NbrOfFeatures ];
  	         }
 	       	 if( inCategories ) {
 	       		if( parseCategory(  sLijn , categories ) == false ) return null;
 	       	 }
 	         if( sLijn.indexOf("</Categories>") >= 0 ) {
 	        	inCategories=false;
 	        	for(int i=0;i<categories.length;i++) 
 	        	{
 	        		if( categories[i] == null ) { do_error( "found uninitialized category"); return null; }
 	        	}
 	        	model.setFeatures( categories );
 	         }
 	       	 //
 	       	  /*
 	       	 if( sLijn.indexOf("<CategoryName>") >= 0 ) {
 	       		String name = xMSet.xU.extractXMLValue(sLijn,"CategoryName");
 	       		ncat++;
 	       		if( ncat >= NbrOfFeatures ) { do_error("Categories overrun"); return null; }
 	       	    categories[ncat] = new ARFFCategoryCoreDTO(name);
 	       	 }
 	       	 if( sLijn.indexOf("<CategoryType>") >= 0 ) {
 	       	    cmcARFF.ARFF_TYPE tipe = fetchARFFType( xMSet.xU.extractXMLValue(sLijn,"CategoryType") );
	        	if( tipe == null ) { do_error("cannot determine tipe" + sLijn); return null; }
	        	if( ncat < 0 ) { do_error("Category underrun"); return null; }
	        	categories[ncat].setTipe( tipe );
	        	continue;	 
 	       	 }
 	       	 if( sLijn.indexOf("<NominalValueList>") >= 0 ) {
  				String[] slist = getSquaredItems(  xMSet.xU.extractXMLValue(sLijn,"NominalValueList") );
  				if( slist.length < 1 ) {
  	       	    	do_error("There is nothing on the nominal value list [" + NumberOfClasses + "] items");
  	       	    	return null;
  	       	    }
  				if( ncat < 0 ) { do_error("Category underrun"); return null; }
  				categories[ncat].setNominalValueList( slist );
  				continue;	 
  			 }
  			 */
 	        
 	       	 //
 	       	 if( sLijn.indexOf("<KNNModelData>") >= 0 ) {
 	       		 inData=1;
 	       		 continue;
 	       	 }
 	       	 if( sLijn.indexOf("<![CDATA[") >= 0 ) {
	       		 if( inData == 1 ) inData = 2;
	       		 nrow=-1;
	       		 continue;
	       	 }
 	         if( sLijn.indexOf("]]>") >= 0 ) {
	       		 if( inData == 2 ) inData = 3;
	       		 continue;
	       	 }
 	       	 //
 	       	 if( inData == 2 ) {
 	       	   double[] list = getDoubleItems( sLijn.trim() );
 	       	   if( list == null ) { do_error("Parsing " + sLijn ); return null; }
 	       	   if( list.length != NbrOfFeatures ) { do_error("Too few values " + sLijn ); return null; }
 	       	   nrow++;
 	       	   if( nrow >= NbrOfSampleRows ) { do_error("Number of rows overrun"); return null; }
 	       	   for(int i=0;i<(list.length-1);i++)
 	       	   {
 	       		   boolean ib=model.setSamplesValue( nrow , i , list[i] );
 	       		   if( ib == false ) return null;
 	       	   }
 	       	   boolean ib = model.setResultsValue( nrow , list[list.length-1] );
 	       	   if( ib == false ) return null;
 	       	 }
 	       	}
 	       	// A couple of tests
 	       	if( nrow != (NbrOfSampleRows-1) ) { do_error("Number of rows underrun" + nrow + " " + NbrOfSampleRows); return null; }
 	       	if( model.getFeatures() == null ) { do_error("Features null"); return null; }
 	       	if( model.getFeatures().length != NbrOfFeatures ) { do_error("Features length mismatch"); return null; }
 	       	if (checkCategories( model.getFeatures() , NbrOfFeatures) == false ) return null;
 	        do_log( 1 , "Read model [" + ModelFileName + "] [Rows=" + NbrOfSampleRows + "] [Features=" + NbrOfFeatures + "]");
     	 }
         catch(Exception e ) {
 	     	do_error("reading [" + ModelFileName + "]" + xMSet.xU.LogStackTrace(e));
 	     	return null;
 	     }
     	 finally {
     		 try  {
     			 reader.close();
     		 }
     		 catch(Exception e ) {
      	     	do_error("Closing [" + ModelFileName + "]" + e.getMessage());
      	     
      	     	return null;
      	     }
     	 }
    	 return model;
    }
    
    //------------------------------------------------------------
    public boolean writeLogisticRegressionModel( cmcLogisticRegressionDTO dto , String LongModelName )
    //------------------------------------------------------------
    {
    	if( dto == null ) return false;
    	if( dto.getFeatures() == null ) { do_error("No features"); return false; }
    	if( dto.getNormalizerMean() == null ) { do_error("No means"); return false; }
    	if( dto.getNormalizerStdDev() == null ) { do_error("No stddevs"); return false; }
    	if( dto.getNormalizerStdDev().length != dto.getNormalizerMean().length ) { do_error("Number of means and stddev differ"); return false; }
    	if( dto.getFeatures().length != (dto.getNormalizerMean().length + 1) ) { do_error("Number features must be #means + 1"); return false; } // last feature is CLASS and therefore not normalized
    	if( dto.getWeights() == null ) { do_error("No weights"); return false; }
    	if( dto.getFeatures().length != dto.getWeights().length ) { do_error("Number weights must be #features   F=" + dto.getFeatures().length + " W=" +dto.getWeights().length ); return false; } // 1 weight per fearures and W0
    	if( dto.getFeatures().length != dto.getNbrOfFeatures() ) {do_error("Number features does not match fearure list"); return false; }
    	//
    	try {
    		gpPrintStream aps = new gpPrintStream( LongModelName , xMSet.getCodePageString());
    		aps.println(xMSet.getXMLEncodingHeaderLine());
    		aps.println("<!-- Application : " + xMSet.getApplicDesc() + " -->");
    		aps.println("<!-- Start : " + 	(xMSet.xU.prntStandardDateTime(System.currentTimeMillis())).toUpperCase() + " -->" );
    		aps.println("<LogisticRegressionModel>");
        	
    		aps.println("<LRModelGeneral>");
    		aps.println("<ModelApplicationDomain>General</ModelApplicationDomain>");
    		aps.println("<ARFFFileName>" + dto.getLongARFFFileName() + "</ARFFFileName>");	
    		aps.println("<MaxCycles>" + dto.getMaxCycles() + "</MaxCycles>");	
    		aps.println("<OptimalCycles>" + dto.getOptimalCycles() + "</OptimalCycles>");
    		aps.println("<StepSize>" + dto.getStepSize() + "</StepSize>");
    		aps.println("<NumberOfFeatures>" + dto.getNbrOfFeatures() + "</NumberOfFeatures>");
        	String sw = "";
    		for(int i=0;i<dto.getWeights().length;i++)
    		{
    			sw += "[" + dto.getWeights()[i] + "]";
    		}
        	aps.println("<Weights>" + sw + "</Weights>");
    		aps.println("</LRModelGeneral>");
    		//
    		aps.println("<LogisticRegressionModelEvaluation>");
    		aps.println("<Evaluation>EMPTY</Evaluation>");
    		aps.println("</LogisticRegressionModelEvaluation>");
    		//
    		if( dumpARFFCategoryCoreDTOList( dto.getFeatures() , aps ) == false ) return false;
    		// normalisation
    		aps.println("<Normalisation>");
    		String sm = "";
    		String ss = "";
    		for(int i=0;i<dto.getNormalizerMean().length;i++)
    		{
    			sm += "[" + dto.getNormalizerMean()[i] + "]";
    			ss += "[" + dto.getNormalizerStdDev()[i] + "]";
    		}
    		aps.println("<NormalisationMean>" + sm + "</NormalisationMean>");
    		aps.println("<NormalisationStdDev>" + ss + "</NormalisationStdDev>");
        	aps.println("</Normalisation>");
        	
        	
    		//
    		aps.println("</LogisticRegressionModel>");
    		
    		return true;
    	}
    	catch(Exception e ) {
    	  do_error( "Writing [" + LongModelName + "]" + e.getMessage() );
    	  return false;
    	}
    	
    }
    //------------------------------------------------------------
    public cmcLogisticRegressionDTO readLogisticRegressionModel( String ModelFileName )
    //------------------------------------------------------------
    {
    	if( xMSet.xU.IsBestand(ModelFileName) == false ) {
        	 do_error("Cannot lcoate model file [" + ModelFileName + "]");
        	 return null;
         }
         //
    	 cmcLogisticRegressionDTO model = null;
    	 boolean inCategories=false;
    	 ARFFCategoryCoreDTO[] categories = null;
    	
    	 //
         BufferedReader reader=null;
     	 try {
 			File inFile  = new File(ModelFileName);  // File to read from.
 	        reader = new BufferedReader(new InputStreamReader(new FileInputStream(inFile), xMSet.getCodePageString()));
 	       	//
 	       	String sLijn=null;
 	       	while ((sLijn=reader.readLine()) != null) {
 	       	  if( sLijn.trim().length() < 1 ) continue;
    	      //do_log( 1 , sLijn );
    	      if( sLijn.indexOf("<ARFFFileName>") >= 0 ) {
	  				String ARFFFileName = xMSet.xU.extractXMLValue(sLijn,"ARFFFileName");
	  				if( ARFFFileName == null ) continue;
	  				if( model != null ) {
	  					do_error( "already a model"); return null;
	  				}
	  				model = new cmcLogisticRegressionDTO( ARFFFileName );
	  				continue;	 
	  		  }
    	      if( model == null )  continue;
    	      //
    	      if( sLijn.indexOf("<MaxCycles>") >= 0 ) {
	  				int mx = xMSet.xU.NaarInt( xMSet.xU.extractXMLValue(sLijn,"MaxCycles"));
	  				if( mx <= 0 ) { do_error("Wrong max cycle value " + sLijn ); return null; }
	  				model.setMaxCycles(mx);
	  				continue;	 
	  		  }
    	      //
    	      if( sLijn.indexOf("<OptimalCycles>") >= 0 ) {
	  				int opt = xMSet.xU.NaarInt( xMSet.xU.extractXMLValue(sLijn,"OptimalCycles"));
	  				if( opt <= 0 ) { do_error("Wrong optimal cycle value " + sLijn ); return null; }
	  				model.setOptimalCycles(opt);
	  				continue;	 
	  		  }
    	      //
    	      if( sLijn.indexOf("<NumberOfFeatures>") >= 0 ) {
	  				int nf = xMSet.xU.NaarInt( xMSet.xU.extractXMLValue(sLijn,"NumberOfFeatures"));
	  				if( nf <= 0 ) { do_error("Wrong number of features" + sLijn ); return null; }
	  				model.setNbrOfFeatures(nf);
	  				continue;	 
	  		  }
    	      //
    	      if( sLijn.indexOf("<StepSize>") >= 0 ) {
	  				double dd = xMSet.xU.NaarDoubleNAN( xMSet.xU.extractXMLValue(sLijn,"StepSize") );
	  				if( dd <= 0 ) { do_error("Wrong value stepsize" + sLijn ); return null; }
	  				model.setStepSize(dd);
	  				continue;	 
	  		  }
    	      //
    	      if( sLijn.indexOf("<Weights>") >= 0 ) {
	  				String[] swlist = getSquaredItems(xMSet.xU.extractXMLValue(sLijn,"Weights"));
	  				if( swlist == null ) { do_error("Cannot read weights" + sLijn); return null; }
	  				if( swlist.length <= 0 ) { do_error("Empty weights" + sLijn); return null; }
	  				double[] dlist = new double[ swlist.length ];
	  				for(int i=0;i<swlist.length;i++)
	  				{
	  					double dd = xMSet.xU.NaarDoubleNAN( swlist[i] );
	  					if( Double.isNaN(dd) ) { do_error("conversion error on weights" + sLijn ); return null; }
	  					dlist[i] = dd;
	  				}
	  				model.setWeights(dlist);
	  				continue;	 
	  		  }
    	      //
    	      if( sLijn.indexOf("<NormalisationMean>") >= 0 ) {
	  				String[] swlist = getSquaredItems(xMSet.xU.extractXMLValue(sLijn,"NormalisationMean"));
	  				if( swlist == null ) { do_error("Cannot read norm mean" + sLijn); return null; }
	  				if( swlist.length <= 0 ) { do_error("Empty norm mean" + sLijn); return null; }
	  				double[] dlist = new double[ swlist.length ];
	  				for(int i=0;i<swlist.length;i++)
	  				{
	  					double dd = xMSet.xU.NaarDoubleNAN( swlist[i] );
	  					if( Double.isNaN(dd) ) { do_error("conversion error on norm mean" + sLijn ); return null; }
	  					dlist[i] = dd;
	  				}
	  				model.setNormalizerMean(dlist);
	  				continue;	 
	  		  }
    	      //
    	      if( sLijn.indexOf("<NormalisationStdDev>") >= 0 ) {
	  				String[] swlist = getSquaredItems(xMSet.xU.extractXMLValue(sLijn,"NormalisationStdDev"));
	  				if( swlist == null ) { do_error("Cannot read norm stddev" + sLijn); return null; }
	  				if( swlist.length <= 0 ) { do_error("Empty norm stddev" + sLijn); return null; }
	  				double[] dlist = new double[ swlist.length ];
	  				for(int i=0;i<swlist.length;i++)
	  				{
	  					double dd = xMSet.xU.NaarDoubleNAN( swlist[i] );
	  					if( Double.isNaN(dd) ) { do_error("conversion error on norm stdev" + sLijn ); return null; }
	  					dlist[i] = dd;
	  				}
	  				model.setNormalizerStdDev(dlist);
	  				continue;	 
	  		  }
    	      //
    	      if( sLijn.indexOf("<Categories>") >= 0 ) {
    	    	 int ncats =  model.getNbrOfFeatures();
    	    	 if( ncats < 0 ) { do_error("Number of features not found"); return null; }
  	       		 inCategories = true;
  	       		 categories = new ARFFCategoryCoreDTO[ncats];
   	          }
  	       	  if( inCategories ) {
  	       		if( parseCategory(  sLijn , categories ) == false ) return null;
  	       	  }
  	          if( sLijn.indexOf("</Categories>") >= 0 ) {
  	        	inCategories=false;
  	        	for(int i=0;i<categories.length;i++) 
  	        	{
  	        		if( categories[i] == null ) { do_error( "found uninitialized category"); return null; }
  	        	}
  	        	model.setFeatures( categories );
  	         }
    	      
 	       	}
     	 } 	
     	 catch(Exception e ) {
     		 do_error("Reading [" + ModelFileName + "]" + e.getMessage());
     		 return null;
     	 }
     	 finally {
     		 try  {
     			 reader.close();
     		 }
     		 catch(Exception e ) {
      	     	do_error("Closing [" + ModelFileName + "]" + e.getMessage());
      	     	return null;
      	     }
     	 }
     	 
     	 // null checks
     	 if( model == null ) { do_error( "Nothing processed"); return null; }
     	 if( model.getLongARFFFileName() == null ) { do_error( "No ARFF File Name"); return null; }
     	 if( model.getMaxCycles() < 0) { do_error( "No max cycles"); return null; }
     	 if( model.getOptimalCycles() < 0) { do_error( "No max cycles"); return null; }
     	 if( model.getNormalizerMean() == null ) { do_error( "Null normalizer mean"); return null; }
     	 if( model.getNormalizerStdDev() == null ) { do_error( "Null normalizer stdev"); return null; }
     	 if( model.getWeights() == null ) { do_error( "Null weights"); return null; }
     	 if( model.getFeatures() == null ) { do_error( "No-ull features"); return null; }
     	 // cats
     	 int NbrOfFeatures = model.getNbrOfFeatures();
      	 if( model.getFeatures().length != NbrOfFeatures ) { do_error("Features length mismatch"); return null; }
	     if (checkCategories( model.getFeatures() , NbrOfFeatures) == false ) return null;
	     //  counts
	     if( model.getNormalizerMean().length < 0 ) { do_error( "Empty normalizer mean"); return null; }
	     if( model.getNormalizerStdDev().length < 0 ) { do_error( "Empty normalizer stddev"); return null; }
	     if( model.getNormalizerStdDev().length != model.getNormalizerMean().length ) { do_error( "Normaliser mean and sttdev number mismatch"); return null; }
	     if( model.getNormalizerMean().length != (NbrOfFeatures -1) ) { do_error( "Normaliser count is different from feature count minus one"); return null; }
	     if( model.getWeights().length != NbrOfFeatures ) { do_error( "Weight count differs from feature count"); return null; }
	     //
	     for(int i=0;i<model.getNormalizerMean().length;i++) {
	        	if( Double.isNaN( model.getNormalizerMean()[i] ) ) {
	        		do_error( "NAN on NormalizedMean "); return null;
	        	}
	        	if( Double.isNaN( model.getNormalizerStdDev()[i] ) ) {
	        		do_error( "NAN on Normalized STDDEV"); return null;
	        	}
	        	if( model.getNormalizerStdDev()[i] == 0 ) {
	        		do_error( "Zero value on Normalized StdDev"); return null;
	        	}
	     }
	     //
	     do_log( 1 , "Read LR model [" + ModelFileName + "] [Features=" + NbrOfFeatures + "]");
	     //
    	 return model;
    }
    //------------------------------------------------------------
    public boolean writeSMO( cmcSMODTO dto , String LongModelName )
    //------------------------------------------------------------
    {
    	if( dto == null ) return false;
    	if( dto.getFeatures() == null ) { do_error("SMO DAO 1"); return false; }
    	cmcMatrix weightMatrix = dto.getWeightMatrix();
		if( weightMatrix == null ) { do_error("SMO DAO 2"); return false; }
    	if( weightMatrix.getNbrOfColumns() != (dto.getNbrOfFeatures() - 1) ) { do_error("SMO DAO 3"); return false; }
    	if( weightMatrix.getNbrOfRows() != 1 ) { do_error("SMO DAO 4"); return false; }
		
    	try {
    		gpPrintStream aps = new gpPrintStream( LongModelName , xMSet.getCodePageString());
    		aps.println(xMSet.getXMLEncodingHeaderLine());
    		aps.println("<!-- Application : " + xMSet.getApplicDesc() + " -->");
    		aps.println("<!-- Start : " + 	(xMSet.xU.prntStandardDateTime(System.currentTimeMillis())).toUpperCase() + " -->" );
    		aps.println("<SMOModel>");
    		
    		aps.println("<SMOModelGeneral>");
    		aps.println("<ModelApplicationDomain>General</ModelApplicationDomain>");
    		aps.println("<ARFFFileName>" + dto.getLongARFFFileName() + "</ARFFFileName>");	
    		aps.println("<KernelTrickType>" + dto.getKernelTrickTipe() + "</KernelTrickType>");	
    		aps.println("<NumberOfFeatures>" + dto.getNbrOfFeatures() + "</NumberOfFeatures>");
    		aps.println("<MaxCycles>" + dto.getMaxCycles() + "</MaxCycles>");	
    		aps.println("<Tolerance>" + dto.getTolerance() + "</Tolerance>");
    		aps.println("<SoftMargin>" + dto.getC() + "</SoftMargin>");
    		aps.println("<NumberOfSupportVectors>" + dto.getNbrOfSupportVectors() + "</NumberOfSupportVectors>");
    		aps.println("</SMOModelGeneral>");
    		//
    		aps.println("<SMOModelEvaluation>");
    		aps.println("<Evaluation>EMPTY</Evaluation>");
    		aps.println("</SMOModelEvaluation>");
    		//
    		aps.println("<LinearEquation>");
    		String sw = "";
    		for(int i=0;i<weightMatrix.getValues()[0].length;i++)
    		{
    			sw += "[" + weightMatrix.getValues()[0][i] + "]";
    		}
    		aps.println("<Weights>" + sw + "</Weights>");
    		aps.println("<Bias>" + dto.getBias() + "</Bias>");
    		aps.println("</LinearEquation>");
    		//
    		if( dumpARFFCategoryCoreDTOList( dto.getFeatures() , aps ) == false ) return false;
    		// normalisation
    		aps.println("<Normalisation>");
    		String sm = "";
    		String ss = "";
    		for(int i=0;i<dto.getNormalizerMean().length;i++)
    		{
    			sm += "[" + dto.getNormalizerMean()[i] + "]";
    			ss += "[" + dto.getNormalizerStdDev()[i] + "]";
    		}
    		aps.println("<NormalisationMean>" + sm + "</NormalisationMean>");
    		aps.println("<NormalisationStdDev>" + ss + "</NormalisationStdDev>");
        	aps.println("</Normalisation>");
    		//
    		aps.println("</SMOModel>");
    		aps.close();
    	}
    	catch(Exception e ) {
      	  do_error( "Writing [" + LongModelName + "]" + e.getMessage() );
      	  return false;
      	}
    	do_log( 1 , "SMO written to [" + LongModelName + "]");
    	return true;
    }
    
    //------------------------------------------------------------
    public cmcSMODTO readSMOModel( String ModelFileName )
    //------------------------------------------------------------
    {
    	if( xMSet.xU.IsBestand(ModelFileName) == false ) {
        	 do_error("Cannot lcoate model file [" + ModelFileName + "]");
        	 return null;
         }
         //
    	 cmcSMODTO model = null;
    	 boolean inCategories=false;
    	 ARFFCategoryCoreDTO[] categories = null;
    	 //
         BufferedReader reader=null;
     	 try {
 			File inFile  = new File(ModelFileName);  // File to read from.
 	        reader = new BufferedReader(new InputStreamReader(new FileInputStream(inFile), xMSet.getCodePageString()));
 	       	//
 	       	String sLijn=null;
 	       	while ((sLijn=reader.readLine()) != null) {
 	       	  if( sLijn.trim().length() < 1 ) continue;
    	      //do_log( 1 , sLijn );
    	      if( sLijn.indexOf("<ARFFFileName>") >= 0 ) {
	  				String ARFFFileName = xMSet.xU.extractXMLValue(sLijn,"ARFFFileName");
	  				if( ARFFFileName == null ) continue;
	  				if( model != null ) {
	  					do_error( "already a model"); return null;
	  				}
	  				model = new cmcSMODTO( ARFFFileName , cmcMachineLearningEnums.KernelType.UNKNOWN );
	  				continue;	 
	  		  }
    	      if( model == null )  continue;
    	      //
    	      if( sLijn.indexOf("<KernelTrickType>") >= 0 ) {
	  				cmcMachineLearningEnums tp = new cmcMachineLearningEnums();
	  				cmcMachineLearningEnums.KernelType tipe = tp.getKernelType(xMSet.xU.extractXMLValue(sLijn,"KernelTrickType"));
	  				if( tipe == null ) { do_error("Wrong Kernel Trick Tipe " + sLijn ); return null; }
	  				model.setKernelTrickTipe(tipe);
	  				continue;	 
	  		  }
    	      //
    	      if( sLijn.indexOf("<MaxCycles>") >= 0 ) {
	  				int mx = xMSet.xU.NaarInt( xMSet.xU.extractXMLValue(sLijn,"MaxCycles"));
	  				if( mx <= 0 ) { do_error("Wrong max cycle value " + sLijn ); return null; }
	  				model.setMaxCycles(mx);
	  				continue;	 
	  		  }
    	      //
    	      if( sLijn.indexOf("<NumberOfFeatures>") >= 0 ) {
	  				int nf = xMSet.xU.NaarInt( xMSet.xU.extractXMLValue(sLijn,"NumberOfFeatures"));
	  				if( nf <= 0 ) { do_error("Wrong number of features" + sLijn ); return null; }
	  				model.setNbrOfFeatures(nf);
	  				continue;	 
	  		  }
    	      // 
    	      if( sLijn.indexOf("<NumberOfSupportVectors>") >= 0 ) {
	  				int nf = xMSet.xU.NaarInt( xMSet.xU.extractXMLValue(sLijn,"NumberOfSupportVectors"));
	  				if( nf <= 0 ) { do_error("Wrong number of sup vectors" + sLijn ); return null; }
	  				model.setNbrOfSupportVectors(nf);
	  				continue;	 
	  		  }
    	      //
    	      if( sLijn.indexOf("<SoftMargin>") >= 0 ) {
	  				double dd = xMSet.xU.NaarDoubleNAN( xMSet.xU.extractXMLValue(sLijn,"SoftMargin"));
	  				if( Double.isNaN(dd) ) { do_error("Wrong Soft Margin (C) " + sLijn ); return null; }
	  				model.setC(dd);
	  				continue;	 
	  		  }
    	      //
    	      if( sLijn.indexOf("<Tolerance>") >= 0 ) {
	  				double dd = xMSet.xU.NaarDoubleNAN( xMSet.xU.extractXMLValue(sLijn,"Tolerance"));
	  				if( Double.isNaN(dd) ) { do_error("Wrong Tolerance " + sLijn ); return null; }
	  				model.setTolerance(dd);
	  				continue;	 
	  		  }
    	      //
    	      if( sLijn.indexOf("<Bias>") >= 0 ) {
	  				double dd = xMSet.xU.NaarDoubleNAN( xMSet.xU.extractXMLValue(sLijn,"Bias"));
	  				if( Double.isNaN(dd) ) { do_error("Wrong Bias " + sLijn ); return null; }
	  				model.setBias(dd);
	  				continue;	 
	  		  }
    	      //
    	      if( sLijn.indexOf("<Weights>") >= 0 ) {
	  				String[] swlist = getSquaredItems(xMSet.xU.extractXMLValue(sLijn,"Weights"));
	  				if( swlist == null ) { do_error("Cannot read weights" + sLijn); return null; }
	  				if( swlist.length <= 0 ) { do_error("Empty weights" + sLijn); return null; }
	  				double[][] dlist = new double[1][ swlist.length ];
	  				for(int i=0;i<swlist.length;i++)
	  				{
	  					double dd = xMSet.xU.NaarDoubleNAN( swlist[i] );
	  					if( Double.isNaN(dd) ) { do_error("conversion error on weights" + sLijn ); return null; }
	  					dlist[0][i] = dd;
	  				}
	  				model.setWeightMatrix( new cmcMatrix( dlist ));
	  				if( model.getWeightMatrix() == null ) { do_error("Could not set weights" + sLijn ); return null; }
	  				continue;	 
	  		  }
    	      //
    	      if( sLijn.indexOf("<NormalisationMean>") >= 0 ) {
	  				String[] swlist = getSquaredItems(xMSet.xU.extractXMLValue(sLijn,"NormalisationMean"));
	  				if( swlist == null ) { do_error("Cannot read norm mean" + sLijn); return null; }
	  				if( swlist.length <= 0 ) { do_error("Empty norm mean" + sLijn); return null; }
	  				double[] dlist = new double[ swlist.length ];
	  				for(int i=0;i<swlist.length;i++)
	  				{
	  					double dd = xMSet.xU.NaarDoubleNAN( swlist[i] );
	  					if( Double.isNaN(dd) ) { do_error("conversion error on norm mean" + sLijn ); return null; }
	  					dlist[i] = dd;
	  				}
	  				model.setNormalizerMean(dlist);
	  				continue;	 
	  		  }
    	      //
    	      if( sLijn.indexOf("<NormalisationStdDev>") >= 0 ) {
	  				String[] swlist = getSquaredItems(xMSet.xU.extractXMLValue(sLijn,"NormalisationStdDev"));
	  				if( swlist == null ) { do_error("Cannot read norm stddev" + sLijn); return null; }
	  				if( swlist.length <= 0 ) { do_error("Empty norm stddev" + sLijn); return null; }
	  				double[] dlist = new double[ swlist.length ];
	  				for(int i=0;i<swlist.length;i++)
	  				{
	  					double dd = xMSet.xU.NaarDoubleNAN( swlist[i] );
	  					if( Double.isNaN(dd) ) { do_error("conversion error on norm stdev" + sLijn ); return null; }
	  					dlist[i] = dd;
	  				}
	  				model.setNormalizerStdDev(dlist);
	  				continue;	 
	  		  }
    	      //
    	      if( sLijn.indexOf("<Categories>") >= 0 ) {
    	    	 int ncats =  model.getNbrOfFeatures();
    	    	 if( ncats < 0 ) { do_error("Number of features not found"); return null; }
  	       		 inCategories = true;
  	       		 categories = new ARFFCategoryCoreDTO[ncats];
   	          }
  	       	  if( inCategories ) {
  	       		if( parseCategory(  sLijn , categories ) == false ) return null;
  	       	  }
  	          if( sLijn.indexOf("</Categories>") >= 0 ) {
  	        	inCategories=false;
  	        	for(int i=0;i<categories.length;i++) 
  	        	{
  	        		if( categories[i] == null ) { do_error( "found uninitialized category"); return null; }
  	        	}
  	        	model.setFeatures( categories );
  	         }
  	       	}
     	 } 	
     	 catch(Exception e ) {
     		 do_error("Reading [" + ModelFileName + "]" + e.getMessage());
     		 return null;
     	 }
     	 finally {
     		 try  {
     			 reader.close();
     		 }
     		 catch(Exception e ) {
      	     	do_error("Closing [" + ModelFileName + "]" + e.getMessage());
      	     	return null;
      	     }
     	 }
     	 
     	 // check
    	 if( model == null ) { do_error( "Nothing processed"); return null; }
    	 if( model.getKernelTrickTipe() == null )  { do_error( "NULL kerneltrick"); return null; }
    	 if( model.getKernelTrickTipe() == cmcMachineLearningEnums.KernelType.UNKNOWN)  { do_error( "Unknown kerneltrick"); return null; }
     	 if( model.getLongARFFFileName() == null ) { do_error( "No ARFF File Name"); return null; }
     	 if( model.getMaxCycles() < 0) { do_error( "No max cycles"); return null; }
     	 if( model.getNormalizerMean() == null ) { do_error( "Null normalizer mean"); return null; }
     	 if( model.getNormalizerStdDev() == null ) { do_error( "Null normalizer stdev"); return null; }
     	 if( model.getWeightMatrix() == null ) { do_error( "Null weights"); return null; }
     	 if( model.getFeatures() == null ) { do_error( "No-ull features"); return null; }
     	 if( Double.isNaN(model.getC()) ) { do_error( "Null Soft Margin C"); return null; }
     	 if( Double.isNaN(model.getBias())) { do_error( "Null Bias b"); return null; }
     	 if( Double.isNaN(model.getTolerance())) { do_error( "Null Bias b"); return null; }
     	 // cats
     	 int NbrOfFeatures = model.getNbrOfFeatures();
      	 if( model.getFeatures().length != NbrOfFeatures ) { do_error("Features length mismatch"); return null; }
	     if (checkCategories( model.getFeatures() , NbrOfFeatures) == false ) return null;
	     //  counts
	     if( model.getWeightMatrix().getNbrOfRows() !=1 ) { do_error( "Weight matrix must have exactly 1 colums"); return null; }
	     if( (model.getNbrOfFeatures() - 1) != model.getWeightMatrix().getNbrOfColumns() ) { do_error( "Number of feature mismatch with weight matrix"); return null; }
         if( model.getNormalizerMean().length < 0 ) { do_error( "Empty normalizer mean"); return null; }
	     if( model.getNormalizerStdDev().length < 0 ) { do_error( "Empty normalizer stddev"); return null; }
	     if( model.getNormalizerStdDev().length != model.getNormalizerMean().length ) { do_error( "Normaliser mean and sttdev number mismatch"); return null; }
	     if( model.getNormalizerMean().length != (NbrOfFeatures -1) ) { do_error( "Normaliser count is different from feature count minus one"); return null; }
	     //
	     for(int i=0;i<model.getNormalizerMean().length;i++) {
	        	if( Double.isNaN( model.getNormalizerMean()[i] ) ) {
	        		do_error( "NAN on NormalizedMean "); return null;
	        	}
	        	if( Double.isNaN( model.getNormalizerStdDev()[i] ) ) {
	        		do_error( "NAN on Normalized STDDEV"); return null;
	        	}
	        	if( model.getNormalizerStdDev()[i] == 0 ) {
	        		do_error( "Zero value on Normalized StdDev"); return null;
	        	}
	     }
         //
	     do_log( 1 , "Read SMO Model [" + ModelFileName + "] [Kernel=" + model.getKernelTrickTipe() + "]");
    	 return model;
    } 	 
}
