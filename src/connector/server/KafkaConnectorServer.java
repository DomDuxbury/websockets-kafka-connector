package connector.server;

import com.google.gson.Gson;
import com.google.gson.internal.LinkedHashTreeMap;
import com.google.gson.internal.LinkedTreeMap;
import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.java_websocket.WebSocket;
import org.java_websocket.handshake.ClientHandshake;

import java.net.InetSocketAddress;
import java.util.ArrayList;

public class KafkaConnectorServer extends FrameworkServer {

    private KafkaProducer<String, String> producer;

    @Override
    public void onMessage(WebSocket webSocket, String s) {
        Message message = Message.deserialize(s);
        String topic = message.getType();
        handleMessage(webSocket, topic, message.getPayload());
    }

    private void handleMessage(WebSocket socket, String topic, Object payload) {
        User user = socket.getAttachment();
        switch (topic) {
            case "mcda/websockets/AUTHENTICATION_REQUEST":
                authenticateUser(user, (String) payload);
                break;
            case "mcda/websockets/SCENARIO_REQUEST":
                if (user.isAuthorised()) {
                    startScenario(user);
                }
                break;
            case "mcda/websockets/SET_ACTIVE_ROUTE":
                if (user.isAuthorised()) {
                    setActiveRoute(user, (String) payload);
                }
                break;
            case "mcda/ScenarioView/UPDATE_COMPARISON":
                LinkedTreeMap<String, ArrayList> map = (LinkedTreeMap<String, ArrayList>) payload;
                Message newComparisonMessage = new Message("COMPARISONS", user.getId(), map);
                ProducerRecord<String, String> newComparisons = new ProducerRecord<String, String>("COMPARISONS", newComparisonMessage.serialize());
                producer.send(newComparisons);
                break;
            default:
                ProducerRecord<String, String> record = new ProducerRecord<String, String>(topic, (String) payload);
                producer.send(record);
        }
    }

    private void setActiveRoute(User user, String routeId) {
        Message activeRouteRequest = new Message("activeRoute", user.getId(), routeId);
        ProducerRecord<String, String> record = new ProducerRecord<String, String>("activeRoute", activeRouteRequest.serialize());
        producer.send(record);
    }

    private void startScenario(User user) {
        Message scenarioRequest = new Message("SCENARIO_REQUEST", user.getId(), "SDKDemo");
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
