package gov.epa.ghs_data_gathering.Parse;

import java.util.Hashtable;

import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.Multimap;

import gov.epa.api.Chemical;
import gov.epa.api.Score;
import gov.epa.api.ScoreRecord;

public class CodeDictionary {
	
	
//	public static Hashtable<String,String>populateJapanHazardClassToScoreName() {
	public static Multimap<String, String>populateJapanHazardClassToScoreName() {
		
//		Hashtable<String,String>ht=new Hashtable<String,String>();
		Multimap<String, String> ht =ArrayListMultimap.create();
		
		ht.put("Explosives","omit");
		ht.put("Flammable gases (including chemically unstable gases)","omit");
		ht.put("Flammable solid","omit");
		
		ht.put("Aerosols","omit");
		ht.put("Oxidizing gases","omit");
		ht.put("Gases under pressure","omit");
		ht.put("Flammable liquids","omit");
		ht.put("Flammable solids","omit");
		ht.put("Self-reactive substances and mixtures","omit");
		ht.put("Pyrophoric liquids","omit");
		ht.put("Pyrophoric solids","omit");
		ht.put("Self-heating substances and mixtures","omit");
		ht.put("Substances and mixtures which, in contact with water, emit flammable gases","omit");
		ht.put("Oxidizing liquids","omit");
		ht.put("Oxidizing solids","omit");
		ht.put("Organic peroxides","omit");
		ht.put("Corrosive to metals","omit");
		ht.put("Aspiration hazard","omit");
		ht.put("Hazardous to the ozone layer","omit");
		ht.put("Respiratory sensitization","omit");
		
		ht.put("Acute toxicity (Oral)",Chemical.strAcute_Mammalian_ToxicityOral);
		ht.put("Acute toxicity (Dermal)",Chemical.strAcute_Mammalian_ToxicityDermal);
		
		ht.put("Acute toxicity (Inhalation: Gases)",Chemical.strAcute_Mammalian_ToxicityInhalation);
		ht.put("Acute toxicity (Inhalation: Vapours)",Chemical.strAcute_Mammalian_ToxicityInhalation);
		ht.put("Acute toxicity (Inhalation: Dusts and mists)",Chemical.strAcute_Mammalian_ToxicityInhalation);
		
		ht.put("Skin corrosion/irritation",Chemical.strSkin_Irritation);
		ht.put("Serious eye damage/eye irritation",Chemical.strEye_Irritation);
		ht.put("Skin sensitization",Chemical.strSkin_Sensitization);
		ht.put("Germ cell mutagenicity",Chemical.strGenotoxicity_Mutagenicity);
		ht.put("Carcinogenicity",Chemical.strCarcinogenicity);
		
		ht.put("Reproductive toxicity",Chemical.strReproductive);
		ht.put("Reproductive toxicity",Chemical.strDevelopmental);
		
		ht.put("Specific target organ toxicity - Single exposure",Chemical.strSystemic_Toxicity_Single_Exposure);
		ht.put("Specific target organ toxicity - Repeated exposure",Chemical.strSystemic_Toxicity_Repeat_Exposure);
		
		ht.put("Specific target organ toxicity - Single exposure",Chemical.strNeurotoxicity_Single_Exposure);
		ht.put("Specific target organ toxicity - Repeated exposure",Chemical.strNeurotoxicity_Repeat_Exposure);
		
		ht.put("Hazardous to the aquatic environment (Acute)",Chemical.strAcute_Aquatic_Toxicity);
		ht.put("Hazardous to the aquatic environment (Long-term)",Chemical.strChronic_Aquatic_Toxicity);
		return ht;
	}
	
	
	

