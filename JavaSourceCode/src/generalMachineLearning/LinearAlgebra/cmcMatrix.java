package generalMachineLearning.LinearAlgebra;

public class cmcMatrix {

	private double[][] values = null;  //  [ ROWS , COLUMNS ]
	private int NbrOfRows = -1;
	private int NbrOfColumns = -1;
	private boolean validMatrix=false;
	
	private void do_error(String sin)
	{
		System.err.println("(cmcMatrix) " + sin);
	}
	
	public cmcMatrix( int irows , int icolumns )  // type 1 constructor
	{
	  NbrOfRows=irows;
	  NbrOfColumns = icolumns;
	  values=null;
	  validMatrix=false;
	}
	
	public cmcMatrix( double[][] ival ) // type 2 constructor
	{
		values=null;
		validMatrix=false;
		NbrOfRows = -1;
		NbrOfColumns = -1;
		//
		NbrOfRows=ival.length;
		if( NbrOfRows >= 1 ) {
		  NbrOfColumns = ival[0].length;
		  if( fillMatrix(ival) == false ) {
			  NbrOfRows = -1;
			  NbrOfColumns = -1;
			  values = null;
		  }
		}
	}
	
	public boolean isValid()
	{
		return this.validMatrix;
	}
	public double[][] getValues()
	{
		return values;
	}
	public boolean setValue( int row , int col , double dd)
	{
		try {
		 values[row][col] = dd;
		 return true;
		}
		catch(Exception e ) { return false; }
	}
	public boolean fillMatrix( double[][] ival)
	{
		if( ival == null ) { do_error( "Null ival"); return false; }
		if( NbrOfRows != ival.length ) { do_error("Number of rows does not match"); return false; }
		if( NbrOfColumns <= 0 ) { do_error("Number of Columns not set correcty"); return false; }
		values = new double[ NbrOfRows ][ NbrOfColumns ];
		for(int i=0;i<NbrOfRows;i++)
		{
			if( ival[i].length != NbrOfColumns ) { do_error("Number of columns is not consistent"); return false; }
			for(int j=0;j<NbrOfColumns;j++) values[i][j] = ival[i][j];
		}
		validMatrix=true;
		return true;
	}
	public int[] getDimension()
	{
		int[] dim = new int[2];
		dim[0] = NbrOfRows;
		dim[1] = NbrOfColumns;
		return dim;
	}
	public int getNbrOfColumns()
	{
		return NbrOfColumns;
	}
	public int getNbrOfRows()
	{
		return NbrOfRows;
	}
}
