package gov.epa.exp_data_gathering.parse;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.util.LinkedHashMap;
import java.util.Vector;

import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.usermodel.WorkbookFactory;

import gov.epa.api.AADashboard;
import gov.epa.api.ExperimentalConstants;
import gov.epa.ghs_data_gathering.GetData.RecordDashboard;

public class RecordPubChem {
	String iupacName;
	String smiles;
	String cas;
	String synonyms;
	Vector<String> physicalDescription;
	Vector<String> density;
	Vector<String> meltingPoint;
	Vector<String> boilingPoint;
	Vector<String> flashPoint;
	Vector<String> solubility;
	Vector<String> vaporPressure;
	Vector<String> henrysLawConstant;
	Vector<String> logP;
	Vector<String> pKa;
	
	static final String sourceName=ExperimentalConstants.strSourcePubChem;
	
	/**
	 * Extracts DTXSIDs from CompTox dashboard records and translates them to PubChem CIDs
	 * @param records	A vector of RecordDashboard objects
	 * @param start		The index in the vector to start converting
	 * @param end		The index in the vector to stop converting
	 * @return			A vector of PubChem CIDs as strings
	 */
	private static Vector<String> getCIDsFromDashboardRecords(Vector<RecordDashboard> records,String dictFilePath,int start,int end) {
		Vector<String> cids = new Vector<String>();
		LinkedHashMap<String,String> dict = new LinkedHashMap<String, String>();
		
		try {
			File file = new File(dictFilePath);
			BufferedReader br = new BufferedReader(new FileReader(file));
			String line="";
			while ((line=br.readLine())!=null) {
				String[] cells=line.split(",");
				dict.put(cells[0], cells[1]);
			}
			
		} catch (Exception ex) {
			ex.printStackTrace();
		}
		
		for (int i = start; i < end; i++) {
			String dtxsid = records.get(i).DTXSID;
			cids.add(dict.get(dtxsid));
		}
		
		return cids;
	}
	
	public static void main(String[] args) {
		Vector<RecordDashboard> records = Parse.getDashboardRecordsFromExcel(AADashboard.dataFolder+"/PFASSTRUCT.xls");
		Vector<String> cids = getCIDsFromDashboardRecords(records,AADashboard.dataFolder+"/CIDDICT.csv",1,1000);
	}
}
