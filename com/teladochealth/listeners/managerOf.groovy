package com.teladochealth.listeners
/*
Author: Jeff Melies
Type: Listener
Purpose: Retrieves the manager of a User field and populates another User field with the Managers name.
Implementation steps:
    1. Add this script to a Listener if one doesn't already exist.
        Completed for 'Manager of Assignee' and 'Manager of Reporter'.
        Add new project to Listener(s) and Custom Fields.
        Add to projects 'View' screen.
    2. Add Jira Project to the destination User Picker field to be populated
    2. Select a User Picker field to read a user from.
    3. Select a User Picker field to populate the managers name from #3 above.
    4. Add the destination field to the screen(s) of the project, usually just the 'View' screen.
See the Listeners for 'Manager of Assignee' and 'Manager of Reporter' for examples
Change log: 1/10/24 JBM: I made several changes and replaced the whole script, see old version at the end.
*/
import com.atlassian.jira.user.util.Users
import com.atlassian.jira.issue.MutableIssue
import com.atlassian.jira.component.ComponentAccessor
import com.atlassian.jira.user.ApplicationUser
import com.atlassian.jira.issue.Issue
import com.atlassian.jira.issue.changehistory.ChangeHistory
import com.atlassian.jira.issue.changehistory.ChangeHistoryItem
import com.atlassian.jira.issue.changehistory.ChangeHistoryManager
import com.atlassian.jira.issue.history.ChangeItemBean
import com.atlassian.jira.bc.user.search.UserSearchService
import java.time.temporal.ChronoUnit
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import com.atlassian.jira.event.type.EventDispatchOption
import com.atlassian.jira.issue.fields.Field as FormsField
import com.onresolve.scriptrunner.parameters.annotation.*
import javax.naming.directory.SearchControls
import com.onresolve.scriptrunner.ldap.LdapUtil
import org.springframework.ldap.core.AttributesMapper
import com.atlassian.jira.user.util.UserManager
import org.apache.log4j.Logger
import org.apache.log4j.Level

log = Logger.getLogger("(${issue}) Listener_ManagerOf") //as Logger
log.setLevel(Level.INFO)

Issue issue = event.getIssue() // We use all 3 of these Issue
Issue myIssue = ComponentAccessor.getIssueManager().getIssueObject(issue.key)
MutableIssue mutableIssue = ComponentAccessor.getIssueManager().getIssueByKeyIgnoreCase(myIssue.key) as MutableIssue

//import com.atlassian.jira.workflow.WorkflowManager
//import com.atlassian.jira.workflow.JiraWorkflow
//WorkflowManager workflowManager = ComponentAccessor.getWorkflowManager()
//JiraWorkflow workflow = workflowManager.getWorkflow(issue)
//log.debug("getActions(): ${workflow.getLinkedStep(issue.status).getActions()}")

//Managers
def cfManager = ComponentAccessor.getCustomFieldManager()
ChangeHistoryManager changeHistoryManager = ComponentAccessor.getChangeHistoryManager()  
UserManager userManager = ComponentAccessor.getUserManager()
def eventTypeManager = ComponentAccessor.getEventTypeManager()

@FieldPicker(label = "Field to Read", description = "Field to retrieve manager from.")
FormsField fieldToRead
log.debug("fieldToRead.id ${fieldToRead?.id} - ${fieldToRead.name}")
@FieldPicker(label = 'Field to write to', description = 'Field to populate the results to.')
FormsField fieldToPopulate
log.debug("fieldToPopulate.id ${fieldToPopulate?.id}")
log.debug("${issue} - The script ManagerOf has started for (${fieldToPopulate}).")

