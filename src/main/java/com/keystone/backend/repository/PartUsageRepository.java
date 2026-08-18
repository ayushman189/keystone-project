package com.keystone.backend.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.keystone.backend.entity.PartUsage;
import com.keystone.backend.entity.WorkOrder;

public interface PartUsageRepository extends JpaRepository<PartUsage, Long> {

    List<PartUsage> findByWorkOrder(WorkOrder workOrder);
}