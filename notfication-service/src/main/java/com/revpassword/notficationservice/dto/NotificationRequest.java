package com.revpassword.notficationservice.dto;

import lombok.Data;

@Data
public class NotificationRequest {

    private String email;
    private String subject;
    private String message;

}