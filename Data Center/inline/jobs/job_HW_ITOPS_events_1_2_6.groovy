import com.atlassian.jira.component.ComponentAccessor
import com.atlassian.jira.event.issue.IssueEvent
import com.atlassian.jira.issue.Issue
import com.atlassian.jira.issue.IssueFieldConstants
import com.atlassian.jira.user.ApplicationUser
import org.ofbiz.core.entity.GenericValue

import static com.atlassian.jira.event.type.EventType.ISSUE_CREATED_ID

//Logging
log.info("Listener - Device, IT, IT Operations Projects  'Add watchers' has started")

boolean hasChange(event, String fieldName) {
    if (!(event instanceof IssueEvent)) {
        log.info("Listener - Device, IT, IT Operations Projects  'Add watchers' has completed with no changes")
        return false;
    }
    try {
        GenericValue changeLog = ((IssueEvent) event).changeLog
        if (changeLog == null) {
            log.info("Listener - Device, IT, IT Operations Projects  'Add watchers' has completed changelog was null")
            return false
        }
        List<GenericValue> changes = changeLog.getRelated("ChildChangeItem");
        log.info("changes: " + changes)
        return changes && changes.find { fieldName == it.get("field") }
    } catch (Exception e) {
        log.error("Error getting ChildChangeItem", e)
        return false;
    }
}

boolean isIssueCreated(event) {
    return event instanceof IssueEvent && ISSUE_CREATED_ID.equals(event.getEventTypeId());
}

def startWatching = { ApplicationUser user, Issue issue ->
    if (user) {
        log.info("[${issue.key}] Adding ${user.name} to watchers")
        ComponentAccessor.watcherManager.startWatching(user, issue)

    }
}

if (event.comment) {
    startWatching(event.comment.authorApplicationUser, event.comment.issue)
} else {
    if (isIssueCreated(event) || hasChange(event, IssueFieldConstants.ASSIGNEE)) {
        startWatching(event.issue.assignee, event.issue)
    }
    if (isIssueCreated(event) || hasChange(event, IssueFieldConstants.REPORTER)) {
        startWatching(event.issue.reporter, event.issue)
    }
}
log.info("Listener - Device, IT, IT Operations Projects  'Add watchers' has completed")