package connector.server;

import com.google.gson.internal.LinkedTreeMap;
import connector.PostgresInterface;
import org.java_websocket.WebSocket;
import org.java_websocket.handshake.ClientHandshake;
import org.java_websocket.server.WebSocketServer;

import java.net.InetSocketAddress;
import java.util.HashMap;

public abstract class FrameworkServer extends WebSocketServer {

	private HashMap<Integer, WebSocket> connectedUsers;

    public FrameworkServer(InetSocketAddress address) {
        super(address);
        connectedUsers = new HashMap<>();
    }

    public void sendFrontendMessage(Message message, boolean requiresAuth) {
        if (connectedUsers.containsKey(message.getUserId())) {
            WebSocket socket = connectedUsers.get(message.getUserId());
            User user = socket.getAttachment();
            if (!requiresAuth || user.isAuthorised()) {
                socket.send(message.serialize());
            }
        }
    }


    public void recordScore(PostgresInterface db, Message message) {
        LinkedTreeMap<String, Double> harbourState = (LinkedTreeMap) message.getPayload();
        int score = (int) Math.round(harbourState.get("score"));
        WebSocket socket = connectedUsers.get(message.getUserId());
        if (socket != null) {
            User user = socket.getAttachment();
            db.recordScore(message.getUserId(), message.getTimeStep(), user.getStage(), score);
        }
    }

    public void sendFrontendMessage(Message message, WebSocket socket, boolean requiresAuth) {
        User user = socket.getAttachment();
        if (!requiresAuth || user.isAuthorised()) {
            socket.send(message.serialize());
        }
    }

	@Override
    public void onOpen(WebSocket conn, ClientHandshake handshake) {
        User newUser = new User();
        conn.setAttachment(newUser);
        System.out.println("Connected User: " + newUser);
    }

    public void connectUser(WebSocket conn, User newUser) {
        connectedUsers.put(newUser.getInfo().getUserId(), conn);
    }

	@Override
	public void onError( WebSocket conn, Exception ex ) {
		ex.printStackTrace();
		if( conn != null ) {
			// some errors like port binding failed may not be assignable to a specific websocket
			conn.close();
		}
	}

	@Override
	public void onClose(WebSocket conn, int code, String reason, boolean remote ) {
        User disconnectingUser = conn.getAttachment();
        System.out.println("Disconnected User: " + disconnectingUser);
        if (disconnectingUser.isAuthorised()) {
            //connectedUsers.remove(disconnectingUser.getInfo().getUserId());
        }
	}

	@Override
	public void onStart() {
		System.out.println("server started!");
		setConnectionLostTimeout(0);
		setConnectionLostTimeout(100);
	}

	@Override
    protected void finalize() {
        try {
            this.stop();
        } catch(Exception e) {
            System.out.println("The server is unstoppable");
        }
    }
}
