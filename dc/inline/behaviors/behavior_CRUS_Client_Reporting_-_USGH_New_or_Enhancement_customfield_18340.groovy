/**
 * Eduardo Rojo
 * CRUS project - Client Reporting - USGH
 * Issue Type: Story
 * Hide fields based on "New or Enhancement"
 * Log: 5/15/2023 Eduardo Rojo (JIRA-5208)
 *		6/5/2024 Eduardo Rojo (JIRA-9551)
 */

import com.atlassian.jira.component.ComponentAccessor;
import com.atlassian.jira.issue.customfields.option.Option;
log.info("Behaviours CRUS Project – New or Enhancement started.");

def customFieldManager = ComponentAccessor.getCustomFieldManager()
def optionsManager = ComponentAccessor.getOptionsManager();

// This listens if the "Report Type" field has changed
def newOrEnhancement = getFieldById(getFieldChanged());
// This saves custom field's ids to manage its behaviors
def standardDataExtractType = getFieldByName("Standard Data Extract Type");
def areThereAnyTags = getFieldByName("Are there any tags?");
def clientName = getFieldByName("Client Name");
def clientLaunchDate = getFieldByName("Client Launch Date");
def contractLanguageForGrievanceReporting = getFieldByName("Contract Language for Grievance Reporting");
def clientLevel = getFieldByName("Client Level");
def businessRequirements = getFieldByName("Business Requirements");
def isThisReusable = getFieldByName("Is this Reusable?");
def reportingTeamOwner = getFieldByName("Reporting Team Owner");
def approver = getFieldByName("Approver");
def audience = getFieldByName("Audience (SL)");
def audienceDetails = getFieldByName("Audience Details");
def contractualObligation = getFieldByName("Contractual Obligation");
def sOWorBRDRequired = getFieldByName("SOW or BRD required?");
def sensitiveInformation = getFieldByName("Sensitive Information");
//def dataSharingAgreement = getFieldByName("Data Sharing Agreement"); // updated (JIRA-5208)
def fileTypePGPEncryption = getFieldByName("File Type PGP Encryption");
def fileTypeExtension = getFieldByName("File Type Extension");
def fileTypeDelimiter = getFieldByName("File Type Delimiter");
def extensionDetails = getFieldByName("Extension Details");
def frequency = getFieldByName("Frequency");
def modeOfDelivery = getFieldByName("Mode of Delivery");
def modeOfDeliveryVendor = getFieldByName("SFTP - Client/Vendor Details");
def modeOfDeliveryEmail = getFieldByName("Email Delivery - Provide Email");
def priorityScore = getFieldByName("Priority Score");
def standardAddOns = getFieldByName("Standard Add Ons");//added JIRA-9551

// This gets the value of "New or Enhancement"
def newOrEnhancementOption = newOrEnhancement.getValue() as String

// This resets the fields in case the user changes the "Report Type"
standardDataExtractType.setHidden(true);
standardDataExtractType.setRequired(false);

