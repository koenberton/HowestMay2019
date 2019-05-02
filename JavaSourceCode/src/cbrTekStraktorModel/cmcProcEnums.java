package cbrTekStraktorModel;



public class cmcProcEnums {
	
	cmcProcSettings xMSet = null;
	//
	public enum OS_TYPE { LINUX , MSDOS }
	public enum ENCODING { ISO_8859_1 , UTF_8 , UTF_16 , ASCII }  
	public enum ClusterClassificationMethod { AUTOMATIC , Cluster1 , Cluster2 , Cluster3 , Cluster4 , Cluster5 }
	public enum BinarizeClassificationMethod { ITERATIVE , FAST_BLEACHED , OTSU , SLOW_NIBLAK, SLOW_SAUVOLA }
	public enum ProximityTolerance { TIGHT , LENIENT , WIDE , ULTRA_WIDE }
	public enum CroppingType { DO_NOT_CROP_IMAGE , CROP_IMAGE }
	public enum ParagraphType { NOISE , LETTER , BUBBLE , FRAME , UNKNOWN }
	public enum TextConfidence { TEXT , NO_TEXT , UNKNOWN }
	public enum ColourSchema { UNKNOWN , COLOUR , GRAYSCALE , MONOCHROME }
	public enum BackdropType { UNKNOWN , ORIGINAL , GRAYRASTERIZED , COLORRASTERIZED , BLEACHED , BLACKBLEACHED , BLUEPRINT , GRAYSCALE , MONOCHROME_NIBLAK , MONOCHROME_SAUVOLA , MONOCHROME_OTSU , MAINFRAME , NONE };
	public enum WiskerColor { RED, GREEN, YELLOW , WHITE , BLACK , BLUE , GRAY, CYAN, NONE };
	public enum PageObjectType { UNKNOWN , FRAME , TEXTPARAGRAPH , PARAGRAPH , LETTER , NOISE };
	public enum VisibilityType { VISIBLE , INVISIBLE };
	public enum EditChangeType { NONE , UNLINK , LINK , REMOVE , CREATE , TO_TEXT , TO_NO_TEXT , UNDO_REMOVE , RELINK};
	public enum OCR_CURATION { IGNORE_DPI , USE_IMAGE_DPI , INCREASE_IMAGE_DPI , INCREASE_IMAGE_DPI_AND_CONVOLVE , INCREASE_IMAGE_DPI_AND_FOURRIER }
	public enum TESS_OPTION_SELECTION { WITHOLD , PARAMETER , VALUE , DESCRIPTION  } 
	//	
	public enum APPLICATION_STATE { IDLE , IMAGELOADING , IMAGE , EXTRACTING , EXTRACTED , EDITLOADING , EDIT , STATSGATHERING , STATISTICS , OCR_BUSY}
	//
	public enum MACHINE_LEARNING_TYPE { UNKNOWN , TENSORFLOW , BAYES , SVM , DECISION_TREE , LOGISTIC_REGRESSION , SMO , K_NEAREST_NEIGHBOURS }
	//public enum AI_TYPES { UNKNOWN , BAYES , TENSORFLOW , SVM , DECISION_TREE , KNN }
	
	public enum MENU_ITEMS {
		 PROJECT_START , OPEN_PROJECT , EDIT_PROJECT , NEW_PROJECT , PROJECT_STOP ,
		 DIV0 , OPEN_IMAGE , OPEN_REPORT , OPEN_ARCHIVE , 
		 DIV1 , SAVE , 
		 DIV2 , INFO , TOGGLE_DETAIL_PANE , REFRESH , 
		 DIV3 , 
		 IMPORT , IMPORT_TEXT , IMPORT_CONT ,
		 EXPORT , EXPORT_TEXT , EXPORT_CONT ,
		 DIV10,
		 CLASSIFIERS , 
		 MACHINE_LEARNING_MODELER , DIV78 ,
		 NAIVE_BAYES , MAKE_NAIVE_BAYES_TRAINING_SET , EXTRACT_NAIVE_BAYES_FEATURES , TRAIN_NAIVE_BAYES_MODEL , EXTRACT_NAIVE_BAYES_SINGLE_SET , READJUST_TEXTBUBBLES_VIA_NAIVE_BAYES , NAIVE_BAYES_CONT , 
		 DECISION_TREE , MAKE_DECISION_TREE_TRAINING_SET , EXTRACT_DECISION_TREE_FEATURES , TRAIN_DECISION_TREE_MODEL , EXTRACT_DECISION_TREE_SINGLE_SET , READJUST_TEXTBUBBLES_VIA_DECISION_TREE , DECISION_TREE_CONT , 
		 K_NEAREST_NEIGHBOURS , MAKE_KNN_TRAINING_SET , EXTRACT_KNN_FEATURES , TRAIN_KNN_MODEL , EXTRACT_KNN_SINGLE_SET, READJUST_TEXTBUBBLES_VIA_KNN, K_NEAREST_NEIGHBOURS_CONT ,
		 
