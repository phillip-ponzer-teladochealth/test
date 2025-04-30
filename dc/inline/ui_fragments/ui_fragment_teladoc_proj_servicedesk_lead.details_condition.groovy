import com.atlassian.jira.security.groups.GroupManager
//import com.atlassian.jira.plugin.webfragment.model.JiraHelper
import com.atlassian.jira.component.ComponentAccessor
import com.atlassian.jira.project.ProjectManager
import com.atlassian.jira.project.Project
import org.apache.log4j.Logger
import org.apache.log4j.Level
log = Logger.getLogger("Frgmnt(Projects SidebarLead&Mgr(Service))-Condition") as Logger
log.setLevel(Level.INFO)

//def groupManager = ComponentAccessor.getGroupManager()
ProjectManager projectMgr = ComponentAccessor.getProjectManager()
//def currentUser = ComponentAccessor.getJiraAuthenticationContext().getLoggedInUser()

String urlProjectKey = jiraHelper?.request?.requestURI?.split("/")?.last() ?: null

if(urlProjectKey){
    Project projectObj = projectMgr?.getProjectByCurrentKeyIgnoreCase(urlProjectKey)
    if(projectObj?.projectTypeKey?.key == 'service_desk') {
        true
    }else{
        false
    }
}else{
    false
}

/* JBM replaced the below with the above on 9/19/2024
import org.apache.log4j.Logger
import org.apache.log4j.Level

log = Logger.getLogger("(Service Management) Frgmnt:")
log.setLevel(Level.DEBUG)

log.warn("Service Project Type Key: ${jiraHelper?.getProject()}")
jiraHelper?.project?.projectTypeKey?.toString() == "Service"
*/