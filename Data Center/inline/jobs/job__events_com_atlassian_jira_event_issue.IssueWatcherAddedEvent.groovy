import com.atlassian.jira.component.ComponentAccessor
import com.atlassian.jira.event.issue.IssueWatcherAddedEvent
import com.atlassian.jira.user.ApplicationUser
import com.atlassian.mail.Email
import org.apache.log4j.Logger

// Set up logging
def listenerlog = Logger.getLogger("com.onresolve.scriptrunner.runner.ScriptRunnerImpl")
//listenerlog.debug("WatcherNotification Listener has started")

// Fetch the issue, watcher and current user from the IssueWatcherAddedEvent event
def issueWatcherAddedEventobj = event as IssueWatcherAddedEvent
def issue = issueWatcherAddedEventobj.getIssue()
def watcher = issueWatcherAddedEventobj.getApplicationUser()
def user = ComponentAccessor.getJiraAuthenticationContext().getLoggedInUser()

// Get the email address of the watcher and current user
def watcherEmailAddress = watcher?.emailAddress
def userEmailAddress = user?.emailAddress

// Disabled Logging for testing
// listenerlog.warn("Issue: ${issue.toString()}")
// listenerlog.warn("Watcher Email Address: ${watcherEmailAddress.toString()}")
// listenerlog.warn("Current User Email Address: ${userEmailAddress.toString()}")

// If the watchers email address exists, and does not match the current user
// (don't notify the user if they added themselves as a watcher)
if (watcherEmailAddress && (watcherEmailAddress != userEmailAddress)) {

    // Send the email
    Mail.send {
        setTo(watcherEmailAddress.toString())
        setSubject("You have been added as a watcher to ${issue.key}")
        setBody("Dear ${watcher?.displayName},\n\n" +
            "You have been added as a watcher to ${issue.key} by ${user?.displayName}.\n\n" +
            "    [${issue.key}]  ${issue.summary}\n" +
            "    ${issue.url}\n\n" +
            "Please keep an eye on its progress.\n\n" +
            "Regards,\n" +
            "Your Team\n\n\n" +
            "Please do NOT reply to this email.\n" +
            "  -Jira Administration")
    }
}

//listenerlog.debug("WatcherNotification Listener has finished")