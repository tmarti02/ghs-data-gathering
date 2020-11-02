package gov.epa.ghs_data_gathering.Parse.ToxVal.ParseTable_genetox_summary;

import java.lang.reflect.Field;

public class RecordToxValGenetox {
	
	public String casrn;
	public String name;
	public String dtxsid;

	public String genetox_summary_id;
	public String reports_pos;
	public String reports_neg;
	public String reports_other;
	public String ames;
	public String micronucleus;
	public String genetox_call;
	
	public transient static String[] varlist = { 
			"genetox_summary_id", "casrn","name","dtxsid","reports_pos","reports_neg","reports_other","ames","micronucleus","genetox_call"};
	
	
	/**
	 * Create record based on header list and data list in csv using reflection to assign by header name
	 * 
	 * @param hlist
	 * @param list
	 * @return
	 */
	public static RecordToxValGenetox createRecord(String headerLine, String Line) {
				
		RecordToxValGenetox r=new RecordToxValGenetox();
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
