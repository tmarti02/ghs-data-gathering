package gov.epa.ghs_data_gathering.Parse.ToxVal.ParseTable_models;

import java.lang.reflect.Field;

public class RecordToxValModels {
	
	public String casrn;
	public String name;
	public String dtxsid;

	public String model_id;
	public String chemical_id;
	public String model;
	public String metric;
	public String value;
	public String units;
	public String qualifier;
	
			
	public transient static String[] varlist = { 
			"casrn","name","dtxsid","model_id","chemical_id","model","metric","value","units","qualifier"};
		
	/**
	 * Create record based on header list and data list in csv using reflection to assign by header name
	 * 
	 * @param hlist
	 * @param list
	 * @return
	 */
	public static RecordToxValModels createRecord(String headerLine, String Line) {
				
		RecordToxValModels r=new RecordToxValModels();
		try {

			String [] list=Line.split("\t");
			String [] hlist=headerLine.split("\t");
			
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
