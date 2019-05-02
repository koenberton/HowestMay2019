package generalImagePurpose;

import logger.logLiason;
import cbrTekStraktorModel.cmcProcEnums;
import cbrTekStraktorModel.cmcProcSettings;
import generalStatPurpose.gpFrequencyDistributionStatFunctions;
import generalStatPurpose.gpListStatFunctions;

public class cmcBulkImageRoutines {

	cmcProcSettings xMSet=null;
	cmcImageRoutines irout=null;
	logLiason logger=null;
	
	private int BANDBREEDTE = 5;
	private int  RASTERDIAMETER=5;  //  even
	private boolean[][] rastermask=null;
	
	//private int fastMean=-1;
	private double fastMean=-1;
	private double fastDeviation=-1;
	private int[] cutWindow;
	private float BWDensity=1f;
	private int[] grayscale_centers = null;
	

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
	public cmcBulkImageRoutines(cmcProcSettings im, logLiason ilog)
	//-----------------------------------------------------------------------
	{
		xMSet = im;
		logger=ilog;
		irout = new cmcImageRoutines(logger);
		maakRasterMask();
	}
	
	//-----------------------------------------------------------------------
	public int[] binarize(int[] pixels , int threshold )
	//-----------------------------------------------------------------------
	{
		    int[] work = new int[ pixels.length ];
			int lum=0; 
			int nbrOfBlackPixels=0;
			BWDensity=1f;
		    for(int i=0;i<pixels.length;i++) 
	        { 
	                int p = pixels[i]; 
	                lum = 0xff & ( p >> 16);    // RGB hebben in een grayscale toch alle dezelfde waarde 
	                if( lum < threshold) {lum = 0; nbrOfBlackPixels++; } else { lum = 255;  }   
	                work[i] = (255 << 24) | (lum << 16) | (lum << 8) | lum; 
	        } 
		    if( pixels.length > 0 ) BWDensity = (float)nbrOfBlackPixels / (float)pixels.length; else BWDensity=1f;
		    return work;
    }
	
	//-----------------------------------------------------------------------
	public float getBWDensity()
	//-----------------------------------------------------------------------
	{
		return BWDensity;
	}
	
	//-----------------------------------------------------------------------
	public int[] doGrayscale(int[] pixels )
	//-----------------------------------------------------------------------
	{
		int[] work = new int[ pixels.length ];
		int lum=0;
	    for(int i=0;i<pixels.length;i++) 
        { 
	         lum = irout.grayScale(pixels[i]);      
             work[i] = (255 << 24) | (lum << 16) | (lum << 8) | lum; 
       } 
	   return work;
	}
	
	
	
	//-----------------------------------------------------------------------
	public int[] invertImage(int[] pixels )
	//-----------------------------------------------------------------------
	{
		int[] work = new int[ pixels.length ];
	    for(int i=0;i<pixels.length;i++) 
        { 
                int p = pixels[i]; 
                int r = 0xff & (0xff - (p >> 16));   
                int g = 0xff & (0xff - (p >> 8)); 
                int b = 0xff & (0xff -p); 
                work[i] = (255 << 24) | (r << 16) | (g << 8) | b; 	
        } 
		return work;
	}
	
	// must be black and white
	//-----------------------------------------------------------------------
	public int[] doBlue(int[] pixels )
	//-----------------------------------------------------------------------
	{
		int[] work = new int[ pixels.length ];
	    for(int i=0;i<pixels.length;i++) 
        { 
	                int p = pixels[i]; 
	                int bw = 0xff & p;   
	                bw = ( bw == 0) ? 0x00 : 0xff;
	                if( bw == 0 ) {  // zwart -> wit
	                	//bw = 0xff;   
		                //work[i] = (255 << 24) | (bw << 16) | (bw << 8) | bw;
		                work[i] = 0xffffffff;
	                }
	                else {   // wit -> blauw
	                    int r = 0x00;   
	                    int g = 0x00;
	                    int b = 0xff;
	                    work[i] = (255 << 24) | (r << 16) | (g << 8) | b; 	
	                }
	    } 
		return work;
	}
	
	// must be black and white
	//-----------------------------------------------------------------------
	public int[] doMainframe(int[] pixels )
	//-----------------------------------------------------------------------
	{
			int[] work = new int[ pixels.length ];
		    for(int i=0;i<pixels.length;i++) 
	        { 
		                int p = pixels[i]; 
		                int bw = 0xff & p;   
		                bw = ( bw == 0) ? 0x00 : 0xff;
		                if( bw == 0 ) {  // zwart lijnen -> groen
		                    int r = 0x00;   
		                    int g = 0xff;
		                    int b = 0x00;
		                    work[i] = (255 << 24) | (r << 16) | (g << 8) | b; 	
		                }
		                else {   // wit -> zwart
		                	work[i] = 0xff000000;
			            }
		    } 
			return work;
	}
	
