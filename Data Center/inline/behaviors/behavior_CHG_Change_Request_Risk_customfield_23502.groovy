/*
Creator: Jeff Melies
Purpose: Set Priority (SL) depending on selection of both Impact & Risk
Change log:
*/
import com.atlassian.jira.issue.customfields.option.Options
import com.atlassian.jira.issue.fields.option.Option
import com.atlassian.jira.component.ComponentAccessor
import com.onresolve.jira.groovy.user.FieldBehaviours
import com.atlassian.jira.issue.Issue
import com.atlassian.crowd.embedded.api.Group
import com.onresolve.scriptrunner.parameters.annotation.GroupPicker
import com.atlassian.jira.security.roles.ProjectRoleManager
import groovy.transform.BaseScript
import org.apache.log4j.Level
import org.apache.log4j.Logger

log = Logger.getLogger("(CHG)Risk (${if(underlyingIssue){underlyingIssue?.getKey()}else{'NEW ISSUE'}})")
log.setLevel(Level.INFO)

log.debug("Started")
@BaseScript FieldBehaviours fieldBehaviours
//Managers
def groupManager = ComponentAccessor.groupManager
def customFieldManager = ComponentAccessor.getCustomFieldManager()
def optionsManager = ComponentAccessor.getOptionsManager()
def projectRoleManager = ComponentAccessor.getComponent(ProjectRoleManager)

Issue issue = underlyingIssue 

final projectName = issueContext?.projectObject.name
final issueTypeName = issueContext?.getIssueType()?.getName()
def origRisk = issue?.getCustomFieldValue("Risk") //Risk only after ticket has been created
def risk = getFieldById(getFieldChanged()) //Risk during create and if field has/is changed
def impact = getFieldByName("Impact")
def priority = getFieldByName("Priority (SL)")
def changeType = getFieldByName("Change type")
//If Risk changes, then reset Impact
//if(risk.value){
//    log.debug("risk: ${risk.value}, origRisk: ${origRisk}")
//    impact.setFormValue(null)?.setReadOnly(true)
//    priority.setFormValue(null)
//}
ArrayList changeTypeValue = changeType.value as ArrayList  //underlyingIssue?.getCustomFieldValue("Change type") as Map
//log.debug("changeTypeValue: ${changeTypeValue}")
//log.debug("changeTypeValue?.get('1'): ${changeTypeValue?.get('1')}")
//log.debug("underlyingIssue.getCustomFieldValue: ${underlyingIssue?.getCustomFieldValue("Change type")}")
//Map changeTypeValue = changeType?.getValue() as Map
final roleName = 'Administrators'
def projectRole = projectRoleManager.getProjectRole(roleName)
def roleActors = projectRoleManager.getProjectRoleActors(projectRole, issueContext.projectObject)?.users*.emailAddress
final loggedInUser = ComponentAccessor.jiraAuthenticationContext.loggedInUser

//Always do these items:
log.debug("changeTypeValue?.size() != 2 ${(changeTypeValue?.size() != 2)}: ${changeTypeValue?.size()}, changeType?.getValue() == '[]' ${(changeType?.getValue() == [])}: ${changeType?.getValue()}")
if((changeTypeValue?.size() != 2) || (! origRisk && ! risk?.value)){
    log.debug("One or both CHANGE TYPE fields IS NULL, EXITING EARLY. ${if(underlyingIssue){underlyingIssue?.getKey()}else{'a New Issue'}}")
    if(impact?.value){impact?.setReadOnly(true)?.setFormValue(null)}
    priority.setFormValue(null)
    return
}

