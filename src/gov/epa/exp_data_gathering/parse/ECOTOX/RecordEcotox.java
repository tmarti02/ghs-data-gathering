package gov.epa.exp_data_gathering.parse.ECOTOX;

import java.sql.ResultSet;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

import gov.epa.database.SQLite_GetRecords;
import gov.epa.database.SQLite_Utilities;

/**
* @author TMARTI02
*/
public class RecordEcotox {

	
	void getToxRecords() {
		Gson gson = new GsonBuilder().setPrettyPrinting().disableHtmlEscaping().create();
		
		String sql="select *\n"+ 
		"from tests t\n"+
		"join results r on t.test_id=r.test_id\n"+
		"join chemicals c on c.cas_number=t.test_cas\n"+
		"join references_ r2 on r2.reference_number=t.reference_number\n"+
		"where t.species_number=1 and ((r.obs_duration_mean=96 and r.obs_duration_unit='h')  or (r.obs_duration_mean=4 and r.obs_duration_unit='d'));";
		
		try {
			String databasePath="data\\experimental\\ECOTOX\\ecotox_ascii_06_15_2023.db";
			
			Statement stat = SQLite_Utilities.getStatement(databasePath);
			ResultSet rs = stat.executeQuery(sql);
			
			JsonArray ja=new JsonArray();
			
			List<String>choices=new ArrayList<>();
			
			
			
			while (rs.next()) {
//				System.out.println(rs.getString(1));
				
				JsonObject jo=new JsonObject();
				
				for(int i=1;i<rs.getMetaData().getColumnCount();i++) {
					
					String columnLabel=rs.getMetaData().getColumnLabel(i);
					String columnValue=rs.getString(i);
					
					if (rs.getString(i)==null || rs.getString(i).isBlank()) continue;
					
					jo.addProperty(columnLabel,columnValue );
					
//					System.out.println(rs.getMetaData().getColumnLabel(i));
				}
				
				if(jo.get("media_type")!=null) {
					String media_type=jo.get("media_type").getAsString();
					if(!media_type.contains("FW")) continue;
				}
				
				if(jo.get("test_location")!=null) {
					String test_location=jo.get("test_location").getAsString();
					if(!test_location.contains("LAB")) continue;
				}
				
				
				String endpoint=null,effect=null,measurement=null;
				
				if(jo.get("endpoint")!=null) {
					endpoint=jo.get("endpoint").getAsString();		
					if(!endpoint.contains("LC50")) continue;
				}
				
				if(jo.get("effect")!=null) {
					effect=jo.get("effect").getAsString();
				}
				
				if(jo.get("measurement")!=null) {
					measurement=jo.get("measurement").getAsString();
					if(!measurement.contains("MORT") &&  !measurement.contains("SURV")) continue;
				}
				
				
				if(!choices.contains(endpoint+"\t"+effect+"\t"+measurement)) choices.add(endpoint+"\t"+effect+"\t"+measurement);
				
//				System.out.println(endpoint+"\t"+effect+"\t"+measurement);
				
				
//				r.endpoint like 'LC50%' and r.effect like '%MOR%' and r.measurement
				
				
//				System.out.println(gson.toJson(jo));
				
				
				ja.add(jo);
				
			}
			
			for (String choice:choices) {
				System.out.println(choice);
			}
			
			System.out.println(ja.size());
			
			System.out.println(gson.toJson(ja));
			
		} catch (Exception ex) {
			ex.printStackTrace();
			
		}
		

	}
	
	
	public static void main(String[] args) {
		RecordEcotox r=new RecordEcotox();
		r.getToxRecords();

	}

}
