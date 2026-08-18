-- Rename existing sla_due_date to due_date to preserve existing normal due-date values
ALTER TABLE work_orders RENAME COLUMN sla_due_date TO due_date;

-- Add a separate sla_due_date column specifically for SLA tracking
ALTER TABLE work_orders ADD COLUMN sla_due_date TIMESTAMP;
