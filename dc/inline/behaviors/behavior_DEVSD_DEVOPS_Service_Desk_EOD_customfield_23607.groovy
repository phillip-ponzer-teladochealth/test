// Author: Ignacio Vera - JIRA-9090
// Show and make fields required based on EOD field. 

def currentField = getFieldById(getFieldChanged()) // field this behaviour script is defined on
def errorMessage = 'You must also populate this field!'

def platformCf = getFieldByName('Platform')
def environmentCf = getFieldById('customfield_12720')
def namespace = getFieldByName('Name')
def relatedTickets = getFieldByName('Link')

// set errors to ensure dependant fields are populated
if (currentField.value == "Yes") {
    platformCf.setHidden(false)
    platformCf.setRequired(true)

    environmentCf.setHidden(false)
    environmentCf.setRequired(true)

    namespace.setHidden(false)
    namespace.setRequired(true) 

    relatedTickets.setHidden(false)
} else {
    platformCf.setHidden(true)
    platformCf.setRequired(false)
    platformCf.setFormValue([""])

    environmentCf.setHidden(true)
    environmentCf.setRequired(false)
    environmentCf.setFormValue([""])

    namespace.setHidden(true)
    namespace.setRequired(false) 
    namespace.setFormValue("")


    relatedTickets.setHidden(true)
    relatedTickets.setFormValue("")

}