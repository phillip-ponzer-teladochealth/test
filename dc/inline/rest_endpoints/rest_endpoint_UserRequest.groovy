/*
Author: Jeff Melies
Purpose: A fragment calls this endpoint which creates the project select list, it also calls another endpoint
        (showDialogProjectRequest) which displays the contents of this endpoint into a dialog box
        which will email the project Lead.
*/

import com.onresolve.scriptrunner.runner.rest.common.CustomEndpointDelegate
import groovy.transform.BaseScript
import javax.ws.rs.core.MediaType
import javax.ws.rs.core.MultivaluedMap 
import javax.ws.rs.core.Response
//import com.atlassian.jira.project.Project
import com.atlassian.jira.project.Project
import com.atlassian.jira.project.ProjectManager
import com.atlassian.jira.security.roles.ProjectRole
import com.atlassian.jira.security.roles.ProjectRoleManager
import com.atlassian.jira.config.properties.APKeys
import com.atlassian.jira.component.ComponentAccessor
import org.apache.log4j.Level

log.setLevel(Level.DEBUG)

@BaseScript CustomEndpointDelegate delegate

ProjectManager projectManager = ComponentAccessor.getProjectManager()
ProjectRoleManager projectRoleManager = ComponentAccessor.getComponent(ProjectRoleManager)

