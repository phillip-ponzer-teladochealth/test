/**import org.apache.jasper.Options

 * Eduardo Rojo
 * IDT project - IM Data Transfer (IDT)
 * Issue Type: All
 * Set required/optional fields based on "Request Type" value
 * Log: 9/26/2023 Eduardo Rojo (JIRA-8410) 
 */
import com.atlassian.jira.component.ComponentAccessor;
import com.atlassian.jira.issue.customfields.option.Option;
import com.atlassian.jira.issue.customfields.option.Options
log.info("Behaviours IDT Project – Request Type started.");

def customFieldManager = ComponentAccessor.getCustomFieldManager();
def optionsManager = ComponentAccessor.getOptionsManager();

// This listens if the "Request Type" field has changed
def requestType = getFieldById(getFieldChanged());

// This saves custom field's ids to manage its behaviors
def summary = getFieldByName("Summary");
def projectName = getFieldByName("Project Name");
def description = getFieldByName("Description");
def clientName = getFieldByName("Client Name");
def groupId = getFieldByName("Group ID");
def eligibilityVerificationMethod = getFieldByName("Eligibility Verification Method");
def partnerName = getFieldByName("Partner Name");
def mappingDetails = getFieldByName("Mapping Details");
def launchDate = getFieldByName("Launch Date");
def sftpDetails = getFieldByName("SFTP Details");
def attachment = getFieldByName("Attachment");


summary.setDescription("A brief overview to give an idea of what the request is about.");
projectName.setDescription("The name of the project this request is/will be part of.");
description.setDescription("The objective of this request. Feel free to also mention anything else you would like to share.");
clientName.setDescription("Client Name(s) from Admin.");
groupId.setDescription("Legacy Group Id(s) / Admin Group (No org IDs).");
eligibilityVerificationMethod.setDescription("The eligibility type (primary).");
partnerName.setDescription("Please specify old vendor and new vendor name, if applicable.");
launchDate.setDescription("The launch date of the client.");
sftpDetails.setDescription("SFTP details should include contact and atleast one IP Address. If existing, provide username. Please also attach the Eligibility Questionnaire.");
mappingDetails.setDescription("Please attach Mapping document.");
attachment.setDescription("Do not attach any kind of PII/PHI, instead please share a box link.");

// This gets the value of "Request Type" as String
def requestTypeOption = requestType.getValue() as String

switch(requestTypeOption){
    case "Configure or Investigate SFTP":
		projectName.setRequired(true);
		description.setRequired(true);
		clientName.setRequired(true);
		groupId.setRequired(true);
		eligibilityVerificationMethod.setRequired(true);
		partnerName.setRequired(true);
		launchDate.setRequired(true);
		sftpDetails.setRequired(true);
		mappingDetails.setRequired(false);
		attachment.setRequired(true);
    	break;
    case "Review and/or Automate New Data":
    	projectName.setRequired(true);
		description.setRequired(true);
		clientName.setRequired(true);
		groupId.setRequired(true);
		eligibilityVerificationMethod.setRequired(true);
		partnerName.setRequired(true);
		launchDate.setRequired(true);
		sftpDetails.setRequired(true);
		mappingDetails.setRequired(true);
		attachment.setRequired(false);
    	break;
    case "Review and/or Analyze Existing Data":
    	projectName.setRequired(true);
		description.setRequired(true);
		clientName.setRequired(true);
		groupId.setRequired(true);
		eligibilityVerificationMethod.setRequired(false);
		partnerName.setRequired(false);
		launchDate.setRequired(false);
		sftpDetails.setRequired(false);
		mappingDetails.setRequired(false);
		attachment.setRequired(false);
    	break;
    case "Update Members":
    	projectName.setRequired(true);
		description.setRequired(true);
		clientName.setRequired(true);
		groupId.setRequired(true);
		eligibilityVerificationMethod.setRequired(false);
		partnerName.setRequired(false);
		launchDate.setRequired(false);
		sftpDetails.setRequired(false);
		mappingDetails.setRequired(false);
		attachment.setRequired(false);
    	break;
    case "Update Admin":
    	projectName.setRequired(true);
		description.setRequired(true);
		clientName.setRequired(true);
		groupId.setRequired(true);
		eligibilityVerificationMethod.setRequired(false);
		partnerName.setRequired(false);
		launchDate.setRequired(true);
		sftpDetails.setRequired(false);
		mappingDetails.setRequired(false);
		attachment.setRequired(true);
    	break;
	case "Other":
    	projectName.setRequired(true);
		description.setRequired(true);
		clientName.setRequired(true);
		groupId.setRequired(true);
		eligibilityVerificationMethod.setRequired(false);
		partnerName.setRequired(false);
		launchDate.setRequired(false);
		sftpDetails.setRequired(false);
		mappingDetails.setRequired(false);
		attachment.setRequired(false);
    	break;
	case "Other - Internal":
    	projectName.setRequired(true);
		description.setRequired(true);
		clientName.setRequired(false);
		groupId.setRequired(false);
		eligibilityVerificationMethod.setRequired(false);
		partnerName.setRequired(false);
		launchDate.setRequired(false);
		sftpDetails.setRequired(false);
		mappingDetails.setRequired(false);
		attachment.setRequired(false);
    	break;
	case "Ingestion Failure":
    	projectName.setRequired(false);
		description.setRequired(false);
		clientName.setRequired(false);
		groupId.setRequired(false);
		eligibilityVerificationMethod.setRequired(false);
		partnerName.setRequired(false);
		launchDate.setRequired(false);
		sftpDetails.setRequired(false);
		mappingDetails.setRequired(false);
		attachment.setRequired(false);
    	break;
    default:
        break;
}
log.info("Behaviours IDT Project – Request Type completed.");