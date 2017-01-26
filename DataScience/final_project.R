
# use mongolite
if(!require(mongolite)) install.packages("mongolite")
library(mongolite)
# library(stringr)

# Connection configuration
MONGO_URL = "mongodb://group9:HAFYergq@141.5.113.177:27017/smartshark_test"
MONGOURL = "mongodb://group9:HAFYergq@141.5.113.177:27017/smartshark_test"
# MONGO_URL = "mongodb://localhost:27017/smartshark_test"

# load data
issue=mongo(collection="issue",url=MONGO_URL)$find()
issue_comment=mongo(collection="issue_comment",url=MONGO_URL)$find()
message=mongo(collection="message",url=MONGO_URL)$find()
event=mongo(collection="event",url=MONGO_URL)$find()
con_commit=mongo(collection="commit", url=MONGO_URL)
commit=con_commit$find()
tag=mongo(collection="tag",url=MONGO_URL)$find()

# load data from csv exported from mongodb
people_orig=read.csv("http://user.informatik.uni-goettingen.de/~chenfeng.zhu/data/people.csv")
# str_replace_all(people_orig$X_id,"ObjectId((","")
people_orig$X_id = gsub('[)]', '', gsub('ObjectId[(]', '', people_orig$X_id))

# classified by name
p_cat=cbind(people_orig, category=1)
for(i in 1:nrow(p_cat)) {
  if (grepl("dev",paste(p_cat[i,2],p_cat[i,3],p_cat[i,4]))==TRUE) {
    p_cat[i,5]=3
  }
  if (grepl("user",paste(p_cat[i,2],p_cat[i,3],p_cat[i,4]))==TRUE) {
    p_cat[i,5]=2
  }
}

# collect all people_id
mccids=message$cc_ids
mtoids=message$to_ids
mcci=c()
mtoi=c()
for(x in unique(mccids)) {
  for(y in unique(x)) {
    mcci=c(mcci, toString(y))
  }
}
for(x in unique(mtoids)) {
  for(y in unique(x)) {
    mtoi=c(mtoi, toString(y))
  }
}
tmpid = c(issue$creator_id, issue$reporter_id, issue_comment$author_id,
           message$from_id, mcci, mtoi,
           event$author_id,
           commit$author_id, commit$committer_id,
           tag$tagger_id)
people_id=unique(tmpid)
# people_id=na.omit(people_id)
people_id=people_id[!is.na(people_id)]

datatable=as.data.frame(people_id)

# commit relative
commit_auth=data.frame(table(commit$author_id))
colnames(commit_auth)=c("people_id","commit_auth_total")
commit_commit=data.frame(table(commit$committer_id))
colnames(commit_commit)=c("people_id","commit_commit_total")

datatable=merge(datatable,commit_auth,by.x="people_id",by.y="people_id",all.x=TRUE)
datatable=merge(datatable,commit_commit,by.x="people_id",by.y="people_id",all.x=TRUE)
datatable[is.na(datatable)]=0

# issue relative
issue_create=data.frame(table(issue$creator_id))
colnames(issue_create)=c("people_id","issue_create_total")
issue_report=data.frame(table(issue$reporter_id))
colnames(issue_report)=c("people_id","issue_report_total")

datatable=merge(datatable,issue_create,by.x="people_id",by.y="people_id",all.x=TRUE)
datatable=merge(datatable,issue_report,by.x="people_id",by.y="people_id",all.x=TRUE)

issue_comm=data.frame(table(issue_comment$author_id))
colnames(issue_comm)=c("people_id","issue_comment_total")
datatable=merge(datatable,issue_comm,by.x="people_id",by.y="people_id",all.x=TRUE)
datatable[is.na(datatable)]=0

# event relative
event_auth=data.frame(table(event$author_id))
colnames(event_auth)=c("people_id","event_auth_total")
datatable=merge(datatable,event_auth,by.x="people_id",by.y="people_id",all.x=TRUE)
datatable[is.na(datatable)]=0

# tag relative
tag_tag=data.frame(table(tag$tagger_id))
colnames(tag_tag)=c("people_id","tag_tag_total")
datatable=merge(datatable,tag_tag,by.x="people_id",by.y="people_id",all.x=TRUE)
datatable[is.na(datatable)]=0

# email relative
message_from=data.frame(table(message$from_id))
colnames(message_from)=c("people_id","message_from_total")
datatable=merge(datatable,message_from,by.x="people_id",by.y="people_id",all.x=TRUE)
datatable[is.na(datatable)]=0

datatable=merge(datatable,p_cat,by.x="people_id",by.y="X_id",all.x=TRUE)

write.csv(datatable, file="~/public_html/data/ds_dragon.csv")



datatable[2,]


# useful commands

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


