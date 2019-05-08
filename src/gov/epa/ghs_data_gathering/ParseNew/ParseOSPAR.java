package gov.epa.ghs_data_gathering.ParseNew; //.getdata

import java.io.File;
import java.io.FileInputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.text.DecimalFormat;
import java.util.Vector;

import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.usermodel.DataFormatter;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import gov.epa.ghs_data_gathering.Parse.Parse;
import gov.epa.ghs_data_gathering.API.AADashboard;
import gov.epa.ghs_data_gathering.API.Chemical;
import gov.epa.ghs_data_gathering.API.ScoreRecord;



public class ParseOSPAR extends Parse {
	
	public static String sourceName=ScoreRecord.sourceOSPAR;
	
	public static String mainFolder = AADashboard.dataFolder+File.separator+sourceName;
	String fileNameJSON_Records = "records.json";
	
	
	public static String jsonFolder = mainFolder+"/json files";

	class data_field {
		String Field_Number;//e.g. 3.1
		String Parameter_Name;//name of field
		String Value;//numerical value
		String Source_Reference;//e.g. QSAR-DK: EPIWIN 3.02
		String Remarks;//e.g. not readily biodegradable (<20%) 
	}
	
	static class OSPARRecords {
		
		String Name;//0
		String CasNo;//1.1
		String EINECS;//1.2
		String Synonym;//1.3
		String Group_Function;//1.4
		String Initial_Selection;//1.5
		String Prioritized_For_Action;//1.6
		
		
		Vector<data_field> Molecular_weight = new Vector<data_field>();//2.1
		Vector<data_field> Water_Solubility = new Vector<data_field>();//2.2
		Vector<data_field> Vapor_Pressure = new Vector<data_field>();//2.3
		Vector<data_field> Abiotic_OH_Oxidation_t1_2_d = new Vector<data_field>();//3.1
		Vector<data_field> Photolysis_t1_2_d = new Vector<data_field>();//3.2
		Vector<data_field> Ready_Biodegradability = new Vector<data_field>();//3.3
		Vector<data_field> Halflife = new Vector<data_field>();//3.4
		Vector<data_field> Inherent_Biodegradability = new Vector<data_field>();//3.5
		Vector<data_field> Biodeg_QSAR = new Vector<data_field>();//3.6
		Vector<data_field> logKow = new Vector<data_field>();//4.1
		Vector<data_field> Bcf = new Vector<data_field>();//4.2
		Vector<data_field> Acute_toxicity_algae = new Vector<data_field>();//5.1
		Vector<data_field> Acute_toxicity_daphnia = new Vector<data_field>();//5.2
		Vector<data_field> Acute_toxicity_fish = new Vector<data_field>();//5.3
		Vector<data_field> Chronic_toxicity_daphnia = new Vector<data_field>();//5.4
		Vector<data_field> Chronic_toxicity_fish = new Vector<data_field>();//5.5
		Vector<data_field> Aquatox_QSAR = new Vector<data_field>();//5.6
		Vector<data_field> Aquatic_toxicity_Other = new Vector<data_field>();//5.7
		Vector<data_field> Human_toxic_properties = new Vector<data_field>();
		Vector<data_field> Acute_toxicity = new Vector<data_field>();//6.1
		Vector<data_field> Carcinogenicity = new Vector<data_field>();//6.2
		Vector<data_field> Chronic_toxicity = new Vector<data_field>();//6.3
		Vector<data_field> Mutagenicity = new Vector<data_field>();//6.4
		Vector<data_field> Reprotoxicity = new Vector<data_field>();//6.5
		Vector<data_field> Production_Volume = new Vector<data_field>();//7.1
		Vector<data_field> Use_Industry_Category = new Vector<data_field>();//7.2
		Vector<data_field> Use_in_articles = new Vector<data_field>();//7.3
		Vector<data_field> Environm_Occur_Measured = new Vector<data_field>();//7.4
		Vector<data_field> Environm_Occur_Modelled = new Vector<data_field>();//7.5
		Vector<data_field> Dir_67_548_EEC_Classification = new Vector<data_field>();//8.1
		Vector<data_field> Reg_793_93_EEC_Existing_substances = new Vector<data_field>();//8.2
		Vector<data_field> Dir_2000_60_EEC_WFD = new Vector<data_field>();//8.3
		Vector<data_field> Dir_76_769_EEC_M_U = new Vector<data_field>();//8.4
		Vector<data_field> Dir_76_464_EEC_water = new Vector<data_field>();//8.5
		Vector<data_field> Dir_91_414_EEC_ppp = new Vector<data_field>();//8.6
		Vector<data_field> Dir_98_8_EEC_biocid = new Vector<data_field>();//8.7
		Vector<data_field> ADDITIONAL_INFORMATION = new Vector<data_field>();
		Vector<data_field> Hazard_assessment_OECD = new Vector<data_field>();//9.1
		Vector<data_field> Other_risk_assessments = new Vector<data_field>();//9.2

		
		public String toString() {
			return Name+"\t"+CasNo;
		}
		
	}
		