		 SUPPORT_VECTOR_MACHINE , MAKE_SVM_TRAINING_SET , EXTRACT_SVM_FEATURES , TRAIN_SVM_MODEL , EXTRACT_SVM_SINGLE_SET ,  SUPPORT_VECTOR_MACHINE_CONT ,
		 GOOGLE , MAKE_TENSORFLOW_TRAINING_SET , EXTRACT_TENSORFLOW_FEATURES , TRAIN_TENSORFLOW_MODEL , EXTRACT_TENSORFLOW_SINGLE_SET , READJUST_TEXTBUBBLES_VIA_TENSORFLOW , GOOGLE_CONT , 
		 CLASSIFIERS_CONT,
		 DIV4 , PROPERTIES_START , GENERAL_PROPERTIES , EDIT_OPTIONS , TESSERACT_OPTIONS , TESSERACT_VERSION , PROPERTIES_STOP , 
		 
		 DIV5, RECENT1 , RECENT2 , RECENT3 , RECENT4 , RECENT5 , 
		 DIV6 , EXIT ,
		 TOOLS ,
		 LOAD_IMAGE , EXTRACT_TEXT , 
		 EDIT_ZONE_START , START_EDIT , STOP_EDIT , QUICK_EDIT , DETAILED_EDIT , EDIT_ZONE_STOP ,
		 OCR , TRANSLATE , REPORT , REINJECT ,  
		 DIV7 ,  IMAGE_FILTERS ,
		 ORIGINAL, GRAYSCALE, BLEACHED , MONOCHROME_OTSU , MONOCHROME_NIBLAK, MONOCHROME_SAUVOLA, BLUEPRINT, MAINFRAME , INVERT, HISTOGRAM_EQUALISATION,
		 HORIZONTAL_GRADIENT , VERTICAL_GRADIENT , GRADIENT_MAGNITUDE , GRADIENT_THETA ,
		 CONVOLUTION_GLOW , CONVOLUTION_EDGE , CONVOLUTION_SHARPEN , CONVOLUTION_GAUSSIAN , SOBEL , SOBEL_ON_GRAYSCALE ,
		 GRADIENT_WIDE , GRADIENT_NARROW ,
		 CANNY , MAXLLOYD , TEXTURE ,
		 TOOLS_CONT ,
		 DIV45 , LOGGING , VIEW_ERRORS , VIEW_LOGGING , LOGGING_CONT ,
		 DIV8 ,
		 MORE , HOUSEKEEPING , STATISTICS , MONITOR , ARCHIVE_BROWSER , ARCHIVE_VIEWER ,
		 MORE_CONT
		 }
    //
	public enum POP_ITEMS {  
		START_IMAGE ,
        LOAD_IMAGE , EXTRACT_TEXT , START_EDIT , STOP_EDIT , OCR , TRANSLATE , REPORT , REINJECT , 
        DIV100 , IMAGE_REFRESH , IMAGE_SAVE , IMAGE_INFO ,  
        
        START_EXTRACTED ,
        EXTRACT_REFRESH , EXTRACT_SAVE , EXTRACT_INFO ,
        
