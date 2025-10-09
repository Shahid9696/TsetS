/*
---------------------------------------------------------------------------------------------------------
                  NEWGEN SOFTWARE TECHNOLOGIES LIMITED

Group                   : Application - Projects
Project/Product			: CourtOrder
Application				: RAK SRM Utility
Module					: CourtOrder
File Name				: CreatePcWi.java
Author 					: Sudhanshu Rathore
Date (DD/MM/YYYY)		: 22/10/2024

---------------------------------------------------------------------------------------------------------
                 	CHANGE HISTORY
---------------------------------------------------------------------------------------------------------

Problem No/CR No        Change Date           Changed By             Change Description
---------------------------------------------------------------------------------------------------------
---------------------------------------------------------------------------------------------------------
*/
package com.newgen.CourtOrder;

import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.io.Writer;
import java.net.Socket;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.text.DateFormat;
import java.text.Format;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import java.util.TreeMap;

import javax.swing.text.DateFormatter;

import org.apache.commons.io.FilenameUtils;
import org.apache.poi.hssf.usermodel.HSSFSheet;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellType;
import org.apache.poi.ss.usermodel.DataFormatter;
import org.apache.poi.ss.usermodel.DateUtil;
import org.apache.poi.ss.usermodel.FillPatternType;
import org.apache.poi.ss.usermodel.IndexedColors;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.xssf.usermodel.XSSFCellStyle;
import org.apache.poi.xssf.usermodel.XSSFRow;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;

import com.newgen.SRM.CSRMR.SROHoldCheck.CSRMRSROHoldCheckLog;
import com.newgen.common.CommonConnection;
import com.newgen.common.CommonMethods;
import com.newgen.omni.jts.cmgr.NGXmlList;
import com.newgen.omni.jts.cmgr.XMLParser;
import com.newgen.omni.wf.util.app.NGEjbClient;
import com.newgen.wfdesktop.xmlapi.WFCallBroker;

import ISPack.CImageServer;
import ISPack.CPISDocumentTxn;
import ISPack.ISUtil.JPDBRecoverDocData;
import ISPack.ISUtil.JPISException;
import ISPack.ISUtil.JPISIsIndex;
import Jdts.DataObject.JPDBString;

public class CourtOrder_CreatePcWi implements Runnable {

	static DataFormatter df = new DataFormatter();
	private static NGEjbClient ngEjbClientCourtOrder;
	static Map<String, String> CourtOrderConfigParamMap = new HashMap<String, String>();
	private int sheetCount;
	public static String newFilename = null;
	public static String sdate = "";
	public static boolean catchflag = false;
	public static String TimeStamp = "";
	static String ExcelColumn = "";
	static String CIR_Excel_INPUT = "";
	static String CIR_Excel_OUTPUT = "";
	static String CIR_Excel_ERROR = "";
	static String CIR_EXCEL_FOLDER_NAME = "";
	static String CIR_AttachDoc_INPUT = "";
	static String CIR_AttachDoc_OUTPUT = "";
	static String CIR_AttachDoc_ERROR = "";
	static String CIRBulk_TempReportPath = "";
	static String CIRBulk_ReportOdFolderName = "";
	static String volumeID = "";
	static String CIRBulk_Report_FolderIndex = "";
	static String CIRBulk_Report_FromMail = "";
	static String CIRBulk_Report_ToMail = "";
	static String CIRBulk_Freeze_Report_Body = "";
	static String CIRBulk_Inquiry_Report_Body = "";
	static String CIRBulk_Prohibited_Report_Body = "";
	static String CIRBulk_ErrorMail_Body = "";
	static String CIRBulk_WaitMail_Body = "";
	static String smsPort = "";
	String sessionID = "";
	String cabinetName = "";
	String jtsIP = "";
	String jtsPort = "";
	String queueID = "";
	String queueID_SystemCheck = "";
	String processDefId = "";
	String ProcessDefIdPC = "";
	int integrationWaitTime = 0;
	int sleepIntervalInMin = 0;
	int attachDocWaitTimeInSec = 0;
	int socketConnectionTimeout = 0;
	public static String ws_name;

	private String DocumentsTag = "";
	private char fieldSep = ((char) 21); // Constant
	private char recordSep = ((char) 25);

	public void run() {

		try {
			CourtOrder_Log.setLogger();
			ngEjbClientCourtOrder = NGEjbClient.getSharedInstance();

			CourtOrder_PCWICreate_Log.CourtOrder_PCWICreate_Logger.debug("Connecting to Cabinet.");

			int configReadStatus = readConfig();

			CourtOrder_PCWICreate_Log.CourtOrder_PCWICreate_Logger.debug("configReadStatus " + configReadStatus);
			if (configReadStatus != 0) {
				CourtOrder_PCWICreate_Log.CourtOrder_PCWICreate_Logger
						.debug("Could not Read Config Properties [EiborReadExcel]");
				return;
			}

			cabinetName = CommonConnection.getCabinetName();
			CourtOrder_PCWICreate_Log.CourtOrder_PCWICreate_Logger.debug("Cabinet Name: " + cabinetName);

			jtsIP = CommonConnection.getJTSIP();
			CourtOrder_PCWICreate_Log.CourtOrder_PCWICreate_Logger.debug("JTSIP: " + jtsIP);

			jtsPort = CommonConnection.getJTSPort();
			CourtOrder_PCWICreate_Log.CourtOrder_PCWICreate_Logger.debug("JTSPORT: " + jtsPort);

			queueID = CourtOrderConfigParamMap.get("queueID");
			CourtOrder_PCWICreate_Log.CourtOrder_PCWICreate_Logger.debug("QueueID: " + queueID);

			socketConnectionTimeout = Integer.parseInt(CourtOrderConfigParamMap.get("MQ_SOCKET_CONNECTION_TIMEOUT"));
			CourtOrder_PCWICreate_Log.CourtOrder_PCWICreate_Logger
					.debug("SocketConnectionTimeOut: " + CourtOrderConfigParamMap);

			sleepIntervalInMin = Integer.parseInt(CourtOrderConfigParamMap.get("SleepIntervalInMin"));
			CourtOrder_PCWICreate_Log.CourtOrder_PCWICreate_Logger.debug("SleepIntervalInMin: " + sleepIntervalInMin);

			attachDocWaitTimeInSec = Integer.parseInt(CourtOrderConfigParamMap.get("AttachDocWaitTimeInSec"));
			CourtOrder_PCWICreate_Log.CourtOrder_PCWICreate_Logger
					.debug("attachDocWaitTimeInSec: " + attachDocWaitTimeInSec);

			processDefId = CourtOrderConfigParamMap.get("processDefId");
			CourtOrder_PCWICreate_Log.CourtOrder_PCWICreate_Logger.debug("processDefId: " + processDefId);

			ProcessDefIdPC = CourtOrderConfigParamMap.get("ProcessDefIdPC");
			CourtOrder_PCWICreate_Log.CourtOrder_PCWICreate_Logger.debug("ProcessDefIdPC: " + ProcessDefIdPC);

			ExcelColumn = CourtOrderConfigParamMap.get("ExcelColumn");
			CourtOrder_PCWICreate_Log.CourtOrder_PCWICreate_Logger.debug("ExcelColumn: " + ExcelColumn);
			CIR_Excel_INPUT = CourtOrderConfigParamMap.get("CIR_Excel_INPUT");
			CourtOrder_PCWICreate_Log.CourtOrder_PCWICreate_Logger.debug("CIR_Excel_INPUT: " + CIR_Excel_INPUT);
			CIR_Excel_OUTPUT = CourtOrderConfigParamMap.get("CIR_Excel_OUTPUT");
			CourtOrder_PCWICreate_Log.CourtOrder_PCWICreate_Logger.debug("CIR_Excel_OUTPUT: " + CIR_Excel_OUTPUT);
			CIR_EXCEL_FOLDER_NAME = CourtOrderConfigParamMap.get("CIR_EXCEL_FOLDER_NAME");
			CourtOrder_PCWICreate_Log.CourtOrder_PCWICreate_Logger
					.debug("CIR_EXCEL_FOLDER_NAME: " + CIR_EXCEL_FOLDER_NAME);
			CIR_Excel_ERROR = CourtOrderConfigParamMap.get("CIR_Excel_ERROR");
			CourtOrder_PCWICreate_Log.CourtOrder_PCWICreate_Logger.debug("CIR_Excel_ERROR: " + CIR_Excel_ERROR);
			queueID_SystemCheck = CourtOrderConfigParamMap.get("queueID_SystemCheck");
			CourtOrder_PCWICreate_Log.CourtOrder_PCWICreate_Logger.debug("queueID_SystemCheck: " + queueID_SystemCheck);
			smsPort = CommonConnection.getsSMSPort();
			CIRBulk_TempReportPath = CourtOrderConfigParamMap.get("CIRBulk_TempReportPath");
			CourtOrder_PCWICreate_Log.CourtOrder_PCWICreate_Logger
					.debug("CIRBulk_TempReportPath: " + CIRBulk_TempReportPath);
			CIRBulk_ReportOdFolderName = CourtOrderConfigParamMap.get("CIRBulk_ReportOdFolderName");
			CourtOrder_PCWICreate_Log.CourtOrder_PCWICreate_Logger
					.debug("CIRBulk_ReportOdFolderName: " + CIRBulk_ReportOdFolderName);
			volumeID = CourtOrderConfigParamMap.get("volumeID");
			CourtOrder_PCWICreate_Log.CourtOrder_PCWICreate_Logger.debug("volumeID: " + volumeID);
			CIRBulk_Report_FolderIndex = CourtOrderConfigParamMap.get("CIRBulk_Report_FolderIndex");
			CourtOrder_PCWICreate_Log.CourtOrder_PCWICreate_Logger
					.debug("CIRBulk_Report_FolderIndex: " + CIRBulk_Report_FolderIndex);
			CIRBulk_Report_FromMail = CourtOrderConfigParamMap.get("CIRBulk_Report_FromMail");
			CourtOrder_PCWICreate_Log.CourtOrder_PCWICreate_Logger
					.debug("CIRBulk_Report_FromMail: " + CIRBulk_Report_FromMail);
			CIRBulk_Report_ToMail = CourtOrderConfigParamMap.get("CIRBulk_Report_ToMail");
			CourtOrder_PCWICreate_Log.CourtOrder_PCWICreate_Logger
					.debug("CIRBulk_Report_ToMail: " + CIRBulk_Report_ToMail);
			CIRBulk_Freeze_Report_Body = CourtOrderConfigParamMap.get("CIRBulk_Freeze_Report_Body");
			CourtOrder_PCWICreate_Log.CourtOrder_PCWICreate_Logger
					.debug("CIRBulk_Freeze_Report_Body: " + CIRBulk_Freeze_Report_Body);
			CIRBulk_Inquiry_Report_Body = CourtOrderConfigParamMap.get("CIRBulk_Inquiry_Report_Body");
			CourtOrder_PCWICreate_Log.CourtOrder_PCWICreate_Logger
					.debug("CIRBulk_Inquiry_Report_Body: " + CIRBulk_Inquiry_Report_Body);
			CIRBulk_Prohibited_Report_Body = CourtOrderConfigParamMap.get("CIRBulk_Prohibited_Report_Body");
			CourtOrder_PCWICreate_Log.CourtOrder_PCWICreate_Logger
					.debug("CIRBulk_Prohibited_Report_Body: " + CIRBulk_Prohibited_Report_Body);
			CIR_AttachDoc_INPUT = CourtOrderConfigParamMap.get("CIR_AttachDoc_INPUT");
			CourtOrder_PCWICreate_Log.CourtOrder_PCWICreate_Logger.debug("CIR_AttachDoc_INPUT: " + CIR_AttachDoc_INPUT);
			CIR_AttachDoc_OUTPUT = CourtOrderConfigParamMap.get("CIR_AttachDoc_OUTPUT");
			CourtOrder_PCWICreate_Log.CourtOrder_PCWICreate_Logger
					.debug("CIR_AttachDoc_OUTPUT: " + CIR_AttachDoc_OUTPUT);
			CIR_AttachDoc_ERROR = CourtOrderConfigParamMap.get("CIR_AttachDoc_ERROR");
			CourtOrder_PCWICreate_Log.CourtOrder_PCWICreate_Logger.debug("CIR_AttachDoc_ERROR: " + CIR_AttachDoc_ERROR);
			CIRBulk_ErrorMail_Body = CourtOrderConfigParamMap.get("CIRBulk_ErrorMail_Body");
			CourtOrder_PCWICreate_Log.CourtOrder_PCWICreate_Logger
					.debug("CIRBulk_ErrorMail_Body: " + CIRBulk_ErrorMail_Body);
			CIRBulk_WaitMail_Body = CourtOrderConfigParamMap.get("CIRBulk_WaitMail_Body");
			CourtOrder_PCWICreate_Log.CourtOrder_PCWICreate_Logger
					.debug("CIRBulk_WaitMail_Body: " + CIRBulk_WaitMail_Body);

			sessionID = CommonConnection.getSessionID(CourtOrder_PCWICreate_Log.CourtOrder_PCWICreate_Logger, false);

			if (sessionID.trim().equalsIgnoreCase("")) {
				CourtOrder_PCWICreate_Log.CourtOrder_PCWICreate_Logger.debug("Could Not Connect to Server!");
			} else {
				CourtOrder_PCWICreate_Log.CourtOrder_PCWICreate_Logger.debug("Session ID found: " + sessionID);
				HashMap<String, String> socketDetailsMap = socketConnectionDetails(cabinetName, jtsIP, jtsPort,
						sessionID);
				while (true) {
					CourtOrder_Log.setLogger();
					CourtOrder_PCWICreate_Log.CourtOrder_PCWICreate_Logger.debug("CourtOrder Read");
					sessionID = CommonConnection.getSessionID(CourtOrder_PCWICreate_Log.CourtOrder_PCWICreate_Logger,
							false);

					if (sessionID == null || sessionID.equalsIgnoreCase("") || sessionID.equalsIgnoreCase("null")) {
						CourtOrder_PCWICreate_Log.CourtOrder_PCWICreate_Logger
								.debug("Could Not Get Session ID " + sessionID);
						return;
					}
					long cycleStart = System.currentTimeMillis();
					InsertDataInDBFromExcel(sessionID);
					createWIFromDb(sessionID, cycleStart);
					start_CourtOrder_System_Check(cabinetName, jtsIP, jtsPort, sessionID, queueID_SystemCheck,
							socketConnectionTimeout, integrationWaitTime, socketDetailsMap);
					start_PC_WiCreate(cabinetName, jtsIP, jtsPort, sessionID, queueID, socketConnectionTimeout,
							integrationWaitTime);
					archival_file_CIR_Bulk(cabinetName, jtsIP, jtsPort, sessionID, socketConnectionTimeout,
							integrationWaitTime, socketDetailsMap);
					System.out.println("No More files to Process, Sleeping!");
					Thread.sleep(sleepIntervalInMin * 60 * 1000);
				}
			}
		}

		catch (Exception e) {
			e.printStackTrace();
			CourtOrder_PCWICreate_Log.CourtOrder_PCWICreate_Logger
					.debug("Exception Occurred in CourtOrder SRO Hold Check : " + e);
			final Writer result = new StringWriter();
			final PrintWriter printWriter = new PrintWriter(result);
			e.printStackTrace(printWriter);
			CourtOrder_PCWICreate_Log.CourtOrder_PCWICreate_Logger
					.debug("Exception Occurred in CourtOrder : " + result);
		}
	}

	private int readConfig() {
		Properties p = null;
		try {

			p = new Properties();
			p.load(new FileInputStream(new File(System.getProperty("user.dir") + File.separator + "ConfigFiles"
					+ File.separator + "CourtOrder_Config.properties")));

			Enumeration<?> names = p.propertyNames();

			while (names.hasMoreElements()) {
				String name = (String) names.nextElement();
				CourtOrderConfigParamMap.put(name, p.getProperty(name));
			}
		} catch (Exception e) {
			return -1;
		}
		return 0;
	}

	// By Sudhanshu Rathore
	private void archival_file_CIR_Bulk(String cabinetName, String sJtsIp, String iJtsPort, String sessionId,
			int socketConnectionTimeOut, int integrationWaitTime, HashMap<String, String> socketDetailsMap)
			throws IOException, Exception {

		try {
			CourtOrder_PCWICreate_Log.CourtOrder_PCWICreate_Logger
					.debug("**************Inside archival_file_CIR_Bulk************ ");
			String query = "select distinct(Request_Reference_No),Request_type from ng_CourtOrder_exttable with(nolock) "
					+ "where Requested_Channel='CIR - Bulk' and (is_archival_mail_trigerred != 'Y' or is_archival_mail_trigerred is null)";
			String wi_pc_inputXml = CommonMethods.apSelectWithColumnNames(query, CommonConnection.getCabinetName(),
					CommonConnection.getSessionID(CourtOrder_PCWICreate_Log.CourtOrder_PCWICreate_Logger, false));
			CourtOrder_PCWICreate_Log.CourtOrder_PCWICreate_Logger.debug("wi_archival_inputXml: " + wi_pc_inputXml);
			String wi_pc__outputXml = WFNGExecute(wi_pc_inputXml, CommonConnection.getJTSIP(),
					CommonConnection.getJTSPort(), 1);
			CourtOrder_PCWICreate_Log.CourtOrder_PCWICreate_Logger.debug("wi_archival_outputXml: " + wi_pc__outputXml);

			XMLParser wi_pc_xmlParserData = new XMLParser(wi_pc__outputXml);
			int totalRetreived = Integer.parseInt(wi_pc_xmlParserData.getValueOf("TotalRetrieved"));
			if (wi_pc_xmlParserData.getValueOf("MainCode").equalsIgnoreCase("0") && totalRetreived > 0) {
				String wi_pc_val = wi_pc_xmlParserData.getNextValueOf("Record");
				wi_pc_val = wi_pc_val.replaceAll("[ ]+>", ">").replaceAll("<[ ]+", "<");
				NGXmlList objWorkList = wi_pc_xmlParserData.createList("Records", "Record");
				String Request_Reference_No = "", Request_type = "";
				//
				for (; objWorkList.hasMoreElements(true); objWorkList.skip(true)) {
					Request_Reference_No = objWorkList.getVal("Request_Reference_No");
					Request_type = objWorkList.getVal("Request_type");
					CourtOrder_PCWICreate_Log.CourtOrder_PCWICreate_Logger
							.debug("Request_Reference_No: " + Request_Reference_No);
					CourtOrder_PCWICreate_Log.CourtOrder_PCWICreate_Logger.debug("Request_type: " + Request_type);
					//
					String query_2 = "select count(Wi_name) as CountWi from ng_CourtOrder_exttable with(nolock) "
							+ "where Request_Reference_No = '" + Request_Reference_No + "' and CURRENT_WS <> 'Exit'";
					String wi_pc_inputXml1 = CommonMethods.apSelectWithColumnNames(query_2,
							CommonConnection.getCabinetName(), CommonConnection
									.getSessionID(CourtOrder_PCWICreate_Log.CourtOrder_PCWICreate_Logger, false));
					CourtOrder_PCWICreate_Log.CourtOrder_PCWICreate_Logger
							.debug("Count wi inputXml1: " + wi_pc_inputXml1);
					String wi_pc__outputXml1 = WFNGExecute(wi_pc_inputXml1, CommonConnection.getJTSIP(),
							CommonConnection.getJTSPort(), 1);
					CourtOrder_PCWICreate_Log.CourtOrder_PCWICreate_Logger
							.debug("Count wi outputXml1: " + wi_pc__outputXml1);
					XMLParser wi_pc_xmlParserData1 = new XMLParser(wi_pc__outputXml1);
					int totalRetreived1 = Integer.parseInt(wi_pc_xmlParserData1.getValueOf("TotalRetrieved"));
					if (wi_pc_xmlParserData1.getValueOf("MainCode").equalsIgnoreCase("0") && totalRetreived1 > 0) {
						String wi_pc_val1 = wi_pc_xmlParserData1.getNextValueOf("Record");
						wi_pc_val1 = wi_pc_val1.replaceAll("[ ]+>", ">").replaceAll("<[ ]+", "<");
						NGXmlList objWorkList1 = wi_pc_xmlParserData1.createList("Records", "Record");
						String CountWi = "";
						for (; objWorkList1.hasMoreElements(true); objWorkList1.skip(true)) {
							CountWi = objWorkList1.getVal("CountWi");
							CourtOrder_PCWICreate_Log.CourtOrder_PCWICreate_Logger.debug("CountWi: " + CountWi);
							if ("0".equalsIgnoreCase(CountWi)) {

								if ("Freeze".equalsIgnoreCase(Request_type)) {
									CreateExcel_CIR_Freeze(Request_Reference_No, cabinetName, sJtsIp, iJtsPort,
											sessionId);
								} else if ("Prohibited".equalsIgnoreCase(Request_type)) {
									CreateExcel_CIR_Prohibited(Request_Reference_No, cabinetName, sJtsIp, iJtsPort,
											sessionId);
								} else if ("Inquiry".equalsIgnoreCase(Request_type)) {
									CreateExcel_CIR_Inquiry(Request_Reference_No, cabinetName, sJtsIp, iJtsPort,
											sessionId);
								}
							}
						}
					}
				}
			}
		} catch (Exception e) {
			CourtOrder_SystemAutoRemittance obj1 = new CourtOrder_SystemAutoRemittance();
			String exception = obj1.customException(e);
			CourtOrder_PCWICreate_Log.CourtOrder_PCWICreate_Logger
					.debug("Inside exception archival_file_CIR_Bulk: " + exception);
		}
	}

	public void CreateExcel_CIR_Freeze(String Request_Reference_No, String cabinetName, String sJtsIp, String iJtsPort,
			String sessionId) {
		try {
			String workItemNameDoc = "";
			CIRBulk_TempReportPath = CourtOrderConfigParamMap.get("CIRBulk_TempReportPath");
			CourtOrder_PCWICreate_Log.CourtOrder_PCWICreate_Logger
					.debug("CIRBulk_TempReportPath: " + CIRBulk_TempReportPath);
			XSSFWorkbook workbook = new XSSFWorkbook();
			XSSFSheet spreadsheet = workbook.createSheet("CIR-Bulk Freeze");
			XSSFRow row;
			Map<Integer, Object[]> data = new TreeMap<Integer, Object[]>();
			int keyvalue = 1;
			int sno = 1;

			data.put(keyvalue,
					new Object[] { "S.No.", "Requested Authority", "Notice Case No", "Due Date", "Name", "Emirates ID",
							"Passport", "Date Of Birth", "Nationality", "Trade License No", "Date of Establishment",
							"Country Of Incorporation", "RAK / Non RAK Customer", "CIF No.", "Account No.",
							"Account Balance", "Related Party CIF" });

			String DBQuery_1 = "Select Created_wi_name,Name,Emirates_ID,Passport,Trade_License_No,Date_of_Birth,"
					+ "Country_of_Incorporation,Nationality,Requested_Authority,Due_Date,Notice_Case_No,"
					+ "Date_of_Establishment,Sno from NG_courtOrder_CIR_ExcelData with(nolock) "
					+ "where is_wi_created = 'Y' and Request_Reference_No = '" + Request_Reference_No + "' "
					+ "order by cast(Sno as int);";
			String extTabDataIPXML_1 = CommonMethods.apSelectWithColumnNames(DBQuery_1,
					CommonConnection.getCabinetName(),
					CommonConnection.getSessionID(CourtOrder_PCWICreate_Log.CourtOrder_PCWICreate_Logger, false));
			CourtOrder_PCWICreate_Log.CourtOrder_PCWICreate_Logger.debug("extTabDataIPXML_1: " + extTabDataIPXML_1);
			String extTabDataOPXML_1 = WFNGExecute(extTabDataIPXML_1, CommonConnection.getJTSIP(),
					CommonConnection.getJTSPort(), 1);
			CourtOrder_PCWICreate_Log.CourtOrder_PCWICreate_Logger.debug("extTabDataOPXML_1: " + extTabDataOPXML_1);
			XMLParser xmlParserData_1 = new XMLParser(extTabDataOPXML_1);
			int iTotalrec_1 = Integer.parseInt(xmlParserData_1.getValueOf("TotalRetrieved"));
			if (xmlParserData_1.getValueOf("MainCode").equalsIgnoreCase("0") && iTotalrec_1 > 0) {
				String xmlDataExtTab1 = xmlParserData_1.getNextValueOf("Record");
				xmlDataExtTab1 = xmlDataExtTab1.replaceAll("[ ]+>", ">").replaceAll("<[ ]+", "<");
				NGXmlList objWorkList1 = xmlParserData_1.createList("Records", "Record");

				String wi_name = "", authorityName = "", dueDate = "", fullName = "", emiratesID = "", passport = "",
						tradeLicNo = "", dateOfBirth = "", countryOfIncorp = "", nationality = "", noticeCaseNo = "",
						dateOfEstablishment = "", Sno = "";
				for (; objWorkList1.hasMoreElements(true); objWorkList1.skip(true)) {
					keyvalue += 1;
					wi_name = objWorkList1.getVal("Created_wi_name");
					fullName = objWorkList1.getVal("Name");
					emiratesID = objWorkList1.getVal("Emirates_ID");
					passport = objWorkList1.getVal("Passport");
					tradeLicNo = objWorkList1.getVal("Trade_License_No");
					dateOfBirth = objWorkList1.getVal("Date_of_Birth");
					countryOfIncorp = objWorkList1.getVal("Country_of_Incorporation");
					nationality = objWorkList1.getVal("Nationality");
					authorityName = objWorkList1.getVal("Requested_Authority");
					dueDate = objWorkList1.getVal("Due_Date");
					noticeCaseNo = objWorkList1.getVal("Notice_Case_No");
					dateOfEstablishment = objWorkList1.getVal("Date_of_Establishment");
					Sno = objWorkList1.getVal("Sno");

					CourtOrder_PCWICreate_Log.CourtOrder_PCWICreate_Logger.debug("wi_name is: " + wi_name);
					// for later use
					if (!"".equalsIgnoreCase(wi_name) && !wi_name.isEmpty()) {
						workItemNameDoc = wi_name;
					}
					// adding later on 16_06_2025 as part of mvp3 change
					// fetching values customer main table
					String DBQuery_6 = "Select CUSTOMER_IDENTIFIED as CUSTOMER_IDENTIFIED_as,CIF as CIF_ID from "
							+ "NG_COURTORDER_GR_INDIVIDUAL_CUSTOMER_DETAILS " + "with(nolock) where wi_name = '"
							+ wi_name + "' union all Select "
							+ "CUSTOMER_IDENTIFIED_as as CUSTOMER_IDENTIFIED_as,CIF_ID as CIF_ID from "
							+ "NG_COURTORDER_GR_NON_INDIVIDUAL_CUSTOMER_DETAILS " + "with(nolock) where wi_name = '"
							+ wi_name + "'";
					String extTabDataIPXML_6 = CommonMethods.apSelectWithColumnNames(DBQuery_6,
							CommonConnection.getCabinetName(), CommonConnection
									.getSessionID(CourtOrder_PCWICreate_Log.CourtOrder_PCWICreate_Logger, false));
					CourtOrder_PCWICreate_Log.CourtOrder_PCWICreate_Logger
							.debug("extTabDataIPXML_6: " + extTabDataIPXML_6);
					String extTabDataOPXML_6 = WFNGExecute(extTabDataIPXML_6, CommonConnection.getJTSIP(),
							CommonConnection.getJTSPort(), 1);
					CourtOrder_PCWICreate_Log.CourtOrder_PCWICreate_Logger
							.debug("extTabDataOPXML_6: " + extTabDataOPXML_6);
					XMLParser xmlParserData_6 = new XMLParser(extTabDataOPXML_6);
					int iTotalrec_6 = Integer.parseInt(xmlParserData_6.getValueOf("TotalRetrieved"));
					if (xmlParserData_6.getValueOf("MainCode").equalsIgnoreCase("0") && iTotalrec_6 > 0) {
						String xmlDataExtTab6 = xmlParserData_6.getNextValueOf("Record");
						xmlDataExtTab6 = xmlDataExtTab6.replaceAll("[ ]+>", ">").replaceAll("<[ ]+", "<");
						NGXmlList objWorkList6 = xmlParserData_6.createList("Records", "Record");

						String customerIdentifiedAs = "", cifID = "";
						for (; objWorkList6.hasMoreElements(true); objWorkList6.skip(true)) {
							keyvalue += 1;
							customerIdentifiedAs = objWorkList6.getVal("CUSTOMER_IDENTIFIED_as");
							cifID = objWorkList6.getVal("CIF_ID");

							// fetching values related party table
							StringBuilder allRelatedCifIds = new StringBuilder();
							String DBQuery_2 = "Select RELATED_CIF_ID from NG_COURTORDER_GR_INDIVIDUAL_RELATED_PARTY_DETAILS "
									+ "with(nolock) where wi_name = '" + wi_name + "' union all Select "
									+ "RELATED_CIF_ID from NG_COURTORDER_GR_NON_INDIVIDUAL_RELATED_PARTY_DETAILS "
									+ "with(nolock) where wi_name = '" + wi_name + "'";
							String extTabDataIPXML_2 = CommonMethods.apSelectWithColumnNames(DBQuery_2,
									CommonConnection.getCabinetName(), CommonConnection.getSessionID(
											CourtOrder_PCWICreate_Log.CourtOrder_PCWICreate_Logger, false));
							CourtOrder_PCWICreate_Log.CourtOrder_PCWICreate_Logger
									.debug("extTabDataIPXML_2: " + extTabDataIPXML_2);
							String extTabDataOPXML_2 = WFNGExecute(extTabDataIPXML_2, CommonConnection.getJTSIP(),
									CommonConnection.getJTSPort(), 1);
							CourtOrder_PCWICreate_Log.CourtOrder_PCWICreate_Logger
									.debug("extTabDataOPXML_2: " + extTabDataOPXML_2);
							XMLParser xmlParserData_2 = new XMLParser(extTabDataOPXML_2);
							int iTotalrec_2 = Integer.parseInt(xmlParserData_2.getValueOf("TotalRetrieved"));
							if (xmlParserData_2.getValueOf("MainCode").equalsIgnoreCase("0") && iTotalrec_2 > 0) {
								String xmlDataExtTab2 = xmlParserData_2.getNextValueOf("Record");
								xmlDataExtTab2 = xmlDataExtTab2.replaceAll("[ ]+>", ">").replaceAll("<[ ]+", "<");
								NGXmlList objWorkList2 = xmlParserData_2.createList("Records", "Record");

								String relatedCifID = "";
								for (; objWorkList2.hasMoreElements(true); objWorkList2.skip(true)) {
									if (allRelatedCifIds.length() > 0) {
										allRelatedCifIds.append(",");
									}
									allRelatedCifIds = allRelatedCifIds.append(objWorkList2.getVal("RELATED_CIF_ID"));
								}
							}
							// fetching values from product table
							String DBQuery_3 = "Select distinct Agreement_No,Account_balance from "
									+ "NG_COURTORDER_GR_INDIVIDUAL_PRODUCT_DETAILS with(nolock) where wi_name = '"
									+ wi_name + "' and CIF_No = '" + cifID + "' union all Select distinct "
									+ "Agreement_No,Account_balance from NG_COURTORDER_GR_NON_INDIVIDUAL_PRODUCT_DETAILS "
									+ "with(nolock) where wi_name = '" + wi_name + "' and CIF_No = '" + cifID + "'";
							String extTabDataIPXML_3 = CommonMethods.apSelectWithColumnNames(DBQuery_3,
									CommonConnection.getCabinetName(), CommonConnection.getSessionID(
											CourtOrder_PCWICreate_Log.CourtOrder_PCWICreate_Logger, false));
							CourtOrder_PCWICreate_Log.CourtOrder_PCWICreate_Logger
									.debug("extTabDataIPXML_3: " + extTabDataIPXML_3);
							String extTabDataOPXML_3 = WFNGExecute(extTabDataIPXML_3, CommonConnection.getJTSIP(),
									CommonConnection.getJTSPort(), 1);
							CourtOrder_PCWICreate_Log.CourtOrder_PCWICreate_Logger
									.debug("extTabDataOPXML_3: " + extTabDataOPXML_3);
							XMLParser xmlParserData_3 = new XMLParser(extTabDataOPXML_3);
							int iTotalrec_3 = Integer.parseInt(xmlParserData_3.getValueOf("TotalRetrieved"));
							if (xmlParserData_3.getValueOf("MainCode").equalsIgnoreCase("0") && iTotalrec_3 > 0) {
								String xmlDataExtTab3 = xmlParserData_3.getNextValueOf("Record");
								xmlDataExtTab3 = xmlDataExtTab3.replaceAll("[ ]+>", ">").replaceAll("<[ ]+", "<");
								NGXmlList objWorkList3 = xmlParserData_3.createList("Records", "Record");
								CourtOrder_PCWICreate_Log.CourtOrder_PCWICreate_Logger
										.debug("Product grid is not empty");
								String accountNo = "", accountBalance = "";
								for (; objWorkList3.hasMoreElements(true); objWorkList3.skip(true)) {
									keyvalue += 1;
									String serialNo = String.valueOf(Sno);
									accountNo = objWorkList3.getVal("Agreement_No");
									accountBalance = objWorkList3.getVal("Account_balance");
									data.put(keyvalue,
											new Object[] { serialNo, authorityName, noticeCaseNo, dueDate, fullName,
													emiratesID, passport, dateOfBirth, nationality, tradeLicNo,
													dateOfEstablishment, countryOfIncorp, customerIdentifiedAs, cifID,
													accountNo, accountBalance, allRelatedCifIds.toString() });
									// sno++;
								}
							}
							// in case product grid empty
							else {
								String serialNo = String.valueOf(Sno);
								data.put(keyvalue,
										new Object[] { serialNo, authorityName, noticeCaseNo, dueDate, fullName,
												emiratesID, passport, dateOfBirth, nationality, tradeLicNo,
												dateOfEstablishment, countryOfIncorp, customerIdentifiedAs, cifID, "",
												"", allRelatedCifIds.toString() });
								// sno++;
							}
						}
					}

				}
			}
			//
			Set<Integer> keyid = data.keySet();
			int rowid = 0;
			for (int key : keyid) {
				row = spreadsheet.createRow(rowid++);
				Object[] objarr = data.get(key);
				int cellid = 0;
				for (Object obj : objarr) {
					Cell cell = row.createCell(cellid++);
					cell.setCellValue((String) obj);
				}
			}
			DateFormat dateFormat = new SimpleDateFormat("dd-MM-yyyy");
			String ReportDatetime = dateFormat.format(new Date());
			String CIRBulk_Freeze_Report_Name = "CIR-Bulk Freeze_" + Request_Reference_No;
			String newExcelFilePath = CIRBulk_TempReportPath + File.separator + CIRBulk_Freeze_Report_Name + ".xlsx";
			File finalFolder = new File(newExcelFilePath);
			if (finalFolder.exists()) {
				File fDumpFolder = new File(newExcelFilePath);
				fDumpFolder.delete();
			}
			FileOutputStream out = new FileOutputStream(new File(newExcelFilePath));
			workbook.write(out);
			out.close();
			// fetching details for addDoc
			String docPath = newExcelFilePath;
			JPISIsIndex ISINDEX = new JPISIsIndex();
			JPDBRecoverDocData JPISDEC = new JPDBRecoverDocData();
			CPISDocumentTxn.AddDocument_MT(null, jtsIP, Short.parseShort(smsPort), cabinetName,
					Short.parseShort(volumeID), docPath, JPISDEC, "", ISINDEX);
			CourtOrder_PCWICreate_Log.CourtOrder_PCWICreate_Logger.debug("After add document mt successful: ");
			String sISIndex = ISINDEX.m_nDocIndex + "#" + ISINDEX.m_sVolumeId;
			CourtOrder_PCWICreate_Log.CourtOrder_PCWICreate_Logger.debug(" sISIndex: " + sISIndex);
			String DocumentType = "N";
			String strDocumentName = CIRBulk_Freeze_Report_Name;
			String strExtension = "xlsx";
			File file = new File(newExcelFilePath);
			long lLngFileSize = 0L;
			lLngFileSize = file.length();
			String lstrDocFileSize = Long.toString(lLngFileSize);
			String sMappedInputXml = CommonMethods.getNGOAddDocument(CIRBulk_Report_FolderIndex, strDocumentName,
					DocumentType, strExtension, sISIndex, lstrDocFileSize, volumeID, cabinetName, sessionId);
			CourtOrder_PCWICreate_Log.CourtOrder_PCWICreate_Logger
					.debug("Input xml For NGOAddDocument Call: " + sMappedInputXml);
			String sOutputXml = WFNGExecute(sMappedInputXml, CommonConnection.getJTSIP(), CommonConnection.getJTSPort(),
					1);
			sOutputXml = sOutputXml.replace("<Document>", "");
			sOutputXml = sOutputXml.replace("</Document>", "");
			CourtOrder_PCWICreate_Log.CourtOrder_PCWICreate_Logger
					.debug("Output xml For NGOAddDocument Call: " + sOutputXml);
			String statusXML = CommonMethods.getTagValues(sOutputXml, "Status");
			String ErrorMsg = CommonMethods.getTagValues(sOutputXml, "Error");
			CourtOrder_PCWICreate_Log.CourtOrder_PCWICreate_Logger
					.debug(" The maincode of the output xml file is " + statusXML);
			if (statusXML.equalsIgnoreCase("0")) {
				// fetching CIR-Bulk Freeze
				String DBQuery_4 = "Select top 1 ISnull(ImageIndex,'') as ImageIndex,ISnull(concat(NAME,'.',AppName),'') as ATTACHMENTNAMES, volumeId from pdbdocument with (nolock) "
						+ "WHERE DocumentIndex in (select DocumentIndex from PDBDocumentContent where ParentFolderIndex =(select FolderIndex from PDBFolder where Name = '"
						+ CIRBulk_ReportOdFolderName + "')) and" + " name like '" + strDocumentName
						+ "%' order by DocumentIndex desc";
				String extTabDataIPXML_4 = CommonMethods.apSelectWithColumnNames(DBQuery_4,
						CommonConnection.getCabinetName(),
						CommonConnection.getSessionID(CourtOrder_PCWICreate_Log.CourtOrder_PCWICreate_Logger, false));
				CourtOrder_PCWICreate_Log.CourtOrder_PCWICreate_Logger.debug("extTabDataIPXML_4: " + extTabDataIPXML_4);
				String extTabDataOPXML_4 = WFNGExecute(extTabDataIPXML_4, CommonConnection.getJTSIP(),
						CommonConnection.getJTSPort(), 1);
				CourtOrder_PCWICreate_Log.CourtOrder_PCWICreate_Logger.debug("extTabDataOPXML_4: " + extTabDataOPXML_4);
				XMLParser xmlParserData_4 = new XMLParser(extTabDataOPXML_4);
				int iTotalrec_4 = Integer.parseInt(xmlParserData_4.getValueOf("TotalRetrieved"));
				String ImageIndex = "", ATTACHMENTNAMES = "", volumeId = "";
				if (xmlParserData_4.getValueOf("MainCode").equalsIgnoreCase("0") && iTotalrec_4 > 0) {
					String xmlDataExtTab = xmlParserData_4.getNextValueOf("Record");
					xmlDataExtTab = xmlDataExtTab.replaceAll("[ ]+>", ">").replaceAll("<[ ]+", "<");
					NGXmlList objWorkList4 = xmlParserData_4.createList("Records", "Record");
					for (; objWorkList4.hasMoreElements(true); objWorkList4.skip(true)) {
						ImageIndex = objWorkList4.getVal("ImageIndex");
						ATTACHMENTNAMES = objWorkList4.getVal("ATTACHMENTNAMES");
						volumeId = objWorkList4.getVal("volumeId");
					}
				}
				String wfattachmentNames = "", wfattachmentIndex = "";
				if (!"".equalsIgnoreCase(ATTACHMENTNAMES) && !ATTACHMENTNAMES.isEmpty()) {
					wfattachmentNames = ATTACHMENTNAMES + ";";
				}
				if (!"".equalsIgnoreCase(ImageIndex) && !ImageIndex.isEmpty() && !"".equalsIgnoreCase(volumeId)
						&& !volumeId.isEmpty()) {
					wfattachmentIndex = ImageIndex + "#" + volumeId + "#;";
				}

				// fetching other WI doc's
				String docToFetched = "Central Bank Attachment";
				String DBQuery_5 = "SELECT ISnull(ImageIndex,'') as ImageIndex, ISnull(concat(NAME,'.',AppName),'') "
						+ "as ATTACHMENTNAMES,volumeId FROM " + "PDBDocument WITH (NOLOCK) WHERE "
						+ "DocumentIndex IN (SELECT DocumentIndex FROM PDBDocumentContent a WITH (NOLOCK) "
						+ "JOIN PDBFolder b WITH (NOLOCK) ON b.FolderIndex = a.ParentFolderIndex WHERE Name = '"
						+ workItemNameDoc + "' ) AND Name in ('" + docToFetched + "');";
				String extTabDataIPXML_5 = CommonMethods.apSelectWithColumnNames(DBQuery_5,
						CommonConnection.getCabinetName(),
						CommonConnection.getSessionID(CourtOrder_PCWICreate_Log.CourtOrder_PCWICreate_Logger, false));
				CourtOrder_PCWICreate_Log.CourtOrder_PCWICreate_Logger.debug("extTabDataIPXML_5: " + extTabDataIPXML_5);
				String extTabDataOPXML_5 = WFNGExecute(extTabDataIPXML_5, CommonConnection.getJTSIP(),
						CommonConnection.getJTSPort(), 1);
				CourtOrder_PCWICreate_Log.CourtOrder_PCWICreate_Logger.debug("extTabDataOPXML_5: " + extTabDataOPXML_5);
				XMLParser xmlParserData_5 = new XMLParser(extTabDataOPXML_5);
				int iTotalrec_5 = Integer.parseInt(xmlParserData_5.getValueOf("TotalRetrieved"));
				String ImageIndex2 = "", ATTACHMENTNAMES2 = "", volumeId2 = "";
				if (xmlParserData_5.getValueOf("MainCode").equalsIgnoreCase("0") && iTotalrec_5 > 0) {
					String xmlDataExtTab = xmlParserData_5.getNextValueOf("Record");
					xmlDataExtTab = xmlDataExtTab.replaceAll("[ ]+>", ">").replaceAll("<[ ]+", "<");
					NGXmlList objWorkList5 = xmlParserData_5.createList("Records", "Record");
					for (; objWorkList5.hasMoreElements(true); objWorkList5.skip(true)) {
						ImageIndex2 = objWorkList5.getVal("ImageIndex");
						ATTACHMENTNAMES2 = objWorkList5.getVal("ATTACHMENTNAMES");
						volumeId2 = objWorkList5.getVal("volumeId");
					}
				}
				//
				if (!"".equalsIgnoreCase(ATTACHMENTNAMES2) && !ATTACHMENTNAMES2.isEmpty()) {
					if (!"".equalsIgnoreCase(wfattachmentNames)) {
						wfattachmentNames += ATTACHMENTNAMES2 + ";";
					} else {
						wfattachmentNames = ATTACHMENTNAMES2 + ";";
					}
				}
				if (!"".equalsIgnoreCase(ImageIndex2) && !ImageIndex2.isEmpty() && !"".equalsIgnoreCase(volumeId2)
						&& !volumeId2.isEmpty()) {
					if (!"".equalsIgnoreCase(wfattachmentIndex)) {
						wfattachmentIndex += ImageIndex2 + "#" + volumeId2 + "#;";
					} else {
						wfattachmentIndex = ImageIndex2 + "#" + volumeId2 + "#;";
					}
				}
				// fetching main input excel file
				String inputExcelDocName = "CIR Freeze-" + Request_Reference_No;
				String DBQuery_6 = "Select top 1 ISnull(ImageIndex,'') as ImageIndex,ISnull(concat(NAME,'.',AppName),'') as ATTACHMENTNAMES, volumeId from pdbdocument with (nolock) "
						+ "WHERE DocumentIndex in (select DocumentIndex from PDBDocumentContent where ParentFolderIndex =(select FolderIndex from PDBFolder where Name = '"
						+ CIRBulk_ReportOdFolderName + "')) and" + " name like '" + inputExcelDocName
						+ "%' order by DocumentIndex desc";
				String extTabDataIPXML_6 = CommonMethods.apSelectWithColumnNames(DBQuery_6,
						CommonConnection.getCabinetName(),
						CommonConnection.getSessionID(CourtOrder_PCWICreate_Log.CourtOrder_PCWICreate_Logger, false));
				CourtOrder_PCWICreate_Log.CourtOrder_PCWICreate_Logger.debug("extTabDataIPXML_6: " + extTabDataIPXML_6);
				String extTabDataOPXML_6 = WFNGExecute(extTabDataIPXML_6, CommonConnection.getJTSIP(),
						CommonConnection.getJTSPort(), 1);
				CourtOrder_PCWICreate_Log.CourtOrder_PCWICreate_Logger.debug("extTabDataOPXML_6: " + extTabDataOPXML_6);
				XMLParser xmlParserData_6 = new XMLParser(extTabDataOPXML_6);
				int iTotalrec_6 = Integer.parseInt(xmlParserData_6.getValueOf("TotalRetrieved"));
				String ImageIndex3 = "", ATTACHMENTNAMES3 = "", volumeId3 = "";
				if (xmlParserData_6.getValueOf("MainCode").equalsIgnoreCase("0") && iTotalrec_6 > 0) {
					String xmlDataExtTab = xmlParserData_6.getNextValueOf("Record");
					xmlDataExtTab = xmlDataExtTab.replaceAll("[ ]+>", ">").replaceAll("<[ ]+", "<");
					NGXmlList objWorkList6 = xmlParserData_6.createList("Records", "Record");
					for (; objWorkList6.hasMoreElements(true); objWorkList6.skip(true)) {
						ImageIndex3 = objWorkList6.getVal("ImageIndex");
						ATTACHMENTNAMES3 = objWorkList6.getVal("ATTACHMENTNAMES");
						volumeId3 = objWorkList6.getVal("volumeId");
					}
				}
				if (!"".equalsIgnoreCase(ATTACHMENTNAMES3) && !ATTACHMENTNAMES3.isEmpty()) {
					if (!"".equalsIgnoreCase(wfattachmentNames)) {
						wfattachmentNames += ATTACHMENTNAMES3 + ";";
					} else {
						wfattachmentNames = ATTACHMENTNAMES3 + ";";
					}
				}
				if (!"".equalsIgnoreCase(ImageIndex3) && !ImageIndex3.isEmpty() && !"".equalsIgnoreCase(volumeId3)
						&& !volumeId3.isEmpty()) {
					if (!"".equalsIgnoreCase(wfattachmentIndex)) {
						wfattachmentIndex += ImageIndex3 + "#" + volumeId3 + "#;";
					} else {
						wfattachmentIndex = ImageIndex3 + "#" + volumeId3 + "#;";
					}
				}
				//
				CourtOrder_PCWICreate_Log.CourtOrder_PCWICreate_Logger
						.debug("Final wfattachmentNames: " + wfattachmentNames);
				CourtOrder_PCWICreate_Log.CourtOrder_PCWICreate_Logger
						.debug("Final wfattachmentIndex: " + wfattachmentIndex);
				//
				String loggerInMailTable = "CIR-Bulk Freeze_" + Request_Reference_No;
				SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd HH:MM:ss");
				String insertedDateTime = simpleDateFormat.format(new Date());
				DateFormat dateFormatnew = new SimpleDateFormat("dd-MM-yyyy");
				String ReportDate = dateFormat.format(new Date());
				String MailSubject = "CIR " + Request_Reference_No + " - Search & Freeze";
				String FinalMailStr = CIRBulk_Freeze_Report_Body;
				String columnName = "MAILFROM,MAILTO,MAILSUBJECT,MAILMESSAGE,MAILCONTENTTYPE,MAILPRIORITY,MAILSTATUS,"
						+ "INSERTEDBY,MAILACTIONTYPE,INSERTEDTIME,PROCESSDEFID,PROCESSINSTANCEID,WORKITEMID,ACTIVITYID,"
						+ "NOOFTRIALS,attachmentNames,attachmentISINDEX";
				String strValues = "'" + CIRBulk_Report_FromMail + "','" + CIRBulk_Report_ToMail + "',N'" + MailSubject
						+ "',N'" + FinalMailStr + "','text/html;charset=UTF-8','1','N','CUSTOM','TRIGGER','"
						+ insertedDateTime + "','" + processDefId + "','" + loggerInMailTable + "','1','1','0','"
						+ wfattachmentNames + "','" + wfattachmentIndex + "'";
				String apInsertInputXML = CommonMethods.apInsert(cabinetName, sessionId, columnName, strValues,
						"WFMAILQUEUETABLE");
				CourtOrder_PCWICreate_Log.CourtOrder_PCWICreate_Logger.debug("APInsertInputXML: " + apInsertInputXML);
				String apInsertOutputXML = CommonMethods.WFNGExecute(apInsertInputXML, jtsIP, jtsPort, 1);
				CourtOrder_PCWICreate_Log.CourtOrder_PCWICreate_Logger.debug("APInsertOutputXML: " + apInsertOutputXML);
				XMLParser xmlParserAPInsert = new XMLParser(apInsertOutputXML);
				String apInsertMaincode = xmlParserAPInsert.getValueOf("MainCode");
				CourtOrder_PCWICreate_Log.CourtOrder_PCWICreate_Logger
						.debug("Status of apInsertMaincode  " + apInsertMaincode);
				if (apInsertMaincode.equalsIgnoreCase("0")) {
					CourtOrder_PCWICreate_Log.CourtOrder_PCWICreate_Logger
							.debug("ApInsert successful: " + apInsertMaincode);
					CourtOrder_PCWICreate_Log.CourtOrder_PCWICreate_Logger
							.debug("Inserted in WFMAILQUEUE table successfully.");

					File finalFolder2 = new File(newExcelFilePath);
					if (finalFolder2.exists()) {
						File fDumpFolder = new File(newExcelFilePath);
						fDumpFolder.delete();
					}
					// update into external table
					updateTable("ng_CourtOrder_exttable", "is_archival_mail_trigerred", "'Y'",
							"Requested_Channel ='CIR - Bulk' and Request_Reference_No = '" + Request_Reference_No + "'",
							jtsIP, jtsPort, cabinetName);

				} else {
					CourtOrder_PCWICreate_Log.CourtOrder_PCWICreate_Logger
							.debug("ApInsert failed: " + apInsertMaincode);
				}
			}
		} catch (Exception ex) {
			CourtOrder_SystemAutoRemittance obj1 = new CourtOrder_SystemAutoRemittance();
			String exception = obj1.customException(ex);
			CourtOrder_PCWICreate_Log.CourtOrder_PCWICreate_Logger
					.debug("Exception in CreateExcel_CIR_Freeze :" + exception);
		} catch (JPISException e) {
			CourtOrder_PCWICreate_Log.CourtOrder_PCWICreate_Logger.debug("Exception getMessage() 2 :" + e.getMessage());
		}
	}

