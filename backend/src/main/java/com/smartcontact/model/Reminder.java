package com.smartcontact.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "reminders")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Reminder {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @JsonProperty(access = JsonProperty.Access.READ_ONLY)
    private Long id;
    
    @Column(name = "contact_id")
    private Long contactId;
    
    @Column(name = "message")
    private String message;
    
    @Column(name = "reminder_date")
    private LocalDateTime reminderDate;
    
    @Column(name = "status")
    private String status; // PENDING, COMPLETED
}
