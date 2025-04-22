// Checks if the "Category" field has been changed
def changedFieldValue = getFieldById(getFieldChanged()).getValue()
def description = getFieldByName("Description")

log.warn("Category changed to: '$changedFieldValue'")

def actionName = getActionName()
log.warn("Action was: '$actionName'")

if (getActionName() in ["Create Issue","Create"]) {

    if (changedFieldValue == "New SSO configuration request") {

        def strDescription = """
        *Customer Name:*

        *Org ID* (if applicable):

        *Group ID* (if applicable):

        *Eligibility Type* (Example: Eligibility File or RTE or Hybrid):

        *Payer:*

        *Contact Information - Please provide the best point of contact for the following:*
        * Coordinating the implementation and testing phases
        * Troubleshooting any production issues after implementation
        * Updating certificates

        *Timeline/Expected Go Live Date* (NOTE: New SSO requests take 4-6 weeks. It can be faster based on information gathering):

        *Additional Details:*

        *NOTE:*  _(These links only become clickable once the ticket has been created)_ Please provide [Technical Specifications Overview.docx|https://teladocpa.sharepoint.com/:w:/r/sites/Solution/Shared%20Documents/General/Integrations/USGH/SSO/Client%20Documents/Technical%20Specifications%20Overview.docx?d=wa0b6a116745c40af91423483614e46f7&csf=1&web=1&e=SoGAad] and [Teladoc Single Sign On Business Requirement Document Template.docx|https://teladocpa.sharepoint.com/:w:/r/sites/Solution/Shared%20Documents/General/Integrations/USGH/SSO/Client%20Documents/Teladoc%20Single%20Sign%20On%20Business%20Requirement%20Document%20Template.docx?d=w724f66f9b8954e6f9ee057160bc1f75a&csf=1&web=1&e=0iRzaQ] to new SSO client. 
        * If you cannot open above files, move your mouse cursor to [Technical Specifications Overview.docx|https://teladocpa.sharepoint.com/:w:/r/sites/Solution/Shared%20Documents/General/Integrations/USGH/SSO/Client%20Documents/Technical%20Specifications%20Overview.docx?d=wa0b6a116745c40af91423483614e46f7&csf=1&web=1&e=SoGAad] or [Teladoc Single Sign On Business Requirement Document Template.docx|https://teladocpa.sharepoint.com/:w:/r/sites/Solution/Shared%20Documents/General/Integrations/USGH/SSO/Client%20Documents/Teladoc%20Single%20Sign%20On%20Business%20Requirement%20Document%20Template.docx?d=w724f66f9b8954e6f9ee057160bc1f75a&csf=1&web=1&e=0iRzaQ]. Click right mouse cursor and click open in new tab. You will be able to see those documents. Please get back to this ticket when you get completed Teladoc Single Sign On Business Requirement Document word file from a client.
        """

        description.setFormValue(strDescription)
    } else if (changedFieldValue == "Existing SSO configuration (Adding or removing groups) request") {

        def strDescription = """
        *Customer Name:*  

        *Issuer/Entity ID:*  

        *Adding or Removing SSO:*  

        *Org ID* (if applicable):  

        *Group ID* (if applicable): 

        *Eligibility Type* (Example: Eligibility File or RTE or Hybrid):

        *Contact Information - Please provide the best point of contact for the following:*
        * (Adding groups only) *testing phases and troubleshooting any production issues*

        *Expected Due Date:*  

        *Additional Details:*
        """

        description.setFormValue(strDescription)
    } else if (changedFieldValue == "SSO troubleshooting request") {

        def strDescription = """
        *Customer Name:*  

        *Issuer/Entity ID:*  

        *Org ID* (if applicable):  

        *Group ID* (if applicable): 

        *Eligibility Type* (Example: Eligibility File or RTE or Hybrid):

        *Please provide detailed issue description* (It is helpful to troubleshoot faster):  

        *Issue Member information* (Please provide *First name, Last name, Date of Birth*, and *Health Plan ID*):

        *NOTE: Please get SAML SSO response log from the client. If you get SAML SSO response logs from clients and attach in this Jira ticket, it is faster to troubleshoot.*
        """

        description.setFormValue(strDescription)
    } else if (changedFieldValue == "SSO certificate (x509 certificate) renewal request") {

        def strDescription = """
        *Customer Name:*

        *Issuer/Entity ID:*

        *Expiration Date of Existing Cert:*

        *Attach x509 certificate file* (.txt file):

        *Contact Information - Please provide the best point of contact for the following:*
        * Any issues with the X509 cert(s)

        *Additional Details:*

        *Note:* Certificate renewals generally do not require a coordinated call as the Teladoc system can handle multiple certs. However, if the customer prefers to arrange a time for cert update, please indicate that in additional details.
        """

        description.setFormValue(strDescription)
    } else if (changedFieldValue == "Adding test member in UAT environment request") {

        def strDescription = """
        *Customer Name:*  

        *Issuer/Entity ID:*  

        *Org ID (if applicable):*  

        *Group ID (if applicable):*  

        *Eligibility Type* (Example: Eligibility File or RTE or Hybrid):  

        *Test Member information* (Please provide *First name, Last name, Date of Birth*, and *Health Plan ID*):

        *Expected Due Date:*  

        *Additional Details:*  
        """

        description.setFormValue(strDescription)
    }

}