	/**
	 * Adds dashes to CAS number
	 * 
	 * @param CAS
	 * @return
	 */
	String convertCAS(String CAS) {
		
		if (CAS.equals("427452 (uncertainty about the CAS No)")) CAS="427452";
		
		DecimalFormat df=new DecimalFormat("0");
		
		double dCAS=Double.parseDouble(CAS);
		
//		System.out.println(CAS);
		CAS=df.format(dCAS);
//		System.out.println(CAS);

		String var3=CAS.substring(CAS.length()-1,CAS.length());
		CAS=CAS.substring(0,CAS.length()-1);
		String var2=CAS.substring(CAS.length()-2,CAS.length());
		CAS=CAS.substring(0,CAS.length()-2);
		String var1=CAS;
		String CASnew=var1+"-"+var2+"-"+var3;
//		System.out.println(CASnew);
		return CASnew;
	}
	
	private Chemical createChemical(OSPARRecords or) {
		Chemical chemical = new Chemical();
		
		chemical.CAS=or.CasNo;
		chemical.name=or.Name;
		
		
		return chemical;
	}
		
		
	private OSPARRecords parseExcelFile(String excelFilePath) {
		// Use loop in main to go through all files

		try {

			GsonBuilder builder = new GsonBuilder();
			builder.setPrettyPrinting();
			Gson gson = builder.create();
			
			
			OSPARRecords or = new OSPARRecords();

			File file = new File(excelFilePath);
			FileInputStream inputStream = new FileInputStream(new File(excelFilePath));

			Workbook workbook = new HSSFWorkbook(inputStream);
			Sheet firstSheet = workbook.getSheetAt(0);

			int row = 0;

			DataFormatter formatter = new DataFormatter();
			
			
			while (true) {
				Row nextRow = firstSheet.getRow(row);

				if (nextRow == null) {
					break;
				}

				String value=nextRow.getCell(0)+"";
				
				if (value.equals("")) {
					
					String v3=nextRow.getCell(3)+"";
					
					if (!v3.equals("Source/Reference")) {
						System.out.println(excelFilePath+"\t"+or.CasNo+"\t"+v3+"\t"+value);						
					}
					
					row++;
					continue;
				}
				
				if (value.equals("null")) break;
				
				float fvalue=Float.parseFloat(value);
				DecimalFormat decform=new DecimalFormat("0.0");
				String svalue=decform.format(fvalue);
				
//				System.out.println(or.CasNo+"\t"+svalue);
				
				
				data_field df = createDataField(nextRow);
				
				if (df.Value.equals("") && df.Source_Reference.equals("")) {
					row++;
					continue;
				}
				

				if (svalue.equals("0.0")) 
					or.Name=df.Value;
				if (svalue.equals("1.1"))
					or.CasNo=convertCAS(df.Value);
				if (svalue.equals("1.2"))
					or.EINECS=df.Value;
				if (svalue.equals("1.3"))
					or.Synonym=df.Value;
				if (svalue.equals("1.4"))
					or.Group_Function=df.Value;
				if (svalue.equals("1.5"))
					or.Initial_Selection=df.Value;
				if (svalue.equals("1.6"))
					or.Prioritized_For_Action=df.Value;

				if (svalue.equals("2.1"))
					or.Molecular_weight.add(df);
				if (svalue.equals("2.2"))
					or.Water_Solubility.add(df);
				if (svalue.equals("2.3"))
					or.Vapor_Pressure.add(df);

				if (svalue.equals("3.1"))
					or.Abiotic_OH_Oxidation_t1_2_d.add(df);
				if (svalue.equals("3.2"))
					or.Photolysis_t1_2_d.add(df);
				if (svalue.equals("3.3"))
					or.Ready_Biodegradability.add(df);
				if (svalue.equals("3.4"))
					or.Halflife.add(df);
				if (svalue.equals("3.5"))
					or.Inherent_Biodegradability.add(df);
				if (svalue.equals("3.6"))
					or.Biodeg_QSAR.add(df);
				
				if (svalue.equals("4.1"))
					or.logKow.add(df);
				if (svalue.equals("4.2"))
					or.Bcf.add(df);

				if (svalue.equals("5.1"))
					or.Acute_toxicity_algae.add(df);
				if (svalue.equals("5.2"))
					or.Acute_toxicity_daphnia.add(df);
				if (svalue.equals("5.3"))
					or.Acute_toxicity_fish.add(df);
				if (svalue.equals("5.4"))
					or.Chronic_toxicity_daphnia.add(df);
				if (svalue.equals("5.5"))
					or.Chronic_toxicity_fish.add(df);
				if (svalue.equals("5.6"))
					or.Aquatox_QSAR.add(df);
				if (svalue.equals("5.7"))
					or.Aquatic_toxicity_Other.add(df);


				if (svalue.equals("6.1"))
					or.Acute_toxicity.add(df);
				if (svalue.equals("6.2"))
					or.Carcinogenicity.add(df);
				if (svalue.equals("6.3"))
					or.Chronic_toxicity.add(df);
				if (svalue.equals("6.4"))
					or.Mutagenicity.add(df);
				if (svalue.equals("6.5"))
					or.Reprotoxicity.add(df);


				if (svalue.equals("7.1"))
					or.Production_Volume.add(df);
				if (svalue.equals("7.2"))
					or.Use_Industry_Category.add(df);
				if (svalue.equals("7.3"))
					or.Use_in_articles.add(df);
				if (svalue.equals("7.4"))
					or.Environm_Occur_Measured.add(df);
				if (svalue.equals("7.5"))
					or.Environm_Occur_Modelled.add(df);

				if (svalue.equals("8.1"))
					or.Dir_67_548_EEC_Classification.add(df);
				if (svalue.equals("8.2"))
					or.Reg_793_93_EEC_Existing_substances.add(df);
				if (svalue.equals("8.3"))
					or.Dir_2000_60_EEC_WFD.add(df);
				if (svalue.equals("8.4"))
					or.Dir_76_769_EEC_M_U.add(df);
				if (svalue.equals("8.5"))
					or.Dir_76_464_EEC_water.add(df);
				if (svalue.equals("8.6"))
					or.Dir_91_414_EEC_ppp.add(df);
				if (svalue.equals("8.7"))
					or.Dir_98_8_EEC_biocid.add(df);

				if (svalue.equals("9.1"))
					or.Hazard_assessment_OECD.add(df);
				if (svalue.equals("9.2"))
					or.Other_risk_assessments.add(df);

				row++;

			}

			inputStream.close();
			return or;
			
		} catch (Exception ex) {
			ex.printStackTrace();
			return null;
		}
	}


