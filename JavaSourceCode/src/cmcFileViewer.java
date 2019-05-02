import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;

import java.awt.*;
import java.awt.event.*;
import java.io.*;
import java.util.ArrayList;
import java.util.StringTokenizer;

import javax.swing.*;
import javax.swing.event.*;
import javax.swing.text.html.*;
import javax.swing.event.HyperlinkListener;

import cbrTekStraktorModel.cmcProcEnums;
import cbrTekStraktorModel.cmcProcSettings;
import customWidgets.ClosableTabbedPane;
import customWidgets.CloseIcon;
import generalpurpose.gpPrintStream;
import logger.logLiason;

public class cmcFileViewer {

	//private static Dimension DialogDimension = new Dimension(1000,750);
	
	cmcProcSettings xMSet = null;
	cmcProcEnums  cenums = null;
	logLiason logger = null;
	
	private static JFrame superframe = null;
	private static JDialog dialog = null;
	private static ClosableTabbedPane tabbedPane = null;
	private static Font dfont = null;
	private String LastErrorMsg = null;
	private long sequence = 0;
	private static int ZOOMSTEP = 5;
	
	class viewer {
		JEditorPane editor = null;
		String  URL = null;
		long UID = -1L;
		int zoom = 100;
		viewer( String url )
		{
			URL = url;
			editor=null;
			UID = (int)sequence++;
			zoom = 100;
		}
	}
	ArrayList<viewer> viewerlist = null;
	
	//------------------------------------------------------------
    private void do_log(int logLevel , String sIn)
	//------------------------------------------------------------
    {
       if( logger != null ) logger.write( this.getClass().getName() , logLevel , sIn);
       else 
       if (logLevel == 0 ) System.err.println(sIn);
       else System.out.println(sIn);
    }
	//------------------------------------------------------------
    private void do_error(String sIn)
	//------------------------------------------------------------
    {
    	LastErrorMsg = sIn;
    	do_log(0,sIn);
    }
    //------------------------------------------------------------
    public String getLastErrorMessage()
    //------------------------------------------------------------
    {
    	return LastErrorMsg;
    }
	//------------------------------------------------------------
	public cmcFileViewer(JFrame jf , cmcProcSettings iM , logLiason ilog , String[] flist)
	//------------------------------------------------------------
	{
		superframe = jf;
	    xMSet = iM;
	    logger = ilog;
	    dfont = xMSet.getPreferredFont();
	    viewerlist = new ArrayList<viewer>();
	    initialize(flist);
	}
	
	//-----------------------------------------------------------------------
	private void initialize( String[] flist )
	//-----------------------------------------------------------------------
	{
		try
        {
            dialog = new JDialog( superframe ,"",Dialog.ModalityType.DOCUMENT_MODAL);  // final voor de dispose   
            dialog.setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
            dialog.setTitle( "HTML File Viewer" );
            //dialog.setLocationRelativeTo( superframe );
            //dialog.setLocation(10,10);
            //dialog.setSize( DialogDimension );
            dialog.setLocationByPlatform(false);
            dialog.setBounds( xMSet.viewerframe );
            //
    		tabbedPane = new ClosableTabbedPane();
    		tabbedPane.setFont(dfont);
            //
    		if( flist != null ) {
    		   for(int i=0;i<flist.length;i++) viewFile( flist[i] , i);	
    		}
            //    		
    		tabbedPane.addMouseListener(new MouseListener()
    		{
    		    @Override
    		    public void mouseClicked(MouseEvent e) {
    		    }

    		    @Override
    		    public void mousePressed(MouseEvent e) {
    		    }

    		    @Override
    		    public void mouseReleased(MouseEvent e) {
    		    	  ClosableTabbedPane tabPane = (ClosableTabbedPane) e.getSource();
    		    	  if (tabPane.isEnabled()) {
    		    	    int tabIndex = tabPane.getSelectedIndex();
    		    	    if (tabIndex >= 0 && tabPane.isEnabledAt(tabIndex)) {
    		    	       CloseIcon closeIcon = (CloseIcon)tabPane.getIconAt(tabIndex);
    		    	       if (closeIcon.coordinatenInIcon(e.getX(), e.getY())) {
    		    			  removeFromViewerList( tabIndex );  // this one first
    		    			  tabPane.remove( tabIndex );
    		    			  // if there are no more tabpanes so close the dialog
    		    			  if( tabPane.getTabCount() == 0 ) dialog.dispose();
    		      		   }
    		    	  }
    		    	}
    		    }

    		    @Override
    		    public void mouseEntered(MouseEvent e) {
    		    }

    		    @Override
    		    public void mouseExited(MouseEvent e) {
    		    }
    		});
    		
    	    //dialog.pack(); DONT
    		dialog.getContentPane().add(tabbedPane);
            dialog.setLocationByPlatform(true);
            dialog.addComponentListener(new ComponentAdapter() 
            {
                public void componentResized(ComponentEvent e)
                {
                    doResizeDialog();
                }
            });
            
            //
            dialog.setVisible(true);
        } 
        catch (Exception e) 
        {
            do_error( "(Initialize)" + xMSet.xU.LogStackTrace(e) );
        }
	}
	
