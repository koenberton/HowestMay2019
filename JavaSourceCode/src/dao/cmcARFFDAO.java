package dao;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.StringTokenizer;

import cbrTekStraktorModel.cmcProcSettings;
import generalMachineLearning.cmcMachineLearningConstants;
import generalMachineLearning.ARFF.cmcARFF;
import generalMachineLearning.ARFF.cmcARFF.ARFFattribute;
import generalpurpose.gpPrintStream;
import logger.logLiason;

public class cmcARFFDAO {

	cmcProcSettings xMSet=null;
	logLiason logger=null;
	
	private static int MAX_NBR_OF_RECORDS = 5000;
	
	enum GRANULARITY { UNIQUE , DUPLICATE , COLLISION }
	
	private boolean ALLOW_DUPLICATES = true;   // it seems that when there are duplicates the model is more accurate. weight factor
	private boolean DEBUG = false;
	
	private int DAOnattribs=-1;
	private int DAOndatarows=-1;
	private int NbrOfDuplicateRows=0;
	private int NbrOfCollisions=0;
	private int firstNonString = -1;
	private String RelationName=null;
	private ArrayList<String> probelist = null;
	private int MaxNumberOfClassItems = -1;
	private String LastErrorMsg = null;
	private int OverruleNumberOfBins = cmcMachineLearningConstants.NBR_BINS;    //  %NUMER_OF_BINS = nn  can be defined as a hint in the ARFF file
	
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
    public cmcARFFDAO(cmcProcSettings is,logLiason ilog )
    //------------------------------------------------------------
    {
    	xMSet = is;
		logger=ilog;
    }
    
    public int getNumberOfDataRows()
    {
     return DAOndatarows;
    }
    public int getNumberOfAttributes()
    {
     return DAOnattribs;
    }
    public ArrayList<String> getProbeList()
    {
     return probelist;
    }
    public String getRelationName()
    {
     return RelationName;
    }
    public int getMaxNumberOfItemsOnClassNames()
    {
     return MaxNumberOfClassItems;
    }
    public String getLastErrorMsg()
    {
    	return LastErrorMsg;
    }
    public int getOverruleNumberOfBins()
    {
    	return OverruleNumberOfBins;
    }
    public int getFirstNonString()
    {
    	return ( firstNonString < 0) ? 0 : firstNonString;
    }
    
    //------------------------------------------------------------
    private void doOverruleNbrOfBins(String sLine)
    //------------------------------------------------------------
    {
       String s = xMSet.xU.getField( sLine.trim(), 2 , "=");
  	   if( s == null ) return;
  	   int zz = xMSet.xU.NaarInt(s.trim());
  	   if( zz > 0 ) {
  		   OverruleNumberOfBins = zz;
  		   do_log( 1 , "Number of bins overruled to [" + OverruleNumberOfBins + "]");
  	   }
    }
    
