package com.teladochealth.uiFragments

import com.atlassian.jira.component.ComponentAccessor
import org.springframework.ldap.core.AttributesMapper
import com.atlassian.jira.project.ProjectManager
import com.onresolve.scriptrunner.ldap.LdapUtil
import com.atlassian.jira.user.ApplicationUser
import javax.naming.directory.SearchControls
import com.atlassian.jira.project.Project
import org.apache.log4j.Logger
import org.apache.log4j.Level

Logger log = Logger.getLogger("Frgmnt_ProjLeadAndMgrOnSidebar")
log.setLevel(Level.DEBUG)
def projContext = context

//Managers
ProjectManager projectManager = ComponentAccessor.getProjectManager()
def avatarService = ComponentAccessor.avatarService
def authenticationContext = ComponentAccessor.jiraAuthenticationContext

//def loggedInUser = authenticationContext.loggedInUser
String avatarURL,leadManager,leadDetails,prjKey
prjKey = context?.project?.toString()?.split(":")[1]?.trim()

def project = projectManager.getProjectObjByKeyIgnoreCase(prjKey) as Project  //projContext.project.toString().split(": ")[1].trim())     //(projContext.projectKey.toString())
if(!(project)){return}

//get Project Lead
def prjLead = Users?.getByName(project?.getLeadUserName()) as ApplicationUser

def managerOf // = retrieveManager (project?.getProjectLead()?.emailAddress?.toString()) as ApplicationUser
//if(Users.getByName(project?.getProjectLead()?.emailAddress).isActive()){
//if(prjLead.isActive()){
    //managerOf = retrieveManager (project?.getProjectLead()?.emailAddress?.toString()) as ApplicationUser
//    managerOf = (retrieveManager prjLead?.getUsername()?.toString()) as ApplicationUser
//}else{
//    managerOf = 'N/A'
//}
//if(managerOf.toString().length() < 4 ){managerOf = 'N/A'}
//def prjLead = Users.getByName(project?.getProjectLead()?.emailAddress) ?: 'N/A' //project?.getProjectLead()?.displayName.toString() ?: 'N/A'
//if(prjLead != 'N/A'){
if(prjLead){
    if(! prjLead?.isActive()){
        prjLead = prjLead?.displayName.toString() + ' (Inactive)'
        managerOf = 'N/A'
    }else{
        managerOf = ((retrieveManager prjLead?.getUsername()?.toString()) as ApplicationUser)?.getDisplayName()?.toString() ?: 'N/A'
        prjLead = prjLead?.displayName.toString()
    }
}else{
    prjLead = 'N/A'
}

//project?.getProjectLead()?.displayName.toString()
leadDetails = """<div id="project-lead-details" class="aui-sidebar-group jira-sidebar-group-with-divider project-shortcuts-group_empty" data-id="project-lead-group" style="line-height:1">
<div class="aui-nav-heading" style="line-height: 1.5;">Project Lead Details</div>
<span class="project-shortcuts-group__description" style="font-size:85%;">For access/permission contact:</span>
<br>
<span class="project-shortcuts-group__description" style="font-size:85%;"><b>Proj Lead:</b> <a href="mailto: ${project?.getProjectLead()?.getEmailAddress().toString() ?: 'N/A'}">${prjLead ?: 'N/A'}</a></span>
<br>
<span class="project-shortcuts-group__description" style="font-size:85%;"><b>Manager:</b> <a href="mailto: ${managerOf}">${managerOf}</a></span>
</div>
"""

try {
    writer.write(leadDetails)
} catch(Exception e1) {
    log.debug("***prjKey: ${prjKey} *** ERROR EXCEPTION: ${e1}")
    log.error("***prjKey: ${prjKey} *** ERROR EXCEPTION: ${e1}")
}

//***************************FUNCTIONS***************************
def retrieveManager( String userID ) {
    String managerOU, manager
    def results
    Logger log = Logger.getLogger("Frgmnt:ProjLeadAndMgrOnSidebar:Function_retrieveManager")
    log.setLevel(Level.INFO)
    log.debug("userID?.length()(${userID?.length()}): ${userID}")
    if(userID?.length() < 3){return}
//    if(Users.getByName(userID).isActive()){
        managerOU = (retrieveLDAP_User "mail=${userID}", "manager").toString().replaceAll("[\\[\\]]", "")
        if(managerOU == '' || managerOU == null){return}
        log.debug("managerOU: ${managerOU}")
        manager = retrieveLDAP_User ("${managerOU.toString().substring(0, managerOU.toString().indexOf(",")) as String}", "userPrincipalName")
        if(manager == '' || manager == null){return}
        log.debug("manager: ${manager}")
        //return ComponentAccessor.getUserManager().getUserByName("${manager.toString().replaceAll("[\\[\\]]", "")}") as ApplicationUser
        try {
            results = Users.getByName("${manager.toString().replaceAll("[\\[\\]]", "")}") as ApplicationUser
        } catch(Exception e1) {
            log.debug("*** ERROR EXCEPTION: ${e1}")
            log.error("*** ERROR EXCEPTION: ${e1}")
        }
        return results
//    }
}

def retrieveLDAP_User(String ldapQuery, String attribute) {
    Logger log = Logger.getLogger("Frgmnt:ProjLeadAndMgrOnSidebar:Function_retrieveLDAP_User")
    log.setLevel(Level.INFO)
    def results
    try {
        //return LdapUtil.withTemplate('LDAP-Prod') { template ->
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