        EDIT_ZONE_START ,
        QUICK_EDIT , DETAILED_EDIT , SET_TO_TEXT , SET_TO_NON_TEXT , DELETE ,
        DIV_300,
        EDIT_REFRESH , EDIT_SAVE , EDIT_INFO ,
        DIV440 , 
        EDIT_OPTIONS , 
        DIV301, 
        IMAGE_FILTER , 
        ORIGINAL, GRAYSCALE, BLEACHED , MONOCHROME_OTSU , MONOCHROME_NIBLAK, MONOCHROME_SAUVOLA, BLUEPRINT, MAINFRAME , INVERT, HISTOGRAM_EQUALISATION ,
        HORIZONTAL_GRADIENT , VERTICAL_GRADIENT , GRADIENT_MAGNITUDE , GRADIENT_THETA ,
        CONVOLUTION_GLOW , CONVOLUTION_EDGE , CONVOLUTION_SHARPEN , CONVOLUTION_GAUSSIAN , SOBEL , SOBEL_ON_GRAYSCALE , 
        GRADIENT_WIDE , GRADIENT_NARROW ,
        IMAGE_FILTER_STOP ,
        DIV401 ,
        STATISTICS , MONITOR , ARCHIVE_BROWSER , ARCHIVE_VIEWER , STOP
	}

	public enum QUICKCOLS { ISTEXT , IMAGE, EXTRACTED_TEXT ,  REMOVED, WIDTH , HEIGHT ,  ELEMENTS , LETTERS , UID , CLASSIFICATION , CHANGETYPE};

	public enum QUICKEDITOPTIONS { TEXT_AREAS , POTENTIAL_TEXT_AREAS , LETTER , FRAMES , TEXT_BUBBLES , NOISE , ALL_CLUSTERED_OBJECTS }
	
	public enum PROJECT_OPTIONS { NEW , EDIT , OPEN , UNKNOWN}
	public enum PROJECT_DROP  { PROJECT , ENCODING , LANGUAGE , EDITOR_BACKDROP , BROWSER , 
		                        TEXTBUBBLE_REASSESSMENT_COVERAGE , END_TO_END_VISUAL_RECOGNITION , END_TO_END_COMPRISES_OCR }
	public enum PROJECT_LABEL { ROOT_FOLDER , PROJECT_NAME , DESCRIPTION , MEAN_CHARACTER_COUNT , HORIZONTAL_VERTICAL_VARIANCE_THRESHOLD , 
								TESSERACT_FOLDER , PYTHON_FOLDER , MAXIMUM_NUMBER_OF_THREADS ,
		                        PREFERRED_FONT_NAME , PREFERRED_FONT_SIZE , 
		                        LOGGING_LEVEL , DATEFORMAT , SPLASH_PICTURE , 
		                        VERSION, SIZE , NUMBER_OF_ARCHIVES , FIRST_ACCESSED , LAST_ACCESSED }
	public enum BROWSER { MOZILLA , EXPLORER , CHROME }
	public enum BULK_MONITOR { COMPLETED , FILENAME , START_TIME , ELAPSED , COMMENT }
	public enum REASSESMENT_COVERAGE { REASSESS_NOTHING , REASSESS_TEXT_PARAGRAPHS_ONLY , REASSESS_ALL }
	
	public enum YESNO { YES , NO }
	public enum HOG_GRADIENT_DISPLAY_TYPE { NONE , HORIZONTAL_GRADIENT , VERTICAL_GRADIENT , MAGNITUDE , THETA , WIDE , NARROW}
	public enum SCATTER_DOT_TYPE { RECTANGLE , CIRCLE , FILLED_RECTANGLE , CROSS }
	public enum ColorSentiment { SOFT , HARSH , RED , BLUE , GREEN }
	public enum DIRECTION { LEFT , RIGHT , UP , DOWN , NONE }
	
	//---------------------------------------------------------------------------------
	public cmcProcEnums(cmcProcSettings iM)
	//---------------------------------------------------------------------------------
	{
		xMSet = iM;
	}

