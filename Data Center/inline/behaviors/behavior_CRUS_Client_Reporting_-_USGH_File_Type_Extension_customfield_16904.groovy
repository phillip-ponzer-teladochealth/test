/**
 * Eduardo Rojo
 * CRUS project - Client Reporting - USGH
 * Issue Type: Story
 * Hide fields based on "File Type Extension"
 */

import com.atlassian.jira.component.ComponentAccessor;
import com.atlassian.jira.issue.customfields.option.Option;
log.info("Behaviours CRUS Project – File Type Extension started.");

def customFieldManager = ComponentAccessor.getCustomFieldManager()
def optionsManager = ComponentAccessor.getOptionsManager();

// This listens if the "File Type Extension" field has changed
def fileTypeExtension = getFieldById(getFieldChanged());

// This saves the custom field "Extension Details" under the name extensionDetails
def extensionDetails = getFieldByName("Extension Details");

// This gets the value of "File Type Extension"
def fileTypeExtensionOption = fileTypeExtension.getValue() as String

log.info("File Type Extension: " + fileTypeExtensionOption);
// Saves true/false if the value of the option is "other"
def other = fileTypeExtensionOption == "other"

// Show/Require "Extension Details" field
extensionDetails.setHidden(!other);
extensionDetails.setRequired(other);
log.info("Behaviours CRUS Project – File Type Extension completed.") 