package com.fu.hotel.dto.response;

public class ApiResponse {

    private boolean success;
    private String message;
    private String actionTaken; // "DELETED" or "DEACTIVATED"

    public ApiResponse() {
    }

    public ApiResponse(boolean success, String message) {
        this.success = success;
        this.message = message;
    }

    public ApiResponse(boolean success, String message, String actionTaken) {
        this.success = success;
        this.message = message;
        this.actionTaken = actionTaken;
    }

    public boolean isSuccess() {
        return success;
    }

    public void setSuccess(boolean success) {
        this.success = success;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public String getActionTaken() {
        return actionTaken;
    }

    public void setActionTaken(String actionTaken) {
        this.actionTaken = actionTaken;
    }
}
