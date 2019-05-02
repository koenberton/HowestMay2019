package generalMachineLearning.DecisionTree;

import java.util.Random;

import cbrTekStraktorModel.cmcProcSettings;
import dao.cmcARFFDAO;
import dao.cmcMachineLearningModelDAO;
import featureExtraction.PredictDetail;
import generalMachineLearning.cmcMachineLearningConstants;
import generalMachineLearning.cmcMachineLearningEnums;
import generalMachineLearning.cmcModelMaker;
import generalMachineLearning.ARFF.ARFFCategory;
import generalMachineLearning.ARFF.ARFFCategoryCoreDTO;
import generalMachineLearning.ARFF.cmcARFF;
import generalMachineLearning.ARFF.cmcARFF.ARFFattribute;
import generalMachineLearning.Evaluation.cmcModelEvaluation;
import logger.logLiason;

public class cmcModelmakerDecisionTree implements cmcModelMaker {

	cmcProcSettings xMSet=null;
	logLiason logger=null;
    
	private static boolean DEBUG = false;
	private static boolean DEBUGFULL = false;
	
	private long GLOBAL_UID=0L; 
	//private long GLOBAL_UID=System.currentTimeMillis();
	
	PredictDetail[] predictlist=null;
	
	public class DecisionTreeCategoryBranch
	{
	   public String CategoryName;
	   public cmcARFF.ARFF_TYPE tipe= cmcARFF.ARFF_TYPE.UNKNOWN;
	   public String[] NominalNameList;
	   double TotalEntropy;
	   DecisionTreeMetrics[] detail;
	   
	   DecisionTreeCategoryBranch(String iname , cmcARFF.ARFF_TYPE itipe , String[] inomlist)
	   {
		   CategoryName = iname;
		   tipe = itipe;
		   NominalNameList=inomlist;
		   TotalEntropy = Double.NaN;
		   detail=null;
	   }
	   public boolean setValues( int[] ilines , double[] ivals , double[] iresults )
	   {
		   if( (ilines == null) || (ivals==null) || (iresults==null) ) {
			   do_error("Nulls op setvalues [" + this.CategoryName + "]");
			   return false;
		   }
		   if( ilines.length != ivals.length ) {
			   do_error("lines do not match vals ["  + this.CategoryName + "]");
			   return false;
		   }
		   if( ilines.length != iresults.length ) {
			   do_error("lines do not match results ["  + this.CategoryName + "]");
			   return false;
		   }
		   //
		   detail = new DecisionTreeMetrics[ ivals.length ];  
		   for(int i=0;i<ivals.length;i++)
		   {
			   detail[i] = new DecisionTreeMetrics( ilines[i] , ivals[i] , iresults[i] );
		   }
		   // sort on values
		   int swapper=0;
		   DecisionTreeMetrics zz = new DecisionTreeMetrics( -1 , Double.NaN , Double.NaN );
		   for(int i=0 ; i< detail.length ; i++)
		   {
			   boolean swap = false;
			   for(int j=0;j<(detail.length-1);j++)
			   {
				   if( detail[j].value <= detail[j+1].value ) continue;  // sorted on value
				   zz.shallowCopy( detail[j] );
				   detail[j].shallowCopy( detail[j+1] );
				   detail[j+1].shallowCopy( zz );
				   swap=true;   
				   //do_log( 1 , " --> " + left.value + " " + right.value );
			   }
			   if( swap == false ) break;
			   swapper++;
		   }
		   //do_log( 1 , " -->" + this.CategoryName + " SORT=>" + swapper);
		   // check
		   boolean sorted=true;
		   for(int i=0 ; i< detail.length-1 ; i++)
		   {
			   if( detail[i].value > detail[i+1].value ) sorted=false;
		   }
		   if( sorted==false ) {
			   do_error("values are not sorted ["  + this.CategoryName + "]");
			   return false;
		   }
		   return true;
	   }
	   public boolean calculateEntropyGains(String[] ClassNameList)
	   {
		   if( this.detail == null ) {
			   do_error("Null detail on calcualteEntropyGains");
			   return false;
		   }
		   if( this.tipe == cmcARFF.ARFF_TYPE.STRING) return true;
		   if( this.tipe == cmcARFF.ARFF_TYPE.CLASS) return true;
		   this.TotalEntropy = calculateEntropy( detail , ClassNameList , this.CategoryName + "-TOTAL");
		   if( Double.isNaN( this.TotalEntropy ) ) return false;
		   double splitvalue = Double.MIN_VALUE;
		   double maxGain= Double.NaN;
		   double maxGainValue=Double.NaN;
		   for(int i=0;i<detail.length;i++)
		   {
			  if( splitvalue == detail[i].value ) {  // same value again, just copy results
				  if( i <= 0 ) {
					  do_error("'System error 0");
					  return false;
				  }
				  detail[i].SplitEntropy = detail[i-1].SplitEntropy;
				  detail[i].SplitGain = detail[i-1].SplitGain;
				  continue;
			  }
			  splitvalue = detail[i].value;
			  int nleft = 0;
			  int nright =0;
			  for(int j=0;j<detail.length;j++)
			  {
				  if( detail[j].value <= splitvalue ) nleft++; 
				  			                     else nright++;
			  }
			  double leftEntropy = Double.NaN;
			  double rightEntropy = Double.NaN;
			  {
		        DecisionTreeMetrics[] left = new DecisionTreeMetrics[ nleft ];
		        int k=0;
		        for(int j=0;j<detail.length;j++)
			    {
				  if( detail[j].value <= splitvalue ) { left[k] = new DecisionTreeMetrics( detail[j].lineno , detail[j].value , detail[j].result); k++; }
			    }
		        leftEntropy = calculateEntropy( left , ClassNameList , this.CategoryName + "-LEFT-" + i);
			  }
			  {
		        if( nright > 0 ) {
			      DecisionTreeMetrics[] right = new DecisionTreeMetrics[ nright ];
			      int k=0;
			      for(int j=0;j<detail.length;j++)
				  {
					  if( detail[j].value > splitvalue ) { right[k] = new DecisionTreeMetrics( detail[j].lineno , detail[j].value , detail[j].result); k++; }
				  }
			      rightEntropy = calculateEntropy( right , ClassNameList , this.CategoryName + "-RIGHT-" + i);
		        }
		        else {  // there might be no values higher than splitvalue; if this is the case the entropy of left must match total
		          rightEntropy = Double.MAX_VALUE;
		          if( leftEntropy != TotalEntropy ) {
		        	  do_error( "System error I");
		        	  return false;
		          }
		        }
			  }
              if( detail.length != (nleft + nright) ) {
            	  do_error( "System error II");
	        	  return false;
			  }
		      //
			  detail[i].SplitEntropy = (((double)nleft / (double)detail.length) * leftEntropy) + (((double)nright / (double)detail.length) * rightEntropy);
			  detail[i].SplitGain    = TotalEntropy - detail[i].SplitEntropy;
			  if( detail[i].SplitGain < 0 ) {
				  do_error("Bizarre -> Negative gain [TotalEntropy=" + TotalEntropy + "] [SplitEntropy=" + detail[i].SplitEntropy + "] [Gain=" + detail[i].SplitGain + "]");
				  //return false;
			  }
			  if( (maxGain < detail[i].SplitGain) || (Double.isNaN(maxGain)) ) {
				  maxGain = detail[i].SplitGain;
				  maxGainValue = detail[i].value;
			  }
		   }
		   if( DEBUGFULL ) {
             do_log( 1 , "EntropyGain [" + 
                  String.format("%40s", this.CategoryName ) + "] [Total=" + 
		          String.format("%10.8f" , this.TotalEntropy ) + "] [MaxGain=" + 
		          String.format("%10.8f" , maxGain ) + "] [GainValue=" + 
		          String.format("%15.8f" , maxGainValue ) + "]");
		   }
		   return true;
	   }
	}
	
	
	public class DecisionTreeBranch
	{
		public DecisionTreeDTO dto = null;
		public DecisionTreeCategoryBranch[] subbranches = null;
		//
		public DecisionTreeBranch( int ilevel , long lparent , ARFFCategoryCoreDTO[] icategories , cmcMachineLearningEnums.BranchOrientation iorient)
		{
			dto = new DecisionTreeDTO();
			dto.UID = GLOBAL_UID++;
			dto.level=ilevel;
			dto.parentUID = lparent;
			dto.orientation = iorient;
			dto.ClassNameList = null;  
			dto.splitCategoryIndex=-1;
			dto.splitOnCategoryName=null;
			dto.splitValueIndex=-1;
			dto.splitOnValue = Double.NaN;
			dto.splitOnARFFTipe = cmcARFF.ARFF_TYPE.UNKNOWN;
			dto.splitOnNominalName=null;
			dto.NumberOfRows = -1;
			dto.LeafClassName=null;
			dto.FlippedACoin=false;
			dto.categories = icategories;
			//
			subbranches = new DecisionTreeCategoryBranch[ icategories.length ];
			// copy all values and results in root node
			for(int i=0;i< icategories.length;i++)
			{
				ARFFCategoryCoreDTO cat = icategories[i];
				subbranches[ i ] = new DecisionTreeCategoryBranch( cat.getCategoryName() , cat.getTipe() , cat.getNominalValueList() );
			}
		}
		
