/*
Creator: Jeff Melies
Purpose: Change Type field
Change log:
*/
//imports
import com.atlassian.jira.issue.customfields.option.Options
import com.atlassian.jira.component.ComponentAccessor
import com.atlassian.jira.security.roles.ProjectRoleActors
import com.atlassian.jira.security.roles.ProjectRole
import com.atlassian.jira.security.roles.ProjectRoleManager
import static com.atlassian.jira.issue.IssueFieldConstants.COMPONENTS
import com.atlassian.jira.issue.Issue
import com.onresolve.jira.groovy.user.FieldBehaviours
import groovy.transform.BaseScript
import org.apache.log4j.Level
import org.apache.log4j.Logger

log = Logger.getLogger("(CHG)Change-type (${if(underlyingIssue){underlyingIssue?.getKey()}else{'NEW ISSUE'}})")
log.setLevel(Level.INFO)

Issue issue = UnderlyingIssue

@BaseScript FieldBehaviours fieldBehaviours
def projectRoleManager = ComponentAccessor.getComponent(ProjectRoleManager)
def optionsManager = ComponentAccessor.getOptionsManager()

Map origChangeTypeValue
if(issue){origChangeTypeValue = issue?.getCustomFieldValue("Change type") as Map}
def changeType = getFieldById(getFieldChanged())
def projectName = issueContext?.projectObject?.name
def issueTypeName = issueContext?.getIssueType()?.getName()
def origChangeTypeKey = origChangeTypeValue?.keySet()
Map changeTypeValue
//log.warn("changeTypeValue: ${changeTypeValue}")
//if(getActionName()?.toString() == 'Create'){
//    log.warn("Line 35.   changeTypeValue?.size(): ${changeTypeValue?.size()}")
//    if(changeType?.keySet()?.size() == 2){
//        log.warn("Line 37 changeTypeValue size:${changeTypeValue?.size()}")
//        return
//    }
//}else{
//    log.warn("Line 41")
//    if(origChangeTypeValue?.size() != 2){
//        log.warn("Line 43 Change type size:${origChangeTypeValue?.size()}")
//        return
//    }
//}

String changeType0
//Did someone Change the Change type field? Modifythe Description and set the changeType0 variable
if(changeType?.getValue()?.toString() != origChangeTypeValue?.values()?.toString()){
    log.warn("The issue was modified or is new. Action Name: ${getActionName()}")
    changeTypeValue = getFieldByName("Change type")?.getValue() as Map //changeType?.getValue() as Map
    if(changeTypeValue?.size() < 1 && origChangeTypeValue?.size() < 1){
        log.warn("One or both Change type fields are empty, update description and Exit Early.")
        changeType.setDescription("Please select a Change type.")
        getFieldByName("Risk")?.setReadOnly(true)
        getFieldByName("Impact")?.setReadOnly(true)
        return
    }
    if(changeTypeValue?.size() == 2){
        log.warn("changeTypeValue-Type: ${changeTypeValue.getProperties()}. Action Name: ${getActionName()?.toString()}")
        log.warn("Both Change type fields are populated, moving on.")
        changeType0 = changeTypeValue?.toString()?.tokenize(",")[0][1..-1]  //get(null).toString()        
    }else{
        log.warn("One of the Change type fields is empty, update description and Exit Early.")
        changeType.setDescription("""<b>***** Please populate both fields *****</b>""")
        getFieldByName("Risk")?.setReadOnly(true)
        getFieldByName("Impact")?.setReadOnly(true)
        return "Both Change type fields are not populated."
    }
}else{
    log.warn("underlyingIssue: ${underlyingIssue}. Action Name: ${getActionName()}")
    changeType0 = origChangeTypeValue?.get(null).toString()     
}
log.warn("changeType0: ${changeType0}. Action Name: ${getActionName()?.toString()}")

log.warn("changeType: ${changeType*.value?.size()}")
log.warn("changeTypeValue?.size(): ${changeTypeValue?.size()}, origChangeTypeValue?.size(): ${origChangeTypeValue?.size()}")
log.warn("Both Change Type ${changeType?.getValue()?.toString()}, underlyingIssue-origChangeTypeValue: ${underlyingIssue?.getCustomFieldValue("Change type")?.values()?.toString()}, ${origChangeTypeValue?.values()?.toString()}")


def roleName = 'Administrators'
def projectRole = projectRoleManager?.getProjectRole(roleName)
def roleActors = projectRoleManager?.getProjectRoleActors(projectRole, issueContext?.projectObject).users*.emailAddress
def loggedInUser = ComponentAccessor?.jiraAuthenticationContext?.loggedInUser
def expediteReason = getFieldByName("Expedite Reason")
def expeditJustification = getFieldByName("Expedite Justification")
def changeReason = getFieldByName("Change Reason")
def impact = getFieldByName("Impact")
def risk = getFieldByName("Risk")
def environment = getFieldByName("Environment (MS)")
def priority = getFieldByName("Priority (SL)")