	//-----------------------------------------------------------------------
	private void doResizeDialog()
	//-----------------------------------------------------------------------
	{
	  xMSet.viewerframe = dialog.getBounds();	
	}
	
	
	//-----------------------------------------------------------------------
	public boolean viewFile(String longFileName , int idx )
	//-----------------------------------------------------------------------
	{
		JPanel TabPane = makeFilePanel(longFileName);
		if( TabPane == null ) return false;
		String ShortFileName = xMSet.xU.GetShortFileName(longFileName);
		tabbedPane.add( idx , ShortFileName , TabPane);
		return true;
	}
	
	//-----------------------------------------------------------------------
	private JPanel makeFilePanel(String longFileName )
	//-----------------------------------------------------------------------
	{
		    if( longFileName == null ) { do_error( "(makeFilePanel) Got a null file request"); return null; }
		    JPanel p = new JPanel();
		       try {
		    	if( xMSet.xU.IsBestand(longFileName) ) longFileName = "file:///" + longFileName;
	        	viewer zz = new viewer( longFileName );
	            zz.editor = new JEditorPane(zz.URL);
	            zz.editor.setEditable(false);
	            zz.editor.addHyperlinkListener(new HyperlinkListener() {
	 	            
	            public void hyperlinkUpdate(HyperlinkEvent e) {
	                    if (e.getEventType() == HyperlinkEvent.EventType.ACTIVATED) {
	                        JEditorPane pane = (JEditorPane) e.getSource();
	                        if (e instanceof HTMLFrameHyperlinkEvent) {
	                            HTMLFrameHyperlinkEvent evt = (HTMLFrameHyperlinkEvent) e;
	                            HTMLDocument doc = (HTMLDocument) pane.getDocument();
	                            doc.processHTMLFrameHyperlinkEvent(evt);
	                        } else {
	                            try {
	                                pane.setPage(e.getURL());
	                            } catch (Throwable t) {
	                                t.printStackTrace();
	                            }
	                        }
	                    }
	                }
	            });
	            viewerlist.add( zz );
	            p.setName("" + zz.UID );
	            do_log( 1 , "Added [" + zz.UID + "] [" + zz.URL + "]");
	            
	            // Panel met buttons
	            JLabel lblURL = new JLabel("URL");
		        lblURL.setFont(dfont);
		        JTextField txtURL = new JTextField(null, 30);
		        txtURL.setFont(dfont);
		        txtURL.addKeyListener(new KeyAdapter() {
		            @Override
		            public void keyPressed(KeyEvent e) {
		                if(e.getKeyCode() == KeyEvent.VK_ENTER){
		                	performFileViewRequest( txtURL.getText() );
		                }
		            }
		        });
		        //
		        JButton btnBrowse = new JButton("Browse");
		        btnBrowse.setFont(dfont);
		        btnBrowse.addActionListener(
	                    new ActionListener() {
	                        public void actionPerformed(ActionEvent e) {
	                            try {
	                            	cmcProcFileChooser fc = new cmcProcFileChooser( xMSet.getRootDir() , false);
	                            	String longFileName = fc.getAbsoluteFilePath();
	                            	if( longFileName != null ) performFileViewRequest(longFileName);
	                            } catch (Exception ex) {
	                                ex.printStackTrace();
	                            }
	                        }
	            });
		        //
		        JButton btnClose = new JButton("Close");
		        btnClose.setFont(dfont);
		        btnClose.addActionListener(
	                    new ActionListener() {
	                        public void actionPerformed(ActionEvent e) {
	                            try {
	                              dialog.dispose();
	                            } catch (Exception ex) {
	                                ex.printStackTrace();
	                            }
	                        }
	            });
		        //
		        JButton btnIncrease = new JButton("Zoom In");
		        btnIncrease.setFont(dfont);
		        btnIncrease.addActionListener(
	                    new ActionListener() {
	                        public void actionPerformed(ActionEvent e) {
	                            try {
	                              performZoom(  tabbedPane.getSelectedIndex() , 1 );
	                            } catch (Exception ex) {
	                                ex.printStackTrace();
	                            }
	                        }
	            });
		        //
		        JButton btnDecrease = new JButton("Zoom Out");
		        btnDecrease.setFont(dfont);
		        btnDecrease.addActionListener(
	                    new ActionListener() {
	                        public void actionPerformed(ActionEvent e) {
	                            try {
	                              performZoom(  tabbedPane.getSelectedIndex() , -1 );
	                            } catch (Exception ex) {
	                                ex.printStackTrace();
	                            }
	                        }
	            });
		        //
		        JPanel panel = new JPanel();
		        panel.setLayout(new BoxLayout(panel, BoxLayout.X_AXIS));
		        panel.add(lblURL);
		        panel.add(txtURL);
		        panel.add(btnBrowse);
		        panel.add(btnDecrease);
		        panel.add(btnIncrease);
		        panel.add(btnClose);
	            //  
		        JScrollPane sp = new JScrollPane(zz.editor);
	            p.setLayout(new BorderLayout());
	            p.add( panel , BorderLayout.SOUTH );
	            p.add( sp , BorderLayout.CENTER);
	           
	            
	        } catch (Exception ex) {
	        	do_error( "[File=" + longFileName + "]");
	            ex.printStackTrace();
	            return null;
	        }
	    
	        return p;
		
	}
	
