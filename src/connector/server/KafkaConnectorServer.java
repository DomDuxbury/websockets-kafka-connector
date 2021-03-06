package connector.server;

import com.google.gson.internal.LinkedTreeMap;
import connector.PostgresInterface;
import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.java_websocket.WebSocket;

import java.net.InetSocketAddress;

public class KafkaConnectorServer extends FrameworkServer {

    private final KafkaProducer<String, String> producer;
    private final PostgresInterface db;

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
                LinkedTreeMap<String, String> credentials = (LinkedTreeMap<String, String>) payload;
                authenticateUser(socket, user, credentials);
                break;
            case "mcda/websockets/START_SESSION":
                if (user.isAuthorised() && !user.sessionStarted()) {
                    System.out.println("Starting session");
                    user.setSessionStarted();
                    Session session = startSession(user);
                    startScenario(session);
                }
                break;
            case "mcda/websockets/SET_ACTIVE_ROUTE":
                if (user.isAuthorised()) {
                    LinkedTreeMap<String, String> activeRoute = (LinkedTreeMap<String, String>) payload;
                    user.recordActivatedRoute(activeRoute.get("type"));
                    setActiveRoute(user, activeRoute.get("sol"));
                }
                break;
            case "mcda/ScenarioView/UPDATE_COMPARISON":
                sendKafkaMessage("COMPARISONS", user.getInfo().getUserId(), payload);
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

    public void handleFrontendMessage(PostgresInterface db, Message message, String topic) {
        if (message.getUserId() != null) {
            sendFrontendMessage(message, true);
        }

        if (topic.equals("TRACKS")) {
            Session currentSession = getUserSession(message.getUserId());

            if (currentSession != null) {

                // If its the final time step record scores
                if (message.isFinalTimeStep()) {
                    currentSession.recordScore(message, db);
                    if (!currentSession.isFinalStage()) {
                        // If its not the final stage then start a scenario
                        startScenario(currentSession);
                    } else {
                        // Else (final of both) then close the session
                        closeSession(message.getUserId());
                    }
                }
            }
        }
    }

    public void startScenario(Session session) {
        session.nextStage();
        User user = session.getSessionUser();
        String scenario = switch(session.getStage()) {
            case 1 -> user.getInfo().getFirstScenario();
            case 2 -> user.getInfo().getSecondScenario();
            case 3 -> user.getInfo().getThirdScenario();
            default -> "";
        };
        if (!scenario.equals("")) {
            sendKafkaMessage("SCENARIO_REQUEST", user.getInfo().getUserId(), scenario);
        }
    }

    private void authenticateUser(WebSocket socket, User user, LinkedTreeMap<String, String> credentials) {
        Message message;
        String password = credentials.get("password");
        String workerID = credentials.get("workerID");

        if (password.equals("experimentUser")) {
            // Authorise the user
            user.authoriseUser();

            // Add the user to the experiment
            user.createExperimentInfo(db, workerID);

            // Connect the user so they can receive messages from the backend
            connectUser(socket, user);

            message = new Message("AUTHENTICATION_SUCCESS", user.getConnectionId(), 0, user);
        } else {
            message = new Message("AUTHENTICATION_FAILURE", user.getConnectionId(), 0,"");
        }
        sendFrontendMessage(message, socket, false);
    }

    public KafkaConnectorServer(int port, KafkaProducer<String, String> producer, PostgresInterface db, int maxConcurrentSessions) {
        super(new InetSocketAddress(port), maxConcurrentSessions);
        this.producer = producer;
        this.db = db;
    }
}
