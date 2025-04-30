/**
 * Eduardo Rojo
 * CRUS project - Client Reporting - USGH
 * Issue Type: Story
 * Hide fields based on "Data Sharing Agreement"
 * Log: 4/18/2023 Eduardo Rojo (JIRA-5208) 
 */

import com.atlassian.jira.component.ComponentAccessor;
import com.atlassian.jira.issue.customfields.option.Option;
log.info("Behaviours CRUS Project – Data Sharing Agreement started.");

def customFieldManager = ComponentAccessor.getCustomFieldManager()
def optionsManager = ComponentAccessor.getOptionsManager();

// This listens if the "Contractual Obligation" field has changed
def dataSharingAgreement = getFieldById(getFieldChanged());

// This saves the custom field "DSA Link" under the name tags
def dsaLink = getFieldByName("DSA Link");

// This gets the value of "Contractual Obligation""
def dataSharingAgreementOption = dataSharingAgreement.getValue() as String

log.info("Data Sharing Agreement: " + dataSharingAgreementOption);
// Show/Hide and set "DSA Link" as Required 
def dsaLinkNew = dataSharingAgreementOption == "Yes"
dsaLink.setHidden(!dsaLinkNew);
// dsaLink.setRequired(dsaLinkNew);
log.info("Behaviours CRUS Project – Data Sharing Agreement completed.");