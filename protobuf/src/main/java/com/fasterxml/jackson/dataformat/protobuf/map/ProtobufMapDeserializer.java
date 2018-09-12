package com.fasterxml.jackson.dataformat.protobuf.map;

import java.io.IOException;
import java.util.Map;
import java.util.Set;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonToken;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.KeyDeserializer;
import com.fasterxml.jackson.databind.deser.NullValueProvider;
import com.fasterxml.jackson.databind.deser.std.MapDeserializer;
import com.fasterxml.jackson.databind.jsontype.TypeDeserializer;
import com.fasterxml.jackson.dataformat.protobuf.ProtobufParser;
import com.fasterxml.jackson.dataformat.protobuf.schema.FieldType;
import com.fasterxml.jackson.dataformat.protobuf.schema.ProtobufField;

public class ProtobufMapDeserializer extends MapDeserializer {
  public ProtobufMapDeserializer(MapDeserializer deserializer) {
    super(deserializer);
  }

  public ProtobufMapDeserializer(ProtobufMapDeserializer protobufMapDeserializer, KeyDeserializer keyDeser,
      JsonDeserializer<Object> valueDeser, TypeDeserializer valueTypeDeser,
      NullValueProvider nuller, Set<String> ignorable) {
    super(protobufMapDeserializer, keyDeser, valueDeser, valueTypeDeser, nuller, ignorable);
  }

  @SuppressWarnings("unchecked")
  @Override
  protected MapDeserializer withResolved(KeyDeserializer keyDeser, TypeDeserializer valueTypeDeser,
      JsonDeserializer<?> valueDeser, NullValueProvider nuller, Set<String> ignorable) {
    if ((_keyDeserializer == keyDeser) && (_valueDeserializer == valueDeser)
        && (_valueTypeDeserializer == valueTypeDeser) && (_nullProvider == nuller)
        && (_ignorableProperties == ignorable)) {
      return this;
    }
    return new ProtobufMapDeserializer(this,
        keyDeser, (JsonDeserializer<Object>) valueDeser, valueTypeDeser,
        nuller, ignorable);
  }

  @SuppressWarnings("unchecked")
  @Override
  public Map<Object, Object> deserialize(JsonParser p, DeserializationContext ctxt) throws IOException {
    JsonToken t = p.currentToken();
    if (t == JsonToken.START_OBJECT) {
//      return super.deserialize(p,ctxt);
      return idlMessageToJavaMap(p, ctxt);
    }

    return idlMapToJavaMap(p, ctxt);
  }

  private Map<Object, Object> idlMapToJavaMap(JsonParser p, DeserializationContext ctxt) throws IOException {
    ProtobufParser parser = (ProtobufParser) p;
    ProtobufField mapField = parser.getCurrentField();
    ProtobufField keyField = mapField.getMessageType().firstIf("key");
    JsonDeserializer<?> keyDeserializer = ctxt.findNonContextualValueDeserializer(keyField.type.getJavaType());

    final Map<Object, Object> result = (Map<Object, Object>) _valueInstantiator.createUsingDefault(ctxt);

    for (; ; ) {
      JsonToken t = p.nextToken();
      if (t == JsonToken.END_ARRAY) {
        break;
      }

      p.nextFieldName(); // key
      t = p.nextToken(); // key type token
      Object key = keyDeserializer.deserialize(p, ctxt);

      p.nextFieldName(); // value
      t = p.nextToken(); // value type token
      Object value = _valueDeserializer.deserialize(p, ctxt);

      t = p.nextToken();
      if (t != JsonToken.END_OBJECT) {
        ctxt.reportWrongTokenException(this, JsonToken.END_OBJECT, null);
      }
      result.put(key, value);
    }
    return result;
  }

  private Map<Object, Object> idlMessageToJavaMap(JsonParser p, DeserializationContext ctxt) throws IOException {
    ProtobufParser protobufParser = (ProtobufParser) p;
    final Map<Object, Object> result = (Map<Object, Object>) _valueInstantiator.createUsingDefault(ctxt);
    for (;;) {
      JsonToken t = p.nextToken();
      if (t == JsonToken.END_OBJECT) {
        break;
      }

      Object key = _keyDeserializer.deserializeKey(p.currentName(), ctxt);

      t = p.nextToken();
      Object value ;
      ProtobufField protobufField = protobufParser.getCurrentField();
      if (protobufField.type == FieldType.MAP){
        value = idlMapToJavaMap(p, ctxt);
      } else {
        value = _valueDeserializer.deserialize(p, ctxt);
      }

      result.put(key, value);
    }
    return result;
  }
}
