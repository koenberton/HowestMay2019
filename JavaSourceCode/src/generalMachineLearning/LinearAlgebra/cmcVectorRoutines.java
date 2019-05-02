package generalMachineLearning.LinearAlgebra;

import generalStatPurpose.gpDoubleListStatFunctions;

public class cmcVectorRoutines {
	
	private static String CRLF = System.getProperty("line.separator");
	
	private double[] normalizerMean = null;
	private double[] normalizerStdDev = null;
	
	private void do_error(String sin)
	{
		System.err.println("(cmcVectorRoutine)" + sin);
	}
	
	public cmcVectorRoutines()
	{
	}
	//-----------------------------------------------------------
	private boolean checkVector(cmcVector x)
	//-----------------------------------------------------------
	{
		if( x == null ) {
			do_error( "Vector is null");
			return false;
		}
		return true;
	}
	//-----------------------------------------------------------
	public cmcVector scalarMultiplicationVector( cmcVector a , double m)
	//-----------------------------------------------------------
	{
		try {
		  if( !checkVector(a) ) return null;
		  double[] ret = new double[ a.getDimension() ];
		  for(int i=0;i<ret.length;i++) ret[i] = a.getVectorValues()[i] * m;
		  return new cmcVector( ret );
		}
		catch (Exception e) {
			do_error("Multiply error");
			return null;
		}
	}
	//-----------------------------------------------------------
	public cmcVector makeAugmentedVector(cmcVector a )
	//-----------------------------------------------------------
	{
		try {
		  if( !checkVector(a) ) return null;
		  double[] augmented = new double[ a.getVectorValues().length + 1];
		  augmented[0]=1;
		  for(int i=0;i<a.getVectorValues().length;i++) augmented[i+1] = a.getVectorValues()[i];
		  return new cmcVector( augmented );
		}
		catch(Exception e) {
			do_error("Augmented");
			e.printStackTrace();
			return null;
		}
	}
	