		public void show()
		{
			do_log( 1 , "=> Branch [" + this.dto.UID + "]");
			for(int i=0;i<subbranches.length;i++)
			{
			   if( subbranches[i] == null ) continue;
			   if( subbranches[i].detail == null ) continue;
			   String sl= subbranches[i].CategoryName + " [" + subbranches[i].detail.length + "] ";
			   for(int j=0;j<subbranches[i].detail.length;j++)	sl += "[" + subbranches[i].detail[j].value + "]";
			   do_log( 1 , sl );
			}
		}
		public int getNumberOfRows()
		{
			for(int i=0;i<subbranches.length;i++)
			{
			   if( subbranches[i] == null ) continue;
			   if( subbranches[i].detail == null ) continue;
			   return subbranches[i].detail.length;
			}
			return -1;
		}
		public String specs()
		{
		   return "[Node=" + this.dto.UID + "] [Level=" + this.dto.level + "] [#Rows=" + this.getNumberOfRows() + "]";
		}
		private boolean findBestGain()
		{
			// find the entry with largest gain
			double maxgain = Double.NaN;
			this.dto.splitCategoryIndex=-1;
			this.dto.splitValueIndex=-1;
			this.dto.splitOnValue=Double.NaN;
			int NumberOfSamples=-1;
			for(int i=0;i<subbranches.length;i++)
			{
				DecisionTreeCategoryBranch sub = this.subbranches[i];
				if( sub == null ) continue;
				if( (sub.tipe == cmcARFF.ARFF_TYPE.STRING) || (sub.tipe == cmcARFF.ARFF_TYPE.CLASS) ) continue;
				if(  NumberOfSamples < 0 ) NumberOfSamples = sub.detail.length;
				if(  NumberOfSamples != sub.detail.length ) {
					do_error("The number of details does not match");
					return false;
				}
				for(int j=0;j<sub.detail.length;j++)
				{
					if( Double.isNaN(sub.detail[j].SplitGain) ) continue;
					// Take the entry which comes last in a series
					if( (sub.detail[j].SplitGain >= maxgain) || (Double.isNaN(maxgain)) ) {
						this.dto.splitCategoryIndex = i;
						this.dto.splitValueIndex=j;
						this.dto.splitOnValue = sub.detail[j].value;
						maxgain = sub.detail[j].SplitGain;
					}
				}
			}
			if( (this.dto.splitCategoryIndex<0) || (this.dto.splitValueIndex<0) ) {
				do_error("Could not find a maximum gain");
				return false;
			}
			
			// a couple of checks
			if( NumberOfSamples <= 0 ) { //  0 and 1 are errors
				do_error("System error - number of samples is zero [" + NumberOfSamples + "]");
				return false;
			}
			else
			if( (NumberOfSamples == 1) && (this.dto.TotalEntropy != 0) ) { //  if there is only 1 entry entropu must be zero
				do_error("System error - number of samples is 1 and entropy is not zero [" + this.dto.TotalEntropy + "]");
				return false;
			}	
			else
			// This is OK total entropy is zero so the last index can be picked
			if( (this.dto.splitValueIndex == (NumberOfSamples-1)) && (this.dto.TotalEntropy == 0) ) {
				//do_error("Got one [Entr=" + this.TotalEntropy + "] [ValueIndex=" + splitValueIndex + "] [CatIndex=" + splitCategoryIndex + "]");
	            NumberOfSamples=NumberOfSamples; // NOP			
			}
			else
			// Check  category and index are the very last -> race condition
			// Total entropy is not zero and the Gain is zero  (probably - could also be identical)
			// This might indicate that the histogram are identical for different classes  (Bad ARFF file)
			// will result in continuously descending the levels without splitting
			if( (this.dto.splitValueIndex == (NumberOfSamples-1)) && (this.dto.TotalEntropy != 0) ) {
				boolean ib = performNoGainScenario(maxgain);
				if( ib == false ) return false;
			}
		    //	
			this.dto.splitOnCategoryName = subbranches[this.dto.splitCategoryIndex].CategoryName;
			this.dto.splitOnARFFTipe = subbranches[this.dto.splitCategoryIndex].tipe;
			if( this.dto.splitOnARFFTipe  == cmcARFF.ARFF_TYPE.NOMINAL ) {
				int idx = (int)this.dto.splitOnValue;
				String[] list =  subbranches[this.dto.splitCategoryIndex].NominalNameList;
				if( list == null ) {
					do_error("Cannot determine nominal label - NominalNameList is null");
					return false;
				}
				if( (idx<0) || (idx>=list.length) ) {
					do_error("Cannot determine nominal label - index out of bound");
					return false;
				}
				this.dto.splitOnNominalName = list[ idx ];
			}
			return true;
		}
		private boolean performNoGainScenario(double maxgain)
		{
			// Force a split
			// determine the classes with maxgain; choose a random one and then split in the middle
			// Should not happen - might indicate a duplicate line
			do_log( 1 , "Race condition scenario [Entropy=" + this.dto.TotalEntropy + "] [MaxGain=" + maxgain + "] [" + subbranches[this.dto.splitCategoryIndex].CategoryName + "]" );
			double[][] validcats = new double[ subbranches.length ][];
			for(int icat=0;icat<subbranches.length;icat++)
			{
				validcats[icat] = null;
				DecisionTreeCategoryBranch sub = this.subbranches[icat];
				if( sub == null ) continue;
				if( (sub.tipe == cmcARFF.ARFF_TYPE.STRING) || (sub.tipe == cmcARFF.ARFF_TYPE.CLASS) ) continue;
				if( sub.detail.length == 0 ) {
					do_error("(performNoGainscenario) no details");
					return false;
				}
				// make a list of all categories which have the same maxgain
				// loop through all these categories and count the number of different values
				boolean gotMaxGain = false;
				double swapvalue =  sub.detail[0].value - Math.PI;
				int nbrOfDifferentValues = 0;
				for(int j=0;j<sub.detail.length;j++)
				{
				   if( Double.isNaN(sub.detail[j].SplitGain) ) continue;
					if( sub.detail[j].SplitGain == maxgain ) gotMaxGain = true;
					if( swapvalue != sub.detail[j].value ) {
						swapvalue = sub.detail[j].value;
						nbrOfDifferentValues++;
					}
					do_log( 1 ,  "(noGain) [" + sub.CategoryName + "] V=" + sub.detail[j].value + " R=" + sub.detail[j].result + " L=" +  sub.detail[j].lineno); 
				}
				if( gotMaxGain == false ) continue;
				swapvalue =  sub.detail[0].value - Math.PI;
				validcats[icat] = new double[nbrOfDifferentValues];
				nbrOfDifferentValues=0;
				for(int j=0;j<sub.detail.length;j++)
				{
				   if( Double.isNaN(sub.detail[j].SplitGain) ) continue;
				   if( sub.detail[j].SplitGain == maxgain ) gotMaxGain = true;
				   if( swapvalue != sub.detail[j].value ) {
						swapvalue = sub.detail[j].value;
						validcats[icat][ nbrOfDifferentValues ] = sub.detail[j].value;
						nbrOfDifferentValues++;
				   }
				}
				
			}
			// display
			for(int i=0;i<subbranches.length;i++)
			{
				if( validcats[i] == null ) continue;
				String sl = "(noGain) [" + subbranches[i].CategoryName + "] [#=" + subbranches[i].detail.length + "]";
				for(int j=0;j<validcats[i].length;j++ ) sl += "[" + validcats[i][j] + "]";
				do_log( 1 , sl );
			}
		    // if there are no different values, then COIN FLIP; else pick a category and pick a value different from the last one
			// different values not implemented - just flip a coin always
			int idx =-1;
			Random rn = new Random();
		    for(int i=0;i<1000;i++)
		    {
		      idx = rn.nextInt(subbranches.length);
		      if( validcats[ idx ] != null ) break;
		    }
		    if( idx < 0 ) {
		    	do_error("Could not find a random category");
		    	return false;
		    }
			int pdx = validcats[idx].length / 2;   // pick the middle one
			try {
				this.dto.splitCategoryIndex = idx;
				this.dto.splitValueIndex = pdx;
				this.dto.splitOnValue = subbranches[idx].detail[pdx].value;
				this.dto.FlippedACoin = true;
				do_log( 1 , "Flipped a coin [Cat=" + this.dto.splitCategoryIndex + "] [Val=" + this.dto.splitOnValue + "]");
			}
			catch( Exception e ) {
				do_error("Could not randomly select a category and value");
				return false;
			}
			return true;
		}
		public boolean calculateAllEntropyGains()
		{
        	if( DEBUG ) do_log( 1 , "Calculate E-Gain " + this.specs() );
			double ClassEntropy=Double.NaN;
			if( subbranches == null ) {
				do_error("Sub Branch not initiated");
				return false;
			}
			this.dto.NumberOfRows = this.getNumberOfRows();
			for(int i=0;i<subbranches.length;i++)
			{
				DecisionTreeCategoryBranch sub = this.subbranches[i];
				if( sub.tipe == cmcARFF.ARFF_TYPE.STRING) continue;
				if( sub.tipe == cmcARFF.ARFF_TYPE.CLASS ) {
					ClassEntropy = calculateEntropy( sub.detail , dto.ClassNameList , "Branch-" + this.dto.level + "-ClassEntropy");
					if( Double.isNaN(ClassEntropy) ) return false;
					this.dto.TotalEntropy = ClassEntropy;
					if( DEBUG ) do_log( 1 , "=> Verification [TotalEntropy=" + this.dto.TotalEntropy + "] [#Items=" + sub.detail.length + "]");
					continue;
				}
				if( sub.calculateEntropyGains(this.dto.ClassNameList) == false ) return false;
			}
			// check - the total entropy per category must match the ClassEntropy
			for(int i=0;i<subbranches.length;i++)
			{
				DecisionTreeCategoryBranch sub = this.subbranches[i];
				if( (sub.tipe == cmcARFF.ARFF_TYPE.STRING) || (sub.tipe == cmcARFF.ARFF_TYPE.CLASS) ) continue;
				if( sub.TotalEntropy != this.dto.TotalEntropy ) {
					do_error("Total entropy on sub does not match branch entropy [" + sub.CategoryName + "] [" + sub.tipe + "] [SUBTOT=" + sub.TotalEntropy + "] [CLASSTOT=" + this.dto.TotalEntropy + "]");
					return false;
				}
			}
			//
		    if( findBestGain() == false ) return false;
			
do_log( 1 , "Max E-Gain [Node="+ this.dto.UID + "] ["  +
             subbranches[this.dto.splitCategoryIndex].CategoryName + 
           "] [Value=" + this.dto.splitOnValue + 
           "] [E-Split=" + subbranches[this.dto.splitCategoryIndex].detail[this.dto.splitValueIndex].SplitEntropy + 
           "] [Gain=" + subbranches[this.dto.splitCategoryIndex].detail[this.dto.splitValueIndex].SplitGain + 
           "] [E-Tot=" + this.dto.TotalEntropy + "]" );

			return true;
		}
		