    //------------------------------------------------------------
    public boolean performFirstPass(String FName)
    //------------------------------------------------------------
    {
    	DAOnattribs=-1;
    	DAOndatarows=-1;
    	NbrOfDuplicateRows=0;
    	NbrOfCollisions=0;
    	MaxNumberOfClassItems =-1;
    	OverruleNumberOfBins=-1;
    	if( xMSet.xU.IsBestand( FName ) == false ) {
    		do_error("Cannot locate ARFF file [" + FName + "]");
    		return false;
    	}
    	DAOnattribs=0;
    	DAOndatarows=0;
    	firstNonString = -1;
    	probelist = new ArrayList<String>();
    	boolean indata=false;
    	BufferedReader reader=null;
    	try {
			File inFile  = new File(FName);  // File to read from.
	        reader = new BufferedReader(new InputStreamReader(new FileInputStream(inFile), xMSet.getCodePageString()));
	       	//
	       	String sLijn=null;
	       	while ((sLijn=reader.readLine()) != null) {
	           String sl = sLijn.toUpperCase().trim();
	           if( sl.length() == 0 ) continue;
	           if( sl.startsWith("@RELATION ") ) {
	        	   RelationName = xMSet.xU.getField( sLijn.trim(), 2 , " \t");
	           }
	           if( (sl.startsWith("@ATTRIBUTE ")) || (sl.startsWith("@ATTRIBUTE\t")) ) {
	        	   DAOnattribs++;
	        	   String s = xMSet.xU.getField( sLijn.trim(), 2 , " \t");
	        	   if( s != null ) {
	        		   probelist.add( s );
	        		   //
	        		   if( (sl.indexOf("STRING") < 0) && (firstNonString == -1) ) firstNonString = (DAOnattribs-1);
	        		   // if a ClassNameList count the classes
	        		   if( sl.indexOf("{") < 0 ) continue;
	        		   if( sl.indexOf("}") < 0 ) continue;
	        		   int zz = xMSet.xU.TelDelims( sl , ',' );
	        		   if (zz > 0) zz++;
	        		   if( zz > MaxNumberOfClassItems ) MaxNumberOfClassItems = zz;	   
	        	   }
	           }
	           if( sl.startsWith("@DATA") ) { indata=true; continue; }
	           if( sl.startsWith("%%") ) continue;  // collisions
	           if( sl.startsWith("%NUMBER_OF_BINS") ) {
	        	   doOverruleNbrOfBins(sl);
	        	   continue;
	           }
	           if( (sl.startsWith("%")) && (ALLOW_DUPLICATES==false)) continue;
	           if( indata ) DAOndatarows++;
	       	}
    	}
    	catch (Exception e) {
    		do_error("Could not read ARFF [" + FName + "]");
    		return false;
    	}
    	finally {
    		try {
    		reader.close();
    		}
    		catch(Exception e ) {
    			do_error("Could not close ARFF [" + FName + "]");
        		return false;
    		}
    	}
    	if( DAOnattribs <= 0 ) { do_error("Could not find attributes in ARFF [" + FName + "]"); return false; }
    	if( DAOndatarows <= 0 ) { do_error("Could not find data in ARFF [" + FName + "]"); return false; }
    	return true;
    }
    
    //------------------------------------------------------------
    private String dumpStructure(cmcARFF xa)
    //------------------------------------------------------------
    {
       String sl = xMSet.xU.ctEOL;
       if( xa != null ) {
    	   for(int i=0;i<xa.getAttributeList().length;i++)
    	   {
    		   ARFFattribute attr = xa.getAttributeList()[i];
    		   sl += "(" + (i+1) + ") " + attr.getAttrName() + " " + attr.getTipe() + xMSet.xU.ctEOL;
    	   }
       }
       return sl;	
    }
    
