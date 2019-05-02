package ocr;

import java.awt.Color;
import java.awt.Font;
import java.util.ArrayList;
import java.util.Random;

import textProcessing.cmcTextObject;
import generalImagePurpose.cmcImageRoutines;
import generalImagePurpose.cmcProcConvolution;
import generalImagePurpose.gpLoadImageInBuffer;
import generalStatPurpose.gpListStatFunctions;
import generalpurpose.gpExecBatch;
import generalpurpose.gpInterrupt;
import generalpurpose.gpPrintStream;
import logger.logLiason;
import cbrTekStraktorModel.cmcProcConstants;
import cbrTekStraktorModel.cmcProcEnums;
import cbrTekStraktorModel.cmcProcSettings;
import dao.cmcArchiveDAO;
import dao.cmcBookMetaDataDAO;
import dao.cmcGraphPageDAO;
import dao.cmcTesseractParameterDAO;
import dao.cmcTextDAO;
import drawing.cmcDrawPixelText;
import drawing.cmcGraphPageObject;

public class cmcOCRController {

	private boolean DEBUG = false;
	
	public enum TESSERACTCOMMAND  { DO_VERSION , DO_OCR }
	
	int MAX_SPACE_BETWEEN_WORDS = 120;
	
	cmcProcSettings xMSet=null;
	logLiason logger=null;
	cmcGraphPageObject[] ar_pabo=null;
	gpInterrupt irq=null;
	
	private String OCRFontName = "arial";
	private boolean doNotOverWriteText = false;
	private boolean UseOriginalImageFile = false;  // false = BlackAndWhite
	private boolean puttext=true;
	private int[] blackwhiteoriginal=null;
	private int[] rapport=null;
	private String originalFileName=null;
	private int payLoadX=0;
	private int payLoadY=0;
	private int uncroppedWidth=0;
	private int uncroppedHeigth=0;
	private int payLoadWidth=0;
	private int payLoadHeigth=0;
	private int realPayLoadX=0;
	private int realPayLoadY=0;
	private String CMXUID;
	private String UID;
	private String cmxLanguage=null;
	private String qualitymarker=null;
	private String preamble = "P";
	private String postamble = "-";
	private int dpi=-1;
	private cmcProcEnums.OCR_CURATION cmxTesseractCuration = cmcProcEnums.OCR_CURATION.IGNORE_DPI;
	// 2018
	private int[] OCRSeeds;
	private int OCRPointer=0;
	private char OCRTagMarker = 'X';
	private String shortOriginalFileName = null;
	private String TesseractVersion = null;
	
	class OCRTempItem  // temp
	{
		int grapObjIdx=-1;
		int MinX=-1;
		int MinY=-1;
		int width=-1;
		int heigth=-1;
		int BottomY=-1;
		int MidX=-1;
		int run=-1;
		int delta=-1;
	}
	
	// May2018 added shallowcopy
	class OCRLineItem
	{
		int GraphObjIdx=-1;
		int AbsX=-1;
		int AbsY=-1;
		int RelX=-1;
		int RelY=-1;
		int width=-1;
		int heigth=-1;
		boolean OutOfBound=false;
		OCRLineItem(int idx)
		{
			GraphObjIdx=idx;
			AbsX=-1;
			AbsY=-1;
			RelX=-1;
			RelY=-1;
			width=-1;
			heigth=-1;
			OutOfBound=false;
		}
		void shallowCopy(OCRLineItem x)
		{
			this.GraphObjIdx = x.GraphObjIdx;
			this.AbsX        = x.AbsX;
			this.AbsY        = x.AbsY;
			this.RelX        = x.RelX;
			this.RelY        = x.RelX;
			this.width       = x.width;
			this.heigth      = x.heigth;
			this.OutOfBound  = x.OutOfBound;
		}
	}
	
	class OCRLine
	{
		int linenumber=-1;
		OCRLineItem[] ar_item=null;
		int MinX=-1;
		int MaxX=-1;
		int MinY=-1;
		int MaxY=-1;
		int MinWidth=-1;
		int MaxWidth=-1;
		int MinHeigth=-1;
		int MaxHeigth=-1;
		int MedianWidth=-1;
		int MedianHeigth=-1;
		int MinDistance=-1;
		int MaxDistance=-1;
		int quartileHeigth=-1;
		int totalWidth=-1;
		int relX=-1;
		OCRLine(int li, int max)
		{
			linenumber=li;
			ar_item = new OCRLineItem[max];
			for(int i=0;i<ar_item.length;i++) ar_item[i]=null;
			MinX=-1;
			MaxX=-1;
			MaxX=-1;
			MinY=-1;
			MinWidth=-1;
			MaxWidth=-1;
			MinHeigth=-1;
			MaxHeigth=-1;
			MedianWidth=-1;
			MedianHeigth=-1;
			MinDistance=-1;
			MaxDistance=-1;
			quartileHeigth=-1;
			totalWidth=-1;
			relX=-1;
		}
	}
	
	// 2018 May created OCRTag to further enable detecting OCRLines in Tesseract output
	class OCRParagraph
	{
		long UID=-1L;
		String OCRTag=null;
		OCRLine[] ar_line=null;
		int MinX=-1;
		int MaxX=-1;
		int Width=-1;
		int Height=-1;
		
		int MinWidth=-1;
		int MaxWidth=-1;
		int MedianWidth=-1;
		int quartile1Width=-1;
		int quartile3Width=-1;
		
		int MinDistance=-1;
		int MaxDistance=-1;
		
		int MinHeigth=-1;
		int MaxHeigth=-1;
		int MedianHeigth=-1;
		int quartile1Heigth=-1;
		int quartile3Heigth=-1;
		
		boolean ingested=false;
		String OCR=null;
		OCRParagraph(long ul,int max)
		{
			UID=ul;
			OCRTag= getOCRTag(ul);
			ar_line = new OCRLine[max];
			for(int i=0;i<max;i++) ar_line[i] = null;
			MinX=-1;
			MaxX=-1;
			Width=-1;
			Height=-1;
			
			MinWidth=-1;
			MaxWidth=-1;
			MedianWidth=-1;
			quartile1Width=-1;
			quartile3Width=-1;
		
			MinHeigth=-1;
			MaxHeigth=-1;
			MedianHeigth=-1;
			quartile1Heigth=-1;
			quartile3Heigth=-1;
			
			MinDistance=-1;
			MaxDistance=-1;
		
			ingested=false;
			OCR=null;
		}
	}
	ArrayList<OCRParagraph> list = null;
	
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
	//-----------------------------------------------------------------------
	public cmcOCRController(cmcProcSettings is,logLiason ilog)
	//-----------------------------------------------------------------------
	{
		xMSet = is;
		logger=ilog;
		irq = new gpInterrupt(xMSet.getInterruptFileName());
    }

	//-----------------------------------------------------------------------
	private String getOCRTag(long ul)
	//-----------------------------------------------------------------------
	{
		if( OCRPointer >= OCRSeeds.length ) return "x" + ul + "y"; // too many lines - default to just the UID!
		String segment = String.format( "%03d" , OCRSeeds[OCRPointer] ).trim();
		OCRPointer++;
		String ret = ( OCRTagMarker + segment + OCRTagMarker + segment + OCRTagMarker + segment + OCRTagMarker).toLowerCase();
		return ret;
	}

	//-----------------------------------------------------------------------
	private void resetOCRTags()
	//-----------------------------------------------------------------------
	{
	   OCRPointer=0;
	   OCRSeeds = new int[999];
	   for(int i=0;i<OCRSeeds.length;i++) OCRSeeds[i]=-1;
	   Random rand = new Random(System.nanoTime());
	   for(int i=0;i<OCRSeeds.length;i++)
	   {
		 int x =- 1;
		 while ( true )
		 {
		  x = rand.nextInt(999);
		  boolean found = false;
		  for(int j=0;j<i;j++)
		  {
			  if( x == OCRSeeds[j] ) { found = true; break; }
		  }
		  if( found == false ) break;
		 }
		 OCRSeeds[i] = x;
	   }
	}
	
	//-----------------------------------------------------------------------
	public boolean prepareOCRImage()
	//-----------------------------------------------------------------------
	{
		shortOriginalFileName = null;
		xMSet.setOCRSummaryResult(null);
		ar_pabo=null;
		if( readXML() == false ) return false;
		xMSet.setOCRSummaryResult(shortOriginalFileName);
		if( extractBinarizedImageFile() == false ) return false;
		if( readMetaData() == false ) return false;
		resetOCRTags();
	    list = new ArrayList<OCRParagraph>();
		// loop through the paragraphs and extract lines
		for(int i=0;i<ar_pabo.length;i++)
		{
		  if( ar_pabo[i].tipe != cmcProcEnums.PageObjectType.TEXTPARAGRAPH ) continue;
		  if( ar_pabo[i].removed ) continue;
		  if (extractLinesFromParagraph( i ) == false ) return false;
		}
		// stop
		if( irq.gotInterrupt() )  {
			do_log(1,"Got an IRQ on overlay");
			return false;
		}
		// overlayen
	    boolean ib = do_overlay(UseOriginalImageFile);	
	    do_log(5,"Overlay created status [" + ib + "]");
		return true;
	}
	
	//-----------------------------------------------------------------------
	private boolean readXML()
	//-----------------------------------------------------------------------
	{
		cmcGraphPageDAO dao = new cmcGraphPageDAO( xMSet , xMSet.getCurrentArchiveFileName() , logger );
		ar_pabo = dao.readXML();
        if( ar_pabo == null ) {
        	do_error("Could not load archive file [" + xMSet.getCurrentArchiveFileName() + "]");
        	return false;
        }
        shortOriginalFileName = dao.getOrigFileName();
        originalFileName = dao.getOrigFileDir() + xMSet.xU.ctSlash + dao.getOrigFileName();
        uncroppedWidth = dao.getImageWidth();
        uncroppedHeigth = dao.getImageHeigth();
        payLoadWidth = dao.getPayLoadWidth();
        payLoadHeigth = dao.getPayLoadHeigth();
        payLoadX = dao.getPayLoadX();
        payLoadY = dao.getPayLoadY();
        CMXUID = dao.getCMXUID();
        UID = dao.getUID();
        dpi = dao.getPhysicalWidthDPI();
        return true;
	}
	
	//-----------------------------------------------------------------------
	private boolean readMetaData()
	//-----------------------------------------------------------------------
	{
		if( xMSet.xU.IsBestand( xMSet.getGraphZMetaXML() ) == false ) {
			do_error("Cannot locate Metadatafile [" + xMSet.getGraphZMetaXML() + "]");
			return false;
		}
		cmcBookMetaDataDAO meta = new cmcBookMetaDataDAO( xMSet, null ,null ,logger);
		meta.readMetaDataFile(xMSet.getGraphZMetaXML());
		if( meta.isEmpty() == true ) {
			meta=null;
			do_error("Could not read metadata from [" + xMSet.getGraphZMetaXML() + "]");
			return false;
		}
		cmxLanguage = meta.getLanguage();
		cmxTesseractCuration = meta.getOCRCuration();
		//
		meta=null;
		return true;
	}
	
	
	//-----------------------------------------------------------------------
	private boolean extractBinarizedImageFile()
	//-----------------------------------------------------------------------
	{
      String LongFileName = xMSet.getBinarizedOutputJPGName();
      if( xMSet.xU.IsBestand( LongFileName ) ) {
    	  xMSet.xU.VerwijderBestand( LongFileName );
      }
      if( xMSet.xU.IsBestand( LongFileName ) ) {
    	  do_error("Cannot remove [" + LongFileName + "]");
    	  return false;
      }
      int pdx = LongFileName.lastIndexOf( xMSet.xU.ctSlash );
      if( pdx < 0 ) {
    	  do_error("Cannot extract filename from " + LongFileName );
    	  return false;
      }
      try {
       String ShortFileName = LongFileName.substring(pdx+1);
       cmcArchiveDAO arc = new cmcArchiveDAO(xMSet,logger);
 	   String ret = arc.unzip_SingleFile( xMSet.getCurrentArchiveFileName() , ShortFileName );
 	   arc=null;
	   if( ret == null ) {
		  arc=null;
		  do_error("Could not extact [" + ShortFileName + "] from [" +  xMSet.getCurrentArchiveFileName() + "]");
		  return false;
	   }
	   if( xMSet.xU.IsBestand( LongFileName ) == false ) {
	       do_error("Could not extract [" + ShortFileName + "]");
	       return false;
	   }
	   return true;
      }
      catch (Exception e) {
    	  do_error("Cannot extract filename from " + LongFileName );
    	  return false;
      }
	}
	
	//---------------------------------------------------------------------------------
	private int[] do_ObjectsEnveloped( int idx )
	//---------------------------------------------------------------------------------
	{
		int[] list = new int[ar_pabo.length];
		for(int i=0;i<list.length;i++) list[i] = -1;
	    int minx = ar_pabo[idx].MinX;
	    int miny = ar_pabo[idx].MinY;
	    int maxx = ar_pabo[idx].MaxX;
	    int maxy = ar_pabo[idx].MaxY;
	    
	    int k=0;
	    for(int i=0;i<ar_pabo.length;i++)
	    {
	    	if( ar_pabo[i].removed ) continue;
	    	// Exclude
	    	if( ar_pabo[i].tipe == cmcProcEnums.PageObjectType.FRAME ) continue;
	    	if( ar_pabo[i].tipe == cmcProcEnums.PageObjectType.UNKNOWN ) continue;
	    	if( ar_pabo[i].tipe == cmcProcEnums.PageObjectType.TEXTPARAGRAPH ) continue;
	    	if( ar_pabo[i].tipe == cmcProcEnums.PageObjectType.PARAGRAPH ) continue;
	    	if( ar_pabo[i].tipe == cmcProcEnums.PageObjectType.NOISE ) continue;
            
            //
	    	if( i == idx ) continue;
			if( (ar_pabo[i].MinX < minx) || (ar_pabo[i].MinX > maxx ) ) continue;
			if( (ar_pabo[i].MinY < miny) || (ar_pabo[i].MinY > maxy ) ) continue;
			
			list[k] = i;
			k++;
			if( k >= (list.length-1) ) {
				do_error("Array overrun");
				break;
			}
	    }
		return list;
	}

