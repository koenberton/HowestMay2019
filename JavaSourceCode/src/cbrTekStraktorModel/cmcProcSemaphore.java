package cbrTekStraktorModel;

public class cmcProcSemaphore {

	public enum TaskType {DO_NOTHING,SHOUT,
		                  DO_LOAD_IMAGE,DO_PREPROCESS,DO_GRAYSCALE,DO_BINARIZE,DO_CONNECTEDCOMPONENT,PROCESS_GRAYSCALE,
		                  PROCESS_BLACKWHITE,PROCESS_SAUVOLA,PROCESS_NIBLAK,PROCESS_INVERT,PROCESS_STATS,PROCESS_BLEACH,PROCESS_EQUAL,
		                  PROCESS_SOBELGRAYSCALE, PROCESS_SOBELCOLOR,
		                  PROCESS_GRADIENT_NARROW , PROCESS_GRADIENT_WIDE ,
		                  PROCESS_GRADIENT_MAGNITUDE , PROCESS_GRADIENT_THETA , 
		                  PROCESS_VERTICAL_GRADIENT , PROCESS_HORIZONTAL_GRADIENT ,
			              DO_DIALOG,OVERTHECLIFF,DO_EDITOR_IMAGE,DO_FAST_LOAD_IMAGE,DO_FLUSH_CHANGES,
		                  PROCESS_MAINFRAME, PROCESS_BLUEPRINT ,
		                  PROCESS_CONVOLUTION_GAUSSIAN, PROCESS_CONVOLUTION_GLOW , PROCESS_CONVOLUTION_SHARPEN , 
		                  PROCESS_CONVOLUTION_EDGE ,
		                  PREPARE_OCR , SHOW_OCR_FILE , RUN_TESSERACT , EDIT_OCR_FILE ,
		                  EXTRACT_ALL_TEXT , IMPORT_ALL_TEXT ,
		                  
		                  VR_MAKE_TRAINING_IMAGE_SET,
		                  VR_EXTRACT_FEATURES,
		                  VR_TRAIN_MODEL,
		                  VR_EXTRACT_AND_RUN_SINGLE_SET,
		                  TENSORFLOW_POSTPROC , 
		                  
		                  PROCESS_MAXLLOYD , PROCESS_CANNY , PROCESS_TEXTURE ,
		                  VR_SHOW_MODEL }
	
	TaskType sema = TaskType.DO_NOTHING;
	
	public cmcProcSemaphore()
	{
		
	}
	
	public void setSemaphore(TaskType it)
	{
		sema=it;
	}
	
	// get and toggle to nothing
	public TaskType getSemaphore()
	{
		if (sema == TaskType.DO_NOTHING)  return TaskType.DO_NOTHING;
        TaskType oldsema = sema;
        sema=TaskType.DO_NOTHING;
        return oldsema;
	}
}