log.warn("(${(getActionName()?.toString() == "Create")}), getActionName: ${getActionName()?.toString()}")
//log.warn("(${(getActionName() == null && origChangeTypeValue.toString() != changeTypeValue.toString())}), getActionName: ${getActionName()?.toString()}")
//log.warn("(${(origChangeTypeValue.toString() != changeTypeValue.toString())}), origChangeTypeValue: ${origChangeTypeValue.toString()} = changeTypeValue: ${changeTypeValue.toString()}")
//log.warn("Stop Running script? ${(getActionName()?.toString() == "Create") || (getActionName() == null && origChangeTypeValue.toString() == changeTypeValue.toString())}")
///if (getActionName() == null && origChangeTypeValue.toString() == changeTypeValue.toString()){
//    log.warn("It's not a new issue or Change Type hasn't been modified or Change Type fields aren't populated, stop running script.")
//    fieldSetup changeTypeValue[0].toString().replaceAll("[\\[\\](){}]",""), "descOnly"
//    return "It's not a new issue or Change Type hasn't been modified or Change Type fields aren't populated."
//}
//Set Description
switch(changeType0){  //changeTypeValue.get(null).toString()){   //[0].toString().replaceAll("[\\[\\](){}]","")){
    case "Standard":
        changeType?.setDescription('Pre-approved change that is low risk, relatively common and follows a specified procedure or work instruction approved by Peer/Manager.')
        getFieldById('issuelinks')?.setDescription("""Begin typing to search for issues to link. If you leave it blank, no link will be made.""")
        expediteReason?.setHidden(true)?.setRequired(false)
        expeditJustification?.setHidden(true)?.setRequired(false)
        getFieldByName("Undocumented Justification")?.setHidden(true)?.setRequired(false)
    break;
    case "CAB":
        changeType?.setDescription('Change follows a prescriptive process which require a full range of assessments and authorizations such as peer or technical approval, manager, QA, SOC, Change Management, and Change Advisory Board (CAB) authorization, to ensure completeness, accuracy, and the least possible disruption to service.')
        getFieldById('issuelinks')?.setDescription("""Begin typing to search for issues to link. If you leave it blank, no link will be made.""")
        expediteReason?.setHidden(true)?.setRequired(false)
        expeditJustification?.setHidden(true)?.setRequired(false)
        getFieldByName("Undocumented Justification")?.setHidden(true)?.setRequired(false)
    break;
    case "Expedite":
        changeType?.setDescription('Change does not meet the lead time requirement for a CAB change but is not an Emergency Change. Changes that are required quickly due to a pressing need such as legal requirement or a business need or restoring service for a standard incident (P3) that is not a Major Incident. Sr Management approval required for expedited change. A failure to plan does not constitute an expedite.')
        getFieldById('issuelinks')?.setDescription("""Begin typing to search for issues to link. If you leave it blank, no link will be made.""")
        expediteReason?.setHidden(false)?.setRequired(true)
        expeditJustification?.setHidden(false)?.setRequired(true)
        getFieldByName("Undocumented Justification")?.setHidden(true)?.setRequired(false)
    break;
    case "Emergency":
    getFieldById('issuelinks')?.setDescription("""Begin typing to search for issues to link. If you leave it blank, no link will be made.""")
        changeType?.setDescription('Emergency changes cannot wait for review during the next CAB meeting before it is implemented. An emergency change is required to resolve a Major Incident (P1 / P2) in the production environment that is has significant impact on performance or to the end users’ ability to perform their duties. In addition, there is an active Major Incident bridge. A failure to plan does not constitute an emergency. There is an active ongoing major incident bridge.')
        expediteReason?.setHidden(true)?.setRequired(false)
        expeditJustification?.setHidden(true)?.setRequired(false)
        getFieldByName("Undocumented Justification")?.setHidden(true)?.setRequired(false)
    break;
    case "Retro":
        log.warn("This is RETRO")
        changeType?.setDescription("""Retro change is an undocumented change that has already been deployed but was not properly submitted on a CHG ticket. For audit and metrics purposes, the CHG must be submitted.""")
        getFieldById('issuelinks')?.setDescription("""<b>For Retro changes:</b> link Jira cards that originally documented the change.<br>Begin typing to search for issues to link. If you leave it blank, no link will be made.""")
        expediteReason?.setHidden(true)?.setRequired(false)
        expeditJustification?.setHidden(true)?.setRequired(false)
    break;
    default:
        changeType?.setDescription('Please select a Change type.')
        getFieldById('issuelinks')?.setDescription("""Begin typing to search for issues to link. If you leave it blank, no link will be made.""")
        expediteReason?.setHidden(true)?.setRequired(false)
        expeditJustification?.setHidden(true)?.setRequired(false)
        getFieldByName("Undocumented Justification")?.setHidden(true)?.setRequired(false)
    break;
}

