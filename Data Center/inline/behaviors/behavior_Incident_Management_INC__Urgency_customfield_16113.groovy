/*
Creator: Jeff Melies
Purpose: Set Priority depending on selection of both Impact & Urgency
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

log.setLevel(Level.DEBUG)

@BaseScript FieldBehaviours fieldBehaviours
//Managers
def groupManager = ComponentAccessor.groupManager
def customFieldManager = ComponentAccessor.getCustomFieldManager()
def optionsManager = ComponentAccessor.getOptionsManager()
def projectRoleManager = ComponentAccessor.getComponent(ProjectRoleManager)

Issue issue = underlyingIssue
final projectName = issueContext.projectObject.name
final issueTypeName = issueContext.getIssueType().getName()

log = Logger.getLogger("${issue}: Behaviours: ${projectName}- Field: Urgency") 

final origUrgency = issue?.getCustomFieldValue("Urgency") //Urgency only after ticket has been created
def urgency = getFieldById(getFieldChanged()) //Urgency during create and if field has/is changed
def impact = getFieldByName("Impact")
//def changeType = getFieldByName("Change type")
//Map changeTypeValue = changeType?.getValue() as Map
//final roleName = 'Administrators'
//def projectRole = projectRoleManager.getProjectRole(roleName)
//def roleActors = projectRoleManager.getProjectRoleActors(projectRole, issueContext.projectObject).users*.emailAddress
//final loggedInUser = ComponentAccessor.jiraAuthenticationContext.loggedInUser

//Always do these items:
log.debug("Has started on ${if(underlyingIssue){underlyingIssue?.getKey()}else{'a New Issue'}}.")
if(!(issue?.getCustomFieldValue("Urgency") || issue?.getCustomFieldValue("Impact"))){
    log.debug("Urgency or Impact IS NULL, EXITING EARLY.")
    return
}

if (getAction()?.id == null || !(getAction()?.id == 1 )){
    log.debug("**EDIT & VIEW Screens**")
    //*******************************************************
    //*******Do this on Edit and View screens**************
    //*******************************************************
//    fieldSetup()
}else{ //This is the initial Create Action
    log.debug("**Create Action**")
    //*******************************************************
    //*******Do this on create screen only*******************
    //*******************************************************
    if(urgency?.value.toString() != null || urgency?.value.toString() != ''){
        log.debug("Urgency is NOT null: ${urgency?.value.toString()}, origUrgency: ${origUrgency?.value.toString()}")
//        fieldSetup()
    }else{
        log.debug("Urgency is null: ${urgency?.value.toString()}, origUrgency: ${origUrgency?.value.toString()}")
    }
}

log.debug("Has completed on ${if(underlyingIssue){underlyingIssue?.getKey()}else{'a New Issue'}}.")

//Functions below here
/*
def fieldSetup(){
    final projectName = issueContext.projectObject.name
    final issueTypeName = issueContext.getIssueType().getName()
    def urgency = getFieldByName("Urgency")
    def impact = getFieldByName("Impact")
    //def changeType = getFieldByName("Change type")
    //Map changeTypeValue = changeType.getValue() as Map

    //switch(changeTypeValue[0].toString().toLowerCase()){
    //    case "standard":
            if(urgency?.getValue().toString().toLowerCase() == 'medium'){
                log.debug("Behaviours: ${projectName}, ${issueTypeName}-'Urgency Field' - SWITCH: Medium")
    //            getFieldByName("Urgency").setDescription('Non-scripted / non-automated Change that is manually configured/ deployed. - A Change that has a feasible roll back mechanism that has been tested and validated to work in a timely manor. - Software/ Code Deployments Subcategory to production systems. - Software/ Code Deployments to high profile CAT/ UAT systems.')
                List slOptions = ["Minor / Localized"]
                selectListOptions("Impact", slOptions)
                impact.setHidden(false).setAllowInlineEdit(false)
                impact.setFormValue("Minor / Localized")
                impact.setReadOnly(false).setAllowInlineEdit(false)
            }else{
                log.debug("Behaviours: ${projectName}, ${issueTypeName}-'Risk Field' - SWITCH: Standard/Not Medium.")
                getFieldByName("Risk").setDescription('The deployment process is for a non- production system. - The Change deployment method is standardized with the use of well documented procedures with low risk of failure. - The majority of the Change is deployed through scripted means that are well tested and known to work when performing the Change to non- production systems. - The Change is deployed via a vendor compiled installation application that has been tested and known to work with the targeted environment/ purpose for non- production systems.')
                List slOptions = ["Moderate / Limited", "Minor / Localized"]
                selectListOptions("Impact", slOptions)
                impact.setHidden(false).setAllowInlineEdit(false)
                impact.setReadOnly(false).setAllowInlineEdit(false)               
            }
        break;
        case "cab":
            switch(risk.getValue().toString().toLowerCase())
            {
                case "low":
                    log.debug("Behaviours: ${projectName}, ${issueTypeName}-'Risk Field' - SWITCH: CAB/Low.")
                    getFieldByName("Risk").setDescription('The deployment process is for a non- production system. - The Change deployment method is standardized with the use of well documented procedures with low risk of failure. - The majority of the Change is deployed through scripted means that are well tested and known to work when performing the Change to non- production systems. - The Change is deployed via a vendor compiled installation application that has been tested and known to work with the targeted environment/ purpose for non- production systems.')
                    List slOptions = ["Significant / Large"]
                    selectListOptions("Impact", slOptions)
                    impact.setHidden(false).setReadOnly(false).setAllowInlineEdit(false) 
                    impact.setFormValue("Significant / Large")
                break;
                case "medium":
                    log.debug("Behaviours: ${projectName}, ${issueTypeName}-'Risk Field' - SWITCH: CAB/Medium.")
                    getFieldByName("Risk").setDescription('Non-scripted / non-automated Change that is manually configured/ deployed. - A Change that has a feasible roll back mechanism that has been tested and validated to work in a timely manor. - Software/ Code Deployments Subcategory to production systems. - Software/ Code Deployments to high profile CAT/ UAT systems.')
                    List slOptions = ["Moderate / Limited", "Significant / Large"]
                    selectListOptions("Impact", slOptions)
                    impact.setHidden(false).setReadOnly(false).setAllowInlineEdit(false) 
                break;
                case "high":
                    log.debug("Behaviours: ${projectName}, ${issueTypeName}-'Risk Field' - SWITCH: CAB/High.")
                    getFieldByName("Risk").setDescription('Adding new hardware or software components (network, application, database, storage) to a major system already in use in production. - Risk of failure could impact systems that put patients at risk. - Change failure could cause an outage of mission critical system(s). - Change can take out a customer facing system and would require significant time, or resources, to bring the system back online. - Performing a Change that cannot be rolled back in a timely manor, or not rolled back at all. - Deploying the Change, or rolling it back, requires close coordination and effort across multiple teams.')
                    List slOptions = ["Minor / Localized","Moderate / Limited", "Significant / Large"]
                    selectListOptions("Impact", slOptions)
                    impact.setHidden(false).setReadOnly(false).setAllowInlineEdit(false) 
                break;
                case "critical":
                    log.debug("Behaviours: ${projectName}, ${issueTypeName}-'Risk Field' - SWITCH: CAB/Critical.")
                    List slOptions = ["Minor / Localized","Moderate / Limited", "Significant / Large"]
                    selectListOptions("Impact", slOptions)
                    impact.setHidden(false).setReadOnly(false).setAllowInlineEdit(false) 
                break;                
            }
        break;
        case "expedite":
            switch(risk.getValue().toString().toLowerCase())
            {
                case "low":
                    log.debug("Behaviours: ${projectName}, ${issueTypeName}-'Risk Field' - SWITCH: Expedite/Low.")
                    getFieldByName("Risk").setDescription('The deployment process is for a non- production system. - The Change deployment method is standardized with the use of well documented procedures with low risk of failure. - The majority of the Change is deployed through scripted means that are well tested and known to work when performing the Change to non- production systems. - The Change is deployed via a vendor compiled installation application that has been tested and known to work with the targeted environment/ purpose for non- production systems.')
                    List slOptions = ["Significant / Large"]
                    selectListOptions("Impact", slOptions)
                    impact.setHidden(false).setReadOnly(false).setAllowInlineEdit(false) 
                    impact.setFormValue("Significant / Large")
                break;
                case "medium":
                    log.debug("Behaviours: ${projectName}, ${issueTypeName}-'Risk Field' - SWITCH: Expedite/Medium.")
                    getFieldByName("Risk").setDescription('Non-scripted / non-automated Change that is manually configured/ deployed. - A Change that has a feasible roll back mechanism that has been tested and validated to work in a timely manor. - Software/ Code Deployments Subcategory to production systems. - Software/ Code Deployments to high profile CAT/ UAT systems.')                    
                    List slOptions = ["Moderate / Limited", "Significant / Large"]
                    selectListOptions("Impact", slOptions)
                    impact.setHidden(false).setReadOnly(false).setAllowInlineEdit(false) 
                break;
                case "high":
                    log.debug("Behaviours: ${projectName}, ${issueTypeName}-'Risk Field' - SWITCH: Expedite/High.")
                    getFieldByName("Risk").setDescription('Adding new hardware or software components (network, application, database, storage) to a major system already in use in production. - Risk of failure could impact systems that put patients at risk. - Change failure could cause an outage of mission critical system(s). - Change can take out a customer facing system and would require significant time, or resources, to bring the system back online. - Performing a Change that cannot be rolled back in a timely manor, or not rolled back at all. - Deploying the Change, or rolling it back, requires close coordination and effort across multiple teams.')
                    List slOptions = ["Minor / Localized","Moderate / Limited", "Significant / Large"]
                    selectListOptions("Impact", slOptions)
                    impact.setHidden(false).setReadOnly(false).setAllowInlineEdit(false) 
                break;
            }
        break;
        case "emergency":
            log.debug("Behaviours: ${projectName}, ${issueTypeName}-'Risk Field' - SWITCH: Emergency.")
            List slOptions = ["Minor / Localized","Moderate / Limited", "Significant / Large"]
            selectListOptions("Impact", slOptions)
            impact.setHidden(false).setReadOnly(false).setAllowInlineEdit(false) 
            switch(risk.getValue().toString().toLowerCase())
            {
                case "low":
                    getFieldByName("Risk").setDescription('The deployment process is for a non- production system. - The Change deployment method is standardized with the use of well documented procedures with low risk of failure. - The majority of the Change is deployed through scripted means that are well tested and known to work when performing the Change to non- production systems. - The Change is deployed via a vendor compiled installation application that has been tested and known to work with the targeted environment/ purpose for non- production systems.')
                break;
                case "medium":
                    getFieldByName("Risk").setDescription('Non-scripted / non-automated Change that is manually configured/ deployed. - A Change that has a feasible roll back mechanism that has been tested and validated to work in a timely manor. - Software/ Code Deployments Subcategory to production systems. - Software/ Code Deployments to high profile CAT/ UAT systems.')
                break;
                case "high":
                    getFieldByName("Risk").setDescription('Adding new hardware or software components (network, application, database, storage) to a major system already in use in production. - Risk of failure could impact systems that put patients at risk. - Change failure could cause an outage of mission critical system(s). - Change can take out a customer facing system and would require significant time, or resources, to bring the system back online. - Performing a Change that cannot be rolled back in a timely manor, or not rolled back at all. - Deploying the Change, or rolling it back, requires close coordination and effort across multiple teams.')
                break;
                default:
                break;
            }
        break;
        default:
            log.debug("Behaviours: ${projectName}, ${issueTypeName}-'Risk Field' - ********** DEFAULT ********THIS NEEDS TO BE FIXED.******")
        break;

    }
}

def selectListOptions(String cfName, List availableOpt){
    def cf = getFieldByName(cfName) 
    def cfObj = customFieldManager.getCustomFieldObject(cf.getFieldId())
    def cfConfig = cfObj.getRelevantConfig(getIssueContext())
    def cfOptions = ComponentAccessor.getOptionsManager().getOptions(cfConfig)

    def cfA = cfOptions.findAll { 
        it.value in availableOpt as List }.collectEntries {
            [ (it.optionId.toString()) : it.value ] } as Map
    cf.setFieldOptions(cfA)
}
*/
