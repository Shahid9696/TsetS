-- First, delete the old one if it exists to avoid duplicates
DELETE FROM [dbo].[USR_0_CPF_TemplateTypeTemplateMapping] 
WHERE ProcessName = 'SRB' AND TemplateId = '8';

-- Insert the updated template for tabular data
INSERT INTO [dbo].[USR_0_CPF_TemplateTypeTemplateMapping]
           ([ProcessName],[CommStage],[TemplateType],[TemplateId],[MailTemplate],
            [FromMail],[DefaultCCMail],[IsActiveMail],[SMSEnglishTemplate],
            [IsActiveEnglish],[SMSArabicTemplate],[isActiveSMSArabic],[MailPlaceHolders],
            [mailSubject],[mailSubPlaceHolder],[Infobip_Alert_ID],[Infobip_Dynamic_Tags],[Infobip_SMS_isActive])
     VALUES
           ('SRB', 
            '', 
            'SLATemplate', 
            '8',
            '<!DOCTYPE html> <html> <head> <meta charset="UTF-8"> <meta name="color-scheme" content="light"> <meta name="supported-color-schemes" content="light"> </head> <body style="margin:0; padding:0; font-family:Verdana, Arial, sans-serif; background:#ffffff;"> <table width="100%" cellpadding="0" cellspacing="0" border="0" align="center"> <tr> <td align="center"> <table width="1000" cellpadding="0" cellspacing="0" border="0" style="border-collapse:collapse; margin:20px auto;"> <tr> <td width="100%" style="padding:20px; padding-bottom:10px; font-size:13px; line-height:1.6; direction:ltr; text-align:left;"> <p>Hello Team,</p> <p>The following work items are pending in <b>#activityname#</b> and have exceeded their TAT. Please find the details below:</p> <table border="1" cellpadding="8" cellspacing="0" style="border-collapse:collapse; width:100%; text-align:left; font-size:12px;"> <tr style="background-color:#f2f2f2;"> <th>Process Instance ID</th> <th>Entry Date</th> <th>TAT (Days)</th> <th>Working Days Elapsed</th> </tr> #HTML_TABLE_ROWS# </table> <p>Kindly clear the same.</p> </td> </tr> </table> </td> </tr> </table> </body> </html>',
            
            'test5@rakbanktst.ae', '', 'Y', '', '', '', '', '', 
            
            'Action Required: Multiple items exceeding TAT in #activityname#', 
            
            '', '', '', '')


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
        MAX(ProcessDefID) AS ProcessDefID, -- Grab one to satisfy the mail table constraint
        MAX(processinstanceid) AS RefProcessInstanceId, 
        MAX(WorkItemID) AS RefWorkItemID,
        MAX(ActivityID) AS RefActivityID,
        HTML_TABLE_ROWS = (
            -- This creates the dynamic <tr><td> rows for the HTML table
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
        
        -- Replace Placeholder in the Subject
        REPLACE(t.mailSubject, '#activityname#', g.activityname),

        -- Replace Placeholders in the HTML Mail Body
        REPLACE(
            REPLACE(t.MailTemplate, '#activityname#', g.activityname),
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
    -- Join template mapping table here
    INNER JOIN USR_0_CPF_TemplateTypeTemplateMapping t WITH(NOLOCK)
        ON t.ProcessName = g.processname
        AND t.TemplateId = '8';
 
    -- 5. Cleanup
    DROP TABLE #BreachingItems;
    DROP TABLE #GroupedMails;
END;
GO
