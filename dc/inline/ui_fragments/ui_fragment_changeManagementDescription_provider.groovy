/*
**Author: Jeff Melies
**Purpose: Displays the workflow statuses and bolds the current status
*/
import com.atlassian.jira.issue.fields.layout.field.FieldLayoutItem
import com.atlassian.jira.component.ComponentAccessor
import com.atlassian.jira.workflow.WorkflowManager
import com.atlassian.jira.issue.CustomFieldManager
import com.atlassian.jira.issue.IssueManager
//import com.atlassian.jira.issue.FieldLayoutManager
import com.atlassian.jira.issue.fields.CustomField
import com.atlassian.jira.issue.RendererManager
import groovy.transform.Field as tField
import com.atlassian.jira.issue.Issue
import org.apache.log4j.Logger
import org.apache.log4j.Level

//User modifiable options
String cfName = "description"
String cfName2 = "ITSM Status"
String hint = "<sup>Click pencil on right to edit ${cfName}</sup>"

//Logging
Logger log = Logger.getLogger("changeManagementDetails") //("com.acme.workflows")
log.setLevel(Level.DEBUG)

//Managers
WorkflowManager workflowManager = ComponentAccessor.getWorkflowManager()
CustomFieldManager customFieldManager = ComponentAccessor.getCustomFieldManager()
IssueManager issueManager = ComponentAccessor.getIssueManager()
RendererManager rendererManager = ComponentAccessor.getComponent(RendererManager)
def fieldLayoutManager = ComponentAccessor.getFieldLayoutManager()

//Issue issue = issueManager.getIssueObject("CHG-284")
//Issue issue = context.issue as Issue
def issue = context.issue as Issue

String strHTML
def status = issue.getStatus()
List wrkflw = chgWorkflow(issue) as List
log.debug("changeManagementDescription - Issue: ${issue}, Status: ${status.name}, Workflow status count: ${wrkflw?.size()}, Workflow: ${wrkflw}")


//def myDesc = customFieldManager.getCustomFieldObjects( issue ).find {it.name == "Description"}

//def fieldLayoutItem = ComponentAccessor.getFieldLayoutManager().getFieldLayout(issue).getFieldLayoutItem("Change Description")
//log.debug("fieldLayoutItem: ${fieldLayoutItem}")
//def renderer = rendererManager.getRendererForField(fieldLayoutItem)
//log.debug("renderer: ${renderer}")
//String renderedField = renderer.render(value, issue.getIssueRenderContext())
//def customfield = "Change Description"
//writer.write(renderer.render(issue.description, issue.getIssueRenderContext()))
//writer.write("<div id=\"" + customfield + "-val\" class=\"field-ignore-highlight editable-field inactive\" title=\"Click to edit\" style=\"margin: -2px 0 0 -5px; width: 100%;\">" + "<div class=\"user-content-block\">" + renderedField +
//hint +
//"</div><span class=\"overlay-icon aui-icon aui-icon-small aui-iconfont-edit\"></span>" + 
//"</div>")


//def fieldLayoutItem = ComponentAccessor.getFieldLayoutManager().getFieldLayout(issue).getFieldLayoutItem("description")
//log.debug("changeManagementDescription - Description-fieldLayoutItem: ${fieldLayoutItem}")
def renderer = rendererManager.getRendererForField(ComponentAccessor.getFieldLayoutManager().getFieldLayout(issue).getFieldLayoutItem("description"))   //(fieldLayoutItem)
log.debug("changeManagementDescription - renderer: ${renderer}, **TYPE**: ${renderer.getRendererType()}, **CLASS**: ${renderer.class}")

String value = issue.description
String renderedDesc = renderer.render(value, issue.getIssueRenderContext())
log.debug("changeManagementDescription - renderedField: ${renderedDesc}")
if (!renderedDesc.empty) {
 hint = ""
}
//For Issue Type
//fieldLayoutItem = ComponentAccessor.getFieldLayoutManager().getFieldLayout(issue).getFieldLayoutItem("issuetype")
//log.debug("changeManagementDescription - Issue Type-fieldLayoutItem1: ${fieldLayoutItem}")
renderer = rendererManager.getRendererForField(ComponentAccessor.getFieldLayoutManager().getFieldLayout(issue).getFieldLayoutItem("issuetype"))   //(fieldLayoutItem as FieldLayoutItem)
log.debug("changeManagementDescription - renderer1: ${renderer}, **TYPE**: ${renderer.getRendererType()}, **CLASS**: ${renderer.class}")

