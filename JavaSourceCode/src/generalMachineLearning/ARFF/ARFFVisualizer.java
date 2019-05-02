package generalMachineLearning.ARFF;

import java.awt.Color;
import java.awt.Font;
import java.awt.image.BufferedImage;
import java.util.ArrayList;

import cbrTekStraktorModel.cmcProcConstants;
import cbrTekStraktorModel.cmcProcEnums;
import cbrTekStraktorModel.cmcProcSettings;
import drawing.cmcDrawPixelText;
import generalImagePurpose.cmcImageRoutines;
import generalMachineLearning.cmcMachineLearningConstants;
import generalMachineLearning.cmcMachineLearningEnums;
import generalMachineLearning.ARFF.cmcARFF.ARFF_TYPE;
import generalStatPurpose.gpDiagram;
import generalStatPurpose.gpDiagramDTO;
import generalStatPurpose.gpDoubleListStatFunctions;
import generalStatPurpose.gpStatAdditionalFunctions;
import logger.logLiason;

public class ARFFVisualizer {
	
	
	cmcProcSettings xMSet=null;
	logLiason logger=null;
	private cmcImageRoutines irout=null;
    private cmcARFFRoutines arout = null;
    private cmcDrawPixelText drwtxt = null;
    private gpDoubleListStatFunctions stat = null;
    private gpStatAdditionalFunctions addstat = null;
    private gpDiagram diarout = null;
	
	private ARFFWrapperDTO gdto = null;
	private boolean resorted = false;  // resort is needed ofr the scatter diagrams - lazy approach
	private String LastErrorMsg=null;
	private boolean normalizeValues=true;
	private double[][] covarianceMatrix = null;
	private double[][] correlationMatrix = null;
	private int AllHistogramsWidth = -1;
	private int AllHistogramsHeigth = -1;
	private int AllScatterWidth=-1;
	private int AllScatterHeigth=-1;
    
    class PictureCacheDTO
    {
    	String label1=null;
    	String label2=null;
    	cmcMachineLearningEnums.PictureType tipe = cmcMachineLearningEnums.PictureType.UNKNOWN;
    	BufferedImage img;
    }
    ArrayList<PictureCacheDTO> pictureCache = null;
   
    
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
    public ARFFVisualizer(cmcProcSettings is,logLiason ilog)
    //------------------------------------------------------------
    {
    	xMSet = is;
		logger=ilog;
		pictureCache=null;
		resorted=false;
		diarout = new gpDiagram(is,ilog);
		arout = new cmcARFFRoutines( xMSet , logger );
		irout = new cmcImageRoutines(logger);
		drwtxt = new cmcDrawPixelText(xMSet,logger);
    }
   
