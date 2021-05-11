package gov.epa.exp_data_gathering.parse.Burkhard;

import static org.apache.poi.ss.usermodel.Cell.CELL_TYPE_STRING;

import java.util.HashMap;
import java.util.List;
import java.util.Set;
import java.util.Vector;

import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Row.MissingCellPolicy;

import com.google.gson.JsonObject;

import gov.epa.api.ExperimentalConstants;
import gov.epa.exp_data_gathering.parse.ExcelSourceReader;
import org.apache.commons.text.StringEscapeUtils;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.usermodel.WorkbookFactory;
import org.apache.poi.ss.usermodel.Row.MissingCellPolicy;





public class RecordBurkhard {
	public String Chemical;
	public String CAS;
	public String Abbreviation;
	public String Log_BCF_Steady_State_mean;
	public String Log_BCF_Steady_State_sd;
	public String Log_BCF_Steady_State_min;
	public String Log_BCF_Steady_State_max;
	public String Log_BCF_Steady_State_type;
	public String Log_BCF_Steady_State_units;
	public String Log_BCF_Kinetic_mean;
	public String Log_BCF_Kinetic_sd;
	public String Log_BCF_Kinetic_95_CI;
	public String Log_BCF_Kinetic_type;
	public String Log_BCF_Kinetic_units;
	public String Log_BAF_mean;
	public String Log_BAF_sd;
	public String Log_BAF_95_CI;
	public String Log_BAF_min;
	public String Log_BAF_max;
	public String Log_BAF_type;
	public String Log_BAF_units;
	public String Measured_Trophic_Level;
	public String Measured_Trophic_Level_sd;
	public String field23;
	public String Estimated_Trophic_Level;
	public String Common_Name;
	public String Species_Latin_Name;
	public String Species_Weight_g;
	public String field28;
	public String Tissue;
	public String Location;
	public String Reference;
	public String OECD_305;
	public String ku;
	public String ku_sd;
	public String field35;
	public String days_of_uptake;
	public String ke;
	public String ke_sd;
	public String field39;
	public String days_of_elimination;
	public String half_life_days;
	public String half_life_sd_days;
	public String SS;
	public String Kinetic;
	public String Modeled;
	public String Exposure_Concentrations;
	public String Study_Quality_BCF;
	public String Comments_BCF;
	public String k_dietary;
	public String k_dietary_sd;
	public String field51;
	public String Assimulation_Efficiency;
	public String Assimulation_Efficiency_SD;
	public String field54;
	public String BAF;
	public String of_water_samples;
	public String start_date1;
	public String end_date1;
	public String field59;
	public String of_biota_samples;
	public String start_date2;
	public String end_date2;
	public String Average_weight_g;
	public String Average_Length_cm;
	public String Sex_F_M;
	public String Average_Age;
	public String Age_sd;
	public String General_experimental_design;
	public String Water_Biota_spatial_coordination;
	public String Water_Biota_temporal_coordination;
	public String Number_Biota_Samples;
	public String Number_of_Water;
	public String Study_Quality_BAF;
	public String Comments_BAFs;
	public String field75;
	public String Mixture_Exposure;
	public String Concentration_in_Biota;
	public String Concentration_in_Biota_min;
	public String Concentration_in_Biota_max;
	public String sd1;
	public String units1;
	public String MDL1;
	public String Comments_Biota_Samples;
	public String Concentration_in_environmental_media;
	public String sd2;
	public String units2;
	public String MDL2;
	public String Comments_Environmental_Media;
	public String field89;
	public String Organic_Carbon;
	public String sd3;
	public String units3;
	public String Comments_OC;
	public String Marine_Brackish_Freshwater;
	public String Comments_Marine_Brackish_Freshwater;
	public String field96;
	public String Taxonomy_Name_Level;
	public String kingdom;
	public String phylum;
	public String class0;
	public String order;
	public String family;
	public String genus;
	public String species;
	public String ITIS_TaxID;
	public String NCBI_TaxID;
	public String GBIF_TaxID;
	public String Common_Name_NCBI_ITIS;
	public String Taxomony_Name_Source;
	public static final String[] fieldNames = {"Chemical","CAS","Abbreviation","Log_BCF_Steady_State_mean","Log_BCF_Steady_State_sd","Log_BCF_Steady_State_min","Log_BCF_Steady_State_max","Log_BCF_Steady_State_type","Log_BCF_Steady_State_units","Log_BCF_Kinetic_mean","Log_BCF_Kinetic_sd","Log_BCF_Kinetic_95_CI","Log_BCF_Kinetic_type","Log_BCF_Kinetic_units","Log_BAF_mean","Log_BAF_sd","Log_BAF_95_CI","Log_BAF_min","Log_BAF_max","Log_BAF_type","Log_BAF_units","Measured_Trophic_Level","Measured_Trophic_Level_sd","field23","Estimated_Trophic_Level","Common_Name","Species_Latin_Name","Species_Weight_g","field28","Tissue","Location","Reference","OECD_305","ku","ku_sd","field35","days_of_uptake","ke","ke_sd","field39","days_of_elimination","half_life_days","half_life_sd_days","SS","Kinetic","Modeled","Exposure_Concentrations","Study_Quality_BCF","Comments_BCF","k_dietary","k_dietary_sd","field51","Assimulation_Efficiency","Assimulation_Efficiency_SD","field54","BAF","of_water_samples","start_date1","end_date1","field59","of_biota_samples","start_date2","end_date2","Average_weight_g","Average_Length_cm","Sex_F_M","Average_Age","Age_sd","General_experimental_design","Water_Biota_spatial_coordination","Water_Biota_temporal_coordination","Number_Biota_Samples","Number_of_Water","Study_Quality_BAF","Comments_BAFs","field75","Mixture_Exposure","Concentration_in_Biota","Concentration_in_Biota_min","Concentration_in_Biota_max","sd1","units1","MDL1","Comments_Biota_Samples","Concentration_in_environmental_media","sd2","units2","MDL2","Comments_Environmental_Media","field89","Organic_Carbon","sd3","units3","Comments_OC","Marine_Brackish_Freshwater","Comments_Marine_Brackish_Freshwater","field96","Taxonomy_Name_Level","kingdom","phylum","class0","order","family","genus","species","ITIS_TaxID","NCBI_TaxID","GBIF_TaxID","Common_Name_NCBI_ITIS","Taxomony_Name_Source"};

