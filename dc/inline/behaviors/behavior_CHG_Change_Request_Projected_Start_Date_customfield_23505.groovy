/*
Creator: Jeff Melies
Purpose: Ensure Projected Start Date is before Projected End Date
*********IMPORTANT: Whatever changes you make on this script you may want to make the same on Projected End Date
Change log:
*/
import com.onresolve.jira.groovy.user.FieldBehaviours
import groovy.transform.BaseScript
import org.apache.log4j.Level
import org.apache.log4j.Logger 
log = Logger.getLogger("(CHG)Projected Start Date (${if(underlyingIssue){underlyingIssue?.getKey()}else{'NEW ISSUE'}})")
log.setLevel(Level.INFO)

@BaseScript FieldBehaviours fieldBehaviours
//Map changeTypeValue = underlyingIssue?.getCustomFieldValue("Change type") as Map
//if(changeTypeValue?.size() != 2){return "Both Change type fields are not populated."}
def origProjStartDt
if(underlyingIssue){origProjStartDt = underlyingIssue?.getCustomFieldValue("Projected Start Date")}
def projectedStartDt = getFieldById(getFieldChanged())
//Exit early if field is null
if(!(projectedStartDt?.getValue() || origProjStartDt)){
    log.debug("projectedStartDt is empty, Stopping.  Action Name: ${getActionName()}")
    return
}
log.debug("projectedStartDt: ${projectedStartDt?.value}")
def projectName = issueContext?.projectObject?.name
def issueTypeName = issueContext?.getIssueType()?.getName()
def projectedEndDt = getFieldByName("Projected End Date")  
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
log.debug("changeType0: ${changeType0}. Action Name: ${getActionName()?.toString()}")

//Logging
log.debug("Started on ${if(underlyingIssue){underlyingIssue?.getKey()}else{'a New Issue'}}.")
def projectedStartDtValue = projectedStartDt?.getValue() as Date
def projectedEndDtValue
if(changeType0 != "Retro"){
    projectedStartDt?.setReadOnly(false)?.setHidden(false)?.setRequired(true)
    projectedEndDt?.setReadOnly(false)?.setHidden(false)?.setRequired(true)
    if(projectedEndDt?.getValue()) {projectedEndDtValue = projectedEndDt?.getValue() as Date}
    if(projectedEndDtValue && projectedStartDtValue){
        if(projectedStartDtValue as Date >= projectedEndDtValue as Date){
            projectedStartDt?.setError("Projected Start Date must be before Projected End Date.")
        }else{
            projectedStartDt?.clearError()
        }
    }
}else{
    projectedStartDt?.setReadOnly(true)?.setHidden(true)?.setRequired(false)//.setFormValue(null)
    projectedEndDt?.setReadOnly(true)?.setHidden(true)?.setRequired(false)
}
/*if(getFieldByName("Projected End Date")?.getValue()) {
    def projectedEndDtValue = projectedEndDt?.getValue() as Date 
    if(projectedEndDtValue && projectedStartDtValue){
        if(projectedStartDtValue >= projectedEndDtValue){
            projectedStartDt?.setError("Projected Start Date must be before Projected End Date.")
        }else{
            projectedStartDt?.clearError()
        }
    }
}*/
log.debug("Completed on ${if(underlyingIssue){underlyingIssue?.getKey()}else{'a New Issue'}}.")
