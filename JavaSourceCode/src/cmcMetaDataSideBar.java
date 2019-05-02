import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Rectangle;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.image.BufferedImage;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.UIManager;

import cbrTekStraktorModel.cmcDrawHistogram;
import cbrTekStraktorModel.cmcProcConstants;
import cbrTekStraktorModel.cmcProcEnums;
import cbrTekStraktorModel.cmcProcSettings;
import customWidgets.cmcMetaDataPanel;
import generalImagePurpose.cmcImagePrimitives;
import generalImagePurpose.gpFetchByteImageData;
import generalImagePurpose.gpMakeThumbNail;
import imageProcessing.cmcProcColorHistogram;
import logger.logLiason;


public class cmcMetaDataSideBar {
	
	cmcProcSettings xMSet=null;
    logLiason logger = null;
    static cmcDrawHistogram drawhisto=null;
    static cmcImagePrimitives imgprimitives=null;
   
    private static int VERTICAL_GAP = 2;
    private static int XOFFSET = 11;
   
    private gpFetchByteImageData iinf = null;
    private JLabel[] labels = null;
    private JLabel thumbnailLabel = null;
    private JLabel boxHistoLabel = null;
    private JButton btnLeft = null;
    private JButton btnRight = null;
   
    // DTO
    class ImageMetaDataDTO
	{
    	String LongFileName= "Something" + System.currentTimeMillis(); // not null
    	String ThumbNailFileName = null;
    	boolean ThumbNailHasBeenDisplayed=false;
    	long size=-1L;
    	long lastmodif=-1L;
		String FileFormat = null;
		String MimeType = null;
		int Width = -1;
		int Heigth = -1;
		int BitsPerPixel = -1;
		boolean Interlaced = false;
		int NumberOfImages = -1;
		int WidthDPI = -1;
		int HeigthDPI = -1;
		double WidthCentiMeter = Double.NaN;
		double HeigthCentiMeter = Double.NaN;
		int NumberOfComments = 0;
		String[] Comments = null;
		int cycle=0;
	}
    ImageMetaDataDTO dto = null;
    
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
    public cmcMetaDataSideBar(cmcProcSettings im,logLiason ilog)
    //------------------------------------------------------------
    {
    	xMSet = im;
    	logger =ilog;
    	imgprimitives = new cmcImagePrimitives();
    }

