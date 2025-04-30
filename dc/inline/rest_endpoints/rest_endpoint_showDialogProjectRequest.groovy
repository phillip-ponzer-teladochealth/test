/*
Author: Jeff Melies
Purpose: This endpoint is called by UserRequest and actually displays the dialog box that the users see,
        it also sends out the email to the Project Lead.
*/

import com.onresolve.scriptrunner.runner.rest.common.CustomEndpointDelegate
import com.atlassian.jira.component.ComponentAccessor
import groovy.transform.BaseScript
import javax.ws.rs.core.MediaType
import javax.ws.rs.core.MultivaluedMap 
import javax.ws.rs.core.Response
import com.atlassian.jira.mail.Email
import com.atlassian.jira.component.ComponentAccessor
import com.atlassian.jira.config.properties.APKeys
import org.apache.log4j.Level

log.setLevel(Level.INFO)
@BaseScript CustomEndpointDelegate delegate

showDialogProjectRequest(httpMethod: "GET") { MultivaluedMap queryParams ->

def projectManager = ComponentAccessor.getProjectManager()

//Get parameters from the calling Endpoint
String myProjectSelect = queryParams.getFirst("myProjSelection") as String
Collection<String> myRoleSelect = queryParams.get("role_sel") as Collection
String myOptSelect = queryParams.getFirst("opt_sel") as String
String myOptURL = queryParams.getFirst("opt_text") as String
String myExp = queryParams.getFirst("myExpText") as String

//User Defined variables
String baseUrl = ComponentAccessor.getApplicationProperties().getString(APKeys.JIRA_BASEURL)
def project = projectManager.getProjectObjByName(myProjectSelect.toString())
final loggedInUser = ComponentAccessor.jiraAuthenticationContext.loggedInUser
log.debug("Project Obj: ${project.getName()}, and the Lead is ${project.getProjectLead()}")
def projLeadName = project.getProjectLead().displayName as String
def projLeadEmail = project.getProjectLead().emailAddress as String           
def projectLeadObj = ComponentAccessor.getUserManager().getUserByName(projLeadEmail)
String firstName, optSelect, roleSelect

log.debug("myRoleSelect: ${myRoleSelect}, myOptSelect: ${myOptSelect}")
if(myRoleSelect){
  if(myRoleSelect?.size() > 1){
    myRoleSelect.each{ role ->
      roleSelect += ", ${role}" 
    }
  }else{
      roleSelect = myRoleSelect[0]
  }
  roleSelect=roleSelect.replace("null,","").trim() 
}else{
  roleSelect = "N/A"
}
if(myOptSelect){
  optSelect = "${myOptSelect.toString()} on <a href=\"${myOptURL}\">board</a>."
}else{
  optSelect = "N/A"
}
log.debug("roleSelect: ${roleSelect}, optSelect: ${optSelect}")
// We need to verify the lead is active, if not send the email to all the system administrators
if(! projectLeadObj || ! projectLeadObj?.active){
    log.debug("User doesn't exist in Jira, Project Lead Name: ${projLeadName}, Email: ${projLeadEmail}")
    projLeadName = "The lead is not active at Teladoc, sending to Jira Operations"
    projLeadEmail = "atlassian.team@teladochealth.com"
    firstName = "Colleague"
    //log.warn("User doesn't exist in Jira; firstName: ${firstName}, projLeadName: ${projLeadName}, projLeadEmail: ${projLeadEmail}")
}else{
    //If the Lead is active, get their first name using their display name only if it has 1 white space, else try their email address.
    if(projLeadName.count(" ") == 1){
      firstName = projLeadName?.split(" ")[0]
    }else if(projLeadEmail.count(".") == 2 && projLeadEmail != "atlassian.team@teladochealth.com"){ //if email has more or less then 2 .'s we have to use a generic first name.
      final myIndex = projLeadEmail?.indexOf('.') as int
      //log.warn(projLeadEmail?.substring(0, myIndex))
      firstName = projLeadEmail?.substring(0, myIndex)
      //log.warn("firstName: ${firstName}")
    }else{
        firstName = "Colleague"
    }
}
log.debug("Email sent - To: ${projLeadEmail}, FromName: ${loggedInUser.displayName}, FromEmail: ${loggedInUser.emailAddress}")
//Set email attributes
firstName = firstName.toString().capitalize() //This will capitalize the first letter of the String.
def emailTo = projLeadEmail  //"jeff.melies@teladochealth.com"
def emailSubject = "Jira Project Access Request"
def emailFromName = "${loggedInUser.displayName}" //"Jira (ScriptRunner)"
def emailFrom = "${loggedInUser.emailAddress}" //"${projLeadEmail}" //"jira@teladochealth.com"
def emailBody = """<font face=\"Calibri\" size=\"3\" color=\"black\"> Dear $firstName, <p> This is an automated request from <a href=$loggedInUser.emailAddress>$loggedInUser.displayName</a> for either 'access' or 'additional access' to the Jira project <a href=$baseUrl/plugins/servlet/project-config/$project.key/summary>$myProjectSelect</a> that you are the project lead on. See the details below: <br>
    <hr>
    <br>
    <b>Requestor:</b>
    <a href=$loggedInUser.emailAddress>$loggedInUser.displayName</a>
    <br>
    <b>Project:</b>
    <a href=$baseUrl/plugins/servlet/project-config/$project.key/summary>$myProjectSelect</a>
    <br>
    <b>Project Key:</b>
    <a href=$baseUrl/plugins/servlet/project-config/$project.key/summary>$project.key</a>
    <br>
    <b>Project Lead Name:</b> $projLeadName <br>
    <b>Project Lead Email: </b>
    <a href=$projLeadEmail>$projLeadEmail</a>
    <br>
    <b>Requested Project Role:</b> $roleSelect <br>
    <b>Additional Permissions Requested:</b> $optSelect <br>
    <b>Additional comments from the requester:</b> $myExp <br>
  <p>
  <h3>
    <b>
      <u>Project Lead Instructions:</u>
    </b>
  </h3>
  <b>First, check to see if the user is already in a project role:</b>
  <br> - Navigate to: <a href=$baseUrl/plugins/servlet/project-config/$project.key/summary>Project Settings</a> -> <a href=$baseUrl/plugins/servlet/project-config/$project.key/roles>Users and roles</a>
  <br> - Enter the name <b>$loggedInUser.displayName</b> into the 'Search' field, and see if their name appears, if so you can review their existing project role. <br>
  <p>
    <hr>
    <hr> You can use the table below to complete the users request: <br>
  <b><u>Add user to an additional role(s): </u></b>Click the 'Roles' select list and add a checkmark on the desired project role.<br>

  <b><u>Add new user to a project role(s): </u></b>Click the blue link 'Add Users to a Role', Search for ( $loggedInUser.displayName ) and select a project role.<br>

  <b><u>Remove existing user from project: </u></b>Locate and click the 'Remove' link located on the far right side of the searched user (doing this may remove the users permissions from the project).<br>

  <b><u>Add user to Project Board Administrator: </u></b>(You must be a Board Administrator to perform these steps)<br>Click on this <a href=\"${myOptURL}\">board</a> link<br>In the upper right corner click <b>Board</b> -> <b>Configure.</b><br>Mouse-over the existing <b>Administrators</b> and click the <b>pencil</b> to the right.<br>Start typing $loggedInUser.emailAddress, selecting the correct user.<br>Press <b>Enter</b>.<br>

  <p> Thank you, <br> Jira Administration <br>
    <hr>
"""
def returnBody = """<font face=\"Calibri\" size=\"3\" color=\"black\"> Dear $firstName, <p> This is an automated request from <a href=$loggedInUser.emailAddress>$loggedInUser.displayName</a> for either 'access' or 'additional access' to the Jira project <a href=$baseUrl/plugins/servlet/project-config/$project.key/summary>$myProjectSelect</a> that you are the project lead on. See the details below: <br>
    <hr>
    <br>
    <b>Requestor:</b>
    <a href=$loggedInUser.emailAddress>$loggedInUser.displayName</a>
    <br>
    <b>Project:</b>
    <a href=$baseUrl/plugins/servlet/project-config/$project.key/summary>$myProjectSelect</a>
    <br>
    <b>Project Key:</b>
    <a href=$baseUrl/plugins/servlet/project-config/$project.key/summary>$project.key</a>
    <br>
    <b>Project Lead Name:</b> $projLeadName <br>
    <b>Project Lead Email: </b>
    <a href=$projLeadEmail>$projLeadEmail</a>
    <br>
    <b>Requested Project Role:</b> $roleSelect <br>
    <b>Additional Permissions Requested:</b> $optSelect <br>
    <b>Additional comments from the requester:</b> $myExp <br>
  <p>

  <p> Thank you, <br> Jira Administration <br>
    <hr>
"""

Mail.send {
    setTo(emailTo)
    setCc(emailFrom)
    setFrom(emailFrom)
    //setFromName(emailFromName)
    setSubject(emailSubject)
    setHtml()
    setBody(emailBody)
}    


def dialog3 ="""
    <html><body><p>Email has been sent to: <br>
    To: <a href=$projLeadEmail>$projLeadName</a> <br>
    CC: <a href=$loggedInUser.emailAddress>$loggedInUser.displayName</a> <br>
    Subject: $emailSubject <br></p>
    <h1 style="background-color: grey;">The following Email has been sent:</h1>
    <br> $returnBody </p>
    <h1><span style="color:#ffffff"><span style="background-color:#c0392b">You may close this window</span></span></h1>
    You may close this window <a href="javascript:history.back();">Return to the previous page.</a>
    <br>
    </body></html>
"""
    Response.ok().type(MediaType.TEXT_HTML).entity(dialog3.toString()).build()
    //Response.ok().type(MediaType.TEXT_HTML).entity(dialog3).build()
}