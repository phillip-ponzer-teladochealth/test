import com.atlassian.jira.security.groups.GroupManager
import com.atlassian.jira.component.ComponentAccessor
import com.atlassian.jira.project.ProjectManager
import com.atlassian.jira.project.Project
import org.apache.log4j.Logger
import org.apache.log4j.Level
log = Logger.getLogger("Frgmnt(Projects SidebarLead&Mgr)-Condition") as Logger
log.setLevel(Level.INFO)

//def groupManager = ComponentAccessor.getGroupManager()
ProjectManager projectMgr = ComponentAccessor.getProjectManager()
//def currentUser = ComponentAccessor.getJiraAuthenticationContext().getLoggedInUser()

String urlProjectKey = jiraHelper?.request?.requestURI?.split("/")?.last() ?: null

//if (groupManager.isUserInGroup(currentUser, "jira-administrators") && jiraHelper?.getProject()?.getProjectTypeKey()?.toString() != 'Service') {
if(urlProjectKey){//projectObj?.projectTypeKey?.key != null){
    Project projectObj = projectMgr?.getProjectByCurrentKeyIgnoreCase(urlProjectKey)
//    log.debug("projectObj-TypeKey: ${projectObj?.projectTypeKey.key}")
    if(projectObj?.projectTypeKey?.key != 'service_desk') {
        true
    }else{
        false
    }
}else{
    false
}