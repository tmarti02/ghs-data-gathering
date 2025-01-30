package gov.epa.QSAR.utilities;

import java.io.FileWriter;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

public class JsonUtilities {
	
	public static Gson gsonPretty = new GsonBuilder().setPrettyPrinting().disableHtmlEscaping().serializeSpecialFloatingPointValues().create();

	
	public static void savePrettyJson(Object obj, String filepath)  {
		try {

			FileWriter fw=new FileWriter(filepath);			
			fw.write(gsonPretty.toJson(obj));
			fw.flush();
			fw.close();

		} catch (Exception ex) {
			ex.printStackTrace();
		}
	}

	
}
