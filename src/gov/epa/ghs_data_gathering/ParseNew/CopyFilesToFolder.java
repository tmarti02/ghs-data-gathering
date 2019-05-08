package gov.epa.ghs_data_gathering.ParseNew;

import java.io.File;
import java.io.IOException;

import org.apache.commons.io.FileUtils;

public class CopyFilesToFolder {

	public static void main(String[] args) throws IOException {

		String sourceFolder = "L:\\Priv\\Cin\\NRMRL\\CompTox\\javax\\web-test\\AA Dashboard\\Data\\Chemidplus\\Oral LD50 webpages";
		File destinationFolder = new File(
				"L:\\Priv\\Cin\\NRMRL\\CompTox\\javax\\web-test\\AA Dashboard\\Data\\Chemidplus\\All Webpages");

		File fileFolder = new File(sourceFolder);
		File[] files = fileFolder.listFiles();

		for (int i = 0; i < files.length; i++) {
			File inputFile = files[i];

//			if (inputFile.exists())
//				continue;

			FileUtils.copyFileToDirectory(inputFile, destinationFolder);

		}

	}
}