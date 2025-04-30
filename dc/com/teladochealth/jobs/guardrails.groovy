package com.teladochealth.jobs
/*
Author: Jeff Melies
Purpose: To monitor Atlassian Guardrails
*/
import java.lang.Integer
import com.onresolve.scriptrunner.parameters.annotation.NumberInput
import com.onresolve.scriptrunner.parameters.annotation.*
import com.atlassian.jira.component.ComponentAccessor
import com.onresolve.scriptrunner.db.NoSuchDataSourceException
import org.ofbiz.core.entity.ConnectionFactory
import org.ofbiz.core.entity.DelegatorInterface
import com.onresolve.scriptrunner.db.DatabaseUtil
import java.sql.Connection
import groovy.sql.Sql
import com.atlassian.jira.mail.Email
import com.atlassian.mail.MailException
import com.atlassian.mail.server.SMTPMailServer
import org.apache.log4j.Logger
import org.apache.log4j.Level

log = Logger.getLogger("guardrails")
log.setLevel(Level.WARN)

@NumberInput(label = 'Threshold:', description = 'Enter the threshold (must be numeric).')
Integer thresholdLimit

@ShortTextInput(label = "Email results to:", description = "Enter an email address to send results to (leave blank to omit email).")
String eMailTo

@ShortTextInput(label = "Email subject:", description = "Enter the email Subject.")
String eMailSubject
eMailSubject = eMailSubject + " ${thresholdLimit}"

@ShortTextInput(label = "Column Name:", description = "Enter the db column name to compare threshold on.")
String columnName

@ShortTextInput(label = "Resource:", description = "Enter the Scriptrunner DB Resource.")
String dbResource

@ShortTextInput(label = "PostgreSQL Command:", description = "Enter the PostgreSQL command.")
String dbCommand

def output = []
List finalResults = []
Integer finalResult
def result = [:]
//def rows
String userHTML
String strHTML
def baseURL = com.atlassian.jira.component.ComponentAccessor.getApplicationProperties().getString("jira.baseurl")
def completeURL = baseURL + "/browse/"
//try{
//    rows = DatabaseUtil.withSql(dbResource) { sql ->
//        sql.rows(dbCommand)
//    }
//}catch (NoSuchDataSourceException e) {
//    return "Data source is invalid: ${e.message}"
//}

//Make db call
ArrayList rows = (dbCall dbCommand) as ArrayList // Map
if(rows == '[]'){return "Database query found no rows"} //JBM added 7/2/2024
log.warn("DB rows: ${rows?.size()}, example: ${if(rows != '[]'){rows[0]}}")
String myHTML

myHTML += "<tr>"
//JBM Replaced this on 8/13/2024 with the following collect() statement so that it passes Static checking
/*rows?.first()?.keySet()?.each { key ->
    myHTML += "<th style='text-align:center; background: black; color: white;'>${key.toString()}</th>"
}*/
rows.collect(){ row -> row}.each { key ->
    myHTML += "<th style='text-align:center; background: black; color: white;'>" + key.toString().split('=')[0].replace('{', '')+ "</th>"
}
myHTML += "</tr>"
rows.eachWithIndex { columns, idx ->
    //log.debug("Columns: ${columns}")        
    columns.each {Map.Entry test ->
        //log.debug("Test: ${test}")
        if(test.toString().contains(columnName.toString())){ //First we need to see if the row contains the columnName
            if(idx <= 1){log.warn("Does the Test.value: (${test.value}) exceed the threshold: (${thresholdLimit})? " + (test.value as Integer > thresholdLimit as Integer))}
            if (test.value as Integer > thresholdLimit as Integer) { //We compared to threshold
                finalResults.add(columns?.toString()?.replaceAll("[\\[\\](){}]","")?.trim())
                myHTML += "<tr>"
                columns.each {Map.Entry cell -> //Add the rows to the HTML
                    if(cell.toString().contains("key")){
                        myHTML += "<td style='text-align:center'><a href=" + completeURL + cell.value + ">${cell.value}</a></td>"
                    }else{
                        myHTML += "<td style='text-align:center; color:Black'>${cell.value}</td>"
                    }
                }
                myHTML += "</tr>"
            }else{
                finalResult = test.value as Integer
            }
        }
    }
}

