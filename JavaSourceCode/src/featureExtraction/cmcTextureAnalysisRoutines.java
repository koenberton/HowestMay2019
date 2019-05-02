package featureExtraction;

import java.util.ArrayList;

import cbrTekStraktorModel.cmcProcSettings;
import generalImagePurpose.cmcBulkImageRoutines;
import generalImagePurpose.cmcGLCMRoutines;
import generalImagePurpose.cmcImageRoutines;
import logger.logLiason;

public class cmcTextureAnalysisRoutines {

	static boolean DEBUG = false;
		
	
	static int BIT_DEPTH = 4;   //
	static int GRAY_BINS = (int)Math.pow( 2 , (double)BIT_DEPTH );
	
	cmcProcSettings xMSet=null;
	logLiason logger=null;
	cmcBulkImageRoutines ibulk = null;
	cmcImageRoutines irout = null;
	
	ArrayList<cmcHaralick> list = null;
	
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
    public cmcTextureAnalysisRoutines(cmcProcSettings is,logLiason ilog)
    //------------------------------------------------------------
    {
    	xMSet = is;
		logger=ilog;
		ibulk = new cmcBulkImageRoutines(xMSet,logger);
		irout = new cmcImageRoutines(logger);
    }
    
    //------------------------------------------------------------
    private void showGLCM( double[][] mtrx )
    //------------------------------------------------------------
	{
		for(int i=0; i<mtrx.length;i++)
		{
			String sline = "% ";
			for(int j=0;j<mtrx[i].length;j++)
			{
				sline += "[" + String.format( "%5.2f" , (mtrx[i][j])*100) + "]";
			}
			do_log( 5 , sline );
		}
	}
    
    //-----------------------------------------------------------------------
  	private int[] doRedscale(int[] pixels )
  	//-----------------------------------------------------------------------
  	{
  			int[] work = new int[ pixels.length ];
  		    for(int i=0;i<pixels.length;i++) 
  	        { 
  		    	 int p = pixels[i]; 
                 int r = 0xff & (p >> 16);   
                 work[i] = (255 << 24) | (r << 16) | (r << 8) | r; 
  	       } 
  		   return work;
  	}
  	
  	//-----------------------------------------------------------------------
  	private int[] doGreenscale(int[] pixels )
  	//-----------------------------------------------------------------------
  	{
  				int[] work = new int[ pixels.length ];
  			    for(int i=0;i<pixels.length;i++) 
  		        { 
  			    	 int p = pixels[i]; 
  	                 int g = 0xff & (p >> 8); 
  	                 work[i] = (255 << 24) | (g << 16) | (g << 8) | g; 
  		       } 
  			   return work;
  	}
  	
    //-----------------------------------------------------------------------
  	private int[] doBluescale(int[] pixels )
  	//-----------------------------------------------------------------------
  	{
  				int[] work = new int[ pixels.length ];
  			    for(int i=0;i<pixels.length;i++) 
  		        { 
  			    	 int p = pixels[i]; 
  	                 int b = 0xff & p;       
  		             work[i] = (255 << 24) | (b << 16) | (b << 8) | b; 
  		       } 
  			   return work;
  	}
  	
  	//-----------------------------------------------------------------------
  	public double extractDensity( int[] image , int width )
  	//-----------------------------------------------------------------------
  	{
  		int[] niblak = ibulk.doNiblak( image , width , true );
  		int black=0;
  		for(int i=0;i<image.length;i++)
  		{
  			if( (niblak[i] & 0x00ffffff) ==  0) black++;
  			else
			if( (niblak[i] & 0x00ffffff) !=  0x00ffffff) {
				do_error("NIBLAK is not Black nor white [" + (niblak[i] & 0x00ffffff) + "]");
				return -1;
			}
		}
  		return (double)black / (double)image.length;
  	}
  	
    //-----------------------------------------------------------------------
  	public double[] extractMeanHSL( int[] image )
  	//-----------------------------------------------------------------------
  	{
  		float[] ret=null;
  		double[] hslMean = new double[3];
  		for(int i=0;i<3;i++) hslMean[i]=0;
  		for(int i=0;i<image.length;i++)
  		{
  	       ret = irout.getHSB( image[i] );
  	       hslMean[0] += (double)ret[0];
  	       hslMean[1] += (double)ret[1];
  	       hslMean[2] += (double)ret[2];
	    }
  		for(int i=0;i<3;i++) hslMean[i] = hslMean[i] / (double)image.length;
  		return hslMean;
  	}

