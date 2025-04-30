package com.teladochealth.customFields

/**
Creator: Jeff Melies
Purpose: Restricts Single Select List including Resolutions.
To Use: In Behaviours add which customfield you want limited,
        Add this server-side script
        Select the field again
        Enter what options you want available (These must be in the fields context).
***This could be coded for a multi select list as well, I just haven't needed it yet***
Change log:
*/

//Imports
import com.atlassian.jira.component.ComponentAccessor
import com.onresolve.jira.groovy.user.FieldBehaviours
import com.onresolve.scriptrunner.parameters.annotation.*
import com.atlassian.jira.issue.fields.Field
import static com.atlassian.jira.issue.IssueFieldConstants.* //.RESOLUTION
import groovy.transform.BaseScript
import org.apache.log4j.Logger
import org.apache.log4j.Level

log = Logger.getLogger("restrict_selectList")
log.setLevel(Level.INFO)

@FieldPicker(label = "Field", description = "Select a field")
Field cField
String cFieldClass = cField.getClass().toString()
log.debug("cField: ${cField}, Type ${cField.getClass()}")
@ShortTextInput(label = "Enter available options", description = "List valid 'Options' any case and comma delimited (Example: Fixed,Completed Successfully,Invalid).")
String limitOptions
log.debug("limitOptions: ${limitOptions}")
def projectName = issueContext.projectObject.name

@BaseScript FieldBehaviours fieldBehaviours
//Logging
log.debug("Behaviours: ${projectName} - restrict_selectList for ${cField} has started.")

def constantsManager = ComponentAccessor.constantsManager
def cfManager = ComponentAccessor.customFieldManager
def optionsManager = ComponentAccessor.optionsManager

switch(cFieldClass.toString()){
  case ~/(?i).*immutablecustomfield$/:
    log.debug("******MATCH*Customfield****")
    def config = ComponentAccessor.fieldConfigSchemeManager.getRelevantConfig(issueContext, cfManager.getCustomFieldObjectsByName(cField.getName())[0])
    def options = optionsManager.getOptions(config)
    try{
      getFieldByName(cField.name).setFieldOptions(options.findAll {
        limitOptions.toString().containsIgnoreCase(it.value.toString())
      })
    }catch(Exception e) {
      log.error("Behaviours: ${issueContext.projectObject.name} - Dynamic Form-Restrict_selectList:${cField.getName()} - Exception: ${e}")
    }finally{
      log.debug("Behaviours: ${projectName} - restrict_selectList for ${cField} has completed.")
    }
  break
  case ~/(?i).*resolutionsystemfield*$/:
    log.debug("******MATCH*Resolution System Field****")
    try{
        getFieldById(RESOLUTION).setFieldOptions(constantsManager.getResolutions().findAll {
            limitOptions.containsIgnoreCase(it.name)
        })
    }catch(Exception e) {
      log.error("Behaviours: ${issueContext.projectObject.name} - Dynamic Form-Restrict_selectList:Resolution - Exception: ${e}")
    }finally{
      log.debug("Behaviours: ${projectName} - restrict_selectList for Resolution has completed.")
    }
  break
  case ~/(?i).*IssueTypeSystemField*$/:
    log.debug("******MATCH*IssueType System Field****")
    try{
        getFieldById(ISSUE_TYPE).setFieldOptions(constantsManager.getAllIssueTypeObjects().findAll {
            limitOptions.containsIgnoreCase(it.name)
        })
    }catch(Exception e) {
      log.error("Behaviours: ${issueContext.projectObject.name} - Dynamic Form-Restrict_selectList:ISSUE_TYPE - Exception: ${e}")
    }finally{
      log.debug("Behaviours: ${projectName} - restrict_selectList for ISSUE_TYPE has completed.")
    }
  break
  default:
    log.error("Behaviours: ${projectName} - restrict_selectList ******DEFAULT-SWITCH THIS MUST BE FIXED BEFORE IT WILL WORK*****.")
  break
}
