package generalMachineLearning;

import cbrTekStraktorModel.cmcProcEnums;
import cbrTekStraktorModel.cmcProcSettings;
import generalMachineLearning.ARFF.ARFFWrapperDTO;
import generalMachineLearning.ARFF.cmcARFFRoutines;
import generalMachineLearning.Bayes.cmcModelMakerNaiveBayes;
import generalMachineLearning.DecisionTree.cmcModelmakerDecisionTree;
import generalMachineLearning.KNN.cmcKNearestNeighbours;
import logger.logLiason;

public class cmcMachineLearningModelController {
    
	cmcProcSettings xMSet=null;
	logLiason logger=null;
	cmcARFFRoutines arffrout = null;
	
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

    //------------------------------------------------------------
    public cmcMachineLearningModelController(cmcProcSettings is,logLiason ilog)
    //------------------------------------------------------------
    {
    	xMSet = is;
		logger=ilog;
    }
  
    //------------------------------------------------------------
    public boolean makeAndTrainMLModel(String OriginalFileName , String ModelFileName)
    //------------------------------------------------------------
    {
      	arffrout = new cmcARFFRoutines( xMSet , logger );
      	arffrout.purgeFolders();
      	ARFFWrapperDTO dto = null;
        // 	
        cmcModelMaker maker = null;
        boolean ib = false;
        switch( xMSet.getMachineLearningType() )
		{
		case DECISION_TREE : ;
		case BAYES : { 
			dto = arffrout.getCategoriesAndOtherStuff(OriginalFileName, cmcMachineLearningConstants.NBR_BINS , true );
	    	if( dto == null ) return false;
	    	if( dto.isValid() == false ) return false;
	        if( xMSet.getMachineLearningType() == cmcProcEnums.MACHINE_LEARNING_TYPE.BAYES )
			maker = new cmcModelMakerNaiveBayes( xMSet , logger );
	        else
	        if( xMSet.getMachineLearningType() == cmcProcEnums.MACHINE_LEARNING_TYPE.DECISION_TREE )
			maker = new cmcModelmakerDecisionTree( xMSet , logger );
	        else {
	        	do_error( "Unsupported type");
	        	return false;
	        }
			//if( maker == null ) return false;
		    ib = maker.trainModel( dto.getCategories() , dto.getNbrOfBins() , dto.getClassNameList(), ModelFileName );
		    if( ib == false ) { maker = null; return false; }
		    ib = maker.testModel( ModelFileName , dto.getFileNameTestSet(), dto.getXa() );
	        break;
		}
	    case K_NEAREST_NEIGHBOURS : {
             maker = new cmcKNearestNeighbours( xMSet , logger );   	
             ib = maker.trainModel( OriginalFileName , ModelFileName );  // will also run test model
             if( ib == false ) { maker = null; return false; }
 		     break;
		}
		default : { do_error("Unsupported type " + xMSet.getMachineLearningType() ); break; }
		}
      
    	arffrout.purgeFolders();
    	arffrout=null;  
	    maker=null;
	    dto=null;
        return ib;
    }
    
   
}
