package featureExtraction;

import cbrTekStraktorModel.cmcProcSettings;
import generalImagePurpose.cmcBulkImageRoutines;
import generalImagePurpose.cmcImageRoutines;
import generalpurpose.gpPrintStream;
import logger.logLiason;

public class cmcHOGRoutines {
	
	static boolean DEBUG = false;
	
	cmcProcSettings xMSet=null;
	logLiason logger=null;
	cmcBulkImageRoutines ibulk = null;
	
	
	private static int CellWidth = 8;
	private static int BinRange  = 180;   // 360 degrees are to be reduced to 180
	private static int NumberOfCellBins = 9;  // uneven
	private static int MARKER  = 0x0000ff00;  // green
	
	private double MaxMagnitude =  Math.sqrt((double)((255 * 2) * (255 * 2)));
	
	class BinBoundary
	{
		double min;
		double max;
		BinBoundary(double mi, double ma)
		{
			min = mi;
			max = ma;
		}
	}
	
	class CellHistogram
	{
		int x=-1;
		int y=-1;
	    double[] histo=null;
	    CellHistogram(int ix, int iy)
	    {
	    	x=ix;
	    	y=iy;
	    	histo = new double[NumberOfCellBins];
	    	for(int i=0;i<histo.length;i++) histo[i]=0;
	    }
	}
	
	class BoxHistogram
	{
	  int x=-1;
	  int y=-1;
	  CellHistogram[] cells = null;
	  double[] vectors = null;
	  double normalizer=0;
	  BoxHistogram(int ix, int iy)
	  {
		  x=ix;
		  y=iy;
		  cells = new CellHistogram[4];
		  vectors = new double[ cells.length * NumberOfCellBins];
	  }
	}
	
	BinBoundary[] ar_binbou = null;
	CellHistogram[] ar_cells = null;
    BoxHistogram[] ar_boxes = null;
    
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
    public cmcHOGRoutines(cmcProcSettings is,logLiason ilog)
    //------------------------------------------------------------
    {
    	xMSet = is;
		logger=ilog;
		ibulk = new cmcBulkImageRoutines(xMSet,logger);
		make_binboundaries();
    }
    
    //------------------------------------------------------------
    private void make_binboundaries()
    //------------------------------------------------------------
    {
     try {
    	// number of bins must be uneven
    	int imid = NumberOfCellBins / 2;
    	double dmid = (double)NumberOfCellBins / (double)2;
    	if( (dmid - (double)imid) == 0 ) {
    		do_error("Number of bins must be an uneven number [" + NumberOfCellBins + "]" );
    		return;
    	}
    	// bin width must be a whole number (no fraction)
    	double dbwidth = (double)BinRange / (double)NumberOfCellBins;
    	int ibwidth = (int)dbwidth;
    	if( (double)ibwidth != dbwidth  ) {
    		do_error( "Width of a single bin must be a whole number [" + dbwidth + "]");
    		return;
    	}
    	//
    	ar_binbou = new BinBoundary[ NumberOfCellBins ];
    	// include exteme left and right boundary; the right will have 1 degree more
    	// so take the bin in the middle and a 1 degree in the middle
    	int start = 0;
    	int stop  = 0;
    	int mididx = (NumberOfCellBins / 2);  // floor N/2
    	for(int i=0;i<NumberOfCellBins;i++)
    	{
    		stop = start + ibwidth - 1;
    		//if( i == mididx ) stop++;
    		BinBoundary x = new BinBoundary( (double)start , (double)stop );
    		ar_binbou[ i ] = x;
    		start = stop + 1;
    	}
    	// check
    	if( ar_binbou[ NumberOfCellBins-1 ].max != (double)(BinRange-1) ) {
    		do_error( "Right extremity [" + ar_binbou[ NumberOfCellBins-1].max + "] differs from range [" + BinRange + "]");
    		ar_binbou=null;	
    	}
    	String sout="";
    	for(int i=0 ; i<ar_binbou.length;i++)
    	{
    		sout += "[" + ar_binbou[i].min + "," + ar_binbou[i].max + "]";
    	}
    	do_log(9 , "Bin Boundaries " + sout);
     }
     catch(Exception e ) {
    	e.printStackTrace();
    	ar_binbou=null;
     }
    }
    