log.debug("${issueTypeName}-'Risk Field' has started on ${if(underlyingIssue){underlyingIssue?.getKey()}else{'a New Issue'}}. " + changeTypeValue?.last())
if (getAction()?.id == null || !(getAction()?.id == 1 )){
    log.debug("Behaviours: ${projectName}, ${issueTypeName}-'Risk Field'  **EDIT & VIEW Screens**")
    //*******************************************************
    //*******Do this on Edit and View screens**************
    //*******************************************************
    fieldSetup()
    //Administrators
    if (loggedInUser.emailAddress in roleActors) {
        log.debug("**EDIT & VIEW Screens** -- '(loggedInUser.emailAddress in roleActors)'==== (${loggedInUser.emailAddress in roleActors})")
        if((issue?.getStatus()?.getName() == "Draft") || 
            (issue?.getStatus()?.getName() == "Under Review") ||
            (issue?.getStatus()?.getName() == "Rejected") || 
            (issue?.getStatus()?.getName() == "Self Review")){
            // ONLY allow users in the Project Administrators Role to change these fields
            //getFieldByName("Change type")?.setReadOnly(false)
            //getFieldByName("Environment (MS)")?.setReadOnly(false)
            getFieldByName("Risk")?.setReadOnly(false)?.setAllowInlineEdit(false)
            //getFieldByName("Impact")?.setReadOnly(false)
        }
    }else{
        if(issue?.getStatus()?.getName() == "Draft"){
            log.debug(" **EDIT & VIEW Screens** -- The user ${loggedInUser.emailAddress} is not in the Administrators role and status is ${issue?.getStatus()?.getName()}, allowing Risk field to be modified. *******")
            getFieldById(getFieldChanged())?.setReadOnly(false)?.setAllowInlineEdit(false)
            getFieldByName("Risk")?.setReadOnly(false)?.setAllowInlineEdit(false)
            getFieldByName("Impact")?.setReadOnly(false)?.setAllowInlineEdit(false)
        }else{
            log.debug("**EDIT & VIEW Screens** -- The user ${loggedInUser.emailAddress} is not in the Administrators role or Draft status, leaving Risk field in read only. *******")
            //getFieldByName("Change type")?.setReadOnly(true)
            getFieldByName("Risk")?.setReadOnly(true)?.setAllowInlineEdit(false)
            //getFieldByName("Impact")?.setReadOnly(true)
        }
    }
}else{ //This is the initial Create Action
    log.debug("Behaviours: ${projectName}, ${issueTypeName}-'Risk Field'  **Create Action**")
    //*******************************************************
    //*******Do this on create screen only*******************
    //*******************************************************
    if(risk?.value.toString() != null || risk?.value.toString() != ''){
        log.debug("Behaviours: ${projectName}, ${issueTypeName}-'Risk Field' Risk is NOT null: ${risk?.value.toString()}, OrigRisk: ${origRisk?.value.toString()}")
        fieldSetup()
    }else{
        log.debug("Behaviours: ${projectName}, ${issueTypeName}-'Risk Field' Risk is null: ${risk?.value.toString()}, OrigRisk: ${origRisk?.value.toString()}")
    }
}

log.debug("Behaviours: ${projectName}, ${issueTypeName}-'Risk Field' has completed on ${if(underlyingIssue){underlyingIssue?.getKey()}else{'a New Issue'}}.")

