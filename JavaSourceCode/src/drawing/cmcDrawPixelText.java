package drawing;

import java.awt.Color;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Image;
import java.awt.font.TextLayout;
import java.awt.geom.Rectangle2D;
import java.awt.image.BufferedImage;
import java.awt.image.PixelGrabber;
//import java.util.HashMap;
//import java.util.Map;

import logger.logLiason;
import cbrTekStraktorModel.cmcProcSettings;

public class cmcDrawPixelText {
	
	cmcProcSettings xMSet=null;
	logLiason logger=null;
	
	private static int BACKGROUNDMARK = 0xabffffff;
	
	private FontMetrics metrics;
    private Color foregroundColor;
    private int txtBreedte=-1;
	private int txtHoogte=-1;
	private Color txtBackGround = Color.WHITE;
	
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
    
	public cmcDrawPixelText(cmcProcSettings is,logLiason ilog)
	{
		xMSet = is;
		logger=ilog;
	}
	
    //---------------------------------------------------------------------------------
	private void setFont (Font f, Color col) 
    //---------------------------------------------------------------------------------
	{
		BufferedImage bi = new BufferedImage( 100 , 100 , BufferedImage.TYPE_INT_RGB);
		Graphics g = bi.getGraphics();
	    g.setFont(f);    
		metrics = g.getFontMetrics();
        foregroundColor = col;
    }

    //---------------------------------------------------------------------------------
    private int[] getPixelText(String sIn)
    //---------------------------------------------------------------------------------
    {
    	try {
    	Rectangle2D bounds = new TextLayout( sIn, metrics.getFont(), metrics.getFontRenderContext()).getOutline(null).getBounds();
        if(bounds.getWidth() == 0 || bounds.getHeight() == 0) {
            return null;
        }
        Image img = new BufferedImage((int)bounds.getWidth()+10, (int)bounds.getHeight(), BufferedImage.TYPE_INT_RGB);
        txtBreedte = img.getWidth(null);
 	    txtHoogte  = img.getHeight(null);
 	    //
        Graphics g = img.getGraphics();
        g.setColor( txtBackGround );
        g.fillRect(0,0,txtBreedte,txtHoogte);
        g.setColor(foregroundColor);
        g.setFont(metrics.getFont());
        g.drawString(sIn, 0, (int)(bounds.getHeight() - bounds.getMaxY()));
   	    //
 	    if( (txtBreedte < 0) || (txtHoogte <0) ) {
 		   do_error("Height or width cannot be determined of string [" + sIn + "]");
 		   return null;
 	    }
 	    int[] srcpixels = new int[ txtHoogte * txtBreedte];
 	    try {
 		   PixelGrabber pg = new PixelGrabber( img , 0 , 0 , txtBreedte , txtHoogte , srcpixels , 0 , txtBreedte);
 		   pg.grabPixels();
 	    }
 	    catch (Exception e ) {
 		   do_error("Pixelgrabber on [" + sIn + "] " + e.getMessage());
 		   return null;
 	    }
 	    img=null;
 	    // mark backgound
 	    int rgb = foregroundColor.getRGB() & 0x00ffffff;
 	    for(int i=0;i<srcpixels.length;i++)
 	    {
 	    	if( (srcpixels[i] & 0x00ffffff) != rgb ) srcpixels[i] = BACKGROUNDMARK;
 	    }
 	    // 
        return srcpixels;
    	}
    	catch(Exception e) {
    		 do_error("getPixelText on [" + sIn + "] " + e.getMessage());
    		 return null;
    	}
    }
    
    //---------------------------------------------------------------------------------
  	private void pasteSnippet( int x, int y , int canvasbreedte , int[] snippet , int[] canvas)
  	//---------------------------------------------------------------------------------
  	{
  		try {
  		  for(int i=0;i<txtHoogte;i++)
  		  {
  			for(int j=0;j<txtBreedte;j++)
  			{
  				int q = snippet[(txtBreedte*i)+j];
  				if( q == BACKGROUNDMARK ) continue; 
  				int p = ((y + i)*canvasbreedte) + x + j;
  				if( p < canvas.length ) canvas[p] = q;
  			}
  		  }
  		  snippet=null;
  		}
  		catch(Exception e ) {
  			do_error("(pasteSnippet)" + e.getMessage());
  		}
  	}
  	
