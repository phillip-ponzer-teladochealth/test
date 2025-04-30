/*
Author: Eduardo Rojo
Purpose: Set Required/Optional fields based on "Request Type" 
Project: DATAOPS
Issue Types: Epic, Story, Task
Run the script: ON LOAD
And on the: CREATE VIEW and ISSUE VIEW
Change Log: 2025-04-30 Phillip Ponzer - Converted script to work on Jira Cloud
*/
const summary = getFieldById("summary")
const projectName = getFieldById("customfield_10123") // "Project Name"
const requestType = getFieldById("customfield_10122") // "Request Type (migrated)"
const description = getFieldById("description")
const dueDate = getFieldById("duedate")
const clientName = getFieldById("customfield_10100") // "Client Name"
const linkToSfRecord = getFieldById("customfield_10236") // "Link to SF Record"
const clientCode = getFieldById("customfield_10235") // "Client Code"
const partnerName = getFieldById("customfield_10124") // "Partner Name"
const link = getFieldById("customfield_10244") // "Link"
const namingConvention = getFieldById("customfield_10240") // "Naming Convention"
const frequency = getFieldById("customfield_10215") // "Frequency"
const multitenant = getFieldById("customfield_10238") // "Multitenant"
const fileType = getFieldById("customfield_10239") // "File Type"
const useCase = getFieldById("customfield_10242") // "Use Case"
const mappingDetails = getFieldById("customfield_10069") // "Mapping Details"
const sftpDetails = getFieldById("customfield_10106") // "SFTP Details"
const priority = getFieldById("priority")
const priorityScore = getFieldById("customfield_10192") // "Priority Score"
// Attachments are not supported in Behaviors for Cloud!
//const attachment = getFieldById("attachment")

//Setting required fields
projectName.setRequired(true)
requestType.setRequired(true)
description.setRequired(true)

//Setting field descriptions
summary.setDescription("A brief overview to give an idea of what the request is about. Please include the client/partner name.")
projectName.setDescription("The name of the project this request is/will be part of.")
requestType.setDescription("The defined ticket type for your request.")
description.setDescription("The objective of this request. Feel free to also mention anything else you would like to share.") 
dueDate.setDescription("The requested date for this request to be completed by.")
clientName.setDescription("Client name/s corresponding to this request.")
linkToSfRecord.setDescription("Salesforce link of the related Client Overview/s.")
clientCode.setDescription("Client code/s corresponding to this request.")
partnerName.setDescription("The name of vendor or source of the file or layout.")
link.setDescription("Box link to the file or layout.")
namingConvention.setDescription("Preferred or agreed upon standard naming convention of the file.")
frequency.setDescription("The cadence/frequency of the file.")
multitenant.setDescription("Whether the file contains data for more than one client code.")
fileType.setDescription("The appplicable file type/s as it relates to the current request.")
useCase.setDescription("The primary purpose of the file as it relates to the current request.")
mappingDetails.setDescription("The mapping details of the client codes incase of multitenant files, i.e., the identifying column and the values to map the different client codes to, and/or the mapping details of the tag to be backfilled, i.e., the configured tag name, the column name and the values/mapping expected whichever is applicable based on the request type.")
sftpDetails.setDescription("The details of the connection/setup, i.e., if the connection is hosted by Teladoc or partner/vendor, if the connection is used for inbound, outbound or both ways data transfer, the web username, the IP addresses, the partner/vendor contact info whichever is applicable in the situation.")
//attachment.setHelpText("Do not attach any kind of PII/PHI, instead please share a box link.")

const context = await getContext()
const projectKey = context.extension.project.key
const projectRoleNames = await makeRequest(`/rest/api/3/project/${projectKey}/role`)
const teamMemberProjectRole = await makeRequest(projectRoleNames.body.get("Team Member"))
const teamMembers = teamMemberProjectRole.body.actors
var userIsTeamMember = false
for( let teamMember of teamMembers ) {
    if( teamMember.actorUser.accountId === context.accountId ) {
        userIsTeamMember = true
        break
    }
}

if( userIsTeamMember ) {
    priority.setVisible(true)
    priorityScore.setVisible(true)
}
else {
    priority.setVisible(false)
    priorityScore.setVisible(false)
}
