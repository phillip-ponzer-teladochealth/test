//Creator: Jeff Melies
//Purpose: Set Priority depending on selection of both Impact & Urgency
//Change log:

import com.atlassian.jira.user.ApplicationUser
import com.atlassian.jira.issue.IssueInputParameters
import com.atlassian.jira.issue.priority.Priority
import com.atlassian.jira.component.ComponentAccessor
import com.atlassian.jira.event.type.EventDispatchOption
import com.atlassian.jira.issue.Issue
import com.onresolve.jira.groovy.user.FieldBehaviours
import com.atlassian.jira.security.roles.ProjectRoleManager
import groovy.transform.BaseScript
import org.apache.log4j.Logger
import org.apache.log4j.Level
log.setLevel(Level.DEBUG)

Issue issue = underlyingIssue

@BaseScript FieldBehaviours fieldBehaviours
//Managers
def projectRoleManager = ComponentAccessor.getComponent(ProjectRoleManager)
def customFieldManager = ComponentAccessor.getCustomFieldManager()
def optionsManager = ComponentAccessor.getOptionsManager()

final projectName = issueContext.projectObject.name
log = Logger.getLogger("${issue}: Behaviours: ${projectName}-Field: Impact") 

final issueTypeName = issueContext.getIssueType().getName()
def impact = getFieldById(getFieldChanged()) //Impact
String origImpact = issue?.getCustomFieldValue("Impact").toString()  //Original Impact value
String urgencyValue = issue?.getCustomFieldValue("Urgency").toString()
//getFieldByName("Priority").setReadOnly(true)

if (!(origImpact || urgencyValue)){
    log.debug("'Impact' or 'Urgency' field is empty EXIT EARLY")
    return
}

log.debug("Has started on ${if(underlyingIssue){underlyingIssue?.getKey()}else{'a New Issue'}}.")

if (getAction()?.id == null || !(getAction()?.id == 1 )){
    log.debug("**EDIT & VIEW Screens**")
    //*******************************************************
    //*******Do this on Edit and View screens**************
    //*******************************************************
    //If someone changes the Impact field do the following
//    fieldSetup()               *******************ENABLE THIS TO GET THE AUTOMATION TO WORK******
}else{ //This is the initial Create Action
    log.debug("**Create action**")
    //*******************************************************
    //*******Do this on create screen only*******************
    //*******************************************************
//    fieldSetup()               *******************ENABLE THIS TO GET THE AUTOMATION TO WORK******
}
log.debug("Has completed on ${if(underlyingIssue){underlyingIssue?.getKey()}else{'a New Issue'}}.")

//Functions
def fieldSetup(){
    def issueService = ComponentAccessor.issueService
    Issue issue = underlyingIssue
    String impactValue = getFieldByName("Impact")?.value.toString().toLowerCase()
    String urgencyValue = getFieldByName("urgency")?.value.toString().toLowerCase()
    def availablePriorities = ComponentAccessor.constantsManager.priorities
    Priority newPriority
    def pr
    switch(impactValue){
        case "high (extensive/widespread)":
            if(urgencyValue == "high (immediately)"){
                newPriority = availablePriorities.find { it.name == "P1" }
            }else if(urgencyValue == "medium (hours)"){
                newPriority = availablePriorities.find { it.name == "P2" }
            }else{
                newPriority = availablePriorities.find { it.name == "P3" }
            }
        break;
        case "medium (significant/large)":
            if(urgencyValue == "high (immediately)"){
                newPriority = availablePriorities.find { it.name == "P2" }
            }else if(urgencyValue == "medium (hours)"){
                newPriority = availablePriorities.find { it.name == "P3" }
            }else{
                newPriority = availablePriorities.find { it.name == "P4" }
            }
        break;
        case "low (minor)":
            if(urgencyValue == "high (immediately)"){
                newPriority = availablePriorities.find { it.name == "P3" }
            }else if(urgencyValue == "medium (hours)"){
                newPriority = availablePriorities.find { it.name == "P4" }
            }else{
                newPriority = availablePriorities.find { it.name == "P5" }
            }
        break;
        default:
            getFieldByName("Priority").setHidden(false).setReadOnly(true)
            newPriority = null
        break;
    }
    assert newPriority : "Could not find priority name. Available priorities are ${availablePriorities*.name.join(", ")}"
    def issueInputParams = issueService.newIssueInputParameters() as IssueInputParameters
    issueInputParams.with {
        priorityId = newPriority.id
    }

    def updateValidationResult = issueService.validateUpdate(ComponentAccessor.jiraAuthenticationContext.loggedInUser, issue?.id, issueInputParams)
    assert updateValidationResult.valid : updateValidationResult.errorCollection

    def updateResult = issueService.update(ComponentAccessor.jiraAuthenticationContext.loggedInUser, updateValidationResult, EventDispatchOption.DO_NOT_DISPATCH, false)
    assert updateResult.valid : updateResult.errorCollection

}