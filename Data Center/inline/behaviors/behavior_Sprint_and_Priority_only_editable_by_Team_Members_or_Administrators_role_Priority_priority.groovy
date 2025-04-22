/*
Author: Jeff Melies
Purpose: Restrict the fields Sprint & Priority to only members in the project roles "Team Member" or "Administrators"
request: JIRA-9123  (updated in JIRA-9621 by Jeff T)
*/
import com.atlassian.jira.project.Project
import com.atlassian.jira.issue.Issue
import com.atlassian.jira.component.ComponentAccessor
import com.atlassian.jira.user.ApplicationUser
import com.atlassian.jira.security.roles.ProjectRole
//import com.atlassian.jira.security.roles.ProjectRoleActors
import com.atlassian.jira.security.roles.ProjectRoleManager
import org.apache.log4j.Logger
import org.apache.log4j.Level
log.setLevel(Level.INFO)
Logger log = Logger.getLogger("Behaviour_${issueContext?.projectObject?.name}_Priority")

Issue issue = underlyingIssue
def projectRoleManager = ComponentAccessor?.getComponent (ProjectRoleManager)

ApplicationUser loggedInUser = ComponentAccessor?.jiraAuthenticationContext?.loggedInUser
ProjectRole projRole = projectRoleManager?.getProjectRole("Team Member")
ProjectRole projRole2 = projectRoleManager?.getProjectRole("Administrators")

def projectObj = issue?.getProjectObject() as Project
def priority = getFieldByName("Priority")

if(issueContext?.projectObject){
    //If loggedInUser is in projRole then allow them to change the fields
    if(loggedInUser?.isMemberOfRole(projRole, issueContext?.projectObject) || loggedInUser?.isMemberOfRole(projRole2, issueContext?.projectObject)){
        getFieldByName("Priority")?.setReadOnly(false)
    }
}else{
    getFieldByName("Priority")?.setReadOnly(true)
}