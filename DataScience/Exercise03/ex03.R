# Exercise 03

# 3.1.1 Load libraries
# install.packages("arules")
# install.packages("arulesViz")
#   SystemRequirements: libcurl: libcurl-devel (rpm) or libcurl4-openssl-dev (deb)
#   install.packages("plotly")
#   install.packages("BH", dependencies=TRUE, INSTALL_opts = c('--no-lock'))
#   R CMD INSTALL <pkg_name>.tar.gz
# packageVersion("arules")
# packageVersion("arulesViz")
library(arules)
library(arulesViz)


# 3.1.2 Load data
association_data <- read.transactions("http://user.informatik.uni-goettingen.de/~sherbold/AssociationRules.csv", rm.duplicates=FALSE, format="basket", sep=" ")


# 3.1.3 Train association rules with the apriori command.
apriori_rule <- apriori(association_data, parameter=list(support=0.005, confidence=0.5))
summary(apriori_rule)

# 3.1.4 Visualize the results with the plot command.
plot(apriori_rule)
plot(apriori_rule, interactive=TRUE)

# 4.1.5


# 3.2.1 Load data
cuse <- read.table("http://data.princeton.edu/wws509/datasets/cuse.dat", header=TRUE)

#3.2.2 Train a logistic regression model
cb_formula <- cbind(cuse$using, cuse$notUsing) ~ cuse$age + cuse$education + cuse$wantsMore
g <- glm(cb_formula, family=binomial(logit), data=cuse)

#3.2.3 Summary and important features
summary(g)





# Testing

data(Groceries)
a_rule <- apriori(Groceries, parameter=list(support=0.005, confidence=0.5))
plot(a_rule)
plotly_arules(a_rule)
inspectDT(a_rule)

data("Adult")
## Mine association rules.
rules <- apriori(Adult, parameter = list(supp = 0.5, conf = 0.9, target = "rules"))
summary(rules)
plot(rules)


counts <- c(18,17,15,20,10,20,25,13,12)
outcome <- gl(3,1,9)
treatment <- gl(3,3)
print(d.AD <- data.frame(treatment, outcome, counts))
glm.D93 <- glm(counts ~ outcome + treatment, family = poisson())
anova(glm.D93)
summary(glm.D93)


