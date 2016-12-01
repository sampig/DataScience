
# 2.1 Create 3 data samples with normally distributed data:
# random creation
s1 <- rnorm(100, mean=0, sd=1)
s2 <- rnorm(100, mean=1.5, sd=1)
s3 <- rnorm(10, mean=1.5, sd=1)

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
plot(density(s1), col="blue", xlim=c(-5,5), ylim=c(0, 0.4), main="")
par(new=TRUE)
plot(density(s2), col="red", xlim=c(-5,5), ylim=c(0, 0.4), main="Comparison with S1 and S2")

# compare S1 and S3
plot(density(s1), col="blue", xlim=c(-5,5), ylim=c(0, 1), main="")
par(new=TRUE)
plot(density(s3), col="green", xlim=c(-5,5), ylim=c(0, 1), main="Comparison with S1 and S3")

# compare S2 and S3
plot(density(s2), col="red", xlim=c(-5,5), ylim=c(0, 1), main="")
par(new=TRUE)
plot(density(s3), col="green", xlim=c(-5,5), ylim=c(0, 1), main="Comparison with S2 and S3")

# compare S1, S2 and S3
plot(density(s1), col="blue", xlim=c(-5,5), ylim=c(0, 1), main="")
par(new=TRUE)
plot(density(s2), col="red", xlim=c(-5,5), ylim=c(0, 1), main="")
par(new=TRUE)
plot(density(s3), col="green", xlim=c(-5,5), ylim=c(0, 1), main="Comparison with all")


# 2.4 Interpret


# 2.5 Preform t-test
# between S1 and S2
t.test(s1, s2)
wilcox.test(s1, s2)

# between S1 and S3
t.test(s1, s3)

# others
t.test(rnorm(1000), rnorm(1000))
t.test(rnorm(1000), rnorm(1000, 0.1, 1))
t.test(rnorm(1000), rnorm(1000, 0.5, 1))
t.test(rnorm(1000), rnorm(1000, 1.5, 1))
t.test(rnorm(1000), rnorm(1000, 2.5, 1))

# 2.6 The kmeans algorithm
iris
# k=2
cluster2_1 <- kmeans(iris[, 3:4], 2)
cluster2_1
cluster2_2 <- kmeans(iris[, 3:4], 2)
cluster2_3 <- kmeans(iris[, 3:4], 2)
cluster2_1$centers
cluster2_2$centers
cluster2_3$centers

# k=3
cluster3_1 <- kmeans(iris[, 3:4], 3)
cluster3_2 <- kmeans(iris[, 3:4], 3)
cluster3_3 <- kmeans(iris[, 3:4], 3)
cluster3_1$centers
cluster3_2$centers
cluster3_3$centers

# k=4
cluster4_1 <- kmeans(iris[, 3:4], 4)
cluster4_2 <- kmeans(iris[, 3:4], 4)
cluster4_3 <- kmeans(iris[, 3:4], 4)
cluster4_1$centers
cluster4_2$centers
cluster4_3$centers


iris$Petal.Length
iris$Petal.Width


# 2.7 Visualize

par(mfrow=c(3,1))
plot(iris[, 3:4], col=cluster2_1$cluster)
points(cluster2_1$centers, col=1:3, pch=3, cex=2)

plot(iris[, 3:4], col=cluster2_2$cluster)
points(cluster2_1$centers, col=1:3, pch=3, cex=2)

plot(iris[, 3:4], col=cluster2_3$cluster)
points(cluster2_1$centers, col=1:3, pch=3, cex=2)


par(mfrow=c(3,1))
plot(iris[, 3:4], col=cluster3_1$cluster)
points(cluster3_1$centers, col=1:3, pch=3, cex=2)

plot(iris[, 3:4], col=cluster3_2$cluster)
points(cluster3_2$centers, col=1:3, pch=3, cex=2)

plot(iris[, 3:4], col=cluster3_3$cluster)
points(cluster3_3$centers, col=1:3, pch=3, cex=2)


par(mfrow=c(3,1))
plot(iris[, 3:4], col=cluster4_1$cluster)
points(cluster4_1$centers, col=1:3, pch=3, cex=2)

plot(iris[, 3:4], col=cluster4_2$cluster)
points(cluster4_2$centers, col=1:3, pch=3, cex=2)

plot(iris[, 3:4], col=cluster4_3$cluster)
points(cluster4_3$centers, col=1:3, pch=3, cex=2)

library(ggplot2)
cluster2_1$cluster <- as.factor(cluster2_1$cluster)
ggplot(iris, aes(Petal.Length, Petal.Width, color=cluster2_1$cluster)) + geom_point()
points(cluster2_1$centers, col=1:3, pch=3, cex=2)

cluster3_1$cluster <- as.factor(cluster3_1$cluster)
ggplot(iris, aes(Petal.Length, Petal.Width, color=cluster3_1$cluster)) + geom_point()

cluster4_1$cluster <- as.factor(cluster4_1$cluster)
ggplot(iris, aes(Petal.Length, Petal.Width, color=cluster4_1$cluster)) + geom_point()



# Iteration

cluster3_i1 <- kmeans(iris[, 3:4], 3)
cluster3_i1$centers
cluster3_i2 <- kmeans(iris[, 3:4], cluster3_i1$centers)
cluster3_i2$centers
cluster3_i3 <- kmeans(iris[, 3:4], cluster3_i2$centers)
cluster3_i3$centers



# Max iteration

cluster_0 <- kmeans(iris[, 3:4], 3)
cluster_1 <- kmeans(iris[, 3:4], 3, iter.max=1)
cluster_2 <- kmeans(iris[, 3:4], 3, iter.max=2)
cluster_3 <- kmeans(iris[, 3:4], 3, iter.max=3)
cluster_5 <- kmeans(iris[, 3:4], 3, iter.max=5)
cluster_9 <- kmeans(iris[, 3:4], 3, iter.max=9)

par(mfrow=c(2,2))
plot(iris[, 3:4], col=cluster_0$cluster)
points(cluster_0$centers, col=1:3, pch=3, cex=2)
plot(iris[, 3:4], col=cluster_1$cluster)
points(cluster_1$centers, col=1:3, pch=3, cex=2)
plot(iris[, 3:4], col=cluster_5$cluster)
points(cluster_5$centers, col=1:3, pch=3, cex=2)
plot(iris[, 3:4], col=cluster_9$cluster)
points(cluster_9$centers, col=1:3, pch=3, cex=2)

