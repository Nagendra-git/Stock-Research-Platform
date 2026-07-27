package com.nagendra.platform.utils;

import com.nagendra.platform.dto.client.Candles;
import java.time.LocalDate;
import tools.jackson.core.JsonParser;
import tools.jackson.databind.DeserializationContext;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.deser.std.StdDeserializer;

public class CandlesDeserializer extends StdDeserializer<Candles> {

  public CandlesDeserializer() {
    super(Candles.class);
  }

  @Override
  public Candles deserialize(JsonParser p, DeserializationContext ctxt) {

    JsonNode node = p.readValueAsTree();

    Candles candle = new Candles();

      candle.setDate(
              LocalDate.parse(
                      node.get(0).asString().substring(0, 10)
              )
      );

    candle.setOpen(node.get(1).asDouble());
    candle.setHigh(node.get(2).asDouble());
    candle.setLow(node.get(3).asDouble());
    candle.setClose(node.get(4).asDouble());
    candle.setVolume(node.get(5).asLong());

    return candle;
  }
}