    //---------------------------------------------------------------------------------
  	private int[] flipImage( int[] pix )
    //---------------------------------------------------------------------------------
  	{
  		try {
          int[] pox = new int[ pix.length ];
          int p = -1;
          int k = -1;
          for(int y=0;y<txtHoogte;y++)
          {
        	  for(int x=0;x<txtBreedte;x++)
        	  {
        		  p = pix[ x + (txtBreedte*y)];
        		  //k =  ((x + 1) * txtHoogte) - y - 1;   // + 90 graden of naar rechts
        		  k =  ((txtBreedte - 1 - x) * txtHoogte) + y;  // -90 graden of naar links
        		  pox[ k ] = p;
        	  }
          }
          k = txtBreedte;
          txtBreedte=txtHoogte;
          txtHoogte=k;
          return pox;
  		}
  		catch(Exception e ) {
  			do_error( "flip " + e.getMessage()) ;
  			return null;
  		}
  	}
  	
    //---------------------------------------------------------------------------------
  	public void drawPixelText(Font f , Color col , int x , int y , int[]pixelcanvas , int canvasWidth , String sIn , boolean flip)
    //---------------------------------------------------------------------------------
  	{
  		txtBreedte=-1;
  		txtHoogte=-1;
  	    setFont(f,col);
  	    int[] txtpixels = getPixelText(sIn);
  	    if( txtpixels == null )  return;
  	    if( flip ) {
  	    	txtpixels = flipImage( txtpixels );
  	    	if( txtpixels == null ) return;
  	    }
  	    pasteSnippet( x , y , canvasWidth , txtpixels , pixelcanvas );    
  	    txtpixels=null;
  	    return;
  	}
  	
    //---------------------------------------------------------------------------------
	public void drawPixelText(Font f , Color col , int x , int y , int[]pixelcanvas , int canvasWidth , String sIn)
    //---------------------------------------------------------------------------------
	{
		drawPixelText( f , col , x , y , pixelcanvas , canvasWidth , sIn , false);
	}
	//---------------------------------------------------------------------------------
	public void drawPixelTextFlip(Font f , Color col , int x , int y , int[]pixelcanvas , int canvasWidth , String sIn)
	//---------------------------------------------------------------------------------
	{
		drawPixelText( f , col , x , y , pixelcanvas , canvasWidth , sIn , true);
	}
	//---------------------------------------------------------------------------------
	public void drawPixelTextCenter(Font f , Color col , int x , int y , int[]pixelcanvas , int canvasWidth , String sIn)
	//---------------------------------------------------------------------------------
	{
			txtBreedte=-1;
			txtHoogte=-1;
		    setFont(f,col);
		    int[] txtpixels = getPixelText(sIn);
		    if( txtpixels == null )  return;
		    int x1 = (int)((double)x - ((double)txtBreedte / 2));
		    int y1 = (int)((double)y - ((double)txtHoogte / 2));
		    txtpixels=null;
		    drawPixelText( f , col , x1 , y1 , pixelcanvas , canvasWidth , sIn );
	}
	//---------------------------------------------------------------------------------
	public void drawPixelTextCenterFlip(Font f , Color col , int x , int y , int[]pixelcanvas , int canvasWidth , String sIn)
	//---------------------------------------------------------------------------------
	{
				txtBreedte=-1;
				txtHoogte=-1;
			    setFont(f,col);
			    int[] txtpixels = getPixelText(sIn);
			    if( txtpixels == null )  return;
			    int x1 = (int)((double)x - ((double)txtHoogte / 2));
			    int y1 = (int)((double)y - ((double)txtBreedte / 2));
			    txtpixels=null;
			    drawPixelTextFlip( f , col , x1 , y1 , pixelcanvas , canvasWidth , sIn );
	}
	/*
    //---------------------------------------------------------------------------------
	public void drawPixelText(Font f , Color col , int x , int y , int[]pixelcanvas , int canvasWidth , String sIn , Color bg)
    //---------------------------------------------------------------------------------
	{
	  Color oldbg = this.txtBackGround;
	  txtBackGround = bg;
	  drawPixelText(f , col , x , y , pixelcanvas , canvasWidth , sIn);		
	  this.txtBackGround = oldbg;
	}		
	
	//---------------------------------------------------------------------------------
	public void drawPixelTextCenter(Font f , Color col , int x , int y , int[]pixelcanvas , int canvasWidth , String sIn , Color bg)
    //---------------------------------------------------------------------------------
	{
		  Color oldbg = this.txtBackGround;
		  txtBackGround = bg;
		  drawPixelTextCenter(f , col , x , y , pixelcanvas , canvasWidth , sIn);		
		  this.txtBackGround = oldbg;
	}
	*/
	/*
	public void setTextBackground(Color bg)
	{
		 txtBackGround = bg;	
	}
	*/
}