value = issue.issueType.name
String renderedType = renderer.render(value, issue.getIssueRenderContext())
log.debug("changeManagementDescription - renderedType: ${renderedType}")

//For ITSM Status
def cfObj = customFieldManager.getCustomFieldObjectsByNameIgnoreCase(cfName2)?.first()
//def fieldLayoutItem2 = ComponentAccessor.getFieldLayoutManager().getFieldLayout(issue).getFieldLayoutItem(cfObj.id)
//log.debug("changeManagementDescription - ITSM Status-fieldLayoutItem: ${fieldLayoutItem2}")
renderer = rendererManager.getRendererForField(ComponentAccessor.getFieldLayoutManager().getFieldLayout(issue).getFieldLayoutItem(cfObj.id))   //(fieldLayoutItem2)
log.debug("changeManagementDescription - rendererCF: ${renderer}, **TYPE**: ${renderer.getRendererType()}, **CLASS**: ${renderer.class}")

String value2
if(!cfObj.getValueFromIssue(issue)){
    def fieldConfig = ComponentAccessor.getFieldConfigSchemeManager().getRelevantConfig(issue, cfObj)
    value2 = cfObj.getCustomFieldType().getDefaultValue(fieldConfig)
    log.debug("changeManagementDescription - cfObj was empty, displaying the default value: ${value2}")
}else{
    value2 = cfObj.getValueFromIssue(issue)  //issue.getCustomFieldValue(customFieldManager.getCustomFieldObjectsByNameIgnoreCase(cfName)?.first()) // ComponentAccessor.getCustomFieldManager().getCustomFieldObjectsByNameIgnoreCase(cfName1)[0])
    log.debug("changeManagementDescription - value2: ${value2}")
}
//def fieldConfig = ComponentAccessor.fieldConfigSchemeManager.getRelevantConfig(IssueContext.GLOBAL, cf)
//def defaultValue = cf.getCustomFieldType().getDefaultValue(fieldConfig)
//if (value2.empty) {
// value2 = "Unresolved"
//}
String renderedCF = renderer.render(value2, issue.getIssueRenderContext())
log.debug("changeManagementDescription - renderedCF: ${renderedCF}")


//writer.write("<div id=\"Description-val\" class=\"field-ignore-highlight editable-field inactive\" title=\"Click to edit\" style=\"margin: -2px 0 0 -5px; width: 100%;\">" + "<div class=\"user-content-block\">" + renderedField + hint + "</div><span class=\"overlay-icon aui-icon aui-icon-small aui-iconfont-edit\"></span>" + "</div>")
//writer.write("<div id=\"" + cfName + "-val\" class=\"field-ignore-highlight editable-field inactive\" title=\"Click to edit\" style=\"margin: -2px 0 0 -5px; width: 100%;\">" + "<div class=\"user-content-block\">" + renderedField + hint + "</div><span class=\"overlay-icon aui-icon aui-icon-small aui-iconfont-edit\"></span>" + "</div>")

String progression = ""
wrkflw.each { sts ->
    if(sts.toString().toLowerCase() == status.name.toLowerCase()){
        progression = progression.concat("<u><b>" +sts.toString()+ "</u></b> -> ")
        log.debug("progression: ${progression}")
        //writer.write("${progression.toString()}")
    }else{
        progression = progression.concat(sts.toString()+ " -> ")
        log.debug("progression-1: ${progression}")
        //writer.write("${progression.toString()}")
    }
}