	public void CreateExcel_CIR_Prohibited(String Request_Reference_No, String cabinetName, String sJtsIp,
			String iJtsPort, String sessionId) {
		try {
			CIRBulk_TempReportPath = CourtOrderConfigParamMap.get("CIRBulk_TempReportPath");
			CourtOrder_PCWICreate_Log.CourtOrder_PCWICreate_Logger
					.debug("CIRBulk_TempReportPath: " + CIRBulk_TempReportPath);
			String workItemNameDoc = "";
			XSSFWorkbook workbook = new XSSFWorkbook();
			XSSFSheet spreadsheet = workbook.createSheet("CIR-Bulk Prohibited");
			XSSFRow row;
			Map<Integer, Object[]> data = new TreeMap<Integer, Object[]>();
			int keyvalue = 1;
			int sno = 1;
			data.put(keyvalue,
					new Object[] { "S.No.", "Requested Authority", "Notice Case No", "Due Date", "Name", "Emirates ID",
							"Passport", "Date Of Birth", "Nationality", "Trade License No", "Date of Establishment",
							"Country Of Incorporation", "RAK / Non RAK Customer", "CIF No.", "Related Party CIF" });

			String DBQuery_1 = "Select Created_wi_name,Name,Emirates_ID,Passport,Trade_License_No,Date_of_Birth,"
					+ "Country_of_Incorporation,Date_of_Establishment,Nationality,Requested_Authority,Due_Date,Notice_Case_No,Sno "
					+ "from NG_courtOrder_CIR_ExcelData with(nolock) "
					+ "where is_wi_created = 'Y' and Request_Reference_No = '" + Request_Reference_No + "' "
					+ "order by cast(Sno as int);";
			String extTabDataIPXML_1 = CommonMethods.apSelectWithColumnNames(DBQuery_1,
					CommonConnection.getCabinetName(),
					CommonConnection.getSessionID(CourtOrder_PCWICreate_Log.CourtOrder_PCWICreate_Logger, false));
			CourtOrder_PCWICreate_Log.CourtOrder_PCWICreate_Logger.debug("extTabDataIPXML_1: " + extTabDataIPXML_1);
			String extTabDataOPXML_1 = WFNGExecute(extTabDataIPXML_1, CommonConnection.getJTSIP(),
					CommonConnection.getJTSPort(), 1);
			CourtOrder_PCWICreate_Log.CourtOrder_PCWICreate_Logger.debug("extTabDataOPXML_1: " + extTabDataOPXML_1);
			XMLParser xmlParserData_1 = new XMLParser(extTabDataOPXML_1);
			int iTotalrec_1 = Integer.parseInt(xmlParserData_1.getValueOf("TotalRetrieved"));
			if (xmlParserData_1.getValueOf("MainCode").equalsIgnoreCase("0") && iTotalrec_1 > 0) {
				String xmlDataExtTab1 = xmlParserData_1.getNextValueOf("Record");
				xmlDataExtTab1 = xmlDataExtTab1.replaceAll("[ ]+>", ">").replaceAll("<[ ]+", "<");
				NGXmlList objWorkList1 = xmlParserData_1.createList("Records", "Record");

				String wi_name = "", authorityName = "", dueDate = "", fullName = "", emiratesID = "", passport = "",
						tradeLicNo = "", dateOfBirth = "", countryOfIncorp = "", nationality = "",
						dateOfEstablishment = "", noticeCaseNo = "", Sno = "";
				for (; objWorkList1.hasMoreElements(true); objWorkList1.skip(true)) {
					keyvalue += 1;
					wi_name = objWorkList1.getVal("Created_wi_name");
					fullName = objWorkList1.getVal("Name");
					emiratesID = objWorkList1.getVal("Emirates_ID");
					passport = objWorkList1.getVal("Passport");
					tradeLicNo = objWorkList1.getVal("Trade_License_No");
					dateOfBirth = objWorkList1.getVal("Date_of_Birth");
					countryOfIncorp = objWorkList1.getVal("Country_of_Incorporation");
					dateOfEstablishment = objWorkList1.getVal("Date_of_Establishment");
					nationality = objWorkList1.getVal("Nationality");
					authorityName = objWorkList1.getVal("Requested_Authority");
					dueDate = objWorkList1.getVal("Due_Date");
					noticeCaseNo = objWorkList1.getVal("Notice_Case_No");
					Sno = objWorkList1.getVal("Sno");

					CourtOrder_PCWICreate_Log.CourtOrder_PCWICreate_Logger.debug("wi_name is: " + wi_name);
					// for later use
					if (!"".equalsIgnoreCase(wi_name) && !wi_name.isEmpty()) {
						workItemNameDoc = wi_name;
					}
					// fetching values customer main table
					String DBQuery_2 = "Select CIF as CIFID,CUSTOMER_IDENTIFIED as CUSTOMER_IDENTIFIED_as, "
							+ "FULL_NAME as FULL_NAME, 'INDIVIDUAL' as recordType from NG_COURTORDER_GR_INDIVIDUAL_CUSTOMER_DETAILS "
							+ "with(nolock) where wi_name = '" + wi_name + "' and (PASSPORT = '" + passport
							+ "' or EMIRATES_ID = '" + emiratesID + "') "
							+ "union all Select CIF_ID as CIFID,CUSTOMER_IDENTIFIED_as as CUSTOMER_IDENTIFIED_as, "
							+ "company_Name as FULL_NAME,'NON-INDIVIDUAL' as recordType from NG_COURTORDER_GR_NON_INDIVIDUAL_CUSTOMER_DETAILS "
							+ "with(nolock) where wi_name = '" + wi_name + "' and Trade_License_Number = '" + tradeLicNo
							+ "'";
					String extTabDataIPXML_2 = CommonMethods.apSelectWithColumnNames(DBQuery_2,
							CommonConnection.getCabinetName(), CommonConnection
									.getSessionID(CourtOrder_PCWICreate_Log.CourtOrder_PCWICreate_Logger, false));
					CourtOrder_PCWICreate_Log.CourtOrder_PCWICreate_Logger
							.debug("extTabDataIPXML_2: " + extTabDataIPXML_2);
					String extTabDataOPXML_2 = WFNGExecute(extTabDataIPXML_2, CommonConnection.getJTSIP(),
							CommonConnection.getJTSPort(), 1);
					CourtOrder_PCWICreate_Log.CourtOrder_PCWICreate_Logger
							.debug("extTabDataOPXML_2: " + extTabDataOPXML_2);
					XMLParser xmlParserData_2 = new XMLParser(extTabDataOPXML_2);
					int iTotalrec_2 = Integer.parseInt(xmlParserData_2.getValueOf("TotalRetrieved"));
					if (xmlParserData_2.getValueOf("MainCode").equalsIgnoreCase("0") && iTotalrec_2 > 0) {
						String xmlDataExtTab2 = xmlParserData_2.getNextValueOf("Record");
						xmlDataExtTab2 = xmlDataExtTab2.replaceAll("[ ]+>", ">").replaceAll("<[ ]+", "<");
						NGXmlList objWorkList2 = xmlParserData_2.createList("Records", "Record");

						String cifID = "", customerIdentifiedAs = "", custNameIndv = "", custNameNonIndv = "",
								recordType = "", custName = "";
						for (; objWorkList2.hasMoreElements(true); objWorkList2.skip(true)) {
							keyvalue += 1;
							cifID = objWorkList2.getVal("CIFID");
							customerIdentifiedAs = objWorkList2.getVal("CUSTOMER_IDENTIFIED_as");
							custName = objWorkList2.getVal("FULL_NAME");
							recordType = objWorkList2.getVal("recordType");

							if ("INDIVIDUAL".equalsIgnoreCase(recordType)) {
								custNameIndv = custName;
							} else if ("NON-INDIVIDUAL".equalsIgnoreCase(recordType)) {
								custNameNonIndv = custName;
							}
							// fetching values from related party table
							String DBQuery_3 = "";
							StringBuilder allRelatedCifIds = new StringBuilder();
							if ("Rak-Bank".equalsIgnoreCase(customerIdentifiedAs)) {
								DBQuery_3 = "Select RELATED_CIF_ID from NG_COURTORDER_GR_INDIVIDUAL_RELATED_PARTY_DETAILS "
										+ "with(nolock) where wi_name = '" + wi_name + "'  and CIF_ID = '" + cifID
										+ "' " + "union all Select RELATED_CIF_ID from "
										+ "NG_COURTORDER_GR_NON_INDIVIDUAL_RELATED_PARTY_DETAILS "
										+ "with(nolock) where wi_name = '" + wi_name + "' and CIF_ID = '" + cifID + "'";
							} else if ("Non-Rak-Bank".equalsIgnoreCase(customerIdentifiedAs)) {
								DBQuery_3 = "Select RELATED_CIF_ID from NG_COURTORDER_GR_INDIVIDUAL_RELATED_PARTY_DETAILS "
										+ "with(nolock) where wi_name = '" + wi_name + "' and CUSTOMER_NAME = '"
										+ custNameIndv + "' union all Select "
										+ "RELATED_CIF_ID from NG_COURTORDER_GR_NON_INDIVIDUAL_RELATED_PARTY_DETAILS "
										+ "with(nolock) where wi_name = '" + wi_name + "' and CUSTOMER_NAME = '"
										+ custNameNonIndv + "'";
							}

							String extTabDataIPXML_3 = CommonMethods.apSelectWithColumnNames(DBQuery_3,
									CommonConnection.getCabinetName(), CommonConnection.getSessionID(
											CourtOrder_PCWICreate_Log.CourtOrder_PCWICreate_Logger, false));
							CourtOrder_PCWICreate_Log.CourtOrder_PCWICreate_Logger
									.debug("extTabDataIPXML_3: " + extTabDataIPXML_3);
							String extTabDataOPXML_3 = WFNGExecute(extTabDataIPXML_3, CommonConnection.getJTSIP(),
									CommonConnection.getJTSPort(), 1);
							CourtOrder_PCWICreate_Log.CourtOrder_PCWICreate_Logger
									.debug("extTabDataOPXML_3: " + extTabDataOPXML_3);
							XMLParser xmlParserData_3 = new XMLParser(extTabDataOPXML_3);
							int iTotalrec_3 = Integer.parseInt(xmlParserData_3.getValueOf("TotalRetrieved"));
							if (xmlParserData_3.getValueOf("MainCode").equalsIgnoreCase("0") && iTotalrec_3 > 0) {
								String xmlDataExtTab3 = xmlParserData_3.getNextValueOf("Record");
								xmlDataExtTab3 = xmlDataExtTab3.replaceAll("[ ]+>", ">").replaceAll("<[ ]+", "<");
								NGXmlList objWorkList3 = xmlParserData_3.createList("Records", "Record");
								CourtOrder_PCWICreate_Log.CourtOrder_PCWICreate_Logger
										.debug("Related Party grid is not empty");
								for (; objWorkList3.hasMoreElements(true); objWorkList3.skip(true)) {
									keyvalue += 1;
									String serialNo = String.valueOf(Sno);
									if (allRelatedCifIds.length() > 0) {
										allRelatedCifIds.append(",");
									}
									allRelatedCifIds = allRelatedCifIds.append(objWorkList3.getVal("RELATED_CIF_ID"));

									data.put(keyvalue,
											new Object[] { serialNo, authorityName, noticeCaseNo, dueDate, fullName,
													emiratesID, passport, dateOfBirth, nationality, tradeLicNo,
													dateOfEstablishment, countryOfIncorp, customerIdentifiedAs, cifID,
													allRelatedCifIds.toString() });
									// sno++;
								}
							}
							// in case related grid empty
							else {
								String serialNo = String.valueOf(Sno);
								data.put(keyvalue,
										new Object[] { serialNo, authorityName, noticeCaseNo, dueDate, fullName,
												emiratesID, passport, dateOfBirth, nationality, tradeLicNo,
												dateOfEstablishment, countryOfIncorp, customerIdentifiedAs, cifID,
												allRelatedCifIds.toString() });
								// sno++;
							}
						}
					}

				}
			}
			//
			Set<Integer> keyid = data.keySet();
			int rowid = 0;
			for (int key : keyid) {
				row = spreadsheet.createRow(rowid++);
				Object[] objarr = data.get(key);
				int cellid = 0;
				for (Object obj : objarr) {
					Cell cell = row.createCell(cellid++);
					cell.setCellValue((String) obj);
				}
			}
			DateFormat dateFormat = new SimpleDateFormat("dd-MM-yyyy");
			String ReportDatetime = dateFormat.format(new Date());
			String CIRBulk_Prohibited_Report_Name = "CIR-Bulk Prohibited_" + Request_Reference_No;
			String newExcelFilePath = CIRBulk_TempReportPath + File.separator + CIRBulk_Prohibited_Report_Name
					+ ".xlsx";
			File finalFolder = new File(newExcelFilePath);
			if (finalFolder.exists()) {
				File fDumpFolder = new File(newExcelFilePath);
				fDumpFolder.delete();
			}
			FileOutputStream out = new FileOutputStream(new File(newExcelFilePath));
			workbook.write(out);
			out.close();
			// fetching details for addDoc
			String docPath = newExcelFilePath;
			JPISIsIndex ISINDEX = new JPISIsIndex();
			JPDBRecoverDocData JPISDEC = new JPDBRecoverDocData();
			CPISDocumentTxn.AddDocument_MT(null, jtsIP, Short.parseShort(smsPort), cabinetName,
					Short.parseShort(volumeID), docPath, JPISDEC, "", ISINDEX);
			CourtOrder_PCWICreate_Log.CourtOrder_PCWICreate_Logger.debug("After add document mt successful: ");
			String sISIndex = ISINDEX.m_nDocIndex + "#" + ISINDEX.m_sVolumeId;
			CourtOrder_PCWICreate_Log.CourtOrder_PCWICreate_Logger.debug(" sISIndex: " + sISIndex);
			String DocumentType = "N";
			String strDocumentName = CIRBulk_Prohibited_Report_Name;
			String strExtension = "xlsx";
			File file = new File(newExcelFilePath);
			long lLngFileSize = 0L;
			lLngFileSize = file.length();
			String lstrDocFileSize = Long.toString(lLngFileSize);
			String sMappedInputXml = CommonMethods.getNGOAddDocument(CIRBulk_Report_FolderIndex, strDocumentName,
					DocumentType, strExtension, sISIndex, lstrDocFileSize, volumeID, cabinetName, sessionId);
			CourtOrder_PCWICreate_Log.CourtOrder_PCWICreate_Logger
					.debug("Input xml For NGOAddDocument Call: " + sMappedInputXml);
			String sOutputXml = WFNGExecute(sMappedInputXml, CommonConnection.getJTSIP(), CommonConnection.getJTSPort(),
					1);
			sOutputXml = sOutputXml.replace("<Document>", "");
			sOutputXml = sOutputXml.replace("</Document>", "");
			CourtOrder_PCWICreate_Log.CourtOrder_PCWICreate_Logger
					.debug("Output xml For NGOAddDocument Call: " + sOutputXml);
			String statusXML = CommonMethods.getTagValues(sOutputXml, "Status");
			String ErrorMsg = CommonMethods.getTagValues(sOutputXml, "Error");
			CourtOrder_PCWICreate_Log.CourtOrder_PCWICreate_Logger
					.debug(" The maincode of the output xml file is " + statusXML);
			if (statusXML.equalsIgnoreCase("0")) {
				// fetching CIR-Bulk Freeze
				String DBQuery_4 = "Select top 1 ISnull(ImageIndex,'') as ImageIndex,ISnull(concat(NAME,'.',AppName),'') as ATTACHMENTNAMES, volumeId from pdbdocument with (nolock) "
						+ "WHERE DocumentIndex in (select DocumentIndex from PDBDocumentContent where ParentFolderIndex =(select FolderIndex from PDBFolder where Name = '"
						+ CIRBulk_ReportOdFolderName + "')) and" + " name like '" + strDocumentName
						+ "%' order by DocumentIndex desc";
				String extTabDataIPXML_4 = CommonMethods.apSelectWithColumnNames(DBQuery_4,
						CommonConnection.getCabinetName(),
						CommonConnection.getSessionID(CourtOrder_PCWICreate_Log.CourtOrder_PCWICreate_Logger, false));
				CourtOrder_PCWICreate_Log.CourtOrder_PCWICreate_Logger.debug("extTabDataIPXML_4: " + extTabDataIPXML_4);
				String extTabDataOPXML_4 = WFNGExecute(extTabDataIPXML_4, CommonConnection.getJTSIP(),
						CommonConnection.getJTSPort(), 1);
				CourtOrder_PCWICreate_Log.CourtOrder_PCWICreate_Logger.debug("extTabDataOPXML_4: " + extTabDataOPXML_4);
				XMLParser xmlParserData_4 = new XMLParser(extTabDataOPXML_4);
				int iTotalrec_4 = Integer.parseInt(xmlParserData_4.getValueOf("TotalRetrieved"));
				String ImageIndex = "", ATTACHMENTNAMES = "", volumeId = "";
				if (xmlParserData_4.getValueOf("MainCode").equalsIgnoreCase("0") && iTotalrec_4 > 0) {
					String xmlDataExtTab = xmlParserData_4.getNextValueOf("Record");
					xmlDataExtTab = xmlDataExtTab.replaceAll("[ ]+>", ">").replaceAll("<[ ]+", "<");
					NGXmlList objWorkList4 = xmlParserData_4.createList("Records", "Record");
					for (; objWorkList4.hasMoreElements(true); objWorkList4.skip(true)) {
						ImageIndex = objWorkList4.getVal("ImageIndex");
						ATTACHMENTNAMES = objWorkList4.getVal("ATTACHMENTNAMES");
						volumeId = objWorkList4.getVal("volumeId");
					}
				}
				String wfattachmentNames = "", wfattachmentIndex = "";
				if (!"".equalsIgnoreCase(ATTACHMENTNAMES) && !ATTACHMENTNAMES.isEmpty()) {
					wfattachmentNames = ATTACHMENTNAMES + ";";
				}
				if (!"".equalsIgnoreCase(ImageIndex) && !ImageIndex.isEmpty() && !"".equalsIgnoreCase(volumeId)
						&& !volumeId.isEmpty()) {
					wfattachmentIndex = ImageIndex + "#" + volumeId + "#;";
				}
				// fetching other WI doc's
				String docToFetched = "Central Bank Attachment";
				String DBQuery_5 = "SELECT ISnull(ImageIndex,'') as ImageIndex, ISnull(concat(NAME,'.',AppName),'') "
						+ "as ATTACHMENTNAMES,volumeId FROM " + "PDBDocument WITH (NOLOCK) WHERE "
						+ "DocumentIndex IN (SELECT DocumentIndex FROM PDBDocumentContent a WITH (NOLOCK) "
						+ "JOIN PDBFolder b WITH (NOLOCK) ON b.FolderIndex = a.ParentFolderIndex WHERE Name = '"
						+ workItemNameDoc + "' ) AND Name in ('" + docToFetched + "');";
				String extTabDataIPXML_5 = CommonMethods.apSelectWithColumnNames(DBQuery_5,
						CommonConnection.getCabinetName(),
						CommonConnection.getSessionID(CourtOrder_PCWICreate_Log.CourtOrder_PCWICreate_Logger, false));
				CourtOrder_PCWICreate_Log.CourtOrder_PCWICreate_Logger.debug("extTabDataIPXML_5: " + extTabDataIPXML_5);
				String extTabDataOPXML_5 = WFNGExecute(extTabDataIPXML_5, CommonConnection.getJTSIP(),
						CommonConnection.getJTSPort(), 1);
				CourtOrder_PCWICreate_Log.CourtOrder_PCWICreate_Logger.debug("extTabDataOPXML_5: " + extTabDataOPXML_5);
				XMLParser xmlParserData_5 = new XMLParser(extTabDataOPXML_5);
				int iTotalrec_5 = Integer.parseInt(xmlParserData_5.getValueOf("TotalRetrieved"));
				String ImageIndex2 = "", ATTACHMENTNAMES2 = "", volumeId2 = "";
				if (xmlParserData_5.getValueOf("MainCode").equalsIgnoreCase("0") && iTotalrec_5 > 0) {
					String xmlDataExtTab = xmlParserData_5.getNextValueOf("Record");
					xmlDataExtTab = xmlDataExtTab.replaceAll("[ ]+>", ">").replaceAll("<[ ]+", "<");
					NGXmlList objWorkList5 = xmlParserData_5.createList("Records", "Record");
					for (; objWorkList5.hasMoreElements(true); objWorkList5.skip(true)) {
						ImageIndex2 = objWorkList5.getVal("ImageIndex");
						ATTACHMENTNAMES2 = objWorkList5.getVal("ATTACHMENTNAMES");
						volumeId2 = objWorkList5.getVal("volumeId");
					}
				}
				//
				if (!"".equalsIgnoreCase(ATTACHMENTNAMES2) && !ATTACHMENTNAMES2.isEmpty()) {
					if (!"".equalsIgnoreCase(wfattachmentNames)) {
						wfattachmentNames += ATTACHMENTNAMES2 + ";";
					} else {
						wfattachmentNames = ATTACHMENTNAMES2 + ";";
					}
				}
				if (!"".equalsIgnoreCase(ImageIndex2) && !ImageIndex2.isEmpty() && !"".equalsIgnoreCase(volumeId2)
						&& !volumeId2.isEmpty()) {
					if (!"".equalsIgnoreCase(wfattachmentIndex)) {
						wfattachmentIndex += ImageIndex2 + "#" + volumeId2 + "#;";
					} else {
						wfattachmentIndex = ImageIndex2 + "#" + volumeId2 + "#;";
					}
				}
				// fetching main input excel file
				String inputExcelDocName = "CIR Prohibited-" + Request_Reference_No;
				String DBQuery_6 = "Select top 1 ISnull(ImageIndex,'') as ImageIndex,ISnull(concat(NAME,'.',AppName),'') as ATTACHMENTNAMES, volumeId from pdbdocument with (nolock) "
						+ "WHERE DocumentIndex in (select DocumentIndex from PDBDocumentContent where ParentFolderIndex =(select FolderIndex from PDBFolder where Name = '"
						+ CIRBulk_ReportOdFolderName + "')) and" + " name like '" + inputExcelDocName
						+ "%' order by DocumentIndex desc";
				String extTabDataIPXML_6 = CommonMethods.apSelectWithColumnNames(DBQuery_6,
						CommonConnection.getCabinetName(),
						CommonConnection.getSessionID(CourtOrder_PCWICreate_Log.CourtOrder_PCWICreate_Logger, false));
				CourtOrder_PCWICreate_Log.CourtOrder_PCWICreate_Logger.debug("extTabDataIPXML_6: " + extTabDataIPXML_6);
				String extTabDataOPXML_6 = WFNGExecute(extTabDataIPXML_6, CommonConnection.getJTSIP(),
						CommonConnection.getJTSPort(), 1);
				CourtOrder_PCWICreate_Log.CourtOrder_PCWICreate_Logger.debug("extTabDataOPXML_6: " + extTabDataOPXML_6);
				XMLParser xmlParserData_6 = new XMLParser(extTabDataOPXML_6);
				int iTotalrec_6 = Integer.parseInt(xmlParserData_6.getValueOf("TotalRetrieved"));
				String ImageIndex3 = "", ATTACHMENTNAMES3 = "", volumeId3 = "";
				if (xmlParserData_6.getValueOf("MainCode").equalsIgnoreCase("0") && iTotalrec_6 > 0) {
					String xmlDataExtTab = xmlParserData_6.getNextValueOf("Record");
					xmlDataExtTab = xmlDataExtTab.replaceAll("[ ]+>", ">").replaceAll("<[ ]+", "<");
					NGXmlList objWorkList6 = xmlParserData_6.createList("Records", "Record");
					for (; objWorkList6.hasMoreElements(true); objWorkList6.skip(true)) {
						ImageIndex3 = objWorkList6.getVal("ImageIndex");
						ATTACHMENTNAMES3 = objWorkList6.getVal("ATTACHMENTNAMES");
						volumeId3 = objWorkList6.getVal("volumeId");
					}
				}
				if (!"".equalsIgnoreCase(ATTACHMENTNAMES3) && !ATTACHMENTNAMES3.isEmpty()) {
					if (!"".equalsIgnoreCase(wfattachmentNames)) {
						wfattachmentNames += ATTACHMENTNAMES3 + ";";
					} else {
						wfattachmentNames = ATTACHMENTNAMES3 + ";";
					}
				}
				if (!"".equalsIgnoreCase(ImageIndex3) && !ImageIndex3.isEmpty() && !"".equalsIgnoreCase(volumeId3)
						&& !volumeId3.isEmpty()) {
					if (!"".equalsIgnoreCase(wfattachmentIndex)) {
						wfattachmentIndex += ImageIndex3 + "#" + volumeId3 + "#;";
					} else {
						wfattachmentIndex = ImageIndex3 + "#" + volumeId3 + "#;";
					}
				}
				//
				CourtOrder_PCWICreate_Log.CourtOrder_PCWICreate_Logger
						.debug("Final wfattachmentNames: " + wfattachmentNames);
				CourtOrder_PCWICreate_Log.CourtOrder_PCWICreate_Logger
						.debug("Final wfattachmentIndex: " + wfattachmentIndex);
				//
				String loggerInMailTable = "CIR-Bulk Prohibited_" + Request_Reference_No;
				SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd HH:MM:ss");
				String insertedDateTime = simpleDateFormat.format(new Date());
				DateFormat dateFormatnew = new SimpleDateFormat("dd-MM-yyyy");
				String ReportDate = dateFormat.format(new Date());
				String MailSubject = "CIR - " + Request_Reference_No + " - Prohibited";
				String FinalMailStr = CIRBulk_Prohibited_Report_Body;
				String columnName = "MAILFROM,MAILTO,MAILSUBJECT,MAILMESSAGE,MAILCONTENTTYPE,MAILPRIORITY,MAILSTATUS,"
						+ "INSERTEDBY,MAILACTIONTYPE,INSERTEDTIME,PROCESSDEFID,PROCESSINSTANCEID,WORKITEMID,ACTIVITYID,"
						+ "NOOFTRIALS,attachmentNames,attachmentISINDEX";
				String strValues = "'" + CIRBulk_Report_FromMail + "','" + CIRBulk_Report_ToMail + "',N'" + MailSubject
						+ "',N'" + FinalMailStr + "','text/html;charset=UTF-8','1','N','CUSTOM','TRIGGER','"
						+ insertedDateTime + "','" + processDefId + "','" + loggerInMailTable + "','1','1','0','"
						+ wfattachmentNames + "','" + wfattachmentIndex + "'";
				String apInsertInputXML = CommonMethods.apInsert(cabinetName, sessionId, columnName, strValues,
						"WFMAILQUEUETABLE");
				CourtOrder_PCWICreate_Log.CourtOrder_PCWICreate_Logger.debug("APInsertInputXML: " + apInsertInputXML);
				String apInsertOutputXML = CommonMethods.WFNGExecute(apInsertInputXML, jtsIP, jtsPort, 1);
				CourtOrder_PCWICreate_Log.CourtOrder_PCWICreate_Logger.debug("APInsertOutputXML: " + apInsertOutputXML);
				XMLParser xmlParserAPInsert = new XMLParser(apInsertOutputXML);
				String apInsertMaincode = xmlParserAPInsert.getValueOf("MainCode");
				CourtOrder_PCWICreate_Log.CourtOrder_PCWICreate_Logger
						.debug("Status of apInsertMaincode  " + apInsertMaincode);
				if (apInsertMaincode.equalsIgnoreCase("0")) {
					CourtOrder_PCWICreate_Log.CourtOrder_PCWICreate_Logger
							.debug("ApInsert successful: " + apInsertMaincode);
					CourtOrder_PCWICreate_Log.CourtOrder_PCWICreate_Logger
							.debug("Inserted in WFMAILQUEUE table successfully.");

					File finalFolder2 = new File(newExcelFilePath);
					if (finalFolder2.exists()) {
						File fDumpFolder = new File(newExcelFilePath);
						fDumpFolder.delete();
					}
					// update into external table
					updateTable("ng_CourtOrder_exttable", "is_archival_mail_trigerred", "'Y'",
							"Requested_Channel ='CIR - Bulk' and Request_Reference_No = '" + Request_Reference_No + "'",
							jtsIP, jtsPort, cabinetName);
				} else {
					CourtOrder_PCWICreate_Log.CourtOrder_PCWICreate_Logger
							.debug("ApInsert failed: " + apInsertMaincode);
				}
			}
		} catch (Exception ex) {
			CourtOrder_SystemAutoRemittance obj1 = new CourtOrder_SystemAutoRemittance();
			String exception = obj1.customException(ex);
			CourtOrder_PCWICreate_Log.CourtOrder_PCWICreate_Logger
					.debug("Exception in CreateExcel_CIR_Prohibited :" + exception);
		} catch (JPISException e) {
			CourtOrder_PCWICreate_Log.CourtOrder_PCWICreate_Logger.debug("Exception getMessage() 2 :" + e.getMessage());
		}
	}

