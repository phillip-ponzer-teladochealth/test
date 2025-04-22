package com.teladochealth.jobs

/*
Author: Jeff Melies
Purpose: Opens Confluence as an anonymous user and if it can see 3 or more rows of data, an email
    will be sent out to atlassian.team@teladochealth.com, asking out team to check.
History: JBM-6/25/24: Previous script is located at the bottom
*/
import com.atlassian.jira.component.ComponentAccessor
import com.onresolve.scriptrunner.db.DatabaseUtil
import com.atlassian.jira.component.ComponentAccessor
import com.atlassian.jira.mail.Email
import com.atlassian.jira.mail.settings.MailSettings
import com.atlassian.mail.MailException
import com.atlassian.mail.server.SMTPMailServer
import com.atlassian.plugin.util.ContextClassLoaderSwitchingUtil
import com.onresolve.scriptrunner.db.DatabaseUtil
import org.apache.log4j.Logger
import org.apache.log4j.Level

log = Logger.getLogger("ConfluenceAnonymousUserTest")
log.setLevel(Level.INFO)

//*****************************Admins change this value when the # of external Spaces change
Integer numberOfSpaces = 2
//*****************************

//Email Variables:
def subject = "ALERT: Confluence Anonymous User access!"  //Subject of eMail
def emailAddr = "atlassian.team@teladochealth.com"  //Who to send email to

def issueManager = ComponentAccessor.issueManager
String dbResource = "Confluence-Prod" //From ScriptRunner Resources page
String sqlQuery = """SELECT SPACENAME AS "Space Name" FROM SPACES WHERE SPACEID IN (SELECT SPACEID FROM SPACEPERMISSIONS WHERE PERMTYPE = 'VIEWSPACE' AND PERMGROUPNAME IS NULL AND PERMUSERNAME IS NULL AND PERMALLUSERSSUBJECT IS NULL);"""
def body

def rows = dbCall(dbResource, sqlQuery) as Map
log.debug("The Scheduled job: Confluence Anonymous User Access for Production returned ${rows.toString()}.")
def bodyTable
//numberOfSpaces plus the header row
if(rows.size() >= numberOfSpaces+1){
    log.error("*************************CONFLUENCE QUERY RESULTS**********************************")
        for(row in rows) {
            String spaceName = row.toString().split("=").last().replaceAll("[\\[\\]\\{\\}]", "")
            log.error("CONFLUENCE: Anonymous users can access ${rows.size()} spaces: ${spaceName}")
        }
    bodyTable = makeHTMLTable(rows)
    body = eMailBody(bodyTable.toString(), numberOfSpaces, rows.size())
    sendEmail(emailAddr.toString(), subject.toString(), body.toString())
}
return body
//************************************************FUNCTIONS*****************************************************
//def bodyTable = makeHTMLTable(sqlQuery)
def eMailBody(String bodyTable, Integer expectedSpaces, Integer actualSpaces) {return """<h1><span style="font-size:14px"><span style="color:#c0392b"><u><strong>ALERT:</strong></u></span>&nbsp;Anonymous users can access more HomeSpaces in Confluence then expected and requires immediate attention.</span></h1>

<p>Anonymous users should only see ${expectedSpaces} HomeSpaces but are seeing ${actualSpaces}:<br>

${bodyTable}<hr>

<p>Administrators please investigate by following these steps:</p>

	<li>Open an Incognito window</li>
	<li>Navigate directly to our Confluence URL without using SSO.
	<ul>
		Production:&nbsp;&nbsp;<a href="https://confluence.teladoc.net/">https://confluence.teladoc.net/</a>
	</ul></li>
	<li>When Confluence appears ensure you are not logged in by viewing the upper right corner.</li><br>
	<span style="font-size: larger"><b><u>Take corrective action: </u></b></span>
    <ul><li>Either remove &#39;Anonymous&#39; access to the other HomeSpace being displayed and contacting the 
    Admin of the HomeSpace.</li>
    <li>Update the ScriptRunner <a href="https://jira.teladoc.net/plugins/servlet/scriptrunner/admin/jobs/edit/2fb2d08c-9166-4805-b9d5-6cf14e801622">Job</a> 
    found <a href="https://jira.teladoc.net/plugins/servlet/scriptrunner/admin/scriptEditor?root=%2Fvar%2Fatlassian%2Fapplication-data%2Fjira%2Fcluster-home%2F10e06c5a-8d2a-0089-1c23-c11b17b3fc8b%2Fscripts&file=com%2Fteladochealth%2Fjobs%2FconfluenceAnonymousUserTest.groovy">here</a> 
    by increasing the number for the variable <b>numberOfSpaces</b> on line #25 (approximately).</li></ul>

<p>Thank you for your quick response in this matter,<br />
Confluence Administration</p>

<p><sup>This email was sent via Scriptrunner within <a href="https://jira.teladoc.net/plugins/servlet/scriptrunner/admin/jobs" target="_blank">Jobs</a>.</sup></p>
"""
}

