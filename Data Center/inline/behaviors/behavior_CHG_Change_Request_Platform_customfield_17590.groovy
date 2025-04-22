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
import org.apache.log4j.Logger

log = Logger.getLogger("(CHG)Platform (${if(underlyingIssue){underlyingIssue?.getKey()}else{'NEW ISSUE'}})")
log.setLevel(Level.INFO)

Issue issue = UnderlyingIssue

@BaseScript FieldBehaviours fieldBehaviours
final projectName = issueContext.projectObject.name
final issueTypeName = issueContext.getIssueType()?.getName()
final loggedInUser = ComponentAccessor.jiraAuthenticationContext.loggedInUser
final platformOwner = underlyingIssue?.getCustomFieldValue("Platform Owner")
final origPlatform = underlyingIssue?.getCustomFieldValue("Platform")
def origPlatformValue = origPlatform?.getValue()  //.values() as HashMap
def platform = getFieldById(getFieldChanged())
def platformValue = platform?.getValue()
def changeType = getFieldByName("Change type")
ArrayList changeTypeValue = changeType.value as ArrayList

if (!(issueTypeName == "Change")){
    log.debug("Issue type is not 'Change', exiting now.")
    return
}
if(changeTypeValue.size() != 2){
    if(platform?.value){platform?.setFormValue(null)}
    log.debug("One or both CHANGE TYPE fields IS NULL, EXITING EARLY. ${if(underlyingIssue){underlyingIssue?.getKey()}else{'a New Issue'}}")
    return
}

log.debug("Behaviours: ${projectName}, ${issueTypeName}-'Platform' Field has started on ${if(underlyingIssue){underlyingIssue?.getKey()}else{'a New Issue'}}.")
if (getAction()?.id == null || !(getAction()?.id == 1 )){
    //*******************************************************
    //*******Do this on Edit and View screens**************
    //*******************************************************
    log.debug("Behaviours: ${projectName}, ${issueTypeName}-'Platform' Field **EDIT & VIEW Screens**.")
    //If someone changes Platform, we need to update the Platform Owner also
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
    def platformOwner = getFieldByName("Platform Owner")
    if(platform?.getValue() == null || platform?.getValue() == "[]") {
        platformOwner.setFormValue("")
        return "'Platform' field is empty."
    }
    def platformValue = platform.getValue() as String

    switch(platformValue.toLowerCase()){
        case "client configuration usgh":
            platformOwner.setFormValue("slandes@teladochealth.com")
        break;
        case "clinical usgh":
            platformOwner.setFormValue("mrosenburg@teladochealth.com")
        break;
        case "consumer experience":
            platformOwner.setFormValue("jdittmar@teladochealth.com")
        break;
        case "corporate applications":
            platformOwner.setFormValue("adias@teladochealth.com")
        break;
        case "cybersecurity":
            platformOwner.setFormValue("mdesmery@teladochealth.com")
        break;
        case "data":
            platformOwner.setFormValue("rupa.narayan@teladochealth.com")
        break;
        case "devops engineering":
            platformOwner.setFormValue("mmanfredonia@teladochealth.com")
        break;
        //case "qa platform":
        //    platformOwner.setFormValue("mmanfredonia@teladochealth.com")
        //break;
        //case "solutions delivery":
        //    platformOwner.setFormValue("preilly@teladochealth.com")
        //break;
        //case "technical operations":
        //    platformOwner.setFormValue("msistona@teladochealth.com")
        //break;
        case "hhs engineering":
            platformOwner.setFormValue("danny.sanchez@teladochealth.com")
        break;
        case "hhs platform ops":
            platformOwner.setFormValue("KHanrahan@teladochealth.com")
        break;
        case "hhs technical operations":
            platformOwner.setFormValue("KHanrahan@teladochealth.com")
        break;
        case "service, benefit and client":
            platformOwner.setFormValue("dshaw@teladochealth.com")
        break;
        case "technology services":
            platformOwner.setFormValue("luc.lafontan@teladochealth.com")
        break;
        default:
            platformOwner.setFormValue("")
        break;

    }
}