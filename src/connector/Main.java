package connector;

import connector.server.KafkaConnectorServer;
import org.apache.flink.api.common.serialization.SimpleStringSchema;
import org.apache.flink.api.common.typeinfo.Types;
import org.apache.flink.api.java.tuple.Tuple2;
import org.apache.flink.streaming.api.datastream.DataStream;
import org.apache.flink.streaming.api.environment.StreamExecutionEnvironment;
import org.apache.flink.streaming.connectors.kafka.FlinkKafkaConsumer;
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

    public static void main(String[] args) throws Exception {
        // Push messages from web sockets onto Kafka
        KafkaProducer producer = createProducer();
        KafkaConnectorServer server = new KafkaConnectorServer(8887, producer);
        Thread thread = new Thread(() -> server.start());
        thread.run();


        // Take messages from topics and put them onto web sockets
        List<String> topics = Arrays.asList("TRACKS", "ROUTES");
        KafkaConsumer consumer = createConsumer();
        consumer.subscribe(topics);
        while (true) {
            ConsumerRecords<String, String> records = consumer.poll(Duration.ofMillis(100));
            for (ConsumerRecord<String, String> record : records) {
                server.sendMessage(record.topic(), record.value());
            }
        }
    }

    private static KafkaProducer<String, String> createProducer() {
        Properties props = new Properties();
        props.put("bootstrap.servers", KAFKA_SERVER);
        props.put("acks", "all");
        props.put("retries", 0);
        props.put("batch.size", 16384);
        props.put("linger.ms", 1);
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
        props.setProperty("auto.commit.interval.ms", "1000");
        props.setProperty("key.deserializer", "org.apache.kafka.common.serialization.StringDeserializer");
        props.setProperty("value.deserializer", "org.apache.kafka.common.serialization.StringDeserializer");
        return new KafkaConsumer<>(props);
    }
}

