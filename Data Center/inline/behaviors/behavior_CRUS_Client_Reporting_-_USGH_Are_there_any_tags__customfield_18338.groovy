/**
 * Eduardo Rojo
 * CRUS project - Client Reporting - USGH
 * Issue Type: Story
 * Hide fields based on "Are there any tags?"
 */

import com.atlassian.jira.component.ComponentAccessor;
import com.atlassian.jira.issue.customfields.option.Option;
log.info("Behaviours CRUS Project – Are there any tags? started.");

def customFieldManager = ComponentAccessor.getCustomFieldManager()
def optionsManager = ComponentAccessor.getOptionsManager();

// This listens if the "Are there any tags?" field has changed
def areThereAnyTags = getFieldById(getFieldChanged());

// This saves the custom field "Tags" under the name tags
def tags = getFieldByName("Tags");

// This gets the value of "Are there any tags?"
def areThereAnyTagsOption = areThereAnyTags.getValue() as String

log.info("Are there any tags?: " + areThereAnyTagsOption);
// Show/Hide and set "tags" as Required 
def tagsNew = areThereAnyTagsOption == "Yes"
tags.setHidden(!tagsNew);
tags.setRequired(tagsNew);

// Show Help Text under "Tags" field
if (tagsNew){
	tags.setHelpText("Please provide tag, unique identifier, client specific identifier (separate each tag by comma).");    
}
else{
    tags.setHelpText("")
}
log.info("Behaviours CRUS Project – Are there any tags? completed.") 