	//---------------------------------------------------------------------------------
	private String[] getSortedList(String[] in)
	//---------------------------------------------------------------------------------
	{
		int aantal = in.length;
		String lst[] = new String[aantal];
		for(int i=0;i<aantal;i++) {
			String s = in[i] + " "; 
			s = in[i].toUpperCase().substring(0,1).trim() + in[i].toLowerCase().substring(1);
			lst[i] = s.trim();
		}
		return xMSet.xU.sortStringArray(lst);	
	}
	//---------------------------------------------------------------------------------
	public String[] getClusterMethodList()
	//---------------------------------------------------------------------------------
	{
		String lijst[] = new String[cmcProcEnums.ClusterClassificationMethod.values().length];
		for(int i=0;i<cmcProcEnums.ClusterClassificationMethod.values().length;i++)
		{
			lijst[i] = cmcProcEnums.ClusterClassificationMethod.values()[i].toString();
		}
		return getSortedList(lijst);
	}
	//---------------------------------------------------------------------------------
	public String[] getBinarizeMethodList()
	//---------------------------------------------------------------------------------
	{
		String lijst[] = new String[cmcProcEnums.BinarizeClassificationMethod.values().length];
		for(int i=0;i<cmcProcEnums.BinarizeClassificationMethod.values().length;i++)
		{
			lijst[i] = cmcProcEnums.BinarizeClassificationMethod.values()[i].toString();
		}
		return getSortedList(lijst);
	}
	//---------------------------------------------------------------------------------
	public String[] getProximityToleranceList()
	//---------------------------------------------------------------------------------
	{
		String lijst[] = new String[cmcProcEnums.ProximityTolerance.values().length];
		for(int i=0;i<cmcProcEnums.ProximityTolerance.values().length;i++)
		{
			lijst[i] = cmcProcEnums.ProximityTolerance.values()[i].toString();
		}
		return getSortedList(lijst);
	}
	//---------------------------------------------------------------------------------
	public String[] getColourSchemaList()
	//---------------------------------------------------------------------------------
	{
		String lijst[] = new String[cmcProcEnums.ColourSchema.values().length];
		for(int i=0;i<cmcProcEnums.ColourSchema.values().length;i++)
		{
			lijst[i] = cmcProcEnums.ColourSchema.values()[i].toString();
		}
		return getSortedList(lijst);
	}
	//---------------------------------------------------------------------------------
	public String[] getBackdropTypeList()
	//---------------------------------------------------------------------------------
	{
			String lijst[] = new String[cmcProcEnums.BackdropType.values().length];
			for(int i=0;i<cmcProcEnums.BackdropType.values().length;i++)
			{
				lijst[i] = cmcProcEnums.BackdropType.values()[i].toString();
			}
			return getSortedList(lijst);
	}
	//---------------------------------------------------------------------------------
	public String[] getWiskerTypeList()
	//---------------------------------------------------------------------------------
	{
				String lijst[] = new String[cmcProcEnums.WiskerColor.values().length];
				for(int i=0;i<cmcProcEnums.WiskerColor.values().length;i++)
				{
					lijst[i] = cmcProcEnums.WiskerColor.values()[i].toString();
				}
				return getSortedList(lijst);
	}
	
	//---------------------------------------------------------------------------------
	public cmcProcEnums.ProximityTolerance getProximityTipe(String s)
	//---------------------------------------------------------------------------------
	{
		int idx = -1;
		for(int i=0;i<cmcProcEnums.ProximityTolerance.values().length;i++)
		{
			if( s.compareToIgnoreCase( cmcProcEnums.ProximityTolerance.values()[i].toString() ) == 0 ) {
				idx=i;
				break;
			}
		}
		if( idx < 0 ) return null;
		return cmcProcEnums.ProximityTolerance.values()[idx];
	}
	//---------------------------------------------------------------------------------
	public cmcProcEnums.BinarizeClassificationMethod getBinarizeClassificationTipe(String s)
	//---------------------------------------------------------------------------------
	{
		int idx = -1;
		for(int i=0;i<cmcProcEnums.BinarizeClassificationMethod.values().length;i++)
		{
			if( s.compareToIgnoreCase( cmcProcEnums.BinarizeClassificationMethod.values()[i].toString() ) == 0 ) {
				idx=i;
				break;
			}
		}
		if( idx < 0 ) return null;
		return cmcProcEnums.BinarizeClassificationMethod.values()[idx];
	}
	//---------------------------------------------------------------------------------
	public cmcProcEnums.ClusterClassificationMethod getClusterClassificationTipe(String s)
	//---------------------------------------------------------------------------------
	{
		int idx = -1;
		for(int i=0;i<cmcProcEnums.ClusterClassificationMethod.values().length;i++)
		{
			if( s.compareToIgnoreCase( cmcProcEnums.ClusterClassificationMethod.values()[i].toString() ) == 0 ) {
				idx=i;
				break;
			}
		}
		if( idx < 0 ) return null;
		return cmcProcEnums.ClusterClassificationMethod.values()[idx];
	}
	//---------------------------------------------------------------------------------
	public cmcProcEnums.ColourSchema getColourSchema(String s)
	//---------------------------------------------------------------------------------
	{
			int idx = -1;
			for(int i=0;i<cmcProcEnums.ColourSchema.values().length;i++)
			{
				if( s.compareToIgnoreCase( cmcProcEnums.ColourSchema.values()[i].toString() ) == 0 ) {
					idx=i;
					break;
				}
			}
			if( idx < 0 ) return null;
			return cmcProcEnums.ColourSchema.values()[idx];
	}
	