    //----------------------------------------------------------
    //----------------------------------------------------------
    public void setScatterDotType( cmcProcEnums.SCATTER_DOT_TYPE tipe )
    {
    	diarout.setScatterDotType(tipe);
    }
    public cmcProcEnums.SCATTER_DOT_TYPE getScatterDotType()
    {
    	return diarout.getScatterDotType();
    }
    public void setScatterDotDiameter(int dia)
    {
    	diarout.setScatterDotDiameter(dia);
    }
    public void setBackGroundColor(Color colour)
    {
    	 diarout.setBackGroundColor(colour);
    }
    public Color getBackGroundColor()
    {
    	 return diarout.getBackGroundColor();
    }
    public int getTileWidth()
    {
    	return diarout.getTileWidth();
    }
    public int getTileHeigth()
    {
    	return diarout.getTileHeigth();
    }
    public int getRectangleSide()
    {
    	return diarout.getRectangleSide();
    }
    public int getCorrelationWidth()
    {
    	return diarout.getCorrelationWidth();
    }
    public int getCorrelationHeigth()
    {
    	return diarout.getCorrelationHeigth();
    }
    public String getLastErrorMsg()
    {
    	return LastErrorMsg;
    }
    public int getNumberOfBins()
    {
    	return gdto.getNbrOfBins();
    }
    public boolean isNormalized()
    {
    	return normalizeValues;
    }
    public void setNormalised(boolean ib)
    {
    	normalizeValues = ib;
    }
    public cmcProcEnums.ColorSentiment getColorSentiment()
    {
    	return diarout.getColorSentiment();
    }
    public void setColorSentiment(cmcProcEnums.ColorSentiment cos)
    {
    	diarout.setColorSentiment(cos);
    }
    public int getAllHistogramsWidth() {
		return AllHistogramsWidth;
	}
	public int getAllHistogramsHeigth() {
		return AllHistogramsHeigth;
	}
	public int getAllScatterWidth() {
		return AllScatterWidth;
	}
	public int getAllScatterHeigth() {
		return AllScatterHeigth;
	}
	public void setAllHistogramsWidth(int allHistogramsWidth) {
		AllHistogramsWidth = allHistogramsWidth;
	}
	public void setAllHistogramsHeigth(int allHistogramsHeigth) {
		AllHistogramsHeigth = allHistogramsHeigth;
	}
	public void setAllScatterWidth(int allScatterWidth) {
		AllScatterWidth = allScatterWidth;
	}
	public void setAllScatterHeigth(int allScatterHeigth) {
		AllScatterHeigth = allScatterHeigth;
	}
    //----------------------------------------------------------
    //----------------------------------------------------------
   
    
    //------------------------------------------------------------
    public boolean prepareVizualizerMemory( String ARFFFileName )
    //------------------------------------------------------------
    {
    	pictureCache=null;
    	resorted=false;
    	covarianceMatrix = correlationMatrix = null;
        gdto = arout.getCategoriesAndOtherStuff( ARFFFileName , cmcMachineLearningConstants.NBR_BINS , false ); 	
        if( gdto == null ) { LastErrorMsg = arout.getLastErrorMsg(); return false; }
        if( gdto.isValid() == false ) { LastErrorMsg = arout.getLastErrorMsg(); return false; }
    	boolean ib = makeARFFImageFromModel( gdto.getXa() , gdto.getCategories() , gdto.getNbrOfBins() , xMSet.getBayesWEKAARFFImageName() , true );
    	if( ib == false ) { gdto=null; return false; }
    	return true;
    }
   
    //------------------------------------------------------------
    public boolean makeARFFImageFromFile( String ARFFFileName , int nbins )
    //------------------------------------------------------------
    {
    	pictureCache=null;
    	resorted=false;
        gdto = arout.getCategoriesAndOtherStuff( ARFFFileName , nbins , false ); // fixed number of bins
        if( gdto == null ) { LastErrorMsg = arout.getLastErrorMsg(); return false; }
        if( gdto.isValid() == false ) { LastErrorMsg = arout.getLastErrorMsg(); return false; }
    	boolean ib = makeARFFImageFromModel( gdto.getXa() , gdto.getCategories() , gdto.getNbrOfBins() , xMSet.getBayesWEKAARFFImageName() , false );
    	if( ib == false ) { gdto=null; return false; }
    	return true;
    }
    
    //------------------------------------------------------------
    public boolean makeARFFImageFromModel(cmcARFF xa , ARFFCategory[] categories , int nbins , String ImageFileName , boolean PrepareOnly )
    //------------------------------------------------------------
    {
    	pictureCache=null;
    	resorted=false;
        if( xa == null ) return false;
        if( xa.getAttributeList().length < 1 ) { do_error("Nothing to visualize"); return false; }
        int nattribs = xa.getAttributeList().length;
        int nvalids = 0;
        String[] ClassNameList=null;
        for(int i=0 ; i<nattribs ; i++)
        {
        	cmcARFF.ARFFattribute attrib = xa.getAttributeList()[i];
        	if( attrib == null ) continue;
        	if( attrib.getTipe() == cmcARFF.ARFF_TYPE.STRING ) continue;
        	if( attrib.getTipe() == cmcARFF.ARFF_TYPE.CLASS ) {
        		ClassNameList = attrib.getClasses();
        	}
        	nvalids++;
        }
        if( nvalids < 0 ) {
        	do_error("There are no attributes differnt from STRING"); 
        	return false;
        }
        if( categories == null ) { do_error("Category is null"); return false; }
        if( categories.length != nattribs ) { do_error("#Categories does not match #Attribs [" + categories.length + "] [" + nattribs + "]"); }
        if( ClassNameList == null ) { do_error("ClassNameList is null"); return false; }
        if( ClassNameList.length < 1 ) { do_error("ClassNameList is empty"); return false; }
        do_log( 1 , "Vizualizer [#Attrs=" + nattribs + "] [#Valids=" + nvalids + "] [Classes=" + ClassNameList.length + "]" ) ;
        // 
        if( PrepareOnly ) return true;
        if( gdto == null ) return true;  // sometimes this function is called without GDTO beibng populated
        return dumpAllHistograms( ImageFileName );
    }
  