		public String getLeafClassName()
		{
			if( this.dto.treetipe != cmcMachineLearningEnums.DecisionTreeType.LEAF ) return null;
			// all entries must be the same - entropy is null
			double classindex=Math.PI;
			for(int i=0;i<subbranches.length;i++)
			{
				DecisionTreeCategoryBranch sub = this.subbranches[i];
				if( sub.tipe == cmcARFF.ARFF_TYPE.STRING) continue;
			    for(int z=0;z<sub.detail.length;z++)
			    {
			    	if (classindex == Math.PI ) classindex = sub.detail[z].result;
			    	if( classindex != sub.detail[z].result ) {
			    		do_error("There are multiple result value son a lead node");
			    		return null;
			    	}
			    }
			}
			if( classindex == Math.PI ) {
				do_error("Could not determine LEAF value");
				return null;
			}
			int idx = (int)classindex;
			if( (idx<0) || (idx> this.dto.ClassNameList.length) ) {
				do_error("Invalid classindex");
				return null;
			}
			return this.dto.ClassNameList[ idx ];
		}
		
	} // BRANCH
	
	
	
	//
	DecisionTreeBranch[] DecisionTree = null;
	
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
    public cmcModelmakerDecisionTree(cmcProcSettings is,logLiason ilog)
    //------------------------------------------------------------
    {
    	xMSet = is;
		logger=ilog;
    }
    
