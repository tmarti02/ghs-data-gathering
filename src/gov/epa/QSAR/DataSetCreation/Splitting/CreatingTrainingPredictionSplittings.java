package gov.epa.QSAR.DataSetCreation.Splitting;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Collections;
import java.util.Random;
import java.util.Vector;

import gov.epa.QSAR.DataSetCreation.api.RecordQSAR;
import gov.epa.QSAR.DataSetCreation.api.RecordsQSAR;
//import gov.epa.QSAR.build.buildAllModels.InstancesFromString;
import gov.epa.database.SQLite_CreateTable;
import gov.epa.database.SQLite_Utilities;
import weka.core.Instances;
import weka.core.Instance;

public class CreatingTrainingPredictionSplittings {
	
	public static final String tableNameSplittings="Splittings";
	
	
	public static final String choiceKennardStoneMaxMin="KennardStoneMaxMin";
	public static final String choiceSphereMaxMin="SphereMaxMin2";
	public static final String choiceOPERA="OPERA";
	
	
	static String [] fieldNames= {"DSSTOX_Structure_Id","property_name","splitting","t_p"};


	static void addSplittingRows (Connection conn,String splitting, String set,RecordsQSAR records) {
		try {
			conn.setAutoCommit(false);
			
			String s=SQLite_CreateTable.create_sql_insert(fieldNames, tableNameSplittings);

			int counter = 0;
			int batchCounter = 0;
			PreparedStatement prep = conn.prepareStatement(s);
			
			
			for (RecordQSAR rec:records) {				
				counter++;

//				if (counter%50000==0) System.out.println("Added "+counter+" entries...");

				String [] fieldValues=	new String [4];
				
				fieldValues[0]=rec.DSSTox_Structure_Id;
				fieldValues[1]=rec.property_name;
				fieldValues[2]=splitting;
				fieldValues[3]=set;
				
//				System.out.println(fieldValues[0]+"|"+fieldValues[1]+"|"+fieldValues[2]+"|"+fieldValues[3]);

				if (fieldValues.length!=fieldNames.length) {//probably wont happen now that list is based on names array
					System.out.println("Wrong number of values: "+fieldValues[0]);
					break;
				}
								
				for (int i = 0; i < fieldValues.length; i++) {
					if (fieldValues[i]!=null && !fieldValues[i].isBlank()) {
						prep.setString(i + 1, fieldValues[i]);
					} else {
						prep.setString(i + 1, null);
					}
				}
				
				try {
					prep.addBatch();
				} catch (Exception ex) {
					System.out.println("Failed to add: "+String.join(",", fieldValues));
				}
				
				if (counter % 1000 == 0) {

					int[] count=prep.executeBatch();
					batchCounter++;
					
//					for (int i=0;i<count.length;i++) System.out.println(i+"\t"+count[i]);				
					
				}

			}

			int[] count = prep.executeBatch();// do what's left
			conn.setAutoCommit(true);

		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	
	
	public static void addSplittingRow (Connection conn,String ID,String property_name,String splitting, String set) {
		try {

			String s=SQLite_CreateTable.create_sql_insert(fieldNames, tableNameSplittings);

			PreparedStatement prep = conn.prepareStatement(s);

			//				if (counter%50000==0) System.out.println("Added "+counter+" entries...");

			String [] fieldValues=	new String [4];

			fieldValues[0]=ID;
			fieldValues[1]=property_name;
			fieldValues[2]=splitting;
			fieldValues[3]=set;


			for (int i = 0; i < fieldValues.length; i++) {
				if (fieldValues[i]!=null && !fieldValues[i].isBlank()) {
					prep.setString(i + 1, fieldValues[i]);
				} else {
					prep.setString(i + 1, null);
				}
			}

			try {
				prep.addBatch();
				int[] count=prep.executeBatch();
			} catch (Exception ex) {
				System.out.println("Failed to add: "+String.join(",", fieldValues));
			}



		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	public static void addSplittingRowsUsingSetInQSARRecord (String dbpath,String splitting, RecordsQSAR records) {
		try {
			Connection conn=SQLite_Utilities.getConnection(dbpath);
			
			conn.setAutoCommit(false);
			
			String s=SQLite_CreateTable.create_sql_insert(fieldNames, tableNameSplittings);

			int counter = 0;
			int batchCounter = 0;
			PreparedStatement prep = conn.prepareStatement(s);
			
			
			for (RecordQSAR rec:records) {				
				counter++;

//				if (counter%50000==0) System.out.println("Added "+counter+" entries...");

				String [] fieldValues=	new String [4];
				
				fieldValues[0]=rec.DSSTox_Structure_Id;
				fieldValues[1]=rec.property_name;
				fieldValues[2]=splitting;
				
				if (rec.Tr_1_Tst_0==null) {
					System.out.println("No set for "+rec.DSSTox_Substance_Id);
					continue;
				}
				
				if (rec.Tr_1_Tst_0==1) fieldValues[3]="T"; 
				else if (rec.Tr_1_Tst_0==0) fieldValues[3]="P";
				
//				System.out.println(fieldValues[0]+"|"+fieldValues[1]+"|"+fieldValues[2]+"|"+fieldValues[3]);

				if (fieldValues.length!=fieldNames.length) {//probably wont happen now that list is based on names array
					System.out.println("Wrong number of values: "+fieldValues[0]);
					break;
				}
								
				for (int i = 0; i < fieldValues.length; i++) {
					if (fieldValues[i]!=null && !fieldValues[i].isBlank()) {
						prep.setString(i + 1, fieldValues[i]);
					} else {
						prep.setString(i + 1, null);
					}
				}
				
				try {
					prep.addBatch();
				} catch (Exception ex) {
					System.out.println("Failed to add: "+String.join(",", fieldValues));
				}
				
				if (counter % 1000 == 0) {

					int[] count=prep.executeBatch();
					batchCounter++;
					
//					for (int i=0;i<count.length;i++) System.out.println(i+"\t"+count[i]);				
					
				}

			}

			int[] count = prep.executeBatch();// do what's left
			conn.setAutoCommit(true);

		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	
	public static void createSphereExclusionSplitting(String property,Instances instances,String dbpath) {
//		double c = 13.25;//FHM LC50
//		double c = 9.5;//TP IGC50
//		double c = 11;//Rat LD50
		double c = 13.5;//BCF		
		int maxCount=5;//maximum count of chemicals inside sphere to add to training and test sets
		SphereExclusion.GoSphere2(instances, 2,choiceSphereMaxMin, c,maxCount,property,dbpath);
	}
	
	public static void createKennardStoneSplitting (String property,Instances instances, String dbpath) {
		String RationalDesignMethod=choiceKennardStoneMaxMin;
		double predFrac=0.2;
		int normMethod=2;
		KennardStone.GoKennardStone(property,instances, predFrac, normMethod, RationalDesignMethod, dbpath);

	}
	
	public static void setUpSplittingTable(String propertyName,String dbpath) {
		
		try {
			
			Connection conn=SQLite_Utilities.getConnection(dbpath);
			Statement stat=conn.createStatement();			
			conn.setAutoCommit(false);
			
			
			SQLite_CreateTable.create_table(stat, tableNameSplittings, fieldNames);
			SQLite_Utilities.deleteRecords(tableNameSplittings, "property_name", propertyName, stat);			
		
		} catch (Exception ex) {
			ex.printStackTrace();
		}
		
	}
	
	
	
	
	
	/**
	 * Randomly create N fold training and prediction files
	 * 
	 * @param NumFolds
	 * @param destFolder
	 * @param endpoint
	 * @param source
	 */
	public static void createNFoldPredictionSets(int NumFolds,String propertyName,RecordsQSAR records,String dbpath) {

		try {
			
			Connection conn=SQLite_Utilities.getConnection(dbpath);
			Statement stat=conn.createStatement();			
			conn.setAutoCommit(false);
							
//			createWeightingFile(instances, destFolder+"\\2d-1.txt");
			Collections.shuffle(records,new Random(1234));//randomly shuffle chemicals

			Vector<RecordsQSAR> v=new Vector(); // vector of array lists
			
			for (int i=0;i<NumFolds;i++) {
				v.add(new RecordsQSAR());
			}

			int NumInFold=(int)Math.floor((double)records.size()/(double)NumFolds);
			System.out.println("Number in each fold="+NumInFold);

			//Note: extra chemicals are lumped into last fold

			int fold=-1;
			
			for (int j=0;j<records.size();j++) {
				if (j%NumInFold==0) {
					if (fold<NumFolds-1) fold++;
				}
				//if (j%NumInFold==0) fold++;//use if last fold is allowed to have fewer chemicals				
//				System.out.println(j+"\t"+fold);
				RecordsQSAR recordsi=v.get(fold);
				
				if (recordsi.size()==0) {
					recordsi=new RecordsQSAR();
					recordsi.add(records.get(j));
					v.set(fold, recordsi);
				} else {
					recordsi.add(records.get(j));	
				}
			}

			for (int i=0;i<NumFolds;i++) {
				
				System.out.println("Creating files for fold "+(i+1));
				RecordsQSAR recordsPredi=v.get(i);	
				
//				System.out.println("# records in predi="+recordsPredi.size());
				
				addSplittingRows(conn, "rnd"+(i+1), "P", recordsPredi);
				
				RecordsQSAR instancesTraining=null;
				
				for (int j=0;j<NumFolds;j++) {
					if (i!=j) {
						RecordsQSAR recordsTrainingj=v.get(j);
						addSplittingRows(conn, "rnd"+(i+1), "T", recordsTrainingj);

					}
				}
				//TODO save values for each instances
			}
				
		} catch (Exception e) {
			e.printStackTrace();
		}
	}



	public static void createRandomAlcoholsSplitting(String property, Instances instances, String dbpath) {
		String descStart="As [+5 valence, one double bond]";
		
		int start=instances.attribute(descStart).index();
		
		int indexCH3=instances.attribute("-CH3 [aliphatic attach]").index();
		
		System.out.println(start+"\t"+indexCH3);
		
		int count=0;
		
		RecordsQSAR records=new RecordsQSAR();
		
		for (int j=0;j<instances.numInstances();j++) {
			
			Instance instance=instances.instance(j);
			
			double nCH3=instance.value(instances.attribute("-CH3 [aliphatic attach]").index());
			double nCH2=instance.value(instances.attribute("-CH2- [aliphatic attach]").index());
			double nCH=instance.value(instances.attribute("-CH< [aliphatic attach]").index());
			double nC=instance.value(instances.attribute(">C< [aliphatic attach]").index());
			double nOH=instance.value(instances.attribute("-OH [aliphatic attach]").index());
			
			double sumAlcoholFrags=nCH3+nCH2+nCH+nC+nOH;
			
			double sumAllFrags=0;
			for (int i=start;i<instances.numAttributes();i++) {
				sumAllFrags+=instance.value(i);
				
//				if (instance.value(i)!=0)
//					System.out.println(instance.stringValue(0)+"\t"+instances.attribute(i).name()+"\t"+instance.value(i));
			}
//			System.out.println(instance.stringValue(0)+"\t"+sumAllFrags+"\t"+sumAlcoholFrags);
			
			if (sumAllFrags>sumAlcoholFrags) continue;
		
			RecordQSAR rec=new RecordQSAR();
			rec.DSSTox_Structure_Id=instance.stringValue(0);
			rec.property_name=property;
			
			records.add(rec);
			count++;
			System.out.println(count+"\t"+instance.stringValue(0));
		}
		
		Collections.shuffle(records,new Random(1234));
		int NumInFold=(int)Math.floor((double)records.size()/(double)5);
		
		RecordsQSAR recordsPred= new RecordsQSAR();
		for (int i=1;i<=NumInFold;i++) {
			recordsPred.add(records.remove(0));
		}
		Connection conn=SQLite_Utilities.getConnection(dbpath);
		addSplittingRows(conn, "rnd-alcohols", "P", recordsPred);
		addSplittingRows(conn, "rnd-alcohols", "T", records);
		
	}
}
