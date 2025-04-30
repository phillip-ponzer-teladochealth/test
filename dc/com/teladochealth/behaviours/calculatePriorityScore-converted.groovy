 /*
 * Eduardo Rojo
 * CRUS project - Client Reporting - USGH
 * Issue Type: Story
 * Calculates the Priority Score based on several custom fields.
 * Log: ER 2/2024 JIRA-8908
 *      PP 4/2025 Converted Data Center/com/teladochealth/behaviours/calculatePriorityScore.groovy
 *                Discovered the project it was used in is no longer in-scope
 *                Saved it here for posterity (just needs testing at this point and it's ready)
 */

/********************************************************
Converted Data Center/com/teladochealth/behaviours/calculatePriorityScore.groovy
Discovered the project it was used in is no longer in-scope
Saved it here for posterity
********************************************************/

def fields = get("/rest/api/3/field").asObject(List).body
def fieldsByName = [:]
for( field in fields ) {
    fieldsByName[field["name"]] = field
}

def fieldRequestType = fieldsByName["Request Type"]["id"]
def issue = Issues.getByKey("SCRUM-1")
if( issue.fields[fieldRequestType] != "Data Extract | Member/Visit/Event level" ) {
    def fieldNoOfEligibleLivesForTheClient =                fieldsByName["No. of Eligible Lives for the Client"]["id"]
    def fieldStrategicValueOfRequest =                      fieldsByName["Strategic Value of Request"]["id"]
    def fieldIsThisReusable =                               fieldsByName["Is this Reusable?"]["id"]
    def fieldIsThereAWorkaroundToGetYourDataOrSolution =    fieldsByName["Is there a workaround to get your data or solution?"]["id"]
    def fieldAudience =                                     fieldsByName["Audience (SL)"]["id"]
    def fieldContractualObligation =                        fieldsByName["Contractual Obligation"]["id"]
    def fieldEscalation =                                   fieldsByName["Escalation"]["id"]

    def valNoOfEligibleLivesForTheClient =              issue.fields[fieldNoOfEligibleLivesForTheClient]
    def valStrategicValueOfRequest =                    issue.fields[fieldStrategicValueOfRequest]
    def valIsThisReusable =                             issue.fields[fieldIsThisReusable]
    def valIsThereAWorkaroundToGetYourDataOrSolution =  issue.fields[fieldIsThereAWorkaroundToGetYourDataOrSolution]
    def valAudience =                                   issue.fields[fieldAudience]
    def valContractualObligation =                      issue.fields[fieldContractualObligation]
    def valEscalation =                                 issue.fields[fieldEscalation]

    def optsNoOfEligibleLivesForTheClient = [
        "Less than 1000":   0,
        "1K – 25K":         1,
        "25K – 500K":       2,
        "500K – 1M":        3,
        "1M – 5M":          4,
        "Greater than 5M":  5
    ]
    def optsStrategicValueOfRequest = [
        "High":     10,
        "Medium":   7,
        "Low":      3
    ]
    def optsIsThisReusable = [
        "CM's across all Business Segments are likely to have a similar need":  5,
        "CM's in only my Business Segments are likely to have a similar need":  3,
        "Recurring but only applicable to my client":                           2,
        "One Time and applicable to my client only":                            1
    ]
    def optsIsThereAWorkaroundToGetYourDataOrSolution = [
        "No Workaround":        5,
        "Complex Workaround":   3,
        "Simple Workaround":    1
    ]
    def optsAudience = [
        "Internal": 1,
        "External": 4,
        "Both":     4
    ]
    def optsContractualObligation = [
        "Yes": 2,
        "No":  0
    ]
    def optsEscalation = [
        "No Escalation":                    0,
        "Director Level Prioritization":    15,
        "VP Level Prioritization":          25,
    ]

    def sumNoOfEligibleLivesForTheClient =              optsNoOfEligibleLivesForTheClient.get(valNoOfEligibleLivesForTheClient) ?: 0
    def sumStrategicValueOfRequest =                    optsStrategicValueOfRequest.get(valStrategicValueOfRequest) ?: 0
    def sumIsThisReusable =                             optsIsThisReusable.get(valIsThisReusable) ?: 0
    def sumIsThereAWorkaroundToGetYourDataOrSolution =  optsIsThereAWorkaroundToGetYourDataOrSolution.get(valIsThereAWorkaroundToGetYourDataOrSolution) ?: 0
    def sumAudience =                                   optsAudience.get(valAudience) ?: 0
    def sumContractualObligation =                      optsContractualObligation.get(valContractualObligation) ?: 0
    def sumEscalation =                                 optsEscalation.get(valEscalation) ?: 0

    def total = sumNoOfEligibleLivesForTheClient + sumStrategicValueOfRequest + sumIsThisReusable + sumIsThereAWorkaroundToGetYourDataOrSolution + sumAudience + sumContractualObligation + sumEscalation

    return total
}