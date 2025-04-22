package com.teladochealth.listeners

/*Creator: Jeff Melies
Purpose: Track 'Time in Status'
Requirements:   1. You must enter at least 1 status as it appears on the ViewStatuses.jspa page.
                2. You must have a customfield created that matches 'TIS-<status> (minutes)' for each status listed.
                3. The above customfield must be available on the screen.
Change log: 1/30/23 JBM corrected an issue whith capturing multiple statuses.
*/

import com.atlassian.jira.component.ComponentAccessor
import com.atlassian.jira.issue.Issue
import java.lang.String
import com.atlassian.jira.component.ComponentAccessor
import com.atlassian.jira.issue.history.ChangeItemBean
import com.atlassian.jira.issue.status.Status
import com.atlassian.jira.issue.changehistory.ChangeHistory
import com.atlassian.jira.issue.changehistory.ChangeHistoryManager
import com.onresolve.scriptrunner.parameters.annotation.*
import com.atlassian.jira.issue.ModifiedValue
import com.atlassian.jira.issue.util.DefaultIssueChangeHolder
import groovy.transform.Field
import com.atlassian.jira.issue.Issue
import java.util.concurrent.TimeUnit
import org.apache.log4j.Level

log.setLevel(Level.INFO)

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

Issue issue = event.getIssue() //event.issue //.getIssue() get issue which triggered listener (ignore the STC error)
def issueResolution = issue.getResolution() // get the issue's resolution field
def issueStatus = issue.getStatus() // get the current status
if(issueResolution)(issueResolution = issueResolution.name)
if(issueStatus)(issueStatus = issueStatus.name)
log.debug("Listener: TimeInStatus for ${issue}, Resolution: ${issueResolution}, Status: ${issueStatus}, On Statuses: ${stringStatuses} has started.")

//Managers
def changeHistoryManager = ComponentAccessor.getChangeHistoryManager()
def customFieldManager = ComponentAccessor.customFieldManager

