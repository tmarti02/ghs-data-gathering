package gov.epa.ghs_data_gathering.GetData.SkinSensitization;

/**
 * 
 * How main spreadsheet for each source is created: 
 * 
 * <ol>
 * <li>Original spreadsheet for
 * each source is parsed into records. For example see
 * gov.epa.ghs_data_gathering.GetData.SkinSensitization.SkinSensitizationNICEATM.parseExcel2().
 * <br>
 * NICEATM is stored as RecordNICEATM with the following fields:
 * Chemical_Name,CASRN,Molecular_Weight,Chemical_Class,Smiles,LLNA_Vehicle,EC3,LLNA_Result,Class,Reference
 * </li>
 * 
 * <li>The chemreg data set for a given endpoint + source is exported to a spreadsheet. It is then stored as RecordChemReg (See
 * gov.epa.ghs_data_gathering.GetData.DSSTOX.parseChemRegExcel()<br>
 * RecordChemReg has the following fields:
 * Lookup_Result,Query_Casrn,Query_Name,Top_HIT_DSSTox_Substance_Id,Top_Hit_Casrn,Top_Hit_Name,Validated
 * </li>
 * 
 * <li>
 * The production version of the dashboard is used to export records for the
 * unique SIDS the previous step. The records are stored as "RecordDashboard"
 * which have the following fields:
 * INPUT,FOUND_BY,DTXSID,PREFERRED_NAME,CASRN,INCHIKEY,IUPAC_NAME,SMILES,INCHI_STRING,MOLECULAR_FORMULA,QSAR_READY_SMILES};
 * </li>
 * 
 * <li>
 * The tox records in the original format is converted to a common format
 * RecordTox with the following fields: CAS,
 * chemicalName,SMILES,isBinary,binaryToxResult, continuousToxResult,
 * continuousToxUnits;
 * </li>
 * 
 * <li>The spreadsheet is created using
 * gov.epa.ghs_data_gathering.GetData.DSSTOX.goThroughToxRecords.
 * 
 * The vector of RecordTox records are looped over and the original records are
 * linked to chemreg records by using CAS+Name as the key (since ChemReg doesnt
 * store an incremented record number field that allows you to link the
 * records). If the CAS+Name fails, it attempts to link them via just the CAS.
 * <br><br>
 * The spreadsheet has several tabs: 
 * <ul>
 * <li>Records are stored in LLNA_source_BadChemReg" if (recordChemReg.Lookup_Result.contentEquals("No
 * Hits")) or (recordChemReg.Validated.contentEquals("FALSE"))</li>
 * 
 * <li>Records are stored in "LLNA_source_Omitted" if there was no parseable smiles, smiles
 * indicated a salt, had no nonambiguous records, had a bad element (such as a
 * metal), or if 0.2 < Avg Score < 0.8.</li>
 * 
 * <li>Records are stored in
 * "LLNA_source_Duplicate2d" if they match another record in terms of the first
 * part of the inchikey</li> 
 * 
 * <li>Records are stored in "LLNA_source" if not omitted</li>
 * </ul>
 * 
 * </li>
 * </ol>
 * @author TMARTI02
 *
 */
public class ReadmeMainSpreadsheetForEachSource {

}
