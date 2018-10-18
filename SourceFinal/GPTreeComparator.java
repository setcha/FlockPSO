import java.util.Comparator;
import java.util.*;
import java.lang.*;
import java.io.*;

//A class only used when sorting GPTrees. This 
public class GPTreeComparator implements Comparator<GPTree>{ 
	public int compare(GPTree a, GPTree b)
	    {
	    	if (a.getFitness() < b.getFitness()) return 1;
	    	if (a.getFitness() > b.getFitness()) return -1;
	        return 0;
	    }
	}
