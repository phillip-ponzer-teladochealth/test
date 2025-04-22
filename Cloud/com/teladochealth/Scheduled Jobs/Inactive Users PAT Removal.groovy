/*
Author: Jeff Melies
Purpose: Delete old and unused tokens from our users.
Schedule: This script must run at 03:00 PM, 04:00 PM, 05:00 PM, 06:00 PM, 07:00 PM, 08:00 PM, 09:00 PM, 
10:00 PM and 11:00 PM, on day 1, 8, 15, 22, and 28 of the month
*/

import com.mashape.unirest.http.Unirest.*
import java.lang.Integer
import java.text.SimpleDateFormat 
import java.util.Date
import groovy.json.JsonSlurper
import groovy.json.JsonBuilder
import groovy.transform.Field as tField
import java.lang.String
import java.io.*
import java.net.URL
import java.net.HttpURLConnection
import org.apache.log4j.*
import java.time.*

//Capture the start time to calculate the elapsed time to run this script:
Long startTime = Instant.now().getEpochSecond() as Long


//Global Variables
@tField log_level = Level.INFO    //Options would be (Level.INFO, Level.WARN, Level.DEBUG, Level.ERROR) without quotes.
@tField testRuns = 0              //Set to how many users to execute this script on, 0 for all users.
@tField create_days = 0           //If the tokens create date is more than this figure, we will check to see when it was last used.
@tField used_days = 0             //If the token hasn't been used since this figure, it will be deleted.
@tField userStatus = "INACTIVE"   //Options would be ('ACTIVE', 'INACTIVE')  
@tField accountType = "atlassian" //Options would be ('atlassian', 'customer', or 'null' for all)
@tField currentHour = LocalDateTime.now().getHour() //.atZone(ZoneId.of("America/Chicago")) 
@tField logName = "Scheduled Job: Inactive Users PAT Removal"
@tField regExpUsers
int waitFor = 3                   //Being Rate Limited? Specify the length of time to wait in seconds.

def jsonParser = new JsonSlurper()
Map tokensParsedJson
String atlassianUserId, getUserTokens, returnMessage
StringBuilder finalSB = new StringBuilder()
String getUsers = "https://api.atlassian.com/admin/v1/orgs/${ORG_ID}/users/search"

Logger.getLogger(logName?.toString()) as Logger
log.setLevel(log_level as Level)

//****************************** FUNCTIONS ************************************
def mgmntRequest(String url){
    Logger.getLogger(logName?.toString()) as Logger
    log.setLevel(log_level as Level)
    int responseCode
    String responseText, results
    def headers = ['Authorization': ORG_JBM_API_KEY, 'Accept': 'application/json']

    try{
        HttpResponse<JsonNode> connection = Unirest.get(url)
            .headers(headers)
            .asJson()

        responseCode = connection?.getStatus()
        responseText = connection.getStatusText()
        results = connection?.getBody()

        log.debug("Response Code: ${responseCode}, ${results}")
        if (responseCode == 200) {
            return results
        } else { // Handle other response codes
            log.error("Error: (${responseCode}) ${responseText}")
            return "Error: (${responseCode}) ${responseText}"
        }

    } catch (Exception e) {
        log.error("An error occurred: ${e.message}")
        return
    }
}

