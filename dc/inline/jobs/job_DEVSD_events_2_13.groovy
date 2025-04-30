/*
Author: Jeff Melies
Required: A Customfield, type: Text Field (single line), Name: TIS-Waiting for Support (minutes)
        the name of the customfield must be 'TIS-<Actual Status Name as it appears in Jira> (minutes)'
10/9/2023 I modified this to get the first Status's Time In Status
Need to verify before replacing the original
*/

import com.atlassian.jira.component.ComponentAccessor
import com.atlassian.jira.issue.Issue
import java.lang.String
import java.text.DecimalFormat
import com.atlassian.jira.component.ComponentAccessor
import com.atlassian.jira.issue.history.ChangeItemBean
import com.atlassian.jira.issue.status.Status
import com.atlassian.jira.issue.changehistory.ChangeHistory
import com.atlassian.jira.issue.changehistory.ChangeHistoryManager
//import com.atlassian.jira.issue.changehistory.ChangeHistoryItem
import com.onresolve.scriptrunner.parameters.annotation.*
import com.atlassian.jira.issue.ModifiedValue
import com.atlassian.jira.issue.util.DefaultIssueChangeHolder
import groovy.transform.Field
import com.atlassian.jira.issue.Issue
import java.util.concurrent.TimeUnit
import java.sql.Timestamp
import org.apache.log4j.Logger
import org.apache.log4j.Level

log.setLevel(Level.INFO)
log = Logger.getLogger("Listener: Waiting for support (TIS)")

@ShortTextInput(label = "Statuses to capture 'time in status'", description = "Enter one or more statuses to capture the TIS, using the correct case as it appears in Jira (comma delimited).")
String stringStatuses
String[] elements = stringStatuses.split(",")
List<String> listStatuses = Arrays.asList(elements)
ArrayList<String> arrayStatuses = new ArrayList<String>(listStatuses)
for (i in 0..<arrayStatuses.size()) {
    arrayStatuses[i] = arrayStatuses[i].trim()
}

@Checkbox(label = "Only execute on 'Closed' issues", description = "If checked this script will only run on issues that have a Resolution populated.")
Boolean issueResolved

@Checkbox(label = 'Only execute on issues leaving from the above listed status(es).', description = 'If checked this script will only run on issues that are being transitioned from the above listed status(es).')
Boolean specifiedOnly

Issue issue = event.issue  //ComponentAccessor.getIssueManager().getIssueObject("DEVSD-17044") //
log.debug("issue: ${issue}")
def issueResolution = issue.getResolution() // get the issue's resolution field
def issueStatus = issue.getStatus() // get the current status
if(issueResolution)(issueResolution = issueResolution.name)
if(issueStatus)(issueStatus = issueStatus.name)
log.debug("Listener: TimeInStatus for ${issue}, Resolution: '${issueResolution}', Status: '${issueStatus}', On Statuses: '${stringStatuses}' has started.")

//Managers
def changeHistoryManager = ComponentAccessor.getChangeHistoryManager()
def customFieldManager = ComponentAccessor.customFieldManager

List statusChanges = changeHistoryManager.getChangeItemsForField(issue, "status") as List
if(statusChanges.size() <= 1){return} "This is a new issue and there is nothing to calculate"
//If the issues 'from status' wasn't the one we want to capture, don't run script.
if(specifiedOnly){
    log.debug("Listener: TimeInStatus for ${issue}, statusChanges.size(): ${statusChanges.size()}")
    ChangeItemBean item = statusChanges[statusChanges.size()-1]
    log.debug("******specifiedOnly ITEM: ${item.toString()}.")
    log.debug("******specifiedOnly item.getFromString().toString(): ${item.getFromString().toString()}.")
    log.debug("******specifiedOnly stringStatuses: ${stringStatuses}.")
    if(!((stringStatuses).contains(item.getFromString().toString()))){
        log.debug("Listener: TimeInStatus for ${issue}, specifiedOnly - is stopping because the listener field is checked for 'Only execute on issues leaving from listed statuses.' - statuses to look for: ${stringStatuses}, the previous status was: ${item.getFromString().toString()} ")
        return //The previous status wasn't what we are looking for
    }//else{
    //    log.debug("Listener: TimeInStatus for ${issue}, specifiedOnly - has completed and will continue the sccript.***")
    //}
}
if(issueResolved) {
    log.debug("Listener: TimeInStatus for ${issue}, issueResolved -  is stopping since the listener field is checked for 'Only execute on Closed issues'.")
    if (!(issueResolution)){
        log.debug("Listener: TimeInStatus for ${issue}, issueResolved - is stopping since ${issue} does not have a Resolution and the project is set to only run this script on Closed issues.")
        return //Only execute script on resolved issues
    }else{
        log.debug("Listener: TimeInStatus for ${issue}, issueResolved - has completed and will continue the sccript.***")
    }
}