	//-----------------------------------------------------------------------
	private int[] bleachImage(int[] pixels , int tipe)
	//-----------------------------------------------------------------------
	{
		int[] work = new int[ pixels.length ];
		if( tipe == 0 ) {
			for(int i=0;i<pixels.length;i++) 
			{ 
	    	 work[i] = irout.bleach(pixels[i]);
			} 
		}
		else
		if( tipe == 1 )
		{
			for(int i=0;i<pixels.length;i++) 
			{ 
	    	 work[i] = irout.hardbleach(pixels[i]);
			}	
		}
		else
		{
			for(int i=0;i<pixels.length;i++) 
			{ 
	    	 work[i] = irout.blackbleach(pixels[i]);
			}	
		}	
		return work;
	}
	//-----------------------------------------------------------------------
	public int[] bleachHardImage(int[] pixels )
	//-----------------------------------------------------------------------
	{
		return bleachImage(pixels,1);
	}
	//-----------------------------------------------------------------------
	public int[] bleachSoftImage(int[] pixels )
	//-----------------------------------------------------------------------
	{
		return bleachImage(pixels,0);
	}
	//-----------------------------------------------------------------------
	public int[] bleachImageToBlack(int[] pixels )
	//-----------------------------------------------------------------------
	{
			return bleachImage(pixels,2);
	}
	//-----------------------------------------------------------------------
	public int[] doHistogramEqualization(int[] pixels)
	//-----------------------------------------------------------------------
	{
		    int[] work = null;
			work = this.doGrayscale(pixels);
		
			// frequentie distributie van grijs
			int[] freq = new int[256];
			for(int i=0;i<freq.length;i++) freq[i]=0;
		    for(int i=0;i<work.length;i++) 
	        { 
	         freq[ work[i] & 0x000000ff]++;
	        }
		    // cumuleren : Cumulative Distribution Function
		    int prev=0;
		    for(int i=0;i<freq.length;i++)
		    {
		    	freq[i] += prev;  // CDF
		    	prev = freq[i];  
		    }
	        // Formule   (CDF(i) - 1) /  ( breedte x hoogte ) )  x  (256-1)
		    for(int i=0;i<freq.length;i++)
		    {
		    	double dd =  ((double)(freq[i] - 1) / (double)work.length) * (double)255; 
		    	freq[i] = (int)Math.round(dd);
		    }
		    // grijs op RGB en Lum zetten
		    for(int i=0;i<freq.length;i++)
		    {
		    	 int j = freq[i] & 0x000000ff;
	             freq[i] = (0xff << 24) | (j <<16) | ( j<<8) | j;
		    }
		    // grijswaarde naar verdeelde grijswaarde
		    for(int i=0;i<work.length;i++) 
	        { 
	          work[i] = freq[ work[i] & 0x000000ff ];
		     }
		    //
		    return work;
	}

	//-----------------------------------------------------------------------
	private int getMarginStat(int iw , int ih , int[] pixels , int tipe)
	//-----------------------------------------------------------------------
	{
		   // calculate the median grayscale values of the border
		   int[] histo = new int[256];
		   
		   // sample the margins by taking a band of BANDBREEDTE pixels
		   int lum=-1;
		   for(int i=0;i<256;i++) histo[i]=0;
		   // 
		   for(int y=0;y<ih;y++)
		   {
			   if( (y > BANDBREEDTE) &&  ( y < (ih - BANDBREEDTE)) ) continue;
			   for(int x=0;x<iw;x++)
			   {
				   if( (x > BANDBREEDTE) &&  ( x < (iw - BANDBREEDTE)) ) continue;
				   lum = irout.grayScale( pixels[ x + (y*iw) ] );
				   histo[lum] = histo[lum]+1;
			   }
		   }
		   gpFrequencyDistributionStatFunctions pstat = new gpFrequencyDistributionStatFunctions();
		   switch( tipe )
		   {
		   case 0 : return pstat.getMean(histo);
		   case 1 : return pstat.getMedian(histo);
		   case 2 : return pstat.getMin(histo);
		   case 3 : return pstat.getMax(histo);
		   default : { do_error("getMargeinStat unsupported tipe [" + tipe + "]"); break; }
		   }
		   return 0;
	}
	
	// this routine calculates the mean RGB value of the margins of a comic book page
	//-----------------------------------------------------------------------
	public int getMarginMeanColor(int iw , int ih , int[] pixels)
	//-----------------------------------------------------------------------
	{      
		   return getMarginStat(iw,ih,pixels,1);
	}
	// this routine calculates the mean RGB value of the margins of a comic book page
	//-----------------------------------------------------------------------
	public int getMarginMinColor(int iw , int ih , int[] pixels)
	//-----------------------------------------------------------------------
	{      
			return getMarginStat(iw,ih,pixels,2);
	}
	

	// Background equalisation
	// RGB values of which the grayscale value is darker than a threshold are ignored, otherwise set to WHITE
	//-----------------------------------------------------------------------
	public int[] binarizeGrayBackGround(int Ondergrens , int[] pixels)
	//-----------------------------------------------------------------------
	{
		    System.out.println("Background is not completly white. Fixing it.");
		    int lum=0; 
		    int[] work = new int[pixels.length];
	        for(int i=0;i<pixels.length;i++) 
	        { 
	                lum = irout.grayScale(pixels[i]);             
	                if( lum < Ondergrens ) {
	                	work[i] = pixels[i];
	                	continue;   
	                }
	                lum = 0xff;  // wit
	                work[i] = (255 << 24) | (lum << 16) | (lum << 8) | lum; 	
	        } 
	        return work;
	}
	
	//---------------------------------------------------------------------------------
	private boolean inRaster(int i , int iImageWidth)
	//---------------------------------------------------------------------------------
	{
			int x = ((i % iImageWidth) % RASTERDIAMETER);
			int y = ((i / iImageWidth) % RASTERDIAMETER);
		     return rastermask[x][y];
	}

