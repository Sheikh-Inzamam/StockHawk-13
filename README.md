## Stock Hawk - project 3 for Udacity Android Nanodegree

In this project I updated a sample stock ticker app to add stock charting and an app widget to show stock quotes on the device home screen.

### Author
[Dave Allen](https://github.com/debun8)

### Credits
Tip - how to set up a Content Observer that updates the widget collection when the data source has changed [link](https://android.googlesource.com/platform/development/+/master/samples/WeatherListWidget/src/com/example/android/weatherlistwidget/WeatherWidgetProvider.java)

### License
Apache 2.0 See the LICENSE file for details

## Comments on the Project
- This was a good 'real world' project, I learned how to get, parse and chart financial data.
- I also learned how to do an AppWidget.

----

## The Assignment --

In this project, you will create an app with multiple flavors that uses
multiple libraries and Google Could Endpoints. The finished app will consist
of four modules. A Java library that provides jokes, a Google Could Endpoints
(GCE) project that serves those jokes, an Android Library containing an
activity for displaying jokes, and an Android app that fetches jokes from the
GCE module and passes them to the Android Library for display.

### Why this Project

As Android projects grow in complexity, it becomes necessary to customize the
behavior of the Gradle build tool, allowing automation of repetitive tasks.
Particularly, factoring functionality into libraries and creating product
flavors allow for much bigger projects with minimal added complexity.

### What Will I Learn?

* Learn how to get stock market history data with the Yahoo Finance API
* Learn how to parse that data and use it to draw stock charts
* How to create an Android AppWidget that lives on the device home screen and shows a collection of stock quotes
* Use Google Cloud Messaging (GCM) Network Manager service to do periodic and one-off network requests
* Use an Android Content Provider to store and query stock market data


# Rubric

* Each stock quote on the main screen is clickable and leads to a new screen which graphs the stock’s value over time.

*
*
*
*
* Stock Hawk does not crash when a user searches for a non-existent stock.


### Required Behavior

* App retrieves jokes from Google Cloud Endpoints module and displays them via an Activity from the Android Library.

### Optional Components

To receive "exceeds specifications", your app must fully implement all of the following items.

* The free app variant displays interstitial ads between the main activity and the joke-displaying activity.
* The app displays a loading indicator while the joke is being fetched from the server.
* The root build.gradle file contains a task that will start up the GCE development server, run all Android tests, then shutdown the development server.