//return "<p style='border-width:1px; border-style:solid; border-color:Black; padding: 1em;'>" + (progression.substring(0, progression.length() - 4)) + "</p>"
//writer.write("<p><b>Workflow Progression:</b><br>")
//writer.write("<p style='border-width:1px; border-style:solid; border-color:Black; padding: 0.1em;'>" + (progression.substring(0, progression.length() - 4)) + "</p>")
//writer.write("<p>" + (progression.substring(0, progression.length() - 4)) + "</p>")
//writer.write("<p><b>Change Description:</b><br><ul><li>Create a new team Big Tuna</li><li>Move 2 quotas from the Other pool to the QA cluster</li><li>Move 1 quota from the Other pool to the DEV cluster</li></ul></p>")
//writer.write("<p><b>Change Description:</b><br>" + myDescValue + "</p>")
//writer.write("<div id='" + cfITSMStatus + "-val' class='field-ignore-highlight editable-field inactive' title='Click to edit'>" + "<div class='user-content-block'>" + renderer.render(cfITSMStatusValue, issue.getIssueRenderContext()) + "</div>" + "</div>")
//writer.write("<div id=\"" + cfITSMStatus + "-val\" class=\"field-ignore-highlight editable-field inactive\" title=\"Click to edit\" style=\"margin: -2px 0 0 -5px; width: 100%;\">" + "<div class=\"user-content-block\">" + renderedField + hint + "</div><span class=\"overlay-icon aui-icon aui-icon-small aui-iconfont-edit\"></span>" + "</div>")
//writer.write("<div id=\"" + myDescValue + "-val\" class=\"field-ignore-highlight editable-field inactive\" title=\"Click to edit\" style=\"margin: -2px 0 0 -5px; width: 100%;\">" + "<div class=\"user-content-block\">" + renderedDesc + descHint + "</div><span class=\"overlay-icon aui-icon aui-icon-small aui-iconfont-edit\"></span>" + "</div>")

strHTML = "<div style='color:Gray; word-wrap:break-word;overflow:auto;' class='module toggle-wrap mod-header'>"
//strHTML += "<a style='color:Gray'>Status Chevron: </a>" + "<a style='color:Black'>" + (progression.substring(0, progression.length() - 4)) + "</a><br>"
//strHTML += "<p class='big'>"
strHTML += "<p>"
//strHTML += "Status Chevron: "
//strHTML += "<a style='color:Gray; text-decoration: none; cursor: default;'>Status Chevron: </a>"
//strHTML += "<a>Status Chevron: </a>"
//strHTML += "<a style='border-width:1px; border-style:solid; border-color:Black; color:Black; padding: 0.1em; text-decoration: none; cursor: default; line-height:35px;'>" + (progression.substring(0, progression.length() - 4)) + "</a>"
//strHTML += "<br>"
//strHTML += "Description: "
//strHTML += "<a style='color:Gray text-decoration: none; cursor: default;'>Description: </a>" 
//strHTML += "<div id=\"" + cfName + "-val\" class=\"field-ignore-highlight editable-field inactive\" title=\"Click to edit\">" + "<div style=\"color: black\" class=\"user-content-block\">" + renderedField + hint + "</div><span class=\"overlay-icon aui-icon aui-icon-small aui-iconfont-edit\"></span>" + "</div>"
//strHTML += "<div class='page-type-split' style='color:Gray; cursor:default;'>Description: "
//strHTML += "<div id='" + cfName + "-val' style='display:inline; text-decoration:none' class='field-ignore-highlight editable-field inactive' title='Click to edit'>" + "<div style='color:black; display:inline;' class='user-content-block'>" + renderedField + hint + "</div>" + "<span class='overlay-icon aui-icon aui-icon-small aui-iconfont-edit'  style='text-decoration: none'></span>" + "</div>"
//strHTML += "<div id='" + cfName + "-val' class='page-type-split field-ignore-highlight editable-field inactive' title='Click to edit'>" + "<div class='page-type-split user-content-block' style='color:Black;'>" + renderedField + hint + "</div>" + "</div>" + "</div>"
//style='display:inline-block;'
//strHTML += "<div id='chg-module'>"
//strHTML += "<div class='aui-tabs horizontal-tabs' id='customfield-tabs' role='application'>"
//strHTML += "<div id='chg-panel-1' class='active-pane'>"


