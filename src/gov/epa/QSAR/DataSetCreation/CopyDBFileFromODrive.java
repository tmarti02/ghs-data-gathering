package gov.epa.QSAR.DataSetCreation;

import java.io.*;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

public class CopyDBFileFromODrive {

	public static void main(String[] args) {
		// TODO Auto-generated method stub

		String filePathFolder1="O:\\Public\\Todd Martin\\PFAS Data Gathering\\";
		String filePathFolder2="C:\\Users\\TMARTI02\\OneDrive - Environmental Protection Agency (EPA)\\0 java\\ghs-data-gathering\\data\\experimental\\";		
				
		extract("ExperimentalRecords",filePathFolder1, filePathFolder2);
//		extract("ToxicityRecords",filePathFolder1, filePathFolder2);
		
	}
	
	public static void extract(String str,String folderPathSrc,String filePathDest) {
		
		
		OutputStream out;
		try {
			out = new FileOutputStream(filePathDest+str+".db");
			FileInputStream fileInputStream = new FileInputStream(folderPathSrc+str+".zip");
			BufferedInputStream bufferedInputStream = new BufferedInputStream(fileInputStream );
			ZipInputStream zin = new ZipInputStream(bufferedInputStream);
			ZipEntry ze = null;
			while ((ze = zin.getNextEntry()) != null) {
				if (ze.getName().equals(str+".db")) {
					byte[] buffer = new byte[9000];
					int len;
					while ((len = zin.read(buffer)) != -1) {
						out.write(buffer, 0, len);
					}
					out.close();
					break;
				}
			}
			zin.close();
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}

}
