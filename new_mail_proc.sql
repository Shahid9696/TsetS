USE [rakcas]
GO

ALTER PROCEDURE [dbo].[NG_SRB_TAT_Escalation_Mail]
AS
BEGIN
    SET NOCOUNT ON;
    SET DATEFIRST 7;  -- Sunday = 1, Saturday = 7
 
    DECLARE 
        @MAIL_FROM NVARCHAR(200) = 'test5@rakbanktst.ae',
        @Today DATE = CAST(GETDATE() AS DATE);
 
    -- 1. Create a Temp Table
    IF OBJECT_ID('tempdb..#TempWorkingDays') IS NOT NULL 
        DROP TABLE #TempWorkingDays;
 
    CREATE TABLE #TempWorkingDays (
        processinstanceid NVARCHAR(100),
        processname NVARCHAR(100),
        activityname NVARCHAR(100),
        entryDATETIME DATETIME,
        ProcessDefID NVARCHAR(100),  
        WorkItemID NVARCHAR(100),    
        ActivityID NVARCHAR(100),    
        TAT_IN_DAYS NVARCHAR(50), 
        ESCALATION_MAIL NVARCHAR(MAX),
        WorkingDaysElapsed INT
    );
 
    -- 2. CTE to isolate items and calculate recursive working days
    ;WITH BaseData AS
    (
        SELECT
            q.processinstanceid,
            q.processname,
            q.activityname,
            q.entryDATETIME,
            q.processdefid    AS ProcessDefID,
            q.workitemid      AS WorkItemID,
            q.activityid      AS ActivityID,
            m.TAT_IN_DAYS,
            m.ESCALATION_MAIL,
            CASE 
                WHEN DATEPART(WEEKDAY, q.entryDATETIME) = 7 
                    THEN DATEADD(DAY, 2, CAST(q.entryDATETIME AS DATE))
                WHEN DATEPART(WEEKDAY, q.entryDATETIME) = 1 
                    THEN DATEADD(DAY, 1, CAST(q.entryDATETIME AS DATE))
                ELSE CAST(q.entryDATETIME AS DATE)
            END AS SLA_StartDate
        FROM QUEUEVIEW q
        INNER JOIN USR_0_SRB_SLA_TAT_MASTER m WITH(NOLOCK)
            ON m.WORKSTEP_NAME = q.activityname
        INNER JOIN RB_SRB_EXTTABLE e WITH(NOLOCK)
            ON e.WI_NAME = q.processinstanceid
        WHERE q.processname = 'SRB'
          AND e.isRoutingForEFT = 'Y'
          AND q.activityname IN ('Ops_Monitor')
          AND ISNULL(m.ESCALATION_MAIL, '') <> ''
    ),
    DateSeries AS
    (
        SELECT
            b.*,
            b.SLA_StartDate AS WorkDate
        FROM BaseData b
        UNION ALL
        SELECT
            d.processinstanceid,
            d.processname,
            d.activityname,
            d.entryDATETIME,
            d.ProcessDefID,
            d.WorkItemID,
            d.ActivityID,
            d.TAT_IN_DAYS,
            d.ESCALATION_MAIL,
            d.SLA_StartDate,
            DATEADD(DAY, 1, d.WorkDate)
        FROM DateSeries d
        WHERE DATEADD(DAY, 1, d.WorkDate) <= @Today
    )
    INSERT INTO #TempWorkingDays
    SELECT
        processinstanceid,
        processname,
        activityname,
        entryDATETIME,
        ProcessDefID,
        WorkItemID,
        ActivityID,
        TAT_IN_DAYS,
        ESCALATION_MAIL,
        COUNT(*) AS WorkingDaysElapsed
    FROM DateSeries
    WHERE DATEPART(WEEKDAY, WorkDate) NOT IN (1,7) 
    GROUP BY
        processinstanceid, processname, activityname, entryDATETIME,
        ProcessDefID, WorkItemID, ActivityID, TAT_IN_DAYS, ESCALATION_MAIL
    OPTION (MAXRECURSION 0);
 
    -- 3. Final INSERT joining the Template Table
    INSERT INTO WFMAILQUEUETABLE
    (
         mailFrom, mailTo, mailCC, mailBCC, mailSubject, mailMessage,
         mailContentType, attachmentISINDEX, attachmentNames, attachmentExts,
         mailPriority, mailStatus, statusComments, lockedBy, successTime,
         LastLockTime, insertedBy, mailActionType, insertedTime,
         processDefId, processInstanceId, workitemId, activityId,
         noOfTrials, zipFlag, zipName, maxZipSize, alternateMessage
    )
    SELECT
        ISNULL(NULLIF(t.FromMail, ''), @MAIL_FROM), 
        w.ESCALATION_MAIL,
        NULL,
        NULL,
        
       -- Replace Placeholders in the Subject
    REPLACE(
        REPLACE(
            REPLACE(t.mailSubject, '#processinstanceid#', w.processinstanceid),
            '#activityname#', w.activityname
        ),
        '#TAT_IN_DAYS#', ISNULL(w.TAT_IN_DAYS, '')
    ),

    -- Replace Placeholders in the HTML Mail Body
    REPLACE(
        REPLACE(
            REPLACE(t.MailTemplate, '#processinstanceid#', w.processinstanceid),
            '#activityname#', w.activityname
        ),
        '#TAT_IN_DAYS#', ISNULL(w.TAT_IN_DAYS, '')
    ),

        'text/html;charset=UTF-8',
        NULL, NULL, NULL, 
        1,       
        'N',     
        NULL, NULL, NULL, NULL,
        'CUSTOM', 
        'TRIGGER', 
        GETDATE(),
        w.ProcessDefID, 
        w.processinstanceid, 
        w.WorkItemID, 
        w.ActivityID,
        0,       
        NULL, NULL, NULL, NULL
    FROM #TempWorkingDays w
    -- Join your template mapping table here
    INNER JOIN USR_0_CPF_TemplateTypeTemplateMapping t WITH(NOLOCK)
        ON t.ProcessName = w.processname
        AND t.TemplateId = '7' 
        -- If you need to check IsActiveMail, add it here: AND t.IsActiveMail = 'Y'
    WHERE w.WorkingDaysElapsed >= TRY_CAST(w.TAT_IN_DAYS AS INT);
 
    -- 4. Cleanup
    DROP TABLE #TempWorkingDays;
END;
GO