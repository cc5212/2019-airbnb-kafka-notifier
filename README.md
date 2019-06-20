# 2019-airbnb-kafka-notifier
Process airbnb lodgings in real time using Kafka. Pablo Arancibia Nicolás Escudero, Emilio Lizama Group : 1

# Overview
This proyect simulates a notifier for Airbnb using Kafka. To do this, we create 2 topics in Kafka, in one (named airbnb-producer) we are going to produce the status of the lodgings in Airbnb (with their price at that moment, and if it is available for renting or not) simulated in real time (ordered by dates). In the other one (airbnb-client), we are going to filter the values from airbnb-producer, and put the status of lodgings that have change their values of available from false to true. With this topic, the client subscribed, will be notified for new available lodgings, and will filter which ones they want.

# Data

The dataset we get it from the page http://insideairbnb.com/get-the-data.html. From here we used the files from the city Santiago de Chile, the files listings.csv and calendar.csv.gz. listings.csv contains a summary of the information of available lodgings in Santiago (this information is the minimun nights in the lodging, a short description, the neighborhood where it is, the latitude and longitude of the lodging, etc.). calendar.csv.gz contains the status of the lodging daily. This status contains the price in that day (price may vary between one day and another), if its available for renting that day or not, etc. The file calendars.csv.gz contains  5.759.010 rows and its size compressed is 13,7MB and the file listings.csv contains 29.777 rows and its size is 2,1MB. The data from this city was choosed because it felt more familiar to use the data from a city we all lived. Also, the good thing of this project, is that we can use the data from any other city, because all the cities have the same files! In the end, there is a section named "Instructions how to run", where we explain how to use another the data from another city instead. 

# Methods

So for the technologies we used are the following: Pig, Python+Pandas,MongoDB and Kafka. We used Pig to clean and order by date the file calendars.csv.gz. Python with Pandas, we used to make the Json file from the listings.csv so we can load that file to MongoDB. The most important of this its Kafka. With this we simulated the cleaned calendars.csv.gz file, so it produces this in a Kafka topic. This simulation is made with the file [AirbnbSimulator](./src/org/mdp/kafka/cli/AirbnbSimulator.java). In the file [AirbnbFilter](./src/org/mdp/kafka/cli/AirbnbFilter.java)we make a filter for this topic, so it puts in another topic, the lodgings that have change their status from not available to available, so with another file  [AirbnbClient](./src/org/mdp/kafka/cli/AirbnbClient.java) it will suscrbe to this topic and it will be "notified" of a new available logding. The MongoDB is used here so the AirbnbFilter can have the actual status of the lodging and the AirbnbClient can have some information about the lodging. 

# Results

The results you can try it yourself! The instructions on how to run this project are down below. 

# Conclusion
The most difficult part it was the cleaning of the files. We think it was the most tedious and boring part, because there were always a case we miss. 
Also we could have made the cleaning a bit more efficiently if we used Apache Spark instead of Pig, because we know how efficient its pig (its not very good :c) 
In the future we would like to make a visualization of this simulation, utilizing some sort of map like google maps, because we have the information of latitude and longitude of the lodgins. So with this, we can show in a more interactive way the status of the lodgings.

# Instructions how to run

There are two options to run this project. The first option are the file in the [data](./data) folder named calendarFinal.csv and the files that are already uploaded to mongoDB. This database used are from Santiago de Chile. In the second one, you can use the database from the city you want from http://insideairbnb.com/get-the-data.html. The problem is that in this case you have to clean the files yourself and upload the file to mongoDB also.

# OPTION 1

## Build the jar

First we create the jar file for the project.

## Connect to the ssh server
First we connect to the ssh server from the course and upload the jar file and the file calendarFinal.csv to the server.

## Create the 2 Kafka topics

Now we have to create 2 Kafka topics in the server: 
```
/data/hadoop/kafka/2.11-2.0.0/bin/kafka-topics.sh --create --zookeeper localhost:2181 --replication-factor 1 --partitions 1 --topic airbnb

/data/hadoop/kafka/2.11-2.0.0/bin/kafka-topics.sh --create --zookeeper localhost:2181 --replication-factor 1 --partitions 1 --topic airbnb-client
```
The airbnb-producer topic will have the rows of the file calendarFinal.csv.

