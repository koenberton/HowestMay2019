package generalMachineLearning.Bayes;

import generalMachineLearning.ARFF.cmcARFF;

public class cmcBayesModel {
	
	   private boolean LAPLACE_TRICK = false;  // do not set to true
	   
	   public String   categoryName=null;
	   public cmcARFF.ARFF_TYPE tipe= cmcARFF.ARFF_TYPE.UNKNOWN;
	   public int      numberOfElements=0;
	   public String[] ClassNameList;
	   public int[]    ClassCounts;
	   public double[] ClassProbs;
       //
	   public double[] binsegmentbegins;
	   public int[] BinCounts;
	   public double[] BinProbs;
	   //
	   public int[][]  BinClassCounts;      // everything will be derived from these counts
	   public double[][] BinClassProbs;
       //
	   public String[] NominalNameList = null;
	   //
	   public cmcBayesModel( int nbins , int nclasses , String s , cmcARFF.ARFF_TYPE itipe)
	   {
		   categoryName = s;
		   tipe = itipe;
		   numberOfElements=0;
		   ClassNameList = new String[ nclasses ];
		   ClassCounts = new int[ nclasses ];
		   ClassProbs = new double[ nclasses ];
           //
		   binsegmentbegins = new double[ nbins ];
		   BinCounts = new int[ nbins ];
           BinProbs = new double[ nbins ];		   
		   //
		   BinClassCounts = new int[ nbins ][ nclasses ];
		   BinClassProbs = new double[ nbins ][ nclasses ];
		   //
		   NominalNameList = null;
		   //
		   for(int i=0;i<nclasses;i++)
		   {
			  ClassCounts[i] = Integer.MIN_VALUE;
			  ClassProbs[i] = Double.NaN;
			  ClassNameList[ i ] = null;
		   }
		   for(int i=0;i<nbins;i++)
		   {
			   binsegmentbegins[i] = Double.NaN;
			   BinCounts[i] = Integer.MIN_VALUE;
			   BinProbs[i] = Double.NaN;
			   for(int j=0;j<nclasses;j++) 
			   {
				   BinClassCounts[i][j] = Integer.MIN_VALUE;
				   BinClassProbs[i][j] = Double.NaN;
			   }
		   }
	   }

	   // P(text|density,entropy,..) = P(density|text) P(entropy|text)    p(text) DIV p(entripy)p(density) ..
	   // all stats are calculated starting from BinClassCount, i.e. the number of occurrence per class per bin
	   // P(density|text) is model.binclassprob  [ number of bins ] [ number of classes ]
	   // P(density) is model.binprob [ number of bins ]
	   // P(text) is model.classprob [ number of classes ]
	   public boolean performAggregate()
	   {
		   int dlaplace = LAPLACE_TRICK ? 1 : 0;
		   if( this.ClassNameList == null ) return false;
		   int nclasses = this.ClassNameList.length;
		   if( nclasses <= 0 ) return false;
		   if( this.BinClassCounts == null ) return false;
		   int nbins = this.BinClassCounts.length;
		   if( nbins <= 0 ) return false;
		   if( this.BinClassCounts[0].length != nclasses ) return false;
		   //  reset
		   this.numberOfElements=0;
		   for(int i=0;i<nclasses;i++)
		   {
			  ClassCounts[i] = 0;
			  ClassProbs[i] = Double.NaN;
		   }
		   for(int i=0;i<nbins;i++)
		   {
			   BinCounts[i] = 0;
			   BinProbs[i] = Double.NaN;
			   for(int j=0;j<nclasses;j++) 
			   {
				   BinClassProbs[i][j] = Double.NaN;
			   }
		   }
		   //
		   this.numberOfElements=0;
		   for(int i=0;i<nclasses;i++)
		   {
			  this.ClassCounts[i] = 0;
			  for(int j=0;j<nbins;j++)
			  {
				  this.ClassCounts[i] += this.BinClassCounts[j][i];
				  this.numberOfElements += this.BinClassCounts[j][i];
			  }
		   }
		   for(int i=0;i<nclasses;i++)
		   {
			    this.ClassProbs[i] =   (double)(this.ClassCounts[i] + dlaplace )/ (double)(this.numberOfElements + dlaplace); // Laplace trick
			    //if( this.ClassProbs[i] == 0 ) this.ClassProbs[i] = (double)1 / (double)ctot;
		   }
		   //
		   for(int i=0;i<nclasses;i++)
		   {
			  for(int j=0;j<nbins;j++)
			  {
				  this.BinCounts[j] += this.BinClassCounts[j][i];
			  }
		   }
	       for(int i=0;i<nbins;i++)
	       {
	    	   this.BinProbs[i] = (double)(this.BinCounts[i] + dlaplace) / (double)(this.numberOfElements + dlaplace);  // Laplace
	    	   //if( this.BinProbs[i] == 0 ) this.BinProbs[i] = (double)1 / (double)ctot;
	       }
		   //
		   for(int i=0 ; i<nbins;i++)
		   {
			   for(int j=0;j<nclasses;j++)
			   {
				   if( ClassCounts[j] == 0 ) BinClassProbs[i][j] = 0;   // to avoid that MIXED has a probability 1
				   else {
				    this.BinClassProbs[i][j] = (double)(BinClassCounts[i][j] + dlaplace )/ (double)(ClassCounts[j] + dlaplace); // laplace trick
				    //if( this.BinClassProbs[i][j] == 0 ) this.BinClassProbs[i][j] = (double)1 / (double)ctot;
				   }
			   }
		   }
		   return true;
	   }
	   
