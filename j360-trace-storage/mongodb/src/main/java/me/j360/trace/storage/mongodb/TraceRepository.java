package me.j360.trace.storage.mongodb;

import me.j360.trace.core.Span;
import org.springframework.data.mongodb.repository.MongoRepository;

public interface TraceRepository extends MongoRepository<Span, String> {

    public Span findByTraceId(String traceId);

}
