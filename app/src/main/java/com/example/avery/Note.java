package com.example.avery;

import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity
public class Note {
    @PrimaryKey(autoGenerate = true)
    public int id;

    public String text;

    public Note(String text) {
        this.text = text;
    }
}
