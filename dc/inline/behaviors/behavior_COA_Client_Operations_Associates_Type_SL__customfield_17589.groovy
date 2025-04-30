/**
 * Eduardo Rojo
 * COA project - Client Operations Associates
 * Issue Type: Task
 * Hide fields based on "Type (SL)" Question and set values for Report Type
 * Log: 12/28/2022 Eduardo Rojo (JIRA-6327)
 */
import com.atlassian.jira.component.ComponentAccessor;
import com.atlassian.jira.issue.customfields.option.Option;
log.info("Behaviours COA Project – Request Type started.");

def customFieldManager = ComponentAccessor.getCustomFieldManager();
def optionsManager = ComponentAccessor.getOptionsManager();

// This listens if the "Request Type" field has changed
def typeSL = getFieldById(getFieldChanged());


// This gets the value of "Request Type" as String
def typeSLOption = typeSL.getValue() as String

def reportName = getFieldByName("Report Name");
def groupId = getFieldByName("Group ID, Org ID or Client Code");
def linkToSFRecord = getFieldByName("Link to SF Record");
def programs = getFieldByName("Programs");
def frequency = getFieldByName("Frequency");


log.info("Behaviours COA Project – Request Type: " + typeSLOption);

switch(typeSLOption){
    case "Standard Report Request":
		typeSL.setDescription("This report already exists in Sharepoint or Cognos.");
		reportName.setRequired(false); //change requested by Kelli Robinson. JIRA-6327 - Ignacio Vera 2/09/24
		groupId.setRequired(true);
		linkToSFRecord.setRequired(true);
		programs.setRequired(false); //change requested by Kelli Robinson. JIRA-6327 - Ignacio Vera 2/09/24
		programs.setHidden(false);
		frequency.setRequired(true);
    	break;
    case "Custom Report Request":
		typeSL.setDescription("This report does not exist anywhere and a custom report needs to be built.");
    	reportName.setRequired(false);
		groupId.setRequired(true);
		linkToSFRecord.setRequired(true);
		programs.setRequired(false); //change requested by Kelli Robinson. JIRA-6327 - Ignacio Vera 2/09/24
		frequency.setRequired(true);
    	break;
    case "Ad-Hoc Report Request":
		typeSL.setDescription("");
    	reportName.setRequired(false);
		groupId.setRequired(true);
		linkToSFRecord.setRequired(true);
		programs.setRequired(false); //change requested by Kelli Robinson. JIRA-6327 - Ignacio Vera 2/09/24
		frequency.setRequired(true);
    	break;
    default:
        break;
}
log.info("Behaviours COA Project – Request Type completed.")