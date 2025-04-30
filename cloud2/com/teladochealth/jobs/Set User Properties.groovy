/*
Author: Jeff Melies
Purpose: Populate User Properties, currently work_email & mgr (this is an id#).
        Excludes emails with 'svc_'
        Excludes all emails in the variable: excludeEmailList
Requirements: This script needs to run every hour as it uses the hour for RegEx to filter 
    users, if ran all at once the script would hit the 240 second time-out.
Good to know: This script will only populate the user properties on Production unless it's copied
    and ran in another environment or Sandbox
Schedule: Every hour, only on Saturday
*/
import com.mashape.unirest.http.Unirest.*
import groovy.json.JsonSlurper
import groovy.json.JsonBuilder
import groovy.json.JsonOutput
import groovy.transform.Field as tField
import java.lang.String
import java.io.*
import java.time.*
import java.net.URL
import java.net.HttpURLConnection
import org.apache.log4j.Logger
import org.apache.log4j.Level

//Capture the start time to calculate the elapsed time to run this script:
Long startTime = Instant.now().getEpochSecond() as Long

//Rate Limiting
int waitFor = 0    //Being Rate Limited? Specify the length of time to wait in seconds.

//Global Variables
@tField log_level = Level.INFO    //Options would be (Level.INFO, Level.WARN, Level.DEBUG, Level.ERROR) without quotes.
@tField logName = "Scheduled Job: Set User Properties"  //Create the name of our log
@tField userStatus = "ACTIVE"     //Options would be ('ACTIVE', 'INACTIVE')  
@tField accountType = "atlassian" //Options would be ('atlassian', 'customer', or 'null' for all)
@tField graphApiUrl = 'https://graph.microsoft.com/v1.0/'
@tField tokenUrl = "https://login.microsoftonline.com/${AZURE_TENANT_ID}/oauth2/v2.0/token"
@tField orgURL = "https://api.atlassian.com/admin/v1/orgs/${ORG_ID}"
@tField excludeEmailList = ["jenkins@teladoc.com","teamsupport-jira-integration@teladochealth.com","jira.ts.connector@teladochealth.com","jiracloud.ts.connector@teladochealth.com"]
@tField currentHour = LocalDateTime.now().getHour()
@tField testRuns = 0              //Set to how many users to execute this script on, 0 for all users.
@tField regExUsed = ""            //Global Reg Exp value for recording

