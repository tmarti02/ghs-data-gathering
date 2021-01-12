package gov.epa.QSAR.utilities;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.channels.FileChannel;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

public class FileUtilities {
	
	
	public static String getFileAsString (String filepath) {
		try {
			return Files.readString(Path.of(filepath));
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return "error reading"+filepath;
	}
	
	/**
	 * Read a text file into String list 
	 * 
	 * 
	 * @param filepath
	 * @return
	 */
	public static List<String>readFile(String filepath) {		
		try {
			Path filePath = new File(filepath).toPath();
			Charset charset = Charset.defaultCharset();        
			List<String> stringList = Files.readAllLines(filePath, charset);
			return stringList;

		} catch (Exception ex) {
			ex.printStackTrace();
			return null;
		}
	}
	
	  public static int CopyFile(File SrcFile, File DestFile) {

		    try {

		      FileChannel in = new FileInputStream(SrcFile).getChannel();

		      FileChannel out = new FileOutputStream(DestFile).getChannel();

		      in.transferTo(0, (int) in.size(), out);
		      in.close();
		      out.close();

		      return 0;

		    } catch (Exception e) {
		    	e.printStackTrace();
		    	return -1;
		    }

		  }
	  
	/**
	 * Fix file names to use correct latest filename format
	 * 
	 * @param folderPath
	 */
	void renameFiles(String folderPath) {
		File Folder=new File(folderPath);
//		LLNA_training_set-2d-rnd3.csv
//		LLNA training set rnd3.csv
		File [] files=Folder.listFiles();
		
		for (File file:files) {
			String fileNameNew=file.getName().replace(" rnd", "-2d-rnd").replace(" ", "_");
			file.renameTo(new File(folderPath+File.separator+fileNameNew));
			System.out.println(fileNameNew);
		}
		
		
	}
}
