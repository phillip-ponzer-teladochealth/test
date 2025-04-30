/*
Creator: Jeff Tompkins
Purpose: Hide Design Fields if "Design/Content Status" field is empty
Projects: HEAL, COP
Issue Types: Bug, Story, Task
Run the script: ON LOAD and ON CHANGE
And on the: CREATE VIEW and ISSUE VIEW
Changelog: 2025-04-29 Phillip Ponzer - Converted script to work on Jira Cloud
*/

/* 
TODO: Update with the correct customfield IDs
*/
const changedField = getChangeField()
var designContentStatus = null
if( changedField && changedField.getName() === "Design/Content Status" ) {
    designContentStatus = changedField.getValue()
} else {
    designContentStatus = getFieldById("customfield_10082").getValue() // "Design/Content Status"
}

const designer = getFieldById("customfield_10104") // "Designer"
const designContentNotes = getFieldById("customfield_10083") // "Design/Content Notes"
if( designContentStatus ) {
    designer.setVisible(true)
    designContentNotes.setVisible(true)
} else {
    designer.setVisible(false)
    designContentNotes.setVisible(false)
}