	//---------------------------------------------------------------------------------
	private void maakRasterMask()
	//---------------------------------------------------------------------------------
	{
			rastermask = new boolean[RASTERDIAMETER][RASTERDIAMETER];
			// rond
			for(int x=0;x<RASTERDIAMETER;x++)
			{
				double xsigned = (double)x - (RASTERDIAMETER/2);
				for(int y=0;y<RASTERDIAMETER;y++)
				{
					double ysigned = (double)y - (RASTERDIAMETER)/2;
					double r = (double)((xsigned * xsigned ) + ( ysigned * ysigned));
					r = Math.sqrt( r );
					if( Math.round(r) > (RASTERDIAMETER/2) ) rastermask[x][y]=false; else rastermask[x][y]=true; 
				}
			}
	}
		
	//---------------------------------------------------------------------------------
	public int[] makeGrayRasterizedPicture(int[] pixels,int width)
	//---------------------------------------------------------------------------------
	{
		    int[] work = new int[pixels.length];
			for(int i=0;i<pixels.length;i++)
			{
				if( !inRaster(i,width) ) {
					work[i] = 0xffffffff;
					continue;
				}
				int lum = irout.grayScale(pixels[i]);
	         	work[i] = (255 << 24) | (lum << 16) | (lum << 8) | lum; ;
		 	}
			return work;
	}
	
	//---------------------------------------------------------------------------------
	public int[] makeColorRasterizedPicture(int[] pixels,int width)
	//---------------------------------------------------------------------------------
	{
		  		int[] work = new int[pixels.length];
				for(int i=0;i<pixels.length;i++)
				{
					if( !inRaster(i,width) ) {
						work[i] = 0xffffffff;
						continue;
					}
		          work[i] = pixels[i] | 0xff000000;
			 	}
				return work;
	}
	
	//---------------------------------------------------------------------------------
	private int[] cutWindow( int x , int y ,  int[]work , int width , int win_w , int win_h)
	//---------------------------------------------------------------------------------
	{
	    // faster
	    int height = work.length / width;
	    int left_x = x - (win_w / 2);
	    if( left_x < 0 ) left_x = 0;
	    int right_x = x + (win_w / 2);
	    if( right_x > (width-1) ) right_x = width-1;
	    int top_y  = y - (win_h / 2);
	    if( top_y < 0 ) top_y = 0;
	    int bottom_y = y + (win_h / 2);
	    if( bottom_y > (height-1) ) bottom_y = height-1;
	    int actual_win_w = right_x - left_x + 1;
	    int actual_win_h = bottom_y - top_y + 1;
	    int[] window = new int[actual_win_w * actual_win_h];
	    
	    int z=0;
	    double sum=0;
	    try {
	    	int start_x = left_x + ((top_y - 1) * width);
		    int stop_x=-1;
		    for(int yy=top_y;yy<=bottom_y;yy++)
	        {
	         start_x += width;
	         stop_x   = start_x + actual_win_w - 1;
	         for(int xx=start_x;xx<=stop_x;xx++)
	         {
	    		window[ z ] =  0xff & ( work[xx] >> 16);     // neem de B waarde
	    		sum += (double)window[z];
	    		z++;  
	         }
	       }
		   fastMean = (int)Math.round( sum / (double)(z-1) );
	   }
       catch(Exception e ) {
    	 do_error("" + x + " " + y +" " + window.length + " " + width + " " + xMSet.xU.LogStackTrace(e));
    	 return null;
       }
	   return window;
	}

	//  has been optimized, ie. also calculates mean
	//---------------------------------------------------------------------------------
	private int cutWindowBis( int x , int y ,  int[]work , int width , int win_w , int win_h)
	//---------------------------------------------------------------------------------
	{
		    int height = work.length / width;
		    int left_x = x - (win_w / 2);
		    if( left_x < 0 ) left_x = 0;
		    int right_x = x + (win_w / 2);
		    if( right_x > (width-1) ) right_x = width-1;
		    int top_y  = y - (win_h / 2);
		    if( top_y < 0 ) top_y = 0;
		    int bottom_y = y + (win_h / 2);
		    if( bottom_y > (height-1) ) bottom_y = height-1;
		    int actual_win_w = right_x - left_x + 1;
		    //int actual_win_h = bottom_y - top_y + 1;
		    //int[] window = new int[actual_win_w * actual_win_h];
		    
		    int z=0;
		    double sum=0;
		    try {
		    	int start_x = left_x + ((top_y - 1) * width);
			    int stop_x=-1;
			    for(int yy=top_y;yy<=bottom_y;yy++)
		        {
		         start_x += width;
		         stop_x   = start_x + actual_win_w - 1;
		         for(int xx=start_x;xx<=stop_x;xx++)
		         {
		    		//cutWindow[ z ] =  0xff & ( work[xx] >> 16);     // neem de B waarde
		        	cutWindow[ z ] =  work[xx];
		    		sum += (double)cutWindow[z];
		    		z++;  
		         }
		       }
			   fastMean = sum / (double)(z-1);
		   }
	       catch(Exception e ) {
	    	 do_error("" + x + " " + y + " " + width + " " + xMSet.xU.LogStackTrace(e));
	    	 return -1;
	       }
		   return (z-1);
	}
	
