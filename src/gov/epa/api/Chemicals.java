package gov.epa.api;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.util.ArrayList;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;

/**
 * Class to store chemicals
 * 
 * @author Todd Martin
 *
 */
public class Chemicals extends ArrayList<Chemical> {

	public JsonElement toJsonElement() {
		String strJSON=this.toJSON();
		Gson gson = new Gson();
		JsonElement json = gson.fromJson(strJSON, JsonElement.class);
		
		
		return json;
	}
	
	public Chemical getChemical(String CAS) {
		
		for (Chemical chemical:this) {
			if (chemical.CAS.equals(CAS)) return chemical;
		}
		return null;
	}
	
	/**
	 * Looks for duplicate CAS numbers and merges changes from later versions into earlier ones
	 */
	public void mergeRevisions() {
		try {

			ArrayList<String> CASList=new ArrayList<>();

			//Get list of unique cas numbers that dont have an underscore:
			for (Chemical chemical:this) {
				if (!CASList.contains(chemical.CAS) && !chemical.CAS.contains("_"))  {
					CASList.add(chemical.CAS);
				}
			}


			for (String CAS : CASList) {
				//				if (!CAS.equals("100-44-7")) continue;
				Chemicals chemicals=new Chemicals();

				//Create array of chemicals that have the cas number (including ones with underscore):
				for (int i=1;i<=5;i++) {
					String casSeek="";

					if (i==1 ) casSeek=CAS;
					else casSeek=CAS+"_"+i;

					for (Chemical chemical:this) {
						if (chemical.CAS.equals(casSeek)) chemicals.add(chemical);
					}
				}

//				if (chemicals.isEmpty()) System.out.println(CAS);
				
				
				if (chemicals.size()==1) continue;
				
//				System.out.println(CAS+"\t"+chemicals.size());
				

				Chemical chemical0=chemicals.get(0);
				//				System.out.println(gson.toJson(chemical0));
				for (int i=1;i<chemicals.size();i++) {
					Chemical chemicali=chemicals.get(i);
					
//					if (chemicali.CAS.contains("107-02-8")) {
//						System.out.println(gson.toJson(chemicali));
//					}
					
					Chemical.merge(chemical0, chemicali);//merge changes from chemicali into chemical0
				}

				chemicals.remove(0);

				//Remove the chemicals with underscore from overall list of chemicals:
				for (int i=0;i<chemicals.size();i++) {					
					Chemical chemicali=chemicals.get(i);

					for (int j=0;j<this.size();j++) {
						Chemical chemicalj=this.get(j);

						if (chemicali.CAS.equals(chemicalj.CAS)) {
							this.remove(j);
							break;
						}
					}
				}


				//				System.out.println(gson.toJson(chemical0));
				//				System.out.println("");

				//				System.out.println(CAS);

			}//end loop over CAS numbers

		} catch (Exception ex) {
			ex.printStackTrace();
		}
	}
	
	public void writeToFile(String filePath) {

		try {

//			removeEmptyFields();//save space
			
//			this.CAS = CAS;

			File file = new File(filePath);
			file.getParentFile().mkdirs();

			GsonBuilder builder = new GsonBuilder();
			builder.setPrettyPrinting();
			Gson gson = builder.create();

			FileWriter fw = new FileWriter(file);
			fw.write(gson.toJson(this));
			fw.flush();
			fw.close();

		} catch (Exception ex) {
			ex.printStackTrace();
		}

	}
	
	public void toFlatFile(String filepath,String del) {
		
		try {
			
						
			FileWriter fw=new FileWriter(filepath);
			
			fw.write(ScoreRecord.getHeader(del)+"\r\n");
			
			ArrayList<String>uniqueCAS=new ArrayList<>();
			
			
			for (Chemical chemical:this) {
				
				ArrayList<String>lines=chemical.toStringArray(del);
				
				if (!uniqueCAS.contains(chemical.CAS)) uniqueCAS.add(chemical.CAS);
				
				
				for (String line:lines) {
					line=line.replace("–", "-").replace("’", "'");//TODO use StringEscapeUtils?
					fw.write(line+"\r\n");
				}
				
//				fw.write(chemical.to);
			}
			fw.flush();
			fw.close();
			
//			for (String CAS:uniqueCAS) {
//				System.out.println(CAS);
//			}
			
		} catch (Exception ex) {
			ex.printStackTrace();
		}
		
	}
	
	public String toJSON() {
		GsonBuilder builder = new GsonBuilder();
		builder.setPrettyPrinting();// makes it multiline and readable
		Gson gson = builder.create();
		

		return gson.toJson(this);//all in one line!
	}
	
	public void toJSON_File(String filepath) {

		try {
			String result=this.toJSON();
			
			FileWriter fw=new FileWriter(filepath);
			fw.write(result);
			fw.flush();
			fw.close();
			
		} catch (Exception ex) {
			ex.printStackTrace();
		}
	}

	public static Chemicals loadFromJSON(String jsonFilePath) {

		try {
			Gson gson = new Gson();

			File file = new File(jsonFilePath);

			if (!file.exists())
				return null;

			Chemicals chemicals = gson.fromJson(new FileReader(jsonFilePath), Chemicals.class);

			
			//FieldNamingPolicy
			// System.out.println(chemicals.size());

			// test it to see if it outputs back out correctly:
			// System.out.println(c.toJSON());

			return chemicals;

		} catch (Exception ex) {
			ex.printStackTrace();
			return null;
		}
	}

	public static void main(String[] args) {
		Chemicals chemicals = loadFromJSON("todd\\AA dashboard\\Records from NITE.json");
//		System.out.println(chemicals.toJSON());
//		chemicals.toJSONElement();
	}

	

}
