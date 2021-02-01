package gov.epa.QSAR.DataSetCreation.Splitting;

import weka.core.Instance;
import weka.core.Instances;
import weka.filters.Filter;
import weka.filters.unsupervised.attribute.Remove;

public class Utilities {

	
	
	private double CalculateTanimotoCoefficient(Instance c1, Instance c2,double []Mean,double []StdDev) {
	
		double TC = 0;
	
		double SumXY = 0;
		double SumX2 = 0;
		double SumY2 = 0;
	
		for (int j = 2; j < c1.numValues(); j++) {
			//       	 	System.out.println(j+"\t"+c1.value(j)+"\t"+c2.value(j));
	
			double xj = c1.value(j);
			double yj = c2.value(j);
	
			if (StdDev[j] == 0) {
				xj = 0;
				yj = 0;
			} else {
				xj = (xj - Mean[j]) / StdDev[j];
				yj = (yj - Mean[j]) / StdDev[j];
			}
	
			SumXY += xj * yj;
			SumX2 += xj * xj;
			SumY2 += yj * yj;
	
		}
	
		TC = SumXY / (SumX2 + SumY2 - SumXY);
	
		return TC;
	
	}
	
public static void Normalize(ChemicalCluster cc) {
		
		int classIndex=1;
		double [] max=new double [cc.numAttributes()];
		double [] min=new double [cc.numAttributes()];
		
		for (int i=classIndex+1;i<cc.numAttributes();i++) {
			max[i]=-99999999;
			min[i]= 99999999;
		}
		
		
		for (int i=classIndex+1;i<cc.numAttributes();i++) {
			
			for (int j=0;j<cc.numInstances();j++) {
				if (cc.instance(j).value(i)>max[i]) {
					max[i]=cc.instance(j).value(i);
				}
				if (cc.instance(j).value(i)<min[i]) {
					min[i]=cc.instance(j).value(i);
				}
			}
		}
		
		for (int i=classIndex+1;i<cc.numAttributes();i++) {
			for (int j=0;j<cc.numInstances();j++) {
				double normval=(cc.instance(j).value(i)-min[i])/(max[i]-min[i]);
				cc.instance(j).setValue(i,normval);
			}
		}
		
	}
	public static void Normalize2(ChemicalCluster cc) {
		
		int classIndex=1;
		
		double [] Mean=cc.CalculateMeans();
		double [] StdDev=cc.CalculateStdDevs();

		for (int i=classIndex+1;i<cc.numAttributes();i++) {

			for (int j=0;j<cc.numInstances();j++) {
			
				Instance instancej=cc.instance(j);
				
				double val=instancej.value(i);
					
				if (StdDev[i]!=0) {
					double normval=(val-Mean[i])/StdDev[i];
					instancej.setValue(i,normval);
				} else {
					instancej.setValue(i,0);
				}
				
			} // end j loop over instances
		} // end i loop over attributes
		
	}
	
	private void Normalize(ChemicalCluster cc,double [] Mean,double []StdDev) {
		
		int classIndex=1;
		
		for (int i=classIndex+1;i<cc.numAttributes();i++) {

			for (int j=0;j<cc.numInstances();j++) {
			
				Instance instancej=cc.instance(j);
				
				double val=instancej.value(i);
					
				if (StdDev[i]!=0) {
					double normval=(val-Mean[i])/StdDev[i];
					instancej.setValue(i,normval);
				} else {
					instancej.setValue(i,0);
				}
				
			} // end j loop over instances
		} // end i loop over attributes
		
	}
	
	
	private double CalculateCosineCoefficient(Instance c1, Instance c2,double [] Mean,double []StdDev) {

		double TC = 0;

		double SumXY = 0;
		double SumX2 = 0;
		double SumY2 = 0;

		for (int j = 2; j < c1.numValues(); j++) {
			//       	 	System.out.println(j+"\t"+c1.value(j)+"\t"+c2.value(j));

			double xj = c1.value(j);
			double yj = c2.value(j);
			//			double weight=weights.instance(j).value(1);

			
			if (StdDev[j] == 0) {
				xj = 0;
				yj = 0;
			} else {
				xj = (xj - Mean[j]) / StdDev[j];
				yj = (yj - Mean[j]) / StdDev[j];
			}


			SumXY += xj * yj;
			SumX2 += xj * xj;
			SumY2 += yj * yj;
		}

		TC = SumXY / Math.sqrt(SumX2 * SumY2);
		//		System.out.println(TC);

		return TC;

	}
	
