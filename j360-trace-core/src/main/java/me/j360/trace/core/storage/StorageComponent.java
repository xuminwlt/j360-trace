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


import me.j360.trace.core.Component;

/**
 * A component that provides storage interfaces used for spans and aggregations. Implementations are
 * free to provide other interfaces, but the ones declared here must be supported.
 *
 * @see InMemoryStorage
 */
public interface StorageComponent extends Component {

  SpanStore spanStore();

  AsyncSpanStore asyncSpanStore();

  AsyncSpanConsumer asyncSpanConsumer();
}
