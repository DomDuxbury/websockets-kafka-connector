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
    private static PostgresInterface db;

    public static void main(String[] args) throws Exception {

        // Get the current environment and corresponding end points
        String env = getEnv();

        System.out.println("Current environment set to : " + env);

        String kafkaURL = getKafkaURL(env);
        String postgresURL = getPostgresURL(env);

        // Set up database connection
        try {
            db = new PostgresInterface(postgresURL);
        } catch (Exception e) {
            e.printStackTrace();
        }

        int maxConcurrentSessions = 2;
        // Push messages from web sockets onto Kafka
        KafkaProducer<String, String> producer = createProducer(kafkaURL);
        KafkaConnectorServer server = new KafkaConnectorServer(8887, producer, db, maxConcurrentSessions);
        Thread thread = new Thread(server::start);
        thread.start();

        // Take messages from topics and put them onto web sockets
        List<String> topics = Arrays.asList("TRACKS", "ROUTES", "WEIGHTS");
        KafkaConsumer<String, String> consumer = createConsumer(kafkaURL);
        consumer.subscribe(topics);
        while (true) {
            ConsumerRecords<String, String> records = consumer.poll(Duration.ofMillis(100));
            for (ConsumerRecord<String, String> record : records) {
                Message serverMessage = Message.deserialize(record.value(), record.topic());
                server.handleFrontendMessage(db, serverMessage, record.topic());
            }
        }
    }

    public static String getEnv() {
        String env = System.getenv("EXPERIMENT_ENV");
        if (env != null && env.equals("DEV")) {
            return env;
        } else if (env != null && env.equals("PROD")) {
            return "PROD";
        } else {
            System.out.println("EXPERIMENT_ENV unset, defaulting to DEV");
            return "DEV";
        }
    }

    public static String getKafkaURL(String env) {
        String devKafkaURL = "localhost:9092";
        String prodKafkaURL = "35.230.144.141:9092";

        if (env.equals("DEV")) {
            return devKafkaURL;
        } else {
            return prodKafkaURL;
        }
    }

    public static String getPostgresURL(String env) {
        String devPostgresURL = "jdbc:postgresql://localhost:5432/postgres";
        String prodPostgresURL = "jdbc:postgresql://35.246.0.142:5432/postgres";

        if (env.equals("DEV")) {
            return devPostgresURL;
        } else {
            return prodPostgresURL;
        }
    }

    private static KafkaProducer<String, String> createProducer(String url) {
        Properties props = new Properties();
        props.put("bootstrap.servers", url);
        props.put("acks", "all");
        props.put("retries", 0);
        props.put("batch.size", 1);
        props.put("linger.ms", 0);
        props.put("buffer.memory", 33554432);
        props.put("key.serializer", "org.apache.kafka.common.serialization.StringSerializer");
        props.put("value.serializer", "org.apache.kafka.common.serialization.StringSerializer");
        return new KafkaProducer<>(props);
    }

    private static KafkaConsumer<String, String> createConsumer(String url) {
        Properties props = new Properties();
        props.setProperty("bootstrap.servers", url);
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

