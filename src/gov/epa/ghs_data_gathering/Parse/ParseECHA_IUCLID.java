package gov.epa.ghs_data_gathering.Parse;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

import gov.epa.ghs_data_gathering.GetData.IUCLID_Document2;
import gov.epa.ghs_data_gathering.GetData.IUCLID_Document.FieldWithCode;
import gov.epa.ghs_data_gathering.GetData.IUCLID_Document.Part1;
import gov.epa.ghs_data_gathering.GetData.IUCLID_Document.ReferenceSubstance;
import gov.epa.ghs_data_gathering.GetData.IUCLID_Document.Part2.ResultsAndDiscussion.TraditionalSensitisationTest.TestResult;

public class ParseECHA_IUCLID {

	void lookAtSkinSensitizationResults(String filepath) {

		try {

			GsonBuilder builder = new GsonBuilder();
			builder.setPrettyPrinting();
			Gson gson = builder.create();

			IUCLID_Document2 d = gson.fromJson(new FileReader(filepath), IUCLID_Document2.class);

			String strFile = gson.toJson(d);

			System.out.println(strFile);

			System.out.println(d.DossierData.ReferenceSubstance.CAS);

			TestResult[] testResults = d.ResultsAndDiscussion.TraditionalSensitisationTest.ResultsOfTest;

			for (int i = 0; i < testResults.length; i++) {
				TestResult tri = testResults[i];

				// public float NoWithReactions;
				// public float TotalNoInGroup;

				String Line = "";

				if (tri.Reading.code.equals("other:")) {
					Line = tri.Reading.other;
				} else {
					Line = tri.Reading.code;
				}

				Line += "\t" + tri.HoursAfterChallenge + "\t";

				if (tri.Group.code.indexOf("other") > -1) {
					Line += tri.Group.other + "\t";
				} else {
					Line += tri.Group.code + "\t";
				}

				Line += tri.NoWithReactions + "/" + tri.TotalNoInGroup;

				System.out.println(Line);

			}

		} catch (Exception ex) {
			ex.printStackTrace();
		}
	}

	void goThroughFolder(String srcFolder, String destFilePath) {

		try {

			GsonBuilder builder = new GsonBuilder();
			builder.setPrettyPrinting();
			Gson gson = builder.create();

			System.out.println(srcFolder + "\r\n");
			File Folder = new File(srcFolder);

			Path pathDest = Paths.get(destFilePath);
			Files.createDirectories(pathDest);

			File[] files = Folder.listFiles();

			// for (int i=0;i<100;i++) {
			for (int i = 0; i < files.length; i++) {
				File filei = files[i];
				// JsonObject jo = gson.fromJson(new FileReader(filei), JsonObject.class);

				IUCLID_Document2 doc = gson.fromJson(new FileReader(filei), IUCLID_Document2.class);

				if (i % 1000 == 0)
					System.out.println(i);

				// JsonObject joDossierData=jo.getAsJsonObject("DossierData");
				// JsonObject
				// joReferenceSubstance=joDossierData.getAsJsonObject("ReferenceSubstance");
				//
				// if (joReferenceSubstance==null) continue;
				//
				// if (joReferenceSubstance.getAsJsonPrimitive("CAS")==null) {
				//// String strReferenceSubstance = gson.toJson(joReferenceSubstance);
				//// System.out.println(strReferenceSubstance);
				// continue;
				// }
				//
				// String CAS=joReferenceSubstance.getAsJsonPrimitive("CAS").getAsString();

				Part1 dd = doc.DossierData;
				ReferenceSubstance rs = dd.ReferenceSubstance;

				if (rs == null)
					continue;

				if (rs.CAS == null)
					continue;
				// String CAS=rs.CAS;
				// System.out.println(CAS);

				if (rs.CAS.equals("106461-41-0")) {
					System.out.println(filei.getName());
				}

			}

		} catch (Exception ex) {
			ex.printStackTrace();
		}
	}

	public static void main(String[] args) {

		ParseECHA_IUCLID p = new ParseECHA_IUCLID();
		String mainFolder = "R:/0 REACH dossiers/reach_study_results_iuclid6_09-02-2017/GetData";
		String endpoint = "SkinSensitisation";
		String srcFolder = mainFolder + "/endpoints with phrases/" + endpoint;
		String destFilePath = mainFolder + "/endpoints with phrases/" + endpoint + ".txt";
		// p.goThroughFolder(srcFolder, destFilePath);

		String filename = "dossier_uuid=ECHA-b5ed476c-b870-4346-b3ba-bfddbebe7095_document_uuid=ECHA-fa4fac6e-9db6-407a-ae36-d25a04b8ca41.json";
		// String
		// filename="dossier_uuid=ECHA-4cff1901-a563-431d-a854-2bb405ea482c_document_uuid=ECHA-2df3280b-5480-41d2-8e1e-0b3b1c737714.json";
		String filepath = mainFolder + "/endpoints with phrases/SkinSensitisation/" + filename;
		p.lookAtSkinSensitizationResults(filepath);

	}
}