	//-----------------------------------------------------------
	public double getBiasFromAugmentedVector(cmcVector a)
	//-----------------------------------------------------------
	{
		if( !checkVector(a) ) return Double.NaN;
		if( a.getDimension() < 2 ) { do_error("Not an augmented vector"); return Double.NaN; }
		return a.getVectorValues()[0];
	}
	//-----------------------------------------------------------
	public cmcVector extractVectorFromAugmentedVector( cmcVector a)
	//-----------------------------------------------------------
	{
		try {
		  if( !checkVector(a) ) return null;
		  if( a.getDimension() < 2 ) { do_error("Not an augmented vector"); return null; }
		  double[] ret = new double[ a.getDimension() - 1 ];
		  for(int i=0; i<ret.length ; i++) ret[i] = a.getVectorValues()[i+1];
		  return new cmcVector( ret );
		}
		catch(Exception e ) {
		  do_error("Augmented");
		  e.printStackTrace();
		  return null;
		}
	}
	//-----------------------------------------------------------	
	public cmcVector addVectors( cmcVector a , cmcVector b)
	//-----------------------------------------------------------
	{
	 try {
		if( !checkVector(a) ) return null;
		if( !checkVector(b) ) return null;
		if( a.getDimension() != b.getDimension() ) {
			do_error( "The number of values/dimensions on the vectors do not match");
			return null;
		}
		double[] ret = new double[ a.getDimension() ];
		for(int i=0;i<a.getDimension();i++)
		{
			ret[i ] = a.getVectorValues()[i] + b.getVectorValues()[i];
		}
		return new cmcVector( ret );
	}
	 catch(Exception e ) {
		do_error( "dot product" );
		e.printStackTrace();
		return null;
	 }
	}
	//-----------------------------------------------------------
	public double dotproduct( cmcVector a , cmcVector b)
	//-----------------------------------------------------------
	{
	 try {
		if( !checkVector(a) ) return Double.NaN;
		if( !checkVector(b) ) return Double.NaN;
		if( a.getDimension() != b.getDimension() ) {
			do_error( "The number of values/dimensions on the vectors do not match");
			return Double.NaN;
		}
		double dot=1;
		for(int i=0;i<a.getVectorValues().length;i++)
		{
			dot += a.getVectorValues()[i] *  b.getVectorValues()[i];
		}
		return dot;
		}
	 catch(Exception e ) {
		do_error( "dot product" );
		e.printStackTrace();
		return Double.NaN;
	 }
	}
	//-----------------------------------------------------------
	public double euclidianDistance( cmcVector a , cmcVector b)
	//-----------------------------------------------------------
	{
		try {
		if( !checkVector(a) ) return Double.NaN;
		if( !checkVector(b) ) return Double.NaN;
		if( a.getDimension() != b.getDimension() ) {
			do_error( "The number of values/dimensions on the vectors do not match");
			return Double.NaN;
		}
		double dist = 0;
		double elem = 0;
		for(int i=0;i<a.getVectorValues().length;i++)
		{
			elem = (a.getVectorValues()[i] -  b.getVectorValues()[i]);
			dist += elem * elem;
		}
		dist = Math.sqrt( dist );
		return dist;
		}
		 catch(Exception e ) {
				do_error( "euclidian distance" );
				e.printStackTrace();
				return Double.NaN;
			 }
	}
	//-----------------------------------------------------------
	public String printMatrix( cmcMatrix m)
	//-----------------------------------------------------------
	{
		String sl= CRLF;
		if( m == null ) return "Null matrix";
		int[] dims = m.getDimension();
		int NbrOfRows=dims[0];
		int NbrOfColumns=dims[1];
		if((NbrOfRows<=0) || (NbrOfRows<=0)) return "Empty values";
		double[][] val = m.getValues();
		if( val == null ) return "Empty values 2";
		for(int i=0;i<NbrOfRows;i++)
		{
			   sl += "[ ";
			   for(int j=0;j<NbrOfColumns;j++)
			   {
				  if( j!= 0) sl += " , ";
				  sl+= String.format("%10.6f", val[i][j] );
			   }
			   sl += " ]" + CRLF;
		}
		return sl;
	}
	//-----------------------------------------------------------
	public boolean equalMatrix(cmcMatrix a , cmcMatrix b)
	//-----------------------------------------------------------
	{
		if( (a==null) || (b==null) ) return false;
		if( (!a.isValid()) || (!b.isValid()) ) return false;
		int[] dima = a.getDimension();
		int[] dimb = b.getDimension();
		if( dima[0] != dimb[0] ) return false;
		if( dima[1] != dimb[1] ) return false;
		int NbrOfRows=dima[0];
		int NbrOfColumns=dima[1];
		double[][] aval = a.getValues();
		double[][] bval = b.getValues();
		for(int i=0;i<NbrOfRows;i++)
		{
			   for(int j=0;j<NbrOfColumns;j++)
			   {
				   if( aval[i][j] != bval[i][j] ) return false;
			   }
		}
		return true;
	}
	//-----------------------------------------------------------
	public cmcMatrix makeAugmentedMatrix(cmcMatrix m)
	//-----------------------------------------------------------
	{
		try {
		 if( m == null ) return null;
		 int NbrOfRows= m.getNbrOfRows();
		 int NbrOfColumns= m.getNbrOfColumns();
		 if((NbrOfRows<=0) || (NbrOfRows<=0)) return null;
		 double[][] augval = new double[ NbrOfRows][ NbrOfColumns + 1 ];
		 for(int i=0;i<NbrOfRows;i++) 
	     {
	    		augval[i][0] = (double)1;
	    		for(int j=0;j<NbrOfColumns;j++) augval[i][j+1] = m.getValues()[i][j];
	     }
	     cmcMatrix augMatrix = new cmcMatrix( augval );
	     return augMatrix;
		}
		catch(Exception e  ) {
			do_error( "makeAugmentedmattrix" + e.getMessage() );
			return null;
		}
	}
	//-----------------------------------------------------------
	public cmcMatrix transposeMatrix(cmcMatrix m)
	//-----------------------------------------------------------
	{
	   if( m == null ) return null;
	   int[] dims = m.getDimension();
	   int NbrOfRows=dims[0];
	   int NbrOfColumns=dims[1];
	   if((NbrOfRows<=0) || (NbrOfRows<=0)) return null;
	   double[][] val = m.getValues();
	   if( val == null ) return null;
	   double[][] tval = new double[NbrOfColumns][NbrOfRows];  // cols and rows are swapped !
	   for(int i=0;i<NbrOfRows;i++)
	   {
		   for(int j=0;j<NbrOfColumns;j++)
		   {
			   tval[j][i] = val[i][j];
		   }
	   }
	   cmcMatrix t = new cmcMatrix( tval );
	   return t;	
	}
	//-----------------------------------------------------------
	public boolean isSymmetricMatrix( cmcMatrix m)
	//-----------------------------------------------------------
	{
		int[] dims = m.getDimension();
		if( (dims[0] <= 0) || (dims[1] <= 0)) return false;
		if( dims[0] != dims[1] ) return false;
		cmcMatrix t = transposeMatrix(m);
		if( t== null ) return false;
		return equalMatrix( m , t);
	}
	//-----------------------------------------------------------
	public cmcMatrix multiplyMatrix( cmcMatrix a , cmcMatrix b)
	//-----------------------------------------------------------
	{
		if( (a==null) || (b==null) ) return null;
		if( (!a.isValid()) || (!b.isValid()) ) return null;
		int[] dima = a.getDimension();
		int[] dimb = b.getDimension();
		//  A(n,m) B(m,p)  =>  C(n,p)  
		int n = dima[0];
		int m = dima[1];
		int p = dimb[1];
		if( dima[1] != dimb[0] ) { do_error("Number of columns A [" + dima[1] + "] does not match Number of rows B [" + dimb[0] + "]"); return null; }
		double[][] aval = a.getValues();
		double[][] bval = b.getValues();
		double[][] res = new double[ n ][ p ];  //
		for(int i=0;i<n;i++)
		{
			for(int j=0;j<p;j++) res[i][j]=0;
		}
		//  Cij =  sum(k=1..m) Aik * Bkj   
		for(int i=0;i<n;i++)
		{
			for(int j=0;j<p;j++)
			{
				for(int k=0;k<m;k++) res[i][j] += aval[i][k] *bval[k][j];
			}
		}
		cmcMatrix r = new cmcMatrix( res );
		return r;
	}
	//-----------------------------------------------------------
	public cmcMatrix aggrMatrix( cmcMatrix a , cmcMatrix b , boolean substract)
	//-----------------------------------------------------------
	{
		if( (a==null) || (b==null) ) return null;
		if( (!a.isValid()) || (!b.isValid()) ) return null;
		int[] dima = a.getDimension();
		int[] dimb = b.getDimension();
		//  A(n,m) B(n,m)  =>  C(n,m)
		if( dima[1] != dimb[1] ) { do_error("Number of columns A [" + dima[1] + "] does not match Number of cols B [" + dimb[1] + "]"); return null; }
		if( dima[0] != dimb[0] ) { do_error("Number of rows A [" + dima[0] + "] does not match Number of rows B [" + dimb[0] + "]"); return null; }
		int n = dima[0];
		int m = dima[1];
		double[][] aval = a.getValues();
		double[][] bval = b.getValues();
		double[][] res = new double[ n ][ m];  //
		if( substract )
		{
		  for(int i=0;i<n;i++)
		  {
			for(int j=0;j<m;j++) res[i][j] = aval[i][j] - bval[i][j];
		  }
		}
		else {
		  for(int i=0;i<n;i++)
		  {
			for(int j=0;j<m;j++) res[i][j] = aval[i][j] + bval[i][j];
		  }
		}
		cmcMatrix r = new cmcMatrix( res );
		return r;
	}
	//-----------------------------------------------------------
	public cmcMatrix addMatrix( cmcMatrix a , cmcMatrix b)
	//-----------------------------------------------------------
	{
		return aggrMatrix( a , b , false);
	}
	//-----------------------------------------------------------
	public cmcMatrix substractMatrix( cmcMatrix a , cmcMatrix b)
	//-----------------------------------------------------------
	{
		return aggrMatrix( a , b , true);
	}
	//-----------------------------------------------------------
	public cmcMatrix scalarMultiplyMatrix( double scalar , cmcMatrix a )
	//-----------------------------------------------------------
	{
		if( a==null ) return null;
		if( !a.isValid() ) return null;
		int[] dima = a.getDimension();
		int n = dima[0];
		int m = dima[1];
		if( (n<=0) || (m<=0) ) { do_error("scalar empty"); return null; }
		double[][] aval = a.getValues();
		double[][] res = new double[ n ][ m];  //
		for(int i=0;i<n;i++)
		{
			for(int j=0;j<m;j++) res[i][j] = aval[i][j] * scalar;
		}
		cmcMatrix r = new cmcMatrix( res );
		return r;
	}	
	//-----------------------------------------------------------
	public double sigmoid( double x)
	//-----------------------------------------------------------
	{
	 return (1/( 1 + Math.pow(Math.E,(-1*x))));	
	}
	//-----------------------------------------------------------
	public cmcMatrix sigmoidMatrix( cmcMatrix a )
	//-----------------------------------------------------------
	{
		if( a==null ) return null;
		if( !a.isValid() ) return null;
		int[] dima = a.getDimension();
		int n = dima[0];
		int m = dima[1];
		if( (n<=0) || (m<=0) ) { do_error("sigmoid empty"); return null; }
		double[][] aval = a.getValues();
		double[][] res = new double[ n ][ m];  //
		for(int i=0;i<n;i++)
		{
			for(int j=0;j<m;j++) res[i][j] = sigmoid(aval[i][j]);
		}
		cmcMatrix r = new cmcMatrix( res );
		return r;
	}		    
	  