	//---------------------------------------------------------------------------------
	private void fastStat(int[] window , int aantal)
	//---------------------------------------------------------------------------------
	{
		   double sq = 0;
		   double dd=0;
		   for(int i=0;i<aantal;i++)
		   {
			    dd = (double)window[i] - fastMean;
				sq += (dd * dd);
		   }
		   double meanVariance=sq;
		   if( aantal > 1 ) meanVariance = sq / ((double)aantal - 1); else meanVariance = sq;
		   fastDeviation = Math.sqrt( meanVariance );
	}
	
	// has been optimized
	//---------------------------------------------------------------------------------
	public int[] doNiblak(int[] pixels , int width , boolean do_niblak)
	//---------------------------------------------------------------------------------
	{
		int[] work = new int[ pixels.length ];
		// grayscale - accelerator : just keep the blue component
		int lum=0;
		int[] grijs = new int [ pixels.length ];  //  grayscale and just keep the blue component
	    for(int i=0;i<pixels.length;i++) 
        { 
	         lum = irout.grayScale(pixels[i]);      
             grijs[i] = (0x00 << 24) | (0x00 << 16) | (0x00 << 8) | lum; 
        }
	    
		// sliding window and calculate T for the pixel in center (T = mean - 0.2 stdev)
		int window_width = 20;  // 2.5 gemiddelde letterbreedte
		int window_heigth = 40; // 2.5 avg letterhoogte
        cutWindow = new int[(window_width + 1) * (window_heigth + 1)];		
	    //
		int aantal=-1;
		if( do_niblak ) {
		 double niblak=-1;
		 for(int i=0;i<pixels.length;i++)
		 {
			aantal = cutWindowBis( i % width , i / width , grijs , width , window_width , window_heigth);
		    fastStat( cutWindow , aantal );
		    niblak = fastMean - ((double)0.2 * fastDeviation);
		    //if( x == 100 ) System.out.println( "" + width + " (" + x + "," + y + ") " + fastMean + " " + fastDeviation + " " + niblak );
			//lum = 0xff & ( grijs[i] >> 16);
		    lum = (grijs[i] < (int)niblak) ? 0x00 : 0xff;
	        work[ i ] = (255 << 24) | (lum << 16) | (lum << 8) | lum; 
		 }
		}
		else {
		 double sauvola=-1;
		 for(int i=0;i<pixels.length;i++)
		 {
			aantal = cutWindowBis( i % width , i / width , grijs , width , window_width , window_heigth);
		    fastStat( cutWindow , aantal );
		    sauvola = fastMean * ((double)1 - ((double)0.2 * ((double)1 - (fastDeviation / (double)128))));
			//if( x == 100 ) System.out.println( "" + width + " (" + x + "," + y + ") " + fastMean + " " + fastDeviation + " " + sauvola);
			//lum = 0xff & ( grijs[i] >> 16);
		    lum = (grijs[i] < (int)sauvola) ? 0x00 : 0xff;
	     	work[ i ] = (255 << 24) | (lum << 16) | (lum << 8) | lum;
		 }
		}
		return work;
	}
	
	
	//-----------------------------------------------------------------------
	public int[] doConvolution(int[] pixels , generalImagePurpose.cmcProcConvolution.kernelType kernelTipe , int width , int heigth)
	//-----------------------------------------------------------------------
	{
		int[] work = new int[ pixels.length ];
		cmcProcConvolution convol = new cmcProcConvolution(logger);
		convol.convolve( pixels, work, kernelTipe , width, heigth);
	    return work;
	}
	
	
	// 
	/*
	 * Sobel filters voor X en Y
	 * GX  +1 0 -1        GY 1  2  1
	 *     +2 0 -2           0  0  0
	 *     +1 0 -1          -1 -2 -1
	 */
	// pixels should already be in grayscale
	//-----------------------------------------------------------------------
	public int[] sobelOnGrayScale(int[] pixels , int imawidth )
	//-----------------------------------------------------------------------
	{
			    int[] work = new int[ pixels.length ];
			    int[] sobel = new int[9];
			    int x=-1;
			    int y=0;
			    int q=-1;
			    for(int i=0;i<pixels.length;i++) 
		        {
			    	    x++;
			    	    if( x >= imawidth ) {
			    	    	x=0;
			    	    	y++;
			    	    }
			    	    q = ((y-1) * imawidth) + x - 1; if (q<0) q=i; sobel[0] = pixels[q]; 
			    	    q = ((y-1) * imawidth) + x    ; if (q<0) q=i; sobel[1] = pixels[q]; 
			    	    q = ((y-1) * imawidth) + x + 1; if (q<0) q=i; sobel[2] = pixels[q]; 
			    	    q = (y * imawidth) + x - 1; if (q<0) q=i; sobel[3] = pixels[q]; 
			    	    q = (y * imawidth) + x   ;  sobel[4] = pixels[q];
			    	    q = (y * imawidth) + x + 1; if (q>=pixels.length) q=i; sobel[5] = pixels[q]; 
			    	    q = ((y+1) * imawidth) + x - 1; if (q>=pixels.length) q=i; sobel[6] = pixels[q]; 
			    	    q = ((y+1) * imawidth) + x    ; if (q>=pixels.length) q=i; sobel[7] = pixels[q]; 
			    	    q = ((y+1) * imawidth) + x + 1; if (q>=pixels.length) q=i; sobel[8] = pixels[q]; 
                        //
			    	    for(int j=0;j<9;j++)
			    	    {
			    	    	 int p = sobel[j]; 
				             sobel[j] = 0xff & p;  // neem de B component van de grayscale
			    	    }
			    	    int sumX = sobel[0] - sobel[2] + (sobel[3]*2) - (sobel[5]*2) + sobel[6] - sobel[8];
			    	    int sumY = sobel[0] + (2*sobel[1]) + sobel[2] - (sobel[6]) - (2*sobel[7]) - sobel[8];
			    	    int lum = ( sumX + sumY ) / 2;
			    	    if( lum > 255) lum = 255; else  if( lum < 0) lum = 0;
			    	    work[i] = (255 << 24) | (lum << 16) | (lum << 8) | lum;
		        } 
			    return work;
   }