	//---------------------------------------------------------------------------------
	private boolean extractLinesFromParagraph(int pdx)
	//---------------------------------------------------------------------------------
	{
		int[] ar_tmp = do_ObjectsEnveloped(pdx);
		if( ar_tmp == null ) return false;
		if( ar_tmp.length == 0 ) return true;
		int aantal=0;
		for(int i=0;i<ar_tmp.length;i++)
		{
			if( ar_tmp[i]>=0 ) aantal++;
		}
		if( aantal <= 0 ) {
			do_error("Strange a text paragraph without childobjects [UID=" + ar_pabo[pdx].UID + "]");
			return true;
		}
		// init
        OCRTempItem[] ar_child = new OCRTempItem[aantal];
        for(int i=0;i<ar_child.length;i++) ar_child[i] = new OCRTempItem();
        //
        int j=0;
        for(int i=0;i<ar_tmp.length;i++)
		{
        	if( ar_tmp[i]>=0 ) {
				int cdx = ar_tmp[i];
                //do_error("pdx=" + pdx + " aantal=" + aantal + " pdx=" + cdx + "  pabo=" + ar_pabo.length );
				ar_child[j].grapObjIdx = cdx;
				ar_child[j].MinX = ar_pabo[cdx].MinX;
				ar_child[j].MinY = ar_pabo[cdx].MinY;
				ar_child[j].width = ar_pabo[cdx].MaxX - ar_pabo[cdx].MinX + 1;
				ar_child[j].heigth = ar_pabo[cdx].MaxY - ar_pabo[cdx].MinY + 1;
				ar_child[j].BottomY = ar_pabo[cdx].MaxY;
				ar_child[j].MidX = (ar_child[j].width / 2) + ar_child[j].MinX;
				ar_child[j].run = -1;
				//
				j++;
			}
			
		}
        int MidX = ((ar_pabo[pdx].MaxX - ar_pabo[pdx].MinX + 1) / 2) + ar_pabo[pdx].MinX;
        // median heigth
        int[] hlist = new int[ar_child.length];
        for(int i=0;i<ar_child.length;i++)
        {
         hlist[i] = ar_child[i].heigth;	
        }
        gpListStatFunctions stat = new gpListStatFunctions(hlist,logger);
		int medianHeigth = stat.getMedian();
		int quart1 = stat.getQuartile1();
		int quart3 = stat.getQuartile3();
		hlist=null;
		stat=null;
        //  
        int NbrOfRuns=-1;
        for(int i=0;i<100;i++) // 100 lines seems enough
        {
       	  int res = determineHighestMiddleObject( ar_child , MidX , i , medianHeigth , quart1 , quart3);
       	  if( res == 0 ) {
       		  NbrOfRuns=i;
       		  break;
       	  }
       	  if( res < 0) {
       		  do_error("Something went wrong");
       		  break;
       	  }
        }
        if( NbrOfRuns <= 0  ) {
        	do_error("Strange - No lines detected");
        	return true;
        }
		// are there still objects unallocated
        aantal=0;
        for(int i=0;i<ar_child.length;i++)
        {
        	if( ar_child[i].run == -1 ) aantal++;
        }
        if( aantal != 0 ) {
           do_error("There are still [" + aantal + "] objects unallocated in paragraph [" + pdx + "]");
           return false;
        }
        // store the info
        OCRParagraph para = new OCRParagraph( ar_pabo[pdx].UID , NbrOfRuns );
        for(int k=0;k<NbrOfRuns;k++)  // k = linenumber or run
        {
          aantal=0;
          for(int i=0;i<ar_child.length;i++)
          {
        	  if( ar_child[i].run == k ) aantal++;
          }
          OCRLine x = new OCRLine(k , aantal);
          j=0;
          for(int i=0;i<ar_child.length;i++)
          {
        	  if( ar_child[i].run == k ) {
        		  int p = ar_child[i].grapObjIdx;
        		  OCRLineItem y = new OCRLineItem(p);
        		  y.AbsX = ar_pabo[p].MinX;
        		  y.AbsY = ar_pabo[p].MinY;
        		  y.width  = ar_pabo[p].MaxX - ar_pabo[p].MinX + 1;
        		  y.heigth = ar_pabo[p].MaxY - ar_pabo[p].MinY + 1;
        		  x.ar_item[j] = y;
        		  j++;
        	  }
          }
          // Sort the items on the line
          doSortLineItemsOnLine( x );
          // Averages - min - max etc per line
          doLineStats( x );
          //
          para.ar_line[k] = x;
        }
        doParagraphStats(para);
        //
        doReAlignCharactersAcrossRuns(para);
        //
        list.add(para);
		return true;
	}

	//---------------------------------------------------------------------------------
	private void doSortLineItemsOnLine(OCRLine x)
	//---------------------------------------------------------------------------------
	{
		int aantal = x.ar_item.length;
		OCRLineItem y = new OCRLineItem(-1);
		boolean swap = false;
		for(int i=0;i<aantal;i++)
		{
			swap=false;
			for(int j=0;j<(aantal-1);j++)
			{
				if( x.ar_item[j].AbsX <= x.ar_item[j+1].AbsX ) continue;
				swap=true;
				/*
				copyLineItem( x.ar_item[j] , y);
				copyLineItem( x.ar_item[j+1] , x.ar_item[j]);
				copyLineItem( y , x.ar_item[j+1] );
				*/
				y.shallowCopy(x.ar_item[j]);
				x.ar_item[j].shallowCopy(x.ar_item[j+1]);
				x.ar_item[j+1] .shallowCopy(y);
			}
			if( swap == false ) break;
		}
		y = null;
	}

	/*
	//---------------------------------------------------------------------------------
	private void copyLineItem( OCRLineItem x , OCRLineItem y )
	//---------------------------------------------------------------------------------
	{
		y.GraphObjIdx = x.GraphObjIdx;
		y.AbsX        = x.AbsX;
		y.AbsY        = x.AbsY;
		y.RelX        = x.RelX;
		y.RelY        = x.RelX;
		y.width       = x.width;
		y.heigth      = x.heigth;
		y.OutOfBound  = x.OutOfBound;
	}
	*/
	
	//---------------------------------------------------------------------------------
	private int determineHighestMiddleObject( OCRTempItem[] ar , int middle , int irun , int medianHeigth ,
			int quart1 , int quart3)
	//---------------------------------------------------------------------------------
	{
		// set middle and verify whether there is still something to process
		int aantal=0;
		for(int i=0;i<ar.length;i++)
		{
			ar[i].delta=999999;
			if( ar[i].run != -1 ) continue; 
			aantal++;
			ar[i].delta = ar[i].MidX - middle;
			if( ar[i].delta < 0 ) ar[i].delta = 0 - ar[i].delta;
		}
		if( aantal == 0 ) return 0;
		
		// locate : hoogste object met kleinste afwijking
		int mdx=-1;
		for(int z=0;z<2;z++)
		{
			// use quartiles for the first run
			if( z == 0 ) {
				int mindelta = 99999;
				int minY = 99999;
				for(int i=0;i<ar.length;i++)
				{
					if( ar[i].run != -1 ) continue;
					// ignore objects that are not close to the median
					if( (ar[i].heigth < quart1) || (ar[i].heigth > quart3) ) continue;
					if( mindelta < ar[i].delta ) continue;
					if( minY < ar[i].BottomY ) continue;
					mindelta = ar[i].delta;
					minY = ar[i].BottomY;
					mdx=i;
				}		
				if( mdx >= 0 ) break;
			}
			else {
				int mindelta = 99999;
				int minY = 99999;
				for(int i=0;i<ar.length;i++)
				{
					if( ar[i].run != -1 ) continue;
					if( mindelta < ar[i].delta ) continue;
					if( minY < ar[i].BottomY ) continue;
					mindelta = ar[i].delta;
					minY = ar[i].BottomY;
					mdx=i;
				}		
			}
		}
		if( mdx < 0 ) {
			do_error("Strange cannot find highest nearest to middle" + quart1 + " " + medianHeigth + " " + quart3 );
			return -1;
		}
		// set the run - targetBottomY = the one on mdx
		ar[mdx].run = irun;
		// cluster the objects that have similar Y location as ar[mdx]
		int found=1;
		for(int i=0;i<ar.length;i++)
		{
			if( ar[i].run != -1 ) continue;
			int HeightDelta = ar[i].BottomY - ar[mdx].BottomY;
			if( HeightDelta < 0 ) HeightDelta = 0 - HeightDelta;
			//if( HeightDelta > ar[i].heigth ) continue;   // xxx
			if( HeightDelta > medianHeigth ) continue;   // xxx
			ar[i].run = irun;
			found++;
		}
        //do_log(9,"Run [" + irun + "] [#items" + ar.length + "] [#ItemsClistered=" + found + "] [HighestMiddle=" + mdx + "]");
		return found;
	}

	// read CCs from links to right using the MidX value
	// for the lowest unprocessed Midx determine the CC which is locate the highest - this should indicate a line of CCs
	// Using the highest Y get all CCs which have a similar Y (difference of Y is)
	// set these CCs to processed  (this is ROW or the RUN)
	// continue
	// finalize by sorting on
	//---------------------------------------------------------------------------------
	private boolean do_overlay(boolean useOriginal)
	//---------------------------------------------------------------------------------
	{
		//
		do_log(5,"Creating OCR overlay [useOriginal=" + useOriginal + "] [File=" + originalFileName + "]");
		// 
		gpLoadImageInBuffer lo = new gpLoadImageInBuffer( xMSet.xU , logger );
		if( useOriginal ) {
		    blackwhiteoriginal = lo.loadBestandInBuffer(originalFileName);  
		}
		else {
			blackwhiteoriginal = lo.loadBestandInBuffer( xMSet.getBinarizedOutputJPGName() );
		}
		if( blackwhiteoriginal == null) return false;
		int realWidth = lo.getBreedte();
		int realHeigth = lo.getHoogte();
		int imgWidth=0;
		int imgHeigth=0;
		// check and set
        if( useOriginal ) {
        	if( (realWidth != uncroppedWidth) || (realHeigth !=uncroppedHeigth) ) {
        		do_error("There are discrepancies in the actual size (" + realWidth + "," + realHeigth +") and uncropped size in metadata (" + uncroppedWidth + "," + uncroppedHeigth +")");
        		return false;
        	}
        	imgWidth = uncroppedWidth;
        	imgHeigth = uncroppedHeigth;
        	realPayLoadX = payLoadX;
        	realPayLoadY = payLoadY;
        }
        else {
        	if( (realWidth != payLoadWidth) || (realHeigth != payLoadHeigth) ) {
        		do_error("There are discrepancies in the actual size (" + realWidth + "," + realHeigth +") and payload size in metadata (" + payLoadWidth + "," + payLoadHeigth +")");
        		return false;
        	}
        	imgWidth = payLoadWidth;
        	imgHeigth = payLoadHeigth;
        	realPayLoadX = 0;
        	realPayLoadY=0;
        }
		//
		int leftMarginWidth = 10;
		int topMarginHeigth = 10;

		// bepaal de regelhoogte
		int regelHoogte = 0;
		for(int i=0;i<list.size();i++)
		{
		 //process paragraph
		 OCRParagraph para = list.get(i);
		 if( regelHoogte < para.MaxHeigth ) regelHoogte = para.MaxHeigth;
		}
		regelHoogte = (regelHoogte * 3) / 2;  // 150%
		//  minimale regelhoogte 25  - voor OCR mogen de markers texten groot zijn
		if( regelHoogte < 30 ) regelHoogte = 30;
		// calculate required heigth
		int rapportHeigth = regelHoogte * 2 * list.size() ; 
		double dd = (double)rapportHeigth * 1.25;
		rapportHeigth = (int)dd;
		rapportHeigth = rapportHeigth + (50 - (rapportHeigth % 50));
		if( rapportHeigth < imgHeigth ) rapportHeigth= imgHeigth;
		int rapportWidth = (imgWidth * 3 ) / 2;   // take lots over overhead
		//
		rapport = new int[ rapportWidth * rapportHeigth];
		for(int i=0;i<rapport.length; i++) rapport[i] = 0xffffffff;
		//
		int ocrfonthoogte = (regelHoogte * 3 )/4;
		//Font ocrfont = new Font("ocr a extended", Font.PLAIN, (regelHoogte / 2) );
		Font ocrfont = new Font(OCRFontName, Font.PLAIN, ocrfonthoogte );
		// text
		qualitymarker = "[p:" + CMXUID + "] [d:" + dpi + "] [u:" + UID + "]";
		cmcDrawPixelText drwtxt = new cmcDrawPixelText(xMSet,logger);
		if( puttext ) drwtxt.drawPixelText( ocrfont , Color.BLACK , leftMarginWidth , topMarginHeigth , rapport , rapportWidth , qualitymarker );
		//
		int canvasBreedte = rapportWidth - (2*leftMarginWidth);
		int canvasHoogte = rapportHeigth - (2*topMarginHeigth);
		//drawRectangle( leftMarginWidth , topMarginHeigth , canvasBreedte , canvasHoogte , rapportWidth);
        //
		int yOffset = topMarginHeigth + regelHoogte;
		for(int i=0;i<list.size();i++)
		{
		 try {
		 //process paragraph
		 OCRParagraph para = list.get(i);
		 String sTag = preamble.trim() + (""+list.get(i).OCRTag).trim() + postamble.trim();
		 if( puttext ) drwtxt.drawPixelText( ocrfont , Color.BLACK , leftMarginWidth , yOffset , rapport , rapportWidth , sTag );
		 yOffset += (ocrfonthoogte * 6)/5;
		 int NbrOfDisplayLines=0;
		 for(int j=0;j<para.ar_line.length;j++)
		 {
		  OCRLine line = para.ar_line[j];
		  //do_log(1, para.UID + " " + line.linenumber );
		  //
		  int prev_x=-1;
		  int prev_y=-1;
		  int prev_h=-1;
		  int next_x=-1;
		  int next_y=-1;
		  int next_h=-1;
		  int next_w=-1;
		  int prev_p=-1;
	  
		  for(int k=0;k<line.ar_item.length;k++)
          {
		    //
        	  int p = line.ar_item[k].GraphObjIdx;
        	  // just put the bitmap on currentx+ minx
        	  int x = line.ar_item[k].RelX;
        	  int yIncrement = x / canvasBreedte;
        	  if( NbrOfDisplayLines < yIncrement ) NbrOfDisplayLines = yIncrement;
        	  x = x % canvasBreedte;
        	  int y = yOffset + (yIncrement * regelHoogte ) + line.ar_item[k].RelY;
              if( y > rapportHeigth ) continue;  // safety
        	  int breedte = line.ar_item[k].width;
        	  int hoogte  = line.ar_item[k].heigth;
        	  //
        	  // Spilled over ?
              int EndX = x + breedte;
              if( EndX > canvasBreedte ) {
            	int leftBreedte = breedte - (EndX - canvasBreedte);
          	    pastePartSnippet( p , 0 , leftBreedte ,  leftMarginWidth + x , y , imgWidth , rapportWidth);
          	    int rightBreedte = breedte - leftBreedte;
          	    pastePartSnippet( p , leftBreedte , rightBreedte ,  leftMarginWidth , y + regelHoogte , imgWidth , rapportWidth );
          	    next_x = leftMarginWidth + leftBreedte;
          	    next_y = y + regelHoogte;
          	    next_h = regelHoogte;
          	    next_w = leftBreedte;
        	  }
              else {
        	    pasteSnippet( p , leftMarginWidth + x , y , imgWidth , rapportWidth );
        	    next_x = leftMarginWidth + x + breedte;
        	    next_y = y;
        	    next_h = hoogte;
        	    next_w = breedte;
          	  }
              // fill space between characters
              int dist = next_x - next_w - prev_x - 1;
              if( k != 0) {
            	  if( dist > 0 ) {
            	    //drawRectangle( prev_x  , prev_y -1  , dist + 2 , prev_h+2 , imgWidth);
            	    int snip_x = ar_pabo[prev_p].MaxX + 1;
            	    int snip_y = ar_pabo[prev_p].MinY;
            	    pasteSnippetFull( snip_x , snip_y , dist , prev_h , prev_x , prev_y , imgWidth , rapportWidth);
                  }
              }
              else {  
               if( DEBUG ) {	  
                 int oo = yOffset + (yIncrement * regelHoogte ) + line.ar_item[k].RelY;
                 drawRectangle( leftMarginWidth + x-1 , oo , leftMarginWidth + line.MaxX - line.MinX + 2 , line.MaxHeigth , rapportWidth);
                 drawHorizontalLine( leftMarginWidth + x-1 , oo , leftMarginWidth + line.MedianHeigth  , rapportWidth);
                 if( j==0 ) drawHorizontalLine( leftMarginWidth + x-1 , oo + para.MedianHeigth , rapportWidth-30 , rapportWidth);
               }
              }
              prev_x = next_x;
              prev_y = next_y;
              prev_h = next_h;
              prev_p = p;
              
       //do_log(9,"(x=" + line.ar_item[k].AbsX + " w=" + line.ar_item[k].width + ") Rel(" + line.ar_item[k].RelX + "," + line.ar_item[k].RelY + ") Disp=(" + x + "," + y + ")");
           }
	     }
		 //int prevY = yOffset;
		 yOffset += ((NbrOfDisplayLines + 1) * regelHoogte) + (regelHoogte/4);
		 if( yOffset > rapportHeigth ) {
			 do_error("Text does not fit on page");
			 break; // safety
		 }
//drawRectangle( leftMarginWidth , prevY , canvasBreedte , yOffset-prevY , imgWidth);
		 
		 }
		 catch( Exception e ) {
		  do_error( "Error while processing [" + i + "] from [" + list.size() + "] " + e.getMessage() );
		  e.printStackTrace();
		  continue;
		 }
		
		}
		// store the bitmap generated onto disk
		// if needed bump up the DPI
		writeOCRFile(rapport,rapportWidth,rapportHeigth);
        //		
		blackwhiteoriginal=null;
		rapport=null;
		return true;
	}
	
