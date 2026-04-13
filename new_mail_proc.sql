ALTER PROCEDURE [dbo].[NG_SRB_TAT_Escalation_Mail]
AS
BEGIN
    SET NOCOUNT ON;
    SET DATEFIRST 7;  -- Sunday = 1, Saturday = 7
 
    DECLARE 
        @MAIL_FROM NVARCHAR(200) = 'test5@rakbanktst.ae',
        @Today DATE = CAST(GETDATE() AS DATE);
 
    -- 1. Create a Temp Table for breaching items
    IF OBJECT_ID('tempdb..#BreachingItems') IS NOT NULL 
        DROP TABLE #BreachingItems;
 
    CREATE TABLE #BreachingItems (
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
        FROM QUEUEVIEW q WITH(NOLOCK)
        INNER JOIN USR_0_SRB_SLA_TAT_MASTER m WITH(NOLOCK)
            ON m.WORKSTEP_NAME = q.activityname
        INNER JOIN RB_SRB_EXTTABLE e WITH(NOLOCK)
            ON e.WI_NAME = q.processinstanceid
        WHERE q.processname = 'SRB'
          AND e.isRoutingForEFT = 'Y'
          AND q.activityname IN ('Ops_Monitor','Q2','Q4')
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
    -- Insert ONLY the items that have actually breached their TAT
    INSERT INTO #BreachingItems
    SELECT
        processinstanceid, processname, activityname, entryDATETIME,
        ProcessDefID, WorkItemID, ActivityID, TAT_IN_DAYS, ESCALATION_MAIL,
        COUNT(*) AS WorkingDaysElapsed
    FROM DateSeries
    WHERE DATEPART(WEEKDAY, WorkDate) NOT IN (1,7) 
    GROUP BY
        processinstanceid, processname, activityname, entryDATETIME,
        ProcessDefID, WorkItemID, ActivityID, TAT_IN_DAYS, ESCALATION_MAIL
    HAVING COUNT(*) >= TRY_CAST(MAX(TAT_IN_DAYS) AS INT)
    OPTION (MAXRECURSION 0);
 
    -- 3. Group the breaching items by ActivityName and build HTML rows
    IF OBJECT_ID('tempdb..#GroupedMails') IS NOT NULL 
        DROP TABLE #GroupedMails;

    SELECT 
        activityname,
        processname,
        ESCALATION_MAIL,
        MAX(ProcessDefID) AS ProcessDefID,
        MAX(processinstanceid) AS RefProcessInstanceId, 
        MAX(WorkItemID) AS RefWorkItemID,
        MAX(ActivityID) AS RefActivityID,
        HTML_TABLE_ROWS = (
            SELECT 
                td = i2.processinstanceid, '',
                td = CONVERT(VARCHAR(19), i2.entryDATETIME, 120), '',
                td = ISNULL(i2.TAT_IN_DAYS, ''), '',
                td = CAST(i2.WorkingDaysElapsed AS VARCHAR)
            FROM #BreachingItems i2
            WHERE i2.activityname = i1.activityname 
              AND i2.ESCALATION_MAIL = i1.ESCALATION_MAIL
            FOR XML PATH('tr')
        )
    INTO #GroupedMails
    FROM #BreachingItems i1
    GROUP BY activityname, processname, ESCALATION_MAIL;

    -- 4. Final INSERT joining the Template Table
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
        g.ESCALATION_MAIL,
        NULL,
        NULL,
        
        -- Replace Placeholder in the Subject with Display Names
        REPLACE(t.mailSubject, '#activityname#', 
            CASE g.activityname 
                WHEN 'Q2' THEN 'OPS Maker'
                WHEN 'Q4' THEN 'OPS Checker'
                ELSE g.activityname 
            END
        ),

        -- Replace Placeholders in the HTML Mail Body with Display Names
        REPLACE(
            REPLACE(t.MailTemplate, '#activityname#', 
                CASE g.activityname 
                    WHEN 'Q2' THEN 'OPS Maker'
                    WHEN 'Q4' THEN 'OPS Checker'
                    ELSE g.activityname 
                END
            ),
            '#HTML_TABLE_ROWS#', ISNULL(g.HTML_TABLE_ROWS, '')
        ),

        'text/html;charset=UTF-8',
        NULL, NULL, NULL, 
        1,       
        'N',     
        NULL, NULL, NULL, NULL,
        'CUSTOM', 
        'TRIGGER', 
        GETDATE(),
        g.ProcessDefID, 
        g.RefProcessInstanceId, 
        g.RefWorkItemID, 
        g.RefActivityID,
        0,       
        NULL, NULL, NULL, NULL
    FROM #GroupedMails g
    INNER JOIN USR_0_CPF_TemplateTypeTemplateMapping t WITH(NOLOCK)
        ON t.ProcessName = g.processname
        AND t.TemplateId = '8';
 
    -- 5. Cleanup
    DROP TABLE #BreachingItems;
    DROP TABLE #GroupedMails;
END;
GO
