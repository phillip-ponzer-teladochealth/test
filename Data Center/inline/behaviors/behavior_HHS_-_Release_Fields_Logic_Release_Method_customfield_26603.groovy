//Creator: Jeff Tompkins
//Purpose: Show other fields dependent on this one

//imports
import com.onresolve.jira.groovy.user.FieldBehaviours
import groovy.transform.BaseScript

@BaseScript FieldBehaviours fieldBehaviours

def changedFieldValue = getFieldById(getFieldChanged())?.getValue()

if( changedFieldValue == "Product" || changedFieldValue == "Superadmin practice configuration" || changedFieldValue == "Integration Setting"){
	getFieldByName("Release Method Rationale")?.setHidden(false)
    getFieldByName("Release Method Rationale")?.setRequired(true)
}else{
	getFieldByName("Release Method Rationale")?.setHidden(true)
    getFieldByName("Release Method Rationale")?.setRequired(false)
}