package gov.epa.exp_data_gathering.parse;

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

}
