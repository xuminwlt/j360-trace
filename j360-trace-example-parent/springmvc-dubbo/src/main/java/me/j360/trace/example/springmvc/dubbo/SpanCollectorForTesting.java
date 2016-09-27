package me.j360.trace.example.springmvc.dubbo;


import me.j360.trace.collector.core.SpanCollector;
import me.j360.trace.collector.core.module.Span;

import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

public class SpanCollectorForTesting implements SpanCollector {

    private final static Logger LOGGER = Logger.getLogger(SpanCollectorForTesting.class.getName());

    private final List<Span> spans = new ArrayList<Span>();

    private static SpanCollectorForTesting INSTANCE;

    private SpanCollectorForTesting() {

    }

    public static synchronized SpanCollectorForTesting getInstance() {
        if (INSTANCE == null) {
            INSTANCE = new SpanCollectorForTesting();
        }
        return INSTANCE;
    }

    @Override
    public void collect(final Span span) {
        LOGGER.info(span.toString());
        spans.add(span);
    }

    public List<Span> getCollectedSpans() {
        return spans;
    }

    @Override
    public void addDefaultAnnotation(final String key, final String value) {
        throw new UnsupportedOperationException();
    }

    public void clear() {
        spans.clear();
    }

}
