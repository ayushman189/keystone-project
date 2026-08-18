package com.keystone.backend.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.keystone.backend.entity.WorkOrderStatusHistory;

public interface WorkOrderStatusHistoryRepository
        extends JpaRepository<WorkOrderStatusHistory, Long> {
}