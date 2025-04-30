import org.apache.log4j.Logger
import org.apache.log4j.Level
log.setLevel(Level.INFO)
log = Logger.getLogger("(CHG)Expedite Reason (${if(underlyingIssue){underlyingIssue?.getKey()}else{'NEW ISSUE'}})")

def expediteReason = getFieldById(getFieldChanged())
def changeType = getFieldByName("Change type")
ArrayList changeTypeValue = changeType.value as ArrayList

//if(changeTypeValue.size() != 2){
//    log.debug("One or both CHANGE TYPE fields IS NULL, EXITING EARLY. ${if(underlyingIssue){underlyingIssue?.getKey()}else{'a New Issue'}}")
//    return
//}

if (expediteReason?.value){
    log.debug("Showing")
    getFieldByName("Expedite Justification")?.setHidden(false)?.setRequired(true)
}else{
    log.debug("Hidding")
    getFieldByName("Expedite Justification")?.setHidden(true)?.setRequired(false)?.setFormValue(null)
    //expediteReason?.setFormValue(null)
}