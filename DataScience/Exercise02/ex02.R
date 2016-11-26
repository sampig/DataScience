
# 2.1 Create 3 data samples with normally distributed data:
# random creation
s1 <- rnorm(100, mean=0, sd=1)
s2 <- rnorm(100, mean=1.5, sd=1)
s3 <- rnorm(10, mean=0, sd=1)

s4 <- rnorm(1000, mean=0, sd=1)
s5 <- rnorm(100000)

# the probability at the value
dnorm(-1, mean = 0, sd = 1, log = FALSE)
dnorm(1, mean = 0, sd = 1, log = FALSE)
# probabilities for cumulative density
pnorm(-1.96, lower.tail=FALSE) - pnorm(1.96, lower.tail=FALSE)
pnorm(-2.33, lower.tail=FALSE) - pnorm(2.33, lower.tail=FALSE)
# inverse for pnorm
qnorm(pnorm(-1.96, lower.tail=FALSE))
qnorm(pnorm(-1.96, lower.tail=FALSE), lower.tail=FALSE)


# 2.2 Plot the densities of them:
plot(density(s1))
plot(density(s2))
plot(density(s3))

plot(density(s4))
plot(density(s5))


# 2.3 Plot together
# compare S1 and S2
plot(density(s1), col="blue", xlim=c(-5,5), ylim=c(0, 0.4))
par(new=TRUE)
plot(density(s2), col="red", xlim=c(-5,5), ylim=c(0, 0.4))

# compare S1 and S3
plot(density(s1), col="blue", xlim=c(-5,5), ylim=c(0, 1))
par(new=TRUE)
plot(density(s3), col="green", xlim=c(-5,5), ylim=c(0, 1))

# compare S1, S2 and S3
plot(density(s1), col="blue", xlim=c(-5,5), ylim=c(0, 1))
par(new=TRUE)
plot(density(s2), col="red", xlim=c(-5,5), ylim=c(0, 0.4))
par(new=TRUE)
plot(density(s3), col="green", xlim=c(-5,5), ylim=c(0, 1))


# 2.4 Interpret


# 2.5 Preform t-test
# between S1 and S2
t.test(s1, s2)
wilcox.test(s1, s2)

# between S1 and S3
t.test(s1, s3)


# 2.6 The kmeans algorithm

cluster2 <- kmeans(iris[, 3:4], 2, nstart = 20)
cluster2

cluster3 <- kmeans(iris[, 3:4], 3, nstart = 20)
cluster3

cluster3 <- kmeans(iris[, 3:4], 4, nstart = 20)
cluster3

iris$Petal.Length
iris$Petal.Width


# 2.7 Visualize
library(ggplot2)
ggplot(iris, aes(Petal.Length, Petal.Width, color=Species)) + geom_point()

cluster2$cluster <- as.factor(cluster2$cluster)
ggplot(iris, aes(Petal.Length, Petal.Width, color=Species)) + geom_point()



