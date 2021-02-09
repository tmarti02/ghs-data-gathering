package gov.epa.ghs_data_gathering.GetData;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Vector;

import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.DataFormatter;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.openscience.cdk.AtomContainer;
import org.openscience.cdk.DefaultChemObjectBuilder;
import org.openscience.cdk.formula.MolecularFormula;
import org.openscience.cdk.tools.manipulator.MolecularFormulaManipulator;


import gov.epa.ghs_data_gathering.Utilities.Utilities;

/**
 * @author Todd Martin
 *
 */



public class EchemportalEyeIrritation {
	public static final String scoreAmbiguous="-1";
	public static final String scorePositive="1";
	public static final String scoreNegative="0";

	
	
	Hashtable<String,String>getIOR_Dictionary() {
		Hashtable <String,String>ht=new Hashtable<>();
		
		String strA=scoreAmbiguous;
		String strN=scoreNegative;
		String strP=scorePositive;
		String scoreMild=strN;//assign mild to negative result for irritation

		ht.put("\"practically non-irritating\"",scoreMild);		
		ht.put("slightly irritating to eyes, but insufficient for classification.",scoreMild);
		ht.put("mild irritant",scoreMild);
		ht.put("mild irritant (class 4 on a 1 to 8 scale)",scoreMild);
		ht.put("mild irritation",scoreMild);
		ht.put("mildly irritating",scoreMild);
		ht.put("mildly irritating to eyes",scoreMild);
		ht.put("minimal irritant",scoreMild);
		ht.put("minimal irritant (class 3 on a 1 to 8 scale)",scoreMild);
		ht.put("minimal irritant, class 3 on a 1 to 8 scale",scoreMild);
		ht.put("minimally irritating",scoreMild);
		ht.put("category 2b (mildly irritating to eyes)",scoreMild);
		ht.put("irritant effects, but not classifiable",scoreMild);
		ht.put("slight irritating",scoreMild);
		ht.put("slightly irritating",scoreMild);
		ht.put("very slight irritant",scoreMild);
		ht.put("very slight irritation",scoreMild);
		ht.put("very slight transient irritation in opinion of study director",scoreMild);
		ht.put("irritating to eyes, but insufficient for classification.",scoreMild);
		ht.put("slightly irritating to eyes, but insufficient for classification.",scoreMild);
		ht.put("practically non-irritating",scoreMild);


		ht.put("some evidence of irritation seen",strA);
		ht.put("an approx. 41% solution (2.5 dilution) of the substance (solid content: 16%) does not need to be classified for eye irritation",strA);
		ht.put("annex i of the clp regulation (1272/2008/ec)",strA);
		ht.put("appropriate classification can not be determined.",strA);
		ht.put("classification not possible",strA);
		ht.put("data inadequate for conclusive judgement",strA);
		ht.put("data insufficient for classification purposes",strA);
		ht.put("due to secondary toxicity dcm could not be classified regarding eye irritation.",strA);
		ht.put("expected as not or weak irritating",strA);
		ht.put("ghs criteria not met a positive irritant to the eye",strA);
		ht.put("low irritation potential",strA);
		ht.put("not clasified under clp  the test item produced a maximum group mean score of 12.0 out of a possible maximum of 110 and was considered to be a mild irritant (class 4 on a 1 to 8 scale) to the rabbit eye",strA);
		ht.put("not classified",strA);
		ht.put("not classified classified",strA);
		ht.put("not irritating in the opinion of the study director",strA);
		ht.put("study cannot be used for classification",strA);
		ht.put("study cannot be used for classification of the registered substance due to dillution applied",strA);
		ht.put("study cannot be used for classification the test substance was tested in formulation at 3% and not pure",strA);
		ht.put("study cannot be used for classification the test substance was used diluted at 1% in vehicle. the results cannot be used for classification because pure substance should be used",strA);
		ht.put("the aqueous solution does not have any classification/labelling requirements; for the concentrated test material there are no test results available, therefore a final classification for this quality can not be made.",strA);
		ht.put("the slight erythema was reversible, resolving by 48 hours post administration of the test substance.   the scores observed for cunjunctival erythema would not lead to a classification under eu-clp (regulation (ec) 1272/2008).",strA);
		ht.put("toxicity category iv",strA);
		ht.put("transient, mild conjunctival irritation only",strA);
		
		
		ht.put("\"not classified\" according clp",strN);
		ht.put("clp criteria not met",strN);
		ht.put("clp criteria not met does not have to be classified",strN);
		ht.put("clp criteria not met does not need to be classified",strN);
		ht.put("clp/ eu ghs criteria not met, no classification required",strN);
		ht.put("clp/eu ghs criteria are not met, no classification required",strN);
		ht.put("clp/eu ghs criteria not met, no classification",strN);
		ht.put("clp/eu ghs criteria not met, no classification required",strN);
		ht.put("clp/ghs criteria not met; no classification required",strN);
		ht.put("criteria for classification as an eye irritant not met",strN);
		ht.put("does not meet the criteria for classification",strN);
		ht.put("eu clp criteria not classified",strN);
		ht.put("ghs criteria not met",strN);
		ht.put("ghs criteria not met clp implementation.",strN);
		ht.put("ghs criteria not met does not meet the criteria for classification",strN);
		ht.put("ghs criteria not met eu clp",strN);
		ht.put("ghs criteria not met eu criteria not met",strN);
		ht.put("ghs criteria not met eu criteria.",strN);
		ht.put("ghs criteria not met no classification",strN);
		ht.put("ghs criteria not met non irritating",strN);
		ht.put("ghs criteria not met not classified",strN);
		ht.put("ghs criteria not met not classified under regulation 1272/2008/ec",strN);
		ht.put("ghs criteria not met not eye irritating",strN);
		ht.put("ghs criteria not met not irritating",strN);
		ht.put("ghs criteria not met not irritating to the eyes.",strN);
		ht.put("ghs criteria not met regulation (ec) 1272/2008",strN);
		ht.put("ghs criteria not met the test item has not to be classified for eye irritation",strN);
		ht.put("no classified",strN);
		ht.put("no labeling required",strN);
		ht.put("no significant or irriversible demage to the rabbit eye",strN);
		ht.put("non irritant",strN);
		ht.put("non irritating",strN);
		ht.put("not an eye irritant",strN);
		ht.put("not an eye irritant.",strN);
		ht.put("not an eye irritant. in accordance regulation (ec) no. 1272/2008 and its amendments.",strN);
		ht.put("not classified (clp regulation ec no. 1272/2008)",strN);
		ht.put("not classified acc. 2001/59/ec; eye irrit. cat.2 acc. 1272/2008",strN);
		ht.put("not classified as an irritant",strN);
		ht.put("not classified as eye irritant (clp regulation ec no. 1272/2008)",strN);
		ht.put("not classified criteria used for interpretation of results:",strN);
		ht.put("not classified under regulation 1272/2008/ec.",strN);
		ht.put("not classified,",strN);
		ht.put("not classified.",strN);
		ht.put("not irritant",strN);
		ht.put("not irritating",strN);
		ht.put("not irritating not classified as irritant",strN);
		ht.put("not irritating regulation (ec) no 1272/2008 on classification, labelling  and packaging (clp) of substances and mixtures",strN);
		ht.put("not irritating under eu clp (ec 1272/2008).",strN);
		ht.put("not irritating under eu dangerous substances directive 67/548/eec or clp.",strN);
		ht.put("not irritating, clp criteria not met.",strN);
		ht.put("not irritating, see explanation in conclusion expert judgment",strN);
		ht.put("not irritating.",strN);
		ht.put("not irrritating",strN);
		ht.put("the substance is not irritating",strN);
		
				
		ht.put("cat 1 or cat 2 (reversibility could not be evaluated)",strP);
		ht.put("cat. 2",strP);
		ht.put("category 1",strP);
		ht.put("category 1 (irreversible effects on the eye)",strP);
		ht.put("category 1 as per clp criteria",strP);
		ht.put("category 2",strP);
		ht.put("category 2 (irritating to eyes)",strP);
		ht.put("category 2 (irritating)",strP);
		ht.put("category 2 according clp",strP);
		ht.put("category 2: irritating to eyes,",strP);
		ht.put("category 2a (irritating to eyes)",strP);
		ht.put("category i (irreversible effects on the eye)",strP);
		ht.put("classified",strP);
		ht.put("classified as cat. 1 (serious eye damage) (clp regulation ec no. 1272/2008)",strP);
		ht.put("classified as irritating to eyes (category 2)",strP);
		ht.put("classified for serious eye damage (category 1)",strP);
		ht.put("clear evidence of irritation seen in 3/6 test (unwashed eyes) animals.",strP);
		ht.put("clp/eu ghs category 1 (h318)",strP);
		ht.put("clp/eu ghs criteria are met, category 1 (irreversible effects on the eye) classification required",strP);
		ht.put("corrosive",strP);
		ht.put("corrosive to the eye (cat. 1).",strP);
		ht.put("eu clp category 2 (irritating to eyes",strP);
		ht.put("extremely irritant",strP);
		ht.put("extremely irritating",strP);
		ht.put("eye cat. 2, h319 at a concentration of 70%",strP);
		ht.put("eye dam. 1 (h318),",strP);
		ht.put("eye dam. 1, h318",strP);
		ht.put("eye damage (category 1)",strP);
		ht.put("eye damage 1 (h318 : causes serious eye damage)",strP);
		ht.put("eye damage 1, h318. classification",strP);
		ht.put("eye damage cat. 1",strP);
		ht.put("eye irrit 2 (h319),",strP);
		ht.put("eye irrit. 2",strP);
		ht.put("eye irrit. 2 (h319)",strP);
		ht.put("eye irrit. 2 (h319),",strP);
		ht.put("eye irrit. 2, h319. classification",strP);
		ht.put("eye irrit. cat. 2",strP);
		ht.put("eye irritation 2, h319. classification",strP);
		ht.put("irreversible effects on the eye (category 1)",strP);
		ht.put("irritant",strP);
		ht.put("irritating",strP);
		ht.put("irritating (",strP);
		ht.put("irritating to eyes",strP);
		ht.put("irritating to eyes (category 2)",strP);
		ht.put("irritating to eyes, category 2",strP);
		ht.put("irritating.",strP);
		ht.put("moderate to severe irritant",strP);
		ht.put("non-corrosive but causes irreversible eye effects",strP);
		ht.put("not clear if category i or ii, as there is no data about the reversibility",strP);
		ht.put("ocular irritant, but not corrosive",strP);
		ht.put("r41: risk of serious damage to eyes",strP);
		ht.put("risk of serious damage to eyes",strP);
		ht.put("risk of serious damage to eyes.",strP);
		ht.put("risk of serious damage to the eyes (r41)",strP);
		ht.put("serious damage to the eyes",strP);
		ht.put("severe damage to eyes",strP);
		ht.put("severe eye damage",strP);
		ht.put("severe irritant",strP);
		ht.put("severe irritating",strP);
		ht.put("severe irritation",strP);
		ht.put("severe local irritation",strP);
		ht.put("severe ocular damage",strP);
		ht.put("severely irritant",strP);
		ht.put("slight to moderate irritation (fully reversible by day 10)",strP);
		ht.put("slight to moderate irritation which is fully reversible on day 7",strP);
		ht.put("the test item causes severe damage to the eye;",strP);
		ht.put("the test item produced a maximum group mean score of 34.5 and was classified as a moderate irritant (class 5 on a 1 to 8 scale)",strP);
		ht.put("the test item was classified as irritating to eyes (category 2)",strP);

		return ht;
	}
	
	
	
	public static void main(String[] args) {
		EchemportalEyeIrritation ssr=new EchemportalEyeIrritation();
		
		String endpoint="eye irritation";
		String date="2019-09-17";
		String folder="AA Dashboard\\Data\\ECHA\\REACH_dossier_data\\eChemPortal\\"+endpoint+"\\";
		String filepathExcel=folder+date+" "+endpoint+".xls";
		String filepathText=folder+date+" "+endpoint+" parsed.txt";

//		Vector<RecordEchemportal2>records=EChemPortalParse.parseExcelEchemportalQueryResult(filepathExcel);
//		RecordEchemportal2.writeToFile(filepathText, records);		
		
		Hashtable<String,String>htDict=ssr.getIOR_Dictionary();
		
	}
}