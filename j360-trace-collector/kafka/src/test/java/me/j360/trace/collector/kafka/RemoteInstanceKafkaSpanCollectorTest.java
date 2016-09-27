package me.j360.trace.collector.kafka;

import me.j360.trace.collector.core.SpanCollectorMetricsHandler;
import me.j360.trace.collector.core.module.Span;
import org.junit.After;
import org.junit.Test;

import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;

public class RemoteInstanceKafkaSpanCollectorTest {

  TestMetricsHander metrics = new TestMetricsHander();
  // set flush interval to 0 so that tests can drive flushing explicitly
  KafkaSpanCollector.Config config = KafkaSpanCollector.Config.builder("192.168.31.164:9092").flushInterval(0).build();

  KafkaSpanCollector collector = new KafkaSpanCollector(config, metrics);

  @After
  public void closeCollector(){
    collector.close();
  }


  @Test
  public void submitsSpansToCorrectTopic() throws Exception {
    collector.collect(span(1L, "foo"));
    collector.collect(span(2L, "bar"));

    collector.flush(); // manually flush the spans
  }

  @Test
  public void submitMultipleSpansInParallel() throws Exception {
    Callable<Void> spanProducer1 = new Callable<Void>() {

      @Override
      public Void call() throws Exception {
        for (int i = 1; i <= 200; i++) {
          collector.collect(span(i, "producer1_" + i));
        }
        return null;
      }
    };

    Callable<Void> spanProducer2 = new Callable<Void>() {

      @Override
      public Void call() throws Exception {
        for (int i = 1; i <= 200; i++) {
          collector.collect(span(i, "producer2_" + i));
        }
        return null;
      }
    };

    ExecutorService executorService = Executors.newFixedThreadPool(2);
    Future<Void> future1 = executorService.submit(spanProducer1);
    Future<Void> future2 = executorService.submit(spanProducer2);

    future1.get(2000, TimeUnit.MILLISECONDS);
    future2.get(2000, TimeUnit.MILLISECONDS);

    collector.flush(); // manually flush the spans
  }

  class TestMetricsHander implements SpanCollectorMetricsHandler {

    final AtomicInteger acceptedSpans = new AtomicInteger();
    final AtomicInteger droppedSpans = new AtomicInteger();

    @Override
    public void incrementAcceptedSpans(int quantity) {
      acceptedSpans.addAndGet(quantity);
    }

    @Override
    public void incrementDroppedSpans(int quantity) {
      droppedSpans.addAndGet(quantity);
    }
  }

  static Span span(long traceId, String spanName) {
    return new Span().setTrace_id(traceId).setId(traceId).setName(spanName);
  }

  static me.j360.trace.core.Span zipkinSpan(long traceId, String spanName) {
    return me.j360.trace.core.Span.builder().traceId(traceId).id(traceId).name(spanName).build();
  }

  /*private List<byte[]> readMessages(String topic) throws TimeoutException {
    return kafka.readMessages(topic, 1, new DefaultDecoder(kafka.consumerConfig().props()));
  }
  private List<byte[]> readMessages() throws TimeoutException {
    return readMessages("zipkin");
  }*/
}
