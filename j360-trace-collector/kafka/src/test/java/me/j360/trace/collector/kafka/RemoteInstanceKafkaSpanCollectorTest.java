package me.j360.trace.collector.kafka;

import com.github.charithe.kafka.KafkaJunitRule;
import kafka.serializer.DefaultDecoder;
import me.j360.trace.collector.core.SpanCollectorMetricsHandler;
import me.j360.trace.collector.core.module.Span;
import me.j360.trace.core.Codec;
import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.Producer;
import org.junit.After;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import java.util.List;
import java.util.Properties;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;

import static org.assertj.core.api.Assertions.assertThat;

public class RemoteInstanceKafkaSpanCollectorTest {

  TestMetricsHander metrics = new TestMetricsHander();
  // set flush interval to 0 so that tests can drive flushing explicitly
  KafkaSpanCollector.Config config = KafkaSpanCollector.Config.builder("172.16.10.201:9092").flushInterval(0).build();

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