    //------------------------------------------------------------
    public cmcARFF performSecondPass(String FName)
    //------------------------------------------------------------
    {
    	NbrOfDuplicateRows=0;
    	NbrOfCollisions=0;
    	cmcARFF xa = null;
     	// init
    	try {
    	  xa = new cmcARFF( FName , DAOnattribs , DAOndatarows);
    	  xa.setLongFileName(FName);
    	  xa.setNbrOfLines(DAOndatarows);
    	}
    	catch(Exception e ) {
    		do_error("Initializing ARFF buffer");
    		e.printStackTrace();
    		return null;
    	}
    	int attribCount=-1;
    	int rowCount=-1;
    	//
    	boolean indata=false;
        int progress=0;
    	BufferedReader reader=null;
    	try {
    	 	File inFile  = new File(FName);  // File to read from.
	        reader = new BufferedReader(new InputStreamReader(new FileInputStream(inFile), xMSet.getCodePageString()));
	       	//
	       	String sLijn=null;
	       	while ((sLijn=reader.readLine()) != null) {
	       	   progress++;
	       	   if( (progress % 1000) == 999) do_log( 5 , "Line [" + progress + "]");
	       	   if( progress >= MAX_NBR_OF_RECORDS) {
	       		   do_error( "Maximum number of records has been reached [" + MAX_NBR_OF_RECORDS + "]");
	       		   return null;
	       	   }
	           String sl = sLijn.toUpperCase().trim();
	           if( sl.length() == 0 ) continue;
	           
	           if( sl.startsWith("%NUMBER_OF_BINS") ) {
	        	   doOverruleNbrOfBins(sl);
	        	   xa.setOverruleNbrOfBins(OverruleNumberOfBins);
	        	   continue;
	           }
	           
	           if( sl.startsWith("@RELATION") ) {
	        	   try {
	        	     xa.setARFFName (sLijn.substring("@RELATION".length()).trim());
	        	   }
	        	   catch(Exception e ) {
	        	  		 do_error("oeps3");
		        		 e.printStackTrace();
		        		 return null;
		    	   }
	           }
	           //
	           if( sl.startsWith("@") ) {
	        	   boolean supp=false;
	        	   if( sl.startsWith("@RELATION") ) supp=true;
	        	   if( sl.startsWith("@ATTRIBUTE") ) supp=true;
	        	   if( sl.startsWith("@DATA") ) supp=true;
	        	   if( supp == false ) {
	        		   do_error( "Unsupported command [" + sl + "]");
	        		   return null;
	        	   }
	           }
	           //
	           if( sl.startsWith("@ATTRIBUTE ") || (sl.startsWith("@ATTRIBUTE\t")) ) {
	        	 try {   
	        	   attribCount++;
	        	   if( attribCount >= xa.getAttributeList().length ) {
	        		   do_error("Too many attributes expect [" + attribCount + "]");
	        		   return null;		
	        	   }
	        	   //do_log(1,sLijn);
	        	   StringTokenizer st = new StringTokenizer(sLijn," \t");
	        	   int idx=0;
	        	   while(st.hasMoreTokens()){
	        		 String sElem = st.nextToken().trim();
	        		 if( sElem.compareToIgnoreCase("@attribute") == 0) { idx =1; continue; }
	        		 if( idx == 1 ) { xa.getAttributeList()[attribCount].setAttrName( sElem ); idx++; continue; }
	        		 if( idx == 2 ) {
	        		   if( sElem.compareToIgnoreCase("NUMERIC") == 0 )  xa.getAttributeList()[attribCount].setTipe(cmcARFF.ARFF_TYPE.NUMERIC);
	        		   else
	        		   if( sElem.compareToIgnoreCase("STRING") == 0 )  xa.getAttributeList()[attribCount].setTipe(cmcARFF.ARFF_TYPE.STRING);
	        		   else
	        		   if( (sElem.startsWith("{")) && (sElem.endsWith("}")) ) {
	        			  xa.getAttributeList()[attribCount].setTipe( cmcARFF.ARFF_TYPE.CLASS );
	        			  String sval = sElem.toUpperCase().trim();
	        			  if( sval.length() <= 2) {
	        				  do_error("No class values specified");
	        				  return null;
	        			  }
	        			  sval = sval.substring( 1 , sval.length() - 1 );
	        			  xa.getAttributeList()[attribCount].setClassesDefinition(sval);
	        			  int nvals = xMSet.xU.TelDelims( sval , ',') + 1;
	        			  if( nvals <= 1 ) {
	        				  do_error("There are no commas between the classvals or there is only 1 class [" + sval + "]");
	        				  return null;
	        			  }
	        			  String[] cats = new String[nvals];
	        			  StringTokenizer sx = new StringTokenizer(sval,",");
	        			  int pdx=-1;
	   	        	      while(sx.hasMoreTokens()){
	   	        	    	pdx++;
	   	        	    	cats[pdx] = sx.nextToken().trim().toUpperCase();
	   	        	      }
	        			  xa.getAttributeList()[attribCount].setClasses(cats);
	        		   }
	        		   else {
	        		    do_error("Unknown type [" + sElem + "]");
	        		    return null;
	        		   }
	        		  }
	        	   }
	        	 }
	        	 catch(Exception e ) {
	        		 do_error("oeps");
	        		 e.printStackTrace();
	        		 return null;
	        	 }
	           }
	           //
	           if( sl.startsWith("@DATA") ) {
	        	 //try {
	        	   if( attribCount != (DAOnattribs-1) ) {
	        		   do_error( dumpStructure(xa) );
	        		   do_error("Number of attributes does not match");
	        		   return null;
	        	   }
	        	   indata=true; 
	        	   continue; 
	           }
	           if( indata ) {
	        	 if( sl.startsWith("%%") ) continue;
	        	 if( (sl.startsWith("%")) && (ALLOW_DUPLICATES==false) ) continue;
	        	 try {
	        	   rowCount++;
	        	   if( rowCount >= DAOndatarows ) {
	        		   do_error( dumpStructure(xa) );
	        		   do_error("Too many rows. Expect [" + DAOndatarows + "] got [" + rowCount + "]");
	        		   return null;	
	        	   }
	        	   // read each value - there must be exactly nattribs
	        	   StringTokenizer st = new StringTokenizer(sLijn,",");
	        	   int idx=-1;
	        	   while(st.hasMoreTokens()){
	        		   String sElem = st.nextToken().trim();
	        		   idx++;
	        	       if( idx >= DAOnattribs ) {
	        	    	   do_error( dumpStructure(xa) );
	        	    	   do_error("Too many values on row [ " + rowCount + "] [" + sLijn + "]");
	        	    	   return null;
	        	       }
	        	       // store the value
	        	       if( xa.getAttributeList()[ idx ].getTipe() == cmcARFF.ARFF_TYPE.STRING ) {
	        		     xa.getAttributeList()[ idx ].setSValue( rowCount , sElem.trim());
	        		   }
	        	       else
	        	       if( xa.getAttributeList()[ idx ].getTipe() == cmcARFF.ARFF_TYPE.NUMERIC ) {
	        		     xa.getAttributeList()[ idx ].setValue( rowCount , xMSet.xU.NaarDouble( sElem ));
	        		     if( xa.getAttributeList()[ idx ].getValue( rowCount ) == -1 ) {
	        			   do_log(1,"Something went wrong" + sElem + " on " + sLijn );
	        		     }
	        	       }
	        	       // find and store the index of the class/category
	        	       else if( xa.getAttributeList()[ idx].getTipe() == cmcARFF.ARFF_TYPE.CLASS ) {
	        	    	     int pdx=-1;
	        	    	     for(int j=0;j<xa.getAttributeList()[idx].getClasses().length;j++)
	        	    	     {
	        	    	    	 if( sElem.compareToIgnoreCase( xa.getAttributeList()[idx].getClasses()[j] ) == 0 ) { pdx=j; break; }
	        	    	     }
	        	    	     if( pdx < 0 ) {
	        	    	    	 do_error( dumpStructure(xa) );
	        	    	    	 do_error("Could not map [" + sElem + "] to the values in class {" + xa.getAttributeList()[idx].getClassesDefinition() + "}  [Row=" + rowCount + "]");
	        	    	    	 return null;
	        	    	     }
	        	    	     xa.getAttributeList()[ idx ].setValue( rowCount , (double)pdx );
	        	    	     xa.getAttributeList()[ idx ].setSValue( rowCount , xa.getAttributeList()[idx].getClasses()[pdx] );
		     	       }
	        	   }
	        	   idx++;
	        	   if( idx != DAOnattribs ) {
	        		   do_error( dumpStructure(xa) );
	        		   do_error("Too few values on row [ " + rowCount + "] [Txt=" + sLijn + "] [idx=" + idx +"] [attr=" + DAOnattribs +"]");
        	    	   return null;
	        	   }
	        	 }
	        	 catch(Exception e ) {
	          		 do_error("oeps2");
	        		 e.printStackTrace();
	        		 return null;
	             }
	           }
	           	           
	       	} // while
	    
    	}
    	catch (Exception e) {
    		do_error("Could not read ARFF [" + FName + "]");
    		e.printStackTrace();
    		return null;
    	}
    	finally {
    		try {
    		reader.close();
    		}
    		catch(Exception e ) {
    			do_error("Could not close ARFF [" + FName + "]");
        		return null;
    		}
    	}
    	// Post Checks
    	if( xa != null ) {
    		 if( xa.getAttributeList().length < 1 ) {
    			do_error("There are zero or one columns in the ARFF [" + FName + "]");
    			return null;
    		 }
    		 cmcARFF.ARFFattribute xcat = xa.getAttributeList()[xa.getAttributeList().length - 1];
    		 if( xcat.getTipe() != cmcARFF.ARFF_TYPE.CLASS ) {
    	    		do_error( "Last column in data is not a CLASS");
    	    		return null;
    	     }
    		 // Trick => all classes except the last one are set to NOMINAL
    		 for(int i=0 ; i<(xa.getAttributeList().length-1) ; i++ )
    		 {
    			xcat = xa.getAttributeList()[i];
    		    if( xcat.getTipe() == cmcARFF.ARFF_TYPE.CLASS ) { xcat.setTipe(cmcARFF.ARFF_TYPE.NOMINAL ); }
    		 }
    	}
    	//
    	if( identifyDuplicates(xa) == null) ; //return null;
    	//
     	do_log( 1 , "ARFF loaded correctly. There are [" + xa.getAttributeList().length + "] attributes. [Collisions=" + NbrOfCollisions + "] [Duplicates=" + NbrOfDuplicateRows + "] [" + FName + "]");
    	return xa;
    }
    
