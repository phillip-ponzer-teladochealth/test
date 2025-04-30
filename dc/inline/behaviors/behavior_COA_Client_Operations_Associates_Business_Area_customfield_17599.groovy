/**
 * Ignacio Vera
 * COA project - Client Operations Associates
 * Issue Type: Task
 * Hide/show fields based on "Business Area" field
 * Jira Ticket: JIRA-8730
 */
import com.atlassian.jira.component.ComponentAccessor;
import com.atlassian.jira.issue.customfields.option.Option;
log.info("Behaviours COA Project - Request Type started.");

def customFieldManager = ComponentAccessor.getCustomFieldManager();
def optionsManager = ComponentAccessor.getOptionsManager();

// This gets the value of "Business Area" as String
def businessArea = getFieldByName("Business Area");
def businessAreaValue = businessArea.getValue() as String;

// Get customs fields hidden in Initialiser
def lineOfBusiness = getFieldByName("Channel - Line of Business");
def clientName = getFieldByName("Client Name");
def programsConditions = getFieldByName("Programs/Conditions");

// Get required fields
def priorityField = getFieldById("priority");
def descriptionField = getFieldByName("Description");
def reportName = getFieldByName("Report Name");
def programsField = getFieldByName("Programs");
def groupIdOrgField = getFieldByName("Group ID, Org ID or Client Code");
def requestedDuedate = getFieldByName("Requested Due Date");

// Get removable fields
def fileTypeExtension = getFieldByName("File Type Extension");
def dateSpan = getFieldByName("Date Span");


if(businessAreaValue=="USGH"){
	lineOfBusiness.setHidden(false); //show and make Channel - Line of Business required
   lineOfBusiness.setRequired(true);
   reportName.setHidden(true);     // Hide Report Name
   clientName.setHidden(false);    //Show and make Client Name required
   clientName.setRequired(true);
   programsField.setHidden(true); //Hide Programs field
   programsConditions.setHidden(false); //Show Programs/Conditions field and make it required.
 	programsConditions.setRequired(true);
   descriptionField.setRequired(true); //Make fields required:
   priorityField.setRequired(true);
 	groupIdOrgField.setRequired(true);
 	requestedDuedate.setRequired(true);
 	fileTypeExtension.setHidden(true); //Hide File Type Extension 
 	dateSpan.setHidden(true); //Hide Date Span 

}
else{
	reportName.setHidden(true); // Hide Report Name 
   clientName.setHidden(false); //Show and make Client Name required  
   clientName.setRequired(true); 
   descriptionField.setRequired(true); //Make fields required:
	priorityField.setRequired(true);
 	groupIdOrgField.setRequired(true);
 	requestedDuedate.setRequired(true);
   programsField.setHidden(true); //Hide Programs field
 	fileTypeExtension.setHidden(true); //Hide File Type Extension 
 	dateSpan.setHidden(true); //Hide Date Span 

}