    //------------------------------------------------------------
    private int getValidFeatures(String caller)
    //------------------------------------------------------------
    {
    	if( gdto == null ) { do_error( "(getValidFearures) Null gdto - called by [" + caller + "]");  return -1; }
        int nvalids = 0;
        for(int i=0 ; i<gdto.getCategories().length ; i++)
        {
         	ARFFCategory cat = gdto.getCategories()[i];
         	if( cat == null ) continue;
         	if( cat.getTipe() == cmcARFF.ARFF_TYPE.STRING ) continue;
         	nvalids++;
        }
        if( nvalids < 1 ) { do_error("(getValidFearures) No valid features found [" + caller + "]"); return -1; }
        return nvalids;
    }
    
    //------------------------------------------------------------
    public boolean dumpAllHistograms( String ImageFileName )
    //------------------------------------------------------------
    {
    	int nvalids = getValidFeatures("dumpAllHistograms");
    	if( nvalids < 0 ) return false;
    	// make a canvas
        int MARGIN = 30;
        int canvas_width = (diarout.getTileWidth() + MARGIN) * 3;
        int canvas_heigth = (diarout.getTileHeigth() + MARGIN) * (((nvalids+1) / 3) + 1);
        int[] canvas = new int[ canvas_width * canvas_heigth ];
        AllHistogramsWidth = canvas_width;
        AllHistogramsHeigth = canvas_heigth;
        for(int i=0;i<canvas.length;i++) canvas[i] = cmcProcConstants.WIT;
        // populate the canvas with key figures
        int[] nbrpix = displayARFFStatistics( gdto.getXa() , gdto.getCategories() , gdto.getClassNameList() );
        if( nbrpix == null ) return false;
        pasteSnippet( canvas , (MARGIN/2) , (MARGIN/2) , canvas_width , nbrpix , diarout.getTileWidth() );
        //
        int cnt=0;
        for(int i=0 ; i<gdto.getCategories().length ; i++)
        {
        	ARFFCategory cat = gdto.getCategories()[i];
        	if( cat == null ) continue;
        	if( cat.getTipe() == cmcARFF.ARFF_TYPE.STRING ) continue;
        	int[] pix = makeHistogram( cat , gdto.getClassNameList() , gdto.getNbrOfBins() , false );
        	if( pix == null ) return false;
       
            int cx = ((cnt+1) % 3) * (diarout.getTileWidth() + MARGIN) + (MARGIN/2);
            int cy = ((cnt+1) / 3) * (diarout.getTileHeigth() + MARGIN) + (MARGIN/2);
            pasteSnippet( canvas , cx , cy , canvas_width , pix , diarout.getTileWidth() );
            cnt++;
            pix = null;
        }
        // ARGB indien transparency
        irout.writePixelsToFile( canvas , canvas_width , canvas_heigth ,  ImageFileName , cmcImageRoutines.ImageType.RGB );
        do_log( 1 , "ARFF histo overview created [" + ImageFileName  + "]");
    	return true;
    }
    