	//---------------------------------------------------------------------------------
	public cmcProcEnums.BackdropType getBackdropType(String s)
	//---------------------------------------------------------------------------------
	{
				int idx = -1;
				for(int i=0;i<cmcProcEnums.BackdropType.values().length;i++)
				{
					if( s.compareToIgnoreCase( cmcProcEnums.BackdropType.values()[i].toString() ) == 0 ) {
						idx=i;
						break;
					}
				}
				if( idx < 0 ) return null;
				return cmcProcEnums.BackdropType.values()[idx];
	}
	//---------------------------------------------------------------------------------
	public cmcProcEnums.WiskerColor getWiskerType(String s)
	//---------------------------------------------------------------------------------
	{
					int idx = -1;
					for(int i=0;i<cmcProcEnums.WiskerColor.values().length;i++)
					{
						if( s.compareToIgnoreCase( cmcProcEnums.WiskerColor.values()[i].toString() ) == 0 ) {
							idx=i;
							break;
						}
					}
					if( idx < 0 ) return null;
					return cmcProcEnums.WiskerColor.values()[idx];
	}
	//---------------------------------------------------------------------------------
	static public int getQuickColsIndex(cmcProcEnums.QUICKCOLS x)
	//---------------------------------------------------------------------------------
	{
		for(int i=0;i<cmcProcEnums.QUICKCOLS.values().length;i++)
		{
			if( x == cmcProcEnums.QUICKCOLS.values()[i] ) return i;
		}
		return -1;
	}
	//---------------------------------------------------------------------------------
	public String[] getQuickEditOptionsList()
	//---------------------------------------------------------------------------------
	{
			String lijst[] = new String[cmcProcEnums.QUICKEDITOPTIONS.values().length];
			for(int i=0;i<cmcProcEnums.QUICKEDITOPTIONS.values().length;i++)
			{
				lijst[i] = cmcProcEnums.QUICKEDITOPTIONS.values()[i].toString();
			}
			return getSortedList(lijst);
	}
	//---------------------------------------------------------------------------------
	public cmcProcEnums.QUICKEDITOPTIONS getQuickEditOption(String s)
	//---------------------------------------------------------------------------------
	{
		if( s == null ) return null;
		String sel = xMSet.xU.Remplaceer(s," ","_").trim().toUpperCase();
	    int idx = -1;
		for(int i=0;i<cmcProcEnums.QUICKEDITOPTIONS.values().length;i++)
		{
			if( sel.compareToIgnoreCase( cmcProcEnums.QUICKEDITOPTIONS.values()[i].toString() ) == 0 ) {
					idx=i;
					break;
			}
		}
		if( idx < 0 ) return null;
		return cmcProcEnums.QUICKEDITOPTIONS.values()[idx];
	}
	
