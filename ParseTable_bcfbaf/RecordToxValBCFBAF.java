package gov.epa.ghs_data_gathering.Parse.ToxVal.ParseTable_bcfbaf;

import java.lang.reflect.Field;



public class RecordToxValBCFBAF {

//	public String casrn;
//	public String name;
	public String chemical_id;
	public String dtxsid;

	public String bcfbaf_id;
	public String bcfbaf_hash;
	public String bcfbaf_uuid;
	public String value_type;
	public String units;
	public String score;
	public String species_supercategory;
	public String species_scientific;
	public String species_common;
	public String author;
	public String title;
	public String year;
	public String journal;
	public String logbaf;
	public String logbcf;
	public String tissue;
	public String calc_method;
	public String comments;
	public String qa_level;
	public String logkow;
	public String logkow_reference;
	public String water_conc;
	public String radiolabel;
	public String exposure_duration;
	public String exposure_type;
	public String temperature;
	public String exposure_route;
	public String media;
	public String pH;
	public String total_organic_carbon;
	public String wet_weight;
	public String lipid_content;



	public transient static String[] varlist = { "bcfbaf_id","chemical_id","bcfbaf_hash","bcfbaf_uuid","dtxsid",
			"value_type","units","score","species_supercategory","species_scientific","species_common",
			"author","title","year","journal","logbaf","logbcf","tissue","calc_method","comments","qa_level",
			"logkow","logkow_reference","water_conc","radiolabel","exposure_duration","exposure_type","temperature",
			"exposure_route","media","pH","total_organic_carbon","wet_weight","lipid_content"};


	/**
	 * Create record based on header list and data list in csv using reflection to assign by header name
	 * 
	 * @param hlist
	 * @param list
	 * @return
	 */
	public static RecordToxValBCFBAF createRecord(String headerLine, String Line) {


		RecordToxValBCFBAF r=new RecordToxValBCFBAF();
		//convert to record:
		try {
			//			LinkedList<String>list=Utilities.Parse3(Line, "\t");

			String [] list=Line.split("\t");
			String [] hlist=headerLine.split("\t");

			//			if (list.length!=hlist.size()) {
			//				System.out.println(list.length+"\t"+hlist.size());
			//				System.out.println(Line);
			//			}

			for (int i=0;i<hlist.length;i++) {
				Field myField =r.getClass().getField(hlist[i]);
				myField.set(r, list[i]);
			}
		} catch (Exception ex) {
			ex.printStackTrace();
			System.out.println(Line);
		}
		return r;
	}
}
