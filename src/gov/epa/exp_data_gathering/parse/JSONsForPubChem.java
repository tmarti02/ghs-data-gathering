package gov.epa.exp_data_gathering.parse;

import java.util.List;

import com.google.gson.annotations.SerializedName;

/**
 * Classes to replicate the structure of various JSONs downloaded from PubChem
 * @author GSINCL01
 *
 */
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
	
	public class Data {
		@SerializedName("Record")
		public Record record;
	}
	
	public class Record {
		@SerializedName("RecordType")
		public String recordType;
		@SerializedName("RecordNumber")
		public String recordNumber;
		@SerializedName("RecordTitle")
		public String recordTitle;
		@SerializedName("Section")
		public List<Section> section = null;
		@SerializedName("Reference")
		public List<Reference> reference = null;
	}
	
	public class Section {
		@SerializedName("TOCHeading")
		public String tocHeading;
		@SerializedName("Description")
		public String description;
		@SerializedName("Section")
		public List<Section> section = null;
		@SerializedName("URL")
		public String url;
		@SerializedName("Information")
		public List<Information> information = null;
	}
	
	public class Reference {
		@SerializedName("ReferenceNumber")
		public String referenceNumber;
		@SerializedName("SourceName")
		public String sourceName;
		@SerializedName("SourceID")
		public String sourceID;
		@SerializedName("Name")
		public String name;
		@SerializedName("Description")
		public String description;
		@SerializedName("URL")
		public String url;
		@SerializedName("LicenseNote")
		public String licenseNote;
		@SerializedName("LicenseURL")
		public String licenseURL;
		@SerializedName("ANID")
		public String aNID;
		@SerializedName("IsToxnet")
		public Boolean isToxnet;
	}
	
	public class Information {
		@SerializedName("ReferenceNumber")
		public String referenceNumber;
		@SerializedName("Value")
		public Value value;
		@SerializedName("Name")
		public String name;
	}
	
	public class Value {
		@SerializedName("StringWithMarkup")
		public List<StringWithMarkup> stringWithMarkup = null;
	}
	
	public class StringWithMarkup {
		@SerializedName("String")
		public String string;
	}
	
}
