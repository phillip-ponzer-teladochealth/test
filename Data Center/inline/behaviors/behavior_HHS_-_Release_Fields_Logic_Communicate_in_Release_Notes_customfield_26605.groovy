//Creator: Jeff Tompkins
//Purpose: Show other fields dependent on this one
//Change log:

//imports
import com.onresolve.jira.groovy.user.FieldBehaviours
import groovy.transform.BaseScript
import com.atlassian.jira.component.ComponentAccessor
def customFieldManager = ComponentAccessor.getCustomFieldManager()
def attachmentManager = ComponentAccessor.getAttachmentManager()

@BaseScript FieldBehaviours fieldBehaviours

import org.apache.log4j.Logger
log = Logger.getLogger("Behaviour: HHS Release Field Logic")

def projectName = issueContext?.projectObject?.name
def changedFieldValue = getFieldById(getFieldChanged())?.getValue()

if( changedFieldValue == "Yes"){
	getFieldByName("Release Notes Title")?.setRequired(true)
	getFieldByName("Release Notes")?.setRequired(true)
	getFieldByName("Customer Engagement")?.setRequired(true)
	getFieldByName("Release Method")?.setRequired(true)
	getFieldByName("Feature Toggle Name")?.setRequired(true)
	getFieldByName("Region")?.setRequired(true)

/*def hasAttachments = (ComponentAccessor.attachmentManager.getAttachments(issue) != null)

if(hasAttachments != null)
{
   hasAttachments = true
   log = Logger.getLogger("${hasAttachments}")
}
else
{
   hasAttachments = false
   log = Logger.getLogger("${hasAttachments}")
}
*/

}else{
	getFieldByName("Release Notes Title")?.setRequired(false)
	getFieldByName("Release Notes")?.setRequired(false)
	getFieldByName("Customer Engagement")?.setRequired(false)
	getFieldByName("Release Method")?.setRequired(false)
	getFieldByName("Feature Toggle Name")?.setRequired(false)
	getFieldByName("Region")?.setRequired(false)
}

log.debug("${projectName}-'Behaviour: HHS Release Field Logic hascompleted'")