package hazard;

/**
* @author TMARTI02
*/
public class Chemical {
	String id;
    String name;
    String casrn;
    Double similarity;
    Double mass;
    String formula;
    String smiles;
    String hazard;
    String authority;
    String sid;
    Boolean checked=true;
    
    String inchi;
    String inchiKey;
    String inchiKey1;
	
    
    public String getCasrn() {
		return casrn;
	}
	public String getName() {
		return name;
	}
	public String getSid() {
		return sid;
	}
	public String getSmiles() {
		return smiles;
	}
}

