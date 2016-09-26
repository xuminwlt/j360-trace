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
package me.j360.trace.example.consumer1;

import me.j360.trace.core.Codec;
import me.j360.trace.core.collector.Collector;
import me.j360.trace.core.collector.CollectorMetrics;
import me.j360.trace.core.collector.CollectorSampler;
import me.j360.trace.core.internal.Nullable;
import me.j360.trace.core.storage.Callback;
import me.j360.trace.core.storage.StorageComponent;
import me.j360.trace.storage.core.kafka.KafkaCollector;
import me.j360.trace.storage.elasticsearch.ElasticsearchStorage;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.ResponseEntity;
import org.springframework.web.context.request.async.DeferredResult;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.zip.GZIPInputStream;

@Configuration
public class ZipkinServerConfiguration {


  @Bean
  CollectorSampler traceIdSampler(@Value("${zipkin.collector.sample-rate:1.0}") float rate) {
    return CollectorSampler.create(rate);
  }



  @Configuration
  // "matchIfMissing = true" ensures this is used when there's no configured storage type

  static class InMemoryConfiguration {
    @Bean
    StorageComponent storage() {
      return ElasticsearchStorage.builder().build();
    }
  }

  final CollectorMetrics metrics =  new ActuateCollectorMetrics(null);
  final Collector collector = Collector.builder(KafkaCollector.class).storage(ElasticsearchStorage.builder().build()).build();

  static final ResponseEntity<?> SUCCESS = ResponseEntity.accepted().build();

  DeferredResult<ResponseEntity<?>> validateAndStoreSpans(String encoding, Codec codec,
                                                          byte[] body) {
    DeferredResult<ResponseEntity<?>> result = new DeferredResult<>();
    metrics.incrementMessages();
    if (encoding != null && encoding.contains("gzip")) {
      try {
        body = gunzip(body);
      } catch (IOException e) {
        metrics.incrementMessagesDropped();
        result.setResult(
                ResponseEntity.badRequest().body("Cannot gunzip spans: " + e.getMessage() + "\n"));
        return result;
      }
    }
    collector.acceptSpans(body, codec, new Callback<Void>() {
      @Override public void onSuccess(@Nullable Void value) {
        result.setResult(SUCCESS);
      }

      @Override public void onError(Throwable t) {
        String message = t.getMessage();
        result.setErrorResult(message.startsWith("Cannot store")
                ? ResponseEntity.status(500).body(message + "\n")
                : ResponseEntity.status(400).body(message + "\n"));
      }
    });
    return result;
  }


  private static final ThreadLocal<byte[]> GZIP_BUFFER = new ThreadLocal<byte[]>() {
    @Override protected byte[] initialValue() {
      return new byte[1024];
    }
  };

  static byte[] gunzip(byte[] input) throws IOException {
    GZIPInputStream in = new GZIPInputStream(new ByteArrayInputStream(input));
    try (ByteArrayOutputStream outputStream = new ByteArrayOutputStream(input.length)) {
      byte[] buf = GZIP_BUFFER.get();
      int len;
      while ((len = in.read(buf)) > 0) {
        outputStream.write(buf, 0, len);
      }
      return outputStream.toByteArray();
    }
  }

}