	   //------------------------------------------------------------
	   private int getClassIndex(String classlabel )
	   //------------------------------------------------------------
	   {
			  try {
				   int idx = -1;
				   for(int i=0;i<ClassNameList.length;i++)
				   {
					   if( ClassNameList[i].compareToIgnoreCase( classlabel ) == 0 ) {
						   idx = i;
						   break;
					   }
				   }
				   return idx;
				  }
				  catch( Exception e ) {
					   System.err.println("OOPS - cannot allocate [" + classlabel + "]" );
					   return -1;
				  }
	   }
	   
	   //------------------------------------------------------------
	   private int getBinIndex( double valuetobebinned)
	   //------------------------------------------------------------
	   {
		   if( this.tipe == cmcARFF.ARFF_TYPE.STRING ) {
			   System.err.println("OOPS - request to determine a bin for STRING tipe" );
			   return -1;
		   }
		   if( this.tipe == cmcARFF.ARFF_TYPE.CLASS ) {
			   System.err.println("OOPS - request to determine a bin for CLASS tipe" );
			   return -1;
		   }
		   if( this.tipe == cmcARFF.ARFF_TYPE.NOMINAL ) {  // bin index is simply the value 
			   if( valuetobebinned < 0 ) {
				   System.err.println("NOMINAL bin value [" + valuetobebinned + "] is less than zero" );
				   return -1;
			   }
			   if( valuetobebinned >= binsegmentbegins.length  ) {
				   System.err.println("NOMINAL bin value [" + valuetobebinned + "] exceeds NBR_BINS [" + binsegmentbegins.length + "]");
				   return -1;
			   }
			   return (int)valuetobebinned;
		   }
           //		   
		   try {
			   int pdx=-1;
			   if( valuetobebinned < binsegmentbegins[0] ) return 0;
			   if( valuetobebinned >= binsegmentbegins[binsegmentbegins.length-1] ) return (binsegmentbegins.length-1);
			   for(int i=1 ; i< binsegmentbegins.length ; i++)
			   {
				   if( valuetobebinned < binsegmentbegins[i] ) { pdx = (i-1); break; }
			   }
			   if( pdx < 0 ) {
				   if( valuetobebinned == Double.MIN_VALUE ) pdx=0;  // just pick the first bin then
				   pdx = binsegmentbegins.length - 1; // just pick the last bin
			   }
			   return pdx;
			  }
			  catch( Exception e ) {
				   System.err.println("OOPS - cannot allocate a bin for [" + valuetobebinned + "]" );
				   return -1;
			  }
	   }
	   
	   //------------------------------------------------------------
	   public double getBinProbability( double valuetobebinned)
	   //------------------------------------------------------------
	   {
		  try {
		   int pdx = getBinIndex( valuetobebinned );
		   if( pdx < 0 ) return Double.NaN;
		   return BinProbs[ pdx ];
		  }
		  catch( Exception e ) {
			   System.err.println("OOPS - cannot allocate [" + valuetobebinned + "]" );
			   return Double.NaN;
		  }
	   }
	   
	   //------------------------------------------------------------
	   public double getBinClassProbability(String classlabel , double valuetobebinned)
	   //------------------------------------------------------------
	   {
		  try {
		   int idx = getClassIndex( classlabel );
		   if( idx < 0 ) return Double.NaN;
		   int pdx = getBinIndex( valuetobebinned );
		   if( pdx < 0 ) return Double.NaN;
		   return BinClassProbs[ pdx ][ idx ];
		  }
		  catch( Exception e ) {
			   System.err.println("OOPS - cannot allocate [" + classlabel + "] [" + valuetobebinned + "]" );
			   return Double.NaN;
		  }
	   }
	  
	   //------------------------------------------------------------
	   public int getHistogramEntries()
	   //------------------------------------------------------------
	   {
		   return this.numberOfElements;
	   }

	   //------------------------------------------------------------
	   public String dumper( double valuetobebinned )
	   //------------------------------------------------------------
	   {
		   String sRet = "->" + valuetobebinned;
		   sRet += " [idx=" + getBinIndex( valuetobebinned ) + "] ->";
		   for(int i=0 ; i< binsegmentbegins.length ; i++)
		   {
			   sRet += "[" + i + ","+ binsegmentbegins[i] + "]";
		   }
		   return sRet;
	   }
	   
}
