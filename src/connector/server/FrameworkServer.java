package connector.server;

import org.java_websocket.WebSocket;
import org.java_websocket.handshake.ClientHandshake;
import org.java_websocket.server.WebSocketServer;

import java.net.InetSocketAddress;
import java.util.HashMap;

public abstract class FrameworkServer extends WebSocketServer {

	private HashMap<Integer, WebSocket> connections;

    public FrameworkServer(InetSocketAddress address) {
        super(address);
        connections = new HashMap<>();
    }

    public void sendMessage(Message message) {
        connections.get(message.getUserId()).send(message.serialize());
    }

	@Override
    public void onOpen(WebSocket conn, ClientHandshake handshake) {
        User newUser = new User();
        conn.setAttachment(newUser);
        connections.put(newUser.getId(), conn);
        System.out.println("Connected User: " + newUser);
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
