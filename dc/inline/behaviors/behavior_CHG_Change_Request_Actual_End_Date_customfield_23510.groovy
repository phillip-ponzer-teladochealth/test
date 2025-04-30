/*
Creator: Jeff Melies
Purpose: Ensure Actual End Date is after Actual Start Date
*********IMPORTANT: Whatever changes you make on this script you may want to make the same on Actual Start Date
Change log:
*/
import com.onresolve.jira.groovy.user.FieldBehaviours
import groovy.transform.BaseScript
import org.apache.log4j.Level
import org.apache.log4j.Logger 
log = Logger.getLogger("(CHG)Actual End Date (${if(underlyingIssue){underlyingIssue?.getKey()}else{'NEW ISSUE'}})")
log.setLevel(Level.INFO)

@BaseScript FieldBehaviours fieldBehaviours

def actualEndDt = getFieldById(getFieldChanged())
def actualEndDtValue = actualEndDt?.getValue()// as Date 
def origActualEndDt = underlyingIssue?.getCustomFieldValue("Actual End Date")

//Exit early is field is null
if(! actualEndDt?.getValue()){
    log.warn("actualEndDt is empty, Stopping.  Action Name: ${getActionName()}")
    return
}

def projectName = issueContext?.projectObject?.name
def issueTypeName = issueContext?.getIssueType()?.getName()
def actualStartDt = getFieldByName("Actual Start Date")
def projectedEnd = getFieldByName("Projected End Date")

Map changeTypeValue = [:]//underlyingIssue?.getCustomFieldValue("Change type") as Map 

String changeType0
//Did someone 
if(getActionName()?.toString() != "Create" || getActionName()?.toString() == null){
    changeTypeValue = underlyingIssue?.getCustomFieldValue("Change type") as Map
    if(changeTypeValue?.size() != 2){ return}

    changeType0 = changeTypeValue?.get(null).toString() 
}else{
    changeTypeValue = getFieldByName("Change type").getValue() as Map
    if(changeTypeValue?.size() != 2){ return}

    changeType0 = changeTypeValue?.toString()?.tokenize(",")[0][1..-1]
}
log.warn("changeType0: ${changeType0}. Action Name: ${getActionName()?.toString()}")

log.warn("has started on ${if(underlyingIssue){underlyingIssue?.getKey()}else{'a New Issue'}}.")

if(changeType0 == "Retro"){
    if(actualEndDtValue){getFieldByName("Projected End Date")?.setFormValue((actualEndDtValue as Date).format("dd/MMM/yy h:mm a"))}
}else{
    def actualStartDtValue
    if(actualStartDt?.getValue()){actualStartDtValue = actualStartDt?.getValue() as Date}
    if(actualEndDtValue && actualStartDtValue){
        if(actualEndDtValue as Date < actualStartDtValue as Date){
            actualEndDt?.setError("Actual End Date must be after Actual Start Date.")
        }else{
            actualEndDt.clearError()
        }
    }
}
log.warn("has completed on ${if(underlyingIssue){underlyingIssue?.getKey()}else{'a New Issue'}}.")
