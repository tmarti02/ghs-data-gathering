package gov.epa.exp_data_gathering.parse.ToxVal;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

//import com.google.gson.Gson;
//import com.google.gson.GsonBuilder;

import gov.epa.exp_data_gathering.parse.ExperimentalRecord;
import gov.epa.exp_data_gathering.parse.ExperimentalRecords;

public class ToxValQuery {
	
	private static final String TOXVAL_DB_PATH = "data/experimental/ToxVal/toxval_v8.db";
	
	private static final String TOXVAL_FILTERED_QUERY_BY_SPECIES_AND_TYPE =
			"SELECT c.dtxsid, c.casrn, c.name,\r\n"
			+ "	tv.toxval_id, tv.source, tv.subsource, tv.toxval_type, tv.toxval_type_original, tv.toxval_subtype, tv.toxval_subtype_original, ttd.toxval_type_supercategory,\r\n"
			+ "	tv.toxval_numeric_qualifier, tv.toxval_numeric_qualifier_original, tv.toxval_numeric, tv.toxval_numeric_original,\r\n"
			+ "	tv.toxval_numeric_converted, tv.toxval_units, tv.toxval_units_original, tv.toxval_units_converted, tv.risk_assessment_class,\r\n"
			+ "	tv.study_type, tv.study_type_original, tv.study_duration_class, tv.study_duration_class_original, tv.study_duration_value,\r\n"
			+ "	tv.study_duration_value_original, tv.study_duration_units, tv.study_duration_units_original, tv.human_eco,\r\n"
			+ "	tv.strain, tv.strain_original, tv.sex, tv.sex_original, tv.generation,\r\n"
			+ "	s.species_id, tv.species_original, s.species_common, s.species_supercategory, s.habitat,\r\n"
			+ "	tv.lifestage, tv.exposure_route, tv.exposure_route_original, tv.exposure_method, tv.exposure_method_original,\r\n"
			+ "	tv.exposure_form, tv.exposure_form_original, tv.media, tv.media_original, tv.critical_effect, tv.year, tv.quality_id, tv.priority_id,\r\n"
			+ "	tv.source_source_id, tv.details_text, tv.toxval_uuid, tv.toxval_hash, tv.datestamp,\r\n"
			+ "	rs.long_ref, rs.url\r\n"
			+ "FROM toxval tv\r\n"
			+ "	INNER JOIN chemical c on c.dtxsid=tv.dtxsid\r\n"
			+ "	LEFT JOIN species s on tv.species_id=s.species_id\r\n"
			+ "	INNER JOIN toxval_type_dictionary ttd on tv.toxval_type=ttd.toxval_type\r\n"
			+ "	LEFT JOIN record_source rs on rs.toxval_id=tv.toxval_id\r\n"
			+ "WHERE tv.species_original = ? AND \r\n"
			+ "	tv.toxval_type = ? AND \r\n"
			+ "	tv.toxval_units in ('mg/L', 'g/L', 'mol/L') AND\r\n"
			+ "	tv.media in ('-', 'Fresh water') AND\r\n"
			+ "	ttd.toxval_type_supercategory in ('Point of Departure', 'Toxicity Value', 'Lethality Effect Level') AND\r\n"
			+ "	tv.toxval_numeric > 0;";

