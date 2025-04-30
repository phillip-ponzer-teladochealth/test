import com.onresolve.jira.groovy.user.FieldBehaviours
import groovy.transform.BaseScript
import org.apache.log4j.Logger
import org.apache.log4j.Level
 
def log = Logger.getLogger("Behaviour: DEVSD Run Allocation Field Logic")
log.setLevel(Level.WARN)
 
def currentAction = getAction().toString()
log.debug("Current Action: '$currentAction'")
 
@BaseScript FieldBehaviours fieldBehaviours
 
// If this is a new issue being created
if(getAction().toString() == "Create issue") {
 
    // Monitor the Build/Run field for any changes
    def changedFieldValue = getFieldById(getFieldChanged())?.getValue()
    log.debug("Build/Run Value:  '$changedFieldValue'")
   
    // If the Build/Run field is set to "Supporting (Run)"
    if(changedFieldValue.toString() == "Supporting (Run)") {
        log.debug("Changed Value was RUN")
        getFieldByName("Eng - Department").setHidden(false).setRequired(true)
        getFieldByName("Eng - Platform").setHidden(false).setRequired(true)
        getFieldByName("RUN Type").setHidden(false).setRequired(true)
        getFieldByName("Deployed Location").setHidden(false).setRequired(true)
        getFieldByName("Deployed Product").setHidden(false).setRequired(true)
        getFieldByName("Product Initiatives").setHidden(true).setRequired(false).setFormValue(null)
    }
   
    // If the Build/Run field is set to "Initiative (Build)"
    else if (changedFieldValue.toString() == "Initiative (Build)") {
        log.debug("Changed Value was BUILD")
        getFieldByName("Eng - Department").setHidden(true).setRequired(false).setFormValue(null)
        getFieldByName("Eng - Platform").setHidden(true).setRequired(false).setFormValue(null)
        getFieldByName("RUN Type").setHidden(true).setRequired(false).setFormValue(null)
        getFieldByName("Deployed Location").setHidden(true).setRequired(false).setFormValue(null)
        getFieldByName("Deployed Product").setHidden(true).setRequired(false).setFormValue(null)
        getFieldByName("Product Initiatives").setHidden(false).setRequired(true)
    }
} else {

    // Hide the Run Allocation fields and make them optional
    getFieldByName("Eng - Department").setHidden(true).setRequired(false)
    getFieldByName("Eng - Platform").setHidden(true).setRequired(false)
    getFieldByName("RUN Type").setHidden(true).setRequired(false)
    getFieldByName("Deployed Location").setHidden(true).setRequired(false)
    getFieldByName("Deployed Product").setHidden(true).setRequired(false)
    getFieldByName("Product Initiatives").setHidden(true).setRequired(false)

    // Monitor the Build/Run field for any changes
    def changedFieldValue = getFieldById(getFieldChanged())?.getValue()
    log.debug("Build/Run Value:  '$changedFieldValue'")

    // If the Build/Run field is set
    if( changedFieldValue != null) {

        // If Build/Run is set to "Supporting (Run)"
        if (changedFieldValue.toString() == "Supporting (Run)") {
            getFieldByName("Eng - Department").setHidden(false).setRequired(true)
            getFieldByName("Eng - Platform").setHidden(false).setRequired(true)
            getFieldByName("RUN Type").setHidden(false).setRequired(true)
            getFieldByName("Deployed Location").setHidden(false).setRequired(true)
            getFieldByName("Deployed Product").setHidden(false).setRequired(true)
            getFieldByName("Product Initiatives").setHidden(true).setRequired(false).setFormValue(null)
            }

        // If Build/Run is set to "Initiative (Build)"
        if (changedFieldValue.toString() == "Initiative (Build)") {
            getFieldByName("Eng - Department").setHidden(true).setRequired(false).setFormValue(null)
            getFieldByName("Eng - Platform").setHidden(true).setRequired(false).setFormValue(null)
            getFieldByName("RUN Type").setHidden(true).setRequired(false).setFormValue(null)
            getFieldByName("Deployed Location").setHidden(true).setRequired(false).setFormValue(null)
            getFieldByName("Deployed Product").setHidden(true).setRequired(false).setFormValue(null)
            getFieldByName("Product Initiatives").setHidden(false).setRequired(true)
            }

        }
       
    }