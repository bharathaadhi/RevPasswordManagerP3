package com.rev.vaultservice.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "vault_entries")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class VaultEntry {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private Long userId;

    private String platform;

    private String username;

    private String encryptedPassword;

    private String category;

    private boolean favorite;
    private String strength;

    @org.hibernate.annotations.CreationTimestamp
    @Column(updatable = false)
    private java.time.LocalDateTime createdAt;

    @org.hibernate.annotations.UpdateTimestamp
    private java.time.LocalDateTime updatedAt;
}