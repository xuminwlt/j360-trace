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
package me.j360.trace.core.internal;


import me.j360.trace.core.DependencyLink;

import java.util.List;

import static me.j360.trace.core.internal.Util.checkNotNull;


/**
 * Internal as only cassandra serializes the start and end timestamps along with link data, and
 * those serialized timestamps are never read.
 *
 * @deprecated See https://github.com/openzipkin/zipkin/issues/1008
 */
@Deprecated
public final class Dependencies {


  public static Dependencies create(long startTs, long endTs, List<DependencyLink> links) {
    return new Dependencies(startTs, endTs, links);
  }

  /** milliseconds from epoch */
  public final long startTs;

  /** milliseconds from epoch) */
  public final long endTs;

  /** link information for every dependent service */
  public final List<DependencyLink> links;

  Dependencies(long startTs, long endTs, List<DependencyLink> links) {
    this.startTs = startTs;
    this.endTs = endTs;
    this.links = checkNotNull(links, "links");
  }

  @Override
  public boolean equals(Object o) {
    if (o == this) {
      return true;
    }
    if (o instanceof Dependencies) {
      Dependencies that = (Dependencies) o;
      return (this.startTs == that.startTs)
          && (this.endTs == that.endTs)
          && (this.links.equals(that.links));
    }
    return false;
  }

  @Override
  public int hashCode() {
    int h = 1;
    h *= 1000003;
    h ^= (startTs >>> 32) ^ startTs;
    h *= 1000003;
    h ^= (endTs >>> 32) ^ endTs;
    h *= 1000003;
    h ^= links.hashCode();
    return h;
  }

}
