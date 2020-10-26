package gov.epa.ghs_data_gathering.Parse.ToxVal.ParseTable_cancer_summary;

import java.lang.reflect.Field;

public class RecordToxValCancer {
	
	public String casrn;
	public String name;
	public String chemical_id;
	public String dtxsid;
	public String source;
	public String cancer_call;
	public String exposure_route;
	public String url;
	
	public transient static String[] varlist = { 
			"casrn","name","chemical_id","dtxsid","source","cancer_call","exposure_route","url"};


	/**
	 * Create record based on header list and data list in csv using reflection to assign by header name
	 * 
	 * @param hlist
	 * @param list
	 * @return
	 */
	public static RecordToxValCancer createRecord(String headerLine, String Line) {
		
		
		RecordToxValCancer r=new RecordToxValCancer();
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
