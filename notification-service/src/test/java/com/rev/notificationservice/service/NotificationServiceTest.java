package com.rev.notificationservice.service;

import com.rev.notificationservice.dto.NotificationRequest;
import com.rev.notificationservice.entity.Notification;
import com.rev.notificationservice.repository.NotificationRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class NotificationServiceTest {

    @Mock
    private JavaMailSender mailSender;

    @Mock
    private NotificationRepository notificationRepository;

    @InjectMocks
    private NotificationService notificationService;

    private NotificationRequest request;
    private Notification notification;

    @BeforeEach
    void setUp() {
        request = new NotificationRequest();
        request.setRecipientEmail("test@ex.com");
        request.setTitle("Test Title");
        request.setMessage("Test Body");
        request.setType("TEST");

        notification = new Notification();
        notification.setId(1L);
        notification.setRecipientEmail("test@ex.com");
        notification.setRead(false);
    }

    @Test
    void sendAndSaveNotification_Success() {
        when(notificationRepository.save(any(Notification.class))).thenReturn(notification);

        notificationService.sendAndSaveNotification(request);

        verify(notificationRepository, times(1)).save(any(Notification.class));
        verify(mailSender, times(1)).send(any(SimpleMailMessage.class));
    }

    @Test
    void markAsRead_Success() {
        when(notificationRepository.findById(1L)).thenReturn(Optional.of(notification));
        when(notificationRepository.save(any(Notification.class))).thenReturn(notification);

        notificationService.markAsRead(1L);

        assertTrue(notification.isRead());
        verify(notificationRepository, times(1)).save(notification);
    }

    @Test
    void getUserNotifications_Success() {
        when(notificationRepository.findByRecipientEmailOrderByTimestampDesc("test@ex.com"))
                .thenReturn(List.of(notification));

        List<Notification> result = notificationService.getUserNotifications("test@ex.com");

        assertEquals(1, result.size());
        verify(notificationRepository, times(1)).findByRecipientEmailOrderByTimestampDesc("test@ex.com");
    }
}
