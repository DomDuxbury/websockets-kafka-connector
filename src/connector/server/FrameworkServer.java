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
    }

    public void sendMessage(Message message) {
        connections.get(message.getUserId()).send(message.serialize());
    }

    public void sendMessage(WebSocket socket, String type, Object payload) {
		Message message = new Message(type, payload);
		socket.send(message.serialize());
	}

	@Override
	public void onOpen( WebSocket conn, ClientHandshake handshake ) {
		System.out.println( conn.getRemoteSocketAddress().getAddress().getHostAddress() + " entered the room!" );
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