	//---------------------------------------------------------------------------------
	public cmcProcEnums.ENCODING getEncoding(String s)
	//---------------------------------------------------------------------------------
	{
			if( s == null ) return null;
			if( s.compareToIgnoreCase("UTF8") == 0 ) s = "UTF_8";
			if( s.compareToIgnoreCase("UTF16") == 0 ) s = "UTF_16";
			if( s.compareToIgnoreCase("LATIN1") == 0 ) s = "ISO_8859_1";
			String sel = xMSet.xU.Remplaceer(s,"-","_").trim().toUpperCase();
		    int idx = -1;
			for(int i=0;i<cmcProcEnums.ENCODING.values().length;i++)
			{
				if( sel.compareToIgnoreCase( cmcProcEnums.ENCODING.values()[i].toString() ) == 0 ) {
						idx=i;
						break;
				}
			}
			if( idx < 0 ) return null;
			return cmcProcEnums.ENCODING.values()[idx];
	}
	//---------------------------------------------------------------------------------
	public String[] getEncodingList()
	//---------------------------------------------------------------------------------
	{
				String lijst[] = new String[cmcProcEnums.ENCODING.values().length];
				for(int i=0;i<cmcProcEnums.ENCODING.values().length;i++)
				{
					lijst[i] = cmcProcEnums.ENCODING.values()[i].toString();
				}
				return getSortedList(lijst);
	}

	//---------------------------------------------------------------------------------
	public cmcProcEnums.TextConfidence getTextConfidence(String s)
	//---------------------------------------------------------------------------------
	{
		try{
			if( s == null ) return null;
		    int idx = -1;
			for(int i=0;i<cmcProcEnums.TextConfidence.values().length;i++)
			{
				if( s.compareToIgnoreCase( cmcProcEnums.TextConfidence.values()[i].toString() ) == 0 ) {
						idx=i;
						break;
				}
			}
			if( idx < 0 ) return null;
			return cmcProcEnums.TextConfidence.values()[idx];
		}
		catch(Exception e ) {
			System.err.println("[s=" + s + "] " + e.getMessage());
			return null;
		}
	}

	//---------------------------------------------------------------------------------
	public String[] getOCRCurationList()
	//---------------------------------------------------------------------------------
	{
			String lijst[] = new String[cmcProcEnums.OCR_CURATION.values().length];
			for(int i=0;i<cmcProcEnums.OCR_CURATION.values().length;i++)
			{
				lijst[i] = cmcProcEnums.OCR_CURATION.values()[i].toString();
			}
			return getSortedList(lijst);
	}

	//---------------------------------------------------------------------------------
	public cmcProcEnums.OCR_CURATION getOCRCuration(String s)
	//---------------------------------------------------------------------------------
	{
			if( s == null ) return null;
			String sel = xMSet.xU.Remplaceer(s," ","_").trim().toUpperCase();
		    int idx = -1;
			for(int i=0;i<cmcProcEnums.OCR_CURATION.values().length;i++)
			{
				if( sel.compareToIgnoreCase( cmcProcEnums.OCR_CURATION.values()[i].toString() ) == 0 ) {
						idx=i;
						break;
				}
			}
			if( idx < 0 ) return null;
			return cmcProcEnums.OCR_CURATION.values()[idx];
	}
	
	//---------------------------------------------------------------------------------
	public String[] getCroppingTypeList()
	//---------------------------------------------------------------------------------
	{
				String lijst[] = new String[cmcProcEnums.CroppingType.values().length];
				for(int i=0;i<cmcProcEnums.CroppingType.values().length;i++)
				{
					lijst[i] = cmcProcEnums.CroppingType.values()[i].toString();
				}
				return getSortedList(lijst);
	}
	
	//---------------------------------------------------------------------------------
	public cmcProcEnums.CroppingType getCroppingType(String s)
	//---------------------------------------------------------------------------------
	{
			if( s == null ) return null;
			String sel = xMSet.xU.Remplaceer(s," ","_").trim().toUpperCase();
		    int idx = -1;
			for(int i=0;i<cmcProcEnums.CroppingType.values().length;i++)
			{
				if( sel.compareToIgnoreCase( cmcProcEnums.CroppingType.values()[i].toString() ) == 0 ) {
						idx=i;
						break;
				}
			}
			if( idx < 0 ) return null;
			return cmcProcEnums.CroppingType.values()[idx];
	}

