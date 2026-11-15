package com.stylesync.model;

import org.bson.types.ObjectId;
import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.Objects;

/**
 * User model class representing a registered user
 * Demonstrates ENCAPSULATION with private fields and public getters/setters
 */
public class User implements Serializable {
    private static final long serialVersionUID = 1L;

    private ObjectId id;
    private String userId; // String representation for easy use
    private String name;
    private String email;
    private String passwordHash;
    private LocalDateTime createdAt;
    private LocalDateTime lastLogin;

    // Default constructor
    public User() {
        this.createdAt = LocalDateTime.now();
    }

    // Parameterized constructor
    public User(String name, String email, String passwordHash) {
        this();
        this.name = name;
        this.email = email;
        this.passwordHash = passwordHash;
    }

    // Full constructor
    public User(ObjectId id, String name, String email, String passwordHash, 
                LocalDateTime createdAt, LocalDateTime lastLogin) {
        this.id = id;
        this.userId = id != null ? id.toHexString() : null;
        this.name = name;
        this.email = email;
        this.passwordHash = passwordHash;
        this.createdAt = createdAt != null ? createdAt : LocalDateTime.now();
        this.lastLogin = lastLogin;
    }

    // Getters and Setters - ENCAPSULATION
    public ObjectId getId() {
        return id;
    }

    public void setId(ObjectId id) {
        this.id = id;
        this.userId = id != null ? id.toHexString() : null;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
        if (userId != null && !userId.isEmpty()) {
            this.id = new ObjectId(userId);
        }
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email.toLowerCase().trim();
    }

    public String getPasswordHash() {
        return passwordHash;
    }

    public void setPasswordHash(String passwordHash) {
        this.passwordHash = passwordHash;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public LocalDateTime getLastLogin() {
        return lastLogin;
    }

    public void setLastLogin(LocalDateTime lastLogin) {
        this.lastLogin = lastLogin;
    }

    // Business methods
    public void updateLastLogin() {
        this.lastLogin = LocalDateTime.now();
    }

    // equals and hashCode for proper collection behavior
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        User user = (User) o;
        return Objects.equals(email, user.email);
    }

    @Override
    public int hashCode() {
        return Objects.hash(email);
    }

    @Override
    public String toString() {
        return "User{" +
                "userId='" + userId + '\'' +
                ", name='" + name + '\'' +
                ", email='" + email + '\'' +
                ", createdAt=" + createdAt +
                '}';
    }
}
