package generalMachineLearning.ARFF;

import cbrTekStraktorModel.cmcProcSettings;
import generalMachineLearning.LinearAlgebra.cmcMatrix;
import generalMachineLearning.LinearAlgebra.cmcVector;
import generalMachineLearning.LinearAlgebra.cmcVectorRoutines;
import logger.logLiason;

public class cmcTextToARFF {
	
	cmcProcSettings xMSet=null;
	logLiason logger=null;
	cmcVectorRoutines vrout = null;
	cmcARFFRoutines arffrout = null;
	
	private cmcVector resultVector = null;
	private String LongTestSetName = null;
	private String LongTrainingSetName = null;
	private String[] CBRFileNameList = null;
	
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
    public cmcTextToARFF(cmcProcSettings is,logLiason ilog)
    //------------------------------------------------------------
    {
    	xMSet = is;
		logger=ilog;
		vrout = new cmcVectorRoutines();
		arffrout = new cmcARFFRoutines( xMSet , logger );
	}

    //------------------------------------------------------------
    //------------------------------------------------------------
    public String getTestSetName()
    {
    	return LongTestSetName;
    }
    public String getTrainingSetName()
    {
    	return LongTrainingSetName;
    }
    public cmcVector getResultVector()
    {
    	return resultVector;
    }
    public String[] getValidatedCBRFileNameList(int len)
    {
    	 if( CBRFileNameList == null ) return null;
    	 if( CBRFileNameList.length != len )  	{
         	do_error("CBR File Name list lenght does not match the anticipated number of items");
         	return null;
         }
         boolean ok=true;
         for(int i=0;i<CBRFileNameList.length;i++)
         {
         		String FName = CBRFileNameList[i];
         		if( FName== null ) { ok=false; break; }
         		FName = FName.trim().toUpperCase();
         		if( (FName.startsWith("POBJ_")==false) && (FName.startsWith("PTXT_")==false) ) { ok=false; break;}
         }
         if( ok == false ) return null;
         do_log(1,"Looks like a genuine CBR ARFF file");
         return CBRFileNameList;
    }
    //------------------------------------------------------------
    //------------------------------------------------------------
     
    //------------------------------------------------------------
    public ARFFCategory[] getCategories( String ARFFFile , String ARFFPicture , int NBR_OF_BINS , boolean RequestTrainingSet)
    //------------------------------------------------------------
    {
    	LongTestSetName = null;
    	LongTrainingSetName = null;
    	ARFFWrapperDTO dto = arffrout.getCategoriesAndOtherStuff( ARFFFile, NBR_OF_BINS , RequestTrainingSet );
    	if( dto == null ) return null;
    	if( dto.isValid() == false ) return null;
    	LongTestSetName = dto.getFileNameTestSet();
    	LongTrainingSetName = dto.getFileNameTrainingSet();
    	int AdjustedNbrOfBins = dto.getNbrOfBins();
    	if( AdjustedNbrOfBins != NBR_OF_BINS ) {
    		do_log( 1 , "Number of bins has been adjusted from [" + NBR_OF_BINS + "] to [" + AdjustedNbrOfBins + "]");
    	}
    	//
    	if( ARFFPicture != null ) {
    	  ARFFVisualizer vis = new ARFFVisualizer( xMSet , logger );
    	  boolean ib = vis.makeARFFImageFromModel( dto.getXa() , dto.getCategories() , dto.getNbrOfBins() , ARFFPicture , false );
    	  vis = null;
    	  if( ib == false ) return null;
    	}
    	//
    	cmcARFF xa = dto.getXa();
    	ARFFCategory[] categories = dto.getCategories();
    	if( xa == null ) return null;
        if( xa.getAttributeList().length < 1 ) { do_error("Nothing to process"); return null; }
        int nattribs = xa.getAttributeList().length;
        int nvalids = 0;
        String[] ClassNameList=null;
        CBRFileNameList=null;
        for(int i=0 ; i<nattribs ; i++)
        {
        	cmcARFF.ARFFattribute attrib = xa.getAttributeList()[i];
        	if( attrib == null ) continue;
        	if( attrib.getTipe() == cmcARFF.ARFF_TYPE.STRING ) {
        		if( attrib.getAttrName().compareToIgnoreCase("FILENAME") == 0 ) { // there are filenames defined
        			do_log( 1 , "Found a CBR Filename list on attribute [" + i + "]" );
        			CBRFileNameList = attrib.getSValues();
        		}
        		continue;
        	}
        	if( attrib.getTipe() == cmcARFF.ARFF_TYPE.CLASS ) {
        		ClassNameList = attrib.getClasses();
        	}
        	nvalids++;
        }
        if( nvalids < 0 ) {
        	do_error("There are no attributes differing from STRING. There is no numeric input."); 
        	return null;
        }
        if( categories == null ) { do_error("Category is null"); return null; }
        if( categories.length != nattribs ) { do_error("#Categories does not match #Attribs [" + categories.length + "] [" + nattribs + "]"); }
        if( ClassNameList == null ) { do_error("ClassNameList is null"); return null; }
        if( ClassNameList.length < 1 ) { do_error("ClassNameList is empty"); return null; }
        do_log( 1 , "[#Attrs=" + nattribs + "] [#Valids=" + nvalids + "] [#ClassItems=" + ClassNameList.length + "]" ) ;
    	//
        int nrows=-1;
        for(int i=0 ; i<categories.length ; i++)
        {
        	ARFFCategory cat = categories[i];
        	if( cat == null ) continue;
        	if( cat.getTipe() == cmcARFF.ARFF_TYPE.STRING ) continue;
            if( nrows < 0 ) nrows=cat.getValues().length;
            if( nrows != cat.getValues().length ) { do_error("number of lines does not match"); return null; }
            //  CAUTION : the value lists on the various categories must be re-ordered by their line number
            if( cat.restoreLineNumberOrder() == false ) { do_error("Could not reorder"); return null; }
        }
        // remove the STRINGs from the list - easiest way to ensure categories in line with matrices
        if( nvalids != categories.length )
        {
        	ARFFCategory[] subset = new ARFFCategory[ nvalids ];
        	int featureIdx=-1;
        	for(int i=0 ; i<categories.length ; i++)
            {
            	ARFFCategory cat = categories[i];
            	if( cat == null ) continue;
            	if( cat.getTipe() == cmcARFF.ARFF_TYPE.STRING ) {
            		do_log( 1 , "Category [" + i + "] [Name=" + cat.getCategoryName() + "] is STRING - skipping");
            	    continue;
            	}
            	featureIdx++;
            	subset[ featureIdx ] = cat; 
            }
            categories=null;
            categories = subset;
            do_log( 1 , "Limited to [#Attrs=" + nattribs + "] [#Valids=" + featureIdx + "] [Classes=" + ClassNameList.length + "]" ) ;
        }
        return categories;
    }