	/**
	 * Create hashtable to lookup hazard statement for each H code<br><br>
	 * 
	 * Source: https://en.wikipedia.org/wiki/GHS_hazard_statements
	 * 
	 * @return
	 */
	public static Hashtable<String,String>getHazardStatementDictionaryH() {
		Hashtable<String,String>htHS=new Hashtable<String,String>();
		
		//Acute mammalian tox by ingestion:
		htHS.put("H300","Fatal if swallowed");
		htHS.put("H301","Toxic if swallowed");
		htHS.put("H302","Harmful if swallowed");
		htHS.put("H303","May be harmful if swallowed");
		
		htHS.put("H304","May be fatal if swallowed and enters airways");
		htHS.put("H305","May be harmful if swallowed and enters airways");

		//Acute mammalian tox by dermal exposure:
		htHS.put("H310","Fatal in contact with skin");
		htHS.put("H311","Toxic in contact with skin");
		htHS.put("H312","Harmful in contact with skin");
		htHS.put("H313","May be harmful in contact with skin");
		
		//Skin irritation
		htHS.put("H314","Causes severe skin burns and eye damage");
		htHS.put("H315","Causes skin irritation");
		htHS.put("H316","Causes mild skin irritation");
		htHS.put("H317","May cause an allergic skin reaction");
		
		//Eye irritation
		htHS.put("H318","Causes serious eye damage");
		htHS.put("H319","Causes serious eye irritation");
		htHS.put("H320","Causes eye irritation");
		
		//Acute mammalian tox by inhalation exposure:
		htHS.put("H330","Fatal if inhaled");
		htHS.put("H331","Toxic if inhaled");
		htHS.put("H332","Harmful if inhaled");
		htHS.put("H333","May be harmful if inhaled");
		
		htHS.put("H334","May cause allergy or asthma symptoms or breathing difficulties if inhaled");
		
		htHS.put("H335","May cause respiratory irritation");
		htHS.put("H336","May cause drowsiness or dizziness");
		
		//Genetic toxicity/mutagenicity
		htHS.put("H340","May cause genetic defects");
		htHS.put("H341","Suspected of causing genetic defects");
		
		//Cancer:
		htHS.put("H350","May cause cancer");
		htHS.put("H350i","May cause cancer by inhalation");
		htHS.put("H351","Suspected of causing cancer");
		
		//Reproductive and/or developmental toxicity codes:
		htHS.put("H360","May damage fertility or the unborn child");
		htHS.put("H360F","May damage fertility");
		htHS.put("H360D","May damage the unborn child");
		htHS.put("H360FD","May damage fertility. May damage the unborn child");
		htHS.put("H360Fd","May damage fertility. Suspected of damaging the unborn child");
		htHS.put("H360Df","May damage the unborn child. Suspected of damaging fertility");
		
		htHS.put("H361","Suspected of damaging fertility or the unborn child");
		htHS.put("H361d","Suspected of damaging the unborn child");
		htHS.put("H361f","Suspected of damaging fertility");
		htHS.put("H361fd","Suspected of damaging fertility. Suspected of damaging the unborn child");

		htHS.put("H362","May cause harm to breast-fed children");
		
		//Systemic toxicity, single exposure:
		htHS.put("H370","Causes damage to organs");
		htHS.put("H371","May cause damage to organs");
		
		//Systemic toxicity, repeat exposure:
		htHS.put("H372","Causes damage to organs through prolonged or repeated exposure");
		htHS.put("H373","May cause damage to organs through prolonged or repeated exposure");

		//TODO- need the following combination codes?
		htHS.put("H300+H310","Fatal if swallowed or in contact with skin");
		htHS.put("H300+H330","Fatal if swallowed or if inhaled");
		htHS.put("H310+H330","Fatal in contact with skin or if inhaled");
		htHS.put("H300+H310+H330","Fatal if swallowed, in contact with skin or if inhaled");
		htHS.put("H301+H311","Toxic if swallowed or in contact with skin");
		htHS.put("H301+H331","Toxic if swallowed or if inhaled");
		htHS.put("H311+H331","Toxic in contact with skin or if inhaled");
		htHS.put("H301+H311+H331","Toxic if swallowed, in contact with skin or if inhaled");
		htHS.put("H302+H312","Harmful if swallowed or in contact with skin");
		htHS.put("H302+H332","Harmful if swallowed or if inhaled");
		htHS.put("H312+H332","Harmful in contact with skin or if inhaled");
		htHS.put("H302+H312+H332","Harmful if swallowed, in contact with skin or if inhaled");
		
		//Acute Aquatic Toxicity: 
		htHS.put("H400","Very toxic to aquatic life");
		htHS.put("H401","Toxic to aquatic life");
		htHS.put("H402","Harmful to aquatic life");
		
		//Chronic Aquatic Toxicity:
		htHS.put("H410","Very toxic to aquatic life with long-lasting effects");
		htHS.put("H411","Toxic to aquatic life with long-lasting effects");
		htHS.put("H412","Harmful to aquatic life with long-lasting effects");
		htHS.put("H413","May cause long-lasting harmful effects to aquatic life");
		
		htHS.put("H420","Harms public health and the environment by destroying ozone in the upper atmosphere");

		return htHS;
	}
	
	
	public static Hashtable<String,String> populateCategoryToScoreName() {
		Hashtable<String,String> dictScore=new Hashtable<>();
		
		dictScore.put("Acute Tox. 1", Chemical.strAcute_Mammalian_Toxicity);
		dictScore.put("Acute Tox. 2", Chemical.strAcute_Mammalian_Toxicity);
		dictScore.put("Acute Tox. 3", Chemical.strAcute_Mammalian_Toxicity);
		dictScore.put("Acute Tox. 4", Chemical.strAcute_Mammalian_Toxicity);

		dictScore.put("Aquatic Acute 1", Chemical.strAcute_Aquatic_Toxicity);

		dictScore.put("Aquatic Chronic 1", Chemical.strChronic_Aquatic_Toxicity);
		dictScore.put("Aquatic Chronic 2", Chemical.strChronic_Aquatic_Toxicity);
		dictScore.put("Aquatic Chronic 3", Chemical.strChronic_Aquatic_Toxicity);
		dictScore.put("Aquatic Chronic 4", Chemical.strChronic_Aquatic_Toxicity);

		dictScore.put("Carc. 1A", Chemical.strCarcinogenicity);
		dictScore.put("Carc. 1B", Chemical.strCarcinogenicity);
		dictScore.put("Carc. 2", Chemical.strCarcinogenicity);

		dictScore.put("Eye Dam.", Chemical.strEye_Irritation);
		dictScore.put("Eye Dam. 1", Chemical.strEye_Irritation);
		dictScore.put("Eye Irrit. 2", Chemical.strEye_Irritation);

		dictScore.put("Muta. 1B", Chemical.strGenotoxicity_Mutagenicity);
		dictScore.put("Muta. 2", Chemical.strGenotoxicity_Mutagenicity);

		dictScore.put("Skin Corr. 1", Chemical.strSkin_Irritation);
		dictScore.put("Skin Corr. 1A", Chemical.strSkin_Irritation);
		dictScore.put("Skin Corr. 1B", Chemical.strSkin_Irritation);
		dictScore.put("Skin Corr. 1C", Chemical.strSkin_Irritation);
		dictScore.put("Skin Irrit. 2", Chemical.strSkin_Irritation);

		dictScore.put("Skin Sens. 1", Chemical.strSkin_Sensitization);
		dictScore.put("Skin Sens. 1A", Chemical.strSkin_Sensitization);
		dictScore.put("Skin Sens. 1B", Chemical.strSkin_Sensitization);

		dictScore.put("STOT RE 1", Chemical.strSystemic_Toxicity_Repeat_Exposure);
		dictScore.put("STOT RE 2", Chemical.strSystemic_Toxicity_Repeat_Exposure);

		dictScore.put("STOT SE 1", Chemical.strSystemic_Toxicity_Single_Exposure);
		dictScore.put("STOT SE 2", Chemical.strSystemic_Toxicity_Single_Exposure);
		dictScore.put("STOT SE 3", Chemical.strSystemic_Toxicity_Single_Exposure);// TODO H335: May cause respiratory
																					// irritation, H336: May cause
		
		// TODO add these categories???
		// Asp. Tox.
		// Lact.
// drowsiness or dizziness
		return dictScore;
		
	}
	
