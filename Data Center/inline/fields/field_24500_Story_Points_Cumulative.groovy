/*
Author: Jeff Tompkins
Created:  9/16/24  [JIRA-9936]
Purpose:    This script is used to iterate through the Epic Link issues and adding the totals of a specified field together and display results on a scripted field.
            This revision uses the "Epic-Story Link" issue link to locate child issues, instead of a JQL search on child issues of the "Epic Name" field
Previous Script Version:  Saved in a comment on this ticket:  JIRA-9936
*/

import com.atlassian.jira.component.ComponentAccessor
import com.atlassian.jira.issue.Issue
import org.apache.log4j.Logger
import org.apache.log4j.Level
import com.atlassian.jira.issue.fields.Field
import com.onresolve.scriptrunner.parameters.annotation.IssueTypePicker
import com.onresolve.scriptrunner.parameters.annotation.*
import com.atlassian.jira.issue.issuetype.IssueType

log = Logger.getLogger("Scriptrunner_Fields.StoryPointsCumulative")
log.setLevel(Level.INFO)

// Allow the user to choose which Numeric field will be summed
@FieldPicker(label = "Field", description = "Select the field that you want to get a cumulitive sum on.")
Field cfField

// Allow the user to choose specific Issue Types to exempt
@IssueTypePicker(
    label = 'Exclude issue type(s)', description = 'Select 1 or more issue types to exclude from calculation', placeholder = 'Select 1 or more issue types to exclude from calculation',
    multiple = true
)
List<IssueType> excludeIssueTypes
List excludeIssueTypeNames = []
excludeIssueTypes.each{
    excludeIssueTypeNames.add(it.getName().toString())
}

// Globals
Integer output = 0

log.debug("Story Point Cumulative started on ${issue}")

// Fetch all non-excluded child issues of the Epic
def linkedIssues = ComponentAccessor.issueLinkManager.getOutwardLinks(issue.id).findAll { (it.issueLinkType.name == "Epic-Story Link" && !excludeIssueTypeNames.contains(it.destinationObject.issueType.name)) }

// Exit the script early if there are no linked issues
if (!linkedIssues) {
    log.debug "Issue has no links - exiting script"
    return null
}

// Exit the script early if the scripted field's context does not apply to the current issue
def storypointscumulativeField = ComponentAccessor.customFieldManager.getCustomFieldObjects(linkedIssues.first().destinationObject).findByName(cfField.name)
if (!storypointscumulativeField) {
    log.debug "Custom field is not configured for that context"
    return null
}

// Calculate the sum
output = linkedIssues*.destinationObject.sum {
    Issue it -> log.debug "Child Issue: '$it'"; it.getCustomFieldValue(storypointscumulativeField) ?: 0
    }

log.debug "Cumulative Sum: '$output'"
log.debug("Story Point Cumulative completed on ${issue}")

output

