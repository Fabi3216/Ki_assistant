package com.example.avery;

import android.content.Context;

import androidx.room.Room;
import androidx.room.migration.Migration;
import androidx.sqlite.db.SupportSQLiteDatabase;

import java.util.Comparator;
import java.util.List;

public class AppointmentManager {

    private AppointmentDao appointmentDao;
    private static AppDatabase db;

    public AppointmentManager(Context context, boolean b) {
        db = Room.databaseBuilder(
                        context.getApplicationContext(),
                        AppDatabase.class,
                        "avery_database"
                )

                .fallbackToDestructiveMigration() // Optional: Datenbank löschen, wenn Migration fehlt (für Entwicklung)
                .build();

        appointmentDao = db.appointmentDao();
    }

    // Migration von Version 1 → 2: Neue Notizen-Tabelle
    public static final Migration MIGRATION_1_2 = new Migration(1, 2) {
        @Override
        public void migrate(SupportSQLiteDatabase database) {
            database.execSQL("CREATE TABLE IF NOT EXISTS `notes` (`id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, `text` TEXT)");
        }
    };

    // Termin hinzufügen
    public void addAppointment(String description) {
        long timestamp = DateParser.parseDateFromText(description);
        String bereinigt = DateParser.cleanDescription(description);

        Appointment newAppointment = new Appointment(bereinigt, timestamp);
        appointmentDao.insert(newAppointment);
    }

    // Termin löschen (nach Beschreibung)
    public void deleteAppointmentByDescription(String description) {
        Appointment existing = appointmentDao.findByDescription(description);
        if (existing != null) {
            appointmentDao.delete(existing);
        }
    }

    // Alle Termine geordnet zurückgeben
    public List<Appointment> getAppointments() {
        List<Appointment> list = appointmentDao.getAllAppointments();
        list.sort(Comparator.comparingLong(a -> a.timestamp));  // Nach Zeit sortieren
        return list;
    }

    // Gibt true zurück, wenn mindestens ein Termin existiert
    public boolean hasAppointments() {
        return !getAppointments().isEmpty();
    }

    // Für RecyclerView-Adapter oder andere direkte UI-Nutzung
    public static AppDatabase getDatabase() {
        return db;
    }
}