	//---------------------------------------------------------------------------------
	public static int getTessOptionIndex(cmcProcEnums.TESS_OPTION_SELECTION x)
	//---------------------------------------------------------------------------------
	{
		for(int i=0;i<cmcProcEnums.TESS_OPTION_SELECTION.values().length;i++)
		{
			if( x == cmcProcEnums.TESS_OPTION_SELECTION.values()[i] ) return i;
		}
		return -1;
	}

	//---------------------------------------------------------------------------------
	public cmcProcEnums.TESS_OPTION_SELECTION getTessOptionAtIndex(int idx)
	//---------------------------------------------------------------------------------
	{
		if( (idx < 0) || (idx >= cmcProcEnums.TESS_OPTION_SELECTION.values().length) ) return null;
		return cmcProcEnums.TESS_OPTION_SELECTION.values()[idx];
	}

	//---------------------------------------------------------------------------------
	public cmcProcEnums.BROWSER getBrowser(String s)
	//---------------------------------------------------------------------------------
	{
			if( s == null ) return null;
			String sel = xMSet.xU.Remplaceer(s," ","_").trim().toUpperCase();
		    int idx = -1;
			for(int i=0;i<cmcProcEnums.BROWSER.values().length;i++)
			{
				if( sel.compareToIgnoreCase( cmcProcEnums.BROWSER.values()[i].toString() ) == 0 ) {
						idx=i;
						break;
				}
			}
			if( idx < 0 ) return null;
			return cmcProcEnums.BROWSER.values()[idx];
	}

	//---------------------------------------------------------------------------------
	public cmcProcEnums.REASSESMENT_COVERAGE getTensorFlowCoverage(String s)
	//---------------------------------------------------------------------------------
	{
			if( s == null ) return null;
			String sel = xMSet.xU.Remplaceer(s," ","_").trim().toUpperCase();
		    int idx = -1;
			for(int i=0;i<cmcProcEnums.REASSESMENT_COVERAGE.values().length;i++)
			{
				if( sel.compareToIgnoreCase( cmcProcEnums.REASSESMENT_COVERAGE.values()[i].toString() ) == 0 ) {
						idx=i;
						break;
				}
			}
			if( idx < 0 ) return null;
			return cmcProcEnums.REASSESMENT_COVERAGE.values()[idx];
	}
	//---------------------------------------------------------------------------------
	public String[] getTensorCoverageList()
	//---------------------------------------------------------------------------------
	{
			String lijst[] = new String[cmcProcEnums.REASSESMENT_COVERAGE.values().length];
			for(int i=0;i<cmcProcEnums.REASSESMENT_COVERAGE.values().length;i++)
			{
				lijst[i] = cmcProcEnums.REASSESMENT_COVERAGE.values()[i].toString();
			}
			return getSortedList(lijst);
	}
	/*
	//---------------------------------------------------------------------------------
	public cmcProcEnums.AI_TYPES getAIType(String s)
	//---------------------------------------------------------------------------------
	{
			if( s == null ) return null;
			String sel = xMSet.xU.Remplaceer(s," ","_").trim().toUpperCase();
		    int idx = -1;
			for(int i=0;i<cmcProcEnums.AI_TYPES.values().length;i++)
			{
				if( sel.compareToIgnoreCase( cmcProcEnums.AI_TYPES.values()[i].toString() ) == 0 ) {
					idx=i;
					break;
				}
			}
			if( idx < 0 ) return null;
			return cmcProcEnums.AI_TYPES.values()[idx];
	}
	//---------------------------------------------------------------------------------
	public String[] getAITypeList()
	//---------------------------------------------------------------------------------
	{
			String lijst[] = new String[cmcProcEnums.AI_TYPES.values().length];
			for(int i=0;i<cmcProcEnums.AI_TYPES.values().length;i++)
			{
				lijst[i] = cmcProcEnums.AI_TYPES.values()[i].toString();
			}
			return getSortedList(lijst);
	}
	*/
	//---------------------------------------------------------------------------------
	public String[] getBrowserList()
	//---------------------------------------------------------------------------------
	{
				String lijst[] = new String[cmcProcEnums.BROWSER.values().length];
				for(int i=0;i<cmcProcEnums.BROWSER.values().length;i++)
				{
					lijst[i] = cmcProcEnums.BROWSER.values()[i].toString();
				}
				return getSortedList(lijst);
	}