	public void CreateExcel_CIR_Inquiry(String Request_Reference_No, String cabinetName, String sJtsIp, String iJtsPort,
			String sessionId) {
		try {
			CIRBulk_TempReportPath = CourtOrderConfigParamMap.get("CIRBulk_TempReportPath");
			CourtOrder_PCWICreate_Log.CourtOrder_PCWICreate_Logger
					.debug("CIRBulk_TempReportPath: " + CIRBulk_TempReportPath);
			String workItemNameDoc = "";
			XSSFWorkbook workbook = new XSSFWorkbook();
			XSSFSheet spreadsheet = workbook.createSheet("CIR-Bulk Inquiry");
			XSSFRow row;
			Map<Integer, Object[]> data = new TreeMap<Integer, Object[]>();
			int keyvalue = 1;
			int sno = 1;

			data.put(keyvalue,
					new Object[] { "S.No.", "Requested Authority", "Notice Case No", "Due Date", "Name", "Emirates ID",
							"Passport", "Date Of Birth", "Nationality", "Trade License No", "Date of Establishment",
							"Country Of Incorporation", "RAK / Non RAK Customer", "CIF No.", "Account No.",
							"Account Balance" });

			String DBQuery_1 = "Select Created_wi_name,Name,Emirates_ID,Date_of_Birth,"
					+ "Due_Date,Trade_License_No,Passport,Notice_Case_No,Date_of_Establishment,Requested_Authority,Sno"
					+ " from NG_courtOrder_CIR_ExcelData with(nolock) "
					+ "where is_wi_created = 'Y' and Request_Reference_No = '" + Request_Reference_No + "' "
					+ "order by cast(Sno as int);";
			String extTabDataIPXML_1 = CommonMethods.apSelectWithColumnNames(DBQuery_1,
					CommonConnection.getCabinetName(),
					CommonConnection.getSessionID(CourtOrder_PCWICreate_Log.CourtOrder_PCWICreate_Logger, false));
			CourtOrder_PCWICreate_Log.CourtOrder_PCWICreate_Logger.debug("extTabDataIPXML_1: " + extTabDataIPXML_1);
			String extTabDataOPXML_1 = WFNGExecute(extTabDataIPXML_1, CommonConnection.getJTSIP(),
					CommonConnection.getJTSPort(), 1);
			CourtOrder_PCWICreate_Log.CourtOrder_PCWICreate_Logger.debug("extTabDataOPXML_1: " + extTabDataOPXML_1);
			XMLParser xmlParserData_1 = new XMLParser(extTabDataOPXML_1);
			int iTotalrec_1 = Integer.parseInt(xmlParserData_1.getValueOf("TotalRetrieved"));
			if (xmlParserData_1.getValueOf("MainCode").equalsIgnoreCase("0") && iTotalrec_1 > 0) {
				String xmlDataExtTab1 = xmlParserData_1.getNextValueOf("Record");
				xmlDataExtTab1 = xmlDataExtTab1.replaceAll("[ ]+>", ">").replaceAll("<[ ]+", "<");
				NGXmlList objWorkList1 = xmlParserData_1.createList("Records", "Record");

				String wi_name = "", dueDate = "", fullName = "", emiratesID = "", dateOfBirth = "", passportExcel = "",
						tradeLicNoExcel = "", noticeCaseNo = "", dateOfEstablishment = "", authorityName = "", Sno = "";
				for (; objWorkList1.hasMoreElements(true); objWorkList1.skip(true)) {
					keyvalue += 1;
					wi_name = objWorkList1.getVal("Created_wi_name");
					fullName = objWorkList1.getVal("Name");
					emiratesID = objWorkList1.getVal("Emirates_ID");
					dateOfBirth = objWorkList1.getVal("Date_of_Birth");
					dueDate = objWorkList1.getVal("Due_Date");
					passportExcel = objWorkList1.getVal("Passport");
					tradeLicNoExcel = objWorkList1.getVal("Trade_License_No");
					noticeCaseNo = objWorkList1.getVal("Notice_Case_No");
					dateOfEstablishment = objWorkList1.getVal("Date_of_Establishment");
					authorityName = objWorkList1.getVal("Requested_Authority");
					Sno = objWorkList1.getVal("Sno");

					CourtOrder_PCWICreate_Log.CourtOrder_PCWICreate_Logger.debug("wi_name is: " + wi_name);
					// for later use
					if (!"".equalsIgnoreCase(wi_name) && !wi_name.isEmpty()) {
						workItemNameDoc = wi_name;
					}
					// fetching values related party table
					String DBQuery_2 = "Select PASSPORT as passTradeNo, NATIONALITY as nationCountIncorp, "
							+ "null as tLIssueAuth,'INDIVIDUAL' as recordType,CUSTOMER_IDENTIFIED as CUSTOMER_IDENTIFIED_as,"
							+ "CIF as CIF_ID from NG_COURTORDER_GR_INDIVIDUAL_CUSTOMER_DETAILS "
							+ "with(nolock) where wi_name = '" + wi_name + "' and " + "(PASSPORT = '" + passportExcel
							+ "' or EMIRATES_ID = '" + emiratesID + "') " + "union all Select "
							+ "Trade_License_Number as passTradeNo, country_of_Incorporation as nationCountIncorp, "
							+ "TL_Issusing_Autrhority as tLIssueAuth,'NON-INDIVIDUAL' as recordType,"
							+ "CUSTOMER_IDENTIFIED_as as CUSTOMER_IDENTIFIED_as,CIF_ID as CIF_ID from "
							+ "NG_COURTORDER_GR_NON_INDIVIDUAL_CUSTOMER_DETAILS " + "with(nolock) where wi_name = '"
							+ wi_name + "' and Trade_License_Number = '" + tradeLicNoExcel + "'";
					String extTabDataIPXML_2 = CommonMethods.apSelectWithColumnNames(DBQuery_2,
							CommonConnection.getCabinetName(), CommonConnection
									.getSessionID(CourtOrder_PCWICreate_Log.CourtOrder_PCWICreate_Logger, false));
					CourtOrder_PCWICreate_Log.CourtOrder_PCWICreate_Logger
							.debug("extTabDataIPXML_2: " + extTabDataIPXML_2);
					String extTabDataOPXML_2 = WFNGExecute(extTabDataIPXML_2, CommonConnection.getJTSIP(),
							CommonConnection.getJTSPort(), 1);
					CourtOrder_PCWICreate_Log.CourtOrder_PCWICreate_Logger
							.debug("extTabDataOPXML_2: " + extTabDataOPXML_2);
					XMLParser xmlParserData_2 = new XMLParser(extTabDataOPXML_2);
					int iTotalrec_2 = Integer.parseInt(xmlParserData_2.getValueOf("TotalRetrieved"));
					if (xmlParserData_2.getValueOf("MainCode").equalsIgnoreCase("0") && iTotalrec_2 > 0) {
						String xmlDataExtTab2 = xmlParserData_2.getNextValueOf("Record");
						xmlDataExtTab2 = xmlDataExtTab2.replaceAll("[ ]+>", ">").replaceAll("<[ ]+", "<");
						NGXmlList objWorkList2 = xmlParserData_2.createList("Records", "Record");

						String passTradeNo = "", nationCountIncorp = "", nullTLIssueAuth = "", recordType = "",
								passport = "", tradeLicNo = "", nationality = "", countryOfIncorp = "",
								tLIssuingAuthority = "", customerIdentifiedAs = "", cifID = "";
						for (; objWorkList2.hasMoreElements(true); objWorkList2.skip(true)) {
							keyvalue += 1;
							passTradeNo = objWorkList2.getVal("passTradeNo");
							nationCountIncorp = objWorkList2.getVal("nationCountIncorp");
							nullTLIssueAuth = objWorkList2.getVal("tLIssueAuth");
							recordType = objWorkList2.getVal("recordType");
							customerIdentifiedAs = objWorkList2.getVal("CUSTOMER_IDENTIFIED_as");
							cifID = objWorkList2.getVal("CIF_ID");

							if ("INDIVIDUAL".equalsIgnoreCase(recordType)) {
								passport = passTradeNo;
								nationality = nationCountIncorp;
								tLIssuingAuthority = "";
							} else if ("NON-INDIVIDUAL".equalsIgnoreCase(recordType)) {
								tradeLicNo = passTradeNo;
								countryOfIncorp = nationCountIncorp;
								tLIssuingAuthority = nullTLIssueAuth;
							}

							if ("null".equalsIgnoreCase(nullTLIssueAuth) || nullTLIssueAuth.isEmpty()
									|| nullTLIssueAuth == null) {
								nullTLIssueAuth = "";
							}
							// fetching values from product table
							String DBQuery_3 = "Select distinct Agreement_No,Account_balance from "
									+ "NG_COURTORDER_GR_INDIVIDUAL_PRODUCT_DETAILS with(nolock) where wi_name = '"
									+ wi_name + "' and CIF_No = '" + cifID + "' union all Select distinct "
									+ "Agreement_No,Account_balance from NG_COURTORDER_GR_NON_INDIVIDUAL_PRODUCT_DETAILS "
									+ "with(nolock) where wi_name = '" + wi_name + "' and CIF_No = '" + cifID + "'";
							String extTabDataIPXML_3 = CommonMethods.apSelectWithColumnNames(DBQuery_3,
									CommonConnection.getCabinetName(), CommonConnection.getSessionID(
											CourtOrder_PCWICreate_Log.CourtOrder_PCWICreate_Logger, false));
							CourtOrder_PCWICreate_Log.CourtOrder_PCWICreate_Logger
									.debug("extTabDataIPXML_3: " + extTabDataIPXML_3);
							String extTabDataOPXML_3 = WFNGExecute(extTabDataIPXML_3, CommonConnection.getJTSIP(),
									CommonConnection.getJTSPort(), 1);
							CourtOrder_PCWICreate_Log.CourtOrder_PCWICreate_Logger
									.debug("extTabDataOPXML_3: " + extTabDataOPXML_3);
							XMLParser xmlParserData_3 = new XMLParser(extTabDataOPXML_3);
							int iTotalrec_3 = Integer.parseInt(xmlParserData_3.getValueOf("TotalRetrieved"));
							if (xmlParserData_3.getValueOf("MainCode").equalsIgnoreCase("0") && iTotalrec_3 > 0) {
								String xmlDataExtTab3 = xmlParserData_3.getNextValueOf("Record");
								xmlDataExtTab3 = xmlDataExtTab3.replaceAll("[ ]+>", ">").replaceAll("<[ ]+", "<");
								NGXmlList objWorkList3 = xmlParserData_3.createList("Records", "Record");
								CourtOrder_PCWICreate_Log.CourtOrder_PCWICreate_Logger
										.debug("Product grid is not empty");

								String accountNo = "", accountBalance = "";
								for (; objWorkList3.hasMoreElements(true); objWorkList3.skip(true)) {
									String serialNo = String.valueOf(Sno);
									accountNo = objWorkList3.getVal("Agreement_No");
									accountBalance = objWorkList3.getVal("Account_balance");
									keyvalue += 1;
									data.put(keyvalue,
											new Object[] { serialNo, authorityName, noticeCaseNo, dueDate, fullName,
													emiratesID, passport, dateOfBirth, nationality, tradeLicNo,
													dateOfEstablishment, countryOfIncorp, customerIdentifiedAs, cifID,
													accountNo, accountBalance });
									// sno++;
								}
							}
							// in case product grid empty
							else {
								String serialNo = String.valueOf(Sno);
								data.put(keyvalue, new Object[] { serialNo, authorityName, noticeCaseNo, dueDate,
										fullName, emiratesID, passport, dateOfBirth, nationality, tradeLicNo,
										dateOfEstablishment, countryOfIncorp, customerIdentifiedAs, cifID, "", "" });
								// sno++;
							}
						}
					}
					//
				}
			}
			Set<Integer> keyid = data.keySet();
			int rowid = 0;
			for (int key : keyid) {
				row = spreadsheet.createRow(rowid++);
				Object[] objarr = data.get(key);
				int cellid = 0;
				for (Object obj : objarr) {
					Cell cell = row.createCell(cellid++);
					cell.setCellValue((String) obj);
				}
			}
			DateFormat dateFormat = new SimpleDateFormat("dd-MM-yyyy");
			String ReportDatetime = dateFormat.format(new Date());
			String CIRBulk_Inquiry_Report_Name = "CIR-Bulk Inquiry_" + Request_Reference_No;
			String newExcelFilePath = CIRBulk_TempReportPath + File.separator + CIRBulk_Inquiry_Report_Name + ".xlsx";
			File finalFolder = new File(newExcelFilePath);
			if (finalFolder.exists()) {
				File fDumpFolder = new File(newExcelFilePath);
				fDumpFolder.delete();
			}
			FileOutputStream out = new FileOutputStream(new File(newExcelFilePath));
			workbook.write(out);
			out.close();
			// fetching details for addDoc
			String docPath = newExcelFilePath;
			JPISIsIndex ISINDEX = new JPISIsIndex();
			JPDBRecoverDocData JPISDEC = new JPDBRecoverDocData();
			CPISDocumentTxn.AddDocument_MT(null, jtsIP, Short.parseShort(smsPort), cabinetName,
					Short.parseShort(volumeID), docPath, JPISDEC, "", ISINDEX);
			CourtOrder_PCWICreate_Log.CourtOrder_PCWICreate_Logger.debug("After add document mt successful: ");
			String sISIndex = ISINDEX.m_nDocIndex + "#" + ISINDEX.m_sVolumeId;
			CourtOrder_PCWICreate_Log.CourtOrder_PCWICreate_Logger.debug(" sISIndex: " + sISIndex);
			String DocumentType = "N";
			String strDocumentName = CIRBulk_Inquiry_Report_Name;
			String strExtension = "xlsx";
			File file = new File(newExcelFilePath);
			long lLngFileSize = 0L;
			lLngFileSize = file.length();
			String lstrDocFileSize = Long.toString(lLngFileSize);
			String sMappedInputXml = CommonMethods.getNGOAddDocument(CIRBulk_Report_FolderIndex, strDocumentName,
					DocumentType, strExtension, sISIndex, lstrDocFileSize, volumeID, cabinetName, sessionId);
			CourtOrder_PCWICreate_Log.CourtOrder_PCWICreate_Logger
					.debug("Input xml For NGOAddDocument Call: " + sMappedInputXml);
			String sOutputXml = WFNGExecute(sMappedInputXml, CommonConnection.getJTSIP(), CommonConnection.getJTSPort(),
					1);
			sOutputXml = sOutputXml.replace("<Document>", "");
			sOutputXml = sOutputXml.replace("</Document>", "");
			CourtOrder_PCWICreate_Log.CourtOrder_PCWICreate_Logger
					.debug("Output xml For NGOAddDocument Call: " + sOutputXml);
			String statusXML = CommonMethods.getTagValues(sOutputXml, "Status");
			String ErrorMsg = CommonMethods.getTagValues(sOutputXml, "Error");
			CourtOrder_PCWICreate_Log.CourtOrder_PCWICreate_Logger
					.debug(" The maincode of the output xml file is " + statusXML);
			if (statusXML.equalsIgnoreCase("0")) {
				// fetching CIR-Bulk Freeze
				String DBQuery_4 = "Select top 1 ISnull(ImageIndex,'') as ImageIndex,ISnull(concat(NAME,'.',AppName),'') as ATTACHMENTNAMES, volumeId from pdbdocument with (nolock) "
						+ "WHERE DocumentIndex in (select DocumentIndex from PDBDocumentContent where ParentFolderIndex =(select FolderIndex from PDBFolder where Name = '"
						+ CIRBulk_ReportOdFolderName + "')) and" + " name like '" + strDocumentName
						+ "%' order by DocumentIndex desc";
				String extTabDataIPXML_4 = CommonMethods.apSelectWithColumnNames(DBQuery_4,
						CommonConnection.getCabinetName(),
						CommonConnection.getSessionID(CourtOrder_PCWICreate_Log.CourtOrder_PCWICreate_Logger, false));
				CourtOrder_PCWICreate_Log.CourtOrder_PCWICreate_Logger.debug("extTabDataIPXML_4: " + extTabDataIPXML_4);
				String extTabDataOPXML_4 = WFNGExecute(extTabDataIPXML_4, CommonConnection.getJTSIP(),
						CommonConnection.getJTSPort(), 1);
				CourtOrder_PCWICreate_Log.CourtOrder_PCWICreate_Logger.debug("extTabDataOPXML_4: " + extTabDataOPXML_4);
				XMLParser xmlParserData_4 = new XMLParser(extTabDataOPXML_4);
				int iTotalrec_4 = Integer.parseInt(xmlParserData_4.getValueOf("TotalRetrieved"));
				String ImageIndex = "", ATTACHMENTNAMES = "", volumeId = "";
				if (xmlParserData_4.getValueOf("MainCode").equalsIgnoreCase("0") && iTotalrec_4 > 0) {
					String xmlDataExtTab = xmlParserData_4.getNextValueOf("Record");
					xmlDataExtTab = xmlDataExtTab.replaceAll("[ ]+>", ">").replaceAll("<[ ]+", "<");
					NGXmlList objWorkList4 = xmlParserData_4.createList("Records", "Record");
					for (; objWorkList4.hasMoreElements(true); objWorkList4.skip(true)) {
						ImageIndex = objWorkList4.getVal("ImageIndex");
						ATTACHMENTNAMES = objWorkList4.getVal("ATTACHMENTNAMES");
						volumeId = objWorkList4.getVal("volumeId");
					}
				}
				String wfattachmentNames = "", wfattachmentIndex = "";
				if (!"".equalsIgnoreCase(ATTACHMENTNAMES) && !ATTACHMENTNAMES.isEmpty()) {
					wfattachmentNames = ATTACHMENTNAMES + ";";
				}
				if (!"".equalsIgnoreCase(ImageIndex) && !ImageIndex.isEmpty() && !"".equalsIgnoreCase(volumeId)
						&& !volumeId.isEmpty()) {
					wfattachmentIndex = ImageIndex + "#" + volumeId + "#;";
				}
				// fetching other WI doc's
				String docToFetched = "Central Bank Attachment";
				String DBQuery_5 = "SELECT ISnull(ImageIndex,'') as ImageIndex, ISnull(concat(NAME,'.',AppName),'') "
						+ "as ATTACHMENTNAMES,volumeId FROM " + "PDBDocument WITH (NOLOCK) WHERE "
						+ "DocumentIndex IN (SELECT DocumentIndex FROM PDBDocumentContent a WITH (NOLOCK) "
						+ "JOIN PDBFolder b WITH (NOLOCK) ON b.FolderIndex = a.ParentFolderIndex WHERE Name = '"
						+ workItemNameDoc + "' ) AND Name in ('" + docToFetched + "');";
				String extTabDataIPXML_5 = CommonMethods.apSelectWithColumnNames(DBQuery_5,
						CommonConnection.getCabinetName(),
						CommonConnection.getSessionID(CourtOrder_PCWICreate_Log.CourtOrder_PCWICreate_Logger, false));
				CourtOrder_PCWICreate_Log.CourtOrder_PCWICreate_Logger.debug("extTabDataIPXML_5: " + extTabDataIPXML_5);
				String extTabDataOPXML_5 = WFNGExecute(extTabDataIPXML_5, CommonConnection.getJTSIP(),
						CommonConnection.getJTSPort(), 1);
				CourtOrder_PCWICreate_Log.CourtOrder_PCWICreate_Logger.debug("extTabDataOPXML_5: " + extTabDataOPXML_5);
				XMLParser xmlParserData_5 = new XMLParser(extTabDataOPXML_5);
				int iTotalrec_5 = Integer.parseInt(xmlParserData_5.getValueOf("TotalRetrieved"));
				String ImageIndex2 = "", ATTACHMENTNAMES2 = "", volumeId2 = "";
				if (xmlParserData_5.getValueOf("MainCode").equalsIgnoreCase("0") && iTotalrec_5 > 0) {
					String xmlDataExtTab = xmlParserData_5.getNextValueOf("Record");
					xmlDataExtTab = xmlDataExtTab.replaceAll("[ ]+>", ">").replaceAll("<[ ]+", "<");
					NGXmlList objWorkList5 = xmlParserData_5.createList("Records", "Record");
					for (; objWorkList5.hasMoreElements(true); objWorkList5.skip(true)) {
						ImageIndex2 = objWorkList5.getVal("ImageIndex");
						ATTACHMENTNAMES2 = objWorkList5.getVal("ATTACHMENTNAMES");
						volumeId2 = objWorkList5.getVal("volumeId");
					}
				}
				//
				if (!"".equalsIgnoreCase(ATTACHMENTNAMES2) && !ATTACHMENTNAMES2.isEmpty()) {
					if (!"".equalsIgnoreCase(wfattachmentNames)) {
						wfattachmentNames += ATTACHMENTNAMES2 + ";";
					} else {
						wfattachmentNames = ATTACHMENTNAMES2 + ";";
					}
				}
				if (!"".equalsIgnoreCase(ImageIndex2) && !ImageIndex2.isEmpty() && !"".equalsIgnoreCase(volumeId2)
						&& !volumeId2.isEmpty()) {
					if (!"".equalsIgnoreCase(wfattachmentIndex)) {
						wfattachmentIndex += ImageIndex2 + "#" + volumeId2 + "#;";
					} else {
						wfattachmentIndex = ImageIndex2 + "#" + volumeId2 + "#;";
					}
				}
				// fetching main input excel file
				String inputExcelDocName = "CIR Inquiry-" + Request_Reference_No;
				String DBQuery_6 = "Select top 1 ISnull(ImageIndex,'') as ImageIndex,ISnull(concat(NAME,'.',AppName),'') as ATTACHMENTNAMES, volumeId from pdbdocument with (nolock) "
						+ "WHERE DocumentIndex in (select DocumentIndex from PDBDocumentContent where ParentFolderIndex =(select FolderIndex from PDBFolder where Name = '"
						+ CIRBulk_ReportOdFolderName + "')) and" + " name like '" + inputExcelDocName
						+ "%' order by DocumentIndex desc";
				String extTabDataIPXML_6 = CommonMethods.apSelectWithColumnNames(DBQuery_6,
						CommonConnection.getCabinetName(),
						CommonConnection.getSessionID(CourtOrder_PCWICreate_Log.CourtOrder_PCWICreate_Logger, false));
				CourtOrder_PCWICreate_Log.CourtOrder_PCWICreate_Logger.debug("extTabDataIPXML_6: " + extTabDataIPXML_6);
				String extTabDataOPXML_6 = WFNGExecute(extTabDataIPXML_6, CommonConnection.getJTSIP(),
						CommonConnection.getJTSPort(), 1);
				CourtOrder_PCWICreate_Log.CourtOrder_PCWICreate_Logger.debug("extTabDataOPXML_6: " + extTabDataOPXML_6);
				XMLParser xmlParserData_6 = new XMLParser(extTabDataOPXML_6);
				int iTotalrec_6 = Integer.parseInt(xmlParserData_6.getValueOf("TotalRetrieved"));
				String ImageIndex3 = "", ATTACHMENTNAMES3 = "", volumeId3 = "";
				if (xmlParserData_6.getValueOf("MainCode").equalsIgnoreCase("0") && iTotalrec_6 > 0) {
					String xmlDataExtTab = xmlParserData_6.getNextValueOf("Record");
					xmlDataExtTab = xmlDataExtTab.replaceAll("[ ]+>", ">").replaceAll("<[ ]+", "<");
					NGXmlList objWorkList6 = xmlParserData_6.createList("Records", "Record");
					for (; objWorkList6.hasMoreElements(true); objWorkList6.skip(true)) {
						ImageIndex3 = objWorkList6.getVal("ImageIndex");
						ATTACHMENTNAMES3 = objWorkList6.getVal("ATTACHMENTNAMES");
						volumeId3 = objWorkList6.getVal("volumeId");
					}
				}
				if (!"".equalsIgnoreCase(ATTACHMENTNAMES3) && !ATTACHMENTNAMES3.isEmpty()) {
					if (!"".equalsIgnoreCase(wfattachmentNames)) {
						wfattachmentNames += ATTACHMENTNAMES3 + ";";
					} else {
						wfattachmentNames = ATTACHMENTNAMES3 + ";";
					}
				}
				if (!"".equalsIgnoreCase(ImageIndex3) && !ImageIndex3.isEmpty() && !"".equalsIgnoreCase(volumeId3)
						&& !volumeId3.isEmpty()) {
					if (!"".equalsIgnoreCase(wfattachmentIndex)) {
						wfattachmentIndex += ImageIndex3 + "#" + volumeId3 + "#;";
					} else {
						wfattachmentIndex = ImageIndex3 + "#" + volumeId3 + "#;";
					}
				}
				//
				CourtOrder_PCWICreate_Log.CourtOrder_PCWICreate_Logger
						.debug("Final wfattachmentNames: " + wfattachmentNames);
				CourtOrder_PCWICreate_Log.CourtOrder_PCWICreate_Logger
						.debug("Final wfattachmentIndex: " + wfattachmentIndex);
				//
				String loggerInMailTable = "CIR-Bulk Inquiry_" + Request_Reference_No;
				SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd HH:MM:ss");
				String insertedDateTime = simpleDateFormat.format(new Date());
				DateFormat dateFormatnew = new SimpleDateFormat("dd-MM-yyyy");
				String ReportDate = dateFormat.format(new Date());
				String MailSubject = "CIR " + Request_Reference_No + " - Inquiry";
				String FinalMailStr = CIRBulk_Inquiry_Report_Body;
				String columnName = "MAILFROM,MAILTO,MAILSUBJECT,MAILMESSAGE,MAILCONTENTTYPE,MAILPRIORITY,MAILSTATUS,"
						+ "INSERTEDBY,MAILACTIONTYPE,INSERTEDTIME,PROCESSDEFID,PROCESSINSTANCEID,WORKITEMID,ACTIVITYID,"
						+ "NOOFTRIALS,attachmentNames,attachmentISINDEX";
				String strValues = "'" + CIRBulk_Report_FromMail + "','" + CIRBulk_Report_ToMail + "',N'" + MailSubject
						+ "',N'" + FinalMailStr + "','text/html;charset=UTF-8','1','N','CUSTOM','TRIGGER','"
						+ insertedDateTime + "','" + processDefId + "','" + loggerInMailTable + "','1','1','0','"
						+ wfattachmentNames + "','" + wfattachmentIndex + "'";
				String apInsertInputXML = CommonMethods.apInsert(cabinetName, sessionId, columnName, strValues,
						"WFMAILQUEUETABLE");
				CourtOrder_PCWICreate_Log.CourtOrder_PCWICreate_Logger.debug("APInsertInputXML: " + apInsertInputXML);
				String apInsertOutputXML = CommonMethods.WFNGExecute(apInsertInputXML, jtsIP, jtsPort, 1);
				CourtOrder_PCWICreate_Log.CourtOrder_PCWICreate_Logger.debug("APInsertOutputXML: " + apInsertOutputXML);
				XMLParser xmlParserAPInsert = new XMLParser(apInsertOutputXML);
				String apInsertMaincode = xmlParserAPInsert.getValueOf("MainCode");
				CourtOrder_PCWICreate_Log.CourtOrder_PCWICreate_Logger
						.debug("Status of apInsertMaincode  " + apInsertMaincode);
				if (apInsertMaincode.equalsIgnoreCase("0")) {
					CourtOrder_PCWICreate_Log.CourtOrder_PCWICreate_Logger
							.debug("ApInsert successful: " + apInsertMaincode);
					CourtOrder_PCWICreate_Log.CourtOrder_PCWICreate_Logger
							.debug("Inserted in WFMAILQUEUE table successfully.");

					File finalFolder2 = new File(newExcelFilePath);
					if (finalFolder2.exists()) {
						File fDumpFolder = new File(newExcelFilePath);
						fDumpFolder.delete();
					}
					// update into external table
					updateTable("ng_CourtOrder_exttable", "is_archival_mail_trigerred", "'Y'",
							"Requested_Channel ='CIR - Bulk' and Request_Reference_No = '" + Request_Reference_No + "'",
							jtsIP, jtsPort, cabinetName);
				} else {
					CourtOrder_PCWICreate_Log.CourtOrder_PCWICreate_Logger
							.debug("ApInsert failed: " + apInsertMaincode);
				}
			}
		} catch (Exception ex) {
			CourtOrder_SystemAutoRemittance obj1 = new CourtOrder_SystemAutoRemittance();
			String exception = obj1.customException(ex);
			CourtOrder_PCWICreate_Log.CourtOrder_PCWICreate_Logger
					.debug("Exception in CreateExcel_CIR_Inquiry :" + exception);
		} catch (JPISException e) {
			CourtOrder_PCWICreate_Log.CourtOrder_PCWICreate_Logger.debug("Exception getMessage() 2 :" + e.getMessage());
		}
	}

