package com.teladochealth.mailHandlers

/*
Author: Jeff Melies
Function: Mail Handler that will create an issue, or add a comment.
		Sets Priority according to the emails priority.
		Will email the original email sender of the ticket creation and ticket # even if they are not in our Jira instance.
        will populate Labels field with 'support'.
        will populate Email field with email addresses from incoming mail
Requirements: This handler requires the custom field 'Email'	
Change log: 8/15/2022 I added attachment functionality
*/
import com.atlassian.jira.config.util.JiraHome
import com.atlassian.jira.exception.CreateException
import com.atlassian.jira.component.ComponentAccessor
import com.atlassian.jira.service.util.ServiceUtils
import com.atlassian.jira.service.util.handler.MessageUserProcessor
import com.atlassian.jira.service.util.handler.MessageHandlerContext
import org.apache.commons.io.FileUtils
import com.atlassian.jira.user.ApplicationUser
import com.atlassian.jira.user.util.UserManager
import com.atlassian.mail.MailUtils
import javax.mail.internet.InternetAddress
import com.atlassian.jira.mail.Email
import com.atlassian.mail.queue.SingleMailQueueItem
import com.atlassian.jira.service.services.file.FileService
import org.apache.commons.io.FileUtils
import com.atlassian.jira.issue.MutableIssue
import com.atlassian.jira.issue.fields.renderer.IssueRenderContext
import com.atlassian.jira.issue.RendererManager
import com.atlassian.jira.event.type.EventDispatchOption
import com.atlassian.jira.issue.comments.CommentManager
import com.atlassian.jira.issue.label.LabelManager
import com.atlassian.jira.issue.Issue
import javax.mail.Message
import javax.mail.Address
import org.apache.log4j.Level

//log.setLevel(Level.INFO)

//Managers 
JiraHome jiraHome = ComponentAccessor.getComponent(JiraHome)
def constantsManager = ComponentAccessor.constantsManager
def userManager = ComponentAccessor.getComponent(UserManager)
def projectManager = ComponentAccessor.getProjectManager()
def issueFactory = ComponentAccessor.getIssueFactory()
def messageUserProcessor = ComponentAccessor.getComponent(MessageUserProcessor)
def watcherManager = ComponentAccessor.getWatcherManager()
def im = ComponentAccessor.getIssueManager()
def commentManager = ComponentAccessor.getCommentManager()
def rendererManager = ComponentAccessor.getComponent(RendererManager)
def cfManager = ComponentAccessor.getCustomFieldManager()
LabelManager labelManager = ComponentAccessor.getComponent(LabelManager)

//Jira Admins can change these variables to fit their requirements
final defaultIssueType = 'Task'
final defaultFallbackPriority = 'Low'
final defaultReporterName = 'it_worker' 
final mailHandlerEmail = 'udf_engineering_helpdesk@teladochealth.com'//11/8/23 JBM 'udf_Engineering_support@teladochealth.com' //This is used if the email going back to the user is different then the user creating the ticket
final projectKey = "UDF"
final freeTextField = "Email"  //Enter a free text custom field to hold the email authors email address
final labels = "support"

//Jira admins shouldn't need to modify these
ApplicationUser defaultFallbackUser = userManager.getUserByName(defaultReporterName)  //defaultFromName = 
log.info("defaultFallbackUser: ${defaultFallbackUser.getDisplayName()}, ${defaultFallbackUser.getEmailAddress()}")
def project = projectManager.getProjectObjByKey("${projectKey}")
final sendCommentNotAppliedEmail = 'true'



				log.info("*********************${project.getKey()}_MAILHANDLER**********************")
//This map defines an example relationship between X-Priority and a Jira Priority Name.
final priorityHeaderValueToJiraPriorityName = [
    '5': 'Lowest',
    '4': 'Low',
    '3': 'Medium',
    '2': 'High',
    '1': 'Highest',
]
//Script variables and should not be modified
def cfMail = cfManager.getCustomFieldObjectsByName(freeTextField).first()
def mailSubject = message.getSubject() as String 
def mailBody = MailUtils.getBody(message) as String
def mailSenders = MailUtils.getSenders(message).join(',')  //Example: jeff.melies@teladochealth.com
ApplicationUser jiraReporter = messageUserProcessor.getAuthorFromSender(message) ?: defaultFallbackUser // <3>
def mailCCRecipients = message.getRecipients(Message.RecipientType.CC) as List<InternetAddress>
def existingIssue = ServiceUtils.findIssueObjectInString(mailSubject) //Existing Issue?
def mailPriority = message.getHeader('X-Priority')?.first() //Get eMail priority
def translatedPriority = priorityHeaderValueToJiraPriorityName[mailPriority]  //convert email priority to Jira priority
def derivedPriority = constantsManager.priorities.findByName(translatedPriority) ?: defaultFallbackPriority  //ensure we can find the email priority or use default
def mailAttachments = MailUtils.getAttachments(message) 

