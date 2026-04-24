package com.smartcontact.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.persistence.*;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "contacts")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Contact {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @JsonProperty(access = JsonProperty.Access.READ_ONLY)
    private Long id;
    
    @NotBlank(message = "Name is required")
    @Column(name = "name")
    private String name;
    
    @NotBlank(message = "Phone is required")
    @Column(name = "phone")
    private String phone;
    
    @Email(message = "Invalid email format")
    @Column(name = "email")
    private String email;
    
    @Column(name = "category")
    private String category; // Work, Family, Friends
    
    @Column(name = "notes", length = 1000)
    private String notes;
    
    @Column(name = "is_favorite")
    private boolean isFavorite;
    
    @Column(name = "created_at")
    private LocalDateTime createdAt;
    
    @Column(name = "last_contacted")
    private LocalDateTime lastContacted;
    
    @Transient
    private int relationshipScore; // 0-100
    
    @Transient
    private String relationshipStrength; // Strong, Medium, Weak
}
