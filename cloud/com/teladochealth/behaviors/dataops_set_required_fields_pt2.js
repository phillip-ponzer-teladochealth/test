/*
Author: Eduardo Rojo
Purpose: Set Required/Optional fields based on "Request Type" 
Project: DATAOPS
Issue Types: Epic, Story, Task
Run the script: ON LOAD
And on the: CREATE VIEW and ISSUE VIEW
Change Log: 2025-04-30 Phillip Ponzer - Converted script to work on Jira Cloud
*/
const changedField = getChangeField()
var requestTypeField = null
if( changedField && changedField.getName() === "Request Type (migrated)" ) {
    requestTypeField = changedField
} else {
    requestTypeField = getFieldById("customfield_10122") // "Request Type (migrated)"
}

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

const requestType = requestTypeField.getValue()
switch(requestType.value){
    case "Configure or Investigate SFTP":
        // Showing/Requiring Report Type
        requestTypeField.setDescription("This type is for updating existing sftp setup to configure folders and aliases, whitelist IP addresses, or for troubleshooting file syncing issues.")
        dueDate.setRequired(true)
        clientName.setRequired(true)
        linkToSfRecord.setRequired(true)
        clientCode.setRequired(true)
        partnerName.setRequired(false)
        link.setRequired(false)
        namingConvention.setRequired(false)
        frequency.setRequired(false)
        multitenant.setRequired(false)
        fileType.setRequired(false)
        useCase.setRequired(false)
        mappingDetails.setRequired(false)
        sftpDetails.setRequired(true)
        break
    case "Review and/or Automate New Data":
        // Showing optional/required fields
        requestType.setDescription("This type is for reviewing and configuring any type of new data eligibility, claims, tags, emails etc, for (automated) processing into our system.")
        dueDate.setRequired(true)
        clientName.setRequired(true)
        linkToSfRecord.setRequired(true)
        clientCode.setRequired(true)
        partnerName.setRequired(true)
        link.setRequired(true)
        namingConvention.setRequired(true)
        frequency.setRequired(true)
        multitenant.setRequired(true)
        fileType.setRequired(true)
        useCase.setRequired(true)
        mappingDetails.setRequired(true)
        sftpDetails.setRequired(true)
        break
    case "Review and/or Analyze Existing Data":
        // Showing/Requiring Report Type
        requestType.setDescription("This type is for any questions, concerns, issues, investigations etc regarding the existing data in our files, systems or dashboards.")
        dueDate.setRequired(true)
        clientName.setRequired(true)
        linkToSfRecord.setRequired(true)
        clientCode.setRequired(true)
        partnerName.setRequired(false)
        link.setRequired(false)
        namingConvention.setRequired(false)
        frequency.setRequired(false)
        multitenant.setRequired(false)
        fileType.setRequired(false)
        useCase.setRequired(false)
        mappingDetails.setRequired(false)
        sftpDetails.setRequired(false)
        break
    case "Update Members":
        // Showing optional/required fields
        requestType.setDescription("This type is for updating missing/incorrect values of existing tag/s or insurance information for members based on the recruitaible data.")
        dueDate.setRequired(true)
        clientName.setRequired(true)
        linkToSfRecord.setRequired(true)
        clientCode.setRequired(true)
        partnerName.setRequired(false)
        link.setRequired(false)
        namingConvention.setRequired(false)
        frequency.setRequired(false)
        multitenant.setRequired(false)
        fileType.setRequired(false)
        useCase.setRequired(false)
        mappingDetails.setRequired(true)
        sftpDetails.setRequired(false)
        break
    case "Stop Recruitable Data Feeds": //hasta acá llegué
        // Showing optional/required fields
        requestType.setDescription("This type is for unconfiguring data feeds from being processed into our system and/or also deactivating corresponding sftp connections.")
        dueDate.setRequired(true)
        clientName.setRequired(true)
        linkToSfRecord.setRequired(true)
        clientCode.setRequired(true)
        partnerName.setRequired(false)
        link.setRequired(false)
        namingConvention.setRequired(false)
        frequency.setRequired(false)
        multitenant.setRequired(false)
        fileType.setRequired(false)
        useCase.setRequired(false)
        mappingDetails.setRequired(false)
        sftpDetails.setRequired(false)
        break
    case "Other":
        requestType.setDescription("This type is for any other request that doesn't fall into any of the defined standard types.")
        dueDate.setRequired(true)
        clientName.setRequired(true)
        linkToSfRecord.setRequired(true)
        clientCode.setRequired(true)
        partnerName.setRequired(false)
        link.setRequired(false)
        namingConvention.setRequired(false)
        frequency.setRequired(false)
        multitenant.setRequired(false)
        fileType.setRequired(false)
        useCase.setRequired(false)
        mappingDetails.setRequired(false)
        sftpDetails.setRequired(false)
        break
    case "Other - Internal":
        requestType.setDescription("This type is exclusively for internal DOPS projects and tasks and should only be created by a DOPS team member.")
        dueDate.setRequired(false)
        clientName.setRequired(false)
        linkToSfRecord.setRequired(false)
        clientCode.setRequired(false)
        partnerName.setRequired(false)
        link.setRequired(false)
        namingConvention.setRequired(false)
        frequency.setRequired(false)
        multitenant.setRequired(false)
        fileType.setRequired(false)
        useCase.setRequired(false)
        mappingDetails.setRequired(false)
        sftpDetails.setRequired(false)
        break
    default:
        requestType.setDescription("")
        partnerName.setRequired(false)
        link.setRequired(false)
        namingConvention.setRequired(false)
        frequency.setRequired(false)
        multitenant.setRequired(false)
        fileType.setRequired(false)
        useCase.setRequired(false)
        mappingDetails.setRequired(false)
        sftpDetails.setRequired(false)
        break
}
