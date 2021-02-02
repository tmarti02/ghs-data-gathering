package gov.epa.QSAR.DataSetCreation.Splitting;

import java.io.IOException;
import java.util.Iterator;
import java.util.List;
import java.util.RandomAccess;
import java.util.Vector;

import weka.core.Instance;
import weka.core.Instances;
import weka.core.WekaException;

public class ChemicalCluster extends Instances implements Cloneable {
    
    /**
     * 
     */
    //private int num_of_properties;
    private double[] centroid;
    private double variance;        // this measure used by Ward's Method
    private double avgToxicity;
    private double toxicUncertainty;
    private int clusterNumber = 0;
    private int parent1 = 0;
    private int parent2 = 0;
    private double rMax = 0;
    
//    ChemicalCluster(Chemical chemical) {
//        this.centroid = (double[]) chemical.toDoubleArray();
//        this.variance = 0;
//        this.avgToxicity = chemical.get_Toxicity();
//        this.toxicUncertainty = 0;
//    }

    // creates a ChemicalCluster from Instances
    public ChemicalCluster(Instances data) { //made public TMM
        // convert all instances over 	
        super(data, 0, data.numInstances());        
    }
    
    // creates a ChemicalCluster with no instances, just headings
    ChemicalCluster(Instances data, int capacity) {
        super(data,capacity);
        
        this.centroid = null;
        this.variance = 0;
        this.avgToxicity = 0;
        this.toxicUncertainty = 0;
    }
    
    // creates a ChemicalCluster with headings and several Chemicals
    public ChemicalCluster(ChemicalCluster cluster, int from, int toCopy) { //made public tmm
        super(cluster,from,toCopy);

        this.CalculateCentroid();
        this.CalculateAverageToxicity();
        this.CalculateToxicUncertainty();
    }
    
    // create a ChemicalCluster from two ChemicalClusters
    ChemicalCluster(ChemicalCluster cluster1, ChemicalCluster cluster2) {
        super(cluster1,cluster1.numInstances()+cluster2.numInstances());
        
        this.addInstances(0, cluster1, cluster1.numInstances());
        this.addInstances(0, cluster2, cluster2.numInstances());
        this.CalculateCentroid(cluster1, cluster2);
        this.CalculateAverageToxicity(cluster1, cluster2);
        this.CalculateToxicUncertainty(cluster1, cluster2);
        this.CalculateSumDistanceSq(cluster1, cluster2);
        this.setParent1(cluster1.getClusterNumber());
        this.setParent2(cluster2.getClusterNumber());
    }
    
    public ChemicalCluster Clone() {
        ChemicalCluster cluster = new ChemicalCluster(this);
        
        cluster.centroid = this.centroid.clone();
        cluster.setVariance(this.variance);
        cluster.setAvgToxicity(this.getAvgToxicity());
        cluster.setToxicUncertainty(this.getToxicUncertainty());
        cluster.setClusterNumber(this.getClusterNumber());
        cluster.setParent1(this.getParent1());
        cluster.setParent2(this.getParent2());
        
        return cluster;
    }
    
    protected void addInstances(int from, Instances data, int num) {
        
        for (int i = from; i < from+num; i++) {
          this.add(data.instance(i));
        }
    }
    
//    /**
//     * Converts an instance to a chemical and adds it to the 
//     * end of the set. Increases the size of the dataset if
//     * it is not large enough. Does not check if the instance
//     * is compatible with the dataset.
//     *
//     * @param instance the Instance to be added
//     */
//    public void add(/*@non_null@*/ Instance instance) {
//
//      Chemical newChemical = new Chemical(instance);
//
//      newChemical.setDataset(this);
//      m_Instances.add(newChemical);
//
//    }
    
//    /**
//     * Adds a chemical to the 
//     * end of the set. Increases the size of the dataset if
//     * it is not large enough. Does not check if the instance
//     * is compatible with the dataset.
//     *
//     * @param chemical the Chemical to be added
//     */
//    public void add(/*@non_null@*/ Chemical chemical) {
//
//      Instance newChemical = new Instance(chemical);
//
//      newChemical.setDataset(this);
//      m_Instances.add(newChemical);
//    }
    