	public static  Multimap<String, String>populateCodeToStatementMultimap() {

		Multimap<String, String> m =ArrayListMultimap.create();

		// Acute oral
		m.put("H300", "Fatal if swallowed");
		m.put("H301", "Toxic if swallowed");
		m.put("H302", "Harmful if swallowed");
		m.put("H303", "May be harmful if swallowed");

		// acute dermal
		m.put("H310", "Fatal in contact with skin");
		m.put("H311", "Toxic in contact with skin");
		m.put("H312", "Harmful in contact with skin");
		m.put("H313", "May be harmful in contact with skin");

		// acute inhalation
		m.put("H330", "Fatal if inhaled");
		m.put("H331", "Toxic if inhaled");
		m.put("H332", "Harmful if inhaled");
		m.put("H333", "May by harmful if inhaled");

		// cancer
		m.put("H350", "May cause cancer");
		m.put("H350i", "May cause cancer if inhaled");
		m.put("H351", "Suspected of causing cancer");
		m.put("H351", "Suspected of causing cancer via inhalation");

		// skin irritation
		m.put("H314", "Causes severe skin burns and eye damage");
		m.put("H315", "Causes skin irritation");
		m.put("H316", "Causes mild skin irritation");

		// skin sensitization
		m.put("H317", "May cause an allergic skin reaction");

		// eye irritation
		m.put("H318", "Causes serious eye damage");
		m.put("H319", "Causes serious eye irritation");
		m.put("H320", "Causes eye irritation");

		// Systemic toxicity, single dose
		m.put("H370", "Causes damage to organs");
		m.put("H370", "Causes damage to the lungs through inhalation");

		m.put("H371", "May cause damage to organs");
		m.put("H335", "May cause respiratory irritation");
		m.put("H336", "May cause drowsiness or dizziness");

		// Systemic toxicity, repeat dose
		m.put("H372", "Causes damage to organs through prolonged or repeated exposure");
		m.put("H372", "Causes damage to organs through prolonged or repeated exposure if inhaled");
		m.put("H372", "Causes damage to organs through prolonged or repeated exposure via inhalation");
		m.put("H372", "Causes damage to the lungs through prolonged or repeated exposure");
		m.put("H372", "Causes damage to the nervous system through prolonged or repeated exposure");
		m.put("H372", "Causes damage to the nervous system through prolonged or repeated exposure if swallowed");
		m.put("H372",
				"Causes damage to organs (respiratory system) through prolonged or repeated exposure via inhalation");

		m.put("H373", "May cause damage to organs through prolonged or repeated exposure");
		m.put("H373", "May cause damage to organs through prolonged or repeated exposure if swallowed");
		m.put("H373", "May cause damage to the heart through prolonged or repeated exposure");
		m.put("H373", "May cause damage to the blood system through prolonged or repeated exposure");
		m.put("H373",
				"May cause damage to the lungs and respiratory system through prolonged or repeated exposure via inhalation");
		m.put("H373",
				"May cause damage to musculature and the nervous system through prolonged or repeated exposure if swallowed");
		m.put("H373",
				"May cause damage to organs through prolonged or repeated exposure if  swallowed or in contact with skin");
		m.put("H373",
				"May cause damage to organs through prolonged or repeated exposure if inhaled or in contact with skin");

		// mutagenicity
		m.put("H340", "May cause genetic defects");
		m.put("H341", "Suspected of causing genetic defects");

		// cancer
		m.put("H350", "May cause cancer");
		m.put("H350i", "May cause cancer if inhaled");

		m.put("H351", "Suspected of causing cancer");

		// Suspected of causing cancer via inhalation

		// Reproductive or developmental toxicity
		m.put("H360", "May damage fertility or the unborn child");
		m.put("H361", "Suspected of damaging fertility or the unborn child");
		m.put("H362", "May cause harm to breast-fed children");

		m.put("H360D", "May damage the unborn child");
		m.put("H360F", "May damage fertility");
		m.put("H360FD", "May damage fertility. May damage the unborn child.");
		m.put("H360Fd", "May damage fertility. Suspected of damaging the unborn child.");
		m.put("H360Df", "May damage the unborn child. Suspected of damaging fertility.");

		m.put("H361f", "Suspected of damaging fertility");
		m.put("H361f", "Suspected of damaging fertility by causing atrophy of the testes");
		m.put("H361d", "Suspected of damaging the unborn child");
		m.put("H361fd", "Suspected of damaging fertility. Suspected of damaging the unborn child.");

		// acute aquatic toxicity
		m.put("H400", "Very toxic to aquatic life");
		m.put("H401", "Toxic to aquatic life");
		m.put("H402", "Harmful to aquatic life");

		// chronic aquatic toxicity
		m.put("H410", "Very toxic to aquatic life with long-lasting effects");
		m.put("H411", "Toxic to aquatic life with long-lasting effects");

		m.put("H412", "Harmful to aquatic life with long lasting effects");
		m.put("H413", "May cause long lasting harmful effects to aquatic life");
		
		return m;
		
	}
	
	
	public static Multimap<String, String> populateCodeToCategory() {

		Multimap<String, String>m = ArrayListMultimap.create();

		// Acute oral
		m.put("H300", "Acute toxicity - category 1");
		m.put("H300", "Acute toxicity - category 2");
		m.put("H301", "Acute toxicity - category 3");
		m.put("H302", "Acute toxicity - category 4");
		m.put("H303", "Acute toxicity - category 5");

		m.put("H304", "Aspiration hazard - category 1");

		// acute dermal
		m.put("H310", "Acute toxicity - category 1");
		m.put("H310", "Acute toxicity - category 2");
		m.put("H311", "Acute toxicity - category 3");
		m.put("H312", "Acute toxicity - category 4");
		m.put("H313", "Acute toxicity - category 5");

		// acute inhalation
		m.put("H330", "Acute toxicity - category 1");
		m.put("H330", "Acute toxicity - category 2");
		m.put("H331", "Acute toxicity - category 3");
		m.put("H332", "Acute toxicity - category 4");
		m.put("H333", "Acute toxicity - category 5");

		// cancer
		m.put("H350", "Carcinogenicity - category 1");
		m.put("H350", "Carcinogenicity - category 1A");
		m.put("H350", "Carcinogenicity - category 1B");
		m.put("H350i", "Carcinogenicity - category 1A");
		m.put("H350i", "Carcinogenicity - category 1B");
		m.put("H351", "Carcinogenicity - category 2");

		// skin irritation
		m.put("H314", "Skin corrosion - category 1");
		m.put("H314", "Skin corrosion - category 1A");
		m.put("H314", "Skin corrosion - category 1B");
		m.put("H314", "Skin corrosion - category 1C");

		m.put("H315", "Skin irritation - category 2");
		m.put("H316", "Skin irritation - category 3");

		// eye irritation
		m.put("H318", "Eye damage - category 1");
		m.put("H314", "Eye damage - category 1");

		m.put("H319", "Eye irritation - category 2");
		m.put("H319", "Eye irritant - category 2A");
		m.put("H319", "Eye irritation - category 2A");
		m.put("H320", "Eye irritation - category 2B");

		// skin sensitization
		m.put("H317", "Skin sensitisation - category 1");
		m.put("H317", "Skin sensitisation - category 1A");
		m.put("H317", "Skin sensitisation - category 1B");
		m.put("H317", "Skin sensitiser - category 1");
		m.put("H317", "Skin sensitiser - category 1B");

		// Systemic toxicity, repeat dose
		m.put("H372", "Specific target organ toxicity (repeated exposure) - category 1");
		m.put("H373", "Specific target organ toxicity (repeated exposure) - category 2");

		// Systemic toxicity, single dose
		m.put("H370", "Specific target organ toxicity (single exposure) - category 1");
		m.put("H371", "Specific target organ toxicity (single exposure) - category 2");
		m.put("H335", "Specific target organ toxicity (single exposure) - category 3");
		m.put("H336", "Specific target organ toxicity (single exposure) - category 3");

		// acute aquatic toxicity
		m.put("H400", "Hazardous to the aquatic environment (acute) - category 1");
		m.put("H401", "Hazardous to the aquatic environment (acute) - category 2");
		m.put("H402", "Hazardous to the aquatic environment (acute) - category 3");

		// chronic aquatic toxicity
		m.put("H410", "Hazardous to the aquatic environment (chronic) - category 1");
		m.put("H411", "Hazardous to the aquatic environment (chronic) - category 2");
		m.put("H412", "Hazardous to the aquatic environment (chronic) - category 3");
		m.put("H413", "Hazardous to the aquatic environment (chronic) - category 4");

		// genetic/mutagencity
		m.put("H340", "Germ cell mutagenicity - category 1A");// May cause genetic defects
		m.put("H340", "Germ cell mutagenicity - category 1B");// May cause genetic defects
		m.put("H341", "Germ cell mutagenicity - category 2");// Suspected of causing genetic defects

		// Reproductive or developmental toxicity
		m.put("H360Df", "Reproductive toxicity - category 1A");
		m.put("H360Df", "Reproductive toxicity - category 1B");

		m.put("H360", "Reproductive toxicity - category 1A");
		m.put("H360", "Reproductive toxicity - category 1B");

		m.put("H360D", "Reproductive toxicity - category 1A");
		m.put("H360D", "Reproductive toxicity - category 1B");

		m.put("H360FD", "Reproductive toxicity - category 1A");
		m.put("H360FD", "Reproductive toxicity - category 1B");
		
		m.put("H360F", "Reproductive toxicity - category 1");
		m.put("H360F", "Reproductive toxicity - category 1A");
		m.put("H360F", "Reproductive toxicity - category 1B");
		m.put("H360Fd", "Reproductive toxicity - category 1A");
		m.put("H360Fd", "Reproductive toxicity - category 1B");

		m.put("H361", "Reproductive toxicity - category 2");
		m.put("H361f", "Reproductive toxicity - category 2");
		m.put("H361fd", "Reproductive toxicity - category 2");
		m.put("H361d", "Reproductive toxicity - category 2");

		m.put("H362", "Reproductive toxicity - effects on or via lactation");// May cause harm to breast-fed children

		m.put("H200", "Unstable explosive");
		m.put("H201", "Explosive - category 1.1");
		m.put("H203", "Explosive - category 1.3");

		m.put("H220", "Flammable gas - category 1");
		m.put("H221", "Explosive - category 1.3");

		m.put("H224", "Flammable liquid - category 1");
		m.put("H225", "Flammable liquid - category 2");
		m.put("H226", "Flammable liquid - category 3");
		m.put("H227", "Flammable liquid - category 4");

		m.put("H228", "Flammable solid - category 1");

		m.put("H240", "self-reactive substance or mixture - type A");

		m.put("H241", "Organic peroxide - type A");
		m.put("H241", "Organic peroxide - type B");
		m.put("H241", "Organic peroxide - type C");

		m.put("H242", "Organic peroxide - type A");
		m.put("H242", "Organic peroxide - type A");
		m.put("H242", "Organic peroxide - type A");

		m.put("H250", "Pyrophoric solid - category 1");

		m.put("H251", "Self-heating substance or mixture - category 1");

		m.put("H252", "Self-heating substance or mixture - category 1");
		m.put("H252", "Self-heating substance or mixture - category 2");

		m.put("H260", "Substance or mixture which in contact with water emits Flammable gas - category 1");
		m.put("H261", "Substance or mixture which in contact with water emits flammable gas - category 2");

		m.put("H270", "Oxidising gas - category 1");
		m.put("H271", "Oxidising solid - category 1");
		m.put("H272", "Oxidising solid - category 1");

		m.put("H290", "Corrosive to metals - category 1");

		m.put("H334", "Respiratory sensitisation - category 1");// Respiratory sensitisation - category 1, omit for now

		m.put("H420", "Hazardous to the ozone layer - category 1");

		m.put("AUH066", "");

		// Set<String> keys = m.keySet();
		// iterate through the key set and display key and values

		// for (String key : keys) {
		// System.out.println("Key = " + key);
		// System.out.println("Values = " + m.get(key) + "n");
		//
		// }

		
		return m;
	}
	
