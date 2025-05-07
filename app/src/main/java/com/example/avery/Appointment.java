package com.example.avery;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "appointments")
public class Appointment {


    @PrimaryKey(autoGenerate = true)
    public int id;

    public String description;
    public long timestamp;

    public Appointment(String description,long timestamp) {
            this.description = description;
            this.timestamp = timestamp;
        }
    }

