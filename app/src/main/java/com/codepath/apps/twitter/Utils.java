package com.codepath.apps.twitter;

import android.app.Activity;
import android.support.v4.content.res.ResourcesCompat;
import android.text.format.DateUtils;
import android.widget.ImageView;
import android.widget.TextView;

import com.devs.vectorchildfinder.VectorChildFinder;
import com.devs.vectorchildfinder.VectorDrawableCompat;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class Utils {
    // getRelativeTimeAgo("Mon Apr 01 21:16:23 +0000 2014");
    public static String getRelativeTimeAgo(String rawJsonDate) {
        String twitterFormat = "EEE MMM dd HH:mm:ss ZZZZZ yyyy";
        SimpleDateFormat sf = new SimpleDateFormat(twitterFormat, Locale.ENGLISH);
        sf.setLenient(true);

        String relativeDate = "";
        try {
            long dateMillis = sf.parse(rawJsonDate).getTime();

            long now = (new Date()).getTime();
            long between = now - dateMillis;
            int hoursBetween = (int) ((between / (1000 * 60 * 60)));
            int yearsBetween = (new Date(now)).getYear() - (new Date(dateMillis)).getYear();

            if (yearsBetween >= 1) {
                SimpleDateFormat moreThanAYear = new SimpleDateFormat("MMM d yyyy", Locale.US);
                relativeDate = moreThanAYear.format(dateMillis);
            } else if (hoursBetween >= 24) {
                SimpleDateFormat moreThanADay = new SimpleDateFormat("MMM d", Locale.US);
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

    public static void changeColor(Activity activity, ImageView iv, TextView tv, int colorId, int drawableId, boolean isColorId) {
        VectorChildFinder vector = new VectorChildFinder(activity, drawableId, iv);
        VectorDrawableCompat.VFullPath path = vector.findPathByName("path1");
        int color = (isColorId) ? colorFromId(activity, colorId) : colorId;
        path.setFillColor(color);
        if (tv != null) tv.setTextColor(color);
    }

    public static int colorFromId(Activity activity, int id) {
        return ResourcesCompat.getColor(activity.getResources(), id, null);
    }
}
