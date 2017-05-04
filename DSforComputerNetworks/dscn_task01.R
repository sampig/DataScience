###### Task01 - Bike Sharing #####

## Load libraries
library(ggplot2)
if (!require(sm))
  install.packages("sm")
library(sm)
if (!require(party))
  install.packages("party")
library(party)
if (!require(e1071))
  install.packages("e1071")
library(e1071)
if (!require(forecast))
  install.packages("forecast")
library(forecast)
if (!require(ModelMetrics))
  install.packages("ModelMetrics")
library(ModelMetrics)
# if(!require(rnn)) install.packages("rnn");library(rnn)


######################
###### Read data #####
######################
train_data <-
  read.table(
    "http://user.informatik.uni-goettingen.de/~chenfeng.zhu/data/train.csv",
    header = TRUE,
    sep = ","
  )
test_data <-
  read.table(
    "http://user.informatik.uni-goettingen.de/~chenfeng.zhu/data/test.csv",
    header = TRUE,
    sep = ","
  )
whole_data <- rbind(train_data, test_data)

## Dataset characteristics
## - instant: record index
## - dteday : date
## - season : season (1:springer, 2:summer, 3:fall, 4:winter)
## - yr : year (0: 2011, 1:2012)
## - mnth : month ( 1 to 12)
## - hr : hour (0 to 23)
## - holiday : whether day is holiday or not (extracted from http://dchr.dc.gov/page/holiday-schedule)
## - weekday : day of the week
## - workingday : if day is neither weekend nor holiday is 1, otherwise is 0.
## + weathersit :
##     - 1: Clear, Few clouds, Partly cloudy, Partly cloudy
##     - 2: Mist + Cloudy, Mist + Broken clouds, Mist + Few clouds, Mist
##     - 3: Light Snow, Light Rain + Thunderstorm + Scattered clouds, Light Rain + Scattered clouds
##     - 4: Heavy Rain + Ice Pallets + Thunderstorm + Mist, Snow + Fog
## - temp : Normalized temperature in Celsius. The values are divided to 41 (max)
## - atemp: Normalized feeling temperature in Celsius. The values are divided to 50 (max)
## - hum: Normalized humidity. The values are divided to 100 (max)
## - windspeed: Normalized wind speed. The values are divided to 67 (max)
## - casual: count of casual users
## - registered: count of registered users
## - cnt: count of total rental bikes including both casual and registered


#############################
###### Data Aggregation #####
#############################
## be used for time series prediction
## Count: useless
count_day <- aggregate(x = whole_data["dteday"],
                       by = list(date = substr(whole_data$dteday, 1, 10)),
                       FUN = length)
count_month <-
  aggregate(whole_data["dteday"], list(month = substr(whole_data$dteday, 1, 7)), length)
## Sum:
sum_day <-
  aggregate(cbind(casual, registered, cnt) ~ substr(dteday, 1, 10), data = whole_data, sum)
colnames(sum_day)[1] <- "dtday"
sum_month <-
  aggregate(cbind(casual, registered, cnt) ~ substr(dteday, 1, 7), data = whole_data, sum)
colnames(sum_month)[1] <- "dtmonth"


#############################
###### Data Exploration #####
#############################

## Basic properties
length(train_data$dteday)
length(unique(train_data$dteday))
summary(train_data)
head(train_data)
tail(train_data)
length(test_data$dteday)
summary(test_data)
length(whole_data$dteday) # 731d * 24h = 17544
length(unique(whole_data$dteday))
summary(whole_data)

