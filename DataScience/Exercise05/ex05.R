# Exercise 04

# 1. Load libraries
# install.packages("forecast")
library(forecast)

# 2. Load the data
nybirths <- scan("http://robjhyndman.com/tsdldata/data/nybirths.dat")
nybirths

# 3. Create a time series object for the data
ts_births <- ts(nybirths, frequency = 12, start = c(1946, 1)) # end=c(2017, 1)
ts_births
plot(ts_births)

# 4. Decompose the data
dec_births <- decompose(ts_births)
dec_births$trend
dec_births$seasonal
dec_births$random
# plot the result
plot(dec_births$trend)
plot(dec_births$seasonal)
plot(dec_births$random)

# 5. Calculate an adjusted time series





myts <- ts(1:1000, frequency = 12, start = c(1946, 1), end=c(2017, 1))
myts
plot(myts)
gnp <- ts(cumsum(1 + round(rnorm(100), 2)), start = c(1954, 7), frequency = 12)
gnp
plot(gnp)
ts(1:100, start=c(2009, 1), end=c(2014, 12), frequency=12)
ts(1:10, frequency = 12, start = c(1959, 2))

