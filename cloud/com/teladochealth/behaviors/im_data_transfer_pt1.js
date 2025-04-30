/*
Author: Jeff Melies
Purpose: Makes "Request Type" required and encourages users to use Components field 
         if Issue Type is Task
Run the script: ON LOAD
And on the: CREATE VIEW and ISSUE VIEW
See also: JIRA-5948, JIRA-8410
Change Log: 2025-04-30 Phillip Ponzer - Converted script to work on Jira Cloud
*/

const issueType = getFieldById("issuetype").getValue().name
if( issueType === "Task" ) {
    const reqType = getFieldById("customfield_10122") // "Request Type (migrated)"
    reqType.setRequired(true)
    reqType.setDescription("Please use the Component/s field instead of Epic Link!!!")
}

// Epic Link is not supported by Behaviors for Cloud!
// getFieldById("customfield_10014").setDescription("Please use the Component/s field instead of Epic Link!!!") // "Epic Link"