  	//------------------------------------------------------------
  	public cmcFeatureCollection.ORIENTATION getOrientation( int width , int height)
  	//------------------------------------------------------------
  	{
  		try {
  	      double aspectratio = (double)width / (double)height;   //   W / H
  	      if( aspectratio >= 1 ) {   // breder dan hoger
  	    	if( aspectratio < (double)1.05 ) return cmcFeatureCollection.ORIENTATION.SQUARE;
  	    	if( aspectratio < (double)2 ) return cmcFeatureCollection.ORIENTATION.LANDSCAPE;
  	   	    if( aspectratio < (double)4 ) return cmcFeatureCollection.ORIENTATION.WIDE;
  	   	    return cmcFeatureCollection.ORIENTATION.ULTRAWIDE;
  	      }
  	      aspectratio = (double)height / (double)width;
  	  	  if( aspectratio < (double)1.05 ) return cmcFeatureCollection.ORIENTATION.SQUARE;
	   	  if( aspectratio < (double)2 ) return cmcFeatureCollection.ORIENTATION.PORTRAIT;
	      if( aspectratio < (double)4 ) return cmcFeatureCollection.ORIENTATION.NARROW;
	      return cmcFeatureCollection.ORIENTATION.ULTRANARROW;
  		}
  		catch( Exception e ) {
  			return cmcFeatureCollection.ORIENTATION.UNKNOWN;
  		}
  	}
  	
  	//------------------------------------------------------------
    public ArrayList<cmcHaralick> extractHaralickFeatures(int[]oriimage , int imagewidth , String ImageFileName  )
    //------------------------------------------------------------
    {
    	// Preprocess : apply gaussian (blur)
    	// tested and there seems to be an adverse effect
    	//int[] image = ibulk.doConvolution( oriimage , generalImagePurpose.cmcProcConvolution.kernelType.GAUSSIAN33 , imagewidth , oriimage.length / imagewidth);
    	int[] image = oriimage;
    	//
    	list = new ArrayList<cmcHaralick>();
        int[] channel = null;
        boolean ib = false;
        for(int i=0;i<4;i++)
    	{
           cmcHaralick.CHANNEL channeltipe = cmcHaralick.CHANNEL.UNKNOWN;
    	   switch( i )
    	   {
    	    case 0  : { channel = doRedscale(image); channeltipe = cmcHaralick.CHANNEL.RED ; break; }
    	    case 1  : { channel = doGreenscale(image); channeltipe = cmcHaralick.CHANNEL.GREEN ; break; }
    	    case 2  : { channel = doBluescale(image); channeltipe = cmcHaralick.CHANNEL.BLUE; break; }
    	    case 3  : { channel = ibulk.doGrayscale(image);	channeltipe = cmcHaralick.CHANNEL.GRAYSCALE ; break; }
    	    default : return null;
    	   }
 //do_error( "" + ImageFileName + " " + channeltipe + " " + (channel[10] & 0x00ffffff) );
    	   ib = extractHaralickFeaturesChannel( channel , imagewidth , ImageFileName  , channeltipe );	   
    	   if( ib == false ) break;
    	   
    	}
    	channel=null;
    	if( ib == false ) return null;
    	// log
    	//for(int i=0;i<list.size();i++) do_log(1 , list.get(i).format() );
    	return list;
    }
    
    //------------------------------------------------------------
    public boolean cleanUpTrainingDebugFiles(String FolderName)
    //------------------------------------------------------------
    {
    	if( DEBUG ) return true;
    	ArrayList<String> list = null;
        list = xMSet.xU.GetFilesInDir( FolderName , null);
        for(int i=0;i<list.size();i++)
        {
        	   String FName = list.get(i);
        	   if( FName.toUpperCase().indexOf("-REDUX.") < 0 ) continue;
        	   FName = FolderName + xMSet.xU.ctSlash + FName;
        	   do_log( 1 , "Removing [" + FName + "]");
        	   xMSet.xU.VerwijderBestand( FName );
        }
    	return true;
    }
    
