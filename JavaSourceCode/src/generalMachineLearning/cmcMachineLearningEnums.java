package generalMachineLearning;



public class cmcMachineLearningEnums {

	public enum DecisionTreeType { UNKNOWN , ROOT , BRANCH , LEAF }
	public enum BranchOrientation { UNKNOWN , LEFT , RIGHT }
	public enum PictureType { UNKNOWN , HISTOGRAM , SCATTER  , CORRELATION }
	public enum KernelType { UNKNOWN , NONE , GAUSSIAN , RBF , POLYNOMIAL , SIGMOID }
	
	
	//---------------------------------------------------------------------------------
	public cmcMachineLearningEnums.KernelType getKernelType(String s)
	//---------------------------------------------------------------------------------
	{
			if( s == null ) return null;
			String sel = s.toUpperCase();
		    int idx = -1;
			for(int i=0;i<cmcMachineLearningEnums.KernelType.values().length;i++)
			{
				if( sel.compareToIgnoreCase( cmcMachineLearningEnums.KernelType.values()[i].toString() ) == 0 ) {
					idx=i;
					break;
				}
			}
			if( idx < 0 ) return null;
			return cmcMachineLearningEnums.KernelType.values()[idx];
	}
}
