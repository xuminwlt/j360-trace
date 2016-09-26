package me.j360.trace.collector.local;


import me.j360.trace.collector.core.SpanCollectorMetricsHandler;
import me.j360.trace.collector.core.module.Span;
import me.j360.trace.core.Component;
import me.j360.trace.core.storage.*;

import java.util.concurrent.atomic.AtomicInteger;


public class LocalSpanCollectorTest {
  public final InMemoryStorage storage = new InMemoryStorage();

  TestMetricsHander metrics = new TestMetricsHander();
  // set flush interval to 0 so that tests can drive flushing explicitly
  LocalSpanCollector.Config config = LocalSpanCollector.Config.builder().flushInterval(0).build();


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

  LocalSpanCollector newLocalSpanCollector(AsyncSpanConsumer consumer) {
    return new LocalSpanCollector(new StorageComponent() {
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
        return Component.CheckResult.OK;
      }

      @Override public void close() {
        throw new AssertionError();
      }
    }, config, metrics);
  }
}
