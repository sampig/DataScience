
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


# 3 visualize mpg and its density
# display the frequency of mpg in some ranges.
hist(new_var$mpg, xlab="Miles per Gallon", main="Histogram of frequency of MPG")
# plot(new_var$mpg, xlab="Miles per Gallon")

# display the density of mpg in plot.
plot(density(new_var$mpg), xlab="Miles per Gallon", ylab="density", main="Density of Miles per Gallon")


# 4 Extends with a fuel consumption column
# fc = (3.78541*100) / (1.60934*mpg)
new_var["fc"] <- 3.78541*100/1.60934/new_var$mpg

# remove the column
# new_var["fc"]<-NULL


# 5 Visulaize the relationship
# the relationship should be like y=c/x
plot(new_var$mpg, new_var$fc, xlab="Miles per Gallon", ylab="Fuel Consumption (liter/100km)", main="Relationship between mpg and fc")



