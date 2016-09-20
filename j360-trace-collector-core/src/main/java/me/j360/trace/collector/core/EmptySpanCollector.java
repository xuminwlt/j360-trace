package me.j360.trace.collector.core;


import me.j360.trace.collector.core.module.Span;

/**
 * A {@link SpanCollector} implementation that does nothing with collected spans.
 * 
 * @author adriaens
 */
public class EmptySpanCollector implements SpanCollector {

    /**
     * {@inheritDoc}
     */
    @Override
    public void collect(final Span span) {
        // Nothing

    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void addDefaultAnnotation(final String key, final String value) {
        // Nothing

    }
}