	public static final String lastUpdated = "05/05/2021";
	public static final String sourceName = "Burkhard"; // TODO Consider creating ExperimentalConstants.strSourceBurkhard instead.

	private static final String fileName = "Copy of Copy of etc5010-sup-0002-data_bcf_baf_pfas_GFBS_partially_cleaned_CR_more_edits2.xlsx";

	public static Vector<JsonObject> parseBurkhardRecordsFromExcel() {
		ExcelSourceReader esr = new ExcelSourceReaderBurkhard(fileName, sourceName);
		Vector<JsonObject> records = esr.parseRecordsFromExcel(0); // TODO Chemical name index guessed from header. Is this accurate?
		return records;
	}
	
	
	

	// TODO Auto-generated constructor stub
}



class ExcelSourceReaderBurkhard extends ExcelSourceReader {

	public ExcelSourceReaderBurkhard(String fileName, String sourceName) {
		super(fileName, sourceName);
		// TODO Auto-generated constructor stub
	}
	
	
	/**
	 * Writes records from a spreadsheet to JSON original records format assuming the template created by generateRecordClassTemplate()
	 * @param chemicalNameIndex		Column index containing chemical names (for special escape character treatment)
	 */
	@Override
	public Vector<JsonObject> parseRecordsFromExcel(int chemicalNameIndex) {
		String[] fieldNames = getHeaders();
		HashMap<Integer,String> hm = generateDefaultMap(fieldNames, 0);
		return parseRecordsFromExcelBurkhard(hm, chemicalNameIndex);
	}
	
	/**
	 * Writes records from a spreadsheet to JSON original records format consistent with field names of an existing Record[SourceName] class
	 * @param hmFieldNames	Matches column numbers to output fields of a Record[SourceName] class
	 * @param chemicalNameIndex		Column index containing chemical names (for special escape character treatment)
	 */
	public Vector<JsonObject> parseRecordsFromExcelBurkhard(HashMap<Integer,String> hmFieldNames, int chemicalNameIndex) {
		Vector<JsonObject> records = new Vector<JsonObject>();
		try {
			int numRows = sheet.getLastRowNum();
			for (int i = 1; i < numRows; i++) {
				Row row = sheet.getRow(i);
				if (row==null) { continue; }
				JsonObject jo = new JsonObject();
				boolean hasAnyFields = false;
				for (int k:hmFieldNames.keySet()) {
					Cell cell = row.getCell(k);
					if (cell==null) { continue; }
					cell.setCellType(CELL_TYPE_STRING);
					String content = "";
					if (k==chemicalNameIndex) {
						content = StringEscapeUtils.escapeHtml4(row.getCell(k,MissingCellPolicy.CREATE_NULL_AS_BLANK).getStringCellValue());
					} else {
						content = row.getCell(k,MissingCellPolicy.CREATE_NULL_AS_BLANK).getStringCellValue();
					}
					if (content!=null && !content.isBlank()) { hasAnyFields = true; }
					jo.addProperty(hmFieldNames.get(k), content);
					jo.addProperty("ID", "Original_Record" + String.valueOf(i)); // this is the only thing that changes, a way to map experimental records and or
				}
				if (hasAnyFields) { records.add(jo); }
			}
		} catch (Exception ex) {
			ex.printStackTrace();
		}
		return records;
	}

	

	
	
}
