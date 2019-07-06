package com.codepath.apps.twitter.models;

import android.util.Log;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

public class Tweet {
    public String body;
    public long uid;
    public User user;
    public String createdAt;
    public int numRetweets, numFavorites;
    public boolean retweeted, favorited;
    public ArrayList<String> imageUrls;
    public int onColor, offColor;

    public static Tweet fromJSON(JSONObject obj) throws JSONException {
        Tweet t = new Tweet();

        t.body = obj.getString("text");
        t.uid = obj.getLong("id");
        t.createdAt = obj.getString("created_at");
        t.user = User.fromJSON(obj.getJSONObject("user"));
        t.numRetweets = obj.getInt("retweet_count");
        t.numFavorites = obj.getInt("favorite_count");
        t.retweeted = obj.getBoolean("retweeted");
        t.favorited = obj.getBoolean("favorited");

        // Try to get image
        t.imageUrls = new ArrayList<>();
        try {
            JSONArray media = obj.getJSONObject("entities").getJSONArray("media");
            for (int i = 0; i < media.length(); i++) {
                String url = media.getJSONObject(i).getString("media_url_https");
                t.imageUrls.add(url);
            }
        } catch (JSONException e) {
            Log.i("TweetJSON", "No media found on @" + t.user.screenName + ": " + ((t.body.length() > 20) ? t.body.substring(0, 20) : t.body));
        }

        return t;
    }


    public boolean hasImages() {
        return imageUrls.size() > 0;
    }
}
