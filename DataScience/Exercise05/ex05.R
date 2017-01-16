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
# plot.ts(ts_births)


# 4. Decompose the data
dec_births <- decompose(ts_births)
dec_births$trend
dec_births$seasonal
dec_births$random
# plot the result
plot(dec_births)
plot(dec_births$x, col="black", ylim=c(20, 30), ylab="data")
par(new=TRUE)
plot(dec_births$trend, col="red", ylim=c(20, 30), ylab="trend")
plot(dec_births$seasonal, col="blue", ylim=c(-2, 2), ylab="seasonal")
par(new=TRUE)
plot(dec_births$random, col="green", ylim=c(-2, 2), ylab="random")
# try additive and multiplicative
# additive: ts = seasonal + trend + random
# multiplicative: ts = seasonal * trend * random
dec_births_add <- decompose(ts_births, type="additive")
plot(dec_births_add)
dec_births_multi <- decompose(ts_births, type="multiplicative")
plot(dec_births_multi)

stl_birth = stl(ts_births, s.window="periodic")
plot(stl_birth)
plot(stl_birth$time.series)


# 5. Calculate an adjusted time series without seasonality
dec_births_cal <- ts_births - dec_births$seasonal
dec_births_cal
plot(dec_births_cal)
plot(dec_births_cal, col="red")
par(new=TRUE)
plot(ts_births, col="black")


# 6. Use the adjusted time series
# p=q=d=1
arima_births <- arima(ts_births, order = c(1L, 1L, 1L))
arima_births
# automatical parameters
arima_auto_births <- auto.arima(ts_births)
arima_auto_births


# 7. Forcast the next 12 months
forecast_births <- forecast.Arima(arima_births, h=12)
forecast_births
plot(forecast_births)
forecast_births2 <- forecast.Arima(arima_auto_births, h=12)
forecast_births2
plot(forecast_births2)
plot(forecast_births, ylim=c(20, 32), col="blue")
par(new=TRUE)
plot(forecast_births2, ylim=c(20, 32), col="red")



# test

myts <- ts(1:100, frequency = 12, start = c(1946, 1), end=c(2017, 1))
myts
plot(myts)
# subset the time series from 2008 forward using window commd
house2 = window(myts, start=c(2008,1), end=c(2013, 6))
plot(house2)

# stl: Seasonal Decomposition of Time Series by LOESS
# fit the stl model using only the s.window argument
fit = stl(house2, s.window="periodic")
plot(fit)
# another fit this time setting the t.window argument, which changes the number of lags used in the LOESS smoothing parameters
fit2 = stl(house2, s.window="periodic", t.window=15)
plot(fit2)

gnp <- ts(cumsum(1 + round(rnorm(100), 2)), start = c(1954, 7), frequency = 12)
gnp
plot(gnp)
ts(1:100, start=c(2009, 1), end=c(2014, 12), frequency=12)
ts(1:10, frequency = 12, start = c(1959, 2))

diff(ts_births,1,1)