    //------------------------------------------------------------
    private boolean extractHaralickFeaturesChannel(int[]image , int imagewidth , String ImageFileName  , cmcHaralick.CHANNEL channeltipe)
    //------------------------------------------------------------
    {
    	// Quantize the 8 bits depth to 4 bits
    	int[] redux = ibulk.doMaxLloyd( image , GRAY_BINS );
    	int[] centers = ibulk.getGrayScaleCenters();
    	if( (redux == null) || (centers == null) ) {
    		do_error("Could not perform Max Lloyd quantization");
    		return false;
    	}
    	if( redux.length != image.length ) {
     		do_error("Quantized image does not have the same number of pixels [" + image.length + "]");
    		return false;
    	}
    	if( centers.length != GRAY_BINS ) {
     		do_error("Quantized image does not have the anticipated number of bins [" + GRAY_BINS + "] got [" + centers.length + "]");
    		return false;
    	}
   	    // visual check 
        if( DEBUG ) {
       	 cmcImageRoutines irout = new cmcImageRoutines(logger);
         irout.writePixelsToFile( redux , imagewidth , image.length / imagewidth , ImageFileName + "-" + channeltipe + "-REDUX.jpg" , cmcImageRoutines.ImageType.RGB );
         irout = null;
         do_log( 1 , "DEBUG => " + ImageFileName );
       	}
        //
    	// now transform the grayscales to 0..level
    	for(int i=0;i<redux.length;i++)
    	{
    		int idx=-1;
    		int p = redux[i] & 0xff;
    		for(int j=0;j<centers.length;j++)
    		{
    			if( centers[j] == p ) { idx =j ; break; }
    		}
    		if( idx < 0 ) {
    			do_error("Reduced image has a pixel value that does not feature on the array of reduced values [" + p + "]");
    			return false;
    		}
    		int lum = idx & 0xff;
    		redux[i] = (255 << 24) | (lum << 16) | (lum << 8) | lum; 	
    	}
    	 // visual check 
    	/*
    	if( DEBUG ) {
          	cmcImageRoutines irout = new cmcImageRoutines(logger);
            irout.writePixelsToFile( redux , imagewidth , image.length / imagewidth , ImageFileName+"GLCM.jpg" , cmcImageRoutines.ImageType.RGB );
            irout = null;
            do_error( "DEBUG => " + ImageFileName );
        }
    	*/  	
    	// GLCM
    	cmcGLCMRoutines glcm = new cmcGLCMRoutines();
    	boolean ib = glcm.makeGLCM( redux , imagewidth , GRAY_BINS );
    	if( ib == false ) {
    		do_error("Could not derive GLCM matrix [" + ImageFileName + "]");
    		return false;
    	}
    	double[][] verticalGLCM   = glcm.getVerticalGLCM();
    	double[][] horizontalGLCM = glcm.getHorizontalGLCM();
    	if( (verticalGLCM == null) || (horizontalGLCM == null) ) {
    		do_error("Could not fetch GLCM matrix" );
    		return false;
    	}
    	
    	cmcHaralick horizontal = new cmcHaralick( cmcHaralick.ORIENTATION.HORIZONTAL , channeltipe , horizontalGLCM );
    	horizontal.maximumProbability = calcMaxProbability( horizontalGLCM );
    	horizontal.contrast           = calcContrast( horizontalGLCM );
    	horizontal.dissimilarity      = calcDissimilarity( horizontalGLCM );
    	horizontal.inverseDifference  = calcInverseDifference( horizontalGLCM );
    	horizontal.homegenity         = calcHomogenity( horizontalGLCM );
    	horizontal.energy             = calcEnergy( horizontalGLCM );
    	horizontal.entropy            = calcEntropy( horizontalGLCM );
    	horizontal.autoCorrelation    = calcAutoCorrelation( horizontalGLCM );
    	horizontal.correlation        = calcCorrelation( horizontalGLCM );
    	horizontal.cbrTekCorrelation  = calcCbrTekCorrelation( horizontalGLCM );
    
    	//
    	cmcHaralick vertical = new cmcHaralick( cmcHaralick.ORIENTATION.VERTICAL , channeltipe , verticalGLCM);
    	vertical.maximumProbability = calcMaxProbability( verticalGLCM );
    	vertical.contrast           = calcContrast( verticalGLCM );
    	vertical.dissimilarity      = calcDissimilarity( verticalGLCM );
    	vertical.inverseDifference  = calcInverseDifference( verticalGLCM );
    	vertical.homegenity         = calcHomogenity( verticalGLCM );
    	vertical.energy             = calcEnergy( verticalGLCM );
    	vertical.entropy            = calcEntropy( verticalGLCM );
    	vertical.autoCorrelation    = calcAutoCorrelation( verticalGLCM );
    	vertical.correlation        = calcCorrelation( verticalGLCM );
    	vertical.cbrTekCorrelation  = calcCbrTekCorrelation( verticalGLCM );
        //
        list.add( horizontal );
        list.add( vertical );
        //
    	redux=null;
    	return true;
    }
    
  
    //-----------------------------------------------------------------------
    //-----------------------------------------------------------------------
    private double calcMaxProbability( double[][] gc)
    {
    	double max=0;
    	for(int i=0;i<gc.length;i++)
    	{
    		for(int j=0;j<gc[i].length;j++)
    		{
    			if( gc[i][j] > max ) max = gc[i][j];
    		}
    	}
    	return max;
    }
    