    //------------------------------------------------------------
    public boolean dumpAllScatterDiagrams( String ImageFileName )
    //------------------------------------------------------------
    {
    	int nvalids = getValidFeatures("dumpAllScatterDiagrams");
    	if( nvalids < 0 ) return false;
    	// make a canvas
        int MARGIN = 30;
        int canvas_width  = (diarout.getRectangleSide() + MARGIN) * nvalids; 
        int canvas_heigth = canvas_width;
        int[] canvas = new int[ canvas_width * canvas_heigth ];
        AllScatterWidth = canvas_width;
        AllScatterHeigth = canvas_heigth;
        for(int i=0;i<canvas.length;i++) canvas[i] = cmcProcConstants.WIT;
        int y=-1;
        for(int i=0 ; i<gdto.getCategories().length ; i++)
        {
        	ARFFCategory cat1 = gdto.getCategories()[i];
        	if( cat1 == null ) continue;
        	if( cat1.getTipe() == cmcARFF.ARFF_TYPE.STRING ) continue;
        	y++;
        	int x=-1;
        	for(int j=0;j<gdto.getCategories().length;j++)
        	{
        		ARFFCategory cat2 = gdto.getCategories()[j];
            	if( cat2 == null ) continue;
            	if( cat2.getTipe() == cmcARFF.ARFF_TYPE.STRING ) continue;
            	x++;
            	int[] pix = null;
            	if( i == j ) pix = makeHistogram(cat1, gdto.getClassNameList() , gdto.getNbrOfBins() , true );
            	        else pix = makeScatter( cat1 , cat2 , gdto.getClassNameList() );
            	if( pix == null ) return false;
            	int cx = x * (diarout.getRectangleSide() + MARGIN) + (MARGIN/2);
                int cy = y * (diarout.getRectangleSide() + MARGIN) + (MARGIN/2);
                pasteSnippet( canvas , cx , cy , canvas_width , pix , diarout.getRectangleSide() );
                pix=null;
        	}
        }
        // ARGB indien transparency
        irout.writePixelsToFile( canvas , canvas_width , canvas_heigth ,  ImageFileName , cmcImageRoutines.ImageType.RGB );
        do_log( 1 , "ARFF scatter overview created [" + ImageFileName  + "]");
    	return true;
    }
    //------------------------------------------------------------
    private boolean performResort()
    //------------------------------------------------------------
    {
    	 int nrows=-1;
    	 for(int i=0;i<gdto.getCategories().length;i++)
    	 {
    		ARFFCategory cat = gdto.getCategories()[i];
           	if( cat == null ) continue;
           	if( cat.getTipe() == cmcARFF.ARFF_TYPE.STRING ) continue;
            if( nrows < 0 ) nrows=cat.getValues().length;
            if( nrows != cat.getValues().length ) { do_error("Number of lines does not match"); return false; }
            //  CAUTION : the value lists on the various categories must be re-ordered by their line number
            if( cat.restoreLineNumberOrder() == false ) { do_error("Could not reorder"); return false; } 
    	 }
    	 do_log( 1 , "Resorted all categories on line number");
    	 resorted=true;
     	 return true;
    }
    
    //------------------------------------------------------------
    private void pasteSnippet( int[] target , int tx , int ty , int target_width , int[] src , int src_width )
    //------------------------------------------------------------
    {
    	int target_heigth = target.length / target_width;
    	int src_heigth = src.length / src_width;
    	for(int y=0;y<src_heigth;y++)
    	{
    		int z = ((ty + y) * target_width) + tx;
    		int k = y * src_width;
    		for(int x=0;x<src_width;x++)
    		{
    			target[z] = src[k];
    			z++;
    			k++;
    		}
    	}
    }
    
    //------------------------------------------------------------
    private int[] makeHistogram( ARFFCategory cat , String[] ClassNameList , int nbins , boolean squared )
    //------------------------------------------------------------
    {
    	try  {
    	  if( cat == null ) return null;
    	  if( cat.getTipe() == ARFF_TYPE.STRING ) return null;
    	  int nclasses = ClassNameList.length;
    	  int[][] subhisto = new int[ nbins ][ nclasses ];
    	  for(int i=0;i<subhisto.length;i++)
    	  {
    		  for(int j=0;j<subhisto[i].length;j++) subhisto[i][j]=0;
    	  }
     	  for(int i=0;i<cat.values.length;i++)
    	  {
    		  subhisto[ cat.binIndexes[i] ][ (int)cat.results[i] ]++;
    	  }
     	  gpDiagramDTO dto = new gpDiagramDTO( subhisto , ClassNameList );
     	  String sDetail = "(" + cat.getTipe() + ")";
     	  if( (cat.getTipe() == cmcARFF.ARFF_TYPE.CLASS) || (cat.getTipe() == cmcARFF.ARFF_TYPE.NOMINAL) ) {
     		  if( cat.getNominalValueList() != null ) {
     		   sDetail = "";
     		   for(int i=0;i<cat.getNominalValueList().length;i++)
     		   {
     			sDetail += (i==0) ? cat.getNominalValueList()[i] : "," + cat.getNominalValueList()[i];
     		   }
     		   sDetail = "{" + sDetail + "}";
     		  }
     	  }
     	  dto.setxLabel( cat.getCategoryName() + " " + sDetail );
     	  return diarout.makeHistogram( dto , squared );
    	}
    	catch( Exception e) {
    		do_error("makeHistogram - Oops " + xMSet.xU.LogStackTrace(e));
    		return null;
    	}
    }