def wikiRenderer
def renderContext
def htmlDesc

List ccEmailAddresses = []
if(mailCCRecipients){
    mailCCRecipients.each{
        ccEmailAddresses.add(it.address.toString())
    }
}

def jiraDesc = ""
if(!(mailCCRecipients)) {
        jiraDesc = """{color:grey}Ticket created via e-mail (To line): ${mailSenders.toString()}.{color}
            \\\\
            ${mailBody}
            \\\\
            ----
            """ + """\\\\"""
}else{
      jiraDesc = """{color:grey}Ticket created via e-mail (To line): ${mailSenders.toString()}
            These people were on the CC line: ${mailCCRecipients.join(", ").toString()}{color}
            \\\\
            ${mailBody}
            \\\\
            ----
            """ + """\\\\"""    
}
def warnBody = """<b><u>Warning:</u></b> The comment was not received with a subject line:<br>
            ${mailSubject}<br>
            <br>
            We could not locate this ticket, if you are trying to add a comment navigate to <a href="https://jira.teladoc.net/issues/?jql=project%20%3D%20${projectKey}%20AND%20issuetype%20%3D%20Task%20ORDER%20BY%20created%20DESC">Jira</a>,
            locate the issue, and click 'Add comment' at the bottom of the screen, or 'respond' to an email from ${mailHandlerEmail} with the proper header and subject line.<br>
            <br>
            example subject line: [Teladoc Health] Issue Created ${project.getKey()}-(ticket number): (followed by original subject).<br>
            <br>Thank you,<br>
            UDF Engineering Helpdesk<br>
			"""
//def jiraComment 
//if(!(mailCCRecipients)) {
//    jiraComment = """{color:grey}This comment was emailed in by: ${mailSenders.toString()}.{color}
//            
//            Comment:
//            ${mailBody}"""
//}else{
//    jiraComment = """{color:grey}This comment was emailed in by: ${mailSenders.toString()}.
//        These people were on the CC line: ${mailCCRecipients.join(", ").toString()}{color}
//            
//            Comment:
//            ${mailBody}"""        
//}

//Functions
def funcComment(String ccCommentRecipents, String sendersComment, String bodyComment){ 
    def jiraComment
    if(ccCommentRecipents == null) {
        jiraComment = """{color:grey}This comment was emailed in by: ${sendersComment}.{color}
            
            Comment:
            ${bodyComment}"""
    }else{
        jiraComment = """{color:grey}This comment was emailed in by: ${sendersComment}.
        These people were on the CC line: ${ccCommentRecipents}{color}
            
            Comment:
            ${bodyComment}"""        
    }
    return jiraComment
}
def jiraMail(List jmArgs){
    String jmSenders 	= jmArgs[0]
    String jmCCs 		= jmArgs[1]
    String jmFrom		= jmArgs[2]
    String jmSubject 	= jmArgs[3]
    String jmBody	 	= jmArgs[4]
    
        def email = new Email(jmSenders) //senders
        if(jmCCs){email.setCc(jmCCs)} //ccEmailAddresses
//        email.setBcc("Jeff Melies <jeff.melies@teladochealth.com>")
        email.setFrom(jmFrom) //defaultFromName  //defaultReporterName
        email.setSubject(jmSubject)  //" Issue Created ${issue}: ${subject}"
        email.setBody(jmBody) //"""<p>A new ticket was created from your eMail:</p>${htmlDesc}"""
        email.setMimeType("text/html") 
        try {
            SingleMailQueueItem item = new SingleMailQueueItem(email)
            ComponentAccessor.getMailQueue().addItem(item)
        } catch (CreateException ex) {
            log.error("ScriptRunner Incoming Mail_Handler for  - Mail Handler received an error creating eMail: ", ex)
        }    
    
}

def destination = new File(jiraHome.home, FileService.MAIL_DIR).getCanonicalFile()
def addAttachments(attach, iss, attchUser, dest){
    //If there are attachments add them to the ticket
	attach.each { MailUtils.Attachment attachment ->
//    	def destination = new File(jiraHome.home, FileService.MAIL_DIR).getCanonicalFile()
        def file = FileUtils.getFile(dest.toString(), attachment.filename.toString()) as File 
        FileUtils.writeByteArrayToFile(file, attachment.contents)
        messageHandlerContext.createAttachment(file, attachment.filename, attachment.contentType, attchUser, iss) 
    }
}