    public double[] CalculateMeans() { //made public TMM
        double[] means = new double[this.numAttributes()];
        
        for (int i = 0; i < this.numAttributes(); i++) {
            if (this.attribute(i).isNumeric()) {
                means[i] = this.meanOrMode(i);
            }
        }
        
        return means;
    }
    
    public double[] CalculateStdDevs() { //made public TMM
        double[] stdDevs = new double[this.numAttributes()];
        
        for (int i = 0; i < this.numAttributes(); i++) {
            if (this.attribute(i).isNumeric()) {
                stdDevs[i] = Math.sqrt(this.variance(i));
            }
        }
        
        return stdDevs;
    }
    
    public void StandardizeCentroid(double[] offsets, double[] scales) {
        Instance chemical;
        int classIndex = this.classIndex();
        
        //  offset the centroid by the mean and scale by standard deviations of all chemicals
        for (int j=0; j<this.numAttributes(); j++) {
            if (this.attribute(j).isNumeric() && j!=classIndex ) {
                if (scales[j]==0) {
                    this.centroid[j] = 0.0;
                } else {
                    this.centroid[j] = (this.centroid[j]-offsets[j]) / scales[j];
                }
            }
        }
        
    }
    
    public void Standardize(double[] offsets, double[] scales) { //made public TMM
        Chemical chemical;
        
        //  offset the properties by the mean and scale by standard deviations of all chemicals
        for (int j=0; j<this.numInstances(); j++) {
            chemical = (Chemical)this.instance(j);
            chemical.Standardize(offsets, scales);
        }
        this.CalculateCentroid(); // recalculate centroid
        this.CalculateAverageToxicity();
        this.CalculateToxicUncertainty();
        
    }
    
       
    public void UnStandardize(double[] offsets, double[] scales) { // made public TMM
        Chemical chemical;
        
        //  offset the properties by the mean and scale by standard deviations of all chemicals
        for (int j=0; j<this.numInstances(); j++) {
            chemical = (Chemical)this.instance(j);
            chemical.UnStandardize(offsets, scales);
        }
        this.CalculateCentroid(); // recalculate centroid
        this.CalculateAverageToxicity();
        this.CalculateToxicUncertainty();
        
    }
    
    public void CalculateCentroid() { //made public tmm
        double alpha = 1.0 / this.numInstances();
        Instance instance;

        if (centroid == null) centroid = new double[this.numAttributes()];
        
        for (int i=0; i<centroid.length; i++) {
            centroid[i] = 0.0;
        }
        for (int j=0; j<this.numInstances(); j++) {
            instance = instance(j);
            for (int i=0; i<centroid.length; i++) {
                if (this.attribute(i).isNumeric() && i != m_ClassIndex) {
                    centroid[i] += instance.value(i);
                }
            }
        }
        for (int i=0; i<centroid.length; i++) {
            centroid[i] *= alpha;
        }
        
    }
    
    void CalculateCentroid(ChemicalCluster cluster1, ChemicalCluster cluster2) {
        double n1 = cluster1.numInstances();
        double n2 = cluster2.numInstances();
        double alpha1 = n1 / (n1 + n2);
        double alpha2 = n2 / (n1 + n2);
        
        if (centroid == null) centroid = new double[cluster1.centroid.length];
        
        for (int i=0; i<centroid.length; i++) {
            centroid[i] = alpha1*cluster1.centroid[i] + alpha2*cluster2.centroid[i];
        }
    }
    
//    void CalculateSumDistanceSq() {
//        double diff;
//        Chemical chemical;
//
//        variance = 0.0;
//        for (int j=0; j<this.numInstances(); j++) {
//            chemical = (Chemical)this.instance(j);
//            for (int i = 0; i < centroid.length; i++) {
//                if (this.attribute(i).isNumeric() && i != m_ClassIndex) {
//                    diff = (this.centroid[i]-chemical.value(i));    // scale the measure in each dimension
//                    variance += diff * diff;
//                }
//            }
//        }
//        // sum of distance squared from the centroid.
//    }
    