	//   a list of vectors.  Each vector is comprised of the same number of features.
	//-----------------------------------------------------------
	public cmcVector[] normalizeVectorList( cmcVector[] vlist)
	//-----------------------------------------------------------
	{
		normalizerMean = normalizerStdDev = null;
		if( vlist == null ) { do_error("null list"); return null; }
		int nrows = vlist.length;
		if( nrows <= 0) { do_error("empty list"); return null; }
		int nfeatures = vlist[0].getDimension();
    	if( nfeatures <= 0) { do_error("no features"); return null; }
    	
    	// Normalize     x(i,j) - mean(j) / stdev(j)     mean(j) = mean of the the feature , std(j) stdev of feature
    	normalizerMean = new double[ nfeatures ];
    	normalizerStdDev = new double[ nfeatures ];
    	gpDoubleListStatFunctions stat = new gpDoubleListStatFunctions( null , null );
    	for(int f=0;f<nfeatures;f++)
    	{
    		double[] val = new double[ nrows ];
    		normalizerMean[f] = normalizerStdDev[f] = Double.NaN;
    		for(int i=0;i<nrows;i++)
    		{
    			val[i] = vlist[i].getVectorValues()[f];
    		}
    		boolean ib = stat.calculate(val);
    		if( ib == false ) { do_error("Stat error"); return null; }
    		normalizerMean[f] = stat.getMean();
    		normalizerStdDev[f] = stat.getStandardDeviation();
    		//do_log( 1 , "Feature Mean=" + featureMean[f] + " Stdv=" + featureStDv[f] + " Min=" + stat.getMinimum() + " Max=" + stat.getMaximum() + " Med=" + stat.getMedian() );
    
    		if( (Double.isNaN(normalizerMean[f])) || (Double.isNaN(normalizerStdDev[f])) ) { do_error("stat error"); return null;}
    		if( normalizerStdDev[f] == 0 ) { do_error( "Zero value on stdev - cannot normalize" ); return null; }
    	}
    	cmcVector[] normalizedVectorList = new cmcVector[ nrows ];
    	for(int row=0 ; row<nrows ; row++)
    	{
    		double[] norma = new double[ nfeatures ];
    		for(int j=0;j<nfeatures;j++)
    		{
    			norma[ j ] = ( vlist[row].getVectorValues()[j] - normalizerMean[j] ) / normalizerStdDev[j];
    		}
    		normalizedVectorList[row] = new cmcVector( norma );
    	}
    	return normalizedVectorList;
	}
	  