    //------------------------------------------------------------
    public void show(cmcARFF xa)
    //------------------------------------------------------------
    {
    	do_log(1,xa.getARFFName());
    	for(int i=0; i < xa.getAttributeList().length ; i++ )
    	{
    		 cmcARFF.ARFFattribute atr = xa.getAttributeList()[i];
    		 do_log( 1 , atr.getAttrName() + " " + atr.getTipe() );
    		 if( atr.getTipe() == cmcARFF.ARFF_TYPE.CLASS ) {
    			String sl="";
    			for(int j=0;j<atr.getClasses().length;j++) sl += "[" + atr.getClasses()[j] + "]";
    			do_log( 1 , "-->" + sl);
    		 }
    		 if( atr.getTipe() != cmcARFF.ARFF_TYPE.STRING ) {
    		  String svl="";
    		  int max = atr.getValues().length < 50 ? atr.getValues().length : 50;
    		  for(int j=0;j<max  ; j++) svl += atr.getValues()[j] + ",";
    		  do_log( 1 , "ATTR:" + i + "  " + svl );
    		 }
    	}
    }
 
    //------------------------------------------------------------
    private int getValidRows( cmcARFF xa )
    //------------------------------------------------------------
    {
    	if( xa == null ) return -1;
    	if( xa.getAttributeList() == null ) return -1;
    	if( xa.getAttributeList().length <= 0 ) return -1;
        int valid = 0;
        for(int i=0; i< xa.getAttributeList()[0].getValues().length ; i++)
        {
        	if( xa.getAttributeList()[0].getKeep(i) ) valid++;
        }
        return valid;
    }
    
   
    //------------------------------------------------------------
    public boolean writeARFFFile( cmcARFF xa , String LongFileName , boolean dedupe  )
    //------------------------------------------------------------
    {
    	gpPrintStream proto = new gpPrintStream( LongFileName , "ASCII");
    	
    	GRANULARITY[] zap = new GRANULARITY[xa.getAttributeList()[0].getValues().length];
    	for(int i=0;i<zap.length;i++) zap[i]=GRANULARITY.UNIQUE;
    	if( dedupe ) {
    		zap = identifyDuplicates( xa );
    		if( DEBUG ) {
    		 for(int i=0;i<zap.length;i++) { if( zap[i] != GRANULARITY.UNIQUE ) do_log( 1 , "Row [" + i + "] [" + zap[i] + "]"); }
    		}
    	}
    	
        try {
        	proto.println("% " + xMSet.getApplicDesc() );
            proto.println("% Generated on " + (xMSet.xU.prntStandardDateTime(System.currentTimeMillis())).toUpperCase() );
            proto.println("% Number of Datarows : " + xa.getAttributeList()[0].getValues().length );
            proto.println("% Number of valid Datarows : " + getValidRows( xa ) );
            proto.println("");
            if( xa.getOverruleNbrOfBins() > 0 ) {
              proto.println("%NUMBER_OF_BINS = " + xa.getOverruleNbrOfBins() );
              proto.println("");
            }
            
    	
    	proto.println("@relation " + xa.getARFFName() );
    	//
    	
    	//
    	proto.println("");
    	for(int i=0;i<xa.getAttributeList().length;i++)
    	{
    	  String sl = "";
    	  if( (xa.getAttributeList()[i].getTipe() == cmcARFF.ARFF_TYPE.CLASS) || (xa.getAttributeList()[i].getTipe() == cmcARFF.ARFF_TYPE.NOMINAL) ) {
    		 for( int j=0; j <xa.getAttributeList()[i].getClasses().length ; j++) {
    			if( j != 0 ) sl += ",";
    			sl +=  xa.getAttributeList()[i].getClasses()[j];
    		 }
    		 sl = "{" + sl + "}";
    	  }
    	  else sl = ""+xa.getAttributeList()[i].getTipe();
    	  //
    	  proto.println("@attribute " + xa.getAttributeList()[i].getAttrName() + " " + sl );
    	}
    	//
    	proto.println("");
    	proto.println("@data");
    	for(int i=0;i<xa.getAttributeList()[0].getValues().length;i++)  // attribute 0 has same number of rows as any other
    	{
    		if( xa.getAttributeList()[0].getKeep(i) == false ) continue; // skip line
    		String sl = "";
    		if( zap[i] == GRANULARITY.COLLISION ) sl = "%% - COLLISION - ";
    		if( zap[i] == GRANULARITY.DUPLICATE ) sl = "%";
    		for(int j=0 ; j < xa.getAttributeList().length ; j++ )
    		{
    		  if( j != 0 ) sl += ",";
    		  if( xa.getAttributeList()[j].getTipe() == cmcARFF.ARFF_TYPE.STRING ) sl += xa.getAttributeList()[j].getSValue(i);
    		  else
    		  if( xa.getAttributeList()[j].getTipe() == cmcARFF.ARFF_TYPE.NUMERIC ) sl += xa.getAttributeList()[j].getValue(i);	
    		  else
    		  if( xa.getAttributeList()[j].getTipe() == cmcARFF.ARFF_TYPE.NOMINAL ) sl += xa.getAttributeList()[j].getSValue(i);
    		  else
        	  if( xa.getAttributeList()[j].getTipe() == cmcARFF.ARFF_TYPE.CLASS ) sl += xa.getAttributeList()[j].getSValue(i);
        	  else {
    			do_error( "Unknown tipe [" + xa.getAttributeList()[j].getTipe() + "]");
    			return false;
    		  }
    		}
    		proto.println( sl );
    	}
    	//
    	proto.close();
    	}
    	catch(Exception e ) {
    		e.printStackTrace();
    		return false;
    	}
    	return true;
    }
    
