/*
Creator: Jeff Melies
Purpose: Projected End Date is before Projected Start Date
Change log:
*/
import com.onresolve.jira.groovy.user.FieldBehaviours
import groovy.transform.BaseScript
import org.apache.log4j.Level
import org.apache.log4j.Logger
log.setLevel(Level.INFO)
log = Logger.getLogger("(CHG)Projected End Date (${if(underlyingIssue){underlyingIssue?.getKey()}else{'NEW ISSUE'}})")

@BaseScript FieldBehaviours fieldBehaviours
def origProjEndDt
if(underlyingIssue){origProjEndDt = underlyingIssue?.getCustomFieldValue("Projected End Date")}
def projectedEndDt = getFieldById(getFieldChanged())
//Exit early if field is null 
if(!(projectedEndDt?.getValue() || origProjEndDt)){
    log.debug("projectedEndDt is empty, Stopping.  Action Name: ${getActionName()}")
    return
}
def projectName = issueContext?.projectObject?.name
def issueTypeName = issueContext?.getIssueType()?.getName()
def projectedStartDt = getFieldByName("Projected Start Date") 
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
log.debug("has started on ${if(underlyingIssue){underlyingIssue?.getKey()}else{'a New Issue'}}.")
Date projectedEndDtValue = getFieldById(getFieldChanged())?.getValue() as Date 
def projectedStartDtValue
if(changeType0 != "Retro"){
    projectedEndDt?.setReadOnly(false)?.setHidden(false)?.setRequired(true)
    projectedStartDt?.setReadOnly(false)?.setHidden(false)?.setRequired(true)
    if(projectedStartDt?.getValue()){projectedStartDtValue = projectedStartDt?.getValue() as Date}
    if(projectedEndDtValue && projectedStartDtValue){
        if(projectedEndDtValue as Date <= projectedStartDtValue as Date){
            projectedEndDt?.setError("Projected End Date must be after Projected Start Date.")
        }else{
            projectedEndDt?.clearError()
        }
    }
}else{
    projectedEndDt?.setReadOnly(true)?.setHidden(true)?.setRequired(false)
    projectedStartDt?.setReadOnly(true)?.setHidden(true)?.setRequired(false)
}


/*if(getFieldByName("Projected Start Date")?.getValue()) {
    def projectedStartDtValue = projectedStartDt?.getValue() as Date 
 
    if(projectedEndDtValue && projectedStartDtValue){
        if(projectedEndDtValue <= projectedStartDtValue){
            projectedEndDt?.setError("Projected End Date must be after Projected Start Date.")
        }else{
            projectedEndDt?.clearError()
        }
    }
}*/
log.debug("has completed on ${if(underlyingIssue){underlyingIssue?.getKey()}else{'a New Issue'}}.")