    //------------------------------------------------------------
    public boolean populateMetadataPanel(String iLongFileName , cmcProcColorHistogram colhist , cmcMetaDataPanel canvas , Dimension pictdim , int WIDTH)
    //------------------------------------------------------------
    {
    	if( iLongFileName == null ) return false;
    	if( canvas == null ) return false;
    	if( canvas.isVisible() == false ) return true;
  	    if( dto == null )  dto = new ImageMetaDataDTO();
    	//
    	if( xMSet.xU.IsBestand( iLongFileName ) == false ) {
    		do_error("Image not found [" + iLongFileName + "]");
    		return false;
    	}
    	dto.ThumbNailHasBeenDisplayed=false;
    	if( iLongFileName.compareToIgnoreCase( dto.LongFileName ) != 0 ) {
    		dto.LongFileName = iLongFileName;
    		try {
    		  dto.cycle = 0;
    		  if( iinf == null ) iinf = new gpFetchByteImageData();
    		  iinf.cmcProcGetImageInfo( dto.LongFileName );
    		  dto.size = xMSet.xU.getFileSize( dto.LongFileName );
    		  dto.ThumbNailFileName = "UNKNOWN" + System.currentTimeMillis();
    		  dto.ThumbNailHasBeenDisplayed= false;
    		  dto.lastmodif = xMSet.xU.getModificationTime( dto.LongFileName );
    		  dto.FileFormat = iinf.getFormatName();
    		  dto.MimeType = iinf.getMimeType();
    		  dto.Width = iinf.getWidth();
    		  dto.Heigth = iinf.getHeight();
    		  dto.BitsPerPixel = iinf.getBitsPerPixel();
    		  dto.Interlaced = iinf.isProgressive();
    		  dto.NumberOfImages = iinf.getNumberOfImages();
    		  dto.WidthDPI = iinf.getPhysicalWidthDpi();
    		  dto.HeigthDPI = iinf.getPhysicalHeightDpi();
    		  dto.WidthCentiMeter = (double)iinf.getPhysicalWidthInch() * (double)2.54;
    		  dto.HeigthCentiMeter = (double)iinf.getPhysicalHeightInch() * (double)2.54;
    		  dto.NumberOfComments = iinf.getNumberOfComments();
    		  if( dto.NumberOfComments > 0 ) {
    		    dto.Comments = new String[ dto.NumberOfComments ];
    		    for(int i=0;i<dto.NumberOfComments;i++) dto.Comments[i] = iinf.getComment(i);
    		  }
    		  else dto.Comments = null;
    		  //do_log( 1 , iinf.getINFO() );
    		}
    		catch(Exception e ) {
    			do_error( "gpFetchByteImageData error");
    			return false;
    		}
    	}
    	
    	// AWT
    	Font pfont = xMSet.getPreferredFont();
    	Font smlfont = pfont.deriveFont((float)10);
    	int  labelHeigth = smlfont.getSize() + 4;
    	int top = 12;
    	int step = labelHeigth + VERTICAL_GAP;
    	if( labels == null ) {
    	 labels = new JLabel[ 15 ];
    	 int y = top;
    	 for(int i=0;i<labels.length;i++) 
    	 {
    		 y += step;
    		 labels[i] = new JLabel();
    		 labels[i].setFont(smlfont);
    		 labels[i].setBounds( XOFFSET , y , WIDTH - 12 , labelHeigth );
    		 canvas.add( labels[i] );
    	 }
    	 // thumbnail
    	 thumbnailLabel = new JLabel();
    	 thumbnailLabel.setBounds( 0 , 0 , WIDTH ,WIDTH );  // just placeholders
    	 canvas.add(thumbnailLabel);
    	 // Buttons
    	 Rectangle br = new Rectangle( 0 , 0 , 14 , 17);
         btnLeft = new JButton();
         btnLeft.setBounds(br);
         btnLeft.setBorderPainted(false);
 		 btnLeft.setFocusPainted(false);
 		 btnLeft.setContentAreaFilled(false);
 		 btnLeft.setOpaque(true);
 		 BufferedImage bi = (BufferedImage)imgprimitives.getLeftTriangle( btnLeft.getWidth()-1 , btnLeft.getHeight() , cmcProcConstants.BUTTONHOVER , canvas.getBackground() );
   		 if( bi != null) btnLeft.setIcon(new ImageIcon( bi) );
 		 btnLeft.addMouseListener(new MouseAdapter() {
 			@Override
 			public void mouseClicked(MouseEvent arg0) {
 				goLeft();
 			}
 		  });
 		  btnLeft.addMouseListener(new java.awt.event.MouseAdapter() {
 		    public void mouseEntered(java.awt.event.MouseEvent evt) {
 		        btnLeft.setBackground(Color.GRAY);
 		    }

 		    public void mouseExited(java.awt.event.MouseEvent evt) {
 		        btnLeft.setBackground(UIManager.getColor("control"));
 		    }
 		  });
         canvas.add( btnLeft );
         br.y += 10;
         btnRight = new JButton();
         btnRight.setBounds(br);
         btnRight.setBorderPainted(false);
 		 btnRight.setFocusPainted(false);
 		 btnRight.setContentAreaFilled(false);
 		 btnRight.setOpaque(true);
 		 BufferedImage rbi = (BufferedImage)imgprimitives.getRightTriangle( btnLeft.getWidth()-1 , btnLeft.getHeight() , cmcProcConstants.BUTTONHOVER , canvas.getBackground() );
   		 if( rbi != null) btnRight.setIcon(new ImageIcon( rbi) );
 		 btnRight.addMouseListener(new MouseAdapter() {
 			@Override
 			public void mouseClicked(MouseEvent arg0) {
 				goRight();
 			}
 		 });
 		 btnRight.addMouseListener(new java.awt.event.MouseAdapter() {
 		    public void mouseEntered(java.awt.event.MouseEvent evt) {
 		        btnRight.setBackground(Color.GRAY);
 		    }

 		    public void mouseExited(java.awt.event.MouseEvent evt) {
 		        btnRight.setBackground(UIManager.getColor("control"));
 		    }
 		 });
         canvas.add( btnRight );
         
    	 // 
    	 if( drawhisto == null ) drawhisto = new  cmcDrawHistogram( xMSet , logger );
    	 y += step;
     	 boxHistoLabel = new JLabel();
     	 int histow = (int)((double)WIDTH * (double)0.60);
     	 int boxWidth = drawhisto.getBoxWidth();
     	 int boxHeigth = drawhisto.getBoxHeigth();
     	 int histoh  =   (int)((double)histow * (double)boxHeigth / (double)boxWidth);
     	 boxHistoLabel.setBounds( (WIDTH - histow)/2 , y , histow , histoh );
         canvas.add(boxHistoLabel);
         //
         
         
    	}
    	
        // resize thumbnail based on image sizes
   	    int tumWidth = (int)((double)WIDTH * (double)0.87);
   	    int tumHeigth  =   (int)((double)tumWidth * (double)pictdim.getHeight() / (double)pictdim.getWidth());
   	    if( tumHeigth > WIDTH ) {
   	    	tumHeigth = WIDTH;
   	    	tumWidth  =   (int)((double)tumHeigth * (double)pictdim.getWidth() / (double)pictdim.getHeight());
   	    }
   	    thumbnailLabel.setBounds( (WIDTH - tumWidth)/2 , top , tumWidth , tumHeigth );
   		// ThumbNail Info might still be present in DTO (close/reopen the pane)
    	dto.ThumbNailHasBeenDisplayed = showThumbNail();
        if( dto.ThumbNailHasBeenDisplayed == false ) {
   	      BufferedImage bi = (BufferedImage)drawhisto.waitImage( thumbnailLabel.getWidth()-2  , thumbnailLabel.getHeight()-2 , new Color(253, 245, 230), canvas.getBackground());
		  if( bi != null ) { 
			thumbnailLabel.setIcon(new ImageIcon( bi) );
		  }
        }
        
    	// populate labels
        for(int i=0;i<labels.length;i++) labels[i].setText("");
     	labels[0].setText( xMSet.xU.GetShortFileName(dto.LongFileName) );
    	try {
    	 int k=1;	
    	 labels[k++].setText( "Size : " +  String.format("%,d",dto.size) + " bytes");
    	 labels[k++].setText( "Width : " +  String.format("%,d",dto.Width) + " pixels" );
    	 labels[k++].setText( "Height : " +  String.format("%,d",dto.Heigth) + " pixels");
    	 labels[k++].setText( "Date : " + xMSet.xU.prntStandardDateTime(dto.lastmodif) );
    	 labels[k++].setText( "Mime : " + dto.MimeType );
    	 labels[k++].setText( "Depth : " + dto.BitsPerPixel + " bits");
    	 labels[k++].setText( "Interlaced : " + dto.Interlaced );
    	 //labels[k++].setText( "#Images : " + dto.NumberOfImages );
    	 if( dto.WidthDPI >= 0) labels[k++].setText( "Width DPI : " +  String.format("%,d",dto.WidthDPI) + " pixels");
    	 if( dto.HeigthDPI >= 0) labels[k++].setText( "Height DPI : " + String.format("%,d",dto.HeigthDPI) + " pixels");
    	 if( dto.WidthCentiMeter >= 0) labels[k++].setText( "Width : " + String.format("%.2f", dto.WidthCentiMeter) + " cm");
    	 if( dto.HeigthCentiMeter >= 0)labels[k++].setText( "Height : " + String.format("%.2f", dto.HeigthCentiMeter) + " cm");
    	 if( dto.Comments != null ) {
    	  for(int i=0;i<dto.Comments.length;i++ )
    	  {
    		if( k >= labels.length ) { do_error("Too few labels"); break; }
    		labels[k++].setText( "" + dto.Comments[i] );
    	  }
    	 }
    	 
    	}
    	catch(Exception e ) { do_error("cmcMetaDataView - oops"); return false; }
    	
    	// re-iterate and display
    	Rectangle r = btnLeft.getBounds();
    	int y = thumbnailLabel.getY() + thumbnailLabel.getHeight(); 
    	r.x = (WIDTH/2) - btnLeft.getWidth() - 2;
    	r.y = y + 4;
    	btnLeft.setBounds(r);
    	r.x = (WIDTH/2) + 2;
    	btnRight.setBounds(r);
    	//
    	y += btnLeft.getHeight() + 4 - (step/2);
        int thumbnailY = btnLeft.getY() + btnLeft.getHeight() + 4;
        r = labels[0].getBounds();
     	for(int i=0;i<labels.length;i++) {
    		if( labels[i].getText().trim().length() == 0 ) continue;
    		y += step;
    		r.y = y;
    		labels[i].setBounds(r);
    	}
     	
    	// histogram
    	if( colhist != null) {
    		BufferedImage hbi = (BufferedImage)drawhisto.getBoxHisto( colhist , boxHistoLabel.getHeight() , canvas.getBackground());
	  		if( hbi != null ) { 
	  		 boxHistoLabel.setIcon(new ImageIcon( hbi) );
	  		}
	  	 	int newy = r.y + r.height + (step * 2);
	  	 	r = boxHistoLabel.getBounds();
		    r.y = newy;
		    boxHistoLabel.setBounds(r);
	  		boxHistoLabel.setVisible(true);
    	}
    	else boxHistoLabel.setVisible(false);
      	//
    	
    	
    	// spacers
    	canvas.resetCanvas();
        canvas.horizontalLine( 12 , thumbnailY  , WIDTH-24 , Color.GRAY );
    	canvas.horizontalLine( 12 , thumbnailY +  1 , WIDTH-24 , Color.WHITE );
        //
    	canvas.horizontalLine( 12 , r.y - step , WIDTH-24 , Color.GRAY );
    	canvas.horizontalLine( 12 , r.y - step +1 , WIDTH-24 , Color.WHITE );
        //
    	if( colhist != null) {
    	 canvas.horizontalLine( 12 , r.y + r.height +  step , WIDTH-24 , Color.GRAY );
    	 canvas.horizontalLine( 12 , r.y + r.height +  step + 1 , WIDTH-24 , Color.WHITE );
    	}
    	
    	return true;
    }
    
