package gov.epa.exp_data_gathering.parse;

import java.math.BigDecimal;
import java.math.MathContext;
import java.math.RoundingMode;
import java.text.DecimalFormat;

/**
* @author TMARTI02
*/
public class ParameterValue {//Simplified version of hibernate class

	public Parameter parameter=new Parameter();
	public ExpPropUnit unit=new ExpPropUnit();

	public String valueQualifier;
	public Double valuePointEstimate;
	public Double valueMin;
	public Double valueMax;
	public String valueText;
	public Double valueError;
	
	public class Parameter {
		public String name;
		public String description;//optional, can set later in database
	}
	
	public class ExpPropUnit {
		public String abbreviation;
		public String name;//should set in Hibernate project
	}
	
	
	public static String getFormattedValue(Double dvalue,int nsig) {

		if(dvalue==null) {
			return "N/A";
		}
		DecimalFormat dfSci=new DecimalFormat("0.00E00");
		DecimalFormat dfInt=new DecimalFormat("0");
		try {
			if(dvalue!=0 && (Math.abs(dvalue)<0.01 || Math.abs(dvalue)>1e3)) {
				return dfSci.format(dvalue);
			}
//			System.out.println(dvalue+"\t"+setSignificantDigits(dvalue, nsig));
			return setSignificantDigits(dvalue, nsig);
		} catch (Exception ex) {
			return null;
		}
	}
	
	public static String setSignificantDigits(double value, int significantDigits) {
	    if (significantDigits < 0) throw new IllegalArgumentException();

	    // this is more precise than simply doing "new BigDecimal(value);"
	    BigDecimal bd = new BigDecimal(value, MathContext.DECIMAL64);
	    bd = bd.round(new MathContext(significantDigits, RoundingMode.HALF_UP));
	    final int precision = bd.precision();
	    if (precision < significantDigits)
	    bd = bd.setScale(bd.scale() + (significantDigits-precision));
	    return bd.toPlainString();
	}    

	
	public String toString() {
		
		int n=3;
		String pointEstimate=getFormattedValue(valuePointEstimate,n);
		String strValMin=getFormattedValue(valueMin,n);
		String strValMax=getFormattedValue(valueMax,n);

		if(valuePointEstimate!=null) {
			if(valueQualifier!=null) {
				return valueQualifier+" "+pointEstimate+" "+unit.abbreviation;
			} else {
				return pointEstimate+" "+unit.abbreviation;
			}
		} else if (valueMin!=null && valueMax!=null) {
			return strValMin+ " "+unit.abbreviation+" < value < " +strValMax+ " "+unit.abbreviation;
		} else if (valueMin!=null) {
			return " > "+strValMin+" "+unit.abbreviation;
		} else if (valueMax!=null) {
			return " < "+strValMax+" "+unit.abbreviation;	
		} else {
			return null;
		}
	}

	
	public String toStringNoUnits() {
		
		int n=3;
		String pointEstimate=getFormattedValue(valuePointEstimate,n);
		String strValMin=getFormattedValue(valueMin,n);
		String strValMax=getFormattedValue(valueMax,n);

		if(valuePointEstimate!=null) {
			if(valueQualifier!=null) {
				return valueQualifier+" "+pointEstimate;
			} else {
				return pointEstimate;
			}
		} else if (valueMin!=null && valueMax!=null) {
			return strValMin+ " < value < " +strValMax;
		} else if (valueMin!=null) {
			return " > "+strValMin;
		} else if (valueMax!=null) {
			return " < "+strValMax;	
		} else {
			return null;
		}
	}

}

