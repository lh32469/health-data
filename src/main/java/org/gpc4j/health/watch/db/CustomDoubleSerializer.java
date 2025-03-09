package org.gpc4j.health.watch.db;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;

import java.io.IOException;
import java.text.DecimalFormat;

public class CustomDoubleSerializer extends JsonSerializer<Double> {

  private static final DecimalFormat df = new DecimalFormat("0.0000");

  @Override
  public void serialize(Double value,
                        JsonGenerator generator,
                        SerializerProvider serializerProvider) throws IOException {
//    generator.writeString(df.format(value));
    generator.writeNumber(Double.valueOf(df.format(value)));
  }

}