## Visualization
## incorrect to use density because count must not be less than zero.
plot(density(train_data$casual))
plot(density(train_data$registered))
plot(density(train_data$cnt))
## related to hour
aggregate(cbind(casual, registered, cnt) ~ hr, data = train_data, sum)
plot(
  aggregate(casual ~ hr, data = train_data, sum),
  ylim = c(0, 250000),
  type = "b",
  col = "blue",
  main = ""
)
par(new = TRUE)
plot(
  aggregate(registered ~ hr, data = train_data, sum),
  ylim = c(0, 250000),
  type = "b",
  col = "red",
  main = "Comparison casual(blue) and registered(red) in each hour"
)
plot(aggregate(cnt ~ hr, data = train_data, sum))
## related to holiday
aggregate(cbind(casual, registered, cnt) ~ holiday, data = train_data, sum)
## related to weekday
aggregate(cbind(casual, registered, cnt) ~ weekday, data = train_data, sum)
plot(
  aggregate(casual ~ weekday, data = train_data, sum),
  ylim = c(0, 500000),
  type = "b",
  col = "blue",
  main = ""
)
par(new = TRUE)
plot(
  aggregate(registered ~ weekday, data = train_data, sum),
  ylim = c(0, 500000),
  type = "b",
  col = "red",
  main = "Comparison casual(blue) and registered(red) in each weekday"
)
plot(aggregate(cnt ~ weekday, data = train_data, sum))
## related to working day
aggregate(cbind(casual, registered, cnt) ~ workingday, data = train_data, sum)
## related to season
aggregate(cbind(casual, registered, cnt) ~ season, data = train_data, sum)
plot(
  aggregate(casual ~ season, data = train_data, sum),
  ylim = c(0, 800000),
  type = "b",
  col = "blue",
  main = ""
)
par(new = TRUE)
plot(
  aggregate(registered ~ season, data = train_data, sum),
  ylim = c(0, 800000),
  type = "b",
  col = "red",
  main = "Comparison casual(blue) and registered(red) in each season"
)
## related to weather
aggregate(cbind(casual, registered, cnt) ~ weathersit, data = train_data, sum)
plot(
  aggregate(casual ~ weathersit, data = train_data, sum),
  ylim = c(0, 1700000),
  type = "b",
  col = "blue",
  main = ""
)
par(new = TRUE)
plot(
  aggregate(registered ~ weathersit, data = train_data, sum),
  ylim = c(0, 1700000),
  type = "b",
  col = "red",
  main = "Comparison casual(blue) and registered(red) in each weather"
)

## Relationship between day-property (discrete) and registered.
ggplot(train_data, aes(x = registered, fill = factor(holiday))) + geom_density() + facet_grid(holiday ~ .)
ggplot(train_data, aes(x = registered, fill = factor(weekday))) + geom_density() + facet_grid(weekday ~ .)
ggplot(train_data, aes(x = registered, fill = factor(workingday))) + geom_density() + facet_grid(workingday ~ .)
ggplot(train_data, aes(x = registered, fill = factor(weathersit))) + geom_density() + facet_grid(weathersit ~ .)
## Relationship between environment (pseudo continous) and registered
ggplot(train_data, aes(x = temp, y = registered)) + geom_point() + geom_smooth(method = "lm")
ggplot(train_data, aes(x = temp, y = registered)) + geom_point() + geom_smooth(method = "auto")
ggplot(train_data, aes(x = atemp, y = registered)) + geom_point() + geom_smooth(method = "auto")
ggplot(train_data, aes(x = hum, y = registered)) + geom_point() + geom_smooth(method = "auto")
# ggplot(train_data, aes(x = windspeed, y = registered)) + geom_point() + geom_smooth(method = "auto")
# smoothing method (function) to use, eg. "lm", "glm", "gam", "loess", "rlm".
# ggplot(sum_month, aes(x = dtmonth, y = cnt)) + geom_point() + geom_line()
## Relationship between environment (pseudo continous) and casual
ggplot(train_data, aes(x = temp, y = casual)) + geom_point() + geom_smooth(method = "auto")
ggplot(train_data, aes(x = atemp, y = casual)) + geom_point() + geom_smooth(method = "auto")
ggplot(train_data, aes(x = hum, y = casual)) + geom_point() + geom_smooth(method = "auto")

## Comparison of density estimates
# attach(train_data)
# workingday.f <- factor(workingday, levels = c(0,1), labels = c("No", "Yes"))
sm.density.compare(train_data$registered, train_data$season)
sm.density.compare(train_data$registered, train_data$workingday)
sm.density.compare(train_data$registered, train_data$weekday)
# colfill <- c(2:(1+length(levels(workingday.f))))
# legend(locator(1), levels(workingday.f), fill = colfill)
# detach(train_data)
sm.density.compare(train_data$casual, train_data$season)
sm.density.compare(train_data$casual, train_data$workingday)
sm.density.compare(train_data$casual, train_data$weekday)

