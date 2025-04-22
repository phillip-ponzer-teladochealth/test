// Author: Ignacio Vera - JIRA-9090
// Set options for the Environment field (shown as Cluster in the portal)  

def platformCf = getFieldById(getFieldChanged()) // field this behaviour script is defined on
def errorMessage = 'You must also populate this field!'

def environmentCf = getFieldById('customfield_12720')
def platformCfValue = getFieldById(getFieldChanged()).value

def availableOptions = []

// set errors to ensure dependant fields are populated
if (platformCfValue == "AWS") {
    availableOptions.addAll(["devops", "integration", "omnibus", "sales"])
} 

if (platformCfValue == "Azure") {
    availableOptions.addAll(["cat", "dev", "integration", "pm-masked", "pm-unmasked", "preprod", "qa", "sat"])
} 

environmentCf.setFieldOptions(availableOptions)
