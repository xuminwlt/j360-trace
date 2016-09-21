package me.j360.trace.collector.core.internal;

import me.j360.trace.collector.core.module.*;
import me.j360.trace.core.Codec;

import java.util.ArrayList;
import java.util.List;

public final class DefaultSpanCodec implements SpanCodec {
  public static final SpanCodec JSON = new DefaultSpanCodec(Codec.JSON);

  private final Codec codec;

  private DefaultSpanCodec(Codec codec) {
    this.codec = codec;
  }

  @Override
  public byte[] writeSpan(Span span) {
    return codec.writeSpan(span.toZipkin());
  }

  @Override
  public byte[] writeSpans(List<Span> spans) {
    List<me.j360.trace.core.Span> out = new ArrayList<me.j360.trace.core.Span>(spans.size());
    for (Span span : spans) {
      out.add(span.toZipkin());
    }
    return codec.writeSpans(out);
  }

  @Override
  public Span readSpan(byte[] bytes) {
    me.j360.trace.core.Span in = codec.readSpan(bytes);
    Span result = new Span();
    result.setTrace_id(in.traceId);
    result.setId(in.id);
    result.setParent_id(in.parentId);
    result.setName(in.name);
    result.setTimestamp(in.timestamp);
    result.setDuration(in.duration);
    result.setDebug(in.debug);
    for (me.j360.trace.core.Annotation a : in.annotations) {
      result.addToAnnotations(Annotation.create(
              a.timestamp,
              a.value,
              to(a.endpoint)));
    }
    for (me.j360.trace.core.BinaryAnnotation a : in.binaryAnnotations) {
      result.addToBinary_annotations(BinaryAnnotation.create(
              a.key,
              a.value,
              AnnotationType.fromValue(a.type.value),
              to(a.endpoint)));
    }
    return result;
  }

  private static Endpoint to(me.j360.trace.core.Endpoint host) {
    if (host == null) return null;
    if (host.port == null) return Endpoint.create(host.serviceName, host.ipv4);
    return Endpoint.create(host.serviceName, host.ipv4, host.port);
  }

}
