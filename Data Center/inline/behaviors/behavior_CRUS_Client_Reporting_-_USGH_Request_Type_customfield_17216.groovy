/**import org.apache.jasper.Options

 * Eduardo Rojo
 * CRUS project - Client Reporting - USGH
 * Issue Type: Story
 * Hide fields based on "Request Type" Question and set values for Report Type
 * Log: 3/8/2023 Eduardo Rojo (JIRA-5208)
 * ER 2/2024 JIRA-8908
 */
import com.atlassian.jira.component.ComponentAccessor;
import com.atlassian.jira.issue.customfields.option.Option;
import com.atlassian.jira.issue.customfields.option.Options
log.info("Behaviours CRUS Project – Request Type started.");

def customFieldManager = ComponentAccessor.getCustomFieldManager();
def optionsManager = ComponentAccessor.getOptionsManager();

// This listens if the "Request Type" field has changed
def requestType = getFieldById(getFieldChanged());

// This saves custom field's ids to manage its behaviors
def newOrEnhancement = getFieldByName("New or Enhancement");
def standardDataExtractType = getFieldByName("Standard Data Extract Type");
def reportName = getFieldByName("Report Name");
def description = getFieldByName("Description");
def clientLevel = getFieldByName("Client Level");
def businessRequirements = getFieldByName("Business Requirements");
def areThereAnyTags = getFieldByName("Are there any tags?");
def clientName = getFieldByName("Client Name");
def clientLaunchDate = getFieldByName("Client Launch Date");
def contractLanguageForGrievanceReporting = getFieldByName("Contract Language for Grievance Reporting");
def isThisReusable = getFieldByName("Is this Reusable?");
def reportingTeamOwner = getFieldByName("Reporting Team Owner");
def approver = getFieldByName("Approver");
def audience = getFieldByName("Audience (SL)");
def audienceDetails = getFieldByName("Audience Details");
def contractualObligation = getFieldByName("Contractual Obligation");
def sOWorBRDRequired = getFieldByName("SOW or BRD required?");
def sensitiveInformation = getFieldByName("Sensitive Information");
def dataSharingAgreement = getFieldByName("Data Sharing Agreement");
def fileTypePGPEncryption = getFieldByName("File Type PGP Encryption");
def fileTypeExtension = getFieldByName("File Type Extension");
def fileTypeDelimiter = getFieldByName("File Type Delimiter");
def extensionDetails = getFieldByName("Extension Details");
def testFileRequired = getFieldByName("Test file Required?");
def frequency = getFieldByName("Frequency");
def modeOfDelivery = getFieldByName("Mode of Delivery");
def modeOfDeliveryVendor = getFieldByName("SFTP Host and Folder");
def modeOfDeliveryEmail = getFieldByName("Email Delivery - Provide Email"); 
def priorityScore = getFieldByName("Priority Score");
// Start (JIRA-5208)
def purpose = getFieldByName("Purpose");
def clientType = getFieldByName("Client Type");
def services = getFieldByName("Services");
def products = getFieldByName("Products (TF)");
def groupName = getFieldByName("Group Name");
def recipientCompany = getFieldByName("Recipient Company");
def recipientType = getFieldByName("Recipient Type");
def boxLocarion = getFieldByName("Box Location");
def dataDeliveryApprovalReason = getFieldByName("Data Delivery Approval Reason");
def privacyApproval = getFieldByName("Privacy Approval");
def privacyApprovalComments = getFieldByName("Privacy Approval Comments");
def informationSecurityApproval = getFieldByName("Information Security Approval");
def informationSecurityApprovalComments = getFieldByName("Information Security Approval Comments");
def clinicalAnalyticsApproval = getFieldByName("Clinical Analytics Approval");
def clinicalAnalyticsApprovalComments = getFieldByName("Clinical Analytics Approval Comments");
def dataScienceApproval = getFieldByName("Data Science Approval");
def dataScienceApprovalComments = getFieldByName("Data Science Approval Comments");
def pricing = getFieldByName("Pricing (MC)");
def levelOfEffort = getFieldByName("Level of Effort");
// End (JIRA-5208)