	public static Multimap<String, String> populateCodeToCategoryCanada() {

		Multimap<String, String>m = ArrayListMultimap.create();

		// Acute oral
		m.put("H300", "Acute toxicity - oral - Category 1");
		m.put("H300", "Acute toxicity - oral - Category 2");
		m.put("H301", "Acute toxicity - oral - Category 3");
		m.put("H302", "Acute toxicity - oral - Category 4");
		m.put("H303", "Acute toxicity - oral - Category 5");

		m.put("H304", "Aspiration hazard - category 1");

		// acute dermal
		m.put("H310", "Acute toxicity - dermal - Category 1");
		m.put("H310", "Acute toxicity - dermal - Category 2");
		m.put("H311", "Acute toxicity - dermal - Category 3");
		m.put("H312", "Acute toxicity - dermal - Category 4");
		m.put("H313", "Acute toxicity - dermal - Category 5");

		// acute inhalation
		m.put("H330", "Acute toxicity - inhalation - Category 1");
		m.put("H330", "Acute toxicity - inhalation - Category 2");
		m.put("H331", "Acute toxicity - inhalation - Category 3");
		m.put("H332", "Acute toxicity - inhalation - Category 4");
		m.put("H333", "Acute toxicity - inhalation - Category 5");

		// cancer
		m.put("H350", "Carcinogenicity - Category 1");
		m.put("H350", "Carcinogenicity - Category 1A");
		m.put("H350", "Carcinogenicity - Category 1B");
		
		m.put("H351", "Carcinogenicity - Category 2");

		// skin irritation
		m.put("H314", "Skin corrosion/irritation - Category 1");
		m.put("H314", "Skin corrosion/irritation - Category 1A");
		m.put("H314", "Skin corrosion/irritation - Category 1B");
		
		m.put("H315", "Skin corrosion/irritation - Category 2");
		m.put("H316", "Skin corrosion/irritation - Category 3");

		// eye irritation
		m.put("H314", "Eye damage - category 1");
		m.put("H318", "Serious eye damage/eye irritation - Category 1");
		m.put("H319", "Serious eye damage/eye irritation - Category 2");
		m.put("H319", "Serious eye damage/eye irritation - Category 2A");
		m.put("H320", "Serious eye damage/eye irritation - Category 2B");

		// skin sensitization
		m.put("H317", "Skin sensitization - Category 1A");
		m.put("H317", "Skin sensitization - Category 1B");
		m.put("H317", "Skin sensitization - Category 1");

		// Systemic toxicity, repeat dose
		m.put("H372", "Specific target organ toxicity - repeated exposure - Category 1");
		m.put("H373", "Specific target organ toxicity - repeated exposure - Category 2");

		// Systemic toxicity, single dose
		m.put("H370", "Specific target organ toxicity - single exposure - Category 1");
		m.put("H371", "Specific target organ toxicity - single exposure - Category 2");
		

		m.put("H335", "Specific target organ toxicity - single exposure (respiratory tract irritation) - Category 3");
		m.put("H335", "Specific target organ toxicity - single exposure (respiratory tract irritation) - Category 3 - Respiratory tract irritation");  
		
		m.put("H336", "Specific target organ toxicity - single exposure (narcotic effects) - Category 3 - Narcotic effect");

		// genetic/mutagencity
		m.put("H340", "Germ cell mutagenicity - Category 1A");// May cause genetic defects
		m.put("H340", "Germ cell mutagenicity - Category 1B");// May cause genetic defects
		m.put("H341", "Germ cell mutagenicity - Category 2");// Suspected of causing genetic defects

		// Reproductive or developmental toxicity
		m.put("H360", "Reproductive toxicity - Category 1");
		m.put("H360", "Reproductive toxicity - Category 1A");
		m.put("H360", "Reproductive toxicity - Category 1B");

		m.put("H361", "Reproductive toxicity - Category 2");
		m.put("H362", "Reproductive toxicity (lactation) - Effects on or via lactation");// May cause harm to breast-fed children

		return m;
	}
	

