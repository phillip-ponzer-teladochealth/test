/*
Creator: Jeff Melies
Purpose: Impacted Countries = Other display 'Other' field
Change log:
*/
//imports
import com.atlassian.jira.component.ComponentAccessor
import com.atlassian.jira.issue.Issue
import com.onresolve.jira.groovy.user.FieldBehaviours
import groovy.transform.BaseScript
import org.apache.log4j.Level
log.setLevel(Level.INFO)

Issue issue = UnderlyingIssue

@BaseScript FieldBehaviours fieldBehaviours

def impactedCountries = getFieldById(getFieldChanged())
String impactedCountriesValue = impactedCountries?.getValue().toString()
def other = getFieldByName("Other")
other.setHidden(true).setRequired(false)

if(impactedCountriesValue.contains('Other')){  //} == 'Other'){ == 'Other'){
    other.setHidden(false).setRequired(true)
}