	// if the DPI is really low then expand the picture
	//---------------------------------------------------------------------------------
	private void writeOCRFile(int[] rapport , int rapportWidth , int rapportHeigth)
	//---------------------------------------------------------------------------------
	{
		do_log(5,"Applying Tesseract curation [" + cmxTesseractCuration + "]");
		cmcImageRoutines irout = new cmcImageRoutines(logger);
		// if DPI cannot be ignored
		if( cmxTesseractCuration != cmcProcEnums.OCR_CURATION.IGNORE_DPI )
		{		
	     // if use image
		 if( cmxTesseractCuration == cmcProcEnums.OCR_CURATION.USE_IMAGE_DPI ) {
	        do_log(5,"Creating the OCR file with DPI from image [" + dpi + "]");
		 }
		 else // so one of the INCREASE_DPI options -> however only Fix DPI if the DPI is between min and max (70 and 300)
		 if( (dpi>cmcProcConstants.MINIMAL_DPI) && (dpi<cmcProcConstants.MAXIMAL_DPI) ) 
		 {
		   int newdpi = cmcProcConstants.MAXIMAL_DPI;
		   double multi = Math.round((double)newdpi / (double)dpi);
		   if( (multi>1) || (multi<5) ) {  // do not expand too much
	         do_log(5,"Creating the OCR file with DPI [" + newdpi + "] [Multiplicator = " + multi + "] [OriginalDPI =" + dpi + "]");
             //
		     int[] expa = irout.resizeImage(rapport, rapportWidth , rapportHeigth, multi , cmcImageRoutines.ImageType.RGB );		
             if( expa == null ) {
        	  do_error("Could not expand the picture - switching to standard approach");
             }
             else {
        	   int width = irout.getResizeWidth();
        	   int heigth = irout.getResizeHeigth();
        	   if( (width>0) && (heigth>0))  {
        		  //  Convolution or Fourrier
        		  if( (cmxTesseractCuration == cmcProcEnums.OCR_CURATION.INCREASE_IMAGE_DPI_AND_CONVOLVE)  ||
        			  (cmxTesseractCuration == cmcProcEnums.OCR_CURATION.INCREASE_IMAGE_DPI_AND_CONVOLVE) )
        		  {	  
        		    int convo[] = new int[ expa.length ]; 
        		    // convolution - blur ten sharpen
        		    cmcProcConvolution post = new cmcProcConvolution(logger);
                    post.convolve(expa , convo , cmcProcConvolution.kernelType.GAUSSIAN33 , width , heigth );
                    post.convolve(convo , expa , cmcProcConvolution.kernelType.SHARPEN , width , heigth );
                    //
                    // put back into expa - if the last filter did not target it
                    //for(int i=0;i<expa.length;i++) expa[i] = convo[i];
                    convo=null;
                    post=null;
        		  } // CONVOLVE - FOURRIER
        		  //
        		  // write
        		  irout.writePixelsToFileDPI(expa, width , heigth , xMSet.getOCROutputImageFileName(), cmcImageRoutines.ImageType.RGB , newdpi);
        		  expa=null;
                  return;
                  //
        	   } do_log(5,"Strange cannot get the dimension of expanded image");
             }  // expa is not null - so expanded
		   } do_log(5,"The resolution will not be expanded.Required multiplication is too high [" + multi + "]");
		 } do_log(5,"The DPI onthe image is out of bounds [" + dpi + "]");
		}
		else { dpi = -1;  } // ignore
		
		// Without modifying the DPI - however check if not too low
		if( dpi < cmcProcConstants.MINIMAL_DPI) {
			do_log(5,"Creating the OCR file with DPI [-1] requested DPI is [" + dpi + "]");
			dpi = -1;
		}
		else do_log(5,"Creating the OCR file with DPI [" + dpi + "]");
		// DPI
		irout.writePixelsToFileDPI(rapport, rapportWidth , rapportHeigth , xMSet.getOCROutputImageFileName(), cmcImageRoutines.ImageType.RGB , dpi);
		return;
		
	}
	
	//---------------------------------------------------------------------------------
	private void filledRect(int x, int y , int w , int h , int imagebreedte)
	//---------------------------------------------------------------------------------
	{
		for(int i=0;i<h;i++)
		{
			for(int j=0;j<w;j++)
			{
				int p = ((y + i)*imagebreedte) + x + j;
				rapport[p] = 0xffababab;
			}
		}
	}

	//---------------------------------------------------------------------------------
	private void drawRectangle(int x, int y , int w , int h , int imageBreedte)
	//---------------------------------------------------------------------------------
	{
		try {
		for(int i=0;i<w;i++)
		{
			rapport[ (y * imageBreedte) + x + i] = 0xffabac3f;
			rapport[ ((y + h - 1) * imageBreedte) + x + i] = 0xffabac3f;
		}
		for(int i=0;i<h;i++)
		{
			rapport[((y + i) * imageBreedte) + x] = 0xffabac3f;
			rapport[((y + i) * imageBreedte) + x + (w-1)] = 0xffabac3f;
		}
		}
		catch(Exception e ) { return; }
	}

	//---------------------------------------------------------------------------------
	private void drawHorizontalLine(int x, int y , int w , int imageBreedte)
	//---------------------------------------------------------------------------------
	{
			try {
			for(int i=0;i<w;i++)
			{
				rapport[ (y * imageBreedte) + x + i] = 0xffff0000;
			}
			}
			catch(Exception e ) { return; }
	}
	
	//---------------------------------------------------------------------------------
	private void pasteSnippet( int idx , int x, int y , int imagebreedte , int targetbreedte)
	//---------------------------------------------------------------------------------
	{
		int breedte = ar_pabo[idx].MaxX - ar_pabo[idx].MinX + 1;
	    int hoogte = ar_pabo[idx].MaxY - ar_pabo[idx].MinY + 1;
		int[] snippet = extractSnippet( ar_pabo[idx].MinX , ar_pabo[idx].MinY , breedte , hoogte , imagebreedte);
		//
		for(int i=0;i<hoogte;i++)
		{
			for(int j=0;j<breedte;j++)
			{
				int p = ((y + i)*targetbreedte) + x + j;
				int q = snippet[(breedte*i)+j];
				if( p < rapport.length ) rapport[p] = q;
			}
		}
		snippet=null;
	}

	//---------------------------------------------------------------------------------
	private void pastePartSnippet( int idx , int offsetX , int requestedBreedte , int x, int y , int imagebreedte , int targetbreedte)
	//---------------------------------------------------------------------------------
	{
		int breedte = requestedBreedte;
	    int hoogte = ar_pabo[idx].MaxY - ar_pabo[idx].MinY + 1;
		int[] snippet = extractSnippet( ar_pabo[idx].MinX + offsetX , ar_pabo[idx].MinY , breedte , hoogte , imagebreedte);
		//
		for(int i=0;i<hoogte;i++)
		{
			for(int j=0;j<breedte;j++)
			{
				int p = ((y + i)*targetbreedte) + x + j;
				int q = snippet[(breedte*i)+j];
				if( p < rapport.length ) rapport[p] = q;
			}
		}
		snippet=null;
	}

	//---------------------------------------------------------------------------------
	private void pasteSnippetFull( int x , int y , int breedte , int hoogte , int dest_x , int dest_y , int imagebreedte , int targetbreedte)
	//---------------------------------------------------------------------------------
	{
		int[] snippet = extractSnippet( x , y , breedte , hoogte , imagebreedte);
		//
		for(int i=0;i<hoogte;i++)
		{
			for(int j=0;j<breedte;j++)
			{
				int p = ((dest_y + i)*targetbreedte) + dest_x + j;
				int q = snippet[(breedte*i)+j];
				if( p < rapport.length ) rapport[p] = q;
			}
		}
		snippet=null;
	}

	//---------------------------------------------------------------------------------
	private int[] extractSnippet(int x , int y , int w , int h , int imagebreedte)
	//---------------------------------------------------------------------------------
	{
		int[] res = new int[ w * h];
		for(int i=0;i<res.length ; i++) res[i] = 0xffffffff;
		for(int i=0;i<h;i++)
		{
			for(int j=0;j<w;j++)
			{
				int p = ((realPayLoadY + y + i) * imagebreedte) + realPayLoadX + x + j;
				res[(i*w)+j] = blackwhiteoriginal[p];
			}
		}
		/* kader
		for(int i=0;i<h;i++)
		{
			res[i*w]= 0xffaabbcc;
			res[(i*w)+(w-1)]= 0xffaabbcc;
		}
		for(int i=0;i<w;i++)
		{
			res[i] = 0xffaabbcc;
			res[i+((h-1)*w)] = 0xffaabbcc;
		}
		*/
		return res;
	}

	//---------------------------------------------------------------------------------
	private void doLineStats( OCRLine x)
	//---------------------------------------------------------------------------------
	{
		int[] xlist = new int[x.ar_item.length];
		int[] ylist = new int[x.ar_item.length];
		int[] wlist = new int[x.ar_item.length];
		int[] hlist = new int[x.ar_item.length];
		int[] blist = new int[x.ar_item.length];  // bottom or baseline of a character
		int[] rightcornerlist = new int[x.ar_item.length];
		//
		for(int i=0;i<x.ar_item.length;i++)
		{
           xlist[i] = x.ar_item[i].AbsX;
           ylist[i] = x.ar_item[i].AbsY;
           wlist[i] = x.ar_item[i].width;
           hlist[i] = x.ar_item[i].heigth;
           blist[i] = x.ar_item[i].AbsY + x.ar_item[i].heigth;
           rightcornerlist[i] = x.ar_item[i].AbsX + x.ar_item[i].width - 1;
        }
		gpListStatFunctions stat = new gpListStatFunctions(xlist,logger);
		x.MinX = stat.getMinimum();
		x.MaxX = stat.getMaximum();
		//
		stat = new gpListStatFunctions(rightcornerlist,logger);
		x.totalWidth = stat.getMaximum() - x.MinX;
	    //
		stat = new gpListStatFunctions(ylist,logger);
		x.MinY = stat.getMinimum();
		x.MaxY = stat.getMaximum();
		//
		stat = new gpListStatFunctions(wlist,logger);
		x.MinWidth = stat.getMinimum();
		x.MaxWidth = stat.getMaximum();
		x.MedianWidth = stat.getMedian();
		//
		stat = new gpListStatFunctions(hlist,logger);
		x.MinHeigth = stat.getMinimum();
		x.MaxHeigth = stat.getMaximum();
		x.MedianHeigth = stat.getMedian();
		x.quartileHeigth = stat.getQuartile3();
	    // relative coordinates
		for(int i=0;i<x.ar_item.length;i++)
		{
			x.ar_item[i].RelX = x.ar_item[i].AbsX - x.MinX;
			x.ar_item[i].RelY = x.ar_item[i].AbsY - x.MinY;  // Aligning is from the topmost Y
		}
		// relative distances - sort on the absX
		boolean swap = false;
		for(int k=0;k<x.ar_item.length;k++)
		{
		 swap=false;
		 for(int i=0;i<(x.ar_item.length-1);i++)
		 {
			if( xlist[i] > xlist[i+1] ) {
				int buf = xlist[i];
				xlist[i] = xlist[i+1];
				xlist[i+1] = buf;
				buf = wlist[i];
				wlist[i] = wlist[i+1];
				wlist[i+1] = buf;
		        swap=true;		
			}
		 }
		 if( swap == false ) break;
		} 
		// distances
		if( x.ar_item.length <= 1 ) {
		  x.MaxDistance = 0;
		  x.MinDistance = 0;
		}
		else {
		 int[] dlist = new int[x.ar_item.length-1];
		 for(int i=1;i<x.ar_item.length;i++)
		 {
			dlist[i-1] = xlist[i] - xlist[i-1] - wlist[i-1];  //  X2 - X1 - Width1
		 }
		 stat = new gpListStatFunctions(dlist,logger);
		 x.MinDistance = stat.getMinimum();
		 x.MaxDistance = stat.getMaximum();
		 /*
		 {
			 String ss = "[Min=" + x.MinDistance + "] [Max=" + x.MaxDistance + "] ";
			 for(int i=0;i<dlist.length;i++) ss += " " + dlist[i];
			 do_log(9,ss);
		 }
		 */
		 dlist=null;
		}
		// remove biggies
		for(int i=0;i<x.ar_item.length;i++)
		{
		 if( x.ar_item[i].heigth > x.quartileHeigth ) x.ar_item[i].OutOfBound = true;
		}
		
		//
		/*
		do_log(9,
			 "L[MinX=" + x.MinX +
			 "][MaxX=" + x.MaxX +
			 "][MinY=" + x.MinY +
			 "][MaxY=" + x.MaxY +
			 "][MinDist=" + x.MinDistance +
			 "][MaxDist=" + x.MaxDistance +
			 "][MinWidth=" + x.MinWidth +
			 "][MaxWidth=" + x.MaxWidth +
			 "][MedWidth=" + x.MedianWidth +
			 "][MinHeigth=" + x.MinHeigth +
			 "][MaxHeigth=" + x.MaxHeigth +
			 "][MedHeight=" + x.MedianHeigth +
			 "][QuartHeight=" + x.quartileHeigth +
			 "]"
			  );
		*/
		//
		stat=null;
		xlist=null;
		ylist=null;
		wlist=null;
		hlist=null;
	}

	
	//---------------------------------------------------------------------------------
	private void doParagraphStats(OCRParagraph x)
	//---------------------------------------------------------------------------------
	{
		// medianHeight and remove OOBs
		int vrac=0;
		for(int i=0;i<x.ar_line.length;i++)
		{
		vrac += x.ar_line[i].ar_item.length;	
		}
		int[] wlist = new int[vrac];
		int[] hlist = new int[vrac];
		//int[] blist = new int[vrac];  // baseline 
		int k=0;
		for(int i=0;i<x.ar_line.length;i++)
		{
		 for(int j=0;j<x.ar_line[i].ar_item.length;j++)
		 {
			 wlist[k] = x.ar_line[i].ar_item[j].width;
			 hlist[k] = x.ar_line[i].ar_item[j].heigth;
			 //blist[k] = x.ar_line[i].ar_item[j].AbsY + x.ar_line[i].ar_item[j].heigth;
			 k++;
		 }
		}
		gpListStatFunctions stat = new gpListStatFunctions(wlist,logger);
		x.MinWidth = stat.getMinimum();
		x.MaxWidth = stat.getMaximum();
		x.MedianWidth = stat.getMedian();
		x.quartile1Width = stat.getQuartile1();
		x.quartile3Width = stat.getQuartile3();
		//
		stat = new gpListStatFunctions(hlist,logger);
		x.MinHeigth = stat.getMinimum();
		x.MaxHeigth = stat.getMaximum();
		x.MedianHeigth = stat.getMedian();
		x.quartile1Heigth = stat.getQuartile1();
		x.quartile3Heigth = stat.getQuartile3();
		// min max distance
		int[] dmxlist = new int[x.ar_line.length];
		int[] dmnlist = new int[x.ar_line.length];
		for(int i=0;i<x.ar_line.length;i++)
		{
			dmnlist[i] = x.ar_line[i].MinDistance;
			dmxlist[i] = x.ar_line[i].MaxDistance;
		}
		stat = new gpListStatFunctions(dmnlist,logger);
		x.MinDistance = stat.getMinimum();
		stat = new gpListStatFunctions(dmxlist,logger);
		x.MaxDistance = stat.getMaximum();
		if( x.MaxDistance > MAX_SPACE_BETWEEN_WORDS ) x.MaxDistance = MAX_SPACE_BETWEEN_WORDS;
		
		// Kick out the large and small ones
		for(int i=0;i<x.ar_line.length;i++)
		{
		 for(int j=0;j<x.ar_line[i].ar_item.length;j++)
		 {
		 if( x.ar_line[i].ar_item[j].width < x.quartile1Width ) x.ar_line[i].ar_item[j].OutOfBound=true;
		 if( x.ar_line[i].ar_item[j].width > x.quartile3Width ) x.ar_line[i].ar_item[j].OutOfBound=true;
		 if( x.ar_line[i].ar_item[j].heigth < x.quartile1Heigth ) x.ar_line[i].ar_item[j].OutOfBound=true;
		 if( x.ar_line[i].ar_item[j].heigth < x.quartile3Heigth ) x.ar_line[i].ar_item[j].OutOfBound=true;
		 }
		}
		
		// determine relative X positions 
		int currentx = 0;
		for(int i=0;i<x.ar_line.length;i++)
		{
			x.ar_line[i].relX = currentx;
		    int currentWidth = x.ar_line[i].totalWidth ;
		    int lineAbsOffsetX = x.ar_line[i].MinX;
		    for(int j=0;j<x.ar_line[i].ar_item.length;j++)
		    {
		       x.ar_line[i].ar_item[j].RelX = currentx + x.ar_line[i].ar_item[j].AbsX - lineAbsOffsetX;
		       //x.ar_line[i].ar_item[j].RelY =  has previously been set already 
		       if( x.ar_line[i].ar_item[j].RelX > (currentx + currentWidth) ) {
		    	   do_error("doParagraphStats : Strange - relative x overrun");
		       }
		    }
		 	currentx += x.MaxDistance + currentWidth;
		}
		
		/*
		do_log(9,
				 "P[MinX=" + x.MinX +
				 "][MaxX=" + x.MaxX +
				 //"][MinY=" + x.MinY +
				 //"][MaxY=" + x.MaxY +
				 "][MinDist=" + x.MinDistance +
				 "][MaxDist=" + x.MaxDistance +
				 
				 "][MinWidth=" + x.MinWidth +
				 "][MaxWidth=" + x.MaxWidth +
				 "][MedWidth=" + x.MedianWidth +
				 "][Quart1Width=" + x.quartile1Width +
				 "][Quart3Width=" + x.quartile3Width +
				 				 
				 "][MinHeigth=" + x.MinHeigth +
				 "][MaxHeigth=" + x.MaxHeigth +
				 "][MedHeight=" + x.MedianHeigth +
				 "][Quart1Height=" + x.quartile1Heigth +
				 "][Quart3Height=" + x.quartile3Heigth +
				 "]"
				  );
		for(int i=0;i<x.ar_line.length;i++)
		{
			do_log(9,"  [" + x.ar_line[i].MinX + "," + x.MaxDistance + "," + x.ar_line[i].totalWidth + "] " + x.ar_line[i].relX );
		}
		*/
		
		//
		wlist = null;
		hlist = null;
		dmxlist = null;
		dmnlist = null;
		stat = null;
	}

