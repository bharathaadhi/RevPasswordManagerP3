package com.rev.notificationservice.service;

import com.rev.notificationservice.dto.NotificationRequest;
import com.rev.notificationservice.entity.Notification;
import com.rev.notificationservice.repository.NotificationRepository;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import java.util.List;

@Service
@Slf4j
@RequiredArgsConstructor
public class NotificationService {

    private final JavaMailSender mailSender;
    private final NotificationRepository notificationRepository;

    public void sendAndSaveNotification(NotificationRequest request) {
        log.info("Saving in-app notification for {}: {}", request.getRecipientEmail(), request.getMessage());
        
        Notification notification = new Notification();
        notification.setRecipientEmail(request.getRecipientEmail());
        notification.setTitle(request.getTitle());
        notification.setMessage(request.getMessage());
        notification.setType(request.getType());
        notification.setRead(false);
        
        notificationRepository.save(notification);

        // Optionally try sending email if we wanted to
        try {
            SimpleMailMessage message = new SimpleMailMessage();
            message.setFrom("revpasswordmanager@gmail.com");
            message.setTo(request.getRecipientEmail());
            message.setSubject(request.getTitle());
            message.setText(request.getMessage());
            mailSender.send(message);
        } catch (Exception e) {
            log.warn("Failed to send real email (ignoring for in-app flow) to {}: {}", request.getRecipientEmail(), e.getMessage());
        }
    }

    public List<Notification> getUserNotifications(String email) {
        return notificationRepository.findByRecipientEmailOrderByTimestampDesc(email);
    }

    public List<Notification> getUnreadNotifications(String email) {
        return notificationRepository.findByRecipientEmailAndIsReadFalse(email);
    }

    public void markAsRead(Long id) {
        notificationRepository.findById(id).ifPresent(notification -> {
            notification.setRead(true);
            notificationRepository.save(notification);
        });
    }
}
