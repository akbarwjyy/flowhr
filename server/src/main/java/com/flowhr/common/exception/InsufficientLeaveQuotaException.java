package com.flowhr.common.exception;

public class InsufficientLeaveQuotaException extends RuntimeException {
    public InsufficientLeaveQuotaException() {
        super("Sisa kuota cuti tidak mencukupi");
    }

    public InsufficientLeaveQuotaException(String message) {
        super(message);
    }
}