	private void start_PC_WiCreate(String cabinetName, String sJtsIp, String iJtsPort, String sessionId, String queueID,
			int socketConnectionTimeOut, int integrationWaitTime) throws IOException, Exception {

		String query_wi_pc = "select Wi_name,IS_PC_WI_created,itemindex from ng_CourtOrder_exttable with(nolock) where is_pc_wi_required='Y' and IS_PC_WI_created is null";
		String wi_pc_inputXml = CommonMethods.apSelectWithColumnNames(query_wi_pc, CommonConnection.getCabinetName(),
				CommonConnection.getSessionID(CourtOrder_PCWICreate_Log.CourtOrder_PCWICreate_Logger, false));
		CourtOrder_PCWICreate_Log.CourtOrder_PCWICreate_Logger.debug("wi_pc_inputXml: " + wi_pc_inputXml);
		String wi_pc__outputXml = WFNGExecute(wi_pc_inputXml, CommonConnection.getJTSIP(),
				CommonConnection.getJTSPort(), 1);
		CourtOrder_PCWICreate_Log.CourtOrder_PCWICreate_Logger.debug("wi_pc__outputXml: " + wi_pc__outputXml);

		XMLParser wi_pc_xmlParserData = new XMLParser(wi_pc__outputXml);

		int totalRetreived = Integer.parseInt(wi_pc_xmlParserData.getValueOf("TotalRetrieved"));

		if (wi_pc_xmlParserData.getValueOf("MainCode").equalsIgnoreCase("0") && totalRetreived > 0) {
			String wi_pc_val = wi_pc_xmlParserData.getNextValueOf("Record");
			wi_pc_val = wi_pc_val.replaceAll("[ ]+>", ">").replaceAll("<[ ]+", "<");

			NGXmlList objWorkList = wi_pc_xmlParserData.createList("Records", "Record");

			String Wi_name = "", IS_PC_WI_created = "", itemindex = "";
			for (; objWorkList.hasMoreElements(true); objWorkList.skip(true)) {
				Wi_name = objWorkList.getVal("Wi_name");
				IS_PC_WI_created = objWorkList.getVal("IS_PC_WI_created");
				itemindex = objWorkList.getVal("itemindex");

				CourtOrder_PCWICreate_Log.CourtOrder_PCWICreate_Logger.debug(" Wi_name : " + Wi_name);
				CourtOrder_PCWICreate_Log.CourtOrder_PCWICreate_Logger.debug("IS_PC_WI_created: " + IS_PC_WI_created);
				CourtOrder_PCWICreate_Log.CourtOrder_PCWICreate_Logger.debug("itemindex: " + itemindex);
				DocumentsTag = "";

				String downloadAndAttachStatus = downloadAllDocsFromCourtOrder(itemindex, Wi_name);
				CourtOrder_PCWICreate_Log.CourtOrder_PCWICreate_Logger
						.debug("DocumentsTag after downloadAllDocsFromCourtOrder " + DocumentsTag);
				CourtOrder_PCWICreate_Log.CourtOrder_PCWICreate_Logger
						.debug("downloadAndAttachStatus: " + downloadAndAttachStatus);
				if ("S".equalsIgnoreCase(downloadAndAttachStatus)) {

					// CREATE WI FOR EACH CIF ID FOR INDIVIDUAL

					String query_inidvidual = "select CIF from NG_COURTORDER_GR_INDIVIDUAL_CUSTOMER_DETAILS with(nolock) where wi_name='"
							+ Wi_name + "'";
					String inidvidual_inputXml = CommonMethods.apSelectWithColumnNames(query_inidvidual,
							CommonConnection.getCabinetName(), CommonConnection
									.getSessionID(CourtOrder_PCWICreate_Log.CourtOrder_PCWICreate_Logger, false));
					CourtOrder_PCWICreate_Log.CourtOrder_PCWICreate_Logger
							.debug("inidvidual_inputXml: " + inidvidual_inputXml);
					String individual_outputXml = WFNGExecute(inidvidual_inputXml, CommonConnection.getJTSIP(),
							CommonConnection.getJTSPort(), 1);
					CourtOrder_PCWICreate_Log.CourtOrder_PCWICreate_Logger
							.debug("individual_outputXml: " + individual_outputXml);

					XMLParser individual_xmlParserData = new XMLParser(individual_outputXml);

					int totalRetreived2 = Integer.parseInt(individual_xmlParserData.getValueOf("TotalRetrieved"));

					if (individual_xmlParserData.getValueOf("MainCode").equalsIgnoreCase("0") && totalRetreived2 > 0) {
						String individual_val = individual_xmlParserData.getNextValueOf("Record");
						individual_val = individual_val.replaceAll("[ ]+>", ">").replaceAll("<[ ]+", "<");

						NGXmlList objWorkList2 = individual_xmlParserData.createList("Records", "Record");

						String CIF = "";
						String wi_name = "";
						for (; objWorkList2.hasMoreElements(true); objWorkList2.skip(true)) {
							CIF = objWorkList2.getVal("CIF");
							CourtOrder_PCWICreate_Log.CourtOrder_PCWICreate_Logger.debug(" CIF : " + CIF);

							String value = "";

							value = CreatePc_Wi(CIF, cabinetName, sJtsIp, iJtsPort, sessionId, Wi_name);

							if (value.contains("Success")) {
								String returnValue[] = value.split("_");
								wi_name = wi_name + returnValue[1] + "','";

							}
						}
						CourtOrder_PCWICreate_Log.CourtOrder_PCWICreate_Logger.debug(" wi_name if success: " + wi_name);
						if (wi_name.length() > 2 && wi_name.contains("PC")) {
							wi_name = wi_name.substring(0, wi_name.length() - 1);
							updateTable("NG_COURTORDER_EXTTABLE", "IS_PC_WI_created", "'" + wi_name + "'",
									"WI_name='" + Wi_name + "'", jtsIP, jtsPort, cabinetName);
						}
					}
				} else {

				}
			}

		}
	}

	protected static String WFNGExecute(String ipXML, String jtsServerIP, String serverPort, int flag)
			throws IOException, Exception {
		CourtOrder_PCWICreate_Log.CourtOrder_PCWICreate_Logger.debug("In WF NG Execute : " + serverPort);
		try {
			if (serverPort.startsWith("33"))
				return WFCallBroker.execute(ipXML, jtsServerIP, Integer.parseInt(serverPort), 1);
			else
				return ngEjbClientCourtOrder.makeCall(jtsServerIP, serverPort, "WebSphere", ipXML);
		} catch (Exception e) {
			CourtOrder_PCWICreate_Log.CourtOrder_PCWICreate_Logger
					.debug("Exception Occured in WF NG Execute : " + e.getMessage());
			e.printStackTrace();
			return "Error";
		}
	}

	String CreatePc_Wi(String Cif, String cabinetName, String sJtsIp, String iJtsPort, String sessionId, String Wi_name)
			throws IOException, Exception {

		String attributeTag = "";
		attributeTag = attributeTag + "SERVICE_REQUEST_SELECTED" + (char) 21 + "Marking of Deceased Account"
				+ (char) 25;
		attributeTag = attributeTag + "SERVICE_REQUEST_TYPE" + (char) 21 + "Personal" + (char) 25;
		attributeTag = attributeTag + "CHANNEL" + (char) 21 + "CourtOrder" + (char) 25;
		attributeTag = attributeTag + "CIF_ID" + (char) 21 + Cif + (char) 25;

		// +<CHANNEL>"+Cif+"</CHANNEL>
		// "<SERVICE_REQUEST_TYPE>Personal</SERVICE_REQUEST_TYPE>"+
		// "<SERVICE_REQUEST_SELECTED>Deceased</SERVICE_REQUEST_SELECTED>"+
		// "<CHANNEL>CourtOrder</CHANNEL>";

		String CreateWi = "<?xml version=\"1.0\"?>" + "<WFUploadWorkItem_Input>" + "<Option>WFUploadWorkItem</Option>"
				+ "<EngineName>" + cabinetName + "</EngineName>" + "<SessionId>" + sessionID + "</SessionId>"
				+ "<ValidationRequired><ValidationRequired>" + "<ProcessDefId>" + ProcessDefIdPC + "</ProcessDefId>"
				+ "<DataDefName></DataDefName>" + "<Fields></Fields>" + "<InitiateAlso>N</InitiateAlso>" + "<Documents>"
				+ DocumentsTag + "</Documents>" + "<Attributes>" + attributeTag + "</Attributes>"
				+ "</WFUploadWorkItem_Input>";

		String CreateWi_outputXml = WFNGExecute(CreateWi, CommonConnection.getJTSIP(), CommonConnection.getJTSPort(),
				1);

		CourtOrder_PCWICreate_Log.CourtOrder_PCWICreate_Logger.debug("CreateWi_outputXml: " + CreateWi_outputXml);

		XMLParser wi_pc_xmlParserData = new XMLParser(CreateWi_outputXml);

		if (wi_pc_xmlParserData.getValueOf("MainCode").equalsIgnoreCase("0")) {

			String Pc_ProcessInstanceId = wi_pc_xmlParserData.getValueOf("ProcessInstanceId");

			return "Success" + "_" + Pc_ProcessInstanceId;

		} else {
			CourtOrder_PCWICreate_Log.CourtOrder_PCWICreate_Logger.debug("Error In Wi Creation in PC");
			return "Error In Wi Creation in PC";
		}

		// return "";
	}

	private void updateTable(String tablename, String columnname, String sMessage, String sWhere, String jtsIP,
			String jtsPort, String cabinetName) {
		int sessionCheckInt = 0;
		int loopCount = 50;
		int mainCode = 0;

		CourtOrder_PCWICreate_Log.CourtOrder_PCWICreate_Logger.debug("Inside update " + tablename + " table: ");

		while (sessionCheckInt < loopCount) {
			try {
				XMLParser objXMLParser = new XMLParser();
				String inputXmlcheckAPUpdate = CommonMethods.getAPUpdateIpXML(tablename, columnname, sMessage, sWhere,
						cabinetName, sessionID);
				CourtOrder_PCWICreate_Log.CourtOrder_PCWICreate_Logger
						.debug(("inputXmlcheckAPUpdate : " + inputXmlcheckAPUpdate));
				String outXmlCheckAPUpdate = null;
				outXmlCheckAPUpdate = WFNGExecute(inputXmlcheckAPUpdate, jtsIP, jtsPort, 1);
				CourtOrder_PCWICreate_Log.CourtOrder_PCWICreate_Logger
						.debug(("outXmlCheckAPUpdate : " + outXmlCheckAPUpdate));
				objXMLParser.setInputXML(outXmlCheckAPUpdate);
				String mainCodeforCheckUpdate = null;
				mainCodeforCheckUpdate = objXMLParser.getValueOf("MainCode");
				if (!mainCodeforCheckUpdate.equalsIgnoreCase("0")) {
					CourtOrder_PCWICreate_Log.CourtOrder_PCWICreate_Logger
							.debug(("Exception in ExecuteQuery_APUpdate updating " + tablename + " table"));
					System.out.println("Exception in ExecuteQuery_APUpdate updating " + tablename + " table");
				} else {
					CourtOrder_PCWICreate_Log.CourtOrder_PCWICreate_Logger
							.debug(("Succesfully updated " + tablename + " table"));

				}
				mainCode = Integer.parseInt(mainCodeforCheckUpdate);
				if (mainCode == 11) {
					sessionID = CommonConnection.getSessionID(CourtOrder_PCWICreate_Log.CourtOrder_PCWICreate_Logger,
							false);
				} else {
					sessionCheckInt++;
					break;
				}

				if (outXmlCheckAPUpdate.equalsIgnoreCase("") || outXmlCheckAPUpdate == ""
						|| outXmlCheckAPUpdate == null)
					break;

			} catch (Exception e) {
				CourtOrder_SystemAutoRemittance obj1 = new CourtOrder_SystemAutoRemittance();
				String exception = obj1.customException(e);
				CourtOrder_PCWICreate_Log.CourtOrder_PCWICreate_Logger
						.debug(("Inside create validateSessionID exception" + exception));
			}
		}
	}

	public String downloadAllDocsFromCourtOrder(String ItemIndex, String Wi_name) {
		String downloadStatus = "";
		String docListXML = GetDocumentsList(ItemIndex,
				CommonConnection.getSessionID(CourtOrder_PCWICreate_Log.CourtOrder_PCWICreate_Logger, false),
				CommonConnection.getCabinetName(), CommonConnection.getJTSIP(), CommonConnection.getJTSPort());
		CourtOrder_PCWICreate_Log.CourtOrder_PCWICreate_Logger.info("docListXML is: " + docListXML);
		if (!docListXML.trim().equalsIgnoreCase("F")) {
			XMLParser sXMLParser = new XMLParser(docListXML);
			int noOfDocs = sXMLParser.getNoOfFields("Document");

			CourtOrder_PCWICreate_Log.CourtOrder_PCWICreate_Logger
					.info("No of docs for " + Wi_name + " is " + noOfDocs);

			if (noOfDocs < 1)
				downloadStatus = "S";

			String isDocPresent = "";
			for (int i = 0; i < noOfDocs; i++) {
				XMLParser subXMLParser = null;
				String subXML1 = sXMLParser.getNextValueOf("Document");
				subXMLParser = new XMLParser(subXML1);
				CourtOrder_PCWICreate_Log.CourtOrder_PCWICreate_Logger
						.info("subXMLParser  " + Wi_name + " is " + subXMLParser);
				String docName = subXMLParser.getValueOf("DocumentName");
				String docExt = subXMLParser.getValueOf("CreatedByAppName");
				CourtOrder_PCWICreate_Log.CourtOrder_PCWICreate_Logger.info("docName " + Wi_name + " is " + docName);

				// if (docName.equalsIgnoreCase("Court Instruction") ||
				// docName.equalsIgnoreCase("Dubai Court Email")) {

				if ("Court Instruction".equalsIgnoreCase(docName) || "Dubai Court Email".equalsIgnoreCase(docName)) {
					isDocPresent = "DocumentIsPresent";
					downloadStatus = DownloadDocument(subXMLParser, Wi_name, docName, docExt,
							CommonConnection.getCabinetName(), CommonConnection.getJTSIP(),
							CommonConnection.getsSMSPort(), "DownloadLoc", CommonConnection.getsVolumeID(),
							CommonConnection.getsSiteID());

				}

			}
			CourtOrder_PCWICreate_Log.CourtOrder_PCWICreate_Logger.info("isDocPresent " + isDocPresent);
			if ("".equalsIgnoreCase(isDocPresent) || isDocPresent.isEmpty()) {
				downloadStatus = "S";
			}
			// deleting processed workitem folder
			StringBuffer strFilePath = new StringBuffer();
			strFilePath.append(System.getProperty("user.dir"));
			strFilePath.append(File.separator);
			strFilePath.append("DownloadLoc");
			CourtOrder_PCWICreate_Log.CourtOrder_PCWICreate_Logger
					.info("strFilePath before deleteFolder is  " + strFilePath);
			File ReportFolder = new File(strFilePath.toString());
			if (ReportFolder.exists()) {
				deleteFolder(strFilePath.toString());
			}
		}

		return downloadStatus;
	}

	public String GetDocumentsList(String itemindex, String sessionId, String cabinetName, String jtsIP,
			String jtsPort) {
		CourtOrder_PCWICreate_Log.CourtOrder_PCWICreate_Logger.info("Inside GetDocumentsList Method ...");
		XMLParser docXmlParser = new XMLParser();
		String mainCode = "";
		String response = "F";
		String outputXML = "";
		try {

			String sInputXML = CommonMethods.getDocumentList(itemindex, sessionId, cabinetName);
			CourtOrder_PCWICreate_Log.CourtOrder_PCWICreate_Logger
					.debug(" Inputxml to get document names for " + itemindex + " " + sInputXML);

			outputXML = CommonMethods.WFNGExecute(sInputXML, jtsIP, jtsPort, 1);
			CourtOrder_PCWICreate_Log.CourtOrder_PCWICreate_Logger
					.debug(" outputxml to get document names for " + itemindex + " " + outputXML);
			docXmlParser.setInputXML(outputXML);
			mainCode = docXmlParser.getValueOf("Status");
			CourtOrder_PCWICreate_Log.CourtOrder_PCWICreate_Logger.info("mainCode is: " + mainCode);
			if (mainCode.equals("0")) {
				response = outputXML;
			}

		} catch (Exception e) {
			CourtOrder_PCWICreate_Log.CourtOrder_PCWICreate_Logger
					.error("Exception occured in GetDocumentsList method : " + e);

			response = "F";
			final Writer result = new StringWriter();
			final PrintWriter printWriter = new PrintWriter(result);
			e.printStackTrace(printWriter);
		}
		return response;
	}

	public String DownloadDocument(XMLParser xmlParser, String winame, String docName, String docExt,
			String cabinetName, String jtsIp, String smsPort, String docDownloadPath, String volumeId, String siteId) {
		CourtOrder_PCWICreate_Log.CourtOrder_PCWICreate_Logger.debug("Inside DownloadDocument Method...");

		String status = "F";
		String msg = "Error";
		StringBuffer strFilePath = new StringBuffer();
		try {

			String imageIndex = xmlParser.getValueOf("ISIndex").substring(0,
					xmlParser.getValueOf("ISIndex").indexOf("#"));

			strFilePath.append(System.getProperty("user.dir"));
			strFilePath.append(File.separator);
			strFilePath.append(docDownloadPath);
			strFilePath.append(File.separator);
			strFilePath.append(winame);

			File af = null;
			boolean bool = false;
			try {
				// returns pathnames for files and directory
				af = new File(strFilePath.toString());
				// create directories
				if (af.exists()) {
					// do nothing
				} else {
					bool = af.mkdirs();
				}
				// print
				CourtOrder_PCWICreate_Log.CourtOrder_PCWICreate_Logger.info("Directory created? " + bool);
			} catch (Exception e) {
				// if any error occurs
				e.printStackTrace();
				CourtOrder_PCWICreate_Log.CourtOrder_PCWICreate_Logger
						.info("For WNAME " + winame + " Exception in creating file path: " + e.getMessage());
			}

			strFilePath.append(File.separatorChar);
			strFilePath.append(docName + "_" + imageIndex);
			strFilePath.append(".");
			strFilePath.append(docExt);

			String DocNameInBAIS = getCourtOrderToPCDocMapping(docName);
			if ("".equalsIgnoreCase(DocNameInBAIS.trim())) {
				CourtOrder_PCWICreate_Log.CourtOrder_PCWICreate_Logger
						.info("No need to attach this iRBL document in BAIS - iBRL_DocName:" + docName);
				status = "S";
				return status;
			}

			CImageServer cImageServer = null;
			try {
				cImageServer = new CImageServer(null, jtsIp, Short.parseShort(smsPort));
			} catch (JPISException e) {
				CourtOrder_PCWICreate_Log.CourtOrder_PCWICreate_Logger.info("cImageServer excp1:" + e.getMessage());
				StringWriter sw = new StringWriter();
				e.printStackTrace(new PrintWriter(sw));
				String exception = sw.toString();
				CourtOrder_PCWICreate_Log.CourtOrder_PCWICreate_Logger.info("cImageServer excp2:" + exception);
				status = "F";
			}
			CourtOrder_PCWICreate_Log.CourtOrder_PCWICreate_Logger
					.debug("values passed -> " + jtsIp + " " + smsPort + " " + cabinetName + " " + volumeId + " "
							+ siteId + " " + imageIndex + " " + strFilePath.toString());
			CourtOrder_PCWICreate_Log.CourtOrder_PCWICreate_Logger
					.debug("signature document name and imageindex for " + winame + " " + docName + "," + imageIndex);

			CourtOrder_PCWICreate_Log.CourtOrder_PCWICreate_Logger.debug("Fetching OD Download Code ::::::");
			int odDownloadCode = cImageServer.JPISGetDocInFile_MT(null, jtsIp, Short.parseShort(smsPort), cabinetName,
					Short.parseShort(siteId), Short.parseShort(volumeId), Integer.parseInt(imageIndex), "",
					strFilePath.toString(), new JPDBString());

			CourtOrder_PCWICreate_Log.CourtOrder_PCWICreate_Logger.debug("OD Download Code :" + odDownloadCode);
			CourtOrder_PCWICreate_Log.CourtOrder_PCWICreate_Logger
					.debug("strFilePath.toString() :" + strFilePath.toString());

			if (odDownloadCode == 1) {

				try {
					JPISIsIndex ISINDEX = new JPISIsIndex();
					JPDBRecoverDocData JPISDEC = new JPDBRecoverDocData();

					String docPath = strFilePath.toString();
					CourtOrder_PCWICreate_Log.CourtOrder_PCWICreate_Logger
							.info("workItemName: " + winame + " The Document address is: " + docPath);
					String sDocsize = "";
					File fppp = new File(docPath);
					long lgvDocSize;
					File obvFile = fppp;
					lgvDocSize = obvFile.length();
					sDocsize = Long.toString(lgvDocSize);

					try {
						CourtOrder_PCWICreate_Log.CourtOrder_PCWICreate_Logger.info("workItemName: " + winame
								+ " before CPISDocumentTxn AddDocument MT: OF cabinet name:"
								+ CommonConnection.getOFCabinetName() + ", OF JTS IP:" + CommonConnection.getOFJTSIP()
								+ ", OF JTS Port:" + CommonConnection.getOFJTSPort() + ", OF Volumn ID:"
								+ CommonConnection.getOFVOLUMNID());
						String getOFJTSPort = CommonConnection.getOFJTSPort();
						CourtOrder_PCWICreate_Log.CourtOrder_PCWICreate_Logger.info("getOFJTSPort: " + getOFJTSPort);
						if (!(getOFJTSPort == null) && CommonConnection.getOFJTSPort().startsWith("33")) {
							CPISDocumentTxn.AddDocument_MT(null, CommonConnection.getOFJTSIP(),
									Short.parseShort(CommonConnection.getOFJTSPort()),
									CommonConnection.getOFCabinetName(),
									Short.parseShort(CommonConnection.getOFVOLUMNID()), docPath, JPISDEC, "", ISINDEX);
						} else {
							CPISDocumentTxn.AddDocument_MT(null, CommonConnection.getOFJTSIP(),
									Short.parseShort(CommonConnection.getOFJTSPort()),
									CommonConnection.getOFCabinetName(),
									Short.parseShort(CommonConnection.getOFVOLUMNID()), docPath, JPISDEC, null, "JNDI",
									ISINDEX);
						}

						CourtOrder_PCWICreate_Log.CourtOrder_PCWICreate_Logger
								.info("workItemName: " + winame + " after CPISDocumentTxn AddDocument MT: ");
						status = "S";
						String sISIndex = ISINDEX.m_nDocIndex + "#" + ISINDEX.m_sVolumeId;

						DocumentsTag = DocumentsTag + DocNameInBAIS + fieldSep + sISIndex + fieldSep
								+ ISINDEX.m_nPageNumber + fieldSep + sDocsize + fieldSep + docExt + recordSep;
						fppp.delete();
						CourtOrder_PCWICreate_Log.CourtOrder_PCWICreate_Logger
								.info("workItemName: " + winame + " sISIndex: " + sISIndex);
					} catch (NumberFormatException e) {
						CourtOrder_PCWICreate_Log.CourtOrder_PCWICreate_Logger
								.info("workItemName1:" + e.getMessage() + CommonMethods.printException(e));
						e.printStackTrace();
						// catchflag=true;
					} catch (JPISException e) {
						CourtOrder_PCWICreate_Log.CourtOrder_PCWICreate_Logger.info("workItemName2:" + e.getMessage());
						StringWriter sw = new StringWriter();
						e.printStackTrace(new PrintWriter(sw));
						String exception = sw.toString();
						CourtOrder_PCWICreate_Log.CourtOrder_PCWICreate_Logger.info("workItemName22:" + exception);
						// catchflag=true;
					} catch (Exception e) {
						CourtOrder_PCWICreate_Log.CourtOrder_PCWICreate_Logger
								.info("workItemName3:" + e.getMessage() + CommonMethods.printException(e));
						e.printStackTrace();
						// catchflag=true;
					}

				} catch (Exception e) {
					CourtOrder_PCWICreate_Log.CourtOrder_PCWICreate_Logger
							.debug("Exception in OF Upload " + winame + " " + docName + "," + imageIndex);
					CourtOrder_PCWICreate_Log.CourtOrder_PCWICreate_Logger
							.error("Exception in OF Upload2 : " + e.getMessage() + CommonMethods.printException(e));
					status = "F";
				}

			} else {
				CourtOrder_PCWICreate_Log.CourtOrder_PCWICreate_Logger.debug("Error in downloading document for "
						+ winame + " docname " + docName + ", imageindex " + imageIndex);

				msg = "Error occured while downloading the document :" + docName;
				status = "F";
			}
		} catch (Exception e) {
			CourtOrder_PCWICreate_Log.CourtOrder_PCWICreate_Logger
					.error("Exception occured in DownloadDocument method : " + e.getMessage()
							+ CommonMethods.printException(e));

			status = "F";
		}

		return status;

	}

	private void deleteFolder(String fileLocation) {
		File folder = new File(fileLocation);
		File[] listofFiles = folder.listFiles();
		if (listofFiles.length == 0) {
			CourtOrder_PCWICreate_Log.CourtOrder_PCWICreate_Logger
					.info("Folder Name :: " + folder.getAbsolutePath() + " is deleted.");
			folder.delete();
			// isFinished = false;
		} else {
			for (int j = 0; j < listofFiles.length; j++) {
				File file = listofFiles[j];
				if (file.isDirectory()) {
					deleteFolder(file.getAbsolutePath());
				}
			}
		}
	}

	public String getCourtOrderToPCDocMapping(String CourtOrderDocName) {
		String PCDocName = "";

		if (CourtOrderDocName.contains("Dubai Court Email"))
			PCDocName = "Deceased Customer Email";
		else if (CourtOrderDocName.contains("Court Instruction"))
			PCDocName = "Court Instruction";

		return PCDocName;
	}

