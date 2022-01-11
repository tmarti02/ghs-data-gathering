package gov.epa.exp_data_gathering.parse.Arnot;

import java.util.Vector;

import com.google.gson.JsonObject;

import gov.epa.exp_data_gathering.parse.ExcelSourceReader;

public class RecordArnot {
	public String endpoint_1_BAF_2_BCF_3_BCFfd_4_BAF;
	public String CAS;
	public String Chemical_Name;
	public String Estimated_Log_Kow_EPISUITE;
	public String Measured_Log_Kow_EPI_DB;
	public String measuredLogKowEPIalternative;
	public String kowRef;
	public String logkow;
	public String logkowmin4max10duplicate;
	public String Organism_classification;
	public String scientific_name;
	public String commond_name;
	public String wwlogBAFExptl;
	public String wwlogbcfExptl;
	public String waterConcMeanugPerLiter;
	public String waterconcMeasuredorNot;
	public String Radiolabel;
	public String ExposureDurationDays;
	public String ExposureType;
	public String TemperatureMean;
	public String ExposureRouteWorW_D;
	public String ExposureMedia;
	public String pHMean;
	public String TOCwaterMeanmgL;
	public String WetWeightMeang;
	public String LipidContentMeanMeasuredPct;
	public String TissueAnalyzed;
	public String CalculationMethod;
	public String Comments;
	public String SourceAuthor;
	public String Year;
	public String ReferenceTitle;
	public String ReferenceSource;
	public static final String[] fieldNames = {"endpoint_1_BAF_2_BCF_3_BCFfd_4_BAF","CAS","Chemical_Name","Estimated_Log_Kow_EPISUITE","Measured_Log_Kow_EPI_DB","measuredLogKowEPIalternative","kowRef","logkow","logkowmin4max10duplicate","Organism_classification","scientific_name","commond_name","wwlogBAFExptl","wwlogbcfExptl","waterConcMeanugPerLiter","waterconcMeasuredorNot","Radiolabel","ExposureDurationDays","ExposureType","TemperatureMean","ExposureRouteWorW_D","ExposureMedia","pHMean","TOCwaterMeanmgL","WetWeightMeang","LipidContentMeanMeasuredPct","TissueAnalyzed","CalculationMethod","Comments","SourceAuthor","Year","ReferenceTitle","ReferenceSource"};

	public static final String lastUpdated = "01/06/2022";
	public static final String sourceName = "Arnot"; // TODO Consider creating ExperimentalConstants.strSourceArnot instead.

	private static final String fileName = "Arnot2006_edited.xlsx";

	public static Vector<JsonObject> parseArnotRecordsFromExcel() {
		ExcelSourceReader esr = new ExcelSourceReader(fileName, sourceName);
		Vector<JsonObject> records = esr.parseRecordsFromExcel(2); 
		return records;
	}
}