	//-----------------------------------------------------------------------
	public int[] colorsobel(int[] pixels , int imawidth )
	//-----------------------------------------------------------------------
	{
				    int[] work = new int[ pixels.length ];
				    int[] sobel = new int[9];
				    int[] red = new int[9];
				    int[] gre = new int[9];
				    int[] blu = new int[9];
				    int x=-1;
				    int y=0;
				    int q=-1;
				    for(int i=0;i<pixels.length;i++) 
			        {
				    	    x++;
				    	    if( x >= imawidth ) {
				    	    	x=0;
				    	    	y++;
				    	    }
				    	    q = ((y-1) * imawidth) + x - 1; if (q<0) q=i; sobel[0] = pixels[q]; 
				    	    q = ((y-1) * imawidth) + x    ; if (q<0) q=i; sobel[1] = pixels[q]; 
				    	    q = ((y-1) * imawidth) + x + 1; if (q<0) q=i; sobel[2] = pixels[q]; 
				    	    q = (y * imawidth) + x - 1; if (q<0) q=i; sobel[3] = pixels[q]; 
				    	    q = (y * imawidth) + x   ;  sobel[4] = pixels[q];
				    	    q = (y * imawidth) + x + 1; if (q>=pixels.length) q=i; sobel[5] = pixels[q]; 
				    	    q = ((y+1) * imawidth) + x - 1; if (q>=pixels.length) q=i; sobel[6] = pixels[q]; 
				    	    q = ((y+1) * imawidth) + x    ; if (q>=pixels.length) q=i; sobel[7] = pixels[q]; 
				    	    q = ((y+1) * imawidth) + x + 1; if (q>=pixels.length) q=i; sobel[8] = pixels[q]; 
	                        //
				    	    for(int j=0;j<9;j++)
				    	    {
				    	    	 int p = sobel[j]; 
				    	    	 red[j] = 0xff & (0xff - (p >> 16));   
				                 gre[j] = 0xff & (0xff - (p >> 8)); 
				                 blu[j] = 0xff & (0xff -p); 
				            }
				    	    //
				    	    int redX = red[0] - red[2] + (red[3]*2) - (red[5]*2) + red[6] - red[8];
				    	    int redY = red[0] + (2*red[1]) + red[2] - (red[6]) - (2*red[7]) - red[8];
				    	    int redL = ( redX + redY ) / 2;
				    	    if( redL > 255) redL = 255; else  if( redL < 0) redL = 0;
                            //
				    	    int greX = gre[0] - gre[2] + (gre[3]*2) - (gre[5]*2) + gre[6] - gre[8];
				    	    int greY = gre[0] + (2*gre[1]) + gre[2] - (gre[6]) - (2*gre[7]) - gre[8];
				    	    int greL = ( greX + greY ) / 2;
				    	    if( greL > 255) greL = 255; else  if( greL < 0) greL = 0;
                            //
				    	    int bluX = blu[0] - blu[2] + (blu[3]*2) - (blu[5]*2) + blu[6] - blu[8];
				    	    int bluY = blu[0] + (2*blu[1]) + blu[2] - (blu[6]) - (2*blu[7]) - blu[8];
				    	    int bluL = ( bluX + bluY ) / 2;
				    	    if( bluL > 255) bluL = 255; else  if( bluL < 0) bluL = 0;
				    	    //
				    	    int p = (255 << 24) | (redL << 16) | (greL << 8) | bluL;
				    	    // grayscale
				    	    int r = irout.grayScale( p );
				    	    work[i] =  work[i] = (255 << 24) | (r << 16) | (r << 8) | r;
			        } 
				    return work;
	   }

