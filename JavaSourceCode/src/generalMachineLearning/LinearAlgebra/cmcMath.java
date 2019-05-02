package generalMachineLearning.LinearAlgebra;

import java.util.Random;

public class cmcMath {

	private void do_error(String sin)
	{
		System.err.println(sin);
	}
	
	public cmcMath()
	{
	}
	
	//------------------------------------------------------------
	public boolean isEven(int i)
	//------------------------------------------------------------
	{
		double rest = i % 2;
		return (rest==0);
	}
	//------------------------------------------------------------
	public int getRandomNumberExcluding(int range , int excli )
	//------------------------------------------------------------
	{
		for(int i=0;i<10000;i++)
		{
			int p = (int)( Math.random() * (double)range);
		    if( p != excli ) return p;			
		}
		return -1;
	}
	//------------------------------------------------------------
	public int[] shuffleList( int len )
	//------------------------------------------------------------
	{
		if( len <= 0 ) { do_error("Empty list"); return null; }
		
		// make a list of LEN random and unique numbers
		int[] rlist = new int[ len ];
		for(int i=0 ; i< rlist.length ; i++ ) rlist[i] = -1;
		Random rnd = new Random();
		int UPPER = len * 1234;
		
		for(int col=0;col<len;col++)
		{
			int insert = -1;
		    for( int r=0 ; r<10000 ; r++)
		    {
		        int result = rnd.nextInt( UPPER );
		        boolean found = false;
	            for(int j=0;j<col;j++) 
	            {
	            	if( rlist[j] == result ) { found = true; break; }
	            }
	            if( found == false ) { insert = result; break; } 
		    }
		    if( insert < 0 ) { do_error("Could not create random list - increase attempts"); return null; }
		    rlist[col] = insert;
		}
	    // list of random number - replace each number by its rang in the range 0 .. len	
		int[] sorted = new int[ len ];
		for(int i=0;i<sorted.length;i++) sorted[i] = rlist[i];
		for(int i=0;i<len;i++)
		{
			boolean swap=false;
			for(int j=0;j<len-1;j++)
			{
				if( rlist[j] > rlist[j+1]) {
					swap=true;
					int z = rlist[j];
					rlist[j] = rlist[j+1];
					rlist[j+1]=z;
				}
			}
			if( swap == false ) break;
		} 
		// replace the value in the rlist by its rang
		for(int i=0;i<len;i++)
		{
			int idx = -1;
			for(int j=0;j<len;j++) {
				if( rlist[i] == sorted[j] ) { idx=j; break; }
			}
			if( idx < 0 ) { do_error("Could not determine rang " + rlist[i] ); return null; }
			rlist[i]=idx;
		}
		// check
		for(int i=0;i<len;i++)
		{
			boolean found = false;
			for(int j=0;j<len;j++)
			{
				if( rlist[j] == i ) { found = true; break; }
			}
			if( found == false ) { do_error("Could not find value in list " + i ); return null; }
		}
		return rlist;
	}
}
