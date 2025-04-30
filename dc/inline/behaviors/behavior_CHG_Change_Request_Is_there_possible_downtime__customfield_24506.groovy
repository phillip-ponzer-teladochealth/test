/*
Creator: Jeff Melies
Purpose: 'Is there possible downtime?' field -> Display additional fields if Yes
Change log:
*/
//imports
import com.atlassian.jira.component.ComponentAccessor
import com.atlassian.jira.issue.Issue
import com.onresolve.jira.groovy.user.FieldBehaviours
import groovy.transform.BaseScript
import org.apache.log4j.Logger
import org.apache.log4j.Level
log.setLevel(Level.INFO)
log = Logger.getLogger("(CHG)Is there possible downtime? (${if(underlyingIssue){underlyingIssue?.getKey()}else{'NEW ISSUE'}})")

Issue issue = UnderlyingIssue

@BaseScript FieldBehaviours fieldBehaviours
def origDowntimeValue
if(underlyingIssue){origDowntimeValue = underlyingIssue?.getCustomFieldValue("Is there possible downtime?")}
def downtime = getFieldById(getFieldChanged())
//Exit early if field is null
if(!(downtime?.getValue() || origDowntimeValue)){
    log.warn("downtime is empty, Stopping.  Action Name: ${getActionName()}")
    getFieldByName("Estimated downtime")?.setHidden(true)?.setRequired(false)?.setFormValue(null)
    return
} 
def projectName = issueContext?.projectObject?.name
def issueTypeName = issueContext?.getIssueType()?.getName()
def loggedInUser = ComponentAccessor?.jiraAuthenticationContext?.loggedInUser
def estimatedDowntime = getFieldByName("Estimated downtime")
def downtimeValue = downtime?.getValue()


Map changeTypeValue = [:]
String changeType0
//Did someone 
if(getActionName()?.toString() != "Create" || getActionName()?.toString() == null){
    changeTypeValue = underlyingIssue?.getCustomFieldValue("Change type") as Map
    if(changeTypeValue?.size() != 2){ return}

//    changeType0 = changeTypeValue?.get(null).toString() 
}else{
    changeTypeValue = getFieldByName("Change type").getValue() as Map
    if(changeTypeValue?.size() != 2){ return}

    //changeType0 = changeTypeValue?.toString()?.tokenize(",")[0][1..-1]
}
changeType0 = changeTypeValue[0].toString() //?.get(null)?.toString() 
log.warn("changeType0: ${changeType0}. Action Name: ${getActionName()?.toString()}")

//if(changeType0 != "Retro"){
    log.warn("Started on ${if(underlyingIssue){underlyingIssue?.getKey()}else{'a New Issue'}}.")
    if(downtimeValue == "Yes"){
        estimatedDowntime?.setHidden(false)?.setRequired(true)
    }else{
        estimatedDowntime?.setHidden(true)?.setRequired(false)?.setFormValue(null)
    }
//}else{
//    estimatedDowntime?.setHidden(true)?.setRequired(false)?.setFormValue(null)
//}
log.warn("Completed on ${if(underlyingIssue){underlyingIssue?.getKey()}else{'a New Issue'}}.")