log.warn("has started on ${if(underlyingIssue){underlyingIssue?.getKey()}else{'a New Issue'}}.")
log.warn("getAction.id: ${getAction()?.id}, if Statement: ${getAction()?.id == null || getAction()?.id != 1 }")
if (getActionName()?.toString() != "Create" || getActionName()?.toString() == null) { //(getAction()?.id == null || getAction()?.id != 1 ){
    //**************************************************************
    log.warn("*******Do this on Edit and View screens**************")
    //**************************************************************
    //If someone modifies the Change type field, we should clear the Environment (MS), Risk, and Impact fields
    //log.warn("(${(getFieldById(getFieldChanged()).getValue() != "[:]")}), origChangeTypeValue.toString(): ${origChangeTypeValue.toString()},  changeTypeValue.toString(): ${changeTypeValue.toString()}")
    log.warn("Did someone change the Change type field?: (${(changeType?.getValue()?.toString() != origChangeTypeValue?.values()?.toString())})") //(origChangeTypeValue.toString() != changeTypeValue.toString()))
    if(changeType?.getValue()?.toString() != origChangeTypeValue?.values()?.toString()){
    //if(getFieldById(getFieldChanged()).getValue() != "[:]"){//origChangeTypeValue.toString() != changeTypeValue.toString()) {//(origChangeType.toString() != changeType.value.toString()){
        log.warn("${issueTypeName}-'Change Type Field' **********Modifing the fields on RISK, IMPACT, and PRIORITY (SL)")
        environment?.setHidden(false)?.setReadOnly(false)
        risk?.setFormValue(null)?.setAllowInlineEdit(false)//.setReadOnly(false)?.setAllowInlineEdit(false)
        risk?.setDescription("The risk of implementing the change based on Outage Scope/Complexity, Locations or number of users impacted, Business Impact of Change, Testing & Recovery.")
        impact?.setFormValue(null)?.setAllowInlineEdit(false)?.setReadOnly(true)
        impact?.setDescription("The effect of the change, usually regarding service level agreements and customers.")
        priority?.setFormValue(null)?.setAllowInlineEdit(false)?.setReadOnly(true)
        priority?.setDescription("Auto-populated based on Risk and Impact selection. Used to state the priority for having a release deployed.")
        getFieldByName("Expedite Reason")?.setFormValue(null)  //?.setHidden(false)
        
        fieldSetup changeType0  //[0].toString().replaceAll("[\\[\\](){}]","")//, "all"
        //if(changeTypeValue?.size() == 1){ 
        //    changeType.setDescription("** Please make a selection from both fields. **")
        //    risk?.setFormValue(null)?.setReadOnly(true)?.setAllowInlineEdit(false)
        //    risk?.setDescription("The risk of implementing the change based on Outage Scope/Complexity, Locations or number of users impacted, Business Impact of Change, Testing & Recovery.")
            //getFieldByName("Impact")?.setReadOnly(true)
        //    impact?.setFormValue(null)?.setReadOnly(true)?.setAllowInlineEdit(false)
        //    impact?.setDescription("The effect of the change, usually regarding service level agreements and customers.")
            //return
        //}
    }
    if(changeType0 == 'Retro'){retroType()}
    //fieldSetup("${changeTypeValue[0].toString().replaceAll("[\\[\\](){}]","")}")
    //Administrators
    if (loggedInUser.emailAddress in roleActors) {
        log.warn("Administrator: '(loggedInUser.emailAddress in roleActors)'==== (${loggedInUser.emailAddress in roleActors})")
        if((issue?.getStatus().getName() == "Draft") || 
            (issue?.getStatus().getName() == "Under Review") || 
            (issue?.getStatus().getName() == "Rejected") ||
            (issue?.getStatus().getName() == "Self Review")){ 
            // ONLY allow users in the Project Administrators Role to change these fields
                log.warn("THIS IS THE ADMIN PORTION, THE USER IS IN THE PROJECT ROLE, SO DON'T SET FIELDS TO READ ONLY.")
                changeType.setReadOnly(false)?.setAllowInlineEdit(false)
                risk?.setReadOnly(false)?.setAllowInlineEdit(false)
                impact?.setReadOnly(false)?.setAllowInlineEdit(false)
        }
    }else{
        if(issue?.getStatus().getName() == "Draft"){
            log.warn("The user ${loggedInUser.emailAddress} is not in the Administrators role and status is ${issue?.getStatus().getName()}, allowing Change type field to be modified. *******")
            changeType?.setReadOnly(false)?.setAllowInlineEdit(false)
            risk?.setReadOnly(false)?.setAllowInlineEdit(false)
            impact?.setReadOnly(false)?.setAllowInlineEdit(false)
        }else{
            log.warn("The user ${loggedInUser.emailAddress} is not in the Administrators role or Draft status, leaving Change type field in read only. *******")
            changeType?.setReadOnly(true)?.setAllowInlineEdit(false)
            getFieldByName("Monitoring/Alerting - New setup, update, or removal required?")?.setRequired(true)
            //getFieldByName("Risk")?.setReadOnly(true)
            //getFieldByName("Impact")?.setReadOnly(true)
        }
    }
    /*if(changeTypeValue?.size() == 1){ 
        changeType.setDescription("** Please make a selection from both fields. **")
        risk?.setFormValue(null)?.setReadOnly(true)?.setAllowInlineEdit(false)
        risk?.setDescription("The risk of implementing the change based on Outage Scope/Complexity, Locations or number of users impacted, Business Impact of Change, Testing & Recovery.")
        //getFieldByName("Impact")?.setReadOnly(true)
        impact?.setFormValue(null)?.setReadOnly(true)?.setAllowInlineEdit(false)
        impact?.setDescription("The effect of the change, usually regarding service level agreements and customers.")
        //return
    }*/
}else{ //This is the initial Create Action
    //****************************************************************
    log.warn("*******Do this on create screen only*******************")
    //*****************************************************************

    log.warn(" **Create Action** ") //origChangeType?.keySet()?.size()})")
    //log.warn("origChangeType?.keySet()?.size(): ${origChangeType?.keySet()?.size()}")

    //Make sure both Change types are populated
    //if(getFieldById(getFieldChanged())){  //changeTypeValue?.size() != 2){
    //    getFieldByName("Change Type")?.setDescription("** Please make a selection from both fields. **")
    //    risk?.setReadOnly(false)?.setFormValue(null)
    //    impact?.setReadOnly(false)?.setFormValue(null)
    //    priority?.setReadOnly(true)?.setFormValue(null)
    //    return
    //}//else{
    //    risk?.setReadOnly(false)
    //    impact?.setReadOnly(true)
    //}
    fieldSetup changeType0  //changeTypeValue[0].toString().replaceAll("[\\[\\](){}]","")//, "all"
    //******************************** By Change Type ********************************
    risk?.setFormValue(null)
    impact?.setFormValue(null)
    priority?.setFormValue(null)
    log.warn("By Change Type")
    if(changeType0 == 'Retro'){
        log.warn("changeType0: ${changeType0}")//.toString().replaceAll("[\\[\\](){}]","")}")
        //retroEdit()
        retroCreate()
        /*
        getFieldById(COMPONENTS)?.setHidden(false)?.setRequired(true)?.setFormValue(null)
        getFieldByName("Actual Start Date")?.setRequired(true)?.setReadOnly(false)?.setFormValue(null)
        getFieldByName("Actual End Date")?.setRequired(true)?.setReadOnly(false)?.setFormValue(null)
        getFieldByName("Projected Start Date")?.setRequired(false)?.setHidden(true)?.setReadOnly(true)
        getFieldByName("Projected End Date")?.setRequired(false)?.setHidden(true)?.setReadOnly(true)
        getFieldByName("Is there data impact?")?.setRequired(false)?.setHidden(true)
        getFieldByName("Is there possible downtime?")?.setRequired(false)?.setHidden(true)
        getFieldByName("Communication Plan")?.setRequired(false)?.setHidden(true)
        getFieldByName("Change Rollback Plan")?.setRequired(false)?.setHidden(true)
        getFieldByName("Post Deployment Verification Plan")?.setRequired(false)?.setHidden(true)
        getFieldByName("Release Notes")?.setRequired(false)?.setHidden(true)
        getFieldByName("Post Validation Owner")?.setRequired(false)?.setHidden(true)
        getFieldByName("Rollback Plan Owner")?.setRequired(false)?.setHidden(true)
        getFieldByName("Post Validation Approver")?.setRequired(false)?.setHidden(true)
        */

    }else{
        log.warn("changeType0 != Retro")
        othersEdit()
        /*
        getFieldById(COMPONENTS)?.setRequired(false)?.setHidden(false)?.setReadOnly(false)//?.setFormValue(null)
        getFieldByName("Projected Start Date")?.setRequired(true)?.setReadOnly(false)?.setHidden(false)
        getFieldByName("Projected End Date")?.setRequired(true)?.setReadOnly(false)?.setHidden(false)
        getFieldByName("Actual Start Date")?.setRequired(false)?.setReadOnly(true)
        getFieldByName("Actual End Date")?.setRequired(false)?.setReadOnly(true)
        getFieldByName("Is there data impact?")?.setRequired(true)?.setHidden(false)
        getFieldByName("Is there possible downtime?")?.setRequired(true)?.setHidden(false)
        getFieldByName("Communication Plan")?.setRequired(true)?.setHidden(false)
        getFieldByName("Change Rollback Plan")?.setRequired(true)?.setHidden(false)
        getFieldByName("Post Deployment Verification Plan")?.setRequired(true)?.setHidden(false)
        getFieldByName("Release Notes")?.setRequired(true)?.setHidden(false)
        getFieldByName("Post Validation Owner")?.setRequired(true)?.setHidden(false)
        getFieldByName("Rollback Plan Owner")?.setRequired(true)?.setHidden(false)
        getFieldByName("Post Validation Approver")?.setRequired(false)?.setReadOnly(true)?.setHidden(false)
        //getFieldByName("QA Approver")?.setRequired(true)?.setHidden(false)
        //getFieldByName("RM Approver")?.setRequired(true)?.setHidden(false)
        */
    }
    
}
//changeReason.setFieldOptions(cfa)
log.warn("Change Type has completed on ${if(underlyingIssue){underlyingIssue?.getKey()}else{'a New Issue'}}.")


