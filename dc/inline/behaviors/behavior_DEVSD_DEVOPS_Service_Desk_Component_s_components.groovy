// Author: Ignacio Vera - JIRA-9758
// Show "Github ID field and make it required"

import com.atlassian.jira.bc.project.component.ProjectComponent

def component = getFieldById(getFieldChanged())
def githubId = getFieldByName('GitHub ID')
// def description = getFieldByName('Description')

//* def descriptionValue = """\
//         h4.  *Specific repository url(s) you need access to:*
//         h4.  *Type of access (read, write):*
//         h4.  *Additional information:*
 
//     """.stripIndent()
// */

List <ProjectComponent> comps = component.getValue() as List
def isthere = false

githubId.setHidden(true)
githubId.setRequired(false)


for(c in comps){
     if(c.getName().contains("Github")){isthere = true}
}
if(isthere){
    githubId.setHidden(false)
    githubId.setRequired(true)
} else {
    githubId.setFormValue('')

}


