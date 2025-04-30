
import com.atlassian.jira.component.ComponentAccessor
import com.atlassian.jira.project.ProjectManager
import com.atlassian.jira.user.ApplicationUser
import com.atlassian.jira.project.Project
import com.atlassian.jira.avatar.Avatar
import com.atlassian.jira.security.roles.ProjectRole
import com.atlassian.jira.security.roles.ProjectRoleActors
import com.atlassian.jira.security.roles.ProjectRoleManager
import org.apache.log4j.Logger
import org.apache.log4j.Level

Logger log = Logger.getLogger("Frgmnt(Project Activity: Administrators)") 
log.setLevel(Level.INFO)

//Managers
ProjectManager projectManager = ComponentAccessor.getProjectManager()
def avatarService = ComponentAccessor.avatarService
def authenticationContext = ComponentAccessor.jiraAuthenticationContext
def groupManager = ComponentAccessor.groupManager
def projectRoleManager = ComponentAccessor.getComponent (ProjectRoleManager)

def baseurl = ComponentAccessor.getApplicationProperties().getString("jira.baseurl")
def loggedInUser = authenticationContext.loggedInUser
ProjectRole projRole = projectRoleManager.getProjectRole("Administrators")



//def projContext = context

log.debug("Project Key: ${context.projectKey.toString()}")
def project = projectManager.getProjectObjByKeyIgnoreCase(context.projectKey.toString())     //(projContext.projectKey.toString())
if(!(project)){return}
def roleActors = projectRoleManager.getProjectRoleActors(projRole, project).getApplicationUsers().toList()
String projType, projTypeName

log.debug("roleActors: ${roleActors.sort()}")
switch(project?.getProjectTypeKey()?.key){
    case "software":
        projType = "software"
        projTypeName = "Software"
    break;
    case "service":
        projType = "service_desk"
        projTypeName = "Service Desk"
    break;
    case "business":
        projType = "business"
        projTypeName = "Business"
    break;
    default:
        projType = "N/A"
        projTypeName
    break;
}

//String actorsHTML = """<dt class="project-meta-label">Project Role Administrators</dt>"""
String actorsHTML = """<dt class="project-meta-label">Type</dt>
<dd class="project-meta-value"><a class="project-category" href="/secure/BrowseProjects.jspa?selectedCategory=all&selectedProjectType=${projType}">${projTypeName}</a></dd>
<dt class="project-meta-label">Project Role Administrators</dt>"""
String avatarURL = ""
roleActors?.sort{it.getDisplayName()}.each{ actors ->
    log.debug("actors: ${actors.toString()}")
    if(( actors?.getDisplayName().startsWith("svc_") || 
        actors?.getUsername().startsWith("svc_") || 
        actors?.getUsername() == "it_worker" || 
        actors?.getUsername() == "jira@teladochealth.com") || 
        (!( actors?.getUsername()))){
            return
        }
    log.debug("actors: ${actors?.getUsername()}")
    avatarURL = avatarService.getAvatarURL(loggedInUser, actors, Avatar.Size.SMALL)?.toString()
    log.debug("avatarURL: ${avatarURL}")
    actorsHTML += """<a class="jira-user-name user-hover jira-user-avatar jira-user-avatar-small" rel="${actors.getUsername()}" id="project-vignette_${actors.getUsername()}" href="/secure/ViewProfile.jspa?name=${actors.getUsername()}">
    <span class="aui-avatar aui-avatar-small"><span class="aui-avatar-inner"><img src=${avatarURL} loading="lazy" alt="${actors.getUsername()}"></span></span>
    ${actors.displayName}
    </a><br>
    """
}

writer.write(actorsHTML)
