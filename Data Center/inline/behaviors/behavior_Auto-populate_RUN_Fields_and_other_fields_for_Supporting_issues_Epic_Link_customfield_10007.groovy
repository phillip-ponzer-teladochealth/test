/**
 * Author: Jeff Tompkins
 * Purpose: Sets the RUN allocation fields based on Project or Project Category if the parent Epic is "Supporting"
 * Log changes: 
*/

import com.atlassian.jira.component.ComponentAccessor
import com.atlassian.jira.issue.customfields.option.Option
import com.onresolve.jira.groovy.user.FieldBehaviours
import groovy.transform.BaseScript
import com.atlassian.jira.issue.MutableIssue

@BaseScript FieldBehaviours fieldBehaviours
def projectName = issueContext.projectObject.name

log.debug("Behaviours: ${projectName} - Auto-populate RUN Fields Initialiser started.")

// Managers
def customFieldManager = ComponentAccessor.getCustomFieldManager();
def optionsManager = ComponentAccessor.getOptionsManager();

// Get the Field Names
def engDepartment = getFieldByName("Eng - Department");
def engPlatform = getFieldByName("Eng - Platform");
def deployedLocation = getFieldByName("Deployed Location");
def deployedProduct = getFieldByName("Deployed Product");
def runType = getFieldByName("RUN Type");
def featureToggle = getFieldByName("Feature Toggle");

// Main Script Body

// If this is a new issue being created, or an existing issue being edited
if(getAction().toString() == "Create" || getActionName() == null) {

	// Monitor the Epic Link field for any changes
	def changedFieldValue = getFieldById(getFieldChanged())?.getValue()
	def epicLinkKey = changedFieldValue.toString().substring(4)

	// If the Epic Link field is set
	if(changedFieldValue != null){
    
    	// Get the value of the "Build/Run" custom field for the linked Epic
		def issueManager = ComponentAccessor.getIssueManager()
		def parentIssue = issueManager.getIssueObject(epicLinkKey)
    	def buildRunField = customFieldManager.getCustomFieldObjectsByName("Build/Run")
		def parentMutableIssue = (MutableIssue) parentIssue // cast parentIssue to MutableIssue interface
    	def buildRunValue = parentMutableIssue.getCustomFieldValue(buildRunField[0])

		// If the Epic's "Build/Run" field is "Supporting"
		if (buildRunValue.toString() == "Supporting") {

            // If the Project belongs to the Marketing team:  DEPG, DSPG, DSPGCCM (JIRA-9669)
            if ((issueContext?.getProjectObject().getName().contains("Data Science Product Growth")) || (issueContext?.getProjectObject().getName().contains("Data Engineering Product Growth"))) {
                // If the "Eng - Department" field is not set
                if (!engDepartment.getValue()) {
                    engDepartment.setFormValue("Data & AI")
                }
                // If the "Eng - Platform" field is not set
                if (!engPlatform.getValue()) {
                    engPlatform.setFormValue("Data & AI")
                }
                // If the "Run Type" field is not set
                if (!runType.getValue()) {
                    runType.setFormValue("Performance and Operational Improvements")
                }
                // If the "Deployed Product" field is not set
                if (!deployedProduct.getValue()) {
                    if (issueContext?.getProjectObject().getName().contains("Data Science Product Growth CCM")) {
                        deployedProduct.setFormValue("CCM")
                    } else {
                        deployedProduct.setFormValue("Global Telemed")
                    }
                }
                // If the "Deployed Location" field is not set
                if (!deployedLocation.getValue()) {
                    deployedLocation.setFormValue("US")
                }
                // If the "Feature Toggle" field is not set
                if (!featureToggle.getValue()) {
                    featureToggle.setFormValue("NO")
                }
            }

            // If the Project belongs to the UCS team:  UCSA, UCSI, UCSS, UCSW (JIRA-9674)
            if (issueContext?.getProjectObject().getName().contains("UCS")) {
                // If the "Eng - Department" field is not set
                if (!engDepartment.getValue()) {
                    engDepartment.setFormValue("Clinical")
                }
                // If the "Eng - Platform" field is not set
                if (!engPlatform.getValue()) {
                    engPlatform.setFormValue("Clinical")
                }
                // If the "Run Type" field is not set
                if (!runType.getValue()) {
                    runType.setFormValue("Performance and Operational Improvements")
                }
                // If the "Deployed Product" field is not set
                if (!deployedProduct.getValue()) {
                    deployedProduct.setFormValue("HHS")
                }
                // If the "Deployed Location" field is not set
                if (!deployedLocation.getValue()) {
                    deployedLocation.setFormValue("US")
                }
            }

            // If the Project is MEX  (JIRA-9749)
            if (issueContext?.getProjectObject().getKey().contains("MEX")) {
                // If the "Eng - Department" field is not set
                if (!engDepartment.getValue()) {
                    engDepartment.setFormValue("Consumer")
                }
                // If the "Eng - Platform" field is not set
                if (!engPlatform.getValue()) {
                    engPlatform.setFormValue("Consumer")
                }
                // If the "Run Type" field is not set
                if (!runType.getValue()) {
                    runType.setFormValue("Production Support")
                }
                // If the "Deployed Product" field is not set
                if (!deployedProduct.getValue()) {
                    deployedProduct.setFormValue("Global Telemed")
                }
                // If the "Deployed Location" field is not set
                if (!deployedLocation.getValue()) {
                    deployedLocation.setFormValue("US")
                }
            }

        }

    }

}

log.info("Behaviours: ${projectName} - Auto-populate RUN Fields Initialiser completed.")