//Adding a switch statement to help with email information
def mitigation
switch(eMailSubject.toLowerCase()){
    case ~/^\[.*-customfields\].*/:
        mitigation = "try <a href='https://confluence.atlassian.com/adminjiraserver/analyzing-the-usage-of-custom-fields-1047552722.html'>Analyzing</a> and " + "<a href='https://confluence.atlassian.com/adminjiraserver/optimizing-custom-fields-956713279.html'>Optimizing </a> custom fields, and this document to <a href='https://confluence.atlassian.com/adminjiraserver/analyzing-the-usage-of-custom-fields-1047552722.html'>analyze the usage of custom fields</a>"
    break;
    case ~/^\[.*-issue links\].*/:
        mitigation = "archive the issue or for <a href='https://confluence.atlassian.com/jirakb/is-there-an-easy-way-to-archive-a-lot-of-issues-976766896.html'>several issues</a>, you may want to query the database to <a href='https://confluence.atlassian.com/jirakb/how-to-find-the-issues-with-most-issue-links-or-comments-in-the-database-1072207097.html'>find additional issues with a lot of links</a>"
    break;
    case ~/^\[.*-attachments\].*/:
        mitigation = "discuss one of these options, <a href='https://confluence.atlassian.com/adminjiraserver/configuring-file-attachments-938847851.html'>turn off thumbnail display</a> and/or <a href='https://confluence.atlassian.com/jirakb/how-to-archive-attachments-of-archived-projects-1047559418.html'>archive attachments</a>, you can query the database to <a href='https://confluence.atlassian.com/jirakb/how-to-find-the-issues-with-most-issue-links-or-comments-in-the-database-1072207097.html'>find additional issues with a lot of attachments</a> to archive"
    break;
    case ~/^\[.*-comments\].*/:
        mitigation = "archive the issue, or discuss moderating a user groups activity by <a href='https://confluence.atlassian.com/jiracore/configure-safeguards-1107628196.html'>setting a global limit on the number Comments that can be created</a>, or remove older comments via REST or Scriptrunner, you can also query the database to <a href='https://confluence.atlassian.com/jirakb/how-to-find-the-issues-with-most-issue-links-or-comments-in-the-database-1072207097.html'>find additional issues with a lot of comments</a>"
    break;
    case ~/^\[.*-inactive projects\].*/:
        mitigation = "get approval from the project Lead and <a href='https://confluence.atlassian.com/adminjiraserver/archiving-a-project-938847621.html'>archive the project(s)</a>, for additional ideas on <a href='https://confluence.atlassian.com/clean/clean-up-your-jira-instance-1018788592.html'>how to identify old projects to clean up</a>"
    break;
    case ~/^\[.*-projects\].*/:
        mitigation = "get approval from the project Lead and <a href='https://confluence.atlassian.com/adminjiraserver/archiving-a-project-938847621.html'>archive the project(s)</a>, for additional ideas on <a href='https://confluence.atlassian.com/clean/clean-up-your-jira-instance-1018788592.html'>how to identify old projects to clean up</a>"
    break;
    case ~/^\[.*-epics\].*/:
        mitigation = "get approval from the project Lead and <a href='https://confluence.atlassian.com/jirakb/is-there-an-easy-way-to-archive-a-lot-of-issues-976766896.html'>archive the issue</a>"
    break;
    case ~/^\[.*-sprints\].*/:
        mitigation = "delete closed Sprints that are no longer needed by using <a href='https://docs.atlassian.com/jira-software/REST/8.13.0/#agile/1.0/sprint-deleteSprint'>(Delete Sprint) REST operation</a> and possibly discuss changing the system property to less than 65,000 on <a href='https://confluence.atlassian.com/adminjiraserver/setting-properties-and-options-on-startup-938847831.html'>jira.search.maxclauses</a>"
    break;
    case ~/^\[.*-users\].*/:
        mitigation = """review the following: 
        <ul><li>If most of the user accounts in your instance are stored in Crowd Data Center, Crowd Server, or Microsoft Active Directory, enable incremental synchronization. This way, only the changes since the last synchronization will be queried, reducing the need for a full sync. For more information, see Connecting to an LDAP directory.</li>
        <li>Consider using Crowd Server and Data Center as your external user directory to take advantage of features such as access-based synchronization. For more information, see Syncing users based on their access rights</li>
        <li>Use LDAP filters to reduce the number of users and groups to process by your instance. For more information, see:
        <ul><li>Connecting to an LDAP directory</li>
        <li>Reducing the number of users synchronized from LDAP to JIRA applications</li>
        <li>How to write LDAP search filters</li></ul>
        <li>Become familiar with User management limitations and recommendations.</li>
        </ul>"""
    break;
    case ~/^\[.*-groups\].*/:
        mitigation = """review these items:
        <ul><li>Configure your LDAP connection pool. Too many or too few connections may have a negative impact on performance. For more information, see <a href='https://confluence.atlassian.com/jirakb/configuring-ldap-connection-pooling-640188840.html'>Configuring LDAP connection pooling</a>.</li>
        <li>Disable group sync on every login by changing the Update group membership when logging in option to For newly added users only or Never. For more information, see <a href='https://confluence.atlassian.com/adminjiraserver/connecting-to-an-ldap-directory-938847052.html'>Connecting to an LDAP directory</a>.
        <i>&nbsp; &nbsp; <b><u>Note:</u></b> Changing this setting means that group membership data will not be updated until the next directory synchronization.</i></li>
        <li>Become familiar with user management limitations and recommendations.</li>
        </ul>"""
    break;
    default:
        mitigation = "review this article from <a href='https://confluence.atlassian.com/adminjiraserver/jira-software-guardrails-1141488685.html?_ga=2.157738749.618045674.1699546694-15355977.1686771631#JiraSoftwareguardrails-Projects'>Atlassian</a>"
    break;
}
   