//strHTML += "<table style='align:left; width:100%; color:Gray;'>"
strHTML += "<table class='cheveronData' boarder='1' width='100%'>"
strHTML += "<tbody style='font-size: 103%'>"
strHTML += "<tr>"
//strHTML += "<td width='15%'; style='color:Gray; text-decoration: none;'>Status Chevron: </td>"
strHTML += "<td class='statusCheveron' style='white-space: nowrap;'>Status Chevron: </td>"
//strHTML += "<td style='white-space: nowrap; border-width:1px; border-style:solid; border-color:Black; color:Black; padding: 0.1em; text-decoration: none; cursor: default;'>" + (progression.substring(0, progression.length() - 4)) + "</td>"
//strHTML += "<td style='color:Black; font-size:13px; cursor: default; white-space: nowrap; border-width:1px; border-style:solid; border-color:Black; padding: .2em;'>" + (progression.substring(0, progression.length() - 4)) + "</td>"
strHTML += "<td style='color:Black; font-size:97%; cursor: default; white-space: nowrap; border-width:1px; border-style:solid; border-color:Black; padding: .2em;'>" + (progression.substring(0, progression.length() - 4)) + "</td>"
strHTML += "</tr>"
strHTML += "<tr style='height:.2em;'><tr>"
strHTML += "<tr>"
strHTML += "<td>Description: </td>"
//strHTML += "<td id='" + cfName + "-val' class='split field-ignore-highlight editable-field inactive' title='Click to edit'>" + "<td style='color:Black;'>" + renderedField + hint + "</td>" + "</td>"
//strHTML += "<td class='value type-text editable-field' title='Click to edit'" + "<td style='color:Black;'>" + renderedField + hint + "</td>" + "</td>"
strHTML += "<td colspan=2 id='description-val' class='field-ignore-highlight editable-field inactive' title='Click to edit'" + "<td style='color: black;' class='jira-wikifield'>" + renderedDesc + hint + "<span class='overlay-icon'></span>" + "</td></td>"
//strHTML += "<div id='" + cfName + "-val' style='display:inline; text-decoration:none' class='field-ignore-highlight editable-field inactive' title='Click to edit'>" + "<div style='color:black; display:inline;' class='user-content-block'>" + renderedField + hint + "</div>" + "<span class='overlay-icon aui-icon aui-icon-small aui-iconfont-edit'  style='text-decoration: none'></span>" + "</div>"
strHTML += "</tr>"
strHTML += "</tbody>"
strHTML += "</table>"

