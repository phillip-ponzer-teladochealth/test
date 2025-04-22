//This is just a quick script to notify me if a ticket gets moved into the CHG project.
//If needed this can be re-written to be more flexible.

import com.atlassian.jira.component.ComponentAccessor

def currentIssue = ComponentAccessor.issueManager.getIssueObject(event.issue.id);

Mail.send {
    setTo('jeff.melies@teladochealth.com')
    setSubject('Issue moved into CHG project')
    setHtml()
    setBody("""An issue was just moved into CHG: <b>${currentIssue.getKey()}</b>.""")
}
//Add a comment
currentIssue.set {
setComment("""This issue was moved into CHG by <b>${currentIssue.creator.displayName}</b> and may not have been through the correct workflow statuses.""")
}