## Box Plot
## In this case, there are some outliers (values outside the range of 1.5*IQR).
boxplot(
  registered ~ weekday,
  data = train_data,
  notch = TRUE,
  varwidth = TRUE
)
boxplot(casual ~ weekday, data = train_data)


######################
##### Regression #####
######################

## Logistic Regression for predicting
coef(lm(cnt ~ temp + atemp + hum + windspeed, data = train_data))
coef(glm(cnt ~ temp + atemp + hum + windspeed, data = train_data))
coef(glm(
  cnt ~ temp + atemp + hum + windspeed,
  data = train_data,
  family = gaussian()
))
coef(glm(
  cnt ~ temp + atemp + hum + windspeed,
  data = train_data,
  family = poisson()
))
summary(glm(cnt ~ temp + atemp + hum + windspeed, data = train_data))
summary(glm(registered ~ temp + atemp + hum + windspeed, data = train_data))
summary(glm(casual ~ temp + atemp + hum + windspeed, data = train_data))
summary(
  glm(
    cnt ~ yr + mnth + hr + season + holiday + weekday + workingday + weathersit + temp + atemp + hum + windspeed,
    data = train_data
  )
)
summary(
  glm(
    registered ~ yr + mnth + hr + season + holiday + weekday + workingday + weathersit + temp + atemp + hum + windspeed,
    data = train_data
  )
)
summary(
  glm(
    casual ~ yr + mnth + hr + season + holiday + weekday + workingday + weathersit + temp + atemp + hum + windspeed,
    data = train_data
  )
)
summary(
  glm(
    registered ~ yr + mnth + hr + season + holiday + weekday + workingday + weathersit + temp + atemp + hum + windspeed,
    data = train_data,
    family = poisson()
  )
)

## Regression diagnostics
## A typical approach
par(mfrow = c(2, 2))
## Compare Gaussian and Poisson.
plot(glm(cnt ~ temp + atemp + hum + windspeed, data = train_data))
plot(glm(
  cnt ~ temp + atemp + hum + windspeed,
  data = train_data,
  family = poisson()
))
plot(glm(cnt ~ temp, data = train_data))
par(mfrow = c(1, 1))
## Anova for model selection
anova(
  lm(cnt ~ temp + atemp + hum, data = train_data),
  lm(cnt ~ temp + atemp + hum + windspeed, data = train_data)
)
anova(
  glm(cnt ~ temp + atemp + hum, data = train_data),
  glm(cnt ~ temp + atemp + hum + windspeed, data = train_data),
  test = "Chisq"
) # windspeed is important for total
anova(
  glm(cnt ~ atemp + hum + windspeed, data = train_data),
  glm(cnt ~ temp + atemp + hum + windspeed, data = train_data),
  test = "Chisq"
) # temp is a little important for total
anova(
  glm(registered ~ atemp + hum + windspeed, data = train_data),
  glm(registered ~ temp + atemp + hum + windspeed, data = train_data),
  test = "Chisq"
) # temp is not important for registered
anova(
  glm(cnt ~ temp + atemp + hum + windspeed, data = train_data),
  glm(
    cnt ~ temp + atemp + hum + windspeed,
    data = train_data,
    family = poisson()
  )
)


##########################
##### Classification #####
##########################

## Naive Bayes (useless)
# naiveBayes(registered ~ temp + atemp + hum + windspeed, data = train_data)
# nb_registered <-
#   naiveBayes(registered ~ hr + season + weekday + workingday,
#              data = train_data)
# nb_registered_train <-
#   predict(nb_registered, train_data[, c("hr",
#                                         "season",
#                                         "weekday",
#                                         "workingday")])
# nb_registered_test <- predict(nb_registered, test_data[, c("hr",
#                                                            "season",
#                                                            "weekday",
#                                                            "workingday")])
# table(nb_registered_train, train_data[, c("hr",
#                                           "season",
#                                           "weekday",
#                                           "workingday")], dnn = list('predict', 'actual'))
# table(nb_registered_test, test_data[, c("hr",
#                                         "season",
#                                         "weekday",
#                                         "workingday")], dnn = list('predict', 'actual'))

