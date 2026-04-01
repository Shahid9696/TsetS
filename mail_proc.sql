CREATE dbo.usp_SRB_TAT_Escalation_Mail
AS
BEGIN
    SET NOCOUNT ON;
    SET DATEFIRST 7;  -- Sunday = 1, Saturday = 7

    DECLARE 
        @MAIL_FROM NVARCHAR(200) = 'test5@rakbanktst.ae',
        @Today DATE = CAST(GETDATE() AS DATE);

    ;WITH BaseData AS
    (
        SELECT
            q.processinstanceid,
            q.processname,
            q.activityname,
            q.entryDATETIME,
            q.processdefid   AS ProcessDefID,
            q.workitemid     AS WorkItemID,
            q.activityid     AS ActivityID,
            m.TAT_IN_DAYS,
            m.ESCALATION_MAIL,

            -- Shift weekend entry to Monday
            CASE 
                WHEN DATEPART(WEEKDAY, q.entryDATETIME) = 7 
                    THEN DATEADD(DAY, 2, CAST(q.entryDATETIME AS DATE))
                WHEN DATEPART(WEEKDAY, q.entryDATETIME) = 1 
                    THEN DATEADD(DAY, 1, CAST(q.entryDATETIME AS DATE))
                ELSE CAST(q.entryDATETIME AS DATE)
            END AS SLA_StartDate
        FROM QUEUEVIEW q
        INNER JOIN USR_0_SRB_SLA_TAT_MASTER m
            ON m.WORKSTEP_NAME = q.activityname
        WHERE q.processname = 'SRB'
          AND m.ESCALATION_MAIL IS NOT NULL
          AND m.ESCALATION_MAIL <> ''
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
    ),
    WorkingDays AS
    (
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
            processinstanceid,
            processname,
            activityname,
            entryDATETIME,
            ProcessDefID,
            WorkItemID,
            ActivityID,
            TAT_IN_DAYS,
            ESCALATION_MAIL
    )

    INSERT INTO WFMAILQUEUETABLE
    (
        mailFrom,
        mailTo,
        mailCC,
        mailBCC,
        mailSubject,
        mailMessage,
        mailContentType,
        attachmentISINDEX,
        attachmentNames,
        attachmentExts,
        mailPriority,
        mailStatus,
        statusComments,
        lockedBy,
        successTime,
        LastLockTime,
        insertedBy,
        mailActionType,
        insertedTime,
        processDefId,
        processInstanceId,
        workitemId,
        activityId,
        noOfTrials,
        zipFlag,
        zipName,
        maxZipSize,
        alternateMessage
    )
    SELECT
        @MAIL_FROM,
        w.ESCALATION_MAIL,
        NULL,
        NULL,

        -- Subject
        CAST(w.processinstanceid AS NVARCHAR(50))
        + ' exceeding TAT ' 
        + CAST(w.TAT_IN_DAYS AS NVARCHAR(10))
        + ' working days '
        + w.activityname,

        -- HTML Body
        '<html><body>'
        + '<p>Subject <b>' + CAST(w.processinstanceid AS NVARCHAR(50)) + '</b> '
        + 'is pending in <b>' + w.activityname + '</b> '
        + 'exceeding <b>' + CAST(w.TAT_IN_DAYS AS NVARCHAR(10)) + '</b> working days.'
        + '</p><p>Kindly clear the same.</p>'
        + '</body></html>',

        'text/html;charset=UTF-8',
        NULL,
        NULL,
        NULL,
        1,
        'N',
        NULL,
        NULL,
        NULL,
        NULL,
        'CUSTOM',
        'TRIGGER',
        GETDATE(),
        w.ProcessDefID,
        w.processinstanceid,
        w.WorkItemID,
        w.ActivityID,
        0,
        NULL,
        NULL,
        NULL,
        NULL
    FROM WorkingDays w
    WHERE w.WorkingDaysElapsed > CAST(w.TAT_IN_DAYS AS INT)
    OPTION (MAXRECURSION 0);
END;
GO