The airbnb-client topic will have the rows filtered of the file [New Calendar3]. It will have the rows that have changed their availability from false to true.


## Run the Kafka topics


Now we have to run 3 Kafka classes, one producer (AirbnbSimulator), one producer-consumer(Airbnbfilter) and one consumer (AirbnbClient).

```
java -jar /path/to/mdp-lab06.jar AirbnbClient airbnb-client [maximum-price]

java -jar /path/to/mdp-lab06.jar AirbnbFilter airbnb-producer airbnb-client

java -jar /path/to/mdp-lab06.jar AirbnbSimulator /path/to/calendarFinal.csv airbnb-producer [speed]
```
The value [maximum-price] will filter the values from airbnb-client leaving only the values that have their price lower than [maximum-price].

The value [speed], it leaves the speed in which the rows are going to be produced in the airbnb-producer

# OPTION 2

## Download the database

First we have to go to the following url: http://insideairbnb.com/get-the-data.html.
Here we download the following files from the city Santiago, Región Metropolitana de Santiago, Chile (this code may work with other countries, but we haven't test them!).
- calendar.csv.gz : Detailed Calendar Data for listings in Santiago
- listings.csv : SUMMARY information and metrics for listings in Santiago.

## Filter and Order the database

Here, we clean the calendar.csv.gz database. First we erase the first row of the calendar.csv.gz file by using the following command (this line contains the name of the columns of the database, we don't need them):

```
gzip -d calendar.csv.gz 
tail -n +2 calendar.csv > [New Calendar1].csv
gzip [New Calendar1].csv
```
In the folder [pig](./pig) there are two files, order.pig and filter.pig. This two files are pig files used to clean the previous file. 
To use them we upload the pig folder and the [New Calendar1].csv.gz file to the server. Then we upload the [New Calendar1].csv.gz file to the distributed file system (hdfs). Now, we change the file loaded in "filter.pig" so now it has the path to [New Calendar1].csv.gz in the hdfs. Now we use the following command:
```
pig filter.pig
```
Now the result file will be saved in the hdfs (we will call it [New Calendar2]). Now we do the same for "order.pig", we update the file by changing the LOAD file so now it has the path to the file [New Calendar2]. Then we execute the following command:
```
pig order.pig
```
This will give us a new file we call it [New Calendar3]. We move it from the hdfs to the server.

## Upload listings.csv to mongoDB

First we convert the listings.csv into a json file using the file [to_json.py](./filter/to_json.py). 

The new file created is listings.json.

After running this file, we have to go to mongo.

First we create and use a new mongo database:
```
use EPN;
```
Then we create the collection:
```
db.createCollection("listing")
```
And finally we import the listings.csv values:
```
mongoimport --db EPN --collection listings --file  listings.json --jsonArray

```
And we are ready with the mongo!


## Create the 2 Kafka topics

Now we have to create 2 Kafka topics in the server: 
```
/data/hadoop/kafka/2.11-2.0.0/bin/kafka-topics.sh --create --zookeeper localhost:2181 --replication-factor 1 --partitions 1 --topic airbnb

/data/hadoop/kafka/2.11-2.0.0/bin/kafka-topics.sh --create --zookeeper localhost:2181 --replication-factor 1 --partitions 1 --topic airbnb-client
```
The airbnb-producer topic will have the rows of the file [New Calendar3].

The airbnb-client topic will have the rows filtered of the file [New Calendar3]. It will have the rows that have changed their availability from false to true.


## Run the Kafka files

First we create the jar.

Now we have to run 3 Kafka classes, one producer (AirbnbSimulator), one producer-consumer(Airbnbfilter) and one consumer (AirbnbClient).

```
java -jar mdp-lab06.jar AirbnbClient airbnb-client [maximum-price]

java -jar mdp-lab06.jar AirbnbFilter airbnb-producer airbnb-client

java -jar mdp-lab06.jar AirbnbSimulator /path/to/[New Calendar3] airbnb-producer [speed]
```
The value [maximum-price] will filter the values from airbnb-client leaving only the values that have their price lower than [maximum-price].

The value [speed], it leaves the speed in which the rows are going to be produced in the airbnb-producer
