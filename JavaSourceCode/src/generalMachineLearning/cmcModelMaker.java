package generalMachineLearning;

import generalMachineLearning.ARFF.ARFFCategory;
import generalMachineLearning.ARFF.cmcARFF;

public interface cmcModelMaker {
	
	public boolean trainModel(	ARFFCategory[] categories , int nbins , String[] ClassNameList , String ModelFileName );
	public boolean trainModel(	String OriginalFileName , String ModelFileName );
	public boolean testModel( String ModelFileName , String FileNameTestSet , cmcARFF xa);

}
