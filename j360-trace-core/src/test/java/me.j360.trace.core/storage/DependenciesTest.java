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
package me.j360.trace.core.storage;

import java.util.List;

import me.j360.trace.core.*;
import me.j360.trace.core.internal.ApplyTimestampAndDuration;
import me.j360.trace.core.internal.CallbackCaptor;
import me.j360.trace.core.storage.SpanStore;
import me.j360.trace.core.storage.StorageComponent;
import org.junit.Before;
import org.junit.Test;

import static java.util.Arrays.asList;
import static java.util.stream.Collectors.toList;

/**
 * Base test for {@link SpanStore} implementations that support dependency aggregation. Subtypes
 * should create a connection to a real backend, even if that backend is in-process.
 *
 * <p>This is a replacement for {@code com.twitter.zipkin.storage.DependencyStoreSpec}.
 */
public abstract class DependenciesTest {

  /** Should maintain state between multiple calls within a test. */
  protected abstract StorageComponent storage();

  SpanStore store() {
    return storage().spanStore();
  }

  /** Clears store between tests. */
  @Before
  public abstract void clear();

  /**
   * Override if dependency processing is a separate job: it should complete before returning from
   * this method.
   */
  protected void processDependencies(List<Span> spans) {
    // Blocks until the callback completes to allow read-your-writes consistency during tests.
    CallbackCaptor<Void> captor = new CallbackCaptor<>();
    storage().asyncSpanConsumer().accept(spans, captor);
    captor.get(); // block on result
  }

  /**
   * Normally, the root-span is where trace id == span id and parent id == null. The default is to
   * look back one day from today.
   */


  /** rebases a trace backwards a day with different trace and span id. */
  List<Span> subtractDay(List<Span> trace) {
    return trace.stream()
        .map(s -> s.toBuilder()
            .traceId(s.traceId + 100)
            .parentId(s.parentId != null ? s.parentId + 100 : null)
            .id(s.id + 100)
            .timestamp(s.timestamp != null ? s.timestamp - (TestObjects.DAY * 1000) : null)
            .annotations(s.annotations.stream()
                .map(a -> Annotation.create(a.timestamp - (TestObjects.DAY * 1000), a.value, a.endpoint))
                .collect(toList()))
            .build()
        ).collect(toList());
  }
}
