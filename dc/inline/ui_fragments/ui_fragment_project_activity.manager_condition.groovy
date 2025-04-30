import com.atlassian.jira.security.groups.GroupManager
import com.atlassian.jira.component.ComponentAccessor
import com.atlassian.jira.project.ProjectManager
import com.atlassian.jira.project.Project
import org.apache.log4j.Logger
import org.apache.log4j.Level
log = Logger.getLogger("Frgmnt(Project Activity: Manager) Condition") as Logger
log.setLevel(Level.INFO)

//def groupManager = ComponentAccessor.getGroupManager()
//def currentUser = ComponentAccessor.getJiraAuthenticationContext().getLoggedInUser()
ProjectManager projectMgr = ComponentAccessor.getProjectManager()
String urlProjectKey = jiraHelper?.request?.getPathInfo()?.split("/")?.last() ?: null

//if (groupManager.isUserInGroup(currentUser, "jira-administrators") && jiraHelper?.getProject()?.getProjectTypeKey()?.toString() != 'Service') {
if(urlProjectKey){//projectObj?.projectTypeKey?.key != null){
    Project projectObj = projectMgr?.getProjectByCurrentKeyIgnoreCase(urlProjectKey)
    log.debug("projectObj-TypeKey: ${projectObj?.projectTypeKey.key}")
    //if (jiraHelper?.getProject()?.getProjectTypeKey()?.toString() != 'service_desk') {
    if(projectObj?.projectTypeKey?.key != 'service_desk') {
        true
    }else{
        false
    }
}else{
    false
}


/* JBM replaced the below script with the above on 9/19/2024

import com.atlassian.jira.security.groups.GroupManager
import com.atlassian.jira.component.ComponentAccessor
import org.apache.log4j.Logger
import org.apache.log4j.Level
Logger log = Logger.getLogger("Frgmnt:ProjLead&MgrOnSidebar") 
log.setLevel(Level.DEBUG)

def groupManager = ComponentAccessor.getGroupManager()
def currentUser = ComponentAccessor.getJiraAuthenticationContext().getLoggedInUser()

//if (groupManager.isUserInGroup(currentUser, "jira-administrators") && jiraHelper?.getProject()?.getProjectTypeKey()?.toString() != 'Service') {
if (jiraHelper?.getProject()?.getProjectTypeKey().toString() != 'service_desk') {
    true
}else{
    false
}
*/