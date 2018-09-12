package com.fasterxml.jackson.dataformat.protobuf.map;

import java.io.IOException;
import java.util.Map;
import java.util.Set;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.BeanProperty;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.ser.std.MapSerializer;
import com.fasterxml.jackson.dataformat.protobuf.ProtobufGenerator;
import com.fasterxml.jackson.dataformat.protobuf.schema.FieldType;
import com.fasterxml.jackson.dataformat.protobuf.schema.ProtobufField;

public class ProtobufMapSerializer extends MapSerializer {
  public ProtobufMapSerializer(MapSerializer src) {
    super(src, null, false);
  }

  public ProtobufMapSerializer(ProtobufMapSerializer protobufMapSerializer, BeanProperty property,
      JsonSerializer<?> keySerializer,
      JsonSerializer<?> valueSerializer, Set<String> ignored) {
    super(protobufMapSerializer, property, keySerializer, valueSerializer, ignored);
  }

  public ProtobufMapSerializer(ProtobufMapSerializer ser, Object filterId, boolean sortKeys) {
    super(ser, filterId, sortKeys);
  }

  @Override
  public MapSerializer withResolved(BeanProperty property, JsonSerializer<?> keySerializer,
      JsonSerializer<?> valueSerializer, Set<String> ignored, boolean sortKeys) {
    ProtobufMapSerializer ser = new ProtobufMapSerializer(this, property, keySerializer, valueSerializer, ignored);
    if (sortKeys != ser._sortKeys) {
      ser = new ProtobufMapSerializer(ser, _filterId, sortKeys);
    }
    return ser;
  }

  @Override
  public void serialize(Map<?, ?> value, JsonGenerator gen, SerializerProvider provider) throws IOException {
    ProtobufGenerator protobufGenerator = (ProtobufGenerator) gen;
    ProtobufField protobufField = protobufGenerator.getCurrField();
    if (protobufField != null && protobufField.type == FieldType.MAP) {
      // java map serialize to IDL map
      toIdlMap(value, protobufGenerator, provider);
      return;
    }

    // java map serialize to IDL message
    super.serialize(value, gen, provider);
  }

  private void toIdlMap(Map<?,?> value, ProtobufGenerator gen, SerializerProvider provider) throws IOException {
    gen.writeStartArray();
    if (!value.isEmpty()) {
      if (_sortKeys || provider.isEnabled(SerializationFeature.ORDER_MAP_ENTRIES_BY_KEYS)) {
        value = _orderEntries(value, gen, provider);
      }

      toIdlMapFields(value, gen, provider);
    }
    gen.writeEndArray();
  }

  public void toIdlMapFields(Map<?,?> value, ProtobufGenerator gen, SerializerProvider provider)
      throws IOException
  {
    ProtobufField mapField = gen.getCurrField();
    ProtobufField keyField = mapField.getMessageType().firstIf("key");
    ProtobufField valueField = mapField.getMessageType().firstIf("value");

    final JsonSerializer<Object> keySerializer = provider.findValueSerializer(keyField.type.getJavaType());
    final Set<String> ignored = _ignoredEntries;
    Object keyElem = null;

    try {
      for (Map.Entry<?,?> entry : value.entrySet()) {
        Object valueElem = entry.getValue();
        // First, serialize key
        keyElem = entry.getKey();
        if (keyElem == null || valueElem == null) {
          continue;
        }
        if (ignored != null && ignored.contains(keyElem)) {
          continue;
        }

        gen.writeStartObject();

        gen.setCurrentField(keyField);
        keySerializer.serialize(keyElem, gen, provider);

        gen.setCurrentField(valueField);
        JsonSerializer<Object> serializer = _valueSerializer;
        if (serializer == null) {
          serializer = _findSerializer(provider, valueElem);
        }
        serializer.serialize(valueElem, gen, provider);

        gen.writeEndObject();
      }
    } catch (Exception e) { // Add reference information
      wrapAndThrow(provider, e, value, String.valueOf(keyElem));
    }
  }

  private final JsonSerializer<Object> _findSerializer(SerializerProvider provider,
      Object value) throws JsonMappingException
  {
    final Class<?> cc = value.getClass();
    JsonSerializer<Object> valueSer = _dynamicValueSerializers.serializerFor(cc);
    if (valueSer != null) {
      return valueSer;
    }
    if (_valueType.hasGenericTypes()) {
      return _findAndAddDynamic(_dynamicValueSerializers,
          provider.constructSpecializedType(_valueType, cc), provider);
    }
    return _findAndAddDynamic(_dynamicValueSerializers, cc, provider);
  }
}
