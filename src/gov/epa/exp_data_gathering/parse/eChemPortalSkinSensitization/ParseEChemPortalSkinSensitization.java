package gov.epa.exp_data_gathering.parse.eChemPortalSkinSensitization;

import java.util.ArrayList;
import java.util.Hashtable;
import gov.epa.eChemPortalAPI.Processing.FinalRecord;
import gov.epa.eChemPortalAPI.Processing.FinalRecords;
import gov.epa.exp_data_gathering.parse.ExperimentalRecord;
import gov.epa.exp_data_gathering.parse.ExperimentalRecords;
import gov.epa.exp_data_gathering.parse.EChemPortal.EChemPortalGetRecords;
import gov.epa.ghs_data_gathering.GetData.EChemPortalParse;

public class ParseEChemPortalSkinSensitization {

	/**
	 * From LLNA paper with Leora:
	 * 
	 * The parameters used to query the eChemPortal database were: 
	 * 
	 * Query Block Type: “skin sensitization”; 
	 * 
	 * Type of Information: “experimental study”; 
	 * 
	 * Reliability:* “1” or “2”; 
	 * 
	 * Test Guideline, Qualifier: “according to” or “equivalent or similar to”; 
	 * 
	 * Test Guideline: EPA OPPTS 870.2600, EU Method B.42, or OECD
	 * Guideline 429, 442A, or 442B; 
	 * 
	 * GLP Compliance: “not specified”, “yes”, or “yes incl. certificate”; 
	 * Endpoint: skin sensitization: in vivo (LLNA); 
	 * 
	 * Type of study: mouse LLNA, mouse LLNA BrdU-ELISA, mouse LLNA DA, reduced LLNA,
	 * reduced LLNA BrdU-ELISA, reduced LLNA DA, or “not specified”; 
	 * 
	 * Species: “mouse”; 
	 * 
	 * Interpretation of results: Category 1, 1A, or 1B, GHS criteria not met, or “other:”
	 */
	public static ExperimentalRecords getEChemPortalLLNARecords() {
		
		Hashtable<String,String>dict=getIOR_Dictionary();
		
		ExperimentalRecords expRecords=EChemPortalGetRecords.getExperimentalRecordsByPropertyName("mouse_SkinSensitisation");
		FinalRecords finalRecords=EChemPortalGetRecords.getFinalRecordsByEndpointType("skin sensitisation: in vivo (LLNA)");
		
		Hashtable<String,FinalRecord>htFinalRecords=new Hashtable<>();
		
		for (FinalRecord fr:finalRecords) {
			htFinalRecords.put(fr.id, fr);
		}
		
		ExperimentalRecords expRecords2=new ExperimentalRecords();
		
		ArrayList<String>casUnique=new ArrayList<>();
		
		for (ExperimentalRecord er:expRecords) {
			if (!er.original_source_name.contentEquals("ECHA REACH")) continue;			
			if (er.reason.contentEquals("No identifiers")) continue;
			
			if (htFinalRecords.get(er.fr_id)==null) continue;
			
			FinalRecord fr=htFinalRecords.get(er.fr_id);			
			if (fr.species==null) continue;
			
			//TODO check guideline:
//			Test Guideline: EPA OPPTS 870.2600, EU Method B.42, or OECD Guideline 429, 442A, or 442B
						
									
			if (fr.name!=null) {//Get rid of obvious mixtures:
				if (fr.name.toLowerCase().contains("react")) continue;
				if (fr.name.toLowerCase().contains("mixed")) continue;
				if (fr.name.toLowerCase().contains("product")) continue;					
			}

			String ior = getIOR(fr);
			if (ior.contains("charcoal")) continue;//dont know what those mean
			
			if (dict.get(ior.toLowerCase())==null)
				System.out.println(ior+"\t***"+fr.interpretationOfResults);
			
			
			if (!casUnique.contains(er.casrn)) casUnique.add(er.casrn);
			
			expRecords2.add(er);
		}
		
		
		System.out.println(expRecords2.size());
		System.out.println(casUnique.size());
		return expRecords2;
//		System.out.println(finalRecords.size());
	}

