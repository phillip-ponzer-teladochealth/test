/*
Author: Eduardo Rojo
This script is setting the shared team id to TEAM field
See https://confluence.teladoc.net/display/saas/Shared+Teams
*/

import groovy.json.JsonSlurper

def orgID = "545b8b69-59k5-1247-j318-18c73k874075"
def siteID = "da2740d5-0d77-48ef-a653-69563d1ce9d1"


//*******************Function: Get Teams Names and IDs from Teams Public API*******************
def getTeams(orgID, siteID){
    boolean flag = true
    def myResponse
    def fullResponse = []
    try {
        //Iteration to get all subscribers from Statuspage by pagination
        //A default limit of 100 is imposed and this endpoint will return paginated data
        def result = get("https://api.atlassian.com/public/teams/v1/org/${orgID}/teams?siteId=${siteID}")
            .basicAuth(USER_NAME_EDU, TOKEN_EDUARDO)
            .header("Accept", "application/json")
            .asJson()
        HashMap jsonResponse = new JsonSlurper().parseText(result.getBody().toString()) as HashMap
        logger.info("Cursor: ${jsonResponse.cursor.toString()}")
        def v = 0
        while(jsonResponse.cursor.toString()!=null){
            jsonResponse = new JsonSlurper().parseText(result.getBody().toString()) as HashMap
            
            //logger.info("jsonResponse: (${jsonResponse.toString()})")
            //logger.info("cursor en el inicio: (${jsonResponse.cursor.toString()})")
            
            List resultList = jsonResponse.entities.each { } as List

            if (result.getStatus() == 200) {
                //logger.info("result.status: "+result.getStatus())
                
                //logger.info("Cursor antes del IF: " + jsonResponse.cursor)
                String variable = jsonResponse.cursor.toString()
                if(!jsonResponse.cursor){
                    flag = false
                    logger.info("Flag en el IF "+ flag)
                    return fullResponse = fullResponse + resultList
                }
                else{
                    fullResponse = fullResponse + resultList
                }

            } else {
                logger.info("Failed to retrieve Teams. Status code: ${result.getStatus()}")
                return null
            }
        
            //Calling Team Public REST API https://developer.atlassian.com/platform/teams/components/team-public-rest-api/
            result = get("https://api.atlassian.com/public/teams/v1/org/${orgID}/teams?siteId=${siteID}&cursor=${jsonResponse.cursor.toString()}")
            .basicAuth(USER_NAME_EDU, TOKEN_EDUARDO)
            .header("Accept", "application/json")
            .asJson()
            
            logger.info("!jsonResponse.cursor.toString(): " + jsonResponse.cursor.toString())
            v++
            logger.info("Count: " + v +" Flag: "+ flag)
        
        }   
        return fullResponse
    } catch (Exception e) {
        logger.info("An error occurred: ${e.message}")
        return null
    }
}

//Calling getTeams() function
List teams = getTeams(orgID, siteID)
logger.info("Teams: "+ teams)

//Looking into the Teams list the current project KEY 
def teamFound = teams.findAll { it -> it.displayName.toString().contains("(${Issues.getByKey(issue.key as String).projectObject.key})")
                                }
logger.info("teamFound: ${teamFound.displayName.toString()} for ${issue.key}")
 
 if(teamFound){
    def teamID = teamFound[0].teamId.toString() 
    logger.info("teamID: "+teamID)
    //setting Team ID on TEAM custom field
    
    // get custom fields List
    def customFields = get("/rest/api/2/field")
        .asObject(List)
        .body
        .findAll { (it as Map).custom } as List<Map>

    // getting Team Custom Field ID from the custom fields list
    def teamCustomFieldID = customFields.find { it.name == 'Team' }?.id

    //Updating ticket
    put("/rest/api/2/issue/${issue.key}") 
    // .queryString("overrideScreenSecurity", Boolean.TRUE) 
    .header("Content-Type", "application/json")
    .body([
        fields:[
                (teamCustomFieldID): teamID
        ]
    ])
    .asString()
 }
