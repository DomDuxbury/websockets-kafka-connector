package connector.server;

import org.java_websocket.WebSocket;
import org.java_websocket.handshake.ClientHandshake;
import org.java_websocket.server.WebSocketServer;

import java.net.InetSocketAddress;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.stream.Collectors;

public abstract class FrameworkServer extends WebSocketServer {

	private HashMap<Integer, WebSocket> connectedUsers;
    private HashMap<Integer, Session> sessions;
    private int maxConcurrentSessions;

    public FrameworkServer(InetSocketAddress address, int maxConcurrentSessions) {
        super(address);
        this.maxConcurrentSessions = maxConcurrentSessions;
        this.sessions = new HashMap<>();
        connectedUsers = new HashMap<>();
    }

    public void sendFrontendMessage(Message message, boolean requiresAuth) {
        if (connectedUsers.containsKey(message.getUserId())) {
            WebSocket socket = connectedUsers.get(message.getUserId());
            User user = socket.getAttachment();
            if (!requiresAuth || user.isAuthorised() && socket.isOpen()) {
                socket.send(message.serialize());
            }
        }
    }

    protected Session getUserSession(Integer userId) {
        return sessions.get(userId);
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

    protected boolean isSessionSpaceAvailable() {
        return this.sessions.size() < maxConcurrentSessions;
    }

    protected int getTimeUntilSessionSpot() {
        List<Integer> timeRemainingForEachSession = this.sessions.values()
                     .stream()
                     .map(Session::getSessionTimeRemaining)
                     .collect(Collectors.toList());
        return Collections.min(timeRemainingForEachSession);
    }

    protected Session startSession(User user) {
        Session newSession = new Session(user);
        sessions.put(user.getInfo().getUserId(), newSession);
        return newSession;
    }

    protected void closeSession(Integer userId) {
        sessions.remove(userId);
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
            if (disconnectingUser.getInfo() != null) {
                closeSession(disconnectingUser.getInfo().getUserId());
            }
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
