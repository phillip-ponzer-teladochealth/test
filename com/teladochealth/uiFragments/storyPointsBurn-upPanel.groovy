package com.teladochealth.uiFragments

/*
Author: Jeff Melies
Purpose: Create a Burn-up panel shown on the right panel of Epic Issues to show
         Story Points used and remaining.  These fields will not be able to be queried 
        by JQL.
Setup: For now to add additional project you must add them to the 'Condition' statement of the 'UI Fragment'
       See me with any questions. This is also setup to find Story Points like Story Point Cumlative Scripted Field
*/
import java.lang.Integer
import com.atlassian.jira.issue.CustomFieldManager
import com.atlassian.jira.component.ComponentAccessor
import com.atlassian.jira.issue.Issue
import com.atlassian.jira.issue.link.IssueLinkManager
import com.atlassian.jira.config.SubTaskManager
import com.atlassian.jira.issue.fields.CustomField
import com.atlassian.jira.issue.status.Status
import com.atlassian.jira.issue.status.category.StatusCategory
import groovy.transform.Field as tField
import com.atlassian.jira.issue.issuetype.IssueType
import com.onresolve.scriptrunner.parameters.annotation.IssueTypePicker
import com.atlassian.jira.issue.fields.Field
import com.onresolve.scriptrunner.parameters.annotation.*
import org.apache.log4j.Logger
import org.apache.log4j.Level

Issue issue = context?.issue as Issue
Logger log = Logger.getLogger("(${if(context?.issue){context?.issue}else{context?.project?.toString()?.split(": ")[1]?.trim()}}) Fragments_StoryBurnUp:") as Logger
log.setLevel(Level.INFO)

@FieldPicker(label = "Feature Field", description = "Select the customfield that holds the story points for the Features children.")
Field featurePointField

@FieldPicker(label = "Epic Field", description = "Select the customfield that holds the story points for the Epic children.")
Field epicPointField

@IssueTypePicker(label = 'Issue type', description = 'Select the highest issue type (Like Epic, Initiative, or Portfolio) for the selected project.', placeholder = 'Select the highest issue type usually Epic or Portfolio')
IssueType issueType

@IssueTypePicker(
    label = 'Exclude issue type(s)', description = 'Select 1 or more issue types to exclude from calculation', placeholder = 'Select 1 or more issue types to exclude from calculation',
    multiple = true
)
//List<IssueType> issueTypes
List<IssueType> excludeIssueTypes
List excludeIssueTypeNames = []
excludeIssueTypes.each{
    excludeIssueTypeNames.add(it.getName().toString())
}

IssueLinkManager issueLinkManager = ComponentAccessor.getIssueLinkManager()
CustomFieldManager customFieldManager = ComponentAccessor.getCustomFieldManager()
SubTaskManager subTaskManager = ComponentAccessor.getSubTaskManager()

long sumOfStoryPoints = 0
long sumOfRemainingStoryPoints = 0
long spEstimate = 0

Issue myLinks

int storyPoints = 0

//Globals
@tField String issue_Types = "Feature, Epic"
@tField String formattedPercentageComplete
@tField Field issuesField

@tField int countOfEpics = 0
@tField int countOfStories = 0 
@tField int countOfCanceledStories = 0 
    
@tField int totalStoryPoints = 0
@tField int totalIssuesWO_Points = 0
    
@tField int countOfToDoStories = 0 
@tField int countOfInProgressStories = 0
@tField int countOfDoneStories = 0
    
@tField int countOfToDoEpics=0
@tField int countOfInProgressEpics=0
@tField int countOfDoneEpics=0
@tField int countOfCanceledEpics=0

@tField int countOfToDoStoryPoints = 0
@tField int countOfToDo_WO_Points = 0 
@tField int countOfInProgressStoryPoints = 0
@tField int countOfInProgress_WO_Points = 0
@tField int countOfDoneStoryPoints = 0
@tField int countOfCanceledStoryPoints = 0
@tField int countOfDone_WO_Points = 0
@tField int countOfCanceled_WO_Points = 0

def percentage = 0
def issuePercentage = 0
def epicPercentage = 0
String formattedEpicPercentage
String formattedIssuePercentage
CustomField cfStoryPoints, cfStoryPointsCumulative
String issueStatusCategory, jqlSearch

def baseurl = com.atlassian.jira.component.ComponentAccessor.getApplicationProperties().getString("jira.baseurl")

