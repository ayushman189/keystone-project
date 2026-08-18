package com.keystone.backend.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.keystone.backend.entity.Notification;
import com.keystone.backend.entity.WorkOrder;

public interface NotificationRepository extends JpaRepository<Notification, Long> {

    boolean existsByWorkOrderAndType(WorkOrder workOrder, String type);

    List<Notification> findByWorkOrder(WorkOrder workOrder);

    List<Notification> findByReadFalse();
}
