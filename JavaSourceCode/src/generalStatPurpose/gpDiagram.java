package generalStatPurpose;

import java.awt.Color;
import java.awt.Font;

import cbrTekStraktorModel.cmcProcConstants;
import cbrTekStraktorModel.cmcProcEnums;
import cbrTekStraktorModel.cmcProcSettings;
import drawing.cmcColor;
import drawing.cmcDrawPixelText;
import generalMachineLearning.LinearAlgebra.cmcMath;
import logger.logLiason;

public class gpDiagram {

	cmcProcSettings xMSet=null;
	logLiason logger=null;
	private cmcDrawPixelText drwtxt = null;
	private cmcColor colrout = null;
	private cmcMath mrout = null;
	
	private static int TILE_WIDTH     = 700;
	private static int TILE_HEIGTH    = 350;
	private static int RECTANGLE_SIDE = TILE_HEIGTH < TILE_WIDTH ? TILE_HEIGTH : TILE_WIDTH;
	private static int CORRELATION_LEVELS            = 16;
	private static double CORRELATION_SIDE_AMPLIFIER = 1.90;
	
	private int TileBackGround = cmcProcConstants.WIT;
	private Color TextColor = Color.BLACK;
	private int LineColor = cmcProcConstants.ZWART;
	private cmcProcEnums.SCATTER_DOT_TYPE ScatterDotType = cmcProcEnums.SCATTER_DOT_TYPE.RECTANGLE;
	private int ScatterDiameter = 3;
	private cmcProcEnums.ColorSentiment colorsentiment = cmcProcEnums.ColorSentiment.HARSH;
	private cmcProcEnums.ColorSentiment histocolorsentiment = cmcProcEnums.ColorSentiment.SOFT;
		
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
    public gpDiagram(cmcProcSettings is,logLiason ilog)
    //--------------------------
    {
      xMSet = is;
      logger = ilog;
  	  mrout = new cmcMath();
  	  colrout = new cmcColor();
	  drwtxt = new cmcDrawPixelText(xMSet,logger);
	  //
	  TextColor = colrout.getColor("Dark Slate Gray");
	  LineColor = colrout.getColorInt("Dark Slate Gray");
    }
    
   
    public void setBackGroundColor(Color colour)
    {
    	 TileBackGround = colour.getRGB();
    	 //drwtxt.setTextBackground(colour);
    }
    public Color getBackGroundColor()
    {
    	return new Color( TileBackGround );
    }
    public int getTileWidth()
    {
    	return TILE_WIDTH;
    }
    public int getTileHeigth()
    {
    	return TILE_HEIGTH;
    }
    public int getRectangleSide()
    {
    	return RECTANGLE_SIDE;
    }
    public int getCorrelationWidth()
    {
    	return (int)((double)RECTANGLE_SIDE * (double)CORRELATION_SIDE_AMPLIFIER);
    }
    public int getCorrelationHeigth()
    {
    	return (int)((double)getCorrelationWidth() * (double)0.6);
    }
    private int getTileHorizontalMargin()
    {
    	return TILE_WIDTH / 20;
    }
    private int getTileVerticalMargin()
    {
    	return TILE_HEIGTH / 20;
    }
    public cmcProcEnums.SCATTER_DOT_TYPE getScatterDotType()
    {
    	return ScatterDotType;
    }
    public void setScatterDotType( cmcProcEnums.SCATTER_DOT_TYPE tipe )
    {
    	ScatterDotType = tipe;
    }
    