def firstChrSubject = mailSubject.substring(0,4).toUpperCase()
def lowerSubject
def jiraEmailArgs = []
//Check for an existing issue
if(!existingIssue){
    //If we didn't find an existing issue, check the first 4 letters of the subject for RE, FW, and FWD
    log.debug("ScriptRunner Incoming Mail_Handler for ${project.getKey()} - existingIssue - Was not found.")
    if (!mailBody) {
        log.error("ScriptRunner Incoming Mail_Handler for ${project.getKey()} - mail has no body content so nothing will happen.")
        return 'ScriptRunner Incoming Mail_Handler for ${project.getKey()} - mail has no body content so nothing will happen.'
    }
    
    lowerSubject = mailSubject.toLowerCase()
    log.debug("*************ScriptRunner Incoming Mail_Handler for ${project.getKey()} - firstChrSubject: ${firstChrSubject}******lowerSubject: ${lowerSubject}******************")
    if((firstChrSubject.contains("RE: ") || firstChrSubject.contains("FW: ") || firstChrSubject.contains("FWD: ")) && (!lowerSubject.contains("[teladoc health] ") && !lowerSubject.contains(project.getKey()+"-"))){
        log.debug("ScriptRunner Incoming Mail_Handler for ${project.getKey()} - Send Warning about comment from wrong email. Subject: ${mailSubject}")
        jiraEmailArgs = [mailSenders.toString(),"","${defaultFallbackUser.getEmailAddress().toString()}","Warning: Comment was not received by UDF Engineering Helpdesk!","${warnBody}"]
        //jiraEmailArgs = ["jeff.melies@teladochealth.com","","${mailHandlerEmail}","Warning: Comment was not received by UDF Engineering Helpdesk!","${warnBody}"]
        try{
            if(sendCommentNotAppliedEmail == 'true'){jiraMail (jiraEmailArgs)}
        } catch (CreateException ex) {
        	log.error("ScriptRunner Incoming Mail_Handler for ${project.getKey()} for warnBody emails Mail Handler received an error creating eMail: ", ex)
    	}   	
	return "ScriptRunner Incoming Mail_Handler for ${project.getKey()} for warnBody emails - email sent, User needs to reply to an email from UDF Engineering Helpdesk."
    	//-----------------------------------------------------------------------------------------------
    }else{
        Issue newIssue
        log.debug("*****${project.getKey()}_MAILHANDLER*****Creating a new issue************")
        def issueObject = issueFactory.getIssue()
            issueObject.setProjectObject(project)
            issueObject.setSummary(mailSubject ?: 'Created via ScriptRunner mail handler')
            issueObject.setDescription("${jiraDesc}")  //(MailUtils.getBody(message))
            issueObject.setIssueTypeId(project.issueTypes.find { it.name == defaultIssueType }.id)
            issueObject.setReporter(jiraReporter)
//            issueObject.setPriority(derivedPriority)
            issueObject.setCustomFieldValue(cfMail, mailSenders)

            try{
                newIssue = messageHandlerContext.createIssue(jiraReporter, issueObject) as MutableIssue
                
            } catch (CreateException e) {
                log.error("ScriptRunner Incoming Mail_Handler for ${project.getKey()}: Error creating issue: ", e)
            }
            
        try {
            labelManager.addLabel(jiraReporter, newIssue.getId(), labels, false)
            watcherManager.startWatching(jiraReporter, newIssue)
        } catch (CreateException e) {
            log.error("Mail_Handler - ${project.getKey()} - ${defaultIssueType} - Mail Handler received an error adding Watcher ${newIssue}: ", e)
            return 'Mail Handler received an error updating issue'
        }
        log.debug("Mail_Handler - ${project.getKey()} - ${defaultIssueType} - Mail Handler created: ${newIssue.key} successfully.")
        wikiRenderer = rendererManager.getRendererForType("atlassian-wiki-renderer")
        renderContext = new IssueRenderContext(newIssue)
        htmlDesc = wikiRenderer.render(newIssue.description, renderContext)
        
        if(ccEmailAddresses){
            jiraEmailArgs = [mailSenders.toString(),ccEmailAddresses.join(", ").toString(),"${mailHandlerEmail}"," Issue Created ${newIssue}: ${mailSubject}","""<p>A new ticket was created from your eMail:</p>${htmlDesc}"""]
            //jiraEmailArgs = ["jeff.melies@teladochealth.com",ccEmailAddresses.join(", ").toString(),"${mailHandlerEmail}"," Issue Created ${newIssue}: ${mailSubject}","""<p>A new ticket was created from your eMail:</p>${htmlDesc}"""]
        }else{
            jiraEmailArgs = [mailSenders.toString(),"","${defaultFallbackUser.getEmailAddress().toString()}"," Issue Created ${newIssue}: ${mailSubject}","""<p>A new ticket was created from your eMail:</p>${htmlDesc}"""]
            //jiraEmailArgs = ["jeff.melies@teladochealth.com","","${mailHandlerEmail}"," Issue Created ${newIssue}: ${mailSubject}","""<p>A new ticket was created from your eMail:</p>${htmlDesc}"""]
        }
        try{
            jiraMail (jiraEmailArgs)
        } catch (CreateException ex) {
        	log.error("ScriptRunner Incoming Mail_Handler for ${project.getKey()} for NEW ISSUE CREATED Mail Handler received an error creating eMail: ", ex)
    	}
        //If there are attachments add them to the ticket
        if(mailAttachments){
            addAttachments mailAttachments, newIssue, jiraReporter, destination
        }        
    }
}else{
    log.debug("*****${project.getKey()}_MAILHANDLER***** Found existing Issue:${existingIssue} - Adding a comment.*****")
    def splitBody
    String commentBody
    if (!mailBody) {
        log.error("Mail_Handler - ${project.getKey()} - ${defaultIssueType} - mail has no body content so comment will not be added")
        return 'Mail_Handler - ${project.getKey()} - ${defaultIssueType} - mail has no body content so comment will not be added'
    }else{  //Parse the email body
            splitBody = mailBody.split("From: ${mailHandlerEmail}")
            commentBody = funcComment mailCCRecipients.toString(), mailSenders.toString(), splitBody[0].toString()
    }
    def splitSub
    String commentSubject
    if(firstChrSubject.contains("RE: ") || firstChrSubject.contains("FW: ") || firstChrSubject.contains("FWD: ")){ mailSubject = mailSubject.substring(4)}
    if(mailSubject.count(":")==1){
        splitSub = mailSubject.split(':')
        commentSubject = splitSub[0].toString()
    }else{
        splitSub = mailSubject.split(':')
        commentSubject = splitSub[1].toString()
    }
    final dispatchCommentedEvent = true   // should dispatch Issue Commented event
    final allowCommentsFromUnregisteredUsers = false   // allow comments from non-jira users
    log.debug("*****Adding a comment using existingIssue: ${existingIssue}, jiraReporter: ${jiraReporter}, commentBody: ${commentBody}*****")
//    commentManager.create(existingIssue,jiraReporter, jiraComment, true)
    commentManager.create(existingIssue,jiraReporter, commentBody, true)
    try{        
        im.updateIssue(jiraReporter, existingIssue as MutableIssue, EventDispatchOption.ISSUE_UPDATED, true)
    } catch (CreateException ex) {
        	log.error("ScriptRunner Incoming Mail_Handler for ${project.getKey()} adding a Comment received an error: ", ex)
    }        
    //If there are attachments add them to the ticket
    if(mailAttachments){
        log.debug("*****Adding a comment with Attachments using destination: ${destination}*****")
        try{
            addAttachments mailAttachments, existingIssue, jiraReporter, destination
        } catch (CreateException ex) {
        	log.error("ScriptRunner Incoming Mail_Handler for ${project.getKey()} adding Attachments received an error: ", ex)
    	}
    }
//        wikiRenderer = rendererManager.getRendererForType("atlassian-wiki-renderer")
//        renderContext = new IssueRenderContext(existingIssue)
//        def htmlComment = wikiRenderer.render(existingIssue.description, renderContext)
//    //jiraEmailArgs = [mailSenders.toString(),"","${defaultFallbackUser.getEmailAddress().toString()}"," Issue Created ${newIssue}: ${mailSubject}","""<p>A new ticket was created from your eMail:</p>${htmlDesc}"""]
//    jiraEmailArgs = ["jeff.melies@teladochealth.com","","${mailHandlerEmail}"," Issue Updated ${existingIssue}: ${commentSubject}","""<p>A comment was added to ticket ${existingIssue} from your eMail:</p>${htmlComment}"""]
//    try{
//        jiraMail (jiraEmailArgs)
//    } catch (CreateException ex) {
//        log.error("ScriptRunner Incoming Mail_Handler for ${project.getKey()} for NEW ISSUE CREATED Mail Handler received an error creating eMail: ", ex)
//    }
}
