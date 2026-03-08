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
}