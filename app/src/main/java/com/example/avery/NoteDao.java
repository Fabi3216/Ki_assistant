package com.example.avery;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Delete;

import java.util.List;

@Dao
public interface NoteDao {
    @Insert
    void insert(Note note);

    @Query("SELECT * FROM note")
    List<Note> getAllNotes();

    @Query("DELETE FROM note")
    void deleteAllNotes();
}
