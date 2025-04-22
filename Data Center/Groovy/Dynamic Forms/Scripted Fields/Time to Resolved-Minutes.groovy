/*Author: Jeff Melies
Purpose: Captures Time to Resolution. 
        Using the issues status categories define a start and end datetime, we search the history of the ticket 
        and calculate the time between them.  You can also exclude weekends and define the
        results in milliseconds, seconds, minutes, hours, or days.
Change log:
*/

import com.atlassian.jira.issue.status.Status
import com.atlassian.jira.config.StatusManager
import com.atlassian.jira.issue.status.category.StatusCategory
import com.atlassian.jira.component.ComponentAccessor
import com.atlassian.jira.issue.history.ChangeItemBean
import com.onresolve.scriptrunner.parameters.annotation.*
import com.atlassian.jira.issue.Issue
import java.util.concurrent.TimeUnit
import org.apache.log4j.Level
import java.time.DayOfWeek
import java.time.LocalDateTime
import java.time.ZoneId
import java.time.Instant
import  java.util.Date.*

log.setLevel(Level.INFO) // Change this to DEBUG to see log messages
//Issue issue = underlyingIssue 
def projectName = issue?.projectObject.name

@ShortTextInput(label = "Enter a Status Category to capture the 'Start time'", description = "Enter a Status Category ('To Do', 'In Progress', or 'Complete') to capture the initial datetime")
String startStatusCategory
if(!(startStatusCategory)){return null}
@ShortTextInput(label = "Enter a Status Category to capture the 'End time'", description = "Enter a Status Category ('To Do', 'In Progress', or 'Complete') to capture the end datetime")
String endStatusCategory // = "Complete"
if(!(endStatusCategory)){return null}
@ShortTextInput(label = "Display duration in (days, hours, minutes, seconds, millisconds)", description = "Enter the time unit you would like the results displayed in.")
String timeUnit
if(!(timeUnit)){return null}
@Checkbox(label = "Exclude Weekends?", description = "Check to exclude weekend days from this calculation.")
Boolean excludeWeekendDays

def changeHistoryManager = ComponentAccessor.getChangeHistoryManager()
StatusManager statusManager = ComponentAccessor.getComponentOfType(StatusManager.class)

//Only continue if the issues current status's category is equal to the given endStatusCategory listed above
if(issue?.getStatus()?.getStatusCategory()?.getName()?.toLowerCase()==endStatusCategory?.toLowerCase()){
log.debug("Scripted Field: Time to Resolved ${projectName} - has started")
    def endDateTime
    def startDateTime
    def elapsedTime
    def durationMS
    if(excludeWeekendDays){
        endDateTime = retrieveEndStatusTime(endStatusCategory, "LocalDateTime") as LocalDateTime//changeHistoryManager.getChangeItemsForField(issue, "status")?.last()
        startDateTime = retrieveStartStatusTime(startStatusCategory, "LocalDateTime") as LocalDateTime //Long
        if(endDateTime == null | startDateTime == null) {return "0"}
        startDateTime = removeWeekends(startDateTime, endDateTime) as LocalDateTime
        endDateTime = endDateTime.atZone(ZoneId.systemDefault()).toInstant().toEpochMilli() as Long 
        startDateTime = startDateTime.atZone(ZoneId.systemDefault()).toInstant().toEpochMilli() as Long
        durationMS = endDateTime - startDateTime
        if (durationMS < 30000) {
            durationMS = 0 as Long
        }else if (durationMS >= 30001 && durationMS < 60000){
            durationMS = 60000 as Long
        }
    }else{
        endDateTime = retrieveEndStatusTime(endStatusCategory, "Long") as Long
        startDateTime = retrieveStartStatusTime(startStatusCategory, "Long") as Long
        if(endDateTime == null | startDateTime == null) {return "0"}
        durationMS = endDateTime - startDateTime
        if (durationMS < 30000) {
            durationMS = 0 as Long
        }else if (durationMS >= 30001 && durationMS < 60000){
            durationMS = 60000 as Long
        }       
    }

    def myTimeFormat
    switch(timeUnit?.toLowerCase()){
        case "seconds":
            log.debug("Time to Resolved: ${TimeUnit.MILLISECONDS.toSeconds(durationMS)}")
            return TimeUnit.MILLISECONDS.toSeconds(durationMS)
        break;
        case "minutes":
            log.debug("Time to Resolved: ${TimeUnit.MILLISECONDS.toMinutes(durationMS)}")
            return TimeUnit.MILLISECONDS.toMinutes(durationMS)
        break;
        case "hours":
            log.debug("Time to Resolved: ${TimeUnit.MILLISECONDS.toMinutes(durationMS)}")
            return TimeUnit.MILLISECONDS.toMinutes(durationMS)
        break;
        case "days":
            log.debug("Time to Resolved: ${TimeUnit.MILLISECONDS.toDays(durationMS)}")
            return TimeUnit.MILLISECONDS.toDays(durationMS)
        break;
        default:
            log.debug("Time to Resolved: ${durationMS}")
            return durationMS
        break;
    }  
}
log.debug("Scripted Field: Time to Resolved ${projectName} - has completed.")