userRequest(httpMethod: "GET", groups: ["jira-users"]) { MultivaluedMap queryParams ->
    def baseUrl = ComponentAccessor.getApplicationProperties().getString(APKeys.JIRA_BASEURL)

    // Get the projects
    def pm = ComponentAccessor.getProjectManager()
    def projectsList = pm.getProjects()//.name
    //log.warn("projectsList: ${projectsList}")
    //List projects = projectsList.collect{ '"' + it.trim() + '"'}
    def projects = pm.getProjects()
    def projLeads = []
    projects.each{ proj -> 
        projLeads << '"'+proj.name+'"'
    }
    //projLeads << '"'+proj.name+"// | Lead: "+proj.getProjectLead().getDisplayName()+" ("+proj.leadUserName+")"+'"'
    int projectSize = projects.size()
    //log.warn("projLeads: ${projLeads}")
    def projRoles = projectRoleManager.getProjectRoles()
    List projectRoles = projRoles.collect{ '"' + it.toString().trim() + '"'}
    int roleSize = projectRoles.size()
    // get a reference to the current page...
    // def page = getPage(queryParams)
//                <--!button id="submit-button" class="aui-button aui-button-primary" style="float: right;">Submit</button-->
    def dialog =
        """
<section role="dialog" id="sr-dialog" class="aui-layer aui-dialog2 aui-dialog2-xlarge" aria-hidden="true">

    <header class="aui-dialog2-header">
        <h2 class="aui-dialog2-header-main">Project Access Request</h2>
    </header>
    <div class="aui-dialog2-content">
        <div id="container" style="width:px">
            <form id="my-custom-sr-dialog-form" class="aui" action="/rest/scriptrunner/latest/custom/showDialogProjectRequest">
                <div id="projects" style="height:450px;width:800px;float:left;">
                    <!--<p>Please select the project:</p>-->
                    <h3> The lead of the selected project will handle the permissions requested in this form.
                    <br> 
                    An email will be sent to the lead and cc'd to yourself detailing the request.</h3>

                    <p>
                        <label id="projLabel"><sup>*</sup><u>Choose a project:</u></label>
                        <br>
                        <select id="proj" name="myProjSelection" required></select>
                        <br>
                        <br>
                        <label id="roleLabel" ><sup>*</sup><u>Choose a project role:</u></label>
                        <br>
                        <input type="radio" name="role_sel" value="Viewer & Commenter">
                        <label for="viewer"><b>Viewer & Commenter:</b> Can view and comment access to tickets in the project.</label><br>
                        <input type="radio" name="role_sel" value="Team Member">
                        <label for="team"><b>Team Member:</b> Can edit, transition, and be assigned tickets as well as all permission the <b>Viewer & Commenter</b> has.</label><br>
                        <input type="radio" name="role_sel" value="Administrators">
                        <label for="admin"><b>Administrators:</b> Has administrative permissions to the project, manage user access, can bypass some engineering workflow transitions, as well as all permissions the <b>Team Member</b> and <b>Viewer & Commenter</b> have.</label><br>
                        <input type="checkbox" name="role_sel" value="Scrum Master">
                        <label for="scrum"><b>Scrum Master:</b> Can manage sprints (Start, Stop, and Edit) Note: <b>Team Member</b> or <b> Administrators </b> access is also required.</label><br>
                        <p>
                        <label id="scrumLabel" ><u>Optional Permissions:</u></label><br>                        
                        <input type="checkbox" id="chkBoard" name="opt_sel" onclick="boardAdmin()" value="Board Administrator">
                        <label for="chkBoard"><b>Project Board Administrator:</b> Can manage the teams board including (Columns, Quick Filters, Swimlanes, etc).</label>
                        <div id="boardAdminYes" name="optText" style="visibility:hidden; margin-left: 3em;"><b>
                            <sup>*</sup></b><input type='text' id='opt_text' name='opt_text' size="100%" placeholder='Enter board URL'>
                        </div>
                        <label id="expLabel" >Comments:</label>
                        <br>
                        <textarea id="explanation" name="myExpText" cols="130" rows="7" ></textarea>
                        <br>
                        </p>
                    </p>
                </div>
                <div id="description" style="height:450px;width:800px;text-align: right;">
                </div><br>
                <div>
                <input type="submit" class="aui-button aui-button-primary" style="float: right;" onclick="return RadioValidator()" value="Submit"/>
                </div>
                <script> 
                    let pSelect = document.getElementById("proj");
                    let pLabel = document.getElementById("projLabel");
                    for (var i = 0; i < $projectSize; i++) {
                        if (i === 0){
                        	var el = document.createElement("option");
                        	el.textContent = '-- Start typing to get a list of matches --';
                        	el.value = '';
                        	pSelect.appendChild(el);
                        }
                        var optn = $projLeads[i];
                        if ( optn.toLowerCase().indexOf('_pr - ') === -1){
                            var el = document.createElement("option");
                            el.textContent = optn;
                            el.value = optn;
                            pSelect.appendChild(el);
                        }
                        pSelect.required = true
                    }
                    function boardAdmin(){
                        if (document.getElementById('chkBoard').checked) {
                            document.getElementById('boardAdminYes').style.visibility = 'visible';
                            //document.getElementById('boardAdminYes').setAttribute('required', 'required');                           
                        } else document.getElementById('boardAdminYes').style.visibility = 'hidden';
                            //document.getElementById('boardAdminYes').removeAttribute('required');
                            
                    }
                    
                    function RadioValidator() {
                        var urlPath = window.location.href.split("t/");
                        var checked_role = document.querySelector('input[name = "role_sel"]:checked');
                        var checked_opt = document.querySelector('input[name = "opt_sel"]:checked');
                        var checked_url = document.getElementById('opt_text').value;
                        if(checked_role == null && checked_opt == null){  //Test if something was checked
                            alert('You must make a selection from either "Project Role" or "Optional Permissions".'); //Alert, nothing was checked.
                            return false;
                        }
                        if(checked_opt != null){
                            if(checked_url == '' || checked_url == null || !checked_url.contains(urlPath[0])){
                                alert('When selecting "Project Board Administrator" you must enter a valid URL to the board.')
                                document.getElementById('opt_text').focus()
                                return false;
                            }
                        }
                    }
                </script>
            </form>
        </div>
    </div>
    <footer class="aui-dialog2-footer">
        <div class="aui-dialog2-footer-actions">
            <button id="dialog-close-button" class="aui-button aui-button-link">Close</button>
        </div>
        <div class="aui-dialog2-footer-hint">Complete form and press 'Submit'</div>
    </footer>
</section>
        """
        
    
    Response.ok().type(MediaType.TEXT_HTML).entity(dialog.toString()).build()	
}