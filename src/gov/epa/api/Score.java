package gov.epa.api;

import java.util.ArrayList;

/**
 * Class to store the score information and literature records used to assign the final score
 * 
 * @author Todd Martin
 *
 */
public class Score {
	
	public String hazard_name;//which hazard i.e. Acute_Mammalian_Toxicity
	public String final_score;//VH, H, M, L		
	public String final_score_source;//VH, H, M, L
	public ArrayList<ScoreRecord>records=new ArrayList<ScoreRecord>();//records from different sources, used in determining final score

//	//Convert to JSON object
//	/**
//	 * @deprecated - not needed
//	 * 
//	 * @return
//	 */
//	public JsonObject toJSON() {
//		JsonObject jo=new JsonObject();
//		
//		jo.addProperty("hazard_name", hazard_name);
//		jo.addProperty("final_score", final_score);
//		
//		JsonArray jaRecords=new JsonArray();
//		
//		if (records.size() > 0) {
//			for (int i = 0; i < records.size(); i++) {
//				jaRecords.add(records.get(i).toJSON());
//			}
//			jo.add("records", jaRecords);
//		}
//		
//		return jo;
//	}
	
	
}