	//-----------------------------------------------------------------------
	private void popMessage(String sMsg)
	//-----------------------------------------------------------------------
	{
	   do_error( sMsg );
	   JOptionPane.showMessageDialog(null,sMsg,xMSet.getApplicDesc(),JOptionPane.WARNING_MESSAGE);
	}
	
	//-----------------------------------------------------------------------
	private void performFileViewRequest(String FName)
	//-----------------------------------------------------------------------
	{
		  if( xMSet.xU.IsBestand(FName) == false ) {
			  popMessage( "File [" + FName + "] not accessible");
			  return;
		  }
		  int z = tabbedPane.getComponentCount();
     	  viewFile( FName , z );
     	  tabbedPane.setSelectedIndex( tabbedPane.getComponentCount()-1 );
	}
	
	//-----------------------------------------------------------------------
	private void removeFromViewerList(int tabidx)
	//-----------------------------------------------------------------------
	{
		try {
		 JPanel p = (JPanel)tabbedPane.getComponentAt(tabidx);
		 long uid = xMSet.xU.NaarLong( p.getName() );
		 if( uid < 0 ) return;
		 int idx = -1;
		 for(int i=0;i<viewerlist.size();i++)
		 {
			if( uid == viewerlist.get(i).UID ) { idx = i; break; }
		 }
		 if( idx < 0 ) return;
		 do_log( 1 , "Removing [" + viewerlist.get(idx).URL + "] [" + idx + "]" );
	     viewerlist.remove(idx);	
		}
		catch(Exception e ) { do_error("(removeFromViewerList) - " + e.getMessage()); }
 	}
	
	//-----------------------------------------------------------------------
	private void performZoom(int tabidx , int step)
	//-----------------------------------------------------------------------
	{
		try {
			 JPanel p = (JPanel)tabbedPane.getComponentAt(tabidx);
			 long uid = xMSet.xU.NaarLong( p.getName() );
			 if( uid < 0 ) return;
			 int idx = -1;
			 for(int i=0;i<viewerlist.size();i++)
			 {
				if( uid == viewerlist.get(i).UID ) { idx = i; break; }
			 }
			 if( idx < 0 ) return;
			 viewer zz  = viewerlist.get(idx);
			 if( zz == null ) return;
			 int oldzoom = zz.zoom;
			 zz.zoom +=  (step * ZOOMSTEP);
			 do_log( 1 , "Zoom [" + zz.URL + "] [Tab=" + tabidx + "] [Zoom=" + zz.zoom + "]");
		     if( updateURL(  zz.URL , oldzoom , zz.zoom ) == false ) return;  
		   
		     // refresh editorpane : you need to remove the document if the same URL is used
		     HTMLDocument doc = (HTMLDocument) zz.editor.getDocument();
		     doc.putProperty(HTMLDocument.StreamDescriptionProperty, null);
		     zz.editor.setPage( zz.URL );
		}
		catch(Exception e ) { do_error("(removeFromViewerList) - " + e.getMessage()); }
	}
	
