//Creator: Jeff Melies
//Purpose: Set Priority (SL) depending on selection of both Impact & RFC Change Risk
//Change log:

import com.atlassian.jira.component.ComponentAccessor
import com.atlassian.jira.issue.Issue
import com.onresolve.jira.groovy.user.FieldBehaviours
import com.atlassian.jira.security.roles.ProjectRoleManager
import groovy.transform.BaseScript
import org.apache.log4j.Level
import org.apache.log4j.Logger

log = Logger.getLogger("(CHG)Impact (${if(underlyingIssue){underlyingIssue?.getKey()}else{'NEW ISSUE'}})")
log.setLevel(Level.INFO)

log.debug("Behaviour_CHG_Change-type Impact")
Issue issue = underlyingIssue 

@BaseScript FieldBehaviours fieldBehaviours
//Managers
def projectRoleManager = ComponentAccessor.getComponent(ProjectRoleManager)
def customFieldManager = ComponentAccessor.getCustomFieldManager()
def optionsManager = ComponentAccessor.getOptionsManager()

final roleName = 'Administrators'
def projectRole = projectRoleManager.getProjectRole(roleName)
def roleActors = projectRoleManager.getProjectRoleActors(projectRole, issueContext.projectObject)?.users*.emailAddress
final loggedInUser = ComponentAccessor.jiraAuthenticationContext.loggedInUser
final projectName = issueContext.projectObject.name
final issueTypeName = issueContext.getIssueType()?.getName()
def risk = getFieldByName("Risk")
def impact = getFieldById(getFieldChanged()) //Impact
final origImpact = underlyingIssue?.getCustomFieldValue("Impact")  //Original Impact value
def changeType = getFieldByName("Change type")
ArrayList changeTypeValue = changeType.value as ArrayList  //underlyingIssue?.getCustomFieldValue("Change type") as Map

if(! risk?.value){
    impact?.setReadOnly(true)
    return
}
log.debug("changeTypeValue.size() != 2 ${(changeTypeValue.size() != 2)}: ${changeTypeValue.size()}, changeType?.getValue() == '[]' ${(changeType?.getValue() == [])}: ${changeType?.getValue()}")
if(changeTypeValue.size() != 2){
    log.debug("One or both CHANGE TYPE fields IS NULL, EXITING EARLY. ${if(underlyingIssue){underlyingIssue?.getKey()}else{'a New Issue'}}")
    if(impact?.value){impact?.setReadOnly(true)?.setFormValue(null)}
    return
}

log.debug("Issue Type: ${issueTypeName}-'Impact Field' has started on ${if(underlyingIssue){underlyingIssue?.getKey()}else{'a New Issue'}}.")

