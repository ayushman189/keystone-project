package com.keystone.backend.repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.keystone.backend.entity.WorkOrder;

@Repository
public interface WorkOrderRepository extends JpaRepository<WorkOrder, Long> {

    @Query("SELECT wo FROM WorkOrder wo WHERE (:status IS NULL OR wo.status = :status) AND (:priority IS NULL OR wo.priority = :priority) AND (:customerId IS NULL OR wo.customer.id = :customerId) AND (:siteId IS NULL OR wo.site.id = :siteId) AND (:assigneeId IS NULL OR wo.assignee.id = :assigneeId) AND (:search IS NULL OR wo.code LIKE %:search% OR wo.title LIKE %:search%)")
    List<WorkOrder> findByFilter(@Param("status") String status, @Param("priority") String priority, @Param("customerId") Long customerId, @Param("siteId") Long siteId, @Param("assigneeId") Long assigneeId, @Param("search") String search);

    @Query("SELECT wo FROM WorkOrder wo WHERE wo.slaDueDate < :now AND wo.status NOT IN :statuses")
    List<WorkOrder> findBySlaDueDateBeforeAndStatusNotIn(@Param("now") LocalDateTime now,
            @Param("statuses") List<String> statuses);

    @Query("SELECT wo FROM WorkOrder wo WHERE wo.dueDate < :now AND wo.status NOT IN :statuses")
    List<WorkOrder> findByDueDateBeforeAndStatusNotIn(@Param("now") LocalDateTime now,
            @Param("statuses") List<String> statuses);

    @Query("SELECT COUNT(wo) FROM WorkOrder wo WHERE wo.status = :status")
    long countByStatus(@Param("status") String status);

    @Query("SELECT COUNT(wo) FROM WorkOrder wo WHERE wo.slaDueDate IS NOT NULL")
    long countWithSla();

    @Query("SELECT COUNT(wo) FROM WorkOrder wo WHERE wo.slaDueDate IS NOT NULL AND (wo.slaDueDate >= :now OR wo.status IN :statuses)")
    long countSlaCompliant(@Param("now") LocalDateTime now, @Param("statuses") List<String> statuses);

    @Query("SELECT COUNT(wo) FROM WorkOrder wo WHERE wo.slaDueDate IS NOT NULL AND wo.slaDueDate < :now AND wo.status NOT IN :statuses")
    long countSlaBreached(@Param("now") LocalDateTime now, @Param("statuses") List<String> statuses);

    @Query("SELECT wo.status, COUNT(wo) FROM WorkOrder wo GROUP BY wo.status")
    List<Object[]> countByStatusGrouped();

    @Query("SELECT u.name, COUNT(wo) FROM WorkOrder wo JOIN wo.assignee u WHERE u.role = 'TECHNICIAN' GROUP BY u.name ORDER BY u.name")
    List<Object[]> countByTechnicianGrouped();

    @Query("SELECT s.name, COUNT(wo) FROM WorkOrder wo JOIN wo.site s GROUP BY s.name ORDER BY s.name")
    List<Object[]> countBySiteGrouped();
}