switch(issue.getIssueType().name.toString().toLowerCase()){
    case "feature":
        issuesField = featurePointField
        jqlSearch = "issueFunction in portfolioChildrenOf('key = ${issue.key}')"
    break;
    case "epic":
        issuesField = epicPointField
        jqlSearch = "issueFunction in issuesInEpics('key = ${issue.key}')"
    break;
    default:
        issuesField = epicPointField
        jqlSearch = "issueFunction in issuesInEpics('key = ${issue.key}')"
    break;
}


log.debug("${issue.key} is a ${issue.getIssueType().name.toString()}")

Issues?.search(jqlSearch)?.each { myLink ->
    if(!excludeIssueTypeNames.contains(myLink?.getIssueType()?.name)){
        if(myLink?.getCustomFieldValue(issuesField as CustomField) != null){
            storyPoints = (int)myLink?.getCustomFieldValue(issuesField as CustomField)
        }else{
            storyPoints = 0
        }
        issueStatusCategory = myLink?.getStatus()?.getStatusCategory()?.getName()?.toString()
        if(! storyPoints){log.debug("1. sumStoryPoints - ${myLink?.key}")} 
        if(storyPoints < 1){log.debug("2. sumStoryPoints - ${myLink?.key}")}
        sumStoryPoints(storyPoints, myLink?.getIssueType()?.getName(), issueStatusCategory, myLink?.status?.name )
    }else{
        log.debug("${myLink} - IssueType: ${myLink?.getIssueType().name} is on the excluded list ${excludeIssueTypeNames}, skipping the linked issue.")
    }
}
//Get the total Story Points
totalStoryPoints = countOfToDoStoryPoints + countOfInProgressStoryPoints + countOfDoneStoryPoints

if( totalStoryPoints > 0 && countOfDoneStoryPoints > 0) {
    percentage =  (countOfDoneStoryPoints / totalStoryPoints ) * 100    
    formattedPercentageComplete = String.format( "%.1f", percentage ) + "%";
}
else {
    formattedPercentageComplete = "0%"
}

log.debug("${( totalStoryPoints > 0 && countOfDoneStoryPoints > 0)}, percentage: ${percentage}, formattedPercentageComplete: ${formattedPercentageComplete} -- totalStoryPoints: ${totalStoryPoints}, countOfToDoStoryPoints: ${countOfToDoStoryPoints}, countOfInProgressStoryPoints: ${countOfInProgressStoryPoints}, countOfDoneStoryPoints: ${countOfDoneStoryPoints}")

if(countOfToDo_WO_Points || countOfInProgress_WO_Points || countOfDone_WO_Points){
    totalIssuesWO_Points = countOfToDo_WO_Points + countOfInProgress_WO_Points + countOfDone_WO_Points
}

writer.write compileHTML(issue as Issue, issuesField).toString()

//******************************************************FUNCTIONS*****************************************************

def sumStoryPoints(int storyPoints, String issueTypeName, String issueStatusCategory, String issueStatusName ) {
    Logger log = Logger.getLogger("Fragments_StoryBurnUp:")
    log.setLevel(Level.INFO)
    //Get the counts 
    switch(issueStatusCategory.toLowerCase()){
        case "new":
            if(storyPoints < 1){
                countOfToDo_WO_Points += 1
            }else{
                countOfToDoStories += 1
                countOfStories += 1           
                countOfToDoStoryPoints += storyPoints
            }
        break;
        case "in progress":
            if(storyPoints < 1){
                countOfInProgress_WO_Points += 1
            }else{
                countOfInProgressStories += 1
                countOfStories += 1         
                countOfInProgressStoryPoints += storyPoints
            }
        break;
        case"complete":
            if( issueStatusName != "Canceled" ){
                if(storyPoints < 1){
                    countOfDone_WO_Points += 1
                }else{
                    countOfDoneStories += 1 
                    countOfStories += 1          
                    countOfDoneStoryPoints += storyPoints
                }
            }else{
                if(storyPoints < 1){   
                    countOfCanceled_WO_Points += 1        
                }else{
                    countOfCanceledStories += 1
                    countOfCanceledStoryPoints += storyPoints 
                }
            }
        break;
        default:
        break;
    }     
}

