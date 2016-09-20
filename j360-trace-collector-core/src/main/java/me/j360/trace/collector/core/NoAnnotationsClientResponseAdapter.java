package me.j360.trace.collector.core;

import java.util.Collection;
import java.util.Collections;


public class NoAnnotationsClientResponseAdapter implements ClientResponseAdapter {

    private final static ClientResponseAdapter INSTANCE = new NoAnnotationsClientResponseAdapter();

    private final static Collection<KeyValueAnnotation> EMPTY = Collections.emptyList();

    private NoAnnotationsClientResponseAdapter() { }


    public static ClientResponseAdapter getInstance() {
        return INSTANCE;
    }

    @Override
    public Collection<KeyValueAnnotation> responseAnnotations() {
        return EMPTY;
    }
}
