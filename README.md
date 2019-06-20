# 2019-airbnb-kafka-notifier
Process airbnb lodgings in real time using Kafka. Pablo Arancibia Nicolás Escudero, Emilio Lizama Group : 1

# Description
This proyect simulates a notifier for Airbnb using Kafka. To do this, we create 2 topics in Kafka, in one (named airbnb-producer) we are going to produce the status of the lodgings in Airbnb (with their price at that moment, and if it is available for renting or not) simulated in real time (ordered by dates). In the other one (airbnb-client), we are going to filter the values from airbnb-producer, and put the status of lodgings that have change their values of available from false to true. With this topic, the client subscribed, will be notified for new available lodgings, and will filter which ones they want.

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

First we convert the listings.csv into a json file using the file toJson.py. 

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
