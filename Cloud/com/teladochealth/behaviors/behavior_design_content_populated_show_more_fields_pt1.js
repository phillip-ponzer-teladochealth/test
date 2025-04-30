/*
Creator: Jeff Melies
Purpose: Sets description of "Design/Content Notes" and "Designer" fields
Run the script: ON LOAD
And on the: CREATE VIEW and ISSUE VIEW
Change log: 2025-04-29 Phillip Ponzer - Converted to Jira Cloud
*/

/*
TODO: Update custom field IDs
*/
getFieldById("customfield_10083").setDescription("UI Design notes and links to Figma files.")
getFieldById("customfield_10104").setDescription("Designer responsible.")
