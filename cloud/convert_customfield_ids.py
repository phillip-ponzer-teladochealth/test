import os
import re

import requests

JIRA_CLOUD_URL = "https://teladochealth-sandbox.atlassian.net"
JIRA_CLOUD_EMAIL = "FIRSTNAME.LASTNAME@teladochealth.com"
JIRA_CLOUD_TOKEN = "YOUR_TOKEN_GOES_HERE"

session = requests.session()
session.auth = (JIRA_CLOUD_EMAIL, JIRA_CLOUD_TOKEN)


def get_from_jira(cmd):
    url = f"{JIRA_CLOUD_URL}/rest/api/3/{cmd}"
    resp = session.get(url)
    return resp.json()


def main():
    jira_fields_by_name = {field["name"].lower(): field for field in get_from_jira("field")}

    files_to_update = []
    for root, _, files in os.walk("."):
        for file in files:
            _, ext = os.path.splitext(file)
            if ext.lower() in (".js", ".groovy"):
                files_to_update.append(os.path.join(root, file))

    for code_file in files_to_update:
        with open(code_file) as file:
            lines = file.readlines()

        needs_update = False
        for i, line in enumerate(lines):
            match = re.search(r"(customfield_\d{5}).+//\s*\"(.+?)\"", line)
            if match:
                customfield_id, customfield_name = match.groups()
                if customfield_name.lower() not in jira_fields_by_name:
                    raise Exception(f"Couldn't find field by name \"{customfield_name}\"")

                customfield_id_new = jira_fields_by_name[customfield_name.lower()]["key"]
                if customfield_id != customfield_id_new:
                    lines[i] = line.replace(customfield_id, customfield_id_new)
                    needs_update = True

        if needs_update:
            with open(code_file, "w", newline="", encoding="utf-8") as file:
                file.writelines(lines)


if __name__ == "__main__":
    main()
