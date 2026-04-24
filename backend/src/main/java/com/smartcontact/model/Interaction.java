package com.smartcontact.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "interactions")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Interaction {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @JsonProperty(access = JsonProperty.Access.READ_ONLY)
    private Long id;
    
    @Column(name = "contact_id")
    private Long contactId;
    
    @Column(name = "type")
    private String type; // e.g., "CALL", "EMAIL", "MEETING"
    
    @Column(name = "interaction_date")
    private LocalDateTime date;
}
