/*
Creator: Eduardo Rojo
Purpose: Set required/optional fields based on "Request Type" value
Project: IDT
Issue Types: ALL
Run the script: ON LOAD and ON CHANGE
And on the: CREATE VIEW and ISSUE VIEW
See also: JIRA-8410
Change Log: 2025-04-29 Phillip Ponzer - Converted script to work on Jira Cloud
            2023-09-23 Eduardo Rojo   - Initial commit (JIRA-8410)
*/

const changedField = getChangeField()
var reqType = null
if( changedField && changedField.getName() === "Request Type (migrated)" ) {
    reqType = changedField.getValue()
} else {
    reqType = getFieldById("customfield_10122").getValue() // "Request Type (migrated)"
}

const summary = getFieldById("summary")
const projectName = getFieldById("customfield_10123") // "Project Name"
const description = getFieldById("description")
const clientName = getFieldById("customfield_10100") // "Client Name"
const groupId = getFieldById("customfield_10087") // "Group ID"
const eligibilityVerificationMethod = getFieldById("customfield_10125") // "Eligibility Verification Method"
const partnerName = getFieldById("customfield_10124") // "Partner Name"
const mappingDetails = getFieldById("customfield_10069") // "Mapping Details"
const launchDate = getFieldById("customfield_10103") // "Launch Date"
const sftpDetails = getFieldById("customfield_10106") // "SFTP Details"
// Attachments are not supported in Behaviors for Cloud!
// const attachment = getFieldById("attachment")

summary.setDescription("A brief overview to give an idea of what the request is about.")
projectName.setDescription("The name of the project this request is/will be part of.")
description.setDescription("The objective of this request. Feel free to also mention anything else you would like to share.")
clientName.setDescription("Client Name(s) from Admin.")
groupId.setDescription("Legacy Group Id(s) / Admin Group (No org IDs).")
eligibilityVerificationMethod.setDescription("The eligibility type (primary).")
partnerName.setDescription("Please specify old vendor and new vendor name, if applicable.")
launchDate.setDescription("The launch date of the client.")
sftpDetails.setDescription("SFTP details should include contact and atleast one IP Address. If existing, provide username. Please also attach the Eligibility Questionnaire.")
mappingDetails.setDescription("Please attach Mapping document.")
//attachment.setDescription("Do not attach any kind of PII/PHI, instead please share a box link.")

switch(reqType){
    case "Configure or Investigate SFTP":
        projectName.setRequired(true)
        description.setRequired(true)
        clientName.setRequired(true)
        groupId.setRequired(true)
        eligibilityVerificationMethod.setRequired(true)
        partnerName.setRequired(true)
        launchDate.setRequired(true)
        sftpDetails.setRequired(true)
        mappingDetails.setRequired(false)
        //attachment.setRequired(true)
        break
    case "Review and/or Automate New Data":
        projectName.setRequired(true)
        description.setRequired(true)
        clientName.setRequired(true)
        groupId.setRequired(true)
        eligibilityVerificationMethod.setRequired(true)
        partnerName.setRequired(true)
        launchDate.setRequired(true)
        sftpDetails.setRequired(true)
        mappingDetails.setRequired(true)
        //attachment.setRequired(false)
        break
    case "Review and/or Analyze Existing Data":
        projectName.setRequired(true)
        description.setRequired(true)
        clientName.setRequired(true)
        groupId.setRequired(true)
        eligibilityVerificationMethod.setRequired(false)
        partnerName.setRequired(false)
        launchDate.setRequired(false)
        sftpDetails.setRequired(false)
        mappingDetails.setRequired(false)
        //attachment.setRequired(false)
        break
    case "Update Members":
        projectName.setRequired(true)
        description.setRequired(true)
        clientName.setRequired(true)
        groupId.setRequired(true)
        eligibilityVerificationMethod.setRequired(false)
        partnerName.setRequired(false)
        launchDate.setRequired(false)
        sftpDetails.setRequired(false)
        mappingDetails.setRequired(false)
        //attachment.setRequired(false)
        break
    case "Update Admin":
        projectName.setRequired(true)
        description.setRequired(true)
        clientName.setRequired(true)
        groupId.setRequired(true)
        eligibilityVerificationMethod.setRequired(false)
        partnerName.setRequired(false)
        launchDate.setRequired(true)
        sftpDetails.setRequired(false)
        mappingDetails.setRequired(false)
        //attachment.setRequired(true)
        break
    case "Other":
        projectName.setRequired(true)
        description.setRequired(true)
        clientName.setRequired(true)
        groupId.setRequired(true)
        eligibilityVerificationMethod.setRequired(false)
        partnerName.setRequired(false)
        launchDate.setRequired(false)
        sftpDetails.setRequired(false)
        mappingDetails.setRequired(false)
        //attachment.setRequired(false)
        break
    case "Other - Internal":
        projectName.setRequired(true)
        description.setRequired(true)
        clientName.setRequired(false)
        groupId.setRequired(false)
        eligibilityVerificationMethod.setRequired(false)
        partnerName.setRequired(false)
        launchDate.setRequired(false)
        sftpDetails.setRequired(false)
        mappingDetails.setRequired(false)
        //attachment.setRequired(false)
        break
    case "Ingestion Failure":
        projectName.setRequired(false)
        description.setRequired(false)
        clientName.setRequired(false)
        groupId.setRequired(false)
        eligibilityVerificationMethod.setRequired(false)
        partnerName.setRequired(false)
        launchDate.setRequired(false)
        sftpDetails.setRequired(false)
        mappingDetails.setRequired(false)
        //attachment.setRequired(false)
        break
    default:
        break
}
