//Creator: Jeff Melies
//Purpose: Show other fields dependent on this one
//Change log:

//imports
import com.atlassian.jira.component.ComponentAccessor
import com.onresolve.jira.groovy.user.FieldBehaviours
import groovy.transform.BaseScript

@BaseScript FieldBehaviours fieldBehaviours

import org.apache.log4j.Logger
log = Logger.getLogger("Behaviour: UI Design")

def projectName = issueContext?.projectObject?.name

//Logging
log.debug("${projectName}-'Design/Content Status' has started")

def changedFieldValue = getFieldById(getFieldChanged())?.getValue()

if( changedFieldValue != null){
	def notes = getFieldByName("Design/Content Notes")?.setHidden(false)
	def designer = getFieldByName("Designer")?.setHidden(false)
	def approvalCO = getFieldByName("Design/Content Approvals")?.setHidden(false)
	def strategist = getFieldByName("Content Strategist")?.setHidden(false)
}else{
	def notes = getFieldByName("Design/Content Notes")?.setHidden(true)
	def designer = getFieldByName("Designer")?.setHidden(true)
	def approvalCO = getFieldByName("Design/Content Approvals")?.setHidden(true)
	def strategist = getFieldByName("Content Strategist")?.setHidden(true)
}

log.debug("${projectName}-'Design/Content Status' has completed")