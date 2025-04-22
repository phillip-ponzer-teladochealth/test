package com.teladochealth.behaviours

 /*
 * Eduardo Rojo
 * CRUS project - Client Reporting - USGH
 * Issue Type: Story
 * Calculates the Priority Score based on several custom fields.
 * Log: ER 2/2024 JIRA-8908
 */
import com.atlassian.jira.component.ComponentAccessor;
import com.atlassian.jira.issue.customfields.option.Option;
import com.atlassian.jira.issue.customfields.option.Options
import org.apache.log4j.Logger
import org.apache.log4j.Level

log.setLevel(Level.INFO)
log = Logger.getLogger("calculatePriorityScore.groovy")
log.debug("Calculates Priority Score started.");

def customFieldManager = ComponentAccessor.getCustomFieldManager();
def optionsManager = ComponentAccessor.getOptionsManager();


def requestType = getFieldByName("Request Type");
def requestTypeOption = requestType.getValue() as String

if(requestTypeOption != "Data Extract | Member/Visit/Event level"){
    // Getting the custom field ID by Name
    def noOfEligibleLivesForTheClient = getFieldByName("No. of Eligible Lives for the Client"); 
    //getFieldById(getFieldChanged());
    // Start - ER 2/2024 JIRA-8908
    def strategicValueOfRequest = getFieldByName("Strategic Value of Request");
    def isThisReusable = getFieldByName("Is this Reusable?");
    def isThereAWorkaroundToGetYourDataOrSolution = getFieldByName("Is there a workaround to get your data or solution?");
    def audience = getFieldByName("Audience (SL)");
    def contractualObligation = getFieldByName("Contractual Obligation");
    def escalation = getFieldByName("Escalation");
    def priorityScore = getFieldByName("Priority Score");
    // This gets the value of custom fields as String
    def noOfEligibleLivesForTheClientOption = noOfEligibleLivesForTheClient.getValue() as String
    def strategicValueOfRequestOption = strategicValueOfRequest.getValue() as String
    def isThisReusableOption = isThisReusable.getValue() as String
    def isThereAWorkaroundToGetYourDataOrSolutionOption = isThereAWorkaroundToGetYourDataOrSolution.getValue() as String
    def audienceOption = audience.getValue() as String
    def contractualObligationOption = contractualObligation.getValue() as String
    def escalationOption = escalation.getValue() as String
    // Default variables for each custom field
    def noOfEligibleLivesForTheClientSum = 0;
    def strategicValueOfRequestSum = 0;
    def isThisReusableSum = 0;
    def isThereAWorkaroundToGetYourDataOrSolutionSum = 0;
    def audienceSum = 0;
    def contractualObligationSum = 0;
    def escalationSum = 0;
    def total = 0;
    // Setting a specific value for each custom field
    switch(noOfEligibleLivesForTheClientOption){
    case "Less than 1000":
        noOfEligibleLivesForTheClientSum = 0;
    break;
    case "1K – 25K":
        noOfEligibleLivesForTheClientSum = 1;
    break;
    case "25K – 500K":
        noOfEligibleLivesForTheClientSum = 2;
    break;
    case "500K – 1M":
        noOfEligibleLivesForTheClientSum = 3;
    break;
    case "1M – 5M":
        noOfEligibleLivesForTheClientSum = 4;
    break;
    case "Greater than 5M":
        noOfEligibleLivesForTheClientSum = 5;
    default:
    break;
    }
    switch(strategicValueOfRequestOption){
    case "High":
        strategicValueOfRequestSum = 10;
    break;
    case "Medium":
        strategicValueOfRequestSum = 7;
    break;
    case "Low":
        strategicValueOfRequestSum = 3;
    break;
    default:
    break;
    }
    switch(isThisReusableOption){
    case "CM's across all Business Segments are likely to have a similar need":
    isThisReusableSum = 5;
    break;
    case "CM's in only my Business Segments are likely to have a similar need":
    isThisReusableSum = 3;
    break;
    case "Recurring but only applicable to my client":
    isThisReusableSum = 2;
    break;
    case "One Time and applicable to my client only":
    isThisReusableSum = 1;
    break;
    }
    switch(isThereAWorkaroundToGetYourDataOrSolutionOption){
    case "No Workaround":
    isThereAWorkaroundToGetYourDataOrSolutionSum = 5;
    break;
    case "Complex Workaround":
    isThereAWorkaroundToGetYourDataOrSolutionSum = 3;
    break;
    case "Simple Workaround":
    isThereAWorkaroundToGetYourDataOrSolutionSum = 1;
    break;
    }
    switch(audienceOption){
    case "Internal":
        audienceSum = 1;
    break;
    case "External":
        audienceSum = 4;
    break;
    case "Both":
        audienceSum = 4;
    break;
    }
    switch(contractualObligationOption){
    case "Yes":
    contractualObligationSum = 2;
    break;
    case "No":
    contractualObligationSum = 0;
    break;
    }
    switch(escalationOption){
    case "No Escalation":
    escalationSum = 0;
    break;
    case "Director Level Prioritization":
    escalationSum = 15;
    break;
    case "VP Level Prioritization":
    escalationSum = 25;
    break;
    }
    // Adding up values on "total" variable
    total = noOfEligibleLivesForTheClientSum + strategicValueOfRequestSum + isThisReusableSum + isThereAWorkaroundToGetYourDataOrSolutionSum + audienceSum + contractualObligationSum + escalationSum;
    // Setting "total" variable on "Priority Score" custom field (setFormValue)
    priorityScore.setFormValue(total).toString();
}


log.debug("Calculates Priority Score completed.");