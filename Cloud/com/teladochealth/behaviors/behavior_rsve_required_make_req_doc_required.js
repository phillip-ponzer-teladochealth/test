/*
Creator: Jeff Melies
Purpose: If Required RS/VE Update is Yes, make Requirements Documentation a required field.
Run the script: ON LOAD and ON CHANGE
And on the: CREATE VIEW and ISSUE VIEW
See also: JIRA-721
Changelog: 2025-04-29 Phillip Ponzer - Converted script to work on Jira Cloud
*/

/* 
TODO: Update lines 18 and 21 with the correct customfield IDs
*/
const changedField = getChangeField()
var reqRSVEUpdate = null
if( changedField && changedField.getName() === "Requires RS/VE Update" ) {
    reqRSVEUpdate = changedField.getValue()
} else {
    reqRSVEUpdate = getFieldById("customfield_10532").getValue()
}

const reqDocField = getFieldById("customfield_10533")
if( reqRSVEUpdate && reqRSVEUpdate.value === "Yes" ) {
    reqDocField.setVisible(true)
    reqDocField.setRequired(true)
} else {
    reqDocField.setVisible(false)
    reqDocField.setRequired(false)
}
