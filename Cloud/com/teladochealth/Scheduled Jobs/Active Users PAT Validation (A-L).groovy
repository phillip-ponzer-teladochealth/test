/*
Author: Jeff Melies
Purpose: Delete old and unused tokens from our users.
        Excludes emails with 'svc_'
        Excludes all emails in the variable: excludeEmailList
Requirements: This script needs to run every hour as it uses the hour for RegEx to filter 
    users since we hit the 240 second timeout.
Schedule: Every hour, only on Sunday
*/
import com.mashape.unirest.http.Unirest.*
import groovy.json.JsonSlurper
import groovy.json.JsonBuilder
import groovy.transform.Field as tField
import java.lang.String
import java.io.*
import java.time.*
import java.net.URL
import java.net.HttpURLConnection
import org.apache.log4j.*

//Capture the start time to calculate the elapsed time to run this script:
Long startTime = Instant.now().getEpochSecond() as Long


//Global Variables
@tField log_level = Level.INFO    //Options would be (Level.INFO, Level.WARN, Level.DEBUG, Level.ERROR) without quotes.
@tField testRuns = 0              //Set to how many users to execute this script on, 0 for all users.
@tField create_days = 5           //If the tokens create date is more than this figure, we will check to see when it was last used.
@tField used_days = 18            //If the token hasn't been used since this figure, it will be deleted.
@tField userStatus = "ACTIVE"     //Options would be ('ACTIVE', 'INACTIVE')  
@tField accountType = "atlassian" //Options would be ('atlassian', 'customer', or 'null' for all)
@tField logName = "Scheduled Job: Active Users PAT Validation (A-L)"  //Create the name of our log
@tField currentHour = LocalDateTime.now().getHour() //.atZone(ZoneId.of("America/Chicago")) 
@tField regExpUsers                //Used to store the RegEx to limit users being processed
@tField excludeEmailList = ["teamsupport-jira-integration@teladochealth.com","jira.ts.connector@teladochealth.com","jiracloud.ts.connector@teladochealth.com"]

//Rate Limiting
int waitFor = 1                   //Being Rate Limited? Specify the length of time to wait in seconds.

def jsonParser = new JsonSlurper()
Map tokensParsedJson
String atlassianUserId, getUserTokens, returnMessage
StringBuilder finalSB = new StringBuilder()
String getUsers = "https://api.atlassian.com/admin/v1/orgs/${ORG_ID}/users/search"

Logger log = Logger.getLogger(logName?.toString()) as Logger
log.setLevel(log_level as Level)

//****************************** FUNCTIONS ************************************
def getUserApiTokens(String atlasId) {
    Logger log = Logger.getLogger(logName?.toString()) as Logger
    log.setLevel(log_level as Level)
    String endpoint  = "https://api.atlassian.com/users/${atlasId.toString()}/manage/api-tokens"

    def headers = ['Authorization': ORG_JBM_API_KEY, 'Accept': 'application/json']   

    try{
        HttpResponse<JsonNode> connection = Unirest.get(endpoint )
            .headers(headers)
            .asJson()

        log.debug("Response Code: ${connection?.getStatus()}")
        if (connection?.getStatus() == 200) {
            return connection?.getBody()
        } else { // Handle other response codes
            log.error("Error: (${connection?.getStatus()}) ${connection.getStatusText()}")
            return "Error: (${connection?.getStatus()}) ${connection.getStatusText()}"
        }
    } catch (Exception e) {
        log.error("An error occurred: ${e.message}")
        return e.message
    }
}

