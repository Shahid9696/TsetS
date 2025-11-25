
USE [rakcas]
GO

/****** Object:  StoredProcedure [dbo].[WFTransferData]    Script Date: 11/25/2025 10:30:09 AM ******/
SET ANSI_NULLS ON
GO

SET QUOTED_IDENTIFIER ON
GO

ALTER PROCEDURE [dbo].[WFTransferData](  
--@TransferHistoryData	char(1) = 'N', 
@DBRowsToProcess		INT, /*Its value should be multiple of 100*/
-- @NoOfDays				INT		= 0, -- toChange -> fromDate
@fromDate				DATETIME,
@ProcessDefId           INT
)
AS
BEGIN 
	DECLARE @v_ProcessDefId					INT 
	DECLARE @v_ProcessName					NVARCHAR(30) 
	DECLARE @v_ProcessVersion				SMALLINT 
	DECLARE @v_ProcessInstanceId			NVARCHAR(63) 
	DECLARE @v_ActivityId					INT 
	DECLARE @v_ActivityName					NVARCHAR(30) 
	DECLARE @v_ParentWorkItemId				INT 
	DECLARE @v_WorkItemId					INT 
	DECLARE @v_WorkItemState				INT 
	DECLARE @v_ProcessInstanceState			INT 
	DECLARE @v_StateName					NVARCHAR(50) 
	DECLARE @v_QueueName					NVARCHAR(63) 
	DECLARE @v_QueueType					NVARCHAR(1) 
	DECLARE @v_AssignedUser					NVARCHAR(63) 
	DECLARE @v_AssignmentType				NVARCHAR(1) 
	DECLARE @v_InstrumentStatus				NVARCHAR(1) 
	DECLARE @v_CheckListCompleteFlag		NVARCHAR(1) 
	DECLARE @v_IntroductionDateTime			DATETIME 
	DECLARE @v_CreatedDateTime				DATETIME 
	DECLARE @v_IntroducedBy					NVARCHAR(63) 
	DECLARE @v_CreatedByName				NVARCHAR(63) 
	DECLARE @v_EntryDateTime				DATETIME 
	DECLARE @v_LockStatus					NVARCHAR(1) 
	DECLARE @v_HoldStatus					SMALLINT 
	DECLARE @v_PriorityLevel				SMALLINT 
	DECLARE @v_LockedByName					NVARCHAR(63) 
	DECLARE @v_LockedTime					DATETIME 
	DECLARE @v_ValidTill					DATETIME 
	DECLARE @v_SaveStage					NVARCHAR(30) 
	DECLARE @v_PreviousStage				NVARCHAR(30) 
	DECLARE @v_ExpectedWorkItemDelayTime	DATETIME 
	DECLARE @v_ExpectedProcessDelayTime		DATETIME 
	DECLARE @v_Status						NVARCHAR(50) 
	DECLARE @v_VAR_INT1						SMALLINT 
	DECLARE @v_VAR_INT2						SMALLINT 
	DECLARE @v_VAR_INT3						SMALLINT 
	DECLARE @v_VAR_INT4						SMALLINT 
	DECLARE @v_VAR_INT5						SMALLINT 
	DECLARE @v_VAR_INT6						SMALLINT 
	DECLARE @v_VAR_INT7						SMALLINT 
	DECLARE @v_VAR_INT8						SMALLINT 
	DECLARE @v_VAR_FLOAT1					NUMERIC 
	DECLARE @v_VAR_FLOAT2					NUMERIC 
	DECLARE @v_VAR_DATE1					DATETIME 
	DECLARE @v_VAR_DATE2					DATETIME 
	DECLARE @v_VAR_DATE3					DATETIME 
	DECLARE @v_VAR_DATE4					DATETIME 
	DECLARE @v_VAR_DATE5					DATETIME 
	DECLARE @v_VAR_DATE6					DATETIME 
	DECLARE @v_VAR_LONG1					INT 
	DECLARE @v_VAR_LONG2					INT 
	DECLARE @v_VAR_LONG3					INT 
	DECLARE @v_VAR_LONG4					INT 
	DECLARE @v_VAR_LONG5					INT 
	DECLARE @v_VAR_LONG6					INT 
	DECLARE @v_VAR_STR1						NVARCHAR(255) 
	DECLARE @v_VAR_STR2						NVARCHAR(255) 
	DECLARE @v_VAR_STR3						NVARCHAR(255) 
	DECLARE @v_VAR_STR4						NVARCHAR(255) 
	DECLARE @v_VAR_STR5						NVARCHAR(255) 
	DECLARE @v_VAR_STR6						NVARCHAR(255) 
	DECLARE @v_VAR_STR7						NVARCHAR(255) 
	DECLARE @v_VAR_STR8						NVARCHAR(255) 
	DECLARE @v_VAR_STR9						NVARCHAR(255) 
	DECLARE @v_VAR_STR10					NVARCHAR(255) 
	DECLARE @v_VAR_STR11					NVARCHAR(255) 
	DECLARE @v_VAR_STR12					NVARCHAR(255) 
	DECLARE @v_VAR_STR13					NVARCHAR(255) 
	DECLARE @v_VAR_STR14					NVARCHAR(255) 
	DECLARE @v_VAR_STR15					NVARCHAR(255) 
	DECLARE @v_VAR_STR16					NVARCHAR(255) 
	DECLARE @v_VAR_STR17					NVARCHAR(255) 
	DECLARE @v_VAR_STR18					NVARCHAR(255) 
	DECLARE @v_VAR_STR19					NVARCHAR(255) 
	DECLARE @v_VAR_STR20					NVARCHAR(255) 
	DECLARE @v_VAR_REC_1					NVARCHAR(255) 
	DECLARE @v_VAR_REC_2					NVARCHAR(255) 
	DECLARE @v_VAR_REC_3					NVARCHAR(255) 
	DECLARE @v_VAR_REC_4					NVARCHAR(255) 
	DECLARE @v_VAR_REC_5					NVARCHAR(255) 
	DECLARE @v_Q_StreamId					INT 
	DECLARE @v_Q_QueueId					INT 
	DECLARE @v_Q_UserID						INT 
	DECLARE @v_LastProcessedBy				INT 
	DECLARE @v_ProcessedBy					NVARCHAR(63) 
	DECLARE @v_ReferredTo					INT 
	DECLARE @v_ReferredToName				NVARCHAR(63) 
	DECLARE @v_ReferredBy					INT 
	DECLARE @v_ReferredByName				NVARCHAR(63) 
	DECLARE @v_CollectFlag					NVARCHAR(1) 
	DECLARE @v_newCab						NVARCHAR(2) 
	DECLARE @ExtTableName					NVARCHAR(510) 
	DECLARE @ExtTableName_History           NVARCHAR(510) 
	DECLARE @ExtTableString					NVARCHAR(2000) 
	DECLARE @ExtTableHistoryString          NVARCHAR(2000) 
	DECLARE @pos1				int  
	DECLARE @pos2				int 
	DECLARE @posext1            int 
	DECLARE @posext2            int 
	DECLARE @v_queryStr						VARCHAR(1000) 
	--DECLARE	@v_rowCount						INT 
	DECLARE @row_present					INT 	 
	DECLARE	@v_Count						INT 
	DECLARE @v_rowProcessStr				VARCHAR(200) 
	DECLARE @v_conditionStr				VARCHAR(200) 
	 
	DECLARE @v_ActivityType					SMALLINT  --Added by Kimil for New Column-ActivityType 
	DECLARE @v_lastModifiedTime             DATETIME  --Added by Kimil for New Column-lastModifiedTime 
	DECLARE @v_ChildProcessInstanceId       NVARCHAR(63)--Added by Kimil for New Column-ChildProcessInstanceId 
    DECLARE @v_ChildWorkitemId				INT--Added by Kimil for New Column-ChildWorkitemId 
	DECLARE @v_FilterValue					INT		--Added by Kimil for New Column-FilterValue	 
	DECLARE @v_Guid 						BIGINT  --Added by Kimil for New Column-Guid  
	DECLARE @v_NotifyStatus				NVARCHAR(1)--Added by Kimil for New Column-NotifyStatus 
	DECLARE @v_Q_DivertedByUserId   		INT   --Added by Kimil for New Column-Q_DivertedByUserId 
	DECLARE @v_RoutingStatus				NVARCHAR(1) --Added by Kimil for New Column-RoutingStatus 
	DECLARE @v_NoOfCollectedInstances		INT --Added by Kimil for New Column-NoOfCollectedInstances 
	DECLARE @v_IsPrimaryCollected			NVARCHAR(1)	--Added by Kimil for New Column-IsPrimaryCollected 
	DECLARE	@v_ExportStatus					NVARCHAR(1)--Added by Kimil -ExportStatus 
	DECLARE	@v_ProcessVariantId 			INT --Added by Kimil for New Column-ProcessVariantId 
	DECLARE @TransferHistoryData	char(1) 
	DECLARE @v_Introducedbyid				INT		  
	DECLARE	@v_IntroducedAt				NVARCHAR(30)	 
	DECLARE	@v_Createdby					INT		 
	DECLARE @v_URN						NVARCHAR(63)	 
	SET @TransferHistoryData='N' 
	IF @TransferHistoryData = 'Y'  -- WFS_6.2_044 : Tirupati Srivastava  
	BEGIN 
		SELECT 	@ExtTableString	= ''  
		SELECT 	@ExtTableHistoryString	= '' 
		 
		SET @v_conditionStr = '' 
		IF(@ProcessDefId > 0) 
		BEGIN 
		SET @v_conditionStr = @v_conditionStr + ' AND ProcessDefId ='+ CONVERT(VARCHAR(5),@ProcessDefId) 
		END		 
		EXECUTE('DECLARE ext_cursor CURSOR FAST_FORWARD FOR   
		SELECT PROCESSDEFID, TABLENAME ,HISTORYTABLENAME FROM Extdbconftable (NOLOCK) WHERE EXTOBJID = 1 '+@v_conditionStr) 
 
		OPEN ext_cursor   
		FETCH ext_cursor  
		INTO @v_ProcessDefId, @ExtTableName , @ExtTableName_History 
		 
		/*IF (@@Fetch_Status = -1 OR  @@Fetch_Status = -2)   
		BEGIN 
			CLOSE ext_cursor    
			DEALLOCATE ext_cursor  
		END  */ 
 
		WHILE(@@FETCH_STATUS  <> -1)   
		BEGIN    
			IF(@@FETCH_STATUS <> -2)   
			BEGIN   
				SELECT @ExtTableString = @ExtTableString + LTRIM(RTRIM(@v_ProcessDefId)) + CHAR(21) + LTRIM(RTRIM(@ExtTableName)) + CHAR(25) 
				BEGIN 
					If Exists (Select * from SysObjects Where xType = 'U' and name = UPPER(LTRIM(RTRIM(@ExtTableName)) +'_HISTORY')) 
					SELECT @ExtTableHistoryString = @ExtTableHistoryString + LTRIM(RTRIM(@v_ProcessDefId)) + CHAR(21) + LTRIM(RTRIM(@ExtTableName)) + '_HISTORY' + CHAR(25) 
					 
					IF(@@FETCH_STATUS <> -1 OR @@FETCH_STATUS <> -2)  
					BEGIN 
						SELECT @ExtTableHistoryString = @ExtTableHistoryString + LTRIM(RTRIM(@v_ProcessDefId)) + CHAR(21) + LTRIM(RTRIM(ISNULL(@ExtTableName_History,'')))  + CHAR(25) 
					END 
				END 
			END   
			FETCH NEXT FROM ext_cursor INTO @v_ProcessDefId, @ExtTableName , @ExtTableName_History   
		END   
 
		CLOSE ext_cursor   
		DEALLOCATE ext_cursor   
	END 
	/*insert into atable values('a-'+isnull(@ExtTableString,''))*/ 
/*	SELECT @v_newCab = 'Y' 
	 
	If Exists (SELECT * FROM sysObjects WHERE UPPER(NAME) =UPPER('CurrentRouteLogTable') and xtype='U') 
	BEGIN 
		SET @v_newCab = 'N' 
	END */ 
	SELECT @v_newCab = 'Y' 
	SET @v_Count = 0 
	 
	SET @v_rowProcessStr = '' 
	IF (@DBRowsToProcess > 0 AND @DBRowsToProcess < 100) 
	BEGIN 
		SET @v_rowProcessStr = @v_rowProcessStr + 'TOP ' + CONVERT(VARCHAR,@DBRowsToProcess) 
	END 
	ELSE 
	BEGIN 
		SET @v_rowProcessStr = @v_rowProcessStr + 'TOP 100' 
	END 
	
	SET @v_conditionStr = '' 
	IF(@fromDate < GETDATE())
	BEGIN 
		SET @v_conditionStr = @v_conditionStr + ' AND ENTRYDATETIME < (' + @fromDate +')'
	END		 
		 
		 
	WHILE ((@DBRowsToProcess > 0 AND @v_Count < @DBRowsToProcess) OR (@DBRowsToProcess = 0 AND 1 = 1)) 
	BEGIN 
		SET @row_present = 0 
		EXECUTE('DECLARE v_transferCursor CURSOR FAST_FORWARD FOR 
		SELECT  ' + @v_rowProcessStr + 
				' ProcessDefId, ProcessName, ProcessVersion, ProcessInstanceId, ProcessInstanceId,  
				ActivityId, ActivityName, ParentWorkItemId, WorkItemId, ProcessInstanceState,  
				WorkItemState, StateName, QueueName, QueueType, AssignedUser,  
				AssignmentType, InstrumentStatus, CheckListCompleteFlag, IntroductionDateTime, CreatedDateTime,  
				IntroducedBy, CreatedByName, EntryDateTime, LockStatus, HoldStatus, 
				PriorityLevel, LockedByName, LockedTime, ValidTill, SaveStage,  
				PreviousStage, ExpectedWorkItemDelay, ExpectedProcessDelay, Status, VAR_INT1,  
				VAR_INT2, VAR_INT3,	VAR_INT4, VAR_INT5, VAR_INT6, 
				VAR_INT7, VAR_INT8, VAR_FLOAT1, VAR_FLOAT2, VAR_DATE1, 
				VAR_DATE2, VAR_DATE3, VAR_DATE4, VAR_DATE5, VAR_DATE6, VAR_LONG1,	VAR_LONG2,  
				VAR_LONG3, VAR_LONG4, VAR_LONG5, VAR_LONG6, VAR_STR1,	VAR_STR2, VAR_STR3,  
				VAR_STR4, VAR_STR5, VAR_STR6, VAR_STR7, VAR_STR8, VAR_STR9, VAR_STR10, VAR_STR11, VAR_STR12, VAR_STR13, VAR_STR14, VAR_STR15, VAR_STR16, VAR_STR17, VAR_STR18, VAR_STR19, VAR_STR20,  
				VAR_REC_1, VAR_REC_2, VAR_REC_3, VAR_REC_4, VAR_REC_5,  
				Q_StreamId, Q_QueueId, Q_UserID, LastProcessedBy, ProcessedBy,  
				ReferredTo, ReferredToName, ReferredBy,	ReferredByName, CollectFlag,ActivityType,lastModifiedTime,ChildProcessInstanceId,ChildWorkitemId,FilterValue,Guid,NotifyStatus,Q_DivertedByUserId,RoutingStatus,NoOfCollectedInstances,IsPrimaryCollected,ExportStatus,ProcessVariantId,Introducedbyid,IntroducedAt,Createdby,URN  
		FROM    WFInstrumentTable  with(nolock)
		WHERE   WorkitemId = 1 AND ProcessInstanceState IN (4, 5, 6)' + @v_conditionStr) 
		
		OPEN v_transferCursor 
		FETCH   v_transferCursor  
		INTO    @v_ProcessDefId, @v_ProcessName, @v_ProcessVersion, @v_ProcessInstanceId, @v_ProcessInstanceId,  
				@v_ActivityId, @v_ActivityName, @v_ParentWorkItemId, @v_WorkItemId, @v_ProcessInstanceState, 
				@v_WorkItemState, @v_StateName, @v_QueueName, @v_QueueType, @v_AssignedUser, 
				@v_AssignmentType, @v_InstrumentStatus, @v_CheckListCompleteFlag, @v_IntroductionDateTime, @v_CreatedDateTime,  
				@v_IntroducedBy, @v_CreatedByName, @v_EntryDateTime, @v_LockStatus, @v_HoldStatus, 
				@v_PriorityLevel, @v_LockedByName, @v_LockedTime, @v_ValidTill, @v_SaveStage, 
				@v_PreviousStage, @v_ExpectedWorkItemDelayTime, @v_ExpectedProcessDelayTime, @v_Status,	@v_VAR_INT1,  
				@v_VAR_INT2, @v_VAR_INT3, @v_VAR_INT4,	@v_VAR_INT5, @v_VAR_INT6,  
				@v_VAR_INT7, @v_VAR_INT8, @v_VAR_FLOAT1, @v_VAR_FLOAT2, @v_VAR_DATE1,  
				@v_VAR_DATE2, @v_VAR_DATE3, @v_VAR_DATE4, @v_VAR_DATE5, @v_VAR_DATE6, @v_VAR_LONG1, @v_VAR_LONG2,  
				@v_VAR_LONG3, @v_VAR_LONG4, @v_VAR_LONG5, @v_VAR_LONG6, @v_VAR_STR1, @v_VAR_STR2, @v_VAR_STR3,  
				@v_VAR_STR4, @v_VAR_STR5, @v_VAR_STR6, @v_VAR_STR7, @v_VAR_STR8, @v_VAR_STR9, @v_VAR_STR10, @v_VAR_STR11, @v_VAR_STR12, 
				@v_VAR_STR13, @v_VAR_STR14, @v_VAR_STR15, @v_VAR_STR16, @v_VAR_STR17, @v_VAR_STR18, @v_VAR_STR19, @v_VAR_STR20, 
				@v_VAR_REC_1, @v_VAR_REC_2, @v_VAR_REC_3, @v_VAR_REC_4,	@v_VAR_REC_5,  
				@v_Q_StreamId, @v_Q_QueueId, @v_Q_UserID,	@v_LastProcessedBy, @v_ProcessedBy,  
				@v_ReferredTo, @v_ReferredToName, @v_ReferredBy, @v_ReferredByName, @v_CollectFlag ,@v_ActivityType,@v_lastModifiedTime, 
				@v_ChildProcessInstanceId,@v_ChildWorkitemId,@v_FilterValue,@v_Guid,@v_NotifyStatus,@v_Q_DivertedByUserId,@v_RoutingStatus,@v_NoOfCollectedInstances,@v_IsPrimaryCollected,@v_ExportStatus,@v_ProcessVariantId,@v_Introducedbyid,@v_IntroducedAt,@v_Createdby,@v_URN  
 
		IF (@@FETCH_STATUS = -1 OR @@FETCH_STATUS = -2) 
		BEGIN				 
			CLOSE v_transferCursor 
			DEALLOCATE v_transferCursor 
			RETURN 
		END 
 
		WHILE (@@FETCH_STATUS  <> -1) 
		BEGIN 
			IF (@@FETCH_STATUS <> -2) 
			BEGIN 
				BEGIN TRANSACTION TRANDATA  
					/*IF @v_newCab = 'Y' 
					BEGIN 
						SET @v_queryStr = 'Select 1 from WFCurrentRouteLogTable where ProcessInstanceId = ''' + @v_ProcessInstanceId + ''' AND (ActionId = 20 OR ActionId = 3 OR ActionId = 127)' 
					END 
					ELSE IF @v_newCab = 'N' 
					BEGIN 
						SET @v_queryStr = 'Select 1 from CurrentRouteLogTable where ProcessInstanceId = ''' + @v_ProcessInstanceId + ''' AND (ActionId = 20 OR ActionId = 3 OR ActionId = 127)' 
					END 
					 
					EXECUTE(@v_queryStr) 
					SELECT @v_rowcount = @@ROWCOUNT 
					 
					IF @v_rowcount > 0 
					BEGIN*/ 
					SET @row_present = 1 
					INSERT INTO QueueHistoryTable 
					( 
						ProcessDefId, ProcessName, ProcessVersion, ProcessInstanceId,  
						ProcessInstanceName, ActivityId, ActivityName, ParentWorkItemId,  
						WorkItemId, ProcessInstanceState, WorkItemState, Statename,  
						QueueName, QueueType, AssignedUser, AssignmentType, InstrumentStatus,  
						CheckListCompleteFlag, IntroductionDateTime, CreatedDateTime,  
						IntroducedBy, CreatedByName, EntryDateTime, LockStatus, HoldStatus,  
						PriorityLevel, LockedByName, LockedTime, ValidTill, SaveStage,  
						PreviousStage, ExpectedWorkItemDelayTime, ExpectedProcessDelayTime,  
						Status, VAR_INT1, VAR_INT2, VAR_INT3, VAR_INT4, VAR_INT5, VAR_INT6,  
						VAR_INT7, VAR_INT8, VAR_FLOAT1, VAR_FLOAT2, VAR_DATE1, VAR_DATE2,  
						VAR_DATE3, VAR_DATE4, VAR_DATE5, VAR_DATE6, VAR_LONG1, VAR_LONG2,  
						VAR_LONG3, VAR_LONG4, VAR_LONG5, VAR_LONG6,  
						VAR_STR1, VAR_STR2, VAR_STR3, VAR_STR4, VAR_STR5, VAR_STR6, VAR_STR7,  
						VAR_STR8, VAR_STR9, VAR_STR10, VAR_STR11, VAR_STR12, VAR_STR13, VAR_STR14, 
						VAR_STR15, VAR_STR16, VAR_STR17, VAR_STR18, VAR_STR19, VAR_STR20, VAR_REC_1, VAR_REC_2, VAR_REC_3,  
						VAR_REC_4, VAR_REC_5,  
						Q_StreamId, Q_QueueId, Q_UserID, LastProcessedBy, ProcessedBy, ReferredTo,  
						ReferredToName, ReferredBy, ReferredByName, CollectFlag, CompletionDateTime,ActivityType,lastModifiedTime,ChildProcessInstanceId,ChildWorkitemId,FilterValue,Guid,NotifyStatus,Q_DivertedByUserId,RoutingStatus,NoOfCollectedInstances,IsPrimaryCollected, 
						ExportStatus,ProcessVariantId,Introducedbyid,IntroducedAt,Createdby,URN   
					)    VALUES  ( 
						@v_ProcessDefId, @v_ProcessName, @v_ProcessVersion, @v_ProcessInstanceId, 
						@v_ProcessInstanceId, @v_ActivityId, @v_ActivityName, @v_ParentWorkItemId, 
						@v_WorkItemId, 6, @v_WorkItemState, @v_StateName, @v_QueueName, 
						@v_QueueType, @v_AssignedUser, @v_AssignmentType, @v_InstrumentStatus, 
						@v_CheckListCompleteFlag, @v_IntroductionDateTime, @v_CreatedDateTime, @v_IntroducedBy, 
						@v_CreatedByName, @v_EntryDateTime, @v_LockStatus, @v_HoldStatus, 
						@v_PriorityLevel, @v_LockedByName, @v_LockedTime, @v_ValidTill, 
						@v_SaveStage, @v_PreviousStage, @v_ExpectedWorkItemDelayTime, @v_ExpectedProcessDelayTime, 
						@v_Status, @v_VAR_INT1, @v_VAR_INT2, @v_VAR_INT3, 
						@v_VAR_INT4, @v_VAR_INT5, @v_VAR_INT6, @v_VAR_INT7, 
						@v_VAR_INT8, @v_VAR_FLOAT1, @v_VAR_FLOAT2, @v_VAR_DATE1, 
						@v_VAR_DATE2, @v_VAR_DATE3, @v_VAR_DATE4, @v_VAR_DATE5, @v_VAR_DATE6, @v_VAR_LONG1, 
						@v_VAR_LONG2, @v_VAR_LONG3, @v_VAR_LONG4, @v_VAR_LONG5, @v_VAR_LONG6, @v_VAR_STR1, 
						@v_VAR_STR2, @v_VAR_STR3, @v_VAR_STR4, @v_VAR_STR5, 
						@v_VAR_STR6, @v_VAR_STR7, @v_VAR_STR8, @v_VAR_STR9, @v_VAR_STR10,  
						@v_VAR_STR11, @v_VAR_STR12, @v_VAR_STR13, @v_VAR_STR14, @v_VAR_STR15,  
						@v_VAR_STR16, @v_VAR_STR17, @v_VAR_STR18, @v_VAR_STR19, @v_VAR_STR20, @v_VAR_REC_1, 
						@v_VAR_REC_2, @v_VAR_REC_3, @v_VAR_REC_4, @v_VAR_REC_5, 
						@v_Q_StreamId, @v_Q_QueueId, @v_Q_UserID, @v_LastProcessedBy, 
						@v_ProcessedBy, @v_ReferredTo, @v_ReferredToName, @v_ReferredBy, 
						@v_ReferredByName, @v_CollectFlag, NULL,@v_ActivityType,@v_lastModifiedTime, 
				        @v_ChildProcessInstanceId,@v_ChildWorkitemId,@v_FilterValue,@v_Guid,@v_NotifyStatus,@v_Q_DivertedByUserId,@v_RoutingStatus, 
						@v_NoOfCollectedInstances,@v_IsPrimaryCollected,@v_ExportStatus,@v_ProcessVariantId,@v_Introducedbyid,@v_IntroducedAt,@v_Createdby,@v_URN 
					) 
 
					IF (@@ERROR <> 0) 
					BEGIN 
						ROLLBACK TRANSACTION TRANDATA 
						INSERT INTO FailureLogTable VALUES (0, @v_ProcessInstanceId) 
						CLOSE v_transferCursor 
						DEALLOCATE v_transferCursor 
						Return 
					END 
					 
					DELETE FROM WFInstrumentTable WHERE ProcessInstanceId = @v_ProcessInstanceId 
					IF (@@ERROR <> 0) 
					BEGIN 
						ROLLBACK TRANSACTION TRANDATA 
						INSERT INTO FailureLogTable VALUES (0, @v_ProcessInstanceId) 
						CLOSE v_transferCursor 
						DEALLOCATE v_transferCursor 
						Return 
					END 
					--PRINT 'Queuhistory Insert Succesfull' 
					INSERT INTO EXCEPTIONHISTORYTABLE 
						SELECT ProcessDefId, ExcpSeqId, WorkitemId, Activityid, ActivityName,  
							ProcessInstanceId, UserId, UserName, ActionId, ActionDateTime, ExceptionId, 
							ExceptionName, FinalizationStatus, ExceptionComments 
						FROM EXCEPTIONTABLE with(nolock)
						WHERE EXCEPTIONTABLE.ProcessInstanceId = @v_ProcessInstanceId 
					IF (@@ERROR <> 0) 
					BEGIN 
						ROLLBACK TRANSACTION TRANDATA 
						INSERT INTO FailureLogTable VALUES (0, @v_ProcessInstanceId) 
						CLOSE v_transferCursor 
						DEALLOCATE v_transferCursor 
						Return 
					END 
					 
					DELETE from EXCEPTIONTABLE 
					where  
					ProcessInstanceId = @v_ProcessInstanceId 
					IF (@@ERROR <> 0) 
					BEGIN 
						ROLLBACK TRANSACTION TRANDATA 
						INSERT INTO FailureLogTable VALUES (0, @v_ProcessInstanceId) 
						CLOSE v_transferCursor 
						DEALLOCATE v_transferCursor 
						Return 
					END 
					--PRINT 'EXCEPTIONHISTORYTABLE Insert Succesfull' 
					INSERT INTO TODOSTATUSHISTORYTABLE 
						SELECT ProcessDefId, ProcessInstanceId, ToDoValue 
						FROM TODOSTATUSTABLE with(nolock)
						WHERE TODOSTATUSTABLE.ProcessInstanceId = @v_ProcessInstanceId 
					IF (@@ERROR <> 0) 
					BEGIN 
						ROLLBACK TRANSACTION TRANDATA 
						INSERT INTO FailureLogTable VALUES (0, @v_ProcessInstanceId) 
						CLOSE v_transferCursor 
						DEALLOCATE v_transferCursor 
						Return 
					END 
					 
					DELETE FROM TODOSTATUSTABLE	WHERE ProcessInstanceId = @v_ProcessInstanceId 
					IF (@@ERROR <> 0) 
					BEGIN 
						ROLLBACK TRANSACTION TRANDATA 
						INSERT INTO FailureLogTable VALUES (0, @v_ProcessInstanceId) 
						CLOSE v_transferCursor 
						DEALLOCATE v_transferCursor 
						Return 
					END 
					--PRINT 'TODOSTATUSHISTORYTABLE Insert Succesfull' 
					INSERT INTO WFREPORTDATAHISTORYTABLE 
						SELECT ProcessInstanceId, WorkitemId, ProcessDefId, Activityid, UserId,  
						TotalProcessingTime,ProcessVariantId 
						FROM WFREPORTDATATABLE with(nolock)
						WHERE WFREPORTDATATABLE.ProcessInstanceId = @v_ProcessInstanceId 
					IF (@@ERROR <> 0) 
					BEGIN 
						ROLLBACK TRANSACTION TRANDATA 
						INSERT INTO FailureLogTable VALUES (0, @v_ProcessInstanceId) 
						CLOSE v_transferCursor 
						DEALLOCATE v_transferCursor 
						Return 
					END 
					 
					DELETE from WFREPORTDATATABLE 
					where  
					ProcessInstanceId = @v_ProcessInstanceId 
					IF (@@ERROR <> 0) 
					BEGIN 
						ROLLBACK TRANSACTION TRANDATA 
						INSERT INTO FailureLogTable VALUES (0, @v_ProcessInstanceId) 
						CLOSE v_transferCursor 
						DEALLOCATE v_transferCursor 
						Return 
					END 
					--PRINT 'WFREPORTDATAHISTORYTABLE Insert Succesfull' 
					IF(@v_newCab = 'Y') 
					BEGIN 
						INSERT INTO WFHistoryRouteLogTable SELECT LogId, ProcessDefId, ActivityId, ProcessInstanceId,  
							WorkItemId, UserId, ActionId, ActionDateTime, AssociatedFieldId,  
							AssociatedFieldName, ActivityName, UserName,NewValue,AssociatedDateTime,QueueID,ProcessVariantId,TaskId,SubTaskId,URN	 
						FROM WFCurrentRouteLogTable with(nolock) WHERE ProcessInstanceId = @v_ProcessInstanceId 
						IF (@@ERROR <> 0) 
						BEGIN 
							ROLLBACK TRANSACTION TRANDATA 
							INSERT INTO FailureLogTable VALUES (0, @v_ProcessInstanceId) 
							CLOSE v_transferCursor 
						DEALLOCATE v_transferCursor 
						Return 
						END 
					 
						DELETE FROM WFCurrentRouteLogTable WHERE ProcessInstanceId = @v_ProcessInstanceId 
						IF (@@ERROR <> 0) 
						BEGIN 
							ROLLBACK TRANSACTION TRANDATA 
							INSERT INTO FailureLogTable VALUES (0, @v_ProcessInstanceId) 
							CLOSE v_transferCursor 
						DEALLOCATE v_transferCursor 
						Return 
						END 
					END 
 
					--PRINT 'WFHistoryRouteLogTable Insert Succesfull' 
				/*	ELSE 
					BEGIN 
						INSERT INTO HistoryRouteLogTable SELECT LogId, ProcessDefId, ActivityId, ProcessInstanceId,  
							WorkItemId, UserId, ActionId, ActionDateTime, AssociatedFieldId,  
							AssociatedFieldName, ActivityName, UserName	 
						FROM CurrentRouteLogTable WHERE ProcessInstanceId = @v_ProcessInstanceId 
						IF (@@ERROR <> 0) 
						BEGIN 
							ROLLBACK TRANSACTION TRANDATA 
							INSERT INTO FailureLogTable VALUES (0, @v_ProcessInstanceId) 
							GOTO ProcessNext 
						END 
					 
						DELETE FROM CurrentRouteLogTable WHERE ProcessInstanceId = @v_ProcessInstanceId 
						IF (@@ERROR <> 0) 
						BEGIN 
							ROLLBACK TRANSACTION TRANDATA 
							INSERT INTO FailureLogTable VALUES (0, @v_ProcessInstanceId) 
							GOTO ProcessNext 
						END 
					END*/ 
						IF @TransferHistoryData = 'Y'	-- WFS_6.2_044 : Tirupati Srivastava  
						BEGIN 
							SELECT @pos1 = CHARINDEX(CONVERT(Nvarchar(512), @v_ProcessDefId), @ExtTableString)  
							SELECT @posext1 = CHARINDEX(CONVERT(Nvarchar(512), @v_ProcessDefId), @ExtTableHistoryString) 
 
							IF @pos1 <> 0 
							BEGIN 
								SELECT @pos1 = CHARINDEX(CHAR(21), @ExtTableString, @pos1)   
								SELECT @pos2 = CHARINDEX(CHAR(25), @ExtTableString, @pos1)   
 
								SELECT @ExtTableName = RTRIM(SUBSTRING(@ExtTableString, @pos1+1, (@pos2-@pos1)-1)) 
								SELECT @posext1 = CHARINDEX(CONVERT(Nvarchar(512), @v_ProcessDefId), @ExtTableHistoryString) 
								IF(@posext1 <> 0) 
								BEGIN 
									SELECT @posext1 = CHARINDEX(CHAR(21), @ExtTableHistoryString, @posext1)  
									SELECT @posext2 = CHARINDEX(CHAR(25), @ExtTableHistoryString, @posext1)  
									SELECT @ExtTableName_History = RTRIM(SUBSTRING(@ExtTableHistoryString, @posext1+1, (@posext2-@posext1)-1)) 
								END 
								IF Exists (SELECT * FROM SysObjects WHERE name = @ExtTableName_History)   
								BEGIN   
									IF (@v_VAR_REC_1 IS NOT NULL AND @v_VAR_REC_1 > '0')   
									BEGIN   
										SELECT @v_VAR_REC_1 = CHAR(39) + @v_VAR_REC_1 + CHAR(39) 
										SELECT @v_VAR_REC_2 = CHAR(39) + @v_VAR_REC_2 + CHAR(39) 
 
										EXECUTE('INSERT INTO '+@ExtTableName_History+' SELECT * from '+@ExtTableName+' (NOLOCK)'+ 
											' WHERE ITEMINDEX = N' + @v_VAR_REC_1 + ' AND ITEMTYPE = N' + @v_VAR_REC_2 )   
										IF @@ERROR <> 0   
										BEGIN   
											ROLLBACK TRANSACTION TRANDATA   
											INSERT INTO FailureLogTable Values (0, @v_ProcessInstanceId) 
											CLOSE v_transferCursor 
											DEALLOCATE v_transferCursor 
											Return 
										END   
 
										EXECUTE('DELETE FROM '+@ExtTableName+' WHERE ItemIndex = N'+@v_VAR_REC_1+' AND ITEMTYPE = N'+@v_VAR_REC_2) 
										IF @@ERROR <> 0   
										BEGIN	  
											ROLLBACK TRANSACTION TRANDATA			 
											INSERT INTO FailureLogTable Values (0, @v_ProcessInstanceId)		 
											CLOSE v_transferCursor 
											DEALLOCATE v_transferCursor 
											Return  
										END   
									END  						 
								END   
							END 
					END	 
					--PRINT '@ExtTableName Insert Succesfull'+@ExtTableName 
					BEGIN 
						 
						if(columnproperty(object_id('WFCommentsHistoryTable'),'CommentsId','IsIdentity') = 1) 
						BEGIN							 
							SET IDENTITY_INSERT WFCommentsHistoryTable ON								 
						END 
						 
						INSERT INTO WFCommentsHistoryTable(CommentsId,ProcessDefId, ActivityId, ProcessInstanceId, WorkItemId, CommentsBy, CommentsByName, CommentsTo, CommentsToName, Comments, ActionDateTime, CommentsType,ProcessVariantId ) SELECT CommentsId,ProcessDefId, ActivityId, ProcessInstanceId, WorkItemId, CommentsBy, CommentsByName, CommentsTo, CommentsToName, Comments, ActionDateTime, CommentsType,ProcessVariantId FROM WFCommentsTable WHERE ProcessInstanceId = @v_ProcessInstanceId						 
						if(columnproperty(object_id('WFCommentsHistoryTable'),'CommentsId','IsIdentity') = 1) 
						BEGIN 
							SET IDENTITY_INSERT WFCommentsHistoryTable OFF 
						END 
						 
						IF (@@ERROR <> 0) 
						BEGIN 
							ROLLBACK TRANSACTION TRANDATA 
							INSERT INTO FailureLogTable VALUES (0, @v_ProcessInstanceId) 
							CLOSE v_transferCursor 
							DEALLOCATE v_transferCursor 
							Return 
						END 
					 
						DELETE FROM WFCommentsTable WHERE ProcessInstanceId = @v_ProcessInstanceId 
						IF (@@ERROR <> 0) 
						BEGIN 
							ROLLBACK TRANSACTION TRANDATA 
							INSERT INTO FailureLogTable VALUES (0, @v_ProcessInstanceId) 
							CLOSE v_transferCursor 
							DEALLOCATE v_transferCursor 
							Return 
						END 
					END 
					--PRINT 'WFCommentsHistoryTable Insert Succesfull' 
					--Case Management TABLES 
					BEGIN 
						INSERT INTO WFtaskstatusHistorytable SELECT ProcessInstanceId,WorkItemId,ProcessDefId,ActivityId,TaskId,SubTaskId,TaskStatus,AssignedBy,AssignedTo,Instructions,ActionDateTime,DueDate,Priority,ShowCaseVisual,ReadFlag,CanInitiate,Q_DivertedByUserId,LockStatus,InitiatedBy,TaskEntryDateTime,ValidTill,ApprovalRequired,ApprovalSentBy,	AllowReassignment,AllowDecline,EscalatedFlag FROM WFtaskstatustable WHERE ProcessInstanceId = @v_ProcessInstanceId 
						IF (@@ERROR <> 0) 
						BEGIN 
							ROLLBACK TRANSACTION TRANDATA 
							INSERT INTO FailureLogTable VALUES (0, @v_ProcessInstanceId) 
							CLOSE v_transferCursor 
							DEALLOCATE v_transferCursor 
							Return 
						END 
					 
						DELETE FROM WFtaskstatustable WHERE ProcessInstanceId = @v_ProcessInstanceId 
						IF (@@ERROR <> 0) 
						BEGIN 
							ROLLBACK TRANSACTION TRANDATA 
							INSERT INTO FailureLogTable VALUES (0, @v_ProcessInstanceId) 
							CLOSE v_transferCursor 
							DEALLOCATE v_transferCursor 
							Return 
						END 
					END 
					--PRINT 'WFtaskstatusHistorytable Insert Succesfull' 
					BEGIN 
						INSERT INTO WFRTTASKINTFCASSOCHISTORY SELECT ProcessInstanceId,WorkItemId,ProcessDefId,ActivityId,TaskId,InterfaceId,InterfaceType,Attribute FROM WFRTTaskInterfaceAssocTable WHERE ProcessInstanceId = @v_ProcessInstanceId 
						IF (@@ERROR <> 0) 
						BEGIN 
							ROLLBACK TRANSACTION TRANDATA 
							INSERT INTO FailureLogTable VALUES (0, @v_ProcessInstanceId) 
							CLOSE v_transferCursor 
							DEALLOCATE v_transferCursor 
							Return 
						END 
					 
						DELETE FROM WFRTTaskInterfaceAssocTable WHERE ProcessInstanceId = @v_ProcessInstanceId 
						IF (@@ERROR <> 0) 
						BEGIN 
							ROLLBACK TRANSACTION TRANDATA 
							INSERT INTO FailureLogTable VALUES (0, @v_ProcessInstanceId) 
							CLOSE v_transferCursor 
							DEALLOCATE v_transferCursor 
							Return 
						END 
					END 
					--PRINT 'WFRTTASKINTFCASSOCHISTORY Insert Succesfull' 
					BEGIN 
						INSERT INTO RTACTIVITYINTFCASSOCHISTORY SELECT ProcessInstanceId,WorkItemId,ProcessDefId,ActivityId,ActivityName,InterfaceElementId,InterfaceType,Attribute,TriggerName,ProcessVariantId FROM RTACTIVITYINTERFACEASSOCTABLE WHERE ProcessInstanceId = @v_ProcessInstanceId 
						IF (@@ERROR <> 0) 
						BEGIN 
							ROLLBACK TRANSACTION TRANDATA 
							INSERT INTO FailureLogTable VALUES (0, @v_ProcessInstanceId) 
							CLOSE v_transferCursor 
							DEALLOCATE v_transferCursor 
							Return 
						END 
					 
						DELETE FROM RTACTIVITYINTERFACEASSOCTABLE WHERE ProcessInstanceId = @v_ProcessInstanceId 
						IF (@@ERROR <> 0) 
						BEGIN 
							ROLLBACK TRANSACTION TRANDATA 
							INSERT INTO FailureLogTable VALUES (0, @v_ProcessInstanceId) 
							CLOSE v_transferCursor 
							DEALLOCATE v_transferCursor 
							Return 
						END 
					END 
					--PRINT 'RTACTIVITYINTFCASSOCHISTORY Insert Succesfull' 
				BEGIN 
					INSERT INTO WFATTRIBUTEMESSAGEHISTORYTABLE(ProcessDefId,ProcessVariantId, ProcessInstanceId, WorkItemId,MessageId,Message,LockedBy,Status,ActionDateTime) SELECT  ProcessDefId,ProcessVariantId, ProcessInstanceId, WorkItemId,MessageId,Message,LockedBy,Status,ActionDateTime FROM WFATTRIBUTEMESSAGETABLE WHERE ProcessInstanceId = @v_ProcessInstanceId 
					IF (@@ERROR <> 0) 
						BEGIN 
							ROLLBACK TRANSACTION TRANDATA 
							INSERT INTO FailureLogTable VALUES (0, @v_ProcessInstanceId) 
							CLOSE v_transferCursor 
							DEALLOCATE v_transferCursor 
							Return 
						END 
						 
					DELETE FROM WFATTRIBUTEMESSAGETABLE WHERE ProcessInstanceId = @v_ProcessInstanceId	 
					IF (@@ERROR <> 0) 
						BEGIN 
							ROLLBACK TRANSACTION TRANDATA 
							INSERT INTO FailureLogTable VALUES (0, @v_ProcessInstanceId) 
							CLOSE v_transferCursor 
							DEALLOCATE v_transferCursor 
							Return 
						END 
				END; 
				 
				--PRINT 'WFATTRIBUTEMESSAGEHISTORYTABLE Insert Succesfull' 
				--IBPS 3.2 release changes start 
				BEGIN 
					INSERT INTO WFCaseSummaryDetailsHistory(ProcessInstanceId,WorkItemId, ProcessDefId, ActivityId,ActivityName,Status,NoOfRetries,EntryDateTime,LockedBy) SELECT  ProcessInstanceId,WorkItemId, ProcessDefId, ActivityId,ActivityName,Status,NoOfRetries,EntryDateTime,LockedBy FROM WFCaseSummaryDetailsTable WHERE ProcessInstanceId = @v_ProcessInstanceId  
					 
					IF (@@ERROR <> 0) 
						BEGIN 
							ROLLBACK TRANSACTION TRANDATA 
							INSERT INTO FailureLogTable VALUES (0, @v_ProcessInstanceId) 
							CLOSE v_transferCursor 
							DEALLOCATE v_transferCursor 
							Return 
						END 
						 
					DELETE FROM WFCaseSummaryDetailsTable  WHERE ProcessInstanceId = @v_ProcessInstanceId  
					IF (@@ERROR <> 0) 
						BEGIN 
							ROLLBACK TRANSACTION TRANDATA 
							INSERT INTO FailureLogTable VALUES (0, @v_ProcessInstanceId) 
							CLOSE v_transferCursor 
							DEALLOCATE v_transferCursor 
							Return 
						END 
				END; 
				 
				--PRINT 'WFCaseSummaryDetailsHistory Insert Succesfull' 
				BEGIN 
					INSERT INTO WFCaseDocStatusHistory(ProcessInstanceId,WorkItemId, ProcessDefId, ActivityId,TaskId,SubTaskId,DocumentType,DocumentIndex,ISIndex,CompleteStatus) SELECT  ProcessInstanceId,WorkItemId, ProcessDefId, ActivityId,TaskId,SubTaskId,DocumentType,DocumentIndex,ISIndex,CompleteStatus FROM WFCaseDocStatusTable WHERE ProcessInstanceId = @v_ProcessInstanceId  
					IF (@@ERROR <> 0) 
						BEGIN 
							ROLLBACK TRANSACTION TRANDATA 
							INSERT INTO FailureLogTable VALUES (0, @v_ProcessInstanceId) 
							CLOSE v_transferCursor 
							DEALLOCATE v_transferCursor 
							Return 
						END 
						 
					DELETE FROM WFCaseDocStatusTable WHERE ProcessInstanceId = @v_ProcessInstanceId	 
					IF (@@ERROR <> 0) 
						BEGIN 
							ROLLBACK TRANSACTION TRANDATA 
							INSERT INTO FailureLogTable VALUES (0, @v_ProcessInstanceId) 
							CLOSE v_transferCursor 
							DEALLOCATE v_transferCursor 
							Return 
						END 
				END; 
				 
				--PRINT 'WFCaseDocStatusHistory Insert Succesfull' 
				BEGIN 
					INSERT INTO WFTaskPreCondResultHistory(ProcessInstanceId,WorkItemId, ActivityId, TaskId,Ready,Mandatory) SELECT  ProcessInstanceId,WorkItemId, ActivityId, TaskId,Ready,Mandatory FROM WFTaskPreConditionResultTable WHERE ProcessInstanceId = @v_ProcessInstanceId 
					IF (@@ERROR <> 0) 
						BEGIN 
							ROLLBACK TRANSACTION TRANDATA 
							INSERT INTO FailureLogTable VALUES (0, @v_ProcessInstanceId) 
							CLOSE v_transferCursor 
							DEALLOCATE v_transferCursor 
							Return 
						END 
						 
					DELETE FROM WFTaskPreConditionResultTable WHERE ProcessInstanceId = @v_ProcessInstanceId	 
					IF (@@ERROR <> 0) 
						BEGIN 
							ROLLBACK TRANSACTION TRANDATA 
							INSERT INTO FailureLogTable VALUES (0, @v_ProcessInstanceId) 
							CLOSE v_transferCursor 
							DEALLOCATE v_transferCursor 
							Return 
						END 
				END; 
				 
				--PRINT 'WFTaskPreCondResultHistory Insert Succesfull' 
				BEGIN 
					INSERT INTO WFTaskPreCheckHistory(ProcessInstanceId, WorkItemId,ActivityId,checkPreCondition) SELECT  ProcessInstanceId, WorkItemId,ActivityId,checkPreCondition FROM WFTaskPreCheckTable WHERE ProcessInstanceId = @v_ProcessInstanceId 
					IF (@@ERROR <> 0) 
						BEGIN 
							ROLLBACK TRANSACTION TRANDATA 
							INSERT INTO FailureLogTable VALUES (0, @v_ProcessInstanceId) 
							CLOSE v_transferCursor 
							DEALLOCATE v_transferCursor 
							Return 
						END 
						 
					DELETE FROM WFTaskPreCheckTable WHERE ProcessInstanceId = @v_ProcessInstanceId	 
					IF (@@ERROR <> 0) 
						BEGIN 
							ROLLBACK TRANSACTION TRANDATA 
							INSERT INTO FailureLogTable VALUES (0, @v_ProcessInstanceId) 
							CLOSE v_transferCursor 
							DEALLOCATE v_transferCursor 
							Return 
						END 
				END; 
				 
				--PRINT 'WFTaskPreCheckHistory Insert Succesfull' 
				--IBPS 3.2 release changes end 
				INSERT INTO SuccessLogTable VALUES (0, @v_ProcessInstanceId) 
					SET @v_Count = @v_Count + 1 
					END 
				COMMIT TRANSACTION TRANDATA 
			--END 
			--PRINT 'WFRTTaskInterfaceAssocHistoryTable Insert Succesfull' 
			ProcessNext: 
			FETCH NEXT FROM v_transferCursor INTO     
				@v_ProcessDefId, @v_ProcessName, @v_ProcessVersion, @v_ProcessInstanceId, @v_ProcessInstanceId,  
				@v_ActivityId, @v_ActivityName, @v_ParentWorkItemId, @v_WorkItemId, @v_ProcessInstanceState, 
				@v_WorkItemState, @v_StateName, @v_QueueName, @v_QueueType, @v_AssignedUser, 
				@v_AssignmentType, @v_InstrumentStatus, @v_CheckListCompleteFlag, @v_IntroductionDateTime, @v_CreatedDateTime,  
				@v_IntroducedBy, @v_CreatedByName, @v_EntryDateTime, @v_LockStatus, @v_HoldStatus, 
				@v_PriorityLevel, @v_LockedByName, @v_LockedTime, @v_ValidTill, @v_SaveStage, 
				@v_PreviousStage, @v_ExpectedWorkItemDelayTime, @v_ExpectedProcessDelayTime, @v_Status,	@v_VAR_INT1,  
				@v_VAR_INT2, @v_VAR_INT3, @v_VAR_INT4,	@v_VAR_INT5, @v_VAR_INT6,  
				@v_VAR_INT7, @v_VAR_INT8, @v_VAR_FLOAT1, @v_VAR_FLOAT2, @v_VAR_DATE1,  
				@v_VAR_DATE2, @v_VAR_DATE3, @v_VAR_DATE4, @v_VAR_DATE5, @v_VAR_DATE6, @v_VAR_LONG1, @v_VAR_LONG2,  
				@v_VAR_LONG3, @v_VAR_LONG4, @v_VAR_LONG5, @v_VAR_LONG6, @v_VAR_STR1, @v_VAR_STR2, @v_VAR_STR3,  
				@v_VAR_STR4, @v_VAR_STR5, @v_VAR_STR6, @v_VAR_STR7, @v_VAR_STR8, @v_VAR_STR9, 
		        @v_VAR_STR10, @v_VAR_STR11, @v_VAR_STR12, @v_VAR_STR13, @v_VAR_STR14,  
				@v_VAR_STR15, @v_VAR_STR16, @v_VAR_STR17, @v_VAR_STR18, @v_VAR_STR19, @v_VAR_STR20,		 
				@v_VAR_REC_1, @v_VAR_REC_2, @v_VAR_REC_3, @v_VAR_REC_4,	@v_VAR_REC_5,  
				@v_Q_StreamId, @v_Q_QueueId, @v_Q_UserID,	@v_LastProcessedBy, @v_ProcessedBy,  
				@v_ReferredTo, @v_ReferredToName, @v_ReferredBy, @v_ReferredByName, @v_CollectFlag ,@v_ActivityType,@v_lastModifiedTime, 
				@v_ChildProcessInstanceId,@v_ChildWorkitemId,@v_FilterValue,@v_Guid,@v_NotifyStatus,@v_Q_DivertedByUserId,@v_RoutingStatus, 
				@v_NoOfCollectedInstances,@v_IsPrimaryCollected,@v_ExportStatus,@v_ProcessVariantId,@v_Introducedbyid,@v_IntroducedAt,@v_Createdby,@v_URN 
			IF (@@ERROR <> 0) 
			BEGIN 
				BREAK 
			END 
		END 
		CLOSE v_transferCursor 
		DEALLOCATE v_transferCursor 
		IF @row_present = 0 
		BEGIN 
			RETURN 
		END 
	END 
END
GO

-- EXEC WFTransferData '10', '2017-04-30 14:44:42.910', ''