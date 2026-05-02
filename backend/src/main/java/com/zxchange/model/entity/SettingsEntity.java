package com.zxchange.model.entity;

import jakarta.persistence.*;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(name = "settings")
public class SettingsEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Column(name = "key_name", nullable = false, unique = true)
    private String key;
    @Column(nullable = false)
    private String value;
    @Column(name = "is_encrypted")
    private boolean encrypted;
    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    public SettingsEntity() {}
    public SettingsEntity(Long id, String key, String value, boolean encrypted, LocalDateTime updatedAt) {
        this.id = id; this.key = key; this.value = value; this.encrypted = encrypted; this.updatedAt = updatedAt;
    }
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getKey() { return key; }
    public void setKey(String key) { this.key = key; }
    public String getValue() { return value; }
    public void setValue(String value) { this.value = value; }
    public boolean isEncrypted() { return encrypted; }
    public void setEncrypted(boolean encrypted) { this.encrypted = encrypted; }
    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }
}
