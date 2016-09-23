/**
 * Copyright 2015-2016 The OpenZipkin Authors
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the License
 * is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express
 * or implied. See the License for the specific language governing permissions and limitations under
 * the License.
 */
package me.j360.trace.storage.core.kafka;

import kafka.javaapi.producer.Producer;
import kafka.producer.KeyedMessage;
import me.j360.trace.core.Codec;
import me.j360.trace.core.Span;
import me.j360.trace.core.collector.InMemoryCollectorMetrics;
import me.j360.trace.core.storage.AsyncSpanConsumer;
import me.j360.trace.core.storage.AsyncSpanStore;
import me.j360.trace.core.storage.SpanStore;
import me.j360.trace.core.storage.StorageComponent;
import org.I0Itec.zkclient.exception.ZkTimeoutException;
import org.junit.ClassRule;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.rules.Timeout;

import java.util.List;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.atomic.AtomicInteger;

import static org.assertj.core.api.Assertions.assertThat;
import static me.j360.trace.storage.core.kafka.TestObjects.*;

public class KafkaCollectorTest {
  @Rule
  public ExpectedException thrown = ExpectedException.none();
  @ClassRule public static Timeout globalTimeout = Timeout.seconds(20);
  Producer<String, byte[]> producer = KafkaTestGraph.INSTANCE.producer();
  InMemoryCollectorMetrics metrics = new InMemoryCollectorMetrics();
  InMemoryCollectorMetrics kafkaMetrics = metrics.forTransport("kafka");

  LinkedBlockingQueue<List<Span>> recvdSpans = new LinkedBlockingQueue<>();
  AsyncSpanConsumer consumer = (spans, callback) -> {
    recvdSpans.add(spans);
    callback.onSuccess(null);
  };

  @Test
  public void checkPasses() throws Exception {
    try (KafkaCollector collector = newKafkaTransport(builder("check_passes"), consumer)) {
      assertThat(collector.check().ok).isTrue();
    }
  }

  @Test
  public void start_failsOnInvalidZooKeeper() throws Exception {
    thrown.expect(ZkTimeoutException.class);
    thrown.expectMessage("Unable to connect to zookeeper server within timeout: 6000");

    KafkaCollector.Builder builder = builder("fail_invalid_zk").zookeeper("1.1.1.1");

    try (KafkaCollector collector = newKafkaTransport(builder, consumer)) {
    }
  }

  @Test
  public void canSetMaxMessageSize() throws Exception {
    KafkaCollector.Builder builder = builder("max_message").maxMessageSize(1);

    try (KafkaCollector collector = newKafkaTransport(builder, consumer)) {
      assertThat(collector.connector.get().config().fetchMessageMaxBytes())
          .isEqualTo(1);
    }
  }

  /** Ensures legacy encoding works: a single TBinaryProtocol encoded span */
  @Test
  public void messageWithSingleThriftSpan() throws Exception {
    KafkaCollector.Builder builder = builder("single_span");

    byte[] bytes = Codec.JSON.writeSpan(TRACE.get(0));
    producer.send(new KeyedMessage<>(builder.topic, bytes));

    try (KafkaCollector collector = newKafkaTransport(builder, consumer)) {
      assertThat(recvdSpans.take()).containsExactly(TRACE.get(0));
    }

    assertThat(kafkaMetrics.messages()).isEqualTo(1);
    assertThat(kafkaMetrics.bytes()).isEqualTo(bytes.length);
    assertThat(kafkaMetrics.spans()).isEqualTo(1);
  }