//****************************** Functions ****************************
def fieldSetup(String ctValue){//, String type){
    log.setLevel(Level.DEBUG)
    log = Logger.getLogger("(CHG)Change-type")
    def projectName = issueContext.projectObject.name
    def issueTypeName = issueContext.getIssueType().getName()
    def changeType = getFieldByName("Change type")
    //Map changeTypeValue = changeType?.getValue() as Map
    //if(changeTypeValue){log.warn("changeTypeValue: ${changeTypeValue}")}
    def impact = getFieldByName("Impact")
    def risk = getFieldByName("Risk")
    def expediteReason = getFieldByName("Expedite Reason")
    def expeditJustification = getFieldByName("Expedite Justification")
    def actualStart = getFieldByName("Actual Start Date")//.setRequired(true)
    def actualEnd = getFieldByName("Actual End Date")//.setRequired(true)
    def projStart = getFieldByName("Projected Start Date")//.setRequired(false)?.setHidden(true)//.setAllowInlineEdit(false)?.setFormValue(getFieldByName("Actual Start Date").value)
    def projEnd = getFieldByName("Projected End Date")//.setRequired(false)
    def undocumentedJust = getFieldByName("Undocumented Justification")
    //def status
    //def ctValue = [:]
      
    //if(type == "Edit"){ 
    //    log.warn("type: ${type}")
    //    changeType = getFieldById(getFieldChanged())
    //    log.warn("changeType: ${changeType}")
    //    ctValue = changeType?.getFormValue() as Map
    //    log.warn("ctValue: ${ctValue}")  
    //}else{ //Create Screen
    //    log.warn("type: ${type}")
    //    ctValue = underlyingIssue?.getCustomFieldValue("Change type") as Map
    //    log.warn("ctValue: ${ctValue}")  
    //}
    //String changeType0
    //if(UnderlyingIssue?.getStatus()){
    //    status = UnderlyingIssue?.getStatus().getName().toLowerCase()
    //}
    log.warn("(Function-fieldSetup) ctValue: ${ctValue}")
    //Get the values in the Change type cascading select list
    //if(ctValue){
    //    if(ctValue?.size() == 2) {
    //        changeType0 = ctValue?.get(null).toString()
    //    }else{
    //        getFieldByName("Change type")?.setDescription("** Please populate both fields! **")
    //    }
    //}
    switch(ctValue){
        //case ~/^\[Standard\], .*/:
        case "Standard":
            //if(type == "all" || type == "descOnly"){
                changeType?.setDescription("Pre-approved change that is low risk, relatively common and follows a specified procedure or work instruction approved by Peer/Manager.")
            //}else{
                log.warn("(Function-fieldSetup)SWITCH: Standard")
                List slOptions = ["Low","Medium"]
                selectListOptions("Risk", slOptions)
                risk?.setReadOnly(false)?.setAllowInlineEdit(false)
                expediteReason?.setHidden(true)?.setRequired(false)//?.setFormValue(null)//["-1", "None"], -1, "-1", null,  [null]
                expeditJustification?.setHidden(true)?.setRequired(false)?.setFormValue(null)
                fields()
            //}
        break;
        //case ~/^\[CAB\], .*/:
        case "CAB":
            //if(type == "all" || type == "descOnly"){
                changeType?.setDescription("Change follows a prescriptive process which require a full range of assessments and authorizations such as peer or technical approval, manager, QA, SOC, Change Management, and Change Advisory Board (CAB) authorization, to ensure completeness, accuracy, and the least possible disruption to service.")
            //}else{
                log.warn("(Function-fieldSetup)SWITCH: CAB.")
                List slOptions = ["Low","Medium","High"]
                selectListOptions("Risk", slOptions)
                risk?.setReadOnly(false)?.setAllowInlineEdit(false)
                expediteReason?.setHidden(true)?.setRequired(false)//?.setFormValue(null)
                expeditJustification?.setHidden(true)?.setRequired(false)?.setFormValue(null)
                fields()
            //}
        break;
        //case ~/^\[Expedite\], .*/:
        case "Expedite":
            //if(type == "all" || type == "descOnly"){
                changeType?.setDescription("Change does not meet the lead time requirement for a CAB change but is not an Emergency Change. Changes that are required quickly due to a pressing need such as legal requirement or a business need or restoring service for a standard incident (P3) that is not a Major Incident. Sr Management approval required for expedited change. A failure to plan does not constitute an expedite.")
            //}else{
                log.warn("(Function-fieldSetup)SWITCH: Expedite.")
                List slOptions = ["Low","Medium","High"]
                selectListOptions("Risk", slOptions)
                risk?.setReadOnly(false)?.setAllowInlineEdit(false)
                expediteReason?.setHidden(false)?.setRequired(true)//?.setFormValue(null)
                expeditJustification?.setHidden(false)?.setRequired(true)?.setFormValue(null)
                fields()
            //}
        break;
        //case ~/^\[Emergency\], .*/:
        case "Emergency":
            //if(type == "all" || type == "descOnly"){
                changeType?.setDescription("Emergency changes cannot wait for review during the next CAB meeting before it is implemented. An emergency change is required to resolve a Major Incident (P1 / P2) in the production environment that is has significant impact on performance or to the end users’ ability to perform their duties. In addition, there is an active Major Incident bridge. A failure to plan does not constitute an emergency. There is an active ongoing major incident bridge.")
            //}else{
                log.warn("(Function-fieldSetup)SWITCH: EMERGENCY.")
                List slOptions = ["Low","Medium","High"]
                selectListOptions("Risk", slOptions)
                risk?.setReadOnly(false)?.setAllowInlineEdit(false)
                expediteReason?.setHidden(true)?.setRequired(false)//?.setFormValue(null)
                expeditJustification?.setHidden(true)?.setRequired(false)?.setFormValue(null)
                fields()
            //}
        break;
        //case ~/^\[Retro\], .*/:
        case "Retro":
            //if(type == "all" || type == "descOnly"){
                //changeType?.setDescription("What description do you want for RETRO?")
                //getFieldByName("Is there possible downtime?")?.setRequired(false)?.setHidden(true)
                //getFieldByName("Is there data impact?")?.setRequired(false)?.setHidden(true)
                //getFieldByName("Communication Plan")?.setRequired(false)?.setHidden(true)
                //getFieldByName("Change Rollback Plan")?.setRequired(false)?.setHidden(true)
                //getFieldByName("Post Deployment Verification Plan")?.setRequired(false)?.setHidden(true)
                //getFieldByName("Release Notes")?.setRequired(false)?.setHidden(true)
                //getFieldByName("Post Validation Owner")?.setRequired(false)?.setHidden(true)
                //getFieldByName("Rollback Plan Owner")?.setRequired(false)?.setHidden(true)
                //getFieldByName("Post Validation Approver")?.setRequired(false)?.setHidden(true)
                //projStart?.setRequired(false)?.setReadOnly(true)
                //projEnd?.setRequired(false)?.setReadOnly(true)
                //expediteReason?.setHidden(true)?.setRequired(false)
                //expeditJustification?.setHidden(true)?.setRequired(false)
            //}
            //if(type == "all"){
                log.warn("(Function-fieldSetup)SWITCH: Retro.")
                changeType?.setDescription("""Retro change is an undocumented change that has already been deployed but was not properly submitted on a CHG ticket. For audit and metrics purposes, the CHG must be submitted.""")
                List slOptions = ["Low","Medium","High"]
                selectListOptions("Risk", slOptions)
                risk?.setReadOnly(false)?.setAllowInlineEdit(false)
                expediteReason?.setHidden(true)?.setRequired(false)//?.setFormValue(null)
                expeditJustification?.setHidden(true)?.setRequired(false)?.setFormValue(null)
                actualStart?.setRequired(true)?.setReadOnly(false)?.setFormValue(null)
                actualEnd?.setRequired(true)?.setReadOnly(false)?.setFormValue(null)
                projStart?.setRequired(false)?.setReadOnly(true)?.setFormValue(null)?.setHidden(true)
                projEnd?.setRequired(false)?.setReadOnly(true)?.setFormValue(null)?.setHidden(true)
                undocumentedJust?.setHidden(false)?.setRequired(true)?.setFormValue(null)
                
                //Make sure these fields are not populated
                getFieldByName("Post Validation Approver")?.setRequired(false)?.setHidden(true)?.setFormValue(null)?.setReadOnly(false)
                getFieldByName("QA Approver")?.setRequired(false)?.setHidden(true)?.setReadOnly(true)?.setFormValue(null)
                getFieldByName("RM Approver")?.setRequired(false)?.setHidden(true)?.setReadOnly(true)?.setFormValue(null)
                getFieldByName("Platform Owner")?.setHidden(false)?.setReadOnly(true)?.setFormValue(null)
                getFieldByName("Structure/Schema Changes")?.setRequired(false)?.setHidden(true)?.setFormValue(null)
                getFieldByName("Additional Data")?.setRequired(false)?.setHidden(true)?.setFormValue(null)
                getFieldByName("Current Data Changes")?.setRequired(false)?.setHidden(true)?.setFormValue(null)
                getFieldByName("Business Functionality Impacts")?.setRequired(false)?.setHidden(true)?.setFormValue(null)
                getFieldByName("Is there possible downtime?")?.setRequired(false)?.setHidden(true)?.setFormValue(null)
                getFieldByName("Is there data impact?")?.setRequired(false)?.setHidden(true)?.setFormValue(null)
                getFieldByName("Communication Plan")?.setRequired(false)?.setHidden(true)?.setFormValue(null)
                getFieldByName("Change Rollback Plan")?.setRequired(false)?.setHidden(true)?.setFormValue(null)
                getFieldByName("Post Deployment Verification Plan")?.setRequired(false)?.setHidden(true)?.setFormValue(null)
                getFieldByName("Release Notes")?.setRequired(false)?.setHidden(true)?.setFormValue(null)
                getFieldByName("Post Validation Owner")?.setRequired(false)?.setHidden(true)?.setFormValue(null)
                getFieldByName("Rollback Plan Owner")?.setRequired(false)?.setHidden(true)?.setFormValue(null)
                getFieldByName("Deployment Results")?.setRequired(false)?.setHidden(false)?.setFormValue(null)
                getFieldByName("Components")?.setRequired(true)?.setHidden(false)?.setFormValue(null)
                getFieldByName("Deployment Testing")?.setRequired(true)?.setHidden(false)?.setFormValue(null)
                getFieldByName("Deployment Plan")?.setRequired(true)?.setHidden(false)?.setFormValue(null)
                getFieldByName("Business/Customer Impact Description")?.setRequired(true)?.setHidden(false)?.setFormValue(null)
                getFieldByName("Monitoring/Alerting - New setup, update, or removal required?")?.setRequired(false)?.setHidden(true)?.setFormValue(null)
                getFieldByName("Platform")?.setRequired(true)?.setHidden(false)?.setFormValue(null)
                getFieldByName("Channel - Line of Business")?.setRequired(true)?.setHidden(false)?.setFormValue(null)
                getFieldByName("Environment (MS)")?.setRequired(true)?.setHidden(false)?.setFormValue(null)
                getFieldByName("Change Reason")?.setRequired(true)?.setHidden(false)?.setFormValue(null)
                getFieldByName("Estimated downtime")?.setRequired(false)?.setHidden(true)?.setFormValue(null)
                getFieldByName("External Ticket #")?.setRequired(false)?.setHidden(true)?.setFormValue(null)
                //getFieldById(DESCRIPTION)
                
            //}
        break;
        default:
            log.warn("(Function-fieldSetup)${UnderlyingIssue} - SWITCH: DEFAUT *****THIS SHOULD GET FIXED****.")
            log.warn("(Function-fieldSetup)ctValue.toString():${ctValue.toString()}")
            changeType?.setDescription("Please select Change Type.")
            getFieldByName("Risk")?.setReadOnly(true)
            List slOptions = ["Low","Medium", "High"]
            selectListOptions("Risk", slOptions)
            risk?.setReadOnly(false)?.setAllowInlineEdit(false)
            expediteReason?.setHidden(true)?.setRequired(false)//?.setFormValue(null)
            expeditJustification?.setHidden(true)?.setRequired(false)?.setFormValue(null)
            fields()
        break;
    }
}

