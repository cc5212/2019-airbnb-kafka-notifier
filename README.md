# 2019-airbnb-kafka-notifier
Process airbnb lodgings in real time using Kafka. Pablo Arancibia Nicolás Escudero, Emilio Lizama Group : 1

# Instructions how to run

# Download the database

First we have to go to the following url: http://insideairbnb.com/get-the-data.html.
Here we download the following files from the city Santiago, Región Metropolitana de Santiago, Chile (this code may work with other countries, but we haven't test them!).
- calendar.csv.gz : Detailed Calendar Data for listings in Santiago
- listings.csv : SUMMARY information and metrics for listings in Santiago.

# Filter and Order the database

Here, we clean the calendar.csv.gz database. First we erase the first row of the calendar.csv.gz file by using the following command (this line contains the name of the columns of the database, we don't need them):

gzip -d calendar.csv.gz 
tail -n +2 calendar.csv > [New Calendar1].csv
gzip [New Calendar1].csv

In the folder "pig" there are two files, order.pig and filter.pig. This two files are pig files used to clean the previous file. 
To use them we upload the pig folder and the [New Calendar1].csv.gz file to the server. Then we upload the [New Calendar1].csv.gz file to the distributed file system (hdfs). Now, we change the file loaded in "filter.pig" so now it has the path to [New Calendar1].csv.gz in the hdfs. Now we use the following command:

pig filter.pig

Now the result file will be saved in the hdfs (we will call it [New Calendar2]). Now we do the same for "order.pig", we upload the files to 
# Upload listings.csv to mongoDB


# Create the 3 Kafka topics

# Run the Kafka files