//User defined variables
    String managerOU, manager
    ApplicationUser ftrUserObj, managerOf
    ApplicationUser loggedinUser = ComponentAccessor.getJiraAuthenticationContext().getLoggedInUser()
    ApplicationUser username = ComponentAccessor.getUserManager().getUserByName("IT_Worker")
    Boolean results_issueHist = false
    def eventTypeName = eventTypeManager.getEventType(event.eventTypeId).getName()
    log.debug("EVENT TYPE NAME --> "+ eventTypeName)
    def ftrObj = cfManager.getCustomFieldObjectsByName(fieldToRead.name)[0]
    def issueFTR = cfManager.getCustomFieldObjects(myIssue).find { it.id == fieldToRead.id }
    def ftpObj = cfManager.getCustomFieldObjectsByName(fieldToPopulate.name)[0]
    def issueFTP = cfManager.getCustomFieldObjects(myIssue).find { it.id == fieldToPopulate.id }
    List<ChangeHistoryItem> allChanges = changeHistoryManager.getAllChangeItems(myIssue)
    List<ChangeItemBean> fieldChanges = []
    if(fieldToRead?.id == 'reporter' || fieldToRead?.id == 'assignee'){
        fieldChanges = changeHistoryManager.getChangeItemsForField(myIssue, fieldToRead.name.toLowerCase())
    }else{
        fieldChanges = changeHistoryManager.getChangeItemsForField(myIssue, fieldToRead.name)
    }
    def issueFTPValue = issueFTP?.getValue(issue)
    def issueFTRValue = issueFTR?.getValue(issue)
    log.debug("ftrObj: ${ftrObj}, issueFTR: ${issueFTR}")
    log.debug("ftpObj: ${ftpObj}, issueFTP: ${issueFTP}")
    log.debug("allChanges: ${allChanges}")
    log.debug("fieldChanges: ${fieldChanges}")

    if(issueFTRValue) {
        ftrUserObj = userManager.getUserByName(ftrObj.getValue(myIssue).toString().tokenize("(")[0]) as ApplicationUser//(customFieldValue.toString().tokenize("(")[0])  //(customFieldValue.toString())
    }
    log.debug("ftrUserObj: ${ftrUserObj}")

    if(! ftrUserObj){
        switch (fieldToRead?.name.toLowerCase()){
            case "assignee":
                ftrUserObj = issue?.getAssignee() as ApplicationUser
                log.debug("Assignee-ftr UserObj: ${ftrUserObj}")
            break;
            case "reporter":
                ftrUserObj = issue?.getReporter() as ApplicationUser
                log.debug("Reporter-ftr UserObj: ${ftrUserObj}")
            break;
            case "deployer":
                ftrUserObj = issue.getCustomFieldValue(issueFTR) as ApplicationUser
                log.debug("Deployer-ftr UserObj: ${ftrUserObj}")
            break;
            default:
                log.error("${issue} - You need to add to the 'Switch' statement within the script managerOf.groovy.")
                return "You need to add to the 'Switch' statement."
            break;
        }
    }


