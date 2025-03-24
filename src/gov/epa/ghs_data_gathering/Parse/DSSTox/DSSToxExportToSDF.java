package gov.epa.ghs_data_gathering.Parse.DSSTox;

import java.io.FileWriter;
import java.util.HashMap;
import java.util.List;
import java.util.Vector;

import org.openscience.cdk.AtomContainer;
import org.openscience.cdk.DefaultChemObjectBuilder;
import org.openscience.cdk.io.MDLV2000Writer;
import org.openscience.cdk.io.SDFWriter;
import org.openscience.cdk.io.setting.BooleanIOSetting;
import org.openscience.cdk.smiles.SmilesParser;

import com.google.gson.JsonObject;

import gov.epa.exp_data_gathering.parse.ExcelSourceReader;

public class DSSToxExportToSDF {
	
	
	static void toSDF(String folderPath,String fileName,Vector<JsonObject> records) {
		

		try {
			SmilesParser   sp  = new SmilesParser(DefaultChemObjectBuilder.getInstance());

		
			FileWriter fw=new FileWriter(folderPath+fileName);
			
			SDFWriter sdf = new SDFWriter(fw);
			
			
			
			MDLV2000Writer mw=new MDLV2000Writer(fw);
			mw.setWriteAromaticBondTypes(false);
						
//			BooleanIOSetting setting=new BooleanIOSetting("writeProperties",BooleanIOSetting.Importance.LOW,
//                    "Should molecule properties be written as non-structural data", "true");
//			mw.addSetting(setting);//doesnt work!
			
		
			for (JsonObject jo:records) {
				System.out.println(jo.toString());
				
				String SMILES=jo.get("SMILES").getAsString();
				
				AtomContainer m = (AtomContainer)sp.parseSmiles(SMILES);
				m.setProperty("CAS", jo.get("CASRN").getAsString());
				m.setProperty("DTXSID", jo.get("DTXSID").getAsString());
				m.setProperty("Name", jo.get("PREFERRED_NAME").getAsString());
				sdf.write(m);
				
//				fw.write("\r\n$$$$\r\n");
				
			}
			fw.flush();
			
			fw.close();
			
		} catch (Exception ex) {
			ex.printStackTrace();
		}
		
	}
	
	
	public static void main(String[] args) {

		String folder="C:\\Users\\TMARTI02\\OneDrive - Environmental Protection Agency (EPA)\\0 java\\TEST_2020_03_18\\MNDOH\\";
		String fileName="list_chemicals-2021-07-16-13-19-26.xls";
		String filepath=folder+fileName;
		String fileNameSDF=fileName.replace(".xls", ".sdf");
		
		ExcelSourceReader esr=new ExcelSourceReader();		
		esr.getSheet(filepath, 0);
						
		List<String> fieldNames=esr.getHeaders();		
		HashMap<Integer,String> hm = ExcelSourceReader.generateDefaultMap(fieldNames, 0);
		Vector<JsonObject> records = esr.parseRecordsFromExcel(hm, 1);
		
		
		toSDF(folder, fileNameSDF, records);
		
	}
}
