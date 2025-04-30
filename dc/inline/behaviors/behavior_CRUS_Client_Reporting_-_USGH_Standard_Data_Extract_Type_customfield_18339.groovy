/**import com.atlassian.jira.issue.customfields.option.Options

 * Eduardo Rojo
 * CRUS project - Client Reporting - USGH
 * Issue Type: Story
 * Hide fields based on "Standard Data Extract Type"
 * Log: 5/3/2023 Eduardo Rojo (JIRA-5208)
 *      6/5/2024 Eduardo Rojo (JIRA-9551)
 */

import com.atlassian.jira.component.ComponentAccessor;
import com.atlassian.jira.issue.customfields.option.Option;
import com.atlassian.jira.issue.customfields.option.Options;
log.info("Behaviours CRUS Project – Standard Data Extract Type started.");

def customFieldManager = ComponentAccessor.getCustomFieldManager()
def optionsManager = ComponentAccessor.getOptionsManager();

// This listens if the "Standard Data Extract Type" field has changed
def standardDataExtractType = getFieldById(getFieldChanged());

// This gets fields by name
def membershipReport = getFieldByName("Membership Report");
def bloodGlucoseReport = getFieldByName("Blood Glucose Report");
def bloodPressureReport = getFieldByName("Blood Pressure Report");
def weightManagementReport = getFieldByName("Weight Management Report");
def alertsReport = getFieldByName("Alerts Report");
def coachingReport = getFieldByName("Coaching Report");
def grievanceReport = getFieldByName("Grievance Report");
def incentiveReport = getFieldByName("Incentive Report");
def meterMessageReport = getFieldByName("Meter Message Report");
def engagementReport = getFieldByName("Engagement Report");
def ineligibleMembersReport = getFieldByName("Ineligible Members Report"); 
def mentalHealthEventsReport = getFieldByName("Mental Health Events Report"); //(JIRA-5208) Custom field name updated from Behavioral Health Events Report to Mental Health Events Report
def marketingOptOutReport = getFieldByName("Marketing Opt Out Report");
def clientName = getFieldByName("Client Name");
def clientLaunchDate = getFieldByName("Client Launch Date");
def contractLanguageForGrievanceReporting = getFieldByName("Contract Language for Grievance Reporting");
def eMSCaseDetailExtractReport = getFieldByName("EMS Case Detail Extract Report"); // added (JIRA-5208)
def estimatedA1cReport = getFieldByName("Estimated A1c Report"); // added (JIRA-5208)
def clinicalOutcomesMemberReport = getFieldByName("Clinical Outcomes Member Report"); // ER added 9/27/2023 - JIRA-8466
def detailedInvoicesReport = getFieldByName("Detailed Invoices Report");//added JIRA-9551
def registrationReport = getFieldByName("Registration Report");//added JIRA-9551

// This gets the value of "Standard Report Type (SL)"
def standardReportTypeOption = standardDataExtractType.getValue() as String

log.info("Standard Data Extract Type: " + standardReportTypeOption);
// Save true/false if standardReportTypeOption == (value selected)
def mentalHealthEventsReportNew = standardReportTypeOption == "Mental Health Events" //updated (JIRA-5208)
def bloodGlucoseReportNew = standardReportTypeOption == "Blood Glucose"
def alertsReportNew = standardReportTypeOption == "Blood Glucose Alerts"
def bloodPressureReportNew = standardReportTypeOption == "Blood Pressure"
def coachingReportNew = standardReportTypeOption == "Coaching"
def engagementReportNew = standardReportTypeOption == "Engagement"
def grievanceReportNew = standardReportTypeOption == "Grievances"
def incentiveReportNew = standardReportTypeOption == "Incentive Report"
def ineligibleMembersReportNew = standardReportTypeOption == "Ineligible Members"
def marketingOptOutReportNew = standardReportTypeOption == "Marketing Opt Out"
def membershipReportNew = standardReportTypeOption == "Membership"
def meterMessageReportNew = standardReportTypeOption == "Meter Message"
def weightManagementReportNew = standardReportTypeOption == "Weight Management"
def eMSCaseDetailExtractReportNew = standardReportTypeOption == "EMS Case Detail Extract"
def estimatedA1cReportNew = standardReportTypeOption == "Estimated A1c (Health Plans Only)"
def clinicalOutcomesMemberReportNew = standardReportTypeOption == "Quarterly Clinical Outcomes Member File"; // ER added 9/27/2023 - JIRA-8466
def detailedBillingReportNew = standardReportTypeOption == "Detailed Invoices" //added JIRA-9551
def registrationReportNew = standardReportTypeOption == "Registration" //added JIRA-9551

// Preperation code for hiding "Standard Report Type" field
def standardDataExtractTypeCF = customFieldManager.getCustomFieldObject(standardDataExtractType.getFieldId())
def config = standardDataExtractTypeCF.getRelevantConfig(getIssueContext())
def options = optionsManager.getOptions(config) as Options

// Show/Hide fields
membershipReport.setHidden(!membershipReportNew);
bloodGlucoseReport.setHidden(!bloodGlucoseReportNew);
bloodPressureReport.setHidden(!bloodPressureReportNew);
weightManagementReport.setHidden(!weightManagementReportNew);
alertsReport.setHidden(!alertsReportNew);
coachingReport.setHidden(!coachingReportNew);
grievanceReport.setHidden(!grievanceReportNew);
incentiveReport.setHidden(!incentiveReportNew);
meterMessageReport.setHidden(!meterMessageReportNew);
engagementReport.setHidden(!engagementReportNew);
ineligibleMembersReport.setHidden(!ineligibleMembersReportNew);
mentalHealthEventsReport.setHidden(!mentalHealthEventsReportNew);//updated (JIRA-5208) 
marketingOptOutReport.setHidden(!marketingOptOutReportNew);
//clientName.setHidden(!grievanceReportNew); //updated (JIRA-5208)
//clientLaunchDate.setHidden(!grievanceReportNew); //updated (JIRA-5208)
contractLanguageForGrievanceReporting.setHidden(!grievanceReportNew);
eMSCaseDetailExtractReport.setHidden(!eMSCaseDetailExtractReportNew); // added (JIRA-5208)
estimatedA1cReport.setHidden(!estimatedA1cReportNew); // added (JIRA-5208)
clinicalOutcomesMemberReport.setHidden(!clinicalOutcomesMemberReportNew); // ER added 9/27/2023 - JIRA-8466
detailedInvoicesReport.setHidden(!detailedBillingReportNew); //added JIRA-9551
registrationReport.setHidden(!registrationReportNew); //added JIRA-9551
log.info("Behaviours CRUS Project – Standard Data Extract Type completed.") 