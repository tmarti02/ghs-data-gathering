package gov.epa.ghs_data_gathering.Parse.OPERA;

import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Vector;
import com.google.gson.JsonObject;
import gov.epa.api.ScoreRecord;
import gov.epa.database.SQLite_GetRecords;

public class RecordOPERA {

	public String DSSTOX_COMPOUND_ID;
	public String LogOH_exp;
	public String LogOH_pred;
	public String LogOH_predRange;
	public String AD_AOH;
	public String AD_index_AOH;
	public String Conf_index_AOH;
	public String AOH_CAS_neighbor_1;
	public String AOH_CAS_neighbor_2;
	public String AOH_CAS_neighbor_3;
	public String AOH_CAS_neighbor_4;
	public String AOH_CAS_neighbor_5;
	public String AOH_InChiKey_neighbor_1;
	public String AOH_InChiKey_neighbor_2;
	public String AOH_InChiKey_neighbor_3;
	public String AOH_InChiKey_neighbor_4;
	public String AOH_InChiKey_neighbor_5;
	public String AOH_DTXSID_neighbor_1;
	public String AOH_DTXSID_neighbor_2;
	public String AOH_DTXSID_neighbor_3;
	public String AOH_DTXSID_neighbor_4;
	public String AOH_DTXSID_neighbor_5;
	public String AOH_DSSTOXMPID_neighbor_1;
	public String AOH_DSSTOXMPID_neighbor_2;
	public String AOH_DSSTOXMPID_neighbor_3;
	public String AOH_DSSTOXMPID_neighbor_4;
	public String AOH_DSSTOXMPID_neighbor_5;
	public String LogOH_Exp_neighbor_1;
	public String LogOH_Exp_neighbor_2;
	public String LogOH_Exp_neighbor_3;
	public String LogOH_Exp_neighbor_4;
	public String LogOH_Exp_neighbor_5;
	public String LogOH_pred_neighbor_1;
	public String LogOH_pred_neighbor_2;
	public String LogOH_pred_neighbor_3;
	public String LogOH_pred_neighbor_4;
	public String LogOH_pred_neighbor_5;
	public String LogBCF_exp;
	public String LogBCF_pred;
	public String BCF_predRange;
	public String AD_BCF;
	public String AD_index_BCF;
	public String Conf_index_BCF;
	public String LogBCF_CAS_neighbor_1;
	public String LogBCF_CAS_neighbor_2;
	public String LogBCF_CAS_neighbor_3;
	public String LogBCF_CAS_neighbor_4;
	public String LogBCF_CAS_neighbor_5;
	public String LogBCF_InChiKey_neighbor_1;
	public String LogBCF_InChiKey_neighbor_2;
	public String LogBCF_InChiKey_neighbor_3;
	public String LogBCF_InChiKey_neighbor_4;
	public String LogBCF_InChiKey_neighbor_5;
	public String LogBCF_DTXSID_neighbor_1;
	public String LogBCF_DTXSID_neighbor_2;
	public String LogBCF_DTXSID_neighbor_3;
	public String LogBCF_DTXSID_neighbor_4;
	public String LogBCF_DTXSID_neighbor_5;
	public String LogBCF_DSSTOXMPID_neighbor_1;
	public String LogBCF_DSSTOXMPID_neighbor_2;
	public String LogBCF_DSSTOXMPID_neighbor_3;
	public String LogBCF_DSSTOXMPID_neighbor_4;
	public String LogBCF_DSSTOXMPID_neighbor_5;
	public String LogBCF_Exp_neighbor_1;
	public String LogBCF_Exp_neighbor_2;
	public String LogBCF_Exp_neighbor_3;
	public String LogBCF_Exp_neighbor_4;
	public String LogBCF_Exp_neighbor_5;
	public String LogBCF_pred_neighbor_1;
	public String LogBCF_pred_neighbor_2;
	public String LogBCF_pred_neighbor_3;
	public String LogBCF_pred_neighbor_4;
	public String LogBCF_pred_neighbor_5;
	public String BioDeg_exp;
	public String BioDeg_LogHalfLife_pred;
	public String BioDeg_predRange;
	public String AD_BioDeg;
	public String AD_index_BioDeg;
	public String Conf_index_BioDeg;
	public String BioDeg_CAS_neighbor_1;
	public String BioDeg_CAS_neighbor_2;
	public String BioDeg_CAS_neighbor_3;
	public String BioDeg_CAS_neighbor_4;
	public String BioDeg_CAS_neighbor_5;
	public String BioDeg_InChiKey_neighbor_1;
	public String BioDeg_InChiKey_neighbor_2;
	public String BioDeg_InChiKey_neighbor_3;
	public String BioDeg_InChiKey_neighbor_4;
	public String BioDeg_InChiKey_neighbor_5;
	public String BioDeg_DTXSID_neighbor_1;
	public String BioDeg_DTXSID_neighbor_2;
	public String BioDeg_DTXSID_neighbor_3;
	public String BioDeg_DTXSID_neighbor_4;
	public String BioDeg_DTXSID_neighbor_5;
	public String BioDeg_DSSTOXMPID_neighbor_1;
	public String BioDeg_DSSTOXMPID_neighbor_2;
	public String BioDeg_DSSTOXMPID_neighbor_3;
	public String BioDeg_DSSTOXMPID_neighbor_4;
	public String BioDeg_DSSTOXMPID_neighbor_5;
	public String BioDeg_LogHalfLife_Exp_neighbor_1;
	public String BioDeg_LogHalfLife_Exp_neighbor_2;
	public String BioDeg_LogHalfLife_Exp_neighbor_3;
	public String BioDeg_LogHalfLife_Exp_neighbor_4;
	public String BioDeg_LogHalfLife_Exp_neighbor_5;
	public String BioDeg_LogHalfLife_pred_neighbor_1;
	public String BioDeg_LogHalfLife_pred_neighbor_2;
	public String BioDeg_LogHalfLife_pred_neighbor_3;
	public String BioDeg_LogHalfLife_pred_neighbor_4;
	public String BioDeg_LogHalfLife_pred_neighbor_5;
	public String BP_exp;
	public String BP_pred;
	public String BP_predRange;
	public String AD_BP;
	public String AD_index_BP;
	public String Conf_index_BP;
	public String BP_CAS_neighbor_1;
	public String BP_CAS_neighbor_2;
	public String BP_CAS_neighbor_3;
	public String BP_CAS_neighbor_4;
	public String BP_CAS_neighbor_5;
	public String BP_InChiKey_neighbor_1;
	public String BP_InChiKey_neighbor_2;
	public String BP_InChiKey_neighbor_3;
	public String BP_InChiKey_neighbor_4;
	public String BP_InChiKey_neighbor_5;
	public String BP_DTXSID_neighbor_1;
	public String BP_DTXSID_neighbor_2;
	public String BP_DTXSID_neighbor_3;
	public String BP_DTXSID_neighbor_4;
	public String BP_DTXSID_neighbor_5;
	public String BP_DSSTOXMPID_neighbor_1;
	public String BP_DSSTOXMPID_neighbor_2;
	public String BP_DSSTOXMPID_neighbor_3;
	public String BP_DSSTOXMPID_neighbor_4;
	public String BP_DSSTOXMPID_neighbor_5;
	public String BP_Exp_neighbor_1;
	public String BP_Exp_neighbor_2;
	public String BP_Exp_neighbor_3;
	public String BP_Exp_neighbor_4;
	public String BP_Exp_neighbor_5;
	public String BP_pred_neighbor_1;
	public String BP_pred_neighbor_2;
	public String BP_pred_neighbor_3;
	public String BP_pred_neighbor_4;
	public String BP_pred_neighbor_5;
	public String CATMoS_VT_pred;
	public String CATMoS_NT_pred;
	public String CATMoS_EPA_pred;
	public String CATMoS_GHS_pred;
	public String CATMoS_LD50_exp;
	public String CATMoS_LD50_pred;
	public String CATMoS_LD50_predRange;
	public String AD_CATMoS;
	public String AD_index_CATMoS;
	public String Conf_index_CATMoS;
	public String CAS_neighbor_1;
	public String CAS_neighbor_2;
	public String CAS_neighbor_3;
	public String CAS_neighbor_4;
	public String CAS_neighbor_5;
	public String InChiKey_neighbor_1;
	public String InChiKey_neighbor_2;
	public String InChiKey_neighbor_3;
	public String InChiKey_neighbor_4;
	public String InChiKey_neighbor_5;
	public String DTXSID_neighbor_1;
	public String DTXSID_neighbor_2;
	public String DTXSID_neighbor_3;
	public String DTXSID_neighbor_4;
	public String DTXSID_neighbor_5;
	public String LD50_Exp_neighbor_1;
	public String LD50_Exp_neighbor_2;
	public String LD50_Exp_neighbor_3;
	public String LD50_Exp_neighbor_4;
	public String LD50_Exp_neighbor_5;
	public String LD50_pred_neighbor_1;
	public String LD50_pred_neighbor_2;
	public String LD50_pred_neighbor_3;
	public String LD50_pred_neighbor_4;
	public String LD50_pred_neighbor_5;
	public String CERAPP_Ago_exp;
	public String CERAPP_Ago_pred;
	public String AD_CERAPP_Ago;
	public String AD_index_CERAPP_Ago;
	public String Conf_index_CERAPP_Ago;
	public String CERAPP_Anta_exp;
	public String CERAPP_Anta_pred;
	public String AD_CERAPP_Anta;
	public String AD_index_CERAPP_Anta;
	public String Conf_index_CERAPP_Anta;
	public String CERAPP_Bind_exp;
	public String CERAPP_Bind_pred;
	public String AD_CERAPP_Bind;
	public String AD_index_CERAPP_Bind;
	public String Conf_index_CERAPP_Bind;
	public String CERAPP_Ago_CAS_neighbor_1;
	public String CERAPP_Ago_CAS_neighbor_2;
	public String CERAPP_Ago_CAS_neighbor_3;
	public String CERAPP_Ago_CAS_neighbor_4;
	public String CERAPP_Ago_CAS_neighbor_5;
	public String CERAPP_Ago_InChiKey_neighbor_1;
	public String CERAPP_Ago_InChiKey_neighbor_2;
	public String CERAPP_Ago_InChiKey_neighbor_3;
	public String CERAPP_Ago_InChiKey_neighbor_4;
	public String CERAPP_Ago_InChiKey_neighbor_5;
	public String CERAPP_Ago_DTXSID_neighbor_1;
	public String CERAPP_Ago_DTXSID_neighbor_2;
	public String CERAPP_Ago_DTXSID_neighbor_3;
	public String CERAPP_Ago_DTXSID_neighbor_4;
	public String CERAPP_Ago_DTXSID_neighbor_5;
	public String CERAPP_Ago_Exp_neighbor_1;
	public String CERAPP_Ago_Exp_neighbor_2;
	public String CERAPP_Ago_Exp_neighbor_3;
	public String CERAPP_Ago_Exp_neighbor_4;
	public String CERAPP_Ago_Exp_neighbor_5;
	public String CERAPP_Ago_pred_neighbor_1;
	public String CERAPP_Ago_pred_neighbor_2;
	public String CERAPP_Ago_pred_neighbor_3;
	public String CERAPP_Ago_pred_neighbor_4;
	public String CERAPP_Ago_pred_neighbor_5;
	public String CERAPP_Anta_CAS_neighbor_1;
	public String CERAPP_Anta_CAS_neighbor_2;
	public String CERAPP_Anta_CAS_neighbor_3;
	public String CERAPP_Anta_CAS_neighbor_4;
	public String CERAPP_Anta_CAS_neighbor_5;
	public String CERAPP_Anta_InChiKey_neighbor_1;
	public String CERAPP_Anta_InChiKey_neighbor_2;
	public String CERAPP_Anta_InChiKey_neighbor_3;
	public String CERAPP_Anta_InChiKey_neighbor_4;
	public String CERAPP_Anta_InChiKey_neighbor_5;
	public String CERAPP_Anta_DTXSID_neighbor_1;
	public String CERAPP_Anta_DTXSID_neighbor_2;
	public String CERAPP_Anta_DTXSID_neighbor_3;
	public String CERAPP_Anta_DTXSID_neighbor_4;
	public String CERAPP_Anta_DTXSID_neighbor_5;
	public String CERAPP_Anta_Exp_neighbor_1;
	public String CERAPP_Anta_Exp_neighbor_2;
	public String CERAPP_Anta_Exp_neighbor_3;
	public String CERAPP_Anta_Exp_neighbor_4;
	public String CERAPP_Anta_Exp_neighbor_5;
	public String CERAPP_Anta_pred_neighbor_1;
	public String CERAPP_Anta_pred_neighbor_2;
	public String CERAPP_Anta_pred_neighbor_3;
	public String CERAPP_Anta_pred_neighbor_4;
	public String CERAPP_Anta_pred_neighbor_5;
	public String CERAPP_Bind_CAS_neighbor_1;
	public String CERAPP_Bind_CAS_neighbor_2;
	public String CERAPP_Bind_CAS_neighbor_3;
	public String CERAPP_Bind_CAS_neighbor_4;
	public String CERAPP_Bind_CAS_neighbor_5;
	public String CERAPP_Bind_InChiKey_neighbor_1;
	public String CERAPP_Bind_InChiKey_neighbor_2;
	public String CERAPP_Bind_InChiKey_neighbor_3;
	public String CERAPP_Bind_InChiKey_neighbor_4;
	public String CERAPP_Bind_InChiKey_neighbor_5;
	public String CERAPP_Bind_DTXSID_neighbor_1;
	public String CERAPP_Bind_DTXSID_neighbor_2;
	public String CERAPP_Bind_DTXSID_neighbor_3;
	public String CERAPP_Bind_DTXSID_neighbor_4;
	public String CERAPP_Bind_DTXSID_neighbor_5;
	public String CERAPP_Bind_Exp_neighbor_1;
	public String CERAPP_Bind_Exp_neighbor_2;
	public String CERAPP_Bind_Exp_neighbor_3;
	public String CERAPP_Bind_Exp_neighbor_4;
	public String CERAPP_Bind_Exp_neighbor_5;
	public String CERAPP_Bind_pred_neighbor_1;
	public String CERAPP_Bind_pred_neighbor_2;
	public String CERAPP_Bind_pred_neighbor_3;
	public String CERAPP_Bind_pred_neighbor_4;
	public String CERAPP_Bind_pred_neighbor_5;
	public String Clint_exp;
	public String Clint_pred;
	public String Clint_predRange;
	public String AD_Clint;
	public String AD_index_Clint;
	public String Conf_index_Clint;
	public String Clint_CAS_neighbor_1;
	public String Clint_CAS_neighbor_2;
	public String Clint_CAS_neighbor_3;
	public String Clint_CAS_neighbor_4;
	public String Clint_CAS_neighbor_5;
	public String Clint_InChiKey_neighbor_1;
	public String Clint_InChiKey_neighbor_2;
	public String Clint_InChiKey_neighbor_3;
	public String Clint_InChiKey_neighbor_4;
	public String Clint_InChiKey_neighbor_5;
	public String Clint_DTXSID_neighbor_1;
	public String Clint_DTXSID_neighbor_2;
	public String Clint_DTXSID_neighbor_3;
	public String Clint_DTXSID_neighbor_4;
	public String Clint_DTXSID_neighbor_5;
	public String Clint_Exp_neighbor_1;
	public String Clint_Exp_neighbor_2;
	public String Clint_Exp_neighbor_3;
	public String Clint_Exp_neighbor_4;
	public String Clint_Exp_neighbor_5;
	public String Clint_pred_neighbor_1;
	public String Clint_pred_neighbor_2;
	public String Clint_pred_neighbor_3;
	public String Clint_pred_neighbor_4;
	public String Clint_pred_neighbor_5;
	public String CoMPARA_Ago_exp;
	public String CoMPARA_Ago_pred;
	public String AD_CoMPARA_Ago;
	public String AD_index_CoMPARA_Ago;
	public String Conf_index_CoMPARA_Ago;
	public String CoMPARA_Anta_exp;
	public String CoMPARA_Anta_pred;
	public String AD_CoMPARA_Anta;
	public String AD_index_CoMPARA_Anta;
	public String Conf_index_CoMPARA_Anta;
	public String CoMPARA_Bind_exp;
	public String CoMPARA_Bind_pred;
	public String AD_CoMPARA_Bind;
	public String AD_index_CoMPARA_Bind;
	public String Conf_index_CoMPARA_Bind;
	public String CoMPARA_Ago_CAS_neighbor_1;
	public String CoMPARA_Ago_CAS_neighbor_2;
	public String CoMPARA_Ago_CAS_neighbor_3;
	public String CoMPARA_Ago_CAS_neighbor_4;
	public String CoMPARA_Ago_CAS_neighbor_5;
	public String CoMPARA_Ago_InChiKey_neighbor_1;
	public String CoMPARA_Ago_InChiKey_neighbor_2;
	public String CoMPARA_Ago_InChiKey_neighbor_3;
	public String CoMPARA_Ago_InChiKey_neighbor_4;
	public String CoMPARA_Ago_InChiKey_neighbor_5;
	public String CoMPARA_Ago_DTXSID_neighbor_1;
	public String CoMPARA_Ago_DTXSID_neighbor_2;
	public String CoMPARA_Ago_DTXSID_neighbor_3;
	public String CoMPARA_Ago_DTXSID_neighbor_4;
	public String CoMPARA_Ago_DTXSID_neighbor_5;
	public String CoMPARA_Ago_Exp_neighbor_1;
	public String CoMPARA_Ago_Exp_neighbor_2;
	public String CoMPARA_Ago_Exp_neighbor_3;
	public String CoMPARA_Ago_Exp_neighbor_4;
	public String CoMPARA_Ago_Exp_neighbor_5;
	public String CoMPARA_Ago_pred_neighbor_1;
	public String CoMPARA_Ago_pred_neighbor_2;
	public String CoMPARA_Ago_pred_neighbor_3;
	public String CoMPARA_Ago_pred_neighbor_4;
	public String CoMPARA_Ago_pred_neighbor_5;
	public String CoMPARA_Anta_CAS_neighbor_1;
	public String CoMPARA_Anta_CAS_neighbor_2;
	public String CoMPARA_Anta_CAS_neighbor_3;
	public String CoMPARA_Anta_CAS_neighbor_4;
	public String CoMPARA_Anta_CAS_neighbor_5;
	public String CoMPARA_Anta_InChiKey_neighbor_1;
	public String CoMPARA_Anta_InChiKey_neighbor_2;
	public String CoMPARA_Anta_InChiKey_neighbor_3;
	public String CoMPARA_Anta_InChiKey_neighbor_4;
	public String CoMPARA_Anta_InChiKey_neighbor_5;
	public String CoMPARA_Anta_DTXSID_neighbor_1;
	public String CoMPARA_Anta_DTXSID_neighbor_2;
	public String CoMPARA_Anta_DTXSID_neighbor_3;
	public String CoMPARA_Anta_DTXSID_neighbor_4;
	public String CoMPARA_Anta_DTXSID_neighbor_5;
	public String CoMPARA_Anta_Exp_neighbor_1;
	public String CoMPARA_Anta_Exp_neighbor_2;
	public String CoMPARA_Anta_Exp_neighbor_3;
	public String CoMPARA_Anta_Exp_neighbor_4;
	public String CoMPARA_Anta_Exp_neighbor_5;
	public String CoMPARA_Anta_pred_neighbor_1;
	public String CoMPARA_Anta_pred_neighbor_2;
	public String CoMPARA_Anta_pred_neighbor_3;
	public String CoMPARA_Anta_pred_neighbor_4;
	public String CoMPARA_Anta_pred_neighbor_5;
	public String CoMPARA_Bind_CAS_neighbor_1;
	public String CoMPARA_Bind_CAS_neighbor_2;
	public String CoMPARA_Bind_CAS_neighbor_3;
	public String CoMPARA_Bind_CAS_neighbor_4;
	public String CoMPARA_Bind_CAS_neighbor_5;
	public String CoMPARA_Bind_InChiKey_neighbor_1;
	public String CoMPARA_Bind_InChiKey_neighbor_2;
	public String CoMPARA_Bind_InChiKey_neighbor_3;
	public String CoMPARA_Bind_InChiKey_neighbor_4;
	public String CoMPARA_Bind_InChiKey_neighbor_5;
	public String CoMPARA_Bind_DTXSID_neighbor_1;
	public String CoMPARA_Bind_DTXSID_neighbor_2;
	public String CoMPARA_Bind_DTXSID_neighbor_3;
	public String CoMPARA_Bind_DTXSID_neighbor_4;
	public String CoMPARA_Bind_DTXSID_neighbor_5;
	public String CoMPARA_Bind_Exp_neighbor_1;
	public String CoMPARA_Bind_Exp_neighbor_2;
	public String CoMPARA_Bind_Exp_neighbor_3;
	public String CoMPARA_Bind_Exp_neighbor_4;
	public String CoMPARA_Bind_Exp_neighbor_5;
	public String CoMPARA_Bind_pred_neighbor_1;
	public String CoMPARA_Bind_pred_neighbor_2;
	public String CoMPARA_Bind_pred_neighbor_3;
	public String CoMPARA_Bind_pred_neighbor_4;
	public String CoMPARA_Bind_pred_neighbor_5;
	public String FUB_exp;
	public String FUB_pred;
	public String FUB_predRange;
	public String AD_FUB;
	public String AD_index_FUB;
	public String Conf_index_FUB;
	public String FUB_CAS_neighbor_1;
	public String FUB_CAS_neighbor_2;
	public String FUB_CAS_neighbor_3;
	public String FUB_CAS_neighbor_4;
	public String FUB_CAS_neighbor_5;
	public String FUB_DTXSID_neighbor_1;
	public String FUB_DTXSID_neighbor_2;
	public String FUB_DTXSID_neighbor_3;
	public String FUB_DTXSID_neighbor_4;
	public String FUB_DTXSID_neighbor_5;
	public String FUB_Exp_neighbor_1;
	public String FUB_Exp_neighbor_2;
	public String FUB_Exp_neighbor_3;
	public String FUB_Exp_neighbor_4;
	public String FUB_Exp_neighbor_5;
	public String FUB_pred_neighbor_1;
	public String FUB_pred_neighbor_2;
	public String FUB_pred_neighbor_3;
	public String FUB_pred_neighbor_4;
	public String FUB_pred_neighbor_5;
	public String LogHL_exp;
	public String LogHL_pred;
	public String HL_predRange;
	public String AD_HL;
	public String AD_index_HL;
	public String Conf_index_HL;
	public String HL_CAS_neighbor_1;
	public String HL_CAS_neighbor_2;
	public String HL_CAS_neighbor_3;
	public String HL_CAS_neighbor_4;
	public String HL_CAS_neighbor_5;
	public String HL_InChiKey_neighbor_1;
	public String HL_InChiKey_neighbor_2;
	public String HL_InChiKey_neighbor_3;
	public String HL_InChiKey_neighbor_4;
	public String HL_InChiKey_neighbor_5;
	public String HL_DTXSID_neighbor_1;
	public String HL_DTXSID_neighbor_2;
	public String HL_DTXSID_neighbor_3;
	public String HL_DTXSID_neighbor_4;
	public String HL_DTXSID_neighbor_5;
	public String HL_DSSTOXMPID_neighbor_1;
	public String HL_DSSTOXMPID_neighbor_2;
	public String HL_DSSTOXMPID_neighbor_3;
	public String HL_DSSTOXMPID_neighbor_4;
	public String HL_DSSTOXMPID_neighbor_5;
	public String LogHL_Exp_neighbor_1;
	public String LogHL_Exp_neighbor_2;
	public String LogHL_Exp_neighbor_3;
	public String LogHL_Exp_neighbor_4;
	public String LogHL_Exp_neighbor_5;
	public String LogHL_pred_neighbor_1;
	public String LogHL_pred_neighbor_2;
	public String LogHL_pred_neighbor_3;
	public String LogHL_pred_neighbor_4;
	public String LogHL_pred_neighbor_5;
	public String LogKM_exp;
	public String LogKM_pred;
	public String KM_predRange;
	public String AD_KM;
	public String AD_index_KM;
	public String Conf_index_KM;
	public String KM_CAS_neighbor_1;
	public String KM_CAS_neighbor_2;
	public String KM_CAS_neighbor_3;
	public String KM_CAS_neighbor_4;
	public String KM_CAS_neighbor_5;
	public String KM_InChiKey_neighbor_1;
	public String KM_InChiKey_neighbor_2;
	public String KM_InChiKey_neighbor_3;
	public String KM_InChiKey_neighbor_4;
	public String KM_InChiKey_neighbor_5;
	public String KM_DTXSID_neighbor_1;
	public String KM_DTXSID_neighbor_2;
	public String KM_DTXSID_neighbor_3;
	public String KM_DTXSID_neighbor_4;
	public String KM_DTXSID_neighbor_5;
	public String KM_DSSTOXMPID_neighbor_1;
	public String KM_DSSTOXMPID_neighbor_2;
	public String KM_DSSTOXMPID_neighbor_3;
	public String KM_DSSTOXMPID_neighbor_4;
	public String KM_DSSTOXMPID_neighbor_5;
	public String LogKM_Exp_neighbor_1;
	public String LogKM_Exp_neighbor_2;
	public String LogKM_Exp_neighbor_3;
	public String LogKM_Exp_neighbor_4;
	public String LogKM_Exp_neighbor_5;
	public String LogKM_pred_neighbor_1;
	public String LogKM_pred_neighbor_2;
	public String LogKM_pred_neighbor_3;
	public String LogKM_pred_neighbor_4;
	public String LogKM_pred_neighbor_5;
	public String LogKOA_exp;
	public String LogKOA_pred;
	public String KOA_predRange;
	public String AD_KOA;
	public String AD_index_KOA;
	public String Conf_index_KOA;
	public String KOA_CAS_neighbor_1;
	public String KOA_CAS_neighbor_2;
	public String KOA_CAS_neighbor_3;
	public String KOA_CAS_neighbor_4;
	public String KOA_CAS_neighbor_5;
	public String KOA_InChiKey_neighbor_1;
	public String KOA_InChiKey_neighbor_2;
	public String KOA_InChiKey_neighbor_3;
	public String KOA_InChiKey_neighbor_4;
	public String KOA_InChiKey_neighbor_5;
	public String KOA_DTXSID_neighbor_1;
	public String KOA_DTXSID_neighbor_2;
	public String KOA_DTXSID_neighbor_3;
	public String KOA_DTXSID_neighbor_4;
	public String KOA_DTXSID_neighbor_5;
	public String KOA_DSSTOXMPID_neighbor_1;
	public String KOA_DSSTOXMPID_neighbor_2;
	public String KOA_DSSTOXMPID_neighbor_3;
	public String KOA_DSSTOXMPID_neighbor_4;
	public String KOA_DSSTOXMPID_neighbor_5;
	public String LogKOA_Exp_neighbor_1;
	public String LogKOA_Exp_neighbor_2;
	public String LogKOA_Exp_neighbor_3;
	public String LogKOA_Exp_neighbor_4;
	public String LogKOA_Exp_neighbor_5;
	public String LogKOA_pred_neighbor_1;
	public String LogKOA_pred_neighbor_2;
	public String LogKOA_pred_neighbor_3;
	public String LogKOA_pred_neighbor_4;
	public String LogKOA_pred_neighbor_5;
	public String LogKoc_exp;
	public String LogKoc_pred;
	public String Koc_predRange;
	public String AD_Koc;
	public String AD_index_Koc;
	public String Conf_index_Koc;
	public String Koc_CAS_neighbor_1;
	public String Koc_CAS_neighbor_2;
	public String Koc_CAS_neighbor_3;
	public String Koc_CAS_neighbor_4;
	public String Koc_CAS_neighbor_5;
	public String Koc_InChiKey_neighbor_1;
	public String Koc_InChiKey_neighbor_2;
	public String Koc_InChiKey_neighbor_3;
	public String Koc_InChiKey_neighbor_4;
	public String Koc_InChiKey_neighbor_5;
	public String Koc_DTXSID_neighbor_1;
	public String Koc_DTXSID_neighbor_2;
	public String Koc_DTXSID_neighbor_3;
	public String Koc_DTXSID_neighbor_4;
	public String Koc_DTXSID_neighbor_5;
	public String Koc_DSSTOXMPID_neighbor_1;
	public String Koc_DSSTOXMPID_neighbor_2;
	public String Koc_DSSTOXMPID_neighbor_3;
	public String Koc_DSSTOXMPID_neighbor_4;
	public String Koc_DSSTOXMPID_neighbor_5;
	public String LogKoc_Exp_neighbor_1;
	public String LogKoc_Exp_neighbor_2;
	public String LogKoc_Exp_neighbor_3;
	public String LogKoc_Exp_neighbor_4;
	public String LogKoc_Exp_neighbor_5;
	public String LogKoc_pred_neighbor_1;
	public String LogKoc_pred_neighbor_2;
	public String LogKoc_pred_neighbor_3;
	public String LogKoc_pred_neighbor_4;
	public String LogKoc_pred_neighbor_5;
	public String LogD55_pred;
	public String LogD55_predRange;
	public String LogD74_pred;
	public String LogD74_predRange;
	public String AD_LogD;
	public String AD_index_LogD;
	public String Conf_index_LogD;
	public String LogD_CAS_neighbor_1;
	public String LogD_CAS_neighbor_2;
	public String LogD_CAS_neighbor_3;
	public String LogD_CAS_neighbor_4;
	public String LogD_CAS_neighbor_5;
	public String LogD_InChiKey_neighbor_1;
	public String LogD_InChiKey_neighbor_2;
	public String LogD_InChiKey_neighbor_3;
	public String LogD_InChiKey_neighbor_4;
	public String LogD_InChiKey_neighbor_5;
	public String LogD_DTXSID_neighbor_1;
	public String LogD_DTXSID_neighbor_2;
	public String LogD_DTXSID_neighbor_3;
	public String LogD_DTXSID_neighbor_4;
	public String LogD_DTXSID_neighbor_5;
	public String LogP_exp;
	public String LogP_pred;
	public String LogP_predRange;
	public String AD_LogP;
	public String AD_index_LogP;
	public String Conf_index_LogP;
	public String LogP_CAS_neighbor_1;
	public String LogP_CAS_neighbor_2;
	public String LogP_CAS_neighbor_3;
	public String LogP_CAS_neighbor_4;
	public String LogP_CAS_neighbor_5;
	public String LogP_InChiKey_neighbor_1;
	public String LogP_InChiKey_neighbor_2;
	public String LogP_InChiKey_neighbor_3;
	public String LogP_InChiKey_neighbor_4;
	public String LogP_InChiKey_neighbor_5;
	public String LogP_DTXSID_neighbor_1;
	public String LogP_DTXSID_neighbor_2;
	public String LogP_DTXSID_neighbor_3;
	public String LogP_DTXSID_neighbor_4;
	public String LogP_DTXSID_neighbor_5;
	public String LogP_DSSTOXMPID_neighbor_1;
	public String LogP_DSSTOXMPID_neighbor_2;
	public String LogP_DSSTOXMPID_neighbor_3;
	public String LogP_DSSTOXMPID_neighbor_4;
	public String LogP_DSSTOXMPID_neighbor_5;
	public String LogP_Exp_neighbor_1;
	public String LogP_Exp_neighbor_2;
	public String LogP_Exp_neighbor_3;
	public String LogP_Exp_neighbor_4;
	public String LogP_Exp_neighbor_5;
	public String LogP_pred_neighbor_1;
	public String LogP_pred_neighbor_2;
	public String LogP_pred_neighbor_3;
	public String LogP_pred_neighbor_4;
	public String LogP_pred_neighbor_5;
	public String MP_exp;
	public String MP_pred;
	public String MP_predRange;
	public String AD_MP;
	public String AD_index_MP;
	public String Conf_index_MP;
	public String MP_CAS_neighbor_1;
	public String MP_CAS_neighbor_2;
	public String MP_CAS_neighbor_3;
	public String MP_CAS_neighbor_4;
	public String MP_CAS_neighbor_5;
	public String MP_InChiKey_neighbor_1;
	public String MP_InChiKey_neighbor_2;
	public String MP_InChiKey_neighbor_3;
	public String MP_InChiKey_neighbor_4;
	public String MP_InChiKey_neighbor_5;
	public String MP_DTXSID_neighbor_1;
	public String MP_DTXSID_neighbor_2;
	public String MP_DTXSID_neighbor_3;
	public String MP_DTXSID_neighbor_4;
	public String MP_DTXSID_neighbor_5;
	public String MP_DSSTOXMPID_neighbor_1;
	public String MP_DSSTOXMPID_neighbor_2;
	public String MP_DSSTOXMPID_neighbor_3;
	public String MP_DSSTOXMPID_neighbor_4;
	public String MP_DSSTOXMPID_neighbor_5;
	public String MP_Exp_neighbor_1;
	public String MP_Exp_neighbor_2;
	public String MP_Exp_neighbor_3;
	public String MP_Exp_neighbor_4;
	public String MP_Exp_neighbor_5;
	public String MP_pred_neighbor_1;
	public String MP_pred_neighbor_2;
	public String MP_pred_neighbor_3;
	public String MP_pred_neighbor_4;
	public String MP_pred_neighbor_5;
	public String pKa_a_exp;
	public String pKa_b_exp;
	public String ionization;
	public String pKa_a_pred;
	public String pKa_a_predRange;
	public String pKa_b_pred;
	public String pKa_b_predRange;
	public String AD_pKa;
	public String AD_index_pKa;
	public String Conf_index_pKa;
	public String pKa_CAS_neighbor_1;
	public String pKa_CAS_neighbor_2;
	public String pKa_CAS_neighbor_3;
	public String pKa_InChiKey_neighbor_1;
	public String pKa_InChiKey_neighbor_2;
	public String pKa_InChiKey_neighbor_3;
	public String pKa_DTXSID_neighbor_1;
	public String pKa_DTXSID_neighbor_2;
	public String pKa_DTXSID_neighbor_3;
	public String pKa_DSSTOXMPID_neighbor_1;
	public String pKa_DSSTOXMPID_neighbor_2;
	public String pKa_DSSTOXMPID_neighbor_3;
	public String pKa_Exp_neighbor_1;
	public String pKa_Exp_neighbor_2;
	public String pKa_Exp_neighbor_3;
	public String pKa_pred_neighbor_1;
	public String pKa_pred_neighbor_2;
	public String pKa_pred_neighbor_3;
	public String ReadyBiodeg_exp;
	public String ReadyBiodeg_pred;
	public String AD_ReadyBiodeg;
	public String AD_index_ReadyBiodeg;
	public String Conf_index_ReadyBiodeg;
	public String ReadyBiodeg_CAS_neighbor_1;
	public String ReadyBiodeg_CAS_neighbor_2;
	public String ReadyBiodeg_CAS_neighbor_3;
	public String ReadyBiodeg_CAS_neighbor_4;
	public String ReadyBiodeg_CAS_neighbor_5;
	public String ReadyBiodeg_InChiKey_neighbor_1;
	public String ReadyBiodeg_InChiKey_neighbor_2;
	public String ReadyBiodeg_InChiKey_neighbor_3;
	public String ReadyBiodeg_InChiKey_neighbor_4;
	public String ReadyBiodeg_InChiKey_neighbor_5;
	public String ReadyBiodeg_DTXSID_neighbor_1;
	public String ReadyBiodeg_DTXSID_neighbor_2;
	public String ReadyBiodeg_DTXSID_neighbor_3;
	public String ReadyBiodeg_DTXSID_neighbor_4;
	public String ReadyBiodeg_DTXSID_neighbor_5;
	public String ReadyBiodeg_DSSTOXMPID_neighbor_1;
	public String ReadyBiodeg_DSSTOXMPID_neighbor_2;
	public String ReadyBiodeg_DSSTOXMPID_neighbor_3;
	public String ReadyBiodeg_DSSTOXMPID_neighbor_4;
	public String ReadyBiodeg_DSSTOXMPID_neighbor_5;
	public String ReadyBiodeg_Exp_neighbor_1;
	public String ReadyBiodeg_Exp_neighbor_2;
	public String ReadyBiodeg_Exp_neighbor_3;
	public String ReadyBiodeg_Exp_neighbor_4;
	public String ReadyBiodeg_Exp_neighbor_5;
	public String ReadyBiodeg_pred_neighbor_1;
	public String ReadyBiodeg_pred_neighbor_2;
	public String ReadyBiodeg_pred_neighbor_3;
	public String ReadyBiodeg_pred_neighbor_4;
	public String ReadyBiodeg_pred_neighbor_5;
	public String RT_exp;
	public String RT_pred;
	public String RT_predRange;
	public String AD_RT;
	public String AD_index_RT;
	public String Conf_index_RT;
	public String RT_CAS_neighbor_1;
	public String RT_CAS_neighbor_2;
	public String RT_CAS_neighbor_3;
	public String RT_CAS_neighbor_4;
	public String RT_CAS_neighbor_5;
	public String RT_DTXSID_neighbor_1;
	public String RT_DTXSID_neighbor_2;
	public String RT_DTXSID_neighbor_3;
	public String RT_DTXSID_neighbor_4;
	public String RT_DTXSID_neighbor_5;
	public String RT_Exp_neighbor_1;
	public String RT_Exp_neighbor_2;
	public String RT_Exp_neighbor_3;
	public String RT_Exp_neighbor_4;
	public String RT_Exp_neighbor_5;
	public String RT_pred_neighbor_1;
	public String RT_pred_neighbor_2;
	public String RT_pred_neighbor_3;
	public String RT_pred_neighbor_4;
	public String RT_pred_neighbor_5;
	public String MolWeight;
	public String nbAtoms;
	public String nbHeavyAtoms;
	public String nbC;
	public String nbO;
	public String nbN;
	public String nbAromAtom;
	public String nbRing;
	public String nbHeteroRing;
	public String Sp3Sp2HybRatio;
	public String nbRotBd;
	public String nbHBdAcc;
	public String ndHBdDon;
	public String nbLipinskiFailures;
	public String TopoPolSurfAir;
	public String MolarRefract;
	public String CombDipolPolariz;
	public String LogVP_exp;
	public String LogVP_pred;
	public String VP_predRange;
	public String AD_VP;
	public String AD_index_VP;
	public String Conf_index_VP;
	public String LogVP_CAS_neighbor_1;
	public String LogVP_CAS_neighbor_2;
	public String LogVP_CAS_neighbor_3;
	public String LogVP_CAS_neighbor_4;
	public String LogVP_CAS_neighbor_5;
	public String LogVP_InChiKey_neighbor_1;
	public String LogVP_InChiKey_neighbor_2;
	public String LogVP_InChiKey_neighbor_3;
	public String LogVP_InChiKey_neighbor_4;
	public String LogVP_InChiKey_neighbor_5;
	public String LogVP_DTXSID_neighbor_1;
	public String LogVP_DTXSID_neighbor_2;
	public String LogVP_DTXSID_neighbor_3;
	public String LogVP_DTXSID_neighbor_4;
	public String LogVP_DTXSID_neighbor_5;
	public String LogVP_DSSTOXMPID_neighbor_1;
	public String LogVP_DSSTOXMPID_neighbor_2;
	public String LogVP_DSSTOXMPID_neighbor_3;
	public String LogVP_DSSTOXMPID_neighbor_4;
	public String LogVP_DSSTOXMPID_neighbor_5;
	public String LogVP_Exp_neighbor_1;
	public String LogVP_Exp_neighbor_2;
	public String LogVP_Exp_neighbor_3;
	public String LogVP_Exp_neighbor_4;
	public String LogVP_Exp_neighbor_5;
	public String LogVP_pred_neighbor_1;
	public String LogVP_pred_neighbor_2;
	public String LogVP_pred_neighbor_3;
	public String LogVP_pred_neighbor_4;
	public String LogVP_pred_neighbor_5;
	public String LogWS_exp;
	public String LogWS_pred;
	public String WS_predRange;
	public String AD_WS;
	public String AD_index_WS;
	public String Conf_index_WS;
	public String LogWS_CAS_neighbor_1;
	public String LogWS_CAS_neighbor_2;
	public String LogWS_CAS_neighbor_3;
	public String LogWS_CAS_neighbor_4;
	public String LogWS_CAS_neighbor_5;
	public String LogWS_InChiKey_neighbor_1;
	public String LogWS_InChiKey_neighbor_2;
	public String LogWS_InChiKey_neighbor_3;
	public String LogWS_InChiKey_neighbor_4;
	public String LogWS_InChiKey_neighbor_5;
	public String LogWS_DTXSID_neighbor_1;
	public String LogWS_DTXSID_neighbor_2;
	public String LogWS_DTXSID_neighbor_3;
	public String LogWS_DTXSID_neighbor_4;
	public String LogWS_DTXSID_neighbor_5;
	public String LogWS_DSSTOXMPID_neighbor_1;
	public String LogWS_DSSTOXMPID_neighbor_2;
	public String LogWS_DSSTOXMPID_neighbor_3;
	public String LogWS_DSSTOXMPID_neighbor_4;
	public String LogWS_DSSTOXMPID_neighbor_5;
	public String LogWS_Exp_neighbor_1;
	public String LogWS_Exp_neighbor_2;
	public String LogWS_Exp_neighbor_3;
	public String LogWS_Exp_neighbor_4;
	public String LogWS_Exp_neighbor_5;
	public String LogWS_pred_neighbor_1;
	public String LogWS_pred_neighbor_2;
	public String LogWS_pred_neighbor_3;
	public String LogWS_pred_neighbor_4;
	public String LogWS_pred_neighbor_5;