def fields(){
    getFieldByName("Impact")?.setRequired(true)?.setReadOnly(true)//16112
    getFieldById("components")?.setRequired(true)?.setHidden(false)//?.setFormValue(null)//
    getFieldByName("Post Validation Approver")?.setRequired(false)?.setHidden(false)?.setFormValue(null)?.setReadOnly(false)//23512
    getFieldByName("Actual Start Date")?.setRequired(false)?.setReadOnly(true)?.setFormValue(null)//23509
    getFieldByName("Actual End Date")?.setRequired(false)?.setReadOnly(true)?.setFormValue(null)//23510
    getFieldByName("Projected Start Date")?.setRequired(true)?.setReadOnly(false)?.setHidden(false)?.setFormValue(null)//23505
    getFieldByName("Projected End Date")?.setRequired(true)?.setReadOnly(false)?.setHidden(false)?.setFormValue(null)//23507
    getFieldByName("Undocumented Justification")?.setHidden(true)?.setRequired(false)?.setFormValue(null)//26004
    getFieldByName("Is there possible downtime?")?.setRequired(true)?.setHidden(false)?.setFormValue(null)//24506
    getFieldByName("Is there data impact?")?.setRequired(true)?.setHidden(false)?.setFormValue(null)//24501
    getFieldByName("Communication Plan")?.setRequired(true)?.setHidden(false)?.setFormValue(null)//
    getFieldByName("Change Rollback Plan")?.setRequired(true)?.setHidden(false)?.setFormValue(null)//
    getFieldByName("Post Deployment Verification Plan")?.setRequired(true)?.setHidden(false)?.setFormValue(null)//
    getFieldByName("Release Notes")?.setRequired(true)?.setHidden(false)?.setFormValue(null)//
    getFieldByName("Post Validation Owner")?.setRequired(true)?.setHidden(false)?.setFormValue(null)//23401
    getFieldByName("Rollback Plan Owner")?.setRequired(true)?.setHidden(false)?.setFormValue(null)//
    getFieldByName("Platform Owner")?.setHidden(false)?.setReadOnly(true)//21501
    getFieldByName("Deployment Results")?.setRequired(false)?.setHidden(false)?.setFormValue(null)//
    getFieldByName("Deployment Testing")?.setRequired(true)?.setHidden(false)?.setFormValue(null)//
    getFieldByName("Deployment Plan")?.setRequired(true)?.setHidden(false)?.setFormValue(null)//
    getFieldByName("Business/Customer Impact Description")?.setRequired(true)?.setHidden(false)?.setFormValue(null)//23506
    getFieldByName("Monitoring/Alerting - New setup, update, or removal required?")?.setRequired(true)?.setHidden(false)?.setFormValue(null)//26002
    getFieldByName("Platform")?.setRequired(true)?.setHidden(false)//?.setFormValue(null)//17590
    getFieldByName("Channel - Line of Business")?.setRequired(true)?.setHidden(false)?.setFormValue(null)//16417
    getFieldByName("Environment (MS)")?.setRequired(true)?.setHidden(false)?.setFormValue(null)//23502
    getFieldByName("Change Reason")?.setRequired(true)?.setHidden(false)?.setFormValue(null)//23500
    getFieldByName("QA Approver")?.setRequired(false)?.setReadOnly(true)?.setFormValue(null)//22703
    getFieldByName("RM Approver")?.setRequired(false)?.setReadOnly(true)?.setFormValue(null)//23515
    getFieldByName("Exec Approver")?.setRequired(false)?.setReadOnly(true)?.setFormValue(null)//23511
    getFieldByName("IT Approver")?.setRequired(false)?.setReadOnly(true)?.setFormValue(null)//16519
    getFieldByName("Post Validation Approver")?.setRequired(false)?.setHidden(false)?.setFormValue(null)?.setReadOnly(false)//23401
    getFieldByName("Structure/Schema Changes")?.setHidden(true)?.setFormValue(null)//
    getFieldByName("Additional Data")?.setHidden(true)?.setFormValue(null)//24503
    getFieldByName("Current Data Changes")?.setHidden(true)?.setFormValue(null)//24504
    getFieldByName("Business Functionality Impacts")?.setHidden(true)?.setFormValue(null)//24505
    getFieldByName("Estimated downtime")?.setHidden(true)?.setFormValue(null)//24507
    getFieldByName("External Ticket #")?.setHidden(false)?.setFormValue(null)//16127
}