def compileHTML(Issue issue, Field issuesField){
    Logger log = Logger.getLogger("Fragments_StoryBurnUp:")
    log.setLevel(Level.INFO)
    String strHTML, type, title
    type = issue?.getIssueType()?.getName().toLowerCase()
    log.debug("Finishing up.  formattedPercentageComplete: ${formattedPercentageComplete}")
    if(type == "epic"){
        title = "Story Points Burn-up %: " + formattedPercentageComplete
    }else{
        title = "Story Points Cumulative Burn-up %: " + formattedPercentageComplete
    }
    strHTML = """<div style='width:90%'>"""// id=burn-up-view class='module toggle-wrap '>"""
    strHTML += """<div class='mod-content'>"""
    strHTML += """<div style=\"line-height:0px; text-align:center\">Story Point Cumulative % : <progress style=\"color:green\" value=\"""" + countOfDoneStoryPoints + """\" max=\"""" + totalStoryPoints + """\" title='""" + title + """'></progress>"""
    strHTML += """<a><small> (${formattedPercentageComplete})</small></a>"""
    strHTML += "</div>"
    strHTML += """<table border='1' style='border-collapse:collapse; width:100%;'>"""
    strHTML += """<tr>"""
    strHTML += """<th style='line-height:.9rem'><center>Status<br>Category</center></th>"""
    strHTML += """<th style='line-height:.9rem'><center>Not<br>Estimated</center></th>"""
    strHTML += """<th style='line-height:.9rem'><center>Estimated</center></th>"""

    if(type == "epic"){
        strHTML += """<th style='line-height:.9rem'><center>Story<br>Points</center></th>"""
        strHTML += """</tr>"""
        strHTML += """<tr>"""
        strHTML += """<td><div style='padding: 1px;align:left;text-indent:3%'><a href='/issues/?jql=issueFunction in issuesInEpics("key = ${issue.key}") and statusCategory = "To Do"' target="_blank"><u>To Do</u></a></div></td>"""
        strHTML += """<td align='center'><a href='/issues/?jql=issueFunction in issuesInEpics("key = ${issue.key}") and statusCategory = "To Do" and "Story Points" is EMPTY' target="_blank"><u>${countOfToDo_WO_Points}</u></a></td>"""
        strHTML += """<td align='center'><a href='/issues/?jql=issueFunction in issuesInEpics("key = ${issue.key}") and statusCategory = "To Do" and "Story Points" is not EMPTY' target="_blank"><u>${countOfToDoStories}</u></a></td>"""
        strHTML += """<td align='center'><a href='/issues/?jql=issueFunction in issuesInEpics("key = ${issue.key}") and statusCategory = "To Do" and "Story Points" is not EMPTY' target="_blank"><u>${countOfToDoStoryPoints}</u></a></td>"""
        strHTML += """</tr>"""

        strHTML += """<tr>"""
        strHTML += """<td><div style='padding: 1px;align:left;text-indent:3%'><a href='/issues/?jql=issueFunction in issuesInEpics("key = ${issue.key}") and statusCategory = "In Progress"' target="_blank"><u>In Progress</u></a></div></td>"""
        strHTML += """<td align='center'><a href='/issues/?jql=issueFunction in issuesInEpics("key = ${issue.key}") and statusCategory = "In Progress" and "Story Points" is EMPTY' target="_blank"><u>${countOfInProgress_WO_Points}</u></a></td>"""
        strHTML += """<td align='center'><a href='/issues/?jql=issueFunction in issuesInEpics("key = ${issue.key}") and statusCategory = "In Progress" and "Story Points" is not EMPTY' target="_blank"><u>${countOfInProgressStories}</u></a></td>"""
        strHTML += """<td align='center'><a href='/issues/?jql=issueFunction in issuesInEpics("key = ${issue.key}") and statusCategory = "In Progress" and "Story Points" is not EMPTY' target="_blank"><u>${countOfInProgressStoryPoints}</u></a></td>"""
        strHTML += """</tr>"""

        strHTML += """<tr>"""
        strHTML += """<td><div style='padding: 1px;align:left;text-indent:3%'><a href='/issues/?jql=issueFunction in issuesInEpics("key = ${issue.key}") and statusCategory = "Done" and status != "Canceled"' target="_blank"><u>Complete</u></a></div></td>"""
        strHTML += """<td align='center'><a href='/issues/?jql=issueFunction in issuesInEpics("key = ${issue.key}") and statusCategory = "Done" and status != "Canceled" and "Story Points" is EMPTY' target="_blank"><u>${countOfDone_WO_Points}</u></a></td>"""
        strHTML += """<td align='center'><a href='/issues/?jql=issueFunction in issuesInEpics("key = ${issue.key}") and statusCategory = "Done" and status != "Canceled" and "Story Points" is not EMPTY' target="_blank"><u>${countOfDoneStories}</u></a></td>"""
        strHTML += """<td align='center'><a href='/issues/?jql=issueFunction in issuesInEpics("key = ${issue.key}") and statusCategory = "Done" and status != "Canceled" and "Story Points" is not EMPTY' target="_blank"><u>${countOfDoneStoryPoints}</u></a></td>"""
        strHTML += """</tr>"""

        strHTML += """<tr>"""
        strHTML += """<td><div style='padding: 1px;text-indent:10%'><a href='/issues/?jql=issueFunction in issuesInEpics("key = ${issue.key}") and status = "Canceled"' target="_blank">- <u>Canceled</u></a></div></td>"""
        strHTML += """<td align='center'><a href='/issues/?jql=issueFunction in issuesInEpics("key = ${issue.key}") and statusCategory = "Done" and status = "Canceled" and "Story Points" is EMPTY' target="_blank"><u>${countOfCanceled_WO_Points}</u></a></td>"""
        strHTML += """<td align='center'><a href='/issues/?jql=issueFunction in issuesInEpics("key = ${issue.key}") and status = "Canceled"' target="_blank"><u>${countOfCanceledStories}</u></a></td>"""
        strHTML += """<td align='center'><a href='/issues/?jql=issueFunction in issuesInEpics("key = ${issue.key}") and statusCategory = "Done" and status = "Canceled" and "Story Points" is not EMPTY' target="_blank"><u>${countOfCanceledStoryPoints}</u></a></td>"""
        strHTML += """</tr>"""

        strHTML += """<tr>"""
        strHTML += """<td><div style='padding: 1px;align:left;text-indent:3%'><b>Totals</b><small> (Issues in Epic)</small></div></td>"""
        strHTML += """<td align='center'><a href='/issues/?jql=issueFunction in issuesInEpics("key = ${issue.key}") and "Story Points" is EMPTY' target="_blank"><u>${totalIssuesWO_Points + countOfCanceled_WO_Points}</u></a></td>"""
        strHTML += """<td align='center'><a href='/issues/?jql=issueFunction in issuesInEpics("key = ${issue.key}") and "Story Points" is not EMPTY' target="_blank"><u>${countOfStories + countOfCanceledStories}</u></a></td>"""
        strHTML += """<td align='center'><a href='/issues/?jql=issueFunction in issuesInEpics("key = ${issue.key}")' target="_blank"><u>${totalStoryPoints + countOfCanceledStoryPoints}</u></a></td>"""
    }else{
        strHTML += """<th style='line-height:.9rem'><center>Story Pts<br>Cumulative</center></th>"""
        strHTML += """</tr>"""
        strHTML += """<tr>"""
        strHTML += """<td><div style='padding: 1px;align:left;text-indent:3%'><a href='/issues/?jql=issueFunction in portfolioChildrenOf("key = ${issue.key}") and statusCategory = "To Do"' target="_blank"><u>To Do</u></a></div></td>"""
        strHTML += """<td align='center'><span>${countOfToDo_WO_Points}</span></td>"""
        strHTML += """<td align='center'><span>${countOfToDoStories}</span></td>"""
        strHTML += """<td align='center'><span>${countOfToDoStoryPoints}</span></td>"""
        strHTML += """</tr>"""

        strHTML += """<tr>"""
        strHTML += """<td><div style='padding: 1px;align:left;text-indent:3%'><a href='/issues/?jql=issueFunction in portfolioChildrenOf("key = ${issue.key}") and statusCategory = "In Progress"' target="_blank"><u>In Progress</u></a></div></td>"""
        strHTML += """<td align='center'><span>${countOfInProgress_WO_Points}</span></td>"""
        strHTML += """<td align='center'><span>${countOfInProgressStories}</span></td>"""
        strHTML += """<td align='center'><span>${countOfInProgressStoryPoints}</span></td>"""
        strHTML += """</tr>"""

        strHTML += """<tr>"""
        strHTML += """<td><div style='padding: 1px;align:left;text-indent:3%'><a href='/issues/?jql=issueFunction in portfolioChildrenOf("key = ${issue.key}") and statusCategory = "Done" and status != "Canceled"' target="_blank"><u>Complete</u></a></div></td>"""
        strHTML += """<td align='center'><span>${countOfDone_WO_Points}</span></td>"""
        strHTML += """<td align='center'><span>${countOfDoneStories}</span></td>"""
        strHTML += """<td align='center'><span>${countOfDoneStoryPoints}</span></td>"""
        strHTML += """</tr>"""

        strHTML += """<tr>"""
        strHTML += """<td><div style='padding: 1px;text-indent:10%'><a href='/issues/?jql=issueFunction in portfolioChildrenOf("key = ${issue.key}") and status = "Canceled"' target="_blank">- <u>Canceled</u></a></div></td>"""
        strHTML += """<td align='center'><span>${countOfCanceled_WO_Points}</span></td>"""
        strHTML += """<td align='center'><span>${countOfCanceledStories}</span></td>"""
        strHTML += """<td align='center'><span>${countOfCanceledStoryPoints}</span></td>"""
        strHTML += """</tr>"""

        strHTML += """<tr>"""
        strHTML += """<td><div style='padding: 1px;align:left;text-indent:3%'><b>Totals</b><small> (Issues in Feature)</small></div></td>"""
        strHTML += """<td align='center'><span>${totalIssuesWO_Points + countOfCanceled_WO_Points}</span></td>"""
        strHTML += """<td align='center'><span>${countOfStories + countOfCanceledStories}</span></td>"""
        strHTML += """<td align='center'><span>${totalStoryPoints + countOfCanceledStoryPoints}</span></td>"""        
    }
    strHTML += """</tr>"""
    strHTML += """</table>"""
    strHTML += """</div>"""	
    strHTML += """</div>"""

    return strHTML.toString()
}

