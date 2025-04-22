// This script Shows/Hides the RUN Fields if the parent Epic is a "Supporting" Epic
// Author:  Jeff Tompkins
 
/* Actions required during Cloud Migration:
  - Lines 10-14:  Confirm the correct Run Field IDs
  - Line 36:  Confirm the correct Build/Run field ID
*/
 
// Fetch the 5 RUN Fields
const engDepartmentField = getFieldById("customfield_10045");
const engPlatformField = getFieldById("customfield_10046");
const runTypeField = getFieldById("customfield_10047");
const deployedLocationField = getFieldById("customfield_10048");
const deployedProductField = getFieldById("customfield_10049");
 
// Make the RUN fields optional and hidden
engDepartmentField?.setRequired(false)
engPlatformField?.setRequired(false)
runTypeField?.setRequired(false)
deployedLocationField?.setRequired(false)
deployedProductField?.setRequired(false)
engDepartmentField?.setVisible(false)
engPlatformField?.setVisible(false)
runTypeField?.setVisible(false)
deployedLocationField?.setVisible(false)
deployedProductField?.setVisible(false)
 
// Fetches the Parent Issue Key
const parentValue = getFieldById("parent").getValue();
 
// If a parent issue exists
if (parentValue != null) {
 
    // Fetch the "Build/Run" field value of the Parent Issue object
    const parentIssue = await makeRequest("/rest/api/2/issue/" + parentValue.key);
    const parentBuildRun = parentIssue.body.fields.customfield_10044.value;
 
    // If the parent "Build/Run" field is set to "Supporting"
    if (parentBuildRun == "Supporting") {
        engDepartmentField?.setVisible(true)
        engPlatformField?.setVisible(true)
        runTypeField?.setVisible(true)
        deployedLocationField?.setVisible(true)
        deployedProductField?.setVisible(true)
        engDepartmentField.setRequired(true)
        engPlatformField.setRequired(true)
        runTypeField.setRequired(true)
        deployedLocationField.setRequired(true)
        deployedProductField.setRequired(true)
 
    } else {
         
        engDepartmentField.setRequired(false)
        engPlatformField.setRequired(false)
        runTypeField.setRequired(false)
        deployedLocationField.setRequired(false)
        deployedProductField.setRequired(false)
        engDepartmentField?.setVisible(false)
        engPlatformField?.setVisible(false)
        runTypeField?.setVisible(false)
        deployedLocationField?.setVisible(false)
        deployedProductField?.setVisible(false)
    }
     
}