log.debug("myHTML: ${myHTML.substring(4, myHTML.length())}")
if(finalResults){log.warn("finalResults: (${finalResults.size()}) - ${finalResults}")}
strHTML = "<style>"
strHTML += "table, th, td {"
strHTML += "border:1px solid black;"
strHTML += "border-collapse: collapse;"
strHTML += "color:Grey;"
strHTML += "}"
strHTML += "th {"
strHTML += "text-align: left;"
strHTML += "}"
strHTML += "tr:nth-child(odd) {"
strHTML += "background-color: #D6EEEE;"
strHTML += "}"
strHTML += "</style>"
strHTML += "<div>"
strHTML += "<p><a>${eMailSubject}</a><br><br>"
strHTML += "<a>To help eleminiate the risk of environment degradation you may want to ${mitigation}.</a><br></p>"
strHTML += "<table style='width:80%'>"
strHTML += myHTML.substring(4, myHTML.length())
strHTML += "</table>"
//strHTML += "</p>"
strHTML += "</div>"
//}
log.debug("Past the following results in: https://www.w3schools.com/html/tryit.asp?filename=tryhtml_default_default")
log.debug(strHTML) //Past these results in: https://www.w3schools.com/html/tryit.asp?filename=tryhtml_default_default
if(finalResults.size() >= 1) { // Only email if there are any rows to report
    SMTPMailServer mailServer = ComponentAccessor.getMailServerManager().getDefaultSMTPMailServer()
    if(mailServer){
        log.debug("We found the SMTP Mail Server.")
        try{
            Mail.send {
                setTo(eMailTo)
                //setCc(emailTo)
                setFrom("jira@teladochealth.com")
                setFromName("Jira Automation")
                setSubject(eMailSubject)
                setHtml()
                setBody("""${strHTML}""")
            }
        }catch (MailException e) {
            log.error("ERROR sending email: ${e.message}")
            return "ERROR sending email: ${e.message}"
        }
    }else{
        log.error("Please make sure that a valid mailServer is configured")
        return "ERROR: Please make sure that a valid mailServer is configured"
    }
    log.debug("${columnName} - Results: ${strHTML}")
    log.warn("We have exceeded the threshold limit of: ${thresholdLimit} finding (${finalResults.size()}) row(s): ${finalResults}")
    return "We have exceeded the threshold limit of: ${thresholdLimit} finding (${finalResults.size()}) row(s): ${finalResults}"
}else{
    log.debug("${columnName} - Results: ${strHTML}")
    log.warn("We have: ${finalResult} rows, and have not exceeded the threshold limit of ${thresholdLimit}.")
    return "We have: ${finalResult} rows, and have not exceeded the threshold limit of ${thresholdLimit}."
}

//******************************************FUNCTIONS******************************
def dbCall(String sqlString){
    def delegator = (DelegatorInterface) ComponentAccessor.getComponent(DelegatorInterface)
    String helperName = delegator.getGroupHelperName("default");
    Connection conn = ConnectionFactory.getConnection(helperName)
    Sql sql = new Sql(conn)
    //Map output = [:]
    def output
    try{
        sql = new Sql(conn)
            output = sql.rows(sqlString) as List //Map
            log.debug("DB Query output: ${output[0]}")
            return output 
    } finally {
        sql.close()
    }
}