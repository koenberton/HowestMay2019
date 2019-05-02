package generalMachineLearning.DecisionTree;



public class DecisionTreeMetrics {
	   int    lineno;
	   double value;
	   double result;
	   double SplitEntropy;
	   double SplitGain;
	   DecisionTreeMetrics( int il , double val , double res )
	   {
		   lineno = il;
		   value  = val;
		   result = res;
		   SplitEntropy = Double.NaN;
		   SplitGain = Double.NaN;
	   }
	   public void shallowCopy(DecisionTreeMetrics y)
	   {
		   this.lineno = y.lineno;
		   this.value = y.value;
		   this.result = y.result;
		   this.SplitEntropy = y.SplitEntropy;
		   this.SplitGain = y.SplitGain;
	   }
	
}