	public static String getRouteFromScoreName(String scoreName) {
		Hashtable<String, String> ht= new Hashtable<>();
		

		if (scoreName.equals(Chemical.strAcute_Mammalian_ToxicityOral)) {
			return "oral";
		} else if (scoreName.equals(Chemical.strAcute_Mammalian_ToxicityDermal)) {
			return "dermal";
		} else if (scoreName.equals(Chemical.strAcute_Mammalian_ToxicityInhalation)) {
			return "inhalation";
		} else {
			return null;
		}
	}
	
	
	public static Multimap<String, String>populateCodeToScoreName() {
		Multimap<String, String> dictCodeToScoreName = ArrayListMultimap.create();
		
		// Acute mammalian toxicity oral
		dictCodeToScoreName.put("H300", Chemical.strAcute_Mammalian_ToxicityOral);
		dictCodeToScoreName.put("H301", Chemical.strAcute_Mammalian_ToxicityOral);
		dictCodeToScoreName.put("H302", Chemical.strAcute_Mammalian_ToxicityOral);
		dictCodeToScoreName.put("H303", Chemical.strAcute_Mammalian_ToxicityOral);

		// Acute mammalian toxicity dermal
		dictCodeToScoreName.put("H310", Chemical.strAcute_Mammalian_ToxicityDermal);
		dictCodeToScoreName.put("H311", Chemical.strAcute_Mammalian_ToxicityDermal);
		dictCodeToScoreName.put("H312", Chemical.strAcute_Mammalian_ToxicityDermal);
		dictCodeToScoreName.put("H313", Chemical.strAcute_Mammalian_ToxicityDermal);

		// Acute mammalian toxicity inhalation
		dictCodeToScoreName.put("H330", Chemical.strAcute_Mammalian_ToxicityInhalation);
		dictCodeToScoreName.put("H331", Chemical.strAcute_Mammalian_ToxicityInhalation);
		dictCodeToScoreName.put("H332", Chemical.strAcute_Mammalian_ToxicityInhalation);
		dictCodeToScoreName.put("H333", Chemical.strAcute_Mammalian_ToxicityInhalation);

		// Carcinogenicity
		dictCodeToScoreName.put("H350", Chemical.strCarcinogenicity);
		dictCodeToScoreName.put("H350i", Chemical.strCarcinogenicity);
		dictCodeToScoreName.put("H351", Chemical.strCarcinogenicity);

		// skin irritation
		dictCodeToScoreName.put("H314", Chemical.strSkin_Irritation);
		dictCodeToScoreName.put("H315", Chemical.strSkin_Irritation);
		dictCodeToScoreName.put("H316", Chemical.strSkin_Irritation);

		// Skin Sensitization
		dictCodeToScoreName.put("H317", Chemical.strSkin_Sensitization);

		// Eye irritation
//		dictCodeToScoreName.put("H314", Chemical.strEye_Irritation);
		dictCodeToScoreName.put("H318", Chemical.strEye_Irritation);
		dictCodeToScoreName.put("H319", Chemical.strEye_Irritation);
		dictCodeToScoreName.put("H320", Chemical.strEye_Irritation);

		// Systemic toxicity, repeat dose
		dictCodeToScoreName.put("H372", Chemical.strSystemic_Toxicity_Repeat_Exposure);
		dictCodeToScoreName.put("H373", Chemical.strSystemic_Toxicity_Repeat_Exposure);

		// Systemic toxicity, single dose
		dictCodeToScoreName.put("H370", Chemical.strSystemic_Toxicity_Single_Exposure);
		dictCodeToScoreName.put("H371", Chemical.strSystemic_Toxicity_Single_Exposure);
		
		
		//Do we want to include this:
		dictCodeToScoreName.put("H335", Chemical.strSystemic_Toxicity_Single_Exposure);
		dictCodeToScoreName.put("H336", Chemical.strSystemic_Toxicity_Single_Exposure);

		// acute aquatic toxicity
		dictCodeToScoreName.put("H400", Chemical.strAcute_Aquatic_Toxicity);
		dictCodeToScoreName.put("H401", Chemical.strAcute_Aquatic_Toxicity);
		dictCodeToScoreName.put("H402", Chemical.strAcute_Aquatic_Toxicity);

		// chronic aquatic toxicity
		dictCodeToScoreName.put("H410", Chemical.strChronic_Aquatic_Toxicity);
		dictCodeToScoreName.put("H411", Chemical.strChronic_Aquatic_Toxicity);
		dictCodeToScoreName.put("H412", Chemical.strChronic_Aquatic_Toxicity);
		dictCodeToScoreName.put("H413", Chemical.strChronic_Aquatic_Toxicity);

		// genetic/mutagencity
		dictCodeToScoreName.put("H340", Chemical.strGenotoxicity_Mutagenicity);
		dictCodeToScoreName.put("H341", Chemical.strGenotoxicity_Mutagenicity);

		// Reproductive or developmental toxicity
		dictCodeToScoreName.put("H360", Chemical.strDevelopmental);
		dictCodeToScoreName.put("H360", Chemical.strReproductive);

		dictCodeToScoreName.put("H361", Chemical.strDevelopmental);
		dictCodeToScoreName.put("H361", Chemical.strReproductive);

		// Assign lactation to just developmental?
		dictCodeToScoreName.put("H362", Chemical.strDevelopmental);

		dictCodeToScoreName.put("H360FD", Chemical.strDevelopmental);
		dictCodeToScoreName.put("H360FD", Chemical.strReproductive);

		dictCodeToScoreName.put("H360D", Chemical.strDevelopmental);
		dictCodeToScoreName.put("H360F", Chemical.strReproductive);

		dictCodeToScoreName.put("H360Fd", Chemical.strDevelopmental);
		dictCodeToScoreName.put("H360Fd", Chemical.strReproductive);

		dictCodeToScoreName.put("H360Df", Chemical.strDevelopmental);
		dictCodeToScoreName.put("H360Df", Chemical.strReproductive);

		dictCodeToScoreName.put("H361d", Chemical.strDevelopmental);
		dictCodeToScoreName.put("H361f", Chemical.strReproductive);

		dictCodeToScoreName.put("H361fd", Chemical.strDevelopmental);
		dictCodeToScoreName.put("H361fd", Chemical.strReproductive);

		dictCodeToScoreName.put("H200", "Omit");
		dictCodeToScoreName.put("H201", "Omit");
		dictCodeToScoreName.put("H202", "Omit");
		dictCodeToScoreName.put("H203", "Omit");
		dictCodeToScoreName.put("H220", "Omit");
		dictCodeToScoreName.put("H221", "Omit");
		dictCodeToScoreName.put("H224", "Omit");
		dictCodeToScoreName.put("H225", "Omit");
		dictCodeToScoreName.put("H226", "Omit");
		dictCodeToScoreName.put("H227", "Omit");
		dictCodeToScoreName.put("H228", "Omit");
		dictCodeToScoreName.put("H240", "Omit");
		dictCodeToScoreName.put("H241", "Omit");
		dictCodeToScoreName.put("H242", "Omit");
		dictCodeToScoreName.put("H250", "Omit");
		dictCodeToScoreName.put("H251", "Omit");
		dictCodeToScoreName.put("H252", "Omit");
		dictCodeToScoreName.put("H260", "Omit");
		dictCodeToScoreName.put("H261", "Omit");
		dictCodeToScoreName.put("H270", "Omit");
		dictCodeToScoreName.put("H271", "Omit");
		dictCodeToScoreName.put("H272", "Omit");
		dictCodeToScoreName.put("H290", "Omit");
		dictCodeToScoreName.put("H304", "Omit");
		dictCodeToScoreName.put("H334", "Omit");
		dictCodeToScoreName.put("H420", "Omit");
		dictCodeToScoreName.put("AUH066", "Omit");
		dictCodeToScoreName.put("H280/281", "Omit");
		dictCodeToScoreName.put("280/281", "Omit");
		
		
		return  dictCodeToScoreName;
		

	}

	
	