//********************************************************* Functions *************************************************
def fieldSetup(){
    log = Logger.getLogger("Function: fieldSetup") as Logger
    log.setLevel(Level.DEBUG)
    
    final projectName = issueContext?.projectObject?.name
    final issueTypeName = issueContext?.getIssueType()?.getName()
    def risk = getFieldByName("Risk")
    def impact = getFieldByName("Impact")
    def changeType = getFieldByName("Change type")
    ArrayList changeTypeValue = changeType?.value as ArrayList// as Map
    log.debug("Starting the function")
    log.debug("changeTypeValue[0].toString()?.toLowerCase(): ${changeTypeValue[0].toString()?.toLowerCase()}")
    switch(changeTypeValue[0].toString()?.toLowerCase()){//[0]?.toString()?.toLowerCase()){
        case "standard":
            if(risk?.getValue()?.toString()?.toLowerCase() == 'medium'){
                log.debug("Behaviours: ${projectName}, ${issueTypeName}-'Risk Field' - SWITCH: Standard/Medium")
                getFieldByName("Risk")?.setDescription('Non-scripted / non-automated Change that is manually configured/ deployed. - A Change that has a feasible roll back mechanism that has been tested and validated to work in a timely manor. - Software/ Code Deployments Subcategory to production systems. - Software/ Code Deployments to high profile CAT/ UAT systems.')
                List slOptions = ["Minor / Localized"]
                selectListOptions("Impact", slOptions)
                impact?.setHidden(false)?.setAllowInlineEdit(false)
                impact?.setFormValue("Minor / Localized")
                impact?.setReadOnly(false)?.setAllowInlineEdit(false)
            }else{
                log.debug("Behaviours: ${projectName}, ${issueTypeName}-'Risk Field' - SWITCH: Standard/Not Medium.")
                getFieldByName("Risk")?.setDescription('The deployment process is for a non- production system. - The Change deployment method is standardized with the use of well documented procedures with low risk of failure. - The majority of the Change is deployed through scripted means that are well tested and known to work when performing the Change to non- production systems. - The Change is deployed via a vendor compiled installation application that has been tested and known to work with the targeted environment/ purpose for non- production systems.')
                List slOptions = ["Moderate / Limited", "Minor / Localized"]
                selectListOptions("Impact", slOptions)
                impact?.setHidden(false)?.setAllowInlineEdit(false)
                impact?.setReadOnly(false)?.setAllowInlineEdit(false)               
            }
        break;
        case "cab":
            log.debug("CASE=CAB")
            switch(risk?.getValue()?.toString()?.toLowerCase())
            {
                case "low":
                    log.debug("Behaviours: ${projectName}, ${issueTypeName}-'Risk Field' - SWITCH: CAB/Low.")
                    getFieldByName("Risk")?.setDescription('The deployment process is for a non- production system. - The Change deployment method is standardized with the use of well documented procedures with low risk of failure. - The majority of the Change is deployed through scripted means that are well tested and known to work when performing the Change to non- production systems. - The Change is deployed via a vendor compiled installation application that has been tested and known to work with the targeted environment/ purpose for non- production systems.')
                    List slOptions = ["Significant / Large"]
                    selectListOptions("Impact", slOptions)
                    impact?.setHidden(false)?.setReadOnly(false)?.setAllowInlineEdit(false) 
                    impact?.setFormValue("Significant / Large")
                break;
                case "medium":
                    log.debug("Behaviours: ${projectName}, ${issueTypeName}-'Risk Field' - SWITCH: CAB/Medium.")
                    getFieldByName("Risk")?.setDescription('Non-scripted / non-automated Change that is manually configured/ deployed. - A Change that has a feasible roll back mechanism that has been tested and validated to work in a timely manor. - Software/ Code Deployments Subcategory to production systems. - Software/ Code Deployments to high profile CAT/ UAT systems.')
                    List slOptions = ["Moderate / Limited", "Significant / Large"]
                    selectListOptions("Impact", slOptions)
                    impact?.setHidden(false)?.setReadOnly(false)?.setAllowInlineEdit(false) 
                break;
                case "high":
                    log.debug("Behaviours: ${projectName}, ${issueTypeName}-'Risk Field' - SWITCH: CAB/High.")
                    getFieldByName("Risk")?.setDescription('Adding new hardware or software components (network, application, database, storage) to a major system already in use in production. - Risk of failure could impact systems that put patients at risk. - Change failure could cause an outage of mission critical system(s)?. - Change can take out a customer facing system and would require significant time, or resources, to bring the system back online. - Performing a Change that cannot be rolled back in a timely manor, or not rolled back at all. - Deploying the Change, or rolling it back, requires close coordination and effort across multiple teams.')
                    List slOptions = ["Minor / Localized","Moderate / Limited", "Significant / Large"]
                    selectListOptions("Impact", slOptions)
                    impact?.setHidden(false)?.setReadOnly(false)?.setAllowInlineEdit(false) 
                break;
                case "critical":
                    log.debug("Behaviours: ${projectName}, ${issueTypeName}-'Risk Field' - SWITCH: CAB/Critical.")
                    List slOptions = ["Minor / Localized","Moderate / Limited", "Significant / Large"]
                    selectListOptions("Impact", slOptions)
                    impact?.setHidden(false)?.setReadOnly(false)?.setAllowInlineEdit(false) 
                break;
                default:
                    log.debug("Behaviours: ${projectName}, ${issueTypeName}-'Risk Field' -  - SWITCH: CAB - DEFAULT ********THIS NEEDS TO BE FIXED.******")
                    impact?.setReadOnly(true)
                    return
                break;                
            }
        break;
        case "expedite":
            switch(risk.getValue()?.toString()?.toLowerCase())
            {
                case "low":
                    log.debug("Behaviours: ${projectName}, ${issueTypeName}-'Risk Field' - SWITCH: Expedite/Low.")
                    getFieldByName("Risk")?.setDescription('The deployment process is for a non- production system. - The Change deployment method is standardized with the use of well documented procedures with low risk of failure. - The majority of the Change is deployed through scripted means that are well tested and known to work when performing the Change to non- production systems. - The Change is deployed via a vendor compiled installation application that has been tested and known to work with the targeted environment/ purpose for non- production systems.')
                    List slOptions = ["Significant / Large"]
                    selectListOptions("Impact", slOptions)
                    impact?.setHidden(false)?.setReadOnly(false)?.setAllowInlineEdit(false) 
                    impact?.setFormValue("Significant / Large")
                break;
                case "medium":
                    log.debug("Behaviours: ${projectName}, ${issueTypeName}-'Risk Field' - SWITCH: Expedite/Medium.")
                    getFieldByName("Risk")?.setDescription('Non-scripted / non-automated Change that is manually configured/ deployed. - A Change that has a feasible roll back mechanism that has been tested and validated to work in a timely manor. - Software/ Code Deployments Subcategory to production systems. - Software/ Code Deployments to high profile CAT/ UAT systems.')                    
                    List slOptions = ["Moderate / Limited", "Significant / Large"]
                    selectListOptions("Impact", slOptions)
                    impact?.setHidden(false)?.setReadOnly(false)?.setAllowInlineEdit(false) 
                break;
                case "high":
                    log.debug("Behaviours: ${projectName}, ${issueTypeName}-'Risk Field' - SWITCH: Expedite/High.")
                    getFieldByName("Risk")?.setDescription('Adding new hardware or software components (network, application, database, storage) to a major system already in use in production. - Risk of failure could impact systems that put patients at risk. - Change failure could cause an outage of mission critical system(s)?. - Change can take out a customer facing system and would require significant time, or resources, to bring the system back online. - Performing a Change that cannot be rolled back in a timely manor, or not rolled back at all. - Deploying the Change, or rolling it back, requires close coordination and effort across multiple teams.')
                    List slOptions = ["Minor / Localized","Moderate / Limited", "Significant / Large"]
                    selectListOptions("Impact", slOptions)
                    impact?.setHidden(false)?.setReadOnly(false)?.setAllowInlineEdit(false) 
                break;
                default:
                    log.debug("Behaviours: ${projectName}, ${issueTypeName}-'Risk Field' -  - SWITCH: Expedite - DEFAULT ********THIS NEEDS TO BE FIXED.******")
                    impact?.setReadOnly(true)
                    return
                break;
            }
        break;
        case "emergency":
            log.debug("Behaviours: ${projectName}, ${issueTypeName}-'Risk Field' - SWITCH: Emergency.")
            List slOptions = ["Minor / Localized","Moderate / Limited", "Significant / Large"]
            selectListOptions("Impact", slOptions)
            impact?.setHidden(false)?.setReadOnly(false)?.setAllowInlineEdit(false) 
            switch(risk?.getValue()?.toString()?.toLowerCase())
            {
                case "low":
                    getFieldByName("Risk")?.setDescription('The deployment process is for a non- production system. - The Change deployment method is standardized with the use of well documented procedures with low risk of failure. - The majority of the Change is deployed through scripted means that are well tested and known to work when performing the Change to non- production systems. - The Change is deployed via a vendor compiled installation application that has been tested and known to work with the targeted environment/ purpose for non- production systems.')
                break;
                case "medium":
                    getFieldByName("Risk")?.setDescription('Non-scripted / non-automated Change that is manually configured/ deployed. - A Change that has a feasible roll back mechanism that has been tested and validated to work in a timely manor. - Software/ Code Deployments Subcategory to production systems. - Software/ Code Deployments to high profile CAT/ UAT systems.')
                break;
                case "high":
                    getFieldByName("Risk")?.setDescription('Adding new hardware or software components (network, application, database, storage) to a major system already in use in production. - Risk of failure could impact systems that put patients at risk. - Change failure could cause an outage of mission critical system(s)?. - Change can take out a customer facing system and would require significant time, or resources, to bring the system back online. - Performing a Change that cannot be rolled back in a timely manor, or not rolled back at all. - Deploying the Change, or rolling it back, requires close coordination and effort across multiple teams.')
                break;
                default:
                    log.debug("Behaviours: ${projectName}, ${issueTypeName}-'Risk Field' -  - SWITCH: Emergency - DEFAULT ********THIS NEEDS TO BE FIXED.******")
                    impact.setReadOnly(true)
                    return
                break;
            }
        break;
        case "retro":
            log.debug("Behaviours: ${projectName}, ${issueTypeName}-'Risk Field' - SWITCH: Retro.")
            List slOptions = ["Minor / Localized","Moderate / Limited", "Significant / Large"]
            selectListOptions("Impact", slOptions)
            impact?.setHidden(false)?.setReadOnly(false)?.setAllowInlineEdit(false) 
            switch(risk?.getValue()?.toString()?.toLowerCase())
            {
                case "low":
                    getFieldByName("Risk")?.setDescription('The deployment process is for a non- production system. - The Change deployment method is standardized with the use of well documented procedures with low risk of failure. - The majority of the Change is deployed through scripted means that are well tested and known to work when performing the Change to non- production systems. - The Change is deployed via a vendor compiled installation application that has been tested and known to work with the targeted environment/ purpose for non- production systems.')
                break;
                case "medium":
                    getFieldByName("Risk")?.setDescription('Non-scripted / non-automated Change that is manually configured/ deployed. - A Change that has a feasible roll back mechanism that has been tested and validated to work in a timely manor. - Software/ Code Deployments Subcategory to production systems. - Software/ Code Deployments to high profile CAT/ UAT systems.')
                break;
                case "high":
                    getFieldByName("Risk")?.setDescription('Adding new hardware or software components (network, application, database, storage) to a major system already in use in production. - Risk of failure could impact systems that put patients at risk. - Change failure could cause an outage of mission critical system(s)?. - Change can take out a customer facing system and would require significant time, or resources, to bring the system back online. - Performing a Change that cannot be rolled back in a timely manor, or not rolled back at all. - Deploying the Change, or rolling it back, requires close coordination and effort across multiple teams.')
                break;
                default:
                    log.debug("Behaviours: ${projectName}, ${issueTypeName}-'Risk Field' -  - SWITCH: Emergency - DEFAULT ********THIS NEEDS TO BE FIXED.******")
                    impact.setReadOnly(true)
                    return
                break;
            }
        break;
        default:
            log.debug("Behaviours: ${projectName}, ${issueTypeName}-'Risk Field' - ********** DEFAULT ********THIS NEEDS TO BE FIXED.******")
            //risk.setReadOnly(true)
            //impact.setReadOnly(true)
            return
        break;

    }
}

def selectListOptions(String cfName, List availableOpt){
    def cf = getFieldByName(cfName) 
    def cfObj = customFieldManager.getCustomFieldObject(cf.getFieldId())
    def cfConfig = cfObj.getRelevantConfig(getIssueContext())
    def cfOptions = ComponentAccessor.getOptionsManager()?.getOptions(cfConfig)

    def cfA = cfOptions.findAll { 
        it.value in availableOpt as List }.collectEntries {
            [ (it.optionId.toString()) : it.value ] } as Map
    cf.setFieldOptions(cfA)
}