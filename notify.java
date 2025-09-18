private String newDelayedNotify1(String sessionID) {
		try {
			MLLogger.debug("Fetching all Workitems on Initiator_Hold queue");

			String fetchWorkitemListInputXML1 = CommonMethods.fetchWorkItemsInput(cabinetName, sessionID, "1296");// credit
			String fetchWorkitemListInputXML2 = CommonMethods.fetchWorkItemsInput(cabinetName, sessionID, "1299");
			MLLogger.debug("InputXML for fetchWorkList Call:1 " + fetchWorkitemListInputXML1);
			MLLogger.debug("InputXML for fetchWorkList Call:2 " + fetchWorkitemListInputXML2);

			String fetchWorkitemListOutputXML1 = CommonMethods.WFNGExecute(fetchWorkitemListInputXML1, jtsIP, jtsPort,
					1);
			MLLogger.debug("WMFetchWorkList Initiator_reject OutputXML1: " + fetchWorkitemListOutputXML1);

			String fetchWorkitemListOutputXML2 = CommonMethods.WFNGExecute(fetchWorkitemListInputXML2, jtsIP, jtsPort,
					1);
			MLLogger.debug("WMFetchWorkList Initiator_reject OutputXML2: " + fetchWorkitemListOutputXML2);

			XMLParser xmlParserFetchWorkItemlist1 = new XMLParser(fetchWorkitemListOutputXML1);
			XMLParser xmlParserFetchWorkItemlist2 = new XMLParser(fetchWorkitemListOutputXML2);

			String fetchWorkItemListMainCode1 = xmlParserFetchWorkItemlist1.getValueOf("MainCode");
			MLLogger.debug("FetchWorkItemListMainCode1: " + fetchWorkItemListMainCode1);
			String fetchWorkItemListMainCode2 = xmlParserFetchWorkItemlist2.getValueOf("MainCode");
			MLLogger.debug("FetchWorkItemListMainCode2: " + fetchWorkItemListMainCode2);

			int fetchWorkitemListCount1 = Integer.parseInt(xmlParserFetchWorkItemlist1.getValueOf("RetrievedCount"));
			MLLogger.debug("RetrievedCount for WMFetchWorkList Call:1 " + fetchWorkitemListCount1);
			System.out.println("Number of workitems retrieved on Initiator_Hold: " + fetchWorkitemListCount1);
			int fetchWorkitemListCount2 = Integer.parseInt(xmlParserFetchWorkItemlist2.getValueOf("RetrievedCount"));
			MLLogger.debug("RetrievedCount for WMFetchWorkList Call:2 " + fetchWorkitemListCount2);
			System.out.println("Number of workitems retrieved on Initiator_Hold: " + fetchWorkitemListCount2);

			// authToken = getAuthToken(MLLogger);
			// MLLogger.debug("KongAuthToken: " + authToken);

			if (fetchWorkItemListMainCode1.trim().equals("0") && fetchWorkitemListCount1 > 0) {
				for (int i = 0; i < fetchWorkitemListCount1; i++) {
					String fetchWorkItemlistData = xmlParserFetchWorkItemlist1.getNextValueOf("Instrument");
					fetchWorkItemlistData = fetchWorkItemlistData.replaceAll("[ ]+>", ">").replaceAll("<[ ]+", "<");

					MLLogger.debug("Parsing <Instrument> in WMFetchWorkList OutputXML: " + fetchWorkItemlistData);
					XMLParser xmlParserfetchWorkItemData = new XMLParser(fetchWorkItemlistData);

					String processInstanceID = xmlParserfetchWorkItemData.getValueOf("ProcessInstanceId");
					MLLogger.debug("Current ProcessInstanceID: " + processInstanceID);

					String WorkItemID = xmlParserfetchWorkItemData.getValueOf("WorkItemId");
					MLLogger.debug("Current WorkItemID: " + WorkItemID);
					String ActivityName = xmlParserfetchWorkItemData.getValueOf("ActivityName");
					MLLogger.debug("ActivityName: " + ActivityName);

					String ActivityID = xmlParserfetchWorkItemData.getValueOf("WorkStageId");
					MLLogger.debug("ActivityID: " + ActivityID);
					String ActivityType = xmlParserfetchWorkItemData.getValueOf("ActivityType");
					MLLogger.debug("ActivityType: " + ActivityType);

					String query = "SELECT top(1) ACTION_DATE_TIME FROM USR_0_ML_WIHISTORY WHERE WI_NAME='"
							+ processInstanceID + "' order by ENTRY_DATE_TIME asc";
					String descQuery2 = "SELECT PREV_WS FROM RB_ML_EXTTABLE WHERE WI_NAME='" + processInstanceID
							+ "' and REQUEST_FOR in ('IPA - Express','IPA - Credit')";
					String descQuery1 = "SELECT DECISION from USR_0_ML_WIHISTORY WHERE WI_NAME='" + processInstanceID
							+ "' and WORKSTEP ='Credit'";

					String inputXML = CommonMethods.apSelectWithColumnNames(query, cabinetName, sessionID);
					String outputXML = CommonMethods.WFNGExecute(inputXML, jtsIP, jtsPort, 1);
					XMLParser parser = new XMLParser(outputXML);
					MLLogger.debug("outputXML: " + outputXML);
					String mainCodeTime = parser.getValueOf("MainCode");

					String inputXML1 = CommonMethods.apSelectWithColumnNames(descQuery1, cabinetName, sessionID);
					String outputXML1 = CommonMethods.WFNGExecute(inputXML1, jtsIP, jtsPort, 1);
					XMLParser parser1 = new XMLParser(outputXML1);
					MLLogger.debug("outputXML1: " + outputXML1);
					String mainCodeD = parser1.getValueOf("MainCode");

					String inputXML2 = CommonMethods.apSelectWithColumnNames(descQuery2, cabinetName, sessionID);
					String outputXML2 = CommonMethods.WFNGExecute(inputXML2, jtsIP, jtsPort, 1);
					XMLParser parser2 = new XMLParser(outputXML2);
					MLLogger.debug("outputXML2: " + outputXML2);
					String mainCodeP = parser2.getValueOf("MainCode");

					String entryDateTimeStr = xmlParserfetchWorkItemData.getValueOf("EntryDateTime");
					MLLogger.debug("EntryDateTime: " + entryDateTimeStr);

					DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss.SSS");
					LocalDateTime entryDateTime = LocalDateTime.parse(entryDateTimeStr, formatter);
					MLLogger.debug("EntryDateTime: " + entryDateTime);

					if ("0".equalsIgnoreCase(mainCodeTime)) {
						MLLogger.debug("SS-mainCodeTime: " + mainCodeTime);
						int recordCount = Integer.parseInt(parser.getValueOf("TotalRetrieved"));
						MLLogger.debug("SS-recordCount: " + recordCount);
						for (int j = 0; j < recordCount; j++) {
							String actDateTime = (String) parser.getNextValueOf("ACTION_DATE_TIME");
							MLLogger.debug("SS-actDateTime: " + actDateTime);
							DateTimeFormatter format = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss.SSS");
							LocalDateTime finalEntryDateTime = LocalDateTime.parse(actDateTime, format);
							MLLogger.debug("EntryDateTime final: " + finalEntryDateTime);

							LocalDateTime currentTime = LocalDateTime.now();
							String formattedCurrentTime = currentTime.format(format);
							MLLogger.debug("CurrentTime: " + formattedCurrentTime);

							Duration duration = Duration.between(finalEntryDateTime, currentTime);
							long millis = duration.toMillis();
							double diffInDays = millis / (1000.0 * 60 * 60 * 24);
							MLLogger.debug("Difference in days (fractional): " + diffInDays);

							if ("0".equalsIgnoreCase(mainCodeP)) {
								MLLogger.debug("SS-mainCodeP: " + mainCodeP);
								String prevWS = parser2.getNextValueOf("PREV_WS");
								MLLogger.debug("prevWS-prevWS: " + prevWS);
								if ("Introduction".equalsIgnoreCase(prevWS)
										|| "Initiator_Reject".equalsIgnoreCase(prevWS) && (!"".equalsIgnoreCase(prevWS) && null != prevWS)) {
									MLLogger.debug("SS-mainCodeP: " + prevWS);
									if ("0".equalsIgnoreCase(mainCodeD)) {
										MLLogger.debug("SS-mainCodeD: " + mainCodeD);
										int recordCount1 = Integer.parseInt(parser1.getValueOf("TotalRetrieved"));
										for (int k = 0; k < recordCount1; k++) {
											String actDesc = parser1.getNextValueOf("DECISION");
											MLLogger.debug("SS-actDesc: for" + actDesc);
											if (!"Approved".equalsIgnoreCase(actDesc)) {
												MLLogger.debug("SS-actDesc: " + actDesc);
												if (diffInDays >= 14) {
													MLLogger.debug("SS-actDesc:14 " + actDesc);
													// Check if diffInDays is
													// exactly 14 or a multiple
													// of 7 beyond 14
													int intervalsSince14 = (int) ((diffInDays - 14) / 7);
													double triggerPoint = 14 + (intervalsSince14 * 7);

													// Use a small tolerance to
													// handle floating-point
													// rounding issues
													if (Math.abs(diffInDays - triggerPoint) < 0.01) {
														MLLogger.debug("Triggering API at day: " + triggerPoint);

														// Api

														MLLogger.debug("API triggered due to pending decision at "
																+ triggerPoint + " days.");
													}
												}
											}
										}
									}
								}
							}

						}

						/*
						 * for (int j = 0; j < recordCount; j++) { String
						 * folsigned =
						 * parser.getNextValueOf("Final_Offer_Letter_Signed");
						 * 
						 * if (folsigned != null) { long roundedDays =
						 * Math.round(diffInDays);
						 * 
						 * if ("Yes".equalsIgnoreCase(folsigned)) { if
						 * (roundedDays == 30) { MLLogger.debug("30"); } else if
						 * (roundedDays == 60) { MLLogger.debug("60"); } else if
						 * (roundedDays == 90) { MLLogger.debug("90"); }else if
						 * (roundedDays == 91) { // Call DoneWI for 91st day
						 * DoneWI(processInstanceID, WorkItemID, "Reject",
						 * "Remarks", ActivityID, ActivityType,
						 * entryDateTimeStr, "Ws_name", MLLogger); }
						 * 
						 * } else if ("No".equalsIgnoreCase(folsigned)) { if
						 * (roundedDays == 3) { MLLogger.debug("3"); } else if
						 * (roundedDays == 6) { MLLogger.debug("6"); } else if
						 * (roundedDays == 9) { MLLogger.debug("9"); } else if
						 * (roundedDays == 10) { // Call DoneWI for 10th day
						 * DoneWI(processInstanceID, WorkItemID, "Reject",
						 * "Remarks", ActivityID, ActivityType,
						 * entryDateTimeStr, "Ws_name", MLLogger); } } } }
						 */
					}
				}
			}
			if (fetchWorkItemListMainCode2.trim().equals("0") && fetchWorkitemListCount2 > 0) {
				for (int i = 0; i < fetchWorkitemListCount2; i++) {
					String fetchWorkItemlistData = xmlParserFetchWorkItemlist1.getNextValueOf("Instrument");
					fetchWorkItemlistData = fetchWorkItemlistData.replaceAll("[ ]+>", ">").replaceAll("<[ ]+", "<");

					MLLogger.debug("Parsing <Instrument> in WMFetchWorkList OutputXML: " + fetchWorkItemlistData);
					XMLParser xmlParserfetchWorkItemData = new XMLParser(fetchWorkItemlistData);

					String processInstanceID = xmlParserfetchWorkItemData.getValueOf("ProcessInstanceId");
					MLLogger.debug("Current ProcessInstanceID: " + processInstanceID);

					String WorkItemID = xmlParserfetchWorkItemData.getValueOf("WorkItemId");
					MLLogger.debug("Current WorkItemID: " + WorkItemID);
					String ActivityName = xmlParserfetchWorkItemData.getValueOf("ActivityName");
					MLLogger.debug("ActivityName: " + ActivityName);

					String ActivityID = xmlParserfetchWorkItemData.getValueOf("WorkStageId");
					MLLogger.debug("ActivityID: " + ActivityID);
					String ActivityType = xmlParserfetchWorkItemData.getValueOf("ActivityType");
					MLLogger.debug("ActivityType: " + ActivityType);

					String query = "SELECT top(1) ACTION_DATE_TIME FROM USR_0_ML_WIHISTORY WHERE WI_NAME='"
							+ processInstanceID + "' order by ENTRY_DATE_TIME asc";
					String descQuery2 = "SELECT PREV_WS FROM RB_ML_EXTTABLE WHERE WI_NAME='" + processInstanceID
							+ "' and REQUEST_FOR in ('IPA - Express','IPA - Credit')";
					String descQuery1 = "SELECT DECISION from USR_0_ML_WIHISTORY WHERE WI_NAME='" + processInstanceID
							+ "' and WORKSTEP ='Credit'";

					String inputXML = CommonMethods.apSelectWithColumnNames(query, cabinetName, sessionID);
					String outputXML = CommonMethods.WFNGExecute(inputXML, jtsIP, jtsPort, 1);
					XMLParser parser = new XMLParser(outputXML);
					String mainCodeTime = parser.getValueOf("MainCode");

					String inputXML1 = CommonMethods.apSelectWithColumnNames(descQuery1, cabinetName, sessionID);
					String outputXML1 = CommonMethods.WFNGExecute(inputXML1, jtsIP, jtsPort, 1);
					XMLParser parser1 = new XMLParser(outputXML1);
					String mainCodeD = parser1.getValueOf("MainCode");

					String inputXML2 = CommonMethods.apSelectWithColumnNames(descQuery2, cabinetName, sessionID);
					String outputXML2 = CommonMethods.WFNGExecute(inputXML2, jtsIP, jtsPort, 1);
					XMLParser parser2 = new XMLParser(outputXML2);
					String mainCodeP = parser2.getValueOf("MainCode");

					String entryDateTimeStr = xmlParserfetchWorkItemData.getValueOf("EntryDateTime");
					MLLogger.debug("EntryDateTime: " + entryDateTimeStr);

					DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss.SSS");
					LocalDateTime entryDateTime = LocalDateTime.parse(entryDateTimeStr, formatter);
					MLLogger.debug("EntryDateTime: " + entryDateTime);

					if ("0".equalsIgnoreCase(mainCodeTime)) {
						MLLogger.debug("SS-mainCodeTime: " + mainCodeTime);
						int recordCount = Integer.parseInt(parser.getValueOf("TotalRetrieved"));
						for (int j = 0; j < recordCount; j++) {
							String actDateTime = (String) parser.getNextValueOf("ACTION_DATE_TIME");
							MLLogger.debug("SS-actDateTime: " + actDateTime);
							DateTimeFormatter format = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss.SSS");
							LocalDateTime finalEntryDateTime = LocalDateTime.parse(actDateTime, format);
							MLLogger.debug("EntryDateTime final: " + finalEntryDateTime);

							LocalDateTime currentTime = LocalDateTime.now();
							String formattedCurrentTime = currentTime.format(format);
							MLLogger.debug("CurrentTime: " + formattedCurrentTime);

							Duration duration = Duration.between(finalEntryDateTime, currentTime);
							long millis = duration.toMillis();
							double diffInDays = millis / (1000.0 * 60 * 60 * 24);
							MLLogger.debug("Difference in days (fractional): " + diffInDays);

							if ("0".equalsIgnoreCase(mainCodeP)) {
								String prevWS = parser2.getNextValueOf("PREV_WS");
								MLLogger.debug("SS-mainCodeP: " + mainCodeP);
								if ("Credit".equalsIgnoreCase(prevWS)) {
									MLLogger.debug("SS-mainCodeP: " + prevWS);
									if ("0".equalsIgnoreCase(mainCodeD)) {
										MLLogger.debug("SS-mainCodeD: " + mainCodeD);
										int recordCount1 = Integer.parseInt(parser1.getValueOf("TotalRetrieved"));
										for (int k = 0; k < recordCount1; k++) {
											String actDesc = parser1.getNextValueOf("DECISION");
											MLLogger.debug("SS-recordCount1: " + recordCount1);
											MLLogger.debug("SS-actDesc: " + actDesc);
											if (!"Approved".equalsIgnoreCase(actDesc)) {
												MLLogger.debug("SS-actDesc: " + actDesc);
												if (diffInDays >= 14) {
													MLLogger.debug("SS-diffInDays: " + diffInDays);
													// Check if diffInDays is
													// exactly 14 or a multiple
													// of 7 beyond 14
													int intervalsSince14 = (int) ((diffInDays - 14) / 7);
													double triggerPoint = 14 + (intervalsSince14 * 7);

													// Use a small tolerance to
													// handle floating-point
													// rounding issues
													if (Math.abs(diffInDays - triggerPoint) < 0.01) {
														MLLogger.debug("Triggering API at day: " + triggerPoint);

														// Api

														MLLogger.debug("API triggered due to pending decision at "
																+ triggerPoint + " days.");
													}
												}
											}
										}
									}
								}
							}

						}
					}
				}
			}
		} catch (Exception e) {
			MLLogger.debug("Exception: " + e.getMessage());
		}

		return "";
	}