//Get Managed users and Delete unused Tokens
def orgRequest(String url, String httpMethod){
    Logger log = Logger.getLogger(logName?.toString()) as Logger
    log.setLevel(log_level as Level)

    StringBuilder sb = new StringBuilder()
    String responseText
    Map response
    Integer curserId = 1
    def objUnirest, requestBody,pattern
    Map usersParsedJson
    def jsonParser = new JsonSlurper()
    def newURL = "${url}" //new URL(url)
    int responseCode
    def payload// = new JsonBuilder(requestBody).toString()
    HttpResponse<JsonNode> connection
    sb.append("""{"data":[""")

    def headers = ['Authorization': ORG_JBM_API_KEY, 'Content-Type': 'application/json', 'Accept': 'application/json'] //, 'Accept': 'application/json'
    
    switch(httpMethod.toLowerCase()){
        case "post":
            requestBody = [
                "accountStatuses":["${userStatus}"],
                "emailDomains": ["eq": ["teladochealth.com","teladoc.com"]],
                "expand":["NAME","EMAIL","EMAIL_VERIFIED"],
                "isSuspended":false
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
    
    log.debug("(orgRequest) responseCode: ${responseCode}, ${response}")

    if(newURL?.toString().contains("/orgs/")){
        if (responseCode != 200) {
            log.error("Error: (${responseCode}): ${responseText}")
            return "Error: (${responseCode}): ${responseText}"
        }
        usersParsedJson = jsonParser.parseText(connection?.getBody()?.toString()) as Map
        
        pattern = regexpUsers(currentHour as Integer)
        if(! pattern){return "There's no users to lookup, check the time this script ran (${currentHour}) and compare with the method regexpUsers."} 

        //Sort in alphabet order by email
        usersParsedJson?.data?.sort({ a, b -> a?.email?.toLowerCase() <=> (b?.email?.toLowerCase()) }).eachWithIndex{ user, usrIdx -> 
            if(curserId == 1){
                if(testRuns as Integer >= usrIdx as Integer){
                    log.debug("(orgRequest) Compare ${user?.accountType} with ${accountType?.toString()}, results: ${user?.accountType == accountType?.toString()}")
                    log.debug("(orgRequest) Complete user Json sample: ${user}")
                    log.debug("(orgRequest) User details being used: account_id: ${user?.accountId}, account_status: ${user?.accountStatus}, account_type: ${user?.accountType}")
                }
            }
            
            //If the email hasn't been verified then the user wouldn't have API tokens
            if(user?.emailVerified && 
                user?.email?.toString()?.toLowerCase().indexOf("svc_") < 0 &&
                excludeEmailList?.toString()?.toLowerCase().indexOf(user?.email?.toString()?.toLowerCase()) < 0){
                if(user?.email?.toString()?.toLowerCase() ==~ pattern){
                    log.debug("""Adding to SB: {"account_id":"${user?.accountId}","account_status":"${user?.accountStatus}","account_name":"${user?.name}","account_nickname":"${user?.nickname}","account_email":"${user?.email}","account_emailVerified":"${user?.emailVerified}","account_statusInUserBase":"${user?.statusInUserbase}","account_type":"${user?.accountType}"}""")
                    sb.append("""{"account_id":"${user?.accountId}","account_status":"${user?.accountStatus}","account_name":"${user?.name}","account_nickname":"${user?.nickname}","account_email":"${user?.email}","account_statusInUserBase":"${user?.statusInUserbase}","account_type":"${user?.accountType}"},""")
                }
            }
        }  
    }else{ //Requests not using the Organizations REST AP.
        newURL = null
        return responseCode
    }    
    if(sb.length() >= 10){
        sb = sb.deleteCharAt(sb.length() - 1)
        sb.append("]}")
    }else{
        sb.setLength(0)
    }
    log.info("Pattern: ${pattern}")
    log.debug("FINAL SB: ${sb.toString()}") 
    return sb.toString()
}

def tokenValidation(String atlassianUserId, Map jsonMap){
    Logger log = Logger.getLogger(logName?.toString()) as Logger
    log.setLevel(log_level as Level)
    String results
    def tokenId = jsonMap?.id

    //Check if token is disabled, if it is delete it otherwise see if it's being used
    if(jsonMap?.disabledStatus){
        log.info("(tokenValidation) '${jsonMap?.label}' is disabled and will be deleted.")
        results = deleteToken(atlassianUserId, jsonMap.id.toString())
        return "${results}"
    }else{
        log.debug("(tokenValidation) Sending token ${jsonMap?.label} to unUsedTokenValidation")
        results = unUsedTokenValidation(atlassianUserId, jsonMap)
        return "${results?.toString()}"
    }
}

def unUsedTokenValidation(String atlassianUserId, Map jsonMap){
    Logger log = Logger.getLogger(logName?.toString()) as Logger
    log.setLevel(log_level as Level)

    String results
    def pattern = "yyyy-MM-dd'T'HH:mm:ss"
    TimeZone.setDefault(TimeZone.getTimeZone('UTC'))
    def currentDate = new Date()
    def tokenId = jsonMap?.id
    def tokenLabel = jsonMap?.label
    def differenceInDays, differenceInDaysCreated, tokenLastAccessTimeInMS
    Date tokenCreatedAt, tokenLastAccess
    tokenCreatedAt = Date.parse(pattern, jsonMap?.createdAt?.toString()) ?: null 
    log.debug("(unUsedTokenValidation) jsonMap: ${jsonMap}")
    if(jsonMap?.lastAccess?.toString()){
        tokenLastAccess = Date.parse(pattern, jsonMap?.lastAccess?.toString())
        tokenLastAccessTimeInMS = tokenLastAccess?.getTime()
    }else{//If Json was null, set last used to 1 year ago.
        tokenLastAccess = null
        tokenLastAccessTimeInMS = 31536000000 as Long 
    }
    def tokenCreateTimeInMS = tokenCreatedAt?.getTime()
    
    def currentTimeInMS = currentDate?.getTime()
    log.debug("(unUsedTokenValidation) currentDate: ${currentDate.format(pattern)}, currentTimeInMS: ${currentTimeInMS}, tokenCreateTimeInMS: ${tokenCreateTimeInMS}, Test: ${(currentTimeInMS - tokenCreateTimeInMS) / (60*60*24*1000) as Integer}")

    differenceInDaysCreated = (currentTimeInMS - tokenCreateTimeInMS) / (60*60*24*1000) as Integer
    differenceInDays = (currentTimeInMS - tokenLastAccessTimeInMS) / (60*60*24*1000) as Integer
    
    //If the token was created > $create_days, check to see if it's been used
    log.debug("(unUsedTokenValidation) Created ${tokenCreatedAt}, ${differenceInDaysCreated} days ago, threshold is (${create_days}), Test: ${(differenceInDaysCreated > create_days as Integer)}.")
    if(differenceInDaysCreated >= create_days as Integer){
        //If the token hasn't been used in $used_days than delete it.
        log.debug("(unUsedTokenValidation) Last Used ${tokenLastAccess}, ${differenceInDays} days ago threshold is (${used_days}), test: ${(differenceInDays > used_days as Integer)}.")
        if(differenceInDays >= used_days as Integer){
            log.info("(unUsedTokenValidation) Send token ${tokenLabel} to be deleted, the last time it was used was ${differenceInDays} days ago, the threshold is ${used_days} days.")
            results = deleteToken (atlassianUserId, tokenId?.toString())
            return "${results}" //"Deleted: Token ${tokenLabel} was not being used so it has been deleted."
        }else{
            log.debug("(unUsedTokenValidation) Retaining token sense it was used ${differenceInDays} days ago and the threshold is ${used_days} days.")
            return "Retained"
        }
    }else{
        log.debug("(unUsedTokenValidation) Retaining token sense it was created ${differenceInDaysCreated} days ago and the threshold is ${create_days} days.")
        return "Retained" //: The token (${tokenLabel}) was either recently created ${tokenCreatedAt} or recently used ${tokenLastAccess}."
    }
}

def deleteToken(String atlassianUserId, String tokenId){
    Logger log = Logger.getLogger(logName?.toString()) as Logger
    log.setLevel(log_level as Level)

    log.debug("(deleteToken) If allowed this token would be removed -- for user: ${atlassianUserId} - Token (${tokenId})")

    String deleteTokenEndpoint = "https://api.atlassian.com/users/${atlassianUserId}/manage/api-tokens/${tokenId}"
    Integer userTokens_Response = orgRequest(deleteTokenEndpoint.toString(), "DELETE") as Integer
    log.info("(deleteToken) ${userTokens_Response} for user: ${atlassianUserId} - Token (${tokenId})")
    if(userTokens_Response == 204){
        return "Deleted"
    } else {
        return "ERROR-${userTokens_Response} failed"
    }
}

def regexpUsers(Integer currentHour){
    if(currentHour >= 0 && currentHour <= 23){
        //Looks like we can process about 100 users safely within 240 seconds
        //email begins with
        Map selection = [
            0:/^a[a-h].*/,                //72 users 
            1:/^a[i-m].*/,                //52 users   
            2:/^a[n-p].*/,                //71 users 
            3:/(^a[q-z]).*|(^b[a-d]).*/,  //80 users  
            4:/^b[e-q].*/,                //70 users  br=30, be=25
            5:/(^b[r-z]).*|(^c[a-b]).*/,  //82 users    
            6:/^c[c-l].*/,                //73 users
            7:/^c[m-z].*/,                //62 users
            8:/^d[a-d].*/,                //57 users 
            9:/^d[e-o].*/,                //69 users 
            10:/(^d[p-z]).*|(^e[a-l]).*/, //59 users
            11:/(^e[m-z]).*/,             //65 users
            12:/^f.*/,                    //59 users  
            13:/^g[a-s].*/,               //77 users
            14:/^h.*|(^g[t-z]).*/,        //69 users   h=57 users, g[t-z]=12 users
            15:/^i.*|^ja.*/,              //81 users
            16:/^j[b-i].*/,               //78 users
            17:/^j[j-o].*/,               //77 users  
            18:/^j[p-z].*/,               //72 users 
            19:/^k[a-d].*/,               //66 users   k[a-e]=87, ke=21, kg=4, kh=9, ki=22
            20:/^k[e-h].*/,               //34 users 
            21:/^k[i-s].*/,               //69 users
            22:/(^k[t-z]).*|(^l[a-e]).*/, //55 users
            23:/^l[f-z].*/                //74 users
        ]
        regExpUsers = selection[currentHour].toString()
    }else{
        log.error("Check the time this script ran (${currentHour}) and compare with the method regexpUsers.")
        regExpUsers = /1.*/
    }
    return regExpUsers
}
//****************Main Script**************************
//Get all Managed Users
def users_Response = orgRequest(getUsers, "Post")
if(users_Response?.length() < 10){return "No users found"}
def usersParsedJson = jsonParser.parseText(users_Response?.toString())
log.info("Total users found: (${usersParsedJson?.data?.size()}) Class: ${usersParsedJson.data[0]}")

finalSB.append("Tokens:[")
for( int idx = 0; idx < usersParsedJson?.data?.size(); idx++){
    Thread.sleep(waitFor * 1000)
    returnMessage = """(Active Users) We ran a total of ${if(testRuns == 0){idx+1}else{idx}} iteration(s) on ${usersParsedJson?.data?.size()} Active userNames that begin with ${regExpUsers}. The following variables that may scew the numbers.
    (Variables: regExpUsers=${regExpUsers}, testRuns=${testRuns}, userStatus=${userStatus}, accountType=${accountType}, log_level=${log_level}, create_days=${create_days}, used_days=${used_days})"""
    if(testRuns as Integer != 0 && testRuns as Integer == idx){return "${returnMessage}"}

    tokensParsedJson = [:]
    atlassianUserId = usersParsedJson?.data[idx]?.account_id?.toString() ?: null
    log.info("(${idx+1} of ${usersParsedJson?.data?.size()}) Atlassian User (${atlassianUserId}): ${usersParsedJson?.data[idx]?.account_name?.toString()} (${usersParsedJson?.data[idx]?.account_email?.toString()})")
    
    if( atlassianUserId == null ){return "No atlassianUserId"}
        
    //See if user has any tokens
    def userTokens_Response = getUserApiTokens(atlassianUserId) ?: null

    if( userTokens_Response?.toString() != "[]" && userTokens_Response?.toString().indexOf("Error:") < 0){
        tokensParsedJson = jsonParser.parseText(userTokens_Response?.toString()) as Map
        log.info("(${idx+1} of ${usersParsedJson?.data?.size()}) User: ${usersParsedJson?.data[idx]?.account_name?.toString()} (${atlassianUserId}) is ${usersParsedJson?.data[idx]?.account_status?.toString()} and has ${tokensParsedJson?.size()} tokens.")
        //User is active and has tokens
        log.debug("Starting to review ${tokensParsedJson?.size()} tokens for ${usersParsedJson?.data[idx]?.account_name?.toString()} (${atlassianUserId})")
        tokensParsedJson.eachWithIndex{ tkn, tknIdx ->
            log.debug("(${tknIdx +1} of ${tokensParsedJson?.size()}) Token detail: Label: ${tkn?.label}, Created: ${tkn?.createdAt}, Last used: ${tkn?.lastAccess}, ID: ${tkn?.id}")
            def results = tokenValidation(atlassianUserId?.toString(), tkn as Map)
            log.debug("Results after token validation: ${results}")
            if(results?.toString() == "Deleted"){finalSB.append("[userName:${usersParsedJson?.data[idx]?.account_name?.toString()}, userEmail:${usersParsedJson?.data[idx]?.account_email?.toString()}, userStatus:${usersParsedJson?.data[idx]?.account_status?.toString()}, atlassianId:${(usersParsedJson?.data[idx])?.account_id}, tokenStatus:${results}, tokenLabel:${tkn?.label}, created:${tkn?.createdAt}, LastAccess:${tkn?.lastAccess}, tokenId:${tkn?.id}, results:${results}],")}
        }
    }else{
        log.debug("(${idx+1} of ${usersParsedJson?.data?.size()}) User: ${usersParsedJson?.data[idx]?.account_name?.toString()} (${atlassianUserId}) is ${usersParsedJson?.data[idx]?.account_status?.toString()} and has no tokens.")
    }
}

if(finalSB.toString() == "Tokens:["){finalSB.append("Found no token violations ")}
finalSB = finalSB.deleteCharAt(finalSB.length() - 1)
finalSB.append("]")

//Stop time of this script
Long endTime = Instant.now().getEpochSecond() as Long 
log.info("(Runtime: ${endTime - startTime} seconds) ${returnMessage}")
log.info("Results: ${finalSB}")
return """(Runtime: ${endTime - startTime} seconds) ${returnMessage} 
            Results: ${finalSB}"""
