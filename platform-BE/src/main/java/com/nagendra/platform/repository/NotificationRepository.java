package com.nagendra.platform.repository;

import com.nagendra.platform.models.Notifications;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface NotificationRepository extends MongoRepository<Notifications, String> {}
