/*
Creator: Jeff Melies
Purpose: CA-Platform field -> Populate CA-Platform Owner
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
final issueTypeName = issueContext.getIssueType()?.getName()
final loggedInUser = ComponentAccessor.jiraAuthenticationContext.loggedInUser
final caPlatformOwner = underlyingIssue?.getCustomFieldValue("CA-Platform Owner")
final origCAPlatform = underlyingIssue?.getCustomFieldValue("CA-Platform")
def origCAPlatformValue = origCAPlatform?.getValue()  //.values() as HashMap
def caPlatform = getFieldById(getFieldChanged())
def caPlatformValue = caPlatform?.getValue()

log.debug("Behaviours: ${projectName}, ${issueTypeName}-'CA-Platform' Field has started on ${if(underlyingIssue){underlyingIssue?.getKey()}else{'a New Issue'}}.")
if (getAction()?.id == null || !(getAction()?.id == 1 )){
    //*******************************************************
    //*******Do this on Edit and View screens**************
    //*******************************************************
    log.debug("Behaviours: ${projectName}, ${issueTypeName}-'CA-Platform' Field **EDIT & VIEW Screens**.")
    //If someone changes CA-Platform, we need to update the CA-Platform Owner also
    if(origCAPlatformValue.toString() != caPlatformValue.toString()) {
        populateChangeOwner()
    }
}else{ //This is the initial Create Action
    //*******************************************************
    //*******Do this on create screen only*******************
    //*******************************************************
    log.debug("Behaviours: ${projectName}, ${issueTypeName}-'CA-Platform' Field  **Create Action**.")
    populateChangeOwner()
}
log.debug("Behaviours: ${projectName}, ${issueTypeName}-'CA-Platform' Field has completed on ${if(underlyingIssue){underlyingIssue?.getKey()}else{'a New Issue'}}.")

//Functions
def populateChangeOwner() {
    //def platform = underlyingIssue?.getCustomFieldValue("Platform")
    def caPlatform = getFieldByName("CA-Platform")
    def caPlatformValue = caPlatform?.getValue() as String
    def caPlatformOwner = getFieldByName("CA-Platform Owner")
    switch(caPlatformValue?.toLowerCase()){
        case "clinical":
            caPlatformOwner?.setFormValue("mrosenburg@teladochealth.com")
            caPlatformOwner?.setReadOnly(true)
        break;
        case "consumer experience":
            caPlatformOwner?.setFormValue("jdittmar@teladochealth.com")
            caPlatformOwner?.setReadOnly(true)
        break;
        case "corporate applications":
            caPlatformOwner?.setFormValue("adias@teladochealth.com")
            caPlatformOwner?.setReadOnly(true)
        break;
        case "cybersecurity":
            caPlatformOwner?.setFormValue("sara.hall@teladochealth.com")
            caPlatformOwner?.setReadOnly(true)
        break;
        case "data":
            caPlatformOwner?.setFormValue("rupa.narayan@teladochealth.com")
            caPlatformOwner?.setReadOnly(true)
        break;
        case "devops engineering":
            caPlatformOwner?.setFormValue("mmanfredonia@teladochealth.com")
            caPlatformOwner?.setReadOnly(true)
        break;
        case "ecosystem":
            caPlatformOwner?.setFormValue("mrosenburg@teladochealth.com")
            caPlatformOwner?.setReadOnly(true)
        break;
        case "engagement":
            caPlatformOwner?.setFormValue("ooluwole@teladochealth.com")
            caPlatformOwner?.setReadOnly(true)
        break;
        case "marketing operations":
            caPlatformOwner?.setFormValue("ssanchez@teladochealth.com")
            caPlatformOwner?.setReadOnly(true)
        break;
        case "member support":
            caPlatformOwner?.setFormValue("maryann.lusk@teladochealth.com")
            caPlatformOwner?.setReadOnly(true)
        break;
        case "product management":
            caPlatformOwner?.setFormValue("jroestel@teladochealth.com")
            caPlatformOwner?.setReadOnly(true)
        break;
        case "service, benefit, and client":
            caPlatformOwner?.setFormValue("dshaw@teladochealth.com")
            caPlatformOwner?.setReadOnly(true)
        break;
        case "solutions delivery":
            caPlatformOwner?.setFormValue("preilly@teladochealth.com")
            caPlatformOwner?.setReadOnly(true)
        break;
        case "supply chain":
            caPlatformOwner?.setFormValue("raj.naik@teladochealth.com")
            caPlatformOwner?.setReadOnly(true)
        break;
        case "tdh international":
            caPlatformOwner?.setFormValue("jmallol@teladochealth.com")
            caPlatformOwner?.setReadOnly(true)
        break;
        case "technical operations":
            caPlatformOwner?.setFormValue("ssaldana@teladochealth.com")
            caPlatformOwner?.setReadOnly(true)
        break;
        case "technology services":
            caPlatformOwner?.setFormValue("luc.lafontan@teladochealth.com")
            caPlatformOwner?.setReadOnly(true)
        break;
        case "workplace services":
            caPlatformOwner?.setFormValue("sselman@teladochealth.com")
            caPlatformOwner?.setReadOnly(true)
        break;
        default:
            caPlatformOwner?.setFormValue("")
            caPlatformOwner?.setReadOnly(false)
        break;
    }
}