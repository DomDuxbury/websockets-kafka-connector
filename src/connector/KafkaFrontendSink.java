package connector;

import connector.server.KafkaConnectorServer;
import org.apache.flink.api.java.tuple.Tuple2;
import org.apache.flink.streaming.api.functions.sink.RichSinkFunction;


public class KafkaFrontendSink extends RichSinkFunction<Tuple2<String, String>> {

    private String topic;
    private KafkaConnectorServer server;

    @Override
    public void invoke(Tuple2<String, String> message, Context context) throws Exception {
        server.sendMessage(message.f0, message.f1);
    }

    public KafkaFrontendSink(String topic, KafkaConnectorServer server) {
        this.topic = topic;
        this.server = server;
    }
}
