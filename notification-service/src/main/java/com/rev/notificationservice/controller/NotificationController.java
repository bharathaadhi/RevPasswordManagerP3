package com.rev.notificationservice.controller;

import com.rev.notificationservice.dto.NotificationRequest;
import com.rev.notificationservice.entity.Notification;
import com.rev.notificationservice.service.NotificationService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/notifications")
@RequiredArgsConstructor
public class NotificationController {

    private final NotificationService notificationService;

    @PostMapping("/send")
    public ResponseEntity<String> sendNotification(@RequestBody NotificationRequest request) {
        notificationService.sendAndSaveNotification(request);
        return ResponseEntity.ok("Notification processed");
    }

    @GetMapping("/user/{email:.+}")
    public ResponseEntity<List<Notification>> getUserNotifications(@PathVariable("email") String email) {
        return ResponseEntity.ok(notificationService.getUserNotifications(email));
    }

    @GetMapping("/user/{email:.+}/unread")
    public ResponseEntity<List<Notification>> getUnreadNotifications(@PathVariable("email") String email) {
        return ResponseEntity.ok(notificationService.getUnreadNotifications(email));
    }

    @PutMapping("/{id}/read")
    public ResponseEntity<String> markAsRead(@PathVariable("id") Long id) {
        notificationService.markAsRead(id);
        return ResponseEntity.ok("Marked as read");
    }

    @PostMapping("/breach")
    public ResponseEntity<String> notifyBreach(@RequestParam String email, @RequestParam String affectedAccount) {
        notificationService.notifyBreach(email, affectedAccount);
        return ResponseEntity.ok("Breach notification sent");
    }
}
