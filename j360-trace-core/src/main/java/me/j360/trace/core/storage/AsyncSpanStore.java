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


import me.j360.trace.core.DependencyLink;
import me.j360.trace.core.Span;
import me.j360.trace.core.internal.Nullable;

import java.util.List;

/**
 * An interface that is equivalent to {@link SpanStore} but accepts callbacks to allow
 * bridging to async libraries.
 *
 * <p>Note: This is not considered a user-level Api, rather an Spi that can be used to bind
 * user-level abstractions such as futures or observables.
 *
 * @see SpanStore
 */
public interface AsyncSpanStore {

  /**
   * Version of {@link SpanStore#getTraces} that accepts {@link Callback}.
   */
  void getTraces(QueryRequest request, Callback<List<List<Span>>> callback);

  /**
   * Version of {@link SpanStore#getTrace} that accepts {@link Callback}.
   */
  void getTrace(long id, Callback<List<Span>> callback);

  /**
   * Version of {@link SpanStore#getRawTrace} that accepts {@link Callback}.
   */
  void getRawTrace(long traceId, Callback<List<Span>> callback);

  /**
   * Version of {@link SpanStore#getServiceNames} that accepts {@link Callback}.
   */
  void getServiceNames(Callback<List<String>> callback);

  /**
   * Version of {@link SpanStore#getSpanNames} that accepts {@link Callback}.
   */
  void getSpanNames(String serviceName, Callback<List<String>> callback);

  /**
   * Version of {@link SpanStore#getDependencies} that accepts {@link Callback}.
   */
  void getDependencies(long endTs, @Nullable Long lookback, Callback<List<DependencyLink>> callback);
}
