package generalMachineLearning.ARFF;

public class ARFFWrapperDTO {

	     private int NbrOfBins=-1;
	     private String[] ClassNameList=null;
	     private cmcARFF xa=null;
	     private String ARFFFileName=null;
	     private String FileNameTrainingSet=null;
	     private String FileNameTestSet=null;
	     private ARFFCategory[] categories=null;
	     private boolean valid=false;
	     
	     public ARFFWrapperDTO(int ibins)
	     {
	    	 NbrOfBins=ibins;
	    	 ClassNameList=null;
	 	     xa=null;
	 	     ARFFFileName=null;
	 	     FileNameTrainingSet=null;
	 	     FileNameTestSet=null;
	 	     categories=null;
	 	     valid=false;
	 	 }

		public int getNbrOfBins() {
			return NbrOfBins;
		}

		public void setNbrOfBins(int n) {
			NbrOfBins = n;
		}

		public String[] getClassNameList() {
			return ClassNameList;
		}

		public void setClassNameList(String[] classNameList) {
			ClassNameList = classNameList;
		}

		public cmcARFF getXa() {
			return xa;
		}

		public void setXa(cmcARFF xa) {
			this.xa = xa;
		}

		public String getARFFFileName() {
			return ARFFFileName;
		}

		public void setARFFFileName(String aRFFFileName) {
			ARFFFileName = aRFFFileName;
		}

		public String getFileNameTrainingSet() {
			return FileNameTrainingSet;
		}

		public void setFileNameTrainingSet(String fileNameTrainingSet) {
			FileNameTrainingSet = fileNameTrainingSet;
		}

		public String getFileNameTestSet() {
			return FileNameTestSet;
		}

		public void setFileNameTestSet(String fileNameTestSet) {
			FileNameTestSet = fileNameTestSet;
		}

		public ARFFCategory[] getCategories() {
			return categories;
		}

		public void setCategories(ARFFCategory[] categories) {
			this.categories = categories;
		}

		public boolean isValid() {
			return valid;
		}

		public void setValid(boolean valid) {
			this.valid = valid;
		}
	     
	     
}