  /** Ensures list encoding works: a TBinaryProtocol encoded list of spans */
  @Test
  public void messageWithMultipleSpans_thrift() throws Exception {
    KafkaCollector.Builder builder = builder("multiple_spans_thrift");

    byte[] bytes = Codec.JSON.writeSpans(TRACE);
    producer.send(new KeyedMessage<>(builder.topic, bytes));

    try (KafkaCollector collector = newKafkaTransport(builder, consumer)) {
      assertThat(recvdSpans.take()).containsExactlyElementsOf(TRACE);
    }

    assertThat(kafkaMetrics.messages()).isEqualTo(1);
    assertThat(kafkaMetrics.bytes()).isEqualTo(bytes.length);
    assertThat(kafkaMetrics.spans()).isEqualTo(TestObjects.TRACE.size());
  }

  /** Ensures list encoding works: a json encoded list of spans */
  @Test
  public void messageWithMultipleSpans_json() throws Exception {
    KafkaCollector.Builder builder = builder("multiple_spans_json");

    byte[] bytes = Codec.JSON.writeSpans(TRACE);
    producer.send(new KeyedMessage<>(builder.topic, bytes));

    try (KafkaCollector collector = newKafkaTransport(builder, consumer)) {
      assertThat(recvdSpans.take()).containsExactlyElementsOf(TRACE);
    }

    assertThat(kafkaMetrics.messages()).isEqualTo(1);
    assertThat(kafkaMetrics.bytes()).isEqualTo(bytes.length);
    assertThat(kafkaMetrics.spans()).isEqualTo(TestObjects.TRACE.size());
  }

  /** Ensures malformed spans don't hang the collector */
  @Test
  public void skipsMalformedData() throws Exception {
    KafkaCollector.Builder builder = builder("decoder_exception");

    producer.send(new KeyedMessage<>(builder.topic, Codec.JSON.writeSpans(TRACE)));
    producer.send(new KeyedMessage<>(builder.topic, new byte[0]));
    producer.send(new KeyedMessage<>(builder.topic, "[\"='".getBytes())); // screwed up json
    producer.send(new KeyedMessage<>(builder.topic, "malformed".getBytes()));
    producer.send(new KeyedMessage<>(builder.topic, Codec.JSON.writeSpans(TRACE)));

    try (KafkaCollector collector = newKafkaTransport(builder, consumer)) {
      assertThat(recvdSpans.take()).containsExactlyElementsOf(TRACE);
      // the only way we could read this, is if the malformed spans were skipped.
      assertThat(recvdSpans.take()).containsExactlyElementsOf(TRACE);
    }

    assertThat(kafkaMetrics.messagesDropped()).isEqualTo(3);
  }

  /** Guards against errors that leak from storage, such as InvalidQueryException */
  @Test
  public void skipsOnConsumerException() throws Exception {
    KafkaCollector.Builder builder = builder("consumer_exception");

    AtomicInteger counter = new AtomicInteger();

    consumer = (spans, callback) -> {
      if (counter.getAndIncrement() == 1) {
        callback.onError(new RuntimeException("storage fell over"));
      } else {
        recvdSpans.add(spans);
        callback.onSuccess(null);
      }
    };

    producer.send(new KeyedMessage<>(builder.topic, Codec.JSON.writeSpans(TRACE)));
    producer.send(new KeyedMessage<>(builder.topic, Codec.JSON.writeSpans(TRACE))); // tossed on error
    producer.send(new KeyedMessage<>(builder.topic, Codec.JSON.writeSpans(TRACE)));

    try (KafkaCollector collector = newKafkaTransport(builder, consumer)) {
      assertThat(recvdSpans.take()).containsExactlyElementsOf(TRACE);
      // the only way we could read this, is if the malformed span was skipped.
      assertThat(recvdSpans.take()).containsExactlyElementsOf(TRACE);
    }

    assertThat(kafkaMetrics.spansDropped()).isEqualTo(TestObjects.TRACE.size());
  }

  KafkaCollector.Builder builder(String topic) {
    return new KafkaCollector.Builder().metrics(metrics).zookeeper("127.0.0.1:2181").topic(topic);
  }

  KafkaCollector newKafkaTransport(KafkaCollector.Builder builder, AsyncSpanConsumer consumer) {
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
}