    private double calcContrast( double[][] gc)
    {
    	double contrast = 0;
    	for(int i=0;i<gc.length;i++)
    	{
    		for(int j=0;j<gc[i].length;j++)
    		{
    			contrast += gc[i][j] * (double)((i - j) * ( i - j ));
    		}
    	}
    	return contrast;
    }
    
    private double calcDissimilarity( double[][] gc)
    {
    	double dissim = 0;
    	for(int i=0;i<gc.length;i++)
    	{
    		for(int j=0;j<gc[i].length;j++)
    		{
    			dissim += gc[i][j] * Math.abs((double)(i - j));
    		}
    	}
    	return dissim;
    }
   
    private double calcInverseDifference( double[][] gc)
    {
    	double homog = 0;
    	for(int i=0;i<gc.length;i++)
    	{
    		for(int j=0;j<gc[i].length;j++)
    		{
    			homog += gc[i][j] / (double)( (double)1 + Math.abs( (double)(i - j)) );    // DIV   1 + ABS  = inverse differenfe
    		}
    	}
    	return homog;
    }

    private double calcAutoCorrelation( double[][] gc)
    {
    	double auto = 0;
    	for(int i=0;i<gc.length;i++)
    	{
    		for(int j=0;j<gc[i].length;j++)
    		{
    			auto += gc[i][j] * i * j;
    		}
    	}
    	return auto;
    }
    
    private double calcHomogenity( double[][] gc)
    {
    	double homog = 0;
    	for(int i=0;i<gc.length;i++)
    	{
    		for(int j=0;j<gc[i].length;j++)
    		{
    			homog += gc[i][j] / (double)( 1 + (double)((i - j) * ( i - j )));        // DIV   1 + KWADRAAT  = HOMO
    		}
    	}
    	return homog;
    }
    
    private double calcEnergy( double[][] gc)
    {
    	double ASM = 0;
    	for(int i=0;i<gc.length;i++)
    	{
    		for(int j=0;j<gc[i].length;j++)
    		{
    			ASM += gc[i][j] * gc[i][j];
    		}
    	}
    	double energy = Math.sqrt( ASM );
    	return energy;
    }
    
    private double calcEntropy( double[][] gc)
    {
    	double ASM = 0;
    	for(int i=0;i<gc.length;i++)
    	{
    		for(int j=0;j<gc[i].length;j++)
    		{
    			double p = gc[i][j];
    			double ln =  p == 0 ? (Double.MIN_VALUE / (gc.length * gc[i].length )) : Math.log( p );
    			if( Double.isNaN( ln ) ) {
    				do_log( 1 , "LN error [" + p + "]");
    			}
    			if( Double.isInfinite( ln ) ) {
    				do_log( 1 , "Infinite result on LN(" + p + ")");
    			}
    			ASM -= p * ln;
    			if( Double.isNaN( ASM ) ) {
    				do_log( 1 , "Calc error [" + p + "][" + ln + "]");
    			}
    		}
    	}
    	return ASM;
    }
    
