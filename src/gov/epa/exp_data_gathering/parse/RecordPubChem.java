package gov.epa.exp_data_gathering.parse;

import java.io.BufferedReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
import java.util.Vector;

import gov.epa.api.AADashboard;
import gov.epa.ghs_data_gathering.GetData.RecordDashboard;

public class RecordPubChem {
	
	public static Vector<String> getCIDsFromDashboardRecords(Vector<RecordDashboard> records) {
		String baseURL="https://pubchem.ncbi.nlm.nih.gov/rest/pug/compound/smiles/";
		String urlTail="/cids/txt";
		Vector<String> cids = new Vector<String>();
		try {
			FileWriter csvWriter = new FileWriter(AADashboard.dataFolder+"/PFASCIDS.csv");
			for (int i = 1; i < records.size(); i++) {
				String smiles = records.get(i).SMILES;
				String url = baseURL+smiles+urlTail;
				try {
					URL rest = new URL(url);
					BufferedReader in = new BufferedReader(new InputStreamReader(rest.openStream()));
					String cid = in.readLine();
					if (cid != null) {
						cids.add(cid);
						csvWriter.append(records.get(i).CASRN+","+cid+"\n");
					}
					in.close();
					Thread.sleep(200);
				} catch (Exception e) {
					System.out.println("Can't access page for "+smiles);
				}
			}
			csvWriter.flush();
			csvWriter.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
		return cids;
	}
	
	public static void main(String[] args) {
		Vector<RecordDashboard> records = Parse.getDashboardRecordsFromExcel(AADashboard.dataFolder+"/PFASSTRUCT.xls");
		Vector<String> cids = getCIDsFromDashboardRecords(records);
	}
}
