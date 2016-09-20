package me.j360.trace.collector.core.module;


import me.j360.trace.collector.core.internal.DefaultSpanCodec;

import java.util.List;

public interface SpanCodec {

  SpanCodec JSON = DefaultSpanCodec.JSON;

  byte[] writeSpan(Span span);

  byte[] writeSpans(List<Span> spans);

  /** throws {@linkplain IllegalArgumentException} if the span couldn't be decoded */
  Span readSpan(byte[] bytes);
}
