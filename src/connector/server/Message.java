package connector.server;

import com.google.gson.Gson;

public class Message {
    private String type;
    private Object payload;

    Message(String type, Object payload) {
        this.type = type;
        this.payload = payload;
    }

    String serialize() {
        Gson gson = new Gson();
        return gson.toJson(this);
    }

    public static Message deserialize(String serializedMessage) {
        Gson gson = new Gson();
        return gson.fromJson(serializedMessage, Message.class);
    }

    public String getType() {
        return type;
    }

    public Object getPayload() {
        return payload;
    }
}
