import com.atlassian.jira.component.ComponentAccessor
import org.springframework.ldap.core.AttributesMapper
import com.atlassian.jira.project.ProjectManager
import com.onresolve.scriptrunner.ldap.LdapUtil
import com.atlassian.jira.user.ApplicationUser
import javax.naming.directory.SearchControls
import com.atlassian.jira.project.Project
import com.atlassian.jira.avatar.Avatar
import org.apache.log4j.Logger
import org.apache.log4j.Level

Logger log = Logger.getLogger("Frgmnt(Project Activity: Manager)") 
log.setLevel(Level.INFO)
def projContext = context

//Managers
ProjectManager projectManager = ComponentAccessor.getProjectManager()
def avatarService = ComponentAccessor.avatarService
def authenticationContext = ComponentAccessor.jiraAuthenticationContext


def baseurl = ComponentAccessor.getApplicationProperties().getString("jira.baseurl")
def projAccessRequest = baseurl + "/rest/scriptrunner/latest/custom/userRequest?"
def loggedInUser = authenticationContext.loggedInUser

def project = projectManager.getProjectObjByKeyIgnoreCase(projContext.project.toString().split(": ")[1].trim())     //(projContext.projectKey.toString())
if(!(project)){return}

def managerOf = retrieveManager (project?.getProjectLead()?.emailAddress?.toString()) as ApplicationUser

try {
    populateManager managerOf as ApplicationUser
} catch(Exception e1) {
    log.debug("*** ERROR EXCEPTION: ${e1}")
    log.error("*** ERROR EXCEPTION: ${e1}")
}


//***************************FUNCTIONS***************************
def retrieveManager( String userID ) {
    if(userID == null){return "N/A"}
    String managerOU, manager
    Logger log = Logger.getLogger("Frgmnt(Project Activity: Manager)-retrieveManager") 
    log.setLevel(Level.INFO)

    log.debug("userID.length(): (${userID?.length()})${userID}")
    if(userID?.length() < 3){return}
    managerOU = (retrieveLDAP_User "mail=${userID}", "manager").toString().replaceAll("[\\[\\]]", "")
    if(managerOU == ''){return}
    log.debug("managerOU: ${managerOU}")
    manager = retrieveLDAP_User ("${managerOU.toString().substring(0, managerOU.toString().indexOf(",")) as String}", "userPrincipalName")
    if(manager == ''){return}
    log.debug("manager: ${manager}")
        
    try {
        return Users.getByName("${manager.toString().replaceAll("[\\[\\]]", "")}") as ApplicationUser
    } catch(Exception e1) {
        log.debug("*** ERROR EXCEPTION: ${e1}")
        log.error("*** ERROR EXCEPTION: ${e1}")
    }
}

def retrieveLDAP_User(String ldapQuery, String attribute) {
    Logger log = Logger.getLogger("Frgmnt(Project Activity: Manager)-retrieveLDAP_User") 
    log.setLevel(Level.INFO)
    def results
    try {
        results = LdapUtil.withTemplate('LDAP-Prod') { template ->
            template.search("OU=Teladoc Users", "(${ldapQuery})", SearchControls.SUBTREE_SCOPE, { attributes ->
                attributes.get(attribute).get()
            } as AttributesMapper<String>)
        }
    } catch(Exception e1) {
        log.debug("*** ERROR EXCEPTION: ${e1}")
        log.error("*** ERROR EXCEPTION: ${e1}")
    }
    log.debug("results: ${results}")
    return results
}

def populateManager(ApplicationUser strMan){
    def avatarService = ComponentAccessor.avatarService
    Logger log = Logger.getLogger("Frgmnt(Project Activity: Manager)-populateManager") 
    log.setLevel(Level.INFO)
    String leadManager = """<dt class="project-meta-label">Manager of lead</dt>"""
    if(strMan){
        log.debug("strMan: ${strMan}")
        String avatarURL = ComponentAccessor?.avatarService?.getAvatarURL(ComponentAccessor?.jiraAuthenticationContext?.getLoggedInUser(), strMan, Avatar?.Size?.SMALL)?.toString()
        log.debug("avatarURL: ${avatarURL}")
        leadManager += """            
            <dd class="project-meta-value">
                <a class="jira-user-name user-hover jira-user-avatar jira-user-avatar-small" rel="${strMan?.getUsername() ?: ""}" id="project-vignette_${strMan?.getUsername() ?: ""}" href="/secure/ViewProfile.jspa?name=${strMan.getUsername() ?: ""}">
                    <span class="aui-avatar aui-avatar-small"><span class="aui-avatar-inner"><img src=${avatarURL} loading="lazy" alt="${strMan?.getUsername() ?: ""}"></span></span>
                    ${strMan?.displayName ?: "Lead is inactive"} 
                </a>
            </dd>
            """
    }else{
        leadManager += """
        <dd class="project-meta-value">
            <a>
                Not available
            </a>
        </dd>
        """
    }
    try {
        writer.write(leadManager)
    } catch(Exception e1) {
        log.debug("*** ERROR EXCEPTION: ${e1}")
        log.error("*** ERROR EXCEPTION: ${e1}")
    }
}