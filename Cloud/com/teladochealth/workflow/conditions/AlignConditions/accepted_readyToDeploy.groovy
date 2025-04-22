// This script returns TRUE if the parent Epic is a "Supporting" Epic
// Author:  Jeff Tompkins
 
/* Actions required during Cloud Migration:
  - Line 9:  Update the Custom Field ID to the "Build/Run" field ID
*/

if (issue?.parent?.customfield_10092?.value == "Supporting") {
    return true;
} else {
    return false;
}