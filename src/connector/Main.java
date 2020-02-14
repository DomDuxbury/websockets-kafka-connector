package connector;

import com.google.gson.internal.LinkedTreeMap;
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
    private static PostgresInterface db;


    public static void main(String[] args) throws Exception {
        // Set up database connection
        try {
            db = new PostgresInterface();
        } catch (Exception e) {
            e.printStackTrace();
        }

        // Push messages from web sockets onto Kafka
        KafkaProducer producer = createProducer();
        KafkaConnectorServer server = new KafkaConnectorServer(8887, producer);
        Thread thread = new Thread(() -> server.start());
        thread.run();

        // Take messages from topics and put them onto web sockets
        List<String> topics = Arrays.asList("TRACKS", "ROUTES", "WEIGHTS");
        KafkaConsumer<String, String> consumer = createConsumer();
        consumer.subscribe(topics);
        while (true) {
            ConsumerRecords<String, String> records = consumer.poll(Duration.ofMillis(100));
            for (ConsumerRecord<String, String> record : records) {
                Message serverMessage = Message.deserialize(record.value(), record.topic());
                if (serverMessage.getUserId() != null) {
                    server.sendFrontendMessage(serverMessage, true);
                }
                if (record.topic().equals("TRACKS") && (serverMessage.getTimeStep() + 1) == 300) {
                    System.out.println("Recording score!");
                    server.recordScore(db, serverMessage);
                }
            }
        }
    }


    private static KafkaProducer<String, String> createProducer() {
        Properties props = new Properties();
        props.put("bootstrap.servers", KAFKA_SERVER);
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
        props.setProperty("bootstrap.servers", KAFKA_SERVER);
        props.setProperty("group.id", "test");
        props.setProperty("enable.auto.commit", "true");
        props.setProperty("max.poll.records", "1");
        props.setProperty("auto.commit.interval.ms", "100");
        props.setProperty("auto.offset.reset", "latest");
        props.setProperty("key.deserializer", "org.apache.kafka.common.serialization.StringDeserializer");
        props.setProperty("value.deserializer", "org.apache.kafka.common.serialization.StringDeserializer");
        KafkaConsumer<String, String> consumer = new KafkaConsumer<>(props);
        //consumer.seekToEnd(consumer.assignment());
        return consumer;
    }
}

