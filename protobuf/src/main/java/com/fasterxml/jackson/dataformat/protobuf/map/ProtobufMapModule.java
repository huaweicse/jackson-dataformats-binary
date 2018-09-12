package com.fasterxml.jackson.dataformat.protobuf.map;

import com.fasterxml.jackson.databind.BeanDescription;
import com.fasterxml.jackson.databind.DeserializationConfig;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializationConfig;
import com.fasterxml.jackson.databind.deser.BeanDeserializerModifier;
import com.fasterxml.jackson.databind.deser.std.MapDeserializer;
import com.fasterxml.jackson.databind.module.SimpleModule;
import com.fasterxml.jackson.databind.ser.BeanSerializerModifier;
import com.fasterxml.jackson.databind.ser.std.MapSerializer;
import com.fasterxml.jackson.databind.type.MapType;


public class ProtobufMapModule extends SimpleModule {
  public ProtobufMapModule() {
    super("protobufMap");

    setSerializerModifier(new BeanSerializerModifier() {
      @Override
      public JsonSerializer<?> modifyMapSerializer(SerializationConfig config, MapType valueType,
          BeanDescription beanDesc, JsonSerializer<?> serializer) {
        return new ProtobufMapSerializer((MapSerializer) serializer);
      }
    });

    setDeserializerModifier(new BeanDeserializerModifier() {
      @Override
      public JsonDeserializer<?> modifyMapDeserializer(DeserializationConfig config, MapType type,
          BeanDescription beanDesc, JsonDeserializer<?> deserializer) {
        return new ProtobufMapDeserializer((MapDeserializer) deserializer);
      }
    });
  }
}