// Start - ER 2/2024 JIRA-8908
def noOfEligibleLivesForTheClient = getFieldByName("No. of Eligible Lives for the Client");
def strategicValueOfRequest = getFieldByName("Strategic Value of Request");
def isThereAWorkaroundToGetYourDataOrSolution = getFieldByName("Is there a workaround to get your data or solution?");
def escalation = getFieldByName("Escalation");
// End ER 2/2024 JIRA-8908

// This gets the value of "Request Type" as String
def requestTypeOption = requestType.getValue() as String

log.info("Request Type: " + requestTypeOption);
// This get the values of "Report Type" to set different options 
def cfField = customFieldManager.getCustomFieldObject(newOrEnhancement.getFieldId())
def cfConfig = cfField.getRelevantConfig(getIssueContext())
def cfOptions = optionsManager.getOptions(cfConfig)


switch(requestTypeOption){
    case "Data Extract | Member/Visit/Event level":
    	// Showing/Requiring Report Type
    	newOrEnhancement.setHidden(false);
    	newOrEnhancement.setRequired(true);
    	// Showing only some values of the original list
		def cfA = cfOptions.findAll 
    	{ it.value in ["----","Standard - New","Standard - Enhancement","Custom - New","Custom - Enhancement"] }.collectEntries 
    	{ [ (it.optionId.toString()) : it.value ] } as Map
		newOrEnhancement.setFieldOptions(cfA);
    	log.info("Custom Option List: " + cfA);
    	clientLevel.setRequired(false);
    	reportName.setHidden(true);
		reportName.setRequired(false);

		//Start (JIRA-5208)
		dataSharingAgreement.setHidden(false);
		purpose.setHidden(false);
		purpose.setRequired(true);
		clientType.setHidden(false);
		clientType.setRequired(true);
		services.setHidden(false);
		services.setRequired(true);
		products.setHidden(false);
		clientName.setHidden(false);
		clientName.setRequired(true);
		groupName.setHidden(false);
		recipientCompany.setHidden(false);
		recipientCompany.setRequired(true);
		recipientType.setHidden(false);
		recipientType.setRequired(true);
		dataDeliveryApprovalReason.setHidden(false);
		privacyApproval.setHidden(false);
		privacyApprovalComments.setHidden(false); 
		informationSecurityApproval.setHidden(false); 
		informationSecurityApprovalComments.setHidden(false); 
		clinicalAnalyticsApproval.setHidden(false);
		clinicalAnalyticsApprovalComments.setHidden(false);
		pricing.setHidden(false);
		levelOfEffort.setHidden(false);
		dataScienceApproval.setHidden(false);
		dataScienceApprovalComments.setHidden(false);
		clientLaunchDate.setHidden(false);

		modeOfDelivery.setHidden(false);
		modeOfDelivery.setRequired(true);
		// End (JIRA-5208)

		// Start ER 2/2024 JIRA-8908
		noOfEligibleLivesForTheClient.setHidden(true);
		noOfEligibleLivesForTheClient.setRequired(false);
		isThisReusable.setHidden(false);
    	//isThisReusable.setRequired(false);
		strategicValueOfRequest.setHidden(true);
		strategicValueOfRequest.setRequired(false);
		isThereAWorkaroundToGetYourDataOrSolution.setHidden(true);
		isThereAWorkaroundToGetYourDataOrSolution.setRequired(false);
		audience.setHidden(false);
		//audience.setRequired(false);
    	audienceDetails.setHidden(false);
		//audienceDetails.setRequired(false);
		contractualObligation.setHidden(false);
		//contractualObligation.setRequired(false);
		escalation.setHidden(true);
		escalation.setRequired(false);
		priorityScore.setHidden(true);
		// End ER 2/2024 JIRA-8908

    	break;
    case "Client Report Feedback":
    	// Showing optional/required fields
    	newOrEnhancement.setHidden(true);
    	newOrEnhancement.setRequired(false);
    	clientLevel.setRequired(false);
    	isThisReusable.setHidden(false);
    	isThisReusable.setRequired(true);
    	reportingTeamOwner.setHidden(true);
    	approver.setHidden(true);
    	audience.setRequired(false);
    	audienceDetails.setRequired(false);
    	contractualObligation.setRequired(false);
    	sOWorBRDRequired.setHidden(true);
    	sOWorBRDRequired.setRequired(false);
    	sensitiveInformation.setHidden(true);
    	sensitiveInformation.setRequired(false);
    	dataSharingAgreement.setHidden(true);
    	// dataSharingAgreement.setRequired(false);
    	fileTypePGPEncryption.setHidden(true);
    	fileTypeExtension.setHidden(true);
    	fileTypeDelimiter.setHidden(true);
    	extensionDetails.setHidden(true);
    	testFileRequired.setHidden(true);
    	testFileRequired.setRequired(false);
    	frequency.setHidden(true);
    	frequency.setRequired(false);
    	modeOfDelivery.setHidden(true);
    	modeOfDelivery.setRequired(false);
    	modeOfDeliveryVendor.setHidden(true);
    	modeOfDeliveryVendor.setRequired(false);
    	modeOfDeliveryEmail.setHidden(true);
    	modeOfDeliveryEmail.setRequired(false);
    	reportName.setHidden(false);
		reportName.setRequired(false);

		//Start (JIRA-5208)
		purpose.setHidden(true);
		purpose.setRequired(false);
		clientType.setHidden(true);
		clientType.setRequired(false);
		services.setHidden(true);
		services.setRequired(false);
		products.setHidden(true);
		clientName.setHidden(true);
		clientName.setRequired(false);
		groupName.setHidden(true);
		recipientCompany.setHidden(true);
		recipientCompany.setRequired(false);
		recipientType.setHidden(true);
		recipientType.setRequired(false);
		dataDeliveryApprovalReason.setHidden(true);
		privacyApproval.setHidden(true);
		privacyApprovalComments.setHidden(true); 
		informationSecurityApproval.setHidden(true); 
		informationSecurityApprovalComments.setHidden(true); 
		clinicalAnalyticsApproval.setHidden(true);
		clinicalAnalyticsApprovalComments.setHidden(true);
		pricing.setHidden(true);
		levelOfEffort.setHidden(true);
		dataScienceApproval.setHidden(true);
		dataScienceApprovalComments.setHidden(true);
		clientLaunchDate.setHidden(true);
		// End (JIRA-5208)

		// Start ER 2/2024 JIRA-8908
		noOfEligibleLivesForTheClient.setHidden(false);
		noOfEligibleLivesForTheClient.setRequired(true);
		isThisReusable.setHidden(false);
    	isThisReusable.setRequired(true);
		strategicValueOfRequest.setHidden(false);
		strategicValueOfRequest.setRequired(true);
		isThereAWorkaroundToGetYourDataOrSolution.setHidden(false);
		isThereAWorkaroundToGetYourDataOrSolution.setRequired(true);
		audience.setHidden(false);
		audience.setRequired(true);
    	audienceDetails.setHidden(false);
		audienceDetails.setRequired(true);
		contractualObligation.setHidden(false);
		contractualObligation.setRequired(true);
		escalation.setHidden(false);
		escalation.setRequired(true);
		priorityScore.setHidden(false);
		// End ER 2/2024 JIRA-8908
    	break;
    case "Custom Client Report | Aggregated/Summary":
    	// Showing/Requiring Report Type
    	newOrEnhancement.setHidden(false);
    	newOrEnhancement.setRequired(true);
    	// Showing only some values of the original list
    	def cfA = cfOptions.findAll 
    	{ it.value in ["----","New","Enhancement"] }.collectEntries 
    	{ [ (it.optionId.toString()) : it.value ] } as Map
		newOrEnhancement.setFieldOptions(cfA);
		log.info("Custom Option List: " + cfA);
    	clientLevel.setRequired(true);
    	reportName.setHidden(true);
		reportName.setRequired(false);

		//Start (JIRA-5208)
		purpose.setHidden(true);
		purpose.setRequired(false);
		clientType.setHidden(true);
		clientType.setRequired(false);
		services.setHidden(true);
		services.setRequired(false);
		products.setHidden(true);
		clientName.setHidden(true);
		clientName.setRequired(false);
		groupName.setHidden(true);
		recipientCompany.setHidden(true);
		recipientCompany.setRequired(false);
		recipientType.setHidden(true);
		recipientType.setRequired(false);
		dataDeliveryApprovalReason.setHidden(true);
		privacyApproval.setHidden(true);
		privacyApprovalComments.setHidden(true); 
		informationSecurityApproval.setHidden(true); 
		informationSecurityApprovalComments.setHidden(true); 
		clinicalAnalyticsApproval.setHidden(true);
		clinicalAnalyticsApprovalComments.setHidden(true);
		pricing.setHidden(true);
		levelOfEffort.setHidden(true);
		dataScienceApproval.setHidden(true);
		dataScienceApprovalComments.setHidden(true);
		clientLaunchDate.setHidden(true);
		// End (JIRA-5208)
		
		// Start ER 2/2024 JIRA-8908
		noOfEligibleLivesForTheClient.setHidden(false);
		noOfEligibleLivesForTheClient.setRequired(true);
		isThisReusable.setHidden(false);
    	isThisReusable.setRequired(true);
		strategicValueOfRequest.setHidden(false);
		strategicValueOfRequest.setRequired(true);
		isThereAWorkaroundToGetYourDataOrSolution.setHidden(false);
		isThereAWorkaroundToGetYourDataOrSolution.setRequired(true);
		audience.setHidden(false);
		audience.setRequired(true);
    	audienceDetails.setHidden(false);
		audienceDetails.setRequired(true);
		contractualObligation.setHidden(false);
		contractualObligation.setRequired(true);
		escalation.setHidden(false);
		escalation.setRequired(true);
		priorityScore.setHidden(false);
		// End ER 2/2024 JIRA-8908

    	break;
    case "Ad-hoc Analysis":
    	// Showing optional/required fields
    	newOrEnhancement.setHidden(true);
    	newOrEnhancement.setRequired(false);
    	clientLevel.setRequired(true);
    	isThisReusable.setHidden(false);// ER 2/2024 JIRA-8908
    	isThisReusable.setRequired(true);// ER 2/2024 JIRA-8908
    	reportingTeamOwner.setHidden(false);
    	approver.setHidden(false);
    	audience.setRequired(true);
    	audienceDetails.setRequired(true);
    	contractualObligation.setRequired(true);
    	sOWorBRDRequired.setHidden(false);
    	sOWorBRDRequired.setRequired(true);
    	sensitiveInformation.setHidden(false);
    	sensitiveInformation.setRequired(true);
    	dataSharingAgreement.setHidden(false);
    	// dataSharingAgreement.setRequired(true);
    	fileTypePGPEncryption.setHidden(true);
    	fileTypeExtension.setHidden(true);
    	fileTypeDelimiter.setHidden(true);
    	extensionDetails.setHidden(true);
    	testFileRequired.setHidden(true);
    	testFileRequired.setRequired(false);
    	frequency.setHidden(true);
    	frequency.setRequired(false);
    	modeOfDelivery.setHidden(true);
    	modeOfDelivery.setRequired(false);
    	modeOfDeliveryVendor.setHidden(false);
    	modeOfDeliveryVendor.setRequired(false);
    	modeOfDeliveryEmail.setHidden(false);
    	modeOfDeliveryEmail.setRequired(false);
    	reportName.setHidden(true);
		reportName.setRequired(false);

		//Start (JIRA-5208)
		purpose.setHidden(true);
		purpose.setRequired(false);
		clientType.setHidden(true);
		clientType.setRequired(false);
		services.setHidden(true);
		services.setRequired(false);
		products.setHidden(true);
		clientName.setHidden(true);
		clientName.setRequired(false);
		groupName.setHidden(true);
		recipientCompany.setHidden(true);
		recipientCompany.setRequired(false);
		recipientType.setHidden(true);
		recipientType.setRequired(false);
		dataDeliveryApprovalReason.setHidden(true);
		privacyApproval.setHidden(true);
		privacyApprovalComments.setHidden(true); 
		informationSecurityApproval.setHidden(true); 
		informationSecurityApprovalComments.setHidden(true); 
		clinicalAnalyticsApproval.setHidden(true);
		clinicalAnalyticsApprovalComments.setHidden(true);
		pricing.setHidden(true);
		levelOfEffort.setHidden(true);
		dataScienceApproval.setHidden(true);
		dataScienceApprovalComments.setHidden(true);
		clientLaunchDate.setHidden(true);
		// End (JIRA-5208)

		// Start ER 2/2024 JIRA-8908
		noOfEligibleLivesForTheClient.setHidden(false);
		noOfEligibleLivesForTheClient.setRequired(true);
		isThisReusable.setHidden(false);
    	isThisReusable.setRequired(true);
		strategicValueOfRequest.setHidden(false);
		strategicValueOfRequest.setRequired(true);
		isThereAWorkaroundToGetYourDataOrSolution.setHidden(false);
		isThereAWorkaroundToGetYourDataOrSolution.setRequired(true);
		audience.setHidden(false);
		audience.setRequired(true);
    	audienceDetails.setHidden(false);
		audienceDetails.setRequired(true);
		contractualObligation.setHidden(false);
		contractualObligation.setRequired(true);
		escalation.setHidden(false);
		escalation.setRequired(true);
		priorityScore.setHidden(false);
		// End ER 2/2024 JIRA-8908
    	break;
    case "Tableau Dashboard Feedback":
    	// Showing optional/required fields
    	newOrEnhancement.setHidden(true);
    	newOrEnhancement.setRequired(false);
    	clientLevel.setRequired(false);
    	isThisReusable.setHidden(false);
    	isThisReusable.setRequired(true);
    	reportingTeamOwner.setHidden(true);
    	approver.setHidden(true);
    	audience.setRequired(false);
    	audienceDetails.setRequired(false);
    	contractualObligation.setRequired(false);
    	sOWorBRDRequired.setHidden(true);
    	sOWorBRDRequired.setRequired(false);
    	sensitiveInformation.setHidden(true);
    	sensitiveInformation.setRequired(false);
    	dataSharingAgreement.setHidden(true);
    	// dataSharingAgreement.setRequired(false);
    	fileTypePGPEncryption.setHidden(true);
    	fileTypeExtension.setHidden(true);
    	fileTypeDelimiter.setHidden(true);
    	extensionDetails.setHidden(true);
    	testFileRequired.setHidden(true);
    	testFileRequired.setRequired(false);
    	frequency.setHidden(true);
    	frequency.setRequired(false);
    	modeOfDelivery.setHidden(true);
    	modeOfDelivery.setRequired(false);
    	modeOfDeliveryVendor.setHidden(true);
    	modeOfDeliveryVendor.setRequired(false);
    	modeOfDeliveryEmail.setHidden(true);
    	modeOfDeliveryEmail.setRequired(false);
    	reportName.setHidden(true);
		reportName.setRequired(false);

		//Start (JIRA-5208)
		purpose.setHidden(true);
		purpose.setRequired(false);
		clientType.setHidden(true);
		clientType.setRequired(false);
		services.setHidden(true);
		services.setRequired(false);
		products.setHidden(true);
		clientName.setHidden(true);
		clientName.setRequired(false);
		groupName.setHidden(true);
		recipientCompany.setHidden(true);
		recipientCompany.setRequired(false);
		recipientType.setHidden(true);
		recipientType.setRequired(false);
		dataDeliveryApprovalReason.setHidden(true);
		privacyApproval.setHidden(true);
		privacyApprovalComments.setHidden(true); 
		informationSecurityApproval.setHidden(true); 
		informationSecurityApprovalComments.setHidden(true); 
		clinicalAnalyticsApproval.setHidden(true);
		clinicalAnalyticsApprovalComments.setHidden(true);
		pricing.setHidden(true);
		levelOfEffort.setHidden(true);
		dataScienceApproval.setHidden(true);
		dataScienceApprovalComments.setHidden(true);
		clientLaunchDate.setHidden(true);
		// End (JIRA-5208)

		// Start ER 2/2024 JIRA-8908
		noOfEligibleLivesForTheClient.setHidden(false);
		noOfEligibleLivesForTheClient.setRequired(true);
		isThisReusable.setHidden(false);
    	isThisReusable.setRequired(true);
		strategicValueOfRequest.setHidden(false);
		strategicValueOfRequest.setRequired(true);
		isThereAWorkaroundToGetYourDataOrSolution.setHidden(false);
		isThereAWorkaroundToGetYourDataOrSolution.setRequired(true);
		audience.setHidden(false);
		audience.setRequired(true);
    	audienceDetails.setHidden(false);
		audienceDetails.setRequired(true);
		contractualObligation.setHidden(false);
		contractualObligation.setRequired(true);
		escalation.setHidden(false);
		escalation.setRequired(true);
		priorityScore.setHidden(false);
		// End ER 2/2024 JIRA-8908
    	break;
    default:
        break;
}
log.info("Behaviours CRUS Project – Request Type completed.");