/*
strHTML += "<table style='align:left; width:100%; color:Gray; font-size:14px;'>"
strHTML += "<tbody style='font-size: 100%'>"
strHTML += "<tr>"
strHTML += "<td style='color:Gray; text-decoration: none;' class='name'>Type: </td>"
//strHTML += "<td width='20%'><strong class='name' title='Type'>" + "<label for='issuetype'>Type:</label></strong></td>"
//strHTML += "<td style='color:Black;'>" + renderedType + "</td>"
strHTML += "<td colspan=2><span id='type-val' class='value' style='color:Black;'>" + "<img alt='height=16' src='/secure/viewavatar?size=xsmall&amp;avatarId=21913&amp;avatarType=issuetype' title='Change - Created by JIRA Service Desk.' width='16'> Change</span></td>"
//strHTML += "<td width='10%'></td>"
strHTML += "<td colspan=3 align='Right'>ITSM Status: </td>"
//strHTML += "<td style='color:Black;'>" + renderedCF + "</td>"
strHTML += "<td colspan=4 id='ITSM Status-val' class='field-ignore-highlight editable-field inactive' title='Click to edit'" + "<td style='color: black' class='value type-select editable-field active inline-edit-fields;'>" + renderedCF + "<span class='overlay-icon aui-icon aui-icon-small aui-iconfont-edit inline-edit-fields'></span>" + "</td></td>"
strHTML += "</tr>"
strHTML += "</tbody>"
strHTML += "</table>"
*/
//strHTML += "</p>"
//strHTML += "</div>"
//strHTML += "<div id=\"" + cfName + "-val\" class=\"field-ignore-highlight editable-field inactive\" title=\"Click to edit\" style=\"margin: -2px 0 0 -5px; width: 100%; color: Black; \">" +  "<div class=\"user-content-block\" style=\"color: Black; \">" + renderedField + hint + "</div><span class=\"overlay-icon aui-icon aui-icon-small aui-iconfont-edit\"></span>" + "</div>"
//strHTML += "<a class='field-ignore-highlight editable-field inactive style=color:Gray;'>" + 'Description:' + "</a>"
//strHTML += "<a style='text-decoration: none;'>" + 'ITSM Status:' + "</a>"
//strHTML += "<table style='width: 100%'>"
//strHTML += "<tr>"
//strHTML += "<td>"
//strHTML += "<span width='50%'; style='color:Gray; text-decoration: none;'>Type: </span>"
//strHTML += "<span style='color:Black; text-decoration: none;'>" + renderedField1 + "</span></td>"
////strHTML += "</tr><tr>"
//strHTML += "<td><span style='color:Gray; text-decoration: none; text-align: center'>ITSM Status: </span>"
//strHTML += "<span style='color:Black; text-decoration: none;'>" + renderedField2 + "</span></td>"
//strHTML += "</tr>"
//strHTML += "<a style='color:Gray; text-decoration: none;'>ITSM Status: </a>"
//strHTML += "<a style='color:Black; text-decoration: none;'>" + renderedField2 + "</a><br>"
//strHTML += "<a style='color:Gray; text-decoration: none;'>Type: </a>"
//strHTML += "<a style='color:Black; text-decoration: none;'>" + renderedField1 + "</a><br>"
//strHTML += "</table>"
//strHTML += "<div style=\"color: black\" class=\"user-content-block\">" + renderedField2 + "</div>"
//strHTML += "<div id=\"" + cfName2 + "-val\" class=\"field-ignore-highlight editable-field inactive\" title=\"Click to edit\">" + "<div style=\"color: black\" class=\"user-content-block\">" + renderedField2 + "</div><span class=\"overlay-icon aui-icon aui-icon-small aui-iconfont-edit\"></span>" + "</div>"
//strHTML += "<div id=\"" + cfName + "-val\" class=\"field-ignore-highlight editable-field inactive\" title=\"Click to edit\" style=\"margin: -2px 0 0 -5px; width: 100%; color: Black;\">" + "<div class=\"user-content-block\">" + renderedField + hint + "</div><span class=\"overlay-icon aui-icon aui-icon-small aui-iconfont-edit\"></span>" + "</div>"
strHTML += "</p>"
strHTML += "</div>"
writer.write(strHTML)
//if(issue.description){
    //writer.write("<b>Description:</b><br>")
    //writer.write(renderer.render(issue.description, null))
    //writer.write("<div id='Description-val' class='field-ignore-highlight editable-field inactive' title='Click to edit'>" + "<div class='user-content-block'>" + renderer.render(myDescValue, issue.getIssueRenderContext()) + "</div>" + "</div>")
    //writer.write(renderer.render(issue.description, issue.getIssueRenderContext()))
    //writer.write(strHTML)

//    writer.write(strHTML + "<div id=\"" + cfName + "-val\" class=\"field-ignore-highlight editable-field inactive\" title=\"Click to edit\">" + "<div class=\"user-content-block\">" + renderedField + hint + "</div><span class=\"overlay-icon aui-icon aui-icon-small aui-iconfont-edit\"></span>" + "</div>")
    
    
    
    
    
    
    //writer.write(strHTML + "<div id=\"" + cfName + "-val\" class=\"field-ignore-highlight editable-field inactive\" title=\"Click to edit\" style=\"margin: -2px 0 0 -5px; width: 100%;\">" + "<div class=\"user-content-block\">" + renderedField + hint + "</div><span class=\"overlay-icon aui-icon aui-icon-small aui-iconfont-edit\"></span>" + "</div>")

//    writer.write("<div id=\"" + cfName + "-val\" class=\"field-ignore-highlight editable-field inactive\" title=\"Click to edit\" style=\"margin: -2px 0 0 -5px; width: 100%;\">" + "<div class=\"user-content-block\">" + renderedField + hint + "</div><span class=\"overlay-icon aui-icon aui-icon-small aui-iconfont-edit\"></span>" +  "</div>")
    //writer.write("<div id=\"" + cfName + "-val\" class=\"field-ignore-highlight editable-field inactive\" title=\"Click to edit\" style=\"margin: -2px 0 0 -5px; width: 100%;\">" +  "<div class=\"user-content-block\">" + renderedField + hint + "</div><span class=\"overlay-icon aui-icon aui-icon-small aui-iconfont-edit\"></span>" + "</div>")
    //writer.write(renderer.render(issue.description, issue.getIssueRenderContext()))
    //writer.write("<p>" + strHTML)
//}

