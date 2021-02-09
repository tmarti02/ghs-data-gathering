package gov.epa.ghs_data_gathering.GetData;

import java.util.Hashtable;
import java.util.Vector;

/**
 * Class to parse skin irr data from echemportal spreadsheet
 * @author Todd Martin
 *
 */



public class EchemportalSkinIrritation {

	
	Hashtable<String,String>getIOR_Dictionary() {
		Hashtable <String,String>ht=new Hashtable<>();
		
		String strA=EChemPortalParse.scoreAmbiguous;
		String strN=EChemPortalParse.scoreNegative;
		String strP=EChemPortalParse.scorePositive;
		String scoreMild=strN;//assign mild to negative result for irritation

		ht.put("category 3 (mild irritant)",scoreMild);
		ht.put("category 3 (mild irritant) r38",scoreMild);
		ht.put("mild irritant",scoreMild);
		ht.put("mild irritant to rabbit skin",scoreMild);
		ht.put("mild irritant to rabbit skin.",scoreMild);
		ht.put("mildly irritating",scoreMild);
		ht.put("minimally irritating",scoreMild);
		ht.put("very mild skin irritant",scoreMild);
		ht.put("very slight irritational effects which do not lead to classification",scoreMild);
		ht.put("very slightly irritant",scoreMild);
		ht.put("practically  non-irritant",scoreMild);
		ht.put("irritation observed, but not sufficient for classification.",scoreMild);
		
		ht.put("as test item a 65 % solution in solvent naphtha was used; the induced irritating effects can be interpreted as solvent driven",strA);
		ht.put("as test item a 65 % solution in solvent naphtha was used; the induced slight irritating effects can be interpreted as solvent driven",strA);
		ht.put("borderline result between no classification and skin cat. 2 (h315)",strA);
		ht.put("classification not possible",strA);
		ht.put("data cannot be used for classification",strA);
		ht.put("data not sufficient for classification",strA);
		ht.put("development of erythema basically possible, however this could not be evaluated due to staining effects caused by the test item.",strA);
		ht.put("inconclusive",strA);
		ht.put("no individual animal data provided so there are insufficient data for evaluation of classification",strA);
		ht.put("non-corrosive",strA);
		ht.put("not classifiable",strA);
		ht.put("not classified as an irritant under clp. classified as an irritant under dsd.",strA);
		ht.put("not classified for hydrated form tmt 55; inconclusive for anhydrous tmt",strA);
		ht.put("not corrosive",strA);
		ht.put("not used for classification purposes",strA);
		ht.put("other:",strA);		
		ht.put("r38 under directive 67/518/eec for dangerous substances and directive 1999/45/ec for preparations. not classified under the new regulation (ec) 1272/2008 on classification, labeling and packaging of substances and mixtures (clp).",strA);
		ht.put("study cannot be used for classification",strA);
		ht.put("study cannot be used for classification : aqueous solution of 15% tmt (tmt 15)",strA);
		ht.put("study design not appropriate to classify skin irritation.",strA);
		ht.put("supporting study.",strA);
		ht.put("the test item produced a primary irritation index of 0.3 and was classified as a mild irritant to rabbit skin",strA);
		ht.put("unable to classify by clp (ec 1272/2008)",strA);
		ht.put("unconclusive",strA);
		ht.put("unreliable",strA);

		
		ht.put("\"not classified\" according clp",strN);
		ht.put("\"not irritating\" according clp",strN);
		ht.put("category- not classified as skin irritant",strN);
		ht.put("classification not required",strN);
		ht.put("clp criteria not met",strN);
		ht.put("clp criteria not met - does not have to be classified",strN);
		ht.put("clp criteria not met does not have to be classified",strN);
		ht.put("clp criteria not met not classified",strN);
		ht.put("clp/ eu ghs criteria not met, no classification required",strN);
		ht.put("clp/eu ghs criteria are not met, no classification required",strN);
		ht.put("clp/eu ghs criteria not met, no classification",strN);
		ht.put("clp/eu ghs criteria not met, no classification required",strN);
		ht.put("clp/ghs criteria not met; no classification required",strN);
		ht.put("eu ghs criteria not met",strN);
		ht.put("ghs criteria not met",strN);
		ht.put("ghs criteria not met / consequently classification",strN);
		ht.put("ghs criteria not met 4",strN);
		ht.put("ghs criteria not met c",strN);
		ht.put("ghs criteria not met clp implementation.",strN);
		ht.put("ghs criteria not met does not need to be classified",strN);
		ht.put("ghs criteria not met eu clp",strN);
		ht.put("ghs criteria not met eu criteria not met",strN);
		ht.put("ghs criteria not met eu criteria.",strN);
		ht.put("ghs criteria not met non skin irritating",strN);
		ht.put("ghs criteria not met not irritant",strN);
		ht.put("ghs criteria not met not irritating",strN);
		ht.put("ghs criteria not met not irritating:",strN);
		ht.put("ghs criteria not met not skin irritating",strN);
		ht.put("ghs criteria not met oecd ghs",strN);
		ht.put("ghs criteria not met regulation (ec) 127272008",strN);
		ht.put("ghs criteria not met the test item has not to be classified for skin irritation",strN);
		ht.put("ghs criteria not met the test item has not to be classified for skin irritation/corrosion",strN);
		ht.put("negative results in skin irritation studies in animals and no positive results in humans result in non classification for irritancy and corrosion.",strN);
		ht.put("no classification is required",strN);
		ht.put("non irritating (",strN);
		ht.put("non-irritant",strN);
		ht.put("not a skin irritant",strN);
		ht.put("not categorized",strN);
		ht.put("not classified",strN);
		ht.put("not classified (clp regulation ec no. 1272/2008)",strN);
		ht.put("not classified (ghs); classified xi, r38 (dsd)",strN);
		ht.put("not classified as a skin irritant",strN);
		ht.put("not classified as an irritant",strN);
		ht.put("not classified as irritating to skin under clp",strN);
		ht.put("not classified through eu clp; not irritating",strN);
		ht.put("not classified under eu criteria; not irritating",strN);
		ht.put("not classified under eu dsd or clp; not irritating.",strN);
		ht.put("not classified under eu; not irritating",strN);
		ht.put("not classified under oecd ghs",strN);
		ht.put("not classified under oecd ghs; not irritating",strN);
		ht.put("not classified under oecd; not irritating",strN);
		ht.put("not classified,",strN);
		ht.put("not classified.",strN);
		ht.put("not irritant",strN);
		ht.put("not irritatiing",strN);
		ht.put("not irritating",strN);
		ht.put("not irritating at a concentration of 10 and 50%",strN);
		ht.put("not irritating not classified as irritant",strN);
		ht.put("not irritating under eu dangerous substances directive 67/548/eec or clp.",strN);
		ht.put("not irritating using clp criteria",strN);
		ht.put("not irritating; no classification",strN);
		ht.put("not skin irritant",strN);
		ht.put("the substance does not meet the criteria for classification as a skin irritant",strN);
		
		
		ht.put("borderline between skin irritation (skin irrit. 2) and skin corrosion (skin cat. 1)",strP);
		ht.put("category 1 (corrosive)",strP);
		ht.put("category 1a (corrosive)",strP);
		ht.put("category 1b (corrosive)",strP);
		ht.put("category 1c",strP);
		ht.put("category 1c (corrosive)",strP);
		ht.put("category 2",strP);
		ht.put("category 2 (irritant)",strP);
		ht.put("category 2 (irritant) r38",strP);
		ht.put("category 2 (irritating)",strP);
		ht.put("category 2 irritant annex i of the clp regulation (1272/2008/ec).",strP);
		ht.put("classification as skin corr. 1c, h314 required",strP);
		ht.put("classified irritant",strP);
		ht.put("clp/eu ghs category 1b (h314)",strP);
		ht.put("clp/eu ghs category 2 (h315)",strP);
		ht.put("clp/eu ghs criteria are  met, classification with category 2 (irritant) is required",strP);
		ht.put("corrosive",strP);
		ht.put("corrosive r34",strP);
		ht.put("eu clp criteria skin irritating (category 2)",strP);
		ht.put("eu crieria category 2: irritant",strP);
		ht.put("irritating",strP);
		ht.put("irritating (aqueous solution of 57 % glycolic acid and 12 % nacl)",strP);
		ht.put("irritating r38",strP);
		ht.put("irritating risk phrase: r38",strP);
		ht.put("moderate dermal irritant",strP);
		ht.put("moderately irritating (mild skin irritant at 10% glycolic acid concentration. moderate skin irritant at 30% or 40 % glycolic acid concentrations.)",strP);
		ht.put("skin corr. 1b (h314),",strP);
		ht.put("skin corr. 1c, h314",strP);
		ht.put("skin irrit 2, h315. classification",strP);
		ht.put("skin irrit. 2",strP);
		ht.put("skin irrit. 2, h315",strP);
		ht.put("skin irrit. 2, h315. classification",strP);
		ht.put("skin irrit. cat 2",strP);
		ht.put("skin irrit. cat 2 acording to regulation (ec) no 1272/2008",strP);
		ht.put("skin irrit. cat. 2 at 100% test substance",strP);
		ht.put("skin irritant cat 2",strP);
		ht.put("the study results are the evidence for skin corrosion of n-nonanoic acid is borderline and not clear cut, because the corrosive effcts observed in 48 h, were reversibel after 14 d observation",strP);



		return ht;
	}
	
	
	public static void main(String[] args) {
		EchemportalSkinIrritation ssr=new EchemportalSkinIrritation();
		
//		String folder="AA Dashboard\\Data\\ECHA\\REACH_dossier_data\\ECHEMPORTAL DATA From Jeremy";		
//		String filepath=folder+"\\SkinSensitizationNewHeader.tsv";		

		String endpoint="skin irritation";
		String date="2019-09-16";
		String folder="AA Dashboard\\Data\\ECHA\\REACH_dossier_data\\eChemPortal\\"+endpoint+"\\";
		String filepathExcel=folder+date+" "+endpoint+".xls";
		String filepathText=folder+date+" "+endpoint+" parsed.txt";

//		Vector<RecordEchemportal2>records=EChemPortalParse.parseExcelEchemportalQueryResult(filepathExcel);
//		RecordEchemportal2.writeToFile(filepathText, records);		
		
		String filepathGood=folder+"echemportal "+endpoint+" good.txt";
		String filepathBad=folder+"echemportal "+endpoint+" bad.txt";		
		String scifinderFilePath=Scifinder.folderScifinder+"\\scifinder_chemical_info.txt";
		String ECHACASlookup="AA Dashboard\\Data\\ECHA\\REACH_dossier_data\\echa cas lookup.txt";
		
		Hashtable<String,String>htDict=ssr.getIOR_Dictionary();
		
//		boolean printUniqueIORs=false;
//		EChemPortalParse.goThroughRecords(filepathText, filepathGood, filepathBad, scifinderFilePath, ECHACASlookup, htDict,printUniqueIORs);
//		
//		String filepathGoodNoDuplicates=folder+"\\echemportal "+endpoint+" good-no duplicates.txt";
//		EChemPortalParse.omitDuplicateRecords(filepathGood, filepathGoodNoDuplicates);
//		
//		String filepathOmitBadScifinder=folder+"\\echemportal "+endpoint+" good-no duplicates-omit bad scifinder.txt";		
//		EChemPortalParse.omitBadScifinderRecords(filepathGoodNoDuplicates, filepathOmitBadScifinder);
	}
}