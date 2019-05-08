package gov.epa.ghs_data_gathering.ParseNew;

import java.util.ArrayList;

public class ParseNERL {
	
	static void readJSON(String jsonFile) {

		ArrayList<String> lines = gov.epa.ghs_data_gathering.Utilities.Utilities.readFileToArray(jsonFile);
		ArrayList<Character> uniqueValues = new ArrayList<>();

		for (String line : lines) {

			// converting string to array and checking each character
			char[] chars = line.toCharArray();
			for (Character c : chars) {

				// regex to not include letters, numbers, "[", "{", ":", "\"", ",", "_", "(",
				// "-", ".", "\", "/"
				if (c.toString().matches("[^\\p{L}\\p{N}\\[\\]{}:\",_(),.\\\\/-]") && (c != ' ')) {
					if (!uniqueValues.contains(c)) {
						uniqueValues.add(c);
					}
				}
			}
		}

		// printing unique characters
		for (Character c : uniqueValues) {
			System.out.println(c);
		}

	}
	

	public static void main(String[] args) {
		String folder = "L:\\Priv\\Cin\\NRMRL\\CompTox\\javax\\web-test\\AA Dashboard\\Output\\NERL\\";
		String jsonFile = folder + "NERL.json";

		readJSON(jsonFile);
	}

}
