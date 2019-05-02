package customWidgets;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.Point;
import java.awt.Polygon;
import java.util.ArrayList;

import javax.swing.JPanel;

public class cmcMetaDataPanel extends JPanel {
	
	class myshape
	{
		Point[] coordinates=null;
		Color color = Color.BLACK;
		myshape(int NumberOfCoordinates, Color c)
		{
			coordinates = new Point[ NumberOfCoordinates ];
			color = c;
		}
		boolean setCoordinate(int idx , int x , int y)
		{
			if( coordinates == null ) return false;
			if( (idx<0) || (idx>=coordinates.length) ) return false;
			coordinates[idx] = new Point( x , y);
			return true;
		}
	}
	ArrayList<myshape> list = null;
	
	public boolean horizontalLine( int x , int y , int w , Color c)
	{
		myshape shp = new myshape( 2 , c );
		if (shp.setCoordinate( 0 , x , y) == false ) return false;
		if (shp.setCoordinate( 1 , x+w , y) == false ) return false;
		list.add( shp );
		return true;
	}
	
	public void resetCanvas()
	{
		list=null;
		list = new ArrayList<myshape>();
	}
	
	
	
	public cmcMetaDataPanel()
	{
		super();
		list = new ArrayList<myshape>();
	}
	
	@Override
    public void paintComponent(Graphics g) {
        super.paintComponent(g);
        
        g.setColor( Color.WHITE );
        if ( list == null ) return;
        for(int i=0;i<list.size();i++)
        {
        	myshape shp = list.get(i);
        	if( shp == null ) continue;
        	if( shp.coordinates.length <=0 ) continue;
        	Polygon p = new Polygon();
        	for(int j=0;j<shp.coordinates.length;j++ ) p.addPoint( shp.coordinates[j].x , shp.coordinates[j].y );
        	g.setColor( shp.color );
        	g.drawPolygon(p);
        }
    }
	
	
}
