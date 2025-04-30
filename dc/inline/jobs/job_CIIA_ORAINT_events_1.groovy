/*
Author: Jeff Melies
Purpose: Set Priority according to email
Change Log:
*/

import com.atlassian.jira.component.ComponentAccessor
import com.atlassian.jira.event.type.EventDispatchOption

def currentIssue = ComponentAccessor.issueManager.getIssueObject(event.issue.id);
//If issue is not a Task then quit
log.warn("If Task != " + currentIssue.getIssueType().name.toString() + " than quit.")
if (currentIssue.getIssueType().name.toString() != "Task"){
    log.warn("Task != " + currentIssue.getIssueType().name.toString() + " quitting.")
	return;
}
log.warn("Listener: Project: ${currentIssue.getProjectObject().key} - Set_Priority_and_Summary has started")

//def loggedInUser = ComponentAccessor.jiraAuthenticationContext.loggedInUser
//log.warn("loggedInUser: ${loggedInUser}")
def issueService = ComponentAccessor.issueService

final boolean sendMail = false


def eventTypeManager = ComponentAccessor.getEventTypeManager()
def eventTypeName = eventTypeManager.getEventType(event.eventTypeId).getName()
def reporter = currentIssue.getReporter()
log.warn("EVENT TYPE NAME, reporter --> "+ eventTypeName + ", " + reporter)

String setPriority
switch(currentIssue.getSummary()) {
    case ~/Priority:1.*/:
    setPriority = "Highest"
//    log.warn(" Highest")
    break
    case ~/Priority:2.*/:
    setPriority = "High"
//    log.warn(" High")
    break
    case ~/Priority:3.*/:
    setPriority = "Medium"
//    log.warn(" Medium")
    break
    case ~/Priority:4.*/:
    setPriority = "Low"
//    log.warn(" Low")
    break
    case ~/Priority:5.*/:
    setPriority = "Lowest"
//    log.warn(" Lowest")
    break
    default:
        setPriority = "Low"
//    	log.warn(" Default")
    break
}

def availablePriorities = ComponentAccessor.constantsManager.priorities
def myPriority = availablePriorities.find { it.name == setPriority }

def issueInputParams = issueService.newIssueInputParameters()
issueInputParams.with {
    priorityId = myPriority.id
}

//def summary = currentIssue.getSummary()

//if(!(reporter.getEmailAddress().toLowerCase().matches("oracleerpsupport@teladoc.com") || reporter.getEmailAddress().toLowerCase().matches("oracleepmsupport@teladoc.com") || reporter.getEmailAddress().toLowerCase().matches("mulesoftsupport@teladochealth.com") || reporter.getEmailAddress().toLowerCase().matches("mulesoftalerts@teladochealth.com"))) {
//    summary = summary + " - User Reported"
//    issueInputParams.setSummary(summary)
//}

def svcUser = ComponentAccessor.getUserManager().getUserByName("it_worker") //	svc_Automation for Jira

def updateValidationResult = issueService.validateUpdate(svcUser, currentIssue?.id, issueInputParams)
assert updateValidationResult.valid : updateValidationResult.errorCollection

def updateResult = issueService.update(svcUser, updateValidationResult, EventDispatchOption.ISSUE_UPDATED, sendMail)
assert updateResult.valid : updateResult.errorCollection

log.warn("Listener: Project: ${currentIssue.getProjectObject().key} - Set_Priority_and_Summary has completed")