    //------------------------------------------------------------
    private void goLeft()
    //------------------------------------------------------------
    {
    	xMSet.setDirectionRequested( cmcProcEnums.DIRECTION.LEFT );
    }
    
    //------------------------------------------------------------
    private void goRight()
    //------------------------------------------------------------
    {
    	xMSet.setDirectionRequested( cmcProcEnums.DIRECTION.RIGHT );
    }
    
    //------------------------------------------------------------
    public boolean requestCreationOfThumbNail( String iLongFileName , String CMXUID )
    //------------------------------------------------------------
    {
    	if( CMXUID == null ) return true;
    	if( iLongFileName == null ) return false;
    	if( dto == null ) return false;
    	if( dto.LongFileName.compareToIgnoreCase( iLongFileName ) != 0 ) return false;
    	dto.ThumbNailFileName = xMSet.getCacheDir() + xMSet.xU.ctSlash + CMXUID + "." + xMSet.getPreferredImageSuffix();
    	if( xMSet.xU.IsBestand(dto.ThumbNailFileName) ) return true;  // already there
    	//
    	int w = thumbnailLabel.getWidth();
    	double scale = (double)w / (double)dto.Width;
    	// MULTI THREAD
    	gpMakeThumbNail tm = new gpMakeThumbNail( xMSet.xU , logger );
    	if( tm.prepare( dto.LongFileName , dto.ThumbNailFileName , scale ) == false ) return false;
    	tm.start();
    	//
    	return true;
    }
    
