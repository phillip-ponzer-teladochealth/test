/*
Creator: Jeff Tompkins
Purpose: If an issue's parent Epic has the "Build/Run" field set to "Supporting", then make the 5 Run Allocation fields visible and required
Change log: 
*/

import com.atlassian.jira.component.ComponentAccessor
// import com.atlassian.jira.issue.fields.CustomField
// import com.atlassian.jira.issue.Issue
import com.atlassian.jira.issue.MutableIssue
import org.apache.log4j.Logger
import org.apache.log4j.Level

def log = Logger.getLogger("Behaviour: Run Allocation Field Logic")
log.setLevel(Level.INFO)

def projectName = issueContext.projectObject.name
log.debug("Behaviour Script '${projectName} - Hide Run Allocation Fields' has started.")

// If this is a new issue being created
if(getAction().toString() == "Create") {

	// Monitor the Epic Link field for any changes
	def changedFieldValue = getFieldById(getFieldChanged())?.getValue()
	def epicLinkKey = changedFieldValue.toString().substring(4)

	// If the Epic Link field is set
	if( changedFieldValue != null){

		// Get the value of the "Build/Run" custom field for the linked Epic
		def issueManager = ComponentAccessor.getIssueManager()
		def customFieldManager = ComponentAccessor.getCustomFieldManager()
		def parentIssue = issueManager.getIssueObject(epicLinkKey)
    	def buildRunField = customFieldManager.getCustomFieldObjectsByName("Build/Run")
		def parentMutableIssue = (MutableIssue) parentIssue // cast parentIssue to MutableIssue interface
    	def buildRunValue = parentMutableIssue.getCustomFieldValue(buildRunField[0])

		// If the Epic's "Build/Run" field is not "Business"
		if (buildRunValue.toString() != "Business") {

			// Show the Run Allocation fields and set them as required
			getFieldByName("Eng - Department").setHidden(false).setRequired(true)
			getFieldByName("Eng - Platform").setHidden(false).setRequired(true)
			getFieldByName("RUN Type").setHidden(false).setRequired(true)
			getFieldByName("Deployed Location").setHidden(false).setRequired(true)
			getFieldByName("Deployed Product").setHidden(false).setRequired(true)

		// If the Epic's "Build/Run" field is "Business"
		} else {

			// Hide the Run Allocation fields, clear their values, and make them optional
			getFieldByName("Eng - Department").setHidden(true).setRequired(false).setFormValue(null)
			getFieldByName("Eng - Platform").setHidden(true).setRequired(false).setFormValue(null)
			getFieldByName("RUN Type").setHidden(true).setRequired(false).setFormValue(null)
			getFieldByName("Deployed Location").setHidden(true).setRequired(false).setFormValue(null)
			getFieldByName("Deployed Product").setHidden(true).setRequired(false).setFormValue(null)
		}

	// If the Epic Link field is not set
	} else {

		// Hide the Run Allocation fields, clear their values, and make them optional
		getFieldByName("Eng - Department").setHidden(true).setRequired(false).setFormValue(null)
		getFieldByName("Eng - Platform").setHidden(true).setRequired(false).setFormValue(null)
		getFieldByName("RUN Type").setHidden(true).setRequired(false).setFormValue(null)
		getFieldByName("Deployed Location").setHidden(true).setRequired(false).setFormValue(null)
		getFieldByName("Deployed Product").setHidden(true).setRequired(false).setFormValue(null)
	}

// If this is an existing issue being edited
} else {

	// Hide the Run Allocation fields and make them optional
	getFieldByName("Eng - Department").setHidden(true).setRequired(false)
	getFieldByName("Eng - Platform").setHidden(true).setRequired(false)
	getFieldByName("RUN Type").setHidden(true).setRequired(false)
	getFieldByName("Deployed Location").setHidden(true).setRequired(false)
	getFieldByName("Deployed Product").setHidden(true).setRequired(false)

	// Monitor the Epic Link field for any changes
	def changedFieldValue = getFieldById(getFieldChanged()).getValue()
	def epicLinkKey = changedFieldValue.toString().substring(4)

	// If the Epic Link field is set
	if( changedFieldValue != null){

		// Get the value of the "Build/Run" custom field for the linked Epic
		def issueManager = ComponentAccessor.getIssueManager()
		def customFieldManager = ComponentAccessor.getCustomFieldManager()
		def parentIssue = issueManager.getIssueObject(epicLinkKey)
    	def buildRunField = customFieldManager.getCustomFieldObjectsByName("Build/Run")
		def parentMutableIssue = (MutableIssue) parentIssue // cast parentIssue to MutableIssue interface
    	def buildRunValue = parentMutableIssue.getCustomFieldValue(buildRunField[0])

		// Set the visibility of target fields based on the value of the "Build/Run" field
		if (buildRunValue.toString() != "Business") {

			// Show the Run Allocation fields and set them as required
			getFieldByName("Eng - Department").setHidden(false).setRequired(true)
			getFieldByName("Eng - Platform").setHidden(false).setRequired(true)
			getFieldByName("RUN Type").setHidden(false).setRequired(true)
			getFieldByName("Deployed Location").setHidden(false).setRequired(true)
			getFieldByName("Deployed Product").setHidden(false).setRequired(true)
		}

	// If the Epic Link field is not set
	} else {

		// Hide the Run Allocation fields, clear their values, and make them optional
		getFieldByName("Eng - Department").setHidden(true).setRequired(false).setFormValue(null)
		getFieldByName("Eng - Platform").setHidden(true).setRequired(false).setFormValue(null)
		getFieldByName("RUN Type").setHidden(true).setRequired(false).setFormValue(null)
		getFieldByName("Deployed Location").setHidden(true).setRequired(false).setFormValue(null)
		getFieldByName("Deployed Product").setHidden(true).setRequired(false).setFormValue(null)
	}
}

log.debug("Behaviour Script '${projectName} - Hide Run Allocation Fields' has completed.")