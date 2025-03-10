package hazard;

import java.util.ArrayList;
import java.util.List;


/**
* @author TMARTI02
*/
public class HazardInput {

	List<HazardChemical>chemicals=new ArrayList<>();
		
	Options options=new Options();
		
	static class Properties {
		Double similarity;
		CTSChemical ctsChemical;
	}
	
	class CTSChemical {
		String smiles;
        String routes;
        Integer generation;
        Double accumulation;
        Double production;
        Double globalAccumulation;
        String likelihood;	
	}
	
	class RequestChemical {
		long ordinal;
		String rid;
		Properties properties;
		Chemical chemical;
		
		String derived;
		
	}
	
	class Options {
		String cts;
		Double minSimilarity;
		String analogsSearchType;
		
	}
	
}
