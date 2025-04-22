((issue?.getProjectObject().getProjectCategory() != "COO - Mike Waters" ||
issue?.getProjectObject().getProjectCategory() != "CMO - Stephany Verstraete" ||
issue?.getProjectObject().getProjectCategory() != "CLO - Adam Vandervoort" || 
issue?.getProjectObject().getProjectCategory() != "CFO - Mala Murthy") &&
(issue?.getIssueType()?.getName() == "Epic" || issue?.getIssueType()?.getName() == "Feature"))

//issue?.getProjectObject().getProjectCategory() != "COO - Mike Waters" ||
//issue?.getProjectObject().getProjectCategory() != "CMO - Stephany Verstraete" ||
//issue?.getProjectObject().getProjectCategory() != "CLO - Adam Vandervoort" || 
//issue?.getProjectObject().getProjectCategory() != "CFO - Mala Murthy" &&
//issue?.getIssueType()?.getName() == "Epic"