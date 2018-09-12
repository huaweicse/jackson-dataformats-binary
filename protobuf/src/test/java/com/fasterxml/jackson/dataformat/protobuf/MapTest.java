/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.fasterxml.jackson.dataformat.protobuf;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

import org.junit.Assert;
import org.junit.Test;

import com.fasterxml.jackson.dataformat.protobuf.ProtobufTestBase.Point;
import com.fasterxml.jackson.dataformat.protobuf.schema.ProtobufSchema;

public class MapTest {
  static class MapMessage {
    public Map<String, String> ssMap;

    public Map<String, Point> spMap;

    public Map<Integer, String> isMap;

    public Map<Integer, Point> ipMap;

    public Map<Long, String> lsMap;

    public Map<Long, Point> lpMap;

    @Override
    public boolean equals(Object o) {
      if (this == o) {
        return true;
      }
      if (o == null || getClass() != o.getClass()) {
        return false;
      }
      MapMessage that = (MapMessage) o;
      return Objects.equals(ssMap, that.ssMap) &&
          Objects.equals(spMap, that.spMap) &&
          Objects.equals(isMap, that.isMap) &&
          Objects.equals(ipMap, that.ipMap) &&
          Objects.equals(lsMap, that.lsMap) &&
          Objects.equals(lpMap, that.lpMap);
    }

    @Override
    public int hashCode() {

      return Objects.hash(ssMap, spMap, isMap, ipMap, lsMap, lpMap);
    }
  }

  static final ProtobufMapper MAPPER = new ProtobufMapper();

  @Test
  public void javaMapToIdlMap() throws IOException {
    MapMessage msg = new MapMessage();

    msg.ssMap = new HashMap<>();
    msg.ssMap.put("k1", "v1");
    msg.ssMap.put("k2", "v2");

    msg.spMap = new HashMap<>();
    msg.spMap.put("p1", new Point(1, 1));
    msg.spMap.put("p2", new Point(2, 2));

    msg.isMap = new HashMap<>();
    msg.isMap.put(1, "i1");
    msg.isMap.put(2, "i2");

    msg.ipMap = new HashMap<>();
    msg.ipMap.put(1, new Point(1, 1));
    msg.ipMap.put(2, new Point(2, 2));

    msg.lsMap = new HashMap<>();
    msg.lsMap.put(1L, "L1");
    msg.lsMap.put(2L, "L2");

    msg.lpMap = new HashMap<>();
    msg.lpMap.put(1L, new Point(1, 1));
    msg.lpMap.put(2L, new Point(2, 2));

    // generate schema
    ProtobufSchema schema = MAPPER.generateSchemaFor(MapMessage.class);

    byte[] msgBytes = MAPPER.writer(schema).writeValueAsBytes(msg);
    MapMessage newMsg = MAPPER.reader(schema.withRootType("MapMessage")).forType(MapMessage.class)
        .readValue(msgBytes);
    Map<String, Object> msgAsMap = MAPPER.reader(schema.withRootType("MapMessage")).forType(Map.class)
        .readValue(msgBytes);
    byte[] msgAsMapBytes = MAPPER.writer(schema).writeValueAsBytes(msgAsMap);

    Assert.assertArrayEquals(msgBytes, msgAsMapBytes);
    Assert.assertEquals(msg, newMsg);
  }
}