def orgRequest(String url, String httpMethod){
    Logger.getLogger(logName?.toString()) as Logger
    log.setLevel(log_level as Level)
    StringBuilder sb = new StringBuilder()
    String response, responseText
    Integer curserId = 1
    def objUnirest, requestBody, pattern
    Map usersParsedJson
    def jsonParser = new JsonSlurper()
    def newURL = "${url}" //new URL(url)
    int responseCode
    def payload// = new JsonBuilder(requestBody).toString()
    HttpResponse<JsonNode> connection
    sb.append("""{"data":[""")

    def headers = ['Authorization': ORG_JBM_API_KEY, 'Content-Type': 'application/json', 'Accept': 'application/json'] //, 'Accept': 'application/json'
    
    while(newURL){
        switch(httpMethod.toLowerCase()){
            case "post":
                requestBody = [
                    "accountStatuses":["${userStatus}"],
                    "emailDomains": ["eq": ["teladochealth.com","teladoc.com"]],
                    "expand":["NAME","EMAIL","EMAIL_VERIFIED"]
                ]
                payload = new JsonBuilder(requestBody).toString()
                connection = Unirest.post(newURL)
                    .headers(headers)
                    .body(payload)
                    .asJson()
                break;
            case "delete":
                connection = Unirest.delete(newURL)
                    .headers(headers)
                    .asJson()
                break;
            default:
                log.error("(orgRequest) Case: DEFAULT")
                break; 
        }

        responseCode = connection?.getStatus()
        responseText = connection.getStatusText()
        response = connection?.getBody()

        log.debug("(orgRequest) responseCode: ${responseCode}, ${response}")

        if(newURL?.toString().contains("/orgs/")){
            if (responseCode != 200) {
                log.error("Error: (${responseCode}): ${responseText}")
                return "Error: (${responseCode}): ${responseText}"
            }
            usersParsedJson = jsonParser.parseText(response?.toString()) as Map
            log.debug("(orgRequest) usersParsedJson: ${usersParsedJson?.data}")

            pattern = regexpUsers(currentHour as Integer)
            if(! pattern){return "There's no users to lookup, check the time this script ran (${currentHour}) and compare with the method regexpUsers."} 
            
            //Sort in alphabet order by email
            usersParsedJson?.data?.sort({ a, b -> a?.email?.toLowerCase() <=> (b?.email?.toLowerCase()) }).eachWithIndex{ user, usrIdx -> 
            //usersParsedJson?.data.eachWithIndex{ user, usrIdx -> 
                if(curserId == 1){
                    if(testRuns as Integer >= usrIdx as Integer){
                        log.debug("(orgRequest) Compare ${user?.accountType} with ${accountType?.toString()}, results: ${user?.accountType == accountType?.toString()}")
                        log.debug("(orgRequest) Complete user Json sample: ${user}")
                        log.debug("(orgRequest) User details being used: account_id: ${user?.accountId}, account_status: ${user?.accountStatus}, account_type: ${user?.accountType}")
                    }
                }

                //If the email hasn't been verified then the user wouldn't have API tokens
                if(user?.emailVerified){
                    if(user?.email?.toString()?.toLowerCase() ==~ pattern){
                        log.debug("""Adding to SB: {"account_id":"${user?.accountId}","account_status":"${user?.accountStatus}","account_name":"${user?.name}","account_nickname":"${user?.nickname}","account_email":"${user?.email}","account_emailVerified":"${user?.emailVerified}","account_statusInUserBase":"${user?.statusInUserbase}","account_type":"${user?.accountType}"}""")
                        sb.append("""{"account_id":"${user?.accountId}","account_status":"${user?.accountStatus}","account_name":"${user?.name}","account_nickname":"${user?.nickname}","account_email":"${user?.email}","account_statusInUserBase":"${user?.statusInUserbase}","account_type":"${user?.accountType}"},""")
                    }
                }
            }  

            //Pagination: get the 'links.next' url string from the previous http response
            String nextURL = usersParsedJson?.links?.next?.toString()
            log.debug("NextURL: ${nextURL}")
            if(nextURL != null){
                newURL = "${nextURL}" //new URL(nextURL) ?: null
                log.debug("(${curserId}) 'next' was found in URL: ${usersParsedJson?.links}")
                curserId = curserId + 1
            }else{
                log.debug("NULL 'next' wasn't found in the response: ${usersParsedJson?.links}")
                newURL = null
                break
            }
        }else{ //Requests not using the Organizations REST AP.
            newURL = null
            return responseCode
        }
    }
    
    if(sb.length() >= 10){
        sb = sb.deleteCharAt(sb.length() - 1)
        sb.append("]}")
    }else{
        sb.setLength(0)
        //sb.append("{Empty:[:]}")
    }
    log.debug("FINAL SB: ${sb.toString()}") 
    return sb.toString()
}