    //------------------------------------------------------------
    private boolean showThumbNail()
    //------------------------------------------------------------
    {
      if( dto == null ) return false;
	  if( xMSet.xU.IsBestand( dto.ThumbNailFileName ) == false ) return false;
   	  ImageIcon icon = new ImageIcon( dto.ThumbNailFileName );
	  thumbnailLabel.setIcon( icon );
      return true;
    }
    
    //------------------------------------------------------------
    public boolean thumbNailHasBeenDisplayed(String iLongFileName)
    //------------------------------------------------------------
    {
    	try {
    	  if( iLongFileName == null ) return false;
    	  if( dto == null ) return false;
   //do_error( "[" + iLongFileName + "] [" + dto.LongFileName + "]");
    	  if( dto.LongFileName.compareToIgnoreCase(iLongFileName) != 0) { dto.ThumbNailHasBeenDisplayed =false; return false; }
    	  if( dto.ThumbNailHasBeenDisplayed ) return true;
    	  // check whether the thumbail exists
    	  if( xMSet.xU.IsBestand( dto.ThumbNailFileName ) == false ) return false;
    	  // wait 3 cycles
    	  if ( (dto.cycle++) <= 1) return false; 
   //do_error( "[" + dto.LongFileName + "] [" + dto.ThumbNailFileName + "]");
    	  dto.ThumbNailHasBeenDisplayed = showThumbNail(); // must always be correct
    	  return true;
    	}
    	catch(Exception e ) {
    		e.printStackTrace();
    		return true;  // will stop the GUI from retrying.
    	}
    }
    
}
