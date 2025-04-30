// This script places template text in the Description when creating tickets
// Author:  Jeff Tompkins

const descriptionValue = getFieldById("description").getValue();

// Check if the description field is a wiki markup type field
if (typeof descriptionValue !== "string") {
    const descriptionValueContent = descriptionValue.content.toString();

    // Only set the field if it does not already have a value
    if (!descriptionValueContent) {

        getFieldById("description").setValue({
  "version": 1,
  "type": "doc",
  "content": [
    {
      "type": "bulletList",
      "content": [
        {
          "type": "listItem",
          "content": [
            {
              "type": "paragraph",
              "content": [
                {
                  "type": "text",
                  "text": "Business Category:",
                  "marks": [
                    {
                      "type": "strong"
                    }
                  ]
                },
                {
                  "type": "text",
                  "text": " (Growth Retention, Engagement, SCM, Other)"
                }
              ]
            }
          ]
        }
      ]
    },
    {
      "type": "bulletList",
      "content": [
        {
          "type": "listItem",
          "content": [
            {
              "type": "paragraph",
              "content": [
                {
                  "type": "text",
                  "text": "Description of Business Need / Problem Statement:",
                  "marks": [
                    {
                      "type": "strong"
                    }
                  ]
                }
              ]
            }
          ]
        }
      ]
    },
    {
      "type": "bulletList",
      "content": [
        {
          "type": "listItem",
          "content": [
            {
              "type": "paragraph",
              "content": [
                {
                  "type": "text",
                  "text": "Key Deliverables:",
                  "marks": [
                    {
                      "type": "strong"
                    }
                  ]
                }
              ]
            }
          ]
        }
      ]
    },
    {
      "type": "bulletList",
      "content": [
        {
          "type": "listItem",
          "content": [
            {
              "type": "paragraph",
              "content": [
                {
                  "type": "text",
                  "text": "Business Value/Expected Outcome is defined:",
                  "marks": [
                    {
                      "type": "strong"
                    }
                  ]
                },
                {
                  "type": "text",
                  "text": " How will we measure the business impact/outcomes (metric, dollar value, etc)?"
                }
              ]
            }
          ]
        }
      ]
    },
    {
      "type": "bulletList",
      "content": [
        {
          "type": "listItem",
          "content": [
            {
              "type": "paragraph",
              "content": [
                {
                  "type": "text",
                  "text": "Data teams needed to do the work:",
                  "marks": [
                    {
                      "type": "strong"
                    }
                  ]
                },
                {
                  "type": "text",
                  "text": " (Data Science, Data Eng)"
                }
              ]
            }
          ]
        }
      ]
    },
    {
      "type": "bulletList",
      "content": [
        {
          "type": "listItem",
          "content": [
            {
              "type": "paragraph",
              "content": [
                {
                  "type": "text",
                  "text": "Is there an MA dependency?:",
                  "marks": [
                    {
                      "type": "strong"
                    }
                  ]
                }
              ]
            }
          ]
        }
      ]
    },
    {
      "type": "bulletList",
      "content": [
        {
          "type": "listItem",
          "content": [
            {
              "type": "paragraph",
              "content": [
                {
                  "type": "text",
                  "text": "Other known dependencies:",
                  "marks": [
                    {
                      "type": "strong"
                    }
                  ]
                },
                {
                  "type": "text",
                  "text": " (Creative, Client, etc)"
                }
              ]
            }
          ]
        }
      ]
    }
  ]
})
    }
}