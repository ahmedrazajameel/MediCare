package com.medical.medicarepro.models;

public class User {
    private String id;
    private String name;
    private String email;
    private String location;
    private String role;
    private String profileImage;
    private String username;
    private String password;
    private String phone;
    private boolean isActive;
    private long createdAt;

    public User() {
    }

    // Getters
    public String getId() { return id; }
    public String getName() { return name; }
    public String getEmail() { return email; }
    public String getLocation() { return location; }
    public String getRole() { return role; }
    public String getProfileImage() { return profileImage; }
    public String getUsername() { return username; }
    public String getPassword() { return password; }
    public String getPhone() { return phone; }
    public boolean isActive() { return isActive; }
    public long getCreatedAt() { return createdAt; }

    // Setters
    public void setId(String id) { this.id = id; }
    public void setName(String name) { this.name = name; }
    public void setEmail(String email) { this.email = email; }
    public void setLocation(String location) { this.location = location; }
    public void setRole(String role) { this.role = role; }
    public void setProfileImage(String profileImage) { this.profileImage = profileImage; }
    public void setUsername(String username) { this.username = username; }
    public void setPassword(String password) { this.password = password; }
    public void setPhone(String phone) { this.phone = phone; }
    public void setActive(boolean active) { isActive = active; }
    public void setCreatedAt(long createdAt) { this.createdAt = createdAt; }
}