//if(allChanges){//allChanges was not empty, then it's an update and additional checks should be done.
if(eventTypeName != "Issue Created"){
    log.debug("${myIssue} - Updated Issue or Assigned.")
    if(eventTypeName == "Issue Assigned" || fieldChanges){ //Issue Update: There has been changes on the specific fieldToRead or it's been assigned.
        results_issueHist = issueHistory (myIssue as Issue, fieldChanges as List)
        log.debug("results_issueHist: ${results_issueHist}")
        if(eventTypeName == "Issue Assigned" || results_issueHist){//Issue was assigned or issueHist returned true, so there are changes that need to be processed
            log.debug("issueHist returned: ${results_issueHist}, so there were changes that need to be processed.")
            if(ftrUserObj != null && ftrUserObj?.active){//'fieldToRead' is populated and user is active, continue
                log.debug("${myIssue} - The user ${ftrUserObj.displayName} is active, Let's continue to update the issue.")
                managerOf = retrieveManager (myIssue, fieldToRead) as ApplicationUser
                log.debug("${myIssue} - managerOf: ${managerOf}")
                if(managerOf){
                    try{ //Populate the fieldToPopulate
                        mutableIssue.setCustomFieldValue(ftpObj, managerOf)
                        ComponentAccessor.getIssueManager().updateIssue(username, mutableIssue, EventDispatchOption.DO_NOT_DISPATCH, false);
                    } catch(Exception e1) {
                        log.error("ManagerOf - ${issue}: ERROR Updating ${ftpObj}: ${e1}")
                    }
                }else{
                    try{ //Populate the fieldToPopulate
                        mutableIssue.setCustomFieldValue(ftpObj, null)
                        ComponentAccessor.getIssueManager().updateIssue(username, mutableIssue, EventDispatchOption.DO_NOT_DISPATCH, false);
                    } catch(Exception e1) {
                        log.error("ManagerOf - ${issue}: ERROR Updating ${ftpObj}: ${e1}")
                    }
                }

            }else{//'fieldToRead' is not populated, clear fieldToPopulate and stop running script.
                log.debug("${myIssue} - ftrUserObj: ${ftrUserObj} - Should be null or the user is inactive ftrUserObj?.active: ${ftrUserObj?.active}.  issueFTPValue: ${issueFTPValue}")
                if(issueFTPValue){
                    log.debug("${myIssue} - The field (${fieldToRead}) is empty, clearing the (${fieldToPopulate}) field.")
                    mutableIssue.setCustomFieldValue(ftpObj, null)
                    ComponentAccessor.getIssueManager().updateIssue(username, mutableIssue, EventDispatchOption.DO_NOT_DISPATCH, false);
                    return "${myIssue} - The field (${fieldToRead}) is empty, clearing the (${fieldToPopulate}) field."
                }
            }
        }else{ //issueHist: Returned false, so there are no changes to process.
            log.debug("${myIssue} - issueHist returned ${results_issueHist}, so there are no changes to process.")
            return "issueHist returned false, so there are no changes to process."
        }
    }else{ //Issue Update: If there hasn't been any changes on the fieldToRead, then stop running the script
        log.debug("${myIssue} - Issue Update: There has been no changes on the fieldToRead quit running script.")
        return "Issue Update: there has been no changes on the fieldToRead then quit running script."
    }
}else{//If allChanges is empty, then this is a new Issue and we just need to see if the fieldToRead is populated 
//***If the event 'issue created' isn't being captured, the script will not run.
    log.debug("${myIssue} - New issue.")
    if(ftrUserObj){//'fieldToRead' is populated, continue.
        log.debug("${myIssue} - New issue: fieldToRead is populated with ${ftrUserObj?.displayName}.")
        if(ftrUserObj.active){ //The user in the 'fieldToRead' field is active, continue running the script
            log.debug("${myIssue} - New issue: The user ${ftrUserObj.displayName} is active, continue")
            managerOf = retrieveManager (myIssue, fieldToRead) as ApplicationUser
            log.debug("${myIssue} - managerOf: ${managerOf}")
            if(managerOf){
                try{ //Populate the fieldToPopulate
                    mutableIssue.setCustomFieldValue(ftpObj, managerOf)
                    ComponentAccessor.getIssueManager().updateIssue(username, mutableIssue, EventDispatchOption.DO_NOT_DISPATCH, false);
                } catch(Exception e1) {
                    log.error("ManagerOf - ${issue}: ERROR Updating ${ftpObj}: ${e1}")
                }
            }
        }
    }
}
if(managerOf?.displayName){
    log.debug("${issue} - ${fieldToPopulate} = ${managerOf.displayName}.")
}else{
    log.debug("${issue} - ${fieldToPopulate} = ${managerOf}.")
}
//---------------------------------------------METHODS-------------------------------------------------------
def retrieveLDAP_User(Issue issue, String ldapQuery, String attribute) {
    log.setLevel(Level.INFO)
    log.debug("retrieveLDAP_User started - ${issue} - Variables: attribute: ${attribute}, ldapQuery: ${ldapQuery}")
    def results
    try {
        //return LdapUtil.withTemplate('LDAP-Prod') { template ->
        results = LdapUtil.withTemplate('LDAP-Prod') { template ->
            template.search("OU=Teladoc Users", "(${ldapQuery})", SearchControls.SUBTREE_SCOPE, { attributes ->
                attributes.get(attribute).get()
            } as AttributesMapper<String>)
        }
    } catch(Exception e1) {
        log.debug("***${issue} *** ManagerOf: ERROR EXCEPTION: ${e1}")
        log.error("***${issue} *** ManagerOf: ERROR EXCEPTION: ${e1}")
    }
    log.debug("retrieveLDAP_User - ${issue} - results: ${results}")
    return results
}

