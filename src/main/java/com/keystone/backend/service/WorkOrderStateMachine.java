package com.keystone.backend.service;

import java.util.HashSet;
import java.util.Set;

/**
 * State machine for work order status transitions.
 * Defines valid transitions and maintains state change history.
 */
public class WorkOrderStateMachine {

    // Valid work order statuses
    public static final String STATUS_OPEN = "Open";
    public static final String STATUS_IN_PROGRESS = "In Progress";
    public static final String STATUS_DONE = "Done";
    public static final String STATUS_CANCELLED = "Cancelled";

    // Valid transitions: from status -> set of allowed to statuses
    private static final java.util.Map<String, Set<String>> VALID_TRANSITIONS;

    static {
        VALID_TRANSITIONS = new java.util.HashMap<>();
        // Open can transition to: In Progress, Done, Cancelled
        VALID_TRANSITIONS.put(STATUS_OPEN, new HashSet<>(java.util.Set.of(
                STATUS_IN_PROGRESS, STATUS_DONE, STATUS_CANCELLED)));
        // In Progress can transition to: Open, Done
        VALID_TRANSITIONS.put(STATUS_IN_PROGRESS, new HashSet<>(java.util.Set.of(
                STATUS_OPEN, STATUS_DONE)));
        // Done is a terminal state - no outgoing transitions
        VALID_TRANSITIONS.put(STATUS_DONE, new HashSet<>());
        // Cancelled is a terminal state - no outgoing transitions
        VALID_TRANSITIONS.put(STATUS_CANCELLED, new HashSet<>());
    }

    /**
     * Check if a transition from 'fromStatus' to 'toStatus' is valid.
     *
     * @param fromStatus the current status
     * @param toStatus   the target status
     * @return true if the transition is valid, false otherwise
     */
    public static boolean isValidTransition(String fromStatus, String toStatus) {
        if (fromStatus == null || toStatus == null) {
            return false;
        }
        Set<String> allowedTransitions = VALID_TRANSITIONS.get(fromStatus);
        if (allowedTransitions == null) {
            return false;
        }
        return allowedTransitions.contains(toStatus);
    }

    /**
     * Get all valid target statuses for a given current status.
     *
     * @param fromStatus the current status
     * @return set of valid target statuses, or null if status is invalid
     */
    public static java.util.Set<String> getValidTransitions(String fromStatus) {
        if (fromStatus == null) {
            return null;
        }
        return VALID_TRANSITIONS.get(fromStatus);
    }

    /**
     * Get the human-readable display name for a status.
     *
     * @param status the status code
     * @return display name
     */
    public static String getStatusDisplayName(String status) {
        if (status == null) {
            return null;
        }
        switch (status) {
            case STATUS_OPEN:
                return "Open";
            case STATUS_IN_PROGRESS:
                return "In Progress";
            case STATUS_DONE:
                return "Done";
            case STATUS_CANCELLED:
                return "Cancelled";
            default:
                return status;
        }
    }

    /**
     * Check if a status is a valid (known) status.
     *
     * @param status the status to check
     * @return true if the status is valid
     */
    public static boolean isValidStatus(String status) {
        if (status == null) {
            return false;
        }
        return java.util.Set.of(STATUS_OPEN, STATUS_IN_PROGRESS, STATUS_DONE, STATUS_CANCELLED)
                .contains(status);
    }

    /**
     * Get the initial status for a new work order.
     *
     * @return the initial status
     */
    public static String getInitialStatus() {
        return STATUS_OPEN;
    }
}