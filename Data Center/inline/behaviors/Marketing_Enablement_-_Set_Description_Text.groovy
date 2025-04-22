// This script places template text in the Description when creating tickets
// Author:  Jeff Tompkins

import com.atlassian.jira.component.ComponentAccessor;

// Only run on the "Create" screen
if (getActionName() == "Create") {

   def fieldName = 'Description'
   def descriptionText

   // Check if the Issue already has a Description entered, and if so ignore it
   if(underlyingIssue){ 

      def cf = ComponentAccessor.customFieldManager.getCustomFieldObjects(underlyingIssue).find{it.name == fieldName}
      descriptionText = underlyingIssue.getCustomFieldValue(cf)

   } else {

      descriptionText = ''

   }

   // If there is no existing Description, add the template text below
   if(!descriptionText){
      getFieldByName(fieldName).setFormValue("""* *Business Category:* (Growth Retention, Engagement, SCM, Other)

   * *Description of Business Need / Problem Statement:*

   * *Key Deliverables:*

   * *Business Value/Expected Outcome is defined:*  How will we measure the business impact/outcomes (metric, dollar value, etc)?

   * *Data teams needed to do the work:* (Data Science, Data Eng)

   * *Is there an MA dependency?:*

   * *Other known dependencies:* (Creative, Client, etc)
   """)
   }
}