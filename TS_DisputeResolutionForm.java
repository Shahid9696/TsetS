package com.newgen.TS;

import java.io.File;

import com.newgen.CourtOrder.CourtOrder_PCWICreate_Log;
import com.newgen.CourtOrder.CourtOrder_SystemAutoRemittance;

public class TS_DisputeResolutionForm {

	public static String disputefraud_template() {
		File docFile = null;
		boolean isDocPresent = false;
		String CIR_AttachDoc_INPUT="";
		String CIR_AttachDoc_OUTPUT="";
		String CIR_AttachDoc_ERROR="";
		
		
		
			String inputFolderPath = "", destinationFolderpath = "", errorFolderPath = "",
					reqFolderName = "";
			//reqFolderName = "CIR " + reqType;
			inputFolderPath = CIR_AttachDoc_INPUT;
			destinationFolderpath = CIR_AttachDoc_OUTPUT;
			errorFolderPath = CIR_AttachDoc_ERROR;
			TS_System_Integration_Log.TS_Logger
			.debug("Input folderPath is " + inputFolderPath);
			TS_System_Integration_Log.TS_Logger
			.debug("destinationFolderpath is " + destinationFolderpath);
			TS_System_Integration_Log.TS_Logger
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
						TS_System_Integration_Log.TS_Logger
						.debug("filepath:" + filepath);
						TS_System_Integration_Log.TS_Logger
						.debug("filename:" + filename);
						TS_System_Integration_Log.TS_Logger
						.debug("Request_RefNo_Doc:" + Request_RefNo_Doc);
						TS_System_Integration_Log.TS_Logger
						.debug("Request_RefNo of WI is:" + reqRefNo);

						
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

			
		
		return null;
	}
}