    //----------------------------------------------------------
    public BufferedImage getPictureFromCache( String CategoryName1 , String CategoryName2 , cmcMachineLearningEnums.PictureType tipe )
    //------------------------------------------------------------
    {
    	if( (CategoryName1 == null) || (CategoryName2 == null) ) return null;
    	if( pictureCache != null ) {
    	  for(int i=0;i<pictureCache.size();i++)
    	  {
    		if( CategoryName1.compareToIgnoreCase( pictureCache.get(i).label1 ) != 0 ) continue;
    		if( CategoryName2.compareToIgnoreCase( pictureCache.get(i).label2 ) != 0 ) continue;
    		if( tipe != pictureCache.get(i).tipe ) continue;
    		if (pictureCache.get(i).img != null) return pictureCache.get(i).img ;
    	 }
    	}
    	boolean ib  = putPictureInCache( CategoryName1 , CategoryName2 , tipe );
    	if( ib == false ) return null;
    	return pictureCache.get( pictureCache.size()-1 ).img;
    }
    
    //----------------------------------------------------------
    public void purgeCache()
    //----------------------------------------------------------
    {
    	pictureCache = null;
    }
      
    //------------------------------------------------------------
    private boolean putPictureInCache( String CategoryName1 , String CategoryName2 , cmcMachineLearningEnums.PictureType tipe ) 
    //------------------------------------------------------------
    {
    	try {
    	if( gdto == null ) return false;
    	ARFFCategory cat1 = null;
    	for(int i=0;i<gdto.getCategories().length;i++)
    	{
    	   if( gdto.getCategories()[i].getCategoryName().compareToIgnoreCase( CategoryName1 ) != 0 ) continue;
    	   cat1 = gdto.getCategories()[i];
    	   break;
    	}
    	if( cat1 == null ) {
    		do_error("Cannot locate category [" + CategoryName1 + "] to create diagram");
    		return false;
    	}
    	//
    	ARFFCategory cat2 = null;
    	for(int i=0;i<gdto.getCategories().length;i++)
    	{
    	   if( gdto.getCategories()[i].getCategoryName().compareToIgnoreCase( CategoryName2 ) != 0 ) continue;
    	   cat2 = gdto.getCategories()[i];
    	   break;
    	}
    	if( cat2 == null ) {
    		do_error("Cannot locate category [" + CategoryName2 + "] to create diagram");
    		return false;
    	}
    	//
    	if( pictureCache == null ) pictureCache = new ArrayList<PictureCacheDTO>();
    	PictureCacheDTO pca = new PictureCacheDTO();
    	pca.label1 = CategoryName1;
    	pca.label2 = CategoryName2;
    	pca.tipe = tipe;
    	// 
    	int width = -1;
    	int[] pix = null;
    	switch( tipe )
    	{
    	case HISTOGRAM : { pix = makeHistogram(cat1, gdto.getClassNameList() , gdto.getNbrOfBins() , false ); width = getTileWidth(); break; }
    	case SCATTER   : { pix = makeScatter( cat1, cat2 ,gdto.getClassNameList() ); width = getRectangleSide(); break; }
    	case CORRELATION : { pix = makeCorrelationDiagram(); width = getCorrelationWidth(); break; }
    	default : { do_error( "unsupported type [" + tipe + "]"); break; }
    	}
        if( pix == null ) { do_error( "NULL picture " + tipe + " " + CategoryName2 + " " + CategoryName1); return false; }
        if( width < 0 ) { do_error ("zero width " + tipe + " " + CategoryName2 + " " + CategoryName1); return false; }
        pca.img = new BufferedImage(width, pix.length / width, BufferedImage.TRANSLUCENT);
	    pca.img.setRGB(0, 0, width, pix.length / width, pix , 0, width);
    	//
	    do_log( 1, "Added [" + cat1.getCategoryName() + "-" + cat2.getCategoryName() + "] to cache [#=" + pictureCache.size() + "] "+ tipe);
    	pictureCache.add( pca );
    	return true;
    	}
    	catch(Exception e) {
    		e.printStackTrace();
    		return false;
    	}
    }
    //------------------------------------------------------------
    private boolean withinToleranceLimits( double a , double b)
    //------------------------------------------------------------
    {
    	double diff = Math.abs( a - b);
    	if( diff < cmcMachineLearningConstants.TOLERANCE_LIMIT ) return true;
    	return false;
    }
    //------------------------------------------------------------
    private int[] makeScatter( ARFFCategory cat1 , ARFFCategory cat2 , String[] ClassNameList )
    //------------------------------------------------------------
    {
    	 if( cat1 == null ) return null;
   	     if( cat1.getTipe() == ARFF_TYPE.STRING ) return null;
   	     if( cat2 == null ) return null;
	     if( cat2.getTipe() == ARFF_TYPE.STRING ) return null;
	     if( ClassNameList == null ) return null;
	     if( resorted == false ) {
	    	 if ( performResort() == false) return null;
	     }
	   
   	     int nvals = cat1.getValues().length;
   	     if( nvals != cat2.getValues().length ) {
   	    	 do_error("Number of values differs");
   	    	 return null;
   	     }
   	     if( nvals < 1) {
   	    	 do_error("There is nothing to process");
   	    	 return null;
   	     }
   	     double[] xvals = new double[nvals];
   	     for(int i=0;i<nvals;i++) xvals[i] = cat1.getValues()[i];
   	     double[] yvals = new double[nvals];
	     for(int i=0;i<nvals;i++) yvals[i] = cat2.getValues()[i];
	     
	     // Normalize or not
	     //do_error( "=> "  + normalizeValues );
	     
	     if( stat == null ) stat = new gpDoubleListStatFunctions( xvals , logger );
	     double xmean   = stat.getMean();
	     double xstddev = stat.getStandardDeviation();
	     if( xstddev == 0 ) { do_error("X STDDEV is zero"); return  null; }
	     //do_log( 1 , "x" + xmean + " " + xstddev );
   	     // 
	     stat.calculate( yvals );
	     double ymean   = stat.getMean();
	     double ystddev = stat.getStandardDeviation();
	     if( ystddev == 0 ) { do_error("Y STDDEV is zero"); return  null; }
	     //do_log( 1 , "y" + ymean + " " + ystddev );
	     
	     //
         if( normalizeValues ) {
	      for(int i=0;i<nvals;i++) xvals[i] = (xvals[i] - xmean) / xstddev; 
	      for(int i=0;i<nvals;i++) yvals[i] = (yvals[i] - ymean) / ystddev;
         }
         
         // transpose to 0..1
	     double minx=xvals[0];
	     double maxx=xvals[0];
	     double miny=yvals[0];
	     double maxy=yvals[0];
	     for(int i=0;i<nvals;i++)
	     {
	    	 if( xvals[i] > maxx ) maxx = xvals[i];
	    	 if( xvals[i] < minx ) minx = xvals[i];
	    	 if( yvals[i] > maxy ) maxy = yvals[i];
	    	 if( yvals[i] < miny ) miny = yvals[i];	 
	     }
	     double xw = maxx - minx;
	     double yw = maxy - miny;
	     if( xw == 0 ) { do_error( "X width is zero"); return null; }
	     if( yw == 0 ) { do_error( "Y width is zero"); return null; }
	     for(int i=0;i<nvals;i++)
	     {
	    	 xvals[i] = (xvals[i] - minx) / xw;
	    	 yvals[i] = (yvals[i] - miny) / yw;
	     }
	     // 
	     // check
	     if( cat1.getCategoryName().compareToIgnoreCase(cat2.getCategoryName()) == 0 ) {
	    	 for(int i=0;i<nvals;i++)
	    	 {
	    		 if( xvals[i] != yvals[i] ) {
	    		   if( withinToleranceLimits( xvals[i] , yvals[i]) == false ) {
	    			 do_error( "System error - The same categories calculate different" + xvals[i] + " " + yvals[i] );
	    			 return null;
	    		   }
	    		 }
	    	 }
	     }
	     // results
	     double[] results = new double[ nvals ];
	     for(int i=0;i<nvals;i++)
	     {
	    	 if( cat1.getResults()[i] != cat2.getResults()[i] ) {
	    		 do_error( "System error - results are not the same for same measurement" +  cat1.getResults()[i] + " " +  cat2.getResults()[i] );
    			 return null;
	    	 }
	    	 if( (cat1.getResults()[i] < 0) || (cat1.getResults()[i] >= ClassNameList.length) ) {
	    		 do_error( "System error - results out of bound" +  cat1.getResults()[i] + " " +  ClassNameList.length );
    			 return null;
	    	 }
	    	 results[i] = cat1.getResults()[i];
	     }
	    
	     do_log( 1 , "[Scatter=" + cat1.getCategoryName() + "] [" + xw  + " " + minx + "]" + cat2.getCategoryName() + "[" + yw + " " + miny  + "]");
	     //
	     gpDiagramDTO dto = new gpDiagramDTO( xvals , yvals , results);
	     dto.setxLabel( cat1.getCategoryName() );
	     dto.setyLabel( cat2.getCategoryName() );
	     //
	     return diarout.drawScatterPlot( dto);
    }
        