    void CalculateSumDistanceSq(ChemicalCluster cluster1, ChemicalCluster cluster2) {
        double n1 = cluster1.numInstances();
        double n2 = cluster2.numInstances();
        double alpha = (n1*n2) / (n1+n2);
        double diff, sumlensq;

        sumlensq = 0.0;
        for (int i=0; i<cluster1.centroid.length; i++) {
            if (this.attribute(i).isNumeric() && i != m_ClassIndex) {
                diff = (cluster1.centroid[i]-cluster2.centroid[i]);   // scale the measure in each dimension
                sumlensq += diff * diff;
            }
        }

        variance = cluster1.getVariance() + cluster2.getVariance() + alpha * sumlensq;
    }
    
    void CalculateAverageToxicity() {
        avgToxicity = 0;
        if (this.numInstances()<=0) return;
        
        for (int j=0; j<this.numInstances(); j++) {
            avgToxicity += this.instance(j).value(m_ClassIndex); // element is LD50      
        }
        avgToxicity /= this.numInstances();
    }
    
   void CalculateAverageToxicity(ChemicalCluster cluster1, ChemicalCluster cluster2) {
        double n1 = (double)cluster1.numInstances();
        double n2 = (double)cluster2.numInstances();
        double alpha1 = n1 / (n1+n2);
        double alpha2 = n2 / (n1+n2);
        
        avgToxicity = alpha1 * cluster1.avgToxicity + alpha2 * cluster2.avgToxicity;
    }

    void CalculateToxicUncertainty() {
        double toxicVariance = 0;
        double diff;

        for (int j=0; j<this.numInstances(); j++) {
            diff = this.avgToxicity - this.instance(j).value(m_ClassIndex);
            toxicVariance += diff*diff;
        }

        if (this.numInstances()<=1) {
            toxicVariance = 0;
        } else {
            toxicVariance = toxicVariance /(this.numInstances()-1); 
        }
        
        toxicUncertainty = Math.sqrt(toxicVariance);
    }

    void CalculateToxicUncertainty(ChemicalCluster cluster1, ChemicalCluster cluster2) {
        double n1 = (double)cluster1.numInstances();
        double n2 = (double)cluster2.numInstances();
        
        double toxicVariance1 = cluster1.toxicUncertainty * cluster1.toxicUncertainty;
        double toxicVariance2 = cluster2.toxicUncertainty * cluster2.toxicUncertainty;
        double toxicVariance, diff;
        
        diff = cluster1.avgToxicity - cluster2.avgToxicity;
        toxicVariance = (n1-1)*toxicVariance1 + (n2-1)*toxicVariance2 + ((n1*n2)/(n1+n2)) * diff * diff;
        toxicVariance = toxicVariance /(n1+n2-1);

        toxicUncertainty = Math.sqrt(toxicVariance);
    }

    double CalculateUncertaintyFraction() {
        if (/*this->containsSamples &&*/ this.avgToxicity == 0) {
            return 0.0;
        } else {
            return this.toxicUncertainty / this.avgToxicity;
        }
    }
    
