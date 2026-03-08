package com.revpassword.notficationservice.controller;

import com.revpassword.notficationservice.service.EmailService;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/notifications")
public class NotificationController {

    private final EmailService emailService;

    public NotificationController(EmailService emailService){
        this.emailService = emailService;
    }

    @PostMapping("/send")
    public String sendNotification(@RequestBody Map<String,String> request){

        String email = request.get("email");
        String subject = request.get("subject");
        String message = request.get("message");

        emailService.sendEmail(email,subject,message);

        return "Email sent successfully";
    }
}