package generalMachineLearning.ARFF;

import java.util.ArrayList;
import java.util.Random;

import cbrTekStraktorModel.cmcProcSettings;
import dao.cmcARFFDAO;
import generalMachineLearning.cmcMachineLearningConstants;
import generalMachineLearning.ARFF.cmcARFF.ARFFattribute;
import logger.logLiason;

public class cmcARFFRoutines {
	cmcProcSettings xMSet=null;
	logLiason logger=null;
	
	
	
	static boolean RANDOMIZE = true;
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

    //------------------------------------------------------------
    public cmcARFFRoutines(cmcProcSettings is,logLiason ilog)
    //------------------------------------------------------------
    {
    	xMSet = is;
		logger=ilog;
    }

    public String getLastErrorMsg()
    {
    	return LastErrorMsg;
    }
    //------------------------------------------------------------
    public boolean getRandomize()
    //------------------------------------------------------------
    {
    	return RANDOMIZE;
    }
    
    //-----------------------------------------------------------
    public int getIndexFirstNumericCategory(cmcARFF xa )
    //------------------------------------------------------------
    {
     	int idx = -1;
    	int ndatarows=-1;
    	int nattributes=0;
    	for(int i=0;i<xa.getAttributeList().length;i++)
    	{
    		ARFFattribute attr = xa.getAttributeList()[i];
    		if( (attr.getTipe() != cmcARFF.ARFF_TYPE.NUMERIC) && (attr.getTipe() != cmcARFF.ARFF_TYPE.NOMINAL) ) continue;
    	    if( ndatarows == -1 ) {
    	    	    ndatarows = (int)attr.getValues().length;
    	    		idx = i;
    	    }
    	    else {
    	    		if( ndatarows != attr.getValues().length ) {
    	    			do_error("Number of rows is differing between attributes [" + ndatarows + "] [" + attr.getValues().length + "]");
    	    			return -1;
    	    		}
    	    }
    	    nattributes++;
    	}
    	if( ndatarows < 0 ) {
    		do_error("There are no numeric or nominal lines in ARFF");
    		return -1;
    	}
        return idx; 	
    }
    
    
    //------------------------------------------------------------
    public boolean splitARFFInTrainingAndTest( cmcARFF xa , String FileNameTrainingSet , String FileNameTestSet )
    //------------------------------------------------------------
    {
        if( xa == null ) {
        	do_error("got a empty XA");
        	return false;
        }
    	if( xMSet.xU.IsBestand( FileNameTrainingSet ) ) {
    		xMSet.xU.VerwijderBestand( FileNameTrainingSet );
    		if( xMSet.xU.IsBestand( FileNameTrainingSet ) ) {
    			do_error("Could not remove [" + FileNameTrainingSet + "]");
    			return false;
    		} 			
    	}
    	if( xMSet.xU.IsBestand( FileNameTestSet ) ) {
    		xMSet.xU.VerwijderBestand( FileNameTestSet );
    		if( xMSet.xU.IsBestand( FileNameTestSet ) ) {
    			do_error("Could not remove [" + FileNameTestSet + "]");
    			return false;
    		} 			
    	}
    	//
     	int idx = getIndexFirstNumericCategory( xa );
    	if( idx < 0 ) return false;
    	int ndatarows = xa.getAttributeList()[idx].getValues().length;
    	int[] keeplist = new int[ ndatarows ];
        for(int i=0;i<keeplist.length;i++) keeplist[i] = -1;
    	// pick random values
        if( RANDOMIZE ) {
          int count=0;
          boolean completed=false;
          int threshold = (int)(((double)ndatarows * (double)cmcMachineLearningConstants.TRAININGSET_PERCENTAGE ) / (double)100);
          for(int i=0;i< (ndatarows*10) ;i++)
          {
        	Random rn = new Random();
        	int ret = rn.nextInt(ndatarows);
        	if( keeplist[ret] == -1 ) { 
        		keeplist[ret] = 0; 
        		count++;
        		if( count > threshold ) { completed = true; break; }
        	}
          }
          if( completed == false ) {
        	do_error("Strange - Could not allocat enough random records");
        	return false;
          }
        }
        else {   // Always pick the same record
            int threshold = (int)(((double)ndatarows * (double)cmcMachineLearningConstants.TRAININGSET_PERCENTAGE ) / (double)100);
            int spread = (int)((double)ndatarows / (double)threshold);
            for(int i=0;i<threshold;i++) keeplist[ i * spread ] = 0;
        }
        for(int i=0;i<keeplist.length;i++) if( keeplist[i] != 0) keeplist[i]=1;
        //
        String sl="";
        for(int i=0;i<keeplist.length;i++) if( keeplist[i] == 0 ) sl+= "[" + i + "]";
        do_log( 1 , sl );
        //
        cmcARFF trainingset = new cmcARFF( xa.getARFFName() , xa.getAttributeList().length , ndatarows );
        if( trainingset.kloon( xa ) == false ) {
        	do_error("Could not clone training set");
        	return false;
        }
        boolean[] bkeep = new boolean[ keeplist.length ];
        for(int i=0;i<keeplist.length;i++) 
        {
        	bkeep[i] = (keeplist[i] == 1) ? true : false;
        }
        if( trainingset.propagateKeep( bkeep ) == false ) {
        	do_error("Could not set keeplist");
        	return false;
        }
        
        //
        cmcARFF testset = new cmcARFF( xa.getARFFName() , xa.getAttributeList().length , ndatarows );
        if( testset.kloon( xa ) == false ) {
        	do_error("Could not clone test set");
        	return false;
        }
        for(int i=0;i<keeplist.length;i++) 
        {
        	bkeep[i] = !bkeep[i];
        }
        if( testset.propagateKeep( bkeep ) == false ) {
        	do_error("Could not set inverse keeplist");
        	return false;
        }
        
        //
        cmcARFFDAO dao = new cmcARFFDAO(xMSet , logger);
        if( dao.writeARFFFile( trainingset , FileNameTrainingSet , false ) == false ) return false;
        if( dao.writeARFFFile( testset , FileNameTestSet , false ) == false ) return false;
        dao = null;
        
        //
    	return true;
    }
   
