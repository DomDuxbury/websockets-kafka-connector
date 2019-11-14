package connector.server;

import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.java_websocket.WebSocket;
import org.java_websocket.handshake.ClientHandshake;

import java.net.InetSocketAddress;
import java.util.ArrayList;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class KafkaConnectorServer extends FrameworkServer {
    private KafkaProducer<String, String> producer;
    private Map<String, String> latestMessages;

    @Override
    public void onMessage(WebSocket webSocket, String s) {
        Message message = Message.deserialize(s);
        String topic = message.getType();
        String payload = (String) message.getPayload();
        handleMessage(webSocket, topic, payload);
    }

    private void handleMessage(WebSocket socket, String topic, String payload) {
        switch (topic) {
            case "mcda/websockets/AUTHENTICATION_REQUEST":
                authenticateUser(socket, payload);
                break;
            default:
                ProducerRecord<String, String> record = new ProducerRecord<String, String>(topic, payload);
                producer.send(record);
        }
    }

    private void authenticateUser(WebSocket websocket, String credentials) {
        if (credentials.equals("hardy")) {
            sendMessage(websocket, "AUTHENTICATION_SUCCESS", "");
            websocket.setAttachment(true);
            System.out.println("Authenticated user");
        } else {
            sendMessage(websocket, "AUTHENTICATION_FAILURE", "");
        }
    }

    @Override
    public void onOpen(WebSocket conn, ClientHandshake handshake) {
        for (String topic: latestMessages.keySet()) {
            sendMessage(topic, latestMessages.get(topic));
        }
    }

    public KafkaConnectorServer(int port, KafkaProducer<String, String> producer, ConcurrentHashMap<String, String> latestMessages) {
        super(new InetSocketAddress(port));
        this.producer = producer;
        this.latestMessages = latestMessages;
    }
}
