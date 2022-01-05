package gov.epa.exp_data_gathering.parse.ToxVal;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
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
	
	private static final String FATHEAD_MINNOW_SPECIES = "pimephales promelas";
	private static final Double FATHEAD_MINNOW_DURATION = 4.0;
	
	private static final String WATER_FLEA_SPECIES = "daphnia magna";
	private static final Double WATER_FLEA_DURATION = 2.0;
	
	private static final String TYPE = "LC50";
	private static final String CRITICAL_EFFECT = "mortality";
	
	private Connection conn;
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
	
	private List<ToxValRecord> getRecords(String species, String type) {
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
	
	private static ExperimentalRecords getExperimentalRecords(String species, String type, Double duration, 
			String criticalEffect, List<String> omitSources) {
		ToxValQuery query = new ToxValQuery();
		List<ToxValRecord> records = query.getRecords(species, type);
		List<ExperimentalRecord> experimentalRecordsList = records.stream()
				.filter(rec -> rec.isAcceptable(duration, criticalEffect, omitSources))
				.map(rec -> rec.toExperimentalRecord())
				.collect(Collectors.toList());
		query.close(); // Is this necessary? Seems like good practice.
		
		int counter = 1;
		ExperimentalRecords experimentalRecords = new ExperimentalRecords();
		for (ExperimentalRecord rec:experimentalRecordsList) {
			rec.id_physchem = rec.source_name + counter;
			experimentalRecords.add(rec);
			counter++;
		}
		
		return experimentalRecords;
	}
	
	public static ExperimentalRecords getFatheadMinnowExperimentalRecords(List<String> omitSources) {
		ExperimentalRecords fatheadMinnowExperimentalRecords = getExperimentalRecords(FATHEAD_MINNOW_SPECIES, TYPE, FATHEAD_MINNOW_DURATION, 
				CRITICAL_EFFECT, omitSources);
		
		return fatheadMinnowExperimentalRecords;
	}
	
	public static ExperimentalRecords getWaterFleaExperimentalRecords(List<String> omitSources) {
		ExperimentalRecords waterFleaExperimentalRecords = getExperimentalRecords(WATER_FLEA_SPECIES, TYPE, WATER_FLEA_DURATION, 
				CRITICAL_EFFECT, omitSources);
		
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
