package featureExtraction;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Random;
import java.util.StringTokenizer;

import cbrTekStraktorModel.cmcProcSettings;
import dao.cmcARFFDAO;
import generalImagePurpose.gpLoadImageInBuffer;
import generalMachineLearning.ARFF.cmcARFF;
import generalpurpose.gpPrintStream;
import logger.logLiason;

public class cmcVRExtractMLAndHOGFeatureSet {

	private boolean RANDOM_PROJECTION = false;
	
	enum PROCESSTYPE { STORE_MEAN , RANDOM_PROJECTION }
	enum DUMPTYPE  { WEKA }
	
	cmcProcSettings xMSet=null;
	logLiason logger=null;
	
	
	private static int VALUES_PER_VECTOR = 9 * 4;
	private static int REDUCTOR          = 20;
	private static int  MAX_REDUCED_COLS = 400;
	
	
	cmcHOGRoutines hogger = null;
	gpLoadImageInBuffer imgloader = null;
	gpPrintStream pout = null;
	private String CurrLabel = null;
	private int NumberOfVectorsPerHOG = -1;
	
	class FileType {
	   String FileName;
	   String label;
	   FileType( String s , String p)
	   {
		   FileName = s;
		   label = p;
	   }
	}
	ArrayList<FileType> filetypelist = null;
	
