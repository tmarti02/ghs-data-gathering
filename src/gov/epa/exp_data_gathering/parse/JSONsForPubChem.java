package gov.epa.exp_data_gathering.parse;

import java.util.List;

import com.google.gson.annotations.SerializedName;

public class JSONsForPubChem {
	
	public class IdentifierData {
		@SerializedName("PropertyTable")
		public PropertyTable propertyTable;
	}
	
	public class PropertyTable {
		@SerializedName("Properties")
		public List<Property> properties;
	}
	
	public class Property {
		@SerializedName("CID")
		public String cid;
		@SerializedName("CanonicalSMILES")
		public String canonicalSMILES;
		@SerializedName("IUPACName")
		public String iupacName;
	}
	
}