  //------------------------------------------------------------
    public boolean makeHOGDescriptor(int[]image , int imagewidth , String ImageFileName , String TargetDir )
  //------------------------------------------------------------
    {
    	// create the 9 bins for the 8x8 pixel cells
    	if( makeHOGCells ( image , imagewidth , ImageFileName) == false ) return false;
    	if( DEBUG ) dumpCells(ImageFileName);
    	
    	// create boxes and slide boxes across picture
    	if( makeBoxes( imagewidth , image.length / imagewidth) == false ) {
    		do_error("Could not make boxes for [" + ImageFileName + "] [Width=" + imagewidth + "]");
    		return false;
    	}
    	if( calculateVector() == false ) {
    		do_error("Could not calculate vector for [" + ImageFileName + "] [Width=" + imagewidth + "]");
    		return false;
    	}
    	if( DumpHOGDescriptor( ImageFileName , imagewidth , image.length / imagewidth , TargetDir ) == false ) {
    		do_error("Could not dump vector for [" + ImageFileName + "] [Target=" + TargetDir + "]");
    		return false;
    	}
    	return true;
    }
    
    //------------------------------------------------------------
    public boolean makeHOGCells(int[]image , int imagewidth , String ImageFileName)
    //------------------------------------------------------------
    {
    	if( ar_binbou == null ) {
    		do_error("No bin boundaries defined");
    		return false;
    	}
    	int imageHeigth = image.length / imagewidth;
    	int[] hogdx = ibulk.HOGGradientDxDy( image , imagewidth );
    	if( hogdx == null ) return false;
    	int[] hogpolair = ibulk.TransformHOGGradientDxDyToMagnitudeTheta(hogdx);
    	
    	// 8 by 8 cells  histogram
    	int n_horz_cells = imagewidth / CellWidth;
    	int n_vert_cells = imageHeigth / CellWidth;
    	ar_cells = new CellHistogram[ n_horz_cells * n_vert_cells];
    	int[] polars = new int[ CellWidth * CellWidth];
    	int cellcounter=0;
    	for(int i=0;i<n_vert_cells;i++)
    	{
    		int y = i * CellWidth;
    		for(int j=0;j<n_horz_cells;j++)
    		{
    			int x = j * CellWidth;
    			int k = 0;
    			for(int h=0;h<CellWidth;h++)
    			{
    				 int idx = ((y + h) * imagewidth ) + x;
    			     for(int w=0;w<CellWidth;w++)
    			     {
    			    	 polars[k] = hogpolair[ idx ];
    			    	 // DEBUG debug hogdx should be green
    			    	 if( DEBUG ) image[ idx ] = MARKER;
    			    	 //
    			    	 idx++;
    			    	 k++;
    			     }
    			}
    			if( k != polars.length ) {
    				do_error("Number of binner entries incorrect [" + k + "] expect [" + polars.length + "]");
    				return false;
    			}
    			// verwerk de cell
    			// Tansform the polars in the binner array into 9 bins
    			CellHistogram ch = new CellHistogram(x,y);
    			ch.histo = TransformPolarsToBins( polars );
    			if( ch.histo == null ) return false;
    			//
    			ar_cells[cellcounter] = ch;
    			cellcounter++;
    		}
    	}
    	//
    	if( cellcounter != ar_cells.length ) {
    		do_error("System error - not all cells are populated [" + cellcounter +"] expecting [" +ar_cells.length + "]");
    		return false;
    	}
    	//
    	if( DEBUG ) {
    	 // visual check to see whether all polars have been used => entirely green picture
    	 cmcImageRoutines irout = new cmcImageRoutines(logger);
         irout.writePixelsToFile( image , imagewidth , image.length / imagewidth , ImageFileName+"Hog.jpg" , cmcImageRoutines.ImageType.RGB );
    	}
    	//
    	return true;
    }

    //------------------------------------------------------------
    private void dumpCells(String ImageFileName)
    //------------------------------------------------------------
    {
    	String ShortFileName = getShortNameWithoutSuffix( ImageFileName );
    	if( ShortFileName == null ) return;
    	String DumpFileName = xMSet.getCorpusDir() + xMSet.xU.ctSlash + "Images" + xMSet.xU.ctSlash + ShortFileName + "_Cell_Dump.txt";
    	gpPrintStream print = new gpPrintStream( DumpFileName , "ASCII");
    	// run a few checks
    	for(int i=0;i<ar_cells.length;i++)
    	{
    		CellHistogram c = ar_cells[i];
    		String sout = "[" + c.x + "," + c.y + "] ";
    		double tot=0;
    		for(int j=0;j<c.histo.length;j++) {
    			sout += "(" + c.histo[j] + ")";
    			tot += c.histo[j];
    		}
    		print.println( sout + " Sum=" + tot);
    	}
    	print.close();
    	do_error("DUMPED CELLS[" + DumpFileName + "");
    }
    
    //------------------------------------------------------------
    private int getBinIdx(double d)
    //------------------------------------------------------------
    {
      for(int i=0;i<ar_binbou.length;i++)
      {
    	  if( (d>= ar_binbou[i].min) && ( d<= ar_binbou[i].max )) return i;
      }
      do_error("Cannot find bin for theta [" + d + "]");
      return -1;
    }
    