// this below is list of updates of <issue>
List changeItems = changeHistoryManager.getAllChangeItems(issue)

def length = changeItems.size() // length of this list
log.debug("Listener: TimeInStatus for ${issue}, length: '${length}', changeItems: '${changeItems}'")

List cfNamesList = []
cfNamesList = dynamicLists(statusChanges, arrayStatuses) as List
log.debug("Listener: TimeInStatus for ${issue}, cfNamesList returned: '${cfNamesList.size()}', '${cfNamesList.toString()}'.")
if(cfNamesList.size() != arrayStatuses.size()){
    log.error("Listener: TimeInStatus for ${issue} - The Custom Fields count doesn't match the Statuses you are wanting to capture, please review.")
    return "Nothing to report"
}
setCFValues(issue, arrayStatuses, cfNamesList, statusChanges)
log.debug("Listener: TimeInStatus for ${issue}, Resolution: '${issueResolution}', Current Status: '${issueStatus}' has completed.")



//Functions
def setCFValues(Issue issue, List statusesArray, List cfNamesList, List Changes){
    issue = event.issue  //ComponentAccessor.getIssueManager().getIssueObject("DEVSD-17044")
    def fieldName
    def issueCF
    def durationInStatus
    def statusToFind
    log.debug("Listener: TimeInStatus for ${issue}, Starting setCFValues - statusesArray: (${statusesArray.size()}) ${statusesArray}, \
            cfNamesList: (${cfNamesList.size()}) ${cfNamesList}, Changes: (${Changes.size()}) ${Changes}.")
    //for (int i = 0; i < cfNamesList.size(); i++) {
    for (int i = 0; i < statusesArray.size(); i++) {
        durationInStatus = ""
        statusToFind = statusesArray[i]
        for (int j = 0; j < cfNamesList.size(); j++){
            fieldName = cfNamesList[j]
            log.debug("Listener: TimeInStatus for ${issue}, setCFValues ${i}/${statusesArray.size()} - Testing if 'TIS-${statusToFind} (minutes)' = '${fieldName}': " + ("TIS-${statusToFind} (minutes)" == fieldName))
            if("TIS-${statusToFind} (minutes)" == fieldName){
                durationInStatus = getTimeInStatus(Changes as List, statusToFind.toString())
                log.debug("Listener: TimeInStatus for ${issue}, setCFValues ${i}/${statusesArray.size()} - *FINAL* - fieldName: ${fieldName}, statusToFind: ${statusToFind}, durationInStatus: ${durationInStatus}.")
                if(durationInStatus){
                    issueCF = ComponentAccessor.customFieldManager.getCustomFieldObjects(issue).findByName(fieldName.toString())
                    if(!(issueCF)){
                        log.debug("Listener: TimeInStatus for ${issue}, setCFValues ${i}/${statusesArray.size()} - Could not find custom field with name ${fieldName}")
                    }else{
                        log.debug("Listener: TimeInStatus for ${issue}, setCFValues ${i}/${statusesArray.size()} - Updating custom field: ${issueCF} value: ${durationInStatus.toString()}, on Issue: ${issue}.")
                        issueCF.updateValue(null, issue, new ModifiedValue(issue.getCustomFieldValue(issueCF), durationInStatus.toString()), new DefaultIssueChangeHolder())
                    }
                }
            }else{
                log.debug("Listener: TimeInStatus for ${issue}, setCFValues ${i}/${statusesArray.size()} - Could not locate '${statusToFind}' ${fieldName}")
            }
        }
    }
    log.debug("Listener: TimeInStatus for ${issue}, setCFValues is complete.")
}
  
