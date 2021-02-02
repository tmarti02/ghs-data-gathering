package gov.epa.QSAR.DataSetCreation.Splitting;

import java.sql.Connection;
import java.util.Collections;
import java.util.Enumeration;
import java.util.Vector;

import gov.epa.database.SQLite_Utilities;
import weka.core.Instance;
import weka.core.Instances;

public class SphereExclusion {
	/**
		 * In this version the initial compound is added to the training set
		 * based on the maximum dissimilarity to the rest of the set instead of
		 * just choosing the one with the maximum activity
		 * 
		 * @param endpoint
		 * @param overallSet
		 * @param destFolder
		 * @param desc
		 * @param c
		 * @param NormMethod
		 */
		public static void GoSphere2(Instances instances, int NormMethod,String RationalDesignMethod,double c,int maxCount,String property_name,String dbpath) {
			
			try {

				Connection conn=SQLite_Utilities.getConnection(dbpath);
				
				
				instances = Utilities.removeBadDescriptors(instances);
	
				ChemicalCluster ccOverall = new ChemicalCluster(instances);
				
				if (NormMethod==1)Utilities.Normalize(ccOverall);
				else if (NormMethod==2) Utilities.Normalize2(ccOverall);
	
				boolean[] Used = new boolean[ccOverall.numInstances()];
	
				double[][] Distances = new double[ccOverall.numInstances()][ccOverall
						.numInstances()];
	
				// calculate Distances:
	
				for (int i = 0; i < ccOverall.numInstances(); i++) {
					Instance instancei = ccOverall.instance(i);
	
					Distances[i][i] = 0;
	
					for (int j = i + 1; j < ccOverall.numInstances(); j++) {
						Instance instancej = ccOverall.instance(j);
						Distances[i][j] = Utilities.CalculateDistance(instancei,
								instancej);
						Distances[j][i] = Distances[i][j];
					}
				}
	
				double N = ccOverall.numInstances();
				double V = 1.0;
				double K = ccOverall.numAttributes() - 2;
				double R = c * Math.pow(V / N, 1.0 / K);
				// System.out.println("R="+R);
	
				
				int origNumInstances = ccOverall.numInstances();
	
				int UsedCount = 0;
	
				int PredCount=0;
				
				int currentChemicalNum=-1;
				
				while (UsedCount < origNumInstances) {
	
					if (UsedCount%500==0) System.out.println("SphereExclusion count="+UsedCount);
					
					if (RationalDesignMethod.equals("SphereMaxMin2")) {
						// find chemical with maximum minimum dist to previous
						// spheres:
						currentChemicalNum = Utilities.FindChemicalWithMaxMinDist(Used,
							Distances);
					} else if (RationalDesignMethod.equals("SphereMaxAvg2")) {
						currentChemicalNum = Utilities.FindChemicalWithMaxAvgDist(Used,
								Distances);
					}
	
					Instance instanceSphere = ccOverall.instance(currentChemicalNum);
					
					CreatingTrainingPredictionSplittings.addSplittingRow(conn, instanceSphere.stringValue(0), property_name, RationalDesignMethod, "T");
										
					Used[currentChemicalNum] = true;
					UsedCount++;
	
					java.util.Hashtable<Double, Integer> htSC = new java.util.Hashtable<Double, Integer>();
	
					for (int i = 0; i < ccOverall.numInstances(); i++) {
						if (!Used[i]) {
							double dist = Distances[i][currentChemicalNum];
							if (dist < R) {
								htSC.put(new Double(dist), new Integer(i));
							}
						} // end !Used if
					}// end for loop over instances
	
					int count = 1;
	
					Vector v = new Vector(htSC.keySet());
					Collections.sort(v);
	
					for (Enumeration e = v.elements(); e.hasMoreElements();) {
	
						if (count>maxCount) break; // stop assigning to training set
						
						// retrieve the object_key
						double key = (Double) e.nextElement();
						// retrieve the object associated with the key
	
						int instancenum = htSC.get(key);
	
						Instance instance = ccOverall.instance(instancenum);
						Used[instancenum] = true;
						UsedCount++;
	
						// System.out.println(count+"\t"+key + "\t" +
						// instance.stringValue(0));
	
						if (count == 1) {// assign first (closest) one to test set
							CreatingTrainingPredictionSplittings.addSplittingRow(conn, instance.stringValue(0), property_name, RationalDesignMethod, "P");
							PredCount++;
						} else { //assign remainder to training set
							CreatingTrainingPredictionSplittings.addSplittingRow(conn, instance.stringValue(0), property_name, RationalDesignMethod, "T");
						}
						count++;
	
					}// end enumeration loop
	
	
				}// end while (UsedCount<origNumInstances) {
	
				System.out.println((double)PredCount/(double)origNumInstances);
				
	
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
}
