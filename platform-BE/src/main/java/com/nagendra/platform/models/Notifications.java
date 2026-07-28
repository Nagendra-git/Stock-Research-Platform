package com.nagendra.platform.models;

import lombok.Data;
import lombok.EqualsAndHashCode;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

@Data
@Document("notifications")
@EqualsAndHashCode(callSuper = false)
public class Notifications extends Audit {
  @Id private String id;

  private String message;
}
