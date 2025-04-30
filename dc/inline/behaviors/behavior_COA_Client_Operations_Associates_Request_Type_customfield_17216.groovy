/**
 * Eduardo Rojo
 * COA project - Client Operations Associates
 * Issue Type: Task
 * Hide fields based on "Request Type" Question and set values for Report Type
 * Log: 12/28/2022 Eduardo Rojo (JIRA-6327)
 */
import com.atlassian.jira.component.ComponentAccessor;
import com.atlassian.jira.issue.customfields.option.Option;
log.info("Behaviours COA Project – Request Type started.");

def customFieldManager = ComponentAccessor.getCustomFieldManager();
def optionsManager = ComponentAccessor.getOptionsManager();

// This listens if the "Request Type" field has changed
def requestType = getFieldById(getFieldChanged());

// This saves custom field's ids to manage its behaviors
def typeSL = getFieldByName("Type (SL)");

// This gets the value of "Request Type" as String
def requestTypeOption = requestType.getValue() as String

if(requestTypeOption=="Reporting Request"){
	typeSL.setRequired(true);
	typeSL.setHidden(false);
}
else{
	typeSL.setRequired(false);
	typeSL.setHidden(true);
}