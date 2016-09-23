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
package me.j360.trace.storage.core.guava;


import me.j360.trace.core.Span;
import me.j360.trace.core.storage.AsyncSpanConsumer;
import me.j360.trace.core.storage.Callback;

import java.util.List;

import static com.google.common.util.concurrent.Futures.addCallback;
import static me.j360.trace.core.internal.Util.checkNotNull;

final class InternalGuavaToAsyncSpanConsumerAdapter implements AsyncSpanConsumer {
  final GuavaSpanConsumer delegate;

  InternalGuavaToAsyncSpanConsumerAdapter(GuavaSpanConsumer delegate) {
    this.delegate = checkNotNull(delegate, "delegate");
  }

  @Override
  public void accept(List<Span> spans, Callback<Void> callback) {
    addCallback(delegate.accept(spans), new InternalForwardingCallback<>(callback));
  }

  @Override public String toString() {
    return delegate.toString();
  }
}
