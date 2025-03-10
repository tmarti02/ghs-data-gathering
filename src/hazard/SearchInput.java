package hazard;

/**
* @author TMARTI02
*/
public class SearchInput {
	
	String searchType="SIMILAR";
	String inputType="SMILES";
	String query;
	Integer offset=0;
	Integer limit;
	String sortBy="similarity";
	String sortDirection="desc";
	Params params=new Params();
}
	