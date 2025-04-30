/**
 * Author: Eduardo Rojo
 * DATAOPS: CCM Data Operations - project
 * Issue Types: Story, Task
 * Set Require/Optional fields based on "Request Type" 
 * Log changes:
 */
import com.atlassian.jira.component.ComponentAccessor;
import com.atlassian.jira.issue.customfields.option.Option;
log.info("Behaviours DATAOPS Project – Request Type started.");

def customFieldManager = ComponentAccessor.getCustomFieldManager();
def optionsManager = ComponentAccessor.getOptionsManager();

// This listens if the "Request Type" field has changed
def requestType = getFieldById(getFieldChanged());

// This saves custom field's ids to manage its behaviors
def dueDate = getFieldByName("Due Date");
def clientName = getFieldByName("Client Name");
def linkToSfRecord = getFieldByName("Link to SF Record");
def clientCode = getFieldByName("Client Code");
def partnerName = getFieldByName("Partner Name");
def link = getFieldByName("Link");
def namingConvention = getFieldByName("Naming Convention");
def frequency = getFieldByName("Frequency");
def multitenant = getFieldByName("Multitenant");
def fileType = getFieldByName("File Type");
def useCase = getFieldByName("Use Case");
def mappingDetails = getFieldByName("Mapping Details");
def sftpDetails = getFieldByName("SFTP Details");

// This gets the value of "Request Type" as String
def requestTypeOption = requestType.getValue() as String

//dueDate.setRequired(true);
//clientName.setRequired(true);
//linkToSfRecord.setRequired(true);
//clientCode.setRequired(true);


switch(requestTypeOption){
    case "Configure or Investigate SFTP":
    	// Showing/Requiring Report Type
    	requestType.setHelpText("This type is for updating existing sftp setup to configure folders and aliases, whitelist IP addresses, or for troubleshooting file syncing issues.");
    	dueDate.setRequired(true);
		clientName.setRequired(true);
		linkToSfRecord.setRequired(true);
		clientCode.setRequired(true);
		partnerName.setRequired(false);
    	link.setRequired(false);
    	namingConvention.setRequired(false);
    	frequency.setRequired(false);
    	multitenant.setRequired(false);
    	fileType.setRequired(false);
    	useCase.setRequired(false);
    	mappingDetails.setRequired(false);
    	sftpDetails.setRequired(true);
    	break;
    case "Review and/or Automate New Data":
    	// Showing optional/required fields
    	requestType.setHelpText("This type is for reviewing and configuring any type of new data; eligibility, claims, tags, emails etc, for (automated) processing into our system.");
    	dueDate.setRequired(true);
		clientName.setRequired(true);
		linkToSfRecord.setRequired(true);
		clientCode.setRequired(true);
		partnerName.setRequired(true);
    	link.setRequired(true);
    	namingConvention.setRequired(true);
    	frequency.setRequired(true);
    	multitenant.setRequired(true);
    	fileType.setRequired(true);
    	useCase.setRequired(true);
    	mappingDetails.setRequired(true);
    	sftpDetails.setRequired(true);
    	break;
    case "Review and/or Analyze Existing Data":
    	// Showing/Requiring Report Type
    	requestType.setHelpText("This type is for any questions, concerns, issues, investigations etc regarding the existing data in our files, systems or dashboards.");
    	dueDate.setRequired(true);
		clientName.setRequired(true);
		linkToSfRecord.setRequired(true);
		clientCode.setRequired(true);
		partnerName.setRequired(false);
    	link.setRequired(false);
    	namingConvention.setRequired(false);
    	frequency.setRequired(false);
    	multitenant.setRequired(false);
    	fileType.setRequired(false);
    	useCase.setRequired(false);
    	mappingDetails.setRequired(false);
    	sftpDetails.setRequired(false);
    	break;
    case "Update Members":
    	// Showing optional/required fields
    	requestType.setHelpText("This type is for updating missing/incorrect values of existing tag/s or insurance information for members based on the recruitaible data.");
    	dueDate.setRequired(true);
		clientName.setRequired(true);
		linkToSfRecord.setRequired(true);
		clientCode.setRequired(true);
		partnerName.setRequired(false);
    	link.setRequired(false);
    	namingConvention.setRequired(false);
    	frequency.setRequired(false);
    	multitenant.setRequired(false);
    	fileType.setRequired(false);
    	useCase.setRequired(false);
    	mappingDetails.setRequired(true);
    	sftpDetails.setRequired(false);
    	break;
    case "Stop Recruitable Data Feeds": //hasta acá llegué
    	// Showing optional/required fields
    	requestType.setHelpText("This type is for unconfiguring data feeds from being processed into our system and/or also deactivating corresponding sftp connections.");
    	dueDate.setRequired(true);
		clientName.setRequired(true);
		linkToSfRecord.setRequired(true);
		clientCode.setRequired(true);
		partnerName.setRequired(false);
    	link.setRequired(false);
    	namingConvention.setRequired(false);
    	frequency.setRequired(false);
    	multitenant.setRequired(false);
    	fileType.setRequired(false);
    	useCase.setRequired(false);
    	mappingDetails.setRequired(false);
    	sftpDetails.setRequired(false);
    	break;
    case "Other":
    	requestType.setHelpText("This type is for any other request that doesn't fall into any of the defined standard types.");
		dueDate.setRequired(true);
		clientName.setRequired(true);
		linkToSfRecord.setRequired(true);
		clientCode.setRequired(true);
		partnerName.setRequired(false);
    	link.setRequired(false);
    	namingConvention.setRequired(false);
    	frequency.setRequired(false);
    	multitenant.setRequired(false);
    	fileType.setRequired(false);
    	useCase.setRequired(false);
    	mappingDetails.setRequired(false);
    	sftpDetails.setRequired(false);
    	break;
	case "Other - Internal":
    	requestType.setHelpText("This type is exclusively for internal DOPS projects and tasks and should only be created by a DOPS team member.");
		dueDate.setRequired(false);
		clientName.setRequired(false);
		linkToSfRecord.setRequired(false);
		clientCode.setRequired(false);
		partnerName.setRequired(false);
    	link.setRequired(false);
    	namingConvention.setRequired(false);
    	frequency.setRequired(false);
    	multitenant.setRequired(false);
    	fileType.setRequired(false);
    	useCase.setRequired(false);
    	mappingDetails.setRequired(false);
    	sftpDetails.setRequired(false);
    	break;
    default:
        	requestType.setHelpText("");
        	partnerName.setRequired(false);
    		link.setRequired(false);
    		namingConvention.setRequired(false);
    		frequency.setRequired(false);
    		multitenant.setRequired(false);
    		fileType.setRequired(false);
    		useCase.setRequired(false);
    		mappingDetails.setRequired(false);
    		sftpDetails.setRequired(false);
        	break;
}
log.info("Behaviours DATAOPS Project – Request Type completed.")