//Returns time in status by status
def getTimeInStatus(List statusChanges, String statustoFind){
    Issue issue = event.issue  //ComponentAccessor.getIssueManager().getIssueObject("DEVSD-17044")
    long durationMS
    long totalDurationMS
    long DurationInStatus
    long totalDurationInStatus
    Double durationMSD = 0
    Double totalDurationMSD = 0
    Double DurationInStatusD = 0
    Double totalDurationInStatusD = 0
    for (int i = 0; i < statusChanges.size(); i++) {
        log.debug("Listener: TimeInStatus for ${issue}, getTimeInStatus - ${i}/${statusChanges.size()}, statustoFind: ${statustoFind}")
        ChangeItemBean item
        ChangeItemBean nextItem
        log.debug("Listener: TimeInStatus for ${issue}, getTimeInStatus - ${i}/${statusChanges.size()}, allChanges: ${ComponentAccessor.getChangeHistoryManager().getAllChangeItems(issue)}")
        log.debug("Listener: TimeInStatus for ${issue}, getTimeInStatus - ${i}/${statusChanges.size()}, item(statusChanges): ${statusChanges[i]}")
        log.debug("Listener: TimeInStatus for ${issue}, getTimeInStatus - ${i}/${statusChanges.size()}, Next(statusChanges): ${statusChanges[i+1]}")
        if(statusChanges[i]) { 
            item = statusChanges[i] 
            log.debug("Listener: TimeInStatus for ${issue}, getTimeInStatus - ${i}/${statusChanges.size()}, item: ${item}")
        }
        if(statusChanges[i+1]){ 
            nextItem = statusChanges[i+1] 
            log.debug("Listener: TimeInStatus for ${issue}, getTimeInStatus - ${i}/${statusChanges.size()}, nextItem: ${nextItem}")
        }
        log.debug("Listener: TimeInStatus for ${issue}, getTimeInStatus ${i}/${statusChanges.size()} --Testing for a match of ${item.getToString()} equals ${statustoFind.toString()}: ${item.getToString() == statustoFind.toString()}.")
        if(item.getToString() == statustoFind.toString()){
            log.debug("Listener: TimeInStatus for ${issue}, getTimeInStatus - **To String**: '${item.getToString()}' matches  statustoFind.toString(): '${statustoFind.toString()}'")
            if(item && nextItem){ //This means there are multiple changes
                log.debug("Listener: TimeInStatus for ${issue}, getTimeInStatus - multiple changes and keep iterating to see if we can find more.")
                durationMS = nextItem.getCreated().getTime() - item.getCreated().getTime()
                durationMSD = (Integer)nextItem.getCreated().getTime() - (Integer)item.getCreated().getTime()
                log.debug("Listener: TimeInStatus for ${issue}, getTimeInStatus ${i}/${statusChanges.size()}, adding ${durationMS} to ${totalDurationMS} = ${durationMS + totalDurationMS}")
                totalDurationMS = totalDurationMS + durationMS
                totalDurationMSD = (Integer)totalDurationMSD + (Integer)durationMSD
                log.debug("Listener: TimeInStatus for ${issue}, getTimeInStatus ${i}/${statusChanges.size()}, totalDurationMS: ${totalDurationMS}, to minutes: ${TimeUnit.MILLISECONDS.toMinutes(totalDurationMS)}")
            }
        //
        }else if(item.getFromString() == statustoFind.toString()){
            log.debug("Listener: TimeInStatus for ${issue}, getTimeInStatus ${i}/${statusChanges.size()}, **From String**: '${item.getFromString()}' matches  '${statustoFind.toString()}'")
            //List allChanges = ComponentAccessor.getChangeHistoryManager().getAllChangeItems(issue) as List
            Timestamp issueCreated = issue.getCreated()
            log.debug("Listener: TimeInStatus for ${issue}, getTimeInStatus ${i}/${statusChanges.size()}, item.getCreate: ${item.getCreated().getTime()} subtract ${issueCreated.getTime()} (${issueCreated})")
            log.debug("Listener: TimeInStatus for ${issue}, getTimeInStatus ${i}/${statusChanges.size()}, DurationMS ${item.getCreated().getTime() - issueCreated.getTime()}")
            durationMS = item.getCreated().getTime() - issueCreated.getTime()
            durationMSD = item.getCreated().getTime() - issueCreated.getTime()
            log.debug("Listener: TimeInStatus for ${issue}, getTimeInStatus ${i}/${statusChanges.size()}, adding ${durationMS} to ${totalDurationMS} = ${durationMS + totalDurationMS}")
            totalDurationMS = totalDurationMS + durationMS
            totalDurationMSD = (Integer)totalDurationMSD + (Integer)durationMSD
        }        
        DurationInStatus = totalDurationMS
        DurationInStatusD = ((totalDurationMSD / 1000) / 60)       
        //Double TISDouble = TimeUnit.MILLISECONDS.toMinutes(DurationInStatus)
        //log.debug("Listener: TimeInStatus for ${issue}, getTimeInStatus - Converted to Minutes as Double w/ 1 dec: ${DurationInStatusOneDec.toString()}")
    }
    if(DurationInStatusD){        
        DecimalFormat df = new DecimalFormat("##.0") //Get only 1 decimal 
        String DurationInStatusOneDec = df.format(DurationInStatusD)
        log.debug("Listener: TimeInStatus for ${issue}, getTimeInStatus, **FINAL**, DurationInStatus -- For ${statustoFind.toString()} we are returning (minutes): ${DurationInStatusOneDec.toString()}") //${TimeUnit.MILLISECONDS.toMinutes(DurationInStatus)}")
        //return TimeUnit.MILLISECONDS.toMinutes(DurationInStatus)
        return DurationInStatusOneDec.toString()
    }
}

