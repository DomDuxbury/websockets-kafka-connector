package connector;

import connector.server.KafkaConnectorServer;
import connector.server.Message;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.apache.kafka.clients.producer.KafkaProducer;

import java.time.Duration;
import java.util.Arrays;
import java.util.List;
import java.util.Properties;

public class Main {
    private static final String KAFKA_SERVER = "localhost:9092";
    private static final String PROD_KAFKA_SERVER = "35.197.214.134:9092";
    private static final String SERVER_ADDRESS = KAFKA_SERVER;


    private static PostgresInterface db;


    public static void main(String[] args) throws Exception {
        // Set up database connection
        try {
            db = new PostgresInterface();
        } catch (Exception e) {
            e.printStackTrace();
        }

        int maxConcurrentSessions = 2;
        // Push messages from web sockets onto Kafka
        KafkaProducer<String, String> producer = createProducer();
        KafkaConnectorServer server = new KafkaConnectorServer(8887, producer, maxConcurrentSessions);
        Thread thread = new Thread(server::start);
        thread.start();

        // Take messages from topics and put them onto web sockets
        List<String> topics = Arrays.asList("TRACKS", "ROUTES", "WEIGHTS");
        KafkaConsumer<String, String> consumer = createConsumer();
        consumer.subscribe(topics);
        while (true) {
            ConsumerRecords<String, String> records = consumer.poll(Duration.ofMillis(100));
            for (ConsumerRecord<String, String> record : records) {
                Message serverMessage = Message.deserialize(record.value(), record.topic());
                server.handleFrontendMessage(db, serverMessage, record.topic());
            }
        }
    }


    private static KafkaProducer<String, String> createProducer() {
        Properties props = new Properties();
        props.put("bootstrap.servers", SERVER_ADDRESS);
        props.put("acks", "all");
        props.put("retries", 0);
        props.put("batch.size", 1);
        props.put("linger.ms", 0);
        props.put("buffer.memory", 33554432);
        props.put("key.serializer", "org.apache.kafka.common.serialization.StringSerializer");
        props.put("value.serializer", "org.apache.kafka.common.serialization.StringSerializer");
        return new KafkaProducer<>(props);
    }

    private static KafkaConsumer<String, String> createConsumer() {
        Properties props = new Properties();
        props.setProperty("bootstrap.servers", SERVER_ADDRESS);
        props.setProperty("group.id", "frontend-consumer");
        props.setProperty("enable.auto.commit", "true");
        props.setProperty("max.poll.records", "1");
        props.setProperty("auto.commit.interval.ms", "100");
        props.setProperty("auto.offset.reset", "latest");
        props.setProperty("key.deserializer", "org.apache.kafka.common.serialization.StringDeserializer");
        props.setProperty("value.deserializer", "org.apache.kafka.common.serialization.StringDeserializer");
        KafkaConsumer<String, String> consumer = new KafkaConsumer<>(props);
        consumer.seekToEnd(consumer.assignment());
        return consumer;
    }
}

