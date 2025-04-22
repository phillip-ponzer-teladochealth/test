/*
Author: Jeff Melies
Modified from the example provided by Atlassian
Purpose: For EPICS populate a custom field called 'Story Points Cumulative' with 
the sum of Story Points of linked issues.

*/
log.info("Listener is running on ${issue.key}")
// Get the ID of the fields for Story Points and Epic Link
def storyPointsField = get("/rest/api/2/field").asObject(List).body.find {(it as Map).name == 'Story Points'}.id
def cumulativeField = get("/rest/api/2/field").asObject(List).body.find {(it as Map).name == 'Story Points Cumulative'}.id

// Retrieve all the issues in this issues' Epic
def epicKey = issue.fields.parent.key
def issuesInEpic = get("/rest/agile/1.0/epic/${epicKey}/issue")
        .asObject(Map)
        .body
        .issues as List<Map>
logger.info("Total issues in Epic for ${epicKey}: ${issuesInEpic.size()}")

// Sum the estimates
def estimate = issuesInEpic.collect { Map issueInEpic ->
    issueInEpic.fields."${storyPointsField}" ?: 0 
}.sum()
logger.info("Summed estimate: ${estimate} to be populated on: ${cumulativeField}")

// Now update the parent Epic
def result = put("/rest/api/2/issue/${epicKey}")
    .queryString("overrideScreenSecurity", Boolean.TRUE)
    .header('Content-Type', 'application/json')
    .body([
        fields: [
                "${cumulativeField}": estimate
        ]
    ])
    .asString()

// check that updating the parent issue worked
assert result.status >= 200 && result.status < 300