//Create customfieldNames list
def dynamicLists(List changeItemsByStatus, ArrayList statusesArray){
    Issue issue = event.issue  //ComponentAccessor.getIssueManager().getIssueObject("DEVSD-17044")
    List cfNamesList = []
    for (int i = 0; i < changeItemsByStatus.size(); i++) {
        binding.variables.remove 'cfName'        
        def cfName
        ChangeItemBean item = changeItemsByStatus[i]
        //Only get collection for statuses listed in dynamic forms field
        for (int j = 0; j < statusesArray.size(); j++) {
            //log.debug("Listener: TimeInStatus for ${issue}, dynamicLists - LOOKING FOR: ${statusesArray[j].toString()} ====== in Change Item: ${item.getFromString().toString()}")
            if(statusesArray[j].toString() == item.getFromString().toString()){
                //log.debug("Listener: TimeInStatus for ${issue}, dynamicLists - Adding TIS-${item.getFromString()} (minutes) to cfNameList if it doesn't already exist.")
                cfName = "TIS-${item.getFromString()} (minutes)" 
                if(cfName){
                    //log.debug("Listener: TimeInStatus for ${issue}, dynamicLists - Checking if cfNamesList ${cfNamesList.toString()} has ${cfName} in it.")
                    if(!(cfNamesList.contains(cfName))){
                        log.debug("Listener: TimeInStatus for ${issue}, dynamicLists - Adding ${cfName} to cfNameList.")
                        cfNamesList.add(cfName)
                    }//else{
                    //    log.debug("**dynamicLists - CFNAMELIST ALREADY CONTAINs CFNAME: ${cfName}.")
                    //}
                }//else{
                //    log.debug("** dynamicLists - cfName: TIS-${item.getFromString()} (minutes) doesn't exist and we are moving on.")
                //}
            }
        }
    }
return cfNamesList as List
}