	//TODO omit the fields that definitely dont need from this query:
	public static final String TOXVAL_FILTERED_QUERY_BY_SPECIES_AND_TYPE_V93 =
			"SELECT c.dtxsid, c.casrn, c.name,\r\n"
			+ "	tv.toxval_id, tv.source, tv.subsource, tv.toxval_type, tv.toxval_type_original, tv.toxval_subtype, tv.toxval_subtype_original, ttd.toxval_type_supercategory,\r\n"
			+ "	tv.toxval_numeric_qualifier, tv.toxval_numeric_qualifier_original, tv.toxval_numeric, tv.toxval_numeric_original,\r\n"
			+ "	tv.toxval_numeric_converted, tv.toxval_units, tv.toxval_units_original, tv.toxval_units_converted, tv.risk_assessment_class,\r\n"
			+ "	tv.study_type, tv.study_type_original, tv.study_duration_class, tv.study_duration_class_original, tv.study_duration_value,\r\n"
			+ "	tv.study_duration_value_original, tv.study_duration_units, tv.study_duration_units_original, tv.human_eco,\r\n"
			+ "	tv.strain, tv.strain_original, tv.sex, tv.sex_original, tv.generation,\r\n"
			+ "	s.species_id, tv.species_original, s.common_name, s.ecotox_group, s.habitat,\r\n"
			+ "	tv.lifestage, tv.exposure_route, tv.exposure_route_original, tv.exposure_method, tv.exposure_method_original,\r\n"
			+ "	tv.exposure_form, tv.exposure_form_original, tv.media, tv.media_original, tv.critical_effect, tv.year, tv.priority_id,\r\n"
			+ "	tv.source_source_id, tv.details_text, tv.toxval_uuid, tv.toxval_hash, tv.datestamp,\r\n"
			+ "	rs.quality, rs.document_name,rs.long_ref, rs.title,rs.author, rs.journal,rs.volume,rs.year, rs.url\r\n"
			+ "FROM toxval tv\r\n"
			+ "	INNER JOIN chemical c on c.dtxsid=tv.dtxsid\r\n"
			+ "	LEFT JOIN species s on tv.species_id=s.species_id\r\n"
			+ "	INNER JOIN toxval_type_dictionary ttd on tv.toxval_type=ttd.toxval_type\r\n"
			+ "	LEFT JOIN record_source rs on rs.toxval_id=tv.toxval_id\r\n"
			+ "WHERE s.common_name = ? AND \r\n"
			+ "	tv.toxval_type = ? AND \r\n"
			+ "	rs.quality not like '3%' AND rs.quality not like '4%' AND \r\n"
//			+ "	tv.toxval_units in ('mg/L', 'g/L', 'mol/L') AND\r\n"
			+ "	tv.media in ('-', 'Fresh water') AND\r\n"//only ECOTOX and DOD ERED have this filled in, only 1 salt water record
			+ "	ttd.toxval_type_supercategory in ('Point of Departure', 'Toxicity Value', 'Lethality Effect Level') AND\r\n"
			+ "	tv.toxval_numeric > 0;";
	
	
	//Following will give duplicates because there are multiple entries for each common name in species table:
	public static final String TOXVAL_FILTERED_QUERY_LOG_BCF ="select casrn,c.name, b.dtxsid, logbcf, units, tissue, calc_method, comments, b.water_conc, exposure_type, exposure_duration, media, temperature, pH, b.species_common, author,title, b.year,b.journal from bcfbaf b\r\n"
			+ "join species s on s.species_common=b.species_common\r\n"
			+ "join chemical c on c.dtxsid=b.dtxsid\r\n"
			+ "where s.species_supercategory like '%fish%' and logbcf is not null and tissue='Whole body'";
			
	public static final String TOXVAL_FILTERED_QUERY_LOG_BCF2 ="select casrn,c.name, b.dtxsid, logbcf, units, tissue, calc_method, comments, b.water_conc, exposure_type, exposure_duration, media, temperature, pH, b.species_common, author,title, b.year,b.journal from bcfbaf b\r\n"
			+ "join chemical c on c.dtxsid=b.dtxsid\r\n"
			+ "where logbcf is not null\n "
			+ "order by b.dtxsid,logbcf";
//			+ "where logbcf is not null and tissue='Whole body' order by b.dtxsid,logbcf";
			
		
	public String doi;
	public String notes;
	
	
	public static final String FATHEAD_MINNOW_SPECIES = "pimephales promelas";
	static final Double FATHEAD_MINNOW_DURATION = 4.0;
	
	private static final String WATER_FLEA_SPECIES = "daphnia magna";
	private static final Double WATER_FLEA_DURATION = 2.0;
	
	static final String TYPE_LC50 = "LC50";
	static final String CRITICAL_EFFECT = "mortality";

	static final String propertyCategoryAcuteAquaticToxicity = "Acute aquatic toxicity";
	static final String propertyCategoryBioaccumulation = "Bioaccumulation";
	
	static String version="v8";
	
	public Connection conn;
//	private static final Gson gson = new GsonBuilder().setPrettyPrinting().disableHtmlEscaping().create();
	
