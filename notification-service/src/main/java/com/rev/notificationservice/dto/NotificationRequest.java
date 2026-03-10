package com.rev.notificationservice.dto;

import lombok.Data;

@Data
public class NotificationRequest {
    private String recipientEmail;
    private String title;
    private String message;
    private String type;
}