def retrieveManager(Issue issue, FormsField fieldToRead ) {
    log.debug("retrieveManager started - ${issue} - Field: ${fieldToRead}")
    log.setLevel(Level.INFO)
    String ldapQueryString, managerOU, manager
    switch (fieldToRead?.name.toLowerCase()){
        case "assignee":
            ldapQueryString = "mail=${issue.assignee.emailAddress}" as String
        break;
        case "reporter":
            ldapQueryString = "mail=${issue.reporter.emailAddress}" as String
        break;
        case "deployer":
            def deployerCF = ComponentAccessor.getCustomFieldManager().getCustomFieldObjectsByName('Deployer').first()
            def deployerUser = (ApplicationUser) issue.getCustomFieldValue(deployerCF) 
            ldapQueryString = "mail=${deployerUser.emailAddress}" as String
        break;
        default:
            log.error("${issue} - retrieveManager method - You need to add ${fieldToRead?.name.toLowerCase()} to the 'Switch' statement.")
            return "retrieveManager method - You need to add ${fieldToRead?.name.toLowerCase()} to the 'Switch' statement."
        break;
    }
    managerOU = (retrieveLDAP_User issue, ldapQueryString, "manager").toString().replaceAll("[\\[\\]]", "")
    manager = retrieveLDAP_User (issue, "${managerOU.toString().substring(0, managerOU.toString().indexOf(",")) as String}", "userPrincipalName")
    log.debug("retrieveManager - ${issue} - manager: ${manager.toString().replaceAll("[\\[\\]]", "")},,, ${manager}")
    return ComponentAccessor.getUserManager().getUserByName("${manager.toString().replaceAll("[\\[\\]]", "")}") as ApplicationUser
}

def issueHistory(Issue myIssue, List<ChangeItemBean> fldCHG){
    DateTimeFormatter dtf = DateTimeFormatter.ofPattern('yyyy-MM-dd HH:mm:ss.SSS')
    Long timeDiff
    String createdString
    LocalDateTime nowDT = LocalDateTime.now()
    LocalDateTime createdDT
    log.setLevel(Level.INFO)
    
    log.debug("issueHistory started - ${myIssue} - fldCHG list: ${fldCHG}.")
    if(fldCHG){ //If there were changes to the specific field, lets review the most recent one
        createdString = fldCHG.last().created.toString() as String 
        //For some reason the createdString is dropping the succeeding 0's for milliseconds WHEN they exist
        def millisec = createdString.tokenize(".")[1]        
        for (int i = millisec.length(); i < 3; i++){
            log.debug("Concat a 0 to the createdString String.")
            createdString += "0"
        }
        createdDT = LocalDateTime.parse(createdString, dtf)
        log.debug("Function: issueHistory - createdDT: ${createdDT}")
        log.debug("Function: issueHistory - nowDT: ${nowDT}")
        //Get the time span between now and the time it was changed
        timeDiff = createdDT.until(nowDT, ChronoUnit.SECONDS) //LocalDateTime.now()
        log.debug("Function: issueHistory - timeDiff: SECONDS: ${timeDiff}")
    //Do what we can to quit early, if the timeDiff is too long then it's probably an old change and fired event doesn't effect this field.
        if(timeDiff > 1){//if timeDiff is greater then 1 second, it's probably a duplicate run and shouldn't continue with the script.
            return false //"TimeDiff"
        }else{//if the timeDiff is less, then the change event is probably for the field and the script should continue to run.
            return true
        }
    }else{//
        return false  //Since there was allChanges and no fieldChanges, we should stop the script.
    }    
}



