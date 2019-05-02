package generalMachineLearning.ARFF;

public class ARFFCategoryCoreDTO {

	private String categoryName;
	private cmcARFF.ARFF_TYPE tipe = cmcARFF.ARFF_TYPE.UNKNOWN;
	private String[] NominalValueList=null;
		
	public ARFFCategoryCoreDTO(String sname)
	{
		categoryName = sname;
	}

	/*
	public boolean isEqual(ARFFCategoryCoreDTO cmp)
	{
		if( cmp == null ) return false;
		if( cmp.getCategoryName().compareToIgnoreCase(this.categoryName) != 0 ) return false;
		if( cmp.getTipe() != this.tipe ) return false;
		if( (cmp.getNominalValueList() == null) && (this.NominalValueList == null)) return true;
		if( (cmp.getNominalValueList() != null) && (this.NominalValueList == null)) return false;
		if( (cmp.getNominalValueList() == null) && (this.NominalValueList != null)) return false;
		if( cmp.getNominalValueList().length != this.NominalValueList.length ) return false;
		for(int i=0;i<cmp.getNominalValueList().length;i++)
		{
			if( cmp.getNominalValueList()[i].compareToIgnoreCase(this.NominalValueList[i]) != 0) return false;
		}
        return true;
	}
	*/
	
	public String getCategoryName() {
		return categoryName;
	}
	public String getName() {
		return categoryName;
	}

	public void setCategoryName(String categoryName) {
		this.categoryName = categoryName;
	}

	public cmcARFF.ARFF_TYPE getTipe() {
		return tipe;
	}

	public void setTipe(cmcARFF.ARFF_TYPE tipe) {
		this.tipe = tipe;
	}

	public String[] getNominalValueList() {
		return NominalValueList;
	}

	public void setNominalValueList(String[] nominalValueList) {
		NominalValueList = nominalValueList;
	}
	
	
}
