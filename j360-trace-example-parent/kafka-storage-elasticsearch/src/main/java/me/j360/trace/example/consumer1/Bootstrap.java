package me.j360.trace.example.consumer1;

import me.j360.trace.core.Span;
import me.j360.trace.core.collector.InMemoryCollectorMetrics;
import me.j360.trace.core.storage.AsyncSpanConsumer;
import me.j360.trace.core.storage.AsyncSpanStore;
import me.j360.trace.core.storage.SpanStore;
import me.j360.trace.core.storage.StorageComponent;
import me.j360.trace.storage.core.kafka.KafkaCollector;
import me.j360.trace.storage.core.kafka.KafkaCollector.Builder;
import me.j360.trace.storage.elasticsearch.ElasticsearchStorage;
import org.elasticsearch.client.Client;

import java.util.List;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Package: me.j360.trace.example.consumer1
 * User: min_xu
 * Date: 2016/9/26 下午4:02
 * 说明：
 */
public class Bootstrap {

    static InMemoryCollectorMetrics metrics = new InMemoryCollectorMetrics();

    public static void main(String[] args){

    }


    public static void initKafkaConsumer(){
        LinkedBlockingQueue<List<Span>> recvdSpans = new LinkedBlockingQueue<>();
        AsyncSpanConsumer consumer ;

        Builder builder = builder("consumer_exception");

        AtomicInteger counter = new AtomicInteger();

        consumer = (spans, callback) -> {
            if (counter.getAndIncrement() == 1) {
                callback.onError(new RuntimeException("storage fell over"));
            } else {
                recvdSpans.add(spans);
                callback.onSuccess(null);
            }
        };

        try (KafkaCollector collector = newKafkaTransport(builder, consumer)) {
            recvdSpans.take();



        }catch (Exception e){

        }

    }


    static Builder builder(String topic) {
        return new Builder().metrics(metrics).zookeeper("127.0.0.1:2181").topic(topic);
    }

    static KafkaCollector newKafkaTransport(Builder builder, AsyncSpanConsumer consumer) {
        return new KafkaCollector(builder.storage(new StorageComponent() {
            @Override public SpanStore spanStore() {
                throw new AssertionError();
            }

            @Override public AsyncSpanStore asyncSpanStore() {
                throw new AssertionError();
            }

            @Override public AsyncSpanConsumer asyncSpanConsumer() {
                return consumer;
            }

            @Override public CheckResult check() {
                return CheckResult.OK;
            }

            @Override public void close() {
                throw new AssertionError();
            }
        })).start();
    }


    public static void consumer(){
        ElasticsearchStorage.Builder builder = ElasticsearchStorage.builder();
        ElasticsearchStorage storage = builder.build();


        Client client = storage.client();
        storage.guavaSpanConsumer();


    }



}
