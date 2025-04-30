/*
Creator: Jeff Melies
Purpose: 'Is there data impact?' field -> Display additional fields if Yes
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
log = Logger.getLogger("(CHG)Is There Data Impact? (${if(underlyingIssue){underlyingIssue?.getKey()}else{'NEW ISSUE'}})")

Issue issue = UnderlyingIssue

@BaseScript FieldBehaviours fieldBehaviours

def dataImpact = getFieldById(getFieldChanged())
def origDataImpactValue

def projectName = issueContext?.projectObject?.name
def issueTypeName = issueContext?.getIssueType()?.getName()
def loggedInUser = ComponentAccessor?.jiraAuthenticationContext?.loggedInUser
def structureSchemaChanges = getFieldByName("Structure/Schema Changes")
def additionalData = getFieldByName("Additional Data")
def currentDataChanges = getFieldByName("Current Data Changes")
def businessFunctionalityImpacts = getFieldByName("Business Functionality Impacts")
def dataImpactValue = dataImpact?.getValue()
Map changeTypeValue = [:]
String changeType0

if(underlyingIssue){origDataImpactValue = underlyingIssue?.getCustomFieldValue("Is there data impact?")}
//Exit early if field is null
if(!dataImpact?.getValue()){
    log.warn("dataImpact is empty, Stopping.  Action Name: ${getActionName()}")
    structureSchemaChanges?.setHidden(true)?.setRequired(false)
    additionalData?.setHidden(true)?.setRequired(false)
    currentDataChanges?.setHidden(true)?.setRequired(false)
    businessFunctionalityImpacts?.setHidden(true)?.setRequired(false)
    return
} 

//Did someone 
if(getActionName()?.toString() != "Create" || getActionName()?.toString() == null){
    changeTypeValue = underlyingIssue?.getCustomFieldValue("Change type") as Map
    if(changeTypeValue?.size() != 2){ return}

    //changeType0 = changeTypeValue?.get(null).toString() 
}else{
    changeTypeValue = getFieldByName("Change type")?.getValue() as Map
    if(changeTypeValue?.size() != 2){ return}

    //changeType0 = changeTypeValue?.toString()?.tokenize(",")[0][1..-1]
    //changeType0 = changeTypeValue?.get(null).toString()
}
changeType0 = changeTypeValue[0].toString() //?.get(null)?.toString()
log.warn("changeType0: ${changeType0}. Action Name: ${getActionName()?.toString()}")

log.warn("Started on ${if(underlyingIssue){underlyingIssue?.getKey()}else{'a New Issue'}}.")
//if(changeType0 != "Retro"){
    if(dataImpactValue == "Yes"){
        structureSchemaChanges?.setHidden(false)?.setRequired(true)
        additionalData?.setHidden(false)?.setRequired(true)
        currentDataChanges?.setHidden(false)?.setRequired(true)
        businessFunctionalityImpacts?.setHidden(false)?.setRequired(true)
    }else{
        structureSchemaChanges?.setHidden(true)?.setRequired(false)
        additionalData?.setHidden(true)?.setRequired(false)
        currentDataChanges?.setHidden(true)?.setRequired(false)
        businessFunctionalityImpacts?.setHidden(true)?.setRequired(false)
    }
//}else{
//    dataImpact?.setHidden(true)?.setRequired(false)?.setFormValue(null)
//    structureSchemaChanges?.setHidden(true)?.setRequired(false)?.setFormValue(null)
//    additionalData?.setHidden(true)?.setRequired(false)?.setFormValue(null)
//    currentDataChanges?.setHidden(true)?.setRequired(false)?.setFormValue(null)
//    businessFunctionalityImpacts?.setHidden(true)?.setRequired(false)?.setFormValue(null)
//}
log.warn("Completed on ${if(underlyingIssue){underlyingIssue?.getKey()}else{'a New Issue'}}.")