def othersEdit(){ 
    getFieldByName("Assignee")?.setRequired(true)
    getFieldByName("Impact")?.setRequired(true)?.setReadOnly(true)//16112
    getFieldById("components")?.setRequired(true)?.setHidden(false)////
    getFieldByName("Post Validation Approver")?.setRequired(false)?.setHidden(false)?.setReadOnly(false)//23512
    getFieldByName("Actual Start Date")?.setRequired(false)?.setReadOnly(true)//23509
    getFieldByName("Actual End Date")?.setRequired(false)?.setReadOnly(true)//23510
    getFieldByName("Projected Start Date")?.setRequired(true)?.setReadOnly(false)?.setHidden(false)//23505
    getFieldByName("Projected End Date")?.setRequired(true)?.setReadOnly(false)?.setHidden(false)//23507
    getFieldByName("Undocumented Justification")?.setHidden(true)?.setRequired(false)//26004
    getFieldByName("Is there possible downtime?")?.setRequired(true)?.setHidden(false)//24506
    getFieldByName("Is there data impact?")?.setRequired(true)?.setHidden(false)//24501
    getFieldByName("Communication Plan")?.setRequired(true)?.setHidden(false)//
    getFieldByName("Change Rollback Plan")?.setRequired(true)?.setHidden(false)//
    getFieldByName("Post Deployment Verification Plan")?.setRequired(true)?.setHidden(false)//
    getFieldByName("Release Notes")?.setRequired(true)?.setHidden(false)//
    getFieldByName("Post Validation Owner")?.setRequired(true)?.setHidden(false)//23401
    getFieldByName("Rollback Plan Owner")?.setRequired(true)?.setHidden(false)//
    getFieldByName("Platform Owner")?.setHidden(false)?.setReadOnly(true)//21501
    getFieldByName("Deployment Results")?.setRequired(false)?.setHidden(false)//
    getFieldByName("Deployment Testing")?.setRequired(true)?.setHidden(false)//
    getFieldByName("Deployment Plan")?.setRequired(true)?.setHidden(false)//
    getFieldByName("Business/Customer Impact Description")?.setRequired(true)?.setHidden(false)//23506
    getFieldByName("Monitoring/Alerting - New setup, update, or removal required?")?.setRequired(true)?.setHidden(false)//26002
    getFieldByName("Platform")?.setRequired(true)?.setHidden(false)//17590
    getFieldByName("Channel - Line of Business")?.setRequired(true)?.setHidden(false)//16417
    getFieldByName("Environment (MS)")?.setRequired(true)?.setHidden(false)//23502
    getFieldByName("Change Reason")?.setRequired(true)?.setHidden(false)//23500
    getFieldByName("QA Approver")?.setRequired(false)?.setReadOnly(true)//22703
    getFieldByName("RM Approver")?.setRequired(false)?.setReadOnly(true)//23515
    getFieldByName("Exec Approver")?.setRequired(false)?.setReadOnly(true)//23511
    getFieldByName("IT Approver")?.setRequired(false)?.setReadOnly(true)//16519
    getFieldByName("Post Validation Approver")?.setHidden(false)?.setReadOnly(true)//23401
    //getFieldByName("Structure/Schema Changes")?.setRequired(false)?.setHidden(true)//
    //getFieldByName("Additional Data")?.setRequired(false)?.setHidden(true)//24503
    //getFieldByName("Current Data Changes")?.setRequired(false)?.setHidden(true)//24504
    //getFieldByName("Business Functionality Impacts")?.setRequired(true)?.setHidden(false)//24505
    //getFieldByName("Estimated downtime")?.setHidden(true)//24507
    getFieldByName("External Ticket #")?.setHidden(false)//16127
    /*
    getFieldByName("Undocumented Justification")?.setHidden(true)?.setRequired(false)
    getFieldByName("Projected Start Date")?.setRequired(true)?.setHidden(false)?.setAllowInlineEdit(false)
    getFieldByName("Projected End Date")?.setRequired(true)?.setHidden(false)?.setAllowInlineEdit(false)
    getFieldByName("Actual Start Date")?.setRequired(false)?.setReadOnly(true)?.setAllowInlineEdit(false)
    getFieldByName("Actual End Date")?.setRequired(false)?.setReadOnly(true)?.setAllowInlineEdit(false)
    getFieldByName("Is there data impact?")?.setHidden(false)?.setRequired(true)
    getFieldByName("Is there possible downtime?")?.setHidden(false)?.setRequired(true)
    getFieldByName("Communication Plan")?.setHidden(false)?.setRequired(true)
    getFieldByName("Change Rollback Plan")?.setHidden(false)?.setRequired(true)
    getFieldByName("Post Deployment Verification Plan")?.setHidden(false)?.setRequired(true)
    getFieldByName("Release Notes")?.setHidden(false)?.setRequired(true)
    getFieldByName("Post Validation Owner")?.setHidden(false)?.setRequired(true)
    getFieldByName("Rollback Plan Owner")?.setHidden(false)?.setRequired(true)
    getFieldByName("Post Validation Approver")?.setHidden(false)?.setRequired(true)
    getFieldByName("QA Approver")?.setHidden(false)?.setRequired(true)
    getFieldByName("RM Approver")?.setHidden(false)?.setRequired(true)
    getFieldByName("Change type")?.setRequired(true)?.setAllowInlineEdit(false)
    getFieldByName("Risk")?.setRequired(true)?.setAllowInlineEdit(false)
    getFieldByName("Impact")?.setRequired(true)?.setAllowInlineEdit(false)
    getFieldByName("Change Reason")?.setRequired(true)
    getFieldByName("Environment (MS)")?.setRequired(true)
    getFieldByName("Channel - Line of Business")?.setRequired(true)
    getFieldByName("Change Owner")?.setRequired(true)
    */
}

