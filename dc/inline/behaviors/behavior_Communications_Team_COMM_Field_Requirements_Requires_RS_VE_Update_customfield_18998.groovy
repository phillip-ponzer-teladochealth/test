/*Creator: Jeff Melies
Purpose: For "Bug","Task","Story" Issue Types If /
         Required RS/VE Update is Yes, make Requirements Documentation /
         a required field.
*/

//Imports
import com.onresolve.jira.groovy.user.FieldBehaviours
import groovy.transform.BaseScript
import static com.atlassian.jira.issue.IssueFieldConstants.*
    
@BaseScript FieldBehaviours fieldBehaviours

//Logging
log.info("Behaviours COMMS Project  'Requires RS/VE Update' has started")

// This listens if the "Requires RS/VE Update" field has changed
def reqRSVEUpdate = getFieldById(getFieldChanged())
log.info("Requires RS/VE Update field: " + reqRSVEUpdate.value)
def issueTypeField = getFieldById(ISSUE_TYPE)  //Get Issue Type Field object
def reqDocumentation = getFieldByName("Requirements Documentation")

//*******Only execute if the Issue Type is Bug, Task, and Story see Jira-721
log.info("Issue Type Field value: " + issueTypeField.value)
if (!(["Bug","Task","Story"].contains(issueTypeField.value))) {}
//reqRSVEUpdate.setRequired(true)
if (reqRSVEUpdate.value == "Yes"){
    reqDocumentation.setHidden(false)
    reqDocumentation.setRequired(true)
}else{
    reqDocumentation.setHidden(true)
    reqDocumentation.setRequired(false)
}

log.info("Behaviours COMMS Project  'Requires RS/VE Update' has completed.")