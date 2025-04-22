/*
Creator: Jeff Melies
Purpose: RC-Platform field -> Populate RC-Platform Owner
Change log: ER 10-11-2024 JIRA-10050
            ER 11-112024 JIRA-10169
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
final projectName = issueContext?.projectObject?.name
final issueTypeName = issueContext?.getIssueType()?.getName()
final loggedInUser = ComponentAccessor?.jiraAuthenticationContext?.loggedInUser
final rcPlatformOwner = underlyingIssue?.getCustomFieldValue("RC-Platform Owner")
final origRCPlatform = underlyingIssue?.getCustomFieldValue("RC-Platform")
def origRCPlatformValue = origRCPlatform?.getValue()  //.values() as HashMap
def rcPlatform = getFieldById(getFieldChanged())
def rcPlatformValue = rcPlatform?.getValue()
log.debug("rcPlatformValue: ${rcPlatformValue}")
log.debug("Behaviours: ${projectName}, ${issueTypeName}-'RC-Platform' Field has started on ${if(underlyingIssue){underlyingIssue?.getKey()}else{'a New Issue'}}.")
if (getAction()?.id == null || !(getAction()?.id == 1 )){
    //*******************************************************
    //*******Do this on Edit and View screens**************
    //*******************************************************
    log.debug("Behaviours: ${projectName}, ${issueTypeName}-'RC-Platform' Field **EDIT & VIEW Screens**.")
    //If someone changes Platform, we need to update the Platform Owner also
    if(origRCPlatformValue?.toString() != rcPlatformValue?.toString()) {
        populateChangeOwner()
    }
}else{ //This is the initial Create Action
    //*******************************************************
    //*******Do this on create screen only*******************
    //*******************************************************
    log.debug("Behaviours: ${projectName}, ${issueTypeName}-'RC-Platform' Field  **Create Action**.")
    populateChangeOwner()
}
log.debug("Behaviours: ${projectName}, ${issueTypeName}-'RC-Platform' Field has completed on ${if(underlyingIssue){underlyingIssue?.getKey()}else{'a New Issue'}}.")

//Functions
def populateChangeOwner() {
    //def platform = underlyingIssue?.getCustomFieldValue("Platform")
    def rcPlatform = getFieldByName("RC-Platform")
    def rcPlatformValue = rcPlatform?.getValue() as String
    log.debug("rcPlatformValue: ${rcPlatformValue}")
    if(rcPlatformValue == null || rcPlatformValue == ""){return "RC Platform is empty."}
    def rcPlatformOwner = getFieldByName("RC-Platform Owner")
    switch(rcPlatformValue?.toLowerCase()){
        case "clinical usgh": // updated "clinical" to "clinical usgh" JIRA-10050
            rcPlatformOwner?.setFormValue("mrosenburg@teladochealth.com")
            rcPlatformOwner?.setReadOnly(true)
        break;
        case "consumer experience":
            rcPlatformOwner?.setFormValue("jdittmar@teladochealth.com")
            rcPlatformOwner?.setReadOnly(true)
        break;
        case "corporate applications":
            rcPlatformOwner?.setFormValue("adias@teladochealth.com")
            rcPlatformOwner?.setReadOnly(true)
        break;
        case "cybersecurity":
            rcPlatformOwner?.setFormValue("sara.hall@teladochealth.com")
            rcPlatformOwner?.setReadOnly(true)
        break;
        case "data":
            rcPlatformOwner?.setFormValue("rupa.narayan@teladochealth.com")
            rcPlatformOwner?.setReadOnly(true)
        break;
        case "devops engineering":
            rcPlatformOwner?.setFormValue("mmanfredonia@teladochealth.com")
            rcPlatformOwner?.setReadOnly(true)
        break;
        case "ecosystem":
            rcPlatformOwner?.setFormValue("mrosenburg@teladochealth.com")
            rcPlatformOwner?.setReadOnly(true)
        break;        
        case "engagement":
            rcPlatformOwner?.setFormValue("ooluwole@teladochealth.com")
            rcPlatformOwner?.setReadOnly(true)
        break;
        case "hhs engineering": // added JIRA-10050
            rcPlatformOwner?.setFormValue("danny.sanchez@teladochealth.com")
            rcPlatformOwner?.setReadOnly(true)
        break;
        case "hhs platform ops": // added JIRA-10050
            rcPlatformOwner?.setFormValue("KHanrahan@teladochealth.com")
            rcPlatformOwner?.setReadOnly(true)
        break;
        case "hhs technical operations": // added JIRA-10050
            rcPlatformOwner?.setFormValue("KHanrahan@teladochealth.com")
            rcPlatformOwner?.setReadOnly(true)
        break;
        case "marketing operations":
            rcPlatformOwner?.setFormValue("ssanchez@teladochealth.com")
            rcPlatformOwner?.setReadOnly(true)
        break;
        case "member support":
            rcPlatformOwner?.setFormValue("maryann.lusk@teladochealth.com")
            rcPlatformOwner?.setReadOnly(true)
        break;
        case "product management":
            rcPlatformOwner?.setFormValue("jroestel@teladochealth.com")
            rcPlatformOwner?.setReadOnly(true)
        break;        
        case "service, benefit, and client":
            rcPlatformOwner?.setFormValue("dshaw@teladochealth.com")
            rcPlatformOwner?.setReadOnly(true)
        break;
        case "solutions delivery":
            rcPlatformOwner?.setFormValue("preilly@teladochealth.com")
            rcPlatformOwner?.setReadOnly(true)
        break;
        case "supply chain":
            rcPlatformOwner?.setFormValue("raj.naik@teladochealth.com")
            rcPlatformOwner?.setReadOnly(true)
        break;        
        case "tdh international":
            rcPlatformOwner?.setFormValue("jmallol@teladochealth.com")
            rcPlatformOwner?.setReadOnly(true)
        break;
        case "technology services":
            rcPlatformOwner?.setFormValue("luc.lafontan@teladochealth.com")
            rcPlatformOwner?.setReadOnly(true)
        break;
        case "technical operations":
            rcPlatformOwner?.setFormValue("ssaldana@teladochealth.com")
            rcPlatformOwner?.setReadOnly(true)
        break;
        case "workplace services":
            rcPlatformOwner?.setFormValue("sselman@teladochealth.com")
            rcPlatformOwner?.setReadOnly(true)
        break;
        //Adding the new value JIRA-10169
        case "hhs solution analysis and design":
            rcPlatformOwner?.setFormValue("gbrallier@teladochealth.com")
            rcPlatformOwner?.setReadOnly(true)
        break;
        default:
            rcPlatformOwner?.setFormValue("")
            rcPlatformOwner?.setReadOnly(false)
        break;

    }
}