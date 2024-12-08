package com.geko.Booking.Entity.Mongo;


public class LogMapper {
    public static AuditLog createLog(String username, Action action) {
        AuditLog auditLog = new AuditLog();
        auditLog.setUsername(username);
        auditLog.setAction(action);
        return auditLog;
    }

    public static AuditLog createLog(String username, Action action, String listingId) {
        AuditLog auditLog = new AuditLog();
        auditLog.setUsername(username);
        auditLog.setAction(action);
        auditLog.setListingId(listingId);
        return auditLog;
    }
}
