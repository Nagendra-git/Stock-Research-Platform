package com.nagendra.platform.models;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Document(collection = "instrument")
public class Instrument {

  @Id private String id;

  @Indexed(unique = true)
  @Field("instrument_key")
  private String instrumentKey;

  @Indexed
  @Field("exchange_token")
  private String exchangeToken;

  @Indexed
  @Field("tradingsymbol")
  private String tradingSymbol;

  @Indexed
  @Field("name")
  private String name;

  @Indexed
  @Field("normalized_name")
  private String normalizedName;

  @Indexed
  @Field("exchange")
  private String exchange;

  @Field("instrument_type")
  private String instrumentType;

  @Indexed
  @Field("isin")
  private String isin;

  @Field("last_price")
  private Double lastPrice;

  @Field("tick_size")
  private Double tickSize;

  @Field("lot_size")
  private Integer lotSize;
}
