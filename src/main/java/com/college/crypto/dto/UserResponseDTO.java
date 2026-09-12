package com.college.crypto.dto;

public class UserResponseDTO {

    private Long id;
    private String name;
    private String email;
    private String bio;
    private String phoneNumber;
    private String profilePhotoUrl;
    private String themePreference;
    private String userTier;
    private java.time.LocalDateTime joinedAt;
    private java.time.LocalDate dateOfBirth;
    private String country;
    private String gender;
    private boolean isKycVerified;

    public UserResponseDTO() {
    }

    public UserResponseDTO(Long id, String name, String email, String bio, String phoneNumber, 
                           String profilePhotoUrl, String themePreference, String userTier, 
                           java.time.LocalDateTime joinedAt, java.time.LocalDate dateOfBirth,
                           String country, String gender, boolean isKycVerified) {
        this.id = id;
        this.name = name;
        this.email = email;
        this.bio = bio;
        this.phoneNumber = phoneNumber;
        this.profilePhotoUrl = profilePhotoUrl;
        this.themePreference = themePreference;
        this.userTier = userTier;
        this.joinedAt = joinedAt;
        this.dateOfBirth = dateOfBirth;
        this.country = country;
        this.gender = gender;
        this.isKycVerified = isKycVerified;
    }

    public Long getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public String getEmail() { return email; }
    public String getBio() { return bio; }
    public String getPhoneNumber() { return phoneNumber; }
    public String getProfilePhotoUrl() { return profilePhotoUrl; }
    public String getThemePreference() { return themePreference; }
    public String getUserTier() { return userTier; }
    public java.time.LocalDateTime getJoinedAt() { return joinedAt; }
    
    public java.time.LocalDate getDateOfBirth() { return dateOfBirth; }
    public String getCountry() { return country; }
    public String getGender() { return gender; }
    public boolean isKycVerified() { return isKycVerified; }
}