/*
Author: Jeff Melies
Type: Listener
Purpose: Retrieves the manager of a User field and populates another User field with the Managers name.
Implementation steps:
    1. Add this script to a Listener if one doesn't already exist.
        Completed for 'Manager of Assignee' and 'Manager of Reporter'.
        Add new project to Listener(s) and Custom Fields.
        Add to projects 'View' screen.
    2. Add Jira Project to the destination User Picker field to be populated
    2. Select a User Picker field to read a user from.
    3. Select a User Picker field to populate the managers name from #3 above.
    4. Add the destination field to the screen(s) of the project, usually just the 'View' screen.
See the Listeners for 'Manager of Assignee' and 'Manager of Reporter' for examples

import com.atlassian.jira.issue.MutableIssue
import com.atlassian.jira.component.ComponentAccessor
import com.atlassian.jira.user.ApplicationUser
import com.atlassian.jira.issue.Issue
import com.atlassian.jira.issue.changehistory.ChangeHistory
import com.atlassian.jira.issue.changehistory.ChangeHistoryItem
import com.atlassian.jira.issue.changehistory.ChangeHistoryManager
import com.atlassian.jira.issue.history.ChangeItemBean
import com.atlassian.jira.bc.user.search.UserSearchService
import java.time.temporal.ChronoUnit
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import com.atlassian.jira.event.type.EventDispatchOption
import com.atlassian.jira.issue.fields.Field as FormsField
import com.onresolve.scriptrunner.parameters.annotation.*
import javax.naming.directory.SearchControls
import com.onresolve.scriptrunner.ldap.LdapUtil
import org.springframework.ldap.core.AttributesMapper
import com.atlassian.jira.user.util.UserManager
import org.apache.log4j.Logger
import org.apache.log4j.Level

log.setLevel(Level.DEBUG)
log = Logger.getLogger("ListenerManagerOf") //as Logger

Issue issue = event.getIssue() // We use all 3 of these Issue
Issue myIssue = ComponentAccessor.getIssueManager().getIssueObject(issue.key)
MutableIssue mutableIssue = ComponentAccessor.getIssueManager().getIssueByKeyIgnoreCase(myIssue.key) as MutableIssue

//Managers
def cfManager = ComponentAccessor.getCustomFieldManager()
ChangeHistoryManager changeHistoryManager = ComponentAccessor.getChangeHistoryManager()  
UserManager userManager = ComponentAccessor.getUserManager()

@FieldPicker(label = "Field to Read", description = "Field to retrieve manager from.")
FormsField fieldToRead

@FieldPicker(label = 'Field to write to', description = 'Field to populate the results to.')
FormsField fieldToPopulate

log.debug("${issue} - The script ManagerOf has started for (${fieldToPopulate}).")

//User defined variables
    String managerOU, manager
    ApplicationUser ftrUserObj, managerOf
    ApplicationUser loggedinUser = ComponentAccessor.getJiraAuthenticationContext().getLoggedInUser()
    Boolean results_issueHist = false
    def ftpObj = cfManager.getCustomFieldObjectsByName(fieldToPopulate.name)[0]
    def ftrObj = cfManager.getCustomFieldObjectsByName(fieldToRead.name)[0]
    def issueFTP = cfManager.getCustomFieldObjects(myIssue).find { it.id == fieldToPopulate.id }
    def issueFTR = cfManager.getCustomFieldObjects(myIssue).find { it.id == fieldToRead.id }
    List<ChangeHistoryItem> allChanges = changeHistoryManager.getAllChangeItems(myIssue)
    List<ChangeItemBean> fieldChanges = changeHistoryManager.getChangeItemsForField(myIssue, fieldToRead.name.toLowerCase())
    def issueFTPValue = issueFTP?.getValue(issue)
    def issueFTRValue = issueFTR?.getValue(issue)

    if(issueFTRValue) {
        ftrUserObj = userManager.getUserByName(ftrObj.getValue(myIssue).toString().tokenize("(")[0]) as ApplicationUser//(customFieldValue.toString().tokenize("(")[0])  //(customFieldValue.toString())
    }
    log.debug("ftrUserObj: ${ftrUserObj}")

    if(! ftrUserObj){
        switch (fieldToRead?.name.toLowerCase()){
            case "assignee":
                ftrUserObj = issue?.getAssignee() as ApplicationUser
                log.debug("ftrUserObj: ${ftrUserObj}")
            break;
            case "reporter":
                ftrUserObj = issue?.getReporter() as ApplicationUser
                log.debug("ftrUserObj: ${ftrUserObj}")
            break;
            default:
                log.error("${issue} - You need to add to the 'Switch' statement within the script managerOf.groovy.")
                return "You need to add to the 'Switch' statement."
            break;
        }
    }


if(allChanges){//allChanges was not empty, then it's an update and additional checks should be done.
    log.debug("${myIssue} - Updated Issue.")
    if(fieldChanges){ //Issue Update: There has been changes on the specific fieldToRead.
        results_issueHist = issueHistory (myIssue as Issue, fieldChanges as List)
        if(results_issueHist){//issueHist: Returned true, so there are changes that need to be processed
            log.debug("${myIssue} - issueHist returned ${results_issueHist}, so there were changes that need to be processed.")
            if(ftrUserObj != null && ftrUserObj?.active){//'fieldToRead' is populated and user is active, continue
                log.debug("${myIssue} - The user ${ftrUserObj.displayName} is active, Let's continue to update the issue.")
                managerOf = retrieveManager (myIssue, fieldToRead) as ApplicationUser
                log.debug("${myIssue} - managerOf: ${managerOf}")
                if(managerOf){
                    try{ //Populate the fieldToPopulate
                        mutableIssue.setCustomFieldValue(ftpObj, managerOf)
                        ComponentAccessor.getIssueManager().updateIssue(loggedinUser, mutableIssue, EventDispatchOption.DO_NOT_DISPATCH, false);
                    } catch(Exception e1) {
                        log.error("ManagerOf - ${issue}: ERROR Updating ${ftpObj}: ${e1}")
                    }
                }

            }else{//'fieldToRead' is not populated, clear fieldToPopulate and stop running script.
                log.debug("${myIssue} - ftrUserObj: ${ftrUserObj} - Should be null or the user is inactive ftrUserObj?.active: ${ftrUserObj?.active}.  issueFTPValue: ${issueFTPValue}")
                if(issueFTPValue){
                    log.debug("${myIssue} - The field (${fieldToRead}) is empty, clearing the (${fieldToPopulate}) field.")
                    mutableIssue.setCustomFieldValue(ftpObj, null)
                    ComponentAccessor.getIssueManager().updateIssue(loggedinUser, mutableIssue, EventDispatchOption.DO_NOT_DISPATCH, false);
                    return "${myIssue} - The field (${fieldToRead}) is empty, clearing the (${fieldToPopulate}) field."
                }
            }
        }else{ //issueHist: Returned false, so there are no changes to process.
            log.debug("${myIssue} - issueHist returned ${results_issueHist}, so there are no changes to process.")
            return "issueHist returned false, so there are no changes to process."
        }
    }else{ //Issue Update: If there hasn't been any changes on the fieldToRead, then stop running the script
        log.debug("${myIssue} - Issue Update: There has been no changes on the fieldToRead quit running script.")
        return "Issue Update: there has been no changes on the fieldToRead then quit running script."
    }
}else{//If allChanges is empty, then this is a new Issue and we just need to see if the fieldToRead is populated 
//***If the event 'issue created' isn't being captured, the script will not run.
    log.debug("${myIssue} - New issue.")
    if(ftrUserObj){//'fieldToRead' is populated, continue.
        log.debug("${myIssue} - New issue: fieldToRead is populated with ${ftrUserObj?.displayName}.")
        if(ftrUserObj.active){ //The user in the 'fieldToRead' field is active, continue running the script
            log.debug("${myIssue} - New issue: The user ${ftrUserObj.displayName} is active, continue")
            managerOf = retrieveManager (myIssue, fieldToRead) as ApplicationUser
            log.debug("${myIssue} - managerOf: ${managerOf}")
            if(managerOf){
                try{ //Populate the fieldToPopulate
                    mutableIssue.setCustomFieldValue(ftpObj, managerOf)
                    ComponentAccessor.getIssueManager().updateIssue(loggedinUser, mutableIssue, EventDispatchOption.DO_NOT_DISPATCH, false);
                } catch(Exception e1) {
                    log.error("ManagerOf - ${issue}: ERROR Updating ${ftpObj}: ${e1}")
                }
            }
        }
    }
}
if(managerOf?.displayName){
    log.info("${issue} - ${fieldToPopulate} = ${managerOf.displayName}.")
}else{
    log.info("${issue} - ${fieldToPopulate} = ${managerOf}.")
}
//---------------------------------------------METHODS-------------------------------------------------------
def retrieveLDAP_User(Issue issue, String ldapQuery, String attribute) {
    log.setLevel(Level.DEBUG)
    log.debug("retrieveLDAP_User started - ${issue} - Variables: attribute: ${attribute}, ldapQuery: ${ldapQuery}")
    def results
    try {
        //return LdapUtil.withTemplate('LDAP-Prod') { template ->
        results = LdapUtil.withTemplate('LDAP-Prod') { template ->
            template.search("OU=Teladoc Users", "(${ldapQuery})", SearchControls.SUBTREE_SCOPE, { attributes ->
                attributes.get(attribute).get()
            } as AttributesMapper<String>)
        }
    } catch(Exception e1) {
        log.debug("***${issue} *** ManagerOf: ERROR EXCEPTION: ${e1}")
        log.error("***${issue} *** ManagerOf: ERROR EXCEPTION: ${e1}")
    }
    log.debug("retrieveLDAP_User - ${issue} - results: ${results}")
    return results
}

def retrieveManager(Issue issue, FormsField fieldToRead ) {
    log.debug("retrieveManager started - ${issue} - Field: ${fieldToRead}")
    log.setLevel(Level.DEBUG)
    String ldapQueryString, managerOU, manager
    switch (fieldToRead?.name.toLowerCase()){
        case "assignee":
            ldapQueryString = "mail=${issue.assignee.emailAddress}" as String
        break;
        case "reporter":
            ldapQueryString = "mail=${issue.reporter.emailAddress}" as String
        break;
        default:
            log.error("${issue} - retrieveManager method - You need to add ${fieldToRead?.name.toLowerCase()} to the 'Switch' statement.")
            return "retrieveManager method - You need to add ${fieldToRead?.name.toLowerCase()} to the 'Switch' statement."
        break;
    }
    managerOU = (retrieveLDAP_User issue, ldapQueryString, "manager").toString().replaceAll("[\\[\\]]", "")
    manager = retrieveLDAP_User (issue, "${managerOU.toString().substring(0, managerOU.toString().indexOf(",")) as String}", "userPrincipalName")
    log.debug("retrieveManager - ${issue} - manager: ${manager.toString().replaceAll("[\\[\\]]", "")},,, ${manager}")
    return ComponentAccessor.getUserManager().getUserByName("${manager.toString().replaceAll("[\\[\\]]", "")}") as ApplicationUser
}

def issueHistory(Issue myIssue, List<ChangeItemBean> fldCHG){
    DateTimeFormatter dtf = DateTimeFormatter.ofPattern('yyyy-MM-dd HH:mm:ss.SSS')
    Long timeDiff
    String createdString
    LocalDateTime nowDT = LocalDateTime.now()
    LocalDateTime createdDT
    log.setLevel(Level.DEBUG)
    
    log.debug("issueHistory started - ${myIssue} - fldCHG list: ${fldCHG}.")
    if(fldCHG){ //If there were changes to the specific field, lets review the most recent one
        createdString = fldCHG.last().created.toString() as String 
        //For some reason the createdString is dropping the succeeding 0's for milliseconds WHEN they exist
        def millisec = createdString.tokenize(".")[1]        
        for (int i = millisec.length(); i < 3; i++){
            log.debug("Concat a 0 to the createdString String.")
            createdString += "0"
        }
        createdDT = LocalDateTime.parse(createdString, dtf)
        log.debug("${myIssue} - Function: issueHistory - createdDT: ${createdDT}")
        log.debug("${myIssue} - Function: issueHistory - nowDT: ${nowDT}")
        //Get the time span between now and the time it was changed
        timeDiff = createdDT.until(nowDT, ChronoUnit.SECONDS) //LocalDateTime.now()
        log.debug("${myIssue} - Function: issueHistory - timeDiff: SECONDS: ${timeDiff}")
    //Do what we can to quit early, if the timeDiff is too long then it's probably an old change and fired event doesn't effect this field.
        if(timeDiff > 1){//if timeDiff is greater then 1 second, it's probably a duplicate run and shouldn't continue with the script.
            return false //"TimeDiff"
        }else{//if the timeDiff is less, then the change event is probably for the field and the script should continue to run.
            return true
        }
    }else{//
        return false  //Since there was allChanges and no fieldChanges, we should stop the script.
    }    
}
*/