List statusChanges = changeHistoryManager.getChangeItemsForField(issue, "status") as List
if(statusChanges.size() <= 1){return} //This is a new issue and there is nothing to calculate
//If the issues 'from status' wasn't the one we want to capture, don't run script.
if(specifiedOnly){
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

List cfNamesList = []
cfNamesList = dynamicLists(statusChanges, arrayStatuses) as List
log.debug("Listener: TimeInStatus for ${issue}, cfNamesList is complete, returning ${cfNamesList.size()}, ${cfNamesList.toString()}. *&*&*&*&*&*&*&*&*&")
if(cfNamesList.size() != arrayStatuses.size()){
    log.debug("Listener: TimeInStatus for ${issue} - The Custom Fields count doesn't match the Statuses you are wanting to capture, please review.")
}
setCFValues(issue, arrayStatuses, cfNamesList, statusChanges)
log.debug("Listener: TimeInStatus for ${issue}, setCFValues is complete. *&*&*&*&*&*&*&*&*&")
log.debug("Listener: TimeInStatus for ${issue}, Resolution: ${issueResolution}, Status: ${issueStatus} has completed.")



//Functions
def setCFValues(Issue issue, List statusesArray, List cfNamesList, List Changes){
    def fieldName
    def issueCF
    def durationInStatus
    def statusToFind
    log.debug("Listener: TimeInStatus for ${issue}, Starting setCFValues - *IMPORTANT* - Issue: ${issue}, statusesArray size: ${statusesArray.size()}, cfNamesList size: ${cfNamesList.size()}, Changes size: ${Changes.size()}.")
    //for (int i = 0; i < cfNamesList.size(); i++) {
    for (int i = 0; i < statusesArray.size(); i++) {
        durationInStatus = ""
        statusToFind = statusesArray[i]
        for (int j = 0; j < cfNamesList.size(); j++){
            fieldName = cfNamesList[j]
            log.debug("Listener: TimeInStatus for ${issue}, setCFValues - ${i} out of ${statusesArray.size()} - statusToFind: ${statusToFind} & fieldName ${fieldName}")
            if("TIS-${statusToFind} (minutes)" == fieldName){
                durationInStatus = getTimeInStatus(Changes as List, statusToFind.toString())
                log.debug("Listener: TimeInStatus for ${issue}, setCFValues ${i} out of ${statusesArray.size()} - *IMPORTANT* - fieldName: ${fieldName}, statusToFind: ${statusToFind}, durationInStatus: ${durationInStatus}.")
                if(durationInStatus as Integer >= 0){
                    issueCF = ComponentAccessor.customFieldManager.getCustomFieldObjects(issue).findByName(fieldName.toString())
                    if(!(issueCF)){
                        log.debug("Listener: TimeInStatus for ${issue} - Could not find custom field with name ${fieldName}")
                    }else{
                        log.debug("Listener: TimeInStatus for ${issue}, setCFValues ${i} out of ${statusesArray.size()} - Updating custom field: ${issueCF} value: ${durationInStatus.toString()}, on Issue: ${issue}.")
                        issueCF.updateValue(null, issue, new ModifiedValue(issue.getCustomFieldValue(issueCF), durationInStatus.toString()), new DefaultIssueChangeHolder())
                    }
                }
            }//else{
            //    log.debug("** setCFValues ${i} out of ${statusesArray.size()} - SKIPPING ${fieldName}")
            //}
        }
    }
}
  
//Returns time in status by status
def getTimeInStatus(List statusChanges, String statustoFind){
    long durationMS
    long totalDurationMS
    Long DurationInStatus
    Long totalDurationInStatus
    for (int i = 0; i < statusChanges.size(); i++) {
        log.debug("Listener: TimeInStatus for ${event.getIssue()}, getTimeInStatus - ${i} out of ${statusChanges.size()}, statustoFind: ${statustoFind}")
        ChangeItemBean item
        ChangeItemBean nextItem
        if(statusChanges[i]) { item = statusChanges[i] }
        if(statusChanges[i+1]){ nextItem = statusChanges[i+1] }
        if(item.getToString() == statustoFind.toString()){
            log.debug("Listener: TimeInStatus for ${event.getIssue()}, getTimeInStatus - item.getToString(): '${item.getToString()}' matches  statustoFind.toString(): '${statustoFind.toString()}'")
            if(item && nextItem){ //This means there are multiple changes
                log.debug("Listener: TimeInStatus for ${event.getIssue()}, getTimeInStatus - multiple changes and keep iterating to see if we can find more.")
                durationMS = nextItem.getCreated().getTime() - item.getCreated().getTime()
                log.debug("Listener: TimeInStatus for ${event.getIssue()}, getTimeInStatus ${i} out of ${statusChanges.size()}, adding ${durationMS} to ${totalDurationMS}")
                totalDurationMS = totalDurationMS + durationMS
                log.debug("Listener: TimeInStatus for ${event.getIssue()}, getTimeInStatus ${i}: totalDurationMS: ${totalDurationMS}, to minutes: ${TimeUnit.MILLISECONDS.toMinutes(totalDurationMS)}")
            }
        }
        DurationInStatus = totalDurationMS
    }
    if(DurationInStatus){
        if (DurationInStatus >= 30000 && DurationInStatus < 60000){
            DurationInStatus = 60000 as Long
        }
        log.debug("Listener: TimeInStatus for ${event.getIssue()}, getTimeInStatus - DurationInStatus -- For ${statustoFind.toString()} we are returning: ${TimeUnit.MILLISECONDS.toMinutes(DurationInStatus)}")       
        if (DurationInStatus < 30000) {
            return 0 as Integer
        }else{
            return TimeUnit.MILLISECONDS.toMinutes(DurationInStatus)
        }
    }
//Replaced the above to populate 0 instead of NULL
//    if(DurationInStatus){
//        log.debug("Listener: TimeInStatus for ${event.getIssue()}, getTimeInStatus - DurationInStatus -- For ${statustoFind.toString()} we are returning: ${TimeUnit.MILLISECONDS.toMinutes(DurationInStatus)}")
//        return TimeUnit.MILLISECONDS.toMinutes(DurationInStatus)
//    }
}

//Create customfieldNames list
def dynamicLists(List changeItemsByStatus, ArrayList statusesArray){
    List cfNamesList = []
    for (int i = 0; i < changeItemsByStatus.size(); i++) {
        binding.variables.remove 'cfName'        
        def cfName
        ChangeItemBean item = changeItemsByStatus[i]
        //Only get collection for statuses listed in dynamic forms field
        for (int j = 0; j < statusesArray.size(); j++) {
            log.debug("Listener: TimeInStatus for ${event.getIssue()}, dynamicLists - LOOKING FOR: ${statusesArray[j].toString()} ====== in Change Item: ${item.getFromString().toString()}")
            if(statusesArray[j].toString() == item.getFromString().toString()){
                log.debug("Listener: TimeInStatus for ${event.getIssue()}, dynamicLists - Adding TIS-${item.getFromString()} (minutes) to cfNameList if it doesn't already exist.")
                cfName = "TIS-${item.getFromString()} (minutes)" 
                if(cfName){
                    log.debug("Listener: TimeInStatus for ${event.getIssue()}, dynamicLists - Checking if cfNamesList ${cfNamesList.toString()} has ${cfName} in it.")
                    if(!(cfNamesList.contains(cfName))){
                        log.debug("Listener: TimeInStatus for ${event.getIssue()}, dynamicLists - Adding ${cfName} to cfNameList.")
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
log.debug("Listener: TimeInStatus for ${event.getIssue()}, dynamicLists - *Important* - Returning ${cfNamesList.size()}, complete list: cfNamesList: ${cfNamesList.toString()}")
return cfNamesList as List
}



/*
import com.atlassian.jira.component.ComponentAccessor
import com.atlassian.jira.issue.Issue
import java.lang.String
import com.atlassian.jira.component.ComponentAccessor
import com.atlassian.jira.issue.history.ChangeItemBean
import com.atlassian.jira.issue.status.Status
import com.atlassian.jira.issue.changehistory.ChangeHistory
import com.atlassian.jira.issue.changehistory.ChangeHistoryManager
import com.onresolve.scriptrunner.parameters.annotation.*
import com.atlassian.jira.issue.ModifiedValue
import com.atlassian.jira.issue.util.DefaultIssueChangeHolder
import groovy.transform.Field
import com.atlassian.jira.issue.Issue
import java.util.concurrent.TimeUnit

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

Issue issue = event.getIssue() // get issue which triggered listeneer
def issueResolution = issue.getResolution() // get the issue's resolution field
def issueStatus = issue.getStatus() // get the current status
if(issueResolution)(issueResolution = issueResolution.name)
if(issueStatus)(issueStatus = issueStatus.name)
log.info("Listener: TimeInStatus for ${issue}, Resolution: ${issueResolution}, Status: ${issueStatus}, On Statuses: ${stringStatuses} has started.")

//Managers
def changeHistoryManager = ComponentAccessor.getChangeHistoryManager()
def customFieldManager = ComponentAccessor.customFieldManager

List statusChanges = changeHistoryManager.getChangeItemsForField(issue, "status") as List
if(statusChanges.size() <= 1){return} //This is a new issue and there is nothing to calculate
//If the issues 'from status' wasn't the one we want to capture, don't run script.
if(specifiedOnly){
    ChangeItemBean item = statusChanges[statusChanges.size()-1]
    if(!((stringStatuses).contains(item.getFromString().toString()))){
        log.warn("Listener: TimeInStatus for ${issue} is stopping because the listener field is checked for 'Only execute on issues leaving from listed statuses.' - statuses to look for: ${stringStatuses}, the previous status was: ${item.getFromString().toString()} ")
        return //The previous status wasn't what we are looking for
    }
}
if(issueResolved) {
    log.info("Listener script TimeInStatus is stopping since the listener field is checked for 'Only execute on Closed issues'.")
    if (!(issueResolution)){
        log.info("Listener script TimeInStatus is stopping since ${issue} does not have a Resolution and the project is set to only run this script on Closed issues.")
        return //Only execute script on resolved issues
    }
}

// this below is list of updates of <issue>
List changeItems = changeHistoryManager.getAllChangeItems(issue)

def length = changeItems.size() // length of this list

List cfNamesList = []
cfNamesList = dynamicLists(statusChanges, arrayStatuses) as List

setCFValues(issue, arrayStatuses, cfNamesList, statusChanges)
log.info("Listener: TimeInStatus for ${issue} has completed.")



//Functions
def setCFValues(Issue issue, List statusesArray, List cfNamesList, List Changes){
    def fieldName
    def issueCF
    def durationInStatus
    def statusToFind
    for (int i = 0; i < cfNamesList.size(); i++) {
        fieldName = cfNamesList[i]
        statusToFind = statusesArray[i]
        durationInStatus = getTimeInStatus(Changes as List, statusToFind.toString())
        if(durationInStatus){
            issueCF = ComponentAccessor.customFieldManager.getCustomFieldObjects(issue).findByName(fieldName.toString())
            if(!(issueCF)){
                log.error("Listener: TimeInStatus - issue: ${issue} - Could not find custom field with name ${fieldName}")
            }else{
                issueCF.updateValue(null, issue, new ModifiedValue(issue.getCustomFieldValue(issueCF), durationInStatus.toString()), new DefaultIssueChangeHolder())
            }
        }
    }
}
  
//Returns time in status by status
def getTimeInStatus(List statusChanges, String statustoFind){
    long durationMS
    long totalDurationMS
    Long DurationInStatus
    Long totalDurationInStatus
    for (int i = 0; i < statusChanges.size(); i++) {
        ChangeItemBean item
        ChangeItemBean nextItem
        if(statusChanges[i]) { item = statusChanges[i] }
        if(statusChanges[i+1]){ nextItem = statusChanges[i+1] }
        if(item.getToString() == statustoFind.toString()){
            if(item && nextItem){ //This means there are multiple changes
                log.info("multiple changes")
                durationMS = nextItem.getCreated().getTime() - item.getCreated().getTime()
                totalDurationMS = totalDurationMS + durationMS
            }
        log.info("totalDurationMS: ${totalDurationMS}")
        DurationInStatus = totalDurationMS
        }
    }
    if(DurationInStatus){
        log.info("TimeUnit.MILLISECONDS.toMinutes(DurationInStatus): ${TimeUnit.MILLISECONDS.toMinutes(DurationInStatus)}")
        return TimeUnit.MILLISECONDS.toMinutes(DurationInStatus)
    }
}

//Create customfieldNames list
def dynamicLists(List changeItemsByStatus, ArrayList statusesArray){
    List cfNamesList = []
    for (int i = 0; i < changeItemsByStatus.size(); i++) {
        binding.variables.remove 'cfName'        
        def cfName
        ChangeItemBean item = changeItemsByStatus[i]
        //Only get collection for statuses listed in dynamic forms field
        for (int j = 0; j < statusesArray.size(); j++) {
            if(statusesArray[j].toString() == item.getFromString().toString()){
                cfName = "TIS-${item.getFromString()} (minutes)" 
                if(cfName){
                    if(!(cfNamesList.contains(cfName))){
                        cfNamesList.add(cfName)
                    }
                }
            }
        }
    }
return cfNamesList as List
}
*/