def removeWeekends(LocalDateTime firstDate, LocalDateTime secondDate){
    def weekend = EnumSet.of(DayOfWeek.SATURDAY, DayOfWeek.SUNDAY)
    LocalDateTime myStartDate = firstDate
    def workingDays = 0
    def weekendDays = 0
    while (firstDate.isBefore(secondDate)) {
        if (!(firstDate.dayOfWeek in weekend)) {
            workingDays += 1
        }else{
            weekendDays += 1
        }
        firstDate = firstDate.plusDays(1)
    }
    myStartDate = myStartDate.plusDays(weekendDays)
    return myStartDate
}

def retrieveStartStatusTime(String startStatusCat, String returnType){
    def allStatusChanges = ComponentAccessor.getChangeHistoryManager().getChangeItemsForField(issue as Issue, "status")
    for (int i = 0; i < allStatusChanges.size(); i++) {
        ChangeItemBean item = allStatusChanges[i]
        //log.debug("ITEM: ${item.toString()}" )
        def itemStatus = item.getToString()
        def foundStatus = ComponentAccessor.constantsManager.getStatusByNameIgnoreCase(item.getToString()) //Gets status object
        if(foundStatus?.getStatusCategory()?.getName()?.toLowerCase() == startStatusCat?.toLowerCase()){
            if(returnType == "Long"){
                log.debug("startStatus Return Long: ${item.getCreated().getTime()}")
                return item.getCreated().getTime()
            }else{
                log.debug("startStatus Return LocalDateTime: ${item.getCreated().toLocalDateTime()}")
                return item.getCreated().toLocalDateTime()
            }
        }
    }
}
def retrieveEndStatusTime(String endStatus, String returnType){
    def allStatusChanges = ComponentAccessor.getChangeHistoryManager().getChangeItemsForField(issue as Issue, "status")?.reverse() 
    //List statuses = ComponentAccessor.getComponentOfType(StatusManager.class).getStatuses()*.getName()
    for (int i = 0; i < allStatusChanges.size(); i++) {
        ChangeItemBean item = allStatusChanges[i]
        def itemStatus = item.getToString()
        def foundStatus = ComponentAccessor.constantsManager.getStatusByNameIgnoreCase(item.getToString()) //Gets status object
        if(foundStatus?.getStatusCategory()?.getName()?.toLowerCase() == endStatus?.toLowerCase()){
            if(returnType == "Long"){
                log.debug("endStatus Return Long: ${item.getCreated().getTime()}")
                return item.getCreated().getTime()
            }else{
                log.debug("endStatus Return LocalDateTime: ${item.getCreated().toLocalDateTime()}")
                return item.getCreated().toLocalDateTime()
            }
        }
    }
}
def definedArrayList(String myString){
    if(myString.contains(',')){
        String[] elements = myString?.toLowerCase().split(",")
        List<String> listArray = Arrays.asList(elements)
        ArrayList<String> myNewArrayList = new ArrayList<String>(listArray)
        for (i in 0..<myNewArrayList.size()) {
            myNewArrayList[i] = myNewArrayList[i].trim()
        }
        return myNewArrayList
    }else{
        List<String> myNewArrayList = Arrays.asList(myString?.toLowerCase().trim())
        return myNewArrayList
    }
}
