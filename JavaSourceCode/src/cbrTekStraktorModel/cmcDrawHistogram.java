package cbrTekStraktorModel;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.Polygon;
import java.awt.image.BufferedImage;
import java.io.File;

import javax.imageio.ImageIO;

import generalImagePurpose.cmcImageRoutines;
import imageProcessing.cmcProcColorHistogram;
import logger.logLiason;

public class cmcDrawHistogram {
	
	cmcProcSettings xMSet=null;
	logLiason logger=null;
	cmcImageRoutines irout = null;
	
	static Color CORNFLOWER = new Color(100, 149, 237);
	static int boxWidth = 170;
	static int boxHeigth = 270;
	
	
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
    public cmcDrawHistogram(cmcProcSettings is,logLiason ilog)
    //------------------------------------------------------------
    {
    	xMSet = is;
		logger=ilog;
		irout = new cmcImageRoutines(logger);
    }
   
    public int getBoxWidth()
    {
    	return boxWidth;
    }
    public int getBoxHeigth()
    {
    	return boxHeigth;
    }
  	
  	//-----------------------------------------------------------------------
  	public void colorHistogram(Graphics g , cmcProcColorHistogram h , int inWi , int inHe)
  	//-----------------------------------------------------------------------
  	{
  		if( h == null ) return;   // bvb. editmode
  		//
  		int yoffset = (inHe - 256) / 2;
  		int breedte = inWi - 30;
  		int xoffset =  inWi - (inWi - breedte) / 2;
  		//	
  		double maxallow= (double)1.5;
  		double histo[] = null;
  		int avg=0;
  		int med=0;
  		int rad=8;
  		double stddev=0;
  		for(int k=0;k<4;k++)
  		{
  			 switch(k)
  			 {
  			 case 0 : { histo = h.getHistoRed(); g.setColor(Color.RED); avg = h.getMeanRed(); med = h.getMedianRed(); stddev = h.getStdDevRed(); break; } 
  			 case 1 : { histo = h.getHistoGreen(); g.setColor(Color.GREEN); avg = h.getMeanGreen(); med = h.getMedianGreen(); stddev = h.getStdDevGreen(); break; } 
  			 case 2 : { histo = h.getHistoBlue(); g.setColor(Color.BLUE); avg = h.getMeanBlue(); med = h.getMedianBlue(); stddev = h.getStdDevBlue(); break; } 
  			 case 3 : { histo = h.getHistoGray(); g.setColor(Color.DARK_GRAY); avg = h.getMeanGray(); med = h.getMedianGray(); stddev = h.getStdDevGray(); break; } 
  			 }
  			
  		    double max = 0;
  		    for(int i=0;i<255;i++) // MAX maar exclusief de WITTE component
  			{
  		    	if( histo[i] > max ) max = histo[i];
  			}
  		    // cap de witte component - in strips is er veel wit / boorden enz.
  		    if( histo[255] > (maxallow*max) ) histo[255]= maxallow * max;
  		    if( histo[0] > (maxallow*max) ) histo[0]= maxallow * max;
  		    //
  		    int xp=0;
  			int x=0;
  			int xavg=0;
  			int xmed=0;
  			for(int i=0;i<256;i++)
  			{
  				x = (int)(histo[i] * (double)breedte / max);    // de histo bevat niet echt de frequentie maar de FREQ / TOTAAL pixels; doch aangeziendeeltal constant
  				g.drawLine( xoffset - xp  , 256 - i + yoffset + 1 , xoffset - x , 256 - i + yoffset);
  				xp = x;
  				if( i == avg ) xavg = x;
  				if( i == med ) xmed = x;
  			}
  			// vullen
  			Color pc = g.getColor();
  		    Color trans = new Color(pc.getRed(),pc.getGreen(),pc.getBlue(), 80 );
  		    g.setColor(trans);
  			for(int i=0;i<256;i++)
  			{
  				x = (int)(histo[i] * (double)breedte / max); 
  				g.drawLine( xoffset , 256 - i + yoffset , xoffset - x - 1 , 256 - i + yoffset);
  			}
  			g.setColor(pc);
  			// mean
  			g.fillOval(xoffset - (rad/2), 256 - avg + yoffset - (rad/2),rad,rad);
  			// mediaan
  			g.fillOval(xoffset - xmed - (rad/2), 256 - med + yoffset - (rad/2),rad,rad);
  		}
  		
  		// as
  		g.setColor(Color.DARK_GRAY);
  		g.fillRect( xoffset, yoffset , 1 , 256 );
  		
  		// gradient
  		for(int i=0;i<256;i++)
  		{
  			g.setColor(new Color(i,i,i));
  			g.fillRect( xoffset + 5, 256 - i + yoffset , 10 , 1 );
  		}
  		
  	}
    
    
    //-----------------------------------------------------------------------
  	private void boxWiskerChart(Graphics g , cmcProcColorHistogram  h , Color pc , boolean opaque )
  	//-----------------------------------------------------------------------
  	{
  		if ( h == null ) {
  			do_error("original histogram is NULL");
  			return;
  		}
  				
  		int dikte = 20;
  		int band = dikte + 20;
  		int hoogte = 300;
  		int breedte = (band *5);
  		int xoffset = 20;
  		int yoffset = 20;
  		// locatie afhankelijk van pixel
  				
  		int Y = yoffset - band + hoogte - (hoogte - 256)/2;
  		//Color pc = Color.WHITE;
  		if( opaque ) g.setColor( new Color(pc.getRed(),pc.getGreen(),pc.getBlue()) );
  		        else g.setColor( new Color(pc.getRed(),pc.getGreen(),pc.getBlue(), 200) );
  	    g.fillRoundRect(xoffset-band, yoffset-band, breedte, hoogte, 20, 20);	
  	    pc = CORNFLOWER;
  		g.setColor( new Color(pc.getRed(),pc.getGreen(),pc.getBlue(), 200) );
  		g.drawLine(xoffset - (band/2) , Y     , xoffset + (7*(band/2)) , Y);
  		g.drawLine(xoffset - (band/2) , Y-128 , xoffset + (7*(band/2)) , Y-128);
  		g.drawLine(xoffset - (band/2) , Y-256 , xoffset + (7*(band/2)) , Y-256);
  		g.drawLine(xoffset - (band/2) , Y-64  , xoffset + (7*(band/2)) , Y-64);
  		g.drawLine(xoffset - (band/2) , Y-192 , xoffset + (7*(band/2)) , Y-192);
  		
  		double histo[] = null;
  		int avg=0;
  		int med=0;
  		//int rad=8;
  		int mi=0;
  		int mx=0;
  		int q1=0;
  		int q3=0;
  		double stddev=0;
  		for(int k=0;k<4;k++)
  		{
  			switch(k)
  			{
  			 case 0 : { histo = h.getHistoRed(); g.setColor(Color.RED); avg = h.getMeanRed(); med = h.getMedianRed(); stddev = h.getStdDevRed(); mi=h.getMinRed(); mx=h.getMaxRed(); q1 = h.getQuart1Red(); q3 = h.getQuart3Red(); break; } 
  			 case 1 : { histo = h.getHistoGreen(); g.setColor(Color.GREEN); avg = h.getMeanGreen(); med = h.getMedianGreen(); stddev = h.getStdDevGreen(); mi=h.getMinGreen(); mx=h.getMaxGreen(); q1 = h.getQuart1Green(); q3 = h.getQuart3Green(); break; } 
  			 case 2 : { histo = h.getHistoBlue(); g.setColor(Color.BLUE); avg = h.getMeanBlue(); med = h.getMedianBlue(); stddev = h.getStdDevBlue(); mi=h.getMinBlue(); mx=h.getMaxBlue(); q1 = h.getQuart1Blue(); q3 = h.getQuart3Blue(); break; } 
  			 case 3 : { histo = h.getHistoGray(); g.setColor(Color.BLACK); avg = h.getMeanGray(); med = h.getMedianGray(); stddev = h.getStdDevGray(); mi=h.getMinGray(); mx=h.getMaxGray(); q1 = h.getQuart1Gray(); q3 = h.getQuart3Gray(); break; } 
  			}
  			//			
  			int midx = (xoffset-band) + (band * (k+1) );
  			int bot = Y - q1;
  			int top = Y - q3;
  			//
  			pc = g.getColor();
  			g.setColor( new Color(pc.getRed(),pc.getGreen(),pc.getBlue(), 100) );
  		    //
  			g.fillRect( midx - (dikte/2) , top , dikte , q3-q1); 
  			g.setColor( new Color(pc.getRed(),pc.getGreen(),pc.getBlue()).brighter() ); // kader helder
  			g.drawRect( midx - (dikte/2) , top , dikte , q3-q1); 
  			g.drawRect( midx - (dikte/2)+1 , top+1 , dikte-2 , q3-q1-2); 
  			// vertikale lijnen tot de MIn en MAx
  			g.drawLine( midx , Y - mi , midx , bot);
  			g.drawLine( midx , top , midx , Y - mx);
  			// dikte
  			g.drawLine( midx+1 , Y - mi , midx+1 , bot);
  			g.drawLine( midx+1 , top , midx+1 , Y - mx);
  			// Horizontale eindjes
  			g.drawLine( midx - 10, Y - mi , midx + 10 , Y - mi);
  			g.drawLine( midx - 10, Y - mx , midx + 10 , Y - mx);
  			// dikte
  			g.drawLine( midx - 10, Y - mi - 1, midx + 10 , Y - mi - 1);
  			g.drawLine( midx - 10, Y - mx - 1, midx + 10 , Y - mx - 1);
  			//median
  			g.drawLine( midx - (dikte/2) , Y - med , midx + (dikte/2) , Y - med); 
  			g.drawLine( midx - (dikte/2) , Y - med -1 , midx + (dikte/2) , Y - med -1); 
  			// mean
  			pc = Color.WHITE;
  			g.setColor( new Color(pc.getRed(),pc.getGreen(),pc.getBlue(), 100) );
  			g.drawLine( midx - (dikte/2) , Y - avg , midx + (dikte/2) , Y - avg); 
  			g.drawLine( midx - (dikte/2) , Y - avg -1 , midx + (dikte/2) , Y - avg -1); 
  			// stdev
  			pc = Color.WHITE;
  			g.setColor( new Color(pc.getRed(),pc.getGreen(),pc.getBlue(), 100) );
  			//g.drawLine( midx - (dikte/2) , Y - avg - ((int)stddev/2) , midx + (dikte/2) , Y - avg - ((int)stddev/2) );
  			//g.drawLine( midx - (dikte/2) , Y - avg + ((int)stddev/2) , midx + (dikte/2) , Y - avg + ((int)stddev/2) );
  			g.drawOval(midx - 5, Y - avg - ((int)stddev/2) - 5 , 10, 10);
  			g.drawOval(midx - 5, Y - avg + ((int)stddev/2) - 5 , 10, 10);
  			g.drawLine(midx , Y - avg - ((int)stddev/2) , midx , Y - avg + ((int)stddev/2) );
  		}	
  		
  	}
  	