	class HOGVectorLine {
		int fileindex=-1;
		double[] buf;
		double mean;
		int clusteridx;
		HOGVectorLine(int filei)
		{
		  fileindex=filei;
		  mean=-1;
		  clusteridx=-1;
		  buf=null;
		}
	}
	HOGVectorLine[] ar_hogvectorlines = null;
	
	
	ArrayList<cmcFeatureCollection> arfflist = null;
	
	
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
    	xMSet.setOCRSummaryResult(sIn);
    }
    
    //------------------------------------------------------------
    public cmcVRExtractMLAndHOGFeatureSet(cmcProcSettings is,logLiason ilog)
    //------------------------------------------------------------
    {
    	xMSet = is;
		logger=ilog;
		hogger = new cmcHOGRoutines(xMSet,logger);
		imgloader = new  gpLoadImageInBuffer( xMSet.xU , logger );
    }
    
    //------------------------------------------------------------
    private boolean clearXMLs( String FolderName )
    //------------------------------------------------------------
    {
    	ArrayList<String> xmllist = xMSet.xU.GetFilesInDir( FolderName , null );
    	for(int i=0;i<xmllist.size();i++)
    	{
    	   String XMLFileName = FolderName + xMSet.xU.ctSlash + xmllist.get(i);
    	   if( XMLFileName.trim().toUpperCase().endsWith(".XML") ) {
    		   if( xMSet.xU.VerwijderBestand( XMLFileName ) == false ) return false;
    	   }
    	}
    	return true;
    }
    
    //------------------------------------------------------------
    private boolean makeFileTypeList(String Folder , String tipe)
    //------------------------------------------------------------
    {
    	if( filetypelist == null ) filetypelist = new ArrayList<FileType>();
    	ArrayList<String> list = xMSet.xU.GetFilesInDir( Folder , null );
    	for(int i=0;i<list.size();i++)
    	{
    		String NoSuffix = list.get(i);
    		int idx = NoSuffix.toUpperCase().lastIndexOf(".JPG");
    		if( idx < 0 ) continue;
    		NoSuffix = NoSuffix.substring(0,idx);
    		//do_log( 9 , " -> " + NoSuffix + " " + list.get(i));
    		FileType x = new FileType( NoSuffix , tipe );
    		filetypelist.add(x);
    	}
    	return true;
    }
    
    //------------------------------------------------------------
    private String getLabel(String FName)
    //------------------------------------------------------------
    {
    	try {
    	 if( FName == null ) return null;
    	 int idx = FName.trim().toUpperCase().lastIndexOf("_HOG.XML");
    	 if( idx < 0 )  return null;
    	 String Look = FName.substring(0,idx);
    	 for(int i=0;i<filetypelist.size();i++)
    	 {
    		if( Look.compareToIgnoreCase( filetypelist.get(i).FileName ) == 0 ) return filetypelist.get(i).label;
    	 }
    	 do_error("Not found " + FName + " " + Look);
    	 return null;
    	}
    	catch(Exception e ) {  return null; }
    }
    
    //------------------------------------------------------------
    private boolean doPrereqs()
    //------------------------------------------------------------
    {
    	// check prereqs
    	xMSet.setOCRSummaryResult(null);
    	for(int i=0;i<10;i++)
    	{
    		String CheckDir = null;
    		switch( i )
    		{
    		case 0 : { CheckDir = xMSet.getBayesDir(); break; }
    		case 1 : { CheckDir = xMSet.getBayesResultDir(); break; }
    		case 2 : { CheckDir = xMSet.getHOGValidDir(); break; }
    		case 3 : { CheckDir = xMSet.getHOGInvalidDir(); break; }
    		case 4 : { CheckDir = xMSet.getBayesTrainingDirText(); break; }
    		case 5 : { CheckDir = xMSet.getBayesTrainingDirGraph(); break; }
    		case 6 : { CheckDir = xMSet.getBayesTrainingDirMixed(); break; }
    		default : break;
    		}
    		if( CheckDir == null ) continue;
    		if( xMSet.xU.IsDir( CheckDir ) == false ) {
        		do_error("Cannot access folder [" + CheckDir + "]");
        		return false;
        	}
    	}
    	return true;
    }
    
    // -------------------------------------
    //   NIEUW  -  ENTRY POINT verification
    //------------------------------------------------------------
    public boolean extractImageFeatures()
    //------------------------------------------------------------
    {
    	if( doPrereqs() == false ) return false;
        //
    	arfflist = new ArrayList<cmcFeatureCollection>();
    	
    	// run the feature extractor on all files
    	// in Text ,Mixed and Graph
    	// KB MIXED has been mapped onto TEXT
    	if( makeTextureFeatureDescriptorsForFolder( xMSet.getBayesTrainingDirText()  , cmcFeatureCollection.SCORE.TEXT  ) == false ) return false;
    	if( makeTextureFeatureDescriptorsForFolder( xMSet.getBayesTrainingDirMixed() , cmcFeatureCollection.SCORE.TEXT ) == false ) return false;
        //if( makeTextureFeatureDescriptorsForFolder( xMSet.getBayesTrainingDirMixed() , cmcFeatureCollection.SCORE.MIXED ) == false ) return false;
    	if( makeTextureFeatureDescriptorsForFolder( xMSet.getBayesTrainingDirGraph() , cmcFeatureCollection.SCORE.GRAPH ) == false ) return false;
    	// Make ARFF file
    	boolean ib=dumpARFF( xMSet.getBayesWEKAARFFFileName() , xMSet.getBayesWEKAProtoTypeARFFFileName() );
    	if( ib == false ) return false;
    	// Look for duplicates
    	return true;
    }
    
    //------------------------------------------------------------
    public boolean makeAnARFFFileFromSingleFolder(String FolderName , String ARFFFileName )
    //------------------------------------------------------------
    {
    	if( doPrereqs() == false ) return false;
        //
    	arfflist = new ArrayList<cmcFeatureCollection>();
    	// run the feature extractor on all files in this folder
    	if( makeTextureFeatureDescriptorsForFolder( FolderName  , cmcFeatureCollection.SCORE.UNKNOWN  ) == false ) return false;
    	// loop thrugh the names and set the score
    	for(int i=0;i< arfflist.size();i++)
    	{
    		cmcFeatureCollection feat = arfflist.get(i);
    		if( feat.ShortFileName.toUpperCase().trim().startsWith("POBJ_")) feat.score = cmcFeatureCollection.SCORE.GRAPH;
    		else
   			if( feat.ShortFileName.toUpperCase().trim().startsWith("PTXT_")) feat.score = cmcFeatureCollection.SCORE.TEXT;
   			else {
   				do_error("CInvalid filename " + feat.LongFileName );
   			}
    		//do_log( 1 , feat.ShortFileName + " " + feat.score );
    	}
    	// Make ARFF file
    	boolean ib=dumpARFF( ARFFFileName , null );
    	if( ib == false ) return false;
    	return true;
    }
    
    //------------------------------------------------------------
    private boolean Zap(String ZapFileName )
    //------------------------------------------------------------
    {
    	if( xMSet.xU.IsBestand( ZapFileName) == false ) return true;
    	if( xMSet.xU.VerwijderBestand( ZapFileName ) == false)  {
    		do_error("Connot delete file [" + ZapFileName + "]");
    		return false;
    	}
    	if( xMSet.xU.IsBestand( ZapFileName) == false ) return true;
		do_error("Connot delete file [" + ZapFileName + "]");
	    return false; 	
    }
    
    //------------------------------------------------------------
    private boolean dumpARFF(String ARFFFileName , String PrototypeFileName)
    //------------------------------------------------------------
    {
    	if( arfflist == null ) return false;
    	if( Zap(ARFFFileName) == false ) return false;
    	if( Zap(PrototypeFileName) == false ) return false;
    	//
    	String[] scoreListDeclaration = new String[]{"GRAPH" , "MIXED" , "TEXT" };
    	//
    	cmcARFF xproto = new cmcARFF("cbrTekstraktorPrototype" , 2 , arfflist.size() );
    	xproto.getAttributeList()[0].setAttrName("filename");
    	xproto.getAttributeList()[0].setTipe( cmcARFF.ARFF_TYPE.STRING );
    	xproto.getAttributeList()[0].setClassesDefinition("N/A");
    	xproto.getAttributeList()[0].setClasses(null);
    	//
    	xproto.getAttributeList()[1].setAttrName("score");
    	xproto.getAttributeList()[1].setTipe( cmcARFF.ARFF_TYPE.CLASS );
    	xproto.getAttributeList()[1].setClassesDefinition("WILL BE TAKEN CARE OF BY DAO");
    	xproto.getAttributeList()[1].setClasses(scoreListDeclaration);
    	//
    	for(int i=0;i<arfflist.size();i++)
    	{
    		cmcFeatureCollection xa = arfflist.get(i);
    		if( xa.haraList == null ) {
    			do_error("NULL haralist");
    			return false;
    		}
    		xproto.getAttributeList()[0].setSValue( i , xa.ShortFileName );
    		xproto.getAttributeList()[1].setSValue( i , ""+xa.score );
    	}
    	//
    	cmcARFFDAO  dao = new cmcARFFDAO( xMSet , logger );
    	if( PrototypeFileName != null ) {
    		if( dao.writeARFFFile( xproto ,  PrototypeFileName , false ) == false ) return false;
    	}
    	//
        //
    	cmcARFF xfeat = new cmcARFF("cbrTekstraktorFeatureSelection" , 14 , arfflist.size() );
    	xfeat.getAttributeList()[0].setAttrName("filename");
    	xfeat.getAttributeList()[0].setTipe( cmcARFF.ARFF_TYPE.STRING );
    	xfeat.getAttributeList()[0].setClassesDefinition("N/A");
    	xfeat.getAttributeList()[0].setClasses(null);
    	//
    	xfeat.getAttributeList()[1].setAttrName("density");
    	xfeat.getAttributeList()[1].setTipe( cmcARFF.ARFF_TYPE.NUMERIC );
    	xfeat.getAttributeList()[1].setClassesDefinition("N/A");
    	xfeat.getAttributeList()[1].setClasses(null);
    	//
    	xfeat.getAttributeList()[2].setAttrName("horizontalcontrast");
    	xfeat.getAttributeList()[2].setTipe( cmcARFF.ARFF_TYPE.NUMERIC );
    	xfeat.getAttributeList()[2].setClassesDefinition("N/A");
    	xfeat.getAttributeList()[2].setClasses(null);
    	//
    	xfeat.getAttributeList()[3].setAttrName("horizontalcorrelation");
    	xfeat.getAttributeList()[3].setTipe( cmcARFF.ARFF_TYPE.NUMERIC );
    	xfeat.getAttributeList()[3].setClassesDefinition("N/A");
    	xfeat.getAttributeList()[3].setClasses(null);
    	//
    	xfeat.getAttributeList()[4].setAttrName("horizontalCBRcorrelation");
    	xfeat.getAttributeList()[4].setTipe( cmcARFF.ARFF_TYPE.NUMERIC );
    	xfeat.getAttributeList()[4].setClassesDefinition("N/A");
    	xfeat.getAttributeList()[4].setClasses(null);
    	//
    	xfeat.getAttributeList()[5].setAttrName("horizontalinversedifference");
    	xfeat.getAttributeList()[5].setTipe( cmcARFF.ARFF_TYPE.NUMERIC );
    	xfeat.getAttributeList()[5].setClassesDefinition("N/A");
    	xfeat.getAttributeList()[5].setClasses(null);
    	//
    	xfeat.getAttributeList()[6].setAttrName("verticalcontrast");
    	xfeat.getAttributeList()[6].setTipe( cmcARFF.ARFF_TYPE.NUMERIC );
    	xfeat.getAttributeList()[6].setClassesDefinition("N/A");
    	xfeat.getAttributeList()[6].setClasses(null);
    	//
    	xfeat.getAttributeList()[7].setAttrName("verticalcorrelation");
    	xfeat.getAttributeList()[7].setTipe( cmcARFF.ARFF_TYPE.NUMERIC );
    	xfeat.getAttributeList()[7].setClassesDefinition("N/A");
    	xfeat.getAttributeList()[7].setClasses(null);
    	//
    	xfeat.getAttributeList()[8].setAttrName("verticalCBRcorrelation");
    	xfeat.getAttributeList()[8].setTipe( cmcARFF.ARFF_TYPE.NUMERIC );
    	xfeat.getAttributeList()[8].setClassesDefinition("N/A");
    	xfeat.getAttributeList()[8].setClasses(null);
    	//
    	xfeat.getAttributeList()[9].setAttrName("verticalinversedifference");
    	xfeat.getAttributeList()[9].setTipe( cmcARFF.ARFF_TYPE.NUMERIC );
    	xfeat.getAttributeList()[9].setClassesDefinition("N/A");
    	xfeat.getAttributeList()[9].setClasses(null);
    	//
    	//xfeat.getAttributeList()[10].setAttrName("meanHue");
    	//xfeat.getAttributeList()[10].setTipe( cmcARFF.ARFF_TYPE.NUMERIC );
    	//xfeat.getAttributeList()[10].setClassesDefinition("N/A");
    	//xfeat.getAttributeList()[10].setClasses(null);
    	//
    	xfeat.getAttributeList()[10].setAttrName("meanSaturation");
    	xfeat.getAttributeList()[10].setTipe( cmcARFF.ARFF_TYPE.NUMERIC );
    	xfeat.getAttributeList()[10].setClassesDefinition("N/A");
    	xfeat.getAttributeList()[10].setClasses(null);
    	//
    	xfeat.getAttributeList()[11].setAttrName("meanLightness");
    	xfeat.getAttributeList()[11].setTipe( cmcARFF.ARFF_TYPE.NUMERIC );
    	xfeat.getAttributeList()[11].setClassesDefinition("N/A");
    	xfeat.getAttributeList()[11].setClasses(null);
      	//
     	xfeat.getAttributeList()[12].setAttrName("nominalorientation");
    	xfeat.getAttributeList()[12].setTipe( cmcARFF.ARFF_TYPE.NOMINAL );
    	xfeat.getAttributeList()[12].setClassesDefinition("N/A");
    	xfeat.getAttributeList()[12].setClasses(new String[]{"ULTRANARROW" , "NARROW" , "PORTRAIT" , "SQUARE", "LANDSCAPE" , "WIDE" , "ULTRAWIDE"});
    	//
    	xfeat.getAttributeList()[13].setAttrName("score");
    	xfeat.getAttributeList()[13].setTipe( cmcARFF.ARFF_TYPE.CLASS );
    	xfeat.getAttributeList()[13].setClassesDefinition("N/A");
    	xfeat.getAttributeList()[13].setClasses(scoreListDeclaration);
    	//
    	for(int i=0;i<arfflist.size();i++)
    	{
    		cmcFeatureCollection xa = arfflist.get(i);
    		//
    		xfeat.getAttributeList()[0].setSValue( i , xa.ShortFileName );
    		xfeat.getAttributeList()[1].setValue( i , xa.density );
    		//
    		for(int j=0;j<xa.haraList.size();j++)
    		{
    			cmcHaralick ha = xa.haraList.get(j);
    			if( ha.channel != cmcHaralick.CHANNEL.GRAYSCALE ) continue;
    			if( ha.orientation != cmcHaralick.ORIENTATION.HORIZONTAL ) continue;
    			//
    			xfeat.getAttributeList()[2].setValue( i , ha.contrast );
    			xfeat.getAttributeList()[3].setValue( i , ha.correlation );
    			xfeat.getAttributeList()[4].setValue( i , ha.cbrTekCorrelation );
    			xfeat.getAttributeList()[5].setValue( i , ha.inverseDifference );
    //do_log( 1 , "Putting [" + ha.contrast +"] in [2," + i + "]");
        	}
    		for(int j=0;j<xa.haraList.size();j++)
    		{
    			cmcHaralick ha = xa.haraList.get(j);
    			if( ha.channel != cmcHaralick.CHANNEL.GRAYSCALE ) continue;
    			if( ha.orientation != cmcHaralick.ORIENTATION.VERTICAL ) continue;
    			//
    			xfeat.getAttributeList()[6].setValue( i , ha.contrast );
    			xfeat.getAttributeList()[7].setValue( i , ha.correlation );
    			xfeat.getAttributeList()[8].setValue( i , ha.cbrTekCorrelation );
    			xfeat.getAttributeList()[9].setValue( i , ha.inverseDifference );
    //do_log( 1 , "Putting [" + ha.contrast +"] in [6," + i + "]");
        	}
    		//
    		//xfeat.getAttributeList()[10].setValue( i , xa.meanHue );
    		xfeat.getAttributeList()[10].setValue( i , xa.meanSaturation );
    		xfeat.getAttributeList()[11].setValue( i , xa.meanLightness );
    		//
      		xfeat.getAttributeList()[12].setSValue( i , ""+xa.orientation );
      	    		
    		xfeat.getAttributeList()[13].setSValue( i , ""+xa.score );
    	}
    	if( dao.writeARFFFile( xfeat , ARFFFileName , true ) == false ) return false;
   
    	//dao.show(xfeat);
    	dao=null;
    	return true;
    }
    
    //------------------------------------------------------------
    public boolean extractHOGFeatures()
    //------------------------------------------------------------
    {
    	if( doPrereqs() == false ) return false;
        
    	// Make the HOG Descriptor for all validbubbles and invalidbubblrs
    	if( makeHOGDescriptorsForFolder( xMSet.getHOGValidDir() , xMSet.getBayesResultDir() ) == false ) return false;
    	if( makeHOGDescriptorsForFolder( xMSet.getHOGInvalidDir() , xMSet.getBayesResultDir() ) == false ) return false;
    	
    	// make filetypelist - just a list of files and whether the file is in the valid or invalid folder
    	if( makeFileTypeList(xMSet.getHOGValidDir(),"VALIDBUBBLE") == false ) return false;
    	if( makeFileTypeList(xMSet.getHOGInvalidDir(),"INVALIDBUBBLE") == false ) return false;
    	
    	// determine the number of 4x9 vectors (by reading an XML)
    	NumberOfVectorsPerHOG = determine_number_of_vectors_per_HOG(xMSet.getBayesResultDir());
    	if( NumberOfVectorsPerHOG < 10) {
    		do_error("Too few vectors per HOG descriptor [" + NumberOfVectorsPerHOG + "]. Consider to increase training image size");
    		return false;
    	}
    	do_log( 9 , "VectorsPerHog [" + NumberOfVectorsPerHOG  + "]");
    	
    	// initialize the workarea to store the 9x4 vectors for all images in the valid and invalid folders
    	ar_hogvectorlines = new HOGVectorLine[ NumberOfVectorsPerHOG * filetypelist.size() ];
    	for(int i=0;i<ar_hogvectorlines.length;i++) ar_hogvectorlines[i]=null;
    	
    	// Read all XML Files and store values in ar_hoglines
    	if( loopThroughHOGDescriptors( xMSet.getBayesResultDir() ) == false ) return false;
    	// check to see of all entries are occupied
    	if( checkArHOGVectorLines() == false ) return false;
    	
    	if( RANDOM_PROJECTION ) {
    	  // Random Projection
    	  double[][] MatrixReduced = performRandomProjectionOnTrainingSet();
    	  if( MatrixReduced == null ) return false;
    	  if( dumpTrainingMatrix(MatrixReduced) == false ) return false;
    	}
    	else {
    	  // just transfer all values
    	  double[][] MatrixReduced = makeTrainingMatrixFromAllXMLs();
      	  if( MatrixReduced == null ) return false;
      	  if( dumpTrainingMatrix(MatrixReduced) == false ) return false;	
    	}
    	//
    	do_error("debug");
    	return false;
    }
    
    //------------------------------------------------------------
    private boolean checkArHOGVectorLines()
    //------------------------------------------------------------
    {
       if( ar_hogvectorlines[ ar_hogvectorlines.length - 1] == null ) {
    		do_error("Looks like not all HOG vectorlines have been populated");
    		int idx=0;
    		for(int i=0;i<ar_hogvectorlines.length;i++)
    		{
    			if( ar_hogvectorlines[i] == null ) { idx = i; break; }
    		}
    		do_error("Only used untill [" + idx + "]");
    		return false;
    	}
       return true;
    }
    
    //------------------------------------------------------------
    private int determine_number_of_vectors_per_HOG(String ResultDir)
    //------------------------------------------------------------
    {
       if( ResultDir == null ) return -1;
       // just open 1 XML file and get the array of doubles
       ArrayList<String> list = xMSet.xU.GetFilesInDir( ResultDir , null );
       if( list == null ) return -1;
       if( list.size() < 0 ) return -1;
       String XMLFileName = ResultDir + xMSet.xU.ctSlash + list.get(0);
       double[] ar_test = extractVectorValuesFromFile( XMLFileName );
       if( ar_test == null ) return -1;
       return ar_test.length / VALUES_PER_VECTOR;
    }
    
    //------------------------------------------------------------
    private boolean makeTextureFeatureDescriptorsForFolder( String FolderName , cmcFeatureCollection.SCORE score)
    //------------------------------------------------------------
    {
    	cmcTextureAnalysisRoutines texture = new cmcTextureAnalysisRoutines( xMSet , logger );
    	ArrayList<String>imagelist = xMSet.xU.GetFilesInDir( FolderName , null );
    	int processed=0;
    	//
    	ArrayList<cmcHaralick> haraList=null;
    	for(int i=0;i<imagelist.size();i++)
    	{
    		String ShortName = imagelist.get(i).trim();
    		String ImageFileName = FolderName + xMSet.xU.ctSlash + ShortName;
    		if( ImageFileName.trim().toUpperCase().endsWith(".JPG") == false ) continue;
    		if( ImageFileName.trim().toUpperCase().indexOf("REDUX") >= 0 ) continue;  // debug created REDUX files ignore these
    		// Extract original size from filename   nnnnn-0000x0000.jpg
    		int pdot = ShortName.toUpperCase().lastIndexOf(".JPG");
    		int pmin = ShortName.toUpperCase().lastIndexOf("-");
    		int pmul = ShortName.toUpperCase().lastIndexOf("X");
    		if( (pdot < 0) || (pmin < 0) || (pmul < 0) ) {
    			do_error("Could not extract dimensions from [" + ShortName + "]" + pmin + " " + pmul + " " + pdot);
    			return false;
    		}
    		if( (pdot <= pmul) || (pdot <= pmin) || (pmul < pmin) ) {
    			do_error("Could not extract dimensions from [" + ShortName + "] pattern not found :  -04dx04d.jpg");
    			return false;
    		}
    		int orig_width = -1;
    		int orig_heigth = -1;
    		try {
    		  String swi = ShortName.substring( pmin+1 , pmin + 5);
    		  String she = ShortName.substring( pmul+1 , pmul + 5);
    		  //do_log( 1 , "[" + swi + "][" + she + "]");
    		  orig_width  = xMSet.xU.NaarInt( swi );
    		  orig_heigth = xMSet.xU.NaarInt( she );
    		}
    		catch( Exception e ) {
    			orig_width = -1;
        		orig_heigth = -1;
    		}
    	    if( (orig_width < 0) || (orig_heigth < 0) ) {
    	    	do_error("Could not extract dimensions from [" + ShortName + "]");
    			return false;
    	    }
    		// load image in buffer
    		int[] pixels = imgloader.loadBestandInBuffer( ImageFileName );
    		if( pixels == null ) {
    			do_error("Could not load image [" + ImageFileName + "`]");
    			texture = null;
    			return false;
    		}
    		int width = imgloader.getBreedte();
    		int heigth = pixels.length / width;
    		if( heigth != imgloader.getHoogte() ) {
    			do_error("Height differs [" + heigth + "] from [" + imgloader.getHoogte() + "]");
    			texture = null;
    			return false;
    		}
    		
    	    // Cut the original image
    		int[] cut = null;
    		int cutWidth = width;
    		if( (width != orig_width) || (heigth != orig_heigth) ) {
    		  //do_log( 1 , "Cutting [" + orig_width + "," + orig_heigth + "]");
    		  cut = new int[ orig_width * orig_heigth];
    		  int offy = (heigth - orig_heigth) / 2;
    		  int offx = (width - orig_width) / 2;
    		  int k=0;
    		  for(int y=0;y<orig_heigth;y++)
    		  {
    			  int z = ((offy + y) * width) + offx;
    			  for(int x=0;x<orig_width;x++)
    			  {
    				  cut[k] = pixels[ z + x ];
    				  k++;
    			  }
    		  }
    		  cutWidth = orig_width;
            } // no cutting needed
    		else {
    		  cut = pixels;
    		  cutWidth = width;
         	}
    		//
    		haraList = texture.extractHaralickFeatures( cut , cutWidth, ImageFileName );
            if( haraList == null ) { texture = null; return false; }
            //  DEBUG
            //for(int z=0;z<haraList.size();z++) do_log( 1 , haraList.get(z).format() );
            
            // calculate density
            double densi = texture.extractDensity( cut , cutWidth );
            // calculate Mean HSL values
            double[] hsl = texture.extractMeanHSL( cut );
           
            // save
            cmcFeatureCollection xa = new cmcFeatureCollection( ImageFileName , ShortName  , score );
            xa.haraList = haraList;
            xa.widthHeightRatio = (double)orig_width / (double)orig_heigth;
            xa.density          = densi;
            xa.meanHue          = hsl[0];
            xa.meanSaturation   = hsl[1];
            xa.meanLightness    = hsl[2];
            xa.orientation      = texture.getOrientation(orig_width,orig_heigth);
            if( xa.orientation == cmcFeatureCollection.ORIENTATION.UNKNOWN ) {
            	do_error( "Got an unknown orientation");
            	return false;
            }
            arfflist.add( xa );
            
  /*
  if( i > 20000 ) {
	  do_error( "DEBUG - QUIT [" + i + "] [" + FolderName + "]");
	  break;
  }
  */
    		//
    		pixels = null;
    		processed++;
    	}
    	if( processed == 0 ) {
    		do_error("There are no image files to process in [" + FolderName + "]");
    		texture = null;
    		return false;
    	}
    	do_log( 9 , "Create texture feature descriptor for [" + processed + "] files in [" + FolderName + "]");
    	//
    	//
        texture.cleanUpTrainingDebugFiles(FolderName);
        //
    	texture = null;
    	return true;
    }
    
    //------------------------------------------------------------
    private boolean makeHOGDescriptorsForFolder( String FolderName , String ResultFolder)
    //------------------------------------------------------------
    {
    	ArrayList<String>imagelist = xMSet.xU.GetFilesInDir( FolderName , null );
    	int processed=0;
    	//
    	for(int i=0;i<imagelist.size();i++)
    	{
    		String ImageFileName = FolderName + xMSet.xU.ctSlash + imagelist.get(i);
    		if( ImageFileName.trim().toUpperCase().endsWith(".JPG") == false ) continue;
    		// load image in buffer
    		int[] pixels = imgloader.loadBestandInBuffer( ImageFileName );
    		if( pixels == null ) {
    			do_error("Could not load image [" + ImageFileName + "`]");
    			return false;
    		}
    		int width = imgloader.getBreedte();
    		int heigth = pixels.length / width;
    		if( heigth != imgloader.getHoogte() ) {
    			do_error("Height differs [" + heigth + "] from [" + imgloader.getHoogte() + "]");
    			return false;
    		}
    		//
            boolean ib = hogger.makeHOGDescriptor(pixels , width, ImageFileName, ResultFolder);
            if( ib == false ) return false;
  //if( i == 5 ) break;
    		//
    		pixels = null;
    		processed++;
    	}
    	if( processed == 0 ) {
    		do_error("There are no image files to process in [" + FolderName + "]");
    		return false;
    	}
    	do_log( 9 , "Create HOG gradient for [" + processed + "] files in [" + FolderName + "]");
    	return true;
    }
    
    //------------------------------------------------------------
    private double[] extractValuesFromVector( String sin  )
    //------------------------------------------------------------
    {
    	double[] vals = new double[ VALUES_PER_VECTOR];
    	StringTokenizer strings = new StringTokenizer(sin, "[] ");
    	String element="";
    	int ctr=0;
        while (strings.hasMoreElements()) {
            element = strings.nextToken();
            if( element == null ) continue;
            element = element.trim();
            if( element.length() < 1 ) continue;
            double dd = xMSet.xU.NaarDouble( element );
            if( dd < 0 ) continue;
            if( ctr < vals.length ) vals[ctr] = dd;
            ctr++;
        }   
    	if( ctr != VALUES_PER_VECTOR ) {
    		do_error("Could not extract exactly [" + VALUES_PER_VECTOR + "] vectors but got [" + ctr + "] vectors from [" + sin + "]");
    		return null;
    	}
    	return vals;
    }
    
    //------------------------------------------------------------
    private double[] extractVectorValuesFromFile(String XMLFileName)
    //------------------------------------------------------------
    {
    	
    	 double[] ar_double = null;
    	 try {
     	    File inFile  = new File(XMLFileName);  // File to read from.
 	        BufferedReader reader = new BufferedReader(new InputStreamReader(new FileInputStream(inFile), xMSet.getCodePageString()));
 	       	int aantal=0;
 	       	String sLine=null;
 	       	String sVector="";
 	       	boolean inVector=false;
 	       	int vecno=0;
 	       	int numberofvectors=-1;
 	       	int valno=0;
         	while ((sLine=reader.readLine()) != null) {
         		if( sLine.trim().length() < 1) continue;
         		// determine number of vectors expected
         		if( sLine.indexOf("<NumberOfVectors>") >= 0 )  {
         		    String sVal = xMSet.xU.extractXMLValue(sLine,"NumberOfVectors");
         		    numberofvectors = xMSet.xU.NaarInt(sVal);
         		    if( numberofvectors < 0 ) {
         		    	do_error("Cannot determine number of vectors from [" + sLine + "] in " + XMLFileName);
         		    	return null;
         		    }
         		    ar_double = new double[ numberofvectors * VALUES_PER_VECTOR ];
         		    for(int k=0;k<ar_double.length;k++) ar_double[k]=-1;
         		    valno=0;
         		}
         	    // Lookfor <Vector> and then <!DATA[
         		if( sLine.toUpperCase().indexOf("<VECTOR>") >= 0 ) { inVector = true; sVector = ""; vecno++; }
         		if( inVector == false ) continue;
                 sVector = sVector + sLine.trim();		
         		if( sLine.toUpperCase().indexOf("</VECTOR>") >= 0 ) {
         			inVector = false;
         			//
         			int idx = sVector.indexOf("<![CDATA[");
         			if( idx < 0) continue;
         			int pdx = sVector.indexOf("]]>");
         			if (pdx < 1 ) continue;
         			String sDoubleList = sVector.substring( idx + "<![CDATA[".length()  , pdx );
         			double[] vals = extractValuesFromVector( sDoubleList );
         			if( vals == null ) return null;
         			for(int k=0;k<vals.length;k++)
         			{
         				if( valno < ar_double.length ) ar_double[valno] = vals[k];
         				valno++;
         			}
         		}
         	}
         	reader.close();
         	if( vecno != numberofvectors ) {
         		do_error("Number of vectors read [" + vecno + "] differs from expected [" + numberofvectors + "]");
         		return null;
         	}
         	int errorCnt=0;
         	for(int k=0;k<ar_double.length;k++) {
         		if( ar_double[k] < 0 ) errorCnt++;
         	}
         	if( errorCnt != 0) {
         		do_error("Not all values [" + ar_double.length + "] have been processed");
         		return null;
         	}
         	  	
     	  }
     	  catch(Exception e ) {
     		  do_error("Cannot open or read file [" + XMLFileName + "]");
     		  return null;
     	  }
     	  return ar_double;
    }
    
    //------------------------------------------------------------
    private boolean loopThroughHOGDescriptors(String ResultFolder)
    //------------------------------------------------------------
    {
    	ArrayList<String> xmllist = xMSet.xU.GetFilesInDir( ResultFolder , null );
    	if( xmllist == null ) {
    		do_error("Could not determine XML files in [" + ResultFolder + "]");
    		return false;
    	}
    	if( xmllist.size() < 1 ) {
    		do_error("Could not find any files in [" + ResultFolder + "]");
    		return false;
    	}
    	
    	//
    	int processed = 0;
    	for(int i=0;i<xmllist.size();i++)
    	{
    	  String XMLFileName = ResultFolder + xMSet.xU.ctSlash + xmllist.get(i);
    	  if( XMLFileName.toUpperCase().trim().endsWith(".XML") == false) continue;
    	  CurrLabel = getLabel( xmllist.get(i) );
    	  if( CurrLabel == null )  {
    		  do_error("Cannot determine label for [" + xmllist.get(i) );
    		  return false;
    	  }
    	  double[] VectorValues = extractVectorValuesFromFile(XMLFileName);
    	  if( VectorValues == null ) return false;
    	  if( extract9x4means(VectorValues,i) == false ) return false;
    	  //
    	  processed++;
    	}  
    	//
    	if( processed < 1 ) {
    		do_error("Could not find any XML files in [" + ResultFolder + "]");
    		return false;
    	}
    	CloseDumpFile();
    	return true;
    }
    
    //------------------------------------------------------------
    private boolean extract9x4means(double[] VectorValues, int fileindex)
    //------------------------------------------------------------
    {
    	if( ar_hogvectorlines == null ) return false;
        int idx=-1;
        int used=0;
        for(int i=0;i<ar_hogvectorlines.length;i++) 
        {
        	if( ar_hogvectorlines[i] == null ) { idx =i; break; }
        	used++;
        }
    	if( idx < 0 ) {
    		do_error("There are no free slots in AR_HOGVECTORLINE. Used [" + used + "]");
    		return false;
    	}
    	int tst = VectorValues.length / VALUES_PER_VECTOR;  //  DIV 4x9
    	if( tst != NumberOfVectorsPerHOG ) {
    		do_error("System error - number of vectors per HOG differs from[" + NumberOfVectorsPerHOG + "] got [" + tst + "]");
    		return false;
    	}
    	for(int h=0;h<NumberOfVectorsPerHOG;h++)
    	{
    	  HOGVectorLine x = new HOGVectorLine(fileindex);
    	  x.buf = new double[VALUES_PER_VECTOR];
    	  double tot = 0;
    	  for(int i=0;i<x.buf.length;i++)
    	  {
    		x.buf[ i ] = VectorValues[ (h*VALUES_PER_VECTOR) + i];
    		tot += x.buf[i];
    	  }
    	  x.mean = tot / (double)x.buf.length;
    	  if( idx < ar_hogvectorlines.length ) ar_hogvectorlines[idx] = x;
    	  else {
    		  do_error("Overshooting number of entries in AR_HOGVECTORLINE [" + idx + "]");
    		  return false;
    	  }
    	  idx++;
    	}
    	
    	return true;
    }
    
    //------------------------------------------------------------ 
   private double[][] makeTrainingMatrixFromAllXMLs()
   //------------------------------------------------------------
   {
	   int cols = VALUES_PER_VECTOR * NumberOfVectorsPerHOG;   // (4 x 9)  x (TRUNC ( WIDTH / 8 )) x (TRUNC HEIGHT/8 
	   int rows = ar_hogvectorlines.length / NumberOfVectorsPerHOG;
	   if( rows != filetypelist.size() ) {
		   do_error("Number of rows in overall matrix is incorrect [" + rows + "] expect [Files=" + filetypelist.size() + "] [VecPerHog=" + NumberOfVectorsPerHOG + "]");
		   return null;
	   }
	   // make a matrix of all vector values per single imagefile,i.e. 4 x 9 x 1296 values 
	   double[][] MatrixTraining = new double[rows][cols];
	   for(int i=0;i<rows;i++)
	   {
		   for(int j=0;j<cols;j++) MatrixTraining[i][j] = -1;   
	   }
	   do_log(9 , "Rows=" + rows + " Cols=" + cols);
	   //	   
	   int imageindex=-1;
	   int zz=-1;
	   for(int i=0;i<rows;i++)
	   {
		   imageindex++;
		   int pp=-1;
		   for(int j=0;j<NumberOfVectorsPerHOG;j++)
		   {
			   zz++;
			   HOGVectorLine vline = ar_hogvectorlines[ zz ];
			   //do_log(9 , "Row=" + i + " VecLine=" + j + " seq=" + zz);
			   if( vline.buf.length !=  VALUES_PER_VECTOR) {
				   do_error("Number of cols does not match buf in vectorline [" + i + "]");
				   return null;
			   }
			   if( imageindex != vline.fileindex ) {
				   do_error("Imageindex [" + imageindex + "] differs from FileIndex on vectorline [" + vline.fileindex + "]");
				   return null;
			   }
			   for(int k=0;k<vline.buf.length;k++)
			   {
				   pp++;
				   MatrixTraining[i][pp] = vline.buf[k];
			   }
		   }
	   }
	   // there is now a matrix with all values of all vectors per image (cols) and number of rows equal to images
	   for(int i=0;i<rows;i++)
	   {
		   for(int j=0;j<cols;j++) {
			   if( MatrixTraining[i][j] == -1 ) {
				   do_error("Value not populated [" + i + "][" + j + "]");
				   return null;
			   }
		   }
	   }
	   return MatrixTraining;
   }
   
   //------------------------------------------------------------
   private double[][] performRandomProjectionOnTrainingSet() 
   //------------------------------------------------------------
   {
	   // Extract all values and store in matrix [ number of files ] [ values ]
	   double[][] MatrixTraining = makeTrainingMatrixFromAllXMLs();
	   if( MatrixTraining == null ) return null;
	   
	   // Perform Random Projection 
	   double[][] MatrixProjection = makeProjectionMatrix(MatrixTraining);
	   if( MatrixProjection == null ) return null;
	   
	   //  Multiply  Training and Projection matrix
	   // T ( row , cols )  P ( rows = T.cols , K )  =>   RES ( row , K )
	   double[][] MatrixReduced = matrixMult( MatrixTraining , MatrixProjection );
	   if( MatrixReduced == null ) return null;
	   
	   // Normalize
	   NormalizeReducedMatrix( MatrixReduced );
	   
	   return MatrixReduced;
   }
   
   // make  matrix of [ number of images  , K ]  K is the reduced number of attributes
   // each value is SQRT(3) of either {-1,0,1}  with probability {1/6,2/3,and 1/6}
   // number of rows is equal to number of attributes (cols) on the matrix to be reduced
   //------------------------------------------------------------
   private double[][] makeProjectionMatrix(double[][] input)
   //------------------------------------------------------------
   {
	   if( input == null ) {
		   do_error("Input matrix to create projection matrix is null");
		   return null;
	   }
	   if( input.length < 1 ) {
		   do_error("there are no rows to create projection matrix");
		   return null;
	   }
	   int cols = input[0].length;
	   double[][] MatrixProjection = null;
	   try {
	     int K =  (int)((double)cols / (double)REDUCTOR);
	     if( K > MAX_REDUCED_COLS ) K = MAX_REDUCED_COLS;
	     MatrixProjection = new double[ cols ] [ K ];
	     do_log( 9 , "Create a projection matrix [" + cols + "," + K + "]");
	     Random r = new Random();
	     for(int i=0;i<cols;i++)
	     {
		   for(int j=0;j<K;j++)
		   {
		    int p = r.nextInt(6) + 1;  // 1 .. 6
		    MatrixProjection[i][j] = 0;
		    switch( p )
		    {
		    case 1 : { MatrixProjection[i][j] = Math.sqrt(3); break; }
		    case 2 : ;
		    case 3 : ;
		    case 4 : ;
		    case 5 : break;
		    case 6 : { MatrixProjection[i][j] = 0 - Math.sqrt(3); break; }
		    default : { do_error("Incorrect probability [" + p + "]"); return null; }
		    }
		   }
	     }
	     // check distribution
	     double[] dist = new double[3];
	     for(int i=0;i<dist.length;i++) dist[i]=0;
	     for(int i=0;i<MatrixProjection.length;i++)
	     {
	    	 for(int j=0;j<MatrixProjection[i].length;j++)
	    	 {
	    		 double dd = MatrixProjection[i][j];
	    		 if( dd < 0 ) dist[0]++;
	    		 else
	    		 if( dd > 0) dist[2]++;
	    		 else dist[1] ++;
	    	 }
	     }
	     double tot = MatrixProjection.length * MatrixProjection[0].length;
	     do_log( 9 , "Distribution [" + (dist[0]/tot) + "] [" + (dist[1]/tot) + "] [" + (dist[2]/tot) + "]");
	     return MatrixProjection;
	   }
	   catch(Exception e ) {
		   do_error("Error when creating projection matrix " + e.getMessage() );
		   e.printStackTrace();
		   return null;
	   }
   }
   
   //------------------------------------------------------------
   private double[][] matrixMult( double[][]one , double[][]two)
   //------------------------------------------------------------
   {
	   if( one == null ) {
		   do_error("matrixMult - first matrix is null");
		   return null;
	   }
	   if( two == null ) {
		   do_error("matrixMult - second matrix is null");
		   return null;
	   }
	   int oneRows = one.length;
	   int oneCols = one[0].length;
	   int twoRows = two.length;
	   int twoCols = two[0].length;
	   int resRows = oneRows;
	   int resCols = twoCols;
	   do_log( 9 , "Multiplying [" + oneRows + "," + oneCols + "] x [" + twoRows + "," + twoCols + "] - [" + resRows + "," + resCols + "]");
	   if( oneCols != twoRows ) {
		   do_error( "Columns of first matrix do not match rows of second matrix [" + oneCols + "] [" + twoCols + "]");
		   return null;
	   }
	   double[][] res = new double[ resRows ][resCols ];
	   for(int k=0;k<resCols;k++)
	   {
		   if( (k % 25) == 24 )  do_log( 9 , "Progress [" + k + "] of [" + resCols + "]");
		   for(int i=0;i<oneRows;i++)
		   {
			   res[i][k]= (double)0;
			   for(int j=0;j<oneCols;j++)
			   {
				   res[i][k] += one[i][j] * two[j][k];
			   }
		   }
	   }
	   return res;
   }
   
   //------------------------------------------------------------
   private boolean NormalizeReducedMatrix( double[][] matrix)
   //------------------------------------------------------------
   {
	   if( matrix == null ) {
		   do_error("Input matrix to normalizema is null");
		   return false;
	   }
	   if( matrix.length < 1 ) {
		   do_error("there are no rows to normalize");
		   return false;
	   }  
	   int rows = matrix.length;
	   int cols = matrix[0].length;
	   do_log( 9 , "Normalizing reduced matrix [" + rows + "," + cols + "]");
	   /*
	   double sumsquares = 0;
	   for(int i=0;i<rows;i++)
	   {
		   sumsquares = 0;
		   for(int j=0;j<cols;j++)
		   {
			   sumsquares +=  matrix[i][j] *  matrix[i][j];
		   }
		   sumsquares = Math.sqrt( sumsquares );
		   if( sumsquares <= 0 ) {
			   do_error("Design error - ");
			   return false;
		   }
		   for(int j=0;j<cols;j++)  // between  -1 and 1
		   {
			   matrix[i][j] =  matrix[i][j] / sumsquares;
		   }
		   for(int j=0;j<cols;j++)  // make positive and between 0 .. 1
		   {
			   matrix[i][j] =  (matrix[i][j] + 1) / 2;
		   }
	   }
	   */
	   /*
	   double min=matrix[0][0];
	   double max=matrix[0][0];
	   for(int i=0;i<rows;i++)
	   {
		   for(int j=0;j<cols;j++)
		   {
			   if( matrix[i][j] > max ) max = matrix[i][j];
			   if( matrix[i][j] < min ) min = matrix[i][j];
		   }
	   }
	   do_log( 9 , "Spread [" + min + "] [" + max + "]");
	   for(int i=0;i<rows;i++)
	   {
		   for(int j=0;j<cols;j++)
		   {
			   matrix[i][j] = (matrix[i][j] - min) / ( max - min );
		   }
	   }
*/
	   return true;
   }
   
   //------------------------------------------------------------
   private boolean dumpTrainingMatrix( double[][] matrix)
   //------------------------------------------------------------
   {
	   if( matrix == null ) {
		   do_error("Input matrix to create projection matrix is null");
		   return false;
	   }
	   if( matrix.length < 1 ) {
		   do_error("there are no rows to create projection matrix");
		   return false;
	   }  
	   int rows = matrix.length;
	   int cols = matrix[0].length;
	   do_log( 9 , "Dumping training matrix [" + rows + "," + cols + "]");
	   
	   // number of rows must match the files
	   if( filetypelist.size() != rows ) {
		   do_error( "Number of rows does not match number of files");
		   return false;
	   }
	   double[] vecval = new double[ cols ];
	   for(int i=0;i<rows;i++)
	   {
		  for(int j=0;j<cols;j++) vecval[j] = matrix[i][j];
		  CurrLabel = filetypelist.get(i).label;
    	  
		  //  twijfel of ditr wel goed is labelop rows zetten - moet soldier
		  // debug
		  //String ss= CurrLabel;
		  //for(int k=0;k<cols;k++) ss += " , " + vecval[k];
		  //System.out.println( ss );
		  dumpVectorLine( vecval , DUMPTYPE.WEKA , i);
	   }
	   
	   return true;
   }
   
   private void CloseDumpFile()
   {
	   if( pout != null ) {
		   pout.println("");
		   pout.close();
	   }
   }
   
   
   private boolean dumpVectorLine( double[] vecval , DUMPTYPE tipe , int sequence)
   {
	   if( vecval == null ) {
		   do_error("VectorValues are NULL");
		   return false;
	   }
	   // If first line initialize output file
	   if( sequence == 0 ) {
		 String DumpFileName = null;
		 switch( tipe )
		 {
		 case WEKA : { DumpFileName = xMSet.getWEKADumpFileName(); break; }
		 default : { do_error("Unsupported dump file tipe [" + tipe + "]"); break; }
		 }
		 if( DumpFileName == null ) return false;
		 pout = new gpPrintStream( DumpFileName , "ASCII" ) ;
	     //
	     if( tipe == DUMPTYPE.WEKA ) {
	    	 
	    	pout.println("@relation cbrtekstraktor");
	    	pout.println("");
	    	for(int i=0;i<vecval.length;i++) pout.println("@attribute hog" + i + " numeric");
	    	pout.println("@attribute bubble {VALIDBUBBLE,INVALIDBUBBLE}");
	    	pout.println("");
	    	pout.println("@data");
	    	 
	     }
	     else {
	    	 do_error("System error - no code to initialize file type [" + tipe + "]");
	    	 return false;
	     }
	   }
	   if( pout == null ) {
		   do_error("DumpFile has not been initialized");
		   return false;
	   }
	   
	   // WEKA
	   if( tipe == DUMPTYPE.WEKA ) {
		   String sLine = "";
		   for(int i=0;i<vecval.length;i++)
		   {
			   if( i != 0 ) sLine += ",";
			   sLine += "" + vecval[i];
		   }
		   pout.println( sLine + "," + CurrLabel.trim().toUpperCase() );
		   //do_log( 9 , "Line [" + sequence + "]");
	   }
	   else {
	    	do_error("System error - no code to dump values for file type[" + tipe + "]");
	    	return false;
	   }
	   return true;
   }
   
}
