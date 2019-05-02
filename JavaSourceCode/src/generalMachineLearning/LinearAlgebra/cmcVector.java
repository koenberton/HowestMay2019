package generalMachineLearning.LinearAlgebra;

public class cmcVector {
	
	private double[] values=null;
	
	public cmcVector( double[] ival)
	{
		values = new double[ ival.length ];
		for(int i=0;i<ival.length;i++ ) values[i]=ival[i];
	}
    public double[] getVectorValues()
    {
    	return values;
    }
	public double getMagnitude()
	{
		double sum=0;
		for(int i=0;i<values.length;i++) sum += (values[i] * values[i]);
		return Math.sqrt(sum);
	}
	public double getNorm()
	{
		return getMagnitude();
	}
	public int getDimension()
	{
		return values.length;
	}
	public String show()
	{
		String sl="";
		for(int i=0;i<values.length;i++) sl += "[" + values[i] + "]";
		return sl;
	}
	public String showRound()
	{
		String sl="";
		for(int i=0;i<values.length;i++) sl += ((i==0)?"":",") + values[i];
		return "(" + sl + ")";
	}
}