	//---------------------------------------------------------------------------------
	private void doReAlignCharactersAcrossRuns(OCRParagraph x)
	//---------------------------------------------------------------------------------
	{
	    // May 2018
		// determine the difference between paragraph and Line baseline and update RelY
		// the idea was to determine the baseline for each line 
		// As it happens it proved that the median of the height can be ued for the baseline
		// it is sufficient to compare the median height for each line wit the median height per paragraph
		// and apply the difference to the RelY
		for(int i=0;i<x.ar_line.length;i++)
		{
		 OCRLine line= x.ar_line[i];
		 int diff = x.MedianHeigth - line.MedianHeigth;  // delta (Paragraph - Line ) if Line is higher then the difference needs to be subtracted
         if( diff == 0 ) continue;
         for(int j=0;j<line.ar_item.length;j++) line.ar_item[j].RelY += diff;
		}
	}
	
	//---------------------------------------------------------------------------------
	public boolean run_tesseract(TESSERACTCOMMAND tesco)
	//---------------------------------------------------------------------------------
	{
		TesseractVersion="Unknown";
		// Tesseract - PATH en options - taal zetten
		if( tesco == TESSERACTCOMMAND.DO_OCR ) {
		 if( xMSet.xU.IsBestand(xMSet.getOCROutputImageFileName()) == false ) {
			do_error("Cannot locate OCR Image file [" +xMSet.getOCROutputImageFileName() + "]");
			return false;
		 }
		}
		// remove the result file
		if( xMSet.xU.IsBestand(xMSet.getOCRResultFile()) ) {
			xMSet.xU.VerwijderBestand(xMSet.getOCRResultFile());
			if( xMSet.xU.IsBestand(xMSet.getOCRResultFile()) ) {
				do_error("Cannot remove OCR result [" + xMSet.getOCRResultFile() + "]");
				return false;
			}	
		}
		// remove the batch file
		if( xMSet.xU.IsBestand(xMSet.getTesseractBatchFileName()) ) {
					xMSet.xU.VerwijderBestand(xMSet.getTesseractBatchFileName());
					if( xMSet.xU.IsBestand(xMSet.getTesseractBatchFileName()) ) {
						do_error("Cannot remove Tesseract Batch file [" + xMSet.getTesseractBatchFileName() + "]");
						return false;
					}	
		}
		// create the tesseract CONFIG file to be used
		if( createTesseractConfigurationFile() == false ) {
			do_error("Cannot create the Tesseract configuration file in [" + xMSet.getTesseractDir() + "]");
			return false;
		}
		if( xMSet.xU.IsBestand(xMSet.getTesseractConfigurationFileName()) == false ) {
    		do_error("Could not find the TesseractConfiguration [" + xMSet.getTesseractConfigurationFileName() + "]");
    		return false;
    	}
		//
		if( irq.gotInterrupt() )  return false;
	    //	
		if( createTesseractCommandFile(tesco) == false ) {
			do_error("Cannot create the Tesseract batch/shell file in [" + xMSet.getTesseractDir() + "]");
			return false;
		}
	    //
    	if( xMSet.xU.IsBestand(xMSet.getTesseractBatchFileName()) == false ) {
    		do_error("Could not create batch/shell file [" + xMSet.getTesseractBatchFileName() + "]");
    		return false;
    	}
        //  
    	gpExecBatch exec = new gpExecBatch( (xMSet.getTesseractBatchFileName()) , logger );
    	int exitlevel = exec.getExitLevel();
    	int stdoutCounter = exec.getSTDOUT() == null ? 0 : exec.getSTDOUT().size();
    	int stderrCounter = exec.getSTDERR() == null ? 0 : exec.getSTDERR().size();
    	do_log(1,"[Exit=" + exitlevel  + "] [#stdout=" + stdoutCounter + "] [#stderr=" + stderrCounter + "]");
    	if( tesco == TESSERACTCOMMAND.DO_VERSION ) {
    	  ArrayList<String> lret = null;
    	  if( stderrCounter != 0 ) lret = exec.getSTDERR();
    	                      else lret = exec.getSTDOUT();
    	  if( lret != null ) {
    		  TesseractVersion="";
    		  for(int i=0;i<lret.size();i++) 
    		  {
    			String sline = lret.get(i).trim();
    			if( sline.length() < 1 ) continue;
    			TesseractVersion += lret.get(i).trim() + "\n";
    		  }
    	  }
    	}
    	exec=null;
    	if( exitlevel != 0) {
    		do_error("Something went wrong whilst running [" + xMSet.getTesseractBatchFileName() + "]");
    		return false;
    	}
    	// remove batch
    	if( xMSet.xU.IsBestand(xMSet.getTesseractBatchFileName()) ) {
			xMSet.xU.VerwijderBestand(xMSet.getTesseractBatchFileName());
			if( xMSet.xU.IsBestand(xMSet.getTesseractBatchFileName()) ) {
				do_error("Cannot remove Tesseract Batch file [" + xMSet.getTesseractBatchFileName() + "]");
				return false;
			}	
        }
    	//
    	if( tesco == TESSERACTCOMMAND.DO_OCR ) return extract();
    	if( tesco == TESSERACTCOMMAND.DO_VERSION ) return true;
    	return false;
	}

	//---------------------------------------------------------------------------------
	private boolean createTesseractConfigurationFile()
	//---------------------------------------------------------------------------------
	{   
		// if it exists then remove
		if( xMSet.xU.IsBestand(xMSet.getTesseractConfigurationFileName()) ) {
			if( xMSet.xU.VerwijderBestand(xMSet.getTesseractConfigurationFileName()) == false ) {
					do_error("Could not remove Tesseract Option File [" + xMSet.getTesseractConfigurationFileName() + "]");
					return false;
			}
		}
		//
		if( readAndCreateTesseractConfigurationFile() == false ) {
			do_error("Creating an empty tesseract config file");
			cmcTesseractParameterDAO tao = new cmcTesseractParameterDAO(xMSet,logger);
			ArrayList<cmcTesseractParameter> tlist = tao.makeDefaultList(true);
			boolean isOK=true;
			if( tlist == null ) isOK=false;
			if( isOK ) {
				if (tlist.size() <= 0 ) isOK=false;
			}
			if( isOK == false ) {  // last resort
			 do_error("Hardcoding empty tesseract config file");
			 gpPrintStream conf = new gpPrintStream( xMSet.getTesseractConfigurationFileName() , "ASCII");
			 conf.println("tessedit_char_whitelist ABCDEFGHIJKLMNOPQRSDTUVWXYZ012345789");
			 conf.println("debug_file " + xMSet.getTesseractLogFileName());
			 conf.println("textord_heavy_nr 1" );  // heavy Noise Reduction
			 conf.println("paragraph_debug_level 1"); // provides debug info
			 conf.close();
			}
			else {
		      dumpConf( tlist );		
			}
			tao=null;
			tlist=null;
		}
		return true;
	}
	
	//---------------------------------------------------------------------------------
	private boolean readAndCreateTesseractConfigurationFile()
	//---------------------------------------------------------------------------------
	{   
		//
		cmcTesseractParameterDAO tao = new cmcTesseractParameterDAO(xMSet,logger);
		ArrayList<cmcTesseractParameter> tlist  = tao.readActiveTesseractOptionFile();
		if( tlist == null ) {
			do_error("Could not load Tesseract Options from file");
			tao=null;
			return false;
		}
		if (tlist.size() <= 0 ) {
			do_error("No Tesseract Options have been set");
			tlist=null;
			tao=null;
			return false;
		}
		//
		dumpConf( tlist );
		//
        tlist=null;
        tao=null;
		return true; 
    }

	//---------------------------------------------------------------------------------
	private void dumpConf(ArrayList<cmcTesseractParameter> tlist)
	//---------------------------------------------------------------------------------
	{
		//
		gpPrintStream conf = new gpPrintStream( xMSet.getTesseractConfigurationFileName() , "ASCII");
		for(int i=0;i<tlist.size();i++)
		{
		  String param = tlist.get(i).getParameter().trim();
		  String value = tlist.get(i).getValue();
		  if( param.compareToIgnoreCase("debug_file") == 0 ) {  // overrule debug file name
			  value = xMSet.getTesseractLogFileName();
		  }
		  conf.println( param + " " + value );
		}
		conf.close();
        
	}
	
	//---------------------------------------------------------------------------------
	private boolean createTesseractCommandFile(TESSERACTCOMMAND tesco)
	//---------------------------------------------------------------------------------
	{
		// check whether OCR Binaries exist
		if( xMSet.xU.IsDir(xMSet.getTesseractDir()) == false ) {
			do_error("Cannot locate the Tesseract folder [" + xMSet.getTesseractDir() + "]");
			return false;
		}
		// check whether the language is supported by Tesseract and continue with supported one
		String TessLanguage = getSupportedTesseractLanguage();
		//
	    switch( xMSet.getMyOS() )
        {
        case MSDOS : return createTesseractMSDOSBatchFile(tesco,TessLanguage);
        case LINUX : return createTesseractLinuxShellFile(tesco,TessLanguage);
        default : { do_error("Unsupported Operating System"); return false; }
        }
	}

	//---------------------------------------------------------------------------------
	private String getSupportedTesseractLanguage()
	//---------------------------------------------------------------------------------
	{
		String testLanguage = xMSet.getTesseractLanguage(cmxLanguage).trim();
		String TessLanguageFolder = xMSet.getTesseractDir() + xMSet.xU.ctSlash + "tessdata" + xMSet.xU.ctSlash;
		ArrayList<String> list = xMSet.xU.GetFilesInDir(TessLanguageFolder,null);
		boolean found = false;
		String LookForFile = (testLanguage + ".traineddata").toLowerCase();
		String SupportedLanguages="";
		for(int i=0;i<list.size();i++)
		{
		 String ff= list.get(i).trim();
		 if( ff.indexOf(".traineddata") < 0 ) continue;
		 String gg = xMSet.xU.Remplaceer(ff,".traineddata","");
		 if( SupportedLanguages.length()==0) SupportedLanguages = gg; else SupportedLanguages += "," + gg;
		 if( ff.compareTo( LookForFile ) == 0 ) found = true;
		}
		do_log(1,"Tesseract configured languages [" + SupportedLanguages + "] [requested=" + testLanguage + "]");
		if( found == false ) {
			do_log(1,"Default Tesseract Language will be used [eng]");
			return "eng";
		}
		return testLanguage;
	}
	
