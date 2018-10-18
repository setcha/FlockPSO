import java.util.Comparator;
import java.util.*;
import java.lang.*;
import java.io.*;

// A class only used when sorting GPTrees. This returns 1 if b is bigger,
// -1 if a is bigger, and 0 if they are the same
public class GPTreeComparator implements Comparator<GPTree>{ 
	public int compare(GPTree a, GPTree b)
	    {
	    	if (a.getFitness() < b.getFitness()) return 1;
	    	if (a.getFitness() > b.getFitness()) return -1;
	        return 0;
	    }
	}