def tokenValidation(String atlassianUserId, Map jsonMap){
    Logger.getLogger(logName?.toString()) as Logger
    log.setLevel(log_level as Level)
    String results
    def tokenId = jsonMap?.id

    //Check if token is disabled, if it is delete it otherwise see if it's being used
    if(jsonMap?.disabledStatus){
        log.info("(tokenValidation) '${jsonMap?.label}' is disabled and will be deleted.")
        results = deleteToken atlassianUserId, jsonMap.id.toString()
        return "${results}"
    }else{
        log.debug("(tokenValidation) Sending token ${jsonMap?.label} to unUsedTokenValidation.")
        results = unUsedTokenValidation (atlassianUserId, jsonMap)
        return "${results}"
    }
}
def unUsedTokenValidation(String atlassianUserId, Map jsonMap){
    Logger.getLogger(logName?.toString()) as Logger
    log.setLevel(log_level as Level)

    String results
    def pattern = "yyyy-MM-dd'T'HH:mm:ss."
    def tokenId = jsonMap?.id //?: null
    def tokenLabel = jsonMap?.label //?: null
    def differenceInHours,differenceInDays, tokenLastAccessTimeInMS
    Date tokenCreatedAt, tokenLastAccess
    tokenCreatedAt = new SimpleDateFormat(pattern).parse(jsonMap?.createdAt?.toString()) ?: null//Date.parse(pattern, jsonMap?.createdAt.toString()) as Date
    if(jsonMap?.toString().indexOf("lastAccess") > 0){
        tokenLastAccess = new SimpleDateFormat(pattern).parse(jsonMap?.lastAccess?.toString())
        tokenLastAccessTimeInMS = tokenLastAccess?.getTime()
    }else{
        tokenLastAccess = null
        tokenLastAccessTimeInMS = 31536000000 as Long //This equals 1 year since jsonMap was empty or null
    }
    def tokenCreateTimeInMS = tokenCreatedAt?.getTime() //?: null
//    def tokenLastAccessTimeInMS = tokenLastAccess?.getTime() ?: null
    def currentDate = new Date()
    def currentTimeInMS = currentDate?.getTime() //?: null

    differenceInDays = (currentTimeInMS - tokenCreateTimeInMS) / (60*60*24*1000) as Integer
    log.info("(unUsedTokenValidation) Token created: ${tokenCreatedAt}, Last Used: ${tokenLastAccess}, Difference (days): ${differenceInDays}") //${differenceInHours/24}, in hours it's: ${differenceInHours}")
    
    //If the token was created > $create_days, check to see if it's been used
    if(differenceInDays > create_days as Integer){
        log.debug("(unUsedTokenValidation) The age of the token is greater than ${create_days} days, we will now check the last time it was used.")
        differenceInDays = (currentTimeInMS - tokenLastAccessTimeInMS) / (60*60*24*1000) as Integer

        //If the token hasn't been used in $used_days than delete it.
        if(differenceInDays > used_days as Integer){
            log.info("(unUsedTokenValidation) Send token ${tokenLabel} to be deleted, the last time it was used was ${differenceInDays} days ago, the threshold is ${used_days} days.")
            results = deleteToken (atlassianUserId, tokenId?.toString())
            return "${results}" //"Deleted: Token ${tokenLabel} was not being used so it has been deleted."
        }
    }else{
        log.info("(unUsedTokenValidation) Retain token ${tokenLabel}, Created: ${tokenCreatedAt}, Last used: ${tokenLastAccess}")
        return "Retained" //: The token (${tokenLabel}) was either recently created ${tokenCreatedAt} or recently used ${tokenLastAccess}."
    }
}

def deleteToken(String atlassianUserId, String tokenId){
    Logger.getLogger(logName?.toString()) as Logger
    log.setLevel(log_level as Level)

    log.debug("(deleteToken) If allowed this token would be removed -- for user: ${atlassianUserId} - Token (${tokenId})")

    String deleteTokenEndpoint = "https://api.atlassian.com/users/${atlassianUserId}/manage/api-tokens/${tokenId}" //as URL //as String
    Integer userTokens_Response = orgRequest(deleteTokenEndpoint.toString(), "DELETE") as Integer
    log.info("(deleteToken) ${userTokens_Response} for user: ${atlassianUserId} - Token (${tokenId})")
    if(userTokens_Response == 204){
        return "Deleted"
    } else {
        return "ERROR-${userTokens_Response} failed"
    }
}

def regexpUsers(Integer currentHour){
    Logger.getLogger(logName?.toString()) as Logger
    log.setLevel(log_level as Level)
    //email begins with
    if(currentHour >= 14 && currentHour <= 23){
        Map selection = [
            14:/^a.*/,      //124 users - 124 seconds
            15:/^[b-d].*/,  //111 users - 165 seconds
            16:/^[e-h].*/,  //78 users - 34 seconds
            17:/^[i-j].*/,  //119 users - 50 seconds
            18:/^[k-l].*/,  //84 users - 36 seconds  j=95, k=43, l=39
            19:/^m.*/,      //109 users - 45 seconds
            20:/^[n-o].*/,  //64 users - 29 seconds
            21:/^[p-r].*/,  //91 users -  seconds
            22:/^s.*/,      //91 users - 40 seconds
            23:/^[t-z].*/   //84 users - 39 seconds
        ]
        regExpUsers = selection[currentHour].toString()
    }else{
        log.error("Check the time this script ran (${currentHour}) and compare with the method regexpUsers.")
        regExpUsers = /1.*/ //I tried null but got some errors, thought I would try a number
    }
    return regExpUsers
}

