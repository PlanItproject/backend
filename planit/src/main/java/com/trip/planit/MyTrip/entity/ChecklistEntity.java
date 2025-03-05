package com.trip.planit.MyTrip.entity;

import com.trip.planit.MyTrip.dto.ChecklistItemType;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Data;

@Entity
@Data
@Table(name = "checklist")
public class ChecklistEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long Id;
    private String country;

    @Enumerated(EnumType.STRING)
    private ChecklistItemType type;
    private String description;
    private boolean isChecked = false;
}
