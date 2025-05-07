package com.example.avery;

import android.content.Context;
import android.speech.tts.TextToSpeech;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.BufferedInputStream;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Scanner;

public class WeatherService {

    private Context context;
    private TextToSpeech tts;
    private TextView weatherTextView;
    private ImageView weatherIcon;
    private final String apiKey = "1e5b39026a93b9e0d043be6e06064fb2"; // OpenWeatherMap API

    public WeatherService(Context context, TextToSpeech tts, TextView weatherTextView, ImageView weatherIcon) {
        this.context = context;
        this.tts = tts;
        this.weatherTextView = weatherTextView;
        this.weatherIcon = weatherIcon;
    }

    public void getWeatherByLocation(double lat, double lon) {
        Thread thread = new Thread(() -> {
            try {
                String urlString = "https://api.openweathermap.org/data/2.5/weather?lat=" + lat + "&lon=" + lon + "&appid=" + apiKey + "&units=metric&lang=de";
                URL url = new URL(urlString);
                HttpURLConnection urlConnection = (HttpURLConnection) url.openConnection();

                try {
                    InputStream in = new BufferedInputStream(urlConnection.getInputStream());
                    Scanner scanner = new Scanner(in);
                    StringBuilder response = new StringBuilder();
                    while (scanner.hasNext()) {
                        response.append(scanner.nextLine());
                    }

                    JSONObject jsonObject = new JSONObject(response.toString());
                    JSONObject main = jsonObject.getJSONObject("main");
                    double temperature = main.getDouble("temp");

                    JSONArray weatherArray = jsonObject.getJSONArray("weather");
                    String description = weatherArray.getJSONObject(0).getString("description");
                    String cityName = jsonObject.getString("name");

                    String weatherMessage = "In " + cityName + " sind es aktuell " + temperature + " Grad mit " + description + ".";

                    ((MainActivity) context).runOnUiThread(() -> {
                        tts.speak(weatherMessage, TextToSpeech.QUEUE_FLUSH, null, null);
                        weatherTextView.setText(weatherMessage);
                        weatherIcon.setVisibility(View.VISIBLE);

                        // 🔽 Icon dynamisch setzen
                        if (description.contains("regen")) {
                            weatherIcon.setImageResource(R.drawable.ic_rain);
                        } else if (description.contains("wolk")) {
                            weatherIcon.setImageResource(R.drawable.ic_cloud);
                        } else if (description.contains("sonn") || description.contains("klar")) {
                            weatherIcon.setImageResource(R.drawable.ic_sun);
                        } else {
                            weatherIcon.setImageResource(R.drawable.ic_cloud); // Standard
                        }
                    });

                } finally {
                    urlConnection.disconnect();
                }
            } catch (Exception e) {
                e.printStackTrace();
                ((MainActivity) context).runOnUiThread(() ->
                        Toast.makeText(context, "Fehler beim Abrufen des Wetters", Toast.LENGTH_SHORT).show()
                );
            }
        });
        thread.start();
    }
}
