/*
Creator: Jeff Melies
Purpose: Platform field -> Populate Platform Owner
Change log:
*/
//imports
import com.atlassian.jira.component.ComponentAccessor
import com.atlassian.jira.issue.Issue
import com.onresolve.jira.groovy.user.FieldBehaviours
import groovy.transform.BaseScript
import org.apache.log4j.Level
log.setLevel(Level.INFO)

Issue issue = UnderlyingIssue

@BaseScript FieldBehaviours fieldBehaviours
final projectName = issueContext.projectObject.name
final issueTypeName = issueContext.getIssueType().getName()
final loggedInUser = ComponentAccessor.jiraAuthenticationContext.loggedInUser
final platformOwner = underlyingIssue?.getCustomFieldValue("Platform Owner")
final origPlatform = underlyingIssue?.getCustomFieldValue("Platform")
def origPlatformValue = origPlatform?.getValue()  //.values() as HashMap
def platform = getFieldById(getFieldChanged())
def platformValue = platform?.getValue()

log.debug("Behaviours: ${projectName}, ${issueTypeName}-'Platform' Field has started on ${if(underlyingIssue){underlyingIssue?.getKey()}else{'a New Issue'}}.")
if (getAction()?.id == null || !(getAction()?.id == 1 )){
    //*******************************************************
    //*******Do this on Edit and View screens**************
    //*******************************************************
    log.debug("Behaviours: ${projectName}, ${issueTypeName}-'Platform' Field **EDIT & VIEW Screens**.")
    //If someone changes CA-Platform, we need to update the Platform Owner also
    if(origPlatformValue.toString() != platformValue.toString()) {
        populateChangeOwner()
    }
}else{ //This is the initial Create Action
    //*******************************************************
    //*******Do this on create screen only*******************
    //*******************************************************
    log.debug("Behaviours: ${projectName}, ${issueTypeName}-'Platform' Field  **Create Action**.")
    populateChangeOwner()
}
log.debug("Behaviours: ${projectName}, ${issueTypeName}-'Platform' Field has completed on ${if(underlyingIssue){underlyingIssue?.getKey()}else{'a New Issue'}}.")

//Functions
def populateChangeOwner() {
    //def platform = underlyingIssue?.getCustomFieldValue("Platform")
    def platform = getFieldByName("Platform")
    def platformValue = platform.getValue() as String
    def platformOwner = getFieldByName("Platform Owner")
    switch(platformValue.toLowerCase()){
        case "clinical":
            platformOwner.setFormValue("mrosenburg@teladochealth.com")
            platformOwner.setReadOnly(true)
        break;
        case "consumer":
            platformOwner.setFormValue("jdittmar@teladochealth.com")
            platformOwner.setReadOnly(true)
        break;
        case "corporate applications":
            platformOwner.setFormValue("adias@teladochealth.com")
            platformOwner.setReadOnly(true)
        break;
        case "cybersecurity":
            platformOwner.setFormValue("sara.hall@teladochealth.com")
            platformOwner.setReadOnly(true)
        break;
        case "data":
            platformOwner.setFormValue("rupa.narayan@teladochealth.com")
            platformOwner.setReadOnly(true)
        break;
        case "devops":
            platformOwner.setFormValue("mmanfredonia@teladochealth.com")
            platformOwner.setReadOnly(true)
        break;
        case "marketing":
            platformOwner.setFormValue("jeremy.kinder@teladochealth.com")
            platformOwner.setReadOnly(true)
        break;
        case "member support":
            platformOwner.setFormValue("maryann.lusk@teladochealth.com")
            platformOwner.setReadOnly(true)
        break;
        case "service & client":
            platformOwner.setFormValue("dshaw@teladochealth.com")
            platformOwner.setReadOnly(true)
        break;
        case "solutions delivery":
            platformOwner.setFormValue("preilly@teladochealth.com")
            platformOwner.setReadOnly(true)
        break;
        case "tdh international":
            platformOwner.setFormValue("jparera@teladochealth.com")
            platformOwner.setReadOnly(true)
        break;
        case "technology services":
            platformOwner.setFormValue("luc.lafontan@teladochealth.com")
            platformOwner.setReadOnly(true)
        break;
        case "technical operations (coe)":
            platformOwner.setFormValue("ssaldana@teladochealth.com")
            platformOwner.setReadOnly(true)
        break;
        case "workplace services":
            platformOwner.setFormValue("sselman@teladochealth.com")
            platformOwner.setReadOnly(true)
        break;
        default:
            platformOwner.setFormValue("")
            platformOwner.setReadOnly(false)
        break;

    }
}