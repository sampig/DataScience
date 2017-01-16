# Exercise 04


# 1. Load libraries
# install.packages("e1071")
# install.packages("party")
library(e1071)
library(party)


# 2. Create a training set and a test set from iris.
origi_set <- iris
train <- sample(nrow(iris), 100)
training_set <- iris[train,]
test_set <- iris[-train,]


# 3. Train naive Bayes classifier
# using all
nb_all <- naiveBayes(training_set[,1:4], training_set[,5])
nb_all
table(predict(nb_all, training_set[,1:4]), training_set[,5], dnn = list('predict','actual'))

# using each
nb_each <- naiveBayes(Species ~ ., data = training_set)
nb_each
table(predict(nb_each, training_set), training_set[,5])
# using given column
nb_pl <- naiveBayes(Species ~ Petal.Length, data = training_set)
nb_pl
table(predict(nb_pl, training_set), training_set[,5])
nb_sl <- naiveBayes(Species ~ Sepal.Length, data = training_set)
nb_sl
table(predict(nb_sl, training_set), training_set[,5])
nb_sw <- naiveBayes(Species ~ Sepal.Width, data = training_set)
nb_sw
table(predict(nb_sw, training_set), training_set[,5])


# 4. Train a decision tree
ct_all <- ctree(Species ~ .,data = training_set)
ct_all
table(predict(ct_all, training_set[,1:4]), training_set[,5])


# 5. Plot the decision tree
plot(ct_all)


# 6. Evaluate the results
# naive bayes
nb_ptrain <- predict(nb_all, training_set[,1:4])
nb_ptest <- predict(nb_all, test_set[,1:4])
nb_ptrain
nb_ptest
table(nb_ptrain, training_set[,5], dnn = list('predict','actual'))
table(nb_ptest, test_set[,5], dnn = list('predict','actual'))
# decision tree
ct_ptrain <- predict(ct_all, training_set[,1:4])
ct_ptest <- predict(ct_all, test_set[,1:4])
ct_ptrain
ct_ptest
table(ct_ptrain, training_set[,5])
table(ct_ptest, test_set[,5])




#training_set <- iris[sample(1:nrow(iris), 100, replace=FALSE),]
#sample(iris, 100, replace=TRUE)
#iris[iris[,1]!=training_set[,1]]
#subset(iris, !(iris[,1:5] %in% training_set[,1:5]))
#[sample(1:nrow(iris), 50, replace=FALSE),]