	// By sudhanshu rathore
	private void InsertDataInDBFromExcel(String sessionID) throws IOException {
		try {
			String[] array = ExcelColumn.split(",");
			String ColumnKey = "";
			String ColumnValue = "";
			String[] temp = null;
			Map<String, String> CoumnMapping = new HashMap<>();
			for (int i = 0; i < array.length; i++) {
				temp = array[i].split("@");
				if (temp.length == 2) {
					ColumnKey = temp[0];
					ColumnValue = temp[1];
					CoumnMapping.put(ColumnKey, ColumnValue);
				}
			}

			CourtOrder_PCWICreate_Log.CourtOrder_PCWICreate_Logger
					.info("CIR_EXCEL_FOLDER_NAME:" + CIR_EXCEL_FOLDER_NAME);
			String[] folderNameArray = CIR_EXCEL_FOLDER_NAME.split(",");
			CourtOrder_PCWICreate_Log.CourtOrder_PCWICreate_Logger
					.info("folderNameArray.length:" + folderNameArray.length);
			String RequestType = "", folderPath = "", destinationFolderpath = "", errorFolderPath = "";
			for (int y = 0; y < folderNameArray.length; y++) {
				RequestType = folderNameArray[y];
				folderPath = CIR_Excel_INPUT.replaceAll("#FOLDERNAME#", RequestType);
				destinationFolderpath = CIR_Excel_OUTPUT.replaceAll("#FOLDERNAME#", RequestType);// change
																									// inside
																									// the
																									// loop
				errorFolderPath = CIR_Excel_ERROR.replaceAll("#FOLDERNAME#", RequestType);
				CourtOrder_PCWICreate_Log.CourtOrder_PCWICreate_Logger.info("folderPath is " + folderPath);
				CourtOrder_PCWICreate_Log.CourtOrder_PCWICreate_Logger
						.debug("destinationFolderpath is " + destinationFolderpath);
				CourtOrder_PCWICreate_Log.CourtOrder_PCWICreate_Logger.debug("errorFolderPath is " + errorFolderPath);
				File folder = new File(folderPath);
				File[] listOfFiles = folder.listFiles(File::isFile);
				for (File file : listOfFiles) {
					boolean havingFaultyRecords = false;
					boolean isWICreationSkipped = false;
					List<Integer> skippedRows = new ArrayList<>();
					String filepath = file.getAbsolutePath();
					String filename = file.getName();
					String Request_Reference_No = "";
					int indexOfDot = filename.lastIndexOf(".");
					int indexOfDash = filename.lastIndexOf("-");
					int lenofFileName = filename.length();
					if (lenofFileName > indexOfDot) {
						Request_Reference_No = filename.substring(indexOfDash + 1, indexOfDot);
					}
					CourtOrder_PCWICreate_Log.CourtOrder_PCWICreate_Logger.debug("filepath " + filepath);
					CourtOrder_PCWICreate_Log.CourtOrder_PCWICreate_Logger.debug("filename " + filename);

					DataFormatter formatter = new DataFormatter();

					if (FilenameUtils.getExtension(filepath).trim().equalsIgnoreCase("xlsx")) {
						FileInputStream fis = null;
						fis = new FileInputStream(new File(filepath));
						XSSFWorkbook workBook = new XSSFWorkbook(fis);
						String sheetName = workBook.getSheetName(0);
						XSSFSheet sheet = workBook.getSheet(sheetName);
						int rowCount = sheet.getPhysicalNumberOfRows();
						int cellCount = sheet.getRow(0).getPhysicalNumberOfCells();
						// creating cell style
						XSSFCellStyle redStyle = workBook.createCellStyle();
						redStyle.setFillForegroundColor(IndexedColors.RED.getIndex());
						redStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);

						XSSFCellStyle yellowStyle = workBook.createCellStyle();
						yellowStyle.setFillForegroundColor(IndexedColors.YELLOW.getIndex());
						yellowStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);
						//
						String[] cellValues = new String[cellCount];
						String columnNames = "";
						String columnValues = "";
						CourtOrder_PCWICreate_Log.CourtOrder_PCWICreate_Logger.debug("CoumnMapping: " + CoumnMapping);
						String Emirates_ID = "", Passport = "", Trade_License_No = "";
						String ExcelColumnName = "";
						CourtOrder_PCWICreate_Log.CourtOrder_PCWICreate_Logger
								.debug("Total rowCount in Excel File: " + rowCount);
						RowCountLoop: for (int i = 1; i < rowCount; i++) {
							columnValues = "";
							columnNames = "";
							if (isRowEmpty(sheet.getRow(i)) == false) {
								String custFullName = "", dob = "";
								for (int k = 0; k < cellCount; k++) {
									Object value = formatter.formatCellValue(sheet.getRow(0).getCell(k));
									ExcelColumnName = (String) value;
									if (CoumnMapping.containsKey(ExcelColumnName)) {

										columnNames = columnNames + "," + CoumnMapping.get(ExcelColumnName);
										Cell value2 = sheet.getRow(i).getCell(k);
										String value3 = "";
										SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy");
										sdf.setLenient(false);
										boolean isDateValid = true;
										if (value2 != null) {
											//
											try {
												if ("Date_of_Birth".equalsIgnoreCase(CoumnMapping.get(ExcelColumnName))
														|| "Date_of_Establishment"
																.equalsIgnoreCase(CoumnMapping.get(ExcelColumnName))
														|| "Due_Date"
																.equalsIgnoreCase(CoumnMapping.get(ExcelColumnName))) {

													if ((value2.getCellType() == Cell.CELL_TYPE_STRING)) {
														value3 = value2.getStringCellValue().trim();
														if (!value3.isEmpty() && !"".equalsIgnoreCase(value3)) {
															sdf.parse(value3);
														}
													} else if (value2.getCellType() == Cell.CELL_TYPE_NUMERIC
															&& DateUtil.isCellDateFormatted(value2)) {
														Date d = value2.getDateCellValue();
														value3 = sdf.format(d);
														if (!value3.isEmpty() && !"".equalsIgnoreCase(value3)) {
															sdf.parse(value3);
														}
													}
												} else {
													value3 = formatter.formatCellValue(value2);
												}
												// checking other symbols
												if (!value3.isEmpty() && !"".equalsIgnoreCase(value3)) {
													value3 = aggressiveTrim(value3);
												}

											} catch (ParseException e) {
												CourtOrder_SystemAutoRemittance obj1 = new CourtOrder_SystemAutoRemittance();
												String exception = obj1.customException(e);
												CourtOrder_PCWICreate_Log.CourtOrder_PCWICreate_Logger
														.debug("exception date format is incorrect for "
																+ CoumnMapping.get(ExcelColumnName) + " " + e);
												isDateValid = false;
											}
											//
											if (!isDateValid) {
												havingFaultyRecords = true;
												// setting color
												Row rowcolored = sheet.getRow(i);
												for (int x = 0; x <= 11; x++) {
													Cell cell = rowcolored.getCell(x, Row.CREATE_NULL_AS_BLANK);
													cell.setCellStyle(redStyle);
												}
												continue RowCountLoop;
												//
											}
										}
										cellValues[k] = value3;
										CourtOrder_PCWICreate_Log.CourtOrder_PCWICreate_Logger
												.debug("ExcelColumnName: " + CoumnMapping.get(ExcelColumnName));
										CourtOrder_PCWICreate_Log.CourtOrder_PCWICreate_Logger
												.debug("ExcelColumnValue: " + cellValues[k]);

										if ("Emirates_ID".equalsIgnoreCase(CoumnMapping.get(ExcelColumnName))) {
											Emirates_ID = cellValues[k];
										} else if ("Passport".equalsIgnoreCase(CoumnMapping.get(ExcelColumnName))) {
											Passport = cellValues[k];
										} else if ("Trade_License_No"
												.equalsIgnoreCase(CoumnMapping.get(ExcelColumnName))) {
											Trade_License_No = cellValues[k];
										} else {
											//
											if ("Date_of_Birth".equalsIgnoreCase(CoumnMapping.get(ExcelColumnName))) {
												dob = cellValues[k];
											}
											if ("Name".equalsIgnoreCase(CoumnMapping.get(ExcelColumnName))) {
												custFullName = cellValues[k];
											}
										}

										columnValues = columnValues + ",'" + cellValues[k] + "'";

									}
								}
								String duplicate_idetifier_columnName = "";
								String duplicate_idetifier_columnValue = "";
								if (Emirates_ID != null && !Emirates_ID.isEmpty()
										&& !"null".equalsIgnoreCase(Emirates_ID)) {
									duplicate_idetifier_columnName = "Emirates_ID";
									duplicate_idetifier_columnValue = Emirates_ID;
								} else if (Passport != null && !Passport.isEmpty()
										&& !"null".equalsIgnoreCase(Passport)) {
									duplicate_idetifier_columnName = "Passport";
									duplicate_idetifier_columnValue = Passport;
								} else if (Trade_License_No != null && !Trade_License_No.isEmpty()
										&& !"null".equalsIgnoreCase(Trade_License_No)) {
									duplicate_idetifier_columnName = "Trade_License_No";
									duplicate_idetifier_columnValue = Trade_License_No;
								} else if (dob != null && !dob.isEmpty() && !"null".equalsIgnoreCase(dob)
										&& custFullName != null && !custFullName.isEmpty()
										&& !"null".equalsIgnoreCase(custFullName)) {

									duplicate_idetifier_columnName = "Name~Date_of_Birth";
									duplicate_idetifier_columnValue = custFullName + "~" + dob;
								}

								if (duplicate_idetifier_columnName != null && !duplicate_idetifier_columnName.isEmpty()
										&& duplicate_idetifier_columnValue != null
										&& !duplicate_idetifier_columnValue.isEmpty()
										&& !"null".equalsIgnoreCase(duplicate_idetifier_columnValue)) {

									CourtOrder_PCWICreate_Log.CourtOrder_PCWICreate_Logger
											.debug("duplicate_idetifier_columnName: " + duplicate_idetifier_columnName
													+ " and its value is " + duplicate_idetifier_columnValue);
									String dob1 = "", fullname1 = "", query_wi = "";
									if (duplicate_idetifier_columnName.contains("~")) {
										String[] dataArray = duplicate_idetifier_columnValue.split("~");
										fullname1 = (dataArray.length > 0 && dataArray[0] != null) ? dataArray[0] : "";
										dob1 = (dataArray.length > 1 && dataArray[1] != null) ? dataArray[1] : "";

										query_wi = "select RequestType from NG_courtOrder_CIR_ExcelData with(nolock)"
												+ " where record_file_name = '" + filename + "' and " + "Name = '"
												+ fullname1 + "' and Date_of_Birth = '" + dob1 + "'";
									} else {
										query_wi = "select RequestType from NG_courtOrder_CIR_ExcelData with(nolock)"
												+ " where record_file_name = '" + filename + "' and "
												+ duplicate_idetifier_columnName + "='"
												+ duplicate_idetifier_columnValue + "' ";
									}
									CourtOrder_PCWICreate_Log.CourtOrder_PCWICreate_Logger
											.debug("query_wi: " + query_wi);
									String wi_inputXml = CommonMethods.apSelectWithColumnNames(query_wi,
											CommonConnection.getCabinetName(), CommonConnection.getSessionID(
													CourtOrder_PCWICreate_Log.CourtOrder_PCWICreate_Logger, false));
									CourtOrder_PCWICreate_Log.CourtOrder_PCWICreate_Logger
											.debug("wi_pc_inputXml: " + wi_inputXml);
									String wi_outputXml = WFNGExecute(wi_inputXml, CommonConnection.getJTSIP(),
											CommonConnection.getJTSPort(), 1);
									CourtOrder_PCWICreate_Log.CourtOrder_PCWICreate_Logger
											.debug("wi_outputXml: " + wi_outputXml);
									XMLParser wi_xmlParserData = new XMLParser(wi_outputXml);

									if (wi_xmlParserData.getValueOf("MainCode").equalsIgnoreCase("0")) {
										int totalRetreived = Integer
												.parseInt(wi_xmlParserData.getValueOf("TotalRetrieved"));
										if (totalRetreived == 0) {

											Date currentDate = new Date();
											SimpleDateFormat formatternew = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
											String currentDateTime = formatternew.format(currentDate);

											columnNames = "record_inserted_date_time,record_file_name,RequestType,Request_Reference_No"
													+ columnNames;
											columnValues = "'" + currentDateTime + "','" + filename + "','"
													+ RequestType + "','" + Request_Reference_No + "'" + columnValues;
											CourtOrder_PCWICreate_Log.CourtOrder_PCWICreate_Logger
													.debug("columnNames: " + columnNames);
											CourtOrder_PCWICreate_Log.CourtOrder_PCWICreate_Logger
													.debug("columnValues: " + columnValues);
											String apInsertInputXML = CommonMethods.apInsert(cabinetName, sessionID,
													columnNames, columnValues, "NG_courtOrder_CIR_ExcelData");
											CourtOrder_PCWICreate_Log.CourtOrder_PCWICreate_Logger
													.debug("apInsertInputXML: " + apInsertInputXML);
											String apInsertOutputXML = CommonMethods.WFNGExecute(apInsertInputXML,
													jtsIP, jtsPort, 1);
											CourtOrder_PCWICreate_Log.CourtOrder_PCWICreate_Logger
													.debug("apInsertOutputXML: " + apInsertOutputXML);
											XMLParser xmlParserAPInsert = new XMLParser(apInsertOutputXML);
											String apInsertMaincode = xmlParserAPInsert.getValueOf("MainCode");
											if (apInsertMaincode.equalsIgnoreCase("0")) {
												CourtOrder_PCWICreate_Log.CourtOrder_PCWICreate_Logger
														.debug("1 row inserted successfully: ");
											} else {
												String errorDesc = xmlParserAPInsert.getValueOf("Output");
												CourtOrder_PCWICreate_Log.CourtOrder_PCWICreate_Logger.debug(
														"Error in insertion query with description : " + errorDesc);
												havingFaultyRecords = true;
												// setting color
												Row rowcolored = sheet.getRow(i);
												for (int x = 0; x <= 11; x++) {
													Cell cell = rowcolored.getCell(x, Row.CREATE_NULL_AS_BLANK);
													cell.setCellStyle(redStyle);
												}
												continue RowCountLoop;
												//
											}
										} else {
											// duplicate values present
											CourtOrder_PCWICreate_Log.CourtOrder_PCWICreate_Logger
													.debug("Duplicate record found already for "
															+ duplicate_idetifier_columnName + " "
															+ duplicate_idetifier_columnValue
															+ " in the record for the same file name");
											isWICreationSkipped = true;
											skippedRows.add(i);
										}
									} else {
										String errorDesc = wi_xmlParserData.getValueOf("Output");
										CourtOrder_PCWICreate_Log.CourtOrder_PCWICreate_Logger
												.debug("Error in select query with description : " + errorDesc);
										havingFaultyRecords = true;
										// setting color
										Row rowcolored = sheet.getRow(i);
										for (int x = 0; x <= 11; x++) {
											Cell cell = rowcolored.getCell(x, Row.CREATE_NULL_AS_BLANK);
											cell.setCellStyle(redStyle);
										}
										continue RowCountLoop;
										//
									}

								} else {
									// mandatory columns are null
									CourtOrder_PCWICreate_Log.CourtOrder_PCWICreate_Logger
											.debug("Inside else and mandatory columns values are null");
									isWICreationSkipped = true;
									skippedRows.add(i);
								}
							}
						}
						// if no faulty records and any record is skipped
						if (!havingFaultyRecords && isWICreationSkipped) {
							for (int skipRow : skippedRows) {
								Row rowcolored = sheet.getRow(skipRow);
								for (int x = 0; x <= 11; x++) {
									Cell cell = rowcolored.getCell(x, Row.CREATE_NULL_AS_BLANK);
									cell.setCellStyle(yellowStyle);
								}
							}

						}
						//
						// saving file
						fis.close();
						FileOutputStream outputStream = new FileOutputStream(new File(filepath));
						workBook.write(outputStream);
						outputStream.close();

						// moving the file
						TimeStamp = get_timestamp();
						if (!havingFaultyRecords) {
							String initialTime = TimeStamp;

							if (isWICreationSkipped) {
								// add input file to OD for future use
								String addStatus = addInputExcelToOD(filepath, filename, sessionID);
								CourtOrder_PCWICreate_Log.CourtOrder_PCWICreate_Logger
										.debug("addStatus of input excel is: " + addStatus);
								if ("Success".equalsIgnoreCase(addStatus)) {
									CourtOrder_PCWICreate_Log.CourtOrder_PCWICreate_Logger
											.debug("********Input Excel attached to OD successfully*******");
								}
							}
							//
							String destinationFolderpathFile = destinationFolderpath + File.separator + initialTime
									+ " " + filename;
							CourtOrder_PCWICreate_Log.CourtOrder_PCWICreate_Logger.debug(
									"destinationFolderpath after setting timestamp: " + destinationFolderpathFile);
							try {
								Path returnFileMove = Files.move(Paths.get(filepath),
										Paths.get(destinationFolderpathFile));
								if (returnFileMove != null) {
									CourtOrder_PCWICreate_Log.CourtOrder_PCWICreate_Logger
											.debug("File renamed and moved successfully");
								} else {
									CourtOrder_PCWICreate_Log.CourtOrder_PCWICreate_Logger
											.debug("Failed to move the file");
									String errorFolderPathFile = errorFolderPath + File.separator + TimeStamp + " "
											+ filename;
									Path returnFileMoveError = Files.move(Paths.get(filepath),
											Paths.get(errorFolderPathFile));
								}
							} catch (Exception e) {
								CourtOrder_PCWICreate_Log.CourtOrder_PCWICreate_Logger
										.debug("exception in file Movement" + e.getMessage());
							}
							//
						} else {
							try {
								String initialTime = TimeStamp;
								String errorFolderPathFile = errorFolderPath + File.separator + initialTime + " "
										+ filename;
								Path returnFileMoveError = Files.move(Paths.get(filepath),
										Paths.get(errorFolderPathFile));
								// moving attachment of that excel to error
								// folder also
								moveAttachToError(RequestType, Request_Reference_No);
								CourtOrder_PCWICreate_Log.CourtOrder_PCWICreate_Logger
										.debug("Having faluty rec---->moving file to error folder");
								if (returnFileMoveError != null) {
									// deleting all records from db for that ref
									// no.
									String apDeleteInputXML = CommonMethods.apDeleteInput(cabinetName, sessionID,
											"NG_courtOrder_CIR_ExcelData",
											"Request_Reference_No = '" + Request_Reference_No + "' and "
													+ "record_file_name = '" + filename + "'");
									CourtOrder_PCWICreate_Log.CourtOrder_PCWICreate_Logger
											.debug("apDeleteInputXML: " + apDeleteInputXML);
									String apDeleteOutputXML = CommonMethods.WFNGExecute(apDeleteInputXML, jtsIP,
											jtsPort, 1);
									CourtOrder_PCWICreate_Log.CourtOrder_PCWICreate_Logger
											.debug("apDeleteOutputXML: " + apDeleteOutputXML);
									XMLParser xmlParserAPDelete = new XMLParser(apDeleteOutputXML);
									String apDeleteMaincode = xmlParserAPDelete.getValueOf("MainCode");
									if (apDeleteMaincode.equalsIgnoreCase("0")) {
										String totalrecords = xmlParserAPDelete.getValueOf("Output");
										CourtOrder_PCWICreate_Log.CourtOrder_PCWICreate_Logger
												.debug("Total records deleted successfully " + "for file : " + filename
														+ " is " + totalrecords + "");
										// triggering mail
										String resultErrorMail = errorWaitMailTrigger(errorFolderPathFile, filename,
												"Error", sessionID);
										if ("Success".equalsIgnoreCase(resultErrorMail)) {
											CourtOrder_PCWICreate_Log.CourtOrder_PCWICreate_Logger
													.debug("*****Mail Trigger Successfully******");
										}
									} else {
										String errorDesc = xmlParserAPDelete.getValueOf("Output");
										CourtOrder_PCWICreate_Log.CourtOrder_PCWICreate_Logger
												.debug("Error in delete query with description : " + errorDesc);
									}

								}
							} catch (Exception e) {
								CourtOrder_PCWICreate_Log.CourtOrder_PCWICreate_Logger.debug(
										"exception in faulty records file Movement to error folder" + e.getMessage());
							}
						}

					} else {
						// if file is in incorrect format
						CourtOrder_PCWICreate_Log.CourtOrder_PCWICreate_Logger
								.debug("****Excel file is not in xlsx format**** "
										+ FilenameUtils.getExtension(filepath).trim());
						TimeStamp = get_timestamp();
						String initialTime = TimeStamp;
						String errorFolderPathFile = errorFolderPath + File.separator + initialTime + " " + filename;
						Path returnFileMoveError = Files.move(Paths.get(filepath), Paths.get(errorFolderPathFile));
						// moving attachment of that excel to error
						// folder also
						moveAttachToError(RequestType, Request_Reference_No);
						// triggering mail
						String resultErrorMail = errorWaitMailTrigger(errorFolderPathFile, filename, "Error",
								sessionID);
						if ("Success".equalsIgnoreCase(resultErrorMail)) {
							CourtOrder_PCWICreate_Log.CourtOrder_PCWICreate_Logger
									.debug("*****Mail Trigger Successfully******");
						}
					}
					//
				}
			}
		} catch (Exception e) {
			CourtOrder_SystemAutoRemittance obj1 = new CourtOrder_SystemAutoRemittance();
			String exception = obj1.customException(e);
			CourtOrder_PCWICreate_Log.CourtOrder_PCWICreate_Logger.debug("exception in reading file: " + exception);
		}
	}

	private void createWIFromDb(String sessionID, long cycleStartTime) {
		try {
			long maxCycleTime = 300000;
			CourtOrder_PCWICreate_Log.CourtOrder_PCWICreate_Logger.debug("******Inside createWIFromDb***********");

			Map<String, List<String>> refNoWiList = new LinkedHashMap<>();
			Map<String, String> docKeyReqType = new HashMap<>();
			Map<String, String> docKeyRefNo = new HashMap<>();

			String query_wi = "Select Request_Reference_No,Name,Date_of_Birth,Date_of_Establishment,Due_Date,Trade_License_No,"
					+ "Nationality,Country_of_Incorporation,Requested_Authority,Emirates_ID,Passport,RequestType,"
					+ "record_inserted_date_time,Notice_Case_No from NG_courtOrder_CIR_ExcelData with(nolock)"
					+ "where (is_wi_created is null or is_wi_created='N')";
			String wi_inputXml = CommonMethods.apSelectWithColumnNames(query_wi, CommonConnection.getCabinetName(),
					CommonConnection.getSessionID(CourtOrder_PCWICreate_Log.CourtOrder_PCWICreate_Logger, false));
			CourtOrder_PCWICreate_Log.CourtOrder_PCWICreate_Logger.debug("wi_pc_inputXml: " + wi_inputXml);
			String wi_outputXml = WFNGExecute(wi_inputXml, CommonConnection.getJTSIP(), CommonConnection.getJTSPort(),
					1);
			CourtOrder_PCWICreate_Log.CourtOrder_PCWICreate_Logger.debug("wi_outputXml: " + wi_outputXml);
			XMLParser wi_xmlParserData = new XMLParser(wi_outputXml);
			int totalRetreived = Integer.parseInt(wi_xmlParserData.getValueOf("TotalRetrieved"));
			if (wi_xmlParserData.getValueOf("MainCode").equalsIgnoreCase("0") && totalRetreived > 0) {
				String wi_pc_val = wi_xmlParserData.getNextValueOf("Record");
				wi_pc_val = wi_pc_val.replaceAll("[ ]+>", ">").replaceAll("<[ ]+", "<");
				NGXmlList objWorkList = wi_xmlParserData.createList("Records", "Record");
				String Name = "", Date_of_Birth = "", Date_of_Establishment = "", Trade_License_No = "",
						Nationality = "", RequestType = "", Country_of_Incorporation = "", authorityName = "",
						Emirates_ID = "", Passport = "", Request_Reference_No = "", Request_Date = "", Due_Date = "",
						Notice_Case_No_Excel = "";
				for (; objWorkList.hasMoreElements(true); objWorkList.skip(true)) {
					//
					Name = objWorkList.getVal("Name");
					Date_of_Birth = objWorkList.getVal("Date_of_Birth");
					Date_of_Establishment = objWorkList.getVal("Date_of_Establishment");
					Trade_License_No = objWorkList.getVal("Trade_License_No");
					Nationality = objWorkList.getVal("Nationality");
					Country_of_Incorporation = objWorkList.getVal("Country_of_Incorporation");
					authorityName = objWorkList.getVal("Requested_Authority");
					Emirates_ID = objWorkList.getVal("Emirates_ID");
					Passport = objWorkList.getVal("Passport");
					Request_Reference_No = objWorkList.getVal("Request_Reference_No");
					RequestType = objWorkList.getVal("RequestType");
					Request_Date = objWorkList.getVal("record_inserted_date_time");
					Due_Date = objWorkList.getVal("Due_Date");
					Notice_Case_No_Excel = objWorkList.getVal("Notice_Case_No");
					String attributeTag = "";
					//
					String formattedDueDate = "";
					if (!"".equalsIgnoreCase(Due_Date) && !Due_Date.isEmpty()) {
						SimpleDateFormat formatterold = new SimpleDateFormat("dd/MM/yyyy");
						Date d1 = formatterold.parse(Due_Date);
						CourtOrder_PCWICreate_Log.CourtOrder_PCWICreate_Logger
								.debug("Due date before formatting is: " + d1);
						SimpleDateFormat formmatternew = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
						formattedDueDate = formmatternew.format(d1);
						CourtOrder_PCWICreate_Log.CourtOrder_PCWICreate_Logger
								.debug("Due date after formatting is: " + formattedDueDate);
					}
					//
					if (!"".equalsIgnoreCase(Emirates_ID) || !"".equalsIgnoreCase(Passport)
							|| (!"".equalsIgnoreCase(Name) && !"".equalsIgnoreCase(Date_of_Birth))) {
						attributeTag = attributeTag + "Full_Name_Indv" + (char) 21 + Name + (char) 25;
					} else if (!"".equalsIgnoreCase(Trade_License_No)) {
						attributeTag = attributeTag + "Company_Name_NonIndv" + (char) 21 + Name + (char) 25;
					}
					String duplicate_idetifier_columnName = "", duplicate_idetifier_columnValue = "";
					if (!"".equalsIgnoreCase(Emirates_ID)) {
						duplicate_idetifier_columnName = "Emirates_ID";
						duplicate_idetifier_columnValue = Emirates_ID;
					} else if (!"".equalsIgnoreCase(Passport)) {
						duplicate_idetifier_columnName = "Passport";
						duplicate_idetifier_columnValue = Passport;
					} else if (!"".equalsIgnoreCase(Trade_License_No)) {
						duplicate_idetifier_columnName = "Trade_License_No";
						duplicate_idetifier_columnValue = Trade_License_No;
					} else if (Date_of_Birth != null && !Date_of_Birth.isEmpty()
							&& !"null".equalsIgnoreCase(Date_of_Birth)) {

						duplicate_idetifier_columnName = "Date_of_Birth";
						duplicate_idetifier_columnValue = Date_of_Birth;
					} else if (Name != null && !Name.isEmpty() && !"null".equalsIgnoreCase(Name)) {

						duplicate_idetifier_columnName = "Name";
						duplicate_idetifier_columnValue = Name;
					}
					//
					// String final_Notice_Case_No = "";
					// if (!"".equalsIgnoreCase(Notice_Case_No_Excel) &&
					// !Notice_Case_No_Excel.isEmpty()) {
					//
					// if(Notice_Case_No_Excel.length() >= 12) {
					// final_Notice_Case_No = Notice_Case_No_Excel.substring(0,
					// 12);
					// }else {
					// final_Notice_Case_No = Notice_Case_No_Excel;
					// }
					// }

					RequestType = RequestType.replaceAll("CIR ", "");
					attributeTag = attributeTag + "DOB_Indv" + (char) 21 + Date_of_Birth + (char) 25;
					attributeTag = attributeTag + "Due_Date" + (char) 21 + formattedDueDate + (char) 25;
					attributeTag = attributeTag + "Emirates_ID_Indv" + (char) 21 + Emirates_ID + (char) 25;
					attributeTag = attributeTag + "Passport_Indv" + (char) 21 + Passport + (char) 25;
					attributeTag = attributeTag + "Nationality_Indv" + (char) 21 + Nationality + (char) 25;
					attributeTag = attributeTag + "Notice_Case_No" + (char) 21 + Notice_Case_No_Excel + (char) 25;
					attributeTag = attributeTag + "Date_Of_Establishment_NonIndv" + (char) 21 + Date_of_Establishment
							+ (char) 25;
					attributeTag = attributeTag + "Trade_License_Number_NonIndv" + (char) 21 + Trade_License_No
							+ (char) 25;
					attributeTag = attributeTag + "Country_Of_Incorporation_NonIndv" + (char) 21
							+ Country_of_Incorporation + (char) 25;

					attributeTag = attributeTag + "Request_Date" + (char) 21 + Request_Date + (char) 25;
					attributeTag = attributeTag + "Authority_Name" + (char) 21 + authorityName + (char) 25;
					attributeTag = attributeTag + "Decision" + (char) 21 + "CIR_Bulk_Created" + (char) 25;
					attributeTag = attributeTag + "Requested_Channel" + (char) 21 + "CIR - Bulk" + (char) 25;
					attributeTag = attributeTag + "Request_type" + (char) 21 + RequestType + (char) 25;
					attributeTag = attributeTag + "Request_Reference_No" + (char) 21 + Request_Reference_No + (char) 25;
					String CreateWi = "<?xml version=\"1.0\"?>" + "<WFUploadWorkItem_Input>"
							+ "<Option>WFUploadWorkItem</Option>" + "<EngineName>" + cabinetName + "</EngineName>"
							+ "<SessionId>" + sessionID + "</SessionId>" + "<ValidationRequired></ValidationRequired>"
							+ "<ProcessDefId>" + processDefId + "</ProcessDefId>" + "<DataDefName></DataDefName>"
							+ "<Fields></Fields>" + "<InitiateAlso>Y</InitiateAlso>" + "<Attributes>" + attributeTag
							+ "</Attributes>" + "</WFUploadWorkItem_Input>";
					CourtOrder_PCWICreate_Log.CourtOrder_PCWICreate_Logger.debug("CreateWi input: " + CreateWi);
					String CreateWi_outputXml = WFNGExecute(CreateWi, CommonConnection.getJTSIP(),
							CommonConnection.getJTSPort(), 1);

					CourtOrder_PCWICreate_Log.CourtOrder_PCWICreate_Logger
							.debug("CreateWi_outputXml: " + CreateWi_outputXml);
					XMLParser wi_co_xmlParserData = new XMLParser(CreateWi_outputXml);
					if (wi_co_xmlParserData.getValueOf("MainCode").equalsIgnoreCase("0")) {
						String Pc_ProcessInstanceId = wi_co_xmlParserData.getValueOf("ProcessInstanceId");
						CourtOrder_PCWICreate_Log.CourtOrder_PCWICreate_Logger
								.debug("WI No. in Creation wi PC from db is: " + Pc_ProcessInstanceId);
						updateTable("NG_courtOrder_CIR_ExcelData", "is_wi_created,Created_wi_name",
								"'Y','" + Pc_ProcessInstanceId + "'",
								"" + duplicate_idetifier_columnName + "='" + duplicate_idetifier_columnValue
										+ "' and Request_Reference_No='" + Request_Reference_No + "'",
								jtsIP, jtsPort, cabinetName);

						//
						String docKey = RequestType + "|" + Request_Reference_No;
						List<String> wiNoNewList = refNoWiList.get(docKey);
						if (wiNoNewList == null) {
							wiNoNewList = new ArrayList<String>();
							refNoWiList.put(docKey, wiNoNewList);
							docKeyReqType.put(docKey, RequestType);
							docKeyRefNo.put(docKey, Request_Reference_No);
						}
						wiNoNewList.add(Pc_ProcessInstanceId);

					} else {
						CourtOrder_PCWICreate_Log.CourtOrder_PCWICreate_Logger.debug("Error In Wi Creation in PC");
					}

				}
				// Retry to check doc
				int maxRetries = 4; // retries is 3 - just to initalize attempt
				for (Map.Entry<String, List<String>> entry : refNoWiList.entrySet()) {

					String docKey = entry.getKey();
					List<String> WiNosList = entry.getValue();
					String reqType = docKeyReqType.get(docKey);
					String reqRefNo = docKeyRefNo.get(docKey);

					CourtOrder_PCWICreate_Log.CourtOrder_PCWICreate_Logger.debug("WiNosList is: " + WiNosList);

					File docFile = null;
					boolean isDocPresent = false;
					for (int attempt = 1; attempt <= maxRetries; attempt++) {
						String inputFolderPath = "", destinationFolderpath = "", errorFolderPath = "",
								reqFolderName = "";
						reqFolderName = "CIR " + reqType;
						inputFolderPath = CIR_AttachDoc_INPUT.replaceAll("#FOLDERNAME#", reqFolderName);
						destinationFolderpath = CIR_AttachDoc_OUTPUT.replaceAll("#FOLDERNAME#", reqFolderName);
						errorFolderPath = CIR_AttachDoc_ERROR.replaceAll("#FOLDERNAME#", reqFolderName);
						CourtOrder_PCWICreate_Log.CourtOrder_PCWICreate_Logger
								.info("Input folderPath is " + inputFolderPath);
						CourtOrder_PCWICreate_Log.CourtOrder_PCWICreate_Logger
								.debug("destinationFolderpath is " + destinationFolderpath);
						CourtOrder_PCWICreate_Log.CourtOrder_PCWICreate_Logger
								.debug("errorFolderPath is " + errorFolderPath);
						// fetching doc's
						File folder = new File(inputFolderPath);
						if (folder.exists() && folder.isDirectory()) {

							File[] listOfFiles = folder.listFiles(File::isFile);
							if (listOfFiles != null && listOfFiles.length > 0) {
								for (File file : listOfFiles) {
									String filepath = file.getAbsolutePath();
									String filename = file.getName();
									String Request_RefNo_Doc = "";
									int indexOfDot = filename.lastIndexOf(".");
									String nameWithoutExtension = (indexOfDot != -1) ? filename.substring(0, indexOfDot)
											: filename;
									int lastDashIndex = nameWithoutExtension.lastIndexOf("-");
									int lenofFileName = filename.length();
									if (lenofFileName > indexOfDot) {
										Request_RefNo_Doc = nameWithoutExtension.substring(lastDashIndex + 1);
									}
									CourtOrder_PCWICreate_Log.CourtOrder_PCWICreate_Logger
											.debug("filepath:" + filepath);
									CourtOrder_PCWICreate_Log.CourtOrder_PCWICreate_Logger
											.debug("filename:" + filename);
									CourtOrder_PCWICreate_Log.CourtOrder_PCWICreate_Logger
											.debug("Request_RefNo_Doc:" + Request_RefNo_Doc);
									CourtOrder_PCWICreate_Log.CourtOrder_PCWICreate_Logger
											.debug("Request_RefNo of WI is:" + reqRefNo);

									if (reqRefNo.equalsIgnoreCase(Request_RefNo_Doc)) {
										isDocPresent = true;
										docFile = file;
										break;
									}
								}
							} else {
								CourtOrder_PCWICreate_Log.CourtOrder_PCWICreate_Logger
										.debug("No Doc is present inside: " + reqType + " Folder");
							}
						}
						// retry mechanism
						if ((isDocPresent == true) && (docFile != null)) {
							break;
						}

						if (attempt < 4) {
							// triggering mail
							String resultWaitMail = errorWaitMailTrigger("", reqFolderName + "-" + reqRefNo, "Wait",
									sessionID);
							if ("Success".equalsIgnoreCase(resultWaitMail)) {
								CourtOrder_PCWICreate_Log.CourtOrder_PCWICreate_Logger
										.debug("*****Mail Trigger Successfully******");
							}
							// commenting as retry time is more than utility
							// sleep time i.e., 5 min //new
							// changes
							// long timeElapsed = System.currentTimeMillis() -
							// cycleStartTime;
							// CourtOrder_PCWICreate_Log.CourtOrder_PCWICreate_Logger.debug("timeElapsed
							// is : " + timeElapsed);
							// long timeLeft = maxCycleTime - timeElapsed;
							// CourtOrder_PCWICreate_Log.CourtOrder_PCWICreate_Logger.debug("timeLeft
							// is : " + timeLeft);
							// if (timeLeft < 10000) {
							// break;
							// }
							// retryDelay = Math.min(attachDocWaitTimeInSec *
							// 1000, timeLeft);

							// setting 10 min retry time directly
							long retryDelay = (attachDocWaitTimeInSec * 1000);

							try {
								CourtOrder_PCWICreate_Log.CourtOrder_PCWICreate_Logger
										.debug("****Sleeping****Waiting for Attachments for next " + (retryDelay / 1000)
												+ " sec");
								CourtOrder_PCWICreate_Log.CourtOrder_PCWICreate_Logger.debug("attempt is " + attempt);
								Thread.sleep(retryDelay);
							} catch (InterruptedException e) {
								CourtOrder_SystemAutoRemittance obj1 = new CourtOrder_SystemAutoRemittance();
								String exception = obj1.customException(e);
								CourtOrder_PCWICreate_Log.CourtOrder_PCWICreate_Logger
										.debug("exception in thread sleeping : " + exception);
								Thread.currentThread().interrupt();
								break;
							}

						}
					}
					//
					if (isDocPresent && docFile != null) {
						for (int i = 0; i < WiNosList.size(); i++) {
							// attaching doc
							String attachDocWiStatus = "";
							String wiNo = WiNosList.get(i);
							boolean isLastWi = (i == WiNosList.size() - 1);
							CourtOrder_PCWICreate_Log.CourtOrder_PCWICreate_Logger.debug("isLastWi :" + isLastWi);
							attachDocWiStatus = attachDocwithWI(docFile, wiNo, reqType, isLastWi, sessionID);
							CourtOrder_PCWICreate_Log.CourtOrder_PCWICreate_Logger
									.debug("attachDocWiStatus is: " + attachDocWiStatus);
							if ("Success".equalsIgnoreCase(attachDocWiStatus)) {
								CourtOrder_PCWICreate_Log.CourtOrder_PCWICreate_Logger
										.debug("Document attached to WI successfully");
							} else if ("Error".equalsIgnoreCase(attachDocWiStatus)
									|| "".equalsIgnoreCase(attachDocWiStatus)) {
								CourtOrder_PCWICreate_Log.CourtOrder_PCWICreate_Logger
										.debug("Error while attaching doc with WI OR NoDocPresent");
							}
						}
					}
				}

			}

		} catch (Exception e) {
			CourtOrder_SystemAutoRemittance obj1 = new CourtOrder_SystemAutoRemittance();
			String exception = obj1.customException(e);
			CourtOrder_PCWICreate_Log.CourtOrder_PCWICreate_Logger.debug("exception in createWIFromDb : " + exception);
		}
	}

	private void start_CourtOrder_System_Check(String cabinetName, String sJtsIp, String iJtsPort, String sessionId,
			String queueID, int socketConnectionTimeOut, int integrationWaitTime,
			HashMap<String, String> socketDetailsMap) {
		ws_name = "System_Check";
		try {
			final HashMap<String, String> CheckGridDataMap = new HashMap<String, String>();
			sessionID = CommonConnection.getSessionID(CourtOrder_PCWICreate_Log.CourtOrder_PCWICreate_Logger, false);
			if (sessionID == null || sessionID.equalsIgnoreCase("") || sessionID.equalsIgnoreCase("null")) {
				CourtOrder_PCWICreate_Log.CourtOrder_PCWICreate_Logger.debug("Could Not Get Session ID " + sessionID);
				return;
			}
			String fileLocationDedupe = new StringBuffer().append(System.getProperty("user.dir"))
					.append(System.getProperty("file.separator")).append("Integration_Input")
					.append(System.getProperty("file.separator")).append("CourtOrder_DEDUP_SUMMARY.txt").toString();
			BufferedReader sbf = new BufferedReader(new FileReader(fileLocationDedupe));
			StringBuilder sbDedupe = new StringBuilder();
			String line = sbf.readLine();
			while (line != null) {
				sbDedupe.append(line);
				sbDedupe.append(System.lineSeparator());
				line = sbf.readLine();
			}
			String fileLocationRelatedShareHolder = new StringBuffer().append(System.getProperty("user.dir"))
					.append(System.getProperty("file.separator")).append("Integration_Input")
					.append(System.getProperty("file.separator")).append("CourtOrder_RELATED_SHAREDHOLDER_DETAILS.txt")
					.toString();
			BufferedReader sbf2 = new BufferedReader(new FileReader(fileLocationRelatedShareHolder));
			StringBuilder sbRelatedShareHolder = new StringBuilder();
			String line2 = sbf2.readLine();
			while (line2 != null) {
				sbRelatedShareHolder.append(line2);
				sbRelatedShareHolder.append(System.lineSeparator());
				line2 = sbf2.readLine();
			}
			String fileLocationCustomerEligibility = new StringBuffer().append(System.getProperty("user.dir"))
					.append(System.getProperty("file.separator")).append("Integration_Input")
					.append(System.getProperty("file.separator")).append("CourtOrder_CUSTOMER_ELIGIBILITY.txt")
					.toString();
			BufferedReader sbf3 = new BufferedReader(new FileReader(fileLocationCustomerEligibility));
			StringBuilder sbCustEligibility = new StringBuilder();
			String line3 = sbf3.readLine();
			while (line3 != null) {
				sbCustEligibility.append(line3);
				sbCustEligibility.append(System.lineSeparator());
				line3 = sbf3.readLine();
			}
			CourtOrder_PCWICreate_Log.CourtOrder_PCWICreate_Logger
					.debug("sbRelatedShareHolder: " + sbRelatedShareHolder);
			CourtOrder_PCWICreate_Log.CourtOrder_PCWICreate_Logger
					.debug("Fetching all Workitems on system check queue");
			String fetchWorkitemListInputXML = CommonMethods.fetchWorkItemsInput(cabinetName, sessionID, queueID);
			CourtOrder_PCWICreate_Log.CourtOrder_PCWICreate_Logger
					.debug("InputXML for fetchWorkList Call: " + fetchWorkitemListInputXML);
			String fetchWorkitemListOutputXML = WFNGExecute(fetchWorkitemListInputXML, sJtsIp, iJtsPort, 1);
			CourtOrder_PCWICreate_Log.CourtOrder_PCWICreate_Logger
					.debug("WMFetchWorkList system integration OutputXML: " + fetchWorkitemListOutputXML);
			XMLParser xmlParserFetchWorkItemlist = new XMLParser(fetchWorkitemListOutputXML);
			String fetchWorkItemListMainCode = xmlParserFetchWorkItemlist.getValueOf("MainCode");
			CourtOrder_PCWICreate_Log.CourtOrder_PCWICreate_Logger
					.debug("FetchWorkItemListMainCode: " + fetchWorkItemListMainCode);
			int fetchWorkitemListCount = Integer.parseInt(xmlParserFetchWorkItemlist.getValueOf("RetrievedCount"));
			CourtOrder_PCWICreate_Log.CourtOrder_PCWICreate_Logger
					.debug("RetrievedCount for WMFetchWorkList Call: " + fetchWorkitemListCount);
			CourtOrder_PCWICreate_Log.CourtOrder_PCWICreate_Logger
					.debug("Number of workitems retrieved on system check: " + fetchWorkitemListCount);
			System.out.println("Number of workitems retrieved on system check : " + fetchWorkitemListCount);

			if (fetchWorkItemListMainCode.trim().equals("0") && fetchWorkitemListCount > 0) {

				for (int i = 0; i < fetchWorkitemListCount; i++) {

					String fetchWorkItemlistData = xmlParserFetchWorkItemlist.getNextValueOf("Instrument");
					fetchWorkItemlistData = fetchWorkItemlistData.replaceAll("[ ]+>", ">").replaceAll("<[ ]+", "<");
					CourtOrder_PCWICreate_Log.CourtOrder_PCWICreate_Logger
							.debug("Parsing <Instrument> in WMFetchWorkList OutputXML: " + fetchWorkItemlistData);
					XMLParser xmlParserfetchWorkItemData = new XMLParser(fetchWorkItemlistData);
					String processInstanceID = xmlParserfetchWorkItemData.getValueOf("ProcessInstanceId");
					CourtOrder_PCWICreate_Log.CourtOrder_PCWICreate_Logger
							.debug("Current ProcessInstanceID: " + processInstanceID);
					CourtOrder_PCWICreate_Log.CourtOrder_PCWICreate_Logger
							.debug("Processing Workitem: " + processInstanceID);
					// System.out.println("Processing For Workitem At System
					// Check: " + processInstanceID);
					String WorkItemID = xmlParserfetchWorkItemData.getValueOf("WorkItemId");
					CourtOrder_PCWICreate_Log.CourtOrder_PCWICreate_Logger.debug("Current WorkItemID: " + WorkItemID);
					String entryDateTime = xmlParserfetchWorkItemData.getValueOf("EntryDateTime");
					CourtOrder_PCWICreate_Log.CourtOrder_PCWICreate_Logger
							.debug("Current EntryDateTime: " + entryDateTime);
					String ActivityName = xmlParserfetchWorkItemData.getValueOf("ActivityName");
					CourtOrder_PCWICreate_Log.CourtOrder_PCWICreate_Logger.debug("ActivityName: " + ActivityName);
					String ActivityID = xmlParserfetchWorkItemData.getValueOf("WorkStageId");
					CourtOrder_PCWICreate_Log.CourtOrder_PCWICreate_Logger.debug("ActivityID: " + ActivityID);
					String ActivityType = xmlParserfetchWorkItemData.getValueOf("ActivityType");
					CourtOrder_PCWICreate_Log.CourtOrder_PCWICreate_Logger.debug("ActivityType: " + ActivityType);
					String ProcessDefId = xmlParserfetchWorkItemData.getValueOf("RouteId");
					CourtOrder_PCWICreate_Log.CourtOrder_PCWICreate_Logger.debug("ProcessDefId: " + ProcessDefId);

					HashMap<String, String> ExtTableData = new HashMap<String, String>();
					String extTableQuery = "select  Request_type,Full_Name_Indv,Company_Name_NonIndv,DOB_Indv,"
							+ "Emirates_ID_Indv,Passport_Indv,Nationality_Indv,Date_Of_Establishment_NonIndv,"
							+ "Trade_License_Number_NonIndv,Country_Of_Incorporation_NonIndv,Authority_Name"
							+ "  from  ng_CourtOrder_exttable with(nolock) where Wi_name='" + processInstanceID + "'";
					String ExtTableInputXML = CommonMethods.apSelectWithColumnNames(extTableQuery, cabinetName,
							sessionID);
					CourtOrder_PCWICreate_Log.CourtOrder_PCWICreate_Logger
							.debug("SExtTableInputXML: " + ExtTableInputXML);
					String ExtTableOutputXML = WFNGExecute(ExtTableInputXML, sJtsIp, iJtsPort, 1);
					CourtOrder_PCWICreate_Log.CourtOrder_PCWICreate_Logger
							.debug("ExtTableOutputXML: " + ExtTableOutputXML);
					XMLParser xmlParserSocketDetails = new XMLParser(ExtTableOutputXML);
					String socketDetailsMainCode = xmlParserSocketDetails.getValueOf("MainCode");
					CourtOrder_PCWICreate_Log.CourtOrder_PCWICreate_Logger
							.debug("SocketDetailsMainCode: " + socketDetailsMainCode);
					int ExtTableTotalRecords = Integer.parseInt(xmlParserSocketDetails.getValueOf("TotalRetrieved"));
					CourtOrder_PCWICreate_Log.CourtOrder_PCWICreate_Logger
							.debug("SocketDetailsTotalRecords: " + ExtTableTotalRecords);

					String Full_Name_Indv = "", DOB_Indv = "", Emirates_ID_Indv = "", Passport_Indv = "",
							Nationality_Indv = "", Date_Of_Establishment_NonIndv = "", Company_Name_NonIndv = "",
							Trade_License_Number_NonIndv = "", Country_Of_Incorporation_NonIndv = "",
							Requested_Authority = "", request_type = "", firstName_Indv = "", lastName_Indv = "";

					if (socketDetailsMainCode.equalsIgnoreCase("0") && ExtTableTotalRecords > 0) {
						String xmlDataSocketDetails = xmlParserSocketDetails.getNextValueOf("Record");
						xmlDataSocketDetails = xmlDataSocketDetails.replaceAll("[ ]+>", ">").replaceAll("<[ ]+", "<");
						XMLParser xmlParserSocketDetailsRecord = new XMLParser(xmlDataSocketDetails);
						Full_Name_Indv = xmlParserSocketDetailsRecord.getValueOf("Full_Name_Indv");
						CourtOrder_PCWICreate_Log.CourtOrder_PCWICreate_Logger
								.debug("Full_Name_Indv: " + Full_Name_Indv);
						Company_Name_NonIndv = xmlParserSocketDetailsRecord.getValueOf("Company_Name_NonIndv");
						CourtOrder_PCWICreate_Log.CourtOrder_PCWICreate_Logger
								.debug("Company_Name_NonIndv: " + Company_Name_NonIndv);
						DOB_Indv = xmlParserSocketDetailsRecord.getValueOf("DOB_Indv");
						CourtOrder_PCWICreate_Log.CourtOrder_PCWICreate_Logger.debug("DOB_Indv: " + DOB_Indv);
						Emirates_ID_Indv = xmlParserSocketDetailsRecord.getValueOf("Emirates_ID_Indv");
						CourtOrder_PCWICreate_Log.CourtOrder_PCWICreate_Logger
								.debug("Emirates_ID_Indv: " + Emirates_ID_Indv);
						Passport_Indv = xmlParserSocketDetailsRecord.getValueOf("Passport_Indv");
						CourtOrder_PCWICreate_Log.CourtOrder_PCWICreate_Logger.debug("Passport_Indv: " + Passport_Indv);
						Nationality_Indv = xmlParserSocketDetailsRecord.getValueOf("Nationality_Indv");
						CourtOrder_PCWICreate_Log.CourtOrder_PCWICreate_Logger
								.debug("Nationality_Indv: " + Nationality_Indv);
						Date_Of_Establishment_NonIndv = xmlParserSocketDetailsRecord
								.getValueOf("Date_Of_Establishment_NonIndv");
						CourtOrder_PCWICreate_Log.CourtOrder_PCWICreate_Logger
								.debug("Date_Of_Establishment_NonIndv: " + Date_Of_Establishment_NonIndv);
						Trade_License_Number_NonIndv = xmlParserSocketDetailsRecord
								.getValueOf("Trade_License_Number_NonIndv");
						CourtOrder_PCWICreate_Log.CourtOrder_PCWICreate_Logger
								.debug("Trade_License_Number_NonIndv: " + Trade_License_Number_NonIndv);
						Country_Of_Incorporation_NonIndv = xmlParserSocketDetailsRecord
								.getValueOf("Country_Of_Incorporation_NonIndv");
						CourtOrder_PCWICreate_Log.CourtOrder_PCWICreate_Logger
								.debug("Country_Of_Incorporation_NonIndv: " + Country_Of_Incorporation_NonIndv);
						Requested_Authority = xmlParserSocketDetailsRecord.getValueOf("Authority_Name");
						CourtOrder_PCWICreate_Log.CourtOrder_PCWICreate_Logger
								.debug("Requested_Authority: " + Requested_Authority);
						request_type = xmlParserSocketDetailsRecord.getValueOf("Request_type");
						CourtOrder_PCWICreate_Log.CourtOrder_PCWICreate_Logger.debug("Request_type: " + request_type);

					}

					String DBQuery_entrydatetime = "select EntryDATETIME from WFINSTRUMENTTABLE with(nolock) where "
							+ "ProcessInstanceID ='" + processInstanceID + "' and ActivityName='System_Check'";
					String extTabDataIPXML = CommonMethods.apSelectWithColumnNames(DBQuery_entrydatetime,
							CommonConnection.getCabinetName(), CommonConnection
									.getSessionID(CourtOrder_PCWICreate_Log.CourtOrder_PCWICreate_Logger, false));
					CourtOrder_PCWICreate_Log.CourtOrder_PCWICreate_Logger
							.debug("extTabDataIPXML_1: " + extTabDataIPXML);
					String extTabDataOPXML = WFNGExecute(extTabDataIPXML, CommonConnection.getJTSIP(),
							CommonConnection.getJTSPort(), 1);
					CourtOrder_PCWICreate_Log.CourtOrder_PCWICreate_Logger
							.debug("extTabDataOPXML_2: " + extTabDataOPXML);
					XMLParser xmlParserData_5 = new XMLParser(extTabDataOPXML);
					int iTotalrec_4 = Integer.parseInt(xmlParserData_5.getValueOf("TotalRetrieved"));
					if (xmlParserData_5.getValueOf("MainCode").equalsIgnoreCase("0") && iTotalrec_4 > 0) {
						String xmlDataExtTab = xmlParserData_5.getNextValueOf("Record");
						xmlDataExtTab = xmlDataExtTab.replaceAll("[ ]+>", ">").replaceAll("<[ ]+", "<");
						NGXmlList objWorkList = xmlParserData_5.createList("Records", "Record");
						for (; objWorkList.hasMoreElements(true); objWorkList.skip(true)) {
							CheckGridDataMap.put("EntryDATETIME", objWorkList.getVal("EntryDATETIME"));
							CourtOrder_PCWICreate_Log.CourtOrder_PCWICreate_Logger
									.debug("EntryDATETIME : " + CheckGridDataMap.get("EntryDATETIME"));
						}
					}
					// For Retail

					if (!"".equalsIgnoreCase(Emirates_ID_Indv) || !"".equalsIgnoreCase(Passport_Indv)
							|| !"".equalsIgnoreCase(Full_Name_Indv)) {
						CourtOrder_PCWICreate_Log.CourtOrder_PCWICreate_Logger
								.debug("Inside Individual customer condition");
						String document_tag = "";

						if (!"".equalsIgnoreCase(Emirates_ID_Indv)) {
							document_tag = document_tag + "<Document>" + "<DocumentType>EMID</DocumentType>"
									+ "<DocumentRefNumber>" + Emirates_ID_Indv + "</DocumentRefNumber>" + "</Document>";
						} else {
							// hardcode as a part of finacle change
							if ("".equalsIgnoreCase(Emirates_ID_Indv) && Emirates_ID_Indv.isEmpty()
									&& !"".equalsIgnoreCase(Full_Name_Indv)) {

								document_tag = document_tag + "<Document>" + "<DocumentType>EMID</DocumentType>"
										+ "<DocumentRefNumber>11111111</DocumentRefNumber>" + "</Document>";
							}
						}
						if (!"".equalsIgnoreCase(Passport_Indv)) {
							document_tag = document_tag + "<Document>" + "<DocumentType>PPT</DocumentType>"
									+ "<DocumentRefNumber>" + Passport_Indv + "</DocumentRefNumber>" + "</Document>";
						}

						String CustomerDetails_tag = "<PersonDetails>";
						if (!"".equalsIgnoreCase(Full_Name_Indv) && !Full_Name_Indv.isEmpty()) {
							Full_Name_Indv = Full_Name_Indv.trim();
							firstName_Indv = Full_Name_Indv.substring(0, Full_Name_Indv.lastIndexOf(" "));
							lastName_Indv = Full_Name_Indv.substring(Full_Name_Indv.lastIndexOf(" ") + 1);
						}
						CourtOrder_PCWICreate_Log.CourtOrder_PCWICreate_Logger
								.debug("firstName_Indv after extraction : " + firstName_Indv);
						CourtOrder_PCWICreate_Log.CourtOrder_PCWICreate_Logger
								.debug("lastName_Indv after extraction : " + lastName_Indv);
						if (!"".equalsIgnoreCase(firstName_Indv)) {
							CustomerDetails_tag = CustomerDetails_tag + "<FirstName>" + firstName_Indv + "</FirstName>";
						} else {
							CustomerDetails_tag = CustomerDetails_tag + "<FirstName></FirstName>";
						}
						if (!"".equalsIgnoreCase(lastName_Indv)) {
							CustomerDetails_tag = CustomerDetails_tag + "<LastName>" + lastName_Indv + "</LastName>";
						} else {
							CustomerDetails_tag = CustomerDetails_tag + "<LastName></LastName>";
						}
						if (!"".equalsIgnoreCase(Full_Name_Indv)) {
							CustomerDetails_tag = CustomerDetails_tag + "<FullName>" + Full_Name_Indv + "</FullName>";
						} else {
							CustomerDetails_tag = CustomerDetails_tag + "<FullName></FullName>";
						}
						if (!"".equalsIgnoreCase(Nationality_Indv)) {
							CustomerDetails_tag = CustomerDetails_tag + "<Nationality>" + Nationality_Indv
									+ "</Nationality>";
						}
						if (!"".equalsIgnoreCase(DOB_Indv)) {
							String unformattedDate = DOB_Indv;
							SimpleDateFormat formatterold = new SimpleDateFormat("dd/MM/yyyy");
							Date d = formatterold.parse(unformattedDate);
							CourtOrder_PCWICreate_Log.CourtOrder_PCWICreate_Logger.debug("DOB d: " + d);
							SimpleDateFormat formmatternew = new SimpleDateFormat("yyyy-MM-dd");
							String formattedDate = formmatternew.format(d);
							CourtOrder_PCWICreate_Log.CourtOrder_PCWICreate_Logger
									.debug("DOB formated: " + formattedDate);
							CustomerDetails_tag = CustomerDetails_tag + "<DateOfBirth>" + formattedDate
									+ "</DateOfBirth>";
						} else {
							CustomerDetails_tag = CustomerDetails_tag + "<DateOfBirth></DateOfBirth>";
						}
						CustomerDetails_tag = CustomerDetails_tag + "</PersonDetails>";
						String RetailCorp_tag = "R";
						String input_xml = sbCustEligibility.toString().replaceAll("#RetailCorpFlag#", RetailCorp_tag)
								.replaceAll("#Personal_Details#", CustomerDetails_tag)
								.replaceAll("#DocumentDetails#", document_tag);
						StringBuilder StringBuilder_InputXML = new StringBuilder();
						StringBuilder_InputXML = StringBuilder_InputXML.append(input_xml);
						HashMap<String, String> socketConnectionMap = socketConnectionDetails(cabinetName, jtsIP,
								jtsPort, sessionID);
						String integrationStatus = "";
						integrationStatus = socketConnection(cabinetName, CommonConnection.getUsername(), sessionID,
								jtsIP, jtsPort, processInstanceID, ws_name, 60, 65, socketConnectionMap,
								StringBuilder_InputXML);
						XMLParser xmlParserSocketDetails1 = new XMLParser(integrationStatus);
						CourtOrder_PCWICreate_Log.CourtOrder_PCWICreate_Logger
								.debug(" xmlParserSocketDetails Cust Eligibility : " + xmlParserSocketDetails1);
						String return_code = xmlParserSocketDetails1.getValueOf("ReturnCode");
						CourtOrder_PCWICreate_Log.CourtOrder_PCWICreate_Logger
								.debug("Return Code: " + return_code + "WI: " + processInstanceID);
						String return_desc = xmlParserSocketDetails1.getValueOf("ReturnDesc");
						if ("0000".equalsIgnoreCase(return_code)) {
							// set cust type on form
							updateTable("ng_CourtOrder_exttable", "Customer_Type", "'Individual'",
									"wi_name='" + processInstanceID + "'", jtsIP, jtsPort, cabinetName);
							// identification of rak or non-rak case
							String isRakBankCase = "", caseIsValid = "";
							if (integrationStatus.contains("<CustomerDetails>")) {
								NGXmlList objWorkList = xmlParserSocketDetails1
										.createList("CustomerEligibilityResponse", "CustomerDetails");

								String IsDedupSuccess = "";
								for (; objWorkList.hasMoreElements(true); objWorkList.skip(true)) {
									IsDedupSuccess = objWorkList.getVal("IsDedupSuccess");
									String searchType = objWorkList.getVal("SearchType");
									CourtOrder_PCWICreate_Log.CourtOrder_PCWICreate_Logger
											.debug("IsDedupSuccess : " + IsDedupSuccess);
									CourtOrder_PCWICreate_Log.CourtOrder_PCWICreate_Logger
											.debug("searchType : " + searchType);
									if (!searchType.isEmpty() && "External".equalsIgnoreCase(searchType)) {
										caseIsValid = "No";
									} else if (!searchType.isEmpty() && "Internal".equalsIgnoreCase(searchType)) {
										caseIsValid = "Yes";
									}
									//
									if ("Y".equalsIgnoreCase(IsDedupSuccess)) {
										isRakBankCase = "No";
										break;
									} else if ("N".equalsIgnoreCase(IsDedupSuccess)) {
										isRakBankCase = "Yes";
									}
								}
							}
							CourtOrder_PCWICreate_Log.CourtOrder_PCWICreate_Logger
									.debug("isRakBankCase : " + isRakBankCase);
							//
							if ("No".equalsIgnoreCase(isRakBankCase) && "Yes".equalsIgnoreCase(caseIsValid)) {
								// Non-Rak Bank Case
								String columnNames = "wi_name,FULL_NAME,dob,PASSPORT,EMIRATES_ID,NATIONALITY,CIF,CUSTOMER_IDENTIFIED";
								String columnValues = "'" + processInstanceID + "','" + Full_Name_Indv + "','"
										+ DOB_Indv + "','" + Passport_Indv + "','" + Emirates_ID_Indv + "','"
										+ Nationality_Indv + "','','Non-Rak-Bank'";
								CourtOrder_PCWICreate_Log.CourtOrder_PCWICreate_Logger
										.debug("columnNames: " + columnNames);
								CourtOrder_PCWICreate_Log.CourtOrder_PCWICreate_Logger
										.debug("columnValues: " + columnValues);
								String apInsertInputXML = CommonMethods.apInsert(cabinetName, sessionID, columnNames,
										columnValues, "NG_COURTORDER_GR_INDIVIDUAL_CUSTOMER_DETAILS");
								CourtOrder_PCWICreate_Log.CourtOrder_PCWICreate_Logger
										.debug("apInsertInputXML: " + apInsertInputXML);
								String apInsertOutputXML = CommonMethods.WFNGExecute(apInsertInputXML, jtsIP, jtsPort,
										1);
								CourtOrder_PCWICreate_Log.CourtOrder_PCWICreate_Logger
										.debug("apInsertOutputXML: " + apInsertOutputXML);
								XMLParser xmlParserAPInsert = new XMLParser(apInsertOutputXML);
								String apInsertMaincode = xmlParserAPInsert.getValueOf("MainCode");
								if (apInsertMaincode.equalsIgnoreCase("0")) {
									CourtOrder_PCWICreate_Log.CourtOrder_PCWICreate_Logger
											.debug("1 row inserted successfully: ");
								} else {
									CourtOrder_PCWICreate_Log.CourtOrder_PCWICreate_Logger.debug("error in Apinsert ");
								}
								// updating into ext table
								updateTable("ng_CourtOrder_exttable", "CustomerIdentified", "'Non-Rak-Bank'",
										"wi_name='" + processInstanceID + "'", jtsIP, jtsPort, cabinetName);

							} else if ("Yes".equalsIgnoreCase(isRakBankCase)) {
								// Rak Bank Case
								NGXmlList objWorkList = xmlParserSocketDetails1
										.createList("CustomerEligibilityResponse", "CustomerDetails");
								CourtOrder_PCWICreate_Log.CourtOrder_PCWICreate_Logger
										.debug("objWorkList : " + objWorkList);
								for (; objWorkList.hasMoreElements(true); objWorkList.skip(true)) {
									// String PassportFromResposne = "",
									// EmiratesFromResposne = "";
									// String Customer =
									// objWorkList.getVal("Customer");
									// Customer = "<Customer>" + Customer +
									// "</Customer>";
									// CourtOrder_PCWICreate_Log.CourtOrder_PCWICreate_Logger
									// .debug("Customer : " + Customer);
									// XMLParser xmlParserSocketDetailsDocument
									// = new XMLParser(Customer);
									// NGXmlList objWorkList2 =
									// xmlParserSocketDetailsDocument.createList("Customer",
									// "Document");
									// CourtOrder_PCWICreate_Log.CourtOrder_PCWICreate_Logger
									// .debug("objWorkList2 : " + objWorkList2);
									// for (;
									// objWorkList2.hasMoreElements(true);
									// objWorkList2.skip(true)) {
									// String DocumentType =
									// objWorkList2.getVal("DocumentType");
									// CourtOrder_PCWICreate_Log.CourtOrder_PCWICreate_Logger
									// .debug("DocumentType : " + DocumentType);
									// if ("PPT".equalsIgnoreCase(DocumentType))
									// {
									// PassportFromResposne =
									// objWorkList2.getVal("DocumentRefNumber");
									// } else if
									// ("EMID".equalsIgnoreCase(DocumentType)) {
									// EmiratesFromResposne =
									// objWorkList2.getVal("DocumentRefNumber");
									// }
									// }
									String CIFID = objWorkList.getVal("CustId");
									String searchType = objWorkList.getVal("SearchType");
									CourtOrder_PCWICreate_Log.CourtOrder_PCWICreate_Logger
											.debug("searchType : " + searchType);
									if (!"".equalsIgnoreCase(CIFID) && "Internal".equalsIgnoreCase(searchType)) {
										CourtOrder_PCWICreate_Log.CourtOrder_PCWICreate_Logger
												.debug("CIFID : " + CIFID);
										String columnNames = "wi_name,FIRST_NAME,MIDDLE_NAME,LAST_NAME,FULL_NAME,dob,PASSPORT,EMIRATES_ID,NATIONALITY,CIF,CUSTOMER_IDENTIFIED";
										String columnValues = "'" + processInstanceID + "','"
												+ objWorkList.getVal("CustFirstName") + "','"
												+ objWorkList.getVal("CustMiddleName") + "','"
												+ objWorkList.getVal("CustLastName") + "','"
												+ objWorkList.getVal("CustFullName") + "','"
												+ objWorkList.getVal("CustDateOfBirth") + "','"
												+ objWorkList.getVal("PassportNum") + "','"
												+ objWorkList.getVal("EmiratesID") + "','"
												+ objWorkList.getVal("CustNationality") + "','"
												+ objWorkList.getVal("CustId") + "','Rak-Bank'";
										CourtOrder_PCWICreate_Log.CourtOrder_PCWICreate_Logger
												.debug("columnNames: " + columnNames);
										CourtOrder_PCWICreate_Log.CourtOrder_PCWICreate_Logger
												.debug("columnValues: " + columnValues);
										String apInsertInputXML = CommonMethods.apInsert(cabinetName, sessionID,
												columnNames, columnValues,
												"NG_COURTORDER_GR_INDIVIDUAL_CUSTOMER_DETAILS");
										CourtOrder_PCWICreate_Log.CourtOrder_PCWICreate_Logger
												.debug("apInsertInputXML: " + apInsertInputXML);
										String apInsertOutputXML = CommonMethods.WFNGExecute(apInsertInputXML, jtsIP,
												jtsPort, 1);
										CourtOrder_PCWICreate_Log.CourtOrder_PCWICreate_Logger
												.debug("apInsertOutputXML: " + apInsertOutputXML);
										XMLParser xmlParserAPInsert = new XMLParser(apInsertOutputXML);
										String apInsertMaincode = xmlParserAPInsert.getValueOf("MainCode");
										if (apInsertMaincode.equalsIgnoreCase("0")) {
											CourtOrder_PCWICreate_Log.CourtOrder_PCWICreate_Logger
													.debug("1 row inserted successfully: ");
										} else {
											CourtOrder_PCWICreate_Log.CourtOrder_PCWICreate_Logger
													.debug("error in Apinsert ");
										}
									}
								}
								// updating into ext table
								updateTable("ng_CourtOrder_exttable", "CustomerIdentified", "'Rak-Bank'",
										"wi_name='" + processInstanceID + "'", jtsIP, jtsPort, cabinetName);
							}
							// for related indiv
							String DBQuery_IndividualCustomer = "select Related_party_status,FULL_NAME,NATIONALITY,"
									+ "PASSPORT,DOB,EMIRATES_ID from NG_COURTORDER_GR_INDIVIDUAL_CUSTOMER_DETAILS "
									+ "with(nolock)  where CUSTOMER_IDENTIFIED='Non-Rak-Bank' and  wi_name='"
									+ processInstanceID + "' ";
							String IndividualCustomer_IPXML = CommonMethods.apSelectWithColumnNames(
									DBQuery_IndividualCustomer, CommonConnection.getCabinetName(),
									CommonConnection.getSessionID(
											CourtOrder_PCWICreate_Log.CourtOrder_PCWICreate_Logger, false));
							CourtOrder_PCWICreate_Log.CourtOrder_PCWICreate_Logger
									.debug("IndividualCustomer_IPXML: " + IndividualCustomer_IPXML);
							String IndividualCustomer_OPXML = WFNGExecute(IndividualCustomer_IPXML,
									CommonConnection.getJTSIP(), CommonConnection.getJTSPort(), 1);
							CourtOrder_PCWICreate_Log.CourtOrder_PCWICreate_Logger
									.debug("IndividualCustomer_OPXML: " + IndividualCustomer_OPXML);
							XMLParser xmlParserData_IndividualCustomer = new XMLParser(IndividualCustomer_OPXML);
							int iTotalrec_IndividualCustomer = Integer
									.parseInt(xmlParserData_IndividualCustomer.getValueOf("TotalRetrieved"));
							if (xmlParserData_IndividualCustomer.getValueOf("MainCode").equalsIgnoreCase("0")
									&& iTotalrec_IndividualCustomer > 0) {
								String xmlDataExtTab = xmlParserData_IndividualCustomer.getNextValueOf("Record");
								xmlDataExtTab = xmlDataExtTab.replaceAll("[ ]+>", ">").replaceAll("<[ ]+", "<");
								NGXmlList objWorkList = xmlParserData_IndividualCustomer.createList("Records",
										"Record");
								String FULL_NAME = "", NATIONALITY = "", PASSPORT = "", DOB = "",
										Related_party_status = "", EMIRATES_ID = "";
								for (; objWorkList.hasMoreElements(true); objWorkList.skip(true)) {
									FULL_NAME = objWorkList.getVal("FULL_NAME");
									NATIONALITY = objWorkList.getVal("NATIONALITY");
									PASSPORT = objWorkList.getVal("PASSPORT");
									DOB = objWorkList.getVal("DOB");
									EMIRATES_ID = objWorkList.getVal("EMIRATES_ID");
									Related_party_status = objWorkList.getVal("Related_party_status");
									CourtOrder_PCWICreate_Log.CourtOrder_PCWICreate_Logger
											.debug("FULL_NAME : " + FULL_NAME);
									CourtOrder_PCWICreate_Log.CourtOrder_PCWICreate_Logger
											.debug("NATIONALITY : " + NATIONALITY);
									CourtOrder_PCWICreate_Log.CourtOrder_PCWICreate_Logger
											.debug("PASSPORT : " + PASSPORT);
									CourtOrder_PCWICreate_Log.CourtOrder_PCWICreate_Logger.debug("DOB : " + DOB);
									CourtOrder_PCWICreate_Log.CourtOrder_PCWICreate_Logger
											.debug("EMIRATES_ID : " + EMIRATES_ID);
									CourtOrder_PCWICreate_Log.CourtOrder_PCWICreate_Logger
											.debug("Related_party_status : " + Related_party_status);
									if (!"RELATED_SHAREDHOLDER = Success".equalsIgnoreCase(Related_party_status)) {
										String formattedDate = "";
										if (!"".equalsIgnoreCase(DOB)) {
											String unformattedDate = DOB;
											SimpleDateFormat formatterold = new SimpleDateFormat("dd/MM/yyyy");
											Date d = formatterold.parse(unformattedDate);
											CourtOrder_PCWICreate_Log.CourtOrder_PCWICreate_Logger.debug("DOB d: " + d);
											SimpleDateFormat formmatternew = new SimpleDateFormat("dd-MM-yyyy");
											formattedDate = formmatternew.format(d);
											CourtOrder_PCWICreate_Log.CourtOrder_PCWICreate_Logger
													.debug("DOB formated RELATED_SHAREHOLDER_DET : " + formattedDate);
										}
										String PersonalDetails = "<RetailCustomer>" + "<Name>" + FULL_NAME + "</Name>"
												+ "<DateOfBirth>" + formattedDate + "</DateOfBirth>" + "<Nationality>"
												+ NATIONALITY + "</Nationality>" + "<PassportNumber>" + PASSPORT
												+ "</PassportNumber>" + "</RetailCustomer>";
										String RetailCorporateFlag = "R";
										String RELATED_SHAREDHOLDER_DETAILS_INPUT_XML = sbRelatedShareHolder.toString()
												.replaceAll("#RetCorpInd#", RetailCorporateFlag)
												.replaceAll("#PersonalDetails#", PersonalDetails);
										CourtOrder_PCWICreate_Log.CourtOrder_PCWICreate_Logger
												.debug("RELATED_SHAREDHOLDER_DETAILS_INPUT_XML after replace: "
														+ RELATED_SHAREDHOLDER_DETAILS_INPUT_XML);
										StringBuilder StringBuilder_InputXML2 = new StringBuilder();
										StringBuilder_InputXML2 = StringBuilder_InputXML2
												.append(RELATED_SHAREDHOLDER_DETAILS_INPUT_XML);
										integrationStatus = socketConnection(cabinetName,
												CommonConnection.getUsername(), sessionID, jtsIP, jtsPort,
												processInstanceID, ws_name, 60, 65, socketConnectionMap,
												StringBuilder_InputXML2);
										XMLParser xmlParserSocketDetails2 = new XMLParser(integrationStatus);
										CourtOrder_PCWICreate_Log.CourtOrder_PCWICreate_Logger
												.debug(" xmlParserSocketDetails2 : " + xmlParserSocketDetails2);
										String ReturnCode = xmlParserSocketDetails2.getValueOf("ReturnCode");
										CourtOrder_PCWICreate_Log.CourtOrder_PCWICreate_Logger
												.debug("Return Code: " + ReturnCode);
										return_desc = xmlParserSocketDetails2.getValueOf("ReturnDesc");
										String relatedPartyStatus = "";
										if ("0000".equalsIgnoreCase(ReturnCode)) {
											relatedPartyStatus = "RELATED SHAREDHOLDER = Success";

											if (integrationStatus.contains("<MainCIFDetail>")) {
												NGXmlList objWorkList1 = xmlParserSocketDetails2
														.createList("InqShareholderDetailsResponse", "MainCIFDetail");
												for (; objWorkList1.hasMoreElements(true); objWorkList1.skip(true)) {
													String Related_CIFID = objWorkList1.getVal("CIFId");
													String CIF_NAME = objWorkList1.getVal("CustomerName");
													String relationshipType = objWorkList1.getVal("RelationShipType");
													String RetCorpFlag = "C";
													String passport_emiratesId = "";
													if (!"".equalsIgnoreCase(Related_CIFID)) {
														if (!"".equalsIgnoreCase(PASSPORT)) {
															passport_emiratesId = PASSPORT;
														} else if (!"".equalsIgnoreCase(EMIRATES_ID)) {
															passport_emiratesId = EMIRATES_ID;
														}
														String columnNames = "wi_name,passport_emiratesId,"
																+ "RELATED_CIF_ID,CUSTOMER_NAME,CIF_NAME,"
																+ "Retail_Corporate,RELATIONSHIP_TYPE";
														String columnValues = "'" + processInstanceID + "','"
																+ passport_emiratesId + "','" + Related_CIFID + "','"
																+ Related_CIFID + "','" + CIF_NAME + "','" + RetCorpFlag
																+ "','" + relationshipType + "'";
														CourtOrder_PCWICreate_Log.CourtOrder_PCWICreate_Logger
																.debug("columnNames: " + columnNames);
														CourtOrder_PCWICreate_Log.CourtOrder_PCWICreate_Logger
																.debug("columnValues: " + columnValues);
														String apInsertInputXML = CommonMethods.apInsert(cabinetName,
																sessionID, columnNames, columnValues,
																"NG_COURTORDER_GR_INDIVIDUAL_RELATED_PARTY_DETAILS");
														CourtOrder_PCWICreate_Log.CourtOrder_PCWICreate_Logger
																.debug("apInsertInputXML: " + apInsertInputXML);
														String apInsertOutputXML = CommonMethods
																.WFNGExecute(apInsertInputXML, jtsIP, jtsPort, 1);
														CourtOrder_PCWICreate_Log.CourtOrder_PCWICreate_Logger
																.debug("apInsertOutputXML: " + apInsertOutputXML);
														XMLParser xmlParserAPInsert = new XMLParser(apInsertOutputXML);
														String apInsertMaincode = xmlParserAPInsert
																.getValueOf("MainCode");
														if (apInsertMaincode.equalsIgnoreCase("0")) {
															CourtOrder_PCWICreate_Log.CourtOrder_PCWICreate_Logger
																	.debug("1 row inserted successfully: ");
														} else {
															CourtOrder_PCWICreate_Log.CourtOrder_PCWICreate_Logger
																	.debug("error in Apinsert ");
														}
													}
												}
											}
											// updating into ext table
											updateTable("ng_CourtOrder_exttable", "IsRelatedPartyExist", "'Y'",
													"wi_name='" + processInstanceID + "'", jtsIP, jtsPort, cabinetName);
											//
											updateTable("NG_COURTORDER_GR_INDIVIDUAL_CUSTOMER_DETAILS",
													"Related_party_status", "'" + relatedPartyStatus + "'",
													"WI_name='" + processInstanceID + "'", jtsIP, jtsPort, cabinetName);
											String decisionValue = "Success";
											String attributesTag = "<Decision>" + decisionValue + "</Decision>";
											CheckGridDataMap.put("Decision", decisionValue);
											doneworkitem(cabinetName, sJtsIp, iJtsPort, sessionID, queueID,
													socketConnectionTimeOut, integrationWaitTime, socketDetailsMap,
													processInstanceID, WorkItemID, ActivityID, ProcessDefId,
													ActivityType, attributesTag, ActivityName, CheckGridDataMap);
										} else if ("FIN : RECORD NOT FOUND".equalsIgnoreCase(return_desc)) {
											relatedPartyStatus = "RELATED SHAREDHOLDER = No CIFs";
											// updating into ext table
											updateTable("ng_CourtOrder_exttable", "IsRelatedPartyExist", "'N'",
													"wi_name='" + processInstanceID + "'", jtsIP, jtsPort, cabinetName);
											//
											updateTable("NG_COURTORDER_GR_INDIVIDUAL_CUSTOMER_DETAILS",
													"Related_party_status", "'" + relatedPartyStatus + "'",
													"WI_name='" + processInstanceID + "'", jtsIP, jtsPort, cabinetName);
											// for freeze & prohibited only
											if ("Freeze".equalsIgnoreCase(request_type)
													|| "Prohibited".equalsIgnoreCase(request_type)) {
												updateTable("NG_COURTORDER_GR_INDIVIDUAL_CUSTOMER_DETAILS",
														"CIF_SELECTION", "'Matched'",
														"WI_name='" + processInstanceID + "'", jtsIP, jtsPort,
														cabinetName);
											}
											String decisionValue = "Success";
											String attributesTag = "<Decision>" + decisionValue + "</Decision>";
											CheckGridDataMap.put("Decision", decisionValue);
											doneworkitem(cabinetName, sJtsIp, iJtsPort, sessionID, queueID,
													socketConnectionTimeOut, integrationWaitTime, socketDetailsMap,
													processInstanceID, WorkItemID, ActivityID, ProcessDefId,
													ActivityType, attributesTag, ActivityName, CheckGridDataMap);
										} else {
											relatedPartyStatus = "RELATED_SHAREDHOLDER = Failure";
											updateTable("NG_COURTORDER_GR_INDIVIDUAL_CUSTOMER_DETAILS",
													"Related_party_status", "'" + relatedPartyStatus + "'",
													"WI_name='" + processInstanceID + "'", jtsIP, jtsPort, cabinetName);
											String decisionValue = "Failure";
											String attributesTag = "<Decision>" + decisionValue + "</Decision>";
											CheckGridDataMap.put("Decision", decisionValue);
											doneworkitem(cabinetName, sJtsIp, iJtsPort, sessionID, queueID,
													socketConnectionTimeOut, integrationWaitTime, socketDetailsMap,
													processInstanceID, WorkItemID, ActivityID, ProcessDefId,
													ActivityType, attributesTag, ActivityName, CheckGridDataMap);
										}
									}
								}
							} else {
								String decisionValue = "Success";
								String attributesTag = "<Decision>" + decisionValue + "</Decision>";
								CheckGridDataMap.put("Decision", decisionValue);
								doneworkitem(cabinetName, sJtsIp, iJtsPort, sessionID, queueID, socketConnectionTimeOut,
										integrationWaitTime, socketDetailsMap, processInstanceID, WorkItemID,
										ActivityID, ProcessDefId, ActivityType, attributesTag, ActivityName,
										CheckGridDataMap);
							}

						} else {
							CourtOrder_PCWICreate_Log.CourtOrder_PCWICreate_Logger
									.debug("API response ReturnCode : " + return_code);
							Date datenow = new Date();
							SimpleDateFormat sf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
							String datenowString = sf.format(datenow);
							String MessageId = xmlParserSocketDetails1.getValueOf("MessageId");
							String MsgFormat = xmlParserSocketDetails1.getValueOf("MsgFormat");
							String columnValues = "'" + processInstanceID + "','" + MsgFormat + "','System_Check','"
									+ return_desc + "','" + MessageId + "','" + datenowString + "'";
							insertInErrorTable(jtsIP, jtsPort, columnValues);
							String decisionValue = "Failure";
							String attributesTag = "<Decision>" + decisionValue + "</Decision>";
							CheckGridDataMap.put("Decision", decisionValue);
							doneworkitem(cabinetName, sJtsIp, iJtsPort, sessionID, queueID, socketConnectionTimeOut,
									integrationWaitTime, socketDetailsMap, processInstanceID, WorkItemID, ActivityID,
									ProcessDefId, ActivityType, attributesTag, ActivityName, CheckGridDataMap);
						}
					}
					// For Corporate
					else if (!"".equalsIgnoreCase(Trade_License_Number_NonIndv)) {
						String document_tag = "";
						CourtOrder_PCWICreate_Log.CourtOrder_PCWICreate_Logger
								.debug("Inside Non-Individual customer condition");
						if (!"".equalsIgnoreCase(Trade_License_Number_NonIndv)) {
							document_tag = document_tag + "<Document>" + "<DocumentType>TDLIC</DocumentType>"
									+ "<DocumentRefNumber>" + Trade_License_Number_NonIndv + "</DocumentRefNumber>"
									+ "</Document>";
						}
						String CustomerDetails_tag = "<OrganizationDetails>";
						if (!"".equalsIgnoreCase(Company_Name_NonIndv)) {
							CustomerDetails_tag = CustomerDetails_tag + "<CorporateName>" + Company_Name_NonIndv
									+ "</CorporateName>";
						}
						if (!"".equalsIgnoreCase(Country_Of_Incorporation_NonIndv)) {
							CustomerDetails_tag = CustomerDetails_tag + "<CountryOfIncorporation>"
									+ Country_Of_Incorporation_NonIndv + "</CountryOfIncorporation>";
						}
						String formattedDate = "";
						if (!"".equalsIgnoreCase(Date_Of_Establishment_NonIndv)) {
							String unformattedDate = Date_Of_Establishment_NonIndv;
							SimpleDateFormat formatterold = new SimpleDateFormat("dd/MM/yyyy");
							Date d = formatterold.parse(unformattedDate);
							CourtOrder_PCWICreate_Log.CourtOrder_PCWICreate_Logger.debug("DOB d: " + d);
							SimpleDateFormat formmatternew = new SimpleDateFormat("yyyy-MM-dd");
							formattedDate = formmatternew.format(d);
							CourtOrder_PCWICreate_Log.CourtOrder_PCWICreate_Logger
									.debug("DOB formated: " + formattedDate);
							CustomerDetails_tag = CustomerDetails_tag + "<DateOfIncorporation>" + formattedDate
									+ "</DateOfIncorporation>";
						}
						CustomerDetails_tag = CustomerDetails_tag + "</OrganizationDetails>";
						CourtOrder_PCWICreate_Log.CourtOrder_PCWICreate_Logger
								.debug("CustomerDetails_tag: " + CustomerDetails_tag);
						String RetailCorp_tag = "C";
						String input_xml = sbDedupe.toString().replaceAll("#RetailCorpFlag#", RetailCorp_tag)
								.replaceAll("#Personal_Organisation_Details#", CustomerDetails_tag)
								.replaceAll("#DocumentDetails#", document_tag);
						StringBuilder StringBuilder_InputXML = new StringBuilder();
						StringBuilder_InputXML = StringBuilder_InputXML.append(input_xml);
						HashMap<String, String> socketConnectionMap = socketConnectionDetails(cabinetName, jtsIP,
								jtsPort, sessionID);
						String integrationStatus = "";
						integrationStatus = socketConnection(cabinetName, CommonConnection.getUsername(), sessionID,
								jtsIP, jtsPort, processInstanceID, ws_name, 60, 65, socketConnectionMap,
								StringBuilder_InputXML);
						XMLParser xmlParserSocketDetails1 = new XMLParser(integrationStatus);
						CourtOrder_PCWICreate_Log.CourtOrder_PCWICreate_Logger
								.debug(" xmlParserSocketDetails : " + xmlParserSocketDetails1);
						String return_code = xmlParserSocketDetails1.getValueOf("ReturnCode");
						CourtOrder_PCWICreate_Log.CourtOrder_PCWICreate_Logger
								.debug("Return Code: " + return_code + "WI: " + processInstanceID);
						String return_desc = xmlParserSocketDetails1.getValueOf("ReturnDesc");
						if ("0000".equalsIgnoreCase(return_code)) {
							// set cust type on form
							updateTable("ng_CourtOrder_exttable", "Customer_Type", "'Non-Individual'",
									"wi_name='" + processInstanceID + "'", jtsIP, jtsPort, cabinetName);
							//
							if (!integrationStatus.contains("<Customer>")) {
								CourtOrder_PCWICreate_Log.CourtOrder_PCWICreate_Logger
										.debug("Inside !integrationStatus.contains(<Customer>) for Non-Indv");
								String columnNames = "wi_name,company_Name,Trade_License_Number,Date_of_Establishment,country_of_Incorporation,TL_Issusing_Autrhority,CIF_ID,CUSTOMER_IDENTIFIED_as";
								String columnValues = "'" + processInstanceID + "','" + Company_Name_NonIndv + "','"
										+ Trade_License_Number_NonIndv + "','" + Date_Of_Establishment_NonIndv + "','"
										+ Country_Of_Incorporation_NonIndv + "','','','Non-Rak-Bank'";
								CourtOrder_PCWICreate_Log.CourtOrder_PCWICreate_Logger
										.debug("columnNames: " + columnNames);
								CourtOrder_PCWICreate_Log.CourtOrder_PCWICreate_Logger
										.debug("columnValues: " + columnValues);
								String apInsertInputXML = CommonMethods.apInsert(cabinetName, sessionID, columnNames,
										columnValues, "NG_COURTORDER_GR_NON_INDIVIDUAL_CUSTOMER_DETAILS");
								CourtOrder_PCWICreate_Log.CourtOrder_PCWICreate_Logger
										.debug("apInsertInputXML: " + apInsertInputXML);
								String apInsertOutputXML = CommonMethods.WFNGExecute(apInsertInputXML, jtsIP, jtsPort,
										1);
								CourtOrder_PCWICreate_Log.CourtOrder_PCWICreate_Logger
										.debug("apInsertOutputXML: " + apInsertOutputXML);
								XMLParser xmlParserAPInsert = new XMLParser(apInsertOutputXML);
								String apInsertMaincode = xmlParserAPInsert.getValueOf("MainCode");
								if (apInsertMaincode.equalsIgnoreCase("0")) {
									CourtOrder_PCWICreate_Log.CourtOrder_PCWICreate_Logger
											.debug("1 row inserted successfully: ");
								} else {
									CourtOrder_PCWICreate_Log.CourtOrder_PCWICreate_Logger.debug("error in Apinsert ");
								}
								// updating into ext table
								updateTable("ng_CourtOrder_exttable", "CustomerIdentified", "'Non-Rak-Bank'",
										"wi_name='" + processInstanceID + "'", jtsIP, jtsPort, cabinetName);
								//
							} else if (integrationStatus.contains("<Customer>")) {
								CourtOrder_PCWICreate_Log.CourtOrder_PCWICreate_Logger
										.debug("Inside integrationStatus.contains(<Customer>) for Non-Indv");
								String TDLICFromResposne = "";
								NGXmlList objWorkList = xmlParserSocketDetails1
										.createList("CustomerDuplicationListResponse", "Customer");
								for (; objWorkList.hasMoreElements(true); objWorkList.skip(true)) {
									String Customer = objWorkList.getVal("Customer");
									Customer = "<Customer>" + Customer + "</Customer>";
									CourtOrder_PCWICreate_Log.CourtOrder_PCWICreate_Logger
											.debug("Customer : " + Customer);
									XMLParser xmlParserSocketDetailsDocument = new XMLParser(Customer);
									NGXmlList objWorkList2 = xmlParserSocketDetailsDocument.createList("Customer",
											"Document");
									CourtOrder_PCWICreate_Log.CourtOrder_PCWICreate_Logger
											.debug("objWorkList2 : " + objWorkList2);
									for (; objWorkList2.hasMoreElements(true); objWorkList2.skip(true)) {
										String DocumentType = objWorkList2.getVal("DocumentType");
										CourtOrder_PCWICreate_Log.CourtOrder_PCWICreate_Logger
												.debug("DocumentType : " + DocumentType);
										if ("TDLIC".equalsIgnoreCase(DocumentType)) {
											TDLICFromResposne = objWorkList2.getVal("DocumentRefNumber");
										}
									}
									String CIFID = objWorkList.getVal("CIFID");
									if (!"".equalsIgnoreCase(CIFID)) {
										CourtOrder_PCWICreate_Log.CourtOrder_PCWICreate_Logger
												.debug("CIFID : " + objWorkList.getVal("CIFID"));
										String columnNames = "wi_name,company_Name,Trade_License_Number,Date_of_Establishment,country_of_Incorporation,CIF_ID,CUSTOMER_IDENTIFIED_as";
										String columnValues = "'" + processInstanceID + "','"
												+ objWorkList.getVal("CorporateName") + "','" + TDLICFromResposne
												+ "','" + objWorkList.getVal("DateOfIncorporation") + "','"
												+ objWorkList.getVal("CountryOfIncorporation") + "','"
												+ objWorkList.getVal("CIFID") + "','Rak-Bank'";
										CourtOrder_PCWICreate_Log.CourtOrder_PCWICreate_Logger
												.debug("columnNames: " + columnNames);
										CourtOrder_PCWICreate_Log.CourtOrder_PCWICreate_Logger
												.debug("columnValues: " + columnValues);
										String apInsertInputXML = CommonMethods.apInsert(cabinetName, sessionID,
												columnNames, columnValues,
												"NG_COURTORDER_GR_NON_INDIVIDUAL_CUSTOMER_DETAILS");
										CourtOrder_PCWICreate_Log.CourtOrder_PCWICreate_Logger
												.debug("apInsertInputXML: " + apInsertInputXML);
										String apInsertOutputXML = CommonMethods.WFNGExecute(apInsertInputXML, jtsIP,
												jtsPort, 1);
										CourtOrder_PCWICreate_Log.CourtOrder_PCWICreate_Logger
												.debug("apInsertOutputXML: " + apInsertOutputXML);
										XMLParser xmlParserAPInsert = new XMLParser(apInsertOutputXML);
										String apInsertMaincode = xmlParserAPInsert.getValueOf("MainCode");
										if (apInsertMaincode.equalsIgnoreCase("0")) {
											CourtOrder_PCWICreate_Log.CourtOrder_PCWICreate_Logger
													.debug("1 row inserted successfully: ");
										} else {
											CourtOrder_PCWICreate_Log.CourtOrder_PCWICreate_Logger
													.debug("error in Apinsert ");
										}
									}
								}
								// updating into ext table
								updateTable("ng_CourtOrder_exttable", "CustomerIdentified", "'Rak-Bank'",
										"wi_name='" + processInstanceID + "'", jtsIP, jtsPort, cabinetName);
								//
							}
							// for related
							String DBQuery_IndividualCustomer = "select company_Name,Trade_License_Number,Date_of_Establishment,TL_Issusing_Autrhority,country_of_Incorporation,Related_party_status from NG_COURTORDER_GR_NON_INDIVIDUAL_CUSTOMER_DETAILS with(nolock) where wi_name='"
									+ processInstanceID + "' and CUSTOMER_IDENTIFIED_as='Non-Rak-Bank'";
							String IndividualCustomer_IPXML = CommonMethods.apSelectWithColumnNames(
									DBQuery_IndividualCustomer, CommonConnection.getCabinetName(),
									CommonConnection.getSessionID(
											CourtOrder_PCWICreate_Log.CourtOrder_PCWICreate_Logger, false));
							CourtOrder_PCWICreate_Log.CourtOrder_PCWICreate_Logger
									.debug("IndividualCustomer_IPXML: " + IndividualCustomer_IPXML);
							String IndividualCustomer_OPXML = WFNGExecute(IndividualCustomer_IPXML,
									CommonConnection.getJTSIP(), CommonConnection.getJTSPort(), 1);
							CourtOrder_PCWICreate_Log.CourtOrder_PCWICreate_Logger
									.debug("IndividualCustomer_OPXML: " + IndividualCustomer_OPXML);
							XMLParser xmlParserData_IndividualCustomer = new XMLParser(IndividualCustomer_OPXML);
							int iTotalrec_IndividualCustomer = Integer
									.parseInt(xmlParserData_IndividualCustomer.getValueOf("TotalRetrieved"));
							if (xmlParserData_IndividualCustomer.getValueOf("MainCode").equalsIgnoreCase("0")
									&& iTotalrec_IndividualCustomer > 0) {
								String xmlDataExtTab = xmlParserData_IndividualCustomer.getNextValueOf("Record");
								xmlDataExtTab = xmlDataExtTab.replaceAll("[ ]+>", ">").replaceAll("<[ ]+", "<");
								NGXmlList objWorkList = xmlParserData_IndividualCustomer.createList("Records",
										"Record");
								String company_Name = "", Trade_License_Number = "", Date_of_Establishment = "",
										TL_Issusing_Autrhority = "", country_of_Incorporation = "",
										Related_party_status = "";
								for (; objWorkList.hasMoreElements(true); objWorkList.skip(true)) {
									company_Name = objWorkList.getVal("company_Name");
									Trade_License_Number = objWorkList.getVal("Trade_License_Number");
									Date_of_Establishment = objWorkList.getVal("Date_of_Establishment");
									TL_Issusing_Autrhority = objWorkList.getVal("TL_Issusing_Autrhority");
									country_of_Incorporation = objWorkList.getVal("country_of_Incorporation");
									Related_party_status = objWorkList.getVal("Related_party_status");
									CourtOrder_PCWICreate_Log.CourtOrder_PCWICreate_Logger
											.debug("company_Name : " + company_Name);
									CourtOrder_PCWICreate_Log.CourtOrder_PCWICreate_Logger
											.debug("Trade_License_Number : " + Trade_License_Number);
									CourtOrder_PCWICreate_Log.CourtOrder_PCWICreate_Logger
											.debug("Date_of_Establishment : " + Date_of_Establishment);
									CourtOrder_PCWICreate_Log.CourtOrder_PCWICreate_Logger
											.debug("TL_Issusing_Autrhority : " + TL_Issusing_Autrhority);
									CourtOrder_PCWICreate_Log.CourtOrder_PCWICreate_Logger
											.debug("country_of_Incorporation : " + country_of_Incorporation);
									CourtOrder_PCWICreate_Log.CourtOrder_PCWICreate_Logger
											.debug("Related_party_status : " + Related_party_status);
									if (!"RELATED_SHAREDHOLDER = Success".equalsIgnoreCase(Related_party_status)) {
										String RetailCorporateFlag = "C";
										formattedDate = "";
										if (!"".equalsIgnoreCase(Date_of_Establishment)) {
											String unformattedDate = Date_of_Establishment;
											SimpleDateFormat formatterold = new SimpleDateFormat("dd/MM/yyyy");
											Date d = formatterold.parse(unformattedDate);
											CourtOrder_PCWICreate_Log.CourtOrder_PCWICreate_Logger.debug("DOB d: " + d);
											SimpleDateFormat formmatternew = new SimpleDateFormat("dd-MM-yyyy");
											formattedDate = formmatternew.format(d);
											CourtOrder_PCWICreate_Log.CourtOrder_PCWICreate_Logger
													.debug("DOB formated: " + formattedDate);
										}
										String PersonalDetails = "<CorporateCustomer>" + "<CorporateName>"
												+ company_Name + "</CorporateName>" + "<DateOfIncorporation>"
												+ formattedDate + "</DateOfIncorporation>" + "<TradeLicenceNumber>"
												+ Trade_License_Number + "</TradeLicenceNumber>"
												+ "</CorporateCustomer>";
										String RELATED_SHAREDHOLDER_DETAILS_INPUT_XML = sbRelatedShareHolder.toString()
												.replaceAll("#RetCorpInd#", RetailCorporateFlag)
												.replaceAll("#PersonalDetails#", PersonalDetails);
										StringBuilder StringBuilder_InputXML2 = new StringBuilder();
										StringBuilder_InputXML2 = StringBuilder_InputXML2
												.append(RELATED_SHAREDHOLDER_DETAILS_INPUT_XML);
										integrationStatus = socketConnection(cabinetName,
												CommonConnection.getUsername(), sessionID, jtsIP, jtsPort,
												processInstanceID, ws_name, 60, 65, socketConnectionMap,
												StringBuilder_InputXML2);
										XMLParser xmlParserSocketDetails2 = new XMLParser(integrationStatus);
										CourtOrder_PCWICreate_Log.CourtOrder_PCWICreate_Logger
												.debug(" xmlParserSocketDetails2 : " + xmlParserSocketDetails2);
										String ReturnCode = xmlParserSocketDetails2.getValueOf("ReturnCode");
										CourtOrder_PCWICreate_Log.CourtOrder_PCWICreate_Logger
												.debug("Return Code: " + ReturnCode);
										return_desc = xmlParserSocketDetails2.getValueOf("ReturnDesc");
										String relatedPartyStatus = "";
										if ("0000".equalsIgnoreCase(ReturnCode)) {
											relatedPartyStatus = "RELATED_SHAREDHOLDER = Success";
											if (integrationStatus.contains("<MainCIFDetail>")) {
												NGXmlList objWorkList1 = xmlParserSocketDetails2
														.createList("InqShareholderDetailsResponse", "MainCIFDetail");
												String CUSTOMER_NAME = "", CIF_ID = "", RELATED_CIF_ID = "",
														CIF_NAME = "", trade_license_no = "";
												for (; objWorkList1.hasMoreElements(true); objWorkList1.skip(true)) {
													String Related_CIFID = objWorkList1.getVal("CIFId");
													String relationshipType = objWorkList1.getVal("RelationShipType");
													String RetCorpFlag = "C";
													if (!"".equalsIgnoreCase(Related_CIFID)) {
														CUSTOMER_NAME = objWorkList1.getVal("company_name");
														CIF_ID = objWorkList1.getVal("CIF_Id");
														RELATED_CIF_ID = objWorkList1.getVal("Related_CIFID");
														CIF_NAME = objWorkList1.getVal("CustomerName");
														trade_license_no = objWorkList1.getVal("Trade_liscense_number");
														String columnNames = "wi_name,CUSTOMER_NAME,CIF_ID,RELATED_CIF_ID,CIF_NAME,trade_license_no,Retail_Corporate,RELATIONSHIP_TYPE";
														String columnValues = "'" + processInstanceID + "','"
																+ CUSTOMER_NAME + "','" + CIF_ID + "','"
																+ RELATED_CIF_ID + "','" + CIF_NAME + "','"
																+ trade_license_no + "','" + RetCorpFlag + "','"
																+ relationshipType + "'";
														CourtOrder_PCWICreate_Log.CourtOrder_PCWICreate_Logger
																.debug("columnNames: " + columnNames);
														CourtOrder_PCWICreate_Log.CourtOrder_PCWICreate_Logger
																.debug("columnValues: " + columnValues);
														String apInsertInputXML = CommonMethods.apInsert(cabinetName,
																sessionID, columnNames, columnValues,
																"NG_COURTORDER_GR_NON_INDIVIDUAL_RELATED_PARTY_DETAILS");
														CourtOrder_PCWICreate_Log.CourtOrder_PCWICreate_Logger
																.debug("apInsertInputXML: " + apInsertInputXML);
														String apInsertOutputXML = CommonMethods
																.WFNGExecute(apInsertInputXML, jtsIP, jtsPort, 1);
														CourtOrder_PCWICreate_Log.CourtOrder_PCWICreate_Logger
																.debug("apInsertOutputXML: " + apInsertOutputXML);
														XMLParser xmlParserAPInsert = new XMLParser(apInsertOutputXML);
														String apInsertMaincode = xmlParserAPInsert
																.getValueOf("MainCode");
														if (apInsertMaincode.equalsIgnoreCase("0")) {
															CourtOrder_PCWICreate_Log.CourtOrder_PCWICreate_Logger
																	.debug("1 row inserted successfully: ");
														} else {
															CourtOrder_PCWICreate_Log.CourtOrder_PCWICreate_Logger
																	.debug("error in Apinsert ");
														}
													}
												}
											}

											// updating into ext table
											updateTable("ng_CourtOrder_exttable", "IsRelatedPartyExist", "'Y'",
													"wi_name='" + processInstanceID + "'", jtsIP, jtsPort, cabinetName);
											//
											updateTable("NG_COURTORDER_GR_NON_INDIVIDUAL_CUSTOMER_DETAILS",
													"Related_party_status", "'" + relatedPartyStatus + "'",
													"WI_name='" + processInstanceID + "'", jtsIP, jtsPort, cabinetName);
											String decisionValue = "Success";
											String attributesTag = "<Decision>" + decisionValue + "</Decision>";
											CheckGridDataMap.put("Decision", decisionValue);
											doneworkitem(cabinetName, sJtsIp, iJtsPort, sessionID, queueID,
													socketConnectionTimeOut, integrationWaitTime, socketDetailsMap,
													processInstanceID, WorkItemID, ActivityID, ProcessDefId,
													ActivityType, attributesTag, ActivityName, CheckGridDataMap);
										} else if ("FIN : RECORD NOT FOUND".equalsIgnoreCase(return_desc)) {
											relatedPartyStatus = "RELATED SHAREDHOLDER = No CIFs";
											// updating into ext table
											updateTable("ng_CourtOrder_exttable", "IsRelatedPartyExist", "'N'",
													"wi_name='" + processInstanceID + "'", jtsIP, jtsPort, cabinetName);
											//
											updateTable("NG_COURTORDER_GR_NON_INDIVIDUAL_CUSTOMER_DETAILS",
													"Related_party_status", "'" + relatedPartyStatus + "'",
													"WI_name='" + processInstanceID + "'", jtsIP, jtsPort, cabinetName);
											// for freeze & prohibited only
											if ("Freeze".equalsIgnoreCase(request_type)
													|| "Prohibited".equalsIgnoreCase(request_type)) {
												updateTable("NG_COURTORDER_GR_NON_INDIVIDUAL_CUSTOMER_DETAILS",
														"CIF_SELECTION", "'Matched'",
														"WI_name='" + processInstanceID + "'", jtsIP, jtsPort,
														cabinetName);
											}
											String decisionValue = "Success";
											String attributesTag = "<Decision>" + decisionValue + "</Decision>";
											CheckGridDataMap.put("Decision", decisionValue);
											doneworkitem(cabinetName, sJtsIp, iJtsPort, sessionID, queueID,
													socketConnectionTimeOut, integrationWaitTime, socketDetailsMap,
													processInstanceID, WorkItemID, ActivityID, ProcessDefId,
													ActivityType, attributesTag, ActivityName, CheckGridDataMap);
										} else {
											relatedPartyStatus = "RELATED_SHAREDHOLDER = Failure";
											updateTable("NG_COURTORDER_GR_NON_INDIVIDUAL_CUSTOMER_DETAILS",
													"Related_party_status", "'" + relatedPartyStatus + "'",
													"WI_name='" + processInstanceID + "'", jtsIP, jtsPort, cabinetName);
											String decisionValue = "Failure";
											String attributesTag = "<Decision>" + decisionValue + "</Decision>";
											CheckGridDataMap.put("Decision", decisionValue);
											doneworkitem(cabinetName, sJtsIp, iJtsPort, sessionID, queueID,
													socketConnectionTimeOut, integrationWaitTime, socketDetailsMap,
													processInstanceID, WorkItemID, ActivityID, ProcessDefId,
													ActivityType, attributesTag, ActivityName, CheckGridDataMap);
										}
									}
								}
							} else {
								String decisionValue = "Success";
								String attributesTag = "<Decision>" + decisionValue + "</Decision>";
								CheckGridDataMap.put("Decision", decisionValue);
								doneworkitem(cabinetName, sJtsIp, iJtsPort, sessionID, queueID, socketConnectionTimeOut,
										integrationWaitTime, socketDetailsMap, processInstanceID, WorkItemID,
										ActivityID, ProcessDefId, ActivityType, attributesTag, ActivityName,
										CheckGridDataMap);
							}
						} else {
							CourtOrder_PCWICreate_Log.CourtOrder_PCWICreate_Logger
									.debug("API response ReturnCode : " + return_code);
							Date datenow = new Date();
							SimpleDateFormat sf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
							String datenowString = sf.format(datenow);
							String MessageId = xmlParserSocketDetails1.getValueOf("MessageId");
							String MsgFormat = xmlParserSocketDetails1.getValueOf("MsgFormat");
							String columnValues = "'" + processInstanceID + "','" + MsgFormat + "','System_Check','"
									+ return_desc + "','" + MessageId + "','" + datenowString + "'";
							insertInErrorTable(jtsIP, jtsPort, columnValues);
							String decisionValue = "Failure";
							String attributesTag = "<Decision>" + decisionValue + "</Decision>";
							CheckGridDataMap.put("Decision", decisionValue);
							doneworkitem(cabinetName, sJtsIp, iJtsPort, sessionID, queueID, socketConnectionTimeOut,
									integrationWaitTime, socketDetailsMap, processInstanceID, WorkItemID, ActivityID,
									ProcessDefId, ActivityType, attributesTag, ActivityName, CheckGridDataMap);
						}
					}
					//
					CourtOrder_PCWICreate_Log.CourtOrder_PCWICreate_Logger.debug("i value is: " + i);
					if (i == 99) {
						String lastProcessInstanceId = processInstanceID;
						String lastWorkItemId = WorkItemID;
						String CreationDateTime = entryDateTime;
						fetchWorkitemListInputXML = CommonMethods.getFetchWorkItemsInputXML(lastProcessInstanceId,
								lastWorkItemId, sessionId, cabinetName, queueID, CreationDateTime);
						CourtOrder_PCWICreate_Log.CourtOrder_PCWICreate_Logger
								.debug("fetchWorkitemListInputXML next: " + fetchWorkitemListInputXML);

						fetchWorkitemListOutputXML = CommonMethods.WFNGExecute(fetchWorkitemListInputXML, sJtsIp,
								iJtsPort, 1);
						CourtOrder_PCWICreate_Log.CourtOrder_PCWICreate_Logger
								.debug("WMFetchWorkList OutputXML next: " + fetchWorkitemListOutputXML);

						xmlParserFetchWorkItemlist = new XMLParser(fetchWorkitemListOutputXML);

						fetchWorkItemListMainCode = xmlParserFetchWorkItemlist.getValueOf("MainCode");
						CourtOrder_PCWICreate_Log.CourtOrder_PCWICreate_Logger
								.debug("FetchWorkItemListMainCode next: " + fetchWorkItemListMainCode);

						fetchWorkitemListCount = Integer
								.parseInt(xmlParserFetchWorkItemlist.getValueOf("RetrievedCount"));
						CourtOrder_PCWICreate_Log.CourtOrder_PCWICreate_Logger
								.debug("RetrievedCount for WMFetchWorkList Call next: " + fetchWorkitemListCount);
						System.out.println(
								"Number of workitems retrieved on System Check Queue next: " + fetchWorkitemListCount);
						i = 0;
					}
				}
			}
		} catch (Exception e) {
			CourtOrder_SystemAutoRemittance obj1 = new CourtOrder_SystemAutoRemittance();
			String exception = obj1.customException(e);
			CourtOrder_PCWICreate_Log.CourtOrder_PCWICreate_Logger
					.debug("Exception in start_CourtOrder_System_Check: " + exception);
		}
	}

	String socketConnection(String cabinetName, String username, String sessionId, String sJtsIp, String iJtsPort,
			String processInstanceID, String ws_name, int connection_timeout, int integrationWaitTime,
			HashMap<String, String> socketDetailsMap, StringBuilder sInputXML) {
		String socketServerIP;
		int socketServerPort;
		Socket socket = null;
		OutputStream out = null;
		InputStream socketInputStream = null;
		DataOutputStream dout = null;
		DataInputStream din = null;
		String outputResponse = null;
		String inputRequest = null;
		String inputMessageID = null;
		try {
			CourtOrder_PCWICreate_Log.CourtOrder_PCWICreate_Logger.debug("userName " + username);
			CourtOrder_PCWICreate_Log.CourtOrder_PCWICreate_Logger.debug("SessionId " + sessionID);
			socketServerIP = socketDetailsMap.get("SocketServerIP");
			CourtOrder_PCWICreate_Log.CourtOrder_PCWICreate_Logger.debug("SocketServerIP " + socketServerIP);
			socketServerPort = Integer.parseInt(socketDetailsMap.get("SocketServerPort"));
			CourtOrder_PCWICreate_Log.CourtOrder_PCWICreate_Logger.debug("SocketServerPort " + socketServerPort);
			if (!("".equalsIgnoreCase(socketServerIP) && socketServerIP == null && socketServerPort == 0)) {
				socket = new Socket(socketServerIP, socketServerPort);
				socket.setSoTimeout(connection_timeout * 1000);
				out = socket.getOutputStream();
				socketInputStream = socket.getInputStream();
				dout = new DataOutputStream(out);
				din = new DataInputStream(socketInputStream);
				CourtOrder_PCWICreate_Log.CourtOrder_PCWICreate_Logger.debug("Dout " + dout);
				CourtOrder_PCWICreate_Log.CourtOrder_PCWICreate_Logger.debug("Din " + din);
				outputResponse = "";
				String History_tablename = "NG_CourtOrder_XMLLOG_HISTORY";
				inputRequest = getRequestXML(cabinetName, sessionID, processInstanceID, ws_name, username, sInputXML,
						History_tablename);
				if (inputRequest != null && inputRequest.length() > 0) {
					int inputRequestLen = inputRequest.getBytes("UTF-16LE").length;
					CourtOrder_PCWICreate_Log.CourtOrder_PCWICreate_Logger.debug("RequestLen: " + inputRequestLen + "");
					inputRequest = inputRequestLen + "##8##;" + inputRequest;
					CourtOrder_PCWICreate_Log.CourtOrder_PCWICreate_Logger
							.debug("InputRequest" + "Input Request Bytes : " + inputRequest.getBytes("UTF-16LE"));
					dout.write(inputRequest.getBytes("UTF-16LE"));
					dout.flush();
				}
				byte[] readBuffer = new byte[500];
				int num = din.read(readBuffer);
				if (num > 0) {
					byte[] arrayBytes = new byte[num];
					System.arraycopy(readBuffer, 0, arrayBytes, 0, num);
					outputResponse = outputResponse + new String(arrayBytes, "UTF-16LE");
					inputMessageID = outputResponse;
					CourtOrder_PCWICreate_Log.CourtOrder_PCWICreate_Logger.debug("OutputResponse: " + outputResponse);
					if (!"".equalsIgnoreCase(outputResponse))
						outputResponse = getResponseXML(cabinetName, sJtsIp, iJtsPort, sessionID, processInstanceID,
								outputResponse, integrationWaitTime);
					if (outputResponse.contains("&lt;")) {
						outputResponse = outputResponse.replaceAll("&lt;", "<");
						outputResponse = outputResponse.replaceAll("&gt;", ">");
					}
				}
				socket.close();
				outputResponse = outputResponse.replaceAll("</MessageId>",
						"</MessageId>/n<InputMessageId>" + inputMessageID + "</InputMessageId>");
				return outputResponse;
			} else {
				CourtOrder_PCWICreate_Log.CourtOrder_PCWICreate_Logger
						.debug("SocketServerIp and SocketServerPort is not maintained " + "");
				CourtOrder_PCWICreate_Log.CourtOrder_PCWICreate_Logger
						.debug("SocketServerIp is not maintained " + socketServerIP);
				CourtOrder_PCWICreate_Log.CourtOrder_PCWICreate_Logger
						.debug(" SocketServerPort is not maintained " + socketServerPort);
				return "Socket Details not maintained";
			}
		} catch (Exception e) {
			CourtOrder_PCWICreate_Log.CourtOrder_PCWICreate_Logger
					.debug("Exception Occured Mq_connection_CC" + e.getStackTrace());
			return "";
		} finally {
			try {
				if (out != null) {
					out.close();
					out = null;
				}
				if (socketInputStream != null) {
					socketInputStream.close();
					socketInputStream = null;
				}
				if (dout != null) {
					dout.close();
					dout = null;
				}
				if (din != null) {
					din.close();
					din = null;
				}
				if (socket != null) {
					if (!socket.isClosed())
						socket.close();
					socket = null;
				}
			} catch (Exception e) {
				CourtOrder_PCWICreate_Log.CourtOrder_PCWICreate_Logger
						.debug("Final Exception Occured Mq_connection_CC" + e.getStackTrace());
			}
		}
	}

	private String getResponseXML(String cabinetName, String sJtsIp, String iJtsPort, String sessionId,
			String processInstanceID, String message_ID, int integrationWaitTime) {
		String outputResponseXML = "";
		try {
			String QueryString = "select OUTPUT_XML from NG_courtorder_XMLLOG_HISTORY with (nolock) where MESSAGE_ID ='"
					+ message_ID + "' and WI_NAME = '" + processInstanceID + "'";
			String responseInputXML = CommonMethods.apSelectWithColumnNames(QueryString, cabinetName, sessionID);
			CourtOrder_PCWICreate_Log.CourtOrder_PCWICreate_Logger
					.debug("Response APSelect InputXML: " + responseInputXML);
			int Loop_count = 0;
			do {
				String responseOutputXML = CommonMethods.WFNGExecute(responseInputXML, sJtsIp, iJtsPort, 1);
				CourtOrder_PCWICreate_Log.CourtOrder_PCWICreate_Logger
						.debug("Response APSelect OutputXML: " + responseOutputXML);
				XMLParser xmlParserSocketDetails = new XMLParser(responseOutputXML);
				String responseMainCode = xmlParserSocketDetails.getValueOf("MainCode");
				CourtOrder_PCWICreate_Log.CourtOrder_PCWICreate_Logger.debug("ResponseMainCode: " + responseMainCode);
				int responseTotalRecords = Integer.parseInt(xmlParserSocketDetails.getValueOf("TotalRetrieved"));
				CourtOrder_PCWICreate_Log.CourtOrder_PCWICreate_Logger
						.debug("ResponseTotalRecords: " + responseTotalRecords);
				if (responseMainCode.equals("0") && responseTotalRecords > 0) {
					String responseXMLData = xmlParserSocketDetails.getNextValueOf("Record");
					responseXMLData = responseXMLData.replaceAll("[ ]+>", ">").replaceAll("<[ ]+", "<");
					XMLParser xmlParserResponseXMLData = new XMLParser(responseXMLData);
					outputResponseXML = xmlParserResponseXMLData.getValueOf("OUTPUT_XML");
					if ("".equalsIgnoreCase(outputResponseXML)) {
						outputResponseXML = "Error";
					}
					break;
				}
				Loop_count++;
				Thread.sleep(1000);
			} while (Loop_count < integrationWaitTime);
			CourtOrder_PCWICreate_Log.CourtOrder_PCWICreate_Logger.debug("integrationWaitTime: " + integrationWaitTime);
		} catch (Exception e) {
			CourtOrder_SystemAutoRemittance obj1 = new CourtOrder_SystemAutoRemittance();
			String exception = obj1.customException(e);
			CourtOrder_PCWICreate_Log.CourtOrder_PCWICreate_Logger
					.debug("Exception occurred in outputResponseXML: " + exception);
			outputResponseXML = "Error";
		}
		return outputResponseXML;
	}

	private String getRequestXML(String cabinetName, String sessionId, String processInstanceID, String ws_name,
			String userName, StringBuilder sInputXML, String tablename) {
		StringBuffer strBuff = new StringBuffer();
		strBuff.append("<APMQPUTGET_Input>");
		strBuff.append("<SessionId>" + sessionID + "</SessionId>");
		strBuff.append("<EngineName>" + cabinetName + "</EngineName>");
		strBuff.append("<XMLHISTORY_TABLENAME>" + tablename + "</XMLHISTORY_TABLENAME>");
		strBuff.append("<WI_NAME>" + processInstanceID + "</WI_NAME>");
		strBuff.append("<WS_NAME>" + ws_name + "</WS_NAME>");
		strBuff.append("<USER_NAME>" + userName + "</USER_NAME>");
		strBuff.append("<MQ_REQUEST_XML>");
		strBuff.append(sInputXML);
		strBuff.append("</MQ_REQUEST_XML>");
		strBuff.append("</APMQPUTGET_Input>");
		CourtOrder_PCWICreate_Log.CourtOrder_PCWICreate_Logger.debug("GetRequestXML: " + strBuff.toString());
		return strBuff.toString();
	}

	private HashMap<String, String> socketConnectionDetails(String cabinetName, String sJtsIp, String iJtsPort,
			String sessionID) {
		HashMap<String, String> socketDetailsMap = new HashMap<String, String>();
		try {
			CourtOrder_PCWICreate_Log.CourtOrder_PCWICreate_Logger.debug("Fetching Socket Connection Details.");
			System.out.println("Fetching Socket Connection Details.");
			String socketDetailsQuery = "SELECT SocketServerIP,SocketServerPort FROM NG_BPM_MQ_TABLE with (nolock) where ProcessName = 'CourtOrder' and CallingSource = 'Utility'";
			String socketDetailsInputXML = CommonMethods.apSelectWithColumnNames(socketDetailsQuery, cabinetName,
					sessionID);
			CourtOrder_PCWICreate_Log.CourtOrder_PCWICreate_Logger
					.debug("Socket Details APSelect InputXML: " + socketDetailsInputXML);
			String socketDetailsOutputXML = WFNGExecute(socketDetailsInputXML, sJtsIp, iJtsPort, 1);
			CourtOrder_PCWICreate_Log.CourtOrder_PCWICreate_Logger
					.debug("Socket Details APSelect OutputXML: " + socketDetailsOutputXML);
			XMLParser xmlParserSocketDetails = new XMLParser(socketDetailsOutputXML);
			String socketDetailsMainCode = xmlParserSocketDetails.getValueOf("MainCode");
			CourtOrder_PCWICreate_Log.CourtOrder_PCWICreate_Logger
					.debug("SocketDetailsMainCode: " + socketDetailsMainCode);
			int socketDetailsTotalRecords = Integer.parseInt(xmlParserSocketDetails.getValueOf("TotalRetrieved"));
			CourtOrder_PCWICreate_Log.CourtOrder_PCWICreate_Logger
					.debug("SocketDetailsTotalRecords: " + socketDetailsTotalRecords);
			if (socketDetailsMainCode.equalsIgnoreCase("0") && socketDetailsTotalRecords > 0) {
				String xmlDataSocketDetails = xmlParserSocketDetails.getNextValueOf("Record");
				xmlDataSocketDetails = xmlDataSocketDetails.replaceAll("[ ]+>", ">").replaceAll("<[ ]+", "<");
				XMLParser xmlParserSocketDetailsRecord = new XMLParser(xmlDataSocketDetails);
				String socketServerIP = xmlParserSocketDetailsRecord.getValueOf("SocketServerIP");
				CourtOrder_PCWICreate_Log.CourtOrder_PCWICreate_Logger.debug("SocketServerIP: " + socketServerIP);
				socketDetailsMap.put("SocketServerIP", socketServerIP);
				String socketServerPort = xmlParserSocketDetailsRecord.getValueOf("SocketServerPort");
				CourtOrder_PCWICreate_Log.CourtOrder_PCWICreate_Logger.debug("SocketServerPort " + socketServerPort);
				socketDetailsMap.put("SocketServerPort", socketServerPort);
				CourtOrder_PCWICreate_Log.CourtOrder_PCWICreate_Logger.debug("SocketServer Details found.");
			}
		} catch (Exception e) {
			CourtOrder_PCWICreate_Log.CourtOrder_PCWICreate_Logger
					.debug("Exception in getting Socket Connection Details: " + e.getMessage());
		}
		return socketDetailsMap;
	}

	public void doneworkitem(String cabinetName, String sJtsIp, String iJtsPort, String sessionId, String queueID,
			int socketConnectionTimeOut, int integrationWaitTime, HashMap<String, String> socketDetailsMap,
			String processInstanceID, String WorkItemID, String ActivityID, String ProcessDefId, String ActivityType,
			String decisionValue, String ActivityName, HashMap<String, String> CheckGridDataMap) {
		try {
			String getWorkItemInputXML = CommonMethods.getWorkItemInput(cabinetName, sessionId, processInstanceID,
					WorkItemID);
			String getWorkItemOutputXml = WFNGExecute(getWorkItemInputXML, sJtsIp, iJtsPort, 1);
			CourtOrder_PCWICreate_Log.CourtOrder_PCWICreate_Logger
					.debug("Output XML For WmgetWorkItemCall: " + getWorkItemOutputXml);
			XMLParser xmlParserGetWorkItem = new XMLParser(getWorkItemOutputXml);
			String getWorkItemMainCode = xmlParserGetWorkItem.getValueOf("MainCode");
			CourtOrder_PCWICreate_Log.CourtOrder_PCWICreate_Logger
					.debug("WmgetWorkItemCall Maincode:  " + getWorkItemMainCode);
			if (getWorkItemMainCode.trim().equals("0")) {
				CourtOrder_PCWICreate_Log.CourtOrder_PCWICreate_Logger
						.debug("WMgetWorkItemCall Successful: " + getWorkItemMainCode);
				String assignWorkitemAttributeInputXML = "<?xml version=\"1.0\"?><WMAssignWorkItemAttributes_Input>"
						+ "<Option>WMAssignWorkItemAttributes</Option>" + "<EngineName>" + cabinetName + "</EngineName>"
						+ "<SessionId>" + sessionID + "</SessionId>" + "<ProcessInstanceId>" + processInstanceID
						+ "</ProcessInstanceId>" + "<WorkItemId>" + WorkItemID + "</WorkItemId>" + "<ActivityId>"
						+ ActivityID + "</ActivityId>" + "<ProcessDefId>" + ProcessDefId + "</ProcessDefId>"
						+ "<LastModifiedTime></LastModifiedTime>" + "<ActivityType>" + ActivityType + "</ActivityType>"
						+ "<complete>D</complete>" + "<AuditStatus></AuditStatus>" + "<Comments></Comments>"
						+ "<UserDefVarFlag>Y</UserDefVarFlag>" + "<Attributes>" + decisionValue + "</Attributes>"
						+ "</WMAssignWorkItemAttributes_Input>";
				CourtOrder_PCWICreate_Log.CourtOrder_PCWICreate_Logger
						.debug("InputXML for assignWorkitemAttribute Call Notify: " + assignWorkitemAttributeInputXML);
				String assignWorkitemAttributeOutputXML = WFNGExecute(assignWorkitemAttributeInputXML, sJtsIp, iJtsPort,
						1);
				CourtOrder_PCWICreate_Log.CourtOrder_PCWICreate_Logger.debug(
						"OutputXML for assignWorkitemAttribute Call Notify: " + assignWorkitemAttributeOutputXML);
				XMLParser xmlParserWorkitemAttribute = new XMLParser(assignWorkitemAttributeOutputXML);
				String assignWorkitemAttributeMainCode = xmlParserWorkitemAttribute.getValueOf("MainCode");
				CourtOrder_PCWICreate_Log.CourtOrder_PCWICreate_Logger
						.debug("AssignWorkitemAttribute MainCode: " + assignWorkitemAttributeMainCode);
				String decision = CheckGridDataMap.get("Decision");
				if (assignWorkitemAttributeMainCode.trim().equalsIgnoreCase("0")) {
					CourtOrder_PCWICreate_Log.CourtOrder_PCWICreate_Logger
							.debug("AssignWorkitemAttribute Successful: " + assignWorkitemAttributeMainCode);
					System.out.println(processInstanceID + " Completed Succesfully with status " + decision);
					CourtOrder_PCWICreate_Log.CourtOrder_PCWICreate_Logger.debug("WorkItem moved to next Workstep.");
				} else {
					String ErrDesc = "Done WI Failed";
					CourtOrder_PCWICreate_Log.CourtOrder_PCWICreate_Logger.debug("decisionValue : " + decision);
					String return_code = assignWorkitemAttributeMainCode.trim();
				}
				DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
				Date current_date = new Date();
				String formattedEntryDatetime = dateFormat.format(current_date);
				CourtOrder_PCWICreate_Log.CourtOrder_PCWICreate_Logger
						.debug("FormattedEntryDatetime: " + formattedEntryDatetime);
				String entrydatetime = CheckGridDataMap.get("EntryDATETIME");
				Date d1 = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").parse(entrydatetime);
				String entrydatetime_format = dateFormat.format(d1);
				String columnNames = "wi_name,decision_date_time,workstep,user_name,Decision,Remarks,entry_date_time";
				String columnValues = "'" + processInstanceID + "','" + formattedEntryDatetime + "','" + ActivityName
						+ "','" + CommonConnection.getUsername() + "','" + decision + "','','" + entrydatetime_format
						+ "'";
				String apInsertInputXML = CommonMethods.apInsert(cabinetName, sessionID, columnNames, columnValues,
						"NG_COURTORDER_GR_DECISION_HISTORY");
				CourtOrder_PCWICreate_Log.CourtOrder_PCWICreate_Logger.debug("APInsertInputXML: " + apInsertInputXML);
				String apInsertOutputXML = WFNGExecute(apInsertInputXML, sJtsIp, iJtsPort, 1);
				CourtOrder_PCWICreate_Log.CourtOrder_PCWICreate_Logger.debug("APInsertOutputXML: " + apInsertInputXML);
				XMLParser xmlParserAPInsert = new XMLParser(apInsertOutputXML);
				String apInsertMaincode = xmlParserAPInsert.getValueOf("MainCode");
				CourtOrder_PCWICreate_Log.CourtOrder_PCWICreate_Logger
						.debug("Status of apInsertMaincode  " + apInsertMaincode);
				CourtOrder_PCWICreate_Log.CourtOrder_PCWICreate_Logger.debug("Completed On " + ActivityName);
				if (apInsertMaincode.equalsIgnoreCase("0")) {
					CourtOrder_PCWICreate_Log.CourtOrder_PCWICreate_Logger
							.debug("ApInsert successful: " + apInsertMaincode);
					CourtOrder_PCWICreate_Log.CourtOrder_PCWICreate_Logger
							.debug("Inserted in WiHistory table successfully.");
				} else {
					CourtOrder_PCWICreate_Log.CourtOrder_PCWICreate_Logger
							.debug("ApInsert failed: " + apInsertMaincode);
				}
			} else {
				getWorkItemMainCode = "";
				CourtOrder_PCWICreate_Log.CourtOrder_PCWICreate_Logger
						.debug("WmgetWorkItem failed: " + getWorkItemMainCode);
				String ErrDesc = "WI Failed";
			}
		} catch (Exception e) {
			CourtOrder_SystemAutoRemittance obj1 = new CourtOrder_SystemAutoRemittance();
			String exception = obj1.customException(e);
			CourtOrder_PCWICreate_Log.CourtOrder_PCWICreate_Logger.debug("WmgetWorkItem Exception: " + exception);
		}
	}

	public static String get_timestamp() {
		Date present = new Date();
		Format pformatter = new SimpleDateFormat("dd-MM-yyyy-hhmmss");
		TimeStamp = pformatter.format(present);
		return TimeStamp;
	}

	public void insertInErrorTable(String jtsIP, String jtsPort, String columnValues) throws IOException, Exception {
		String columnNames = "wi_name,Integration_failed,Queue_Name,Reason_Failure,MessageId,Unique_identifier";
		String apInsertInputXML = CommonMethods.apInsert(cabinetName, sessionID, columnNames, columnValues,
				"NG_COURTORDER_GR_Error_handling");
		CourtOrder_PCWICreate_Log.CourtOrder_PCWICreate_Logger.debug("APInsertInputXML: " + apInsertInputXML);
		String apInsertOutputXML = CommonMethods.WFNGExecute(apInsertInputXML, jtsIP, jtsPort, 1);
		CourtOrder_PCWICreate_Log.CourtOrder_PCWICreate_Logger.debug("APInsertOutputXML: " + apInsertInputXML);
		XMLParser xmlParserAPInsert = new XMLParser(apInsertOutputXML);
		String apInsertMaincode = xmlParserAPInsert.getValueOf("MainCode");
		CourtOrder_PCWICreate_Log.CourtOrder_PCWICreate_Logger.debug("Status of apInsertMaincode  " + apInsertMaincode);
		if (apInsertMaincode.equalsIgnoreCase("0")) {
			CourtOrder_PCWICreate_Log.CourtOrder_PCWICreate_Logger.debug("ApInsert successful: " + apInsertMaincode);
			CourtOrder_PCWICreate_Log.CourtOrder_PCWICreate_Logger.debug("Inserted in WiHistory table successfully.");
		} else {
			CourtOrder_PCWICreate_Log.CourtOrder_PCWICreate_Logger.debug("ApInsert failed: " + apInsertMaincode);
		}
	}

	private String attachDocwithWI(File file, String ProcessInstanceId, String RequestType, boolean isLastWI,
			String sessionID) {
		String returnStatus = "";
		try {
			CourtOrder_PCWICreate_Log.CourtOrder_PCWICreate_Logger
					.debug("Inside attachDocWI & Processing for Wi: " + ProcessInstanceId);
			CourtOrder_PCWICreate_Log.CourtOrder_PCWICreate_Logger.debug("RequestType is : " + RequestType);

			String inputFolderPath = "", destinationFolderpath = "", errorFolderPath = "", reqFolderName = "";
			reqFolderName = "CIR " + RequestType;
			inputFolderPath = CIR_AttachDoc_INPUT.replaceAll("#FOLDERNAME#", reqFolderName);
			destinationFolderpath = CIR_AttachDoc_OUTPUT.replaceAll("#FOLDERNAME#", reqFolderName);
			errorFolderPath = CIR_AttachDoc_ERROR.replaceAll("#FOLDERNAME#", reqFolderName);
			CourtOrder_PCWICreate_Log.CourtOrder_PCWICreate_Logger.info("Input folderPath is " + inputFolderPath);
			CourtOrder_PCWICreate_Log.CourtOrder_PCWICreate_Logger
					.debug("destinationFolderpath is " + destinationFolderpath);
			CourtOrder_PCWICreate_Log.CourtOrder_PCWICreate_Logger.debug("errorFolderPath is " + errorFolderPath);
			// fetching doc details
			String filepath = file.getAbsolutePath();
			String filename = file.getName();
			CourtOrder_PCWICreate_Log.CourtOrder_PCWICreate_Logger.debug("filepath:" + filepath);
			CourtOrder_PCWICreate_Log.CourtOrder_PCWICreate_Logger.debug("filename:" + filename);
			//
			JPISIsIndex ISINDEX = new JPISIsIndex();
			JPDBRecoverDocData JPISDEC = new JPDBRecoverDocData();
			File file2 = new File(filepath);
			long lLngFileSize = 0L;
			lLngFileSize = file2.length();
			String lstrDocFileSize = Long.toString(lLngFileSize);
			CPISDocumentTxn.AddDocument_MT(null, jtsIP, Short.parseShort(smsPort), cabinetName,
					Short.parseShort(volumeID), filepath, JPISDEC, "", ISINDEX);
			CourtOrder_PCWICreate_Log.CourtOrder_PCWICreate_Logger.debug("After add document MT successful");
			String sISIndex = ISINDEX.m_nDocIndex + "#" + ISINDEX.m_sVolumeId;
			CourtOrder_PCWICreate_Log.CourtOrder_PCWICreate_Logger.debug(" sISIndex: " + sISIndex);
			String DocumentType = "N";
			String strDocumentName = "Central Bank Attachment";
			int filenameWithExtDotIndex = filename.lastIndexOf(".");
			String strDocComment = filename.substring(0, filenameWithExtDotIndex);
			String strExtension = FilenameUtils.getExtension(filepath).trim();
			String strFolderIndex = "";
			// fetching folderIndex
			String strInputQry1 = "SELECT FOLDERINDEX FROM PDBFOLDER WITH(NOLOCK) WHERE " + "NAME = '"
					+ ProcessInstanceId + "'";
			String strInputQry1_IPXML = CommonMethods.apSelectWithColumnNames(strInputQry1,
					CommonConnection.getCabinetName(),
					CommonConnection.getSessionID(CourtOrder_PCWICreate_Log.CourtOrder_PCWICreate_Logger, false));
			CourtOrder_PCWICreate_Log.CourtOrder_PCWICreate_Logger.debug("strInputQry1_IPXML: " + strInputQry1_IPXML);
			String strInputQry1_OPXML = WFNGExecute(strInputQry1_IPXML, CommonConnection.getJTSIP(),
					CommonConnection.getJTSPort(), 1);
			CourtOrder_PCWICreate_Log.CourtOrder_PCWICreate_Logger.debug("strInputQry1_OPXML: " + strInputQry1_OPXML);
			XMLParser xmlParserData_strInputQry1 = new XMLParser(strInputQry1_OPXML);
			int iTotalrec_IndividualCustomer = Integer
					.parseInt(xmlParserData_strInputQry1.getValueOf("TotalRetrieved"));
			if (xmlParserData_strInputQry1.getValueOf("MainCode").equalsIgnoreCase("0")
					&& iTotalrec_IndividualCustomer > 0) {
				String xmlDataExtTab = xmlParserData_strInputQry1.getNextValueOf("Record");
				xmlDataExtTab = xmlDataExtTab.replaceAll("[ ]+>", ">").replaceAll("<[ ]+", "<");
				NGXmlList objWorkList = xmlParserData_strInputQry1.createList("Records", "Record");

				for (; objWorkList.hasMoreElements(true); objWorkList.skip(true)) {
					strFolderIndex = objWorkList.getVal("FOLDERINDEX");
				}
			}
			CourtOrder_PCWICreate_Log.CourtOrder_PCWICreate_Logger.debug("strFolderIndex is : " + strFolderIndex);
			//
			String sMappedInputXml = getNGOAddDocumentIPXML(strFolderIndex, strDocumentName, DocumentType,
					strDocComment, strExtension, sISIndex, lstrDocFileSize, volumeID, cabinetName, sessionID);
			CourtOrder_PCWICreate_Log.CourtOrder_PCWICreate_Logger
					.debug("Input xml For NGOAddDocument Call:" + sMappedInputXml);
			String sOutputXml = WFNGExecute(sMappedInputXml, CommonConnection.getJTSIP(), CommonConnection.getJTSPort(),
					1);
			sOutputXml = sOutputXml.replace("<Document>", "");
			sOutputXml = sOutputXml.replace("</Document>", "");
			CourtOrder_PCWICreate_Log.CourtOrder_PCWICreate_Logger
					.debug("Output xml For NGOAddDocument Call: " + sOutputXml);
			String statusMsg = CommonMethods.getTagValues(sOutputXml, "Status");
			String ErrorMsg = CommonMethods.getTagValues(sOutputXml, "Error");
			String docIndex = "";
			CourtOrder_PCWICreate_Log.CourtOrder_PCWICreate_Logger
					.debug(" The maincode of the output xml file is " + statusMsg);

			if (statusMsg.equalsIgnoreCase("0")) {
				docIndex = CommonMethods.getTagValues(sOutputXml, "DocumentIndex");
				CourtOrder_PCWICreate_Log.CourtOrder_PCWICreate_Logger.debug("Newly added docIndex is: " + docIndex);
				returnStatus = "Success";
				if (isLastWI) {
					// Moving doc
					TimeStamp = get_timestamp();
					String destinationFolderpathDoc = destinationFolderpath + File.separator + TimeStamp + " "
							+ filename;
					CourtOrder_PCWICreate_Log.CourtOrder_PCWICreate_Logger
							.debug("destinationFolderpath after setting timestamp: " + destinationFolderpathDoc);
					try {
						Path returnFileMove = Files.move(Paths.get(filepath), Paths.get(destinationFolderpathDoc));
						if (returnFileMove != null) {
							CourtOrder_PCWICreate_Log.CourtOrder_PCWICreate_Logger
									.debug("Document renamed and moved successfully");
						} else {
							CourtOrder_PCWICreate_Log.CourtOrder_PCWICreate_Logger.debug("Failed to move the document");
							String errorFolderPathDoc = errorFolderPath + File.separator + TimeStamp + " " + filename;
							Path returnFileMoveError = Files.move(Paths.get(filepath), Paths.get(errorFolderPathDoc));
						}
					} catch (Exception e) {
						CourtOrder_PCWICreate_Log.CourtOrder_PCWICreate_Logger
								.debug("exception in Document Movement" + e.getMessage());
					}
				}
			}

			return returnStatus;

		} catch (Exception e) {

			CourtOrder_SystemAutoRemittance obj1 = new CourtOrder_SystemAutoRemittance();
			String exception = obj1.customException(e);
			CourtOrder_PCWICreate_Log.CourtOrder_PCWICreate_Logger.debug("Exception in attachDocWI: " + exception);
			return returnStatus;
		} catch (JPISException e) {
			returnStatus = "Error";
			CourtOrder_PCWICreate_Log.CourtOrder_PCWICreate_Logger
					.debug("JPISException in attachDocWI: " + e.getMessage());
			e.printStackTrace();
			return returnStatus;
		}

	}

	public static String getNGOAddDocumentIPXML(String parentFolderIndex, String strDocumentName, String DocumentType,
			String DocComment, String strExtension, String sISIndex, String lstrDocFileSize, String volumeID,
			String cabinetName, String sessionId) {
		StringBuffer ipXMLBuffer = new StringBuffer();

		ipXMLBuffer.append("<?xml version=\"1.0\"?>\n");
		ipXMLBuffer.append("<NGOAddDocument_Input>\n");
		ipXMLBuffer.append("<Option>NGOAddDocument</Option>");
		ipXMLBuffer.append("<CabinetName>");
		ipXMLBuffer.append(cabinetName);
		ipXMLBuffer.append("</CabinetName>\n");
		ipXMLBuffer.append("<UserDBId>");
		ipXMLBuffer.append(sessionId);
		ipXMLBuffer.append("</UserDBId>\n");
		ipXMLBuffer.append("<GroupIndex>0</GroupIndex>\n");
		ipXMLBuffer.append("<Document>\n");
		ipXMLBuffer.append("<VersionFlag>Y</VersionFlag>\n");
		ipXMLBuffer.append("<ParentFolderIndex>");
		ipXMLBuffer.append(parentFolderIndex);
		ipXMLBuffer.append("</ParentFolderIndex>\n");
		ipXMLBuffer.append("<DocumentName>");
		ipXMLBuffer.append(strDocumentName);
		ipXMLBuffer.append("</DocumentName>\n");
		ipXMLBuffer.append("<Comment>");
		ipXMLBuffer.append(DocComment);
		ipXMLBuffer.append("</Comment>\n");
		ipXMLBuffer.append("<VolumeIndex>");
		ipXMLBuffer.append(volumeID);
		ipXMLBuffer.append("</VolumeIndex>\n");
		ipXMLBuffer.append("<ISIndex>");
		ipXMLBuffer.append(sISIndex);
		ipXMLBuffer.append("</ISIndex>\n");
		ipXMLBuffer.append("<NoOfPages>1</NoOfPages>\n");
		ipXMLBuffer.append("<DocumentType>");
		ipXMLBuffer.append(DocumentType);
		ipXMLBuffer.append("</DocumentType>\n");
		ipXMLBuffer.append("<DocumentSize>");
		ipXMLBuffer.append(lstrDocFileSize);
		ipXMLBuffer.append("</DocumentSize>\n");
		ipXMLBuffer.append("<CreatedByAppName>");
		ipXMLBuffer.append(strExtension);
		ipXMLBuffer.append("</CreatedByAppName>\n");
		ipXMLBuffer.append("</Document>\n");
		ipXMLBuffer.append("</NGOAddDocument_Input>\n");
		return ipXMLBuffer.toString();
	}

	// By sudhanshu rathore
	public String aggressiveTrim(String value) {
		return value == null ? null : value.replaceAll("^[\\p{Z}\\s]+|[\\p{Z}\\s]+$", "");
	}

	private String errorWaitMailTrigger(String filePath, String filename, String typeofMail, String sessionId) {
		String returnStatus = "";
		try {
			CourtOrder_PCWICreate_Log.CourtOrder_PCWICreate_Logger
					.debug("*********Inside errorWaitMailTrigger************");
			CourtOrder_PCWICreate_Log.CourtOrder_PCWICreate_Logger.debug(" typeofMail is : " + typeofMail);

			if ("Error".equalsIgnoreCase(typeofMail)) {
				String docPath = filePath;
				JPISIsIndex ISINDEX = new JPISIsIndex();
				JPDBRecoverDocData JPISDEC = new JPDBRecoverDocData();
				CPISDocumentTxn.AddDocument_MT(null, jtsIP, Short.parseShort(smsPort), cabinetName,
						Short.parseShort(volumeID), docPath, JPISDEC, "", ISINDEX);
				CourtOrder_PCWICreate_Log.CourtOrder_PCWICreate_Logger.debug("After add document mt successful: ");
				String sISIndex = ISINDEX.m_nDocIndex + "#" + ISINDEX.m_sVolumeId;
				CourtOrder_PCWICreate_Log.CourtOrder_PCWICreate_Logger.debug(" sISIndex: " + sISIndex);
				String DocumentType = "N";
				String strDocumentName = "", strExtension = "";
				int indexOfDot = filename.lastIndexOf(".");
				int lenofFileName = filename.length();
				if (lenofFileName > indexOfDot) {
					strDocumentName = filename.substring(0, indexOfDot);
					strExtension = filename.substring(indexOfDot + 1);
				}
				File file = new File(filePath);
				long lLngFileSize = 0L;
				lLngFileSize = file.length();
				String lstrDocFileSize = Long.toString(lLngFileSize);
				String sMappedInputXml = CommonMethods.getNGOAddDocument(CIRBulk_Report_FolderIndex, strDocumentName,
						DocumentType, strExtension, sISIndex, lstrDocFileSize, volumeID, cabinetName, sessionId);
				CourtOrder_PCWICreate_Log.CourtOrder_PCWICreate_Logger
						.debug("Input xml For NGOAddDocument Call: " + sMappedInputXml);
				String sOutputXml = WFNGExecute(sMappedInputXml, CommonConnection.getJTSIP(),
						CommonConnection.getJTSPort(), 1);
				sOutputXml = sOutputXml.replace("<Document>", "");
				sOutputXml = sOutputXml.replace("</Document>", "");
				CourtOrder_PCWICreate_Log.CourtOrder_PCWICreate_Logger
						.debug("Output xml For NGOAddDocument Call: " + sOutputXml);
				String statusXML = CommonMethods.getTagValues(sOutputXml, "Status");
				String ErrorMsg = CommonMethods.getTagValues(sOutputXml, "Error");
				CourtOrder_PCWICreate_Log.CourtOrder_PCWICreate_Logger
						.debug(" The maincode of the output xml file is " + statusXML);

				if (statusXML.equalsIgnoreCase("0")) {
					// fetching doc details from db
					String DBQuery_4 = "Select top 1 ISnull(ImageIndex,'') as ImageIndex,ISnull(concat(NAME,'.',AppName),'') as ATTACHMENTNAMES, volumeId from pdbdocument with (nolock) "
							+ "WHERE DocumentIndex in (select DocumentIndex from PDBDocumentContent where ParentFolderIndex =(select FolderIndex from PDBFolder where Name = '"
							+ CIRBulk_ReportOdFolderName + "')) and" + " name like '" + strDocumentName
							+ "%' order by DocumentIndex desc";
					String extTabDataIPXML_4 = CommonMethods.apSelectWithColumnNames(DBQuery_4,
							CommonConnection.getCabinetName(), CommonConnection
									.getSessionID(CourtOrder_PCWICreate_Log.CourtOrder_PCWICreate_Logger, false));
					CourtOrder_PCWICreate_Log.CourtOrder_PCWICreate_Logger
							.debug("extTabDataIPXML_4: " + extTabDataIPXML_4);
					String extTabDataOPXML_4 = WFNGExecute(extTabDataIPXML_4, CommonConnection.getJTSIP(),
							CommonConnection.getJTSPort(), 1);
					CourtOrder_PCWICreate_Log.CourtOrder_PCWICreate_Logger
							.debug("extTabDataOPXML_4: " + extTabDataOPXML_4);
					XMLParser xmlParserData_4 = new XMLParser(extTabDataOPXML_4);
					int iTotalrec_4 = Integer.parseInt(xmlParserData_4.getValueOf("TotalRetrieved"));
					String ImageIndex = "", ATTACHMENTNAMES = "", volumeId = "";
					if (xmlParserData_4.getValueOf("MainCode").equalsIgnoreCase("0") && iTotalrec_4 > 0) {
						String xmlDataExtTab = xmlParserData_4.getNextValueOf("Record");
						xmlDataExtTab = xmlDataExtTab.replaceAll("[ ]+>", ">").replaceAll("<[ ]+", "<");
						NGXmlList objWorkList4 = xmlParserData_4.createList("Records", "Record");
						for (; objWorkList4.hasMoreElements(true); objWorkList4.skip(true)) {
							ImageIndex = objWorkList4.getVal("ImageIndex");
							ATTACHMENTNAMES = objWorkList4.getVal("ATTACHMENTNAMES");
							volumeId = objWorkList4.getVal("volumeId");
						}
					}
					String wfattachmentNames = "", wfattachmentIndex = "";
					if (!"".equalsIgnoreCase(ATTACHMENTNAMES) && !ATTACHMENTNAMES.isEmpty()) {
						wfattachmentNames = ATTACHMENTNAMES + ";";
					}
					if (!"".equalsIgnoreCase(ImageIndex) && !ImageIndex.isEmpty() && !"".equalsIgnoreCase(volumeId)
							&& !volumeId.isEmpty()) {
						wfattachmentIndex = ImageIndex + "#" + volumeId + "#;";
					}

					CourtOrder_PCWICreate_Log.CourtOrder_PCWICreate_Logger
							.debug("Final wfattachmentNames: " + wfattachmentNames);
					CourtOrder_PCWICreate_Log.CourtOrder_PCWICreate_Logger
							.debug("Final wfattachmentIndex: " + wfattachmentIndex);
					//
					String loggerInMailTable = filename;
					SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd HH:MM:ss");
					String insertedDateTime = simpleDateFormat.format(new Date());
					DateFormat dateFormatnew = new SimpleDateFormat("dd-MM-yyyy");
					String MailSubject = "Action Required: File Received with Faulty Data - [" + filename + "]";
					String FinalMailStr = CIRBulk_ErrorMail_Body.replaceAll("#filename#", filename);
					String columnName = "MAILFROM,MAILTO,MAILSUBJECT,MAILMESSAGE,MAILCONTENTTYPE,MAILPRIORITY,MAILSTATUS,"
							+ "INSERTEDBY,MAILACTIONTYPE,INSERTEDTIME,PROCESSDEFID,PROCESSINSTANCEID,WORKITEMID,ACTIVITYID,"
							+ "NOOFTRIALS,attachmentNames,attachmentISINDEX";
					String strValues = "'" + CIRBulk_Report_FromMail + "','" + CIRBulk_Report_ToMail + "',N'"
							+ MailSubject + "',N'" + FinalMailStr
							+ "','text/html;charset=UTF-8','1','N','CUSTOM','TRIGGER','" + insertedDateTime + "','"
							+ processDefId + "','" + loggerInMailTable + "','1','1','0','" + wfattachmentNames + "','"
							+ wfattachmentIndex + "'";
					String apInsertInputXML = CommonMethods.apInsert(cabinetName, sessionId, columnName, strValues,
							"WFMAILQUEUETABLE");
					CourtOrder_PCWICreate_Log.CourtOrder_PCWICreate_Logger
							.debug("APInsertInputXML mailqueue: " + apInsertInputXML);
					String apInsertOutputXML = CommonMethods.WFNGExecute(apInsertInputXML, jtsIP, jtsPort, 1);
					CourtOrder_PCWICreate_Log.CourtOrder_PCWICreate_Logger
							.debug("APInsertOutputXML mailqueue: " + apInsertOutputXML);
					XMLParser xmlParserAPInsert = new XMLParser(apInsertOutputXML);
					String apInsertMaincode = xmlParserAPInsert.getValueOf("MainCode");
					CourtOrder_PCWICreate_Log.CourtOrder_PCWICreate_Logger
							.debug("Status of apInsertMaincode  " + apInsertMaincode);
					if (apInsertMaincode.equalsIgnoreCase("0")) {
						CourtOrder_PCWICreate_Log.CourtOrder_PCWICreate_Logger
								.debug("ApInsert successful: " + apInsertMaincode);
						CourtOrder_PCWICreate_Log.CourtOrder_PCWICreate_Logger
								.debug("Inserted in WFMAILQUEUE table successfully.");

						// File finalFolder2 = new File(newExcelFilePath);
						// if (finalFolder2.exists()) {
						// File fDumpFolder = new File(newExcelFilePath);
						// fDumpFolder.delete();
						// }
						// update into external table
						returnStatus = "Success";
					} else {
						CourtOrder_PCWICreate_Log.CourtOrder_PCWICreate_Logger
								.debug("ApInsert failed: " + apInsertMaincode);
						returnStatus = "Failure";
					}
				}
			} else if ("Wait".equalsIgnoreCase(typeofMail)) {
				//
				String loggerInMailTable = filename;
				SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd HH:MM:ss");
				String insertedDateTime = simpleDateFormat.format(new Date());
				DateFormat dateFormatnew = new SimpleDateFormat("dd-MM-yyyy");
				String MailSubject = "Action Required: Attachment Not Found For - [" + filename + "]";
				String FinalMailStr = CIRBulk_WaitMail_Body.replaceAll("#filename#", filename);
				String columnName = "MAILFROM,MAILTO,MAILSUBJECT,MAILMESSAGE,MAILCONTENTTYPE,MAILPRIORITY,MAILSTATUS,"
						+ "INSERTEDBY,MAILACTIONTYPE,INSERTEDTIME,PROCESSDEFID,PROCESSINSTANCEID,WORKITEMID,ACTIVITYID,"
						+ "NOOFTRIALS,attachmentNames,attachmentISINDEX";
				String strValues = "'" + CIRBulk_Report_FromMail + "','" + CIRBulk_Report_ToMail + "',N'" + MailSubject
						+ "',N'" + FinalMailStr + "','text/html;charset=UTF-8','1','N','CUSTOM','TRIGGER','"
						+ insertedDateTime + "','" + processDefId + "','" + loggerInMailTable + "','1','1','0','" + ""
						+ "','" + "" + "'";
				String apInsertInputXML = CommonMethods.apInsert(cabinetName, sessionId, columnName, strValues,
						"WFMAILQUEUETABLE");
				CourtOrder_PCWICreate_Log.CourtOrder_PCWICreate_Logger
						.debug("APInsertInputXML mailqueue: " + apInsertInputXML);
				String apInsertOutputXML = CommonMethods.WFNGExecute(apInsertInputXML, jtsIP, jtsPort, 1);
				CourtOrder_PCWICreate_Log.CourtOrder_PCWICreate_Logger
						.debug("APInsertOutputXML mailqueue: " + apInsertOutputXML);
				XMLParser xmlParserAPInsert = new XMLParser(apInsertOutputXML);
				String apInsertMaincode = xmlParserAPInsert.getValueOf("MainCode");
				CourtOrder_PCWICreate_Log.CourtOrder_PCWICreate_Logger
						.debug("Status of apInsertMaincode  " + apInsertMaincode);
				if (apInsertMaincode.equalsIgnoreCase("0")) {
					CourtOrder_PCWICreate_Log.CourtOrder_PCWICreate_Logger
							.debug("ApInsert successful: " + apInsertMaincode);
					CourtOrder_PCWICreate_Log.CourtOrder_PCWICreate_Logger
							.debug("Inserted in WFMAILQUEUE table successfully.");

					returnStatus = "Success";
				} else {
					CourtOrder_PCWICreate_Log.CourtOrder_PCWICreate_Logger
							.debug("ApInsert failed: " + apInsertMaincode);
					returnStatus = "Failure";
				}
			}
			return returnStatus;
		} catch (Exception e) {

			CourtOrder_SystemAutoRemittance obj1 = new CourtOrder_SystemAutoRemittance();
			String exception = obj1.customException(e);
			CourtOrder_PCWICreate_Log.CourtOrder_PCWICreate_Logger
					.debug("Exception in errorWaitMailTrigger: " + exception);
			return "Failure";
		} catch (JPISException e) {
			CourtOrder_PCWICreate_Log.CourtOrder_PCWICreate_Logger.debug("Exception getMessage() 2 :" + e.getMessage());
			return "Failure";
		}
	}

	private void moveAttachToError(String RequestType, String excelreqRefNo) {
		try {
			CourtOrder_PCWICreate_Log.CourtOrder_PCWICreate_Logger
					.debug("Inside moveAttachToError & excelreqRefNo is: " + excelreqRefNo);
			CourtOrder_PCWICreate_Log.CourtOrder_PCWICreate_Logger.debug("RequestType is : " + RequestType);

			String inputFolderPath = "", destinationFolderpath = "", errorFolderPath = "", reqFolderName = "";
			reqFolderName = RequestType;
			inputFolderPath = CIR_AttachDoc_INPUT.replaceAll("#FOLDERNAME#", reqFolderName);
			destinationFolderpath = CIR_AttachDoc_OUTPUT.replaceAll("#FOLDERNAME#", reqFolderName);
			errorFolderPath = CIR_AttachDoc_ERROR.replaceAll("#FOLDERNAME#", reqFolderName);
			CourtOrder_PCWICreate_Log.CourtOrder_PCWICreate_Logger.info("Input folderPath is " + inputFolderPath);
			CourtOrder_PCWICreate_Log.CourtOrder_PCWICreate_Logger
					.debug("destinationFolderpath is " + destinationFolderpath);
			CourtOrder_PCWICreate_Log.CourtOrder_PCWICreate_Logger.debug("errorFolderPath is " + errorFolderPath);
			// fetching doc's
			File folder = new File(inputFolderPath);
			if (folder.exists() && folder.isDirectory()) {

				File[] listOfFiles = folder.listFiles(File::isFile);
				if (listOfFiles != null && listOfFiles.length > 0) {
					for (File file : listOfFiles) {
						String filepath = file.getAbsolutePath();
						String filename = file.getName();
						String Request_RefNo_Doc = "";
						int indexOfDot = filename.lastIndexOf(".");
						String nameWithoutExtension = (indexOfDot != -1) ? filename.substring(0, indexOfDot) : filename;
						int lastDashIndex = nameWithoutExtension.lastIndexOf("-");
						int lenofFileName = filename.length();
						if (lenofFileName > indexOfDot) {
							Request_RefNo_Doc = nameWithoutExtension.substring(lastDashIndex + 1);
						}

						if (excelreqRefNo.equalsIgnoreCase(Request_RefNo_Doc)) {
							CourtOrder_PCWICreate_Log.CourtOrder_PCWICreate_Logger
									.debug("Request_RefNo_Doc:" + Request_RefNo_Doc);
							TimeStamp = get_timestamp();
							String errorFolderpathDoc = errorFolderPath + File.separator + TimeStamp + " " + filename;
							CourtOrder_PCWICreate_Log.CourtOrder_PCWICreate_Logger
									.debug("errorFolderpathDoc after setting timestamp: " + errorFolderpathDoc);
							try {
								Path returnFileMove = Files.move(Paths.get(filepath), Paths.get(errorFolderpathDoc));
								if (returnFileMove != null) {
									CourtOrder_PCWICreate_Log.CourtOrder_PCWICreate_Logger
											.debug("Attachment renamed and moved successfully to error");
								}
							} catch (Exception e) {
								CourtOrder_PCWICreate_Log.CourtOrder_PCWICreate_Logger
										.debug("exception in Attachment Movement" + e.getMessage());
							}
							break;
						} else {
							continue;
						}
					}
				} else {
					CourtOrder_PCWICreate_Log.CourtOrder_PCWICreate_Logger
							.debug("No Doc is present inside for refNo. : " + excelreqRefNo + " inside " + RequestType
									+ " Folder");
				}
			}
		} catch (Exception e) {

			CourtOrder_SystemAutoRemittance obj1 = new CourtOrder_SystemAutoRemittance();
			String exception = obj1.customException(e);
			CourtOrder_PCWICreate_Log.CourtOrder_PCWICreate_Logger.debug("Exception in attachDocWI: " + exception);
		}

	}

	//
	private boolean isRowEmpty(Row row) {
		try {
			// CourtOrder_PCWICreate_Log.CourtOrder_PCWICreate_Logger.debug("Inside
			// isRowEmpty");

			if (row == null) {
				return true;
			}
			for (int x = row.getFirstCellNum(); x < row.getLastCellNum(); x++) {
				Cell cell = row.getCell(x);

				if (cell != null && cell.getCellType() != Cell.CELL_TYPE_BLANK) {
					return false;
				}
			}
			return true;

		} catch (Exception e) {

			CourtOrder_SystemAutoRemittance obj1 = new CourtOrder_SystemAutoRemittance();
			String exception = obj1.customException(e);
			CourtOrder_PCWICreate_Log.CourtOrder_PCWICreate_Logger.debug("Exception in isRowEmpty: " + exception);
			return true;
		}

	}

	private String addInputExcelToOD(String filePath, String filename, String sessionId) {
		String returnStatus = "";
		try {
			CourtOrder_PCWICreate_Log.CourtOrder_PCWICreate_Logger
					.debug("*********Inside addInputExcelToOD************");

			String docPath = filePath;
			JPISIsIndex ISINDEX = new JPISIsIndex();
			JPDBRecoverDocData JPISDEC = new JPDBRecoverDocData();
			CPISDocumentTxn.AddDocument_MT(null, jtsIP, Short.parseShort(smsPort), cabinetName,
					Short.parseShort(volumeID), docPath, JPISDEC, "", ISINDEX);
			CourtOrder_PCWICreate_Log.CourtOrder_PCWICreate_Logger.debug("After add document mt successful: ");
			String sISIndex = ISINDEX.m_nDocIndex + "#" + ISINDEX.m_sVolumeId;
			CourtOrder_PCWICreate_Log.CourtOrder_PCWICreate_Logger.debug(" sISIndex: " + sISIndex);
			String DocumentType = "N";
			String strDocumentName = "", strExtension = "";
			int indexOfDot = filename.lastIndexOf(".");
			int lenofFileName = filename.length();
			if (lenofFileName > indexOfDot) {
				strDocumentName = filename.substring(0, indexOfDot);
				strExtension = filename.substring(indexOfDot + 1);
			}
			File file = new File(filePath);
			long lLngFileSize = 0L;
			lLngFileSize = file.length();
			String lstrDocFileSize = Long.toString(lLngFileSize);
			String sMappedInputXml = CommonMethods.getNGOAddDocument(CIRBulk_Report_FolderIndex, strDocumentName,
					DocumentType, strExtension, sISIndex, lstrDocFileSize, volumeID, cabinetName, sessionId);
			CourtOrder_PCWICreate_Log.CourtOrder_PCWICreate_Logger
					.debug("Input xml For NGOAddDocument Call is: " + sMappedInputXml);
			String sOutputXml = WFNGExecute(sMappedInputXml, CommonConnection.getJTSIP(), CommonConnection.getJTSPort(),
					1);
			sOutputXml = sOutputXml.replace("<Document>", "");
			sOutputXml = sOutputXml.replace("</Document>", "");
			CourtOrder_PCWICreate_Log.CourtOrder_PCWICreate_Logger
					.debug("Output xml For NGOAddDocument Call is: " + sOutputXml);
			String statusXML = CommonMethods.getTagValues(sOutputXml, "Status");
			String ErrorMsg = CommonMethods.getTagValues(sOutputXml, "Error");
			CourtOrder_PCWICreate_Log.CourtOrder_PCWICreate_Logger
					.debug(" The maincode of the output xml file is " + statusXML);

			if (statusXML.equalsIgnoreCase("0")) {
				returnStatus = "Success";
			}
			return returnStatus;
		} catch (Exception e) {

			CourtOrder_SystemAutoRemittance obj1 = new CourtOrder_SystemAutoRemittance();
			String exception = obj1.customException(e);
			CourtOrder_PCWICreate_Log.CourtOrder_PCWICreate_Logger
					.debug("Exception in addInputExcelToOD : " + exception);
			return "Failure";
		} catch (JPISException e) {
			CourtOrder_PCWICreate_Log.CourtOrder_PCWICreate_Logger
					.debug("Exception addInputExcelToOD getMessage() 2 :" + e.getMessage());
			return "Failure";
		}
	}
}