def retroType(){
    //These are for Edit screen only
    getFieldByName("Summary")?.setHidden(false)?.setRequired(true)
    getFieldByName("Description")?.setHidden(false)?.setRequired(true)
    getFieldByName("Assignee")?.setHidden(false)?.setRequired(true)
    getFieldByName("Component/s")?.setHidden(false)?.setRequired(true)
    getFieldByName("Change type")?.setHidden(false)?.setRequired(true)
    getFieldByName("Risk")?.setHidden(false)?.setRequired(true)
    getFieldByName("Impact")?.setHidden(false)?.setRequired(true)
    getFieldByName("Priority (SL)")?.setHidden(false)?.setRequired(true)
    getFieldByName("Change Reason")?.setHidden(false)?.setRequired(true)
    getFieldByName("Undocumented Justification")?.setHidden(false)?.setRequired(true)
    getFieldByName("Environment (MS)")?.setHidden(false)?.setRequired(true)
    getFieldByName("Channel - Line of Business")?.setHidden(false)?.setRequired(true)
    getFieldByName("Platform (MS)")?.setHidden(false)?.setRequired(true)
    getFieldByName("Is there data impact?")?.setHidden(false)?.setRequired(true)
    getFieldByName("Is there possible downtime?")?.setHidden(false)?.setRequired(true)
    getFieldByName("Business/Customer Impact Description")?.setHidden(false)?.setRequired(true)
    getFieldByName("Deployment Plan")?.setHidden(false)?.setRequired(true)
    getFieldByName("Deployment Testing")?.setHidden(false)?.setRequired(true)
    getFieldByName("Actual Start Date")?.setHidden(false)?.setRequired(true)
    getFieldByName("Actual End Date")?.setHidden(false)?.setRequired(true)
    getFieldByName("Change Owner")?.setHidden(false)?.setRequired(true)
    getFieldByName("Platform Owner")?.setHidden(false)?.setRequired(true)
    getFieldByName("Exec Approver")?.setHidden(false)?.setReadOnly(true)
    getFieldByName("IT Approver")?.setHidden(false)?.setReadOnly(true)

//    getFieldByName("Undocumented Justification")?.setHidden(false)?.setRequired(true)
//    getFieldByName("Actual Start Date")?.setRequired(true)
//    getFieldByName("Actual End Date")?.setRequired(true)
    getFieldByName("Projected Start Date")?.setHidden(true)?.setRequired(false)//?.setAllowInlineEdit(true)//.setFormValue(getFieldByName("Actual Start Date").value)
    getFieldByName("Projected End Date")?.setHidden(true)?.setRequired(false)//?.setAllowInlineEdit(true)//.setFormValue(getFieldByName("Actual End Date").value)
    getFieldByName("Is there data impact?")?.setHidden(false)?.setRequired(false)//?.setFormValue(null)
    getFieldByName("Is there possible downtime?")?.setHidden(false)?.setRequired(false)//?.setFormValue(null)
    getFieldByName("Communication Plan")?.setHidden(true)?.setRequired(false)//?.setFormValue(null)
    getFieldByName("Change Rollback Plan")?.setHidden(true)?.setRequired(false)//?.setFormValue(null)
    getFieldByName("Post Deployment Verification Plan")?.setHidden(true)?.setRequired(false)//?.setFormValue(null)
    getFieldByName("Release Notes")?.setHidden(true)?.setRequired(false)//?.setFormValue(null)
    getFieldByName("Post Validation Owner")?.setHidden(true)?.setRequired(false)//?.setFormValue(null)
    getFieldByName("Rollback Plan Owner")?.setHidden(true)?.setRequired(false)//?.setFormValue(null)
    getFieldByName("Post Validation Approver")?.setHidden(true)?.setRequired(false)//?.setFormValue(null)
    getFieldByName("QA Approver")?.setHidden(true)?.setRequired(false)//?.setFormValue(null)
    getFieldByName("RM Approver")?.setHidden(true)?.setRequired(false)//?.setFormValue(null)
    getFieldByName("Structure/Schema Changes")?.setHidden(true)?.setRequired(false)
    getFieldByName("Additional Data")?.setHidden(true)?.setRequired(false)
    getFieldByName("Current Data Changes")?.setHidden(true)?.setRequired(false)
    getFieldByName("Business Functionality Impacts")?.setHidden(true)?.setRequired(false)
    getFieldByName("CM Approver")?.setHidden(true)?.setReadOnly(true)
    getFieldByName("Emergency Approver")?.setHidden(true)?.setReadOnly(true)
    getFieldByName("Deployment Results")?.setHidden(true)
}

