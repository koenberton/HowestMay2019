package featureExtraction;

import cbrTekStraktorModel.cmcProcEnums;
import cbrTekStraktorModel.cmcProcSettings;
import dao.cmcARFFDAO;
import generalMachineLearning.ARFF.cmcARFF;
import generalMachineLearning.Bayes.cmcBayesModel;
import generalMachineLearning.Bayes.cmcModelMakerNaiveBayes;
import generalMachineLearning.DecisionTree.cmcModelmakerDecisionTree;
import generalMachineLearning.Evaluation.cmcModelEvaluation;
import generalMachineLearning.KNN.cmcKNearestNeighbours;
import logger.logLiason;


public class cmcVRRunMachineLearning {
	
	cmcProcSettings xMSet=null;
	logLiason logger=null;
	private String ErrorMsg   = null;
	
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
    	ErrorMsg = sIn + "\n";
    	do_log(0,sIn);
    }
    
    //------------------------------------------------------------
    public String getErrorMessage()
    //------------------------------------------------------------
    {
      return ErrorMsg;	
    }
    
    // ---------------------------------------------------------------------------------
 	public cmcVRRunMachineLearning(cmcProcSettings iM, logLiason ilog )
    // ---------------------------------------------------------------------------------
 	{
 		xMSet = iM;
		logger = ilog;
 	}

    // ---------------------------------------------------------------------------------
 	public boolean performVisualRecognition()
    // ---------------------------------------------------------------------------------
 	{
 		// pr req tests
 		
 		
 		//
 	 	int maxiter = xMSet.getmoma().getScanListSize();
 	 	if( maxiter < 1 ) {
 	 		  do_error("There is nothing to process");
 	 		  return true;
 	 	}
 	 	//
 		//cmcMonitorController moni = new cmcMonitorController(xMSet,logger);
 		//moni.startMonitor(xMSet.getCurrentArchiveFileName());
 		
 	 	  // create an ARFF file for this set
 	 	  String SingleARFFFileName = xMSet.getBayesResultDir() +  xMSet.xU.ctSlash + "SingleComicPageARFF.txt";
 	 	  if( xMSet.xU.IsBestand( SingleARFFFileName ) ) {
 	 		  if( xMSet.xU.VerwijderBestand(SingleARFFFileName) == false ) return false;
 	 	  }
 	 	  cmcVRExtractMLAndHOGFeatureSet exa = new cmcVRExtractMLAndHOGFeatureSet( xMSet , logger );
 	 	  boolean ib = exa. makeAnARFFFileFromSingleFolder( xMSet.getCorpusDir() + xMSet.xU.ctSlash + "Images" ,  SingleARFFFileName );
 	 	  if( ib == false ) return false;
 	 	  
 	 	  //
 	 	  // Read back the ARFF file
 	 	  cmcARFFDAO adao = new cmcARFFDAO( xMSet , logger );
 	 	  if( adao.performFirstPass(SingleARFFFileName) == false ) return false;
 	 	  cmcARFF xa = adao.performSecondPass(SingleARFFFileName);
 	 	  if( xa == null ) return false;
 	 	  //
 	 	  do_log( 1 , "Adjusting via [" +  xMSet.getMachineLearningType() + "]" );
 	 	  PredictDetail[] reslst = null;
 	 	  cmcModelMakerNaiveBayes nb = null;
 	 	  cmcModelmakerDecisionTree dt=null;
 	 	  cmcKNearestNeighbours knn = null;
 	 	  switch( xMSet.getMachineLearningType() )
 	 	  {
 	 	  case BAYES : {
 	 		  if( xMSet.xU.IsBestand( xMSet.getBayesModelName()) == false ) {
 	 			do_error("There is no Bayes Model. Please create one [" + xMSet.getBayesModelName() + "]");
 	 			return false;
 	 		  }
 	 		  // load the Bayes Model
 	 	 	  nb = new cmcModelMakerNaiveBayes(xMSet , logger );
 	 	 	  cmcBayesModel[] modellist = nb.readBayesModels( xMSet.getBayesModelName() );
 	 	 	  if( modellist == null ) return false;
 	 	 	  
 	 	 	  // There is now a model and an ARFF
 	          cmcModelEvaluation confusion = nb.makeConfusionMatrix(  xa  , modellist );
 	          if( confusion == null ) return false;
 	          
 	          // results
 	          reslst = nb.getPredictionResults();
 	          break;	  
 	 	  }
 	 	  case DECISION_TREE : {
 	 		if( xMSet.xU.IsBestand( xMSet.getDecisionTreeModelName()) == false ) {
 	 			do_error("There is no DecisionTree Model. Please create one [" + xMSet.getDecisionTreeModelName() + "]");
 	 			return false;
 	 		  }
 	 		 dt = new cmcModelmakerDecisionTree( xMSet , logger );
 	 		 if( dt.readDecisionTreeModel( xMSet.getDecisionTreeModelName() ) == false) return false;
 	 	    
 	 		 // There is now a model and an ARFF
	         cmcModelEvaluation confusion = dt.makeConfusionMatrix(  xa  );
	         if( confusion == null ) return false;
	         
	         // results
	         reslst = dt.getPredictionResults();
 	 		 break;
 	 	  }
 	 	  case K_NEAREST_NEIGHBOURS : {
 	 		  if( xMSet.xU.IsBestand( xMSet.getKNNModelName()) == false ) {
 	 			do_error("There is no KNN Model file. Please create one [" + xMSet.getKNNModelName() + "]");
 	 			return false;
 	 		  }
 	 		  knn = new cmcKNearestNeighbours( xMSet , logger );
 	 		  ib = knn.executeModel(  xMSet.getKNNModelName() , SingleARFFFileName , null );
 	 		  if( ib == false ) return false;
 	 		  // results
 	          reslst = knn.getPredictionResults();
 	 		  break;
 	 	  }
 	 	  default : { do_error("Unsupported type [" + xMSet.getMachineLearningType() + "]"); return false; }
 	 	  }
 	 	  
 	 	  if( reslst == null ) {
        	  do_error("Got a NULL prediction list");
        	  return false;
          }
          if( reslst.length < 1 ) {
         	  do_error("Got an empty prediction list");
        	  return false;
          }
          // verify
          for(int i=0;i<reslst.length;i++)
          {
        	  if( reslst[i].getUID() < 0L )  {
        		  do_error("There is a UID -1 on the result list");
        		  return false;
        	  }
        	  //do_log( 1 , reslst[i].getFileName() + " " + reslst[i].getUID() + " " + reslst[i].getAnticipated() + " " + reslst[i].getPredicted() );
          }

 	 	  long prevtime = System.currentTimeMillis();
 	 	  int nbrChanges=0;
          for(int iter=0;iter<maxiter;iter++)
		  {
			  String ImageFileName = xMSet.getmoma().popScanListItem();  // also sets the starttime
			  cmcVRParagraph obj = (cmcVRParagraph)xMSet.getmoma().getObjectFromScanList(iter);
			  //do_log( 1 , obj.getLongImageFileName() +" " + obj.getUID() + " " + obj.getTipe() );
			  // look for UID
			  int idx =-1;
			  for(int i=0;i<reslst.length;i++)
	          {
				 if( reslst[i].getUID() == obj.getUID() ) { idx=i; break; }
	          }
			  if( idx < 0 ) {
				  do_error("Missing UID [" + obj.getUID() + "]");
				  return false;
			  }
			  String anticipated =reslst[idx].getAnticipated();
			  String predicted =reslst[idx].getPredicted();
			  if( (anticipated == null) || (predicted ==null) ) {
				  do_error("There is a NULL anticpated or predicted");
				  return false;
			  }
			  if( anticipated.trim().compareToIgnoreCase( predicted.trim() ) == 0 ) continue;  // no change
			  do_log( 1 , "UPDATE " + iter + " " + obj.getUID() + " " + anticipated + " -> " + predicted );
			  
			  //
			  cmcProcEnums.PageObjectType newtipe = cmcProcEnums.PageObjectType.UNKNOWN;
			  if( predicted.compareToIgnoreCase("TEXT") == 0 ) newtipe =  cmcProcEnums.PageObjectType.TEXTPARAGRAPH;
			  if( predicted.compareToIgnoreCase("GRAPH") == 0 ) newtipe =  cmcProcEnums.PageObjectType.PARAGRAPH;
			  if( newtipe == cmcProcEnums.PageObjectType.UNKNOWN ) {
				  do_error("Unsupported tipe predicted " + predicted);
				  return false; 
			  }
			  obj.setNewTipe(newtipe);
			  nbrChanges++;
			  prevtime = System.currentTimeMillis();
		  }
          // the changes will be merged with XML via the cmcProcessor module
          
          /*
 	 	  long prevtime = System.currentTimeMillis();
		  for(int iter=0;iter<maxiter;iter++)
		  {
			  String ImageFileName = xMSet.getmoma().popScanListItem();  // also sets the starttime
			  if( performTensorOnParagraph( moni , ImageFileName ) == false ) {
				  ok=false;
				  break;
			  }
			  // Ease on NIO
			  //long lo = 500L - (System.currentTimeMillis() - prevtime);
			  //if( lo > 0) sleep( lo+100L );
			  prevtime = System.currentTimeMillis();
		  }
	  	  */
 	
          //
	  	  nb=null;
	  	  dt=null;
	  	  adao=null;
		  exa = null;
 		  doHousekeeping();
 		  return true;
 	}
 	
 	// ---------------------------------------------------------------------------------
 	private boolean doHousekeeping()
    // ---------------------------------------------------------------------------------
 	{
 		boolean ok=true;
 		int maxiter = xMSet.getmoma().getScanListSize();
		for(int iter=0;iter<maxiter;iter++)
		{
		  cmcVRParagraph obj = (cmcVRParagraph)xMSet.getmoma().getObjectFromScanList(iter);
		  if( obj != null ) {
			 String LongImageFileName = obj.getLongImageFileName();
			 if( xMSet.xU.IsBestand(LongImageFileName) == false ) continue;
			 if( xMSet.xU.VerwijderBestand(LongImageFileName) == false )  {
			   do_error("Could not remove [" + LongImageFileName + "]");
			   ok=false;
			 }
		  }
		}
 		return ok;
 	}
 	
}
