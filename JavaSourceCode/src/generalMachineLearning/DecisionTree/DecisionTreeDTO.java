package generalMachineLearning.DecisionTree;

import generalMachineLearning.cmcMachineLearningEnums;
import generalMachineLearning.ARFF.ARFFCategoryCoreDTO;
import generalMachineLearning.ARFF.cmcARFF;


public class DecisionTreeDTO {
	
	public int level=-1;
	public long UID=-1L;
	public long parentUID=-1L;
	public cmcMachineLearningEnums.BranchOrientation orientation = cmcMachineLearningEnums.BranchOrientation.UNKNOWN;
	public String[] ClassNameList;
	public cmcMachineLearningEnums.DecisionTreeType treetipe = cmcMachineLearningEnums.DecisionTreeType.UNKNOWN;
	public double TotalEntropy;
	public int splitCategoryIndex=-1;
	public String splitOnCategoryName=null;
	public cmcARFF.ARFF_TYPE splitOnARFFTipe = cmcARFF.ARFF_TYPE.UNKNOWN;
	int splitValueIndex=-1;
	public double splitOnValue=Double.NaN;
	public String splitOnNominalName=null;
	public int NumberOfRows=-1;
	public String LeafClassName=null;
	public boolean FlippedACoin=false;
	public ARFFCategoryCoreDTO[] categories=null;
	
	public DecisionTreeDTO()
	{
		
	}

}
