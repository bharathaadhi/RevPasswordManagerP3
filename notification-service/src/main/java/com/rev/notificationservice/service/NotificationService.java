package com.rev.notificationservice.service;

import com.rev.notificationservice.dto.NotificationRequest;
import com.rev.notificationservice.entity.Notification;
import com.rev.notificationservice.repository.NotificationRepository;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import java.util.List;
import org.springframework.transaction.annotation.Transactional;

@Service
@Slf4j
@RequiredArgsConstructor
public class NotificationService {

    private final JavaMailSender mailSender;
    private final NotificationRepository notificationRepository;
    private final SimpMessagingTemplate messagingTemplate;

    @Transactional
    public void sendAndSaveNotification(NotificationRequest request) {
        String normalizedEmail = request.getRecipientEmail() != null ? request.getRecipientEmail().toLowerCase().trim() : "";
        Notification notification = new Notification();
        notification.setRecipientEmail(normalizedEmail);
        notification.setTitle(request.getTitle());
        notification.setMessage(request.getMessage());
        notification.setType(request.getType());
        notification.setRead(false);
        
        notificationRepository.save(notification);

        // Broadcast via WebSocket
        try {
            messagingTemplate.convertAndSend("/topic/notifications/" + normalizedEmail, notification);
        } catch (Exception e) {
            log.warn("WebSocket broadcast failed for user {}: {}", normalizedEmail, e.getMessage());
        }

        // Optionally try sending email if we wanted to
        try {
            SimpleMailMessage message = new SimpleMailMessage();
            message.setFrom("bharathaadhi32@gmail.com");
            message.setTo(request.getRecipientEmail());
            message.setSubject(request.getTitle());
            message.setText(request.getMessage());
            mailSender.send(message);
        } catch (Exception e) {
            log.warn("Failed to send real email (ignoring for in-app flow) to {}: {}", request.getRecipientEmail(), e.getMessage());
        }
    }

    @Transactional
    public void notifyBreach(String email, String affectedAccount) {
        NotificationRequest request = new NotificationRequest();
        request.setRecipientEmail(email);
        request.setTitle("Security Breach Warning");
        request.setMessage("A potential security breach has been detected affecting your account: " + affectedAccount + ". Please review your passwords.");
        request.setType("BREACH_WARNING");
        sendAndSaveNotification(request);
    }

    public List<Notification> getUserNotifications(String email) {
        String normalizedEmail = email != null ? email.toLowerCase().trim() : "";
        return notificationRepository.findByRecipientEmailOrderByTimestampDesc(normalizedEmail);
    }

    public List<Notification> getUnreadNotifications(String email) {
        String normalizedEmail = email != null ? email.toLowerCase().trim() : "";
        return notificationRepository.findByRecipientEmailAndIsReadFalse(normalizedEmail);
    }

    @Transactional
    public void markAsRead(Long id) {
        notificationRepository.findById(id).ifPresent(notification -> {
            notification.setRead(true);
            notificationRepository.save(notification);
        });
    }
}
