package com.example.avery;

import java.util.Calendar;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class DateParser {

    public static long parseDateFromText(String text) {
        Calendar calendar = Calendar.getInstance();
        boolean datumGefunden = false;
        boolean uhrzeitGefunden = false;

        // Standard: heute
        if (text.contains("morgen")) {
            calendar.add(Calendar.DAY_OF_YEAR, 1);
        } else if (text.contains("übermorgen")) {
            calendar.add(Calendar.DAY_OF_YEAR, 2);
        } else if (text.contains("heute")) {
            // nichts tun
        } else if (text.contains("eine woche")) {
            calendar.add(Calendar.DAY_OF_WEEK,7);

        }

        // Uhrzeit erkennen: z. B. "um 14 Uhr"
        Pattern pattern = Pattern.compile("um (\\d{1,2}) ?uhr");
        Matcher matcher = pattern.matcher(text);

        if (matcher.find()) {
            int hour = Integer.parseInt(matcher.group(1));
            calendar.set(Calendar.HOUR_OF_DAY, hour);
            calendar.set(Calendar.MINUTE, 0);
        } else {
            // keine Uhrzeit erkannt → nur Datum verwenden (0 Uhr)
            calendar.set(Calendar.HOUR_OF_DAY, 0);
            calendar.set(Calendar.MINUTE, 0);
        }

        calendar.set(Calendar.SECOND, 0);
        calendar.set(Calendar.MILLISECOND, 0);

        if (!datumGefunden && !uhrzeitGefunden) {
            return -1;
        }

        return calendar.getTimeInMillis();
    }

    //filtern von zeitangaben
    public static String cleanDescription(String text) {
        String[] muster = {
                "heute", "morgen", "übermorgen",
                "um \\d{1,2} ?uhr"
        };
        for (String regex : muster) {
            text = text.replace(regex, "").trim();
        }

        text = text.replaceAll(" +", " ");

        return text;
    }
}





