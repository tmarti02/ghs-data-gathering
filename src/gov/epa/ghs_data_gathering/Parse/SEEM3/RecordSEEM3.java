package gov.epa.ghs_data_gathering.Parse.SEEM3;

import java.util.HashMap;
import java.util.Vector;

import com.google.gson.Gson;
import com.google.gson.JsonObject;

import gov.epa.api.ExperimentalConstants;
import gov.epa.api.ScoreRecord;
import gov.epa.exp_data_gathering.parse.ExcelSourceReader;
import gov.epa.ghs_data_gathering.Parse.ParseSIN;

public class RecordSEEM3 {
	
	public String dsstox_substance_id;
	public String CAS;
	public String Substance_Name;
	public String delta_Diet_pred;
	public String delta_Res_pred;
	public String delta_Pest_pred;
	public String delta_Indust_pred;
	public String seem3;
	public String seem3_u95;
	public String Pathway;
	public String AD;


	public static final String[] fieldNames = {"dsstox_substance_id","CAS","Substance_Name",
			"delta_Diet_pred","delta_Res_pred","delta_Pest_pred","delta_Indust_pred",
			"seem3","seem3_u95","Pathway","AD"};
	
	public static final String lastUpdated = "08/25/2021";//email from John Wambaugh
	public static final String sourceName = ScoreRecord.strSourceSEEM3;
	
	private static final String fileName = "Ring2018-ExposurePreds.xlsx";
	
	public static Vector<JsonObject> parseRecordsFromExcel() {
		String mainFolderPath="AA Dashboard\\Data";
		ExcelSourceReader esr = new ExcelSourceReader(fileName, mainFolderPath,sourceName);
		
//		esr.createClassTemplateFiles();		
		
		HashMap<Integer,String> hm = esr.generateDefaultMap(fieldNames, 0);
		Vector<JsonObject> records = esr.parseRecordsFromExcel(hm, 2);
		return records;
		
		
		/**
		 * dsstox_substance_id	CAS	Substance_Name	delta.Diet.pred	delta.Res.pred	delta.Pest.pred	delta.Indust.pred	seem3	seem3.u95	Pathway	AD
DTXSID00110012	47857-64-7	4-(2-Chlorophenyl)-3-[3-[4-(2-chlorophenyl)benzo[f]quinolin-3(4H)-ylidene]-1-propenyl]-benzo[f]quinolinium	0.365	0.4012	0.3212	0.4672	2.96E-07	NA	Unknown	0
DTXSID00110017	64023-93-4	(+)-Phenazocine	0.1624	0.308	0.0964	0.0664	1.11E-07	NA	Unknown	0
DTXSID00110032	5530-52-9	6-Methoxy-3-methyl-2(3H)-benzothiazolimine hydroiodide (1:1)	0.238	0.3266	0.7084	0.2144	1.45E-07	0.000150444	Unknown	1
DTXSID00110037	57754-86-6	Nisoxetine hydrochloride	0.1302	0.3404	0.3268	0.104	1.33E-07	NA	Unknown	0
DTXSID00110052	6114-18-7	Ethyl (9E)-octadec-9-enoate	0.6918	0.6196	0.7944	0.4368	3.50E-06	0.000491135	Unknown	1
DTXSID00110057	632-68-8	C.I.Acid Red 94	0.2178	0.3398	0.366	0.6256	1.60E-07	5.16E-05	Unknown	1
DTXSID00142853	1000-63-1	1-Tert-butoxybutane	0.671	0.8586	0.1658	0.9318	6.84E-06	0.000172381	Residential, Industrial	1
DTXSID00142858	100009-01-6	Glycinamide-beta-carboline-3-carboxylate methyl ester	0.1368	0.2806	0.3356	0.129	1.02E-07	NA	Unknown	0
DTXSID00142878	1000296-71-8	PF-04064900	0.2852	0.3368	0.2196	0.2546	1.83E-07	NA	Unknown	0
DTXSID00142893	100055-08-1	"1-Pentanol, 5-(p-aminophenoxy)-"	0.3476	0.4072	0.4874	0.5486	2.20E-07	0.000148809	Unknown	1
DTXSID00142898	100063-56-7	2-(((2-((Hydroxymethyl)(2-((1-oxoisononyl)amino)ethyl)amino)ethyl)amino)carbonyl)benzoic acid	0.1772	0.282	0.2366	0.265	1.28E-07	NA	Unknown	0
DTXSID00142914	100078-95-3	"1,2,4-Triazolo(4,3-b)pyridazine, 7-(4-chlorophenyl)-3-methyl-"	0.315	0.2688	0.7994	0.2166	1.34E-07	9.30E-05	Unknown	1
DTXSID00142919	100079-23-0	"Benzamide, N-heptyl-3,4,5-trihydroxy-"	0.2808	0.2852	0.4452	0.38	1.26E-07	NA	Unknown	0
DTXSID00142934	100098-72-4	"Oxazole, 4,5-dihydro-2-(((3-bromophenyl)methyl)thio)-4,4-dimethyl-, hydrochloride"	0.2946	0.3366	0.8186	0.18	1.58E-07	8.83E-05	Pesticide	1
DTXSID00142939	1001-55-4	(Acetyloxy)acetonitrile	0.4728	0.6144	0.2558	0.9352	2.17E-06	0.003160713	Industrial	1
DTXSID00142954	100113-05-1	"4-Acridinecarboxamide, 9-amino-2-chloro-N-(2-(dimethylamino)ethyl)-, dihydrochloride"	0.071	0.2116	0.2096	0.2006	7.96E-08	NA	Unknown	0

		 */
		
	}
	
	
	public static void main(String[] args) {
		Vector<JsonObject> records =parseRecordsFromExcel();
		
		Gson gson= new Gson();
		for (JsonObject record:records) {
			
			
			RecordSEEM3 r = gson.fromJson(record.toString(),RecordSEEM3.class);
			
			System.out.println(r.CAS+"\t"+r.delta_Indust_pred);
		}
		
	}
	
}