//************************************Main Script************************************
//Get all Managed Users
def users_Response = orgRequest(getUsers, "Post")
if(users_Response?.length() < 10){return "No users found"}
log.debug("Class: (${users_Response?.class}), Response: ${users_Response}")
//Map usersParsedJson = jsonParser.parseText(users_Response?.toString()) as Map
def usersParsedJson = jsonParser.parseText(users_Response?.toString())
log.info("Total users found: (${usersParsedJson?.data?.size()})")

finalSB.append("Deleted Tokens:[")
for( int idx = 0; idx < usersParsedJson?.data?.size(); idx++){
//    Thread.sleep(waitFor * 1000)
    returnMessage = """(Inactive Users) We ran a total of ${if(testRuns == 0){idx+1}else{idx}} iteration(s) on ${usersParsedJson?.data?.size()} inactive userNames that begin with ${regExpUsers}. The following variables that may scew the numbers.
    (Variables: regExpUsers=${regExpUsers}, testRuns=${testRuns}, userStatus=${userStatus}, accountType=${accountType}, log_level=${log_level}, create_days=${create_days}, used_days=${used_days})"""
    if(testRuns as Integer != 0 && testRuns as Integer == idx){return "${returnMessage}"}

    tokensParsedJson = [:]
    atlassianUserId = usersParsedJson?.data[idx]?.account_id?.toString()
    log.info("(${idx+1} of ${usersParsedJson?.data?.size()}) Atlassian User (${atlassianUserId}): ${usersParsedJson?.data[idx]?.account_name?.toString()} (${usersParsedJson?.data[idx]?.account_email?.toString()})")

    if( atlassianUserId == null ){return "No atlassianUserId"} //if no account_id return

    getUserTokens = "https://api.atlassian.com/users/${atlassianUserId.toString()}/manage/api-tokens"
   
    //See if user has any tokens
    def userTokens_Response = mgmntRequest(getUserTokens) ?: null
    log.debug("userTokens_Response: ${userTokens_Response}")

    if( userTokens_Response?.toString() != "[]" && userTokens_Response?.toString().indexOf("Error:") < 0){
        tokensParsedJson = jsonParser.parseText(userTokens_Response?.toString()) as Map
        log.info("(${idx+1} of ${usersParsedJson?.data?.size()}) User: ${usersParsedJson?.data[idx]?.account_name?.toString()} (${atlassianUserId}) is ${usersParsedJson?.data[idx]?.account_status?.toString()} and has ${tokensParsedJson?.size()} tokens.")
        //User is active and has tokens
        log.debug("Starting to review ${tokensParsedJson?.size()} tokens for ${usersParsedJson?.data[idx]?.account_name?.toString()} (${atlassianUserId})")
        tokensParsedJson.eachWithIndex{ tkn, tknIdx ->
            log.debug("(${tknIdx +1} of ${tokensParsedJson?.size()}) Token detail: Label: ${tkn?.label}, Created: ${tkn?.createdAt}, Last used: ${tkn?.lastAccess}, ID: ${tkn?.id}")
            def results = tokenValidation(atlassianUserId?.toString(), tkn as Map)
            finalSB.append("[userName:${usersParsedJson?.data[idx]?.account_name?.toString()}, userEmail:${usersParsedJson?.data[idx]?.account_email?.toString()}, userStatus:${usersParsedJson?.data[idx]?.account_status?.toString()}, atlassianId:${(usersParsedJson?.data[idx])?.account_id}, tokenStatus:${results}, tokenLabel:${tkn?.label}, created:${tkn?.createdAt}, lastUsed:${tkn?.lastAccess}, tokenId:${tkn?.id}, results:${results}],")
        }
    }else{
        log.debug("(${idx+1} of ${usersParsedJson?.data?.size()}) User: ${usersParsedJson?.data[idx]?.account_name?.toString()} (${atlassianUserId}) is ${usersParsedJson?.data[idx]?.account_status?.toString()} and has no tokens.")
    }
}

if(finalSB.toString() == "Deleted Tokens:["){finalSB.append("Found no token violations ")}
finalSB = finalSB.deleteCharAt(finalSB.length() - 1)
finalSB.append("]")
//Stop time of this script
Long endTime = Instant.now().getEpochSecond() as Long 
log.info("(Runtime: ${endTime - startTime} seconds) ${returnMessage}")
log.info("View 'results' after each token processed: ${finalSB}")
return """(Runtime: ${endTime - startTime} seconds) ${returnMessage} 
            ${finalSB}""" 

