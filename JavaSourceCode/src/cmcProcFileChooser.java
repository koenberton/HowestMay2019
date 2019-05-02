import java.io.File;

import javax.swing.JFileChooser;
import javax.swing.filechooser.FileNameExtensionFilter;



public class cmcProcFileChooser {

	String FNaam = null;

	cmcProcFileChooser(String sDir , boolean FolderOnly)
	{
	 JFileChooser fc = new JFileChooser(sDir);
	 if( FolderOnly ) fc.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
	 fc.addChoosableFileFilter(new FileNameExtensionFilter("*.pdf", "pdf"));
	 int result = fc.showOpenDialog(null);
     if( result == JFileChooser.APPROVE_OPTION ) {
    	 File f = fc.getSelectedFile();
    	 FNaam = f.getAbsolutePath();
     }
	}
	
	cmcProcFileChooser(String sDir , String suffix)
	{
	 JFileChooser fc = new JFileChooser(sDir);
	 fc.addChoosableFileFilter(new FileNameExtensionFilter( ("*."+suffix).toLowerCase(), suffix.toLowerCase() , ("*."+suffix).toUpperCase(), suffix.toUpperCase()));
	 int result = fc.showOpenDialog(null);
     if( result == JFileChooser.APPROVE_OPTION ) {
    	 File f = fc.getSelectedFile();
    	 FNaam = f.getAbsolutePath();
     }
	}
	
	public String getAbsoluteFilePath()
	{
		return FNaam;
	}
}
