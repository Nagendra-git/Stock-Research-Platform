package com.nagendra.platform.mapper;

import com.nagendra.platform.dto.client.InstrumentData;
import com.nagendra.platform.models.Company;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Named;

@Mapper(componentModel = "spring")
public interface CompanyMapper {

  @Named("extractIsin")
  static String extractIsin(String underlyingKey) {
    if (underlyingKey == null || !underlyingKey.contains("|")) {
      return null;
    }
    return underlyingKey.split("\\|")[1];
  }

  @Mapping(target = "companyName", source = "name")
  @Mapping(target = "symbol", source = "tradingSymbol")
  @Mapping(target = "isin", source = "isin")
  Company toCompany(InstrumentData instrumentData);
}