	/*
	 * Wide Gx = -1 0 1        Narrow Gx = -1 1
	 * 
	 * wide Gy -1              Wide Gy = -1
	 *          0                         1
	 *          1
	 */
	//-----------------------------------------------------------------------
	private int[] gradient(int[] pixels , int imawidth , boolean wide , boolean forHOGGradient )
	//-----------------------------------------------------------------------
	{
				    int[] work = new int[ pixels.length ];  // 0 1 2      3 4 5      6 7 8 
				    int[] grad = new int[9];    // links boven - boven - rechts boven - links - center - etc
				    int[] red = new int[9];
				    int[] gre = new int[9];
				    int[] blu = new int[9];
				    int x=-1;
				    int y=0;
				    int q=-1;
				    for(int j=0;j<9;j++) grad[j]=0;
				    int idx1 = wide ? 5 : 4;
				    int idx2 = wide ? 7 : 4;
				    double MaxMagnitude =  Math.sqrt((double)((255 * 2) * (255 * 2)));
				    for(int i=0;i<pixels.length;i++) 
			        {
				    	    x++;
				    	    if( x >= imawidth ) {
				    	    	x=0;
				    	    	y++;
				    	    }
				    	    q = ((y-1) * imawidth) + x  ; if (q<0) q=i; grad[1] = pixels[q]; 
					        q = ( y * imawidth ) + x - 1; if (q<0) q=i; grad[3] = pixels[q];
				    	    q = ( y * imawidth ) + x    ; if (q<0) q=i; grad[4] = pixels[q];
				    	    q = (y * imawidth) + x + 1  ; if (q>=pixels.length) q=i; grad[5] = pixels[q]; 
				    	    q = ((y+1)* imawidth) + x   ; if (q>=pixels.length) q=i; grad[7] = pixels[q]; 
				    	    //
				    	    for(int j=0;j<9;j++)
				    	    {
				    	    	 int p = grad[j]; 
				    	    	 red[j] = 0xff & (0xff - (p >> 16));   
				                 gre[j] = 0xff & (0xff - (p >> 8)); 
				                 blu[j] = 0xff & (0xff -p); 
				            }
				    	    int redX = red[idx1] - red[3] + 255;   // range is -255 to 255 ; so make it positive by adding 255
				    	    int redY = red[idx2] - red[1] + 255;
				    	    int redL = (int)Math.sqrt((double)((redX*redX) + (redY*redY)));
				    	    //
				    	    int greX = gre[idx1] - gre[3] + 255;
				    	    int greY = gre[idx2] - gre[1] + 255;
				    	    int greL = (int)Math.sqrt((double)((greX*greX) + (greY*greY)));
				    	    //
				    	    int bluX = blu[idx1] - blu[3] + 255;
				    	    int bluY = blu[idx2] - blu[1] + 255;
				    	    int bluL = (int)Math.sqrt((double)((bluX*bluX) + (bluY*bluY)));
				    	    //
					        if( forHOGGradient ) {  // Pick the color element with the largest Magnitude
					        	int dx = redX;
					        	int dy = redY;
					        	if( redL < greL ) {
					        		dx = greX;
					        		dy = greY;
					        		if( greL < bluL ) {
					        			dx = bluX;
					        			dy = bluY;
					        		}
					        	}
					        	//  Use an integer to store the Dx and Dy. Dx and Dy are larger then 255
					  		    //  So if the bytes in the int are as follows : ABCD  then  AB = Dy (2 bytes) and  CD = Dx (2 bytes)	
					            work[ i ] = ((dy & 0xffff) << 16) | (dx & 0xffff);
					      	    continue;
					        }
					        // Reduce all color magnitudes to 255
					        redL = (int)(((double)redL / MaxMagnitude) * (double)255);
					        greL = (int)(((double)greL / MaxMagnitude) * (double)255);
					        bluL = (int)(((double)bluL / MaxMagnitude) * (double)255);
					        int r = (255 << 24) | (redL << 16) | (greL << 8) | bluL;
					        work[i] =  work[i] = (255 << 24) | (r << 16) | (r << 8) | r;
				    } 
				    return work;
   }

	//-----------------------------------------------------------------------
	public int[] gradientwide(int[] pixels , int imawidth )
	//-----------------------------------------------------------------------
	{
		return gradient(pixels,imawidth,true,false);
	}

	//-----------------------------------------------------------------------
	public int[] gradientnarrow(int[] pixels , int imawidth )
	//-----------------------------------------------------------------------
	{
		return gradient(pixels,imawidth,false,false);
	}

	
	// returns an INT :  first 2 bytes are Dy and last 2 bytes are Dx  Dx and DY range from 0 .. (2*255)
	//-----------------------------------------------------------------------
	public int[] HOGGradientDxDy(int[] pixels , int imawidth )
	//-----------------------------------------------------------------------
	{
		return gradient(pixels,imawidth,true,true);  //  HOG is the WIDE variant

		/*
		  int[] input = isGrayScale ? pixels : doGrayscale(pixels);
		  int[] work = new int[ pixels.length ];
		  int width = imawidth;
		  int height = pixels.length / width;
		  
		  
		  try {
		  
		  //  Use an integer to store the Dx and Dy. Dx and Dy are larger then 255
		  //  So if the bytes in the int are as follows : ABCD  then  AB = Dy (2 bytes) and  CD = Dx (2 bytes)
		  // horizontal 
		  for(int i=0;i<height;i++)
		  {
			  int k = (width * i) + 1;
			  for(int j=1;j<(width-1);j++)  // skip first and last column
			  {
				  int left  = input[ k-1 ] & 0xff;
				  int right = input[ k+1 ] & 0xff;
				  int dx = right - left + 0xff;   // range is -255 to +255  so add 255 to make it positive  => 0 .. 510
				  work[ k ] = dx & 0xffff;   // use 2 bytes
				  k++;
			  }
		  }
		  // vertical
		  for(int i=1;i<(height-1);i++)
		  {
			  int k = i * width;
			  for(int j=0;j<width;j++)
			  {
				  int up = input[ k - width] & 0xff;
				  int down = input[ k + width ] & 0xff;
				  int dy = down - up + 0xff;
				  work[ k ] = ((dy & 0xffff) << 16) | work[ k ];
				  k++;
			  }
		  }
		  
		  }
		  catch(Exception e ) { 
			  e.printStackTrace();
		      do_error("-->" + input.length );
		      return null;
		  }
		  return work;
		  */  
	}
	
