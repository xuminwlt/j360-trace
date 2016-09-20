package me.j360.trace.collector.core.internal;

import me.j360.trace.collector.core.module.Span;
import me.j360.trace.collector.core.module.SpanCodec;

import java.util.ArrayList;
import java.util.List;

public final class DefaultSpanCodec implements SpanCodec {

  private final Codec codec;

  private DefaultSpanCodec(Codec codec) {
    this.codec = codec;
  }

  @Override
  public byte[] writeSpan(Span span) {
    return new byte[0];
  }

  @Override
  public byte[] writeSpans(List<Span> spans) {
    return new byte[0];
  }

  @Override
  public Span readSpan(byte[] bytes) {
    return null;
  }
}