    //------------------------------------------------------------
    private int[] displayARFFStatistics( cmcARFF xa , ARFFCategory[] categories , String[] ClassNameList )
    //------------------------------------------------------------
    {
      int TILE_WIDTH  = diarout.getTileWidth();
      int TILE_HEIGTH = diarout.getTileHeigth();
      Color TextColor = Color.BLACK;
      int[] tile = new int[ TILE_HEIGTH * TILE_WIDTH ];
   	  for(int i=0;i<tile.length;i++) tile[i] = cmcProcConstants.WIT;
   	  int xLne = TILE_WIDTH / 20;
   	  int yLne = TILE_HEIGTH / 20;
   	  Font f = xMSet.getPreferredFont();
   	  int lineHeigth = 20;
	  String ClassNames="";
	  String ShortFileName = xMSet.xU.GetFileName(xa.getLongFileName());
	  for(int i=0;i<ClassNameList.length;i++) ClassNames += "[" + ClassNameList[i] + "]";
	  //
   	  drwtxt.drawPixelText(f, TextColor , xLne , yLne , tile, TILE_WIDTH, "Name = " + xa.getARFFName() );
   	  yLne += lineHeigth;
   	  drwtxt.drawPixelText(f, TextColor , xLne , yLne , tile, TILE_WIDTH, "File = " +ShortFileName );
 	  yLne += lineHeigth;
   	  drwtxt.drawPixelText(f, TextColor , xLne , yLne , tile, TILE_WIDTH, "#Lines = " + xa.getNbrOfLines() );
   	  yLne += lineHeigth;
  	  drwtxt.drawPixelText(f, TextColor , xLne , yLne , tile, TILE_WIDTH, "#Duplicates = " + xa.getNbrOfDuplicateLines() );
  	  yLne += lineHeigth;
 	  drwtxt.drawPixelText(f, TextColor , xLne , yLne , tile, TILE_WIDTH, "#Collisions = " + xa.getNbrOfCollissions() );
 	  yLne += lineHeigth;
	  drwtxt.drawPixelText(f, TextColor , xLne , yLne , tile, TILE_WIDTH, ClassNames );
   	     
   	  return tile;
    }
    
