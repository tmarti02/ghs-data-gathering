package gov.epa.ghs_data_gathering.GetData.ECHA_IUCLID;

import java.util.Hashtable;

/**
 * Class that simulates the class structure in IUCLID json files. It makes it easier to visualize what data we have access to
 * 
 * IUCLID_Document2 is used instead for revised json files with phrases and substance since easier to load into 
 * Java class 
 * 
 * *** Note: This class will need to be revised to add fields which show up in the documents for different
 *           tox endpoints if you want to load a json file into Java object instead of a JsonObject 
 * 
 * 
 * @author Todd Martin
 *
 */
public class IUCLID_Document {

	//	Object[] objects= {Object1,Object2};

	Object []objects= {new Part1(),new Part2()};

	public class Part1{
		public String key;
		public String definition;
		public String parentKey;
		public String parentDefinition;
		public long order;
		public String name;
		public String[] attachments;
		public String createdOn;
		public String modifiedOn;
		
		public ReferenceSubstance ReferenceSubstance;//added by TMM to store details for ref substance for dossier
	}

	public class Part2{
		public AdministrativeData AdministrativeData;
		public DataSource DataSource;
		public MaterialsAndMethods MaterialsAndMethods;
		public ResultsAndDiscussion ResultsAndDiscussion;	
		
		public class AdministrativeData {
			public FieldWithCode Endpoint;//needs to be capital "E"ndpoint for it to work
			public FieldWithCode StudyResultType;
			public FieldWithCode PurposeFlag;
			public boolean robustStudy;
			public boolean usedForClassification;
			public boolean usedForMSDS;
			public FieldWithCode Reliability;
		}
		
		public class DataSource {
			public String [] Reference;
		}
		
		
		public class MaterialsAndMethods {
			public Guideline Guideline[];
			
			public FieldWithCode TestType;
			public FieldWithCode LimitTest;
			
			public TestMaterials TestMaterials;
			public InVivoTestSystem InVivoTestSystem;
			//TODO- may need to add more subclasses similar to StudyDesignInVivoNonLLNA depending on the endpoint you are looking at
			
			public class Guideline {
				public String uuid;
				public FieldWithCode Qualifier;
				public FieldWithCode Guideline;
				public FieldWithCode Deviation;
				
			}
			
			public class TestMaterials {
				public String TestMaterialInformation;
				public ReferenceSubstance [] ReferenceSubstances;//added by TMM to store details for chemical(s) in test material
			}
			
			
			public class InVivoTestSystem {
				public TestAnimals TestAnimals;
				public StudyDesignInVivoNonLLNA StudyDesignInVivoNonLLNA;
				
				public class TestAnimals {
					public FieldWithCode Species;
					public FieldWithCode Strain;
					public FieldWithCode Sex;				
				}
				
				public class StudyDesignInVivoNonLLNA {
					public InductionRecord[] Induction;
					public InductionRecord[] Challenge;
					
					public class InductionRecord {
						public String uuid;
						public FieldWithCode Route;
					}
				}
			}
			
		}
		
		public class ResultsAndDiscussion {
			
			public TraditionalSensitisationTest TraditionalSensitisationTest; // for skin sensitisation document results
			public EffectLevel [] EffectLevels; //for acute Mammalian Toxicity results
			
			public class EffectLevel {
				public String uuid;
				public boolean KeyResult;
				public FieldWithCode Sex;
				public FieldWithCode Endpoint;
				public FieldWithRange EffectLevel;
				public FieldWithRange cl;
			}
			
			
			public class TraditionalSensitisationTest {
				public TestResult[] ResultsOfTest;
				
				public class TestResult {
					public String uuid;
					public FieldWithCode Reading;
					public float HoursAfterChallenge;
					public FieldWithCode Group;
					public float NoWithReactions;
					public float TotalNoInGroup;
					public FieldWithCode RemarksOnResults;
				}
				
			}
		}
	}


	public class ReferenceSubstance {
		public String EC_Number;
		public String IUPACName;
		public String [] Synonyms;
		public String CAS;
		public String MolecularFormula;
		public  String SmilesNotation;
		public String InChl;
		public FieldWithRange MolecularWeightRange;
		
	}
	
	public class FieldWithRange {
		public String lowerValue;
		public String upperValue;
		public FieldWithCode unit;
	}
	
	public class FieldWithCode {
		public String code;
		public String other;
	}

}
