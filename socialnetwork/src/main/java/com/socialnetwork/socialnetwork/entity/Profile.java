package com.socialnetwork.socialnetwork.entity;

import com.socialnetwork.socialnetwork.enums.IsepSpecialization;
import com.socialnetwork.socialnetwork.enums.UserGender;
import io.hypersistence.utils.hibernate.type.json.JsonBinaryType;
import jakarta.persistence.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.Type;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Map;
import java.util.UUID;

@Entity
@Table(name = "profile")
public class Profile {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @OneToOne
    @JoinColumn(name = "user_id", unique = true, nullable = false)
    private User user;

    @Column(length = 100)
    private String location;

    @Column(length = 255)
    private String website;

    @Column(name = "phone_number", length = 20)
    private String phoneNumber;

    private LocalDate birthdate;

    @Enumerated(EnumType.STRING)
    private UserGender gender;

    @Column(length = 100)
    private String profession;

    @Column(length = 100)
    private String company;

    @Column(length = 255)
    private String education;

    @Enumerated(EnumType.STRING)
    private IsepSpecialization specialization;

    @Column(name = "promo_year")
    private Short promoYear;

    @Type(JsonBinaryType.class)
    @Column(columnDefinition = "jsonb")
    private Map<String, Object> interests;

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    // Getters and setters

    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }

    public String getLocation() {
        return location;
    }

    public void setLocation(String location) {
        this.location = location;
    }

    public String getWebsite() {
        return website;
    }

    public void setWebsite(String website) {
        this.website = website;
    }

    public String getPhoneNumber() {
        return phoneNumber;
    }

    public void setPhoneNumber(String phoneNumber) {
        this.phoneNumber = phoneNumber;
    }

    public LocalDate getBirthdate() {
        return birthdate;
    }

    public void setBirthdate(LocalDate birthdate) {
        this.birthdate = birthdate;
    }

    public UserGender getGender() {
        return gender;
    }

    public void setGender(UserGender gender) {
        this.gender = gender;
    }

    public String getProfession() {
        return profession;
    }

    public void setProfession(String profession) {
        this.profession = profession;
    }

    public String getCompany() {
        return company;
    }

    public void setCompany(String company) {
        this.company = company;
    }

    public String getEducation() {
        return education;
    }

    public void setEducation(String education) {
        this.education = education;
    }

    public IsepSpecialization getSpecialization() {
        return specialization;
    }

    public void setSpecialization(IsepSpecialization specialization) {
        this.specialization = specialization;
    }

    public Short getPromoYear() {
        return promoYear;
    }

    public void setPromoYear(Short promoYear) {
        this.promoYear = promoYear;
    }

    public Map<String, Object> getInterests() {
        return interests;
    }

    public void setInterests(Map<String, Object> interests) {
        this.interests = interests;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }
}