if (getAction()?.id == null || !(getAction()?.id == 1 )){
    log.debug("Issue Type: ${issueTypeName}-'Impact Field' **EDIT & VIEW Screens**")
    //*******************************************************
    //*******Do this on Edit and View screens**************
    //*******************************************************
    //If someone changes the Risk field do the following
    log.debug("Issue Type: ${issueTypeName}-'Impact Field' Did someone change the Impact field?: " + (origImpact.toString() != impact.value.toString()))
    fieldSetup()
    //Administrators
    if (loggedInUser.getEmailAddress() in roleActors) {
        log.debug("Issue Type: ${issueTypeName}-'Impact Field' **EDIT & VIEW Screens** -- '(loggedInUser.getEmailAddress() in roleActors)'==== (${loggedInUser.getEmailAddress() in roleActors})")
        if((issue?.getStatus()?.getName() == "Draft") || 
            (issue?.getStatus()?.getName() == "Under Review") ||
            (issue?.getStatus()?.getName() == "Rejected") || 
            (issue?.getStatus()?.getName() == "Self Review")){ 
            // ONLY allow users in the Project Administrators Role to change these fields
            getFieldByName("Change type")?.setReadOnly(false)
            getFieldByName("Risk")?.setReadOnly(false)
            getFieldByName("Impact")?.setReadOnly(false)
        }
    }else{
        if(issue?.getStatus()?.getName() == "Draft"){
            log.debug("Behaviours: ${projectName} - 'Impact Field' **EDIT & VIEW Screens** -- The user ${loggedInUser.emailAddress} is not in the Administrators role and status is ${issue?.getStatus()?.getName()}, allowing Impact field to be modified. *******")
            getFieldById(getFieldChanged())?.setReadOnly(false)
            getFieldByName("Risk")?.setReadOnly(false)
            getFieldByName("Impact")?.setReadOnly(false)
        }else{
            log.debug("Behaviours: ${projectName} - 'Impact Field' **EDIT & VIEW Screens** -- The user ${loggedInUser.emailAddress} is not in the Administrators role or Draft status, leaving Impact field in read only. *******")
            //getFieldByName("Change type")?.setReadOnly(true)
            //getFieldByName("Risk")?.setReadOnly(true)
            getFieldByName("Impact")?.setReadOnly(true)
        }
    }
}else{ //This is the initial Create Action
    log.debug("Behaviours: ${projectName}, Issue Type: ${issueTypeName}-'Impact Field'  **Create Action**")
    //*******************************************************
    //*******Do this on create screen only*******************
    //*******************************************************
    fieldSetup()
}
log.debug("Behaviours: ${projectName}, Issue Type: ${issueTypeName}-'Impact Field' has completed on ${if(underlyingIssue){underlyingIssue?.getKey()}else{'a New Issue'}}.")

//Functions
def fieldSetup(){
    def impact = getFieldByName("Impact")
    def risk = getFieldByName("Risk")
    def formPriority = getFieldByName("Priority (SL)")
    def prioritySL = customFieldManager?.getCustomFieldObjectsByName("Priority (SL)")[0]
    def pr
    switch(impact?.getValue()){
        case "Significant / Large":
            getFieldByName("Impact")?.setDescription('Telepresence consult functionality interrupted or blocked. - Patient lives at risk - Outage of mission critical system(s) - System is offline or severely impacted for multiple customers across multiple practices. - IOS, Android, and Web browser connectivity are all impacted.')
            if(risk?.getValue() == "High"){
                pr = "Critical"
            }else if(risk?.getValue() == "Medium"){
                pr = "High"
            }else{
                pr = "Medium"
            }
        break;
        case "Moderate / Limited":
            getFieldByName("Impact")?.setDescription('Non telepresence consult functionality interrupted or blocked. - Outage for limited set/ type of customers or means of connection. - Majority of other customers are able to function normally.')
            if(risk?.getValue() == "High"){
                pr = "High"
            }else if(risk?.getValue() == "Medium"){
                pr = "Medium"
            }else{
                pr = "Low"
            }
        break;
        case "Minor / Localized":
            getFieldByName("Impact")?.setDescription('Non telepresence consult functionality interrupted or blocked for very limited number of customers. - Outage for a small set/ type of customers or means of connection. - Majority of other customers are able to function normally.')
            if(risk?.getValue() == "High"){
                pr = "Medium"
            }else if(risk?.getValue() == "Medium"){
                pr = "Low"
            }else{
                pr = "Very Low"
            }
        break;
        default:
            formPriority?.setHidden(false)?.setReadOnly(true)
            pr = "None"
        break;
    }
    def config = prioritySL?.getRelevantConfig(issueContext)
    def options = ComponentAccessor?.getOptionsManager()?.getOptions(config)
    def selectedOption 
    if(pr){
        selectedOption = options?.find { it?.value == pr }
        if(selectedOption){
            log.debug("Behaviours: ${issueContext?.projectObject?.name}, ${issueContext?.getIssueType()?.getName()}-'Impact Field' Priority (SL) set OPTION: ${selectedOption}")
            formPriority?.setFormValue(selectedOption?.optionId)
        }
    }
}