    //------------------------------------------------------------
    private double calculateEntropy( DecisionTreeMetrics[] dets , String[] ClassNameList , String label)
    //------------------------------------------------------------
	{
    	try {
		if( ClassNameList == null ) {
			do_error("(calculateEntropy) NULL Classnamelist" + label );
			return Double.NaN;
		}
		if( dets == null ) {
			do_error("(calculateEntropy) Null detals" + label );
			return Double.NaN;
		}
		if( dets.length == 0 ) {
			do_error("(calculateEntropy) Empty detail " + label);
			return Double.NaN;
		}
		int nclasses=ClassNameList.length;
		int[] histo = new int[ nclasses ];
		for(int i=0;i<nclasses;i++) histo[i] = 0;
		for(int i=0;i<dets.length;i++)
		{
			int idx = (int)dets[i].result;
			if( (idx<0) || (idx>=nclasses) ) {
				do_error("(calculateEntropy) Invalid result " + label);
				return Double.NaN;
			}
			histo[idx]++;
		}
		double entro=0;
		for(int i=0;i<histo.length;i++)
		{
			double proba = (double)histo[i] / (double)dets.length;
			if( proba == 0 ) continue;
			entro = entro - (proba * Math.log10( proba ));
		}
		return entro;
    	}
    	catch(Exception e) {
    		do_error("(calculateEntropy) " + xMSet.xU.LogStackTrace(e));
    		return Double.NaN;
    	}
	}
    
    private ARFFCategoryCoreDTO[] makeProxyCategories( ARFFCategory[] categories )
    {
    	ARFFCategoryCoreDTO[] pcats = new ARFFCategoryCoreDTO[ categories.length ];
    	for(int i=0;i<pcats.length;i++)
    	{
    	    pcats[i] = new ARFFCategoryCoreDTO( categories[i].getCategoryName() );
    	    pcats[i].setNominalValueList( categories[i].getNominalValueList() );
    	    pcats[i].setTipe( categories[i].getTipe() );
    	}
        return pcats;    	
    }
    //------------------------------------------------------------
    private DecisionTreeBranch makeRootNode(ARFFCategory[] categories ,  String[] cllist )
    //------------------------------------------------------------
    {
    	// transform categories into categoryproxy
    	ARFFCategoryCoreDTO[] pcats = makeProxyCategories(categories);
    	DecisionTreeBranch root = new DecisionTreeBranch( 0 , -1L , pcats , cmcMachineLearningEnums.BranchOrientation.UNKNOWN );
    	root.dto.treetipe = cmcMachineLearningEnums.DecisionTreeType.ROOT;
    	root.dto.ClassNameList = cllist;
    	for(int i=0;i<root.subbranches.length;i++)
    	{
    		DecisionTreeCategoryBranch sub = root.subbranches[i];
    		if( sub.tipe == cmcARFF.ARFF_TYPE.STRING ) { // just use the lineno as the value -- probably not necessary
    			for(int j=0;j<categories[i].getValues().length;j++) categories[i].getValues()[j] = (double)categories[i].getArfflineno()[j];
    		}
    		if( sub.setValues( categories[i].getArfflineno() , categories[i].getValues() , categories[i].getResults() ) == false ) return null;
    	}
    	return root;
    }
    
    // Realigning is done by sorting on the LINENO of the ARFF file
    //------------------------------------------------------------
    private boolean reAlignCategory(DecisionTreeCategoryBranch sub )
    //------------------------------------------------------------
    {
    	if( sub.detail == null ) {
    		do_error( "(reAlignCategory) Detail on sub cat is null [" + sub.CategoryName + "]" );
    		return false;
    	}
    	if( sub.detail.length < 1) {
    		do_error("Nothing to realign [" + sub.CategoryName + "]");
    		return false;
    	}
    	int swapper=0;
    	DecisionTreeMetrics zz = new DecisionTreeMetrics( -1 , Double.NaN , Double.NaN );
    	for(int i=0;i<sub.detail.length;i++)
    	{
    		boolean swap = false;
    		for(int j=0;j<(sub.detail.length-1);j++)
    		{
				   if( sub.detail[j].lineno <= sub.detail[j+1].lineno ) continue;
				   zz.shallowCopy( sub.detail[j] );
				   sub.detail[j].shallowCopy( sub.detail[j+1] );
				   sub.detail[j+1].shallowCopy( zz );
				   swap=true;  
					//do_log( 1 , " --> REALIGN =>" + left.lineno + " " + right.lineno );
    		}
    		if( swap == false ) break;
    		swapper++;
    	}
    	// check
    	boolean sorted=true;
		for(int i=0 ; i< sub.detail.length-1 ; i++)
		{
		   if( sub.detail[i].lineno > sub.detail[i+1].lineno ) sorted=false;
		}
		if( sorted==false ) {
		   do_error("values are not sorted on lineno ["  + sub.CategoryName + "]");
		   return false;
		}
    	return true;
    }