	public ToxValQuery() {
		try {
			Class.forName("org.sqlite.JDBC");
		} catch (ClassNotFoundException e) {
			e.printStackTrace();
		}  

		try {
			String url = "jdbc:sqlite:" + TOXVAL_DB_PATH;
			conn = DriverManager.getConnection(url);
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}
	
	

	

	private void setConnection(String db) {
		
		String host = System.getenv().get("DSSTOX_HOST");
		String port = System.getenv().get("DSSTOX_PORT");

		String url = "jdbc:mysql://" + host + ":" + port + "/" + db;
		String user = System.getenv().get("DSSTOX_USER");
		String password = System.getenv().get("DSSTOX_PASS");

		
		System.out.println("Getting connection to "+host+"\t"+port+"\t"+url);
		
		try {
			
			Class.forName("com.mysql.jdbc.Driver");
			conn = DriverManager.getConnection(url, user, password);			
		} catch (Exception ex) {
			ex.printStackTrace();
		}
	}
	
	public void setConnectionToxVal(String version) {
		if (version.equals(ParseToxVal.versionV93)) {
			setConnection("prod_toxval_v93");
		} else if (version.equals(ParseToxVal.versionProd)) {
			setConnection("prod_toxval");	
		} else {
			System.out.println("Invalid toxval version");
			conn=null;
		}
		
	}
	
	List<ToxValRecord> getRecords(String species, String type) {
		List<ToxValRecord> records = new ArrayList<ToxValRecord>();
		try (PreparedStatement prep = conn.prepareStatement(TOXVAL_FILTERED_QUERY_BY_SPECIES_AND_TYPE)) {
			prep.setString(1, species);
			prep.setString(2, type);
			try (ResultSet rs = prep.executeQuery()) {
				while (rs.next()) {
					records.add(ToxValRecord.fromResultSet(rs));
				}
			}
		} catch (SQLException e) {
			e.printStackTrace();
		}
		return records;
	}
	
	
		List<ToxValRecord> records = new ArrayList<ToxValRecord>();
		List<ToxValRecord> getRecords(String species, String type,String sql) {
		try (PreparedStatement prep = conn.prepareStatement(sql)) {
			prep.setString(1, species);
			prep.setString(2, type);
			
//			System.out.println(sql);
			
			try (ResultSet rs = prep.executeQuery()) {
				while (rs.next()) {
					records.add(ToxValRecord.fromResultSet2(rs));
				}
			}
		} catch (SQLException e) {
			e.printStackTrace();
		}
		return records;
	}
	
	List<ToxValRecord> getRecords(String sql) {
		List<ToxValRecord> records = new ArrayList<ToxValRecord>();

		try {
			
			Statement st = conn.createStatement();			
			ResultSet rs = st.executeQuery(sql);
			
			while (rs.next()) {
				records.add(ToxValRecord.fromResultSet2(rs));
			}
			
	
//			System.out.println(sql);
			
		} catch (SQLException e) {
			e.printStackTrace();
		}
		return records;
	}
	
	private static ExperimentalRecords getExperimentalRecords(String species, String type, Double duration, 
			String criticalEffect, List<String> omitSources,String version,String propertyType) {
		ToxValQuery query = new ToxValQuery();
		List<ToxValRecord> records = query.getRecords(species, type);
		List<ExperimentalRecord> experimentalRecordsList = records.stream()
				.filter(rec -> rec.isAcceptable(duration, criticalEffect, omitSources))
				.map(rec -> rec.toExperimentalRecord(version,duration,propertyType))
				.collect(Collectors.toList());
		query.close(); // Is this necessary? Seems like good practice.
		
		int counter = 1;
		ExperimentalRecords experimentalRecords = new ExperimentalRecords();
		for (ExperimentalRecord rec:experimentalRecordsList) {
			rec.id_physchem = rec.source_name + " "+counter;
			experimentalRecords.add(rec);
			counter++;
		}
		
		return experimentalRecords;
	}
	
	public static ExperimentalRecords getFatheadMinnowExperimentalRecords(List<String> omitSources) {
		ExperimentalRecords fatheadMinnowExperimentalRecords = getExperimentalRecords(FATHEAD_MINNOW_SPECIES, TYPE_LC50, FATHEAD_MINNOW_DURATION, 
				CRITICAL_EFFECT, omitSources,version,propertyCategoryAcuteAquaticToxicity);
		
		return fatheadMinnowExperimentalRecords;
	}
	
	public static ExperimentalRecords getWaterFleaExperimentalRecords(List<String> omitSources) {
		ExperimentalRecords waterFleaExperimentalRecords = getExperimentalRecords(WATER_FLEA_SPECIES, TYPE_LC50, WATER_FLEA_DURATION, 
				CRITICAL_EFFECT, omitSources,version,propertyCategoryAcuteAquaticToxicity);
		
		return waterFleaExperimentalRecords;
	}
	
	public void close() {
		try {
			if (!conn.isClosed()) {
				conn.close();
			}
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

}