    // aanpassen zodat die een boolean[] teruggeeft met true op de dups
    //------------------------------------------------------------
    private GRANULARITY[] identifyDuplicates ( cmcARFF xa )
    //------------------------------------------------------------
    {
    	if( xa == null )  { do_error( "ARFF is null"); return null; }
    	int cnt = 0;
    	int idx = -1;
    	int nattribs = xa.getAttributeList().length;
    	for(int i=0;i<nattribs;i++)  
    	{
  //do_log( 1 , "" + xa.getAttributeList()[i].getTipe()  + " " + xa.getAttributeList()[i].getAttrName());
           if( xa.getAttributeList()[i].getTipe() == cmcARFF.ARFF_TYPE.STRING )  continue;
           if( xa.getAttributeList()[i].getTipe() == cmcARFF.ARFF_TYPE.CLASS )  continue;
           cnt++;
           if( idx < 0 ) idx = i;     
    	}
    	if( cnt == 0 ) {
    		do_error("(identifyDuplicates) There are no numeric or nomimal attributes in the ARFF file [" + xa.getARFFName() + "]");
    		return null;
    	}
        // loop through all rows and look for duplicates
    	cmcARFF.ARFFattribute pivot = xa.getAttributeList()[idx];
    	int ndatarows = pivot.getValues().length;
    	boolean[] duper = new boolean[ ndatarows ];
    	boolean dupes=false;
    	boolean superduper[][] = new boolean[ndatarows][ndatarows];
    	for( int row=0;row<ndatarows; row++)
    	{
    		for(int i=0;i<ndatarows;i++)
    		{
    			duper[i] = true;
    			if( i == row ) { duper[i] = false; continue; }
    			for(int j=0;j<nattribs;j++)
    			{
    			  cmcARFF.ARFFattribute x = xa.getAttributeList()[j];
    			  if( x.getTipe() == cmcARFF.ARFF_TYPE.STRING )  continue;
    			  if( x.getTipe() == cmcARFF.ARFF_TYPE.CLASS )  continue;
    	          if( x.getTipe() == cmcARFF.ARFF_TYPE.NUMERIC )  {
    	          if( xa.getAttributeList()[j].getValue(row) != xa.getAttributeList()[j].getValue(i) ) duper[i] = false;
    	          } 
    	          if( x.getTipe() == cmcARFF.ARFF_TYPE.NOMINAL )  {
        	          if( xa.getAttributeList()[j].getValue(row) != xa.getAttributeList()[j].getValue(i) ) duper[i] = false;
        	      } 
    			}  
    		}
    		for(int i=0;i<ndatarows;i++)
    		{
    			superduper[row][i] = duper[i];
    			if( duper[i] ) dupes = true;
   		    }
    	}
    	//
    	int classidx = nattribs-1;
    	if(  xa.getAttributeList()[classidx].getTipe() != cmcARFF.ARFF_TYPE.CLASS ) {
    		do_error("Last category is not a CLASS");
    		return null;
    	}
    	//
    	NbrOfDuplicateRows=0;
    	NbrOfCollisions=0;
    	GRANULARITY[] granul = new GRANULARITY[ ndatarows ];
    	for( int i=0;i<granul.length;i++) granul[i] = GRANULARITY.UNIQUE;
    	if( dupes ) {
    		for(int i=0;i<ndatarows;i++)
    		{
    			String sl = "Duplicates/collisions [Row=" + i + "] - ";
    			boolean dd=false;
    			for(int j=i;j<ndatarows;j++)   // symmetrical
    			{
    				if( superduper[i][j] ) {
    					sl += "[" + j + "]"; 
    					dd = true; 
    					NbrOfDuplicateRows++; 
    					String one = xa.getAttributeList()[classidx].getSValue(i);
    					String two = xa.getAttributeList()[classidx].getSValue(j);
    					if( one.compareToIgnoreCase(two)==0 ) {
    						granul[ j ] =  GRANULARITY.DUPLICATE;
    						NbrOfDuplicateRows++;
    						sl += " DUPLICATE";
    					}
    					else {
    						granul[ j ] =  GRANULARITY.COLLISION;
    						NbrOfCollisions++;
    						sl += " COLLISION";
    						do_error( sl );
    					}
    			   }
    			}
    			if( dd && DEBUG ) do_log( 1 , sl );
    		}
    		//return false;  //?
    	}
    	xa.setNbrOfDuplicateLines(NbrOfDuplicateRows);
    	xa.setNbrOfCollissions(NbrOfCollisions);
    	return granul;
    }
}
