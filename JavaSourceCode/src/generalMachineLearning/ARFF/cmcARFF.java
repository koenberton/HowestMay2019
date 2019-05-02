package generalMachineLearning.ARFF;



public class cmcARFF {

	public enum ARFF_TYPE { STRING , NUMERIC , NOMINAL , CLASS , UNKNOWN }
	
	// main class
	private String ARFFname;
	private String LongFileName=null;
	private ARFFattribute[] attributeList;
	private int NbrOfLines=-1;
	private int NbrOfDuplicateLines=-1;
	private int NbrOfCollissions=-1;
	private int OverruleNbrOfBins=-1;
	
	//
	public cmcARFF(String s,int nattribs , int ndatarows)
	{
			ARFFname = s;
			OverruleNbrOfBins=-1;
			attributeList = new ARFFattribute[ nattribs ];
			for(int i=0;i<nattribs;i++) attributeList[i] = new ARFFattribute( "ATTR-"+i , ndatarows);
	}
	public String getLongFileName() {
		return LongFileName;
	}
	public void setLongFileName(String s) {
	    LongFileName=s;
	}
	public String getARFFName() {
		return ARFFname;
	}
	public void setARFFName(String name) {
		this.ARFFname = name;
	}
	public ARFFattribute[] getAttributeList()
	{
		return this.attributeList;
	}
	public void setAttributeList(ARFFattribute[] ar)
	{
		this.attributeList = ar;
	}
	public int getNbrOfLines() {
		return NbrOfLines;
	}
	public void setNbrOfLines(int nbrOfLines) {
		NbrOfLines = nbrOfLines;
	}
	public int getNbrOfDuplicateLines() {
		return NbrOfDuplicateLines;
	}
	public void setNbrOfDuplicateLines(int nbrOfDuplicateLines) {
		NbrOfDuplicateLines = nbrOfDuplicateLines;
	}
	public int getNbrOfCollissions() {
		return NbrOfCollissions;
	}
	public void setNbrOfCollissions(int nbrOfCollissions) {
		NbrOfCollissions = nbrOfCollissions;
	}
	public int getOverruleNbrOfBins() {
		return OverruleNbrOfBins;
	}
	public void setOverruleNbrOfBins(int overruleNbrOfBins) {
		OverruleNbrOfBins = overruleNbrOfBins;
	}
	//
	public class ARFFattribute
	{
		private String ARFFAttributeName;
		private ARFF_TYPE tipe = ARFF_TYPE.UNKNOWN;
		private String classesDefinition;  // comma separated list
		private String[] classes;  //  nominal values or class values
		private double[] values;   // if tipe is category or class then the index
		private String[] svalues;
		private boolean[] keep;
		//
		public ARFFattribute(String s, int nrows)
		{
			ARFFAttributeName=s;
			classesDefinition=null;
			classes = null;
			tipe = ARFF_TYPE.UNKNOWN;
			values = new double[nrows];
			for(int i=0;i<nrows;i++) values[i] = Double.MIN_VALUE;
			svalues = new String[nrows];
			for(int i=0;i<nrows;i++) svalues[i] = null;
			keep = new boolean[nrows];
			for(int i=0;i<nrows;i++) keep[i] = true;
		}
		
		public String getAttrName()
		{
			return ARFFAttributeName;
		}
		public void setAttrName(String s)
		{
			this.ARFFAttributeName = s;
		}
		public String getClassesDefinition()
		{
			return classesDefinition;
		}
		public void setClassesDefinition(String s)
		{
			this.classesDefinition = s;
		}
        public ARFF_TYPE getTipe()
        {
        	return tipe;
        }
        public void setTipe(ARFF_TYPE t)
        {
        	tipe=t;
        }
        public String[] getClasses()
        {
        	return this.classes;
        }
        public void setClasses(String[] ar)
        {
        	this.classes = ar;
        }
        public double[] getValues()
        {
        	return this.values;
        }
        public double getValue(int idx)
        {
        	return this.values[idx];
        }
        public void setValue(int idx , double d)
        {
        	this.values[idx] = d;
        }
        public String[] getSValues()
        {
        	return this.svalues;
        }
        public String getSValue(int idx)
        {
        	return this.svalues[idx];
        }
        public void setSValue(int idx , String s)
        {
        	this.svalues[idx] = s;
        }
        
        public boolean getKeep(int idx)
        {
        	return this.keep[ idx];
        }
	}
	
	public String show()
	{
		String sOut= this.getARFFName();
		String ctEOL = System.getProperty("line.separator");
		for(int i=0; i < this.getAttributeList().length ; i++ )
    	{
    		 cmcARFF.ARFFattribute atr = this.getAttributeList()[i];
    		 sOut += ctEOL + atr.getAttrName() + " " + atr.getTipe();
    		 if( atr.getTipe() == cmcARFF.ARFF_TYPE.CLASS ) {
    			String sl="";
    			for(int j=0;j<atr.getClasses().length;j++) sl += "[" + atr.getClasses()[j] + "]";
    			sOut += ctEOL + sl;
    		 }
    		 if( atr.getTipe() != cmcARFF.ARFF_TYPE.STRING ) {
    		  String svl="";
    		  int max = atr.getValues().length < 15 ? atr.getValues().length : 15;
    		  for(int j=0;j<max  ; j++) svl += atr.getValues()[j] + ",";
    		  sOut +=  ctEOL + "ATTR:" + i + "  " + svl;
    		 }
    	}
		return sOut;
	}
	
	public boolean kloon( cmcARFF parent)
	{
		    if( parent == null ) return false;
			this.ARFFname = parent.ARFFname;
			this.OverruleNbrOfBins = parent.getOverruleNbrOfBins();
			if( parent.attributeList == null ) return false;
			attributeList = new ARFFattribute[ parent.attributeList.length ];
			for(int i=0 ; i< parent.attributeList.length ; i++) {
				int nrows = (parent.attributeList[i] == null) ? -1 : parent.attributeList[i].getValues().length;
				attributeList[i] = new ARFFattribute( "ATTR-"+i , nrows );
			}
			for(int i=0 ; i<attributeList.length ; i++)
			{
				ARFFattribute left = attributeList[i];
				ARFFattribute right = parent.attributeList[i];
				//
				left.ARFFAttributeName = right.ARFFAttributeName;
				left.tipe              = right.tipe;
				left.classesDefinition = right.classesDefinition;
				if( right.classes != null ) {
				 left.classes = new String[ right.classes.length ];
			 	 for(int j=0;j<right.classes.length;j++)
				 {
			      left.classes[j] = right.classes[j];
				 }
				}
				if( right.values != null ) {
				 left.values = new double[ right.values.length ];	
				 for(int j=0;j<right.values.length;j++)
				 {
			      left.values[j] = right.values[j];
			    }
				}
				if( right.svalues != null ) {
					 left.svalues = new String[ right.svalues.length ];	
					 for(int j=0;j<right.svalues.length;j++)
					 {
				      left.svalues[j] = right.svalues[j];
					 }
				}
				if( right.keep != null ) {
					 left.keep = new boolean[ right.keep.length ];	
					 for(int j=0;j<right.keep.length;j++)
					 {
				      left.keep[j] = right.keep[j];
					 }
				}
			}
			return true;
	}

	public boolean propagateKeep( boolean[] sk)
	{
		for(int i=0 ; i<attributeList.length ; i++)
		{
			ARFFattribute atr = attributeList[i];
			if( atr.keep == null ) continue;
			if( atr.keep.length != sk.length )  return false;
			for(int j=0;j<sk.length;j++) atr.keep[j] = sk[j];
		}	
		return true;
	}
	
}
