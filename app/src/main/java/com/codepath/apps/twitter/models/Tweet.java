package com.codepath.apps.twitter.models;

import org.json.JSONException;
import org.json.JSONObject;

public class Tweet {
    public String body;
    public long uid;
    public User user;
    public String createdAt;

    public static Tweet fromJSON(JSONObject obj) throws JSONException {
        Tweet t = new Tweet();
        t.body = obj.getString("text");
        t.uid = obj.getLong("id");
        t.createdAt = obj.getString("created_at");
        t.user = User.fromJSON(obj.getJSONObject("user"));
        return t;
    }
}