	//-----------------------------------------------------------------------
	private boolean updateURL(String URL , int oldzoom , int newzoom)
	//-----------------------------------------------------------------------
	{
		String LongFileName = URL.trim();
		if( LongFileName.toUpperCase().indexOf("FILE:///") == 0 ) {
			LongFileName = LongFileName.substring( "FILE:///".length() );
		}
		do_log( 1 , "File [" + LongFileName + "] [Zoom=" + newzoom + "]");
		if( xMSet.xU.IsBestand(LongFileName) == false ) return false;
		// zoomable?
		String header = xMSet.xU.ReadContentFromFile( LongFileName , 30 , "ASCII");
		if( header.indexOf("ZOOMABLE") < 0 ) { popMessage("File is not zoomable\n" + LongFileName ); return false; }
		
		String TempFile = xMSet.getCacheDir() + xMSet.xU.ctSlash + "TempHtmlFile.html";
		gpPrintStream html = new gpPrintStream( TempFile , "UTF8");
		// read file an write it back and adpt the zoom factors
		
		 BufferedReader reader=null;
     	 try {
 			File inFile  = new File(LongFileName);  // File to read from.
 	        reader = new BufferedReader(new InputStreamReader(new FileInputStream(inFile), xMSet.getCodePageString()));
 	       	//
 	       	String sLine=null;
 	       	while ((sLine=reader.readLine()) != null) {
 	          if ( sLine.toUpperCase().indexOf( "<IMG" ) < 0 ) {
 	        	  html.println( sLine );
 	        	  continue;
 	          }
 	          do_log( 1 , "ORIG -->" + sLine );
 	          //
 	          String[] elements = new String[100];
 	          for(int i=0;i<elements.length ; i++ ) elements[i]=null;
 	          int pdx=0;
 	          StringTokenizer tokenizer = new StringTokenizer(sLine, " ");
 	          while (tokenizer.hasMoreElements()) {
 	            String st = tokenizer.nextToken();
 	            if( st == null ) continue;
 	            if( st.trim().length() == 0 ) continue;
 	            elements[ pdx ] = st;
 	            pdx++;
 	          }
 	          //
 	          String smodif = "";
 	          for(int i=0;i<pdx;i++)
 	          {
 	        	if( (elements[i].toUpperCase().startsWith("HEIGHT=")) || (elements[i].toUpperCase().startsWith("WIDTH=")) ) {
 	        	  String ss = xMSet.xU.keepNumbers( elements[i] );
 	        	  if( ss == null ) return false;
 	        	  int curzoom = xMSet.xU.NaarInt( ss );
 	        	  if( curzoom < 0 ) return false;
 	        	  double originalzoom =   ((double)curzoom * 100.0) / (double)oldzoom;
 	        	  double calcuzoom =  Math.round( originalzoom * (double)newzoom / 100.0 );
 	        	
 	        	  if( elements[i].toUpperCase().startsWith("HEIGHT=") ) smodif = smodif + " HEIGHT=";
 	        	                                                   else smodif = smodif + " WIDTH=";
 	        	  smodif = smodif + "\"" + (int)calcuzoom + "\"";
 	        	  continue;
 	        	}
 	        	smodif = smodif + " " + elements[i];
 	          }
 	          smodif = smodif.trim();
 	          html.println( smodif ); 
 	          //
 	          do_log( 1 , "NEW  -->" + smodif );
 	       	}
 	       	html.close();
     	 }
     	 catch( Exception e ) {
     		 do_error( "Reading [" + LongFileName + "]" + e.getMessage() );
     		 return false;
     	 }
     	finally {
     		try {
     		reader.close();
     		}
     		catch(Exception e ) {
     			do_error("Could not file [" + LongFileName + "]");
         		return false;
     		}
     	 }
     	 // swap the files
     	 try {
     	  xMSet.xU.VerwijderBestand( LongFileName );
     	  xMSet.xU.copyFile( TempFile , LongFileName );
     	  xMSet.xU.VerwijderBestand( TempFile );
     	 }
     	 catch(Exception e ) {
     		 do_error("Copying [" + TempFile + "] [" + LongFileName + "]");
     		 return false;
     	 }
		 return true;
	}
	
}