def retroCreate(){
    //These are for Create screen only
    getFieldByName("Summary")?.setRequired(true)
    getFieldByName("Assignee")?.setRequired(true)
    getFieldByName("Change type")?.setRequired(true)
    getFieldByName("Risk")?.setRequired(true)
    getFieldByName("Impact")?.setRequired(true)
    getFieldByName("Priority (SL)")?.setRequired(true)
    getFieldByName("Change Reason")?.setRequired(true)
    getFieldByName("Undocumented Justification")?.setRequired(true)
    getFieldByName("Environment (MS)")?.setRequired(true)
    getFieldByName("Channel - Line of Business")?.setRequired(true)
    //getFieldByName("Platform (MS)")?.setRequired(true)
    //getFieldByName("Is there data impact?")?.setRequired(true)
    //getFieldByName("Is there possible downtime?")?.setRequired(true)
    getFieldByName("Change Owner")?.setRequired(true)
    getFieldByName("Platform Owner")?.setRequired(true)
    //getFieldByName("Exec Approver")?.setReadOnly(true)

    getFieldByName("Description")?.setRequired(false)
    getFieldByName("Component/s")?.setRequired(false)
    getFieldByName("Actual Start Date")?.setRequired(false)?.setHidden(false)?.setReadOnly(false)
    getFieldByName("Actual End Date")?.setRequired(false)?.setHidden(false)?.setReadOnly(false)
    getFieldByName("Business/Customer Impact Description")?.setRequired(false)
    getFieldByName("Deployment Plan")?.setRequired(false)
    getFieldByName("Deployment Testing")?.setRequired(false)
    getFieldByName("Platform")?.setRequired(false)
    getFieldByName("Projected Start Date")?.setHidden(true)?.setRequired(false)//?.setFormValue(null)
    getFieldByName("Projected End Date")?.setHidden(true)?.setRequired(false)
    getFieldByName("Is there data impact?")?.setHidden(false)?.setRequired(false)//?.setFormValue(null)
    getFieldByName("Is there possible downtime?")?.setHidden(false)?.setRequired(false)//?.setFormValue(null)
    getFieldByName("Communication Plan")?.setHidden(true)?.setRequired(false)//?.setFormValue(null)
    getFieldByName("Change Rollback Plan")?.setHidden(true)?.setRequired(false)//?.setFormValue(null)
    getFieldByName("Post Deployment Verification Plan")?.setHidden(true)?.setRequired(false)//?.setFormValue(null)
    getFieldByName("Release Notes")?.setHidden(true)?.setRequired(false)//?.setFormValue(null)
    getFieldByName("Post Validation Owner")?.setHidden(true)?.setRequired(false)//?.setFormValue(null)
    getFieldByName("Rollback Plan Owner")?.setHidden(true)?.setRequired(false)//?.setFormValue(null)
    getFieldByName("Post Validation Approver")?.setHidden(true)?.setRequired(false)//?.setFormValue(null)
    getFieldByName("QA Approver")?.setHidden(true)?.setRequired(false)//?.setFormValue(null)
    getFieldByName("RM Approver")?.setHidden(true)?.setRequired(false)//?.setFormValue(null)
    getFieldByName("Structure/Schema Changes")?.setHidden(true)?.setRequired(false)
    getFieldByName("Additional Data")?.setHidden(true)?.setRequired(false)
    getFieldByName("Current Data Changes")?.setHidden(true)?.setRequired(false)
    getFieldByName("Business Functionality Impacts")?.setHidden(true)?.setRequired(false)
    getFieldByName("CM Approver")?.setHidden(true)?.setReadOnly(true)
    getFieldByName("Emergency Approver")?.setHidden(true)?.setReadOnly(true)
    getFieldByName("Deployment Results")?.setHidden(true)
}


def selectListOptions(String cfName, List availableOpt){
    def cf = getFieldByName(cfName) 
    def cfObj = customFieldManager.getCustomFieldObject(cf.getFieldId())
    def cfConfig = cfObj.getRelevantConfig(getIssueContext())
    def cfOptions = ComponentAccessor.getOptionsManager().getOptions(cfConfig)

    def cfA = cfOptions.findAll { 
        //it.value in ["Minor / Localized","Moderate / Limited", "Significant / Large"] }.collectEntries {
        it.value in availableOpt as List }.collectEntries {
            [ (it.optionId.toString()) : it.value ] } as Map
    cf.setFieldOptions(cfA)
}