	public static final String[] fieldNames = { "DSSTOX_COMPOUND_ID", "LogOH_exp", "LogOH_pred", "LogOH_predRange",
			"AD_AOH", "AD_index_AOH", "Conf_index_AOH", "AOH_CAS_neighbor_1", "AOH_CAS_neighbor_2",
			"AOH_CAS_neighbor_3", "AOH_CAS_neighbor_4", "AOH_CAS_neighbor_5", "AOH_InChiKey_neighbor_1",
			"AOH_InChiKey_neighbor_2", "AOH_InChiKey_neighbor_3", "AOH_InChiKey_neighbor_4", "AOH_InChiKey_neighbor_5",
			"AOH_DTXSID_neighbor_1", "AOH_DTXSID_neighbor_2", "AOH_DTXSID_neighbor_3", "AOH_DTXSID_neighbor_4",
			"AOH_DTXSID_neighbor_5", "AOH_DSSTOXMPID_neighbor_1", "AOH_DSSTOXMPID_neighbor_2",
			"AOH_DSSTOXMPID_neighbor_3", "AOH_DSSTOXMPID_neighbor_4", "AOH_DSSTOXMPID_neighbor_5",
			"LogOH_Exp_neighbor_1", "LogOH_Exp_neighbor_2", "LogOH_Exp_neighbor_3", "LogOH_Exp_neighbor_4",
			"LogOH_Exp_neighbor_5", "LogOH_pred_neighbor_1", "LogOH_pred_neighbor_2", "LogOH_pred_neighbor_3",
			"LogOH_pred_neighbor_4", "LogOH_pred_neighbor_5", "LogBCF_exp", "LogBCF_pred", "BCF_predRange", "AD_BCF",
			"AD_index_BCF", "Conf_index_BCF", "LogBCF_CAS_neighbor_1", "LogBCF_CAS_neighbor_2", "LogBCF_CAS_neighbor_3",
			"LogBCF_CAS_neighbor_4", "LogBCF_CAS_neighbor_5", "LogBCF_InChiKey_neighbor_1",
			"LogBCF_InChiKey_neighbor_2", "LogBCF_InChiKey_neighbor_3", "LogBCF_InChiKey_neighbor_4",
			"LogBCF_InChiKey_neighbor_5", "LogBCF_DTXSID_neighbor_1", "LogBCF_DTXSID_neighbor_2",
			"LogBCF_DTXSID_neighbor_3", "LogBCF_DTXSID_neighbor_4", "LogBCF_DTXSID_neighbor_5",
			"LogBCF_DSSTOXMPID_neighbor_1", "LogBCF_DSSTOXMPID_neighbor_2", "LogBCF_DSSTOXMPID_neighbor_3",
			"LogBCF_DSSTOXMPID_neighbor_4", "LogBCF_DSSTOXMPID_neighbor_5", "LogBCF_Exp_neighbor_1",
			"LogBCF_Exp_neighbor_2", "LogBCF_Exp_neighbor_3", "LogBCF_Exp_neighbor_4", "LogBCF_Exp_neighbor_5",
			"LogBCF_pred_neighbor_1", "LogBCF_pred_neighbor_2", "LogBCF_pred_neighbor_3", "LogBCF_pred_neighbor_4",
			"LogBCF_pred_neighbor_5", "BioDeg_exp", "BioDeg_LogHalfLife_pred", "BioDeg_predRange", "AD_BioDeg",
			"AD_index_BioDeg", "Conf_index_BioDeg", "BioDeg_CAS_neighbor_1", "BioDeg_CAS_neighbor_2",
			"BioDeg_CAS_neighbor_3", "BioDeg_CAS_neighbor_4", "BioDeg_CAS_neighbor_5", "BioDeg_InChiKey_neighbor_1",
			"BioDeg_InChiKey_neighbor_2", "BioDeg_InChiKey_neighbor_3", "BioDeg_InChiKey_neighbor_4",
			"BioDeg_InChiKey_neighbor_5", "BioDeg_DTXSID_neighbor_1", "BioDeg_DTXSID_neighbor_2",
			"BioDeg_DTXSID_neighbor_3", "BioDeg_DTXSID_neighbor_4", "BioDeg_DTXSID_neighbor_5",
			"BioDeg_DSSTOXMPID_neighbor_1", "BioDeg_DSSTOXMPID_neighbor_2", "BioDeg_DSSTOXMPID_neighbor_3",
			"BioDeg_DSSTOXMPID_neighbor_4", "BioDeg_DSSTOXMPID_neighbor_5", "BioDeg_LogHalfLife_Exp_neighbor_1",
			"BioDeg_LogHalfLife_Exp_neighbor_2", "BioDeg_LogHalfLife_Exp_neighbor_3",
			"BioDeg_LogHalfLife_Exp_neighbor_4", "BioDeg_LogHalfLife_Exp_neighbor_5",
			"BioDeg_LogHalfLife_pred_neighbor_1", "BioDeg_LogHalfLife_pred_neighbor_2",
			"BioDeg_LogHalfLife_pred_neighbor_3", "BioDeg_LogHalfLife_pred_neighbor_4",
			"BioDeg_LogHalfLife_pred_neighbor_5", "BP_exp", "BP_pred", "BP_predRange", "AD_BP", "AD_index_BP",
			"Conf_index_BP", "BP_CAS_neighbor_1", "BP_CAS_neighbor_2", "BP_CAS_neighbor_3", "BP_CAS_neighbor_4",
			"BP_CAS_neighbor_5", "BP_InChiKey_neighbor_1", "BP_InChiKey_neighbor_2", "BP_InChiKey_neighbor_3",
			"BP_InChiKey_neighbor_4", "BP_InChiKey_neighbor_5", "BP_DTXSID_neighbor_1", "BP_DTXSID_neighbor_2",
			"BP_DTXSID_neighbor_3", "BP_DTXSID_neighbor_4", "BP_DTXSID_neighbor_5", "BP_DSSTOXMPID_neighbor_1",
			"BP_DSSTOXMPID_neighbor_2", "BP_DSSTOXMPID_neighbor_3", "BP_DSSTOXMPID_neighbor_4",
			"BP_DSSTOXMPID_neighbor_5", "BP_Exp_neighbor_1", "BP_Exp_neighbor_2", "BP_Exp_neighbor_3",
			"BP_Exp_neighbor_4", "BP_Exp_neighbor_5", "BP_pred_neighbor_1", "BP_pred_neighbor_2", "BP_pred_neighbor_3",
			"BP_pred_neighbor_4", "BP_pred_neighbor_5", "CATMoS_VT_pred", "CATMoS_NT_pred", "CATMoS_EPA_pred",
			"CATMoS_GHS_pred", "CATMoS_LD50_exp", "CATMoS_LD50_pred", "CATMoS_LD50_predRange", "AD_CATMoS",
			"AD_index_CATMoS", "Conf_index_CATMoS", "CAS_neighbor_1", "CAS_neighbor_2", "CAS_neighbor_3",
			"CAS_neighbor_4", "CAS_neighbor_5", "InChiKey_neighbor_1", "InChiKey_neighbor_2", "InChiKey_neighbor_3",
			"InChiKey_neighbor_4", "InChiKey_neighbor_5", "DTXSID_neighbor_1", "DTXSID_neighbor_2", "DTXSID_neighbor_3",
			"DTXSID_neighbor_4", "DTXSID_neighbor_5", "LD50_Exp_neighbor_1", "LD50_Exp_neighbor_2",
			"LD50_Exp_neighbor_3", "LD50_Exp_neighbor_4", "LD50_Exp_neighbor_5", "LD50_pred_neighbor_1",
			"LD50_pred_neighbor_2", "LD50_pred_neighbor_3", "LD50_pred_neighbor_4", "LD50_pred_neighbor_5",
			"CERAPP_Ago_exp", "CERAPP_Ago_pred", "AD_CERAPP_Ago", "AD_index_CERAPP_Ago", "Conf_index_CERAPP_Ago",
			"CERAPP_Anta_exp", "CERAPP_Anta_pred", "AD_CERAPP_Anta", "AD_index_CERAPP_Anta", "Conf_index_CERAPP_Anta",
			"CERAPP_Bind_exp", "CERAPP_Bind_pred", "AD_CERAPP_Bind", "AD_index_CERAPP_Bind", "Conf_index_CERAPP_Bind",
			"CERAPP_Ago_CAS_neighbor_1", "CERAPP_Ago_CAS_neighbor_2", "CERAPP_Ago_CAS_neighbor_3",
			"CERAPP_Ago_CAS_neighbor_4", "CERAPP_Ago_CAS_neighbor_5", "CERAPP_Ago_InChiKey_neighbor_1",
			"CERAPP_Ago_InChiKey_neighbor_2", "CERAPP_Ago_InChiKey_neighbor_3", "CERAPP_Ago_InChiKey_neighbor_4",
			"CERAPP_Ago_InChiKey_neighbor_5", "CERAPP_Ago_DTXSID_neighbor_1", "CERAPP_Ago_DTXSID_neighbor_2",
			"CERAPP_Ago_DTXSID_neighbor_3", "CERAPP_Ago_DTXSID_neighbor_4", "CERAPP_Ago_DTXSID_neighbor_5",
			"CERAPP_Ago_Exp_neighbor_1", "CERAPP_Ago_Exp_neighbor_2", "CERAPP_Ago_Exp_neighbor_3",
			"CERAPP_Ago_Exp_neighbor_4", "CERAPP_Ago_Exp_neighbor_5", "CERAPP_Ago_pred_neighbor_1",
			"CERAPP_Ago_pred_neighbor_2", "CERAPP_Ago_pred_neighbor_3", "CERAPP_Ago_pred_neighbor_4",
			"CERAPP_Ago_pred_neighbor_5", "CERAPP_Anta_CAS_neighbor_1", "CERAPP_Anta_CAS_neighbor_2",
			"CERAPP_Anta_CAS_neighbor_3", "CERAPP_Anta_CAS_neighbor_4", "CERAPP_Anta_CAS_neighbor_5",
			"CERAPP_Anta_InChiKey_neighbor_1", "CERAPP_Anta_InChiKey_neighbor_2", "CERAPP_Anta_InChiKey_neighbor_3",
			"CERAPP_Anta_InChiKey_neighbor_4", "CERAPP_Anta_InChiKey_neighbor_5", "CERAPP_Anta_DTXSID_neighbor_1",
			"CERAPP_Anta_DTXSID_neighbor_2", "CERAPP_Anta_DTXSID_neighbor_3", "CERAPP_Anta_DTXSID_neighbor_4",
			"CERAPP_Anta_DTXSID_neighbor_5", "CERAPP_Anta_Exp_neighbor_1", "CERAPP_Anta_Exp_neighbor_2",
			"CERAPP_Anta_Exp_neighbor_3", "CERAPP_Anta_Exp_neighbor_4", "CERAPP_Anta_Exp_neighbor_5",
			"CERAPP_Anta_pred_neighbor_1", "CERAPP_Anta_pred_neighbor_2", "CERAPP_Anta_pred_neighbor_3",
			"CERAPP_Anta_pred_neighbor_4", "CERAPP_Anta_pred_neighbor_5", "CERAPP_Bind_CAS_neighbor_1",
			"CERAPP_Bind_CAS_neighbor_2", "CERAPP_Bind_CAS_neighbor_3", "CERAPP_Bind_CAS_neighbor_4",
			"CERAPP_Bind_CAS_neighbor_5", "CERAPP_Bind_InChiKey_neighbor_1", "CERAPP_Bind_InChiKey_neighbor_2",
			"CERAPP_Bind_InChiKey_neighbor_3", "CERAPP_Bind_InChiKey_neighbor_4", "CERAPP_Bind_InChiKey_neighbor_5",
			"CERAPP_Bind_DTXSID_neighbor_1", "CERAPP_Bind_DTXSID_neighbor_2", "CERAPP_Bind_DTXSID_neighbor_3",
			"CERAPP_Bind_DTXSID_neighbor_4", "CERAPP_Bind_DTXSID_neighbor_5", "CERAPP_Bind_Exp_neighbor_1",
			"CERAPP_Bind_Exp_neighbor_2", "CERAPP_Bind_Exp_neighbor_3", "CERAPP_Bind_Exp_neighbor_4",
			"CERAPP_Bind_Exp_neighbor_5", "CERAPP_Bind_pred_neighbor_1", "CERAPP_Bind_pred_neighbor_2",
			"CERAPP_Bind_pred_neighbor_3", "CERAPP_Bind_pred_neighbor_4", "CERAPP_Bind_pred_neighbor_5", "Clint_exp",
			"Clint_pred", "Clint_predRange", "AD_Clint", "AD_index_Clint", "Conf_index_Clint", "Clint_CAS_neighbor_1",
			"Clint_CAS_neighbor_2", "Clint_CAS_neighbor_3", "Clint_CAS_neighbor_4", "Clint_CAS_neighbor_5",
			"Clint_InChiKey_neighbor_1", "Clint_InChiKey_neighbor_2", "Clint_InChiKey_neighbor_3",
			"Clint_InChiKey_neighbor_4", "Clint_InChiKey_neighbor_5", "Clint_DTXSID_neighbor_1",
			"Clint_DTXSID_neighbor_2", "Clint_DTXSID_neighbor_3", "Clint_DTXSID_neighbor_4", "Clint_DTXSID_neighbor_5",
			"Clint_Exp_neighbor_1", "Clint_Exp_neighbor_2", "Clint_Exp_neighbor_3", "Clint_Exp_neighbor_4",
			"Clint_Exp_neighbor_5", "Clint_pred_neighbor_1", "Clint_pred_neighbor_2", "Clint_pred_neighbor_3",
			"Clint_pred_neighbor_4", "Clint_pred_neighbor_5", "CoMPARA_Ago_exp", "CoMPARA_Ago_pred", "AD_CoMPARA_Ago",
			"AD_index_CoMPARA_Ago", "Conf_index_CoMPARA_Ago", "CoMPARA_Anta_exp", "CoMPARA_Anta_pred",
			"AD_CoMPARA_Anta", "AD_index_CoMPARA_Anta", "Conf_index_CoMPARA_Anta", "CoMPARA_Bind_exp",
			"CoMPARA_Bind_pred", "AD_CoMPARA_Bind", "AD_index_CoMPARA_Bind", "Conf_index_CoMPARA_Bind",
			"CoMPARA_Ago_CAS_neighbor_1", "CoMPARA_Ago_CAS_neighbor_2", "CoMPARA_Ago_CAS_neighbor_3",
			"CoMPARA_Ago_CAS_neighbor_4", "CoMPARA_Ago_CAS_neighbor_5", "CoMPARA_Ago_InChiKey_neighbor_1",
			"CoMPARA_Ago_InChiKey_neighbor_2", "CoMPARA_Ago_InChiKey_neighbor_3", "CoMPARA_Ago_InChiKey_neighbor_4",
			"CoMPARA_Ago_InChiKey_neighbor_5", "CoMPARA_Ago_DTXSID_neighbor_1", "CoMPARA_Ago_DTXSID_neighbor_2",
			"CoMPARA_Ago_DTXSID_neighbor_3", "CoMPARA_Ago_DTXSID_neighbor_4", "CoMPARA_Ago_DTXSID_neighbor_5",
			"CoMPARA_Ago_Exp_neighbor_1", "CoMPARA_Ago_Exp_neighbor_2", "CoMPARA_Ago_Exp_neighbor_3",
			"CoMPARA_Ago_Exp_neighbor_4", "CoMPARA_Ago_Exp_neighbor_5", "CoMPARA_Ago_pred_neighbor_1",
			"CoMPARA_Ago_pred_neighbor_2", "CoMPARA_Ago_pred_neighbor_3", "CoMPARA_Ago_pred_neighbor_4",
			"CoMPARA_Ago_pred_neighbor_5", "CoMPARA_Anta_CAS_neighbor_1", "CoMPARA_Anta_CAS_neighbor_2",
			"CoMPARA_Anta_CAS_neighbor_3", "CoMPARA_Anta_CAS_neighbor_4", "CoMPARA_Anta_CAS_neighbor_5",
			"CoMPARA_Anta_InChiKey_neighbor_1", "CoMPARA_Anta_InChiKey_neighbor_2", "CoMPARA_Anta_InChiKey_neighbor_3",
			"CoMPARA_Anta_InChiKey_neighbor_4", "CoMPARA_Anta_InChiKey_neighbor_5", "CoMPARA_Anta_DTXSID_neighbor_1",
			"CoMPARA_Anta_DTXSID_neighbor_2", "CoMPARA_Anta_DTXSID_neighbor_3", "CoMPARA_Anta_DTXSID_neighbor_4",
			"CoMPARA_Anta_DTXSID_neighbor_5", "CoMPARA_Anta_Exp_neighbor_1", "CoMPARA_Anta_Exp_neighbor_2",
			"CoMPARA_Anta_Exp_neighbor_3", "CoMPARA_Anta_Exp_neighbor_4", "CoMPARA_Anta_Exp_neighbor_5",
			"CoMPARA_Anta_pred_neighbor_1", "CoMPARA_Anta_pred_neighbor_2", "CoMPARA_Anta_pred_neighbor_3",
			"CoMPARA_Anta_pred_neighbor_4", "CoMPARA_Anta_pred_neighbor_5", "CoMPARA_Bind_CAS_neighbor_1",
			"CoMPARA_Bind_CAS_neighbor_2", "CoMPARA_Bind_CAS_neighbor_3", "CoMPARA_Bind_CAS_neighbor_4",
			"CoMPARA_Bind_CAS_neighbor_5", "CoMPARA_Bind_InChiKey_neighbor_1", "CoMPARA_Bind_InChiKey_neighbor_2",
			"CoMPARA_Bind_InChiKey_neighbor_3", "CoMPARA_Bind_InChiKey_neighbor_4", "CoMPARA_Bind_InChiKey_neighbor_5",
			"CoMPARA_Bind_DTXSID_neighbor_1", "CoMPARA_Bind_DTXSID_neighbor_2", "CoMPARA_Bind_DTXSID_neighbor_3",
			"CoMPARA_Bind_DTXSID_neighbor_4", "CoMPARA_Bind_DTXSID_neighbor_5", "CoMPARA_Bind_Exp_neighbor_1",
			"CoMPARA_Bind_Exp_neighbor_2", "CoMPARA_Bind_Exp_neighbor_3", "CoMPARA_Bind_Exp_neighbor_4",
			"CoMPARA_Bind_Exp_neighbor_5", "CoMPARA_Bind_pred_neighbor_1", "CoMPARA_Bind_pred_neighbor_2",
			"CoMPARA_Bind_pred_neighbor_3", "CoMPARA_Bind_pred_neighbor_4", "CoMPARA_Bind_pred_neighbor_5", "FUB_exp",
			"FUB_pred", "FUB_predRange", "AD_FUB", "AD_index_FUB", "Conf_index_FUB", "FUB_CAS_neighbor_1",
			"FUB_CAS_neighbor_2", "FUB_CAS_neighbor_3", "FUB_CAS_neighbor_4", "FUB_CAS_neighbor_5",
			"FUB_DTXSID_neighbor_1", "FUB_DTXSID_neighbor_2", "FUB_DTXSID_neighbor_3", "FUB_DTXSID_neighbor_4",
			"FUB_DTXSID_neighbor_5", "FUB_Exp_neighbor_1", "FUB_Exp_neighbor_2", "FUB_Exp_neighbor_3",
			"FUB_Exp_neighbor_4", "FUB_Exp_neighbor_5", "FUB_pred_neighbor_1", "FUB_pred_neighbor_2",
			"FUB_pred_neighbor_3", "FUB_pred_neighbor_4", "FUB_pred_neighbor_5", "LogHL_exp", "LogHL_pred",
			"HL_predRange", "AD_HL", "AD_index_HL", "Conf_index_HL", "HL_CAS_neighbor_1", "HL_CAS_neighbor_2",
			"HL_CAS_neighbor_3", "HL_CAS_neighbor_4", "HL_CAS_neighbor_5", "HL_InChiKey_neighbor_1",
			"HL_InChiKey_neighbor_2", "HL_InChiKey_neighbor_3", "HL_InChiKey_neighbor_4", "HL_InChiKey_neighbor_5",
			"HL_DTXSID_neighbor_1", "HL_DTXSID_neighbor_2", "HL_DTXSID_neighbor_3", "HL_DTXSID_neighbor_4",
			"HL_DTXSID_neighbor_5", "HL_DSSTOXMPID_neighbor_1", "HL_DSSTOXMPID_neighbor_2", "HL_DSSTOXMPID_neighbor_3",
			"HL_DSSTOXMPID_neighbor_4", "HL_DSSTOXMPID_neighbor_5", "LogHL_Exp_neighbor_1", "LogHL_Exp_neighbor_2",
			"LogHL_Exp_neighbor_3", "LogHL_Exp_neighbor_4", "LogHL_Exp_neighbor_5", "LogHL_pred_neighbor_1",
			"LogHL_pred_neighbor_2", "LogHL_pred_neighbor_3", "LogHL_pred_neighbor_4", "LogHL_pred_neighbor_5",
			"LogKM_exp", "LogKM_pred", "KM_predRange", "AD_KM", "AD_index_KM", "Conf_index_KM", "KM_CAS_neighbor_1",
			"KM_CAS_neighbor_2", "KM_CAS_neighbor_3", "KM_CAS_neighbor_4", "KM_CAS_neighbor_5",
			"KM_InChiKey_neighbor_1", "KM_InChiKey_neighbor_2", "KM_InChiKey_neighbor_3", "KM_InChiKey_neighbor_4",
			"KM_InChiKey_neighbor_5", "KM_DTXSID_neighbor_1", "KM_DTXSID_neighbor_2", "KM_DTXSID_neighbor_3",
			"KM_DTXSID_neighbor_4", "KM_DTXSID_neighbor_5", "KM_DSSTOXMPID_neighbor_1", "KM_DSSTOXMPID_neighbor_2",
			"KM_DSSTOXMPID_neighbor_3", "KM_DSSTOXMPID_neighbor_4", "KM_DSSTOXMPID_neighbor_5", "LogKM_Exp_neighbor_1",
			"LogKM_Exp_neighbor_2", "LogKM_Exp_neighbor_3", "LogKM_Exp_neighbor_4", "LogKM_Exp_neighbor_5",
			"LogKM_pred_neighbor_1", "LogKM_pred_neighbor_2", "LogKM_pred_neighbor_3", "LogKM_pred_neighbor_4",
			"LogKM_pred_neighbor_5", "LogKOA_exp", "LogKOA_pred", "KOA_predRange", "AD_KOA", "AD_index_KOA",
			"Conf_index_KOA", "KOA_CAS_neighbor_1", "KOA_CAS_neighbor_2", "KOA_CAS_neighbor_3", "KOA_CAS_neighbor_4",
			"KOA_CAS_neighbor_5", "KOA_InChiKey_neighbor_1", "KOA_InChiKey_neighbor_2", "KOA_InChiKey_neighbor_3",
			"KOA_InChiKey_neighbor_4", "KOA_InChiKey_neighbor_5", "KOA_DTXSID_neighbor_1", "KOA_DTXSID_neighbor_2",
			"KOA_DTXSID_neighbor_3", "KOA_DTXSID_neighbor_4", "KOA_DTXSID_neighbor_5", "KOA_DSSTOXMPID_neighbor_1",
			"KOA_DSSTOXMPID_neighbor_2", "KOA_DSSTOXMPID_neighbor_3", "KOA_DSSTOXMPID_neighbor_4",
			"KOA_DSSTOXMPID_neighbor_5", "LogKOA_Exp_neighbor_1", "LogKOA_Exp_neighbor_2", "LogKOA_Exp_neighbor_3",
			"LogKOA_Exp_neighbor_4", "LogKOA_Exp_neighbor_5", "LogKOA_pred_neighbor_1", "LogKOA_pred_neighbor_2",
			"LogKOA_pred_neighbor_3", "LogKOA_pred_neighbor_4", "LogKOA_pred_neighbor_5", "LogKoc_exp", "LogKoc_pred",
			"Koc_predRange", "AD_Koc", "AD_index_Koc", "Conf_index_Koc", "Koc_CAS_neighbor_1", "Koc_CAS_neighbor_2",
			"Koc_CAS_neighbor_3", "Koc_CAS_neighbor_4", "Koc_CAS_neighbor_5", "Koc_InChiKey_neighbor_1",
			"Koc_InChiKey_neighbor_2", "Koc_InChiKey_neighbor_3", "Koc_InChiKey_neighbor_4", "Koc_InChiKey_neighbor_5",
			"Koc_DTXSID_neighbor_1", "Koc_DTXSID_neighbor_2", "Koc_DTXSID_neighbor_3", "Koc_DTXSID_neighbor_4",
			"Koc_DTXSID_neighbor_5", "Koc_DSSTOXMPID_neighbor_1", "Koc_DSSTOXMPID_neighbor_2",
			"Koc_DSSTOXMPID_neighbor_3", "Koc_DSSTOXMPID_neighbor_4", "Koc_DSSTOXMPID_neighbor_5",
			"LogKoc_Exp_neighbor_1", "LogKoc_Exp_neighbor_2", "LogKoc_Exp_neighbor_3", "LogKoc_Exp_neighbor_4",
			"LogKoc_Exp_neighbor_5", "LogKoc_pred_neighbor_1", "LogKoc_pred_neighbor_2", "LogKoc_pred_neighbor_3",
			"LogKoc_pred_neighbor_4", "LogKoc_pred_neighbor_5", "LogD55_pred", "LogD55_predRange", "LogD74_pred",
			"LogD74_predRange", "AD_LogD", "AD_index_LogD", "Conf_index_LogD", "LogD_CAS_neighbor_1",
			"LogD_CAS_neighbor_2", "LogD_CAS_neighbor_3", "LogD_CAS_neighbor_4", "LogD_CAS_neighbor_5",
			"LogD_InChiKey_neighbor_1", "LogD_InChiKey_neighbor_2", "LogD_InChiKey_neighbor_3",
			"LogD_InChiKey_neighbor_4", "LogD_InChiKey_neighbor_5", "LogD_DTXSID_neighbor_1", "LogD_DTXSID_neighbor_2",
			"LogD_DTXSID_neighbor_3", "LogD_DTXSID_neighbor_4", "LogD_DTXSID_neighbor_5", "LogP_exp", "LogP_pred",
			"LogP_predRange", "AD_LogP", "AD_index_LogP", "Conf_index_LogP", "LogP_CAS_neighbor_1",
			"LogP_CAS_neighbor_2", "LogP_CAS_neighbor_3", "LogP_CAS_neighbor_4", "LogP_CAS_neighbor_5",
			"LogP_InChiKey_neighbor_1", "LogP_InChiKey_neighbor_2", "LogP_InChiKey_neighbor_3",
			"LogP_InChiKey_neighbor_4", "LogP_InChiKey_neighbor_5", "LogP_DTXSID_neighbor_1", "LogP_DTXSID_neighbor_2",
			"LogP_DTXSID_neighbor_3", "LogP_DTXSID_neighbor_4", "LogP_DTXSID_neighbor_5", "LogP_DSSTOXMPID_neighbor_1",
			"LogP_DSSTOXMPID_neighbor_2", "LogP_DSSTOXMPID_neighbor_3", "LogP_DSSTOXMPID_neighbor_4",
			"LogP_DSSTOXMPID_neighbor_5", "LogP_Exp_neighbor_1", "LogP_Exp_neighbor_2", "LogP_Exp_neighbor_3",
			"LogP_Exp_neighbor_4", "LogP_Exp_neighbor_5", "LogP_pred_neighbor_1", "LogP_pred_neighbor_2",
			"LogP_pred_neighbor_3", "LogP_pred_neighbor_4", "LogP_pred_neighbor_5", "MP_exp", "MP_pred", "MP_predRange",
			"AD_MP", "AD_index_MP", "Conf_index_MP", "MP_CAS_neighbor_1", "MP_CAS_neighbor_2", "MP_CAS_neighbor_3",
			"MP_CAS_neighbor_4", "MP_CAS_neighbor_5", "MP_InChiKey_neighbor_1", "MP_InChiKey_neighbor_2",
			"MP_InChiKey_neighbor_3", "MP_InChiKey_neighbor_4", "MP_InChiKey_neighbor_5", "MP_DTXSID_neighbor_1",
			"MP_DTXSID_neighbor_2", "MP_DTXSID_neighbor_3", "MP_DTXSID_neighbor_4", "MP_DTXSID_neighbor_5",
			"MP_DSSTOXMPID_neighbor_1", "MP_DSSTOXMPID_neighbor_2", "MP_DSSTOXMPID_neighbor_3",
			"MP_DSSTOXMPID_neighbor_4", "MP_DSSTOXMPID_neighbor_5", "MP_Exp_neighbor_1", "MP_Exp_neighbor_2",
			"MP_Exp_neighbor_3", "MP_Exp_neighbor_4", "MP_Exp_neighbor_5", "MP_pred_neighbor_1", "MP_pred_neighbor_2",
			"MP_pred_neighbor_3", "MP_pred_neighbor_4", "MP_pred_neighbor_5", "pKa_a_exp", "pKa_b_exp", "ionization",
			"pKa_a_pred", "pKa_a_predRange", "pKa_b_pred", "pKa_b_predRange", "AD_pKa", "AD_index_pKa",
			"Conf_index_pKa", "pKa_CAS_neighbor_1", "pKa_CAS_neighbor_2", "pKa_CAS_neighbor_3",
			"pKa_InChiKey_neighbor_1", "pKa_InChiKey_neighbor_2", "pKa_InChiKey_neighbor_3", "pKa_DTXSID_neighbor_1",
			"pKa_DTXSID_neighbor_2", "pKa_DTXSID_neighbor_3", "pKa_DSSTOXMPID_neighbor_1", "pKa_DSSTOXMPID_neighbor_2",
			"pKa_DSSTOXMPID_neighbor_3", "pKa_Exp_neighbor_1", "pKa_Exp_neighbor_2", "pKa_Exp_neighbor_3",
			"pKa_pred_neighbor_1", "pKa_pred_neighbor_2", "pKa_pred_neighbor_3", "ReadyBiodeg_exp", "ReadyBiodeg_pred",
			"AD_ReadyBiodeg", "AD_index_ReadyBiodeg", "Conf_index_ReadyBiodeg", "ReadyBiodeg_CAS_neighbor_1",
			"ReadyBiodeg_CAS_neighbor_2", "ReadyBiodeg_CAS_neighbor_3", "ReadyBiodeg_CAS_neighbor_4",
			"ReadyBiodeg_CAS_neighbor_5", "ReadyBiodeg_InChiKey_neighbor_1", "ReadyBiodeg_InChiKey_neighbor_2",
			"ReadyBiodeg_InChiKey_neighbor_3", "ReadyBiodeg_InChiKey_neighbor_4", "ReadyBiodeg_InChiKey_neighbor_5",
			"ReadyBiodeg_DTXSID_neighbor_1", "ReadyBiodeg_DTXSID_neighbor_2", "ReadyBiodeg_DTXSID_neighbor_3",
			"ReadyBiodeg_DTXSID_neighbor_4", "ReadyBiodeg_DTXSID_neighbor_5", "ReadyBiodeg_DSSTOXMPID_neighbor_1",
			"ReadyBiodeg_DSSTOXMPID_neighbor_2", "ReadyBiodeg_DSSTOXMPID_neighbor_3",
			"ReadyBiodeg_DSSTOXMPID_neighbor_4", "ReadyBiodeg_DSSTOXMPID_neighbor_5", "ReadyBiodeg_Exp_neighbor_1",
			"ReadyBiodeg_Exp_neighbor_2", "ReadyBiodeg_Exp_neighbor_3", "ReadyBiodeg_Exp_neighbor_4",
			"ReadyBiodeg_Exp_neighbor_5", "ReadyBiodeg_pred_neighbor_1", "ReadyBiodeg_pred_neighbor_2",
			"ReadyBiodeg_pred_neighbor_3", "ReadyBiodeg_pred_neighbor_4", "ReadyBiodeg_pred_neighbor_5", "RT_exp",
			"RT_pred", "RT_predRange", "AD_RT", "AD_index_RT", "Conf_index_RT", "RT_CAS_neighbor_1",
			"RT_CAS_neighbor_2", "RT_CAS_neighbor_3", "RT_CAS_neighbor_4", "RT_CAS_neighbor_5", "RT_DTXSID_neighbor_1",
			"RT_DTXSID_neighbor_2", "RT_DTXSID_neighbor_3", "RT_DTXSID_neighbor_4", "RT_DTXSID_neighbor_5",
			"RT_Exp_neighbor_1", "RT_Exp_neighbor_2", "RT_Exp_neighbor_3", "RT_Exp_neighbor_4", "RT_Exp_neighbor_5",
			"RT_pred_neighbor_1", "RT_pred_neighbor_2", "RT_pred_neighbor_3", "RT_pred_neighbor_4",
			"RT_pred_neighbor_5", "MolWeight", "nbAtoms", "nbHeavyAtoms", "nbC", "nbO", "nbN", "nbAromAtom", "nbRing",
			"nbHeteroRing", "Sp3Sp2HybRatio", "nbRotBd", "nbHBdAcc", "ndHBdDon", "nbLipinskiFailures", "TopoPolSurfAir",
			"MolarRefract", "CombDipolPolariz", "LogVP_exp", "LogVP_pred", "VP_predRange", "AD_VP", "AD_index_VP",
			"Conf_index_VP", "LogVP_CAS_neighbor_1", "LogVP_CAS_neighbor_2", "LogVP_CAS_neighbor_3",
			"LogVP_CAS_neighbor_4", "LogVP_CAS_neighbor_5", "LogVP_InChiKey_neighbor_1", "LogVP_InChiKey_neighbor_2",
			"LogVP_InChiKey_neighbor_3", "LogVP_InChiKey_neighbor_4", "LogVP_InChiKey_neighbor_5",
			"LogVP_DTXSID_neighbor_1", "LogVP_DTXSID_neighbor_2", "LogVP_DTXSID_neighbor_3", "LogVP_DTXSID_neighbor_4",
			"LogVP_DTXSID_neighbor_5", "LogVP_DSSTOXMPID_neighbor_1", "LogVP_DSSTOXMPID_neighbor_2",
			"LogVP_DSSTOXMPID_neighbor_3", "LogVP_DSSTOXMPID_neighbor_4", "LogVP_DSSTOXMPID_neighbor_5",
			"LogVP_Exp_neighbor_1", "LogVP_Exp_neighbor_2", "LogVP_Exp_neighbor_3", "LogVP_Exp_neighbor_4",
			"LogVP_Exp_neighbor_5", "LogVP_pred_neighbor_1", "LogVP_pred_neighbor_2", "LogVP_pred_neighbor_3",
			"LogVP_pred_neighbor_4", "LogVP_pred_neighbor_5", "LogWS_exp", "LogWS_pred", "WS_predRange", "AD_WS",
			"AD_index_WS", "Conf_index_WS", "LogWS_CAS_neighbor_1", "LogWS_CAS_neighbor_2", "LogWS_CAS_neighbor_3",
			"LogWS_CAS_neighbor_4", "LogWS_CAS_neighbor_5", "LogWS_InChiKey_neighbor_1", "LogWS_InChiKey_neighbor_2",
			"LogWS_InChiKey_neighbor_3", "LogWS_InChiKey_neighbor_4", "LogWS_InChiKey_neighbor_5",
			"LogWS_DTXSID_neighbor_1", "LogWS_DTXSID_neighbor_2", "LogWS_DTXSID_neighbor_3", "LogWS_DTXSID_neighbor_4",
			"LogWS_DTXSID_neighbor_5", "LogWS_DSSTOXMPID_neighbor_1", "LogWS_DSSTOXMPID_neighbor_2",
			"LogWS_DSSTOXMPID_neighbor_3", "LogWS_DSSTOXMPID_neighbor_4", "LogWS_DSSTOXMPID_neighbor_5",
			"LogWS_Exp_neighbor_1", "LogWS_Exp_neighbor_2", "LogWS_Exp_neighbor_3", "LogWS_Exp_neighbor_4",
			"LogWS_Exp_neighbor_5", "LogWS_pred_neighbor_1", "LogWS_pred_neighbor_2", "LogWS_pred_neighbor_3",
			"LogWS_pred_neighbor_4", "LogWS_pred_neighbor_5" };
	
