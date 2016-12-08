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

# Association: Apriori

# 3.1.2 Load data
association_data <- read.transactions("http://user.informatik.uni-goettingen.de/~sherbold/AssociationRules.csv", rm.duplicates=FALSE, format="basket", sep=" ")
association_data

# 3.1.3 Train association rules with the apriori command.
# 0.2 0.1 low confidence
# 0.2 0.4 only 1 rule
# 0.2 >=0.5 no rules
# 0.3 0.4 only 1 rule
# 0.4 0.4 only 1 rule
# >=0.5 0.4 no rules
# 0.001 0.5 too many rules and too high lift (166701)
# 0.003 0.5 too many rules (17248)
# 0.005 0.5 many rules (5946)
# 0.007 0.5 rules (2672)
# 0.009 0.5 rules (1562)
# 0.01 0.5 rules (1562) and lift 
apriori_rule <- apriori(association_data, parameter=list(support=0.01, confidence=0.5))
summary(apriori_rule)
inspect(head(sort(apriori_rule, by="lift"), n=10))
inspect(head(sort(apriori_rule, by="confidence"), n=10))

# 3.1.4 Visualize the results with the plot command.
plot(apriori_rule)
plot(apriori_rule, interactive=TRUE)

# 3.1.5


# Regression: Logistic

# 3.2.1 Load data
cuse <- read.table("http://data.princeton.edu/wws509/datasets/cuse.dat", header=TRUE)
summary(cuse)
# table(cuse$education)

#3.2.2 Train a logistic regression model
cb_formula <- cbind(cuse$using, cuse$notUsing) ~ cuse$age + cuse$education + cuse$wantsMore
g <- glm(cb_formula, family=binomial(logit), data=cuse)

#3.2.3 Summary and important features
summary(g)
coef(g)
exp(coef(g))

cb_formula1 <- cbind(cuse$notUsing, cuse$using) ~ cuse$age + cuse$education + cuse$wantsMore
g1 <- glm(cb_formula1, family=binomial(logit), data=cuse)
summary(g1)

cb_formula2 <- cbind(cuse$using, cuse$using) ~ cuse$age + cuse$education + cuse$wantsMore
g2 <- glm(cb_formula2, family=binomial(logit), data=cuse)
summary(g2)



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


data(Affairs, package = "AER")
summary(Affairs)
table(Affairs$affairs)

Affairs$ynaffair[Affairs$affairs > 0] <- 1
Affairs$ynaffair[Affairs$affairs == 0] <- 0
Affairs$ynaffair <- factor(Affairs$ynaffair, levels=c(0,1), labels=c("No","Yes"))
table(Affairs$ynaffair)

