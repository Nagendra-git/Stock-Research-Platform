package com.nagendra.platform.listener;

import com.nagendra.platform.models.Audit;
import java.time.LocalDateTime;
import org.springframework.data.mongodb.core.mapping.event.AbstractMongoEventListener;
import org.springframework.data.mongodb.core.mapping.event.BeforeConvertEvent;
import org.springframework.stereotype.Component;

@Component
public class AuditingEventListener extends AbstractMongoEventListener<Audit> {

  @Override
  public void onBeforeConvert(BeforeConvertEvent<Audit> event) {
    Audit audit = event.getSource();
    String currentUser = getCurrentUsername();

    if (audit.getCreatedAt() == null) {
      audit.setCreatedAt(LocalDateTime.now());
      audit.setCreatedBy(currentUser);
      audit.setActive(true);
    }
    audit.setUpdatedAt(LocalDateTime.now());
    audit.setUpdatedBy(currentUser);
  }

  private String getCurrentUsername() {
    return "system";
  }
}
