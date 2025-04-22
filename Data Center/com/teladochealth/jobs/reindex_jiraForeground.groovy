package com.teladochealth.jobs

/*
* Author: Eduardo Rojo
* Reindex_JiraForeground
* Date: 1/11/2023
* Get current host and call this API /rest/api/2/reindex
* Reindexing Type: FOREGROUND 
*/
import com.atlassian.sal.api.ApplicationProperties 
import com.atlassian.sal.api.UrlMode 
import com.atlassian.sal.api.net.Request 
import com.onresolve.scriptrunner.runner.customisers.PluginModule 
import groovy.json.JsonSlurper
import org.apache.log4j.Logger
import org.apache.log4j.Level
/*
JSON slurper parses text or reader content into a data structure of lists and maps.
def slurper = new groovy.json.JsonSlurper()
 def result = slurper.parseText('{"person":{"name":"Guillaume","age":33,"pets":["dog","cat"]}}')
 assert result.person.name == "Guillaume"
 assert result.person.age == 33
 assert result.person.pets.size() == 2
 assert result.person.pets[0] == "dog"
 assert result.person.pets[1] == "cat"
 */
 log = Logger.getLogger("Jira Foreground ReIndex")
log.setLevel(Level.INFO)
import com.atlassian.sal.api.net.TrustedRequestFactory
// TrustedRequestFactory is used to create a Request  
import groovyx.net.http.URIBuilder 

@PluginModule 
TrustedRequestFactory trustedRequestFactory 

@PluginModule 
ApplicationProperties applicationProperties 

log.info("ScriptRunner: Reindex_JiraForeground in has started.")

final reindexAPI = '/rest/api/latest/reindex?type=FOREGROUND' 
def urlStartIndex = applicationProperties.getBaseUrl(UrlMode.CANONICAL) + reindexAPI 
def requestStartIndex = trustedRequestFactory.createTrustedRequest(Request.MethodType.POST, urlStartIndex) 

def host = new URIBuilder(urlStartIndex).host 
log.info("Host: " + host) 

try {     
    requestStartIndex.addTrustedTokenAuthentication(host)
 
    def responseBody = requestStartIndex.execute()     
    def responseAsMap = new JsonSlurper().parseText(responseBody) as Map 
    
    log.debug("Index started. - " + responseAsMap) 
}
catch(def error){
     log.error("Reindex not started. - " + error) 
}
log.info("ScriptRunner: Reindex_JiraForeground completed.")