    public void CalculateRMax(double[] scales) {
        // TODO Auto-generated method stub
        double[] dist = new double[this.numInstances()];
        Instance instance;

        double distmax =0.0;

        if (centroid.length > 1){

            for (int j=0; j<this.numInstances(); j++) {
                instance = instance(j);
                dist [j]= 0.0;
                for (int i=0; i<centroid.length; i++) {
                    if (scales[i] != 0.0){
                        if (this.attribute(i).isNumeric() && i != m_ClassIndex) {
                            double sumc =(instance.value(i)- centroid[i])/scales[i];
                            dist[j] += sumc*sumc;
                        }
                    }
                }
//              this  calculation is based on the harmonic distance in the cluster changed by sdas 5/10/07 
                //           distmax += 1.0/Math.sqrt(dist[j]);
                // This is the selection of the largest distance of a chemical in the cluster.      
                if (  Math.sqrt(dist[j]) > distmax)  distmax = Math.sqrt(dist[j]);

            }
        }
        rMax= distmax;// this was the highest distance of chemical in cluster     
        //  Rmax = this.numInstances()/ distmax;
    }
    
    
    void Print()
    {
        Instance chemical;
        
        System.out.println("Number of Chemicals = "+this.numInstances());
        System.out.println("Number of Properties = "+this.centroid.length);        
        System.out.println("Avg lengthsq from centroid = "+this.variance);
        
        System.out.print("centroid = {"+this.centroid[0]);
        for (int i = 1; i < this.centroid.length; i++) {
            System.out.print(", "+this.centroid[i]);
        }
        System.out.print("}\n");
        
        for (int j=0; j<this.numInstances(); j++) {          
            chemical = (Instance)this.instance(j);
            System.out.print(chemical.value(0)+": "+chemical.value(1)+"\n");
        }
        
    }

    /**
     * @return Returns the avgToxicity.
     */
    public double getAvgToxicity() {
        return avgToxicity;
    }

    /**
     * @return Returns the centroid.
     */
    public double[] getCentroid() {
        return centroid;
    }

    /**
     * @return Returns the toxicUncertainty.
     */
    public double getToxicUncertainty() {
        return toxicUncertainty;
    }

    /**
     * @return Returns the variance.
     */
    public double getVariance() {
        return variance;
    }

    /**
     * @return Returns the clusterNumber.
     */
    public int getClusterNumber() {
        return clusterNumber;
    }

    /**
     * @param clusterNumber The clusterNumber to set.
     */
    public void setClusterNumber(int clusterNumber) {
        this.clusterNumber = clusterNumber;
    }

    /**
     * @return Returns the pClusterNum1.
     */
    public int getParent1() {
        return parent1;
    }

    /**
     * @param clusterNum1 The pClusterNum1 to set.
     */
    public void setParent1(int clusterNum1) {
        parent1 = clusterNum1;
    }

    /**
     * @return Returns the pClusterNum2.
     */
    public int getParent2() {
        return parent2;
    }

    /**
     * @param clusterNum2 The pClusterNum2 to set.
     */
    public void setParent2(int clusterNum2) {
        parent2 = clusterNum2;
    }

    /**
     * @param avgToxicity The avgToxicity to set.
     */
    public void setAvgToxicity(double avgToxicity) {
        this.avgToxicity = avgToxicity;
    }

    /**
     * @param toxicUncertainty The toxicUncertainty to set.
     */
    public void setToxicUncertainty(double toxicUncertainty) {
        this.toxicUncertainty = toxicUncertainty;
    }

    public void setVariance(double value) {
        this.variance = value;
    }
    
    public void setCentroid(double[] centroid) {
        this.centroid = centroid;
    }

    /**
     * @return the rMax
     */
    public double getRMax() {
        return rMax;
    }
    
    /*
    void PrintSampleToxicities()
    {
        Chemical chemical;

        for (int i=0; i<this.elementCount; i++) {          
            chemical = (Chemical)this.elementAt(i);
            if (chemical.get_Sample()) {
                System.out.println("name = "+chemical.get_Name()+", toxicity = "+chemical.get_Toxicity()
                        +", predicted = "+chemical.get_Predicted()+" +/- "+chemical.get_Uncertainty());
            }
        }
    }
    */

}
