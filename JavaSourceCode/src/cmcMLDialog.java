import java.awt.Color;
import java.awt.Dialog;
import java.awt.Font;
import java.awt.Rectangle;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.image.BufferedImage;
import java.util.ArrayList;

import javax.swing.DefaultListModel;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTabbedPane;
import javax.swing.JTextField;
import javax.swing.ListSelectionModel;
import javax.swing.UIManager;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;

import logger.logLiason;
import cbrTekStraktorModel.cmcProcEnums;
import cbrTekStraktorModel.cmcProcSettings;
import dao.cmcARFFDAO;
import generalMachineLearning.cmcMachineLearningConstants;
import generalMachineLearning.cmcMachineLearningEnums;
import generalMachineLearning.cmcMachineLearningModelController;
import generalMachineLearning.ARFF.ARFFVisualizer;
import generalMachineLearning.LogisticRegression.cmcLinearRegression;
import generalMachineLearning.LogisticRegression.cmcLogisticRegression;
import generalMachineLearning.SupportVectorMachine.cmcSMO;
import generalpurpose.gpFileChooser;
import generalpurpose.gpPrintStream;


public class cmcMLDialog {

	
	private static String DEFAULT_TITLE = "Modeler";
	//private static Dimension DialogDimension = new Dimension(1100,650);
	private static int WIDGET_HEIGTH  = 20;
	private static int BUTTON_HEIGTH  = 25;
	private static int BUTTON_WIDTH   = 160;
	private static int XMARGIN        = 10;
	private static int YMARGIN        = 10;
	private static int VERTICAL_GAP   = 2;
	private static int HORIZONTAL_GAP = 6;
	private static int LABEL_WIDTH    = 80;
	private static String[] DummyDropDownList = { "Select an ARFF file"  };
	
	cmcProcSettings xMSet = null;
	cmcProcEnums  cenums = null;
	logLiason logger = null;
	private ARFFVisualizer visualizer = null;
	private Font dfont = null;
	//
	private static JDialog dialog = null;
	private static JTabbedPane tabbedPane = null;
	//
	private static JPanel firstTabPane = null;
	private static JPanel secondTabPane = null;
	private static JPanel thirdTabPane = null;
	private static JPanel fourthTabPane = null;
	private JTextField txtDataFileName = null;
	private JTextField txtRelationName = null;
	private JTextField txtNbrOfLines = null;
	private JLabel lblHisto = null;
	private JTextField txtNbrOfBins = null;
	private JTextField txtTrainPerc = null;
	private JList featureList=null;
	private JButton btnT1Browse=null;
	private JButton btnT1Probe=null;
	private JButton btn1Overview=null;
	private JButton btnT1Close=null;
	private JButton btnT1Open=null;
	private JButton btnT2Regression=null;
	//
	private JList featureList1=null;
	private JList featureList2=null;
	private JLabel lblScatterLeft = null;
	private JLabel lblScatterRight = null;
	private JLabel lblCorrelation = null;
	private JCheckBox ckNormalized=null;
	private JButton btn2Overview=null;
	private JButton btnT2Close=null;
	//
	private JComboBox<String> methode=null;
	private JComboBox<String> scatterdots=null;
	private JComboBox<String> scattersize=null;
	private JComboBox<String> colorsentiment=null;
	private JButton btnT3Run=null;
	private JButton btnT3View=null;
	//
	private String[] scatterTuple = new String[2];
	private String LastErrorMsg = null;
	
	
	class MLDTO {
		String LongDataFileName = null;
		String LongModelName = null;
		String RelationName = null;
		long FileSize=0L;
		int DataLines=0;
		int nbrofbins=0;
		ArrayList<String> featureList = null;
		int firstNonString = 0;
		cmcProcEnums.MACHINE_LEARNING_TYPE ModelType = cmcProcEnums.MACHINE_LEARNING_TYPE.UNKNOWN;
		// specific for lineair regression display
		double[] hyperplane=null;
		int degree=-1;
		int cycles=-1;
		double MeanSquareError=-1;
		double stepsize=Double.NaN;
		MLDTO()
		{
			LongDataFileName = "";
			RelationName = "";
			FileSize=0L;
			DataLines=0;
			featureList = new ArrayList<String>();
			nbrofbins = -1;
			firstNonString=0;
			ModelType = cmcProcEnums.MACHINE_LEARNING_TYPE.UNKNOWN;
			hyperplane=null;
			degree=-1;
			cycles=-1;
			stepsize= Double.NaN;
			MeanSquareError=-1;
		}
	}
	private MLDTO mldto = null;
	private String accuracyInfo = null;
    
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
	public cmcMLDialog(JFrame jf , cmcProcSettings iM , logLiason ilog )
	//------------------------------------------------------------
	{
		xMSet = iM;
		logger = ilog;
	    cenums = new cmcProcEnums(xMSet);
	    visualizer = new ARFFVisualizer( xMSet , logger );
	    //
	    scatterTuple[0] = scatterTuple[1] = null;
		WIDGET_HEIGTH = xMSet.getPreferredFontSize() * 2;
		BUTTON_HEIGTH = WIDGET_HEIGTH + 2;
	    dfont = xMSet.getPreferredFont();  
		mldto = new MLDTO();
	    //	
		initialize(jf);
	}
	
	//-----------------------------------------------------------------------
	private void popMessage(String sMsg)
	//-----------------------------------------------------------------------
	{
		//do_error(sMsg);
		Font f = (Font)UIManager.get("OptionPane.messageFont");
		UIManager.put("OptionPane.messageFont", new Font("monospaced", Font.PLAIN, 13));
		JOptionPane.showMessageDialog(null,sMsg,xMSet.getApplicDesc(),JOptionPane.WARNING_MESSAGE);
		UIManager.put("OptionPane.messageFont", f );
    }
		
