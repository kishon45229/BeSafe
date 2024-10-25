package com.project.BeSafe;

import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "notifications")
public class NotificationEntity {
    @PrimaryKey(autoGenerate = true)
    private int id;
    private String incidentDetails;

    public NotificationEntity(String incidentDetails) {
        this.incidentDetails = incidentDetails;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getIncidentDetails() {
        return incidentDetails;
    }

    public void setIncidentDetails(String incidentDetails) {
        this.incidentDetails = incidentDetails;
    }
}