	//---------------------------------------------------------------------------------
	public cmcProcEnums.BULK_MONITOR getBulkMonitorAtIndex(int idx)
	//---------------------------------------------------------------------------------
	{
		if( (idx < 0) || (idx >= cmcProcEnums.BULK_MONITOR.values().length) ) return null;
		return cmcProcEnums.BULK_MONITOR.values()[idx];
	}
	
	//---------------------------------------------------------------------------------
	public String[] getMachineLearningTypeList()
	//---------------------------------------------------------------------------------
	{
		String lijst[] = new String[cmcProcEnums.MACHINE_LEARNING_TYPE.values().length];
		for(int i=0;i<cmcProcEnums.MACHINE_LEARNING_TYPE.values().length;i++)
		{
					lijst[i] = cmcProcEnums.MACHINE_LEARNING_TYPE.values()[i].toString();
		}
		return getSortedList(lijst);
	}
	//---------------------------------------------------------------------------------
	public cmcProcEnums.MACHINE_LEARNING_TYPE getMachineLearningType(String s)
	//---------------------------------------------------------------------------------
	{
		if( s == null ) return null;
		String sel = xMSet.xU.Remplaceer(s," ","_").trim().toUpperCase();
	    int idx = -1;
		for(int i=0;i<cmcProcEnums.MACHINE_LEARNING_TYPE.values().length;i++)
		{
			if( sel.compareToIgnoreCase( cmcProcEnums.MACHINE_LEARNING_TYPE.values()[i].toString() ) == 0 ) {
				idx=i;
				break;
			}
		}
		if( idx < 0 ) return null;
		return cmcProcEnums.MACHINE_LEARNING_TYPE.values()[idx];
	}
	//---------------------------------------------------------------------------------
	public String[] getScatterDotTypeList()
	//---------------------------------------------------------------------------------
	{
			String lijst[] = new String[cmcProcEnums.SCATTER_DOT_TYPE.values().length];
			for(int i=0;i<cmcProcEnums.SCATTER_DOT_TYPE.values().length;i++)
			{
			 lijst[i] = cmcProcEnums.SCATTER_DOT_TYPE.values()[i].toString();
			}
			return getSortedList(lijst);
	}
	//---------------------------------------------------------------------------------
	public cmcProcEnums.SCATTER_DOT_TYPE getScatterDotType(String s)
	//---------------------------------------------------------------------------------
	{
			if( s == null ) return null;
			String sel = xMSet.xU.Remplaceer(s," ","_").trim().toUpperCase();
		    int idx = -1;
			for(int i=0;i<cmcProcEnums.SCATTER_DOT_TYPE.values().length;i++)
			{
				if( sel.compareToIgnoreCase( cmcProcEnums.SCATTER_DOT_TYPE.values()[i].toString() ) == 0 ) {
					idx=i;
					break;
				}
			}
			if( idx < 0 ) return null;
			return cmcProcEnums.SCATTER_DOT_TYPE.values()[idx];
	}
	
	//---------------------------------------------------------------------------------
	public String[] getColorSentimentTypeList()
	//---------------------------------------------------------------------------------
	{
		String lijst[] = new String[cmcProcEnums.ColorSentiment.values().length];
		for(int i=0;i<cmcProcEnums.ColorSentiment.values().length;i++)
		{
		 lijst[i] = cmcProcEnums.ColorSentiment.values()[i].toString();
		}
		return getSortedList(lijst);
	}
	
	//---------------------------------------------------------------------------------
	public cmcProcEnums.ColorSentiment getColorSentimentType(String s)
	//---------------------------------------------------------------------------------
	{
		if( s == null ) return null;
		String sel = xMSet.xU.Remplaceer(s," ","_").trim().toUpperCase();
	    int idx = -1;
		for(int i=0;i<cmcProcEnums.ColorSentiment.values().length;i++)
		{
			if( sel.compareToIgnoreCase( cmcProcEnums.ColorSentiment.values()[i].toString() ) == 0 ) {
				idx=i;
				break;
			}
		}
		if( idx < 0 ) return null;
		return cmcProcEnums.ColorSentiment.values()[idx];
	}
}
