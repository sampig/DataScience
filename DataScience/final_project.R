
# use mongolite
if(!require(mongolite)) install.packages("mongolite")
library(mongolite)

# Connection configuration
MONGO_URL = "mongodb://group9:HAFYergq@141.5.113.177:27017/smartshark_test"
# MONGO_URL = "mongodb://localhost:27017/smartshark_test"

con_project = mongo(collection = "project", url = MONGO_URL)
project = con_project$find()

con_file = mongo(collection = "file", url = MONGO_URL)
file = con_file$find()

con_fileaction = mongo(collection = "file_action", url = MONGO_URL)
fileaction = con_fileaction$find()

con_people = mongo(collection = "people", url = MONGO_URL)

people = con_people$find()

people = con_people$find('{"username":"zookeeper-user"}')

con_commit = mongo(collection="commit", url=MONGO_URL)
commits = con_commit$find()
commits = con_commit$find(fields='{"_id":1, "committer_date":1}')
print(paste("latest commit:", max(commits$committer_date)))
latest_commit_id = commits[which.max(commits$committer_date),1]

con_codeentitystate = mongo(collection="code_entity_state", url=MONGO_URL)
query_str = paste('{"commit_id":{"$oid": "',latest_commit_id,'"}}', sep="")
code_entities = con_codeentitystate$find(query_str)


