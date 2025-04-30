/*
Creator: Jeff Melies
Purpose: Shows other Design fields dependent on whether "Design/Content Status" has been 
         populated
Run the script: ON LOAD and ON CHANGE
And on the: CREATE VIEW and ISSUE VIEW
Change log: 2025-04-29 Phillip Ponzer - Converted to Jira Cloud
*/

/*
TODO: Convert custom field IDs
*/
const changedField = getChangeField()
var designContentStatus = null
if( changedField && changedField.getName() === "Design/Content Status" ) {
    designContentStatus = changedField.getValue()
} else {
    designContentStatus = getFieldById("customfield_10082").getValue() // "Design/Content Status"
}

const designContentNotes = getFieldById("customfield_10083") // "Design/Content Notes"
const designer = getFieldById("customfield_10104") // "Designer"
const designContentApprovals = getFieldById("customfield_10119") // "Design/Content Approvals"
const contentStrategist = getFieldById("customfield_10118") // "Content Strategist"
if( designContentStatus && designContentStatus.value !== "No Design Needed" ) {
    designContentNotes.setVisible(true)
    designer.setVisible(true)
    designContentApprovals.setVisible(true)
    contentStrategist.setVisible(true)
} else {
    designContentNotes.setVisible(false)
    designer.setVisible(false)
    designContentApprovals.setVisible(false)
    contentStrategist.setVisible(false)
}