	private double CalculateCosineCoefficient2(Instance c1, Instance c2,double [] Max,double []Min) {

		double TC = 0;

		double SumXY = 0;
		double SumX2 = 0;
		double SumY2 = 0;

		for (int j = 2; j < c1.numValues(); j++) {
			//       	 	System.out.println(j+"\t"+c1.value(j)+"\t"+c2.value(j));

			double xj = c1.value(j);
			double yj = c2.value(j);
			//			double weight=weights.instance(j).value(1);

			
			if (Max[j]-Min[j] == 0) {
				xj = 0;
				yj = 0;
			} else {
				xj = (xj - Min[j]) / (Max[j]-Min[j]);
				yj = (yj - Min[j]) / (Max[j]-Min[j]);
			}

			SumXY += xj * yj;
			SumX2 += xj * xj;
			SumY2 += yj * yj;
		}

		TC = SumXY / Math.sqrt(SumX2 * SumY2);
		//		System.out.println(TC);

		return TC;

	}
	
	private double FindMinDist (int num,boolean [] Used,double [][] Distances) {
		
		double minDist=99999;
		
		for (int i=0;i<Used.length;i++) {
			if (i==num) continue;
			
			double CurrentDist=Distances[num][i];
			
			if (CurrentDist<minDist) minDist=CurrentDist;
		}
		return minDist;
	}
	
	private double CalculateTanimotoCoefficient2(Instance c1, Instance c2,double []Max,double []Min) {

		double TC = 0;

		double SumXY = 0;
		double SumX2 = 0;
		double SumY2 = 0;

		for (int j = 2; j < c1.numValues(); j++) {
			//       	 	System.out.println(j+"\t"+c1.value(j)+"\t"+c2.value(j));

			double xj = c1.value(j);
			double yj = c2.value(j);

			if (Max[j]-Min[j] == 0) {
				xj = 0;
				yj = 0;
			} else {
				xj = (xj - Min[j]) / (Max[j]-Min[j]);
				yj = (yj - Min[j]) / (Max[j]-Min[j]);
			}

			SumXY += xj * yj;
			SumX2 += xj * xj;
			SumY2 += yj * yj;

		}

		TC = SumXY / (SumX2 + SumY2 - SumXY);

		return TC;

	}

	double [] CalculateMaxValues(Instances instances) {
		double []Max=new double [instances.numAttributes()];
		
		for (int i=2;i<instances.numAttributes();i++) {
			
			Max[i]=-99999;
			
			for (int j=0;j<instances.numInstances();j++) {
				
				Instance instancej=instances.instance(j);
				double valij=instancej.value(i);
				
				if (valij>Max[i]) {
					Max[i]=valij;
				}
			}
		}
		return Max;
	}
	
	double [] CalculateMinValues(Instances instances) {
		double []Min=new double [instances.numAttributes()];
		
		for (int i=2;i<instances.numAttributes();i++) {
			
			Min[i]=99999999;
			
			for (int j=0;j<instances.numInstances();j++) {
				
				Instance instancej=instances.instance(j);
				double valij=instancej.value(i);
				
				if (valij<Min[i]) {
					Min[i]=valij;
				}
			}
		}
		return Min;
	}

	private int GetIndex(ChemicalCluster instances, Instance instance) {
	
		for (int i = 0; i < instances.numInstances(); i++) {
			Instance currentInstance = (Instance) instances.instance(i);
			if (instance.equals(currentInstance))
				return i;
		}
	
		return -1;
	}

	public static int FindIndexMaxActivity(Instances overallSet) {
	
		//Find compound with highest activity:
		double maxActivity=-9999;
		int maxActivityIndex=-1;
		
		for (int i=0;i<overallSet.numInstances();i++) {
			double activity=overallSet.instance(i).classValue();
			if (activity>maxActivity) {
				maxActivity=activity;
				maxActivityIndex=i;				
			}
		}
		return maxActivityIndex;
		
	}

	//finds minimum distance to used chemicals
	private double FindTotMinDist(boolean [] Used ,double [][]Distances)  {
		
		double totMinDist=0;
		
		for (int i=0;i<Used.length;i++) {
			if (!Used[i]) continue;
			totMinDist+=FindMinDist(i,Used,Distances);
		}
	
		return totMinDist;
		
	}

	//finds chemical with maximum minimum distance from previous clusters
	
