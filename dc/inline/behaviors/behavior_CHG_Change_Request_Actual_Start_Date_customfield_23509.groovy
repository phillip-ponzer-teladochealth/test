/*
Creator: Jeff Melies
Purpose: Ensure Actual Start Date is before Actual End Date
*********IMPORTANT: Whatever changes you make on this script you may want to make the same on Actual End Date
Change log:
*/
import com.onresolve.jira.groovy.user.FieldBehaviours
import groovy.transform.BaseScript
import org.apache.log4j.Level
import org.apache.log4j.Logger 
log = Logger.getLogger("(CHG)Actual Start Date (${if(underlyingIssue){underlyingIssue?.getKey()}else{'NEW ISSUE'}})")
log.setLevel(Level.INFO)

@BaseScript FieldBehaviours fieldBehaviours

def actualStartDt = getFieldById(getFieldChanged()) 
def actualStartDtValue = actualStartDt?.getValue()// as Date 
def origActualStartDt = underlyingIssue?.getCustomFieldValue("Actual Start Date")

//Stop early if field is empty
if(! actualStartDt?.getValue()){
    log.warn("actualStartDt is empty, Stopping.  Action Name: ${getActionName()}")
    return
}
log.warn("actualStartDt: ${actualStartDt?.value}")
def projectName = issueContext?.projectObject?.name
def issueTypeName = issueContext.getIssueType()?.getName()
def actualEndtDt = getFieldByName("Actual End Date")
def projectedStart = getFieldByName("Projected Start Date")

Map changeTypeValue = [:]//underlyingIssue?.getCustomFieldValue("Change type") as Map 
String changeType0
def actualEndDtValue
//Did someone 
if(getActionName()?.toString() != "Create" || getActionName()?.toString() == null){
    //if(actualStartDt?.value == null){return}
    changeTypeValue = underlyingIssue?.getCustomFieldValue("Change type") as Map
    if(underlyingIssue?.getCustomFieldValue("Actual End Date")) {actualEndDtValue = underlyingIssue?.getCustomFieldValue("Actual End Date")}
    //if(underlyingIssue?.getCustomFieldValue("Actual Start Date")){actualStartDtValue = underlyingIssue?.getCustomFieldValue("Actual Start Date") as Date}
    if(changeTypeValue?.size() != 2){ return}

    changeType0 = changeTypeValue?.get(null).toString() 
}else{
    //if(! origActualStartDt){return}
    actualEndDtValue = getFieldByName("Actual End Date")?.getValue()// as Date
    //actualStartDtValue = getFieldByName("Actual Start Date")?.getValue() as Date
    changeTypeValue = getFieldByName("Change type")?.getValue() as Map
    if(changeTypeValue?.size() != 2){ return}

    changeType0 = changeTypeValue?.toString()?.tokenize(",")[0][1..-1]
}
log.warn("changeType0: ${changeType0}. Action Name: ${getActionName()?.toString()}, actualStartDtValue: ${actualStartDtValue}")

//Logging
log.warn("has started on ${if(underlyingIssue){underlyingIssue?.getKey()}else{'a New Issue'}}.")

//if(getFieldByName("Actual End Date")?.getValue()) {
//    def actualEndDtValue = actualEndDt.getValue() as Date 
//    if(actualEndDtValue as Date && actualStartDtValue as Date){
//        if(actualStartDtValue as Date > actualEndDtValue as Date){
//            actualStartDt?.setError("Actual Start Date must be before Actual End Date.")
//        }else{
//            actualStartDt.clearError()
//            if(changeTypeValue?.toString() == ~/^\[Retro, .*/){
//                projectedStart?.setFormValue(actualStartDtValue)
//            }
//        }
//    }
//}

if(changeType0 == "Retro"){
    if(actualStartDtValue){getFieldByName("Projected Start Date")?.setFormValue((actualStartDtValue as Date).format("dd/MMM/yy h:mm a"))}
}else{
    //def actualEndDtValue = getFieldByName("Actual End Date")?.getValue() //as Date
    if(actualStartDtValue && actualEndDtValue){
        if(actualStartDtValue as Date > actualEndDtValue as Date){
            actualStartDt?.setError("Actual Start Date must be before Actual End Date.")
        }else{
            actualStartDt.clearError()
        }
    }
}

log.warn("has completed on ${if(underlyingIssue){underlyingIssue?.getKey()}else{'a New Issue'}}.")

