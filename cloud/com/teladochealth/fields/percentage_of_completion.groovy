import java.text.DecimalFormat

def hapiIssue = Issues.getByKey(issue.key.toString())
if( hapiIssue.isSubTask() ) {
    return "0%"
}

def issueSubTasks = hapiIssue.getSubtasks()
if( !issueSubTasks ) {
    return "0%"
}

int completedSubTasks = 0
for( subTask in issueSubTasks ) {
    completedSubTasks += (subTask.getResolution() != null ? 1 : 0)
}

return new DecimalFormat("###.#%").format(completedSubTasks / issueSubTasks.size())
