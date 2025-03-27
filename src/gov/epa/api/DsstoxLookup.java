package gov.epa.api;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;

import gov.epa.database.SqlUtilities;
import gov.epa.exp_data_gathering.parse.ParseUtilities;


/**
* @author TMARTI02
*/
public class DsstoxLookup {
	
	
	public static class DsstoxRecord {
		
		public String casrn;
		public String dtxsid;
		public String preferredName;
		public Double molecularWeight;
		public String smiles;
	}
	
	
	public List<DsstoxRecord> getDsstoxRecordsByCAS(Collection<String> casrns,boolean searchOther) {
		
		List<DsstoxRecord> dsstoxRecords = new ArrayList<DsstoxRecord>();
		List<String>casrnsBatch=new ArrayList<>();
		
		for (String casrn:casrns) {
			casrnsBatch.add(casrn);

			if(casrnsBatch.size()==1000) {
				
				List<DsstoxRecord> dsstoxRecordsBatch=findAsDsstoxRecordsByCasrnIn(casrnsBatch);
				dsstoxRecords.addAll(dsstoxRecordsBatch);
//				System.out.println(casrnsBatch.size()+"\t"+dsstoxRecords.size());
				
				if(searchOther) {
					dsstoxRecordsBatch=findAsDsstoxRecordsByOtherCasrnIn(casrnsBatch);
					dsstoxRecords.addAll(dsstoxRecordsBatch);
				}

				casrnsBatch.clear();
			}
		}
		//Do what's left
		List<DsstoxRecord> dsstoxRecordsBatch=findAsDsstoxRecordsByCasrnIn(casrnsBatch);
		dsstoxRecords.addAll(dsstoxRecordsBatch);
//		System.out.println(dsstoxRecordsBatch.size());
		
		if(searchOther) {
			dsstoxRecordsBatch=findAsDsstoxRecordsByOtherCasrnIn(casrnsBatch);
			dsstoxRecords.addAll(dsstoxRecordsBatch);
		}
//		System.out.println(dsstoxRecordsBatch.size());

//		System.out.println(casrnsBatch.size()+"\t"+dsstoxRecords.size());

		return dsstoxRecords;
	}
	
	
	public List<DsstoxRecord> getDsstoxRecordsByDTXSIDS(Collection<String> dtxsids) {
		
		List<DsstoxRecord> dsstoxRecords = new ArrayList<DsstoxRecord>();
		List<String>dtxsidsBatch=new ArrayList<>();
		
		for (String dtxsid:dtxsids) {
			dtxsidsBatch.add(dtxsid);

			if(dtxsidsBatch.size()==1000) {
				
				List<DsstoxRecord> dsstoxRecordsBatch=findAsDsstoxRecordsByDtxsidsWithCompoundInfo(dtxsidsBatch);
				dsstoxRecords.addAll(dsstoxRecordsBatch);
//				System.out.println(casrnsBatch.size()+"\t"+dsstoxRecords.size());
				dtxsidsBatch.clear();
			}
		}
		//Do what's left
		List<DsstoxRecord> dsstoxRecordsBatch=findAsDsstoxRecordsByDtxsidsWithCompoundInfo(dtxsidsBatch);
		dsstoxRecords.addAll(dsstoxRecordsBatch);
//		System.out.println(dsstoxRecordsBatch.size());
		
//		System.out.println(dsstoxRecordsBatch.size());

//		System.out.println(casrnsBatch.size()+"\t"+dsstoxRecords.size());

		return dsstoxRecords;
	}