	/**
	 * Create hashtable to assign scores based on GHS H codes
	 * 
	 * @return
	 */
	public static Hashtable<String,String> populateCodeToScoreValue() {
		
		Hashtable<String,String>dictCode=new Hashtable<String,String>();
		
		dictCode.put("H300", ScoreRecord.scoreVH);
		dictCode.put("H301", ScoreRecord.scoreH);
		dictCode.put("H302", ScoreRecord.scoreM);
		dictCode.put("H303", ScoreRecord.scoreL);
		
		//acute dermal
		dictCode.put("H310", ScoreRecord.scoreVH);
		dictCode.put("H311", ScoreRecord.scoreH);
		dictCode.put("H312", ScoreRecord.scoreM);
		dictCode.put("H313", ScoreRecord.scoreL);
		
		//acute inhalation
		dictCode.put("H330", ScoreRecord.scoreVH);
		dictCode.put("H331", ScoreRecord.scoreH);
		dictCode.put("H332", ScoreRecord.scoreM);
		dictCode.put("H333", ScoreRecord.scoreL);

		//cancer
		dictCode.put("H350", ScoreRecord.scoreVH);
		dictCode.put("H350i", ScoreRecord.scoreVH);
		dictCode.put("H351", ScoreRecord.scoreH);
		dictCode.put("H351 (Inhalation)", ScoreRecord.scoreH);
		
		//skin irritation
		dictCode.put("H314", ScoreRecord.scoreVH);
		dictCode.put("H315", ScoreRecord.scoreH);
		dictCode.put("H316", ScoreRecord.scoreM);
		
		//eye irritation
		dictCode.put("H318", ScoreRecord.scoreVH);
		dictCode.put("H319", ScoreRecord.scoreH);
		dictCode.put("H320", ScoreRecord.scoreM);
		
		//Systemic toxicity, repeat dose
//		dictCode.put("H372", ScoreRecord.scoreVH);//to match Japan increase by one score category, see pg 30 of Design for the Environment Program Alternatives Assessment Criteria for Hazard Evalulation 
//		dictCode.put("H373", ScoreRecord.scoreH);
		//To match DfE:
		dictCode.put("H372", ScoreRecord.scoreH); 
		dictCode.put("H373", ScoreRecord.scoreM);

		//Systemic toxicity, single dose
//		dictCode.put("H370", ScoreRecord.scoreVH);
//		dictCode.put("H371", ScoreRecord.scoreH);
		
		dictCode.put("H370", ScoreRecord.scoreH);//make consistent with repeat dose
		dictCode.put("H371", ScoreRecord.scoreM);

		
		dictCode.put("H335", ScoreRecord.scoreM);
		dictCode.put("H336", ScoreRecord.scoreM);

		
		//skin sensitization
		dictCode.put("H317", ScoreRecord.scoreH);

		//acute aquatic toxicity
		dictCode.put("H400", ScoreRecord.scoreVH);
		dictCode.put("H401", ScoreRecord.scoreH);
		dictCode.put("H402", ScoreRecord.scoreM);
		
		//chronic aquatic toxicity
		dictCode.put("H410", ScoreRecord.scoreVH);
		dictCode.put("H411", ScoreRecord.scoreH);
		dictCode.put("H412", ScoreRecord.scoreM);
		dictCode.put("H413", ScoreRecord.scoreL);

		//Reproductive or developmental toxicity
		dictCode.put("H360", ScoreRecord.scoreH);//May damage fertility or the unborn child
		dictCode.put("H361", ScoreRecord.scoreM);//Suspected of damaging fertility or the unborn child
		dictCode.put("H362", ScoreRecord.scoreH);//May cause harm to breast-fed children
		
		//genetic
//		dictCode.put("H340", ScoreRecord.scoreH);//May cause genetic defects- see pg 29 DfE guide
//		dictCode.put("H341", ScoreRecord.scoreM);//Suspected of causing genetic defects

		dictCode.put("H340", ScoreRecord.scoreVH);//May cause genetic defects- see pg 29 DfE guide
		dictCode.put("H341", ScoreRecord.scoreH);//Suspected of causing genetic defects

		
		return dictCode;
		
	}
	
	
	public static void main(String[] args) {
		// TODO Auto-generated method stub

	}


	

}