log.info("New or Enhancement: " + newOrEnhancementOption);
switch(newOrEnhancementOption){
    case "Standard - New":
    	// Show/Hide fields for "Standar - New" option
    	standardDataExtractType.setHidden(false);
		standardDataExtractType.setRequired(true);
		areThereAnyTags.setHidden(false);
		areThereAnyTags.setRequired(true);
		//clientName.setHidden(true);
		//clientName.setRequired(false);
		//clientLaunchDate.setHidden(true); // updated (JIRA-5208)
		clientLaunchDate.setRequired(false);
		contractLanguageForGrievanceReporting.setHidden(true);
		contractLanguageForGrievanceReporting.setRequired(false); 
    	// Show/Hide other fields
    	clientLevel.setRequired(false);
    	businessRequirements.setRequired(false);
    	isThisReusable.setHidden(true);
		isThisReusable.setRequired(false); 
    	reportingTeamOwner.setHidden(false);
    	approver.setHidden(false);
    	audience.setRequired(true);
    	audienceDetails.setRequired(true);
    	contractualObligation.setRequired(true);
    	sOWorBRDRequired.setHidden(false);
		//sOWorBRDRequired.setRequired(true); // updated (JIRA-5208)
    	sensitiveInformation.setHidden(false);
		sensitiveInformation.setRequired(true);
    	// dataSharingAgreement.setHidden(false); // updated (JIRA-5208)
		// dataSharingAgreement.setRequired(true); // updated (JIRA-5208)
		fileTypePGPEncryption.setHidden(false);
    	fileTypeExtension.setHidden(false);
    	fileTypeDelimiter.setHidden(false);
    	extensionDetails.setHidden(true);
    	frequency.setHidden(false);
		frequency.setRequired(true);
    	modeOfDelivery.setHidden(false);
		modeOfDelivery.setRequired(true);
    	modeOfDeliveryVendor.setHidden(true);
    	modeOfDeliveryEmail.setHidden(true);
    	priorityScore.setHidden(true);
		standardAddOns.setHidden(false);//added JIRA-9551
    	break;
    case "Standard - Enhancement":
    	// Show/Hide fields for "Standar - New" option
    	standardDataExtractType.setHidden(false);
		standardDataExtractType.setRequired(true);
		areThereAnyTags.setHidden(false);
		areThereAnyTags.setRequired(true);
		//clientName.setHidden(true);
		//clientName.setRequired(false);
		//clientLaunchDate.setHidden(true); // updated (JIRA-5208)
		clientLaunchDate.setRequired(false);
		contractLanguageForGrievanceReporting.setHidden(true);
		contractLanguageForGrievanceReporting.setRequired(false); 
    	// Show/Hide other fields
    	clientLevel.setRequired(false);
    	businessRequirements.setRequired(false);
    	isThisReusable.setHidden(true);
		isThisReusable.setRequired(false); 
    	reportingTeamOwner.setHidden(false);
    	approver.setHidden(false);
    	audience.setRequired(true);
    	audienceDetails.setRequired(true);
    	contractualObligation.setRequired(true);
    	sOWorBRDRequired.setHidden(false);
		//sOWorBRDRequired.setRequired(true); // updated (JIRA-5208)
    	sensitiveInformation.setHidden(false);
		sensitiveInformation.setRequired(true);
    	// dataSharingAgreement.setHidden(false); // updated (JIRA-5208)
		// dataSharingAgreement.setRequired(true); // updated (JIRA-5208)
		fileTypePGPEncryption.setHidden(false);
    	fileTypeExtension.setHidden(false);
    	fileTypeDelimiter.setHidden(false);
    	extensionDetails.setHidden(true);
    	frequency.setHidden(false);
		frequency.setRequired(true);
    	modeOfDelivery.setHidden(false);
		modeOfDelivery.setRequired(true);
    	modeOfDeliveryVendor.setHidden(true);
    	modeOfDeliveryEmail.setHidden(true);
    	priorityScore.setHidden(true);
		standardAddOns.setHidden(false);//added JIRA-9551
		/* ************COMMENTED JIRA-9551
		// Show/Hide fields for "Standar - New" option
    	standardDataExtractType.setHidden(true);
		standardDataExtractType.setRequired(false);
		areThereAnyTags.setHidden(true);
		areThereAnyTags.setRequired(false);
		//clientName.setHidden(true);
		//clientName.setRequired(false);
		//clientLaunchDate.setHidden(true); // updated (JIRA-5208)
		clientLaunchDate.setRequired(false);
		contractLanguageForGrievanceReporting.setHidden(true);
		contractLanguageForGrievanceReporting.setRequired(false); 
    	// Show/Hide fields for "Standard - Enhancement" option
    	clientLevel.setRequired(true);
    	businessRequirements.setRequired(true);
    	isThisReusable.setHidden(false);
		isThisReusable.setRequired(false); 
    	reportingTeamOwner.setHidden(false);
    	approver.setHidden(false);
    	audience.setRequired(false);
    	audienceDetails.setRequired(false);
    	contractualObligation.setRequired(true);
    	sOWorBRDRequired.setHidden(false);
		//sOWorBRDRequired.setRequired(true); // updated (JIRA-5208)
    	sensitiveInformation.setHidden(false);
		sensitiveInformation.setRequired(true);
    	// dataSharingAgreement.setHidden(false); // updated (JIRA-5208)
		// dataSharingAgreement.setRequired(true); // updated (JIRA-5208)
		fileTypePGPEncryption.setHidden(false);
    	fileTypeExtension.setHidden(false);
    	fileTypeDelimiter.setHidden(false);
    	extensionDetails.setHidden(true);
    	frequency.setHidden(false);
		frequency.setRequired(true);
    	modeOfDelivery.setHidden(false);
		modeOfDelivery.setRequired(true);
    	modeOfDeliveryVendor.setHidden(true);
    	modeOfDeliveryEmail.setHidden(true);
    	priorityScore.setHidden(false);
		standardAddOns.setHidden(true);//added JIRA-9551
    	*/
		break;
    case "Custom - New":
    	// Show/Hide fields for "Standar - New" option
    	standardDataExtractType.setHidden(true);
		standardDataExtractType.setRequired(false);
		areThereAnyTags.setHidden(true);
		areThereAnyTags.setRequired(false);
		//clientName.setHidden(true);
		//clientName.setRequired(false);
		//clientLaunchDate.setHidden(true); // updated (JIRA-5208)
		clientLaunchDate.setRequired(false);
		contractLanguageForGrievanceReporting.setHidden(true);
		contractLanguageForGrievanceReporting.setRequired(false); 
    	// Show/Hide fields for "Custom - New" option
		clientLevel.setRequired(true);
    	businessRequirements.setRequired(true);
    	isThisReusable.setHidden(false);
		isThisReusable.setRequired(true); 
    	reportingTeamOwner.setHidden(false);
    	approver.setHidden(false);
    	audience.setRequired(true);
    	audienceDetails.setRequired(true);
    	contractualObligation.setRequired(true);
    	sOWorBRDRequired.setHidden(false);
		//sOWorBRDRequired.setRequired(true); // updated (JIRA-5208)
    	sensitiveInformation.setHidden(false);
		sensitiveInformation.setRequired(true);
    	// dataSharingAgreement.setHidden(false); // updated (JIRA-5208)
		// dataSharingAgreement.setRequired(true); // updated (JIRA-5208)
		fileTypePGPEncryption.setHidden(false);
    	fileTypeExtension.setHidden(false);
    	fileTypeDelimiter.setHidden(false);
    	extensionDetails.setHidden(true);
    	frequency.setHidden(false);
		frequency.setRequired(true);
    	modeOfDelivery.setHidden(false);
		modeOfDelivery.setRequired(true);
    	modeOfDeliveryVendor.setHidden(true);
    	modeOfDeliveryEmail.setHidden(true);
    	priorityScore.setHidden(false);
		standardAddOns.setHidden(true);//added JIRA-9551
    	break;
    case "Custom - Enhancement":
    	// Show/Hide fields for "Standar - New" option
    	standardDataExtractType.setHidden(true);
		standardDataExtractType.setRequired(false);
		areThereAnyTags.setHidden(true);
		areThereAnyTags.setRequired(false);
		//clientName.setHidden(true);
		//clientName.setRequired(false);
		//clientLaunchDate.setHidden(true); // updated (JIRA-5208)
		clientLaunchDate.setRequired(false);
		contractLanguageForGrievanceReporting.setHidden(true);
		contractLanguageForGrievanceReporting.setRequired(false); 
    	// Show/Hide fields for "Custom - Enhancement" option
		clientLevel.setRequired(true);
    	businessRequirements.setRequired(true);
    	isThisReusable.setHidden(false);
		isThisReusable.setRequired(false); 
    	reportingTeamOwner.setHidden(false);
    	approver.setHidden(false);
    	audience.setRequired(false);
    	audienceDetails.setRequired(false);
    	contractualObligation.setRequired(true);
    	sOWorBRDRequired.setHidden(false);
		//sOWorBRDRequired.setRequired(true); // updated (JIRA-5208)
    	sensitiveInformation.setHidden(false);
		sensitiveInformation.setRequired(true);
    	// dataSharingAgreement.setHidden(false); // updated (JIRA-5208)
		// dataSharingAgreement.setRequired(true); // updated (JIRA-5208)
		fileTypePGPEncryption.setHidden(false);
    	fileTypeExtension.setHidden(false);
    	fileTypeDelimiter.setHidden(false);
    	extensionDetails.setHidden(true);
    	frequency.setHidden(false);
		frequency.setRequired(true);
    	modeOfDelivery.setHidden(false);
		modeOfDelivery.setRequired(true);
    	modeOfDeliveryVendor.setHidden(true);
    	modeOfDeliveryEmail.setHidden(true);
    	priorityScore.setHidden(false);
		standardAddOns.setHidden(true);//added JIRA-9551
    	break;
    case "New":
    	// Show/Hide fields for "New" option
    	standardDataExtractType.setHidden(true);
		standardDataExtractType.setRequired(false);
		areThereAnyTags.setHidden(true);
		areThereAnyTags.setRequired(false);
		//clientName.setHidden(true);
		//clientName.setRequired(false);
		//clientLaunchDate.setHidden(true); // updated (JIRA-5208)
		clientLaunchDate.setRequired(false);
		contractLanguageForGrievanceReporting.setHidden(true);
		contractLanguageForGrievanceReporting.setRequired(false); 
    	// Show/Hide fields for "New" option
		clientLevel.setRequired(true);
    	businessRequirements.setRequired(true);
    	isThisReusable.setHidden(true);
		isThisReusable.setRequired(false); 
    	reportingTeamOwner.setHidden(false);
    	approver.setHidden(false);
    	audience.setRequired(true);
    	audienceDetails.setRequired(true);
    	contractualObligation.setRequired(true);
    	sOWorBRDRequired.setHidden(false);
		//sOWorBRDRequired.setRequired(true); // updated (JIRA-5208)
    	sensitiveInformation.setHidden(false);
		sensitiveInformation.setRequired(true);
    	// dataSharingAgreement.setHidden(false); // updated (JIRA-5208)
		// dataSharingAgreement.setRequired(true); // updated (JIRA-5208)
		fileTypePGPEncryption.setHidden(false);
    	fileTypeExtension.setHidden(false);
    	fileTypeDelimiter.setHidden(false);
    	extensionDetails.setHidden(true);
    	frequency.setHidden(false);
		frequency.setRequired(true);
    	modeOfDelivery.setHidden(false);
		modeOfDelivery.setRequired(true);
    	modeOfDeliveryVendor.setHidden(true);
    	modeOfDeliveryEmail.setHidden(true);
		standardAddOns.setHidden(true);//added JIRA-9551
    	break;
    case "Enhancement":
    	// Show/Hide fields for "Standar - New" option
    	standardDataExtractType.setHidden(true);
		standardDataExtractType.setRequired(false);
		areThereAnyTags.setHidden(true);
		areThereAnyTags.setRequired(false);
		//clientName.setHidden(true);
		//clientName.setRequired(false);
		//clientLaunchDate.setHidden(true); // updated (JIRA-5208)
		clientLaunchDate.setRequired(false);
		contractLanguageForGrievanceReporting.setHidden(true);
		contractLanguageForGrievanceReporting.setRequired(false); 
    	// Show/Hide fields for "Enhancement" option
		clientLevel.setRequired(true);
    	businessRequirements.setRequired(true);
    	isThisReusable.setHidden(false);
		isThisReusable.setRequired(false); 
    	reportingTeamOwner.setHidden(false);
    	approver.setHidden(false);
    	audience.setRequired(false);
    	audienceDetails.setRequired(false);
    	contractualObligation.setRequired(true);
    	sOWorBRDRequired.setHidden(false);
		//sOWorBRDRequired.setRequired(true); // updated (JIRA-5208)
    	sensitiveInformation.setHidden(false);
		sensitiveInformation.setRequired(true);
    	// dataSharingAgreement.setHidden(false); // updated (JIRA-5208)
		// dataSharingAgreement.setRequired(true); // updated (JIRA-5208)
		fileTypePGPEncryption.setHidden(false);
    	fileTypeExtension.setHidden(false);
    	fileTypeDelimiter.setHidden(false);
    	extensionDetails.setHidden(true);
    	frequency.setHidden(false);
		frequency.setRequired(true);
    	modeOfDelivery.setHidden(false);
		modeOfDelivery.setRequired(true);
    	modeOfDeliveryVendor.setHidden(true);
    	modeOfDeliveryEmail.setHidden(true);
		standardAddOns.setHidden(true);//added JIRA-9551
    	break;
    default:
		standardAddOns.setHidden(true);//added JIRA-9551
        break;
}
log.info("Behaviours CRUS Project – New or Enhancement completed.");