def dbCall(String myResource, String myQuery) {
    def results = DatabaseUtil.withSql(myResource) { sql ->
        sql.rows(myQuery.toString())
    }
    return results as Map
}

def makeHTMLTable(Map sqlResults) {
    def sqlTable = new StringBuffer()
    sqlTable << """<ol>"""
    for(row in sqlResults) {
        String spaceName = row.toString().split("=").last().replaceAll("[\\[\\]\\{\\}]", "")
        sqlTable << """<li>${spaceName}</li>"""
    }
    sqlTable << """</ol>"""
    return sqlTable
}

String sendEmail(String emailAddr, String subject, String body) {
    // Stop emails being sent if the outgoing mail server gets disabled (useful if you start a script sending emails and need to stop it)
    def mailSettings = ComponentAccessor.getComponent(MailSettings)
    if (mailSettings?.send()?.disabled) {
        return 
    }

    def mailServer = ComponentAccessor.mailServerManager.defaultSMTPMailServer
    if (!mailServer) {
        return
    }

    def email = new Email(emailAddr)
    email.setMimeType('text/html')
    email.setSubject(subject)
    email.setBody(body)
    try {
        ContextClassLoaderSwitchingUtil.runInContext(SMTPMailServer.classLoader) {
            mailServer.send(email)
        }
    } catch (MailException e) {
        log.error("Send mail failed with error: ${e.message}")
        log.error('Failed to Send Mail, Check Logs for error')
    }
}

