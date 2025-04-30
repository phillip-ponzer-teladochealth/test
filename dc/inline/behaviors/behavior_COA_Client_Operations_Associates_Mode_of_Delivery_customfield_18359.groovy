/**
 * Eduardo Rojo
 * COA project - Client Operations Associates
 * Hide fields based on "Mode of Delivery" value
 */

import com.atlassian.jira.component.ComponentAccessor;
import com.atlassian.jira.issue.customfields.option.Option;
log.info("Behaviours COA Project – Mode of Delivery started.");

def customFieldManager = ComponentAccessor.getCustomFieldManager()
def optionsManager = ComponentAccessor.getOptionsManager();

// This listens if the "Mode of Delivery" field has changed
def modeOfDelivery = getFieldById(getFieldChanged());

// This saves the custom field "SFTP - Client/Vendor Details" and "Email Delivery - Provide Email"
def sftpHostAndFolder = getFieldByName("SFTP Host and Folder");
def emailDelivery = getFieldByName("Email Delivery - Provide Email");

// This gets the value of "Mode of Delivery" field
def modeOfDeliveryOption = modeOfDelivery.getValue() as String

log.info("Mode of Delivery: " + modeOfDeliveryOption);
// Saves true/false if the "Mode of Delivery" option is match
def sftpHostAndFolderNew = modeOfDeliveryOption == "SFTP"
//def vendorDetailsExisting = modeOfDeliveryOption == "SFTP (Existing Connection)"
def emailDeliveryNew = modeOfDeliveryOption == "Secure Email"

// Shows "SFTP - Client/Vendor Details" field when the option is "SFTP"
if(sftpHostAndFolderNew){
    sftpHostAndFolder.setHidden(false);
	sftpHostAndFolder.setRequired(true);
}
else{
    sftpHostAndFolder.setHidden(true);
	sftpHostAndFolder.setRequired(false);
}
sftpHostAndFolder.setDescription("");
emailDelivery.setDescription("");

// // Shows "Email Delivery - Provide Email" field when the option is "Email Delivery – For reports not containing PHI or PII"
emailDelivery.setHidden(!emailDeliveryNew);
emailDelivery.setRequired(emailDeliveryNew);
log.info("Behaviours COA Project – Mode of Delivery completed.")