    //------------------------------------------------------------
    private boolean reAlignCategories(DecisionTreeCategoryBranch[] subbra )
    //------------------------------------------------------------
    {
    	for(int i=0;i<subbra.length;i++)
    	{
    		DecisionTreeCategoryBranch sub = subbra[i];
    		if( sub == null ) continue;
    		//if( sub.tipe == cmcARFF.ARFF_TYPE.STRING ) continue;   JUST SORT anything
    		//if( sub.tipe == cmcARFF.ARFF_TYPE.CLASS ) continue;
        	if( reAlignCategory( sub ) == false ) return false;	
    	}
    	return true;
    }

    //------------------------------------------------------------
    private int findLineNo( int lkp , DecisionTreeMetrics[] detail )
    //------------------------------------------------------------
    {
      for(int i=0;i<detail.length;i++) if( detail[i].lineno == lkp ) return i;
      return -1;
    }
 
    //------------------------------------------------------------
    private DecisionTreeBranch makeBranch( DecisionTreeBranch parent , ARFFCategoryCoreDTO[] categories  , double splitvalue , int nrows , cmcMachineLearningEnums.BranchOrientation orientation )
    //------------------------------------------------------------
    {
    	
    	DecisionTreeBranch work = new DecisionTreeBranch( parent.dto.level + 1 , parent.dto.UID , categories , orientation );
    	work.dto.treetipe = cmcMachineLearningEnums.DecisionTreeType.BRANCH;
    	work.dto.ClassNameList = parent.dto.ClassNameList;
    	for(int i=0;i<work.subbranches.length;i++)
    	{ 
    		DecisionTreeCategoryBranch sub = work.subbranches[i];
    		//if( sub.tipe == cmcARFF.ARFF_TYPE.STRING ) continue;
    		double[] dvals = new double[ nrows ];
    		double[] dres = new double[ nrows ];
    		int[] ilines = new int[ nrows ];
    		int k=0;
    		for( int j=0;j<  parent.subbranches[i].detail.length ;j++)
    		{
    		    //	
    			if( orientation == cmcMachineLearningEnums.BranchOrientation.LEFT ) { // LEFT - keep what is less or equal than splitvalue
    			 if( parent.dto.FlippedACoin ) {
    				 if( j > parent.dto.splitValueIndex ) continue;
    			 }
    			 else
    			 if( parent.subbranches[parent.dto.splitCategoryIndex].detail[j].value > splitvalue ) continue; // split op de splitcategory
    			}
    			else 
    			if( orientation == cmcMachineLearningEnums.BranchOrientation.RIGHT ) {  // RIGHT - keep what is higher than splitvalue
    			 if( parent.dto.FlippedACoin ) {
       				 if( j <= parent.dto.splitValueIndex ) continue;
       			 }
       			 else
       			 if( parent.subbranches[parent.dto.splitCategoryIndex].detail[j].value <= splitvalue ) continue; // split op de splitcategory
    			}
    			else {
    			  do_error("Unsupported orientation [" + orientation + "]");
    			  return null;
    			}
    			// run a couple of tests - lineno must match
    			int aidx = parent.subbranches[parent.dto.splitCategoryIndex].detail[j].lineno;
    			int bidx = parent.subbranches[i].detail[j].lineno;
    			if( aidx != bidx ) {
    				int idx = findLineNo( aidx , parent.subbranches[i].detail );
    				do_error("ARFF entries not aligned " + parent.subbranches[i].CategoryName + " [" + bidx +"] expecting [" + aidx + "] [j=" + j + "]" + splitvalue + " " + idx);
    				do_error("ARFF res [" + parent.subbranches[parent.dto.splitCategoryIndex].detail[j].result + " ? " + parent.subbranches[i].detail[idx].result + "]");
    				return null;
    			}
    			// Results must match
    			double ares = parent.subbranches[parent.dto.splitCategoryIndex].detail[j].result;
    			double bres = parent.subbranches[i].detail[j].result;
    			if( ares != bres ) {
    				int idx = findLineNo( aidx , parent.subbranches[i].detail );
    	            do_error("ARFF results are not aligned " + parent.subbranches[i].CategoryName + " [" + bres +"] expecting [" + ares + "] [j=" + j + "]" + splitvalue + " " + idx);
    	        	return null;
    			}
    			// 
    			dvals[ k] = parent.subbranches[i].detail[j].value;
    			dres[ k ] = parent.subbranches[i].detail[j].result;
    			ilines[ k ] = parent.subbranches[i].detail[j].lineno;
    			k++;
//do_log( 1 ," " + parent.subbranches[i].detail[j].value + " " + parent.subbranches[i].detail[j].result + " " + parent.subbranches[parent.subbranches.length-1].detail[j].value );
    		}
    		if( sub.setValues( ilines , dvals , dres ) == false ) return null;
    	}
    	return work;
    }
    
