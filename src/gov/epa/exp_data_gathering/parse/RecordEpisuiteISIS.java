package gov.epa.exp_data_gathering.parse;

import java.io.File;
import java.io.FileInputStream;
import java.util.Vector;

import org.openscience.cdk.AtomContainer;
import org.openscience.cdk.DefaultChemObjectBuilder;
import org.openscience.cdk.interfaces.IAtom;
import org.openscience.cdk.interfaces.IAtomContainer;
import org.openscience.cdk.io.iterator.IteratingSDFReader;
import org.openscience.cdk.smiles.SmiFlavor;
import org.openscience.cdk.smiles.SmilesGenerator;

import gov.epa.TEST.Descriptors.DescriptorUtilities.AtomicProperties;
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
	Double WS_LogMolarCalc;
	
	static final String sourceName=ExperimentalConstants.strSourceEpisuiteISIS;

	
	private static Vector<RecordEpisuiteISIS> getRecords(String filepath,String abbrev){
		Vector<RecordEpisuiteISIS> records = new Vector<>();
		
		try {
			
			IteratingSDFReader mr = new IteratingSDFReader(new FileInputStream(filepath),DefaultChemObjectBuilder.getInstance());								

			int counter=0;

//			DescriptorFactory df=new DescriptorFactory(false);
			
			while (mr.hasNext()) {
				
				AtomContainer m=null;

				m = (AtomContainer)mr.next();
				
				RecordEpisuiteISIS r=new RecordEpisuiteISIS();
				
				counter++;
				
				r.CAS=m.getProperty("CAS");				
//				System.out.println(counter+"\t"+r.CAS);
				
				if (m.getProperty("NAME")!=null) r.Name=m.getProperty("NAME");
				r.Smiles=generateSmiles(m);
				
//				String desc=DescriptorsFromSmiles.goDescriptors(r.Smiles);
				

				if (abbrev.contentEquals("WS")) {
					if (m.getProperty(abbrev)!=null) {
						r.WS_mg_L=Double.parseDouble(m.getProperty(abbrev));
						double MW=Calculate_mw(m);
//						double WS_LogMolarCalc=Math.log10(r.WS_mg_L/1000.0/dd.MW);
						r.WS_LogMolarCalc=Math.log10(r.WS_mg_L/1000.0/MW);
					}
					r.WS_LogMolar=Double.parseDouble(m.getProperty("LogMolar"));
					
//					DescriptorData dd=new DescriptorData();
//					df.CalculateDescriptors(m, dd, false);
					
					
//					System.out.println(r.CAS+"\t"+mw);
					
				}
				
				r.DataSet=m.getProperty("DataSet");
				r.Reference=m.getProperty(abbrev+" Reference");
				if (m.getProperty(abbrev+" Temperature")!=null) r.Temperature=Double.parseDouble(m.getProperty(abbrev +" Temperature"));
				
				records.add(r);
			}

		} catch (Exception ex) {
			ex.printStackTrace();
		}
		return records;
	}

	
	private static double Calculate_mw(IAtomContainer m) {
		// tried to use CDK built in methods but they suck
		// alternative method would be to use m2 which includes the hydrogens
		
		try {
			AtomicProperties ap=AtomicProperties.getInstance();
		
			double MW=0;
			
			for (int i=0;i<=m.getAtomCount()-1;i++) {			
				IAtom a=m.getAtom(i);
				
				if (a.getSymbol().contentEquals("Na")) MW+=22.98977; 
				else if (a.getSymbol().contentEquals("K")) MW+=39.0983;
				else if (a.getSymbol().contentEquals("Ca")) MW+=40.08;
				else if (a.getSymbol().contentEquals("Ba")) MW+=137.33;		
				else if (a.getSymbol().contentEquals("U")) MW+=238.029;
				else if (a.getSymbol().contentEquals("Sr")) MW+=87.62;
				else MW+=ap.GetMass(a.getSymbol());
				
				MW+=a.getImplicitHydrogenCount()*ap.GetMass("H");
				
			}
		
			return MW;
			
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		return -9999;
			
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
		Vector<RecordEpisuiteISIS> records1 = getRecords(strFolder+"EPI_WaterFrag_Data_SDF.sdf","WS");
		Vector<RecordEpisuiteISIS> records2 = getRecords(strFolder+"EPI_Wskowwin_Data_SDF.sdf","WS");
		
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
//				System.out.println(rec2.CAS);
				records.add(rec2);
			}
		}
		return(records);
	}

	
	public static void main (String[] args) {
		Vector<RecordEpisuiteISIS> records = recordWaterFragmentData();
	}
}

