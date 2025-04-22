/**
 * Eduardo Rojo
 * CRUS project - Client Reporting - USGH
 * Issue Type: Story, Task, Bug
 * Hide fields based on "Issue Type" Question
 */
import com.atlassian.jira.component.ComponentAccessor;
import com.atlassian.jira.issue.customfields.option.Option;
log.info("Behaviours CRUS Project – Issue Type started.");

def customFieldManager = ComponentAccessor.getCustomFieldManager();
def optionsManager = ComponentAccessor.getOptionsManager();


// This listens if the "Issue Type" field has changed
def issueType = getFieldById(getFieldChanged());
// This saves custom field's ids to manage its behaviors
def requestType = getFieldByName("Request Type");
def newOrEnhancement = getFieldByName("New or Enhancement");
def standardDataExtractType = getFieldByName("Standard Data Extract Type");
def reportName = getFieldByName("Report Name");
def description = getFieldByName("Description");
def businessRequirements = getFieldByName("Business Requirements");
def areThereAnyTags = getFieldByName("Are there any tags?");
def clientName = getFieldByName("Client Name");
def clientLaunchDate = getFieldByName("Client Launch Date");
def contractLanguageForGrievanceReporting = getFieldByName("Contract Language for Grievance Reporting");
def clientLevel = getFieldByName("Client Level");
def sensitiveInformation = getFieldByName("Sensitive Information");
def fileTypeDelimiter = getFieldByName("File Type Delimiter");

log.info("Issue Type: " + issueContext.issueType.name);

switch(issueContext.issueType.name){
    case "Task":
    	reportName.setHidden(true);
		reportName.setRequired(false);
    	description.setHidden(false);
    	description.setRequired(true);
    	clientLevel.setRequired(false);
    	sensitiveInformation.setHidden(false);
    	sensitiveInformation.setRequired(false);
    	fileTypeDelimiter.setHidden(false);
    	break;
    case "Story":
    	requestType.setRequired(true);
    	newOrEnhancement.setHidden(true);
    	standardDataExtractType.setHidden(true);
		standardDataExtractType.setRequired(false);
		areThereAnyTags.setHidden(true);
		areThereAnyTags.setRequired(false);
		clientName.setHidden(true);
		clientName.setRequired(false);
		//clientLaunchDate.setHidden(true); // updated (JIRA-5208)
		clientLaunchDate.setRequired(false);
		contractLanguageForGrievanceReporting.setHidden(true);
		contractLanguageForGrievanceReporting.setRequired(false);
		description.setHidden(false);
		description.setRequired(true);
		businessRequirements.setRequired(true);
    	break;
    case "Bug":
    	reportName.setHidden(false);
		reportName.setRequired(true);
    	description.setHidden(false);
    	description.setRequired(false);
    	clientLevel.setRequired(false);
    	sensitiveInformation.setHidden(false);
    	sensitiveInformation.setRequired(false);
    	fileTypeDelimiter.setHidden(true);
    	break;
    default:
        break;
}
log.info("Behaviours CRUS Project – Issue Type completed.") 