    //------------------------------------------------------------
    private DecisionTreeBranch[] splitBranch( DecisionTreeBranch parent , ARFFCategoryCoreDTO[] categories )
    //------------------------------------------------------------
    {
    	if( parent.dto.splitCategoryIndex < 0 ) return null;
    	if( parent.dto.splitValueIndex < 0 ) return null;
    	
    	//
    	int nleft = 0;
    	int nright = 0;
    	double splitvalue = parent.subbranches[parent.dto.splitCategoryIndex].detail[ parent.dto.splitValueIndex ].value;
    	for(int i=0;i<parent.subbranches[parent.dto.splitCategoryIndex].detail.length;i++)
    	{
    		if( parent.subbranches[parent.dto.splitCategoryIndex].detail[i].value <= splitvalue ) nleft++; else nright++;
    	}
    	if( parent.subbranches[parent.dto.splitCategoryIndex].detail.length != (nleft + nright ) ) {
    		do_error("Could not split - sum mismatch");
    		return null;
    	}
    	// Exceptional case
    	if( nright == 0 ) {
    		if( parent.dto.FlippedACoin == false ) {
    			do_error( "There are no left entries right and not FlippedCoin");
    			return null;
    		}
    		nleft = parent.dto.splitValueIndex + 1;
    		nright = parent.subbranches[parent.dto.splitCategoryIndex].detail.length - nleft;
    	}
        if( DEBUG ) do_log( 1 , "Splitting [Node=" + parent.dto.UID + "] on [" + splitvalue + "] [#Left=" + nleft + "] [#Right=" + nright + "] " + (parent.dto.FlippedACoin ? "FLIPPED-A-COIN" : "") );
    	
    	// realign
    	// the various entries in the categories need to be realigned as they were on the ARFF, so sort on lineno
    	if (reAlignCategories( parent.subbranches ) == false) return null;
    	
    	//
        DecisionTreeBranch[] result = new DecisionTreeBranch[2];
        result[0] = result[1] = null;
    	result[0] = makeBranch( parent , categories , splitvalue , nleft , cmcMachineLearningEnums.BranchOrientation.LEFT );
        if( result[0] == null ) return  null;
        if( nright > 0 ) {
         result[1] = makeBranch( parent , categories , splitvalue , nright , cmcMachineLearningEnums.BranchOrientation.RIGHT );
         if( result[1] == null ) return null;
        }
        else {   // RIGHT branch has no rows - check whether the splitvalue is indeed the last one 
        	int lastidx = parent.subbranches[parent.dto.splitCategoryIndex].detail.length - 1;
        	if( lastidx < 0 ) {
        		do_error("last index is not postive");
        		return null;
        	}
        	double lastvalue = parent.subbranches[parent.dto.splitCategoryIndex].detail[lastidx].value;
        	if( splitvalue != lastvalue ) {
        		do_error("Last value and splitvalue do not match and right branch empty [LastValue=" + lastvalue + "] [SplitValue=" + splitvalue + "]");
        		//return null;
        	}
        	// 
        	result[1] = null; // OK
        	// DEBUG JANUARY
         	do_error( "NO RIGHT BRANCH - this should no longer be the case");
        	return null;
       
        }
        
        return result;
    }

    //------------------------------------------------------------
    private int getFreeSpotOnStack()
    //------------------------------------------------------------
    {
    	for(int z=0;z<(DecisionTree.length-1);z++) 
    	{
    		if( DecisionTree[z] == null) return z;
    	}
        return -1;
    }
    
    //------------------------------------------------------------
	public boolean trainModel(	String OriginalFileName , String ModelFileName )
	//------------------------------------------------------------
    {
		do_error( "SYTEM ERROR");
    	return false;
    }
    //------------------------------------------------------------
    public boolean trainModel(	ARFFCategory[] fullcategories , int nbins , String[] ClassNameList , String ModelFileName )
    //------------------------------------------------------------
    {
    	DecisionTree = new DecisionTreeBranch[ cmcMachineLearningConstants.MAX_NBR_OF_BRANCHES ];
        for(int i=0;i<DecisionTree.length;i++) DecisionTree[i] = null;
        
        ARFFCategoryCoreDTO[] categories = makeProxyCategories( fullcategories );
        if( categories == null ) return false;
        if( categories.length < 1 ) return false;
        
        try {
    	// Make root node
        DecisionTree[0] = makeRootNode( fullcategories , ClassNameList );
        if( DecisionTree[0] == null ) return false;
        // loop trough the nodes and split them per level
        boolean spillover=false;
        for(int level=0;level<cmcMachineLearningConstants.MAX_NBR_OF_LEVELS;level++)
        {
         if( DEBUG ) do_log( 1 , "[---------- Level " + level + " -----------]");
         int processed=0;
         for(int z=0;z<DecisionTree.length;z++)
         {
        	DecisionTreeBranch cur = DecisionTree[z];
        	if( cur == null ) continue;
        	if( cur.dto.level != level ) continue;
        	
        	// calculate entropy gain on each category/subbranch
         	if( cur.calculateAllEntropyGains() == false ) return false;
         	//  if the entropy of cur is zero then set to leaf and continue
         	if( cur.dto.TotalEntropy == 0 ) {
        		cur.dto.treetipe = cmcMachineLearningEnums.DecisionTreeType.LEAF;
                cur.dto.LeafClassName = cur.getLeafClassName();
                if( cur.dto.LeafClassName == null ) {
                	do_error("Cannot determine classname for leaf");
                	return false;
                }
         		if( DEBUG ) do_log( 1 , "**** LEAF [" + cur.dto.UID + "] [Class=" + cur.dto.LeafClassName + "]");
         		continue;
         	}
        	//
        	DecisionTreeBranch[] leftright = splitBranch( cur , categories );
        	if( leftright == null ) {
        		do_error("Could not split branch");
        		return false;
        	}
        	// Add the split branches to the stack
        	int idx = getFreeSpotOnStack();
        	if( idx < 0 ) { do_error("no place left to store the node "); spillover=true; break; }
        	if( leftright[0] != null ) {
        		processed++;
        		DecisionTree[ idx ] = leftright[0];   
        		if( DEBUG ) do_log(1,"(trainModel) Added " + leftright[0].specs() ); }  
        	if( leftright[1] != null ) {
        		processed++;
        		DecisionTree[ idx+1 ] = leftright[1]; 
        		if( DEBUG ) do_log(1,"(trainModel) Added " + leftright[1].specs()); }
         } // z
         if( spillover ) break;
         if( processed == 0 ) break;
         if( (level + 1) >= cmcMachineLearningConstants.MAX_NBR_OF_LEVELS ) spillover=true;
        }
    	if( spillover ) {
    		do_error("Maximum number of levels or branches reached - System error - increase array sizes");
    		return false;
    	}
    	// 
    	if( checkModel() == false ) return false;
    	//
    	if( dumpModel(ModelFileName) == false ) return false;
    	//
    	return true;
        }
        catch( Exception e) {
        	do_error("(trainModel) " + xMSet.xU.LogStackTrace(e));
        	return false;
        }
    }
   
    //------------------------------------------------------------
    private boolean checkModel()
    //------------------------------------------------------------
    {
    	boolean isOK=true;
    	//
    	int RootEntries=-1;
    	int LeafEntries=0;
    	for(int i=0;i<DecisionTree.length;i++)
    	{
    		DecisionTreeBranch node = DecisionTree[i];
        	if( node == null ) continue;
    	    if( node.dto.treetipe == cmcMachineLearningEnums.DecisionTreeType.ROOT ) RootEntries = node.dto.NumberOfRows;
    	    if( node.dto.treetipe == cmcMachineLearningEnums.DecisionTreeType.LEAF ) {
    	    	LeafEntries += node.dto.NumberOfRows;
    	    	if( node.getLeafClassName() == null ) {
    	    		do_error( "Leaf has an invalid result [" + node.dto.UID + "]");
    	    		isOK=false;
    	    	}
    	    }
    	}
    	if( RootEntries != LeafEntries ) {
    		do_error("Aggregate number of leaf records does not match Root records");
    		isOK=false;
    	}
    	//
    	
    	return isOK;
    }
    
