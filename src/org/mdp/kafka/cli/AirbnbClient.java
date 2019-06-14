package org.mdp.kafka.cli;

import com.mongodb.MongoClient;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.bson.Document;
import org.mdp.kafka.def.KafkaConstants;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.time.Duration;
import java.util.Arrays;
import java.util.Properties;
import java.util.UUID;

public class AirbnbClient {
    public static final MongoClient mango = new MongoClient("127.0.0.1", 27017);
    public static final MongoDatabase mangoDB = mango.getDatabase("EPN");
    public static final MongoCollection mangoCollection = mangoDB.getCollection("listing");
    public static double maximumPrice;
    public static final int WARNING_WINDOW_SIZE = 50000; // create warning for this window
    public static final int CRITICAL_WINDOW_SIZE = 25000; // create critical message for this window

    public static void main(String[] args) throws FileNotFoundException, IOException{
        if(args.length==0 || args.length>2 ){
            System.err.println("Usage [inputTopic] [maximumPrice]");
            return;
        }

        Properties props = KafkaConstants.PROPS;
        if(args.length==2){ // if we should replay stream from the start
            // randomise consumer ID for kafka doesn't track where it is
            props.put(ConsumerConfig.GROUP_ID_CONFIG, UUID.randomUUID().toString());
            // tell kafka to replay stream for new consumers
            props.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "earliest");
        }

        KafkaConsumer<String, String> consumer = new KafkaConsumer<String, String>(props);
        consumer.subscribe(Arrays.asList(args[0]));
        maximumPrice = Double.parseDouble(args[1]);

        try{
            while (true) {
                // every ten milliseconds get all records in a batch
                ConsumerRecords<String, String> records = consumer.poll(Duration.ofMillis(10));

                // for all records in the batch
                for (ConsumerRecord<String, String> record : records) {
                    String[] array = record.value().split(",");
                    Double listingPrice = Double.parseDouble(array[3]);
                    if (listingPrice < maximumPrice){
                        String query = "{\"id\": "+ array[0]+ " }"; //array[0] = id del listing
                        Document docQuery = Document.parse(query);
                        Document result = (Document) mangoCollection.find(docQuery).first();
                        System.out.println("Short Description: " + result.get("name"));
                        System.out.println("Neighbourhood: " + result.get("neighbourhood"));
                        System.out.println("Minimum nights: " + result.get("minimum_nights"));
                        System.out.println("Room Type: " + result.get("room_type"));
                        System.out.println("Host Name: " + result.get("host_name"));
                        System.out.println("Price: "+ array[3]);
                        System.out.println("Date: " + array[1] + "\n");
                        //System.out.println("Available: " + array[2] + "\n");
                    }
                }
            }
        } finally{
            consumer.close();
        }
    }
}