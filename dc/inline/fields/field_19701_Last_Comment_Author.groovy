import com.atlassian.jira.component.ComponentAccessor

def commentManager = ComponentAccessor.commentManager
def comment = commentManager.getLastComment(issue)

if (comment) {
    comment.authorApplicationUser
}