//JBM: Replaced on 10/21/24 to include Feature Issue Types
//Author: Jeff Melies
//Purpose: Create a Burn-up panel shown on the right panel of Epic Issues to show
//         Story Points used and remaining.  These fields will not be able to be queried 
//        by JQL.
//Setup: For now to add additional project you must add them to the 'Condition' statement of the 'UI Fragment'
//       See me with any questions. This is also setup to find Story Points like Story Point Cumlative Scripted Field
/*
import java.lang.Integer
import com.atlassian.jira.issue.CustomFieldManager
import com.atlassian.jira.component.ComponentAccessor
import com.atlassian.jira.issue.Issue
import com.atlassian.jira.issue.link.IssueLinkManager
import com.atlassian.jira.config.SubTaskManager
import com.atlassian.jira.issue.fields.CustomField
import com.atlassian.jira.issue.status.Status
import com.atlassian.jira.issue.status.category.StatusCategory
import groovy.transform.Field as tField
import com.atlassian.jira.issue.issuetype.IssueType
import com.onresolve.scriptrunner.parameters.annotation.IssueTypePicker
import com.atlassian.jira.issue.fields.Field
import com.onresolve.scriptrunner.parameters.annotation.*
import org.apache.log4j.Logger
import org.apache.log4j.Level

Issue issue = context?.issue as Issue
Logger log = Logger.getLogger("(${if(context?.issue){context?.issue}else{context?.project?.toString()?.split(": ")[1]?.trim()}}) Fragments_StoryBurnUp:")
log.setLevel(Level.INFO)

@FieldPicker(label = "Field", description = "Select the customfield that holds the story points for the project")
Field intPoints

@IssueTypePicker(label = 'Issue type', description = 'Select the highest issue type (Like Epic, Initiative, or Portfolio) for the selected project.', placeholder = 'Select the highest issue type usually Epic or Portfolio')
IssueType issueType

@IssueTypePicker(
    label = 'Exclude issue type(s)', description = 'Select 1 or more issue types to exclude from calculation', placeholder = 'Select 1 or more issue types to exclude from calculation',
    multiple = true
)
//List<IssueType> issueTypes
List<IssueType> excludeIssueTypes
List excludeIssueTypeNames = []
excludeIssueTypes.each{
    excludeIssueTypeNames.add(it.getName().toString())
}

IssueLinkManager issueLinkManager = ComponentAccessor.getIssueLinkManager()
CustomFieldManager customFieldManager = ComponentAccessor.getCustomFieldManager()
SubTaskManager subTaskManager = ComponentAccessor.getSubTaskManager()

long sumOfStoryPoints = 0
long sumOfRemainingStoryPoints = 0
long spEstimate = 0

Issue myLinks

int storyPoints = 0

//Globals
@tField int countOfEpics = 0
@tField int countOfStories = 0 
@tField int countOfCanceledStories = 0 
    
@tField int totalStoryPoints = 0
@tField int totalIssuesWO_Points = 0
    
@tField int countOfToDoStories = 0 
@tField int countOfInProgressStories = 0
@tField int countOfDoneStories = 0
    
@tField int countOfToDoEpics=0
@tField int countOfInProgressEpics=0
@tField int countOfDoneEpics=0
@tField int countOfCanceledEpics=0

@tField int countOfToDoStoryPoints = 0
@tField int countOfToDo_WO_Points = 0 
@tField int countOfInProgressStoryPoints = 0
@tField int countOfInProgress_WO_Points = 0
@tField int countOfDoneStoryPoints = 0
@tField int countOfCanceledStoryPoints = 0
@tField int countOfDone_WO_Points = 0
@tField int countOfCanceled_WO_Points = 0

String strHTML
def percentage = 0
def issuePercentage = 0
def epicPercentage = 0
String formattedPercentageComplete
String formattedEpicPercentage
String formattedIssuePercentage
CustomField cfPoints
CustomField cfStoryPoints
String issueStatusCategory

String epicName = issue.getCustomFieldValue("Epic Name")
def baseurl = com.atlassian.jira.component.ComponentAccessor.getApplicationProperties().getString("jira.baseurl")

CustomFieldManager myCustomFieldManager = ComponentAccessor.getCustomFieldManager()

log.debug("Feature Progress started on ${if(issue){issue?.key}else{'a New Issue'}}")

def sumStoryPoints(int storyPoints, String issueTypeName, String issueStatusCategory, String issueStatusName ) {
    //Get the counts    
    if ( issueStatusCategory == "New" ) {
        if(storyPoints < 1){
            countOfToDo_WO_Points += 1
        }else{
            countOfToDoStories += 1
            countOfStories += 1           
            countOfToDoStoryPoints += storyPoints
        }
    }
    else if ( issueStatusCategory == "In Progress" ) {
        if(storyPoints < 1){
            countOfInProgress_WO_Points += 1
        }else{
            countOfInProgressStories += 1
            countOfStories += 1         
            countOfInProgressStoryPoints += storyPoints
        }
    }
    else if ( issueStatusCategory == "Complete" ){
        if( issueStatusName != "Canceled" ){
            if(storyPoints < 1){
                countOfDone_WO_Points += 1
            }else{
                countOfDoneStories += 1 
                countOfStories += 1          
                countOfDoneStoryPoints += storyPoints
            }
        }else{
            if(storyPoints < 1){   
                countOfCanceled_WO_Points += 1        
            }else{
                countOfCanceledStories += 1
                countOfCanceledStoryPoints += storyPoints 
            }
        }
    }   
}

//Get the estimated (Story Points)
cfPoints = myCustomFieldManager?.getCustomFieldObjects( issue )?.find {it?.name == intPoints?.name} 
if ( cfPoints ) {
    if ( issue?.getCustomFieldValue(cfPoints) != null ) {
        spEstimate = (int)issue?.getCustomFieldValue(cfPoints)
    }
    else {
        spEstimate = 0
    }
}
log.debug("WebItem - cfPoints - spEstimate: ${spEstimate}")
//Loop through all linked issues associated with this ticket. 
if(epicName == null || epicName == ""){return "Epic Name is empty."}
//String jqlSearch = "\"Epic Link\" = \"${epicName?.replaceAll("\"","\\\\\"")?.replace("\t", "")}\""
String jqlSearch = "issueFunction in issuesInEpics('key = ${issue.key}')"
log.debug("jqlSearch: ${jqlSearch}")
Issues?.search(jqlSearch)?.each { myLink ->
    log.debug("my new linked issue: ${myLink?.getKey()}")
    Issue issueObj = Issues?.getByKey("${myLink?.getKey()}")
    log.debug("linkedIssue: ${issueObj}, ${issueObj?.getIssueType()?.name}")
    if(!excludeIssueTypeNames.contains(issueObj?.getIssueType()?.name)){
        log.debug("${issueObj} - ${excludeIssueTypeNames} does not contain (${issueObj?.getIssueType().name}), so we will check for the field ${cfPoints}.")
        storyPoints = issueObj?.getCustomFieldValue(cfPoints) as Integer
        log.warn("(${myLink?.getKey()}) - storyPoints: ${storyPoints}")
        if(issueObj?.getCustomFieldValue(cfPoints) != null){
            storyPoints = (int)issueObj?.getCustomFieldValue(cfPoints)
        }else{
            storyPoints = 0
        }
        issueStatusCategory = issueObj?.getStatus()?.getStatusCategory()?.getName()?.toString()
        sumStoryPoints(storyPoints, issueObj?.getIssueType()?.getName(), issueStatusCategory, issueObj?.status?.name )
        log.debug("WebItem - issueLinkManager on ${myLink?.getKey()} - issueStatusCategory: ${issueStatusCategory}")
    
    }else{
        log.debug("${issueObj} - IssueType: ${issueObj?.getIssueType().name} is on the excluded list ${excludeIssueTypeNames}, skipping the linked issue.")
    }
}
//Get the total Story Points
totalStoryPoints = countOfToDoStoryPoints + countOfInProgressStoryPoints + countOfDoneStoryPoints

if( totalStoryPoints > 0 && countOfDoneStoryPoints > 0) {
    percentage =  (countOfDoneStoryPoints / totalStoryPoints ) * 100    
    formattedPercentageComplete = String.format( "%.1f", percentage ) + "%";
}
else {
    formattedPercentageComplete = "0%"
}

log.debug("Calculate percentage: countOfStories:${countOfStories}, countOfDoneStories: ${countOfDoneStories}")
//Calculate percentage Issues closed.
if ( countOfStories > 0 && countOfDoneStories > 0 ) {
    issuePercentage = (countOfDoneStories / countOfStories ) * 100
    formattedIssuePercentage = String.format( "%.1f", issuePercentage ) + "%";
}
else {
    formattedIssuePercentage = "0%"
}

if(countOfToDo_WO_Points || countOfInProgress_WO_Points || countOfDone_WO_Points){
    totalIssuesWO_Points = countOfToDo_WO_Points + countOfInProgress_WO_Points + countOfDone_WO_Points
}

log.debug("Finishing up")
String title = "Story Points Burn-up %: " + formattedPercentageComplete
strHTML = """<div style='width:90%'>"""// id=burn-up-view class='module toggle-wrap '>"""
strHTML += """<div class='mod-content'>"""
strHTML += """<div style=\"line-height:0px; text-align:center\">Story Points % : <progress style=\"color:green\" value=\"""" + countOfDoneStoryPoints + """\" max=\"""" + totalStoryPoints + """\" title='""" + title + """'></progress>"""
strHTML += """<a><small> (${formattedPercentageComplete})</small></a>"""
strHTML += "</div>"
strHTML += """<table border='1' style='border-collapse:collapse; width:100%;'>"""
strHTML += """<tr>"""
strHTML += """<th style='line-height:.9rem'><center>Status<br>Category</center></th>"""
strHTML += """<th style='line-height:.9rem'><center>Not<br>Estimated</center></th>"""
strHTML += """<th style='line-height:.9rem'><center>Estimated</center></th>"""
strHTML += """<th style='line-height:.9rem'><center>Story<br>Points</center></th>"""
strHTML += """</tr>"""

strHTML += """<tr>"""
strHTML += """<td><div style='padding: 1px;align:left;text-indent:3%'><a href='/issues/?jql=issueFunction in issuesInEpics("key = ${issue.key}") and statusCategory = "To Do"' target="_blank"><u>To Do</u></a></div></td>"""
strHTML += """<td align='center'><a href='/issues/?jql=issueFunction in issuesInEpics("key = ${issue.key}") and statusCategory = "To Do" and "Story Points" is EMPTY' target="_blank"><u>${countOfToDo_WO_Points}</u></a></td>"""
strHTML += """<td align='center'><a href='/issues/?jql=issueFunction in issuesInEpics("key = ${issue.key}") and statusCategory = "To Do" and "Story Points" is not EMPTY' target="_blank"><u>${countOfToDoStories}</u></a></td>"""
strHTML += """<td align='center'><a href='/issues/?jql=issueFunction in issuesInEpics("key = ${issue.key}") and statusCategory = "To Do" and "Story Points" is not EMPTY' target="_blank"><u>${countOfToDoStoryPoints}</u></a></td>"""
strHTML += """</tr>"""

strHTML += """<tr>"""
strHTML += """<td><div style='padding: 1px;align:left;text-indent:3%'><a href='/issues/?jql=issueFunction in issuesInEpics("key = ${issue.key}") and statusCategory = "In Progress"' target="_blank"><u>In Progress</u></a></div></td>"""
strHTML += """<td align='center'><a href='/issues/?jql=issueFunction in issuesInEpics("key = ${issue.key}") and statusCategory = "In Progress" and "Story Points" is EMPTY' target="_blank"><u>${countOfInProgress_WO_Points}</u></a></td>"""
strHTML += """<td align='center'><a href='/issues/?jql=issueFunction in issuesInEpics("key = ${issue.key}") and statusCategory = "In Progress" and "Story Points" is not EMPTY' target="_blank"><u>${countOfInProgressStories}</u></a></td>"""
strHTML += """<td align='center'><a href='/issues/?jql=issueFunction in issuesInEpics("key = ${issue.key}") and statusCategory = "In Progress" and "Story Points" is not EMPTY' target="_blank"><u>${countOfInProgressStoryPoints}</u></a></td>"""
strHTML += """</tr>"""

strHTML += """<tr>"""
strHTML += """<td><div style='padding: 1px;align:left;text-indent:3%'><a href='/issues/?jql=issueFunction in issuesInEpics("key = ${issue.key}") and statusCategory = "Done" and status != "Canceled"' target="_blank"><u>Complete</u></a></div></td>"""
strHTML += """<td align='center'><a href='/issues/?jql=issueFunction in issuesInEpics("key = ${issue.key}") and statusCategory = "Done" and status != "Canceled" and "Story Points" is EMPTY' target="_blank"><u>${countOfDone_WO_Points}</u></a></td>"""
strHTML += """<td align='center'><a href='/issues/?jql=issueFunction in issuesInEpics("key = ${issue.key}") and statusCategory = "Done" and status != "Canceled" and "Story Points" is not EMPTY' target="_blank"><u>${countOfDoneStories}</u></a></td>"""
strHTML += """<td align='center'><a href='/issues/?jql=issueFunction in issuesInEpics("key = ${issue.key}") and statusCategory = "Done" and status != "Canceled" and "Story Points" is not EMPTY' target="_blank"><u>${countOfDoneStoryPoints}</u></a></td>"""
strHTML += """</tr>"""

strHTML += """<tr>"""
strHTML += """<td><div style='padding: 1px;text-indent:10%'><a href='/issues/?jql=issueFunction in issuesInEpics("key = ${issue.key}") and status = "Canceled"' target="_blank">- <u>Canceled</u></a></div></td>"""
//strHTML += """<td align='center'>-</td>"""
strHTML += """<td align='center'><a href='/issues/?jql=issueFunction in issuesInEpics("key = ${issue.key}") and statusCategory = "Done" and status = "Canceled" and "Story Points" is EMPTY' target="_blank"><u>${countOfCanceled_WO_Points}</u></a></td>"""
strHTML += """<td align='center'><a href='/issues/?jql=issueFunction in issuesInEpics("key = ${issue.key}") and status = "Canceled"' target="_blank"><u>${countOfCanceledStories}</u></a></td>"""
//countOfCanceled_WO_Points
strHTML += """<td align='center'><a href='/issues/?jql=issueFunction in issuesInEpics("key = ${issue.key}") and statusCategory = "Done" and status = "Canceled" and "Story Points" is not EMPTY' target="_blank"><u>${countOfCanceledStoryPoints}</u></a></td>"""
//strHTML += """<td align='center'>-</td>"""
strHTML += """</tr>"""

strHTML += """<tr>"""
strHTML += """<td><div style='padding: 1px;align:left;text-indent:3%'><b>Totals</b><small> (Issues in Epic)</small></div></td>"""
strHTML += """<td align='center'><a href='/issues/?jql=issueFunction in issuesInEpics("key = ${issue.key}") and "Story Points" is EMPTY' target="_blank"><u>${totalIssuesWO_Points + countOfCanceled_WO_Points}</u></a></td>"""
strHTML += """<td align='center'><a href='/issues/?jql=issueFunction in issuesInEpics("key = ${issue.key}") and "Story Points" is not EMPTY' target="_blank"><u>${countOfStories + countOfCanceledStories}</u></a></td>"""
strHTML += """<td align='center'><a href='/issues/?jql=issueFunction in issuesInEpics("key = ${issue.key}")' target="_blank"><u>${totalStoryPoints + countOfCanceledStoryPoints}</u></a></td>"""
strHTML += """</tr>"""
strHTML += """</table>"""
strHTML += """</div>"""	
strHTML += """</div>"""

writer.write strHTML
*/