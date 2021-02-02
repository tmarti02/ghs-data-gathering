/**
 * 
 */
package gov.epa.QSAR.DataSetCreation.Splitting;

import java.util.Vector;

import weka.core.DenseInstance;
import weka.core.Instance;

/**
 * @author Paul Harten
 *
 */
public class Chemical extends DenseInstance {

    /**
     * 
     */

    public Chemical(Chemical instance) {
        super(instance);
    }
    
    void setToxicity(double toxicity) {
        this.setValue(this.classIndex(),toxicity);
    }
    
    double getToxicity() {
        return this.value(this.classIndex());
    }
    
    void Standardize(double[] offsets, double[] scales) {
        double val;
        //  offset the properties by the mean and scale by standard deviations of all chemicals
        for (int i=0; i<this.numAttributes(); i++) {
            if (this.attribute(i).isNumeric()) {
              val = this.value(i);
              // Just subtract the mean if the standard deviation is zero
              if (scales[i] > 0) { 
                  val = (val - offsets[i]) / scales[i];
              } else {
                  val = (val - offsets[i]);
              }
              this.setValue(i,val);
          }
        }
    }
    
    void UnStandardize(double[] offsets, double[] scales) {
        double val;
        //  offset the properties by the mean and scale by standard deviations of all chemicals
        for (int i=0; i<this.numAttributes(); i++) {
            if (this.attribute(i).isNumeric()) {
              val = this.value(i);
              // Just add the mean if the standard deviation is zero
              if (scales[i] > 0) {
                  val = val * scales[i] + offsets[i];
              } else {
                  val = val + offsets[i];
              }
              this.setValue(i,val);
          }
        }
    }

    
}
