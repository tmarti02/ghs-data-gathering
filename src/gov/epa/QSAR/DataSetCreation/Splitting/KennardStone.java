package gov.epa.QSAR.DataSetCreation.Splitting;

import java.sql.Connection;
import gov.epa.database.SQLite_Utilities;
import weka.core.Instance;
import weka.core.Instances;

public class KennardStone {
	
	
	/**
	 *
	 * maximizes minimum distance between successive points // The first 2
	 * chemicals are the ones that are the farthest apart in terms of euclidean
	 * distance // once training chemicals are selected, remaining compounds are put
	 * in pred set //This method produces the same sets each time (no randomness)
	 * 
	 * @param property
	 * @param instances
	 * @param predfrac
	 * @param NormMethod
	 * @param RationalDesignMethod
	 * @param dbpath
	 */
		public static void GoKennardStone(String property,Instances instances,double predfrac,int NormMethod,String RationalDesignMethod,String dbpath) {

			try {

				Connection conn=SQLite_Utilities.getConnection(dbpath);
				
				instances = Utilities.removeBadDescriptors(instances);

				ChemicalCluster ccOverall = new ChemicalCluster(instances);
				
				if (NormMethod==1) 	Utilities.Normalize(ccOverall);
				else if (NormMethod==2) Utilities.Normalize2(ccOverall);

				double[][] Distances = new double[ccOverall.numInstances()][ccOverall
						.numInstances()];

				boolean[] Used = new boolean[ccOverall.numInstances()];
				// java.util.ArrayList<Integer> selected=new
				// java.util.ArrayList<Integer>();

				// calculate Distances:

				//numbers for 2 chemicals which are the farthest apart:
				int MaxI=-1;
				int MaxJ=-1;
				
				double MaxDist=-1;
				
				for (int i = 0; i < ccOverall.numInstances(); i++) {
					Instance instancei = ccOverall.instance(i);

					Distances[i][i] = 0;

					for (int j = i + 1; j < ccOverall.numInstances(); j++) {
						Instance instancej = ccOverall.instance(j);
						Distances[i][j] = Utilities.CalculateDistance(instancei,
								instancej);
						if (Distances[i][j]>MaxDist) {
							MaxDist=Distances[i][j];
							MaxI=i;
							MaxJ=j;
						}
						
						Distances[j][i] = Distances[i][j];
					}
				}

//				double predfrac = 0.167;
				int TrainSize = (int) (ccOverall.numInstances() * (1 - predfrac));

//				System.out.println((ccOverall.numInstances() * (1 - predfrac)));
//				System.out.println(TrainSize);
				
				int UsedCount = 0;

				//Mark points that are the farthest apart as used:
				Used[MaxI] = true;
				UsedCount++;
				Used[MaxJ] = true;
				UsedCount++;
				
				System.out.println(ccOverall.instance(MaxI).stringValue(0));
				System.out.println(ccOverall.instance(MaxJ).stringValue(0));
				
				while (UsedCount < TrainSize) {
					int next=-1;
					
					if (RationalDesignMethod.equals("KennardStoneMaxMin")) {
						next = Utilities.FindChemicalWithMaxMinDist(Used, Distances);
					} else if (RationalDesignMethod.equals("KennardStoneMaxAvg")) {
						next = Utilities.FindChemicalWithMaxAvgDist(Used, Distances);
					}
					
					Used[next] = true;
					UsedCount++;
					// System.out.println(UsedCount+"\t"+next);
				}

				for (int i = 0; i < Used.length; i++) {
					Instance instance = ccOverall.instance(i);
					if (Used[i]) {  // output to training set:
						CreatingTrainingPredictionSplittings.addSplittingRow(conn, instance.stringValue(0), property, RationalDesignMethod, "T");
					} else  {// output to prediction set:
						CreatingTrainingPredictionSplittings.addSplittingRow(conn, instance.stringValue(0), property, RationalDesignMethod, "P");					
					}	
				}	

			} catch (Exception e) {
				e.printStackTrace();
			}
		}

}
