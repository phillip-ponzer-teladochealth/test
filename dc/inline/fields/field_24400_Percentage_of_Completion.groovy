import com.atlassian.jira.issue.Issue
import com.atlassian.jira.component.ComponentAccessor
import org.apache.log4j.Logger
import org.apache.log4j.Level
import com.atlassian.jira.issue.CustomFieldManager

log = Logger.getLogger("ScriptField: PercentageOfCompletion")
log.setLevel(Level.INFO)

//Managers
def customFieldManager = ComponentAccessor.getCustomFieldManager()
//Get field name
def percentOfComp = customFieldManager.getCustomFieldObjectsByName("Percentage of Completion")
//Get a count of all sub-tasks
def totalSubTasks = issue.getSubTaskObjects().size()
log.debug("totalSubTasks on " + issue.key + ": " + totalSubTasks)
if ( ! issue.isSubTask() ) {
    //If there are no sub-tasks, fille the field with "0%"
    def result = (double)0 
    if (totalSubTasks > 0){
        def completedSubTasks = 0
        for (subTask in issue.getSubTaskObjects()){
            if (subTask.getResolution() != null){
                completedSubTasks++
            }
        }
        log.debug("completed SubTasks on " + issue.key + ": " + completedSubTasks)
        result = (double)(completedSubTasks*100) / totalSubTasks
        result = result.round(1) + "%"
        log.debug("Percentage of Completion on " + issue.key + ": " + result)
        return result
    }else{
        log.debug("There were no sub-tasks on " + issue.key) 
        result = result.round(1) + "%"
    }
return result 
}