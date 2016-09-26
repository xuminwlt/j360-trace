package me.j360.trace.collection.core.zipkin;

import me.j360.trace.collector.core.module.Endpoint;
import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class EndpointTest {

  @Test
  public void testServiceNameLowercase() {
    Endpoint ep = Endpoint.create("ServiceName", 1, 1);
    assertEquals("servicename", ep.service_name);
  }
}
