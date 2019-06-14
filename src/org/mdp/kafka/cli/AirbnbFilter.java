package org.mdp.kafka.cli;

//Consumidor-Productor 1

import java.io.FileNotFoundException;
import java.io.IOException;
import java.time.Duration;
import java.util.Arrays;
import java.util.Properties;
import java.util.UUID;


import com.mongodb.MongoClient;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.Producer;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.bson.BSON;
import org.bson.Document;
import org.bson.conversions.Bson;
import org.mdp.kafka.def.KafkaConstants;

import javax.print.Doc;

import static com.mongodb.client.model.Filters.eq;


public class AirbnbFilter {
	public static final MongoClient mango = new MongoClient("127.0.0.1", 27017);
	public static final MongoDatabase mangoDB = mango.getDatabase("EPN");
	public static final MongoCollection mangoCollection = mangoDB.getCollection("listing");
	public static final int WARNING_WINDOW_SIZE = 50000; // create warning for this window
	public static final int CRITICAL_WINDOW_SIZE = 25000; // create critical message for this window
	
	
	public static void main(String[] args) throws FileNotFoundException, IOException{
		if(args.length==0 || args.length>3 || (args.length==3 && !args[2].equals("replay"))){
			System.err.println("Usage [inputTopic] [outputTopic] [replay]");
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

		/*
		Agregamos el Kafka Producer
		*/
		String outputTopic = args[1];
		Producer<String, String> producer = new KafkaProducer<String, String>(KafkaConstants.PROPS);

		try{
			while (true) {
				// every ten milliseconds get all records in a batch
				ConsumerRecords<String, String> records = consumer.poll(Duration.ofMillis(10));
				
				// for all records in the batch
				for (ConsumerRecord<String, String> record : records) {
					String[] array = record.value().split(",");

					//System.out.println(array[0]);
					
					
					String query = "{\"id\": "+ array[0]+ " }"; //array[0] = id del listing
					String query2 = "{\"available\" : 1, \"_id\":0}";
					Document docQuery = Document.parse(query);
					Document docQuery2 = Document.parse(query2);

					Document result = (Document) mangoCollection.find(docQuery).projection(docQuery2).first();
					String resultString = result.get("available").toString();
					//System.out.println(resultString);

					String u = "uwu";
					if (array[2].equals("f")){
						u = "false";
					}
					else if (array[2].equals("t")){
						u="true";
					}
					if (!u.equals(resultString)) {
						String update ="{$set: {\"available\":"+ u +" } }";
						Document docUpdate = Document.parse(update);
						mangoCollection.updateOne(docQuery, docUpdate);
						if (u.equals("true")){
							producer.send(new ProducerRecord<String,String>(outputTopic, record.partition(), record.timestamp(), record.key(), record.value()));
						}
					}

				}
			}
		} finally{
			consumer.close();
		}
	}
}
