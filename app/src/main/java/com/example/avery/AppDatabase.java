package com.example.avery;

import androidx.room.Database;
import androidx.room.RoomDatabase;

@Database(entities = {Appointment.class, Note.class}, version = 2, exportSchema = false)
public abstract class AppDatabase extends RoomDatabase {
    public abstract AppointmentDao appointmentDao();
    public abstract NoteDao noteDao();
}
