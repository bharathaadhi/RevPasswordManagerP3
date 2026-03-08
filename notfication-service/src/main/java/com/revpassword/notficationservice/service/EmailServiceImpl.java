package com.revpassword.notficationservice.service;

import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

@Service
public class EmailServiceImpl implements EmailService {

    private final JavaMailSender mailSender;

    public EmailServiceImpl(JavaMailSender mailSender){
        this.mailSender = mailSender;
    }

    @Override
    public void sendEmail(String to,String subject,String message){

        SimpleMailMessage mail = new SimpleMailMessage();

        mail.setTo(to);
        mail.setSubject(subject);
        mail.setText(message);

        mailSender.send(mail);

    }
}