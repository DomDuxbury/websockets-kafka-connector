package connector.server;

import com.google.gson.internal.LinkedTreeMap;
import connector.PostgresInterface;
import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.java_websocket.WebSocket;

import java.net.InetSocketAddress;
import java.util.ArrayList;

public class KafkaConnectorServer extends FrameworkServer {

    private KafkaProducer<String, String> producer;
    private PostgresInterface db;

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
                authenticateUser(socket, user, (String) payload);
                break;
            case "mcda/websockets/SCENARIO_REQUEST":
                System.out.println(user);
                if (user.isAuthorised()) {
                    System.out.println("starting scenario");
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
                sendKafkaMessage("COMPARISONS", user.getInfo().getUserId(), map);
                break;
            default:
                break;
        }
    }

    private void sendKafkaMessage(String type, Integer userId, Object payload) {
        Message kafkaMessage = new Message(type, userId, 0, payload);
        ProducerRecord<String, String> record = new ProducerRecord<String, String>(type, kafkaMessage.serialize());
        producer.send(record);
    }

    private void setActiveRoute(User user, String routeId) {
        sendKafkaMessage("activeRoute", user.getInfo().getUserId(), routeId);
    }

    private void startScenario(User user) {
        user.incrementStage();
        String scenario = switch(user.getStage()) {
            case 1 -> user.getInfo().getFirstScenario();
            case 2 -> user.getInfo().getSecondScenario();
            case 3 -> user.getInfo().getThirdScenario();
            default -> "";
        };
        if (!scenario.equals("")) {
            sendKafkaMessage("SCENARIO_REQUEST", user.getInfo().getUserId(), scenario);
        }
    }

    private void authenticateUser(WebSocket socket, User user, String credentials) {
        Message message;
        if (credentials.equals("hardy")) {
            // Authorise the user
            user.authoriseUser();

            // Add the user to the experiment
            user.createExperimentInfo(db);

            // Connect the user so they can receive messages from the backend
            connectUser(socket, user);

            message = new Message("AUTHENTICATION_SUCCESS", user.getConnectionId(), 0, user);
        } else {
            message = new Message("AUTHENTICATION_FAILURE", user.getConnectionId(), 0,"");
        }
        sendFrontendMessage(message, socket, false);
    }

    public KafkaConnectorServer(int port, KafkaProducer<String, String> producer) {
        super(new InetSocketAddress(port));
        this.producer = producer;
        try {
            this.db = new PostgresInterface();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