	private List<DsstoxRecord> findAsDsstoxRecordsByCasrnIn(Collection<String> casrns) {
		String sql="SELECT dsstox_substance_id,casrn, preferred_name FROM generic_substances gs where casrn ";
		sql+=arrayToSqlIn(casrns)+";";
		
//		System.out.println(sql);
		
		ResultSet rs=SqlUtilities.runSQL2(SqlUtilities.getConnectionDSSTOX(), sql);
		List<DsstoxRecord>recs=new ArrayList<>();
		try {
			while (rs.next()) {
				DsstoxRecord dr=new DsstoxRecord();
				dr.dtxsid=rs.getString(1);
				dr.casrn=rs.getString(2);
				dr.preferredName=rs.getString(3);
				recs.add(dr);
			}
		} catch (SQLException e) {
			e.printStackTrace();
		}
		
//		System.out.println(sql);
		return recs;
	}
	
	private List<DsstoxRecord> findAsDsstoxRecordsByDtxsidsWithCompoundInfo(Collection<String> dtxsids) {
		
		String sql="SELECT dsstox_substance_id,casrn, preferred_name, c.mol_weight, c.smiles FROM generic_substances gs\n"+
		"join generic_substance_compounds gsc on gs.id = gsc.fk_generic_substance_id\n"+
		"join compounds c on gsc.fk_compound_id = c.id\n"+
		"where dsstox_substance_id "+arrayToSqlIn(dtxsids)+";";
		
		
//		String sql="SELECT dsstox_substance_id,casrn, preferred_name FROM generic_substances gs where casrn ";
//		sql+=arrayToSqlIn(dtxsids)+";";
		
//		System.out.println(sql);
		
		ResultSet rs=SqlUtilities.runSQL2(SqlUtilities.getConnectionDSSTOX(), sql);
		List<DsstoxRecord>recs=new ArrayList<>();
		try {
			while (rs.next()) {
				DsstoxRecord dr=new DsstoxRecord();
				dr.dtxsid=rs.getString(1);
				dr.casrn=rs.getString(2);
				dr.preferredName=rs.getString(3);
				dr.molecularWeight=rs.getDouble(4);
				dr.smiles=rs.getString(5);
				recs.add(dr);
			}
		} catch (SQLException e) {
			e.printStackTrace();
		}
		
//		System.out.println(sql);
		return recs;
	}
	
	private List<DsstoxRecord> findAsDsstoxRecordsByOtherCasrnIn(Collection<String> casrns) {
		String sql="select oc.casrn, dsstox_substance_id,gs.preferred_name from other_casrns oc\r\n"
				+ "		join generic_substances gs on oc.fk_generic_substance_id = gs.id\r\n"
				+ "		where oc.casrn";
		sql+=arrayToSqlIn(casrns)+";";
		
		ResultSet rs=SqlUtilities.runSQL2(SqlUtilities.getConnectionDSSTOX(), sql);
		List<DsstoxRecord>recs=new ArrayList<>();
		try {
			while (rs.next()) {
				DsstoxRecord dr=new DsstoxRecord();
				dr.dtxsid=rs.getString(1);
				dr.casrn=rs.getString(2);
				dr.preferredName=rs.getString(3);
				recs.add(dr);
			}
		} catch (SQLException e) {
			e.printStackTrace();
		}
		
//		System.out.println(sql);
		return recs;
	}

	String arrayToSqlIn(Collection<String>array) {
		String sql=" in (";
		int count=0;
		for(String val:array) {
			sql+="'"+ val+"'";
			if(count<array.size()-1) sql+=",";
			count++;
		}
		sql+=")";
		return sql;
	}
	
	
	public static void main(String[] args) {
		DsstoxLookup d=new DsstoxLookup();
//		List<String>casrns=Arrays.asList("71-43-2","91-20-3","129-00-0");
//		List<DsstoxRecord>recs=d.getDsstoxRecordsByCAS(casrns);

		
		List<String>casrns=Arrays.asList("1001914-35-7");
		List<DsstoxRecord>recs=d.getDsstoxRecordsByCAS(casrns,true);

//		System.out.println(d.arrayToSqlIn(casrns));
		System.out.println(ParseUtilities.gson.toJson(recs));
	}

}
