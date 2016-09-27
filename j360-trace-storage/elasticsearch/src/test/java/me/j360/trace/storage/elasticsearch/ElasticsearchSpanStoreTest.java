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
import me.j360.trace.core.internal.Util;
import me.j360.trace.core.storage.QueryRequest;
import me.j360.trace.core.storage.SpanStoreTest;
import me.j360.trace.core.storage.StorageComponent;
import org.junit.Test;

import java.util.List;

public class ElasticsearchSpanStoreTest extends SpanStoreTest {

  private final ElasticsearchStorage storage;

  public ElasticsearchSpanStoreTest() {
    this.storage = ElasticsearchTestGraph.INSTANCE.storage.get();
  }

  @Override protected StorageComponent storage() {
    return storage;
  }

  @Override public void clear() {

    //storage.clear();
  }

  @Test
  public void spanStore(){
    /*List<String> names = storage.spanStore().getServiceNames();
    for(String name:names){
      System.out.println(name);
    }

    List<String> spanNames = storage.spanStore().getSpanNames("j360servletinterceptorintegration");
    for(String name:spanNames){
      System.out.println(name);
    }*/

    QueryRequest queryRequest = QueryRequest.builder()
            .serviceName("j360servletinterceptorintegration")
            .spanName("")
            .parseAnnotationQuery("")
            .minDuration(1l)
            .maxDuration(3600000L)
            .endTs(1574947278711000L)
            .lookback(3600000l)
            .limit(10).build();

    List<List<Span>> spanList = storage.spanStore().getTraces(queryRequest);

    for(List<Span> span:spanList){
      System.out.println(span.size());
    }
  }

  @Test
  public void traceIdtest(){
    List<Span> list = storage.spanStore().getTrace(Util.lowerHexToUnsignedLong("7f88ce35d4be0917"));
    for(Span span:list){
      System.out.println(span.duration);
    }
  }

  @Test
  public void dependtest(){
    List<DependencyLink> list = storage.spanStore().getDependencies(1574947278711000L,360000L);
    for(DependencyLink span:list){
      System.out.println(span.parent);
    }
  }
}
