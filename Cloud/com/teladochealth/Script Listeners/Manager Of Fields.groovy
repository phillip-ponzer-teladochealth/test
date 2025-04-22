/*
Author: Jeff Melies
Purpose: Populates Manager of Assignee, Manager of Reporter, and/or Manager of Deployer
Requirements: 
    The 'mgr' user property must be set with managers atlassian id #, see scheduled job.
*/

import java.util.List
import java.lang.String
import com.mashape.unirest.http.Unirest.*
import groovy.transform.Field as tField
import org.apache.log4j.*


@tField log_level = Level.INFO    //Options would be (Level.INFO, Level.WARN, Level.DEBUG, Level.ERROR) without quotes.
@tField logName = "Listener-Manager of Fields"
@tField getUsrPrpURL = "/rest/api/3/user/properties"
//**********************************FUNCTIONS*************************************
def setManagerField(Map fieldDetails){
    def log = Logger.getLogger(logName?.toString())
    log.setLevel(log_level as Level)

    def setUserPickerField = put("/rest/api/3/issue/${issue.key}")// + issue.key)
        .header('Content-Type', 'application/json')
        .body([
            fields: [
                (fieldDetails.keySet().first()): [id: fieldDetails.get(fieldDetails.keySet().first())]
            ]
        ])
        .asString()

    if(setUserPickerField.status == 204){
        log.debug("(${issue.key})-setManagerField: Field changed successfully.")
        return true
    }else{
        log.error("(${issue.key})-setManagerField: API Status: ${setUserPickerField.status}, ${setUserPickerField.statusText}")
        return false
    }
}

def readUserProperty(String usrPrp, String atlasId){
    def log = Logger.getLogger(logName?.toString())
    log.setLevel(log_level as Level)

    try{
        get(getUsrPrpURL?.toString().concat("/${usrPrp}?accountId=${atlasId}")?.toString())
            .asObject(Map)
            .body

    }catch(e) {
        return null
    }   
}
//*********************** Main Script ***********************
Logger log = Logger.getLogger(logName?.toString()) as Logger
log.setLevel(log_level as Level)

def managerOfField, field_ManagerId
String jiraField, eventString 

//Which field changed (Reporter or Assignee)
def assigneeChange = changelog?.items.find { it['field'] == 'assignee' } ?: null
def reporterChange = changelog?.items.find { it['field'] == 'reporter' } ?: null
def deployerChange = changelog?.items.find { it['field'] == 'Deployer' } ?: null
if(!assigneeChange && !reporterChange && !deployerChange) {return "The Reporter, Assignee, or Deployer didn't changed."}
log.debug("(${issue.key})-MAIN: assigneeChange: ${assigneeChange}, reporterChange: ${reporterChange}, deployerChange: ${deployerChange}")

log.debug("(${issue.key})-MAIN: Listener started")
log.debug("(${issue.key})-MAIN: Changelog: ${changelog}")
//Get the event that fired, created ('jira:issue_created') or updated ('jira:issue_updated')?
eventString = webhookEvent?.toString()
log.debug("(${issue.key})-MAIN: What event fired: eventString: ${eventString}") //, ${event.issue}")

// Get  all custom fields
def customFields = get("/rest/api/3/field")
        .asObject(List)
        .body
        .findAll { (it as Map).custom } as List<Map>

//If the issue was just created then we should populate Manager of Reporter at a minimum.
if (eventString == 'jira:issue_created') {
    log.debug("(${issue.key})-MAIN: Issue created: ${issue.key}.")
    jiraField = "New_Issue"
    managerOfField = customFields.find { it.name == 'Manager of Reporter' }?.id ?: null
    String mgrId = (readUserProperty ("mgr", assigneeChange?.tmpToAccountId?.toString()) as Map)?.value ?: null
    field_ManagerId = [(managerOfField):mgrId] as Map
}else{
    if (assigneeChange){
        log.debug("(${issue.key})-MAIN: Assignee updated: ${issue.key}.")
        jiraField = "Assignee"
        managerOfField = customFields.find { it.name == 'Manager of Assignee' }?.id ?: null
        String mgrId = (readUserProperty ("mgr", assigneeChange?.tmpToAccountId?.toString()) as Map)?.value ?: null
        field_ManagerId = [(managerOfField):mgrId] as Map
    }
    if(reporterChange){
        log.debug("(${issue.key})-MAIN: Reporter updated: ${issue.key}.")
        jiraField = "Reporter"
        managerOfField = customFields.find { it.name == 'Manager of Reporter' }?.id ?: null
        String mgrId = (readUserProperty ("mgr", assigneeChange?.tmpToAccountId?.toString()) as Map)?.value ?: null
        field_ManagerId = [(managerOfField):mgrId] as Map
    }
    if(deployerChange){
        log.debug("(${issue.key})-MAIN: Deployer updated: ${issue.key}.") 
        jiraField = "Deployer"
        managerOfField = customFields.find { it.name == 'Manager of Deployer' }?.id ?: null
        String mgrId = (readUserProperty ("mgr", assigneeChange?.tmpToAccountId?.toString()) as Map)?.value ?: null
        field_ManagerId = [(managerOfField):mgrId] as Map
    }
}
log.debug("(${issue.key})-MAIN: Field: ${jiraField}, field_ManagerId: ${field_ManagerId}")
setManagerField(field_ManagerId)

log.debug("(${issue.key})-MAIN: Listener ended on ${jiraField}")