package connector.server;

import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.java_websocket.WebSocket;

import java.net.InetSocketAddress;

public class KafkaConnectorServer extends FrameworkServer {
    private KafkaProducer<String, String> producer;

    @Override
    public void onMessage(WebSocket webSocket, String s) {
        Message message = Message.deserialize(s);
        String topic = message.getType();
        String payload = (String) message.getPayload();
        ProducerRecord<String, String> record = new ProducerRecord<String, String>(topic, payload);
        producer.send(record);
    }

    public KafkaConnectorServer(int port, KafkaProducer<String, String> producer) {
        super(new InetSocketAddress(port));
        this.producer = producer;
    }
}
