package cbrTekStraktorProject;

import java.awt.Font;
import java.sql.Date;
import java.text.SimpleDateFormat;

import cbrTekStraktorModel.cmcProcConstants;
import cbrTekStraktorModel.cmcProcEnums;

public class cmcProjectCore {
	
	private String ProjectFolderName =  null;
	private String ProjectName =  null;
	private String TesseractDir = null;
	private cmcProcEnums.ENCODING encoding = cmcProcEnums.ENCODING.ISO_8859_1;
	private String Description = null;
	private String dateformat="dd-MMM-yy HH:mm:ss";
	private int mean_character_count = 500;
	private int HorizontalVerticalVarianceThreshold = 5;
	private String preferredFontName = "Comic Sans MS";
	private int preferredFontSize = 12;
	private Font preferredFont = new Font(preferredFontName, Font.PLAIN, preferredFontSize);
	private int LogLevel=9;
    private long created = -1L;
    private long updated = -1L;
    private String preferredLanguageLong = "french";
    private cmcProcEnums.BackdropType backdrop = cmcProcEnums.BackdropType.BLEACHED;
    private cmcProcEnums.BROWSER browser = cmcProcEnums.BROWSER.MOZILLA;
    private String PythonHomeDir = null;
    private int MaxThreads = 5;
    private cmcProcEnums.REASSESMENT_COVERAGE TensorCoverage = cmcProcEnums.REASSESMENT_COVERAGE.REASSESS_NOTHING;
    //private boolean AlsoRunTensorFlow =  false;
    private boolean AlsoRunTesseract = false;
    private cmcProcEnums.MACHINE_LEARNING_TYPE PostProcessAIType = cmcProcEnums.MACHINE_LEARNING_TYPE.UNKNOWN;
    private String SplashPictureName = null;
    
    //
    private String sErr=null;
    
	//---------------------------------------------------------------------------------
	public cmcProjectCore(String projdir)
	//---------------------------------------------------------------------------------
	{
		ProjectFolderName = projdir;
		ProjectName = projdir;  // default
		TesseractDir = this.getDefaultTesseractDir();
		PythonHomeDir = this.getDefaultPythonHomeDir();
	}
	
	//---------------------------------------------------------------------------------
  	private String getDefaultTesseractDir()
  	//---------------------------------------------------------------------------------
  	{
  		return ( System.getProperty("file.separator").trim().startsWith("/") ) ? cmcProcConstants.LINUX_TESSERACTHOME : cmcProcConstants.MSDOS_TESSERACTHOME;
  	}

	//---------------------------------------------------------------------------------
  	private String getDefaultPythonHomeDir()
  	//---------------------------------------------------------------------------------
  	{
  		return ( System.getProperty("file.separator").trim().startsWith("/") ) ? cmcProcConstants.LINUX_PYTHONHOME : cmcProcConstants.MSDOS_PYTHONHOME;
  	}

