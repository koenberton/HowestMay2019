package generalImagePurpose;



import generalpurpose.gpUtils;
import logger.logLiason;


public class gpMakeThumbNail extends Thread {
	
	gpUtils xU = null;
	logLiason logger = null;
	
	gpLoadImageInBuffer imgloader = null;
	cmcImageRoutines irout = null;
	
	private String ThrdLongFileName = null;
	private String ThrdThumbNailFileName = null;
	private double ThrdScale = -1;
    private long startt=0L;
    
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
	public gpMakeThumbNail(gpUtils iu , logLiason ilog)
	//------------------------------------------------------------
	{
		  startt = System.currentTimeMillis();
	      xU = iu;
	      logger = ilog;
		  irout = new cmcImageRoutines(null);
		  imgloader = new gpLoadImageInBuffer(xU,logger);
	}
	
	//  T H R E A D
	//------------------------------------------------------------
	public void run()
	//------------------------------------------------------------
	{
		if( makeThumbNail( ThrdLongFileName , ThrdThumbNailFileName , ThrdScale) == false ) { do_error( "Error in thread"); }
		else { 
		 do_log( 1, "Thumbnail created [" + ThrdThumbNailFileName + "] in [" + ((System.currentTimeMillis() - startt)) + " msec]");
		}
	}
	
	//------------------------------------------------------------
	private boolean checkParams ( String LongFileName , String ThumbNailFileName , double scale)
	//------------------------------------------------------------
	{
			if( LongFileName == null ) {do_error("Null filename"); return false; }
			if( ThumbNailFileName == null ) {do_error("Null thumbnail"); return false; }
		    if( scale <= 0 )  { do_error("Zero scale"); return false; }
		    if( scale >= 1 )  { do_error("scale greater than 1"); return false; }
		    if( xU.IsBestand( LongFileName ) == false ) {
				do_error("Cannot find file [" + LongFileName + "]");
				return false;
			}
		    return true;
	}
	
	//------------------------------------------------------------
	public boolean prepare( String LongFileName , String ThumbNailFileName , double scale)
	//------------------------------------------------------------
	{
	   ThrdLongFileName = ThrdThumbNailFileName = null;
	   ThrdScale = -1;
	   if( checkParams( LongFileName , ThumbNailFileName , scale) == false) return false;
	   ThrdLongFileName = LongFileName;
	   ThrdThumbNailFileName = ThumbNailFileName;
	   ThrdScale = scale;
	   return true;
	}
	
	//------------------------------------------------------------
	public boolean makeThumbNail( String LongFileName , String ThumbNailFileName , double scale)
	//------------------------------------------------------------
	{
		if( checkParams( LongFileName , ThumbNailFileName , scale) == false) return false;
		int[] pix = null;
		int[] tumpix=null;
		try {
		  pix = imgloader.loadBestandInBuffer( LongFileName );
		  if( pix == null ) { do_error("Canot load image in buffer"); return false; }
		  int width = imgloader.getBreedte();
		  int heigth = imgloader.getHoogte();
		  //
		  tumpix = irout.resizeImage(pix, width, heigth, scale, cmcImageRoutines.ImageType.ARGB );
		  if( tumpix== null ) { do_error("Canot make thumbnail"); return false; }
		  //
		  if( xU.IsBestand( ThumbNailFileName ) == true ) {
			boolean ib = xU.VerwijderBestand( ThumbNailFileName );
			do_error("Cannot delete [" + ThumbNailFileName + "]");
			return false;
		  }
		  int twidth =  irout.getResizeWidth();
		  int theigth = irout.getResizeHeigth();
		  //
		  irout.writePixelsToFile( tumpix, twidth, theigth, ThumbNailFileName, cmcImageRoutines.ImageType.ARGB );
		  return true;
		}
		catch(Exception e ) {
			e.printStackTrace();
			return false;
		}
		finally {
		 tumpix =null;
		 pix = null;
		}
	}
	
	
}
