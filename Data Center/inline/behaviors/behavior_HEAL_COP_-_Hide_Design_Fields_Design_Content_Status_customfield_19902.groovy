// Jeff Tompkins
// HEAL & COP projects - Bug, Story, Task
// Hide Design Fields if Design Status is empty

import com.atlassian.jira.component.ComponentAccessor;
import com.atlassian.jira.issue.customfields.option.Option;
import com.onresolve.jira.groovy.user.FieldBehaviours;
import groovy.transform.BaseScript;

//Managers
def customFieldManager = ComponentAccessor.getCustomFieldManager();
def optionsManager = ComponentAccessor.getOptionsManager();

// This listens if the "Design Status" field has changed
def designStatus = getFieldById(getFieldChanged()); 

// This fetches the names of the other 3 Design custom fields
def approvalCheckoff = getFieldByName("Approval Checkoff");
def designer = getFieldByName("Designer");
def notes = getFieldByName("Notes");

// This gets the value of the Design Status field
def designStatusOption = designStatus.getValue() as String
// log.info("Design Status selected: " + designStatusOption)

// Hide the 3 Design fields
if (designStatusOption == null) {
    // hide Approval Checkoff field
    approvalCheckoff.setHidden(true);
    // hide Designer field
    designer.setHidden(true);
    // hide Notes field
    notes.setHidden(true);
// else Show the 3 Design fields
} else {
    // show Approval Checkoff field
    approvalCheckoff.setHidden(false);
    // show Designer field
    designer.setHidden(false);
    // show Notes field
    notes.setHidden(false);
}