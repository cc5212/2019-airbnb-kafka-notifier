# 2019-airbnb-kafka-notifier
Process airbnb lodgings in real time using Kafka. Pablo Arancibia Nicolás Escudero, Emilio Lizama Group : 1

# Description
owo

# Instructions how to run

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
java -jar mdp-lab06.jar AirbnbClient airbnb-client [minimum-money]

java -jar mdp-lab06.jar AirbnbFilter airbnb-producer airbnb-client

java -jar mdp-lab06.jar AirbnbSimulator /path/to/[New Calendar3] airbnb-producer [speed]
```