	private static String getIOR(FinalRecord fr) {
		String ior=fr.interpretationOfResults.replace("Migrated information", "").replace("other:", "").trim().toLowerCase();

		if (ior.contains("criteria used for")) 
			ior=ior.substring(0, ior.indexOf("criteria used for"));		 
		if (ior.contains("in accordance")) 
			ior=ior.substring(0, ior.indexOf("in accordance"));
		if (ior.contains("according to")) 
			ior=ior.substring(0, ior.indexOf("according to"));
		if (ior.contains("based on")) 
			ior=ior.substring(0, ior.indexOf("based on"));
		
		ior =ior.trim();
		
				
		
		return ior;
	}
	
	public static void main (String[] args) {		
		getEChemPortalLLNARecords();
	}
	
	private static Hashtable<String,String>getIOR_Dictionary() {
		
		Hashtable <String,String>ht=new Hashtable<>();
		
		String strA=EChemPortalParse.scoreAmbiguous;
		String strN=EChemPortalParse.scoreNegative;
		String strP=EChemPortalParse.scorePositive;
		
//		String scoreMild=strA;//assign mild to ambiguous result
		String scoreMild=strP;//assign mild to positive result
		
		ht.put("mild sensitiser",scoreMild);
		ht.put("weak dermal sensitization potential",scoreMild);
		ht.put("weak sensitizer",scoreMild);
		ht.put("weak skin sensitizer",scoreMild);
		ht.put("weak-sensitiser",scoreMild);
		ht.put("weakly sensitising",scoreMild);
		ht.put("weakly sensitizing",scoreMild);
		ht.put("potentially weak skin sensitizer",scoreMild);
		ht.put("likely sensitising",scoreMild);
		ht.put("the llna indicates weak skin sensitising properties for furfuryl alcohol.  evidence of skin irritation at 75%",scoreMild);
		ht.put("the llna indicates weak skin sensitising properties for furfuryl alcohol.  some evidence of skin irritation, particularly in initial test.",scoreMild);
		ht.put("weak sensitizing",scoreMild);


		ht.put("unclassified",strA);
		ht.put("evaluation",strA);
		ht.put("ambigous",strA);
		ht.put("ambiguous",strA);
		ht.put("ambiguous 1,4-chdm dge belongs to a class of chemistries (diglycidyl ethers/epoxies) that have been well-established to have dermal sensitization potential. therefore, at concentrations greater than 10%, 1,4-chdm dge may have weak dermal sensitization potential.",strA);
		ht.put("equivocal",strA);
		ht.put("false positive",strA);
		ht.put("inconclusive",strA);
		ht.put("inconclusive (read-across); the llna is not a suitable method for this substance",strA);
		ht.put("irritant but not sensitising",strA);
		ht.put("no indication for a specific skin sensitizing effect in opinion of study director",strA);
		ht.put("not a skin sensitiser up to 25%",strA);
		ht.put("not conclusive",strA);
		ht.put("potential skin sensitiser but response was borderline",strA);
		ht.put("sensitising (precausionary classification",strA);
		ht.put("study cannot be used for classification",strA);
		ht.put("study cannot be used for classification result not conclusive",strA);
		ht.put("study cannot be used for classification test substance had a positive result in llna test, but potential irriation effect does not rule out the possibility that it could be false positive result.",strA);
		ht.put("the concentration of test item expected to cause a 3 fold increase in3htdr incorporation (ec3value) was calculated to be 94%.",strA);
		
		ht.put("a non-sensitizer",strN);
		ht.put("clp criteria not met",strN);
		ht.put("clp criteria not met does not need to be classified",strN);
		ht.put("clp criteria not met not classified",strN);
		ht.put("clp/ eu ghs criteria not met, no classification required",strN);
		ht.put("clp/eu ghs criteria are not met, no classification required",strN);
		ht.put("clp/eu ghs criteria not met, no classification",strN);
		ht.put("clp/eu ghs criteria not met, no classification required",strN);
		ht.put("considered to be a non-sensitizer under the conditions of the test",strN);
		ht.put("criteria for classification as a skin sensitiser not met",strN);
		ht.put("eu classification criteria not met",strN);
		ht.put("ghs criteria not met",strN);
		ht.put("ghs criteria not met clp implementation.",strN);
		ht.put("ghs criteria not met eu criteria.",strN);
		ht.put("ghs criteria not met non-sensitizer",strN);
		ht.put("ghs criteria not met not a skin sensitiser.",strN);
		ht.put("ghs criteria not met not classified",strN);
		ht.put("ghs criteria not met not sensitising",strN);
		ht.put("ghs criteria not met not sensitizing",strN);
		ht.put("ghs criteria not met stimulation index is < 3 (non sensitizer)",strN);
		ht.put("ghs criteria not met substance is not a skin sensitizer",strN);
		ht.put("ghs criteria not met the ec3 value could not be determined.",strN);
		ht.put("ghs criteria not met the substance was concluded not to be a sensitizer.",strN);
		ht.put("ghs criteria not met the test item has not to be classified for skin sensitisation",strN);
		ht.put("no sensitizing potential; no indication for non-specific (irritant) activation",strN);
		ht.put("no skin sensitisation potential",strN);
		ht.put("no skin sensitization potencial",strN);
		ht.put("no skin sensitization potential",strN);
		ht.put("non sensitising",strN);
		ht.put("not-sensitising",strN);
		ht.put("non-sensitising",strN);
		ht.put("non skin sensitizing",strN);
		ht.put("not a skin sensitiser",strN);
		ht.put("not a skin sensitiser.",strN);
		ht.put("not a skin sensitizer",strN);
		ht.put("not classified",strN);
		ht.put("not classified annex i of the clp regulation (1272/2008/ec)",strN);
		ht.put("not classified as a skin sensitizer ghs criteria",strN);
		ht.put("not classified as skin sensitiser",strN);
		ht.put("not classified as skin sensitizer",strN);
		ht.put("not classified classification criteria",strN);
		ht.put("not classified for skin sensitization (clp regulation ec no. 1272/2008)",strN);
		ht.put("not classified,",strN);
		ht.put("not classified, criteria not met",strN);
		ht.put("not irritant or sensitising",strN);
		ht.put("not sensiting",strN);
		ht.put("not sensitising",strN);
		ht.put("not sensitising, clp criteria not met.",strN);
		ht.put("not sensitising.",strN);
		ht.put("not sensitizing",strN);
		ht.put("not skin sensitising",strN);
		ht.put("not skin sensitizer",strN);
		ht.put("not skin sensitizing",strN);
		ht.put("the substance does not need to be classified as skin sensitiser",strN);
		ht.put("the substance is unlikely to be a moderate or strong skin sensitiser under the conditions of the test.",strN);
		ht.put("the test item does not meet the criteria for classification",strN);
		ht.put("not induce delayed contact hypersensivity in the murine llna. for more information, see executive summary.",strN);
		ht.put("ghs criteria not met the test item was considered to be a non-sensitizer under the conditions of the test.",strN);
		ht.put("no sensitizing or irritating potential was ascribed to lee011-a4",strN);
		ht.put("ghs criteria not met the test item was considered to be a non-sensitizer under the conditions of the test.",strN);
		ht.put("not classified for skin sensitisation",strN);
		ht.put("ghs criteria not met substance is not a skin sensitiser",strN);
		ht.put("ghs criteria not met si < 3",strN);
		ht.put("no category (clp regulation ec no. 1272/2008)",strN);
		ht.put("eu ghs criteria not met",strN);
		ht.put("no skin sensitising potential",strN);
		ht.put("no skin sensitising potential",strN);
		ht.put("ghs criteria not met unclassified",strN);
		ht.put("not considered a sensitizer",strN);
		ht.put("ghs criteria not met eu clp",strN);
		ht.put("negative for skin sensitization",strN);
		ht.put("not classified as a skin sensitiser not classified as a skin sensitiser",strN);
		ht.put("ghs criteria not met not classified by clp criteria",strN);
		ht.put("ghs criteria not met specific count",strN);
		ht.put("ghs criteria not met regulation (ec) no 1272/2008",strN);
		ht.put("the substance does not need to be classified for skin sensitisation",strN);

		ht.put("a moderate skin sensitizer",strP);
		ht.put("cat. 1, h317",strP);
		ht.put("category 1 (skin sensitising)",strP);
		ht.put("sensitising clp category 1b",strP);		
		ht.put("category 1a",strP);
		ht.put("category 1a (indication of significant skin sensitising potential)",strP);
		ht.put("category 1b",strP);
		ht.put("category 1b (clp regulation ec no. 1272/2008)",strP);
		ht.put("category 1b (indication of skin sensitising potential)",strP);
		ht.put("category 1b (sensitising)",strP);
		ht.put("category 1b (skin sensitising)",strP);
		ht.put("category 1b - as per clp criteria",strP);
		ht.put("category 1b according clp",strP);
		ht.put("category 1b as per clp criteria",strP);
		ht.put("category 1b skin sensitiser",strP);
		ht.put("category skin sens 1.b",strP);
		ht.put("classified as a sensitiser as category 1b",strP);
		ht.put("classified as a skin sensitiser category 1b",strP);
		ht.put("classified as category 1 for skin sensitisation",strP);
		ht.put("classified under eu criteria",strP);
		ht.put("clp criteria met category 1b",strP);
		ht.put("clp/eu ghs category 1, h317",strP);
		ht.put("clp/eu ghs category 1a (h317)",strP);
		ht.put("clp/eu ghs category 1b (h317)",strP);
		ht.put("eu clp criteria skin sensitising (category 1b)",strP);
		ht.put("eu criteria: category 1a",strP);
		ht.put("eu criteria: category 1a (h317: may cause an allergic skin reaction)",strP);
		ht.put("eu criteria: h317: may cause an allergic skin reaction",strP);
		ht.put("moderate sensitiser",strP);
		ht.put("moderate skin sensitiser",strP);
		ht.put("moderate skin sensitizer",strP);
		ht.put("moderate skin sensitizing",strP);
		ht.put("positive results in cell proliferation revealed that the test substance could be a contact allergen in mice.",strP);
		ht.put("produced sensitising effects",strP);
		ht.put("sensitiser 1b",strP);
		ht.put("sensitising",strP);
		ht.put("sensitising (cat 1b) eu clp 1272/2008 and its amendments",strP);
		ht.put("sensitising (skin sensitizer, category 1)",strP);
		ht.put("sensitising 1b",strP);
		ht.put("sensitising cat. 1b",strP);
		ht.put("sensitising cat. 1b eu clp (ec 1272/2008 and its amendments)",strP);
		ht.put("sensitising category 1b",strP);
		ht.put("sensitising in concentration >5 v/v %",strP);
		ht.put("sensitising resulting in cat 1b",strP);
		ht.put("sensitizer",strP);
		ht.put("sensitizing",strP);
		ht.put("skin sens 1b (h317),",strP);
		ht.put("skin sens cat 1",strP);
		ht.put("skin sens cat 1b is required",strP);
		ht.put("skin sens. 1b",strP);
		ht.put("skin sens. cat. 1b",strP);
		ht.put("skin sensitisation potential is indicated",strP);
		ht.put("skin sensitisation sub-category 1a",strP);
		ht.put("skin sensitiser",strP);
		ht.put("skin sensitiser (category 1b)",strP);
		ht.put("skin sensitiser 1b",strP);
		ht.put("skin sensitiser cat. 1b",strP);
		ht.put("skin sensitiser category 1b",strP);
		ht.put("skin sensitiser, category 1b",strP);
		ht.put("skin sensitising (category 1b)",strP);
		ht.put("skin sensitizer",strP);
		ht.put("skin sensitizer 1b",strP);
		ht.put("skin sensitizer 1b.",strP);
		ht.put("skin sensitizer category 1b",strP);
		ht.put("skin sensitizing",strP);
		ht.put("strong sensitizer",strP);
		ht.put("the ec 1.4 value calculated is 28.80%.",strP);		
		ht.put("clp/eu ghs criteria are met, category 1b classification is required",strP);
		ht.put("skin sensitizer cat.1b (clp regulation ec no. 1272/2008)",strP);
		ht.put("clp classified as 1b sensitizer",strP);
		ht.put("eu criteria: skin sensitiser (category 1)",strP);
		ht.put("skin sensitiser.",strP);
		ht.put("skin sens. 1b, h317. classification",strP);
		ht.put("skin sensitising potential",strP);
		ht.put("sensitising category 1",strP);
		ht.put("sensitizer (category 1b)",strP);
		ht.put("eu criteria: category 1b. may cause an allergic skin reaction (h317)",strP);
		ht.put("skin sensitizer, category 1b",strP);
		ht.put("the test item was considered to be a sensitizer under the conditions of the test.",strP);
		ht.put("skin sensitiser according regulation (ec) no. 1272/2008 and its amendments.",strP);
		ht.put("skin sensitiser (cat 1) (clp regulation ec no. 1272/2008)",strP);

		
		return ht;
	}
}