	private data_field createDataField(Row row) {
		data_field df=new data_field();
		df.Field_Number = row.getCell(0)+"";// adding +"" converts to string and avoids null field problem
	    df.Parameter_Name = row.getCell(1)+"";
		df.Value = row.getCell(2)+"";
		df.Source_Reference = row.getCell(3)+"";
		df.Remarks = row.getCell(4)+"";
		return df;
	}
		
	
	
	private void createOSPARRecords(String folder) {
		
		try {

			GsonBuilder builder = new GsonBuilder();
			builder.setPrettyPrinting();
			Gson gson = builder.create();
			
			Vector<OSPARRecords>records=new Vector<OSPARRecords>();
			
			for (int i = 0; i < 136; i++) {
//			for (int i = 0; i < 1; i++) {
				OSPARRecords or=parseExcelFile(folder + "\\OSPAR" + i + ".xls");
				
//				if (or.CasNo.equals("84742"))
//				if (or.Initial_Selection.toLowerCase().indexOf("endoc")>-1)
//					System.out.println(or.CasNo+"\t"+or.Initial_Selection);
				
				records.add(or);
			}
			
			
			FileWriter fw=new FileWriter(folder+"/records.json");
			fw.write(gson.toJson(records));
			fw.close();
			
			
			
		} catch (Exception ex) {
			ex.printStackTrace();
		}
	}
	
	
	private void createRecords(String folder, String inputExcelFileName, String outputJSON_Filename) {
		
		try {

			GsonBuilder builder = new GsonBuilder();
			builder.setPrettyPrinting();
			Gson gson = builder.create();
			
			OSPARRecords or=parseExcelFile(folder + "/"+inputExcelFileName);
			
			
			FileWriter fw=new FileWriter(folder+"/"+outputJSON_Filename);
			
			String json=gson.toJson(or);
			
			System.out.println(json);
			
			fw.write(json);
			fw.close();
			
		} catch (Exception ex) {
			ex.printStackTrace();
		}
	}
	
	public static void main(String[] args) {
		// TODO Auto-generated method stub
		ParseOSPAR po = new ParseOSPAR();
		
//		String folder = "L:\\Priv\\Cin\\NRMRL\\CompTox\\javax\\web-test\\AA Dashboard\\Data\\OSPAR";
//		String excelFileName = "OSPAR0.xls";
//		String outputfilename="OSPAR0.json";
//		po.createOSPARRecord(excelFileName, folder,outputfilename);
//		OSPARRecord or=po.parseExcelFile(sourceExcelFile);
//		System.out.println(or.Aquatox_QSAR);
		
//		po.createOSPARRecords(folder);
		
		po.createFiles();
	}


	
	

}
