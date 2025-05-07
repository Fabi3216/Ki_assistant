package com.example.avery;

import android.content.Context;

import java.util.ArrayList;
import java.util.List;

public class NoteManager {

    private final NoteDao noteDao;

    public NoteManager(Context context, boolean b) {
        // Gemeinsame Datenbank-Instanz aus AppointmentManager holen
        AppDatabase db = AppointmentManager.getDatabase();
        this.noteDao = db.noteDao();
    }

    // Neue Notiz speichern
    public void addNote(String text) {
        noteDao.insert(new Note(text));
    }

    // Alle Notizen als Strings zurückgeben
    public List<Note> getNotes() {
        return noteDao.getAllNotes();
    }


    // Alle Notizen löschen
    public void clearNotes() {
        noteDao.deleteAllNotes();
    }
    public interface NoteCallback {
        void onNotesLoaded(List<String> notes);
    }
    public void getNotesAsync(NoteCallback callback) {
        new Thread(() -> {
            List<Note> notes = noteDao.getAllNotes();
            List<String> result = new ArrayList<>();
            for (Note n : notes) {
                result.add(n.text);
            }

            // Callback im UI-Thread aufrufen
            new android.os.Handler(android.os.Looper.getMainLooper()).post(() -> {
                callback.onNotesLoaded(result);
            });
        }).start();
    }


}
