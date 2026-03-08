package com.revpassword.notficationservice.service;

public interface EmailService {

    void sendEmail(String to,String subject,String message);

}