## Binary Trees (useless)
plot(ctree(registered ~ temp + atemp + hum + windspeed, data = train_data))
plot(ctree(registered ~ atemp + hum + windspeed, data = train_data))

plot(
  ctree(
    registered ~ hr + season + weekday + workingday + atemp + hum + windspeed,
    data = train_data
  )
)

ct_registered <-
  ctree(registered ~ atemp + hum + windspeed, data = train_data)
ct_registered
table(predict(ct_registered, train_data[, c("atemp",
                                            "hum",
                                            "windspeed")]), train_data[, "registered"])
ct_registered_test <- predict(ct_registered, test_data[, c("atemp",
                                                           "hum",
                                                           "windspeed")])
table(ct_registered_test, test_data[, "registered"])


##################################
##### Time Series Prediction #####
##################################

## ARIMA month
ts_cnt_month <-
  ts(sum_month$cnt,
     frequency = 12,
     start = c(2011, 1))
plot(ts_cnt_month)
dec_cnt_month <- decompose(ts_cnt_month)
# ts_cnt_month_add <- decompose(ts_cnt_month, type="additive")
plot(dec_cnt_month)
dec_cnt_month_adjust <- ts_cnt_month - dec_cnt_month$seasonal
arima_cnt_month <-
  arima(dec_cnt_month_adjust, order = c(1L, 1L, 1L))
arima_cnt_month <- auto.arima(dec_cnt_month_adjust)
forecast_cnt_month <- forecast.Arima(arima_cnt_month, h = 12)
plot(forecast_cnt_month)
## ARIMA day (doesn't work)
ts_cnt_day <-
  ts(sum_day$cnt, frequency = 365.25, start = c(2011, 1))
plot(ts_cnt_day)
dec_cnt_day <- decompose(ts_cnt_day)
plot(dec_cnt_day)
dec_cnt_day_adjust <- ts_cnt_day - dec_cnt_day$seasonal
plot(dec_cnt_day_adjust)
arima_cnt_day <- auto.arima(dec_cnt_day_adjust)
forecast_cnt_day <- forecast.Arima(arima_cnt_day, h = 365)
plot(forecast_cnt_day)

## Multi-Seasonal Time Series
msts_cnt_day <- msts(sum_day$cnt, seasonal.periods = c(7, 365.25))
#y <- msts(sum_day$cnt, seasonal.periods = c(7, 30.4375, 365.25))
plot(msts_cnt_day)
fit_cnt_day <- tbats(msts_cnt_day)
fc_cnt_day <- forecast(fit_cnt_day)
plot(fc_cnt_day)

## Others
fc_month1 <- meanf(ts_cnt_month, h = 12)
fc_month2 <- rwf(ts_cnt_month, h = 12)
fc_month3 <- snaive(ts_cnt_month, h = 12)
plot(fc_month1)
plot(fc_month2)
plot(fc_month3)
plot(fc_month1, plot.conf = FALSE, main = "Forecasts from Mean, Naive and Seasonal naive")
lines(fc_month2$mean, col = 2)
lines(fc_month3$mean, col = 3)
# legend("topright", lty=1, col=c(4,2,3),
#       legend=c("Mean method","Naive method","Seasonal naive method"))
## daily doesn't work
fc_day1 <- meanf(ts_cnt_day, h = 360)
fc_day2 <- rwf(ts_cnt_day, h = 360)
fc_day3 <- snaive(ts_cnt_day[1:720], h = 360)
plot(fc_day3)
plot(fc_day1, plot.conf = FALSE, ylim = c(0, 8000))
lines(fc_day2$mean, col = 2)
lines(fc_day3$mean, col = 3)

######################
##### Evaluation #####
######################

## MAE (Mean Absolute Error)
mae(test_data, predicted)

## Accuracy
accuracy(forecast_cnt_month)
accuracy(forecast_cnt_day)
accuracy(fc_cnt_day)
accuracy(fc_month1)
accuracy(fc_month2)
accuracy(fc_month3)
accuracy(fc_day1)
accuracy(fc_day2)
accuracy(fc_day3)