	// make polar (complex getal) from the DxDy gradient
	// int = 4 bytes=ABCD   AB = magnitude (0..sqrt(255*255*2) and  CD = theta in degrees 0..360
	//-----------------------------------------------------------------------
	public int[] TransformHOGGradientDxDyToMagnitudeTheta(int[]dydx)
	//-----------------------------------------------------------------------
	{
		 int[] work = new int[ dydx.length ];
		 double degreefactor = (double)360 / ((double)2 * Math.PI);
		 for(int i=0;i<dydx.length;i++)
		 {
			  int dx = (dydx[i] & 0xffff);
			  int dy = ((dydx[i] & 0xffff0000) >> 16 );
			  int magnitude = (int)(Math.sqrt( (double)((dx *dx) + (dy * dy)) ));
			  // angle => you need to transform back to -255 > 255 to determine correct polarity
			  // ATAN2   -PI to PI
			  double thePI = Math.atan2( (double)dy -(double)255, (double)dx - (double)255 );  // -PI to PI
			  if( thePI < 0 ) thePI = 2 * Math.PI + thePI;  // -PI => above 180 degrees
			  int theta = (int)(thePI * degreefactor) % 360; //  Degrees 0..259
		      work[i] =  ((magnitude & 0xffff) << 16) | (theta & 0xffff);
		 }
		 // debug
		 int[] d360 = new int[361];
		 for(int i=0;i<d360.length;i++) d360[i]=0;
		 for(int i=0;i<work.length;i++)
		 {
			 int theta = work[i] & 0xffff;
			 if( (theta > 360) || (theta<0) ) {
				 do_error("System error - TransformHOGGradienToMagnitudeAndTheta [" + theta + "]");
				 continue;
			 }
			 d360[ theta ] ++;
		 }
		 String sout ="";
		 for(int i=0;i<d360.length;i++) sout += "[" + d360[i] + "]";
		 do_log( 9 , "Distribution of angles " + sout );
		 //
		 return work;
	}
	
	//-----------------------------------------------------------------------
	public int[] DisplayHOGGradient(int[] pixels , int imawidth , cmcProcEnums.HOG_GRADIENT_DISPLAY_TYPE displaytipe)
	//-----------------------------------------------------------------------
	{
		 int[] work = null;
		 try {
			  // create the HOG gradient ( DX - DY )
			  work = HOGGradientDxDy( pixels , imawidth );
			  if( work == null ) {
				  do_error("Something went wrong ");
				  return null;
			  }
			  
		      // Vizualization of the Gradient can be performed in 4 ways : Dx , Dy , Magnitude and Theta (angle)
		      switch( displaytipe )
			  {
			      case NONE : {  return work; }   // NO display
				  case HORIZONTAL_GRADIENT : { // DX
					  for(int i=0;i<work.length;i++)
					  {
						  int dx = ((work[i] & 0xffff) >> 1) & 0xff;   // >>1  is /2
						  work[i] =  (255 << 24) | (dx << 16) | (dx << 8) | dx;
					  }
					  break;
				  }
				  case VERTICAL_GRADIENT : { // DY
					  for(int i=0;i<work.length;i++)
					  {
						  int dy = ((work[i] & 0xffff0000) >> 17 ) & 0xff;   // >>16 and /2 => >>17
						  work[i] =  (255 << 24) | (dy << 16) | (dy << 8) | dy;
					  }
					  break;
				  }
				  case MAGNITUDE : {  // MAGNITUDE
					  work=TransformHOGGradientDxDyToMagnitudeTheta( work );
					  double MaxMagnitude =  Math.sqrt((double)((255 * 2) * (255 * 2)));
					  for(int i=0;i<work.length;i++)
					  {
						  int magnitude = ((work[i] & 0xffff0000) >> 16 );
						  int scaled = (int)(((double)magnitude / MaxMagnitude) * (double)255);
					      work[i] =  (255 << 24) | (scaled << 16) | (scaled << 8) | scaled;
					  }
					  break;
				  }
				  case THETA : { // Theta
					  work=TransformHOGGradientDxDyToMagnitudeTheta( work );
					  double factor = (double)255 / (double)360;
					  for(int i=0;i<work.length;i++)
					  {
						  int theta = (work[i] & 0xffff);  // 0 .. 360 degrees
						  int scaled =  (int)((double)theta * factor);
						  work[i] =  (255 << 24) | ((int)scaled << 16) | ((int)scaled << 8) | (int)scaled;
					  }
					  break;
				  }
				  /*
				  case MAGNITUDE : {  // MAGNITUDE
					  double MaxMagnitude =  Math.sqrt((double)((255 * 2) * (255 * 2)));
					  for(int i=0;i<work.length;i++)
					  {
						  int dx = (work[i] & 0xffff);
						  int dy = ((work[i] & 0xffff0000) >> 16 );
						  double dd = Math.sqrt( (double)((dx *dx) + (dy * dy)) );
						  int magnitude =  ((int)( (dd / MaxMagnitude) * (double)255)) & 0xff; 
					      work[i] =  (255 << 24) | (magnitude << 16) | (magnitude << 8) | magnitude;
					  }
					  break;
				  }
				  */
				  /*
				  case THETA : { // Theta
				      double RadMulti = (double) 255 / ((double)2 * Math.PI);
					  for(int i=0;i<work.length;i++)
					  {
						  int dx = (work[i] & 0xffff);
						  int dy = ((work[i] & 0xffff0000) >> 16 );
						  int theta = (int)(Math.atan2( (double)dx , (double)dy ) * RadMulti);  //  RADials
					      //double dd = 255 * ((Math.toDegrees(Math.atan2( (double)dx , (double)dy )) + 180) / 360);
						  work[i] =  (255 << 24) | ((int)theta << 16) | ((int)theta << 8) | (int)theta;
					  }
					  break;
				  }
				  */
				  default : { do_error("Unsupported option HOGGradient [" + displaytipe + "]"); return work; }
			  }		 
		   
		  
		  }
		  catch(Exception e) {
			  e.printStackTrace();
		  }
		  //
		  return work;
	}
	
