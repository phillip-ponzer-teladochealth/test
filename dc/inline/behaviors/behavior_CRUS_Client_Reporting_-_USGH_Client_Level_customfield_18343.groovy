/**import org.apache.jasper.Options

* Eduardo Rojo
* CRUS project - Client Reporting - USGH
* Issue Type: Story
* Hide fields based on "Client Level"
* Log: 4/3/2023 JIRA-5208
*/

import com.atlassian.jira.component.ComponentAccessor;
import com.atlassian.jira.issue.customfields.option.Option;
log.info("Behaviours CRUS Project – Client Level started.");

def customFieldManager = ComponentAccessor.getCustomFieldManager();
def optionsManager = ComponentAccessor.getOptionsManager();

// This listens if the "Client Level" field has changed
def clientLevel = getFieldById(getFieldChanged());

// This saves the custom field "SFTP - Client/Vendor Details" and "Email Delivery - Provide Email"
def groupId = getFieldByName("Group ID, Org ID or Client Code");

// This gets the value of "Mode of Delivery" field
def clientLevelOption = clientLevel.getValue() as String

log.info("Client Level: " + clientLevelOption);

// Set  "Group ID, Org ID or Client Code" field as Required
if(clientLevelOption){
	groupId.setRequired(true);
	}
else{
	groupId.setRequired(false);
	}
log.info("Behaviours CRUS Project – Client Level finished.");