    //------------------------------------------------------------
    private boolean dumpModel(String LongFileName)
    //------------------------------------------------------------
    {
    	if( DecisionTree == null ) { do_error("Null decision tree"); return false; }
    	if( DecisionTree.length <= 0 ) { do_error("Empty decision tree"); return false; }
        
    	DecisionTreeDTO[] dtolist = new DecisionTreeDTO[ DecisionTree.length ];
    	for(int i=0;i<dtolist.length;i++) {
    		if( DecisionTree[i] == null) dtolist[i] = null;
    		                        else dtolist[i] = DecisionTree[i].dto;
    	}
    	cmcMachineLearningModelDAO dao = new cmcMachineLearningModelDAO( xMSet , logger );
    	boolean ib = dao.treeWalk( dtolist , null );
    	if( ib == false ) return false;
    	ib = dao.writeDecisionTreeModel( dtolist , LongFileName );
    	dao=null;
    	if( ib == false ) do_error("Could not write model file");
    	return ib;
    	
    }
  
    //------------------------------------------------------------
    public boolean readDecisionTreeModel( String ModelFileName )
    //------------------------------------------------------------
    {
    	 DecisionTreeDTO[] dtolist = null;
    	 cmcMachineLearningModelDAO moddao = new cmcMachineLearningModelDAO( xMSet , logger );
         dtolist = moddao.readDecisionTreeModel(ModelFileName);
         if( dtolist == null ) return false;
         if( dtolist.length < 1) { do_error("Empty model"); return false; }
         DecisionTree=null;
         DecisionTree = new DecisionTreeBranch[ dtolist.length ];
         for(int i=0;i<dtolist.length;i++)
         {
        	 DecisionTree[i] = new DecisionTreeBranch( dtolist[i].level , dtolist[i].parentUID, dtolist[i].categories , dtolist[i].orientation );
        	 DecisionTree[i].dto = dtolist[i];
         }
         return true;
    }
    
    //------------------------------------------------------------
    public boolean testModel( String ModelFileName , String FileNameTestSet , cmcARFF xa)
    //------------------------------------------------------------
    {
    	 // Read the model from the modelfile
    	 if( readDecisionTreeModel( ModelFileName ) == false ) return false;
         
         // check the internal quality of the model
    	 //modstat = MODELING_STATUS.EVALUATING;
         cmcModelEvaluation bres = makeConfusionMatrix( xa , DecisionTree );
         if( bres == null ) return false;
         bres.calculate();
       
         // run the bayes model against the test set
    	 //modstat = MODELING_STATUS.TESTING;
         cmcARFFDAO dao = new cmcARFFDAO( xMSet , logger );
    	 if( dao.performFirstPass(FileNameTestSet) == false )  return false;
    	 do_log(1,"ARRF[" + FileNameTestSet + "] [Attribs=" + dao.getNumberOfAttributes() +"][Rows=" + dao.getNumberOfDataRows() + "]");
    	
    	 // second pass - read the data from the training set
    	 xa = dao.performSecondPass(FileNameTestSet);
    	 if( xa == null ) return false;
        
    	 // check the quality of the model against test set
    	 cmcModelEvaluation tres = makeConfusionMatrix( xa , DecisionTree );
         if( tres == null ) return false;
         tres.calculate();
    	
         // write back the results of the Evaluation
         if( insertEvaluation ( ModelFileName , bres, tres ) == false ) return false;
           	
         dao=null;
         return true;
    }
    
  //------------------------------------------------------------
    private boolean insertEvaluation( String ModelFileName , cmcModelEvaluation bres , cmcModelEvaluation tres )
    //------------------------------------------------------------
    {
    	do_log( 1 , "Model accuracy [" + bres.getAccuracy() + "]");
    	do_log( 1 , bres.showConfusionMatrix() );
    	do_log( 1 , "Train accuracy [" + tres.getAccuracy() + "]");
    	do_log( 1 , tres.showConfusionMatrix() );
        //	
    	cmcMachineLearningModelDAO dao = new cmcMachineLearningModelDAO( xMSet ,logger );
        boolean ib = dao.insertEvaluation( bres , tres , null , ModelFileName );
    	dao = null;
    	return ib;
    }
    