	//---------------------------------------------------------------------------------
	private boolean createTesseractLinuxShellFile(TESSERACTCOMMAND tesco , String TessLanguage)
	//---------------------------------------------------------------------------------
	{
		 // look for usr/bin/tesseract
		 String TesseractExe = xMSet.getTesseractDir() + xMSet.xU.ctSlash + "tesseract";
		 if( xMSet.xU.IsBestand(TesseractExe) == false ) {
		 do_error("Cannot locate the Tesseract binaries [" + TesseractExe  + "]");
		 return false;
		 }

		 //
		 // you need to remove .txt from the resultfile for the tesseract command
		 String ResultFile = xMSet.getOCRResultFile();
		 ResultFile = xMSet.xU.Remplaceer( ResultFile, ".txt" , "" );
		 /*
		 * tesseract.sh /home/johndoe/cbrTekStaktor/Tutorial/Temp/OCRoutput.png /home/johndoe/cbrTekStraktor/Tutorial/temp/Ocr -l fra -c tessedit_char_whitelist=ABCDEFGHIJKLMNOPQRSDTUVWXYZ012345789
		 */
		 gpPrintStream script = new gpPrintStream( xMSet.getTesseractBatchFileName() , "ASCII");
		 script.println("# Generated by " + xMSet.getApplicDesc() );
		 script.println("# Generated on " + xMSet.xU.prntStandardDateTime(System.currentTimeMillis()).toUpperCase() );
		 //
		 if( tesco == TESSERACTCOMMAND.DO_OCR )
		  script.println("tesseract " + xMSet.getOCROutputImageFileName() + " " + ResultFile  + " -l " + TessLanguage + " " + xMSet.getTesseractConfigurationFileName() );
		 else 
		 if( tesco == TESSERACTCOMMAND.DO_VERSION )
		  script.println("tesseract --version" );
		 else return false;
		 //
		 script.println("#");    
		 script.close();
		 //
		 return true;
	}

	//---------------------------------------------------------------------------------
	private boolean createTesseractMSDOSBatchFile(TESSERACTCOMMAND tesco , String TessLanguage)
	//---------------------------------------------------------------------------------
	{
		String TesseractExe = xMSet.getTesseractDir() + xMSet.xU.ctSlash + "tesseract.exe";
		if( xMSet.xU.IsBestand(TesseractExe) == false ) {
			do_error("Cannot locate the Tesseract binaries [" + TesseractExe  + "]");
			return false;
		}
	    //		
		String sDrive = xMSet.getTesseractDir().toUpperCase().trim();
		if( sDrive.length() > 3 ) {
			char[] buf = sDrive.toUpperCase().toCharArray();
			sDrive = ( buf[1] == ':' ) ? ""+buf[0]+buf[1] : null;
		}
		else sDrive=null;
		// you need to remove .txt from the resultfile for th tesseract command
	    String ResultFile = xMSet.getOCRResultFile();
	    ResultFile = xMSet.xU.Remplaceer( ResultFile, ".txt" , "" );
		/*
		 * C:
		 * cd c:\temp\Tesseract-OCR-4\Tesseract-OCR
           tesseract C:\temp\cmcProc\temp\OCRoutput.png C:\temp\cmcProc\temp\ocr -l fra -c tessedit_char_whitelist=ABCDEFGHIJKLMNOPQRSDTUVWXYZ012345789
		 */
		gpPrintStream script = new gpPrintStream( xMSet.getTesseractBatchFileName() , "ASCII");
		script.println("REM Generated by " + xMSet.getApplicDesc() );
        script.println("REM Generated on " + xMSet.xU.prntStandardDateTime(System.currentTimeMillis()).toUpperCase() );
        if( sDrive != null ) script.println(sDrive);
        script.println("CD " + xMSet.getTesseractDir() );
        //script.println("tesseract " + xMSet.getOCROutputImageFileName() + " " + ResultFile  + " -l " + TessLanguage + " -c tessedit_char_whitelist=ABCDEFGHIJKLMNOPQRSDTUVWXYZ012345789");
        //
        if( tesco == TESSERACTCOMMAND.DO_OCR ) 
      	 script.println("tesseract " + xMSet.getOCROutputImageFileName() + " " + ResultFile  + " -l " + TessLanguage + " " + xMSet.getTesseractConfigurationFileName() );
        else
        if( tesco == TESSERACTCOMMAND.DO_VERSION )
  		  script.println("tesseract --version" );
        else return false;
        //
        script.println("REM");
        script.close();
        //
		return true;
	}
	
	//---------------------------------------------------------------------------------
	private long getUID(String sIn)
	//---------------------------------------------------------------------------------
	{
		if( sIn == null ) return -1L;
		for(int i=0;i<list.size();i++)
		{
		 OCRParagraph para = list.get(i);
		 if( para.ingested == true ) continue;
		 int p = sIn.indexOf( (""+para.OCRTag).trim());
		 if( p >= 0 ) {
			 //do_log( 1 , "    Found ->" + sIn );
			 return para.UID;  // return UID
		 }
		}
		// not found - see if you can find a sub element of the OCRtag
		// Does it start wit PX ? 
	    String sTemp = xMSet.xU.RemplaceerIgnoreCase(sIn, " " , "").trim().toUpperCase();
	    if( sTemp.length() < 1) return -1L;
	    String sStart = ("" + preamble.trim() + OCRTagMarker).trim().toUpperCase();
	    if( sTemp.startsWith( sStart ) == false ) return -1L;
	    //do_log(1," ==> Looking for [" + sTemp + "]");
	    try {
	    for(int k=0;k<3;k++)
	    {
	      String sSegment = null;
    	  for(int i=0;i<list.size();i++)
    	  {
            sSegment = list.get(i).OCRTag.substring(1,4).trim();
            if( sSegment == null ) continue;
    	    switch( k)
    	    {
    	     case 0 : { sStart = OCRTagMarker + sSegment + OCRTagMarker + sSegment + OCRTagMarker + sSegment + OCRTagMarker; break; }
    	     case 1 : { sStart = OCRTagMarker + sSegment + OCRTagMarker + sSegment + OCRTagMarker; break; }
    	     case 2 : { sStart = OCRTagMarker + sSegment + OCRTagMarker; break; }
             default : { do_error("System error in getUID"); return -1L; }
    	    }
    	    sStart = sStart.toUpperCase().trim();
    	    if( sTemp.indexOf( sStart ) >= 0 ) {
    	      do_log(1,"Got a partial match [" + sTemp + "] [" + list.get(i).OCRTag + "] [" + sStart + "] [" + list.get(i).UID + "]");
    		  return list.get(i).UID;  // match on sub element 
    	    }
    	  }
	    }
	    }
	    catch (Exception e ) { 
	    	do_error("Oops - something went wrong in getUID");
	    }
	    do_log(1,"Could not match [" + sTemp + "]"); 
		return -1L;
	}
	//---------------------------------------------------------------------------------
	private int getIdxViaUid(Long luid)
	//---------------------------------------------------------------------------------
	{
		if( luid <= 0L) return -1;
		for(int i=0;i<list.size();i++)
		{
			if( list.get(i).UID == luid ) return i;
		}
		return -1;
	}
	//---------------------------------------------------------------------------------
	private int getIdxViaUIDOnLine(String sIn)
	//---------------------------------------------------------------------------------
	{
		return getIdxViaUid( getUID(sIn));
	}
	//---------------------------------------------------------------------------------
	private boolean extract()
	//---------------------------------------------------------------------------------
	{
		String sLogLine = "";
		int quality=-1;
		String result = xMSet.xU.ReadContentFromFile( xMSet.getOCRResultFile() , 1000 , "ASCII" );
		int currentIdx=-1;
    	int aantal = xMSet.xU.TelDelims( result , '\n' );
    	for(int i=0;i<=aantal;i++)
    	{
    		String sLijn = xMSet.xU.GetVeld(result,(i+1),'\n');
    		if( sLijn == null ) continue;
    		sLijn = sLijn.trim();
    		//if( sLijn.length() < 1) continue;  Empty line should also be attached to a UID
    		int idx = getIdxViaUIDOnLine( sLijn );
    		if( idx >= 0 ) {
    			if( idx >= list.size() ) {
    				do_error("Internal error [" + idx + "]");
    				return false;
    			}
    			currentIdx=idx;
    			continue;
    		}
    		else 
    		if( currentIdx == -1 ) {  // quality statement
    			quality= determineQuality(sLijn);
    			do_log(1,"Maximum Quality Indicator [" + quality + "%]");
    		    continue;	
    		}
    		OCRParagraph para = list.get(currentIdx);
    		para.ingested = true;
    		if( para.OCR == null ) para.OCR = "";
    		para.OCR += sLijn + " ";
    	}
    	// scrubbing
    	doScrubbing();
    	//
    	int unres=0;
    	for(int i=0;i<list.size();i++) {
    		if( list.get(i).ingested == false ) {
    			unres++;
    			do_log(9,"Unresolved OCR line[" + list.get(i).OCRTag + "]");
    		}
    	}
    	do_log(1,"Unresolved lines [" + unres + "] on a total of [" + list.size() + "]");
    	if( unres != 0 ) {
    		sLogLine = sLogLine + " [Lines=" + list.size() +"] [Unresolved=" + unres + "]";
    	}
    	xMSet.setOCRSummaryResult( sLogLine  + "[Accuracy=" + quality + "%] [" + this.shortOriginalFileName + "]" );
    	return true;
	}
	
	//---------------------------------------------------------------------------------
	private int determineQuality(String sIn)
	//---------------------------------------------------------------------------------
	{
		String sOne = xMSet.xU.keepLettersAndNumbers( qualitymarker ).trim();
		String sTwo = xMSet.xU.keepLettersAndNumbers( sIn ).trim();
		do_log(9,"[" + sOne + "] [" + sTwo + "]");
		char[] BufOne = sOne.toCharArray();
		char[] BufTwo = sTwo.toCharArray();		
		char[] BufThre = sOne.toUpperCase().toCharArray();	
		char[] BufFour = sTwo.toUpperCase().toCharArray();	
	    int cntr=0;
		for(int i=0;i<BufOne.length;i++) 
		{	
			 if( i >= BufTwo.length) break;
			 if( BufOne[i] == BufTwo[i] ) cntr++;
			 if( BufThre[i] == BufFour[i] ) cntr++;
		}		
		double qual = ((double)100 * (double)cntr) / (double)(BufOne.length * 2);
		return (int)qual;
	}

	//---------------------------------------------------------------------------------
	public boolean conclude_Tesseract()
	//---------------------------------------------------------------------------------
	{
		return true;
	}

	//---------------------------------------------------------------------------------
	public void doScrubbing()
	//---------------------------------------------------------------------------------
	{
		// if SCRUBBING is then srub
		for(int i=0;i<list.size();i++)
		{
			if( list.get(i).ingested == false ) continue;
			list.get(i).OCR = doScrubLine( list.get(i).OCR );
		}
	}

	// do not remove space at the end
	//---------------------------------------------------------------------------------
	private String doScrubLine(String sIn)
	//---------------------------------------------------------------------------------
	{
		//String sLog="";
		String sRet="";
        char[] sbuf = sIn.toCharArray();
        int skipped=0;
	    for(int i=0;i<sbuf.length;i++) 
		{	
		 int ichar = (int)sbuf[i];
		 //sLog += "[" + sbuf[i] + " " + ichar + "]";
		 if( (ichar < 32) || (ichar > 255) ) { skipped++; continue; }
		 sRet += sbuf[i];
		}
	    // transform to all upper or lowercase
	    int upper=0;
	    int lower=0;
	    int valid=0;
	    sbuf = sRet.toCharArray();
        for(int i=0;i<sbuf.length;i++) 
		{
          if( (sbuf[i] >= 'a') && (sbuf[i] <= 'z')) lower++;
          if( (sbuf[i] >= 'A') && (sbuf[i] <= 'Z')) upper++;
          if( sbuf[i] != ' ') valid++;
        }
	    int drempel = (int)((double)valid * (double)0.70);
        if( lower >= drempel ) { sRet = sRet.toLowerCase(); skipped++; }
        if( upper >= drempel ) { sRet = sRet.toUpperCase(); skipped++; }
        // double spaces
	    sbuf = sRet.toCharArray();
        String sTemp="";
        int sp=0;
        for(int i=0;i<sbuf.length;i++) 
    	{
           if( sbuf[i] == ' ') {
        	   sp++;
        	   if( sp > 1 ) { skipped++; continue;}
           }
           else sp=0;
           sTemp += sbuf[i];
        }
        sRet =sTemp;
    	//    
        if( skipped > 0 ) do_log( 9 , "[" + sIn + "] scrubbed-> [" + sRet + "] [Lo=" + lower + "] [Up=" + upper + "] [th=" + drempel + "]");
		return sRet;
	}
	
	//---------------------------------------------------------------------------------
	private String getOCRTextViaUID(long uid)
	//---------------------------------------------------------------------------------
	{
		for(int i=0;i<list.size();i++)
    	{
			if( list.get(i).ingested == false ) continue;
			if( list.get(i).UID == uid ) return list.get(i).OCR;
    	}
		return null;
	}

	//---------------------------------------------------------------------------------
	public boolean mergeOCRText()
	//---------------------------------------------------------------------------------
	{
		/*
		// debug
    	for(int i=0;i<list.size();i++)
    	{
    		do_log(1,"Line [" + list.get(i).UID + "] [" + list.get(i).ingested + "] [" + list.get(i).OCR + "]");
    	}
    	*/	
    	// start by reading the Text XML file
    	cmcTextDAO dao = new cmcTextDAO( xMSet , logger );
    	cmcTextObject[] txt = dao.readTextObjects();
    	//
    	String OriginalLanguage = dao.getOriginalLanguageCode();
    	do_log(5, "Read [" + txt.length + "] textobjects.  Original language [" + OriginalLanguage + "]");
    	// merge list and txt
    	int NbrOfChanges = 0;
    	for (int i=0;i<txt.length;i++)
    	{
    		String OCRText = getOCRTextViaUID( txt[i].UID );
    		if( OCRText == null ) continue;
    		OCRText = OCRText.trim();
    		if( OCRText.length() < 1 ) continue;
    		NbrOfChanges++;
    		do_log( 5 , "Merging [" + txt[i].UID + "] -> " + OCRText);
    		//
    		txt[i].TextOCR = OCRText;
    		txt[i].changeDate = System.currentTimeMillis();
    		txt[i].hasChanged = true;
    		if( doNotOverWriteText ) {
    		    if( txt[i].TextFrom == null ) continue;
    		    if( txt[i].TextFrom.trim().length() == 0 ) txt[i].TextFrom = OCRText;
    		}
    		else {
    			txt[i].TextFrom = OCRText;
    		}
    	}
    	boolean ib = true;
    	if( NbrOfChanges > 0 ) {
    		ib = dao.flushChangesToXML( txt , OriginalLanguage );
    		do_log(5,"text XML has been updated with [" + NbrOfChanges + "]");
    		if( ib ) {  // recreate the ZIP file
    			cmcArchiveDAO archo = new cmcArchiveDAO( xMSet , logger );
    			ib = archo.reZipAllFiles(xMSet.getCurrentArchiveFileName());
    			archo=null;
    		}
    	}
    	dao = null;
		return ib;
	}
	
	//---------------------------------------------------------------------------------
	public String getTesseractVersion()
	//---------------------------------------------------------------------------------
	{
		boolean ib = run_tesseract( TESSERACTCOMMAND.DO_VERSION );
		return ib == false ? "Unknown" : TesseractVersion;
	}
	
	
}

