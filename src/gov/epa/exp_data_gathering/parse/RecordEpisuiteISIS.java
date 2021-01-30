package gov.epa.exp_data_gathering.parse;

import java.io.File;
import java.io.FileInputStream;
import java.util.Vector;

import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.openscience.cdk.AtomContainer;
import org.openscience.cdk.DefaultChemObjectBuilder;
import org.openscience.cdk.interfaces.IAtomContainer;
import org.openscience.cdk.io.iterator.IteratingSDFReader;
import org.openscience.cdk.smiles.SmiFlavor;
import org.openscience.cdk.smiles.SmilesGenerator;

import gov.epa.api.ExperimentalConstants;

public class RecordEpisuiteISIS {
	String CAS;
	String Name;
	String Smiles;	
	Double WS_LogMolar;	
	Double WS_mg_L;
	Double Temperature;
	String DataSet;
	String Reference;
	
	static final String sourceName=ExperimentalConstants.strSourceEpisuiteISIS;

	
	private static Vector<RecordEpisuiteISIS> getRecords(String filepath){
		Vector<RecordEpisuiteISIS> records = new Vector<>();
		
		try {
			
			IteratingSDFReader mr = new IteratingSDFReader(new FileInputStream(filepath),DefaultChemObjectBuilder.getInstance());								

			int counter=0;
			while (mr.hasNext()) {
				
				AtomContainer m=null;

				m = (AtomContainer)mr.next();
				
				RecordEpisuiteISIS r=new RecordEpisuiteISIS();
				
				counter++;
				
				r.CAS=m.getProperty("CAS");
				

//				System.out.println(counter+"\t"+r.CAS);
				
				if (m.getProperty("NAME")!=null) r.Name=m.getProperty("NAME");
				r.Smiles=generateSmiles(m);
				
				if (m.getProperty("WS")!=null) r.WS_mg_L=Double.parseDouble(m.getProperty("WS"));
				r.WS_LogMolar=Double.parseDouble(m.getProperty("LogMolar"));
				r.DataSet=m.getProperty("DataSet");
				r.Reference=m.getProperty("WS Reference");
				if (m.getProperty("WS Temperature")!=null) r.Temperature=Double.parseDouble(m.getProperty("WS Temperature"));
				
				records.add(r);
			}

		} catch (Exception ex) {
			ex.printStackTrace();
		}
		return records;
	}

	
	public static String generateSmiles(IAtomContainer ac) {
		return generateSmiles(ac, SmiFlavor.Unique);
	}

	
	public static String generateSmiles(IAtomContainer ac, int flavor) {
		try {
			SmilesGenerator sg = new SmilesGenerator(flavor);
			String smiles = sg.create(ac);
			return smiles;

		} catch (Exception ex) {
			ex.printStackTrace();
		}
		return null;
	}
	
	//TODO add rest of properties from SDFs
	
	
	public static Vector<RecordEpisuiteISIS> recordWaterFragmentData() {
		Vector<RecordEpisuiteISIS> records = new Vector<>();

		
		String strFolder = "Data"+File.separator+"Experimental"+ File.separator + sourceName+File.separator+"EPI_SDF_Data"+File.separator;
		Vector<RecordEpisuiteISIS> records1 = getRecords(strFolder+"EPI_WaterFrag_Data_SDF.sdf");
		Vector<RecordEpisuiteISIS> records2 = getRecords(strFolder+"EPI_Wskowwin_Data_SDF.sdf");
		
		records.addAll(records1);
		
		// Only add new records from second file:
		for (RecordEpisuiteISIS rec2:records2) {
			boolean haveRec=false;
			for (RecordEpisuiteISIS rec1:records1) {
				if (rec1.CAS.contentEquals(rec2.CAS)) {
//					System.out.println(rec2.CAS+"\t"+rec1.CAS);
					haveRec=true;
					break;
				}
			}
			if (!haveRec) {
				System.out.println(rec2.CAS);
				records.add(rec2);
			}
		}
		
			
		
		return(records);
	}

	
	public static void main (String[] args) {
		Vector<RecordEpisuiteISIS> records = recordWaterFragmentData();
		
	}
}
