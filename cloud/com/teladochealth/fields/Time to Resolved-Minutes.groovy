import org.apache.groovy.dateutil.extensions.DateUtilExtensions
import java.time.DayOfWeek
import java.text.SimpleDateFormat
import java.text.DateFormat
import groovy.time.TimeCategory

/*Author: Jeff Melies
Purpose: Captures Time to Resolution. 
         Using the issues status categories define a start and end datetime, we search the history of the ticket 
         and calculate the time between them. You can also exclude weekends and define the
         results in milliseconds, seconds, minutes, hours, or days.
Change log: (4/21/2025) Phillip Ponzer Converted to Cloud
*/

int getTimeToResolution(String issueKey, String startStatusCategory, String endStatusCategory, String timeUnit="minutes", boolean excludeWeekends=true) {
    assert startStatusCategory.toLowerCase() in ["to do","in progress","done"] : "Start Status Category must be one of: To Do, In Progress, Done"
    assert endStatusCategory.toLowerCase() in ["to do","in progress","done"] : "End Status Category must be one of: To Do, In Progress, Done"
    assert startStatusCategory.toLowerCase() != endStatusCategory.toLowerCase() : "Start and End Status Category must be different"
    assert timeUnit.toLowerCase() in ["days","hours","minutes","seconds","milliseconds"] : "Time Unit must be one of: Days, Hours, Minutes, Seconds, Milliseconds"

    SimpleDateFormat dateFormat = new SimpleDateFormat("yyy-MM-dd'T'HH:mm:ss.SSSZ")
    Map<String, String> statusCategories = [:]

    Map issueWithHistories = get("rest/api/3/issue/$issueKey?expand=changelog").asObject(Map).body
    List<Map> histories = issueWithHistories.get("changelog").asType(Map).get("histories").asType(List).reverse()
    Date startDate = null
    Date endDate = null

    // Get start and end dates
    for( history in histories ) {
        for( item in history.get("items").asType(List<Map>) ) {
            if( item.get("fieldId") != "status" ) {
                continue
            }
        
            String toStatus = item.get("toString")
            String toStatusID = item.get("to")
            String toStatusCategory = statusCategories.get(toStatus) ?: null
            if( toStatusCategory == null ) {
                toStatusCategory = get("/rest/api/3/status/$toStatusID").asObject(Map).getBody().get("statusCategory").asType(Map).get("name")
                statusCategories.put(toStatus, toStatusCategory)
            }

            if( toStatusCategory == startStatusCategory ) {
                startDate = dateFormat.parse(history.created.toString())
                logger.info("startDate = $history.created")
            } else if( toStatusCategory == endStatusCategory ) {
                endDate = dateFormat.parse(history.created.toString())
                logger.info("endDate = $history.created")
            }

            if( startDate != null && endDate != null ) {
                break
            }
        }

        if( startDate != null && endDate != null ) {
            break
        }
    }
    
    if( startDate == null || endDate == null ) {
        return 0
    }

    if( excludeWeekends ) {
        Date firstDate = new Date(startDate.getTime())
        while( firstDate.before(endDate) ) {
            if( firstDate.toDayOfWeek() in [DayOfWeek.SATURDAY, DayOfWeek.SUNDAY] ) {
                startDate = DateUtilExtensions.next(startDate)
            }
            firstDate = DateUtilExtensions.next(firstDate)
        }
    }

    TimeCategory.minus(endDate, startDate).getAt(timeUnit).asType(int)
}

getTimeToResolution(issue.key.toString(), "In Progress", "Done")