/*

Tesseract parameters:
classify_num_cp_levels               3             Number of Class Pruner Levels
textord_dotmatrix_gap               3             Max pixel gap for broken pixed pitch
textord_debug_block   0             Block to do debug on
textord_pitch_range     2             Max range test on pitch
textord_words_veto_power    5             Rows required to outvote a veto
textord_tabfind_show_strokewidths   0             Show stroke widths
pitsync_linear_version 6             Use new fast algorithm
pitsync_fake_depth      1             Max advance fake generation
oldbl_holed_losscount 10           Max lost before fallback line used
textord_skewsmooth_offset   4             For smooth factor
textord_skewsmooth_offset2 1             For smooth factor
textord_test_x                -2147483647      coord of test pt
textord_test_y                -2147483647      coord of test pt
textord_min_blobs_in_row      4             Min blobs before gradient counted
textord_spline_minblobs           8             Min blobs in each spline segment
textord_spline_medianwin       6             Size of window for spline segmentation
textord_max_blob_overlaps    4             Max number of blobs a big blob can overlap
textord_min_xheight   10           Min credible pixel xheight
textord_lms_line_trials               12           Number of linew fits to do
textord_tabfind_show_images               0             Show image blobs
textord_fp_chop_error               2             Max allowed bending of chop cells
edges_max_children_per_outline         10           Max number of children inside a character outline
edges_max_children_layers     5             Max layers of nested children inside a character outline
edges_children_per_grandchild              10           Importance ratio for chucking outlines
edges_children_count_limit      45           Max holes allowed in blob
edges_min_nonhole     12           Min pixels for potential char in box
edges_patharea_ratio  40           Max lensq/area for acceptable child outline
devanagari_split_debuglevel    0             Debug level for split shiro-rekha process.
textord_tabfind_show_partitions          0             Show partition bounds, waiting if >1
textord_debug_tabfind              0             Debug tab finding
textord_debug_bugs    0             Turn on output related to bugs in tab finding
textord_testregion_left              -1            Left edge of debug reporting rectangle
textord_testregion_top              -1            Top edge of debug reporting rectangle
textord_testregion_right            2147483647        Right edge of debug rectangle
textord_testregion_bottom      2147483647        Bottom edge of debug rectangle
editor_image_xpos       590         Editor image X Pos
editor_image_ypos       10           Editor image Y Pos
editor_image_menuheight        50           Add to image height for menu bar
editor_image_word_bb_color 7             Word bounding box colour
editor_image_blob_bb_color   4             Blob bounding box colour
editor_image_text_color            2             Correct text colour
editor_dbwin_xpos       50           Editor debug window X Pos
editor_dbwin_ypos       500         Editor debug window Y Pos
editor_dbwin_height    24           Editor debug window height
editor_dbwin_width     80           Editor debug window width
editor_word_xpos         60           Word window X Pos
editor_word_ypos         510         Word window Y Pos
editor_word_height      240         Word window height
editor_word_width       655         Word window width
wordrec_display_splits                0             Display splits
poly_debug       0             Debug old poly
poly_wide_objects_better        1             More accurate approx on wide things
wordrec_display_all_blobs        0             Display Blobs
wordrec_display_all_words       0             Display Words
wordrec_blob_pause   0             Blob pause
textord_fp_chopping   1             Do fixed pitch chopping
textord_force_make_prop_words        0             Force proportional word segmentation on all rows
textord_chopper_test 0             Chopper is being tested.
textord_restore_underlines     1             Chop underlines & put back
textord_show_initial_words     0             Display separate words
textord_show_new_words       0             Display separate words
textord_show_fixed_words     0             Display forced fixed pitch words
textord_blocksall_fixed               0             Moan about prop blocks
textord_blocksall_prop               0             Moan about fixed pitch blocks
textord_blocksall_testing           0             Dump stats when moaning
textord_test_mode      0             Do current test
textord_pitch_scalebigwords   0             Scale scores on big words
textord_all_prop            0             All doc is proportial text
textord_debug_pitch_test        0             Debug on fixed pitch test
textord_disable_pitch_test       0             Turn off dp fixed pitch algorithm
textord_fast_pitch_test              0             Do even faster pitch algorithm
textord_debug_pitch_metric   0             Write full metric stuff
textord_show_row_cuts            0             Draw row-level cuts
textord_show_page_cuts          0             Draw page-level cuts
textord_pitch_cheat     0             Use correct answer for fixed/prop
textord_blockndoc_fixed           0             Attempt whole doc/block fixed pitch
textord_show_tables   0             Show table regions
textord_tablefind_show_mark               0             Debug table marking steps in detail
textord_tablefind_show_stats                0             Show page stats used in table finding
textord_tablefind_recognize_tables     0             Enables the table recognizer for table layout and filtering.
textord_tabfind_show_initialtabs          0             Show tab candidates
textord_tabfind_show_finaltabs            0             Show tab vectors
textord_tabfind_only_strokewidths     0             Only run stroke widths
textord_really_old_xheight       0             Use original wiseowl xheight
textord_oldbl_debug   0             Debug old baseline generation
textord_debug_baselines          0             Debug baseline generation
textord_oldbl_paradef                1             Use para default mechanism
textord_oldbl_split_splines       1             Split stepped splines
textord_oldbl_merge_parts      1             Merge suspect partitions
oldbl_corrfix      1             Improve correlation of heights
oldbl_xhfix         0             Fix bug in modes threshold for xheights
textord_ocropus_mode              0             Make baselines for ocropus
textord_heavy_nr          0             Vigorously remove noise
textord_show_initial_rows        0             Display row accumulation
textord_show_parallel_rows    0             Display page correlated rows
textord_show_expanded_rows             0             Display rows after expanding
textord_show_final_rows          0             Display rows after final fitting
textord_show_final_blobs         0             Display blob bounds after pre-ass
textord_test_landscape              0             Tests refer to land/port
textord_parallel_baselines        1             Force parallel baselines
textord_straight_baselines        0             Force straight baselines
textord_old_baselines 1             Use old baseline algorithm
textord_old_xheight     0             Use old xheight algorithm
textord_fix_xheight_bug           1             Use spline baseline
textord_fix_makerow_bug       1             Prevent multiple baselines
textord_debug_xheights            0             Test xheight algorithms
textord_biased_skewcalc           1             Bias skew estimates with line length
textord_interpolating_skew     1             Interpolate across gaps
textord_new_initial_xheight    1             Use test xheight mechanism
textord_debug_blob    0             Print test blob information
gapmap_debug               0             Say which blocks have tables
gapmap_use_ends        0             Use large space at start and end of rows
gapmap_no_isolated_quanta   0             Ensure gaps not less than 2quanta wide
edges_use_new_outline_complexity  0             Use the new outline complexity module
edges_debug   0             turn on debugging for this module
edges_children_fix        0             Remove boxy parents of char-like children
textord_show_fixed_cuts          0             Draw fixed pitch cell boundaries
devanagari_split_debugimage 0             Whether to create a debug image for split shiro-rekha process.
textord_tabfind_show_color_fit            0             Show stroke widths
textord_tabfind_show_initial_partitions            0             Show partition bounds
textord_tabfind_show_reject_blobs    0             Show blobs rejected as noise
textord_tabfind_show_columns            0             Show column bounds
textord_tabfind_show_blocks 0             Show final block bounds
textord_tabfind_find_tables    1             run table detection
textord_space_size_is_variable              0             If true, word delimiter spaces are assumed to have variable width, even though characters have fixed pitch.
textord_debug_printable           0             Make debug windows printable
equationdetect_save_bi_image             0             Save input bi image
equationdetect_save_spt_image           0             Save special character image
equationdetect_save_seed_image       0             Save the seed image
equationdetect_save_merged_image 0             Save the merged image
stream_filelist  0             Stream a filelist from stdin
debug_file                         File to send tprintf output to
classify_training_file      MicroFeatures  Training file
classify_font_name       UnknownFont  Default font name to be used in training
fx_debugfile     FXDebug             Name of debugfile
editor_image_win_name           EditorImage       Editor image window name
editor_dbwin_name     EditorDBWin      Editor debug window name
editor_word_name       BlnWords            BL normalized word window
editor_debug_config_file                          Config file to apply to single words
classify_pico_feature_length    0.05       Pico Feature Length
classify_norm_adj_midpoint     32           Norm adjust midpoint ...
classify_norm_adj_curl                2             Norm adjust curl ...
classify_min_slope         0.414214              Slope below which lines are called horizontal
classify_max_slope        2.41421                Slope above which lines are called vertical
classify_cp_angle_pad_loose   45           Class Pruner Angle Pad Loose
classify_cp_angle_pad_medium             20           Class Pruner Angle Pad Medium
classify_cp_angle_pad_tight     10           CLass Pruner Angle Pad Tight
classify_cp_end_pad_loose      0.5          Class Pruner End Pad Loose
classify_cp_end_pad_medium                0.5          Class Pruner End Pad Medium
classify_cp_end_pad_tight        0.5          Class Pruner End Pad Tight
classify_cp_side_pad_loose      2.5          Class Pruner Side Pad Loose
classify_cp_side_pad_medium                1.2          Class Pruner Side Pad Medium
classify_cp_side_pad_tight        0.6          Class Pruner Side Pad Tight
classify_pp_angle_pad 45           Proto Pruner Angle Pad
classify_pp_end_pad    0.5          Proto Prune End Pad
classify_pp_side_pad   2.5          Proto Pruner Side Pad
textord_underline_offset          0.1          Fraction of x to ignore
textord_wordstats_smooth_factor       0.05       Smoothing gap stats
textord_width_smooth_factor                0.1          Smoothing width stats
textord_words_width_ile          0.4          Ile of blob widths for space est
textord_words_maxspace         4             Multiple of xheight
textord_words_default_maxspace       3.5          Max believable third space
textord_words_default_minspace        0.6          Fraction of xheight
textord_words_min_minspace               0.3          Fraction of xheight
textord_words_default_nonspace        0.2          Fraction of xheight
textord_words_initial_lower    0.25       Max initial cluster size
textord_words_initial_upper    0.15       Min initial cluster spacing
textord_words_minlarge            0.75       Fraction of valid gaps needed
textord_words_pitchsd_threshold        0.04       Pitch sync threshold
textord_words_def_fixed         0.016     Threshold for definite fixed
textord_words_def_prop          0.09       Threshold for definite prop
textord_pitch_rowsimilarity      0.08       Fraction of xheight for sameness
words_initial_lower       0.5          Max initial cluster size
words_initial_upper      0.15       Min initial cluster spacing
words_default_prop_nonspace              0.25       Fraction of xheight
words_default_fixed_space     0.75       Fraction of xheight
words_default_fixed_limit        0.6          Allowed size variance
textord_words_definite_spread            0.3          Non-fuzzy spacing region
textord_spacesize_ratiofp         2.8          Min ratio space/nonspace
textord_spacesize_ratioprop    2             Min ratio space/nonspace
textord_fpiqr_ratio       1.5          Pitch IQR/Gap IQR threshold
textord_max_pitch_iqr               0.2          Xh fraction noise in pitch
textord_fp_min_width                0.5          Min width of decent blobs
textord_projection_scale           0.2          Ding rate for mid-cuts
textord_balance_factor              1             Ding rate for unbalanced char cells
textord_tabvector_vertical_gap_fraction           0.5          max fraction of mean blob width allowed for vertical gaps in vertical text
textord_tabvector_vertical_box_ratio 0.5          Fraction of box matches required to declare a line vertical
pitsync_joined_edge    0.75       Dist inside big blob for chopping
pitsync_offset_freecut_fraction             0.25       Fraction of cut for free cuts
oldbl_xhfract    0.4          Fraction of est allowed in calc
oldbl_dot_error_size    1.26       Max aspect ratio of a dot
textord_oldbl_jumplimit             0.15       X fraction for new partition
textord_spline_shift_fraction   0.02       Fraction of line spacing for quad
textord_spline_outlier_fraction              0.1          Fraction of line spacing for outlier
textord_skew_ile           0.5          Ile of gradients for page skew
textord_skew_lag          0.02       Lag for skew on row accumulation
textord_linespace_iqrlimit         0.2          Max iqr/median for linespace
textord_width_limit      8             Max width of blobs to make rows
textord_chop_width     1.5          Max width before chopping
textord_expansion_factor         1             Factor to expand rows by in expand_rows
textord_overlap_x         0.375     Fraction of linespace for good overlap
textord_minxh 0.25       fraction of linesize for min xheight
textord_min_linesize    1.25       * blob height for initial linesize
textord_excess_blobsize            1.3          New row made if blob makes row this big
textord_occupancy_threshold 0.4          Fraction of neighbourhood
textord_underline_width           2             Multiple of line_size for underline
textord_min_blob_height_fraction       0.75       Min blob height/top to include blob top into xheight stats
textord_xheight_mode_fraction            0.4          Min pile height to make xheight
textord_ascheight_mode_fraction        0.08       Min pile height to make ascheight
textord_descheight_mode_fraction     0.08       Min pile height to make descheight
textord_ascx_ratio_min              1.25       Min cap/xheight
textord_ascx_ratio_max             1.8          Max cap/xheight
textord_descx_ratio_min           0.25       Min desc/xheight
textord_descx_ratio_max          0.6          Max desc/xheight
textord_xheight_error_margin               0.1          Accepted variation
gapmap_big_gaps          1.75       xht multiplier
textord_fp_chop_snap               0.5          Max distance of chop pt from vertex
edges_childarea              0.5          Min area fraction of child outline
edges_boxarea               0.875     Min area fraction of grandchild for box
textord_underline_threshold   0.5          Fraction of width occupied
ambigs_debug_level     0             Debug level for unichar ambiguities
tessedit_single_match 0             Top choice only from CP
classify_debug_level     0             Classify debug level
classify_norm_method                1             Normalization Method   ...
matcher_debug_level  0             Matcher Debug Level
matcher_debug_flags  0             Matcher Debug Flags
classify_learning_debug_level 0             Learning Debug Level: 
matcher_permanent_classes_min         1             Min # of permanent classes
matcher_min_examples_for_prototyping         3             Reliable Config Threshold
matcher_sufficient_examples_for_prototyping             5             Enable adaption even if the ambiguities have not been seen
classify_adapt_proto_threshold             230         Threshold for good protos during adaptive 0-255
classify_adapt_feature_threshold         230         Threshold for good features during adaptive 0-255
classify_class_pruner_threshold             229         Class Pruner Threshold 0-255
classify_class_pruner_multiplier             15           Class Pruner Multiplier 0-255:       
classify_cp_cutoff_strength      7             Class Pruner CutoffStrength:         
classify_integer_matcher_multiplier     10           Integer Matcher Multiplier  0-255:   
il1_adaption_test           0             Don't adapt to i/I at beginning of word
dawg_debug_level        0             Set to 1 for general debug info, to 2 for more details, to 3 to see all the debug messages
hyphen_debug_level   0             Debug level for hyphenated words.
max_viterbi_list_size    10           Maximum size of viterbi list.
stopper_smallword_size             2             Size of dict word to be treated as non-dict word
stopper_debug_level   0             Stopper debug level
tessedit_truncate_wordchoice_log       10           Max words to keep in list
fragments_debug          0             Debug character fragments
max_permuter_attempts          10000    Maximum number of different character choices to consider during permutation. This limit is especially useful when user patterns are specified, since overly generic patterns can result in dawg search exploring an overly large number of options.
repair_unchopped_blobs           1             Fix blobs that aren't chopped
chop_debug      0             Chop debug
chop_split_length          10000    Split Length
chop_same_distance    2             Same distance
chop_min_outline_points          6             Min Number of Points on Outline
chop_seam_pile_size   150         Max number of seams in seam_pile
chop_inside_angle         -50         Min Inside Angle Bend
chop_min_outline_area              2000      Min Outline Area
chop_centered_maxwidth        90           Width of (smaller) chopped blobs above which we don't care that a chop is not near the center.
chop_x_y_weight          3             X / Y  length weight
segment_adjust_debug              0             Segmentation adjustment debug
wordrec_debug_level  0             Debug level for wordrec
wordrec_max_join_chunks       4             Max number of broken pieces to associate
segsearch_debug_level              0             SegSearch debug level
segsearch_max_pain_points    2000      Maximum number of pain points stored in the queue
segsearch_max_futile_classifications   20           Maximum number of pain point classifications per chunk thatdid not result in finding a better word choice.
language_model_debug_level 0             Language model debug level
language_model_ngram_order               8             Maximum order of the character ngram model
language_model_viterbi_list_max_num_prunable       10           Maximum number of prunable (those for which PrunablePath() is true) entries in each viterbi list recorded in BLOB_CHOICEs
language_model_viterbi_list_max_size              500         Maximum size of viterbi lists recorded in BLOB_CHOICEs
language_model_min_compound_length         3             Minimum length of compound words
wordrec_display_segmentations            0             Display Segmentations
tessedit_pageseg_mode            6             Page seg mode: 0=osd only, 1=auto+osd, 2=auto, 3=col, 4=block, 5=line, 6=word, 7=char (Values from PageSegMode enum in publictypes.h)
tessedit_ocr_engine_mode      2             Which OCR engine(s) to run (Tesseract, LSTM, both). Defaults to loading and running the most accurate available.
pageseg_devanagari_split_strategy      0             Whether to use the top-line splitting process for Devanagari documents while performing page-segmentation.
ocr_devanagari_split_strategy 0             Whether to use the top-line splitting process for Devanagari documents while performing ocr.
bidi_debug        0             Debug level for BiDi
applybox_debug             1             Debug level
applybox_page                0             Page number to apply boxes from
tessedit_bigram_debug              0             Amount of debug output for bigram correction.
debug_noise_removal 0             Debug reassignment of small outlines
noise_maxperblob         8             Max diacritics to apply to a blob
noise_maxperword       16           Max diacritics to apply to a word
debug_x_ht_level          0             Reestimate debug
quality_min_initial_alphas_reqd             2             alphas in a good word
tessedit_tess_adaption_mode                39           Adaptation decision algorithm for tess
tessedit_test_adaption_mode                3             Adaptation decision algorithm for tess
multilang_debug_level                0             Print multilang debug info.
paragraph_debug_level              0             Print paragraph debug info.
tessedit_preserve_min_wd_len             2             Only preserve wds longer than this
crunch_rating_max        10           For adj length in rating per ch
crunch_pot_indicators 1             How many potential indicators needed
crunch_leave_lc_strings              4             Don't crunch words with long lower case strings
crunch_leave_uc_strings            4             Don't crunch words with long lower case strings
crunch_long_repetitions             3             Crunch words with long repetitions
crunch_debug  0             As it says
fixsp_non_noise_limit  1             How many non-noise blbs either side?
fixsp_done_mode         1             What constitues done for spacing
debug_fix_space_level               0             Contextual fixspace debug
x_ht_acceptance_tolerance      8             Max allowed deviation of blob top outside of font data
x_ht_min_change          8             Min change in xht before actually trying it
superscript_debug         0             Debug level for sub & superscript fixer
suspect_level   99           Suspect marker level
suspect_space_level     100         Min suspect level for rejecting spaces
suspect_short_words   2             Don't suspect dict wds longer than this
tessedit_reject_mode 0             Rejection algorithm
tessedit_image_border               2             Rej blbs near image edge limit
min_sane_x_ht_pixels 8             Reject any x-ht lt or eq than this
tessedit_page_number               -1            -1 -> All pages , else specific page to process
tessdata_manager_debug_level            0             Debug level for TessdataManager functions.
tessedit_parallelize        0             Run in parallel where possible
tessedit_ok_mode        5             Acceptance decision algorithm
segment_debug             0             Debug the whole segmentation process
language_model_fixed_length_choices_depth              3             Depth of blob choice lists to explore when fixed length dawgs are on
tosp_debug_level          0             Debug data
tosp_enough_space_samples_for_median      3             or should we use mean
tosp_redo_kern_limit  10           No.samples reqd to reestimate for row
tosp_few_samples        40           No.gaps reqd with 1 large gap to treat as a table
tosp_short_row              20           No.gaps reqd with few cert spaces to use certs
tosp_sanity_method    1             How to avoid being silly
textord_max_noise_size            7             Pixel size of noise
textord_baseline_debug            0             Baseline debug level
textord_noise_sizefraction        10           Fraction of size for maxima
textord_noise_translimit            16           Transitions for normal blob
textord_noise_sncount               1             super norm blobs to save row
use_definite_ambigs_for_classifier       0             Use definite ambiguities when running character classifier
use_ambigs_for_adaption         0             Use ambigs for deciding whether to adapt to a character
allow_blob_division       1             Use divisible blobs chopping
prioritize_division           0             Prioritize blob division over chopping
classify_enable_learning             1             Enable adaptive classifier
tess_cn_matching          0             Character Normalized Matching
tess_bn_matching         0             Baseline Normalized Matching
classify_enable_adaptive_matcher       1             Enable adaptive classifier
classify_use_pre_adapted_templates 0             Use pre-adapted classifier templates
classify_save_adapted_templates         0             Save adapted templates to a file
classify_enable_adaptive_debugger     0             Enable match debugger
classify_nonlinear_norm             0             Non-linear stroke-density normalization
disable_character_fragments   1             Do not include character fragments in the results of the classifier
classify_debug_character_fragments   0             Bring up graphical debugging windows for fragments training
matcher_debug_separate_windows    0             Use two different windows for debugging the matching: One for the protos and one for the features.
classify_bln_numeric_mode     0             Assume the input is numbers [0-9].
load_system_dawg       1             Load system word dawg.
load_freq_dawg             1             Load frequent word dawg.
load_unambig_dawg    1             Load unambiguous word dawg.
load_punc_dawg            1             Load dawg with punctuation patterns.
load_number_dawg      1             Load dawg with number patterns.
load_bigram_dawg        1             Load dawg with special word bigrams.
use_only_first_uft8_step           0             Use only the first UTF8 step of the given string when computing log probabilities.
stopper_no_acceptable_choices            0             Make AcceptableChoice() always return false. Useful when there is a need to explore all segmentations
save_raw_choices          0             Deprecated- backward compatibility only
segment_nonalphabetic_script               0             Don't use any alphabetic-specific tricks.Set to true in the traineddata config file for scripts that are cursive or inherently fixed-pitch
save_doc_words            0             Save Document Words
merge_fragments_in_matrix    1             Merge the fragments in the ratings matrix and delete them after merging
wordrec_no_block         0             Don't output block information
wordrec_enable_assoc               1             Associator Enable
force_word_assoc         0             force associator to run regardless of what enable_assoc is.This is used for CJK where component grouping is necessary.
fragments_guide_chopper        0             Use information from fragments to guide chopping process
chop_enable     1             Chop enable
chop_vertical_creep     0             Vertical creep
chop_new_seam_pile  1             Use new seam_pile
assume_fixed_pitch_char_segment     0             include fixed-pitch heuristics in char segmentation
wordrec_skip_no_truth_words              0             Only run OCR for words that had truth recorded in BlamerBundle
wordrec_debug_blamer             0             Print blamer debug messages
wordrec_run_blamer   0             Try to set the blame for errors
save_alt_choices            1             Save alternative paths found during chopping and segmentation search
language_model_ngram_on     0             Turn on/off the use of character ngram model
language_model_ngram_use_only_first_uft8_step     0             Use only the first UTF8 step of the given string when computing log probabilities.
language_model_ngram_space_delimited_language   1             Words are delimited by space
language_model_use_sigmoidal_certainty        0             Use sigmoidal score for certainty
tessedit_resegment_from_boxes         0             Take segmentation and labeling from box file
tessedit_resegment_from_line_boxes               0             Conversion of word/line box file to char box file
tessedit_train_from_boxes       0             Generate training data from boxed chars
tessedit_make_boxes_from_boxes     0             Generate more boxes from boxed chars
tessedit_train_line_recognizer                0             Break input into lines and remap boxes if present
tessedit_dump_pageseg_images           0             Dump intermediate images made during page segmentation
tessedit_ambigs_training           0             Perform training for ambiguities
tessedit_adaption_debug          0             Generate and print debug information for adaption
applybox_learn_chars_and_char_frags_mode                0             Learn both character fragments (as is done in the special low exposure mode) as well as unfragmented characters.
applybox_learn_ngrams_mode              0             Each bounding box is assumed to contain ngrams. Only learn the ngrams whose outlines overlap horizontally.
tessedit_display_outwords       0             Draw output words
tessedit_dump_choices              0             Dump char choices
tessedit_timing_debug               0             Print timing stats
tessedit_fix_fuzzy_spaces         1             Try to improve fuzzy spaces
tessedit_unrej_any_wd              0             Don't bother with word plausibility
tessedit_fix_hyphens   1             Crunch double hyphens?
tessedit_redo_xheight                1             Check/Correct x-height
tessedit_enable_doc_dict          1             Add words to the document dictionary
tessedit_debug_fonts  0             Output font info per char
tessedit_debug_block_rejection            0             Block and Row stats
tessedit_enable_bigram_correction     1             Enable correction based on the word bigram dictionary.
tessedit_enable_dict_correction            0             Enable single word correction based on the dictionary.
enable_noise_removal                1             Remove and conditionally reassign small outlines when they confuse layout analysis, determining diacritics vs noise
debug_acceptable_wds              0             Dump word pass/fail chk
tessedit_minimal_rej_pass1     0             Do minimal rejection on pass 1 output
tessedit_test_adaption               0             Test adaption criteria
tessedit_matcher_log  0             Log matcher activity
test_pt 0             Test for point
paragraph_text_based                1             Run paragraph detection on the post-text-recognition (more accurate)
lstm_use_matrix             1             Use ratings matrix/beam search with lstm
docqual_excuse_outline_errs  0             Allow outline errs in unrejection?
tessedit_good_quality_unrej   1             Reduce rejection on good docs
tessedit_use_reject_spaces     1             Reject spaces?
tessedit_preserve_blk_rej_perfect_wds           1             Only rej partially rejected words in block rejection
tessedit_preserve_row_rej_perfect_wds         1             Only rej partially rejected words in row rejection
tessedit_dont_blkrej_good_wds           0             Use word segmentation quality metric
tessedit_dont_rowrej_good_wds         0             Use word segmentation quality metric
tessedit_row_rej_good_docs  1             Apply row rejection to good docs
tessedit_reject_bad_qual_wds               1             Reject all bad quality wds
tessedit_debug_doc_rejection               0             Page stats
tessedit_debug_quality_metrics            0             Output data to debug file
bland_unrej      0             unrej potential with no checks
unlv_tilde_crunching    1             Mark v.bad words for tilde crunch
hocr_font_info                0             Add font info to hocr output
crunch_early_merge_tess_fails              1             Before word crunch?
crunch_early_convert_bad_unlv_chs   0             Take out ~^ early?
crunch_terrible_garbage            1             As it says
crunch_pot_garbage     1             POTENTIAL crunch garbage
crunch_leave_ok_strings            1             Don't touch sensible strings
crunch_accept_ok          1             Use acceptability in okstring
crunch_leave_accept_strings   0             Don't pot crunch sensible strings
crunch_include_numerals          0             Fiddle alpha figures
tessedit_prefer_joined_punct 0             Reward punctation joins
tessedit_write_block_separators           0             Write block separators in output
tessedit_write_rep_codes         0             Write repetition char code
tessedit_write_unlv      0             Write .unlv output file
tessedit_create_txt       0             Write .txt output file
tessedit_create_hocr   0             Write .html hOCR output file
tessedit_create_tsv      0             Write .tsv output file
tessedit_create_pdf     0             Write .pdf output file
textonly_pdf     0             Create PDF with only one invisible text layer
suspect_constrain_1Il   0             UNLV keep 1Il chars rejected
tessedit_minimal_rejection       0             Only reject tess failures
tessedit_zero_rejection              0             Don't reject ANYTHING
tessedit_word_for_word           0             Make output have exactly one word per WERD
tessedit_zero_kelvin_rejection               0             Don't reject ANYTHING AT ALL
tessedit_consistent_reps           1             Force all rep chars the same
tessedit_rejection_debug          0             Adaption debug
tessedit_flip_0O             1             Contextual 0O O0 flips
rej_trust_doc_dawg     0             Use DOC dawg in 11l conf. detector
rej_1Il_use_dict_word 0             Use dictword test
rej_1Il_trust_permuter_type   1             Don't double check
rej_use_tess_accepted               1             Individual rejection control
rej_use_tess_blanks     1             Individual rejection control
rej_use_good_perm     1             Individual rejection control
rej_use_sensible_wd   0             Extend permuter check
rej_alphas_in_number_perm  0             Extend permuter check
tessedit_create_boxfile              0             Output text with boxes
tessedit_write_images                0             Capture the image from the IPE
interactive_display_mode          0             Run interactively?
tessedit_override_permuter    1             According to dict_word
tessedit_use_primary_params_model                0             In multilingual mode use params model of the primary language
textord_tabfind_show_vlines  0             Debug line finding
textord_use_cjk_fp_model      0             Use CJK fixed pitch model
poly_allow_detailed_fx               0             Allow feature extractors to see the original outline
tessedit_init_config_only           0             Only initialize with the config file. Useful if the instance is not going to be used for OCR but say only for layout analysis.
textord_equation_detect          0             Turn on equation detector
textord_tabfind_vertical_text  1             Enable vertical detection
textord_tabfind_force_vertical_text    0             Force using vertical text page mode
preserve_interword_spaces     0             Preserve multiple interword spaces
include_page_breaks   0             Include page separator string in output text after each image/page.
textord_tabfind_vertical_horizontal_mix           1             find horizontal lines such as headers in vertical page mode
load_fixed_length_dawgs          1             Load fixed length dawgs (e.g. for non-space delimited languages)
permute_debug             0             Debug char permutation process
permute_script_word  0             Turn on word script consistency permuter
segment_segcost_rating            0             incorporate segmentation cost in word rating?
permute_fixed_length_dawg  0             Turn on fixed-length phrasebook search permuter
permute_chartype_word           0             Turn on character type (property) consistency permuter
ngram_permuter_activated      0             Activate character-level n-gram-based permuter
permute_only_top        0             Run only the top choice permuter
use_new_state_cost    0             use new state cost heuristics for segmentation state evaluation
enable_new_segsearch              1             Enable new segmentation search path.
textord_single_height_mode   0             Script has no xheight, so use a single mode
tosp_old_to_method   0             Space stats use prechopping?
tosp_old_to_constrain_sp_kn 0             Constrain relative values of inter and intra-word gaps for old_to_method.
tosp_only_use_prop_rows       1             Block stats to use fixed pitch rows?
tosp_force_wordbreak_on_punct         0             Force word breaks on punct to break long lines in non-space delimited langs
tosp_use_pre_chopping             0             Space stats use prechopping?
tosp_old_to_bug_fix    0             Fix suspected bug in old code
tosp_block_use_cert_spaces   1             Only stat OBVIOUS spaces
tosp_row_use_cert_spaces      1             Only stat OBVIOUS spaces
tosp_narrow_blobs_not_cert  1             Only stat OBVIOUS spaces
tosp_row_use_cert_spaces1    1             Only stat OBVIOUS spaces
tosp_recovery_isolated_row_stats       1             Use row alone when inadequate cert spaces
tosp_only_small_gaps_for_kern            0             Better guess
tosp_all_flips_fuzzy       0             Pass ANY flip to context?
tosp_fuzzy_limit_all      1             Don't restrict kn->sp fuzzy limit to tables
tosp_stats_use_xht_gaps          1             Use within xht gap for wd breaks
tosp_use_xht_gaps       1             Use within xht gap for wd breaks
tosp_only_use_xht_gaps           0             Only use within xht gap for wd breaks
tosp_rule_9_test_punct             0             Don't chng kn to space next to punct
tosp_flip_fuzz_kn_to_sp            1             Default flip
tosp_flip_fuzz_sp_to_kn            1             Default flip
tosp_improve_thresh  0             Enable improvement heuristic
textord_no_rejects       0             Don't remove noise blobs
textord_show_blobs    0             Display unsorted blobs
textord_show_boxes   0             Display unsorted blobs
textord_noise_rejwords             1             Reject noise-like words
textord_noise_rejrows               1             Reject noise-like rows
textord_noise_debug  0             Debug row garbage detector
m_data_sub_dir             tessdata/            Directory for data files
tessedit_module_name              libtesseract        Module colocated with tessdata dir
classify_learn_debug_str                           Class str to debug learning
user_words_file                             A filename of user-provided words.
user_words_suffix                        A suffix of user-provided words located in tessdata.
user_patterns_file                         A filename of user-provided patterns.
user_patterns_suffix                    A suffix of user-provided patterns located in tessdata.
output_ambig_words_file                         Output file for ambiguities found in the dictionary
word_to_debug                             Word for which stopper debug information should be printed to stdout
word_to_debug_lengths                           Lengths of unichars in word_to_debug
tessedit_char_blacklist                Blacklist of chars not to recognize
tessedit_char_whitelist                               Whitelist of chars to recognize
tessedit_char_unblacklist                           List of chars to override tessedit_char_blacklist
tessedit_write_params_to_file                               Write all parameters to the given file.
applybox_exposure_pattern    .exp       Exposure value follows this pattern in the image filename. The name of the image files are expected to be in the form [lang].[fontname].exp[num].tif
chs_leading_punct         ('`"          Leading punctuation
chs_trailing_punct1       ).,;:?!     1st Trailing punctuation
chs_trailing_punct2       )'`"          2nd Trailing punctuation
outlines_odd    %|          Non standard number of outlines
outlines_2          ij!?%":; Non standard number of outlines
numeric_punctuation   .,             Punct. chs expected WITHIN numbers
unrecognised_char        |             Output char for unidentified blobs
ok_repeated_ch_non_alphanum_wds               -?*=      Allow NN to unrej
conflict_set_I_l_1           Il1[]        Il1 conflict set
file_type             .tif          Filename extension
tessedit_load_sublangs                              List of languages to load with this one
page_separator               
page separator (default is form feed control character)
classify_char_norm_range         0.2          Character Normalization Range ...
classify_min_norm_scale_x       0             Min char x-norm scale ...
classify_max_norm_scale_x      0.325     Max char x-norm scale ...
classify_min_norm_scale_y       0             Min char y-norm scale ...
classify_max_norm_scale_y      0.325     Max char y-norm scale ...
classify_max_rating_ratio           1.5          Veto ratio between classifier ratings
classify_max_certainty_margin               5.5          Veto difference between classifier certainties
matcher_good_threshold          0.125     Good Match (0-1)
matcher_reliable_adaptive_result         0             Great Match (0-1)
matcher_perfect_threshold      0.02       Perfect Match (0-1)
matcher_bad_match_pad          0.15       Bad Match Pad (0-1)
matcher_rating_margin               0.1          New template margin (0-1)
matcher_avg_noise_size            12           Avg. noise blob length
matcher_clustering_max_angle_delta 0.015     Maximum angle delta for prototype clustering
classify_misfit_junk_penalty     0             Penalty to apply when a non-alnum is vertically out of its expected textline position
rating_scale       1.5          Rating scaling factor
certainty_scale 20           Certainty scaling factor
tessedit_class_miss_scale          0.00390625         Scale factor for features not used
classify_adapted_pruning_factor           2.5          Prune poor adapted results this much worse than best result
classify_adapted_pruning_threshold    -1            Threshold at which classify_adapted_pruning_factor starts
classify_character_fragments_garbage_certainty_threshold    -3            Exclude fragments that do not look like whole characters from training and adaption
speckle_large_max_size             0.3          Max large speckle size
speckle_rating_penalty               10           Penalty to add to worst rating for noise
xheight_penalty_subscripts      0.125     Score penalty (0.1 = 10%) added if there are subscripts or superscripts in a word, but it is otherwise OK.
xheight_penalty_inconsistent  0.25       Score penalty (0.1 = 10%) added if an xheight is inconsistent.
segment_penalty_dict_frequent_word              1             Score multiplier for word matches which have good case andare frequent in the given language (lower is better).
segment_penalty_dict_case_ok             1.1          Score multiplier for word matches that have good case (lower is better).
segment_penalty_dict_case_bad          1.3125   Default score multiplier for word matches, which may have case issues (lower is better).
segment_penalty_ngram_best_choice               1.24       Multipler to for the best choice from the ngram model.
segment_penalty_dict_nonword           1.25       Score multiplier for glyph fragment segmentations which do not match a dictionary word (lower is better).
segment_penalty_garbage        1.5          Score multiplier for poorly cased strings that are not in the dictionary and generally look like garbage (lower is better).
certainty_scale 20           Certainty scaling factor
stopper_nondict_certainty_base           -2.5        Certainty threshold for non-dict words
stopper_phase2_certainty_rejection_offset    1             Reject certainty offset
stopper_certainty_per_char     -0.5        Certainty to add for each dict char above small word size.
stopper_allowable_character_badness               3             Max certaintly variation allowed in a word (in sigma)
doc_dict_pending_threshold    0             Worst certainty for using pending dictionary
doc_dict_certainty_threshold  -2.25      Worst certainty for words that can be inserted into thedocument dictionary
wordrec_worst_state   1             Worst segmentation state
tessedit_certainty_threshold   -2.25      Good blob limit
chop_split_dist_knob   0.5          Split length adjustment
chop_overlap_knob      0.9          Split overlap adjustment
chop_center_knob        0.15       Split center adjustment
chop_sharpness_knob 0.06       Split sharpness adjustment
chop_width_change_knob        5             Width change adjustment
chop_ok_split   100         OK split limit
chop_good_split             50           Good split limit
segsearch_max_char_wh_ratio              2             Maximum character width-to-height ratio
language_model_ngram_small_prob   1e-006  To avoid overly small denominators use this as the floor of the probability returned by the ngram model.
language_model_ngram_nonmatch_score       -40         Average classifier score of a non-matching unichar.
language_model_ngram_scale_factor 0.03       Strength of the character ngram model relative to the character classifier 
language_model_ngram_rating_factor               16           Factor to bring log-probs into the same range as ratings when multiplied by outline length 
language_model_penalty_non_freq_dict_word            0.1          Penalty for words not in the frequent word dictionary
language_model_penalty_non_dict_word        0.15       Penalty for non-dictionary words
language_model_penalty_punc              0.2          Penalty for inconsistent punctuation
language_model_penalty_case               0.1          Penalty for inconsistent case
language_model_penalty_script             0.5          Penalty for inconsistent script
language_model_penalty_chartype      0.3          Penalty for inconsistent character type
language_model_penalty_font               0             Penalty for inconsistent font
language_model_penalty_spacing         0.05       Penalty for inconsistent spacing
language_model_penalty_increment   0.01       Penalty increment
noise_cert_basechar    -8            Hingepoint for base char certainty
noise_cert_disjoint        -1            Hingepoint for disjoint certainty
noise_cert_punc             -3            Threshold for new punc char certainty
noise_cert_factor           0.375     Scaling on certainty diff from Hingepoint
quality_rej_pc  0.08       good_quality_doc lte rejection limit
quality_blob_pc              0             good_quality_doc gte good blobs limit
quality_outline_pc         1             good_quality_doc lte outline error limit
quality_char_pc               0.95       good_quality_doc gte good char limit
test_pt_x           100000  xcoord
test_pt_y           100000  ycoord
tessedit_reject_doc_percent   65           %rej allowed before rej whole doc
tessedit_reject_block_percent               45           %rej allowed before rej whole block
tessedit_reject_row_percent  40           %rej allowed before rej whole row
tessedit_whole_wd_rej_row_percent                70           Number of row rejects in whole word rejectswhich prevents whole row rejection
tessedit_good_doc_still_rowrej_wd    1.1          rej good doc wd if more than this fraction rejected
quality_rowrej_pc          1.1          good_quality_doc gte good char limit
crunch_terrible_rating 80           crunch rating lt this
crunch_poor_garbage_cert       -9            crunch garbage cert lt this
crunch_poor_garbage_rate       60           crunch garbage rating lt this
crunch_pot_poor_rate                40           POTENTIAL crunch rating lt this
crunch_pot_poor_cert -8            POTENTIAL crunch cert lt this
crunch_del_rating          60           POTENTIAL crunch rating lt this
crunch_del_cert              -10         POTENTIAL crunch cert lt this
crunch_del_min_ht       0.7          Del if word ht lt xht x this
crunch_del_max_ht      3             Del if word ht gt xht x this
crunch_del_min_width               3             Del if word width lt xht x this
crunch_del_high_word               1.5          Del if word gt xht x this above bl
crunch_del_low_word 0.5          Del if word gt xht x this below bl
crunch_small_outlines_size       0.6          Small if lt xht x this
fixsp_small_outlines_size           0.28       Small if lt xht x this
superscript_worse_certainty    2             How many times worse certainty does a superscript position glyph need to be for us to try classifying it as a char with a different baseline?
superscript_bettered_certainty              0.97       What reduction in badness do we think sufficient to choose a superscript over what we'd thought.  For example, a value of 0.6 means we want to reduce badness of certainty by at least 40%
superscript_scaledown_ratio    0.4          A superscript scaled down more than this is unbelievably small.  For example, 0.3 means we expect the font size to be no smaller than 30% of the text line font size.
subscript_max_y_top   0.5          Maximum top of a character measured as a multiple of x-height above the baseline for us to reconsider whether it's a subscript.
superscript_min_y_bottom       0.3          Minimum bottom of a character measured as a multiple of x-height above the baseline for us to reconsider whether it's a superscript.
suspect_rating_per_ch                999.9     Don't touch bad rating limit
suspect_accept_rating -999.9   Accept good rating limit
tessedit_lower_flip_hyphen     1.5          Aspect ratio dot/hyphen test
tessedit_upper_flip_hyphen    1.8          Aspect ratio dot/hyphen test
rej_whole_of_mostly_reject_word_fract          0.85       if >this fract
min_orientation_margin             7             Min acceptable orientation margin
textord_tabfind_vertical_text_ratio     0.5          Fraction of textlines deemed vertical to use vertical page mode
textord_tabfind_aligned_gap_fraction                0.75       Fraction of height used as a minimum gap for aligned blobs.
bestrate_pruning_factor            2             Multiplying factor of current best rate to prune other hypotheses
segment_reward_script              0.95       Score multipler for script consistency within a word. Being a 'reward' factor, it should be <= 1. Smaller value implies bigger reward.
segment_reward_chartype       0.97       Score multipler for char type consistency within a word. 
segment_reward_ngram_best_choice                0.99       Score multipler for ngram permuter's best choice (only used in the Han script path).
heuristic_segcost_rating_base 1.25       base factor for adding segmentation cost into word rating.It's a multiplying factor, the larger the value above 1, the bigger the effect of segmentation cost.
heuristic_weight_rating              1             weight associated with char rating in combined cost ofstate
heuristic_weight_width              1000      weight associated with width evidence in combined cost of state
heuristic_weight_seamcut         0             weight associated with seam cut in combined cost of state
heuristic_max_char_wh_ratio  2             max char width-to-height ratio allowed in segmentation
segsearch_max_fixed_pitch_char_wh_ratio    2             Maximum character width-to-height ratio for fixed-pitch fonts
tosp_old_sp_kn_th_factor        2             Factor for defining space threshold in terms of space and kern sizes
tosp_threshold_bias1   0             how far between kern and space?
tosp_threshold_bias2   0             how far between kern and space?
tosp_narrow_fraction  0.3          Fract of xheight for narrow
tosp_narrow_aspect_ratio        0.48       narrow if w/h less than this
tosp_wide_fraction       0.52       Fract of xheight for wide
tosp_wide_aspect_ratio             0             wide if w/h less than this
tosp_fuzzy_space_factor           0.6          Fract of xheight for fuzz sp
tosp_fuzzy_space_factor1         0.5          Fract of xheight for fuzz sp
tosp_fuzzy_space_factor2         0.72       Fract of xheight for fuzz sp
tosp_gap_factor             0.83       gap ratio to flip sp->kern
tosp_kern_gap_factor1               2             gap ratio to flip kern->sp
tosp_kern_gap_factor2               1.3          gap ratio to flip kern->sp
tosp_kern_gap_factor3               2.5          gap ratio to flip kern->sp
tosp_ignore_big_gaps  -1            xht multiplier
tosp_ignore_very_big_gaps      3.5          xht multiplier
tosp_rep_space              1.6          rep gap multiplier for space
tosp_enough_small_gaps          0.65       Fract of kerns reqd for isolated row stats
tosp_table_kn_sp_ratio              2.25       Min difference of kn & sp in table
tosp_table_xht_sp_ratio            0.33       Expect spaces bigger than this
tosp_table_fuzzy_kn_sp_ratio                3             Fuzzy if less than this
tosp_fuzzy_kn_fraction              0.5          New fuzzy kn alg
tosp_fuzzy_sp_fraction               0.5          New fuzzy sp alg
tosp_min_sane_kn_sp                1.5          Don't trust spaces less than this time kn
tosp_init_guess_kn_mult           2.2          Thresh guess - mult kn by this
tosp_init_guess_xht_mult         0.28       Thresh guess - mult xht by this
tosp_max_sane_kn_thresh      5             Multiplier on kn to limit thresh
tosp_flip_caution           0             Don't autoflip kn to sp when large separation
tosp_large_kerning       0.19       Limit use of xht gap with large kns
tosp_dont_fool_with_small_kerns        -1            Limit use of xht gap with odd small kns
tosp_near_lh_edge       0             Don't reduce box if the top left is non blank
tosp_silly_kn_sp_gap   0.2          Don't let sp minus kn get too small
tosp_pass_wide_fuzz_sp_to_context 0.75       How wide fuzzies need context
textord_blob_size_bigile            95           Percentile for large blobs
textord_noise_area_ratio          0.7          Fraction of bounding box for noise
textord_blob_size_smallile        20           Percentile for small blobs
textord_initialx_ile         0.75       Ile of sizes for xheight guess
textord_initialasc_ile     0.9          Ile of sizes for xheight guess
textord_noise_sizelimit               0.5          Fraction of x for big t count
textord_noise_normratio           2             Dot to norm ratio for deletion
textord_noise_syfract  0.2          xh fract height error for norm blobs
textord_noise_sxfract  0.4          xh fract width error for norm blobs
textord_noise_hfract   0.015625              Height fraction to discard outlines as speckle noise
textord_noise_rowratio              6             Dot to norm ratio for deletion
textord_blshift_maxshift            0             Max baseline shift
textord_blshift_xfraction           9.99       Min size of baseline shift
*/
