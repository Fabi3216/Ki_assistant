package com.example.avery;

import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;
import java.util.List;

@Dao
public interface AppointmentDao {

    @Query("SELECT * FROM appointments")
    List<Appointment> getAllAppointments();

    @Insert
    void insert(Appointment appointment);

    @Delete
    void delete(Appointment appointment);

    @Query("SELECT * FROM appointments WHERE description = :desc")
    Appointment findByDescription(String desc);
}
