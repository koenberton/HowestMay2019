package featureExtraction;

public class PredictDetail {
	
	String FileName;
	String Anticipated;
	String Predicted;
	long UID=-1L;
	
	public PredictDetail( String fn , String ant , String pre )
	{
      FileName=fn;
      Anticipated=ant;
      Predicted=pre;
      UID = extractUID(fn);
	}
	
	public String getFileName() { return FileName; }
	public String getAnticipated() { return Anticipated; }
	public String getPredicted() { return Predicted; }
	public long getUID() { return UID; }
	
	private long extractUID(String fn)
	{ // PTXT_aabbcccc_nnnnnn-0000x0000.jpg
	  try {
	   if( (!fn.toUpperCase().startsWith("PTXT_")) && (!fn.toUpperCase().startsWith("POBJ_")) ) return -1L;
	   char[] buf = fn.toCharArray();
       int cnt=0;
       String suid="";
       for(int i=0;i<buf.length;i++)
       {
    	  if( (buf[i] == '_') || (buf[i] == '-') ) { 
    		  cnt++;
    		  continue;
    	  }
    	  if( cnt == 2) {
    		  suid += buf[i];
    	  }
       }
       long ll = Long.parseLong( suid );
	   return ll;
	  }
	  catch(Exception e) { return -1L; }
	}

}