    //------------------------------------------------------------
    public ARFFWrapperDTO getCategoriesAndOtherStuff(String OriginalFileName , int NbrOfBins , boolean splitFileRequested )
    //------------------------------------------------------------
    {
    	// first pass : read number of attributes and number of data rows
    	cmcARFFDAO dao = new cmcARFFDAO( xMSet , logger );
    	if( dao.performFirstPass(OriginalFileName) == false )  return null;
    	do_log(1,"ARRF[" + OriginalFileName + "] [Attribs=" + dao.getNumberOfAttributes() +"][Rows=" + dao.getNumberOfDataRows() + "]");
    	
    	// bump the NBR of bins
    	int AdjustedNumberOfBins = NbrOfBins;
    	int MaxNumberOfClassItems = dao.getMaxNumberOfItemsOnClassNames();
    	if( MaxNumberOfClassItems >= NbrOfBins ) {
    		do_log( 1 , "The requested number of bins [" + NbrOfBins + "] is too low. Required [" + MaxNumberOfClassItems + "]");
    		int overrule = dao.getOverruleNumberOfBins();
    		if( overrule <= MaxNumberOfClassItems ) {
    			do_error(  "The requested number of bins [" + NbrOfBins + "] is too low. Required [" + MaxNumberOfClassItems + "].Define %NUMBER_OF_BINS= in ARFF");
    			return null;
    		}
    		AdjustedNumberOfBins = overrule;
    	 	do_log( 1 , "The requested number of bins [" + NbrOfBins + "] has been overrule [" + AdjustedNumberOfBins + "]" );
    	}
    	//do_log( 1 , "The requested number of bins [" + NBR_BINS + "]. Required [" + MaxNumberOfClassItems + "]" );
    	
    	// second pass - read the data from the training set
    	cmcARFF xa = dao.performSecondPass(OriginalFileName);
    	if( xa == null ) return null;
    	
    	// split the ARFF into a training set and test set
    	String NoSuffix = xMSet.xU.getFileNameWithoutSuffix( OriginalFileName );
    	if( NoSuffix == null ) { do_error("Cannot strip suffix"); return null; }
    	String FileNameTrainingSet = NoSuffix + "-TrainSet.arff";
    	String FileNameTestSet = NoSuffix + "-TestSet.arff";
    	String FileNameToBeReRead = null;
    	
    	if( splitFileRequested ) {
    		FileNameToBeReRead = FileNameTrainingSet;
    	    if( splitARFFInTrainingAndTest( xa , FileNameTrainingSet , FileNameTestSet ) == false ) return null;
    	}
    	else {
    		FileNameToBeReRead = OriginalFileName;
    	}
    	//
    	//modstat = MODELING_STATUS.TRAINING;
    	
    	// There is now a trainingset and test set stored in ARFF files
    	// Process the trainingset
    	// first pass : read number of attributes and number of data rows
    	if( dao.performFirstPass(FileNameToBeReRead) == false )  return null;
    	do_log(1,"ARRF[" + FileNameToBeReRead + "] [Attribs=" + dao.getNumberOfAttributes() +"][Rows=" + dao.getNumberOfDataRows() + "]");
    	
    	// second pass - read the data from the training set
    	xa = dao.performSecondPass(FileNameToBeReRead);
    	if( xa == null ) return null;
    	
    	//
    	cmcARFF.ARFFattribute last = xa.getAttributeList()[ xa.getAttributeList().length - 1 ];
    	if( last.getTipe() != cmcARFF.ARFF_TYPE.CLASS ) {
    		do_error("Last column is not CLASS");
    		return null;
    	}
    	int NumberOfClasses = last.getClasses().length;
    	if( NumberOfClasses < 2) {
    		do_error( "Number of classes is too low [" + NumberOfClasses +"] expecting at least 2" );
    		return null;
    	}
    	//
    	String[] ClassNameList = new String[ last.getClasses().length ];
        for(int i=0;i<last.getClasses().length;i++) ClassNameList[i] = last.getClasses()[i];
        //
    	do_log(1,xa.show());
        
        // create the triples :  { value - result - bin }
       	ARFFCategory[] categories = new ARFFCategory[ xa.getAttributeList().length ];
        for(int i=0 ; i < xa.getAttributeList().length ; i++ )
        {
        	cmcARFF.ARFFattribute attr = xa.getAttributeList()[i];
        	ARFFCategory x = new ARFFCategory ( xMSet , logger , attr.getAttrName() , attr.getValues().length , attr.getTipe() , NumberOfClasses , attr.getClasses() , AdjustedNumberOfBins );
        	for( int j=0 ; j<x.values.length ; j++ )
        	{
        		x.values[ j ]   = attr.getValue(j);
        		x.results[ j ]  = last.getValue(j);
        		x.arfflineno[j] = j;
        	}
        	//
        	if( x.performSort() == false ) return null;
        	if( x.performBinning() == false ) return null;
        	//
        	categories[i] = x;
        }
        //
        ARFFWrapperDTO dto = new ARFFWrapperDTO( AdjustedNumberOfBins );
        dto.setClassNameList(ClassNameList);
        dto.setXa( xa );
        dto.setARFFFileName(OriginalFileName);
        dto.setFileNameTrainingSet(FileNameTrainingSet);
        dto.setFileNameTestSet(FileNameTestSet);
        dto.setCategories(categories);
        dto.setValid(true);
        return dto;
    }
    
    //------------------------------------------------------------
  	public void purgeFolders()
  	//------------------------------------------------------------
  	{
  		ArrayList<String> removelist = new ArrayList<String>();
  		for(int k=0;k<2;k++)
  		{
  		 String DirName = "";	
  		 if( k == 0) DirName = 	xMSet.getSandBoxDir();
  		 if( k == 1) DirName = 	xMSet.getBayesResultDir();
  		 ArrayList<String> list1 = xMSet.xU.GetFilesInDir( DirName , null );
  		 for(int i=0;i<list1.size();i++)
  		 {
  		 	String FName = list1.get(i);
  			if( FName == null ) continue;
  			if( (FName.toUpperCase().indexOf("TESTSET")>=0) || (FName.toUpperCase().indexOf("TRAINSET")>=0) ) {
  				String LongName = DirName + xMSet.xU.ctSlash + FName;
  				removelist.add( LongName ); 
  			}
  		 }
  		}
  	    for(int i=0;i<removelist.size();i++)
  	    {
  	    	do_log(1,"remove [" + removelist.get(i) );
  	    	xMSet.xU.VerwijderBestand( removelist.get(i) );
  	    }
  	}
}