    // returns 2 matrices : first is the Data matrix, second is the target matrix
    //------------------------------------------------------------
    public cmcMatrix[] transformIntoMatrix(ARFFCategory[] categories )
    //------------------------------------------------------------
    {
        int nvalidcats=0;
        int numidx = -1;
        for(int i=0 ; i<categories.length ; i++)
        {
        	ARFFCategory cat = categories[i];
        	if( cat == null ) continue;
        	if( cat.getTipe() == cmcARFF.ARFF_TYPE.STRING ) continue;
           	if( cat.getTipe() == cmcARFF.ARFF_TYPE.CLASS ) {
        		if( i != (categories.length-1) ) {
        			do_error( "CLASS not on last column");
        			return null;
        		}
                continue;
           	}
           	numidx = i;
        	nvalidcats++;
        }
        if( nvalidcats == 0 ) {
        	do_error( "there are no valid categories");
        	return null;
        }
        int nrows = categories[numidx].getValues().length;
        double[][] smodat = new double[nrows][nvalidcats]; 
        double[][] target = new double[nrows][1];
        for(int i=0;i<smodat.length;i++)
        {
       	  for(int j=0;j<smodat[i].length;j++) smodat[i][j] = Double.NaN;
        }
        for(int i=0;i<target.length;i++)
        {
       	  for(int j=0;j<target[i].length;j++) target[i][j] = Double.NaN;
        }
        //
        int featureIdx=-1;
        for(int i=0 ; i<categories.length ; i++)
        {
        	ARFFCategory cat = categories[i];
        	if( cat == null ) continue;
        	else
        	if( cat.getTipe() == cmcARFF.ARFF_TYPE.STRING ) {
        		//do_log( 1 , "Category [" + i + "] [Name=" + cat.getCategoryName() + "] is STRING - skipping");
        		do_error( "Bummer there is still a STRING");  // approach now is to remove the string sform the categeories
        		return null;
        		//continue; // skip
        	}
        	else
        	if( cat.getTipe() == cmcARFF.ARFF_TYPE.CLASS ) { // class
        		String[] ClassNames = cat.getNominalValueList();
        		if( ClassNames == null ) { do_error("System Error - NULL ClassNames"); return null; }
        		// neem gewoon de nominal index over
        		for(int j=0;j<cat.getValues().length;j++)
             	{   // the nominal parser transforms classes to indices,
        			target[j][0] = (int)cat.getValues()[j];
             	} 	
        	    continue;
        	}
        	else
        	if( (cat.getTipe() == cmcARFF.ARFF_TYPE.NUMERIC) || (cat.getTipe() == cmcARFF.ARFF_TYPE.NOMINAL) ) {
        	   //do_log( 1  , "cat=" + i + " " + cat.getCategoryName() + " " + cat.getTipe() );
        	   featureIdx++;
        	   if( featureIdx >= nvalidcats ) {
        		   do_error("System error - too few columns [FeatureIdx=" + featureIdx + "] [ValidCats=" + nvalidcats +"]"); 
        		   return null;
        	   }
        	   for(int j=0;j<cat.getValues().length;j++)
        	   {
        		smodat[j][featureIdx] = cat.getValues()[j];
        	   }
        	}
        	else {
        		do_error( "Unsuported type [" + cat.getTipe() + "]");
    			return null;
    		}
        }
        if( featureIdx != (nvalidcats - 1) ) {
        	do_error("Not every feature/category has been loaded in the matrix");
        	return null;
        }
        if( target.length != smodat.length ) { do_error("number of labels does not match"); return null; }
        int cntr=0;
        for(int i=0;i<smodat.length;i++)
        {
        	for(int j=0;j<smodat[i].length;j++) if( Double.isNaN( smodat[i][j] )) cntr++;
        }
        for(int i=0;i<target.length;i++)
        {
        	for(int j=0;j<target[i].length;j++) if( Double.isNaN(target[i][j] )) cntr++;
        }
        if( cntr != 0 ) {
        	do_error("Matrices not initialized correctly. Found [" + cntr + "] NaN"); return null;
        }
        cmcMatrix[] DataTargetCouple = new cmcMatrix[ 2 ];
        DataTargetCouple[0] = new cmcMatrix( smodat );
        DataTargetCouple[1] = new cmcMatrix( target );
        return DataTargetCouple;
    }
    
