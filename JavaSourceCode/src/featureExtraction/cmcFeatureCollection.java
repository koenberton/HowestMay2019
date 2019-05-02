package featureExtraction;

import java.util.ArrayList;

public class cmcFeatureCollection {
	
	enum SCORE { TEXT , MIXED , GRAPH , UNKNOWN }
	enum ORIENTATION { ULTRAWIDE , WIDE , LANDSCAPE , SQUARE , PORTRAIT , NARROW , ULTRANARROW , UNKNOWN }
	
	String LongFileName;
	String ShortFileName;
	SCORE score = SCORE.UNKNOWN;
	ORIENTATION orientation = ORIENTATION.UNKNOWN;
	ArrayList<cmcHaralick> haraList;
	double density=0;
	//
	double relativeSurface=0;
	double widthHeightRatio=0;
	//
	double meanHue=0;
	double meanSaturation=0;
	double meanLightness=0;
	
	cmcFeatureCollection(String lfl , String sfl , SCORE sc)
	{
		LongFileName = lfl;
		ShortFileName = sfl;
		score = sc;
		haraList = null;
		density=0;
		meanHue=0;
		meanSaturation=0;
		meanLightness=0;
	}
	
	
}
