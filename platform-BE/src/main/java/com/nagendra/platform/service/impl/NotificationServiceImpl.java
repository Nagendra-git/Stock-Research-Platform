package com.nagendra.platform.service.impl;

import com.nagendra.platform.models.Notifications;
import com.nagendra.platform.repository.NotificationRepository;
import com.nagendra.platform.service.NotificationService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class NotificationServiceImpl implements NotificationService {

  private final NotificationRepository notificationRepository;

  @Override
  public void saveNotification(Notifications notifications) {
    notificationRepository.save(notifications);
  }
}