//*************************FUNCTIONS****************************************
//Adding a user property temporarly until Manager is avaialable from Atlassian
def addUserProperty(String accId, List accEmail){ //, String accProperty){
    Logger.getLogger(logName?.toString()) as Logger
    log.setLevel(log_level as Level)
    def properties = [
        "work_email": accEmail[0],
        "mgr": accEmail[1]
    ]
    def headers = ['Content-Type': 'application/json', 'Accept': 'application/json']
    //def jsonBody = JsonOutput.toJson(accEmail)
    //def response = put("/rest/api/3/user/properties/${accProperty}?accountId=${accId}")
    properties.each { key, value ->
        def endpoint = "/rest/api/3/user/properties/${key}?accountId=${accId}"
        def jsonBody = JsonOutput.toJson(value?.toString())
        def response = put(endpoint)
            .headers(headers) //("Accept", "application/json")
            .body(jsonBody)
            .asObject(Map)

        if(response.getStatus() >= 202){
            log.error("ERROR: Failed to add user Property on (${accId}), ${key}: ${value}")
        }
    }
}
// Function to call MS Graph API to retrieve manager
def getManagerDetails(String userId) {
    Logger.getLogger(logName?.toString()) as Logger
    log.setLevel(log_level as Level)
    try {
        def accessToken = getOAuthToken() // Get OAuth2 token

        def graphUrl = graphApiUrl + "users/${userId}/manager" //userEndpoint.toString().replace("{user-id}", userId)
        def url = new URL(graphUrl)
        def conn = url.openConnection() as HttpURLConnection
        conn.setRequestMethod("GET")
        conn.setRequestProperty("Authorization", "Bearer ${accessToken}")
        conn.setRequestProperty("Content-Type", "application/json")

        def responseCode = conn.responseCode
        def responseMessage = conn.inputStream.text

        if (responseCode == 200) {
            def jsonResponse = new JsonSlurper().parseText(responseMessage)
            Map results = [displayName:"'${jsonResponse.displayName ?: null}'", jobTitle:"'${jsonResponse.jobTitle ?: null}'", givenName:"'${jsonResponse.givenName ?: null}'", surname:"'${jsonResponse.surname ?: null}'", mail:"'${jsonResponse.mail ?: null}'", mobilePhone:"'${jsonResponse.mobilePhone ?: null}'", businessPhones:"'${jsonResponse.businessPhones ?: null}'", officeLocation:"'${jsonResponse.officeLocation ?: null}'", preferredLanguage:"'${jsonResponse.preferredLanguage ?: null}'", userPrincipalName:"'${jsonResponse.userPrincipalName ?: null}'"]
            log.debug("(getManagerDetails) results returned: ${results}")
            return  results as Map //List// Manager's name
        } else {
            throw new RuntimeException("Failed to get manager information: $responseCode $responseMessage")
        }
    } catch (Exception e) {
        log.error("Error getting manager name: ${e.message}")
    }
}
def getOAuthToken() {
    Logger.getLogger(logName?.toString()) as Logger
    log.setLevel(log_level as Level)
    def conn = new URL(tokenUrl).openConnection() as HttpURLConnection
    conn.requestMethod = "POST"
    conn.setDoOutput(true)
    conn.setRequestProperty("Content-Type", "application/x-www-form-urlencoded")

    def body = "grant_type=client_credentials&client_id=$ENTRA_ATLASSIAN_ID&client_secret=$ENTRA_ATLASSIAN_SECRET&scope=https://graph.microsoft.com/.default"
    conn.outputStream.write(body.getBytes("UTF-8"))

    def responseCode = conn.responseCode
    def responseMessage = conn.inputStream.text
    def jsonResponse = new JsonSlurper().parseText(responseMessage)

    if (responseCode == 200) {
        log.debug("(getOAuthToken) results returned: ${jsonResponse?.access_token}")
        return jsonResponse?.access_token
    } else {
        log.error("(getOAuthToken) Failed to get access token: ${responseCode} ${responseMessage}")
        throw new RuntimeException("Failed to get access token: ${responseCode} ${responseMessage}")
    }
}
def searchForUser(String queryString){
    String searchString = queryString?.replace(" ", "%20") //.replace("'", "") 
    def log = Logger.getLogger(logName)
    log.setLevel(log_level as Level)
    log.debug("(searchForUser) queryString: ${searchString.drop(1).reverse().drop(1).reverse()}")
    Map results// = new HashMap<>() 
    List foundUsers = []
    
    def userSearchResults = get("/rest/api/3/user/picker?query=${searchString.drop(1).reverse().drop(1).reverse()}") //search //
        .asObject(Map)

    if(userSearchResults.status == 200){
        def content = userSearchResults?.body?.users
        log.debug("(searchForUser): content: ${content}")

        content.each{ usr ->
            if(usr.html.toString().contains(queryString.replace("'", ""))){// == queryString.replace("'", "")){
                log.debug("(searchForUser): ****FOUND A MATCH-usr: ${usr}")
                foundUsers << ["accountId:'${usr.accountId.toString()}', accountType:'${usr.accountType.toString()}', html:'${usr.html.toString()}', displayName:'${usr.displayName.toString()}'"]
            }
        }
        return foundUsers ?: null  //userSearchResults?.body ?: null //[userSearchResults] as List //?: [] //results ?: [] //["${userSearchResults?.body}"] as List
    }else{
        return null
    }
}


