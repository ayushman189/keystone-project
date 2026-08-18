package com.keystone.backend.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.keystone.backend.entity.TimeLog;
import com.keystone.backend.entity.WorkOrder;

public interface TimeLogRepository extends JpaRepository<TimeLog, Long> {

    List<TimeLog> findByWorkOrder(WorkOrder workOrder);
}