	// Is this a grayscale image (or an image in which the RGB channels are the same for all pixels)
	//-----------------------------------------------------------------------
	public boolean IsGrayScale(int[] pixels )
	//-----------------------------------------------------------------------
	{
	    for(int i=0;i<pixels.length;i++) 
        { 
                int p = pixels[i]; 
                int r = 0xff & (0xff - (p >> 16));   
                int g = 0xff & (0xff - (p >> 8)); 
                if( r != g ) return false;
                int b = 0xff & (0xff -p); 
                if( r != b ) return false;
        } 
		return true;
	}
		
	//-----------------------------------------------------------------------
	public int[] doMaxLloyd(int[] pixels , int targetlevel)
	//-----------------------------------------------------------------------
	{
	    // is this a grayscale image (or an image in which the RGB channels are the same for all pixels)
		boolean isGrayScale = IsGrayScale( pixels );
		grayscale_centers = null;
		//do_log( 9 , "GrayScale [" + isGrayScale + "]");
		
		// extract the RGB histograms
		int[] red   = new int[256];
		int[] green = new int[256];
		int[] blue  = new int[256];
		for(int i=0;i<red.length;i++) red[i] = blue[i] = green[i] = 0;
		for(int i=0;i<pixels.length;i++) 
        { 
                int p = pixels[i]; 
                int r = 0xff & (p >> 16);   
                int g = 0xff & (p >> 8); 
                int b = 0xff & p; 
                red[ r ]   += 1;
                green[ g ] += 1;
                blue[ b ]  += 1;
        } 
		//
		cmcMaxLloydQuantization  maxlloyd = new cmcMaxLloydQuantization(xMSet,logger); 
		//  array of startpoint of boundaries
		int[] red_reduced = maxlloyd.performMaxLloydQuantizationINT( red , targetlevel );
		if( red_reduced == null )  return pixels;
		int[] red_center = maxlloyd.getCentersINT();
        
		int[] green_reduced = null;
		int[] blue_reduced = null;
		int[] green_center = null;
		int[] blue_center = null;
        if( !isGrayScale ) {
        	green_reduced = maxlloyd.performMaxLloydQuantizationINT( green , targetlevel );
        	if( green_reduced == null ) return pixels;
        	green_center = maxlloyd.getCentersINT();
        	//
        	blue_reduced  = maxlloyd.performMaxLloydQuantizationINT( blue , targetlevel );
        	if( blue_reduced == null ) return pixels;
        	blue_center = maxlloyd.getCentersINT();
        }
        else {  // just duplicate the results from red
        	green_reduced = blue_reduced = red_reduced;
        	green_center = blue_center = red_center;
        	// store values 
        	grayscale_centers = new int[ red_center.length ];
        	for(int i=0;i<red_center.length;i++) grayscale_centers[i] = red_center[i];
        }
                
        // debug
        /*
        String sLijn=" reduced ";
        String sLijn2 = " center ";
        for(int i=0;i<red_reduced.length;i++)
        {
        	sLijn += "[" + red_reduced[i] + "]";
        	sLijn2 += "[" + red_center[i] + "]";
        }
        do_log( 1 , sLijn );
        do_log( 1 , sLijn2 );
        */
        
        int[] work = new int[ pixels.length ];
	    for(int i=0;i<pixels.length;i++) 
        { 
                int p = pixels[i]; 
                int r = 0xff & (p >> 16);   
                int g = 0xff & (p >> 8); 
                int b = 0xff & p;
                int r2 = r;
                int g2 = g;
                int b2 = b;
                // remap
                for(int j=0;j<red_reduced.length;j++) {
                	if( r < red_reduced[j] ) break;
                	r2 = red_center[j];
                }
                for(int j=0;j<green_reduced.length;j++) {
                	if( g < green_reduced[j] ) break;
                	g2 = green_center[j];
                }
                for(int j=0;j<blue_reduced.length;j++) {
                	if( b < blue_reduced[j] ) break;
                	b2 = blue_center[j];
                }
                work[i] = (255 << 24) | (r2 << 16) | (g2 << 8) | b2; 	
                
                //if( (i % 97) == 13 )do_log( 1 , "[R=" + r + ">" + r2 + "] [G=" + g + ">" + g2 + "] ]B=" + b + ">" + b2 + "]");
        } 
       
        //
        maxlloyd=null;
		return work;
	}
	
	//-----------------------------------------------------------------------
	public int[] getGrayScaleCenters()
	//-----------------------------------------------------------------------
	{
		return grayscale_centers;
	}
	
	//-----------------------------------------------------------------------
	public int[] doCanny(int[] pixels , int width)
	//-----------------------------------------------------------------------
	{
	  CannyEdgeDetector canny = new CannyEdgeDetector();
	  int[] ret = canny.cmcProcess( pixels , width );
	  canny=null;
	  return ret;
	}
	
}