//Get Managed users and Delete unused Tokens
def orgRequest(String url, String httpMethod){
    Logger.getLogger(logName?.toString()) as Logger
    log.setLevel(log_level as Level)

    StringBuilder sb = new StringBuilder()
    String responseText
    Map response
    Integer curserId = 1
    def objUnirest, requestBody,pattern
    Map usersParsedJson
    def jsonParser = new JsonSlurper()
    def newURL = orgURL + url //"${url}" //new URL(url)
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

def regexpUsers(Integer currentHour){
    if(currentHour >= 0 && currentHour <= 23){
        //Looks like we can process about 100 users safely within 240 seconds
        //email begins with
        Map selection = [
            0:/^a[a-l].*/,                //151 users 163 seconds 
            1:/^a[m-z].*/,                //168 users 179 seconds   
            2:/^b.*/,                     //125 users 135 seconds 
            3:/^c[a-m].*/,                //117 users 116 seconds  
            4:/^c[n-z].*/,                //60 users 63 seconds 
            5:/^d.*/,                     //146 users 150 seconds  
            6:/^[e-f].*/,                 //164 users 161 seconds 
            7:/^[g-h].*/,                 //147 users 141 seconds
            8:/^i.*|(j[a-l].*)/,          //168 users 190 seconds 
            9:/^j[m-z].*/,                //139 users 138 seconds       
            10:/^k[a-s].*/,               //169 users 181 seconds
            11:/^k[t-z].*|(^l.*)/,        //130 users 148 seconds
            12:/^m[a-d].*/,               //149 users 153 seconds  ma=56, me=25- mh=7
            13:/^m[e-k].*/,               //92 users 102 seconds 
            14:/^m[l-z].*/,               //75 users 81 seconds
            15:/^[n-o].*/,                //150 users 153 seconds
            16:/^[p-q].*/,                //117 users 191 seconds 
            17:/^r[a-m].*/,               //98 users 112 seconds
            18:/^r[n-z].*/,               //66 users 86 seconds 
            19:/^s[a-j].*/,               //149 users 165 seconds  k[a-e]=87, ke=21, kg=4, kh=9, ki=22
            20:/^s[k-z].*/,               //118 users 140 seconds  t=98
            21:/^[t-u].*/,                //109 users 111 seconds
            22:/^v.*|([w-z].*)/,          //164 users 173 seconds  u=11 , v=104, w=18, x=, y=, z=
            23:/^f.*|(j[j-l].*)/   //****This is a duplicate run and not needed****
        ]
        regExUsed = selection[currentHour].toString()
        //regExpUsers = regExUsed.toString()
    }else{
        log.error("Check the time this script ran (${currentHour}) and compare with the method regexpUsers.")
        regExUsed = /1.*/  //regExpUsers = /1.*/
    }
    return regExUsed.toString()  //regExpUsers.toString()
}

//**************************MAIN SCRIPT*************************************
Logger.getLogger(logName?.toString()) as Logger
log.setLevel(log_level as Level)

def jsonParser = new JsonSlurper()
String atlassianUserEmail, mgrSearchItem 
def managerDetails
//List managerUser

//Get all Managed Users
def users_Response = orgRequest("/users/search", "Post")
if(users_Response?.length() < 10){return "No users found"}
def usersParsedJson = jsonParser.parseText(users_Response?.toString())
log.info("Total users found: (${usersParsedJson?.data?.size()}) Class: ${usersParsedJson.data[0]}")

for( int idx = 0; idx < usersParsedJson?.data?.size(); idx++){
    managerDetails = null
    mgrSearchItem = null
//    managerUser = null
    Thread.sleep(waitFor * 1000)

    //Set Atlassian User variables
    atlassianUserId = usersParsedJson?.data[idx]?.account_id?.toString() ?: null
    atlassianUserEmail = usersParsedJson?.data[idx]?.account_email?.toString() ?: null
    //addUserProperty (atlassianUserId, atlassianUserEmail)

    //Using the Atlassian user email, get their Manager info from Entra
    managerDetails = getManagerDetails(atlassianUserEmail) as Map ?: null
    log.info("(${idx + 1}/${usersParsedJson?.data?.size()})User: ${usersParsedJson?.data[idx]?.account_email?.toString()}(${usersParsedJson?.data[idx]?.account_name?.toString()}), Manager: ${managerDetails?.displayName}")
    //We will use the Managers displayName from Entra to get the 'Atlassian User' for the manager.
    if(managerDetails?.userPrincipalName?.toString() != 'null'){ //(managerDetails?.displayName?.toString() != 'null'){
        mgrSearchItem = managerDetails?.userPrincipalName?.toString() 
    }else{ //Otherwise use "displayName" to try and find it.  //"givenName sirName" 
        mgrSearchItem = managerDetails?.displayName?.toString()  //givenName?.toString() + ' ' + managerDetails?.surname?.toString()
    }

    //Using the Entra information get the Atlassian user details for the manager
    if(mgrSearchItem ){
        def mgrID = ""
        def managerUser = (searchForUser(mgrSearchItem) as List) ?: null
        log.debug("(${managerUser?.size()})managerUser: ${managerUser?.getClass()}, ${managerUser}")
        if(managerUser){
            mgrID = Eval.me(managerUser?.toString())?.accountId?.toString()?.replaceAll("[\\[\\](){}]","") ?: ""
        }
        List propertyDetails = [atlassianUserEmail ?: "", mgrID]
        log.debug("propertyDetails: ${propertyDetails}")
        //Set the Atlassian User Properties for both work_email & mgr
        addUserProperty (atlassianUserId, propertyDetails) //(Eval.me(managerUser.toString()))?.accountId?.toString()?.replaceAll("[\\[\\](){}]",""), "mgr")
    }else{
        log.error("No manager found for: ${Users.getByAccountId(atlassianUserId)?.displayName}, (${atlassianUserEmail})")
    }
}

//Stop time of this script
Long endTime = Instant.now().getEpochSecond() as Long 
log.info("(${regExUsed})-Updated ${usersParsedJson?.data?.size()} user in ${endTime - startTime} seconds")
return "(${regExUsed})-Updated ${usersParsedJson?.data?.size()} users in ${endTime - startTime} seconds"