    //---------------------------------------------------------------------------------
    //---------------------------------------------------------------------------------
  	public void setProjectFolderName(String s) { ProjectFolderName =s;}    
  	public void setProjectName(String s)       { ProjectName =s;}    
    public void setDateformat(String s)        { dateformat = s; }
    public void setMeanCharacterCount(int i)   { mean_character_count = i; }
    public void setTesseractDir(String s)      { TesseractDir = s; }
    public void setPreferredFontName(String s) { preferredFontName = s ; }
    public void setPreferredFontSize(int i)    { preferredFontSize =i ; }
    public void setPreferredFont(Font x)       { preferredFont =x ; }
    public void setLogLevel(int i)             { LogLevel=i; }
    public void setDescription(String s)       { Description =s; }
    public void setEncoding(cmcProcEnums.ENCODING cp) { encoding = cp; }
    public void setCreated(long l)                    { created = l; }
	public void setUpdated(long l)                    { updated = l; }
	public void setPreferredLanguageLong(String s)    { preferredLanguageLong = s; }
	public void setBackDropType(cmcProcEnums.BackdropType bd) { backdrop= bd; }
	public void setHorizontalVerticalVarianceThreshold(int i) { HorizontalVerticalVarianceThreshold=i; }
	public void setBrowser(cmcProcEnums.BROWSER bd) { browser= bd; }
	public void setPythonHomeDir(String s)      { PythonHomeDir = s; }
	public void setMaxThreads(int i)             { MaxThreads=i; }
	public void setTensorCoverage(cmcProcEnums.REASSESMENT_COVERAGE c) { TensorCoverage=c; }
	//public void setALsoRunTensorFlow(boolean b)  { AlsoRunTensorFlow = b; }
	public void setALsoRunTesseract(boolean b)  { AlsoRunTesseract = b; }
	public void setPostProcessAIType(cmcProcEnums.MACHINE_LEARNING_TYPE c) { PostProcessAIType=c; }
	public void setSplashPictureName(String s) { SplashPictureName =s;}    
    //
	//---------------------------------------------------------------------------------
	//---------------------------------------------------------------------------------
    public String getProjectFolderName()  { return ProjectFolderName; }
    public String getDateformat()         { return dateformat; }
    public int getMeanCharacterCount()    { return mean_character_count; }
    public String getTesseractDir()       { return TesseractDir; }
    public Font getPreferredFont()        { return preferredFont; }
    public String getPreferredFontName()  { return preferredFontName; }
    public int getPreferredFontSize()     { return preferredFontSize; }
    public int getLogLevel()              { return LogLevel; }
    public String getProjectDescription() { return Description; }
    public String getProjectName()        { return ProjectName; }
    public cmcProcEnums.ENCODING getEncoding() { return encoding; }
    public int getHorizontalVerticalVarianceThreshold() { return HorizontalVerticalVarianceThreshold; }
    public long getCreated()              { return created; }
    public long getUpdated()              { return updated; }
    public String getErrors()             { return sErr; }
    public String getPreferredLanguageLong()  { return preferredLanguageLong; }
    public cmcProcEnums.BackdropType  getBackDropType() { return backdrop; }   
    public cmcProcEnums.BROWSER getBrowser() { return browser; }
    public String getPythonHomeDir()       { return PythonHomeDir; }
    public int getMaxThreads()             { return MaxThreads; }
    public cmcProcEnums.REASSESMENT_COVERAGE getTensorCoverage() { return TensorCoverage; }
    public boolean getAlsoRunTesseract() { return AlsoRunTesseract; }
    //public boolean getAlsoRunTensorFlow() { return AlsoRunTensorFlow; }
    public cmcProcEnums.MACHINE_LEARNING_TYPE getPostProcessAIType() { return PostProcessAIType; }
    public String getSplashPictureName()  { return SplashPictureName; }
    
	//---------------------------------------------------------------------------------
    public boolean hasValidProjectCharacteristics()
   	//---------------------------------------------------------------------------------
    {
    	sErr="";
    	sErr += isEmpty(getProjectFolderName(),"FolderName");
    	sErr += isEmpty(getDateformat(),"Dateformat");
    	sErr += isEmpty(getTesseractDir(),"TesseractFolder");
    	sErr += isEmpty(getTesseractDir(),"PythonHomeDir");
    	sErr += isEmpty(getPreferredFontName(),"PreferredFont");
    	sErr += isEmpty(getProjectDescription(),"ProjectDescription");
    	sErr += isEmpty(getProjectName(),"ProjetName");
    	sErr += (getEncoding() == null) ? "Encoding is null" : "";
    	// numerics
        sErr += ( (getMeanCharacterCount() < 100) || ((getMeanCharacterCount() > 800)) ) ? "Unsupported Mean Character Count\n" : "";
        sErr += ( (getPreferredFontSize() < 5) || ((getPreferredFontSize() > 20)) ) ? "Unsupported Fontsize\n" : "";
        sErr += ( (getLogLevel() < 0) || ((getLogLevel() > 9)) ) ? "Unsupported loglevel\n" : "";
        sErr += ( (getMaxThreads() < 0) || ((getMaxThreads() > 30)) ) ? "Unsupported number of threads\n" : "";
        sErr += ( (getHorizontalVerticalVarianceThreshold() < 0) || ((getHorizontalVerticalVarianceThreshold() > 20)) ) ? "Wrong variance" : "";
        // advanced checks
        try {
			 Font x = new Font( getPreferredFontName() , Font.PLAIN, getPreferredFontSize());
			}
			catch(Exception e) {
			  sErr += "Invalid font[" + getPreferredFontName() + " " + getPreferredFontSize() + "]";	
			}
        //
        try {
          Date date = new Date(20170102L);
		  SimpleDateFormat ft = new SimpleDateFormat (this.getDateformat());
        }
        catch (Exception e ) {
          sErr += 	"Unsupported date format [" + getDateformat() +"]\n";
        }
		//
    	if( sErr.trim().length() == 0 ) return true;
    	return false;
    }

	//---------------------------------------------------------------------------------
    private String isEmpty(String s , String tipe)
   	//---------------------------------------------------------------------------------
    {
    	if( s == null ) return "Null [" + tipe + "]\n";
    	if( s.trim().length() < 1) return "Empty [" + tipe + "]\n";
    	return "";
    }
    
}
