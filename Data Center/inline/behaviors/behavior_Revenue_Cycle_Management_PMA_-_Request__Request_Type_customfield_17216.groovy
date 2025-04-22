/**
 * Eduardo Rojo
 * PMA project
 * Update "Request Type" available options based on "Issue Type"
 */
import com.atlassian.jira.component.ComponentAccessor;
import com.atlassian.jira.issue.customfields.option.Option;
log.info("Behaviours PMA Project – Request Type started.");


def customFieldManager = ComponentAccessor.getCustomFieldManager();
def optionsManager = ComponentAccessor.getOptionsManager();

def requestType = getFieldById(getFieldChanged());

// This gets the value of "Request Type" as String
def requestTypeOption = requestType.getValue() as String

// Custom fields for all Request Type options except for "Price Overrides" option
def originalDueDate = getFieldByName("Original due date");
def dateOccurred = getFieldByName("Date Occurred");
def endDate = getFieldByName("End Date");
def dueDate = getFieldByName("Due Date"); // added JIRA-10090
def typeSL = getFieldByName("Type (SL)");
def isThisaGMConsultsIncludedRequest = getFieldByName("Is this a GM Consults included request?");
def clientName = getFieldByName("Client Name");
def doGroupNeedToBeSupressed = getFieldByName("Do group(s) need to be supressed?");
def areThereAnyProductsThatNeedToBeSuppressed = getFieldByName("Are there any products that need to be suppressed?");
def productName = getFieldByName("Product Name");

//Custom fields for "Price Overrides" Request Type option
//Description 
def actualEndDate = getFieldByName("Actual End Date");
def groupPayerCount = getFieldByName("Group/Payer Count");
def description = getFieldByName("Description");

def strDescription = """
|*Question*|*Answer*|*Question*|*Answer*|
|*Health Plan:*| |*Legacy ID/Group ID:*
{color:#de350b}**If multiple please attach spreadsheet*{color}| |
|*TEIT Ticket #:*
{color:#de350b}**Analysis required before submitting*{color}| |*New, Modification, Removal:*| |
|*Effective Date:*| |*Expiration Date:*| |
|*Pricing (Dollar Amount):*
{color:#de350b}**If multiple please attach spreadsheet*{color}| |*Products:* 
{color:#de350b}**(GM, MH, Derm, VPC, Nutrition)*{color}| |
""" 

description.setRequired(true);
productName.setHidden(true);
dateOccurred.setHelpText("Effective Date of Suppression.");

switch(requestTypeOption){
    case "New Claims Suppression":
        actualEndDate.setHidden(true);
        groupPayerCount.setHidden(true);
        //Only run on the create screen
        if ((getActionName() == "Create")) {
            description.setFormValue("");
        }
        originalDueDate.setHidden(true);
        originalDueDate.setRequired(false);
        dateOccurred.setHidden(false);
        dateOccurred.setRequired(true);
        endDate.setHidden(false);
        endDate.setRequired(true);
        dueDate.setHidden(true); // added JIRA-10090
        
        typeSL.setHidden(false);
        typeSL.setRequired(true);
        //isThisaGMConsultsIncludedRequest.setHidden(false);
        //isThisaGMConsultsIncludedRequest.setRequired(true);
        clientName.setHidden(false);
        clientName.setRequired(true);
        doGroupNeedToBeSupressed.setHidden(false);
        //doGroupNeedToBeSupressed.setRequired(true);
        areThereAnyProductsThatNeedToBeSuppressed.setHidden(false);
        //areThereAnyProductsThatNeedToBeSuppressed.setRequired(true)
    break;
    case "Extension of an Existing Claims Suppression":
        actualEndDate.setHidden(true);
        groupPayerCount.setHidden(true);
        //Only run on the create screen
        if ((getActionName() == "Create")) {
            description.setFormValue("");
        }
        originalDueDate.setHidden(false);
        originalDueDate.setRequired(true);
        dateOccurred.setHidden(true);
        dateOccurred.setRequired(false);
        endDate.setHidden(true);
        endDate.setRequired(false);
        dueDate.setHidden(true); // added JIRA-10090

        typeSL.setHidden(false);
        typeSL.setRequired(true);
        //isThisaGMConsultsIncludedRequest.setHidden(false);
        clientName.setHidden(false);
        clientName.setRequired(true);
        doGroupNeedToBeSupressed.setHidden(false);
        areThereAnyProductsThatNeedToBeSuppressed.setHidden(false);
    break;
    case "Pre Existing Suppression (GM Consults Included)":
        actualEndDate.setHidden(true);
        groupPayerCount.setHidden(true);
        //Only run on the create screen
        if ((getActionName() == "Create")) {
            description.setFormValue("");
        }
        originalDueDate.setHidden(true);
        originalDueDate.setRequired(false);
        dateOccurred.setHidden(false);
        dateOccurred.setRequired(true);
        endDate.setHidden(false)
        endDate.setRequired(true);
        dueDate.setHidden(true); // added JIRA-10090

        typeSL.setHidden(false);
        typeSL.setRequired(true);
        //isThisaGMConsultsIncludedRequest.setHidden(false);
        clientName.setHidden(false);
        clientName.setRequired(true);
        doGroupNeedToBeSupressed.setHidden(false);
        areThereAnyProductsThatNeedToBeSuppressed.setHidden(false);
    break;
    case "Price Overrides":
        actualEndDate.setHidden(false);
        groupPayerCount.setHidden(false);
        //Only run on the create screen
        if ((getActionName() == "Create")) {
            description.setFormValue(strDescription);
        }
        originalDueDate.setHidden(true);
        originalDueDate.setRequired(false);
        dateOccurred.setHidden(true);
        dateOccurred.setRequired(false);
        endDate.setHidden(true);
        endDate.setRequired(false);
        dueDate.setHidden(false); // added JIRA-10090

        typeSL.setHidden(true);
        typeSL.setRequired(false);
        //isThisaGMConsultsIncludedRequest.setHidden(true);
        clientName.setHidden(true);
        clientName.setRequired(false);
        doGroupNeedToBeSupressed.setHidden(true);
        areThereAnyProductsThatNeedToBeSuppressed.setHidden(true);
    break;
    default:
        actualEndDate.setHidden(false);
        groupPayerCount.setHidden(false);
        //Only run on the create screen
        if ((getActionName() == "Create")) {
            description.setFormValue("");
        }
        originalDueDate.setHidden(true);
        dateOccurred.setHidden(true);
        endDate.setHidden(false)

        typeSL.setHidden(true);
        isThisaGMConsultsIncludedRequest.setHidden(true);
        clientName.setHidden(true);
        clientName.setRequired(false);
        doGroupNeedToBeSupressed.setHidden(true);
        areThereAnyProductsThatNeedToBeSuppressed.setHidden(true);
    break;
}