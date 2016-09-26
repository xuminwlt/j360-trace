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
package me.j360.trace.storage.elasticsearch;


import me.j360.trace.core.DependencyLink;
import me.j360.trace.core.Span;
import me.j360.trace.core.internal.MergeById;
import me.j360.trace.core.storage.InMemorySpanStore;
import me.j360.trace.core.storage.InMemoryStorage;
import me.j360.trace.core.storage.StorageComponent;
import me.j360.trace.storage.core.TestObjects;
import me.j360.trace.storage.elasticsearch.storage.DependenciesTest;

import java.util.List;

import static me.j360.trace.core.internal.Util.midnightUTC;


public class ElasticsearchDependenciesTest extends DependenciesTest {

  private final ElasticsearchStorage storage;

  public ElasticsearchDependenciesTest() {
    this.storage = ElasticsearchTestGraph.INSTANCE.storage.get();
  }

  @Override protected StorageComponent storage() {
    return storage;
  }

  @Override public void clear() {
    storage.clear();
  }

  /**
   * The current implementation does not include dependency aggregation. It includes retrieval of
   * pre-aggregated links.
   *
   * <p>This uses {@link InMemorySpanStore} to prepare links and {@link
   * ElasticsearchStorage#writeDependencyLinks(List, long)}} to store them.
   */
  @Override
  public void processDependencies(List<Span> spans) {
    InMemoryStorage mem = new InMemoryStorage();
    mem.spanConsumer().accept(spans);
    List<DependencyLink> links = mem.spanStore().getDependencies(TestObjects.TODAY + TestObjects.DAY, null);

    // This gets or derives a timestamp from the spans
    long midnight = midnightUTC(MergeById.apply(spans).get(0).timestamp / 1000);
    storage.writeDependencyLinks(links, midnight);
  }
}