    /*
     * 
     * //-----------------------------------------------------------------------
		public static BufferedImage doeResize(BufferedImage image, int width, int heigth)
		//-----------------------------------------------------------------------
		{
		    //System.err.println("resize " + width +" " + heigth + " MXH=" + MAXH);
		    BufferedImage bi = new BufferedImage(width, heigth, BufferedImage.TRANSLUCENT);
		    Graphics2D g2d = (Graphics2D) bi.createGraphics();
		    g2d.addRenderingHints(new RenderingHints(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY));
		    g2d.drawImage(image, 0, 0, width, heigth, null);
		    g2d.dispose();
		    return bi;
		}
     * 
     */
   
    //------------------------------------------------------------
    private int[] makeCorrelationDiagram()
    //------------------------------------------------------------
    {
    	if( calculateAllCorrelations() == false ) return null;
        gpDiagramDTO dto = new gpDiagramDTO( correlationMatrix );
 	    dto.setxLabel( "" );
 	    dto.setyLabel( "" );
 	    String[] vertLabels = new String[ gdto.getCategories().length ];
 	    for(int i=0;i<gdto.getCategories().length;i++)
   	    {
   	      vertLabels[i] = gdto.getCategories()[i].getCategoryName();
   	    }
 	    dto.setVerticalTickLabels(vertLabels);
 	    return diarout.drawCorrelationDiagram( dto);
    }
    
