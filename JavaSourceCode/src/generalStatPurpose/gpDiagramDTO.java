package generalStatPurpose;

public class gpDiagramDTO {
	
	private int[][] histo = null;
	private String[] ClassNameList=null;
	private double[] xvals = null;
	private double[] yvals = null; 
	private double[] results = null;
	private double[][] dblarray=null;
	private String xLabel=null;
	private String yLabel=null;
	private String[] horizontalTickLabels = null;
	private String[] verticalTickLabels = null;

	public gpDiagramDTO(double[] x, double[] y , double[] r)
	{
		xvals = x;
		yvals = y;
		results = r;
	}
	public gpDiagramDTO(int[][] h , String[]cnl)
	{
		histo = h;
		ClassNameList=cnl;
	}
	public gpDiagramDTO( double[][] md)
	{
		dblarray=md;
		ClassNameList=null;
	}
	
	public double[][] getDoubleArray() {
		return dblarray;
	}
	public void setDoubleArray(double[][] dar)
	{
		dblarray=dar;
	}
	
	public int[][] getHisto() {
		return histo;
	}

	public String[] getClassNameList() {
		return ClassNameList;
	}

	public void setHisto(int[][] histo) {
		this.histo = histo;
	}

	public void setClassNameList(String[] classNameList) {
		ClassNameList = classNameList;
	}
	public double[] getXvals() {
		return xvals;
	}

	public void setXvals(double[] xvals) {
		this.xvals = xvals;
	}

	public double[] getYvals() {
		return yvals;
	}

	public void setYvals(double[] yvals) {
		this.yvals = yvals;
	}

	public double[] getResults() {
		return results;
	}

	public void setResults(double[] results) {
		this.results = results;
	}

	public String getxLabel() {
		return xLabel == null ? "nil-X" : xLabel;
	}

	public void setxLabel(String xLabel) {
		this.xLabel = xLabel;
	}

	public String getyLabel() {
		return yLabel == null ? "nil-Y" : yLabel;
	}

	public void setyLabel(String yLabel) {
		this.yLabel = yLabel;
	}
	public String[] getHorizontalTickLabels() {
		return horizontalTickLabels;
	}
	public String[] getVerticalTickLabels() {
		return verticalTickLabels;
	}
	public void setHorizontalTickLabels(String[] xTickLabels) {
		this.horizontalTickLabels = xTickLabels;
	}
	public void setVerticalTickLabels(String[] yTickLabels) {
		this.verticalTickLabels = yTickLabels;
	}
}