    public void setScatterDotDiameter(int dia)
    {
    	int olddia = ScatterDiameter;
    	if( dia < 0 ) ScatterDiameter -=2;
    	else
    	if( dia > 100 ) ScatterDiameter += 2;
    	else ScatterDiameter = dia;
    	if( ScatterDiameter < 1 ) ScatterDiameter = olddia;
    	if( ScatterDiameter > 20) ScatterDiameter = olddia;
    	//do_log( 1 , "DIAMETER " + ScatterDiameter );
    }
    public cmcProcEnums.ColorSentiment getColorSentiment()
    {
    	return colorsentiment;
    }
    public void setColorSentiment(cmcProcEnums.ColorSentiment cos)
    {
    	colorsentiment = cos;
    }
    //------------------------------------------------------------
    private int pickColor( int j , cmcProcEnums.ColorSentiment sentiment  )
    //------------------------------------------------------------
    {
    	   int color = cmcProcConstants.WIT;
    	   if( sentiment == cmcProcEnums.ColorSentiment.SOFT )
    	   {
	        switch( j % 5)
	        {
	         case 0 :   { color = colrout.getColorInt("Dim Gray"); break; }
	         case 1 :   { color = colrout.getColorInt("Dark Sea Green"); break; }
	         case 2 :   { color = colrout.getColorInt("Cadet Blue"); break; }
	         case 3 :   { color = colrout.getColorInt("Slate Gray"); break; }
	         default :  { color = colrout.getColorInt("Maroon"); break; }
	        }
    	   }
    	   else
    	   if( sentiment == cmcProcEnums.ColorSentiment.HARSH )
           {
    	     switch( j % 5)
    	     {
    	      case 0 :   { color = colrout.getColorInt("Red"); break; }
    	      case 1 :   { color = colrout.getColorInt("Orange"); break; }
    	      case 2 :   { color = colrout.getColorInt("Blue"); break; }
    	      case 3 :   { color = colrout.getColorInt("Pink"); break; }
    	      default :  { color = colrout.getColorInt("FireBrick"); break; }
    	     }
           }   
    	   else
           if( sentiment == cmcProcEnums.ColorSentiment.RED )
           {
        	 switch( j % 10)
        	 {
        	  case 0 :   { color = colrout.getColorInt("deep pink"); break; }
        	  case 8 :   { color = colrout.getColorInt("medium orchid"); break; }
        	  case 2 :   { color = colrout.getColorInt("orchid"); break; }
        	  case 7 :   { color = colrout.getColorInt("violet"); break; }
        	  case 4 :   { color = colrout.getColorInt("plum"); break; }
        	  case 5 :   { color = colrout.getColorInt("thistle"); break; }
        	  case 6 :   { color = colrout.getColorInt("pink"); break; }
        	  case 3 :   { color = colrout.getColorInt("rosy brown"); break; }
        	  case 1 :   { color = colrout.getColorInt("medium violet red"); break; }
        	  default :  { color = colrout.getColorInt("hot pink"); break; }
        	 }
           }   
           else
           if( sentiment == cmcProcEnums.ColorSentiment.BLUE )
           {
             switch( j % 10)
             {
              case 0 :   { color = colrout.getColorInt("powder blue"); break; }
              case 8 :   { color = colrout.getColorInt("light steel blue"); break; }
              case 2 :   { color = colrout.getColorInt("light sky blue"); break; }
              case 7 :   { color = colrout.getColorInt("dodger blue"); break; }
              case 4 :   { color = colrout.getColorInt("royal blue"); break; }
              case 5 :   { color = colrout.getColorInt("aquamarine"); break; }
              case 6 :   { color = colrout.getColorInt("dark turquoise"); break; }
              case 3 :   { color = colrout.getColorInt("aqua"); break; }
              case 1 :   { color = colrout.getColorInt("steel blue"); break; }
              default :  { color = colrout.getColorInt("blue violet"); break; }
             }
           }   
    	   else
    	   if( sentiment == cmcProcEnums.ColorSentiment.GREEN )
           {
             switch( j % 10)
             {
              case 0 :   { color = colrout.getColorInt("forest green"); break; }
              case 8 :   { color = colrout.getColorInt("lime green"); break; }
              case 2 :   { color = colrout.getColorInt("gold"); break; }
              case 7 :   { color = colrout.getColorInt("olive drab"); break; }
              case 4 :   { color = colrout.getColorInt("sea green"); break; }
              case 5 :   { color = colrout.getColorInt("dark sea green"); break; }
              case 6 :   { color = colrout.getColorInt("pale green"); break; }
              case 3 :   { color = colrout.getColorInt("light green"); break; }
              case 1 :   { color = colrout.getColorInt("dark green"); break; }
              default :  { color = colrout.getColorInt("yellow green"); break; }
             }
           }   
    	   else { do_error("unsupported color sentiment " + sentiment ); }
    	   if( color == colrout.getDefaultColorInt() ) color = colrout.getColorInt("yellow");
	       return color;
    }
    //------------------------------------------------------------
    private void doDot( int[]pix , int width , int x , int y , int color)
    //------------------------------------------------------------
    {
    	pix[ x + (y * width)] = color;
    	return;
    }
    //------------------------------------------------------------
    private void doVerticalBar( int[] pix , int width , int x , int y , int w , int h , int color)
    //------------------------------------------------------------
    {
    	for(int i=0;i<=w;i++)
    	{
    		doVerticalLine( pix , width  , (x+i) , y , (y - h) , color);
    	}
    }
    //------------------------------------------------------------
    private void doLine( int[]pix , int width , int x0 , int y0 , int x1 , int y1 , int color)
    //------------------------------------------------------------
    {
    	if( x0==x1 ) doVerticalLine( pix , width , x0 , y0 , y1 , color);
    	else
        if( y0==y1 ) doHorizontalLine( pix , width , y0 , x0 , x1 , color);
        // todo angle	
    	return;
    }
    //------------------------------------------------------------
    private void doVerticalLine( int[] pix , int width , int x0 , int y1 , int y2 , int color)
    //------------------------------------------------------------
    {
       int Y1 = ( y1 < y2 ) ? y1 : y2;
       int Y2 = ( y1 < y2 ) ? y2 : y1;
       for(int y=Y1 ; y<=Y2 ; y++) pix[ x0 + (y * width)] = color;
    }
    //------------------------------------------------------------
    private void doHorizontalLine( int[] pix , int width , int y0 , int x1 , int x2 , int color)
    //------------------------------------------------------------
    {
    	int X1 = ( x1 < x2 ) ? x1 : x2;
    	int X2 = ( x1 < x2 ) ? x2 : x1;
    	int y = y0 * width;
    	for(int x=X1; x<=X2 ; x++) pix[ y + x ] = color;
    }
    //------------------------------------------------------------
    private void drawFilledRectangleFromCenter( int[] pix , int width , int x1 , int y1 , int size , int color)
    //------------------------------------------------------------
    {
        int vsize = mrout.isEven( size ) ? size + 1 : size;
        for(int y=0 ; y<vsize ; y++)
        {
        	int k = ( ( y1 - (vsize/2) + y ) * width ) + ( x1 - (vsize/2) ) - 1;
        	for(int x=0;x<vsize;x++)
        	{
        		k++;
        		if( (k<0) || (k>=pix.length)) continue;
        		pix[ k ] = color;
        	}
        } 	
    }
    //------------------------------------------------------------
    private void drawRectangleFromCenter( int[] pix , int width , int x1 , int y1 , int size , int color)
    //------------------------------------------------------------
    {
            int vsize = mrout.isEven( size ) ? size + 1 : size;
            int h = pix.length / width;
        	int x = x1 - (vsize/2); if( x < 0) x=0;
        	int y = y1 - (vsize/2); if( y < 0) y=0;
        	int z = x + vsize; if( z >= width) z=width-1;
        	doHorizontalLine( pix , width , y , x , z , color);
        	y += vsize;  if( y >= h ) y = h-1;
        	doHorizontalLine( pix , width , y , x , z , color);
        
        	x = x1 - (vsize/2); if( x < 0) x=0;
        	y = y1 - (vsize/2); if( y < 0) y=0;
            z = y + vsize; if( z >= h ) z = h-1;
            doVerticalLine( pix , width , x , y , z , color);
            x += vsize; if( x >= width) x=width-1;
            doVerticalLine( pix , width , x , y , z , color);
    }
    //------------------------------------------------------------
    private void drawCircleFromCenter( int[] pix , int width , int x1 , int y1 , int diameter , int color)
    //------------------------------------------------------------
    {
        int h = pix.length / width;
    	double ddia  =   mrout.isEven( diameter ) ? diameter+1: diameter;
    	double radius = (double)ddia / 2;
    	int steps = (int)(radius * 2);
    	double stepsize = radius / (double)steps;
    	double dx=0;
    	int[] xprev = new int[4];  // track previous x an y in order to prevent different y for same x => single edge circle
    	int[] yprev = new int[4];
    	for(int i=0;i<steps+1;i++)
    	{
    	    double dy = Math.sqrt((radius * radius) - (dx*dx));
    	    int idy = (int)Math.round(dy);
    	    int idx = (int)Math.round(dx);
    	 
    		int xco = x1 - idx; if( xco < 0) xco=0;
    		int yco = y1 - idy; if( yco < 0) yco=0;
    		if( (i != 0) && (xco == xprev[0]) && (yco != yprev[0]) ) yco = yprev[0];
    		pix[ (yco * width) + xco ] = color;
    		xprev[0] = xco;
    		yprev[0] = yco;
    		//
    		yco = y1 + idy; if( yco > h) yco=h;
    		if( (i != 0) && (xco == xprev[1]) && (yco != yprev[1]) ) yco = yprev[1];
    	    pix[ (yco * width) + xco ] = color;
    		xprev[1] = xco;
    		yprev[1] = yco;
    		//
    	    xco = x1 + idx; if (xco > width) xco = width;
    	    if( (i != 0) && (xco == xprev[2]) && (yco != yprev[2]) ) yco = yprev[2];
    	    pix[ (yco * width) + xco ] = color;
    		xprev[2] = xco;
    		yprev[2] = yco;
    	    //
    	    yco = y1 - idy; if( yco < 0) yco=0;
    	    if( (i != 0) && (xco == xprev[3]) && (yco != yprev[3]) ) yco = yprev[3];
    	    pix[ (yco * width) + xco ] = color;
    		xprev[3] = xco;
    		yprev[3] = yco;
    	    //
    		dx += stepsize;
    		if( stepsize > radius) break;
    	}
    }
    //------------------------------------------------------------
    private void drawCrossFromCenter( int[] pix , int width , int x1 , int y1 , int diameter , int color)
    //------------------------------------------------------------
    {
        int h = pix.length / width;
    	double ddia  =   mrout.isEven( diameter ) ? diameter+1: diameter;
    	double radius = (double)ddia / 2;
    	
    	int x = x1 - (int)radius; if( x < 0) x=0;
    	int y = y1 - (int)radius; if( y < 0) y=0;
    	int x3 = x+diameter; if( x >= width) x=width-1;
    	int y3 = y+diameter; if( y>=h) y=h-1;
    	doLine( pix , width , x , y1 , x3 , y1 , color);
    	doLine( pix , width , x1 , y , x1 , y3 , color);
    	
    }
    //------------------------------------------------------------
    public int[] makeHistogram( gpDiagramDTO dto , boolean squared)
    //------------------------------------------------------------
    {
    	int diagram_width = TILE_WIDTH;
    	int diagram_heigth = TILE_HEIGTH;
    	int bargap = 10;
    	cmcProcEnums.ColorSentiment barcolor = histocolorsentiment;
    	if( squared ) {
    		diagram_width = diagram_heigth = RECTANGLE_SIDE;
    		bargap = 0;
    		barcolor = colorsentiment;
    	}
    	//  
    	try  {
    	  if( dto == null ) return null;
    	  // SUBHISTO = [  number of bins ] [ number of classes ]
     	  if( dto.getHisto() == null ) return null;
     	  int nbins = dto.getHisto().length;
     	  if( nbins < 0 ) { do_error( "Empty"); return null; }
     	  int nclasses = dto.getClassNameList().length;
     	  int sampleTotal = 0;
     	  for( int i=0; i<dto.getHisto().length ; i++)
     	  {
     		 if( dto.getHisto()[i].length != nclasses ) { do_error("Number of classes differs"); return null; }
     		 for(int j=0;j<nclasses;j++) sampleTotal += dto.getHisto()[i][j];
     	  }
     	 
    	  //
    	  int[] tile = new int[ diagram_heigth * diagram_width ];
    	  for(int i=0;i<tile.length;i++) tile[i] = TileBackGround;
    	  int histoWidth = diagram_width - ( 2 * getTileHorizontalMargin());
    	  int histoHeigth = diagram_heigth - ( 2 * getTileVerticalMargin());
    	
    	  Font f = xMSet.getPreferredFont();
    	  drwtxt.drawPixelText(f, TextColor, 5 , 5 , tile, diagram_width, dto.getxLabel() );
    	  // Axis
    	  doLine( tile , diagram_width , 0 , histoHeigth , histoWidth , histoHeigth , LineColor );
    	 
    	  // width of the histobar
    	  int bWidthWide =  histoWidth / nbins;
    	  int bWidthSmall = bWidthWide - bargap;   
    	  // 
          int categoryMax=0;
          for(int i=0;i<dto.getHisto().length;i++)
          {
        	  int subMax=0;
        	  for(int j=0;j<dto.getHisto()[i].length;j++) subMax += dto.getHisto()[i][j];
        	  if( subMax > categoryMax) categoryMax = subMax;
          }
          categoryMax = (int)((double)categoryMax * (double)1.25);   // take a bigger portion
          int quarter = (int)((double)histoHeigth/ (double)5);
          int divi=quarter;
          for(int i=1;i<5;i++)
          {
        	  doLine( tile , diagram_width , 0 , histoHeigth-divi , histoWidth , histoHeigth-divi , LineColor ); 
        	  divi += quarter;
        	  if( quarter >= histoHeigth)break;
          }
          //
          int minX=-1;
          int minY=100000;
          int minVal=-1;
    	  for(int z=0 ; z<nbins ; z++)
    	  {
    		int barX = (z * bWidthWide ) + (bargap/2);
    	    doVerticalLine( tile , diagram_width , barX + (bWidthSmall/2), histoHeigth , histoHeigth+5 ,  LineColor);	  
    	    int hoffset=0;
    	    for(int j=0;j<nclasses;j++)
    	    {
    	       double dh = ( (double)dto.getHisto()[z][j] / (double)categoryMax ) * histoHeigth;
    	       if( dh > (double)histoHeigth ) dh = histoHeigth;
    	       if( dh < 1 ) continue;
    	       doVerticalBar( tile , diagram_width , barX , histoHeigth - hoffset , bWidthSmall , (int)dh , pickColor(j,barcolor) );
    	       hoffset += (int)dh;
    	       if( (histoHeigth - hoffset) < minY )  {
    	    	   minY = histoHeigth - hoffset;
    	    	   //minX = barX;
    	    	   minX = barX + ((bWidthSmall+bargap)/2);
    	    	   minVal = z;
    	       }
    	    }
    	  }
    	  int subtot=0;
    	  for(int j=0;j<nclasses;j++) subtot += dto.getHisto()[minVal][j];
    	  subtot =   (int)((double)subtot * 100 / (double)sampleTotal);
    	  minY -= 15;
    	  if( minY > 0) drwtxt.drawPixelTextCenter(f, TextColor, minX  , minY + 5 , tile , diagram_width , "%"+subtot );
    	  
    	  // move everything to the right and down
    	  return tile;
    	}
    	catch( Exception e) {
    		do_error("makeHistogram - Oops " + xMSet.xU.LogStackTrace(e));
    		return null;
    	}
    }
    
