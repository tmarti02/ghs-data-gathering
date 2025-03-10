package hazard;

import java.util.List;
import hazard.HazardInput.Properties;
import hazard.HazardInput.RequestChemical;

/**
* @author TMARTI02
*/
public  class HazardChemical {
	
	String chemicalId;
	Chemical chemical;
	RequestChemical requestChemical;
	Properties properties=new Properties();
	List<Score>scores;

	public HazardChemical(Chemical record) {
		properties.similarity=record.similarity;//he stores in both places- needed???
		this.chemical=record;
	}
	
	public class Score {
		String hazardId;
		String hazardName;
		public String finalScore;    // VH, H, M, L, I, ND
		public String finalAuthority;
		public String finalScoreSource;

		List<ScoreRecord>records;

		public String getFinalScore() {
			return finalScore;
		}

		public String getFinalAuthority() {
			return finalAuthority;
		}

		public String getHazardName() {
			return hazardName;
		}
	}

	public List<Score> getScores() {
		return scores;
	}

	public void setScores(List<Score> scores) {
		this.scores = scores;
	}

	public Chemical getChemical() {
		return chemical;
	}

	public void setChemical(Chemical chemical) {
		this.chemical = chemical;
	}
	
}