	//	private int FindChemicalWithMaxDist(ChemicalCluster ccSpheres,ChemicalCluster ccOverall)  {
	//		
	//		int maxDistNum=-1;
	//		double maxDist=-1;
	//
	//		for (int j=0;j<ccOverall.numInstances();j++) {
	//			Instance instancej=ccOverall.instance(j);
	//
	//			double avgDist=0;
	//			
	//			double minDist=999999999;
	//			
	//			for (int i=0;i<ccSpheres.numInstances();i++) {
	//				Instance instancei=ccSpheres.instance(i);
	//			
	//				double dist=this.CalculateDistance(instancei, instancej);
	//
	//				if (dist<minDist) {
	//					minDist=dist;
	//				}
	//				
	//				if (minDist<maxDist) {//save time this chemical isnt going to have maximum minimum distance
	//					break;
	//				}
	//			}
	//			
	//			if (minDist>maxDist) {
	//				maxDist=minDist;
	//				maxDistNum=j;
	//			}
	//		}
	//
	//		return maxDistNum;
	//		
	//	}
		
		
	//	private int FindChemicalWithMaxMinDist(int size,boolean [] Used ,java.util.ArrayList<Integer> selected,double [][]Distances)  {
	//		
	//		int maxDistNum=-1;
	//		double maxDist=-1;
	//
	//		for (int i=0;i<size;i++) {
	//
	//			if (Used[i]) continue;
	//			
	//			double minDist=9999999;
	//			
	//			for (int j=0;j<selected.size();j++) {
	//				int num=selected.get(j);
	//				
	//				double currentDist=Distances[i][num];
	//				
	////				System.out.println(selected.size()+"\t"+i+"\t"+num+"\t"+currentDist);
	//				
	//				if (currentDist<minDist) minDist=currentDist;
	//				
	//				if (minDist<maxDist) //save time this chemical isnt going to have maximum minimum distance
	//					break;
	//
	//			}
	//			
	//			if (minDist>maxDist) {
	//				maxDistNum=i;
	//				maxDist=minDist;
	//			}
	//		}
	//
	//		return maxDistNum;
	//		
	//	}
		
		
	public static int FindChemicalWithMaxMinDist(boolean [] Used ,double [][]Distances)  {
			
			int maxDistNum=-1;
			double maxDist=-1;
	
			for (int i=0;i<Used.length;i++) {
	
				if (Used[i]) continue;
				
				double minDist=9999999;
				
				for (int j=0;j<Used.length;j++) {
					
					if (i==j)continue;
					
					if (!Used[j]) continue; //only compare distance to previously selected
					
					double currentDist=Distances[i][j];
					
					if (currentDist<minDist) minDist=currentDist;
					
	//				System.out.println(i+"\t"+j+currentDist);
					
					if (minDist<maxDist) //save time this chemical isnt going to have maximum minimum distance
						break;
				}
				
				if (minDist>maxDist) {
					maxDistNum=i;
					maxDist=minDist;
				}
			}
	
			return maxDistNum;
			
		}

	private double CalculateDistance(ChemicalCluster cc,int num1,int num2) {
			
			int classIndex=1;
			weka.core.Instance chemical1=cc.instance(num1);
			weka.core.Instance chemical2=cc.instance(num2);
	
			double sum=0;
	        for (int i=classIndex+1; i<cc.numAttributes(); i++) {
	            if (cc.attribute(i).isNumeric() && i != classIndex) {
	//            	if (num2==1) {
	//            		System.out.println(i+"\t"+chemical2.value(i));
	//            	}
	            	sum+=Math.pow(chemical1.value(i)-chemical2.value(i),2);
	            }
	        }
	        
	        sum=Math.sqrt(sum);
	        
	        return sum;
		}

	public static double CalculateDistance(Instance chemical1,Instance chemical2) {
			
			int classIndex=1;
			
	
			double sum=0;
	        for (int i=classIndex+1; i<chemical1.numAttributes(); i++) {
	            if (chemical1.attribute(i).isNumeric() && i != classIndex) {
	//            	if (num2==1) {
	//            		System.out.println(i+"\t"+chemical2.value(i));
	//            	}
	            	sum+=Math.pow(chemical1.value(i)-chemical2.value(i),2);
	            }
	        }
	        
	        sum=Math.sqrt(sum);
	        
	        return sum;
		}

	public static Instances removeBadDescriptors(Instances set) {
	
	    	try {
	//    		set=this.removeNonNumericDescriptors(set);
	    		set=removeConstantDescriptors(set);
	    		
	    	} catch (Exception e) {
	    		e.printStackTrace();
	    	}
	    	return set;
	    	
	    }