	//------------------------------------------------------------
	private void initialize(JFrame jf)
	//------------------------------------------------------------
	{
		try
        {
            dialog = new JDialog(jf,"",Dialog.ModalityType.DOCUMENT_MODAL);  // final voor de dispose   
            dialog.setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
            dialog.setTitle( DEFAULT_TITLE );
            //dialog.setLocationRelativeTo( jf );
            //dialog.setLocation(10,10);
            //dialog.setSize( DialogDimension );
            dialog.setLocationByPlatform(false);
            dialog.setBounds( xMSet.modelerframe );
            //
    		tabbedPane = new JTabbedPane(JTabbedPane.TOP);
    		tabbedPane.setFont(dfont);

    		firstTabPane = makeFilePanel();
    		secondTabPane = makeVisualizePanel();
    		thirdTabPane = makeTrainPanel();
    		fourthTabPane = makeCovarPanel();
    		
    		tabbedPane.addTab("File", firstTabPane );
    		tabbedPane.addTab("Scatter Diagrams", secondTabPane);
    		tabbedPane.addTab("Correlation Diagram", fourthTabPane);
    		tabbedPane.addTab("Train", thirdTabPane);
    	
    		tabbedPane.addChangeListener(new ChangeListener() {
                @Override
                public void stateChanged(ChangeEvent e) {
                    if (e.getSource() instanceof JTabbedPane) {
                        JTabbedPane pane = (JTabbedPane) e.getSource();
                        //currentPane = pane.getSelectedIndex();
                        performTabPane( pane.getSelectedIndex() );
                    }
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
            makeMLDTO( xMSet.getLastARFFFileName() );
            contentRefresh();
            doResizeDialog();
            dialog.setVisible(true);
        } 
        catch (Exception e) 
        {
            do_error( "(Initialize)" + xMSet.xU.LogStackTrace(e) );
        }
	}
	
	/*
	private JPanel makePanel(String text) {
		JPanel p = new JPanel();
		p.add(new Label(text));
		//p.setLayout(new GridLayout(1, 1));
		p.setLayout(new BoxLayout(p, BoxLayout.Y_AXIS));
		return p;
	}
	*/
	
	//------------------------------------------------------------
	private void doResizeDialog()
	//------------------------------------------------------------
	{
		xMSet.modelerframe = dialog.getBounds();
		int dwidth = dialog.getWidth();
		int dheigth = dialog.getHeight();
		
		// first tab
		txtDataFileName.setBounds( txtDataFileName.getX() , txtDataFileName.getY() , dwidth - (4 * XMARGIN) - txtDataFileName.getX() , txtDataFileName.getHeight());
		txtRelationName.setBounds( txtRelationName.getX() , txtRelationName.getY() , dwidth - (4 * XMARGIN) - txtRelationName.getX() , txtRelationName.getHeight());
		
		int y = dheigth - 80 - BUTTON_HEIGTH;
		Rectangle r = btnT1Browse.getBounds(); r.y = y;
		btnT1Browse.setBounds( r );
		r = btnT1Probe.getBounds(); r.y = y;
		btnT1Probe.setBounds( r );
		r = btn1Overview.getBounds(); r.y = y;
		btn1Overview.setBounds( r );
		r = btnT1Close.getBounds(); r.y = y;
		btnT1Close.setBounds( r );
		r = btnT1Open.getBounds(); r.y = y;
		btnT1Open.setBounds( r );
		
		// two
		r = btn2Overview.getBounds(); r.y = y;
		btn2Overview.setBounds(r);
		r = btnT2Close.getBounds(); r.y = y;
		btnT2Close.setBounds(r);
		r = btnT2Regression.getBounds(); r.y = y;
		btnT2Regression.setBounds(r);
		
		// third
		r = btnT3Run.getBounds(); r.y = y;
		btnT3Run.setBounds(r);
		r = btnT3View.getBounds(); r.y = y;
		btnT3View.setBounds(r);
	}
	
	//------------------------------------------------------------
	private JPanel makeFilePanel()
	//------------------------------------------------------------
	{
		int x = XMARGIN;
		int y = YMARGIN;
		
		JPanel p = new JPanel();
		p.setLayout(null);   // Absolute positioning

		JLabel lblFile = new JLabel("File");
		lblFile.setFont(dfont);
	    lblFile.setBounds( x , y , LABEL_WIDTH , WIDGET_HEIGTH );
	    p.add(lblFile);
	    //
		y += WIDGET_HEIGTH + VERTICAL_GAP;
		JLabel lblRelation = new JLabel("@Relation");
		lblRelation.setFont(dfont);
	    lblRelation.setBounds( x , y , LABEL_WIDTH , WIDGET_HEIGTH );
	    p.add(lblRelation);
		//
		y += WIDGET_HEIGTH + VERTICAL_GAP;
		JLabel lblLines = new JLabel("@Data");
		lblLines.setFont(dfont);
	    lblLines.setBounds( x , y , LABEL_WIDTH , WIDGET_HEIGTH );
	    p.add(lblLines);
	    
	    
	    // Input fields (2nd column)
	    x = lblFile.getX() + lblFile.getWidth() + HORIZONTAL_GAP;
	    y = lblFile.getY();
		txtDataFileName = new JTextField( mldto.LongDataFileName==null ? "" : mldto.LongDataFileName );
		txtDataFileName.setBounds( x , y , LABEL_WIDTH * 2, WIDGET_HEIGTH );
		txtDataFileName.setFont(dfont);
		p.add(txtDataFileName);
	    //
		y += WIDGET_HEIGTH + VERTICAL_GAP;
		txtRelationName = new JTextField( mldto.RelationName==null ? "" : mldto.RelationName );
		txtRelationName.setBounds( x , y , LABEL_WIDTH * 2, WIDGET_HEIGTH );
		txtRelationName.setFont(dfont);
		p.add(txtRelationName);
		//
		y += WIDGET_HEIGTH + VERTICAL_GAP;
		txtNbrOfLines = new JTextField("" );
		txtNbrOfLines.setBounds( x , y , LABEL_WIDTH * 2, WIDGET_HEIGTH );
		txtNbrOfLines.setFont(dfont);
		p.add(txtNbrOfLines);
		//
		y += WIDGET_HEIGTH + 2;
		featureList = new JList(DummyDropDownList); 
		featureList.setSelectionMode(ListSelectionModel.SINGLE_INTERVAL_SELECTION);
		featureList.setLayoutOrientation(JList.VERTICAL);
		featureList.setFont(dfont);
		featureList.addListSelectionListener(new ListSelectionListener() {
            @Override
            public void valueChanged(ListSelectionEvent arg0) {
                if (!arg0.getValueIsAdjusting()) {
                  if( featureList.getSelectedValue() != null ) {
                    String s = featureList.getSelectedValue().toString();
                    performHistogram( s );
                  }
                }
            }
        });
		JScrollPane scrollPane = new JScrollPane();
		scrollPane.setViewportView(featureList);
		scrollPane.setBounds( x  , y , 300 , visualizer.getTileHeigth() );
		p.add( scrollPane);
		
		// canvas for histogram
		lblHisto = new JLabel();
		lblHisto.setBackground( Color.WHITE );
		lblHisto.setBounds( scrollPane.getX() + scrollPane.getWidth() + XMARGIN , scrollPane.getY() , visualizer.getTileWidth() , visualizer.getTileHeigth() );
		lblHisto.setText("canvas");
		p.add( lblHisto );
		
		//
		y += WIDGET_HEIGTH + HORIZONTAL_GAP;
		x = lblFile.getX();
		btnT1Browse = new JButton("Browse");
		btnT1Browse.setBounds( x, y , BUTTON_WIDTH , BUTTON_HEIGTH);
		btnT1Browse.setFont(dfont);
        btnT1Browse.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseClicked(MouseEvent arg0) {
	               selectDataFileName();	
	               contentRefresh();
			}
		});
		p.add(btnT1Browse);
		//
		x += BUTTON_WIDTH + HORIZONTAL_GAP;
		btnT1Probe = new JButton("Scatterplot");
		btnT1Probe.setBounds( x , y , BUTTON_WIDTH , BUTTON_HEIGTH);
		btnT1Probe.setFont(dfont);
        btnT1Probe.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseClicked(MouseEvent arg0) {
			  //dialog.dispose();
			  tabbedPane.setSelectedIndex(1);
			}
		});
		p.add(btnT1Probe);
	    //
		x += BUTTON_WIDTH + HORIZONTAL_GAP;
		btn1Overview = new JButton("Overview");
		btn1Overview.setBounds( x , y , BUTTON_WIDTH , BUTTON_HEIGTH);
		btn1Overview.setFont(dfont);
        btn1Overview.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseClicked(MouseEvent arg0) {
			   makeAllDiagrams( cmcMachineLearningEnums.PictureType.HISTOGRAM );
			}
		});
		p.add(btn1Overview);
	    //
		x += BUTTON_WIDTH + HORIZONTAL_GAP;
		btnT1Open = new JButton("Data");
		btnT1Open.setBounds( x , y , BUTTON_WIDTH , BUTTON_HEIGTH);
		btnT1Open.setFont(dfont);
	    btnT1Open.addMouseListener(new MouseAdapter() {
					@Override
					public void mouseClicked(MouseEvent arg0) {
					  viewDataFile();
					}
		});
		p.add(btnT1Open);
	    //
		x += BUTTON_WIDTH + HORIZONTAL_GAP;
		btnT1Close = new JButton("Close");
		btnT1Close.setBounds( x , y , BUTTON_WIDTH , BUTTON_HEIGTH);
		btnT1Close.setFont(dfont);
        btnT1Close.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseClicked(MouseEvent arg0) {
			  dialog.dispose();
			}
		});
		p.add(btnT1Close);
		
		
		
	    //		
		return p;
	}

	//------------------------------------------------------------
	private JPanel makeVisualizePanel()
	//------------------------------------------------------------
	{
		int x = XMARGIN;
		int y = YMARGIN;
		int GAP = VERTICAL_GAP * 2;
		
		JPanel p = new JPanel();
		p.setLayout(null);   // Absolute positioning
		
		
		y += WIDGET_HEIGTH + 2;
		int scrollPaneHeigth = (int)((double)(visualizer.getTileHeigth() - GAP) * (double)0.50);
		int scrollPaneWidth  = 200;
		featureList1 = new JList(DummyDropDownList); 
		featureList1.setSelectionMode(ListSelectionModel.SINGLE_INTERVAL_SELECTION);
		featureList1.setLayoutOrientation(JList.VERTICAL);
		featureList1.setFont(dfont);
		featureList1.addListSelectionListener(new ListSelectionListener() {
            @Override
            public void valueChanged(ListSelectionEvent arg0) {
                if (!arg0.getValueIsAdjusting()) {
                  if( featureList1.getSelectedValue() != null ) {
                    String s = featureList1.getSelectedValue().toString();
                    performScatter( 0 , s , false );
                  }
                }
            }
        });
		JScrollPane scrollPane = new JScrollPane();
		scrollPane.setViewportView(featureList1);
		scrollPane.setBounds( x  , y , scrollPaneWidth ,  scrollPaneHeigth );
		p.add( scrollPane);
		
		//
		y += scrollPane.getHeight() + GAP;
		featureList2 = new JList(DummyDropDownList); 
		featureList2.setSelectionMode(ListSelectionModel.SINGLE_INTERVAL_SELECTION);
		featureList2.setLayoutOrientation(JList.VERTICAL);
		featureList2.setFont(dfont);
		featureList2.addListSelectionListener(new ListSelectionListener() {
            @Override
            public void valueChanged(ListSelectionEvent arg0) {
                if (!arg0.getValueIsAdjusting()) {
                  if( featureList2.getSelectedValue() != null ) {
                    String s = featureList2.getSelectedValue().toString();
                    performScatter( 1 , s , false );
                  }
                }
            }
        });
		JScrollPane scrollPane2 = new JScrollPane();
		scrollPane2.setViewportView(featureList2);
		scrollPane2.setBounds( x  , y , scrollPaneWidth , scrollPaneHeigth );
		p.add( scrollPane2);
		//
		y += scrollPane2.getHeight() + GAP;
	    scatterdots = new JComboBox<String>(cenums.getScatterDotTypeList());
        scatterdots.setFont(dfont);
        scatterdots.setMaximumSize(scatterdots.getPreferredSize()); // added code
	    scatterdots.setBounds( x , y , scrollPaneWidth , WIDGET_HEIGTH );
	    scatterdots.addItemListener(new ItemListener() {
	        public void itemStateChanged(ItemEvent ev) {
	          if(ev.getStateChange() == ItemEvent.SELECTED) performScatterDot( ev.getItem().toString() );
	        }
	    });
	    p.add(scatterdots);
	    //
	    y += WIDGET_HEIGTH + VERTICAL_GAP;
	    String[] pchoices = { "-" , "3" , "5" , "7" , "9" , "11" , "13" , "15" , "+" };
	    scattersize = new JComboBox<String>(pchoices);
        scattersize.setFont(dfont);
        scattersize.setMaximumSize(scattersize.getPreferredSize()); // added code
	    scattersize.setBounds( x , y , scrollPaneWidth , WIDGET_HEIGTH );
	    scattersize.addItemListener(new ItemListener() {
	        public void itemStateChanged(ItemEvent ev) {
	          if(ev.getStateChange() == ItemEvent.SELECTED) performScatterDiameter( ev.getItem().toString() );
	        }
	    });
	    p.add(scattersize);
	    //
	    y += WIDGET_HEIGTH + VERTICAL_GAP;
	    colorsentiment = new JComboBox<String>(cenums.getColorSentimentTypeList());
        colorsentiment.setFont(dfont);
        colorsentiment.setMaximumSize(scattersize.getPreferredSize()); // added code
	    colorsentiment.setBounds( x , y , scrollPaneWidth , WIDGET_HEIGTH );
	    colorsentiment.addItemListener(new ItemListener() {
	        public void itemStateChanged(ItemEvent ev) {
	          if(ev.getStateChange() == ItemEvent.SELECTED) performColorSelect( ev.getItem().toString() );
	        }
	    });
	    p.add(colorsentiment);
	    
	    //
	    y += WIDGET_HEIGTH + VERTICAL_GAP + 2 ;
	    ckNormalized = new JCheckBox("Normalized");
	    ckNormalized.setFont(dfont);
	    ckNormalized.setSelected( visualizer.isNormalized() );
	    ckNormalized.setBounds( x , y , scrollPaneWidth , WIDGET_HEIGTH );
	    ckNormalized.addItemListener(new ItemListener() {
            public void itemStateChanged(ItemEvent e) {
            	doNormalizer( e.getStateChange() == ItemEvent.SELECTED );
                //System.out.println(e.getStateChange() == ItemEvent.SELECTED ? "SELECTED" : "DESELECTED");
            }
        });
	    p.add( ckNormalized );
	   
		// canvas for scatter diagrams
		lblScatterLeft = new JLabel();
		lblScatterLeft.setBackground( Color.WHITE );
		lblScatterLeft.setBounds( scrollPane.getX() + scrollPane.getWidth() + XMARGIN , scrollPane.getY() , visualizer.getRectangleSide() , visualizer.getRectangleSide() );
		lblScatterLeft.setText("scatterLeft");
		p.add( lblScatterLeft );
		//
		lblScatterRight = new JLabel();
		lblScatterRight.setBackground( Color.WHITE );
		lblScatterRight.setBounds( lblScatterLeft.getX() + lblScatterLeft.getWidth() + XMARGIN , scrollPane.getY() , visualizer.getRectangleSide() , visualizer.getRectangleSide() );
		lblScatterRight.setText("scatterRight");
		p.add( lblScatterRight );
		
		//
		x = scrollPane.getX();
		btn2Overview = new JButton("Overview");
		btn2Overview.setBounds( x , y , BUTTON_WIDTH , BUTTON_HEIGTH);
		btn2Overview.setFont(dfont);
        btn2Overview.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseClicked(MouseEvent arg0) {
			   makeAllDiagrams( cmcMachineLearningEnums.PictureType.SCATTER );
			}
		});
		p.add(btn2Overview);
		//
		x += BUTTON_WIDTH + HORIZONTAL_GAP;
		btnT2Regression = new JButton("Regression");
		btnT2Regression.setBounds( x , 200 , BUTTON_WIDTH , BUTTON_HEIGTH);
		btnT2Regression.setFont(dfont);
        btnT2Regression.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseClicked(MouseEvent arg0) {
			   doLineairRegression( featureList1.getSelectedValue().toString() , featureList2.getSelectedValue().toString() );
			}
		});
		p.add(btnT2Regression);
		//
		x += BUTTON_WIDTH + HORIZONTAL_GAP;
		btnT2Close = new JButton("Close");
		btnT2Close.setBounds( x , 200 , BUTTON_WIDTH , BUTTON_HEIGTH);
		btnT2Close.setFont(dfont);
        btnT2Close.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseClicked(MouseEvent arg0) {
			  dialog.dispose();
			}
		});
		p.add(btnT2Close);
		
	    //		
		return p;
	}

	//------------------------------------------------------------
	private JPanel makeCovarPanel()
	//------------------------------------------------------------
	{
			int x = XMARGIN;
			int y = YMARGIN;
			int GAP = VERTICAL_GAP * 2;
			
			JPanel p = new JPanel();
			p.setLayout(null);   // Absolute positioning
			
			
			y += WIDGET_HEIGTH + 2;
			
			// canvas for scatter diagrams
			lblCorrelation = new JLabel();
			lblCorrelation.setBackground( Color.WHITE );
			lblCorrelation.setBounds( 20 , 20 , visualizer.getCorrelationWidth() , visualizer.getCorrelationHeigth() );
			lblCorrelation.setText("correlation");
			p.add( lblCorrelation );
	
			
		    //		
			return p;
		}
	//------------------------------------------------------------
	private JPanel makeTrainPanel()
	//------------------------------------------------------------
	{
		int x = XMARGIN;
		int y = YMARGIN;
		
		JPanel p = new JPanel();
		p.setLayout(null);   // Absolute positioning

		JLabel lblMethod = new JLabel("Method");
		lblMethod.setFont(dfont);
	    lblMethod.setBounds( x , y , LABEL_WIDTH , WIDGET_HEIGTH );
	    p.add(lblMethod);
	    //
	    y += WIDGET_HEIGTH + VERTICAL_GAP;
	    JLabel lblBins = new JLabel("#Bins");
		lblBins.setFont(dfont);
	    lblBins.setBounds( x , y , LABEL_WIDTH , WIDGET_HEIGTH );
	    p.add(lblBins);
	    //
	    y += WIDGET_HEIGTH + VERTICAL_GAP;
	    JLabel lblTrainPerc = new JLabel("Training Set %");
		lblTrainPerc.setFont(dfont);
	    lblTrainPerc.setBounds( x , y , LABEL_WIDTH , WIDGET_HEIGTH );
	    p.add(lblTrainPerc);
	    
	    //
	    x = lblMethod.getX() + lblMethod.getWidth() + HORIZONTAL_GAP;
	    y = lblMethod.getY();
	    String[] choices = cenums.getMachineLearningTypeList();
	    methode = new JComboBox<String>(choices);
        methode.setFont(dfont);
        methode.setMaximumSize(methode.getPreferredSize()); // added code
	    methode.setBounds( x , y , LABEL_WIDTH*3 , WIDGET_HEIGTH );
	    p.add(methode);
	    //
	    y += WIDGET_HEIGTH + VERTICAL_GAP;
		txtNbrOfBins = new JTextField("" );
		txtNbrOfBins.setBounds( x , y , LABEL_WIDTH * 2, WIDGET_HEIGTH );
		txtNbrOfBins.setFont(dfont);
		p.add(txtNbrOfBins);
	    //
	    y += WIDGET_HEIGTH + VERTICAL_GAP;
		txtTrainPerc = new JTextField("" );
		txtTrainPerc.setBounds( x , y , LABEL_WIDTH * 2, WIDGET_HEIGTH );
		txtTrainPerc.setFont(dfont);
		p.add(txtTrainPerc);
	    
	    //
		x = lblMethod.getX();
		btnT3Run = new JButton("Train");
		btnT3Run.setBounds( x , 200 , BUTTON_WIDTH , BUTTON_HEIGTH);
		btnT3Run.setFont(dfont);
        btnT3Run.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseClicked(MouseEvent arg0) {
			  popMessage( doTrainModel() ? "Completed OK\n" + accuracyInfo : "Error : " + LastErrorMsg );
			}
		});
		p.add(btnT3Run);
	    //
		x += BUTTON_WIDTH + HORIZONTAL_GAP;
		btnT3View = new JButton("View");
		btnT3View.setBounds( x , 200 , BUTTON_WIDTH , BUTTON_HEIGTH);
		btnT3View.setFont(dfont);
        btnT3View.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseClicked(MouseEvent arg0) {
			  viewModel();
			}
		});
		p.add(btnT3View);
		
		
		return p;
	}
	
	//------------------------------------------------------------
	private void makeMLDTO(String fname)
	//------------------------------------------------------------
	{
		   mldto = new MLDTO();
		   if( fname == null ) return;
		   if( xMSet.xU.IsBestand( fname ) == false ) return;
	 	   cmcARFFDAO dap = new cmcARFFDAO( xMSet , logger );
	       boolean ib = dap.performFirstPass( fname );
	       if ( ib == false ) {
	    	   popMessage("Error reading [" + fname + "]\n" + dap.getLastErrorMsg());
	    	   mldto=null;
	    	   return;
	       }
	       //
	       mldto.LongDataFileName = fname;
	       mldto.RelationName     = dap.getRelationName();
	       mldto.FileSize         = xMSet.xU.getFileSize(fname);
	       mldto.DataLines        = dap.getNumberOfDataRows();
	       mldto.featureList      = dap.getProbeList();
	       mldto.firstNonString   = dap.getFirstNonString();
	       //
	       xMSet.setLastARFFFileName(fname);
	       visualizer.setBackGroundColor(btnT1Browse.getBackground());
	       ib = visualizer.prepareVizualizerMemory( fname );
	       if( ib == false ) {
	    	   popMessage("Error reading [" + fname + "]\n" + visualizer.getLastErrorMsg() );
	    	   mldto=null;
	    	   return;
	       }
	       mldto.nbrofbins = visualizer.getNumberOfBins();
	}
	
	//------------------------------------------------------------
	private void selectDataFileName()
	//------------------------------------------------------------
	{
	   gpFileChooser jc = new gpFileChooser( xMSet.getSandBoxDir() );
	   jc.setFilter("ARFF");
   	   jc.runDialog();
       String fname = jc.getAbsoluteFilePath();
       if( fname==null ) return;
       makeMLDTO(fname);
 	}
	
	//------------------------------------------------------------
	private void contentRefresh()
	//------------------------------------------------------------
	{
		 dialog.setTitle( DEFAULT_TITLE );
		 if( mldto == null ) return;
		 txtDataFileName.setText( mldto.LongDataFileName );
		 txtRelationName.setText( mldto.RelationName );
		 txtNbrOfLines.setText( ""+mldto.DataLines );
		 //
		 DefaultListModel<String> model = new DefaultListModel<>();
		 featureList.setModel(model);
		 for ( int i=0; i<mldto.featureList.size(); i++ )
		 {
		   model.addElement( mldto.featureList.get(i) );
		   if( i==mldto.firstNonString ) featureList.setSelectedIndex(i);
		 }
		 //
		 DefaultListModel<String> model1 = new DefaultListModel<>();
		 featureList1.setModel(model1);
		 for ( int i=0; i<mldto.featureList.size(); i++ )
		 {
		   model1.addElement( mldto.featureList.get(i) );
		   if( i==mldto.firstNonString ) featureList1.setSelectedIndex(i);
		 }
		 //
		 DefaultListModel<String> model2 = new DefaultListModel<>();
		 featureList2.setModel(model2);
		 for ( int i=0; i<mldto.featureList.size(); i++ )
		 {
		   model2.addElement( mldto.featureList.get(i) );
		   if( i==mldto.firstNonString ) featureList2.setSelectedIndex(i);
		 }
		
		 //
		 cmcProcEnums.SCATTER_DOT_TYPE scat = visualizer.getScatterDotType();
		 if( scat != null ) {
		  for ( int i=0; i<scatterdots.getModel().getSize(); i++ )
		  {
		   String sl = scatterdots.getModel().getElementAt(i);
		   if( sl.compareToIgnoreCase( ""+scat ) == 0 ) { scatterdots.getModel().setSelectedItem((String)sl); break; }
		  }
		 }
		 //
		 cmcProcEnums.ColorSentiment csdef = visualizer.getColorSentiment();
		 if( csdef != null ) {
		  for ( int i=0; i<colorsentiment.getModel().getSize(); i++ )
		  {
		   String sl = colorsentiment.getModel().getElementAt(i);
		   if( sl.compareToIgnoreCase( ""+csdef ) == 0 ) { colorsentiment.getModel().setSelectedItem((String)sl); break; }
		  }
		 }
				 
		 //
		 txtNbrOfBins.setText( ""+mldto.nbrofbins );
		 txtTrainPerc.setText( ""+cmcMachineLearningConstants.TRAININGSET_PERCENTAGE + "%" );
		 String title = (mldto.RelationName==null) ? "" : mldto.RelationName;
	     if( title.length() != 0 ) title = " [" + title + "]";
		 dialog.setTitle( DEFAULT_TITLE + title);
	}
	
	//------------------------------------------------------------
	private boolean updateMLDTO()
	//------------------------------------------------------------
	{
		String mltipe = (String)methode.getSelectedItem();
		if( mltipe == null ) return false;
		mltipe = mltipe.trim().toUpperCase();
		cmcProcEnums.MACHINE_LEARNING_TYPE tipe = cenums.getMachineLearningType(mltipe);
		if( tipe == null ) {
			do_error("Unsupported tipe [" + mltipe + "]");
			return false;
		}
		mldto.ModelType = tipe;
		String FileName = xMSet.xU.getFileNameWithoutSuffix( mldto.LongDataFileName );
		if( FileName == null ) { do_error("Could not strip suffix [" + mldto.LongDataFileName + "]"); return false; }
		mldto.LongModelName = FileName.trim() + "_" + (""+tipe).toLowerCase() + ".xml";
		return true;
	}
	
	//------------------------------------------------------------
	private boolean doTrainModel()
	//------------------------------------------------------------
	{
		accuracyInfo = null;
		if( updateMLDTO() == false ) return false;
		xMSet.setMachineLearningType( mldto.ModelType );
	   
	    //  TODO ?? MULTI THREADED version
		// Gradually move all models to the controller
		if( (mldto.ModelType == cmcProcEnums.MACHINE_LEARNING_TYPE.BAYES) || 
			(mldto.ModelType == cmcProcEnums.MACHINE_LEARNING_TYPE.K_NEAREST_NEIGHBOURS) || 
			(mldto.ModelType == cmcProcEnums.MACHINE_LEARNING_TYPE.DECISION_TREE) ) {
		 cmcMachineLearningModelController ctrl = new cmcMachineLearningModelController(xMSet,logger);
		 boolean ib = ctrl.makeAndTrainMLModel( mldto.LongDataFileName , mldto.LongModelName);
	     ctrl=null;
		 if( ib == false ) { ctrl =null; return false; }

		}
		else
		if( mldto.ModelType == cmcProcEnums.MACHINE_LEARNING_TYPE.LOGISTIC_REGRESSION ) {
			if( doLogisticRegression() == false ) return false;
		}
		else
		if( mldto.ModelType == cmcProcEnums.MACHINE_LEARNING_TYPE.SMO ) {
			if( doSMO() == false ) return false;
		}
		else {
			do_error("Unsupported tipe [" + mldto.ModelType +"]" );
			return false;
		}
		// there is a model file - fetch the accuracies
		fetchAccuracies(   mldto.LongModelName );
		return true;
	}
	
	//------------------------------------------------------------
	private void fetchAccuracies( String ModelFileName )
	//------------------------------------------------------------
	{
	    if( xMSet.xU.IsBestand( ModelFileName ) == false ) return;
	    String ret = xMSet.xU.ReadContentFromFile( ModelFileName , 50 , "ASCII" );
	    int nlines = xMSet.xU.TelDelims( ret , '\n' );
	    boolean keep=false;
	    for(int i=1;i<(nlines-1);i++)
	    {
	    	String sline = xMSet.xU.getField( ret , i , "\n" );
	    	if( sline == null ) continue;
	    	if( sline.indexOf("<TrainedModel>") >= 0 ) { keep = true; accuracyInfo = "Results training";  continue; }
	    	if( sline.indexOf("</TestedModel>" ) >= 0 ) { keep = false; continue; }
	    	if( !keep ) continue;
	    	if( sline.indexOf("</TrainedModel>") >= 0 ) continue;
	    	if( sline.indexOf("<TestedModel>") >= 0 ) sline = "Results testing";
	    	if( sline.indexOf("<![CDATA") >= 0 ) continue;
	    	if( sline.indexOf("]]>") >= 0 ) continue;
	    	accuracyInfo += "\n" + sline;
	    }
	}
	
	//------------------------------------------------------------
	private boolean performHistogram(String featureName)
	//------------------------------------------------------------
	{
	    lblHisto.setText(featureName);
	    if( visualizer == null ) return false;
	    BufferedImage img = visualizer.getPictureFromCache( featureName , featureName , cmcMachineLearningEnums.PictureType.HISTOGRAM );
	    if( img == null ) { lblHisto.setVisible(false); return false; }
	    if( lblHisto.isVisible() == false ) lblHisto.setVisible(true);
	    lblHisto.setIcon(new ImageIcon(img));
		return true;
	}
	
	//------------------------------------------------------------
	private boolean performScatter( int idx , String featureName , boolean RELOAD )
	//------------------------------------------------------------
	{
	//do_log( 1 , "" + idx + " " + featureName + " " + RELOAD );
		if( visualizer == null ) return false;
		if( RELOAD == false ) {
		  if( (idx!=0) && (idx!=1) ) return false;
		  scatterTuple[ idx ] = featureName;
		  if( scatterTuple[0] == null ) return false;
		  if( scatterTuple[1] == null ) return false;
		}
		else visualizer.purgeCache();
		//
		BufferedImage imgL = visualizer.getPictureFromCache( scatterTuple[0] , scatterTuple[1] , cmcMachineLearningEnums.PictureType.SCATTER );
		if( imgL == null ) { lblScatterLeft.setVisible(false); return false; }
		if( lblScatterLeft.isVisible() == false ) lblScatterLeft.setVisible(true);
		lblScatterLeft.setIcon(new ImageIcon(imgL));
		//
		BufferedImage imgR = visualizer.getPictureFromCache( scatterTuple[1] , scatterTuple[0] , cmcMachineLearningEnums.PictureType.SCATTER );
		if( imgR == null ) { lblScatterLeft.setVisible(false); return false; }
		if( lblScatterRight.isVisible() == false ) lblScatterRight.setVisible(true);
		lblScatterRight.setIcon(new ImageIcon(imgR));
		return true;
	}
	
	//------------------------------------------------------------
	private boolean performScatterDot( String s)
	//------------------------------------------------------------
	{
		if( s == null ) return false;
		cmcProcEnums.SCATTER_DOT_TYPE tipe = cenums.getScatterDotType(s);
		if( visualizer == null ) return false;
		visualizer.setScatterDotType( tipe );
		return performScatter( -1 , null , true );
	}
	
	//------------------------------------------------------------
	private boolean performColorSelect( String s)
	//------------------------------------------------------------
	{
		if( s == null ) return false;
		cmcProcEnums.ColorSentiment tipe = cenums.getColorSentimentType(s);
		if( visualizer == null ) return false;
		visualizer.setColorSentiment( tipe );
		return performScatter( -1 , null , true );
	}
	
	//------------------------------------------------------------
	private boolean performScatterDiameter( String s)
	//------------------------------------------------------------
	{
		int dia=0;
		if( s == null ) return false;
		if( s.startsWith("-") ) dia = -999;
		else
		if( s.startsWith("+") ) dia = 999;
		else dia = xMSet.xU.NaarInt(s); 
		if( visualizer == null ) return false;
		visualizer.setScatterDotDiameter( dia );
		return performScatter( -1 , null , true );
	}
	
	//------------------------------------------------------------
	private boolean doNormalizer(boolean ib)
	//------------------------------------------------------------
	{
		if( visualizer == null ) return false;
		visualizer.setNormalised(ib);
		return performScatter( -1 , null , true );
	}
	//------------------------------------------------------------
	private void performTabPane( int paneIndex )
	//------------------------------------------------------------
	{
		if( paneIndex == 2) performCorrelationDiagram();
	}
	//------------------------------------------------------------
	private boolean performCorrelationDiagram()
	//------------------------------------------------------------
	{
		if( visualizer == null ) return false;
		String dummyCatName = (String)featureList.getModel().getElementAt(0);
		BufferedImage imgL = visualizer.getPictureFromCache( dummyCatName ,dummyCatName , cmcMachineLearningEnums.PictureType.CORRELATION );
		if( imgL == null ) { lblCorrelation.setVisible(false); return false; }
		if( lblCorrelation.isVisible() == false ) lblCorrelation.setVisible(true);
		lblCorrelation.setIcon(new ImageIcon(imgL));
		return true;
	}
		
	//------------------------------------------------------------
	private boolean openViewer(String fname)
	//------------------------------------------------------------
	{
		String[] pp = new String[2];
  		pp[0] = fname;
  		pp[1] = null;
		cmcFileViewer ff = new cmcFileViewer( null , xMSet , logger , pp);
		ff=null;
		return true;
	}
	
	//------------------------------------------------------------
	private boolean makeAllDiagrams( cmcMachineLearningEnums.PictureType tipe )
	//------------------------------------------------------------
	{
		if( visualizer == null ) return false;
		String BaseName = null;
		switch( tipe )
		{
		case HISTOGRAM : { BaseName = "MachineLearningAllHistograms"; break; }
		case SCATTER   : { BaseName = "MachineLearningAllScatters"; break; }
		default : { do_error("(makeAllDiagrams) unsupported type [" + tipe + "]"); break; }
		}
		String sDir = xMSet.getTempDir(); //xMSet.getCacheDir();
		String ImageFileName = sDir + xMSet.xU.ctSlash + BaseName + ".png";
		if( xMSet.xU.IsBestand( ImageFileName ) ) xMSet.xU.VerwijderBestand( ImageFileName );
		//
		Color oldBackGroundColor = visualizer.getBackGroundColor();
		visualizer.setBackGroundColor( Color.WHITE );
		boolean ib=false;
		int imgwidth = -1;
		int imgheigth = -1;
		switch( tipe )
		{
		case HISTOGRAM : {  ib = visualizer.dumpAllHistograms( ImageFileName ); 
							if( ib == false ) return false;
							imgwidth = visualizer.getAllHistogramsWidth();
							imgheigth = visualizer.getAllHistogramsHeigth();
		                    break; }
		case SCATTER   : {  ib = visualizer.dumpAllScatterDiagrams( ImageFileName ); 
							if( ib == false ) return false;
							imgwidth = visualizer.getAllScatterWidth();
							imgheigth = visualizer.getAllScatterHeigth();
							break; }
		default : { do_error("(makeAllDiagrams) unsupported type [" + tipe + "]"); break; }
		}
		visualizer.setBackGroundColor( oldBackGroundColor );
		if( ib == false ) return false;
		if( imgwidth < 0 ) return false;
		if( imgheigth < 0 ) return false;
		//
		String HTMLFileName = makeHTMLToViewImage( ImageFileName , imgwidth , imgheigth );
		if( HTMLFileName == null ) return false;
		if( xMSet.xU.IsBestand( HTMLFileName ) == false ) return false;
		//
		openViewer(HTMLFileName);
		//
		if( xMSet.xU.IsBestand( ImageFileName ) ) xMSet.xU.VerwijderBestand( ImageFileName );
		if( xMSet.xU.IsBestand( HTMLFileName ) ) xMSet.xU.VerwijderBestand( HTMLFileName );
		return true;
	}
	
	//------------------------------------------------------------
	private String makeHTMLToViewImage( String ImageFileName , int imgwidth , int imgheigth )
	//------------------------------------------------------------
	{
		String HTMLFileName = xMSet.xU.getFileNameWithoutSuffix( ImageFileName );
		if( HTMLFileName == null ) return null;
		HTMLFileName = HTMLFileName + ".html";
		if( xMSet.xU.IsBestand( HTMLFileName ) ) xMSet.xU.VerwijderBestand( HTMLFileName );
		String CSSFileName = xMSet.getReportHTMLDir() + xMSet.xU.ctSlash + "cbrTekStraktorCSS.txt";
	    //
		gpPrintStream html = new gpPrintStream( HTMLFileName , "UTF-8");
		html.println("<!DOCTYPE html>");
		html.println("<!-- Generated by : "  + xMSet.getApplicDesc() +" -->");
		html.println("<!-- Generated at : "  +  xMSet.xU.prntStandardDateTime(System.currentTimeMillis()) +" -->");
		html.println("<!-- ZOOMABLE -->");
		html.println("<html>");
		html.println("<head>");
		html.println("<link rel=\"stylesheet\" href=\"" + CSSFileName + "\">");
		html.println("<style>");
		html.println(".large_text");
		html.println("{");
		html.println("font-family: Arial, Helvetica, sans-serif;");
		html.println("font-size: 20px;");
		html.println("}");
		html.println(".medium_text");
		html.println("{");
		html.println("font-family: Arial, Helvetica, sans-serif;");
		html.println("font-size: 12px;");
		html.println("}");
		html.println(".small_text");
		html.println("{");
		html.println("font-family: Arial, Helvetica, sans-serif;");
		html.println("font-size: 8px;");
		html.println("}");
		html.println("</style>");
		html.println("</head>");
		html.println("<body>");
		html.println("<div class=\"large_text\">");
		html.println( "Relationship : " + mldto.RelationName + "<br>");
		html.println("</div>");
		html.println("<img src=\"file:///" + ImageFileName + "\" alt=\"allhistograms\" height=\"" + imgheigth + "\" width=\"" + imgwidth + "\" >" ); 
		if( mldto.hyperplane != null ) {
	        String shyp = "";
	        for(int i=0 ; i<mldto.hyperplane.length ; i++)
	        {
	           int k = mldto.hyperplane.length - (i+1);
	           shyp += String.format( "%4.5f" , mldto.hyperplane[ k ] );
	           if( k != 0 ) {
	        	   if( k == 1 ) shyp += " x";
	        	           else shyp += " (x^" + k +")";
	        	   if( mldto.hyperplane[k-1] >= 0 ) shyp += " + "; else shyp += "  ";
	           }
	        }
	        html.println("<div class=\"medium_text\">");
	        html.println( "Polynome degree         : " + mldto.degree + "<br>");
	        html.println( "Stepsize                : " + mldto.stepsize + "<br>");
	        html.println( "Max Number of Cycles    : " + mldto.cycles + "<br>");
	        html.println( "Mean Squared Error      : " + mldto.MeanSquareError + "<br>");
			html.println( "Regression fitted curve : " + shyp + "<br>");
			html.println("</div>");
		}
		
		html.println("</body>");
		html.println("</html>");
		html.close();
		//
		return HTMLFileName;
	}
	
	//------------------------------------------------------------
	private boolean viewModel()
	//------------------------------------------------------------
	{
	   if( updateMLDTO() == false ) return false;
	   if( xMSet.xU.IsBestand( mldto.LongModelName) == false ) {
		   popMessage("Model file not found [" + mldto.LongModelName + "]" );
		   return false;
	   }
	   openViewer( mldto.LongModelName );
	   return false;	
	}
	
	//------------------------------------------------------------
	private boolean viewDataFile()
	//------------------------------------------------------------
	{
	   if( xMSet.xU.IsBestand( mldto.LongDataFileName) == false ) {
		   popMessage("ARFF file not found [" + mldto.LongDataFileName + "]" );
		   return false;
	   }
	   openViewer( mldto.LongDataFileName);
	   return false;	
	}
	
	//------------------------------------------------------------
	private boolean doLogisticRegression()
	//------------------------------------------------------------
	{
		mldto.hyperplane = null;
		mldto.degree= -1;
		mldto.cycles= mldto.cycles < 0 ? cmcMachineLearningConstants.MAX_NUMBER_OF_LOGISTIC_REGRESSION_CYCLES : mldto.cycles;
		mldto.stepsize= Double.isNaN( mldto.stepsize ) ? cmcMachineLearningConstants.LOGISTIC_REGRESSION_STEPSIZE : mldto.stepsize;
		mldto.MeanSquareError=-1;
		
		// Very nice trick - Joption pane with mulitple entry fields
		JTextField jcycles = new JTextField(10); jcycles.setText(""+mldto.cycles);
		JTextField jstep = new JTextField(10); jstep.setText(""+mldto.stepsize);
		Object[] msg = {"Number of cycles:",  jcycles , "Step size:" , jstep };
		int result = JOptionPane.showConfirmDialog(
		            null,
		            msg,
		            "Logistic regression parameters",
		            JOptionPane.OK_CANCEL_OPTION,
		            JOptionPane.PLAIN_MESSAGE);
		if (result == JOptionPane.YES_OPTION)
		{
		            mldto.cycles = xMSet.xU.NaarInt( jcycles.getText() );
		            mldto.stepsize = xMSet.xU.NaarDoubleNAN( jstep.getText() );
		}
		else return false;
		if( mldto.cycles < 0 ) { popMessage( "Invalid number of cycles [" + mldto.cycles + "]"); return false; }
		if( Double.isNaN(mldto.stepsize) ) { popMessage( "Invalid stepsize [" + jstep.getText() + "]"); return false; }
		if( (mldto.stepsize <= 0) || (mldto.stepsize > 1) ) { popMessage( "Invalid number of stepsize [" + mldto.cycles + "]"); return false; }
		//
		cmcLogisticRegression lg = new cmcLogisticRegression( xMSet , logger );
		boolean ib = lg.testLogisticRegression( mldto.LongDataFileName , mldto.LongModelName , mldto.cycles , mldto.stepsize);
		if( ib == false ) { LastErrorMsg = lg.getLastErrorMsg(); lg =null; return false; }
		//
		int optimal = lg.getOptimalNumberOfCycles();
		String sl = (optimal == mldto.cycles) ? "Optimal number of cycles could not be found" : null;
	    if( sl != null ) popMessage( sl );
		lg=null;
		return true;
	}
	
	//------------------------------------------------------------
	private boolean doLineairRegression(String feat1 , String feat2 )
	//------------------------------------------------------------
	{
		mldto.hyperplane = null;
		mldto.degree= mldto.degree < 0 ? 2 : mldto.degree;
		mldto.cycles= mldto.cycles < 0 ? cmcMachineLearningConstants.MAX_NUMBER_OF_LINEAIR_REGRESSION_CYCLES : mldto.cycles;
		mldto.stepsize= Double.isNaN( mldto.stepsize ) ? cmcMachineLearningConstants.LINEAIR_REGRESSION_STEPSIZE : mldto.stepsize;
		mldto.MeanSquareError=-1;
		if( (feat1 == null) || (feat2==null) || (mldto==null) ) return false;
	    String[] flist = new String[2];
		flist[0] = feat1;
		flist[1] = feat2;
		     
		// Very nice trick - Joption pane with mulitple entry fields
		JTextField jdegree = new JTextField(10); jdegree.setText(""+mldto.degree);
		JTextField jcycles = new JTextField(10); jcycles.setText(""+mldto.cycles);
		JTextField jstep = new JTextField(10); jstep.setText(""+mldto.stepsize);
        Object[] msg = {"Polynomial degree:", jdegree , "Number of cycles:",  jcycles , "Step size:" , jstep };
        int result = JOptionPane.showConfirmDialog(
            null,
            msg,
            "Lineair regression parameters",
            JOptionPane.OK_CANCEL_OPTION,
            JOptionPane.PLAIN_MESSAGE);
        if (result == JOptionPane.YES_OPTION)
        {
            mldto.degree = xMSet.xU.NaarInt( jdegree.getText() );
            mldto.cycles = xMSet.xU.NaarInt( jcycles.getText() );
            mldto.stepsize = xMSet.xU.NaarDoubleNAN( jstep.getText() );
        }
        else return false;
        if( mldto.degree < 0 ) { popMessage( "Invalid degree [" + mldto.degree + "]"); return false; }
        if( mldto.cycles < 0 ) { popMessage( "Invalid number of cycles [" + mldto.cycles + "]"); return false; }
        if( Double.isNaN(mldto.stepsize) ) { popMessage( "Invalid stepsize [" + jstep.getText() + "]"); return false; }
        if( (mldto.stepsize <= 0) || (mldto.stepsize > 1) ) { popMessage( "Invalid number of stepsize [" + mldto.cycles + "]"); return false; }
        //
		cmcLinearRegression per = new cmcLinearRegression(xMSet,logger);
		boolean ib = per.testLinearRegression( mldto.LongDataFileName , flist , mldto.degree , mldto.cycles , mldto.stepsize );
  		if( ib == false ) {
  			popMessage( per.getLastErrorMsg() );
  			return false;
  		}
  		mldto.hyperplane = per.getWeights();
  		mldto.MeanSquareError = per.getMeanSquareError();
		//  prepare to display image
		String ImageFileName = per.getRegressionFileName();
  		if( ImageFileName == null ) return false;
  		int imgwidth = per.getCanvasWidth();
  		if( imgwidth <= 0 ) return false;
  		int imgheigth = per.getCanvasHeigth();
  		if( imgheigth <= 0 ) return false;
  		String HTMLFileName = makeHTMLToViewImage( ImageFileName , imgwidth , imgheigth );
  	    if( HTMLFileName == null ) return false;	
  	    //
		openViewer(HTMLFileName);
		//
		if( xMSet.xU.IsBestand( ImageFileName ) ) xMSet.xU.VerwijderBestand( ImageFileName );
		if( xMSet.xU.IsBestand( HTMLFileName ) ) xMSet.xU.VerwijderBestand( HTMLFileName );
		//
		per=null;
  		return true;
	}
	
	
	private boolean doSMO()
	{
		
		
	   JTextField jsoft = new JTextField(10); jsoft.setText("0.6");
	   JTextField jtolerance = new JTextField(10); jtolerance.setText("0.001");
	   JTextField jcycles = new JTextField(10); jcycles.setText("40");
	   JComboBox jkernel = new JComboBox(); jkernel.addItem("NONE");jkernel.addItem("RBF");jkernel.addItem("GAUSSIAN");
	   JTextField jsigma = new JTextField(10); jsigma.setText("5");
	   JTextField jgamma = new JTextField(10); jgamma.setText("1");
	   Object[] msg = {"Soft margin:", jsoft , "Number of cycles:",  jcycles , "Tolerance:" , jtolerance , "Kernel:" , jkernel , "Sigma:" , jsigma , "Gamma:" , jgamma };
	   int result = JOptionPane.showConfirmDialog(
	            null,
	            msg,
	            "Sequential Minimum Optimization",
	            JOptionPane.OK_CANCEL_OPTION,
	            JOptionPane.PLAIN_MESSAGE);
	    if (result == JOptionPane.YES_OPTION) {
		        	
	    }
	    else return false;
		//
		cmcSMO smo = new cmcSMO(xMSet,logger);
		boolean ib = smo.testSMO( mldto.LongDataFileName , mldto.LongModelName , cmcMachineLearningEnums.KernelType.GAUSSIAN );
  		if( ib == false ) {
  			popMessage( smo.getLastErrorMsg() );
  			smo=null;
  			return false;
  		}
  		
  		
  		smo=null;
  		return true;
	}
	
}