    //------------------------------------------------------------
    private boolean calculateAllCorrelations()
    //------------------------------------------------------------
    {
    	if( covarianceMatrix != null )  return false;  // already calculated
    	if( gdto == null )  { do_error( "no GDTO"); return false; }
    	if( gdto.getCategories() == null )  { do_error( "NULL categories"); return false; }
    	if( gdto.getCategories().length < 1 ) { do_error( "Empty categories"); return false; }
    	int ndims = gdto.getCategories().length;
    	
    	if( addstat == null ) addstat = new gpStatAdditionalFunctions();
    	covarianceMatrix = new double[ ndims ][ ndims ];
    	correlationMatrix = new double[ ndims ][ ndims ];
    	for(int i=0;i<ndims;i++) { for(int j=0;j<ndims;j++) {covarianceMatrix[i][j] = correlationMatrix[i][j] = Double.NaN; }}
    	ARFFCategory cat1 , cat2 = null;
    	for(int i=0;i<gdto.getCategories().length;i++)
    	{
    	   cat1 = gdto.getCategories()[i];
    	   if( cat1.getTipe() == cmcARFF.ARFF_TYPE.STRING ) { correlationMatrix[ i ][ i ] = 1; continue; }
    	   if( cat1.getTipe() == cmcARFF.ARFF_TYPE.CLASS ) { correlationMatrix[ i ][ i ] = 1; continue; }
    	   if( cat1.getTipe() == cmcARFF.ARFF_TYPE.NOMINAL ) { correlationMatrix[ i ][ i ] = 1; continue; }
    	   for(int j=0; j < gdto.getCategories().length;j++)
    	   {
    		   cat2 = gdto.getCategories()[j];
    		   if( cat2.getTipe() == cmcARFF.ARFF_TYPE.STRING ) continue;
    		   if( cat2.getTipe() == cmcARFF.ARFF_TYPE.CLASS ) continue;
    		   if( cat2.getTipe() == cmcARFF.ARFF_TYPE.NOMINAL ) continue;
    		   if( Double.isNaN( covarianceMatrix[i][j] ) == false ) continue; // already processed 
    		   double[] ret = calculateCorrelation( cat1 , cat2 );
    		   if( ret == null ) return false;
    		   //do_log( 1 , cat1.getCategoryName() + " " + cat2.getCategoryName() + " " + ret[1] + " " + ret[0]);
    		   covarianceMatrix[ i ][ j ] =  covarianceMatrix[ j ][ i ] = ret[0];
    		   correlationMatrix[ i ][ j ] =  correlationMatrix[ j ][ i ] = ret[1];
    	   }
    	}
    	return true;
    }
    
    // 0 covariance 1 correlation
    //------------------------------------------------------------
    private double[] calculateCorrelation( ARFFCategory cat1 , ARFFCategory cat2  )
    //------------------------------------------------------------
    {
    	
        	 if( cat1 == null ) return null;
       	     if( cat1.getTipe() == ARFF_TYPE.STRING ) return null;
       	     if( cat2 == null ) return null;
    	     if( cat2.getTipe() == ARFF_TYPE.STRING ) return null;
    	     if( resorted == false ) {
    	    	 if ( performResort() == false) return null;
    	     }
    	   
       	     int nvals = cat1.getValues().length;
       	     if( nvals != cat2.getValues().length ) {
       	    	 do_error("Number of values differs");
       	    	 return null;
       	     }
       	     if( nvals < 1) {
       	    	 do_error("There is nothing to process");
       	    	 return null;
       	     }
       	     double[] xvals = new double[nvals];
       	     for(int i=0;i<nvals;i++) xvals[i] = cat1.getValues()[i];
       	     double[] yvals = new double[nvals];
    	     for(int i=0;i<nvals;i++) yvals[i] = cat2.getValues()[i];
    	     
    	     boolean ib = addstat.calcCovariance( xvals , yvals );
    	     if( ib == false ) return null;
    	     double[] ret = new double[2];
    	     ret[0] = addstat.getCovariance();
    	     ret[1] = addstat.getCorrelation();
    	     return ret;
    }
}
