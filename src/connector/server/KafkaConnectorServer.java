package connector.server;

import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.java_websocket.WebSocket;
import org.java_websocket.handshake.ClientHandshake;

import java.net.InetSocketAddress;

public class KafkaConnectorServer extends FrameworkServer {

    private KafkaProducer<String, String> producer;

    @Override
    public void onMessage(WebSocket webSocket, String s) {
        Message message = Message.deserialize(s);
        String topic = message.getType();
        String payload = (String) message.getPayload();
        handleMessage(webSocket, topic, payload);
    }

    private void handleMessage(WebSocket socket, String topic, String payload) {
        User user = socket.getAttachment();
        switch (topic) {
            case "mcda/websockets/AUTHENTICATION_REQUEST":
                authenticateUser(user, payload);
                break;
            case "mcda/websockets/SCENARIO_REQUEST":
                if (user.isAuthorised()) {
                    startScenario(user);
                }
                break;
            default:
                ProducerRecord<String, String> record = new ProducerRecord<String, String>(topic, payload);
                producer.send(record);
        }
    }

    private void startScenario(User user) {
        Message scenarioRequest = new Message("SCENARIO_REQUEST", user.getId(), "IW18");
        ProducerRecord<String, String> record = new ProducerRecord<String, String>("SCENARIO_REQUESTS", scenarioRequest.serialize());
        producer.send(record);
    }

    private void authenticateUser(User user, String credentials) {
        Message message;
        if (credentials.equals("hardy")) {
            message = new Message("AUTHENTICATION_SUCCESS", user.getId(), "");
            user.authoriseUser();
        } else {
            message = new Message("AUTHENTICATION_FAILURE", user.getId(), "");
        }
        sendMessage(message, false);
    }

    public KafkaConnectorServer(int port, KafkaProducer<String, String> producer) {
        super(new InetSocketAddress(port));
        this.producer = producer;
    }
}
