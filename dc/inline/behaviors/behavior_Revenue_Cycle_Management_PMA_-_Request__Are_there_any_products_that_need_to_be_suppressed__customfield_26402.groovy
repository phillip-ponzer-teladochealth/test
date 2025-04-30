/**
 * Eduardo Rojo
 * PMA project
 * Show - Hide fields based on "Are there any products that need to be suppressed?" custom field
 */
import com.atlassian.jira.component.ComponentAccessor;
import com.atlassian.jira.issue.customfields.option.Option;
log.info("Behaviours PMA Project – Do group(s) need to be supressed? started.");


def customFieldManager = ComponentAccessor.getCustomFieldManager();
def optionsManager = ComponentAccessor.getOptionsManager();

def areThereAnyProductsThatNeedToBeSuppressed = getFieldById(getFieldChanged());

// This gets the value of "Do group(s) need to be supressed?" as String
def areThereAnyProductsThatNeedToBeSuppressedOption = areThereAnyProductsThatNeedToBeSuppressed.getValue() as String

def productName = getFieldByName("Product Name");

if (areThereAnyProductsThatNeedToBeSuppressedOption == "Yes"){
    productName.setHidden(false);
    productName.setRequired(true);
}
else{
    productName.setHidden(true);
    productName.setRequired(false);
}