    //------------------------------------------------------------
    public int[] drawScatterPlot( gpDiagramDTO dto )
    //------------------------------------------------------------
    {
    	 if( dto.getXvals() == null ) return null;
    	 if( dto.getYvals() == null ) return null;
    	 if( dto.getResults() == null ) return null;
    	 if( dto.getXvals().length != dto.getYvals().length ) return null;
    	 if( dto.getXvals().length != dto.getResults().length ) return null;
    	 int nvals = dto.getXvals().length;
    	 //
    	 int[] tile = new int[ RECTANGLE_SIDE * RECTANGLE_SIDE ];
  	     for(int i=0;i<tile.length;i++) tile[i] = TileBackGround;
  	     int horzMargin = (int)((double)RECTANGLE_SIDE * 0.12);
  	     int vertMargin = (int)((double)RECTANGLE_SIDE * 0.12);
  	     int scatterWidth  = RECTANGLE_SIDE - (2 * horzMargin);
  	     int scatterHeigth = RECTANGLE_SIDE - (2 * vertMargin);
  	     if( scatterWidth > scatterHeigth ) scatterWidth = scatterHeigth; 
  	                                   else scatterHeigth = scatterWidth;
  	     // rescale so that it fits
  	     double minx = Double.NaN;
  	     double maxx = Double.NaN;
  	     double miny = Double.NaN;
	     double maxy = Double.NaN;
	     for(int i=0;i<nvals;i++)
	     {
	    	 if( i == 0 )  {
	    		 minx = maxx = dto.getXvals()[i];
	    		 miny = maxy = dto.getYvals()[i];
	    	 }
	    	 if( minx > dto.getXvals()[i] ) minx = dto.getXvals()[i];
	    	 if( maxx < dto.getXvals()[i] ) maxx = dto.getXvals()[i];
	    	 if( miny > dto.getYvals()[i] ) miny = dto.getYvals()[i];
	    	 if( maxy < dto.getYvals()[i] ) maxy = dto.getYvals()[i];
	     }
	     double spanx = maxx - minx;
	     double spany = maxy - miny;
	     //
  	     Font f = xMSet.getPreferredFont();
  	     for(int i=0;i<nvals;i++)
	     {
  	    	 double xrescale = (dto.getXvals()[i] - minx) / spanx;
  	    	 double yrescale = (dto.getYvals()[i] - miny) / spany;
  	     	 int x = (int)Math.round( xrescale * scatterWidth) + horzMargin;
	    	 int y = RECTANGLE_SIDE - ((int)Math.round( yrescale * scatterHeigth) + vertMargin);  // Y0 is top of page
	  
	    	 //int x = (int)Math.round(dto.getXvals()[i] * scatterWidth) + horzMargin;
	    	 //int y = RECTANGLE_SIDE - ((int)Math.round(dto.getYvals()[i] * scatterHeigth) + vertMargin);  // Y0 is top of page
	    	 try {
	    	  switch( ScatterDotType )
	    	  {
	    	  case FILLED_RECTANGLE : {  drawFilledRectangleFromCenter( tile , RECTANGLE_SIDE , x , y , ScatterDiameter , pickColor( (int)dto.getResults()[i] , colorsentiment ) ); break; }
	 	      case RECTANGLE : { drawRectangleFromCenter( tile , RECTANGLE_SIDE , x , y , ScatterDiameter , pickColor( (int)dto.getResults()[i] , colorsentiment) ); break;}
	    	  case CIRCLE : {  drawCircleFromCenter( tile , RECTANGLE_SIDE , x , y , ScatterDiameter , pickColor( (int)dto.getResults()[i] , colorsentiment) ); break; }
	    	  case CROSS : {  drawCrossFromCenter( tile , RECTANGLE_SIDE , x , y , ScatterDiameter , pickColor( (int)dto.getResults()[i] , colorsentiment) ); break; }
		      default : { do_error("Unsupported scatter type"); return null; }
	    	  }
	    	 }
	    	 catch(Exception e) {
	    	   do_log( 1 , "OOB xd" + dto.getXvals()[i] + " yd" + dto.getYvals()[i] + " x" + x + " y" + y );
	    	 }
	     }
    	
	     // Border
	     drawRectangleFromCenter( tile  , RECTANGLE_SIDE , horzMargin + (scatterWidth/2) , vertMargin + (scatterHeigth/2) , scatterWidth + vertMargin , LineColor );
	     drwtxt.drawPixelTextCenter(f, TextColor, RECTANGLE_SIDE/2 , RECTANGLE_SIDE - 10 , tile, RECTANGLE_SIDE , dto.getxLabel() );
		 drwtxt.drawPixelTextCenterFlip(f, TextColor, 10 , RECTANGLE_SIDE/2 , tile, RECTANGLE_SIDE , dto.getyLabel() );
		 
	     // Ticks
	     // todo
		 
		 // curve requested
		 double[][] curve = dto.getDoubleArray();
		 if( curve != null ) {
			 //do_log( 1 , "Got a curve request " + curve.length );
		     if( curve[0].length == 2 ) {
		       for(int i=0;i< curve.length; i++ )
		       {
		    	 try {
		   	   	   //int x = (int)Math.round( curve[i][0] * scatterWidth) + horzMargin;
		    	   //int y = RECTANGLE_SIDE - ((int)Math.round( curve[i][1] * scatterHeigth) + vertMargin);  // Y0 is top of page
		    	   //do_log( 1 , "" + curve[i][0] + " " + curve[i][1] + " " + x + " " + y);
		    	   
		    	   double xrescale = (curve[i][0] - minx) / spanx;
		  	       double yrescale = (curve[i][1] - miny) / spany;
		  	       int x = (int)Math.round( xrescale * scatterWidth) + horzMargin;
			       int y = RECTANGLE_SIDE - ((int)Math.round( yrescale * scatterHeigth) + vertMargin);  // Y0 is top of page
			     
		    	   doDot( tile , RECTANGLE_SIDE , x , y , cmcProcConstants.ZWART );
		    	 }
		    	 catch( Exception e) { ; } 
		       }
		      }
		 }
	     //		 
    	 return tile;
    }
    //------------------------------------------------------------
    private int getShade( Color basecolor , int step , int totalNumberOfLevels)
    //------------------------------------------------------------
    {
    	int rgb       = basecolor.getRGB();
    	int red       =  (rgb>>16) & 0xff;
    	int green     =  (rgb>>8) & 0xff;
    	int blue      =  rgb & 0xff;
    	float[] hsb = Color.RGBtoHSB( red , green , blue , null);
    	float hue = hsb[0];
    	float saturation = hsb[1];
    	float brightness = hsb[2];
    	//
    	saturation =  (saturation / (float)(totalNumberOfLevels+1)) * (float)(step+1);
    	//hue =  (hue / (float)(totalNumberOfLevels+1)) * (float)(step+1);
    	//brightness =  (brightness / (float)(totalNumberOfLevels+1)) * (float)(step+1);
        rgb = Color.HSBtoRGB(hue, saturation, brightness);
    	return rgb;
    	
    }
    //------------------------------------------------------------
    public int[] drawCorrelationDiagram( gpDiagramDTO dto )
    //------------------------------------------------------------
    {
    	int CORRELATION_WIDTH = getCorrelationWidth();
    	int CORRELATION_HEIGTH = getCorrelationHeigth();
    	Color drawColor = Color.BLUE;
    	
    	double[][] dvals = dto.getDoubleArray();
    	if( dvals == null ) return null;
    	int ndims = dvals.length;
    	if( ndims < 1) return null;
    	if( ndims != dvals[0].length ) return null;
    	boolean showVerticalLabels = true;
   		String[] vertLabels = dto.getVerticalTickLabels();
   		if( vertLabels == null ) showVerticalLabels=false;
   		if( vertLabels.length != ndims ) showVerticalLabels=false;
   		
   	    //
    	double portion = 0.12;
   	    int[] tile = new int[ CORRELATION_WIDTH * CORRELATION_HEIGTH ];
 	    for(int i=0;i<tile.length;i++) tile[i] = TileBackGround;
 	    int horzMargin = (int)((double)CORRELATION_WIDTH * portion);
 	    int vertMargin = (int)((double)CORRELATION_HEIGTH * portion);
 	    int scatterWidth  = CORRELATION_WIDTH - (2 * horzMargin);
 	    int scatterHeigth = CORRELATION_HEIGTH - (2 * vertMargin);
 	    if( scatterWidth > scatterHeigth ) scatterWidth = scatterHeigth; 
 	                                  else scatterHeigth = scatterWidth;
 	     
 	    Font f = xMSet.getPreferredFont();
 	    double dmin = Double.NaN;
 	    double dmax = Double.NaN;
 	    for(int i=0;i<ndims;i++)
 	    {
 	    	for(int j=0;j<ndims;j++)
 	    	{
 	    	  if( Double.isNaN( dvals[i][j]) ) continue;
 	    	  if( Double.isNaN(dmin)) dmin = dmax = dvals[i][j];
 	    	  if( dmin > dvals[i][j] ) dmin = dvals[i][j];
 	    	  if( dmax < dvals[i][j] ) dmax = dvals[i][j];
	    	}
 	    }
 	    do_log( 1 , "Min=" + dmin + " Max=" + dmax );  
 	    double spread = dmax - dmin;
 	    if( spread == 0 ) { do_error( "There is no difference between minimum and maximum - No numerics?") ; return  null; }
 	    double[][] dscore = new double[ ndims ][ ndims ];
 	    for(int i=0;i<ndims;i++)
	    {
	    	for(int j=0;j<ndims;j++)
	    	{
	    	  if( Double.isNaN( dvals[i][j]) ) { dscore[i][j] = Double.NaN; continue; }
	    	  dscore[i][j] = Math.round(  (( dvals[i][j] - dmin ) / spread) * CORRELATION_LEVELS );
	    	}
	    }
 	    //
 	    /*
 	    String sl="";
 	    for(int i=0;i<ndims;i++)
	    {
 	    	sl += "dim=" + i + "   -> ";
 	    	for(int j=0;j<ndims;j++) sl += "[" + dscore[i][j] + "]";
 	    	sl += "\n";
	    	
	    }
 	    do_log( 1 , sl );
 	    */
 	    //
 	    int step = (int)(scatterWidth / ndims);
 	    int x = 0 - step;
 	    for(int i=0;i<ndims;i++)
	    {
	    	x += step;
	    	int y = 0 - step;
	    	for(int j=0;j<ndims;j++)
	    	{
	    		y += step;
	    		int shade = cmcProcConstants.ZWART;
	    		if( Double.isNaN(dscore[i][j]) ) shade = cmcProcConstants.WIT;
	    		else shade = getShade( drawColor ,(int)dscore[i][j] , CORRELATION_LEVELS);
	    		
	    		try {
	    		  doVerticalBar( tile , CORRELATION_WIDTH , x + horzMargin ,  y+step+vertMargin , (step-2) , (step-2) , shade );
	    		  if((i == 0) && (showVerticalLabels)) drwtxt.drawPixelText(f, TextColor, horzMargin + scatterWidth + 8  , y+(step/2)+vertMargin  , tile , CORRELATION_WIDTH , ""+j + " " + vertLabels[j]);
	    		}
	    		catch(Exception e ) {
	    			do_error( "oops" + e.getMessage() );
	    		}
	    	}
	 		drwtxt.drawPixelTextCenterFlip(f, TextColor, horzMargin + x + (step/2) , vertMargin + scatterHeigth + (step/2)  , tile , CORRELATION_WIDTH , ""+i );
	 		    
	    }
 	    //doHorizontalLine( tile , CORRELATION_WIDTH , 0 , 0 , CORRELATION_WIDTH-1 , cmcProcConstants.WIT);
	    //doHorizontalLine( tile , CORRELATION_WIDTH , CORRELATION_HEIGTH-1 , 0 , CORRELATION_WIDTH-1 , cmcProcConstants.WIT);
 	   
	    // shaded bar
	    step = (int)(Math.round((double)scatterHeigth / (double)(CORRELATION_LEVELS)));
	    int y = 0 - step;
	    for(int i=0;i<CORRELATION_LEVELS;i++)
	    {
	      y += step;
	      int shade = getShade( drawColor , CORRELATION_LEVELS - i , CORRELATION_LEVELS);
  		  doVerticalBar( tile , CORRELATION_WIDTH , 2 ,  y+step+vertMargin , 20 , step , shade );
  		  if( (i== 0) || (i==(CORRELATION_LEVELS-1))) {
  		   drwtxt.drawPixelText(f, TextColor, 25  ,  y+vertMargin+(step/2)  , tile , CORRELATION_WIDTH , (i==0)?"Hi":"Lo");
  		  }
  	    }
    	return tile;
    }
}