    private double calcCbrTekCorrelation( double[][] gc)
    {
    	// mu I =   sum  ( i * P(i,j)
    	double[] meansi = new double[ gc.length ];
    	for(int i=0;i<meansi.length ;i++) meansi[i]=0;
        for(int i=0;i<gc.length ;i++)
    	{
    		for(int j=0;j<gc[i].length;j++)
    		{
    			//meansi[i] += i * gc[i][j];
    			meansi[i] += gc[i][j];
    		}
    	}
    	// mu J =   sum  ( j * P(i,j)
        double[] meansj = new double[ gc[0].length ];
    	for(int i=0;i<meansj.length ;i++) meansj[i]=0;
        for(int i=0;i<gc.length ;i++)
    	{
    		for(int j=0;j<gc[i].length;j++)
    		{
    			//meansj[j] += j * gc[i][j];
    			meansj[j] += gc[i][j];
       	}
    	}
    	// std i   pij * ( i - mui)
        double[] stdi = new double[ gc.length ];
        for(int i=0;i<stdi.length ;i++) stdi[i]=0;
        for(int i=0;i<gc.length ;i++)
    	{
    		for(int j=0;j<gc[i].length;j++)
    		{
    			//stdi[i] +=   gc[i][j] * (double)((i - meansi[i])*(i - meansi[i]));
    			stdi[i] +=   (gc[i][j] - meansi[i]) * (gc[i][j] - meansi[i]);
    		}
    	}
        for(int i=0;i<stdi.length ;i++) stdi[i]=Math.sqrt( stdi[i] );
        // std j
        double[] stdj = new double[ gc[0].length ];
        for(int i=0;i<stdj.length ;i++) stdj[i]=0;
        for(int i=0;i<gc.length ;i++)
    	{
    		for(int j=0;j<gc[i].length;j++)
    		{
    			//stdj[j] +=  gc[i][j] * (double)((j - meansj[j])*(j - meansj[j]));
    			stdj[j] +=  (gc[i][j] - meansj[j]) * (gc[i][j] - meansj[j]);
    		}
    	}
        for(int i=0;i<stdj.length ;i++) stdj[i]=Math.sqrt( stdj[i] );
    	//
        double correl=0;
        for(int i=0;i<gc.length ;i++)
    	{
    		for(int j=0;j<gc[i].length;j++)
    		{
    			double tel = (double)( i - meansi[i]) * ( j - meansj[j]);
    			double noe = (stdi[i]*stdi[i]) * (stdj[j]*stdj[j]);
    			noe =  noe == 0 ? Double.MAX_VALUE / gc[i].length : noe;
    			correl += gc[i][j] * tel * Math.sqrt( noe );
    		}
    	}
        return correl;
    }
    
    private double calcCorrelation( double[][] gc)
    {
    	double meani =0;
    	double meanj =0;
    	for(int i=0;i<gc.length ;i++)
    	{
    		for(int j=0;j<gc[i].length;j++)
    		{
    			meani += i * gc[i][j];
    			meanj += j * gc[i][j];
    		}
    	}
    	//
    	double stdevi =0;
    	double stdevj =0;
    	for(int i=0;i<gc.length ;i++)
    	{
    		for(int j=0;j<gc[i].length;j++)
    		{
    			stdevi += ( i - meani ) * ( i - meani ) * gc[i][j];
    			stdevj += ( j - meanj ) * ( j - meanj ) * gc[i][j];
       	    }
    	}
    	stdevi = Math.sqrt( stdevi );
    	stdevj = Math.sqrt( stdevi );
        //
    	double correl = 0;
    	double noemer =  stdevi * stdevj;
    	if( noemer == 0 ) noemer = Double.MAX_VALUE / gc.length;  // lrge number
    	for(int i=0;i<gc.length ;i++)
    	{
    		for(int j=0;j<gc[i].length;j++)
    		{
    			double tel = ( i - meani ) * ( j - meanj ) * gc[i][j];
    			correl += tel / noemer;
    	    }
    	}
        return correl;
    }
    
    
}
