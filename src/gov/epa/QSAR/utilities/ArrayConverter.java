package gov.epa.QSAR.utilities;

import java.util.ArrayList;
import java.util.List;
import java.util.ListIterator;
import java.util.Vector;

public class ArrayConverter {
	
	public String [] convertVectorToStringArray(Vector<String>vec) {
		String [] str=new String[vec.size()];
		for (int i=0;i<vec.size();i++) {
			str[i]=vec.get(i);
		}
		return str;
	}
	
	public String [] convertStringToStringArray(String str) {
		if (str==null || str.equals("")) return null;
		
//		java.util.List<String> list=LineSplitterParse(str, "\t");
		java.util.List<String> list=LineSplitter.ParseWithTokenizer(str, "\t");
		
		String  [] array=new String [list.size()];
		
		ListIterator <String>itr = list.listIterator();
	    int counter=0;
		while(itr.hasNext())
	    	array[counter++]=itr.next();

		return array;
	}
	
	public int [] convertStringToIntArray(String str,String del) {
		
		if (str==null || str.equals("")) return null;
		
//		java.util.List<String> list=LineSplitterParse(str, del);
		java.util.List<String> list=LineSplitter.ParseWithTokenizer(str, del);

//		for (int i=0;i<list.size();i++) {
//			System.out.println(i+"\t"+list.get(i));
//		}
		
		int  [] array=new int [list.size()];
		
		ListIterator <String>itr = list.listIterator();
	    int counter=0;
		while(itr.hasNext())
	    	array[counter++]=Integer.parseInt(itr.next());

		return array;
	}
	public float [] convertStringToFloatArray(String str,String del) {
		
		if (str==null || str.equals("")) return null;
		
//		java.util.List<String> list=LineSplitterParse(str, del);
		java.util.List<String> list=LineSplitter.ParseWithTokenizer(str, del);

//		for (int i=0;i<list.size();i++) {
//			System.out.println(i+"\t"+list.get(i));
//		}
		
		float  [] array=new float [list.size()];
		
		ListIterator <String>itr = list.listIterator();
	    int counter=0;
		while(itr.hasNext())
	    	array[counter++]=Float.parseFloat(itr.next());

		return array;
	}
	
	public String convertArrayToTabDelimitedString(int []array) {
		if (array==null) return "";
		String str = "";
		for (int i = 0; i < array.length; i++) {// loop over descriptor names
			str += array[i];
			if (i < array.length-1)
				str += "\t";
		}
		return str;
	}
	
	public String convertArrayToTabDelimitedString(double []array) {
		if (array==null) return "";
		String str = "";
		for (int i = 0; i < array.length; i++) {// loop over descriptor names
			str += array[i];
			if (i < array.length-1)
				str += "\t";
		}
		return str;
	}

	public String convertArrayToTabDelimitedString(String []array) {
		if (array==null) return "";
		String str = "";
		for (int i = 0; i < array.length; i++) {// loop over descriptor names
			str += array[i];
			if (i < array.length-1)
				str += "\t";
		}
		return str;
	}
	
	public String convertIntArrayToTabDelimitedString(int []array) {
		if (array==null) return "";
		String str = "";
		for (int i = 0; i < array.length; i++) {// loop over descriptor names
			str += array[i];
			if (i < array.length-1)
				str += "\t";
		}
		return str;
	}
	
	public static String [] convertStringArrayListToStringArray(ArrayList<String>al) {
		
		String [] strArray=new String [al.size()];
		
		for (int i=0;i<al.size();i++) {
			strArray[i]=al.get(i);
		}
		return strArray;
		
	}
	
	public static double [] convertDoubleArrayListToDoubleArray(ArrayList<Double>al) {
		
		double [] strArray=new double [al.size()];
		
		for (int i=0;i<al.size();i++) {
			strArray[i]=al.get(i);
		}
		return strArray;
		
	}
	
	public static double [] convertStringListToDoubleArray(List<String>al) {
		
		double [] dblArray=new double [al.size()];
		
		for (int i=0;i<al.size();i++) {
			if (al.get(i).isEmpty()) dblArray[i]=-9999;
			else dblArray[i]=Double.parseDouble(al.get(i));
		}
		return dblArray;
		
	}
	
	public String convertArrayToTabDelimitedString(boolean []array) {
		if (array==null) return "";
		
		String str = "";
		for (int i = 0; i < array.length; i++) {// loop over descriptor names
			if (array[i]) {
				str += 1;
			} else {
				str+=0;
			}
			if (i < array.length-1)
				str += "\t";
		}
		return str;
	}
	
	public double [] convertStringToDoubleArray(String str,String del) {
		
		if (str==null || str.equals("")) return null;
		
//		java.util.List<String> list=LineSplitterParse(str, del);
		java.util.List<String> list=LineSplitter.ParseWithTokenizer(str, del);

//		for (int i=0;i<list.size();i++) {
//			System.out.println(i+"\t"+list.get(i));
//		}
		
		double  [] array=new double [list.size()];
		
		ListIterator <String>itr = list.listIterator();
	    int counter=0;
		while(itr.hasNext())
	    	array[counter++]=Double.parseDouble(itr.next());

		return array;
	}

}
