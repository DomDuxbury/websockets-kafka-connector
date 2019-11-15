package connector.server;

import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.java_websocket.WebSocket;
import org.java_websocket.handshake.ClientHandshake;

import java.net.InetSocketAddress;
import java.util.ArrayList;

public class KafkaConnectorServer extends FrameworkServer {

    private KafkaProducer<String, String> producer;
    private ArrayList<WebSocket> sockets = new ArrayList<>();

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
                authenticateUser(socket.getAttachment(), payload);
                break;
            default:
                ProducerRecord<String, String> record = new ProducerRecord<String, String>(topic, payload);
                producer.send(record);
        }
    }

    private void authenticateUser(User user, String credentials) {
        Message message;
        System.out.println(user);
        if (credentials.equals("hardy")) {
            message = new Message("AUTHENTICATION_SUCCESS", user.getId(), "");
            user.authoriseUser();
            System.out.println("Authenticated user");
        } else {
            message = new Message("AUTHENTICATION_FAILURE", user.getId(), "");
        }
        System.out.println(message);
        sendMessage(message);
    }



    @Override
	public void onClose(WebSocket conn, int code, String reason, boolean remote ) {
        System.out.println("Disconnected User: " + conn.getAttachment());
	}

    public KafkaConnectorServer(int port, KafkaProducer<String, String> producer) {
        super(new InetSocketAddress(port));
        this.producer = producer;
    }
}
