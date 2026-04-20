package io.spring.userservice.api.exception;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.ser.std.StdSerializer;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ErrorResourceSerializer extends StdSerializer<ErrorResource> {
  public ErrorResourceSerializer() {
    super(ErrorResource.class);
  }

  @Override
  public void serialize(ErrorResource value, JsonGenerator gen, SerializerProvider provider)
      throws IOException {
    Map<String, List<String>> json = new HashMap<>();
    gen.writeStartObject();
    gen.writeObjectFieldStart("errors");
    for (FieldErrorResource fieldErrorResource : value.getFieldErrors()) {
      if (!json.containsKey(fieldErrorResource.getField())) {
        json.put(fieldErrorResource.getField(), new ArrayList<>());
      }
      json.get(fieldErrorResource.getField()).add(fieldErrorResource.getMessage());
    }
    for (Map.Entry<String, List<String>> pair : json.entrySet()) {
      gen.writeArrayFieldStart(pair.getKey());
      pair.getValue().forEach(v -> writeString(gen, v));
      gen.writeEndArray();
    }
    gen.writeEndObject();
    gen.writeEndObject();
  }

  private void writeString(JsonGenerator gen, String value) {
    try {
      gen.writeString(value);
    } catch (IOException e) {
      throw new RuntimeException(e);
    }
  }
}