//**********************************************FUNCTIONS**********************************************
//For this project I had to hard code the statuses since these are dependent on a couple custom fields (Change type & Priority (SL))
def chgWorkflow(Issue issue){
    Logger log = Logger.getLogger("changeManagementDetails") //("com.acme.workflows")
    log.setLevel(Level.INFO)
    def prioritySL = issue.getCustomFieldValue("Priority (SL)").toString()
    def chgType = issue.getCustomFieldValue("Change type")?.values()
    log.debug("Function: chgWorkflow: ${issue} - Change Type [${chgType?.size()}]: ${chgType}, prioritySL: ${prioritySL}")
    if(! chgType[1]){
        log.error{"Change type is not complete on ${issue}."}
        return
    }
    String chgType1 = chgType[0].toString()
    String chgType2 = chgType[1].toString()
    log.debug("Function: chgWorkflow - Switch: ${chgType}, First check chgType2.toLowerCase(): ${chgType2.toLowerCase()}")
    switch(chgType2.toLowerCase()){
        case "software change":
            log.debug("Function: chgWorkflow - Switch - Software change, now check chgType1.toLowerCase(): ${chgType1.toLowerCase()}")
            switch(chgType1.toLowerCase()){
                case "standard":
                    if(prioritySL == "Very Low"){
                        log.debug("Function: chgWorkflow - case=standard - Very Low")
                        return ["Draft", "Self Review", "QA Review", "Approved", "In Progress", "Done", "Closed"]
                        //writer.write("Progression: Draft, Self Review, QA Review, Approved, In Progress, Done, Closed")
                    }else{
                        log.debug("Function: chgWorkflow - case=standard - NOT 'Very Low'")
                        return ["Draft", "Under Review", "QA Review", "Release Control", "Approved", "In Progress", "Done", "Closed"]
                    }
                break;
                case "cab":
                    log.debug("Function: chgWorkflow - case=cab")
                    return ["Draft", "Under Review", "QA Review", "Release Control", "Change Control", "CAB Review", "Approved", "In Progress", "Done", "Closed"]
                break;
                case "emergency":
                    log.debug("Function: chgWorkflow - case=emergency")
                    return ["Draft", "Under Review", "Emer Deploy Approved", "In Progress", "Change Control", "Approved", "Closed"]
                break;
                case "retro":
                    log.debug("Function: chgWorkflow - case=retro")
                    return ["Draft", "Under Review", "Change Control", "Exec Review", "Done", "Closed"]
                break;
                case "expedite":
                    log.debug("Function: chgWorkflow - case=expedite")
                    return ["Draft", "Under Review", "QA Review", "Release Control", "Change Control", "Exec Review", "Approved", "In Progress", "Done", "Closed"]
                break;
                default:
                break;
            }
        break;
        case "infrastructure change":
            log.debug("Function: chgWorkflow - Switch - infrastructure change")
            switch(chgType1.toLowerCase()){
                case "standard":
                    if(prioritySL == "Very Low"){
                        log.debug("Function: chgWorkflow - case=standard - Very Low")
                        return ["Draft", "Self Review", "Approved", "In Progress", "Done", "Closed"]
                    }else{
                        log.debug("Function: chgWorkflow - case=standard - NOT Very Low")
                        return ["Draft", "Under Review", "Approved", "In Progress", "Done", "Closed"]
                    }
                break;
                case "cab":
                    log.debug("Function: chgWorkflow - case=cab")
                    return ["Draft", "Under Review", "Change Control", "CAB Review", "Approved", "In Progress", "Done", "Closed"]
                break;
                case "emergency":
                    log.debug("Function: chgWorkflow - case=emergency")
                    return ["Draft", "Under Review", "Emer Deploy Approved", "In Progress", "Change Control", "Approved", "Closed"]
                break;
                case "retro":
                    log.debug("Function: chgWorkflow - case=retro")
                    return ["Draft", "Under Review", "Change Control", "Exec Review", "Done", "Closed"]
                break;
                case "expedite":
                    log.debug("Function: chgWorkflow - case=expedite")
                    return ["Draft", "Under Review", "Change Control", "Exec Review", "Approved", "In Progress", "Done", "Closed"]
                break;
                default:
                    return []
                break;
            }
        break;
        default:
            return []
        break;
    }

}
