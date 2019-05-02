package generalImagePurpose;

public class cmcGLCMRoutines {

	int[][] east_GLCM = null;
	int[][] west_GLCM = null;
	int[][] nort_GLCM = null;
	int[][] sout_GLCM = null;
	int[][] vert_GLCM = null;
	int[][] horz_GLCM = null;
	double[][] double_vert_GLCM = null;
	double[][] double_horz_GLCM = null;
	
	//-----------------------------------------------------------------------
	public cmcGLCMRoutines()
	//-----------------------------------------------------------------------
	{
		
	}
	
	//-----------------------------------------------------------------------
	public int[][] getVerticalGLCMAbsolute()
	//-----------------------------------------------------------------------
	{
	  return vert_GLCM;
	}
	//-----------------------------------------------------------------------
	public int[][] getHorizontalGLCMAbsolute()
	//-----------------------------------------------------------------------
	{
	  return horz_GLCM;
	}
	//-----------------------------------------------------------------------
	public double[][] getVerticalGLCM()
	//-----------------------------------------------------------------------
	{
		  return double_vert_GLCM;
	}
	//-----------------------------------------------------------------------
	public double[][] getHorizontalGLCM()
	//-----------------------------------------------------------------------
	{
		  return double_horz_GLCM;
	}
	
	// only works on GRAYSCALE
	//-----------------------------------------------------------------------
	public boolean makeGLCM( int[]pixels , int width , int bins)
	//-----------------------------------------------------------------------
	{
	    if( pixels == null ) return false;
	    int heigth = pixels.length / width;
	    east_GLCM = new int[bins][bins];
	    west_GLCM = new int[bins][bins];
	    sout_GLCM = new int[bins][bins];
	    nort_GLCM = new int[bins][bins];
	    vert_GLCM = new int[bins][bins];
	    horz_GLCM = new int[bins][bins];
	    double_horz_GLCM = new double[bins][bins];
	    double_vert_GLCM = new double[bins][bins];
	    
	    //
	    for(int i=0;i<bins;i++)
	    {
	    	for(int j=0;j<bins;j++) 
	    	{
	    		east_GLCM[i][j]=0;
	    		west_GLCM[i][j]=0;
	    		nort_GLCM[i][j]=0;
	    		sout_GLCM[i][j]=0;
	    		vert_GLCM[i][j]=0;
	    		horz_GLCM[i][j]=0;
	    	}
	    }
	    // calcuate the east, west,north and south GLCM values
	    try {
	     int curr=-1;
	     for(int y=0;y<heigth;y++)
	     {
	    	for(int x=0;x<width;x++)
	    	{
	    		curr++;
	    		int east = (x < (width-1)) ? curr+1 : -1;
	    		int west = x > 0 ? curr-1 : -1;
	    		int nort = y > 0 ? curr-width : -1;
	    		int sout = (y < (heigth-1)) ? curr+width : -1;
	    		//
	    		int pcurr = 0xff & pixels[curr];
	    		int ecurr = east >= 0 ? 0xff & pixels[east] : -1;
	            int wcurr = west >= 0 ? 0xff & pixels[west] : -1;
	            int ncurr = nort >= 0 ? 0xff & pixels[nort] : -1;
	            int scurr = sout >= 0 ? 0xff & pixels[sout] : -1;
	            //
	            if( ecurr >=0 ) east_GLCM[pcurr][ecurr] += 1;
	            if( wcurr >=0 ) west_GLCM[pcurr][wcurr] += 1;
	            if( scurr >=0 ) sout_GLCM[pcurr][scurr] += 1;
	            if( ncurr >=0 ) nort_GLCM[pcurr][ncurr] += 1;
	    	}
	     }
	     //
	    }
	    catch(Exception e )  {
	    	e.printStackTrace();
	    	return false;
	    }
	    //  east and west are transposed check
	    int vert_check=0;
	    int horz_check=0;
	    int vert_total=0;
	    int horz_total=0;
	    for(int i=0;i<east_GLCM.length;i++)
	    {
	    	for(int j=0;j<east_GLCM[i].length;j++)
	    	{
	    		horz_GLCM[i][j] = east_GLCM[i][j] + west_GLCM[j][i];
	    		vert_GLCM[i][j] = nort_GLCM[i][j] + sout_GLCM[j][i];
	    		horz_total += east_GLCM[i][j] + west_GLCM[j][i];
	    		vert_total += nort_GLCM[i][j] + sout_GLCM[j][i];
	    		horz_check += Math.abs( east_GLCM[i][j] - west_GLCM[j][i] );
	    		vert_check += Math.abs( nort_GLCM[i][j] - sout_GLCM[j][i] );
	    	}
	    }
	    if( (horz_check !=0) || (vert_check !=0) ) {
	    	System.err.println("Matrices are not transposed");
	    	return false;
	    }
	    if( (horz_total ==0) || (vert_total ==0) ) {
	    	System.err.println("Matrices are not empty");
	    	return false;
	    }
	    for(int i=0;i<east_GLCM.length;i++)
	    {
	    	for(int j=0;j<east_GLCM[i].length;j++)
	    	{
	    		double_horz_GLCM[i][j] = (double)horz_GLCM[j][i] / (double)horz_total;
	    		double_vert_GLCM[i][j] = (double)vert_GLCM[j][i] / (double)vert_total;
	    	}
	    }
	    //
	    //show( horz_GLCM );
	    //show( vert_GLCM );
	    
		return true;
	}
	
	private void show( int[][] mtrx)
	{
		for(int i=0; i<mtrx.length;i++)
		{
			System.out.println(" ");
			for(int j=0;j<mtrx[i].length;j++)
			{
				System.out.print( " " + String.format( "%6d" , mtrx[i][j]) );
			}
		}
		System.out.println(" ");
	}
	
}
