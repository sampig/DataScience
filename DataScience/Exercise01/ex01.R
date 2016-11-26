
# R CMD BATCH ex01.R

# 1.1 create a new variable
new_var <- mtcars

# 1.2 display its type and class
paste("type: ", typeof(new_var))
paste("class: ", class(new_var))
paste("attributes: ", attributes(new_var))


# 2 calculate the mean, median and max
# mean: the common average. the sum of the values divided by the number (normally).
apply(new_var,2,mean)
sapply(new_var,mean)
sapply(new_var,mean,na.rm=FALSE)

# median: the center number. if the number is odd, the central value; if the number is even, the average of the 2 central values (normally).
apply(new_var,2,median)

apply(new_var,2,max)

summary(new_var)


# 3 visualize mpg and its density
# display the frequency of mpg in some ranges.
plot(new_var$mpg, ylab="Miles per Gallon")
plot(sort(new_var$mpg), ylab="Miles per Gallon")
dotchart(new_var$mpg, labels=row.names(new_var), xlab="Miles Per Gallon")
# labels are useless after sorting
# dotchart(sort(new_var$mpg), labels=row.names(new_var), xlab="Miles Per Gallon")

h <- hist(new_var$mpg, xlab="Miles per Gallon", main="Histogram of frequency of MPG")

# display the density of mpg in plot.
p <- plot(density(new_var$mpg), xlab="Miles per Gallon", ylab="density", main="Density of Miles per Gallon")
rug(new_var$mpg, side=1, col="red")

# frequency with density
d <- density(mtcars$mpg)
d$y <- d$y * (h$counts / h$density)[1]
plot(h, col="grey", xlab="Miles per Gallon")
lines(d, col="blue")

# density with frequency
hist(new_var$mpg, density=10, breaks=5, prob=TRUE, 
     xlab="Miles per Gallon", ylim=c(0, 0.1))
curve(dnorm(x, mean=mean(new_var$mpg), sd=sqrt(var(new_var$mpg))), 
      col="blue", lwd=2, add=TRUE, yaxt="n")

# 4 Extends with a fuel consumption column
# fc = (3.78541*100) / (1.60934*mpg)
new_var["fc"] <- 3.78541*100/1.60934/new_var$mpg

# remove the column
# new_var["fc"]<-NULL


# 5 Visulaize the relationship
# the relationship should be like y=c/x
plot(new_var$mpg, new_var$fc, xlab="Miles per Gallon", ylab="Fuel Consumption (liter/100km)", main="Relationship between mpg and fc")