	private static Instances removeConstantDescriptors(Instances train) throws Exception {
	
	    int nBadDesc = 0;
	    int classIndex = train.classIndex();
	    double constantThreshold = 0.000001;
	    double avg, diff, variance, std;
	    int numDescriptors = train.numAttributes();
	    int numInstances = train.numInstances();
	    boolean[] removeDescriptor = new boolean[numDescriptors];
	
//	    System.out.println("classIndex="+classIndex);
	    
	    for (int i = classIndex+1; i < numDescriptors; i++) {
	        
	        removeDescriptor[i] = false;
	        
	        if (i != classIndex ) {
	            
	            if (isDescriptorConstant(train, i)) {
	                
	                removeDescriptor[i] = true;
	                nBadDesc++;
	                
	            } else {
	                
	                avg = train.meanOrMode(i);
	                std = Math.sqrt(train.variance(i));
	                
	                if (std < avg * constantThreshold) {
	                    removeDescriptor[i] = true;
	                    nBadDesc++;
	                } 
	                
	            }
	        }
	
	    }
	
	    if (nBadDesc > 0) train = removeDescriptors(train, removeDescriptor);
	
	    return train;
	}

	
	double Calc_M_test_train(ChemicalCluster ccTrain,ChemicalCluster ccPred,double R) {
		double Ntest=ccPred.numInstances();
		double Na=0; //number of points in the test set for which the spheres contain no points of the training set

		for (int i=0;i<ccPred.numInstances();i++) {
			boolean HavePointInSphere=false;
			for (int j=0;j<ccTrain.numInstances();j++) {
				double dist=this.CalculateDistance(ccPred.instance(i), ccTrain.instance(j));
				if (dist<R) {
					HavePointInSphere=true;
					break;
				}
			}
			if (!HavePointInSphere) Na++;
//			System.out.println(i+"\t"+Nb);
		}
		double M_test_train=(double)Na/(double)Ntest;
		return M_test_train;
	}

	double Calc_M_train_test(ChemicalCluster ccTrain,ChemicalCluster ccPred,double R) {
		
		double Nb=0; //number of points in the training set for which the spheres contain no points of the test set
		double Ntrain=ccTrain.numInstances();
		
		for (int i=0;i<ccTrain.numInstances();i++) {
			
			boolean HavePointInSphere=false;
			
			for (int j=0;j<ccPred.numInstances();j++) {
				double dist=this.CalculateDistance(ccPred.instance(j), ccTrain.instance(i));
				if (dist<R) {
					HavePointInSphere=true;
					break;
				}
			}
			if (!HavePointInSphere) Nb++;
//			System.out.println(i+"\t"+Nb);
		}

		double M_train_test=(double)Nb/(double)Ntrain;
		
		return M_train_test;

	}
	
	double CalcItrain(ChemicalCluster ccTrain,double R) {

		double Nc = 0; // number of points in the training set for which the
						// spheres contain no other points of the train set
		double Ntrain = ccTrain.numInstances();

		for (int i = 0; i < ccTrain.numInstances(); i++) {

			boolean HavePointInSphere = false;

			for (int j = 0; j < ccTrain.numInstances(); j++) {

				if (i == j)
					continue;

				double dist = this.CalculateDistance(ccTrain.instance(j),
						ccTrain.instance(i));
				if (dist < R) {
					HavePointInSphere = true;
					break;
				}
			}
			if (!HavePointInSphere)
				Nc++;
			// System.out.println(i+"\t"+Nb);
		}

		double Itrain = (double) Nc / (double) Ntrain;

		return Itrain;
	}
	/**
	 * @param train
	 * @return train with an array of descriptors deleted
	 * @throws Exception
	 */
	private static Instances removeDescriptors(Instances train, boolean[] removeDescriptor) throws Exception {
	
	    int nBadDesc = 0;
	    for (int i=0; i<removeDescriptor.length; i++) {
	        if (removeDescriptor[i]) nBadDesc++;
	    }
	    
	    if (nBadDesc>0) {
	        
	        // modify invert array to account for descriptor numbering after columns are deleted.
	        int[] badDesc = new int[nBadDesc];
	        
	        int [] invertPrevious = new int[train.numAttributes()];
            for (int i=0; i < invertPrevious.length; i++) {
                invertPrevious[i] = i;
            }
	        	        
	        
	        int [] invert = new int[invertPrevious.length-nBadDesc];
	        int j = 0;
	        for (int i=0; i<invertPrevious.length; i++) {
	            if (removeDescriptor[i]) {
	                badDesc[j++] = i;
	            } else {
	                invert[i-j] = invertPrevious[i];
	            }
	        }
	        
	        // remove descriptor columns
	        Remove filter = new Remove();
	        filter.setAttributeIndicesArray(badDesc);   // mark the column(s) to be removed
	        filter.setInputFormat(train);
	        train = Filter.useFilter(train,filter);     // remove the marked columns
	    }
	    
	    return train;
	}
	


