package com.codepath.apps.twitter;

import android.text.format.DateUtils;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class StringUtils {
    // getRelativeTimeAgo("Mon Apr 01 21:16:23 +0000 2014");
    public static String getRelativeTimeAgo(String rawJsonDate) {
        String twitterFormat = "EEE MMM dd HH:mm:ss ZZZZZ yyyy";
        SimpleDateFormat sf = new SimpleDateFormat(twitterFormat, Locale.ENGLISH);
        sf.setLenient(true);

        String relativeDate = "";
        try {
            long dateMillis = sf.parse(rawJsonDate).getTime();

            long now = (new Date()).getTime();
            long hoursBetween = now - dateMillis;
            hoursBetween = (int) ((hoursBetween / (1000 * 60 * 60)));

            if (hoursBetween >= 24) {
                SimpleDateFormat moreThanADay = new SimpleDateFormat("MMM dd", Locale.US);
                relativeDate = moreThanADay.format(dateMillis);
            } else {
                String rawRelativeDate = DateUtils.getRelativeTimeSpanString(dateMillis, System.currentTimeMillis(), DateUtils.SECOND_IN_MILLIS).toString();
                String[] parts = rawRelativeDate.split(" ");
                relativeDate = parts[0];
                switch (parts[1]) {
                    case "second":
                    case "seconds":
                        relativeDate += "s";
                        break;
                    case "minute":
                    case "minutes":
                        relativeDate += "m";
                        break;
                    case "hour":
                    case "hours":
                        relativeDate += "h";
                        break;
                    case "day":
                    case "days":
                        relativeDate += "d";
                        break;
                    case "month":
                    case "months":
                        relativeDate += "mo";
                        break;
                    case "year":
                    case "years":
                        relativeDate += "y";
                        break;
                    default:
                        relativeDate = rawRelativeDate;
                        break;
                }
            }
        } catch (ParseException e) {
            e.printStackTrace();
        }

        return relativeDate;
    }

    public static String ellipsize(String str, int maxChars) {
        if (str.length() > maxChars) {
            String ellipsized = str.substring(0, maxChars - 3);
            ellipsized += "...";
            return ellipsized;
        } else {
            return str;
        }
    }
}
