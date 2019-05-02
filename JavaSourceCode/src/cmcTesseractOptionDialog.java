import java.awt.Color;
import java.awt.Dialog;
import java.awt.Font;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ComponentEvent;
import java.awt.event.ComponentListener;
import java.awt.event.FocusAdapter;
import java.awt.event.FocusEvent;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

import javax.swing.BoxLayout;
import javax.swing.GroupLayout;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.table.TableColumn;
import javax.swing.table.TableColumnModel;

import logger.logLiason;
import cbrTekStraktorModel.cmcProcConstants;
import cbrTekStraktorModel.cmcProcEnums;
import cbrTekStraktorModel.cmcProcSettings;
import drawing.cmcGraphController;


public class cmcTesseractOptionDialog {

	cmcProcSettings xMSet = null;
	cmcTesseractOptionModel MyScanModel   = null;
    cmcProcEnums cenums = null;
    logLiason logger=null;
    
    
    JScrollPane frmComicScrollPane;
	JTable frmComicScannerTable;
	JPanel onderPane;
	JPanel omvatOnderPane;
	JButton okButton=null;
	JButton cancelButton=null;
	JCheckBox checker=null;
	
    private int CurrentSelectedRow = -1;
	private int CurrentSelectedCol = -1;
	private int maxImageBreedte=200;
	private boolean isOK=false;
	private boolean okPressed=false;
	private boolean cancelPressed=false;
    
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
    	do_log(0,sIn);
    }
	
    public cmcTesseractOptionDialog(JFrame jf , cmcProcSettings iM ,  logLiason ilog )
    {
    	xMSet = iM;
		cenums = new cmcProcEnums(xMSet);
		logger = ilog;
		// initialize and populate model
		MyScanModel = new cmcTesseractOptionModel(xMSet,this,logger);
		MyScanModel.ReadDataAndShow();
		MyScanModel.fireTableDataChanged();
		//
		initialize(jf);
    }
    
  //-----------------------------------------------------------------------
  	private void initialize(JFrame jf)
  	//-----------------------------------------------------------------------
  	{
  	
  		try {
  			   final JDialog dialog = new JDialog(jf,"",Dialog.ModalityType.DOCUMENT_MODAL);  // final voor de dispose   
  	            dialog.setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
  	            dialog.setTitle("Tesseract V4 - Parameter selection");
  	            dialog.setLocationRelativeTo( jf );
  	            dialog.setLocation(10,10);
  	            //
  	            dialog.addComponentListener(new ComponentListener() {
                   public void componentHidden(ComponentEvent e)
                   {
                     //System.out.println("dialog hidden");
                   }
                   public void componentMoved(ComponentEvent e)
                   {
                     //System.out.println("dialog moved");
                   }
                   public void componentResized(ComponentEvent e)
                   {
                     //System.out.println("dialog resized");
                   }
                   public void componentShown(ComponentEvent e)
                   {
                     //zetKolomHoogte();
                   }
                  });
  	            dialog.addWindowListener(new WindowAdapter() {
  	                @Override
  	                public void windowClosed(WindowEvent e) {
  	                    do_close();
  	                }
  	            });
  	            //
  	            Font font = xMSet.getPreferredFont();  
  	            //
  	            JPanel koepelPane = new JPanel();
  	            //koepelPane.setBackground(Color.WHITE);
  	            koepelPane.setLayout(new BoxLayout(koepelPane, BoxLayout.Y_AXIS));
  	            //
  	            JPanel upperPanel = new JPanel();
  	            upperPanel.setBackground(Color.WHITE);
  	            upperPanel.setLayout(new BoxLayout(upperPanel, BoxLayout.Y_AXIS));
  	            //
  	            JPanel bovenPanel = new JPanel();
  	            //bovenPanel.setBackground(Color.WHITE);
  	            bovenPanel.setLayout(new BoxLayout(bovenPanel, BoxLayout.X_AXIS));
	            //
	            frmComicScrollPane = new JScrollPane(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED, JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
	    		//frmComicScrollPane.setBounds(55, 76, 1151, 398);
	    		bovenPanel.add(frmComicScrollPane);
                //	    		
	    		frmComicScannerTable = new JTable();
  	            frmComicScannerTable = new JTable();
  	            frmComicScannerTable.addFocusListener(new FocusAdapter() {
  	    			@Override
  	    			public void focusLost(FocusEvent arg0) { // nodig om bij te houden wat de huidige selectie is
  	    				CurrentSelectedRow = -1;
  	    				CurrentSelectedCol = -1;
  	    				//checkButtonStatus();
  	    			}
  	    		});
  	    		//
  	    		frmComicScannerTable.setRowHeight(30);
  	    		frmComicScannerTable.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);   // zorgt voor de horizontale scrollbar
  	    		//
  	    		frmComicScannerTable.addMouseListener(new MouseAdapter() {
  	    			@Override
  	    			public void mouseClicked(MouseEvent arg0) {
  	    				//
  	    				JTable target = (JTable)arg0.getSource();
  	    				CurrentSelectedRow = target.getSelectedRow();
  	    		        CurrentSelectedCol = target.getSelectedColumn();
  	    		        //System.out.println("R"+ CurrentSelectedRow + " C" + CurrentSelectedCol );
  	    		  }
  	    		});
  	    		
  	    		
  	    		//
  	    		frmComicScannerTable.setModel(MyScanModel);
  	    		//
  	    		TableColumnModel tcm = frmComicScannerTable.getColumnModel();
  	    		
  	    		// Renderers zetten
  	    		for(int i=0;i<tcm.getColumnCount();i++)
  	    		{
  	    			TableColumn tc = tcm.getColumn(i);
  	    			
  	    			int BREEDTE = 100;
  	    			cmcProcEnums.TESS_OPTION_SELECTION x = cenums.getTessOptionAtIndex(i);
  	    			if( x != null ) {
  	    			 switch( x )
  	    			 {
  	    			 case WITHOLD : { BREEDTE = 30; break; }
  	    			 case PARAMETER : { BREEDTE = 300; break; }
  	    			 case VALUE : { BREEDTE = 200; break; }
  	    			 case DESCRIPTION : { BREEDTE = 400; break; }
  	    			 default : { BREEDTE = 100; break; }
  	    			 }
  	    			}
  	    			// Size
  	    			tc.setMaxWidth(BREEDTE*10);  
  	    			tc.setMinWidth(BREEDTE);
  	    			tc.setWidth(BREEDTE);
  	    		}
  	    	
                //	            
  	    		frmComicScrollPane.setViewportView(frmComicScannerTable);
  	    	    //
  	    		// Clicken op tabel header
  	    		frmComicScannerTable.getTableHeader().addMouseListener(new MouseAdapter() {
  	    		    @Override
  	    		    public void mouseClicked(MouseEvent e) {
  	    		        int col = frmComicScannerTable.columnAtPoint(e.getPoint());
  	    		        String name = frmComicScannerTable.getColumnName(col);
  	    		        do_log(1,"Column index selected " + col + " " + name);
  	    		        //xCtrl.doeSorteer(col,MyScanModel.toggleColSortOrder(col));
  	    		        //MyScanModel.fireTableDataChanged();
  	    		    }
  	    		});
  	    		
  	    	    //
  	    		koepelPane.add(upperPanel);
  	            koepelPane.add(bovenPanel);
  	            //
  	            omvatOnderPane = new JPanel();
  	            omvatOnderPane.setLayout(new BoxLayout(omvatOnderPane, BoxLayout.X_AXIS));
  	            //
  	            JPanel onderPane = new JPanel();
  	            
  			    //
  			    okButton = new JButton("Save");
  			    okButton.setEnabled(false);
  	            okButton.addMouseListener(new MouseAdapter() {
  	    			@Override
  	    			public void mouseClicked(MouseEvent arg0) {
  	    			  okPressed=true;
  	    			  MyScanModel.propagateChanges();
  	    			  dialog.dispose();
  	    			}
  	    		});
  	            okButton.setFont(font);
  	            //
  			    cancelButton = new JButton("Cancel");
  	            cancelButton.addMouseListener(new MouseAdapter() {
  	    			@Override
  	    			public void mouseClicked(MouseEvent arg0) {
  	    			  cancelPressed=true;
  	    			  dialog.dispose();
  	    			}
  	    		});
  	            cancelButton.setFont(font);
  	            //
  	            
  	            GroupLayout layout = new GroupLayout(onderPane);
  	            onderPane.setLayout(layout);
  	            layout.setAutoCreateGaps(true);
  	            layout.setAutoCreateContainerGaps(true);
  	            layout.setHorizontalGroup(
  	            		   layout.createSequentialGroup()
  	            		      //.addComponent(filterComboBox,GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE,GroupLayout.PREFERRED_SIZE)
  	            		      .addComponent(okButton)
  	            		      .addComponent(cancelButton)
  	           	);
  	           	layout.setVerticalGroup(
  	            		   layout.createSequentialGroup()
  	            		      .addGroup(layout.createParallelGroup(GroupLayout.Alignment.BASELINE)
  	            		    	   //.addComponent(filterComboBox,GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE,GroupLayout.PREFERRED_SIZE)
  	            		           .addComponent(okButton)
  	            		           .addComponent(cancelButton))
  	            );
  	            
  	            //
  	           	//filterComboBox.setBackground(Color.WHITE);
  	            koepelPane.setBackground(Color.WHITE);
  	            omvatOnderPane.setBackground(Color.WHITE);
  	            onderPane.setBackground(Color.WHITE);
  	            //
  	            omvatOnderPane.add(onderPane);
  	            koepelPane.add(omvatOnderPane);
  			    //
  	            dialog.add(koepelPane);
  	            dialog.pack();
  	            dialog.setLocationByPlatform(true);
  	            dialog.setSize( 800 , 500 );
   	            dialog.setVisible(true);
  	            isOK=true;
  	            //
  		}
  		catch(Exception e) {
  			isOK=false;
  			do_error("Error openining Quick Edit Dialog" + xMSet.xU.LogStackTrace(e));
  		}

  	}
  	
  	public void setConfirm()
  	{
  		okButton.setEnabled(true);
  	}
  	
  	private void do_close()
  	{
  	    	
  	}
}