	//-----------------------------------------------------------
	public cmcVector[] matrix2VectorList( cmcMatrix x )
	//-----------------------------------------------------------
	{
		try {
	     if( x == null ) return null;
	     int nrows = x.getNbrOfRows();
	     if( nrows <= 0) return null;
	     int ncols = x.getNbrOfColumns();
	     if( ncols <= 0 ) return null;
	     cmcVector[] vlist = new cmcVector[ nrows ];
	     for(int i=0;i<nrows;i++)
	     {
	      if( x.getValues()[i].length != ncols ) { do_error("(matrix2VectorList) differing number of cols"); return null; }
	      double[] vals = new double[ ncols ];
	 	  for(int j=0;j<vals.length;j++) vals[j] = x.getValues()[i][j];  	
	 	  vlist[i] = new cmcVector( vals );
	     }
	     return vlist;
		}
		catch(Exception e ) {
			do_error("matrix2vlist" + e.getMessage() );
			return null;
		}
	}
	//-----------------------------------------------------------
	public cmcMatrix vectorList2Matrix( cmcVector[] vlist )
	//-----------------------------------------------------------
	{
		try {
		 if( vlist == null ) return null;
		 int nrows = vlist.length;
		 if( nrows <= 0 ) return null;
		 int ncols = vlist[0].getDimension();
		 if( ncols <= 0 ) return null;
		 double[][] vals = new double[ nrows ][ ncols ];
		 for(int i=0;i<nrows;i++)
		 {
			if( vlist[i].getDimension() != ncols ) { do_error("(v2matrix) columns differ"); return null; }
			for(int j=0;j<ncols;j++) vals[i][j] = vlist[i].getVectorValues()[j];
		 }
		 cmcMatrix z = new cmcMatrix(vals);
		 if( nrows != z.getNbrOfRows() ) { do_error("v2m - rows differ"); return null; }
		 if( ncols != z.getNbrOfColumns() ) { do_error("v2m - cols differ"); return null; }
		 return z;
		}
		catch(Exception e) {
			do_error("v2m - error " + e.getMessage());
			return null;
		}
	}
	//-----------------------------------------------------------
	public double[] getNormalizerMean()
	//-----------------------------------------------------------
	{
		return normalizerMean;
	}
	//-----------------------------------------------------------
	public double[] getNormalizerStdDev()
	//-----------------------------------------------------------
	{
		return normalizerStdDev;
	}
	//-----------------------------------------------------------
	public cmcMatrix normalizeMatrix( cmcMatrix x)
	//-----------------------------------------------------------
	{
		normalizerMean = normalizerStdDev = null;
		if( x == null ) return null;
		cmcVector[] vlist = matrix2VectorList( x );
		if( vlist == null ) return null;
		cmcVector[] nlist = normalizeVectorList( vlist );
		if( nlist == null ) return null;
		cmcMatrix z = vectorList2Matrix( nlist );
		if( z == null ) return null;
		if( x.getNbrOfColumns() != z.getNbrOfColumns() ) { do_error("normMtrx - cols differ"); return null; }
		if( x.getNbrOfRows() != z.getNbrOfRows() ) { do_error("normMtrx - rows differ"); return null; }
		vlist=null;
		nlist=null;
		return z;
	}
	//-----------------------------------------------------------
	public cmcMatrix transform2ZeroAndOne( cmcMatrix x )
	//-----------------------------------------------------------
	{
	 return  transform2ZeroAndOneOrMinusOne( x , 0);
    }
	//-----------------------------------------------------------
	public cmcMatrix transform2ZeroAndMinusOne( cmcMatrix x )
	//-----------------------------------------------------------
	{
	 return  transform2ZeroAndOneOrMinusOne( x , -1);
	}
	//-----------------------------------------------------------
	private cmcMatrix transform2ZeroAndOneOrMinusOne( cmcMatrix x , int ZeroOrMinusOne)
	//-----------------------------------------------------------
	{
		try {
		 if( x == null )  { do_error("input matrix null"); return null; }
		 int nrows = x.getNbrOfRows();
		 if( nrows <= 0 ) { do_error("No rows"); return null; }
		 int ncols = x.getNbrOfColumns();
		 if( ncols != 1 ) { do_error("Number of columns is not 1"); return null; }
		 double[] cross = new double[2];
		 cross[0] = Double.NaN;
		 cross[1] = Double.NaN;
		 int ptr = -1;
		 for(int i=0;i<nrows;i++)
		 {
			double dd = x.getValues()[i][0];
			boolean found = false;
			for(int j=0;j<2;j++)
			{
				if( Double.isNaN( cross[j] ) ) continue;
				if( cross[j] == dd ) { found = true; break; }
			}
			if( found ) continue;
			ptr++;
			if( ptr >= 2 ) {
				do_error("There should be exactly 2 different values - too many - got [" + (ptr+1) + "]");
				return null;
			}
			cross[ptr]=dd;
		 }
		 if( ptr != 1 ) {
				do_error("There should be exactly 2 different values - too few - got [" + (ptr+1) + "]");
				do_error("The data set is probably too small to heve different target classes");
				return null;
		 }
		 if( Double.isNaN(cross[0]) ) { do_error("cannot determine cross value 1"); return null; }
		 if( Double.isNaN(cross[1]) ) { do_error("cannot determine cross value 2"); return null; }
		 
		 // must be in sorted order
		 if( cross[0] > cross[1] ) {
			 double dd = cross[0];
			 cross[0] = cross[1];
			 cross[1] = dd;
		 }
		 if( cross[0] == cross[1] ) {
			 do_error( "System error - cross values must differ");
			 return null;
		 }
		 //do_error( "[-1=" + cross[0] + "] [1=" + cross[1] + "]" );
		 double[][] vals = new double[ nrows ][ 1 ];
		 for(int i=0;i<nrows;i++)
		 {
			double dd = x.getValues()[i][0];
			int idx=-1;
			for(int j=0;j<2;j++)
			{
				if( cross[j] == dd ) { idx=j; break; }
			}
			if( idx < 0 )  { do_error("cannot cross value " + dd); return null; }
			if( idx == 0 ) vals[i][0] = (double)ZeroOrMinusOne;
			else
			if( idx == 1 ) vals[i][0] = (double)1;
			else  { do_error("2MinusOne cannot cross value " + dd); return null; }
		 } 
		 cmcMatrix z = new cmcMatrix( vals );
		 return z;
		}
		catch(Exception e ) {
			do_error("2MinusOne " + e.getMessage());
			return null;
		}
	}
	
	// returns a 1 row N column matrix which is the rowth row in the source matrix
	public cmcMatrix getRowsAsMatrix( cmcMatrix x , int row)
	{
		try {
		if ( x == null ) return null;
		if( (row<0) || (row>=x.getNbrOfRows())) return null;
		int ncols = x.getNbrOfColumns();
		if( ncols < 0) return null;
		double[][] rowm = new double[ 1 ][ ncols ];
		for(int i=0;i<ncols;i++)
		{
			rowm[0][i] = x.getValues()[ row ][ i ];
		}
		return new cmcMatrix( rowm );
		}
		catch(Exception e) { return null; }
	}
}
