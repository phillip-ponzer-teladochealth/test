package com.teladochealth.workflow.postfunctions

/*
Author: Eduardo Rojo
This script is setting the shared team id to TEAM field
See https://confluence.teladoc.net/display/saas/Shared+Teams
*/

import com.onresolve.scriptrunner.parameters.annotation.*
import com.onresolve.scriptrunner.db.DatabaseUtil
import com.atlassian.jira.project.Project
import java.util.ArrayList


//Jira Database connection function using local_db 
def dbCall(qry){
    def output = []
    def result = [:]
	StringBuffer sb = new StringBuffer()
    
	output = DatabaseUtil.withSql('local_db') { sql ->
        sql.rows(qry.toString()) as Map
    }
    log.debug("The SQL query ran: ${qry.toString()}")
	return output
}
//Retriving all Team ID's (ID) and Team Names (TITLE) from Jira database
//def teams = []
ArrayList<String> teams = new ArrayList<String>()
String mySQL = """SELECT team."ID", team."TITLE" FROM "AO_82B313_TEAM" team Where "SHAREABLE" is true;"""
teams = dbCall(mySQL.toString()) as Map
log.info("teams: "+teams)
//getting current project key
 def projectKey = '('+issue.getProjectObject().key+')'
 log.info("projectKey: "+projectKey)
 //searching the current project key in Team database array
 def teamFound = teams.findAll { it -> it.toString().contains(projectKey) }
 log.info("teamFound: "+teamFound)
 
 if(teamFound){
    def teamID = teamFound.id.join( '' ) as int 
    log.info("teamID: "+teamID)
    //setting Team ID on TEAM custom field
    issue.set{
        setCustomFieldValue('Team', teamID.toString())
    }
 }