    // returns a list of vectors and resultvector
    //------------------------------------------------------------
    public cmcVector[] transformIntoVectors(ARFFCategory[] categories )
    //------------------------------------------------------------
    {
       resultVector = null;
       cmcMatrix[] DataTargetCouple =  transformIntoMatrix( categories );
       if( DataTargetCouple == null ) return null;
       
       // list of data vectors
       cmcMatrix datam = DataTargetCouple[0];
       if( datam == null ) { do_error("Null data matrix");   return null; }
       int nrows = datam.getNbrOfRows();
       int ncols = datam.getNbrOfColumns();
       if( (nrows <1) || (ncols <1) ) { do_error("no rows or columns found"); return null; }
       cmcVector[] dataVectorList = new cmcVector[ nrows ];
       for(int row=0;row<nrows;row++)
       {
    	   double[] vals = new double[ ncols ];
    	   for(int i=0;i<ncols;i++) vals[i] = datam.getValues()[row][i];
    	   dataVectorList[ row ] = new cmcVector( vals );
       }
       
       // target vector
       cmcMatrix target = DataTargetCouple[1];
       if( target == null ) { do_error("Null target matrix");   return null; }
       if( target.getNbrOfColumns() != 1) {
    	   do_error("The dimension of the target matrix is not [M,1] [" + target.getNbrOfRows() + "][" + target.getNbrOfColumns() + "]");
    	   return null;
       }
       if( nrows != target.getNbrOfRows() ) {
    	   do_error("The number of datarows differs from number of results [" + nrows + "] -> [" + target.getNbrOfColumns() + "]");
    	   return null;
       }
       double[] res = new double[ nrows ];
       for(int i=0;i<res.length;i++) {
    	   res[i] = target.getValues()[i][0];
       }
       resultVector = new cmcVector( res );
       //
       do_log( 1 , "Data [" + datam.getNbrOfRows() + "][" + datam.getNbrOfColumns() + "]");
       do_log( 1 , "Res  [" + target.getNbrOfRows() + "][" + target.getNbrOfColumns() + "]"); // + resultVector.showRound() );
       //
       return dataVectorList;
    }
 
    //------------------------------------------------------------
    public String[] getClassNames(ARFFCategory[] categories)
    //------------------------------------------------------------
    {
    	//
        String[] ClassNames = null;
        for(int i=0 ; i<categories.length ; i++)
        {
        	ARFFCategory cat = categories[i];
        	if( cat == null ) continue;
        	
        	if( cat.getTipe() == cmcARFF.ARFF_TYPE.CLASS ) { // class
        		if( i != (categories.length-1) ) {
        			do_error( "CLASS not on last column");
        			return null;
        		}
        		ClassNames = cat.getNominalValueList();
        		if( ClassNames == null ) { do_error("System Error - NULL ClassNames"); return null; }
        	}	
        } 
        return ClassNames;
    }
}
