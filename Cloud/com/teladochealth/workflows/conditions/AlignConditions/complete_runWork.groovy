// This script returns FALSE if the issue has a Parent, and if that Parent's "Build/Run" field is set to "Supporting".   Otherwise returns TRUE.
// Author:  Jeff Tompkins
 
/* TODO: Actions required during Cloud Migration:
  - Line 9:  Update the Custom Field ID to the "Build/Run" field ID
*/

if (issue?.parent?.customfield_10092?.value == "Supporting") { // "Build/Run"
    return false;
} else {
    return true;
}