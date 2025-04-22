/*
Author: Jeff Melies
Desc: Populate Description field depending on Category field, only on Create
Change log:
*/

//*************** ONLY ON CREATE *******************************
if (getAction()?.id != 1) {  return }  //Only run on Create
//**************************************************************

def desc = getFieldById("description")
def summ = getFieldById("summary")
def requestType = getFieldById(getFieldChanged()) 

def newDesc = htmlDesc(requestType.getValue().toString().toLowerCase())
desc.setFormValue(newDesc)

if(requestType.getValue().toString().toLowerCase() == "new rte/product"){
    summ.setFormValue("<PayerName> | <RequestType> | <Launch Date>")
    summ.setDescription"Please format to: 'PayerName | RequestType | Launch Date' (Ex: Meritain | RTE P360 Launch | 1/1/2022)"
}else{
    summ.setFormValue("")
    summ.setDescription""
}

def htmlDesc(String reqType){
    def desc
    switch(reqType.toLowerCase()){
        case "new payer":
            return """{panel:title=PAYER DETAILS section - all fields required|borderStyle=solid|borderColor=#cccccc|titleBGColor=#00ffff}
                *\\*Name:* 
                *\\*Payer Code:* 
                    ||Question||Answer (Y/N)||Question||Answer (Y/N)||
                    |Active?| |RTE Payer?| |
                    |Service Facility?| |Taxonomy?| |
                    |Update Plan ID?| |Info Only?| |
                    |Send zero-dollar claim?| |State Restriction?| |
                    |Charge Member Cost?| |Is Reciever ID approved by RCM?| |
		{panel}

		{panel:title=CLAIMS BILLING (FLAVOR) section - Fields with an *(asterisk) are required!|borderStyle=solid|borderColor=#cccccc|titleBGColor=#7fffd4}
                *Are These Encounter Claims?* {_}(Y/N{_}){*}:{*} 
                ^If “N” claims go to Next Gen. If "Y", claims go to Change Health Care.^
                ----
                *Are you adding a 'New Flavor'?* {_}(Y/N){_}{*}:{*} 
                ^Flavor=Customizations such as place of service or modifiers. If "Y" attach Billing Attribute form: [https://confluence.teladoc.net/x/4RQepw]^
                *\\*Receiver ID:* 
                ^Must be approved by Revenue Cycle Management^
                *Claim Gateway ID:* 
                ^Will be driven by Encounter claims^
                *\\*Rendering Provider* {_}(Individual Dr./Provider PA){_}{*}:{*} 
                ^Do you want the Individual Dr. or Provider?^
                *\\*Billing Provider State:* 
                *Provider Role TIN Override:* 
                ^Default is 'None'.^
                *Max Daily Claims:* 
                *Preferred Clearinghouse:* 
                ^Example: Ability Solutions, Change Healthcare, Clearinghouse Encounter Claim, Nextgen^ 
                {panel}
                 """
        break;
        case "update payer":
            //getFieldById("customfield_10325").setHidden(false) //PID
            //getFieldById("customfield_10325").setDescription("Enter Payer ID") //PID
            //getFieldById("customfield_10325").setRequired(true) //PID
            return"""{panel:title=UPDATE EXISTING PAYER section - all fields required.|borderStyle=solid|borderColor=#cccccc|titleBGColor=#00ffff}
            *\\*Payer ID:* 
            {panel}
            {panel:title=CLAIMS BILLING (FLAVOR) section - Only populate fields that are to be updated|borderStyle=solid|borderColor=#cccccc|titleBGColor=#7fffd4}
            *Are These Encounter Claims?* {_}(Y/N{_}){*}:{*} 
            ^If “N” claims go to Next Gen. If "Y", claims go to Change Health Care.^
            ----
            *Are you adding a 'New Flavor'?* {_}(Y/N){_}{*}:{*} 
            ^Flavor=Customizations such as place of service or modifiers, if "Y" attach Billing Attribute form: [https://confluence.teladoc.net/x/4RQepw]^
            *Receiver ID:* 
            ^Must be approved by Revenue Cycle Management^
            *Claim Gateway ID:* 
            ^Will be driven by Encounter claims^
            *Rendering Provider* {_}(Individual Dr./Provider PA){_}{*}:{*} 
            ^Do you want the Individual Dr. or Provider?^
            *Billing Provider State:* 
            *Provider Role TIN Override:* 
            ^Default is 'None'.^
            *Max Daily Claims:* 
            *Preferred Clearinghouse:* 
            ^Example: Ability Solutions, Change Healthcare, Clearinghouse Encounter Claim, Nextgen, etc.^ 
            {panel}
            {panel:title=REAL TIME ELIGIBILITY (RTE) section - Only populate fields that are to be updated.|borderStyle=solid|borderColor=#cccccc|titleBGColor=#87cefa}
            *RTE Fields?* {_}(Y/N){_}: 
            ^If "Y" create a separate Jira ticket for the Technical Analyst team, providing updated fields only.^
             
            {panel}"""
        break;
        case "new rte/product":"""{panel:title=New RTE/Product - Fields with an *(asterisk) are required!|borderStyle=solid|borderColor=#cccccc|titleBGColor=#00ffff}
            {color:#de350b}_*NOTE- Before scheduling a call with the client, a TEIT Jira ticket must be created. Please also be sure to connect internally with the Technical Analyst assigned to the implementation.*_{color}
            ||Required Fields||               Answer               ||
            |*Payer name| |
            |*Payer Id| |
            |*Launch Date| |
            |*Products launching| |
            ||Optional Field||Answer||
            |Legacy group Id
            ^{color:#c1c7d0}(only needed for staged to RTE requests){color}^| |
            The RTE configuration document must be attached to this ticket once completed. Configuration templates and RTE implementation process can be found [here.|https://confluence.teladoc.net/pages/viewpage.action?spaceKey=PMBA&title=Technical+Analysts+Role+in+Eligibility]
               *'* ^[https://confluence.teladoc.net/x/TgJ1qQ]^
             
            {panel}"""
        break;
        case "rte issues":
            return """{panel:title=REAL-TIME ELIGIBITY - Fields with an *(asterisk) are required!|borderStyle=solid|borderColor=#cccccc|titleBGColor=#00ffff}
            *\\*+Payer:+* 
            {color:#c1c7d0}~Please attach (drag and drop) any examples on this ticket.~{color}
            ||Question||   Answer   ||
            |*\\*Product type the issue occurred for?*
            ^{color:#c1c7d0}(GM, MH, Derm, Nutrition, VPC){color}^| |
            |*\\*Members first and last name?*| |
            |*\\*HPID?*| |
            |*\\*Members DOB?*
            ^{color:#c1c7d0}(Enter in MM/DD/YYYY format){color}^| |
            |*Consultation ID?*
            ^{color:#c1c7d0}(If applicable){color}^| |
            |*\\*Overview of the issue*| |
            {color:#c1c7d0}^Please attach (drag and drop) any examples on this ticket.^{color}
             
            {panel}"""
        break;
        case "ccm migrations":
            return """{panel:title=CCM Migrations - Fields with an *(asterisk) are required!|borderStyle=solid|borderColor=#cccccc|titleBGColor=#00ffff}
             
            ||Required Fields||               Answer               ||
            |*\\*Payer name:*| |
            |*\\*CCM Products launching:*
            {color:#c1c7d0}^Please list all^{color}| |
            |*\\*Launch Date:*| |
            |*\\*Service type code:* 
            {color:#c1c7d0}^Will be sent in the 270 request^{color}
            {color:#505f79}_Default: STC 30 will be used unless otherwise requested._{color}| |
            |*\\*Group mapping:* 
            {color:#c1c7d0}^Ref used in mapping members to a group^{color}
            {color:#505f79}_Default: Standard ref 6p used for mapping new payers, if payer is existing and uses something other then 6p we will follow the existing setup._{color}| |
            |*\\*Segment:* 
            {color:#c1c7d0}^To determine eligibility^{color}
            {color:#505f79}_Default: Standard "Eb*1" and "STC 30" for new payers, if payer is existing and uses something other then "Eb*1" and "STC 30", we will follow the existing setup._{color}| |
            ||Optional Fields||Answer||
            |*External Payer ID:*
            ^{color:#c1c7d0}For new payers only{color}^| |
            |*Legacy group Id:*
            ^{color:#c1c7d0}To determine what payer we should use (only in the case of multiple payers with the same name).{color}^| |
            *Test Members:* {color:#505f79}_(Please use the test member template linked in [this doc.|https://teladocpa.sharepoint.com/:x:/r/sales/acctmgmt/Client%20Operations/clientintegration/_layouts/15/Doc.aspx?sourcedoc=%7BD019618C-A462-4BD5-AB1A-8EF4CBE49B4F%7D&file=RTE%20Test%20members%20template.xlsx&action=default&mobileredirect=true&cid=a22f6b2e-4c4b-44c2-9c97-2c0cfd1359e3] _{color}
            ^{color:#c1c7d0}Format: HPID, First and last name, DOB{color}^ 
             
            {panel}"""
        break;
        default:
            return """"""
        break;
    }
}