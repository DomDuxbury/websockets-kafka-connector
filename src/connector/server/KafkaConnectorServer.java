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
    private Integer latestID;

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
        String messageToBackend = new Message(topic, webSocket.getAttachment(), payload).serialize();
        ProducerRecord<String, String> record = new ProducerRecord<String, String>(topic, messageToBackend);
        producer.send(record);
    }

    @Override
    public void onOpen(WebSocket conn, ClientHandshake handshake) {
        conn.setAttachment(latestID);
        System.out.println("Connected User: " + latestID);
        latestID++;
    }

    @Override
	public void onClose(WebSocket conn, int code, String reason, boolean remote ) {
        System.out.println("Disconnected User: " + conn.getAttachment());
	}

    public KafkaConnectorServer(int port, KafkaProducer<String, String> producer) {
        super(new InetSocketAddress(port));
        this.producer = producer;
        this.latestID = 0;
    }
}
