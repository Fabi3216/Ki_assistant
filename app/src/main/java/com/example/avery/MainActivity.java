package com.example.avery;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.speech.RecognizerIntent;
import android.speech.tts.TextToSpeech;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class MainActivity extends AppCompatActivity {



    private TextToSpeech tts;
    private WeatherService weatherService;
    private AppointmentManager appointmentManager;
    private NoteManager noteManager;

    private TextView textGreeting;
    private TextView weatherTextView;
    private ImageView weatherIcon;

    private RecyclerView recyclerView;
    private AppointmentAdapter appointmentAdapter;

    private RecyclerView noteRecyclerView;
    private NoteAdapter noteAdapter;

    private static final String PREFS_NAME = "AveryPrefs";
    private static final String KEY_USERNAME = "username";
    private FusedLocationProviderClient fusedLocationClient;

    @SuppressLint("SetTextI18n")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        textGreeting = findViewById(R.id.text_greeting);
        weatherTextView = findViewById(R.id.text_weather);
        weatherIcon = findViewById(R.id.weather_icon);
        Button speakButton = findViewById(R.id.button_speak);

        recyclerView = findViewById(R.id.recycler_appointments);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setVisibility(View.GONE);

        noteRecyclerView = findViewById(R.id.recycler_notes);
        noteRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        noteRecyclerView.setVisibility(View.GONE);

        weatherTextView.setVisibility(View.GONE);
        weatherIcon.setVisibility(View.GONE);

        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this);

        Animation anim = AnimationUtils.loadAnimation(this, R.anim.fade_in_scale);
        textGreeting.startAnimation(anim);
        speakButton.startAnimation(anim);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.POST_NOTIFICATIONS},
                    2000);
        }

        tts = new TextToSpeech(getApplicationContext(), status -> {
            appointmentManager = new AppointmentManager(this, true);
            noteManager = new NoteManager(this, true);

            // Lade Daten
            refreshAppointments();
            noteAdapter = new NoteAdapter(new ArrayList<>());
            noteRecyclerView.setAdapter(noteAdapter);
            refreshNotes();

            weatherService = new WeatherService(this, tts, weatherTextView, weatherIcon);

            if (status == TextToSpeech.SUCCESS) {
                tts.setLanguage(Locale.GERMAN);
                tts.setSpeechRate(1.2f);

                String name = loadUsername();
                if (name == null) {
                    askForUsername();
                } else {
                    textGreeting.setText("Hallo " + name + " 👋");
                    tts.speak("Hallo " + name + ", wie kann ich dir heute assistieren?", TextToSpeech.QUEUE_FLUSH, null, null);
                }
            }
        });



        speakButton.setOnClickListener(view -> startSpeechRecognition());
    }

    private void refreshAppointments() {
        new Thread(() -> {
            List<Appointment> appointments = appointmentManager.getAppointments();
            runOnUiThread(() -> {
                appointmentAdapter = new AppointmentAdapter(appointments);
                recyclerView.setAdapter(appointmentAdapter);
            });
        }).start();
    }

    private void refreshNotes() {
        new Thread(() -> {
            List<Note> notes = noteManager.getNotes();
            runOnUiThread(() -> noteAdapter.setNotes(notes));
        }).start();
    }

    @SuppressLint("SetTextI18n")
    private void askForUsername() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Wie heißt du?");
        final EditText input = new EditText(this);
        input.setHint("Dein Name");
        builder.setView(input);
        builder.setCancelable(false);

        builder.setPositiveButton("Speichern", (dialog, which) -> {
            String name = input.getText().toString().trim();
            if (!name.isEmpty()) {
                saveUsername(name);
                textGreeting.setText("Hallo " + name + " 👋");
                tts.speak("Schön dich kennenzulernen, " + name + "!", TextToSpeech.QUEUE_FLUSH, null, null);
            } else {
                tts.speak("Kannst du deinen Namen bitte nochmal wiederholen?", TextToSpeech.QUEUE_FLUSH, null, null);
                askForUsername();
            }
        });

        builder.show();
    }

    private void saveUsername(String name) {
        getSharedPreferences(PREFS_NAME, MODE_PRIVATE).edit().putString(KEY_USERNAME, name).apply();
    }

    private String loadUsername() {
        return getSharedPreferences(PREFS_NAME, MODE_PRIVATE).getString(KEY_USERNAME, null);
    }

    private void startSpeechRecognition() {
        Intent intent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
        intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);
        intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE, "de-DE");
        intent.putExtra(RecognizerIntent.EXTRA_PROMPT, "Was möchtest du wissen?");

    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == 1001 && resultCode == RESULT_OK && data != null) {
            ArrayList<String> result = data.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS);
            if (result != null && !result.isEmpty()) {
                String spokenText = result.get(0).toLowerCase();

                if (spokenText.contains("termin speichern")) {
                    appointmentManager.addAppointment(spokenText);
                    refreshAppointments();
                    recyclerView.setVisibility(View.VISIBLE);
                    noteRecyclerView.setVisibility(View.GONE);
                    weatherTextView.setVisibility(View.GONE);
                    weatherIcon.setVisibility(View.GONE);
                    tts.speak("Termin wurde gespeichert.", TextToSpeech.QUEUE_FLUSH, null, null);

                } else if (spokenText.contains("zeige termine")) {
                    recyclerView.setVisibility(View.VISIBLE);
                    noteRecyclerView.setVisibility(View.GONE);
                    weatherTextView.setVisibility(View.GONE);
                    weatherIcon.setVisibility(View.GONE);
                    refreshAppointments();
                    tts.speak("Hier sind deine Termine.", TextToSpeech.QUEUE_FLUSH, null, null);

                } else if (spokenText.contains("wetter")) {
                    getWeatherLocation();
                    recyclerView.setVisibility(View.GONE);
                    noteRecyclerView.setVisibility(View.GONE);

                } else if (spokenText.contains("notiz speichern")) {
                    noteManager.addNote(spokenText);
                    refreshNotes();
                    recyclerView.setVisibility(View.GONE);
                    noteRecyclerView.setVisibility(View.VISIBLE);
                    weatherTextView.setVisibility(View.GONE);
                    weatherIcon.setVisibility(View.GONE);
                    tts.speak("Notiz gespeichert.", TextToSpeech.QUEUE_FLUSH, null, null);

                } else if (spokenText.contains("zeige notizen")) {
                    refreshNotes();
                    recyclerView.setVisibility(View.GONE);
                    noteRecyclerView.setVisibility(View.VISIBLE);
                    weatherTextView.setVisibility(View.GONE);
                    weatherIcon.setVisibility(View.GONE);
                    tts.speak("Hier sind deine Notizen.", TextToSpeech.QUEUE_FLUSH, null, null);

                } else if (spokenText.contains("lösche notizen")) {
                    noteManager.clearNotes();
                    refreshNotes();
                    noteRecyclerView.setVisibility(View.GONE);
                    tts.speak("Alle Notizen wurden gelöscht.", TextToSpeech.QUEUE_FLUSH, null, null);

                } else {
                    tts.speak("Das habe ich leider nicht verstanden.", TextToSpeech.QUEUE_FLUSH, null, null);
                }
            } else {
                tts.speak("Ich konnte dich leider nicht verstehen.", TextToSpeech.QUEUE_FLUSH, null, null);
            }
        }
    }

    @Override
    protected void onDestroy() {
        if (tts != null) {
            tts.stop();
            tts.shutdown();
        }
        super.onDestroy();
    }

    private void getWeatherLocation() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, 1002);
            return;
        }

        fusedLocationClient.getLastLocation().addOnSuccessListener(this, location -> {
            if (location != null) {
                double lat = location.getLatitude();
                double lon = location.getLongitude();
                weatherService.getWeatherByLocation(lat, lon);
            } else {
                tts.speak("Ich konnte deinen Standort leider nicht ermitteln.", TextToSpeech.QUEUE_FLUSH, null, null);
            }
        });
    }
}
