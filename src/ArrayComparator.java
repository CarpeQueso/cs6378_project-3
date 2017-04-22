import java.util.Comparator;

public class ArrayComparator implements Comparator<int[]>{
	//First compare the clockValue,then compare the senderId
	@Override
	public int compare(int[] a, int[] b) {
	    int r = Integer.compare(a[0], b[0]);
	    return r == 0 ? Integer.compare(a[1], b[1]) : r;
	}	
}
