package generalImagePurpose;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.Polygon;
import java.awt.image.BufferedImage;

public class cmcImagePrimitives {
	
	
	public cmcImagePrimitives()
	{
		
	}
	
	//-----------------------------------------------------------------------
  	public Image getTopLeftTriangle( int w , int h , Color fg , Color bg)
    //-----------------------------------------------------------------------
  	{
  		try {
  	     BufferedImage bi = new BufferedImage( w , h , BufferedImage.TYPE_INT_ARGB);
		 Graphics2D g = bi.createGraphics();
		 g.setColor( bg );
		 g.fillRect( 0 , 0 , w , h );
		 g.setColor( fg );
		 Polygon triangle1 = new Polygon();
	     triangle1.addPoint(0,0); 
	     triangle1.addPoint(w,0);
	     triangle1.addPoint(0,h);
	     g.fillPolygon(triangle1);
		 return bi;
  		}
  		catch( Exception e ) { return null; }
  	}

    //-----------------------------------------------------------------------
  	public Image getLeftTriangle( int w , int h , Color fg , Color bg)
    //-----------------------------------------------------------------------
  	{
  		try {
  	     BufferedImage bi = new BufferedImage( w , h , BufferedImage.TYPE_INT_ARGB);
		 Graphics2D g = bi.createGraphics();
		 g.setColor( bg );
		 g.fillRect( 0 , 0 , w , h );
		 g.setColor( fg );
		 
		 Polygon triangle1 = new Polygon();
	     triangle1.addPoint(w,0); 
	     triangle1.addPoint(w,h);
	     triangle1.addPoint(0,h/2);
	     g.fillPolygon(triangle1);
	 	 return bi;
  		}
  		catch( Exception e ) { return null; }
  	}
  	
    //-----------------------------------------------------------------------
  	public Image getRightTriangle( int w , int h , Color fg , Color bg)
    //-----------------------------------------------------------------------
  	{
  		try {
  	     BufferedImage bi = new BufferedImage( w , h , BufferedImage.TYPE_INT_ARGB);
		 Graphics2D g = bi.createGraphics();
		 g.setColor( bg );
		 g.fillRect( 0 , 0 , w , h );
		 g.setColor( fg );
		 
		 Polygon triangle1 = new Polygon();
	     triangle1.addPoint(0,0); 
	     triangle1.addPoint(w,h/2);
	     triangle1.addPoint(0,h);
	     g.fillPolygon(triangle1);
	 	 return bi;
  		}
  		catch( Exception e ) { return null; }
  	}
}
