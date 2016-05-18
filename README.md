# 2016 Presidential Twitter News Coverage

This visualization aims to display changes in sentiment (positive or negative opinions) expressed, on Twitter, by some of the most popular TV networks about presidential candidates over the course of the campaign.

A measure called impact (sentiment score * number of retweets) has been introduced to measure the opinionated impact of tweets.

It has been aggregated by weeks and displayed as a timeline to show trends and values at particular time.
Furthermore, we can access the most impactful positive and negative tweets at each period of time in order to attempt at finding out what might have been the reasons for sudden changes (if such exist) in sudden changes.

## Demo
http://nyu-cs6313-spring2016.github.io/Group-19/

## Screencast

TODO

## Write-up
http://nyu-cs6313-spring2016.github.io/Group-19/writeup.pdf

## Repository Contents

### Visualization

The visualization is written entirely in JavaScript and D3.
The data is loaded from the file _data.json_.
It does not have to be launched, it can be simply accessed by the link http://nyu-cs6313-spring2016.github.io/Group-19/.

### Data Aquisition

Data aquisition part has been written in Java (see TweetHarvest folder) with the use of _twitter4j_ and _Stanford NLPCore_ libraries.
I recommend using eclipse to compile and run the application (TweetHarvest folder is an eclipse project).
Before executing, make sure to change SQLiteComponent.DEFAULT_DB variable to your SQLite database file path.

To prepare data for vizualization, you need to run main() method of the following classes:
* HistoryScraper: it retrieves tweets from Twitter and stores them in the database.
* Categorizer: it assigns tweets to candidates.
* SentimentClassifier: it classifies the sentiment of the tweets.
* JsonExporter: it exports the database to a JSON file (Note: make sure to change the path to your JSON file).
