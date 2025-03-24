package gov.epa.exp_data_gathering.parse.ThreeM;

import java.net.URL;

/**
* @author TMARTI02
*/
public class TestPoiVersion {
	public static String getClassLoaderPath(Class<?> clazz) {
	    ClassLoader classloader = clazz.getClassLoader();
	    String resource = clazz.getName().replaceAll("\\.", "/") + ".class";
	    URL res = classloader.getResource(resource);
	    String path = res.getPath();
	    return path;
	  }
	public static void main(String[] args) {
		// TODO Auto-generated method stub
		Class<?>[] classes = {
		        org.apache.poi.ooxml.POIXMLDocument.class,
		        org.apache.poi.xssf.usermodel.XSSFCell.class,
		        org.apache.poi.ss.usermodel.CellType.class};
		    
		for (Class<?> clazz : classes) {
		     String path = getClassLoaderPath(clazz);
		     System.out.println(String.format("%s came from %s", clazz.getSimpleName(), path));
		}
	}

}