    //-----------------------------------------------------------------------
  	public Image getBoxHisto(cmcProcColorHistogram  h , Color bg)
    //-----------------------------------------------------------------------
  	{
  		try {
  			  BufferedImage bi = new BufferedImage( boxWidth , boxHeigth , BufferedImage.TYPE_INT_RGB);
  			  Graphics2D ig = bi.createGraphics();
  			  boxWiskerChart( ig , h , bg , true );
  			  return bi;
  			}
  			catch(Exception e) {
  				do_error("Creating box diagram " + e.getMessage() + " " + xMSet.xU.LogStackTrace(e) );
  				return null;
  			}
  	}
  	
  	//-----------------------------------------------------------------------
  	public Image getBoxHisto(cmcProcColorHistogram  h, int height , Color bg)
    //-----------------------------------------------------------------------
  	{
  		BufferedImage bi = (BufferedImage)getBoxHisto( h , bg );
  		if( bi == null ) return null;
  		if( bi.getHeight() == height ) return bi;
  		double scale = (double)height / (double)bi.getHeight();
  		BufferedImage bx = irout.resizeImage( bi , bi.getWidth(), bi.getHeight(), scale );
  		return bx;
  	}
  	//-----------------------------------------------------------------------
  	public void writeBoxDiagramToFile(cmcProcColorHistogram  h , Color bg )
  	//-----------------------------------------------------------------------
  	{
  		try {
  		  BufferedImage bi = (BufferedImage)getBoxHisto( h , bg );
  		  if( bi == null ) return;
  		  ImageIO.write(bi, xMSet.getPreferredImageSuffix() , new File(xMSet.getBoxDiagramName()));
    	}
  		catch(Exception e) {
  			do_error("Creating box diagram " + e.getMessage() + " " + xMSet.xU.LogStackTrace(e) );
  		}
  	}
  	//-----------------------------------------------------------------------
  	public void writeHistogramToFile(String sTipe , cmcProcColorHistogram h , int breedte , int hoogte)
  	//-----------------------------------------------------------------------
  	{
  	   try {
  			  BufferedImage bi = new BufferedImage( breedte , hoogte , BufferedImage.TYPE_INT_RGB);
  			  Graphics2D ig = bi.createGraphics();
  			  // witte achtergond
  			  Color pc = Color.WHITE;
  			  ig.setColor( new Color(pc.getRed(),pc.getGreen(),pc.getBlue()));
  			  ig.fillRect(0,0,breedte,hoogte);
  			  //
  			  if( sTipe.toUpperCase().trim().indexOf("ORIGINAL")>=0) {
  			    colorHistogram(ig,h,breedte,hoogte);
  			    ImageIO.write(bi, xMSet.getPreferredImageSuffix() , new File(xMSet.getHistoScreenShotDumpNameUncropped()));
  			  }
  			  if( sTipe.toUpperCase().trim().indexOf("GRAY")>=0) {
  				colorHistogram(ig,h,breedte,hoogte);
  				ImageIO.write(bi, xMSet.getPreferredImageSuffix() , new File(xMSet.getHistoScreenShotDumpNameGrayScale()));
  			  }
  			}
  			catch(Exception e) {
  				do_error("Writing histogram " + e.getMessage() + " " + xMSet.xU.LogStackTrace(e));
  			}
  	}
  	
  	//-----------------------------------------------------------------------
  	public Image waitImage( int w , int h , Color fg , Color bg)
    //-----------------------------------------------------------------------
  	{
  		try {
  	     BufferedImage bi = new BufferedImage( w , h , BufferedImage.TYPE_INT_RGB);
		 Graphics2D g = bi.createGraphics();
		 g.setColor( bg );
		 g.fillRect( 0 , 0 , w , h );
		 g.setColor( fg );
		 g.drawLine( 0 , 0 , w , h );
		 g.drawLine( w , 0 , 0 , h );
		 g.fillRoundRect( 0 , 0 , w , h , 5 , 5);
		 return bi;
  		}
  		catch( Exception e ) { return null; }
  	}
}
