/**
 * Eduardo Rojo
 * PMA project
 * Update "Request Type" available options based on "Issue Type"
 */
import com.atlassian.jira.component.ComponentAccessor;
import com.atlassian.jira.issue.customfields.option.Option;
log.info("Behaviours PMA Project – Issue Type started.");

def customFieldManager = ComponentAccessor.getCustomFieldManager();
def optionsManager = ComponentAccessor.getOptionsManager();


// This listens if the "Issue Type" field has changed
def issueType = getFieldById(getFieldChanged());

def requestType = getFieldByName("Request Type");

def customField = customFieldManager.getCustomFieldObject(requestType.getFieldId())
def config = customField.getRelevantConfig(getIssueContext())
def options = optionsManager.getOptions(config)
log.info("issueContext.issueType.name:"+ issueContext.issueType.name)

switch(issueContext.issueType.name){
    case "Service Request with Approvals":
        //requestType.setFieldOptions(options.findAll {it.value in ['New Claims Suppression', 'Extension of an Existing Claims Suppression','Pre Existing Suppression (GM Consults Included)','Price Overrides']})
        requestType.setFieldOptions(options.findAll {it.value in ['Price Overrides']})
        requestType.setRequired(true);
        requestType.setFormValue('Price Overrides')
    break;
    case "Task":
        requestType.setFieldOptions(options.findAll {it.value in ['Core','CCM', 'Both']})
        requestType.setRequired(true);
    break;
    default:
        requestType.setRequired(false);
    break;
}