    //------------------------------------------------------------
    private double getProportionLeft( double theta , int binidx)
    //------------------------------------------------------------
    {
    	try {
    		double dd = (theta - ar_binbou[binidx].min) / ((double)BinRange / (double)NumberOfCellBins);
            if( dd < 0 ) {
            	do_error("System error - getProportionLeft [" + theta + " " + ar_binbou[binidx].min + "] theta is not on the right");
        		return -1;
            }
            if( dd > 1 ) {
            	do_error("System error - getProportionLeft [" + theta + " " + ar_binbou[binidx].min + "] portion[" + dd + "] cannot be larger than 1");
        		return -1;	
            }
            return 1 - dd;
    	}
    	catch(Exception e ) {
    		do_error("System error - getProportionLeft");
    		return -1;
    	}
    }
    
    //------------------------------------------------------------
    private double[] TransformPolarsToBins( int[] polars)
    //------------------------------------------------------------
    {
    	double[] bins = new double[NumberOfCellBins];
    	for(int i=0;i<bins.length;i++) bins[i]=0;
    	//  bin number is Theta reduced to 0 -> 180,  so ABS theta - 180 degrees  (?? why : to narroow bins and have a better distributon of vlaues ocross bins
    	//  value is sum of magnitudes - magnitudes are reduced/normalized to 0..1 by  / MaxMagnitude
    	// distribute proportional 
    	for(int i=0;i<polars.length;i++)
    	{
          int magnitude = ((polars[i] & 0xffff0000) >> 16 );
          double  dmag = magnitude / MaxMagnitude;
    	  int theta = (polars[i] & 0xffff) % 360;  // UNSIGNED 0 .. 259 degrees  but needs to be transformed in 0 -> 180 - thin radials and a circle
    	  if( theta >= 180 ) {
    		  theta = theta - 180;   //  190 degrees is the same as 10 degrees (make it SIGNED); also 180 is 0
    		  //if( theta >= 180 ) theta = theta - 180;  // could have been 360
    	  }
    	  int binidx = getBinIdx( (double)theta );
    	  if( binidx < 0 ) return null;
    	  // percentual distance to left bin
    	  double dleft = getProportionLeft( (double)theta , binidx );
    	  if( dleft < 0) return null;
    	  // distribute
    	  double portionleft = dleft * dmag;
    	  double portionright = dmag - portionleft;
  //if( binidx != 2)  do_log( 9 , "" + portionleft + " " + portionright + " " + binidx + " " + ar_binbou[binidx].min + "]");
    	  bins[ binidx ] += portionleft;
    	  binidx++;
    	  if( binidx >= ar_binbou.length ) binidx=0;
    	  bins[ binidx ] += portionright;
    	}
    	return bins;
    }
    
    //------------------------------------------------------------
    private boolean makeBoxes(int imagewidth , int imageheigth)
    //------------------------------------------------------------
    {
    	// 1 box = 2 by 2 cells 
    	int n_horz_boxes = (imagewidth / CellWidth) - 1;
    	int n_vert_boxes = (imageheigth / CellWidth) - 1;
    	ar_boxes = new BoxHistogram[ n_horz_boxes * n_vert_boxes];
    	int boxCounter= 0;
    	
    	int stride = CellWidth;
    	for(int i=0;i<n_vert_boxes;i++)
    	{
    		int y = i * stride;
    		for(int j=0;j<n_horz_boxes;j++)
    		{
    			int x = j * stride;
    			BoxHistogram bx = new BoxHistogram( x , y);
    			bx.cells[0] = getCell( x , y);
    			bx.cells[1] = getCell( x + stride , y);
    			bx.cells[2] = getCell( x , y + stride);
    			bx.cells[3] = getCell( x + stride , y + stride );
    			for(int k=0;k<bx.cells.length;k++) if( bx.cells[k] == null) return false;
    			ar_boxes[boxCounter] = bx;
    			boxCounter++;
    		}
    	}
    	if( boxCounter != ar_boxes.length ) {
    		do_error("System error - not all boxes populated [" + boxCounter + "] expected [" + ar_boxes.length + "]");
    		return false;
    	}
    	
    	return true;
    }
    
    //------------------------------------------------------------
    private CellHistogram getCell(int ix , int iy)
    //------------------------------------------------------------
    {
    	if( ar_cells == null ) return null;
    	for(int i=0;i<ar_cells.length;i++)
    	{
    		if( ar_cells[i].x != ix ) continue;
    		if( ar_cells[i].y != iy ) continue;
    		return ar_cells[i];
    	}
    	do_error("(getCell) Could not find cell [" + ix + "," + iy + "] in [" + ar_cells.length + "]");
    	return null;
    }
    