    //------------------------------------------------------------
    public cmcModelEvaluation makeConfusionMatrix( cmcARFF xa  )
    //------------------------------------------------------------
    {
    	return  makeConfusionMatrix( xa , DecisionTree );
    }
    //------------------------------------------------------------
    private cmcModelEvaluation makeConfusionMatrix( cmcARFF xa , DecisionTreeBranch[] model )
    //------------------------------------------------------------
    {
    	if( model == null ) { do_error( "Model is null"); return null; }
    	if( model.length <= 0 ) { do_error("Model is empty"); return null; }
    	if( xa == null ) { do_error("ARFF is null"); return null; }
    	if( xa.getAttributeList() == null ) { do_error("Attribute list is null"); return null; }
    	if( xa.getAttributeList().length <= 0 ) { do_error("Attribute list is empty"); return null; }
    	//
    	int nattribs = xa.getAttributeList().length;
    	int ndatarows=-1;
    	int valid=0;
    	for(int i=0;i<nattribs;i++)
    	{
    	   ARFFattribute attrib = xa.getAttributeList()[i];
    	   if( attrib == null ) continue;
    	   if( attrib.getTipe() == cmcARFF.ARFF_TYPE.STRING ) continue;
    	   if( attrib.getTipe() == cmcARFF.ARFF_TYPE.CLASS ) continue;
    	   if( ndatarows < 0 ) ndatarows = attrib.getValues().length;
    	   if( ndatarows != attrib.getValues().length ) {
    		   do_error("The number of data rows does not match across attributes");
    		   return null;
    	   }
    	   valid++;
    	}
    	if( valid == 0 ) {
    		do_error("There are no attributes to process");
    		return null;
    	}
    	if( ndatarows < 0 ) {
    		do_error("There are no rows to process");
    		return null;
    	}
        if( xa.getAttributeList()[ xa.getAttributeList().length - 1].getTipe() != cmcARFF.ARFF_TYPE.CLASS ) {
        	do_error("Last attrinbute is not of type CLASS");
        	return null;
        }
        //
        if( model.length < 1 ) { do_error("Empty decision tree model"); return null; }
        DecisionTreeBranch root = model[0];
        if( root.dto.treetipe != cmcMachineLearningEnums.DecisionTreeType.ROOT ) { do_error("First branch is not a root"); return null; }
        String[] ClassNameList = root.dto.ClassNameList;
        if( ClassNameList == null ) { do_error("Classnamelist is null"); return null; }
        if( ClassNameList.length <= 0 ) { do_error("Classnamelist is empty"); return null; }
        cmcModelEvaluation eval = new cmcModelEvaluation( ClassNameList );
        
        // loop through all rows
    	double[] inputvalues = new double[ nattribs ];
    	String[] inputcatnames = new String[ nattribs ];
  	    for(int i=0; i< nattribs; i++ ) {
		  ARFFattribute attrib = xa.getAttributeList()[i];
		  if( attrib == null ) continue;
		  if( attrib.getAttrName() == null ) {
			  do_error("NULL categoryname");
			  return null;
		  }
		  inputcatnames[ i ] = attrib.getAttrName().trim();
		}
  	    //
  	    predictlist=new PredictDetail[ndatarows];
        for(int i=0;i<ndatarows;i++) predictlist[i]=null;
  	    String actual="?";
  	    int hits=0;
  	    for(int row=0; row < ndatarows ; row++)
    	{
    	   for(int i=0; i< nattribs; i++ ) inputvalues[i] = Double.NaN;
    	   String FileName = null;
    	   for(int col=0;col<nattribs;col++)
    	   {
    		  ARFFattribute attrib = xa.getAttributeList()[col];
    		  if( attrib == null ) continue;
    		  if( (attrib.getTipe() == cmcARFF.ARFF_TYPE.STRING) && (FileName==null) ) {
  				FileName = xa.getAttributeList()[col].getSValue( row );
  			  }
    		  if( attrib.getTipe() == cmcARFF.ARFF_TYPE.STRING ) continue;
       	      if( attrib.getTipe() == cmcARFF.ARFF_TYPE.CLASS ) { actual = attrib.getSValue(row); continue; }
              inputvalues[ col ] = attrib.getValue( row );
          }
    	  String predicted = predict( inputvalues , inputcatnames , model );
          if( predicted == null ) {
        	  do_error("Could not predict");
        	  return null;
          }
          //
          if( predicted.compareToIgnoreCase(actual) == 0 ) hits++;
          //
          if( eval.addToConfusion( predicted , actual) == false ) {
  			do_error("Could not add to confusion matrix [" + predicted + "," + actual + "]");
  			return null;
  		  }
      	  predictlist[row] = new PredictDetail( FileName , actual , predicted );
     	}
    	if( DEBUG ) do_log( 1 , "Total=" + ndatarows + " Hits=" + hits + " Prec=" + ((hits * 100) / ndatarows) + "%" );
    	return eval;
    }
   
    //------------------------------------------------------------
    public PredictDetail[] getPredictionResults()
    //------------------------------------------------------------
    {
    	return predictlist;
    }
    
    //------------------------------------------------------------
    private String predict ( double[] inputvalues , String[] catnames , DecisionTreeBranch[] model)
    //------------------------------------------------------------
    {
  	    if( model == null ) { do_error("(getNextBranch) NULL model"); return null; }
     	DecisionTreeBranch rule = model[0];  // go for the roor
    	for(int z=0;z<(cmcMachineLearningConstants.MAX_NBR_OF_LEVELS+1);z++) // should stop after max level but hey ..
    	{
    		DecisionTreeBranch  next = getNextBranch( inputvalues , catnames , rule , model );
    		if( next == null ) return  null;
    		if( next.dto.treetipe == cmcMachineLearningEnums.DecisionTreeType.LEAF ) {
    			String res = next.dto.LeafClassName;
    			if( res == null ) {
    				do_error("LEAF node does not have a class name");
    				return null;
    			}
    			return res;
    		}
    		rule = next;
    	}
    	return null;
    }
   
    //------------------------------------------------------------
    private DecisionTreeBranch getNextBranch( double[] inputvalues , String[] catnames , DecisionTreeBranch rule , DecisionTreeBranch[] model )
    //------------------------------------------------------------
    {
          if( rule == null ) { do_error("(getNextBranch) NULL rule"); return null; }
          if( catnames == null ) { do_error("(getNextBranch) NULL category name list"); return null; }
          String rulecatname =  rule.dto.splitOnCategoryName;
          if( rulecatname == null ) { do_error("(getNextBranch) NULL category name"); return  null; }
          //       
          int idx = -1;
          rulecatname = rulecatname.trim();
          for(int i=0;i<catnames.length;i++)
          {
        	if( catnames[i] == null ) continue;
        	if( catnames[i].compareToIgnoreCase( rulecatname ) != 0 ) continue;
        	idx = i;
        	break;
          }
          if( idx < 0 ) {
        	  do_error("Cannot locate category [" + rulecatname + "] on rule [" + rule.dto.UID + "]");
        	  return null;
          }
          double testvalue = inputvalues[ idx ];
          if( Double.isNaN( testvalue ) ) {
        	  do_error("There is a NAN on the inputvalue list [" + idx + "] for [" + rulecatname + "]");
        	  return null;
          }
          cmcMachineLearningEnums.BranchOrientation orient = (testvalue <= rule.dto.splitOnValue) ? cmcMachineLearningEnums.BranchOrientation.LEFT : cmcMachineLearningEnums.BranchOrientation.RIGHT;
          
    //do_log( 1, "Rule Category=" + rulecatname + " splitvalue=" + rule.splitOnValue + " input=" + testvalue + " " + orient );
          
          // If FLIP A COIN the flip a coin to decide to go left or right
          if( rule.dto.FlippedACoin ) {
        	  Random rn = new Random();
  		      int flipper = rn.nextInt(1000);
  		      orient = ((flipper % 2)==1) ? cmcMachineLearningEnums.BranchOrientation.LEFT : cmcMachineLearningEnums.BranchOrientation.RIGHT;
  		      do_log( 1, "FLIPPED -> " + orient);
          }
          // find the child rule with the correct orientation
          idx=-1;
          for(int i=0;i<model.length;i++)
          {
        	  DecisionTreeBranch bra = model[i];
        	  if( bra == null ) continue;
        	  if( bra.dto.parentUID != rule.dto.UID ) continue;
        	  if( bra.dto.orientation != orient ) continue;
        	  idx=i;
          }
          if( idx < 0 ) {
        	  do_error("Could not find child rule for parent [" + rule.dto.UID + "]");
        	  return null;
          }
          if( model[idx] == null) {
        	  do_error("There is a null rule");
        	  return null;
          }
          return model[idx];
    }
    
    
}
