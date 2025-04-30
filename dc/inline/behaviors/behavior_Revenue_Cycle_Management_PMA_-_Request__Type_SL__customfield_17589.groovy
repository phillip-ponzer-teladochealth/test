/**
 * Eduardo Rojo
 * PMA project
 * Show - Hide fields based on "Type (SL)" custom field
 */
import com.atlassian.jira.component.ComponentAccessor;
import com.atlassian.jira.issue.customfields.option.Option;
log.info("Behaviours PMA Project – Type (SL) started.");


def customFieldManager = ComponentAccessor.getCustomFieldManager();
def optionsManager = ComponentAccessor.getOptionsManager();

def typeSL = getFieldById(getFieldChanged());

// This gets the value of "Type (SL)" as String
def typeSLOption = typeSL.getValue() as String

def isThisaGMConsultsIncludedRequest = getFieldByName("Is this a GM Consults included request?");

switch (typeSLOption){
    case "Chronic Care Management (CCM)":
        isThisaGMConsultsIncludedRequest.setHidden(true);
    break;
    case "Telemedicine":
        isThisaGMConsultsIncludedRequest.setHidden(false);
    break;
    case "Both Telemedicine & Chronic Care Management (CCM)":
        isThisaGMConsultsIncludedRequest.setHidden(false);
    break;
    default:
        isThisaGMConsultsIncludedRequest.setHidden(true);
    break;
}