	private Instances removeNonNumericDescriptors(Instances train) throws Exception {
	
	    int numAttr = train.numAttributes();
	    boolean[] nonNumeric = new boolean[numAttr];
	    
	    for (int i=0; i < numAttr; i++) {
	        nonNumeric[i] = !train.attribute(i).isNumeric();
	    }
	
	    train = removeDescriptors(train, nonNumeric);
	    
	    return train;
	}

	/**
	 * @param train
	 * @param numInstances
	 * @param i
	 * @return
	 */
	private static boolean isDescriptorConstant(Instances train, int i) {
	
	    if (train.numInstances()==1) return true;
	    
	    double value = train.instance(0).value(i);
	    for (int j=1; j<train.numInstances(); j++) {
	        if (train.instance(j).value(i) != value) return false;
	    }
	    return true;
	
	}

	//maximizes minimum distance between successive points
		//This algorithm replaces chemicals at each iteration until max min distances are all maximized 
		// Note: current implementation seems to give same results as Kennard-Stone with MaxAvgDist
	
		
		
		
		
		
		
	
	
		
		//maximizes minimum distance between successive points, first point picked at random or by using one w/max activity
		// once training chemicals are selected, remaining compounds are put in pred set
		//This method tends to produce very similar sets even though starting point is random
		//TODO: this method might be wrong- might need to implement algorithm by Federov, 1972 to do it right
		
		
		
		
	
	
		//finds chemical with maximum minimum distance from previous clusters
	
	//	private int FindChemicalWithMaxDist(ChemicalCluster ccSpheres,ChemicalCluster ccOverall)  {
	//		
	//		int maxDistNum=-1;
	//		double maxDist=-1;
	//
	//		for (int j=0;j<ccOverall.numInstances();j++) {
	//			Instance instancej=ccOverall.instance(j);
	//
	//			double avgDist=0;
	//			
	//			double minDist=999999999;
	//			
	//			for (int i=0;i<ccSpheres.numInstances();i++) {
	//				Instance instancei=ccSpheres.instance(i);
	//			
	//				double dist=this.CalculateDistance(instancei, instancej);
	//
	//				if (dist<minDist) {
	//					minDist=dist;
	//				}
	//				
	//				if (minDist<maxDist) {//save time this chemical isnt going to have maximum minimum distance
	//					break;
	//				}
	//			}
	//			
	//			if (minDist>maxDist) {
	//				maxDist=minDist;
	//				maxDistNum=j;
	//			}
	//		}
	//
	//		return maxDistNum;
	//		
	//	}
		
		
	//	private int FindChemicalWithMaxMinDist(int size,boolean [] Used ,java.util.ArrayList<Integer> selected,double [][]Distances)  {
	//		
	//		int maxDistNum=-1;
	//		double maxDist=-1;
	//
	//		for (int i=0;i<size;i++) {
	//
	//			if (Used[i]) continue;
	//			
	//			double minDist=9999999;
	//			
	//			for (int j=0;j<selected.size();j++) {
	//				int num=selected.get(j);
	//				
	//				double currentDist=Distances[i][num];
	//				
	////				System.out.println(selected.size()+"\t"+i+"\t"+num+"\t"+currentDist);
	//				
	//				if (currentDist<minDist) minDist=currentDist;
	//				
	//				if (minDist<maxDist) //save time this chemical isnt going to have maximum minimum distance
	//					break;
	//
	//			}
	//			
	//			if (minDist>maxDist) {
	//				maxDistNum=i;
	//				maxDist=minDist;
	//			}
	//		}
	//
	//		return maxDistNum;
	//		
	//	}
		
		
	public static int FindChemicalWithMaxAvgDist(boolean [] Used ,double [][]Distances)  {
		
		int maxDistNum=-1;
		double maxAvgDist=-1;
	
		for (int i=0;i<Used.length;i++) {
	
			if (Used[i]) continue;// only look at unselected chemicals
			
			double AvgDist=9999999;
			double count=0;
	
			for (int j=0;j<Used.length;j++) {
				
				if (i==j)continue;
				
				if (!Used[j]) continue;//only compare distance to previously selected
				
				double currentDist=Distances[i][j];
				
				AvgDist+=currentDist;
				count++;
			}
			
			AvgDist/=count;
			
			if (AvgDist>maxAvgDist) {
				maxDistNum=i;
				maxAvgDist=AvgDist;
			}
		}
	
		return maxDistNum;
		
	}

}