	public static final String lastUpdated = "11/22/2021";//results from running opera version 2.7: https://github.com/kmansouri/OPERA/releases/tag/v2.7-beta2
	public static final String sourceName = ScoreRecord.strSourceOPERA;
	public static final String dbpath="D:\\opera\\OPERA_2.7.db";
	
	

	/**
	 * Get subset of results based on id so dont run out of memory
	 * 
	 * @param minID
	 * @param maxID
	 * @param stat
	 * @return
	 */
	static Vector<JsonObject> getRecords(int minID, int maxID, Statement stat) {
		Vector<JsonObject>vec=new Vector<>();
		
		String sql="SELECT * FROM RESULTS WHERE ID>"+minID+" AND ID <= "+maxID;
		 
		 try {
			 ResultSet rs=SQLite_GetRecords.getRecords(stat, sql);
			 
			 ResultSetMetaData rsmd = rs.getMetaData();
			 
			 while (rs.next()) {
				JsonObject jo=createJsonObject(rs,rsmd);
				vec.add(jo);
			}			 
			 
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return vec;
	}
	
	public static JsonObject getRecordID(String cid,Statement stat) {
		
		
		String sql="SELECT * FROM IDs WHERE DSSTOX_COMPOUND_ID=\""+cid+"\"";
		 
		 try {
			 ResultSet rs=SQLite_GetRecords.getRecords(stat, sql);
			 ResultSetMetaData rsmd = rs.getMetaData();
			 
			 while (rs.next()) {
				JsonObject jo=createJsonObject(rs,rsmd);
				return jo;
			}			 
			 
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		 
		return null;
	}
	
	public  static JsonObject createJsonObject(ResultSet rs, ResultSetMetaData rsmd) {
		JsonObject jo=new JsonObject();

		try {
			for (int i = 1; i<= rsmd.getColumnCount(); i++) {
				String fieldName=rsmd.getColumnLabel(i);
				String fieldValue=rs.getString(i);
				jo.addProperty(fieldName, fieldValue);
			}
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return jo;
	}
	
}