/*Replaced 6/25/24 JBM
//Author: Jeff Melies
//Purpose: Opens Confluence as an anonymous user and if it can see 3 or more rows of data, an email
//    will be sent out to atlassian.team@teladochealth.com, asking out team to check.

import com.atlassian.jira.component.ComponentAccessor
import com.onresolve.scriptrunner.db.DatabaseUtil
import com.atlassian.jira.component.ComponentAccessor
import com.atlassian.jira.mail.Email
import com.atlassian.jira.mail.settings.MailSettings
import com.atlassian.mail.MailException
import com.atlassian.mail.server.SMTPMailServer
import com.atlassian.plugin.util.ContextClassLoaderSwitchingUtil
import com.onresolve.scriptrunner.db.DatabaseUtil

def issueManager = ComponentAccessor.issueManager
String dbResource = "Confluence-Prod" //From ScriptRunner Resources page
String sqlQuery = """SELECT SPACENAME AS "Space Names" FROM SPACES WHERE SPACEID IN (SELECT SPACEID FROM SPACEPERMISSIONS WHERE PERMTYPE = 'VIEWSPACE' AND PERMGROUPNAME IS NULL AND PERMUSERNAME IS NULL AND PERMALLUSERSSUBJECT IS NULL);"""
def subject = "ALERT: Confluence Anonymous User access!"  //Subject of eMail
def emailAddr = "atlassian.team@teladochealth.com"  //Who to send email to
def body

def rows = dbCall(dbResource, sqlQuery) as ArrayList
log.warn("The Scheduled job: Confluence Anonymous User Access for Production returned ${rows.toString()}.")
def bodyTable
if(rows.size() >= 4){
    log.error("*************************CONFLUENCE QUERY RESULTS**********************************")
        rows.each {
        log.error("CONFLUENCE: Anonymous users can access ${rows.size()} space, the name is: ${it}")
    }
    bodyTable = makeHTMLTable(rows)
    body = eMailBody(bodyTable.toString())
    sendEmail(emailAddr.toString(), subject.toString(), body.toString())
}


//def bodyTable = makeHTMLTable(sqlQuery)
def eMailBody(String bodyTable) {return """<h1><span style="font-size:14px"><span style="color:#c0392b"><u><strong>ALERT:</strong></u></span>&nbsp;Anonymous user access in production Confluence&nbsp;is displaying more results then expected and requires immediate attention.</span></h1>

<p>An Anonymous user should only see one&nbsp;Space: &#39;Fleet Ops - External Knowledge Base&#39; but are seeing multiple:</p>

${bodyTable}

<p>Administrators please investigate by following these steps:</p>

<ol>
	<li>Open an Incognito window</li>
	<li>Navigate directly to your Confluence URL without SSO.
	<ul>
		<li>Production:&nbsp;&nbsp;<a href="https://confluence.teladoc.net/">https://confluence.teladoc.net/</a></li>
	</ul>
	</li>
	<li>When Confluence appears ensure you are not logged in by viewing the upper right corner.</li>
	<li>Take corrective actions by removing &#39;Anonymous&#39; access to the other items being displayed.</li>
	<li>Contact the user and explain this action is not allowed or correct the script which can be found <a href="https://jira.teladoc.net/plugins/servlet/scriptrunner/admin/jobs/edit/2fb2d08c-9166-4805-b9d5-6cf14e801622">here</a>.</li>
</ol>

<p>Thank you for your quick response in this matter,<br />
Confluence Administration</p>

<p><sup>This email was sent via Scriptrunner within <a href="https://jira.teladoc.net/plugins/servlet/scriptrunner/admin/jobs" target="_blank">Jobs</a>.</sup></p>
"""
}

def dbCall(String myResource, String myQuery) {
    def results = DatabaseUtil.withSql(myResource) { sql ->
        sql.rows(myQuery.toString())
    }
    return results
}

def makeHTMLTable(ArrayList sqlResults) {
    def sqlTable = new StringBuffer()
    //make a header row from the set of keys
    sqlTable << """<table class="aui"><tbody><tr>"""
    sqlResults .first().keySet().each{
    sqlTable << """<th><u>$it</u></th>"""
    }
    sqlTable << "</tr>"

    //now get each sql row and create a table row
    sqlResults.each{ columns ->
    sqlTable << "<tr>"
    //look in each columna dn create a td element
    columns.each{ cell ->
        sqlTable << "<td>$cell.value</td>"
    }
    sqlTable << """</tr></tbody></table>"""
    }
    return sqlTable
}

String sendEmail(String emailAddr, String subject, String body) {
    // Stop emails being sent if the outgoing mail server gets disabled (useful if you start a script sending emails and need to stop it)
    def mailSettings = ComponentAccessor.getComponent(MailSettings)
    if (mailSettings?.send()?.disabled) {
        return 
    }

    def mailServer = ComponentAccessor.mailServerManager.defaultSMTPMailServer
    if (!mailServer) {
        return
    }

    def email = new Email(emailAddr)
    email.setMimeType('text/html')
    email.setSubject(subject)
    email.setBody(body)
    try {
        ContextClassLoaderSwitchingUtil.runInContext(SMTPMailServer.classLoader) {
            mailServer.send(email)
        }
    } catch (MailException e) {
        log.error("Send mail failed with error: ${e.message}")
        log.error('Failed to Send Mail, Check Logs for error')
    }
}
*/