    //------------------------------------------------------------
    private String getShortNameWithoutSuffix(String LongFileName)
    //------------------------------------------------------------
    {
    	try {
    	 String ShortFileName = xMSet.xU.GetFileName(LongFileName);
    	 int idx = ShortFileName.lastIndexOf(".");
    	 if( idx < 0 ) {
    		do_error("Cannot remove suffic from [" + ShortFileName + "]");
    		return null;
    	 }
    	return ShortFileName.substring(0,idx);
    	}
    	catch(Exception e ) { return null; }
    }

    //------------------------------------------------------------
    private boolean calculateVector()
    //------------------------------------------------------------
    {
    	
        for(int i=0;i<ar_boxes.length;i++)
        {
        	BoxHistogram box = ar_boxes[i];
        	int p=0;
        	for(int j=0;j<box.vectors.length;j++) box.vectors[j]=0;
        	double norm = 0;
        	for(int j=0;j<box.cells.length;j++) 
        	{
        		CellHistogram cel = box.cells[j];
        		for(int k=0;k<cel.histo.length;k++)
        		{
        		  box.vectors[p]= cel.histo[k];
        		  norm += cel.histo[k] * cel.histo[k];
        		  p++;
        		}
        	}
        	if( p != box.vectors.length ) {
        		do_error("System error - number of vectors is [" + p + "] expecting [" + box.cells.length + "]");
        	}
        	// normalize
        	if( norm  <= 0 ) norm = 1;
        	box.normalizer = Math.sqrt( norm );
            for(int j=0;j<box.vectors.length;j++) box.vectors[j] = box.vectors[j] / box.normalizer;        	
        }
    	
    	return true;
    }
    
    //------------------------------------------------------------
    private boolean DumpHOGDescriptor(String ImageFileName , int imagewidth , int  imageheigth , String TargetDir)
    //------------------------------------------------------------
    {
    	String ShortFileName = getShortNameWithoutSuffix( ImageFileName );
    	if( ShortFileName == null ) return false;
    	String HOGFileName = TargetDir + xMSet.xU.ctSlash + ShortFileName + "_HOG.xml";
        //
    	gpPrintStream print = new gpPrintStream( HOGFileName , xMSet.getCodePageString());
    	print.println (xMSet.getXMLEncodingHeaderLine());
		print.println ("<!-- Application : " + xMSet.getApplicDesc() + " -->");
		print.println ("<!-- File Created: " + (xMSet.xU.prntStandardDateTime(System.currentTimeMillis())).toUpperCase() + " -->" );
        //	
    	print.println("<HOGDescriptor>" );
    	print.println("<ImageFileName>" + ImageFileName + "</ImageFileName>");
    	print.println("<ImageWidth>" + imagewidth + "</ImageWidth>" );
    	print.println("<ImageHeigth>" + imageheigth + "</ImageHeigth>" );
    	print.println("<CellWidth>" + CellWidth + "</CellWidth>" );
    	print.println("<NumberOfVectors>" +  ar_boxes.length + "</NumberOfVectors>");
        //	
        for(int i=0;i<ar_boxes.length;i++)
        {
        	BoxHistogram box = ar_boxes[i];
        	print.println("<Box>");
        	print.println("<Coord><![CDATA[" + box.x + "," + box.y + "]]></Coord>");
        	String sLine = "";
        	for(int j=0;j<box.cells.length;j++) sLine += "[" + box.cells[j].x  + "," + box.cells[j].y + "]"; 
            print.println( "<Cells><![CDATA[" + sLine + "]]>" );
            //
            if( DEBUG ) {
            	for(int j=0;j<box.cells.length;j++)
            	{
                	CellHistogram cel = box.cells[j];
                	sLine = "";
                    for(int k=0;k<cel.histo.length;k++) sLine += "[" + cel.histo[k] + "]";	
                    print.println("<CellHisto><![CDATA[" + sLine + "]]></CellHisto>");
            	}
            }
            //
            print.println("</Cells>");
            //
            sLine = "";
            for(int j=0;j< box.vectors.length;j++) sLine += "[" +box.vectors[j] + "]";
            print.println("<Vector><VecNo>" + i + "</VecNo><Normalizer>" + box.normalizer + "</Normalizer>");
            print.println("<![CDATA[" + sLine + "]]></Vector>" );
            //
            print.println("</Box>");
        }
    	//
    	print.println("</HOGDescriptor>" );
    	print.close();
     	do_log( 9 , "Created HOG descriptor [" + HOGFileName + "]");
    	return true;
    }
    
}
