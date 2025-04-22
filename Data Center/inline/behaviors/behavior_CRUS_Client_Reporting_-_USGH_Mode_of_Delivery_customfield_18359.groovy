/**
 * Eduardo Rojo
 * CRUS project - Client Reporting - USGH
 * Issue Type: Story
 * Hide fields based on "Mode of Delivery"
 * Log: 5/3/2023 Eduardo Rojo (JIRA-5208)
 */

import com.atlassian.jira.component.ComponentAccessor;
import com.atlassian.jira.issue.customfields.option.Option;
log.info("Behaviours CRUS Project – Mode of Delivery started.");

def customFieldManager = ComponentAccessor.getCustomFieldManager()
def optionsManager = ComponentAccessor.getOptionsManager();

// This listens if the "Mode of Delivery" field has changed
def modeOfDelivery = getFieldById(getFieldChanged());

// This saves the custom field "SFTP - Client/Vendor Details" and "Email Delivery - Provide Email"
def vendorDetails = getFieldByName("SFTP Host and Folder");
def emailDelivery = getFieldByName("Email Delivery - Provide Email");
def boxLocation = getFieldByName("Box Location"); //added 5/3/2023 Eduardo Rojo (JIRA-5208) 

// This gets the value of "Mode of Delivery" field
def modeOfDeliveryOption = modeOfDelivery.getValue() as String

log.info("Mode of Delivery: " + modeOfDeliveryOption);
// Saves true/false if the "Mode of Delivery" option is match
def vendorDetailsNew = modeOfDeliveryOption == "SFTP"
//def vendorDetailsExisting = modeOfDeliveryOption == "SFTP (Existing Connection)"
def emailDeliveryNew = modeOfDeliveryOption == "Email Delivery – For reports not containing PHI or PII"

def boxLocationNew = modeOfDeliveryOption == "Box" //added 5/3/2023 Eduardo Rojo (JIRA-5208) 

// Shows "SFTP - Client/Vendor Details" field when the option is "SFTP"
if(vendorDetailsNew){
    vendorDetails.setHidden(false);
	vendorDetails.setRequired(true);
}
else{
    vendorDetails.setHidden(true);
	vendorDetails.setRequired(false);
}

// // Shows "Email Delivery - Provide Email" field when the option is "Email Delivery – For reports not containing PHI or PII"
emailDelivery.setHidden(!emailDeliveryNew);
emailDelivery.setRequired(emailDeliveryNew);

boxLocation.setHidden(!boxLocationNew); //added 5/3/2023 Eduardo Rojo (JIRA-5208)
boxLocation.setRequired(boxLocationNew); //added 5/3/2023 Eduardo Rojo (JIRA-5208)
log.info("Behaviours CRUS Project – Mode of Delivery completed.")